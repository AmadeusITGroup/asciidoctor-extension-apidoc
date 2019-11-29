package com.amadeus.asciidoc.apidoc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Properties;

import org.junit.jupiter.api.Test;

public class ApidocRegistryTest {

  Properties properties = new Properties();

  ApidocRegistry registry;

  @Test
  public void longestMatchingKeyIsReturned() {
    properties.put("key", "1");
    properties.put("key.other", "2");
    properties.put("key.ot", "3");
    properties.put("key.other.two", "4");
    registry = new ApidocRegistry(properties);

    assertEquals("2", registry.findBestMatch("key.other.one"));
  }

  @Test
  public void longestMatchingKeyIsReturned2() {
    properties.put("java", "1");
    properties.put("java.text", "2");
    registry = new ApidocRegistry(properties);

    assertEquals("1", registry.findBestMatch("java.util"));
  }

  @Test
  public void noMatch() {
    properties.put("key.else", "value");
    registry = new ApidocRegistry(properties);

    assertNull(registry.findBestMatch("key.other"));
  }

}
