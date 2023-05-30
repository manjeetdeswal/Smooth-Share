package com.thenotesgiver.smooth_share.framework.app;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class SuperUser extends LeafSuperUser {

    public static String BIN_SUIO;

    public static long length(SuIO su, File f) {
        try {
            su.write("length", f);
            return Long.parseLong(su.readString());
        } catch (IOException e) {
            throw new RuntimeException(new Result(su.cmd, su.su, e).errno());
        }
    }

    public static Result delete(SuIO su, File target) {
        try {
            su.write("delete", target);
            return su.ok();
        } catch (IOException e) {
            throw new RuntimeException(new Result(su.cmd, su.su, e).errno());
        }
    }






    public static class SuIO {
        public InputStream is;
        public OutputStream os;
        public Commands cmd;
        public Process su;
        public boolean valid = true;

        public SuIO() {
            this(BIN_SU);
        }

        public SuIO(String shell) {
            try {
                cmd = new Commands(BIN_SUIO + ";" + BIN_EXIT).exit(true);
                su = Runtime.getRuntime().exec(shell);
                os = new BufferedOutputStream(su.getOutputStream());
                if (cmd.exit && !EXITCODE)
                    SuperUser.writeString(BIN_TRAP + " '" + KILL_SELF + "' ERR" + EOL, os);
                SuperUser.writeString(cmd.build(), os);
                is = new BufferedInputStream(su.getInputStream());
            } catch (IOException e) {
                if (su != null)
                    throw new RuntimeException(new Result(cmd, su, e).errno());
                else
                    throw new RuntimeException(e);
            }
        }

        public void writeString(String str) throws IOException {
            os.write(str.getBytes(Charset.defaultCharset()));
            os.write(0);
            os.flush();
        }

        public void write(Object... oo) throws IOException {
            for (Object o : oo) {
                if (o instanceof String)
                    writeString((String) o);
                else if (o instanceof Long)
                    writeString(Long.toString((Long) o));
                else if (o instanceof Integer)
                    writeString(Integer.toString((Integer) o));
                else if (o instanceof File)
                    writeString(((File) o).getPath());
                else
                    throw new IOException("unknown type");
            }
        }

        public String readString() throws IOException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int c;
            while ((c = is.read()) != 0) {
                if (c == -1) {
                    valid = false;
                    try {
                        su.waitFor(); // wait to read exitCode() or exception will be thrown
                    } catch (InterruptedException e) {
                        throw new IOException(e);
                    }
                    throw new EOFException();
                }
                os.write(c);
            }
            return os.toString();
        }

        public boolean ping() throws IOException {
            write("ping");
            String str = readString();
            return str.equals("pong");
        }

        public void clear() {
            try {
                skipAll(su.getInputStream());
                skipAll(su.getErrorStream());
            } catch (IOException e) {
                Log.e(TAG, "clear", e);
                valid = false;
            }
        }

        public boolean valid() {
            return valid;
        }

        public void exit() throws IOException { // no handling exit codes and stderr here
            valid = false;
            write("exit");
            try {
                su.waitFor();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            Result.must(su);
        }

        public void alive() throws IOException {
            try {
                su.exitValue();
                valid = false;
                throw new IOException("!alive");
            } catch (IllegalThreadStateException ignore) { // not exited
            }
        }

        public Result ok() { // input / output stream sanity checks
            try {
                String ok = readString();
                if (ok.equals("ok"))
                    return new Result(0);
                valid = false;
                return new Result(cmd, su, new Throwable("!ok: " + ok));
            } catch (IOException e) { // wrap exceptions, so caller decide raise or twist
                valid = false;
                return new Result(cmd, su, e);
            }
        }

        public void close() {
            valid = false;
            su.destroy();
        }
    }

    public static boolean exists(SuIO su, File f) {
        try {
            su.write("exists", f);
            return toBoolean(su.readString());
        } catch (IOException e) {
            throw new RuntimeException(new Result(su.cmd, su.su, e).errno());
        }
    }

    public static boolean isDirectory(SuIO su, File f) {
        try {
            su.write("isdir", f);
            return toBoolean(su.readString());
        } catch (IOException e) {
            throw new RuntimeException(new Result(su.cmd, su.su, e).errno());
        }
    }

    public static boolean toBoolean(String str) throws IOException {
        if (str.equals("true"))
            return true;
        if (str.equals("false"))
            return false;
        throw new IOException("bad input");
    }

    public static void skipAll(InputStream is) throws IOException {
        int a;
        while ((a = is.available()) > 0)
            IOUtils.skip(is, a);
    }




}
