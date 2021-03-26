package pl.kubehe.helpers.demo;

import pl.kubehe.helpers.di.Component;

import java.util.Arrays;

@Component
public class Logger {

  private final TimeProvider timeProvider;

  public Logger(TimeProvider timeProvider) {
    this.timeProvider = timeProvider;
  }

  public void info(String text, Object... args) {

    var res = Arrays.stream(args)
      .reduce(text, (prev, curr) -> prev.replaceFirst("\\Q{}\\E", curr.toString()), (str1, str2) -> str1 + str2);

    System.out.println(commonInfo() + res);
  }

  public void error(String text, Throwable e) {

    System.err.println(commonInfo() + text + e.getMessage());
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
