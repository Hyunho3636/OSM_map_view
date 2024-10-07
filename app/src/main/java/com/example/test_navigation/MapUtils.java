package com.example.test_navigation;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MapUtils {
    private static final String TAG = "MapUtils";

    public static boolean copyMBTilesToInternalStorage(Context context, String fileName) {
        try {
            File internalStorageDir = new File(context.getFilesDir(), "maps");
            if (!internalStorageDir.exists()) {
                boolean dirCreated = internalStorageDir.mkdirs();
                Log.d(TAG, "Maps directory created: " + dirCreated);
            }

            File destFile = new File(internalStorageDir, fileName);
            Log.d(TAG, "Destination file path: " + destFile.getAbsolutePath());

            if (!destFile.exists()) {
                InputStream in = context.getAssets().open(fileName);
                OutputStream out = new FileOutputStream(destFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                in.close();
                out.close();
                Log.d(TAG, "MBTiles file copied successfully");
            } else {
                Log.d(TAG, "MBTiles file already exists");
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error copying MBTiles file", e);
            return false;
        }
    }
}