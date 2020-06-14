package com.example.myapplication.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.List;

public class PermissionUtils {

    public static final int REQUEST_CRITICAL_PERMISSIONS = 10;
    private static final String[] CRITICAL_PERMISSION_ARRAY = {
            //要申请的权限，放在一个数组里面
            //Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE
    };

    public static void requirePermissions(Activity context){
        context.requestPermissions(CRITICAL_PERMISSION_ARRAY, REQUEST_CRITICAL_PERMISSIONS);
    }

    //判断是否有这个权限。。。
    public static boolean hasCriticalPermission(Context context) {
        for (int i = 0; i < CRITICAL_PERMISSION_ARRAY.length; i++) {
            if (context.checkSelfPermission(CRITICAL_PERMISSION_ARRAY[i])
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
