package com.thenotesgiver.smooth_share.framework.app;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import android.provider.DocumentsContract;
import android.webkit.MimeTypeMap;


import com.thenotesgiver.smooth_share.framework.util.JavaUtils;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class LeafStorage {

 public static final String SAF = "com.android.externalstorage";


    public static final String CONTENTTYPE_OCTETSTREAM = "application/octet-stream";
    public static final String CONTENTTYPE_OPUS = "audio/opus";
    public static final String CONTENTTYPE_OGG = "audio/ogg";
    public static final String CONTENTTYPE_FB2 = "application/x-fictionbook";
    public static final String CONTENTTYPE_RAR = "application/rar";



    public static final String COLON = ":";


    protected Context context;
    protected ContentResolver resolver;

    // String functions










    public static String getExt(String name) { // FilenameUtils.getExtension(n)
        int i = name.lastIndexOf('.');
        if (i > 0)
            return name.substring(i + 1);
        return "";
    }



    public static String getTypeByExt(String ext) {
        if (ext == null || ext.isEmpty())
            return CONTENTTYPE_OCTETSTREAM; // replace 'null'
        ext = ext.toLowerCase();
        switch (ext) {
            case "opus":
                return CONTENTTYPE_OPUS; // android missing
            case "ogg":
                return CONTENTTYPE_OGG; // replace 'application/ogg'
            case "fb2":
                return CONTENTTYPE_FB2;
            case "rar":
                return CONTENTTYPE_RAR;
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        if (type == null)
            return CONTENTTYPE_OCTETSTREAM;
        return type;
    }

    // file functions

    public static File getFile(Uri u) {
        return new File(u.getPath());
    }







    public static boolean delete(File f) {
        return FileUtils.deleteQuietly(f);
    }

    public static File copy(File f, File to) {
        long last = f.lastModified();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(f));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(to));
            IOUtils.copy(in, out);
            in.close();
            out.close();
            if (last > 0)
                to.setLastModified(last);
            return to;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




  
    public static Uri createDocumentFile(Context context, Uri u, String name) throws FileNotFoundException {
        if (!DocumentsContract.isDocumentUri(context, u)) // tree uri?
            u = DocumentsContract.buildDocumentUriUsingTree(u, DocumentsContract.getTreeDocumentId(u));
        ContentResolver resolver = context.getContentResolver();
        String ext = getExt(name);
        String mime = getTypeByExt(ext);
        return DocumentsContract.createDocument(resolver, u, mime, name);
    }

 
  
    @SuppressLint("Range")
    public static String getQueryName(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, null, null, null, null); // can throw UnsupportedOperationException
            if (cursor != null && cursor.moveToNext())
                return cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
        } catch (Exception ignore) { // UnsupportedOperationException | IllegalArgumentException
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return uri.getLastPathSegment();
    }



  
    public static String getDocumentName(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String id = DocumentsContract.getDocumentId(uri);
            if (Build.VERSION.SDK_INT >= 24 && DocumentsContract.isTreeUri(uri)) {
                String pid = DocumentsContract.getTreeDocumentId(uri);
                if (pid.equals(id))
                    return JavaUtils.ROOT;
                if (!pid.contains(COLON)) // isDocumentHomeUri()
                    return getQueryName(context, uri);
            }
            String[] ss = id.split(COLON, 2);
            if (ss[1].isEmpty())  // unknown id format or root
                return getQueryName(context, uri); // DocumentFile.getName() return null for non existent files
            else
                return new File(ss[1]).getName(); // not using query when it is possible
        } else {
            return JavaUtils.ROOT;
        }
    }

 
    public static String getContentName(Context context, Uri uri) {
        if (uri.getAuthority().startsWith(SAF)) // query crashed for DocumentsContract.isTreeUri() uris
            return getDocumentName(context, uri);
        else
            return getQueryName(context, uri);
    }

  


   

    

  
   


    // file and content twists

    public static String getName(Context context, Uri uri) {
        String s = uri.getScheme();
        if (s.equals(ContentResolver.SCHEME_CONTENT)) { // all SDK_INT
            return getContentName(context, uri);
        } else if (s.equals(ContentResolver.SCHEME_FILE)) {
            return getFile(uri).getName();
        } else {
            throw new UnknownUri();
        }
    }

   


    public static boolean delete(Context context, Uri f) throws FileNotFoundException {
        ContentResolver resolver = context.getContentResolver();
        String s = f.getScheme();
        if (Build.VERSION.SDK_INT >= 21 && s.equals(ContentResolver.SCHEME_CONTENT)) {
            return DocumentsContract.deleteDocument(resolver, f);
        } else if (s.equals(ContentResolver.SCHEME_FILE)) {
            File ff = getFile(f);
            return delete(ff);
        } else {
            throw new UnknownUri();
        }
    }

  
 
  
    

  




    public static ArrayList<Node> list(Context context, Uri uri) {
        return list(context, uri, null);
    }

    public static ArrayList<Node> list(Context context, Uri uri, NodeFilter filter) { // Node.name = file name, return _no_ root uris
        ArrayList<Node> files = new ArrayList<>();
        String s = uri.getScheme();
        if (s.equals(ContentResolver.SCHEME_FILE)) {
            File file = getFile(uri);
            File[] ff = file.listFiles();
            if (ff != null) {
                for (File f : ff) {
                    Node n = new Node(f);
                    if (filter == null || filter.accept(n))
                        files.add(n);
                }
                return files;
            }
            throw new RuntimeException("Unable to read");
        } else if (s.equals(ContentResolver.SCHEME_CONTENT)) {
            String id;
            if (DocumentsContract.isDocumentUri(context, uri))
                id = DocumentsContract.getDocumentId(uri);
            else
                id = DocumentsContract.getTreeDocumentId(uri);
            Uri doc = DocumentsContract.buildChildDocumentsUriUsingTree(uri, id);
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(doc, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Node n = new Node(doc, cursor);
                    if (filter == null || filter.accept(n))
                        files.add(n);
                }
                cursor.close();
                return files;
            }
            throw new RuntimeException("Unable to read");
        } else {
            throw new UnknownUri();
        }
    }

   
  
    // classes

    public static class UnknownUri extends RuntimeException {
    }

    @SuppressLint("Range")
    public static class Node {
        public Uri uri;
        public String name;
        public boolean dir;
        public long size;
        public long last;

       

        public Node(Uri uri, String n, boolean dir, long size, long last) {
            this.uri = uri;
            this.name = n;
            this.dir = dir;
            this.size = size;
            this.last = last;
        }

        public Node(File f) {
            this.uri = Uri.fromFile(f);
            this.name = f.getName();
            this.dir = f.isDirectory();
            this.size = f.length();
            this.last = f.lastModified();
        }

      

       
        public Node(Uri doc, Cursor cursor) {
            String id = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
            String type = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
            name = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
            size = cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
            last = cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
            uri = DocumentsContract.buildDocumentUriUsingTree(doc, id);
            dir = type.equals(DocumentsContract.Document.MIME_TYPE_DIR);
        }

        public String toString() { // display version of file name
            if (dir)
                return name.endsWith(JavaUtils.ROOT) ? name : name + JavaUtils.ROOT;
            return name;
        }
    }

    public interface NodeFilter {
        boolean accept(Node n);
    }


   

    public LeafStorage(Context context) {
        this.context = context;
        this.resolver = context.getContentResolver();
    }

    public Context getContext() {
        return context;
    }


    public static File getLocalInternal(Context context) {
        File file = context.getFilesDir();
        if (file == null)
            return getDataDir(context);
        return file;
    }

    public static File getDataDir(Context context) {
        return new File(context.getApplicationContext().getApplicationInfo().dataDir);
    }

 



   
 
}
