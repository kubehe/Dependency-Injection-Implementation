package pl.kubehe.helpers.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kubehe.helpers.di.Bean;

public class Configuration {

  @Bean
  public static ObjectMapper getObjectMapper() {
    return new ObjectMapper();
  }
}
