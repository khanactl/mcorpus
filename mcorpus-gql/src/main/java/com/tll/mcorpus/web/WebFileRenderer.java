package com.tll.mcorpus.web;

import static com.tll.core.Util.asStringAndClean;
import static com.tll.core.Util.dflt;
import static com.tll.core.Util.isNotNull;
import static com.tll.core.Util.isNullOrEmpty;
import static com.tll.core.Util.not;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.render.Renderable;

/**
 * Simple {@link Renderable} in place of groovy templating
 * to avoid the heaviness of groovy on the classpath.
 * <p>
 * Here, we support simple ${...} property substitution
 * via in-memory string manipulation.
 * <p>
 * Properties are expressed as a map where the key is the property name.
 *
 */
public class WebFileRenderer implements Renderable {

  private static final String rootWebDir;

  static {
    try {
      rootWebDir = Thread.currentThread().getContextClassLoader().getResource("public").toURI().toString();
      if(rootWebDir == null) throw new Exception();
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  /**
   * Get an instance whose HTTP Content-Type is 'text/html'.
   *
   * @param webFileTemplatePath the string-wise path <em>relative to the ratpack
   *          web roor dir</em> which, in-fact, is the jar root dir when run from
   *          uber-jar.
   * @param dataMap optional map of property names (key) and values.
   * @param noCache when true, add http headers to signal the client browser to
   *          <em>NOT</em> cache this response.
   * @return newly created {@link WebFileRenderer}
   */
  public static WebFileRenderer html(String webFileTemplatePath, Map<String, Object> dataMap, boolean noCache) {
    return new WebFileRenderer(URI.create(rootWebDir + "/" + webFileTemplatePath), dataMap, "text/html", noCache);
  }

  private final URI webFileTemplatePath;
  private final Map<String, Object> dataMap;
  private final String contentType;
  private final boolean noCache;

  /**
   * Constructor.
   *
   * @param webFileTemplatePath the path to the template file to serve
   * @param dataMap the data map holding the property names and values
   *                to resolve the declared <code>${propertyName}</code>
   *                tokens in the template file.
   *                <p>
   *                May be null in which case no property 'filtering' happens.
   * @param contentType the HTTP content-type set in the response
   * @param noCache when true, add http headers to signal the client browser to
   *                <em>NOT</em> cache this response.
   */
  private WebFileRenderer(URI webFileTemplatePath, Map<String, Object> dataMap, String contentType, boolean noCache) {
    this.webFileTemplatePath = webFileTemplatePath;
    this.dataMap = dataMap;
    this.contentType = dflt(contentType, "text/html");
    this.noCache = noCache;
  }

  @Override
  public void render(Context context) throws Exception {
    Blocking.get(() -> {
      return new String(Objects.requireNonNull(Files.readAllBytes(Paths.get(webFileTemplatePath))), StandardCharsets.UTF_8);
    }).then(fileStr -> {
      if(isNotNull(dataMap) && not(dataMap.isEmpty())) {
        for(final Map.Entry<String, Object> pentry : dataMap.entrySet())
          if(not(isNullOrEmpty(pentry.getKey())))
            fileStr = fileStr.replace(String.format("${%s}", pentry.getKey()), asStringAndClean(pentry.getValue()));
      }
      if(noCache) {
        context.getResponse().getHeaders()
          .add("Expires", "Sun, 01 Jan 2018 06:00:00 GMT")
          .add("Last-Modified", ZonedDateTime.now(ZoneOffset.UTC).toString())
          .add("Cache-Control", "max-age=0, no-cache, must-revalidate, proxy-revalidate");
      }
      context.getResponse().send(contentType, fileStr);
    });
  }

  @Override
  public String toString() { return String.format("%s (%s)", webFileTemplatePath, contentType); }
}
