package com.thenotesgiver.smooth_share.framework.app;

import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import androidx.annotation.NonNull;

import com.thenotesgiver.smooth_share.framework.util.JavaUtils;
import com.yanzhenjie.andserver.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class LeafSuperUser {
    public static String TAG = SuperUser.class.getSimpleName();


    public static final String SYSTEM = "/system";

    public static final String USR = "/usr";
    public static final String XBIN = "/xbin";
    public static final String SBIN = "/sbin";
    public static final String BIN = "/bin";

    public static String[] WHICH_USER = new String[]{SYSTEM + SBIN, SYSTEM + BIN,
            SYSTEM + USR + SBIN, SYSTEM + USR + BIN,
            USR + SBIN, USR + BIN,
            SBIN, BIN};
    public static String[] WHICH_XBIN = new String[]{SYSTEM + XBIN};
    public static String[] WHICH = concat(WHICH_XBIN, WHICH_USER);


    public static final String BIN_SU = which("su");

    public static final String BIN_CAT = which("cat");

    public static final String BIN_RM = which("rm");
    public static final String BIN_KILL = which("kill");

    public static final String BIN_EXIT = "exit"; // build-in
    public static final String BIN_SET = "set"; // build-in
    public static final String BIN_TRAP = "trap"; //build-in
   public static final String BIN_STAT = which("stat");

    public static final String SETE = BIN_SET + " -e";
   public static final String DELETE = BIN_RM + " -rf {0}";
   public static final String STATLCS = BIN_STAT + " -Lc%s {0}"; // L = follow symlinks, c = custom format, %s = file size

    public static final String KILL_SELF = BIN_KILL + " -9 $$";

    public static final String EOL = "\n";

    public static final File DOT = new File(".");
    public static final File DOTDOT = new File("..");

    public static boolean EXITCODE = false; // does su support for exit code for pipe scripts? run exitTest()
    public static boolean TRAPERR = false; // does sh support for trap ERR for scripts? run trapTest()

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

  
    public void close(FileDescriptor fd) {
        try {
            Os.close(fd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Exception errno(String func, int errno) {
        return new ErrnoException(func, errno);
    }

   

    public static String find(String... args) {
        for (String s : args) {
            File f = new File(s);
            if (f.exists())
                return s;
        }
        return null;
    }

    public static String escape(File p) {
        return escape(p.getPath());
    }

    public static String escape(String p) { // https://unix.stackexchange.com/questions/347332
        if (p.startsWith("-"))
            p += "./";
        p = p.replaceAll("\\\\", "\\\\\\\\"); // has go first
        p = p.replaceAll("\\$", "\\\\\\$");
        p = p.replaceAll("\\*", "\\\\*");
        p = p.replaceAll("<", "\\\\<");
        p = p.replaceAll(">", "\\\\>");
        p = p.replaceAll("=", "\\\\=");
        p = p.replaceAll("\\[", "\\\\[");
        p = p.replaceAll("\\]", "\\\\]");
        p = p.replaceAll("\\{", "\\\\{");
        p = p.replaceAll("\\}", "\\\\}");
        p = p.replaceAll("\\|", "\\\\|");
        p = p.replaceAll("~", "\\\\~");
        p = p.replaceAll("`", "\\\\`");
        p = p.replaceAll(";", "\\\\;");
        p = p.replaceAll("&", "\\\\&");
        p = p.replaceAll("#", "\\\\#");
        p = p.replaceAll("\\)", "\\\\)");
        p = p.replaceAll("\\(", "\\\\(");
        p = p.replaceAll(" ", "\\\\ ");
        p = p.replaceAll("'", "\\\\'");
        p = p.replaceAll("\"", "\\\\\"");
        return p;
    }

    public static void writeString(String str, OutputStream os) throws IOException {
        os.write(str.getBytes(Charset.defaultCharset()));
        os.flush();
    }

    public static Result su(String pattern, Object... args) {
        return su(MessageFormat.format(pattern, args));
    }

    public static Result su(String cmd) {
        return su(new Commands(cmd).exit(true));
    }

    public static Result su(Commands cmd) {
        return exec(BIN_SU, cmd);
    }

    public static Result exec(String sh, Commands cmd) {
        Process su = null;
        try {
            su = Runtime.getRuntime().exec(sh);
            if (cmd.stderr != null && !cmd.stderr)
                su.getErrorStream().close();
            OutputStream os = su.getOutputStream();
            if (cmd.sete)
                writeString(SETE + EOL, os);
            if (cmd.exit && !EXITCODE && TRAPERR) // without 'trap' scrips with or without (set -e) always exit with '0'
                writeString(BIN_TRAP + " '" + KILL_SELF + "' ERR" + EOL, os);
            writeString(cmd.build(), os);
            writeString(BIN_EXIT + EOL, os);
            su.waitFor();
            return new Result(cmd, su);
        } catch (IOException e) {
            return new Result(cmd, su, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new Result(cmd, su, e);
        }
    }





    public static Result delete(File f) {
        return su(DELETE, escape(f));
    }

    


    public static boolean isDirectory(File f) {
        return su("[ -d {0} ]", escape(f)).ok();
    }

    public static ArrayList<File> isDirectory(ArrayList<File> ff) {
        Commands cmd = new Commands();
        for (File f : ff)
            cmd.add("[ -d " + escape(f) + " ] && echo " + escape(f));
        Result r = su(cmd.stdout(true));
        ArrayList<File> a = new ArrayList<>();
        Scanner s = new Scanner(r.stdout);
        while (s.hasNextLine())
            a.add(new File(s.nextLine()));
        s.close();
        return a;
    }

    

    public static String which(String cmd) {
        return which(WHICH, cmd);
    }

    public static String which(String[] ss, String cmd) {
        for (String s : ss) {
            String f = find(s + "/" + cmd);
            if (f != null)
                return f;
        }
        return cmd;
    }


 







    public static class Commands {
        public StringBuilder sb = new StringBuilder();
        public boolean sete = true; // handle exit codes during script execution
        public boolean stdout = false;
        public Boolean stderr = null; // null means get error only on error
        public boolean exit = false; // does exit code matters?

        public Commands() {
        }

        public Commands(String cmd) {
            add(cmd);
        }

        public Commands sete(boolean b) {
            this.sete = b;
            return this;
        }

        public Commands stdout(boolean b) {
            stdout = b;
            return this;
        }

        public Commands stderr(boolean b) {
            stderr = b;
            return this;
        }

        public Commands exit(boolean b) {
            exit = b;
            return this;
        }

        public Commands add(String cmd) {
            sb.append(cmd);
            sb.append(EOL);
            return this;
        }

        public String build() {
            return sb.toString();
        }

        public String toString() {
            return build();
        }
    }

    public static class Result {
        public int errno;
        public String stdout;
        public String stderr;
        public Throwable e;

        public static void must(Process p) throws IOException {
            if (p.exitValue() != 0)
                throw new IOException("bad exit code");
        }

        public Result(int res) {
            this.errno = res;
        }

        public Result(Commands cmd, Process p) {
            errno = p.exitValue();
            captureOutputs(cmd, p);
        }

        public Result(Commands cmd, Process p, Throwable e) {
            if (p == null) {
                this.errno = 1;
                this.e = e;
                return;
            }
            this.e = e;
            captureOutputs(cmd, p);
            this.errno = p.exitValue();
            p.destroy();
        }

        public void captureOutputs(Commands cmd, Process p) {
            if (cmd.stdout) {
                try {
                    stdout = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
                } catch (IOException e1) {
                    Log.e(TAG, "unable to get error", e1);
                }
            }
            if ((cmd.stderr != null && cmd.stderr) || (cmd.stderr == null && !ok())) {
                try {
                    stderr = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());
                } catch (IOException e) {
                    Log.e(TAG, "unable to get error", e);
                }
            }
        }

        public boolean ok() {
            return errno == 0 && e == null;
        }

        public Result must() {
            if (!ok())
                throw new RuntimeException(errno());
            return this;
        }

        public Exception errno() {
            if (stderr != null && !stderr.isEmpty())
                return SuperUser.errno(stderr, errno);
            if (e != null)
                return SuperUser.errno(JavaUtils.toMessage(e), errno);
            return SuperUser.errno("", errno);
        }
    }



    public static class FileOutputStream extends OutputStream {
        Process su;
        OutputStream os;

        public FileOutputStream(File f) throws IOException {
            Commands cmd = new Commands(MessageFormat.format(BIN_CAT + " > {0}", escape(f))).exit(true);
            su = Runtime.getRuntime().exec(BIN_SU);
            os = su.getOutputStream();
            writeString(cmd.build(), os);
        }

        @Override
        public void write(int b) throws IOException {
            os.write(b);
        }

        @Override
        public void write(@NonNull byte[] b, int off, int len) throws IOException {
            os.write(b, off, len);
        }

        @Override
        public void close() throws IOException {
            super.close();
            su.destroy();
        }
    }

    public static class RandomAccessFile {
        public int bs;

        Process su;
        public InputStream is;
        public OutputStream os;
        public long size;
        public long offset;

        public RandomAccessFile() {
        }

        public RandomAccessFile(int bs) {
            this.bs = bs;
        }

        public RandomAccessFile(File f, int bs) {
            this(bs);
            Commands cmd = new Commands(MessageFormat.format(STATLCS + "; while read offset size; do dd if={0} iseek=$offset count=$size bs={1}; done", escape(f), bs)).exit(true).stderr(false);
            try {
                su = Runtime.getRuntime().exec(BIN_SU);
                if (cmd.stderr != null && !cmd.stderr)
                    su.getErrorStream().close();
                os = new BufferedOutputStream(su.getOutputStream());
                writeString(cmd.build(), os);
                is = new BufferedInputStream(su.getInputStream());
                size = Long.valueOf(readLine().trim());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }



        public String readLine() throws IOException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int b;
            while ((b = is.read()) > 0) {
                if (b == 0x0a)
                    break;
                os.write(b);
            }
            return os.toString();
        }

        public int read(byte[] buf, int off, int size) throws IOException {
            long fs = offset / bs; // first sector
            long ls = (offset + size) / bs; // last sector
            int sc = (int) (ls - fs + 1); // sectors count
            long so = fs * bs; // first sector offset in bytes
            int skip = (int) (offset - so); // bytes to skip from first reading sector
            int length = sc * bs; // to read from pipe
            long eof = so + length;
            if (eof > this.size)
                length = (int) (this.size - so); // do not cross end of file
            writeString(fs + " " + sc + EOL, os);
            long len;
            while (skip > 0) {
                len = is.skip(skip);
                if (len <= 0)
                    throw new RuntimeException("unable to skip");
                skip -= len;
                length -= len;
            }
            int read = 0;
            while ((len = is.read(buf, off, size)) > 0) {
                off += len;
                offset += len;
                size -= len;
                length -= len;
                read += len;
            }
            while (length > 0) {
                len = is.skip(length);
                if (len <= 0)
                    throw new RuntimeException("unable to skip");
                length -= len;
            }
            return read;
        }

        public long getSize() {
            return size;
        }



        public long getPosition() {
            return offset;
        }

        public void close() throws IOException {
            is.close();
            os.close();
            su.destroy();
        }
    }
}

