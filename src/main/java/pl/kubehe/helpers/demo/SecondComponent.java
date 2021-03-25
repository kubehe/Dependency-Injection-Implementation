package pl.kubehe.helpers.demo;

import pl.kubehe.helpers.di.CommandLineRunner;
import pl.kubehe.helpers.di.Component;

@Component
public class SecondComponent implements CommandLineRunner {

  private final FirstComponent firstComponent;
  private final ExternalBean externalBean;

  public SecondComponent(FirstComponent firstComponent, ExternalBean externalBean) {
    this.firstComponent = firstComponent;
    this.externalBean = externalBean;
  }

  @Override
  public void run() {
    this.firstComponent.test();
    this.externalBean.test();
  }
}
