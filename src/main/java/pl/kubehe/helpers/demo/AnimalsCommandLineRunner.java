package pl.kubehe.helpers.demo;

import pl.kubehe.helpers.di.CommandLineRunner;
import pl.kubehe.helpers.di.Component;

import java.util.stream.Collectors;

@Component
public class AnimalsCommandLineRunner implements CommandLineRunner {

  private final CatFactsService catFactsService;
  private final Logger logger;

  public AnimalsCommandLineRunner(CatFactsService catFactsService, Logger logger) {
    this.catFactsService = catFactsService;
    this.logger = logger;
  }

  @Override
  public void run() {
    var factsAboutCats = this.catFactsService.randomFactsAboutCats(2).stream()
      .map(CatFactsService.CatFact::text)
      .collect(Collectors.joining(" "));

    logger.info(factsAboutCats);
  }
}
