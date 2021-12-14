package com.generator.jwt.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the utility class for fetching environment variables.
 *
 * @author - Pawan Jaiswal
 */
public class GlobalProps {

  private final Logger log = LoggerFactory.getLogger(GlobalProps.class);

  private static GlobalProps globalProps;

  private Map<String, String> env;


  private GlobalProps() {
    loadProps();
  }

  /**
   * Returns singleton instance of GlobalProps class.
   *
   * @return globalProps GlobalProps
   */
  public static GlobalProps getInstance() {
    if (globalProps == null) {
      return new GlobalProps();
    }
    return globalProps;
  }

  /**
   * Reads all the system properties and sets it on the env Map.
   */
  private void loadProps() {
    env = System.getenv().entrySet().stream().collect(Collectors.toMap(
        e -> String.valueOf(e.getKey()),
        e -> String.valueOf(e.getValue()),
        (prev, next) -> next,
        HashMap::new));
    log.debug("Environment Properties loaded {}", env);
  }

  public Map<String, String> getEnv() {
    return env;
  }

}
