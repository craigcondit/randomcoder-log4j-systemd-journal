package org.randomcoder.log4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;

public class SystemdJournalAppenderTest {

  static MockSystemd systemd;

  @BeforeClass
  public static void setUpBeforeClass() {
    systemd = new MockSystemd(System.err);
    SystemdFactory.setInstance(systemd);

    DOMConfigurator.configure(SystemdJournalAppenderTest.class.getResource("/log4j-test.xml"));
  }

  @Test
  public void testNoLayout() {
    Logger.getLogger("nolayout").info("test");
    assertEquals("MESSAGE=%s", systemd.getLastFormat());
    assertEquals(8, systemd.getLastArgs().size());
    assertEquals("test", systemd.getLastArgs().get(0));
    assertEquals("PRIORITY=%d", systemd.getLastArgs().get(1));
    assertEquals(Integer.valueOf(6), systemd.getLastArgs().get(2));
    assertEquals("THREAD_NAME=%s", systemd.getLastArgs().get(3));
    assertEquals(Thread.currentThread().getName(), systemd.getLastArgs().get(4));
    assertEquals("LOG4J_LOGGER=%s", systemd.getLastArgs().get(5));
    assertEquals("nolayout", systemd.getLastArgs().get(6));
    assertNull(systemd.getLastArgs().get(7));
  }

  @Test
  public void testIdentifier() {
    Logger.getLogger("id").info("test");
    assertEquals("MESSAGE=%s", systemd.getLastFormat());
    assertEquals(10, systemd.getLastArgs().size());
    assertEquals("test", systemd.getLastArgs().get(0));
    assertEquals("PRIORITY=%d", systemd.getLastArgs().get(1));
    assertEquals(Integer.valueOf(6), systemd.getLastArgs().get(2));
    assertEquals("THREAD_NAME=%s", systemd.getLastArgs().get(3));
    assertEquals(Thread.currentThread().getName(), systemd.getLastArgs().get(4));
    assertEquals("LOG4J_LOGGER=%s", systemd.getLastArgs().get(5));
    assertEquals("id", systemd.getLastArgs().get(6));
    assertEquals("SYSLOG_IDENTIFIER=%s", systemd.getLastArgs().get(7));
    assertEquals("log4j-appender", systemd.getLastArgs().get(8));
    assertNull(systemd.getLastArgs().get(9));
  }

  @Test
  public void testLayout() {
    Logger.getLogger("layout").info("test");
    assertEquals("MESSAGE=%s", systemd.getLastFormat());
    assertEquals(8, systemd.getLastArgs().size());
    assertEquals("INFO test", systemd.getLastArgs().get(0));
    assertEquals("PRIORITY=%d", systemd.getLastArgs().get(1));
    assertEquals(Integer.valueOf(6), systemd.getLastArgs().get(2));
    assertEquals("THREAD_NAME=%s", systemd.getLastArgs().get(3));
    assertEquals(Thread.currentThread().getName(), systemd.getLastArgs().get(4));
    assertEquals("LOG4J_LOGGER=%s", systemd.getLastArgs().get(5));
    assertEquals("layout", systemd.getLastArgs().get(6));
    assertNull(systemd.getLastArgs().get(7));
  }

  @Test
  public void testLocation() {
    Logger.getLogger("location").info("test");
    assertEquals("MESSAGE=%s", systemd.getLastFormat());
    assertEquals(14, systemd.getLastArgs().size());

    String[] parts = systemd.getLastArgs().get(0).toString().split(" ");
    assertEquals(6, parts.length);
    assertEquals("INFO", parts[0]);
    assertEquals("test", parts[1]);
    assertEquals(getClass().getName(), parts[2]);
    String method = parts[3];
    String src = parts[4];
    String line = parts[5];

    assertEquals("PRIORITY=%d", systemd.getLastArgs().get(1));
    assertEquals(Integer.valueOf(6), systemd.getLastArgs().get(2));
    assertEquals("THREAD_NAME=%s", systemd.getLastArgs().get(3));
    assertEquals(Thread.currentThread().getName(), systemd.getLastArgs().get(4));
    assertEquals("LOG4J_LOGGER=%s", systemd.getLastArgs().get(5));
    assertEquals("location", systemd.getLastArgs().get(6));
    assertEquals("CODE_FILE=%s", systemd.getLastArgs().get(7));
    assertEquals(src, systemd.getLastArgs().get(8));
    assertEquals("CODE_LINE=%s", systemd.getLastArgs().get(9));
    assertEquals(line, systemd.getLastArgs().get(10));
    assertEquals("CODE_FUNC=%s", systemd.getLastArgs().get(11));
    assertEquals(getClass().getName() + "." + method, systemd.getLastArgs().get(12));
    assertNull(systemd.getLastArgs().get(13));
  }
}
