package pl.kubehe.helpers.demo;

import pl.kubehe.helpers.di.*;

@PackageScanner(scan = "pl.kubehe.helpers.demo")
public class DemoApplication extends ApplicationContext {

  public static void main(String[] args) {
    new DemoApplication().run();
  }

}
