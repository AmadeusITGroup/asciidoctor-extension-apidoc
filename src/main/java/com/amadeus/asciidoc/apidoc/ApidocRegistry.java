package com.amadeus.asciidoc.apidoc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry based on a {@link Properties} file, with qualified packages as keys and base URLs as values.
 */
class ApidocRegistry {

  private static final Logger LOG = Logger.getLogger(ApidocRegistry.class.getName());

  private Map<String, String> apidocs = null;

  ApidocRegistry(String path) {
    Properties properties = new Properties();
    if (path != null) {
      properties = loadProperties(path);
    }
    init(properties);
  }

  ApidocRegistry(Properties properties) {
    init(properties);
  }

  /**
   * Finds the longest matching packages: org.springframework < org.springframework.boot
   */
  String findBestMatch(String target) {
    String url = null;
    if (apidocs != null) {
      if (apidocs.containsKey(target)) {
        url = apidocs.get(target);
      } else {
        int lastPackageStart = target.lastIndexOf('.');
        if (lastPackageStart != -1) {
          url = findBestMatch(target.substring(0, lastPackageStart));
        }
      }
    }
    return url;
  }

  private Properties loadProperties(String path) {
    Properties props = new Properties();
    File configFile = new File(path);
    try (Reader reader = new FileReader(configFile)) {
      props.load(reader);
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Could not read config file " + path, e);
    }
    return props;
  }

  private void init(Properties props) {
    apidocs = new HashMap<>();
    for (Entry<Object, Object> entry : props.entrySet()) {
      apidocs.put((String)entry.getKey(), (String)entry.getValue());
    }
  }
}
