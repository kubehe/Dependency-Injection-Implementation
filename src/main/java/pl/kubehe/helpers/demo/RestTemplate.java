package pl.kubehe.helpers.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kubehe.helpers.di.Bean;
import pl.kubehe.helpers.di.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class RestTemplate {

  private final HttpClient httpClient;

  private final Logger logger;

  private final ObjectMapper objectMapper;

  public RestTemplate(Logger logger, ObjectMapper objectMapper) {
    logger.info("Creating instance of: {}", RestTemplate.class);
    this.objectMapper = objectMapper;
    this.logger = logger;
    this.httpClient = HttpClient.newHttpClient();
  }

  public <T> T getForEntity(String url, Class<T> clazz) throws IOException, InterruptedException {

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .build();

    logger.info("Starting request for: {}", url);

    var response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    logger.info("Finished request for: {}, status code: {}, body: {}", url, response.statusCode(), response.body());

    return this.objectMapper.readValue(response.body(), clazz);
  }

}
