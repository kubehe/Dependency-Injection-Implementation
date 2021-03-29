package pl.kubehe.helpers.di;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.scaladsl.model.StatusCode;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kubehe.helpers.demo.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static akka.http.javadsl.server.PathMatchers.separateOnSlashes;

public class WebContext extends AllDirectives {

  private final ActorSystem system;
  private final CompletionStage<ServerBinding> binding;
  private final Logger logger;

  public WebContext(Map<Class<?>, Object> context) {

    this.logger = (Logger) context.get(Logger.class);

    var requestMappers = context.keySet().stream()
      .filter(clazz -> clazz.isAnnotationPresent(RequestMapping.class))
      .map(context::get)
      .collect(Collectors.toSet());

    var objectMapper = (ObjectMapper)context.get(ObjectMapper.class);

    var routes = requestMappers.stream().flatMap(x -> {
        var clazz = x.getClass();
        var basePath = clazz.getAnnotation(RequestMapping.class).path();

        var pathAndMethod = Arrays.stream(clazz.getMethods())
          .filter(method -> method.isAnnotationPresent(GetMapping.class))
          .map(method -> {
            var methodPath = method.getAnnotation(GetMapping.class).path();
            return Map.entry(methodPath, method);
          })
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        // todo add more *Mapping with ability to inject PathParams and QueryParams + beans

      return pathAndMethod.entrySet().stream()
        .map(entry -> {
          var mappingPath = basePath + entry.getKey();

            return path(separateOnSlashes(mappingPath) , () -> get(() -> {

              try {
                entry.getValue().setAccessible(true);
                Arrays.stream(entry.getValue().getReturnType().getConstructors()).forEach(constructor -> constructor.setAccessible(true));

                return complete(objectMapper.writeValueAsString(entry.getValue().invoke(context.get(entry.getValue().getDeclaringClass()))));
              } catch (IllegalAccessException | InvocationTargetException | JsonProcessingException e) {
                this.logger.error("Failed to send response. ", e);
                return complete(StatusCode.int2StatusCode(500));
              }

            }));
          });
    }).toArray(Route[]::new);

    system = ActorSystem.create("routes");

    final Http http = Http.get(system);
    final ActorMaterializer materializer = ActorMaterializer.create(system);

    final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = route(routes).flow(system, materializer);

    this.binding = http.bindAndHandle(
      routeFlow,
      ConnectHttp.toHost("localhost", 8080),
      materializer
    );

    logger.info("Server online at http://localhost:8080/");
  }

  public void destroy() {
    this.binding
      .thenCompose(ServerBinding::unbind)
      .thenAccept(unbound -> system.terminate());

  }

}
