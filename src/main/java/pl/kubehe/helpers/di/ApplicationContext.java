package pl.kubehe.helpers.di;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class ApplicationContext implements Runnable {

  protected Map<Class<?>, Object> context;

  @Override
  public void run() {
    var packages = packagesToScanForBeans();

    var wiredBeans = wireBeans(packages);

    context = wiredBeans;

    var classesToRun = findRootClassesToRun(wiredBeans);

    executeRootClasses(wiredBeans, classesToRun);



  }

  private List<Reflections> packagesToScanForBeans() {
    return Stream.of(this.getClass().getAnnotations())
      .filter(annotation -> annotation instanceof PackageScanner)
      .map((annotation) -> ((PackageScanner)annotation).scan())
      .map(packageName ->  new Reflections(packageName, new MethodAnnotationsScanner(),  new TypeAnnotationsScanner(), new SubTypesScanner()))
      .collect(toList());
  }

  private List<Class<?>> findRootClassesToRun(Map<Class<?>, Object> beans) {
    return beans.keySet().stream()
      .filter(CommandLineRunner.class::isAssignableFrom)
      .collect(Collectors.toList());
  }

  private void executeRootClasses(Map<Class<?>, Object> beans, List<Class<?>> classesToRun) {
    var runningTasks = classesToRun.stream().map(clazz ->
      CompletableFuture.runAsync(((CommandLineRunner)beans.get(clazz)))
    ).toArray(CompletableFuture[]::new);

    CompletableFuture.allOf(runningTasks).join();
  }

  private Map<Class<?>, Object> wireBeans(List<Reflections> packages) {
    return packages.stream()
      .flatMap(reflections -> {
        var components = reflections.getTypesAnnotatedWith(Component.class);
        var beans = reflections.getMethodsAnnotatedWith(Bean.class);

        var constructors = components.stream().map(clazz -> Arrays.stream(clazz.getConstructors())
          .max(Comparator.comparingInt(Constructor::getParameterCount))
        )
          .flatMap(Optional::stream)
          .collect(toList());

        Stream<Map.Entry<Class<?>, Object>> beansWithoutDependencies = beans.stream()
          .filter(constructor -> constructor.getParameterCount() == 0)
          .map(constructor -> {
            try {

              return Map.entry(constructor.getReturnType(), constructor.invoke(null));

            } catch (IllegalAccessException | InvocationTargetException e) {
              throw new RuntimeException("Failed to initialize object.", e);
            }
          });

        Stream<Map.Entry<Class<?>, Object>> constructorsWithoutDependencies = constructors.stream()
          .filter(constructor -> constructor.getParameterCount() == 0)
          .map(constructor -> {
            try {

              return Map.entry(constructor.getDeclaringClass(), constructor.newInstance());

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
              throw new RuntimeException("Failed to initialize object.", e);
            }
          });

        Map<Class<?>, Object> availableBeansToInject = Stream.concat(beansWithoutDependencies, constructorsWithoutDependencies)
          .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        var beansWithParameters = beans.stream()
          .filter(constructor -> constructor.getParameterCount() != 0)
          .collect(toSet());

        var constructorsWithParameters = constructors.stream()
          .filter(constructor -> constructor.getParameterCount() != 0)
          .collect(toSet());

        while(!beansWithParameters.isEmpty() || !constructorsWithParameters.isEmpty()) {

          beansWithParameters.stream()
            .filter(method -> Arrays.stream(method.getParameterTypes())
              .anyMatch(availableBeansToInject::containsKey)
            )
            .map(method -> {
              var arguments = Arrays.stream(method.getParameterTypes())
                .map(availableBeansToInject::get)
                .toArray();

              try {

                return Map.entry(method.getReturnType(), method.invoke(arguments));

              } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to initialize object.", e);
              }

            }).forEach(entry -> availableBeansToInject.put(entry.getKey(), entry.getValue()));


           beansWithParameters = beansWithParameters.stream()
            .filter(method -> Arrays.stream(method.getParameterTypes())
              .anyMatch(Predicate.not(availableBeansToInject::containsKey))
            ).collect(toSet());


          constructorsWithParameters.stream()
            .filter(method -> Arrays.stream(method.getParameterTypes())
              .anyMatch(availableBeansToInject::containsKey)
            )
            .map(method -> {
              var arguments = Arrays.stream(method.getParameterTypes())
                .map(availableBeansToInject::get)
                .toArray();

              try {

                return Map.entry(method.getDeclaringClass(), method.newInstance(arguments));

              } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to initialize object.", e);
              }

            }).forEach(entry -> availableBeansToInject.put(entry.getKey(), entry.getValue()));


          constructorsWithParameters = constructorsWithParameters.stream()
            .filter(method -> Arrays.stream(method.getParameterTypes())
              .anyMatch(Predicate.not(availableBeansToInject::containsKey))
            ).collect(toSet());
        }

        return availableBeansToInject.entrySet().stream();
      })
      .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

}
