package org.randomcoder.log4j;

import com.sun.jna.Library;

public interface Systemd extends Library {

  public int sd_journal_send(String format, Object... args);

}
