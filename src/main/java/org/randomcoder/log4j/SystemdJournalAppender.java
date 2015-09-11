package org.randomcoder.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SystemdJournalAppender extends AppenderSkeleton {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final int LINE_SEPARATOR_LENGTH = LINE_SEPARATOR.length();

  private static final String MDC_PREFIX = "LOG4J_MDC_";
  private boolean logLocationInformation = false;
  private String syslogIdentifier = null;

  private final Systemd library;

  public SystemdJournalAppender() {
    this.library = SystemdFactory.getInstance();
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

  @Override
  public void close() {
  }

  @Override
  protected void append(LoggingEvent event) {
    if (checkAppend()) {
      appendInternal(event);
    }
  }

  public void setLogLocationInformation(boolean logLocationInformation) {
    this.logLocationInformation = logLocationInformation;
  }

  public boolean isLogLocationInformation() {
    return logLocationInformation;
  }

  public void setSyslogIdentifier(String syslogIdentifier) {
    this.syslogIdentifier = syslogIdentifier;
  }

  public String getSyslogIdentifier() {
    return syslogIdentifier;
  }

  private boolean checkAppend() {
    if (super.closed) {
      LogLog.warn("Not allowed to write to a closed appender.");
      return false;
    }
    if (library == null) {
      LogLog.warn("Unable to load native library");
      return false;
    }
    return true;
  }

  private void appendInternal(LoggingEvent event) {
    List<Object> args = new ArrayList<>();

    String message = (layout == null) ? event.getRenderedMessage() : chop(layout.format(event));

    if ((layout == null || layout.ignoresThrowable()) && event.getThrowableStrRep() != null) {
      // layout doesn't handle stack trace; write to message inline
      StringBuilder buf = new StringBuilder();
      buf.append(message);
      for (String line : event.getThrowableStrRep()) {
        buf.append(LINE_SEPARATOR).append(line);
      }
      message = buf.toString();
    }

    args.add(message);

    args.add("PRIORITY=%d");
    args.add(priority(event.getLevel()));

    args.add("THREAD_NAME=%s");
    args.add(event.getThreadName());

    args.add("LOG4J_LOGGER=%s");
    args.add(event.getLogger().getName());

    if (event.getThrowableStrRep() != null) {
      StringBuilder buf = new StringBuilder();
      for (String line : event.getThrowableStrRep()) {
        buf.append(line).append(LINE_SEPARATOR);
      }
    }

    if (syslogIdentifier != null) {
      args.add("SYSLOG_IDENTIFIER=%s");
      args.add(syslogIdentifier);
    }

    if (isLogLocationInformation()) {
      LocationInfo loc = event.getLocationInformation();
      String fileName = loc.getFileName();
      if (fileName != null && !fileName.equals(LocationInfo.NA)) {
        args.add("CODE_FILE=%s");
        args.add(fileName);
      }
      String lineNumber = loc.getLineNumber();
      if (lineNumber != null && !lineNumber.equals(LocationInfo.NA)) {
        args.add("CODE_LINE=%s");
        args.add(lineNumber);
      }
      String className = loc.getClassName();
      String methodName = loc.getMethodName();
      if (className != null && methodName != null) {
        if (!(className.equals(LocationInfo.NA) && methodName.equals(LocationInfo.NA))) {
          // at least one is present
          args.add("CODE_FUNC=%s");
          args.add(className + "." + methodName);
        }
      }
    }

    Map<?, ?> props = event.getProperties();
    if (props != null) {
      for (Map.Entry<?, ?> entry : props.entrySet()) {
        args.add(MDC_PREFIX + normalize(entry.getKey()) + "=%s");
        args.add(entry.getValue().toString());
      }
    }

    args.add(null);

    library.sd_journal_send("MESSAGE=%s", args.toArray());
  }

  private String chop(String msg) {
    if (msg.endsWith(LINE_SEPARATOR)) {
      msg = msg.substring(0, msg.length() - LINE_SEPARATOR_LENGTH);
    }
    return msg;
  }

  private String normalize(Object key) {
    return key.toString().toUpperCase(Locale.US).replaceAll("[^_A-Z0-9]", "_");
  }

  private int priority(Level level) {
    return Math.max(level.getSyslogEquivalent(), 2);
  }
}
