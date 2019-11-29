package com.amadeus.asciidoc.apidoc;

import org.asciidoctor.extension.Format;
import org.asciidoctor.extension.FormatType;
import org.asciidoctor.extension.Name;

/**
 * Explicit inline macro, for cases not covered by the implicit regexp of {@link ImplicitApidocMacro}, due to risk of
 * false positives.
 */
@Name("apidoc")
@Format(FormatType.LONG)
public class ExplicitApidocMacro extends ImplicitApidocMacro {

}
