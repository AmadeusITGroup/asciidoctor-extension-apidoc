# asciidoctor-extension-apidoc

[![Maven Central](https://img.shields.io/maven-central/v/com.amadeus.asciidoc/asciidoctor-extension-apidoc.svg)](https://search.maven.org/#search%7Cga%7C1%7Ccom.amadeus.asciidoc)
[![Travis](https://img.shields.io/travis/com/AmadeusITGroup/asciidoctor-extension-apidoc.svg)](https://travis-ci.com/AmadeusITGroup/asciidoctor-extension-apidoc)
[![GitHub license](https://img.shields.io/github/license/AmadeusITGroup/asciidoctor-extension-apidoc)](https://github.com/AmadeusITGroup/asciidoctor-extension-apidoc/blob/master/LICENSE)

> Easily link to Javadoc from an Asciidoc user guide !

This extension is an inline macro based on the idea of Ruby [ImplicitApidocInlineMacro extension](https://github.com/asciidoctor/asciidoctor-extensions-lab/blob/master/lib/implicit-apidoc-inline-macro.rb) from the asciidoctor-extensions-lab.

It is written in Java using JRuby and AsciidoctorJ 2.x bindings, with the assumption that referencing Javadoc links in a documentation likely means Java is already present in the build toolchain.

It is compatible with any Asciidoctor backend (html5, pdf, ...)

## References

* [AsciidoctorJ extension API](https://asciidoctor.org/docs/asciidoctorj/)
* [Javadoc technology](https://docs.oracle.com/javase/8/docs/technotes/guides/javadoc/index.html)
* [Naming a package](https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html)
* [Identifiers in Java](https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8)

## Building the project

Pre-requisites are Maven 3+ and JDK8+:

> mvn install

Willing to contribute ? Check [this guide](./CONTRIBUTING.md) !

## Usage

Here is how to use the extension:

### Implicit macro

The idea is to write full-qualified Java class or package names in the document, and let the extension generate the actual links.

Rather than:

```adoc
See link:{jse-apidocs}/java/net/HttpURLConnection.html[`HTTPUrlConnection`] for more info.
```

It allows:

```adoc
See `java.net.HttpURLConnection` for more info.
```

The benefits are:
* The improved readability of the .adoc files
* The Javadoc-linking is systematic and consistent
* There is a single place to version and update Javadoc base URLs.

In addition the document remains semantically correct without the extension.
When rendering Asciidoc on github without document attributes, it's better than a broken link for readability.

The extension does its best to avoid false-positives.
But if required, the macro can be bypassed using pass macro:

```adoc
pass:[com.company.some_property_that_looks_like_a_package]
```

### Explicit macro

The macro may also be called explicitly: 

```adoc
apidoc:java.util.List[]
```

### asciidoctor-maven-plugin

Simply add the extension as a dependency of [asciidoctor-maven-plugin](https://asciidoctor.org/docs/asciidoctor-maven-plugin/):

```xml
<plugin>
  <groupId>org.asciidoctor</groupId>
  <artifactId>asciidoctor-maven-plugin</artifactId>
  <dependencies>
    <dependency>
      <groupId>com.amadeus.asciidoc</groupId>
      <artifactId>asciidoctor-extension-apidoc</artifactId>
    </dependency>
  </dependencies>
</plugin>
```

The extension registers itself via its /META-INF/services/org.asciidoctor.extension.spi.ExtensionRegistry

### Attribute apidocs_config

To configure the extension, add a document attribute __apidocs_config__ linking to a  .properties file:

```xml
<plugin>
  <groupId>org.asciidoctor</groupId>
  <artifactId>asciidoctor-maven-plugin</artifactId>
  <configuration>
    <attributes>
      <apidocs_config>apidoc.properties</apidocs_config>
    </attributes>
  </configuration>
</plugin>
```

The file should contain keys with fully-qualified packages, and values with the corresponding Javadoc base URL (the path parent of index.html, including the trailing slash):

```properties
java=https://docs.oracle.com/javase/10/docs/api/
javax=https://javaee.github.io/javaee-spec/javadocs/
org.springframework.boot=https://docs.spring.io/spring-boot/docs/2.0.2.RELEASE/api/
org.springframework.batch=https://docs.spring.io/spring-batch/3.0.x/apidocs/
org.springframework=https://docs.spring.io/spring/docs/5.0.7.BUILD-SNAPSHOT/javadoc-api/
```

The most precise key will be used, if any.
Otherwise the element processing will be skipped, a warning will be logged and the rendering won't be altered.

Relative links are allowed, if the documentation is hosted next to some javadoc. This will only work with an HTML backend though.

Note: the configuration cannot be done "inline" in pom.xml, as ascidoctorj attributes don't allow nested XML tags.

### Class-level Javadoc

The class needs to be fully-qualified.

```adoc
java.lang.String
```
is processed as:

```adoc
link:https://docs.oracle.com/javase/8/docs/api/java/lang/String.html[`String`]
```

Nested classes:

```adoc
java.util.Map.Entry
```

is processed as:

```adoc
link:https://docs.oracle.com/javase/8/docs/api/java/util/Map.Entry.html[`Map.Entry`]
```

Annotation classes support an optional '@' prefix:

```adoc
@javax.inject.Inject
```
is processed as:

```adoc
link:https://docs.oracle.com/javaee/7/api/javax/inject/Inject.html[`@Inject`].
```

### Package-level Javadoc

Requires at least one sub-package (obviously). 
To avoid false positives, it requires an explict macro call.
The warning regarding unknown configured package is skipped when only 1 sub-package, as it triggers false positives with "filename.extension" texts.

```adoc
apidoc:java.util.logging[]
```
is processed as:

```adoc
link:https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html[`java.util.logging`]
```
