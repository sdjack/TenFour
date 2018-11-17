package com.steve-jackson-studios.tenfour.IO;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by sjackson on 7/25/2017.
 * FileIO
 */

public class FileIO {

    private FileIO() {}

    public static boolean saveToJsonFile(Context context, String fileName, String rawJson) {
        fileName = (fileName.endsWith(".json")) ? fileName : fileName + ".json";
        //Log.d("FileIO", "saveToJsonFile >>> " + fileName);
        try {
            File file = new File(context.getFilesDir(), "data");
            if (!file.exists()){
                file.mkdir();
            }
            File jsonFile = new File(file, fileName);
            if (!jsonFile.exists()){
                jsonFile.createNewFile();
            }
            FileWriter filewriter = new FileWriter(jsonFile);
            filewriter.write(rawJson);
            filewriter.flush();
            filewriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d("FileIO", "saveToJsonFile >>> ERROR");
        return false;
    }

    public static JSONArray readFromJsonFile(Context context, String fileName) throws Resources.NotFoundException, JSONException {
        fileName = (fileName.endsWith(".json")) ? fileName : fileName + ".json";
        //Log.d("FileIO", "readFromJsonFile >>> " + fileName);
        String rawJson = null;
        try {
            File file = new File(context.getFilesDir(), "data");
            if (file.exists()) {
                File jsonFile = new File(file, fileName);
                if (jsonFile.exists()){
                    FileInputStream is = new FileInputStream(jsonFile);
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    rawJson = new String(buffer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (rawJson != null) {
            return new JSONArray(rawJson);
        } else {
            //Log.d("FileIO", "readFromJsonFile >>> ERROR");
            return new JSONArray();
        }
    }

    public static JSONObject loadRawJsonObject(Context context, int resourceId) throws Resources.NotFoundException, JSONException {
        InputStream input = context.getResources().openRawResource(resourceId);

        String output;
        try {
            byte[] var3 = getByteArray(input);
            output = new String(var3, "UTF-8");
            return new JSONObject(output);
        } catch (IOException e) {
            output = String.valueOf(e);
            throw new Resources.NotFoundException((new StringBuilder(37 + String.valueOf(output).length())).append("Failed to read resource ").append(resourceId).append(": ").append(output).toString());
        }
    }

    public static JSONArray loadRawJsonArray(Context context, int resourceId) throws Resources.NotFoundException, JSONException {
        InputStream input = context.getResources().openRawResource(resourceId);

        String output;
        try {
            byte[] var3 = getByteArray(input);
            output = new String(var3, "UTF-8");
            return new JSONArray(output);
        } catch (IOException e) {
            output = String.valueOf(e);
            throw new Resources.NotFoundException((new StringBuilder(37 + String.valueOf(output).length())).append("Failed to read resource ").append(resourceId).append(": ").append(output).toString());
        }
    }

    private static void closeInputStream(Closeable var0) {
        if(var0 != null) {
            try {
                var0.close();
            } catch (IOException var2) {
                ;
            }
        }

    }

    private static byte[] getByteArray(InputStream var0) throws IOException {
        return getByte(var0, true);
    }

    private static void getByte(ParcelFileDescriptor var0) {
        if(var0 != null) {
            try {
                var0.close();
            } catch (IOException var2) {
                ;
            }
        }

    }

    private static long getByte(InputStream var0, OutputStream var1) throws IOException {
        return getByte(var0, var1, false);
    }

    private static long getByte(InputStream var0, OutputStream var1, boolean var2) throws IOException {
        return getByte(var0, var1, var2, 1024);
    }

    private static long getByte(InputStream var0, OutputStream var1, boolean var2, int var3) throws IOException {
        byte[] var4 = new byte[var3];
        long var5 = 0L;

        int var7;
        try {
            while((var7 = var0.read(var4, 0, var3)) != -1) {
                var5 += (long)var7;
                var1.write(var4, 0, var7);
            }
        } finally {
            if(var2) {
                closeInputStream(var0);
                closeInputStream(var1);
            }

        }

        return var5;
    }

    private static byte[] getByte(InputStream var0, boolean var1) throws IOException {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        getByte(var0, var2, var1);
        return var2.toByteArray();
    }
}
