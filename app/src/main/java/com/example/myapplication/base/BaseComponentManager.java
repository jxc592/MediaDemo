package com.example.myapplication.base;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Context;
import android.os.Message;

import java.util.HashMap;

public class BaseComponentManager {

    private static BaseComponentManager instance;

    private HashMap<String,Activity> mAliveActivities;
    private HashMap<String,Service> mAliveServices;
    private HashMap<String,ContentProvider> mAliveProviders;
    private HashMap<String,BroadcastReceiver> mAliveReceivers;

    private Application mApplication;

    private BaseComponentManager(){

        mAliveActivities = new HashMap<>();
        mAliveServices = new HashMap<>();
        mAliveProviders = new HashMap<>();
        mAliveReceivers = new HashMap<>();

    }

    public static BaseComponentManager getInstance() {
        if(instance == null) {
            instance = new BaseComponentManager();
        }
        return instance;
    }

    public void addComponent(Context context){

        String name = context.getClass().getName();
        if(context instanceof Activity) {
            mAliveActivities.put(name, (Activity) context);
        } else if (context instanceof Service) {
            mAliveServices.put(name, (Service) context);
        } else if (context instanceof Application) {
            mApplication = (Application) context;
        }
    }


    public void removeComponent(Context context){
        String name = context.getClass().getName();
        if(context instanceof Activity) {
            mAliveActivities.remove(name);
        } else if (context instanceof Service) {
            mAliveServices.remove(name);
        } else if (context instanceof Application) {
            mApplication = null;
        }
    }

    public void sendMessageTo(Class target, Message message){

    }

    public void sendMessageTo(Context curContext,Class target, Message message){

    }
}
