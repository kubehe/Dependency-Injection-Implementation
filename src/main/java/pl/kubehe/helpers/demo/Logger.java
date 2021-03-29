package pl.kubehe.helpers.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kubehe.helpers.di.Component;

import java.util.Arrays;

@Component
public class Logger {

  private final TimeProvider timeProvider;
  private final ObjectMapper objectMapper;

  public Logger(TimeProvider timeProvider, ObjectMapper objectMapper) {
    this.timeProvider = timeProvider;
    this.objectMapper = objectMapper;
  }

  public void info(String text, Object... args) {

    var res = Arrays.stream(args)
      .reduce(text, (prev, curr) -> {
        try {
          return prev.replaceFirst("\\Q{}\\E", objectMapper.writeValueAsString(curr));
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
        return "Failed to parse object";
      }, (str1, str2) -> str1 + str2);

    System.out.println("[INFO] " + commonInfo() + res);
  }

  public void error(String text, Throwable e) {

    System.err.println("[ERROR] " + commonInfo() + text + e.getMessage());
  }

  private String commonInfo() {
    var classNameExecutingLogger = StackWalker.getInstance().walk(frames -> frames.skip(2)
      .findFirst()
      .map(StackWalker.StackFrame::getClassName)
    );

    var className = classNameExecutingLogger.orElse("");
    return "[" + timeProvider.getNow() + "] ["+ className +"] Thread-"+ Thread.currentThread().getId() + " - ";
  }

}
