package pl.kubehe.helpers.demo;

import pl.kubehe.helpers.di.Bean;

public class ExternalBean {

  public void test() {
    System.out.println("Hello from ExternalBean");
  }

  @Bean
  public static ExternalBean getExternalBean() {
    return new ExternalBean();
  }

}
