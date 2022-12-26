package com.tll.web.ratpack;

import static com.tll.core.Util.asStringAndClean;
import static com.tll.core.Util.isNotNullOrEmpty;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ratpack.exec.Blocking;
import ratpack.core.handling.Context;
import ratpack.core.render.RendererSupport;

/**
 * Simple {@link Renderer} in place of groovy templating
 * to avoid the heaviness of groovy on the classpath.
 * <p>
 * Here, we support simple ${...} property substitution
 * via in-memory string manipulation.
 * <p>
 * Files which are referenced by a {@link Path} are loaded
 * only once from disk then cached for subsequent re-use.
 *
 * @author jpk
 */
public class WebFileRenderer extends RendererSupport<WebFileRenderer.TRefAndData> {

  /**
   * The web file template reference along with an optional data map.
   * This object type serves as the key for supporting
   * {@link ratpack.render.Renderer} rendering in Ratpack.
   */
  public static class TRefAndData {

    /**
     * Input for rendering a web template file with NO template data parameters.
     * No-caching headers are added to the http response.
     *
     * @param tref the web temlate file path
     * @return Newly created {@link TRefAndData} instance
     */
    public static TRefAndData htmlNoCache(final Path tref) {
      return htmlNoCache(tref, null);
    }

    /**
     * Input for rendering a web template file with a map of data key/values.
     * No-caching headers are added to the http response.
     *
     * @param tref the web temlate file path
     * @param data the web template data parameters
     * @return Newly created {@link TRefAndData} instance
     */
    public static TRefAndData htmlNoCache(final Path tref, final Map<String, Object> data) {
      return new TRefAndData(tref, data, "text/html", true);
    }

    private final Path path;
    private final Map<String, Object> data;
    private final String contentType;
    private final boolean noCache;

    private TRefAndData(final Path path, final Map<String, Object> data, String contentType, boolean noCache) {
      this.path = path;
      this.data = data;
      this.contentType = contentType;
      this.noCache = noCache;
    }
  } // TrefAndData

  /**
   * The cache of loaded web file templates that persist
   * for the duration of the application's uptime.
   */
  private static final Cache<Path, String> cache;

  static {
    cache = Caffeine.newBuilder().maximumSize(5).build();
  }

  private final Logger log = LoggerFactory.getLogger(WebFileRenderer.class);

  /**
   * Constructor.
   */
  public WebFileRenderer() {
  }

  void render(final Context ctx, TRefAndData tref, String tstr) throws Exception {
    String lstr = tstr;
    if(isNotNullOrEmpty(tref.data)) {
      for(final Map.Entry<String, Object> pentry : tref.data.entrySet())
        if(isNotNullOrEmpty(pentry.getKey()))
          lstr = lstr.replace(
            String.format("${%s}", pentry.getKey()),
            asStringAndClean(pentry.getValue())
          );
    }
    if(tref.noCache) {
      ctx.getResponse().getHeaders()
        .add("Expires", "Sun, 01 Jan 2018 06:00:00 GMT")
        .add("Last-Modified", ZonedDateTime.now(ZoneOffset.UTC).toString())
        .add("Cache-Control", "max-age=0, no-cache, must-revalidate, proxy-revalidate");
    }
    ctx.getResponse().send(tref.contentType, lstr);
  }

  @Override
  public void render(final Context ctx, TRefAndData tref) throws Exception {
    final String tstr = cache.getIfPresent(tref.path);
    if(tstr == null) {
      Blocking.get(() -> {
        return new String(Objects.requireNonNull(Files.readAllBytes(tref.path)), StandardCharsets.UTF_8);
      }).then(fstr -> {
        cache.put(tref.path, fstr);
        log.debug("Rendering -just- cached Web template file: {}.", tref.path);
        render(ctx, tref, fstr);
      });
    } else {
      log.debug("Rendering already cached Web template file: {}.", tref.path);
      render(ctx, tref, tstr);
    }
  }

}