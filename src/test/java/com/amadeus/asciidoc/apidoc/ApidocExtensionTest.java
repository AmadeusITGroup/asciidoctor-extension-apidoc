package com.amadeus.asciidoc.apidoc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.log.Severity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Not a unit test, will execute all Asciidoctor extensions loaded from the classpath.
 */
public class ApidocExtensionTest {

  private static Asciidoctor asciidoctor;

  private Options options;

  private Attributes attributes = Attributes.builder().build();

  private StubLogHandler logHandler = new StubLogHandler();

  @BeforeAll
  public static void setUpOnce() {
    asciidoctor = Asciidoctor.Factory.create(); // Slow, do it only once.
  }

  @BeforeEach
  public void setUp() {
    attributes.setAttribute(ImplicitApidocMacro.ATTRIBUTE_APIDOCS_CONFIG, "src/test/resources/apidoc.properties");
    options = Options.builder().attributes(attributes).build();
    asciidoctor.registerLogHandler(logHandler);
  }

  @AfterEach
  public void tearDown() {
    asciidoctor.unregisterLogHandler(logHandler);
  }

  @Test
  public void explicitMacro() {
    String output = asciidoctor.convert("apidoc:java.util.List[]", options);

    assertEquals(block("<a href=\"https://javase/java/util/List.html\">List</a>"), output);
  }

  @Test
  public void testLinkClass() throws URISyntaxException {
    String output = asciidoctor.convert("javax.persistence.EntityManager", options);

    assertEquals(block("<a href=\"https://javaee/javax/persistence/EntityManager.html\">EntityManager</a>"), output);
  }

  @Test
  public void testLinkNestedClass() throws URISyntaxException {
    String output = asciidoctor.convert("java.util.Map.Entry", options);

    assertEquals(block("<a href=\"https://javase/java/util/Map.Entry.html\">Map.Entry</a>"), output);
  }

  @Test
  public void testClassTrailingDot() {
    String output = asciidoctor.convert("javax.persistence.EntityManager.", options);

    assertEquals(block("<a href=\"https://javaee/javax/persistence/EntityManager.html\">EntityManager</a>."), output);
  }

  /**
   * Only explicitly to avoid false positives with camel-case properties
   */
  @Test
  public void testLinkPackage() throws URISyntaxException {
    String output = asciidoctor.convert("apidoc:javax.persistence[]", options);

    assertEquals(block("<a href=\"https://javaee/javax/persistence/package-summary.html\">javax.persistence</a>"),
        output);
  }

  @Test
  public void testPackageTrailingDot() throws URISyntaxException {
    String output = asciidoctor.convert("See apidoc:javax.persistence[].", options);

    assertEquals(block("See <a href=\"https://javaee/javax/persistence/package-summary.html\">javax.persistence</a>."),
        output);
  }

  @Test
  public void testLinkAnnotation() throws URISyntaxException {
    String output = asciidoctor.convert("@javax.inject.Inject ", options);

    assertEquals(block("<a href=\"https://javaee/javax/inject/Inject.html\">@Inject</a>"), output);
  }

  @Test
  public void relativeLinks() {
    String output = asciidoctor.convert("com.company.my.Class", options);

    assertEquals(block("<a href=\"apidocs/com/company/my/Class.html\">Class</a>"), output);
  }

  @Test
  public void relativeLinksWithBaseUrl() {
    attributes.setAttribute(ImplicitApidocMacro.ATTRIBUTE_APIDOCS_BASE_URL, "https://my.company.com/");
    Options options = Options.builder().attributes(attributes).build();

    String output = asciidoctor.convert("com.company.my.Class", options);

    assertEquals(block("<a href=\"https://my.company.com/apidocs/com/company/my/Class.html\">Class</a>"), output);
  }

  @Test
  public void passthroughShouldSkipMacro() {
    String output = asciidoctor.convert("pass:[com.company.my.Class]", options);

    assertEquals(block("com.company.my.Class"), output);
  }

  @Test
  public void combinesWithSurroundingInlineMacro() {
    assertNotNull(asciidoctor);
    String output = asciidoctor.convert("`java.lang.String`", options);

    assertEquals(block("<code><a href=\"https://javase/java/lang/String.html\">String</a></code>"), output);
  }

  /**
   * It's correctly handled by browsers though
   */
  @Test
  public void explicitLinksWillBeNested() {
    String output = asciidoctor.convert("https://javase/java/lang/String.html[java.lang.String]", options);

    assertEquals(block(
        "<a href=\"https://javase/java/lang/String.html\"><a href=\"https://javase/java/lang/String.html\">String</a></a>"),
        output);
  }

  @Test
  public void testNormalLinks() {
    String output = asciidoctor.convert("See link:https://company.com[] for info", options);

    assertEquals(block("See <a href=\"https://company.com\" class=\"bare\">https://company.com</a> for info"), output);
  }

  @Test
  public void linksThatLookLikePackagesShouldNotBeProcessed() {
    String output = asciidoctor.convert("See link:https://java.net[] for info", options);

    assertEquals(block("See <a href=\"https://java.net\" class=\"bare\">https://java.net</a> for info"), output);
  }

  @Test
  public void camelCasePackageShouldSkipProcessing() {
    String output = asciidoctor.convert("java.net.someProperty", options);

    assertEquals(block("java.net.someProperty"), output);
  }

  /**
   * The prefix dot is consumed before calling the macro. Would make more sense to link to the source code rather than
   * the Javadoc.
   */
  @Test
  public void blockListingTitleShouldNotTriggerWarning() {
    String output = asciidoctor.convert(".pom.xml\n----\n----", options);

    assertThat(output, containsString("<div class=\"title\">pom.xml</div>"));
  }

  @Test
  public void unknownPackageBaseApidocShouldSkipAndWarn() {
    String output = asciidoctor.convert("com.unknown.Class", options);

    assertEquals(block("com.unknown.Class"), output);
    assertEquals(1, logHandler.logs.size());
    assertEquals(Severity.WARN, logHandler.logs.get(0).getSeverity());
  }

  @Test
  public void mostPrecisePackageShouldBeUsed() {
    String output = asciidoctor.convert("java.text.Format", options);

    assertEquals(block("<a href=\"https://javasetext/java/text/Format.html\">Format</a>"), output);
  }

  @Test
  public void renderExampleWithoutConfig() {
    Options options = Options.builder().build();
    options.setToFile("target/sample.html");
    asciidoctor.convertFile(new File("src/it/sample/sample.adoc"), options);

    // Warnings for unknown package base URL
    assertThat(logHandler.logs.size(), is(greaterThan(0)));
  }

  @Test
  public void renderExample() {
    Options options = Options.builder().attributes(attributes).build();
    options.setToFile("target/sample.html");
    asciidoctor.convertFile(new File("src/it/sample/sample.adoc"), options);

    assertThat(logHandler.logs.size(), is(0));
  }

  private String block(String expected) {
    return "<div class=\"paragraph\">\n<p>" + expected + "</p>\n</div>";
  }
}
