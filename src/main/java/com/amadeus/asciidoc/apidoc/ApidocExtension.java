package com.amadeus.asciidoc.apidoc;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

public class ApidocExtension implements ExtensionRegistry {

  @Override
  public void register(Asciidoctor asciidoctor) {
    JavaExtensionRegistry extensionRegistry = asciidoctor.javaExtensionRegistry();
    extensionRegistry.inlineMacro(ImplicitApidocMacro.class);
    extensionRegistry.inlineMacro("apidoc", ExplicitApidocMacro.class);
  }
}
