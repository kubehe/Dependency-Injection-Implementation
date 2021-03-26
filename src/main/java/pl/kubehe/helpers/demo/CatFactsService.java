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

  static class CatFact {

    private final String id;
    private final Long version;
    private final String text;
    private final String updatedAt;
    private final Boolean deleted;
    private final String source;
    private final Long sentCount;
    private final Status status;
    private final String type;
    private final String user;
    private final String createdAt;

    @JsonCreator
    public CatFact(
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
      this.id = id;
      this.version = version;
      this.text = text;
      this.updatedAt = updatedAt;
      this.deleted = deleted;
      this.source = source;
      this.sentCount = sentCount;
      this.status = status;
      this.type = type;
      this.user = user;
      this.createdAt = createdAt;

    }

    public String getId() {
      return id;
    }

    public Long getVersion() {
      return version;
    }

    public String getText() {
      return text;
    }

    public String getUpdatedAt() {
      return updatedAt;
    }

    public Boolean getDeleted() {
      return deleted;
    }

    public String getSource() {
      return source;
    }

    public Long getSentCount() {
      return sentCount;
    }

    public Status getStatus() {
      return status;
    }

    public String getType() {
      return type;
    }

    public String getUser() {
      return user;
    }

    public String getCreatedAt() {
      return createdAt;
    }

    static class Status {
      private final Boolean verified;
      private final Long sentCount;

      @JsonCreator
      public Status(@JsonProperty("verified") Boolean verified, @JsonProperty("sentCount") Long sentCount) {
        this.verified = verified;
        this.sentCount = sentCount;
      }

      public Boolean getVerified() {
        return verified;
      }

      public Long getSentCount() {
        return sentCount;
      }
    }
  }
}
