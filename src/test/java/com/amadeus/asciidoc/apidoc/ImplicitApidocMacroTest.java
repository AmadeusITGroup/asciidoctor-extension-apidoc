package com.amadeus.asciidoc.apidoc;

import org.asciidoctor.Options;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.asciidoctor.Attributes;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Attempt at unit testing, but AsciidoctorJ/JRuby doesn't play well with mocks.
 */
@ExtendWith(MockitoExtension.class)
public class ImplicitApidocMacroTest {

  static ImplicitApidocMacro macro;

  @Mock
  private StructuralNode parent;

  @Mock
  private Document document;

  private final Attributes attributes = Attributes.builder().build();

  private final Map<String, Object> options = Options.builder().build().map();

  @BeforeEach
  public void setUp() {
    macro = new ImplicitApidocMacro();
    attributes.setAttribute(ImplicitApidocMacro.ATTRIBUTE_APIDOCS_CONFIG, "src/test/resources/apidoc.properties");

    when(parent.getDocument()).thenReturn(document);
    when(document.getAttributes()).thenReturn(attributes.map());
  }

  @Disabled("#log requires a JRuby runtime")
  @Test
  public void unknownPackageShouldSkipProcessing() {
    String output = macro.process(parent, "com.unknown.Class", options).convert();

    assertEquals("com.unknown.Class", output);
  }

  @Disabled("JRuby bugs when calling Inline#convert, see ImplicitApidocMacroAscidocTest instead")
  @Test
  public void relativeLinks() {
    String output = macro.process(parent, "com.company.my.Class", options).convert();

    assertEquals("<a href=\"apidocs/com/company/my/Class.html\">Class</a>", output);
  }
}
