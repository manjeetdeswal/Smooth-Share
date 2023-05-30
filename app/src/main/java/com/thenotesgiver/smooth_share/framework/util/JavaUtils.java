package com.thenotesgiver.smooth_share.framework.util;

import android.content.Intent;


import java.io.FileNotFoundException;


public class JavaUtils {


    public static final String PREF_ROOT = "root";
    public static final String ROOT = "/";
    public static final int RW = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

    public static FileNotFoundException fnfe(final Throwable e) {
        return (FileNotFoundException) new FileNotFoundException() {
            @Override
            public String getMessage() {
                return e.getMessage();
            }
        }.initCause(e);
    }

    public static Throwable getCause(Throwable e) { // get to the bottom
        Throwable c = null;
        while (e != null) {
            c = e;
            e = e.getCause();
        }
        return c;
    }

    public static String toMessage(Throwable e) { // eat RuntimeException's
        Throwable p = e;
        while (e instanceof RuntimeException) {
            e = e.getCause();
            if (e != null)
                p = e;
        }
        String msg = p.getMessage();
        if (msg == null || msg.isEmpty())
            msg = p.getClass().getCanonicalName();
        return msg;
    }



}
