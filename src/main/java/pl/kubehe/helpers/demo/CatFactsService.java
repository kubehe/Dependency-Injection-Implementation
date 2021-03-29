package pl.kubehe.helpers.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import pl.kubehe.helpers.di.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

@Component
public class CatFactsService {

  private final Logger logger;
  private final RestTemplate restTemplate;

  public CatFactsService(Logger logger, RestTemplate restTemplate) {
    this.logger = logger;
    this.restTemplate = restTemplate;
  }


  public List<CatFact> randomFactsAboutCats(int factsCount) {
    logger.info("Fetching random facts about cats");


    var url = "https://cat-fact.herokuapp.com/facts/random?animal_type=cat&amount=" + factsCount;

    try {
      var res = restTemplate.getForEntity(url, CatFact[].class);

      return Arrays.asList(res);
    } catch (IOException | InterruptedException e) {
      logger.error("Failed to fetch random facts about cats, ", e);
    }

    return emptyList();
  }

  public static record CatFact(
    @JsonProperty("_id")
    String id,
    @JsonProperty("__v")
    Long version,
    @JsonProperty("text")
    String text,
    @JsonProperty("updatedAt")
    String updatedAt,
    @JsonProperty("deleted")
    Boolean deleted,
    @JsonProperty("source")
    String source,
    @JsonProperty("sentCount")
    Long sentCount,
    @JsonProperty("status")
    Status status,
    @JsonProperty("type")
    String type,
    @JsonProperty("user")
    String user,
    @JsonProperty("createdAt")
    String createdAt,
    @JsonProperty("used")
    String used
  ) {
    public static record Status(
      @JsonProperty("verified") Boolean verified,
      @JsonProperty("sentCount") Long sentCount
    ) {}
  }

}

