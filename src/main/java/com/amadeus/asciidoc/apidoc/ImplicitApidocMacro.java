package com.amadeus.asciidoc.apidoc;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.extension.Format;
import org.asciidoctor.extension.FormatType;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

/**
 * Transforms fully-qualified Java class into a link to their corresponding Javadoc.
 * <p>
 * Supports annotations prefix '@' and nested classes.
 * <p>
 * Linking to methods is not supported.
 * <p>
 * Due to false positives with property names and URLs, fully qualified package names require use of
 * {@link ExplicitApidocMacro}. The implicit regexp still has to match the explicit, in case it is processed first.
 */
@Name("implicit-apidoc") // Not supposed to be used explicitly in documents
@Format(value = FormatType.CUSTOM, regexp = "(?:apidoc:)?(@?" + ImplicitApidocMacro.JAVA_PACKAGE_REGEX + "(?:\\."
    + ImplicitApidocMacro.JAVA_CLASS_REGEX + "))(\\[\\])?")
public class ImplicitApidocMacro extends InlineMacroProcessor {

  static final String ATTRIBUTE_APIDOCS_CONFIG = "apidocs_config";

  /**
   * Packages are only lowercase. Assume there is at least 1 sub-package.
   */
  static final String JAVA_PACKAGE_REGEX = "[a-z_][a-z_0-9]+(?:\\.[a-z_][a-z_0-9]+)+";

  /**
   * Classes start with uppercase, and can be nested
   */
  static final String JAVA_CLASS_REGEX = "[A-Z][A-Za-z0-9_]+(?:\\.[A-Z][A-Za-z0-9_]+)*";

  /**
   * Logger for troubleshooting extension execution. To log asciidoctor logs, use {@link #log(LogRecord)} instead.
   */
  private static final Logger LOG = Logger.getLogger(ImplicitApidocMacro.class.getName());

  /**
   * Fully qualified class
   */
  static final Pattern QUALIFIED_JAVA_CLASS_REGEX =
      Pattern.compile("@?(" + JAVA_PACKAGE_REGEX + ")\\.(" + JAVA_CLASS_REGEX + ")");

  private ApidocRegistry registry = null;

  @Override
  public Object process(ContentNode parent, String target, Map<String, Object> attributes) {
    LOG.log(Level.FINE, "Processing {0}", target);

    if (registry == null) {
      String path = (String)parent.getDocument().getAttributes().get(ATTRIBUTE_APIDOCS_CONFIG);
      registry = new ApidocRegistry(path);
    }

    String baseUrl = registry.findBestMatch(target.replaceFirst("@", ""));
    if (baseUrl != null) {
      Link link = buildLink(baseUrl, target);
      return renderLink(parent, link, attributes);
    } else {
      // If single sub-package, assume it may be "filename.extension" and don't report a warning.
      if (StringUtils.countMatches(target, ".") > 1) {
        log(new LogRecord(Severity.WARN,
            String.format(
                "Unknown apidocs package: <%s>, no links will be generated."
                    + " Add the package via %s attribute, report the false-positive or use passthrough macro.",
                target, ATTRIBUTE_APIDOCS_CONFIG)));
      }
      return renderText(parent, target, attributes); // skip
    }
  }

  private Link buildLink(String baseUrl, String target) {
    String url = baseUrl;
    String text = "";

    Matcher matcher = QUALIFIED_JAVA_CLASS_REGEX.matcher(target);
    if (matcher.matches()) { // Java class
      String packageName = matcher.group(1);
      String className = matcher.group(2);
      url += packageName.replaceAll("\\.", "/") + "/" + className + ".html";
      if (target.startsWith("@")) { // Annotation
        text += "@";
      }
      text += className;

    } else { // Java package
      url += target.replaceAll("\\.", "/") + "/package-summary.html";
      text = target;
    }
    return new Link(url, text);
  }

  private PhraseNode renderLink(ContentNode parent, Link link, Map<String, Object> attributes) {
    Map<String, Object> options = new HashMap<>();
    options.put("type", ":link");
    options.put("target", link.url);
    // Use built-in method, to be independent of backend type.
    return createPhraseNode(parent, "anchor", link.text, attributes, options);
  }

  /**
   * String need to be wrapped into quoted phrase, see https://github.com/asciidoctor/asciidoctor/issues/3176
   */
  private PhraseNode renderText(ContentNode parent, String target, Map<String, Object> attributes) {
    return createPhraseNode(parent, "quoted", target, attributes);
  }

  private static class Link {

    String url;

    String text;

    public Link(String url, String text) {
      this.url = url;
      this.text = text;
    }
  }
}
