package org.randomcoder.log4j;

import com.sun.jna.Native;

import org.apache.log4j.helpers.LogLog;

abstract public class SystemdFactory {
    private SystemdFactory() {
    }

    private static Systemd INSTANCE;

    static {
        try {
            INSTANCE = (Systemd) Native.loadLibrary("systemd", Systemd.class);
        } catch (Throwable t) {
            LogLog.error(t.getMessage());
            INSTANCE = null;
        }
    }

    static void setInstance(Systemd instance) {
        INSTANCE = instance;
    }

    public static Systemd getInstance() {
        return INSTANCE;
    }

}
