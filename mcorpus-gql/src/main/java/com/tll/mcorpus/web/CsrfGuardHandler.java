package com.tll.mcorpus.web;

import static com.tll.core.Util.emptyIfNull;
import static com.tll.core.Util.isNull;
import static com.tll.core.Util.neclean;
import static com.tll.core.Util.not;
import static com.tll.mcorpus.web.RequestUtil.getOrCreateRequestSnapshot;
import static com.tll.mcorpus.web.RequestUtil.setRstCookie;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import com.google.common.reflect.TypeToken;
import com.tll.web.RequestSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Anti-CSRF statelessly by comparing the http header 'rst' (request sync token)
 * value to the rst cookie value both contained in a received http request.
 * <p>
 * This is a variant on the double submit cookie method to thwart CSRF attacks.
 *
 * @author jpk
 */
public class CsrfGuardHandler implements Handler {

  public static final class RST {
    public final String rst;
    public RST(final String rst) { this.rst = rst; }
  }

  public static final String RST_TOKEN_NAME = "rst";

  @SuppressWarnings("serial")
  public static final TypeToken<RST> RST_TYPE = new TypeToken<RST>() {};

  static final Pattern PTRN_NO_RST = Pattern.compile("^.*\\.(js|css|png|gif|jpg|jpeg|mpeg|ico)$", Pattern.CASE_INSENSITIVE);

  private static final SecretKey SKEY;
  private static final byte[] MSG_PREFIX;

  static {
    try {
      SKEY = KeyGenerator.getInstance("AES").generateKey();
      MSG_PREFIX = new byte[32];
      new SecureRandom().nextBytes(MSG_PREFIX);
    }
    catch(Exception e) {
      throw new Error();
    }
  }

  static boolean doRstCheck(final Context ctx) {
    return ctx.getRequest().getMethod().isPost();
  }

  static boolean doNextRst(final Context ctx) {
    return not(PTRN_NO_RST.matcher(emptyIfNull(ctx.getRequest().getPath())).matches());
  }

  /**
   * Generate an RST.
   * <p>
   * FORMAT: <code>"HMAC({MSG_PREFIX} + {timestamp})|{timstamp}"</code>
   *
   * @param ts timestamp
   *
   * @return Newly generated RST
   */
  static String genRst(long ts) {
    try {
      final Mac mac = Mac.getInstance("HMACSHA256");
      final String tstr = Long.toString(ts);
      mac.init(SKEY);
      mac.update(MSG_PREFIX);
      mac.update(tstr.getBytes(StandardCharsets.UTF_8));
      final String macstr = Base64.getUrlEncoder().encodeToString(mac.doFinal());
      final String rst = macstr + "|" + tstr;
      return rst;
    } catch(NoSuchAlgorithmException e) {
      throw new Error();
    } catch(InvalidKeyException e) {
      throw new Error();
    }
  }

  static String[] parseRst(final String rst) {
    try {
      int si = rst.lastIndexOf("|");
      return new String[] {
        rst.substring(0, si),
        rst.substring(si + 1)
      };
    } catch(Exception e) {
      return null;
    }
  }

  private final Logger log = LoggerFactory.getLogger(CsrfGuardHandler.class);

  private final long rstTtlInSeconds;

  /**
   * Constructor.
   *
   * @param rstTtlInSeconds the time to live for an RST in seconds
   */
  public CsrfGuardHandler(long rstTtlInSeconds) {
    this.rstTtlInSeconds = rstTtlInSeconds;
  }

  @Override
  public void handle(final Context ctx) throws Exception {

    if(doRstCheck(ctx)) {
      final RequestSnapshot requestSnapshot = getOrCreateRequestSnapshot(ctx);
      final String cookieRst = neclean(requestSnapshot.getRstCookie());
      final String headerRst = neclean(requestSnapshot.getRstHeader());

      // send a no content response if *both* the cookie and header rst are not present
      // this serves as a way for the clients to sync up and issue a valid subsequent request
      if(isNull(cookieRst) && isNull(headerRst)) {
        final String nextRst = genRst(System.currentTimeMillis());
        log.warn("No request sync tokens present in request.  Re-setting with short-lived token: {}.", nextRst);
        setRstCookie(ctx, nextRst, 120, "/"); // you got 2 mins to re-request
        ctx.getResponse().getHeaders().add(RST_TOKEN_NAME, nextRst);
        ctx.clientError(205); // 205 - Reset Content
        return;
      }

      // one of expected 2 rst(s) not present
      if(isNull(cookieRst) || isNull(headerRst)) {
        log.error("Request sync token(s) missing in request: cookie: {}, header: {}.", requestSnapshot.getRstCookie(), requestSnapshot.getRstHeader());
        ctx.clientError(400); // bad request
        return;
      }

      // rst match verify (both are non-null)
      if(not(Objects.equals(headerRst, cookieRst))) {
        log.error("Request sync token mismatch in request: cookie: {}, header {}.", requestSnapshot.getRstCookie(), requestSnapshot.getRstHeader());
        ctx.clientError(400); // bad request
        return;
      }

      // rst MAC verification
      try {
        final String[] parsedRst = parseRst(cookieRst);
        final long extractedTs = Long.valueOf(parsedRst[1]);

        // verify mac integrity
        final String generatedRst = genRst(extractedTs);
        if(isNull(generatedRst) || not(generatedRst.equals(cookieRst)))
          throw new Exception();

        // verify timestamp hasnt gone beyond the configured time to live
        if(Duration.between(
            Instant.ofEpochMilli(extractedTs),
            Instant.now()
        ).getSeconds() > this.rstTtlInSeconds) {
          log.error("Request sync token expired: {}", cookieRst);
          ctx.clientError(400); // bad request
          return;
        }
      } catch(Exception e) {
        log.error("Request sync token MAC verify error: {}", cookieRst);
        ctx.clientError(400); // bad request
        return;
      }

      // rst now verified
      log.info("Request sync tokens {} verified for incoming http request.", requestSnapshot.getRstCookie());
    }

    if(doNextRst(ctx)) {
      final String nextRst = genRst(System.currentTimeMillis());
      setRstCookie(ctx, nextRst, (int) rstTtlInSeconds, "/");
      ctx.getResponse().getHeaders().add(RST_TOKEN_NAME, nextRst);
      ctx.getRequest().add(RST_TYPE, new RST(nextRst)); // make next rst available downstream
      log.info("Request sync token {} added to http response.", nextRst);
    }

    ctx.next();
  }

}
