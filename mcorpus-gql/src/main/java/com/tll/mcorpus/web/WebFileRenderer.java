package com.tll.mcorpus.web;

import static com.tll.mcorpus.Util.asString;
import static com.tll.mcorpus.Util.dflt;
import static com.tll.mcorpus.Util.isNotNull;
import static com.tll.mcorpus.Util.isNullOrEmpty;
import static com.tll.mcorpus.Util.not;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
      rootWebDir = Thread.currentThread().getContextClassLoader().getResource("templates").toURI().toString();
    } catch (URISyntaxException e) {
      throw new Error(e);
    }
  }

  /**
   * Get an instance whose HTTP Content-Type is 'text/html'.
   *
   * @param webFileTemplatePath
   *          the string-wise path <em>relative to the ratpack web roor dir</em>
   *          which, in-fact, is the jar root dir when run from uber-jar.
   * @param dataMap
   *          optional map of property names (key) and values.
   * @return newly created {@link WebFileRenderer}
   */
  public static WebFileRenderer html(String webFileTemplatePath, Map<String, Object> dataMap) {    
    return new WebFileRenderer(URI.create(rootWebDir + "/" + webFileTemplatePath), dataMap, "text/html");
  }

  private final URI webFileTemplatePath;
  private final Map<String, Object> dataMap;
  private final String contentType;

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
   */
  private WebFileRenderer(URI webFileTemplatePath, Map<String, Object> dataMap, String contentType) {
    this.webFileTemplatePath = webFileTemplatePath;
    this.dataMap = dataMap;
    this.contentType = dflt(contentType, "text/html");
  }

  @Override
  public void render(Context context) throws Exception {
    Blocking.get(() -> {
      return new String(Objects.requireNonNull(Files.readAllBytes(Paths.get(webFileTemplatePath))), StandardCharsets.UTF_8);
    }).then(fileStr -> {
      if(isNotNull(dataMap) && not(dataMap.isEmpty())) {
        for(final Map.Entry<String, Object> pentry : dataMap.entrySet())
          if(not(isNullOrEmpty(pentry.getKey())))
            fileStr = fileStr.replace(String.format("${%s}", pentry.getKey()), asString(pentry.getValue()).trim());
      }
      context.getResponse().send(contentType, fileStr);
    });
  }
  
  @Override
  public String toString() { return String.format("%s (%s)", webFileTemplatePath, contentType); }
}
