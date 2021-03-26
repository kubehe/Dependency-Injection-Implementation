package pl.kubehe.helpers.demo;

import pl.kubehe.helpers.di.Component;

import java.time.LocalDateTime;

@Component
public class TimeProvider {

  LocalDateTime getNow() {
    return LocalDateTime.now();
  }
}
