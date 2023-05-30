package com.thenotesgiver.smooth_share.framework.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;


import com.thenotesgiver.smooth_share.framework.util.JavaUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Storage extends LeafStorage {

    public static final String TAG = Storage.class.getSimpleName();
    SuperUser.SuIO su;

    public Storage(Context context) {
        super(context);
    }




    public boolean delete(Uri t) { // default Storage.delete() uses 'rm -rf'
        String s = t.getScheme();
        if (s.equals(ContentResolver.SCHEME_FILE)) {
            File k = Storage.getFile(t);
            if (getRoot())
                return SuperUser.delete(getSu(), k).ok();
            else
                return k.delete();
        } else if (s.equals(ContentResolver.SCHEME_CONTENT)) {
            DocumentFile f = DocumentFile.fromSingleUri(context, t);
            assert f != null;
            return f.delete();
        } else {
            throw new UnknownUri();
        }
    }

    public boolean getRoot() {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
        return shared.getBoolean(JavaUtils.PREF_ROOT, false);
    }

    public SuperUser.SuIO getSu() {
        if (su == null)
            su = new SuperUser.SuIO();
        if (!su.valid()) {
            closeSu();
            su = new SuperUser.SuIO();
        }
        su.clear();
        if (!su.valid()) {
            closeSu();
            su = new SuperUser.SuIO();
        }
        return su;
    }












    public void closeSu() {
        try {
            if (su != null) {
                su.exit();
                su.close();
                su = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "close", e);
            su.close();
            su = null;
        }
    }













    public static class UriOutputStream {
        public Uri uri;
        public OutputStream os;

        public UriOutputStream(Uri u, OutputStream os) {
            this.uri = u;
            this.os = os;
        }

        public UriOutputStream(File f, OutputStream os) {
            this.uri = Uri.fromFile(f);
            this.os = os;
        }
    }

    public UriOutputStream write(Uri uri) throws IOException {
        String s = uri.getScheme();
        if (s.equals(ContentResolver.SCHEME_FILE)) {
            File k = getFile(uri);
            if (getRoot())
                return new UriOutputStream(k, new SuperUser.FileOutputStream(k));
            else
                return new UriOutputStream(k, new FileOutputStream(k));
        } else if (s.equals(ContentResolver.SCHEME_CONTENT)) {
            return new UriOutputStream(uri, resolver.openOutputStream(uri, "rwt"));
        } else {
            throw new UnknownUri();
        }
    }

    public UriOutputStream open(Uri uri, String name) throws IOException {
        String s = uri.getScheme();
        if (s.equals(ContentResolver.SCHEME_FILE)) {
            File k = getFile(uri);
            File m = new File(k, name);
            if (getRoot())
                return new UriOutputStream(m, new SuperUser.FileOutputStream(m));
            else
                return new UriOutputStream(m, new FileOutputStream(m));
        } else if (s.equals(ContentResolver.SCHEME_CONTENT)) {
            Uri doc = createDocumentFile(context, uri, name);
            return new UriOutputStream(doc, resolver.openOutputStream(doc, "rwt"));
        } else {
            throw new UnknownUri();
        }
    }
}
