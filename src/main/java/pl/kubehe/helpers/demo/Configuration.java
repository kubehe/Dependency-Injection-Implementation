package pl.kubehe.helpers.demo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kubehe.helpers.di.Bean;

public class Configuration {

  @Bean
  public static ObjectMapper getObjectMapper() {

    return new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }
}
