package com.fftools.androidtv.testspeech;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.ContextCompat;

public class Util {
    public static boolean isPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            return Environment.isExternalStorageManager();
        }
        else {
            int readExtStorge = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            return readExtStorge == PackageManager.PERMISSION_GRANTED;
        }
    }
}
