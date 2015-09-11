package org.randomcoder.log4j;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MockSystemd implements Systemd {
  static final String LINE_SEPARATOR = System.getProperty("line.separator");

  private final PrintStream out;

  public MockSystemd(PrintStream out) {
    this.out = out;
  }

  private volatile String lastFormat;
  private volatile List<Object> lastArgs = Collections.emptyList();

  @Override
  public synchronized int sd_journal_send(String format, Object... args) {
    lastFormat = format;
    lastArgs = Collections.unmodifiableList(Arrays.asList(args));

    StringBuilder buf = new StringBuilder();
    buf.append(format);
    buf.append(LINE_SEPARATOR);
    for (Object arg : args) {
      buf.append("  ");
      buf.append(arg);
      buf.append(LINE_SEPARATOR);
    }
    out.print(buf.toString());
    return 0;
  }

  public String getLastFormat() {
    return lastFormat;
  }

  public List<Object> getLastArgs() {
    return lastArgs;
  }
}
