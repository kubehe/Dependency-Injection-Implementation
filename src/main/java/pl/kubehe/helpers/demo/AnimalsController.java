package pl.kubehe.helpers.demo;

import pl.kubehe.helpers.di.Component;
import pl.kubehe.helpers.di.GetMapping;
import pl.kubehe.helpers.di.RequestMapping;

import java.util.List;

@Component
@RequestMapping(path = "animals")
public class AnimalsController {


  private final CatFactsService catFactsService;
  private final Logger logger;

  public AnimalsController(CatFactsService catFactsService, Logger logger) {
    this.catFactsService = catFactsService;
    this.logger = logger;
  }

  @GetMapping(path = "/cats")
  public List<CatFactsService.CatFact> getFactsAboutCats() {
    var factsAboutCats = this.catFactsService.randomFactsAboutCats(2);

    logger.info("facts: {}", factsAboutCats);

    return factsAboutCats;
  }

}
