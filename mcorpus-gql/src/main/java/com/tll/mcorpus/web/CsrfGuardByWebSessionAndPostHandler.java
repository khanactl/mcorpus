package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.clean;
import static com.tll.mcorpus.Util.not;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.mcorpus.web.WebSessionManager.WebSession;
import com.tll.mcorpus.web.WebSessionManager.WebSessionInstance;

import ratpack.form.Form;
import ratpack.handling.Context;
import ratpack.handling.Handler;

/**
 * Anti-CSRF by stateful server-side web sessions and rst in post checking.
 * <p>
 * Verifies the rst in an incoming post request against the server held web
 * session rst then adds the processed form data as a {@link Form} instance to
 * the current request object held in the given context.
 * <p>
 * If the incoming form post rst does not match the server web session held rst,
 * a client error is issued as a response.
 * <p>
 * When the rst matches that held in the server side session, this handler will
 * reset the rst and cache the client-bound 'next' rst in the request under
 * type: {@link NextRst} for downstream handler to access and possibly inject
 * into the response.
 * <p>
 * Form data is processed and cached in the request as well as a {@link Form}
 * object. This is what the downsteam handlers will access to get at the user
 * submitted post data.
 * 
 * @author jkirton
 */
public class CsrfGuardByWebSessionAndPostHandler implements Handler {
  
  private static final Logger log = LoggerFactory.getLogger(CsrfGuardByWebSessionAndPostHandler.class);
  
  /**
   * Strongly type the 'next rst' to add to the request for use by downstream
   * handlers.
   */
  public static class NextRst {
    public final UUID nextRst;

    public NextRst(UUID nextRst) {
      super();
      this.nextRst = nextRst;
    }
  } // NextRst class

  @Override
  public void handle(Context ctx) throws Exception {
    ctx.parse(Form.class).then(formData -> {
      
      // get the already resolved web session instance 
      // which is expected to be present by way of upstream handlers
      final WebSession webSession = ctx.getRequest().get(WebSessionInstance.class).getWebSession();
      
      // rst verify
      final String formRstString = clean(formData.get("rst"));
      if(formRstString.isEmpty()) {
        log.error("rst absent in post.");
        ctx.clientError(205); // 205 - Reset Content
        return;
      }
      
      try {
        final UUID formRst = UUID.fromString(formRstString);
        final UUID webSessionRst = webSession.currentRst();
        if(not(formRst.equals(webSessionRst))) { 
          log.error("Rst mismatch: formRst: {}, webSessionRst: {}", formRstString, webSessionRst);
          ctx.clientError(400); // bad request
          return;
        }
      }
      catch(IllegalArgumentException e) {
        log.error("Bad rst: {}", formRstString);
        ctx.clientError(400); // bad request
        return;
      }
      finally {
        // reset rst and cache for downstream handlers
        ctx.getRequest().add(new NextRst(webSession.resetRst()));
        log.info("Next rst added to request.");
      }
      // the form post rst matches up with the server side web session held rst
      
      // cache the form data for downstream handlers
      ctx.getRequest().add(formData);
      log.info("Form post data processed and cached in request.");
      
      ctx.next();
    });      
  }

}
