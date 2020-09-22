package com.example.myapplication.base;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Context;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;

public class BaseComponentManager {

    private static BaseComponentManager instance;

    private HashMap<String,Activity> mAliveActivities;
    private HashMap<String,Service> mAliveServices;

    private Application mApplication;

    private BaseComponentManager(){

        mAliveActivities = new HashMap<>();
        mAliveServices = new HashMap<>();
    }

    public static BaseComponentManager getInstance() {
        if(instance == null) {
            instance = new BaseComponentManager();
        }
        return instance;
    }

    public void addComponent(Context context){
        Log.d("JXC","" + context.getClass()==null? "class null":context.getClass().getName());
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
        String name = target.getName();
        if(mAliveActivities.get(name) !=null) {
            BaseHandlerActivity baseHandlerActivity = (BaseHandlerActivity) mAliveActivities.get(name);
            baseHandlerActivity.sendMessage(message);
        }
        if(mAliveServices.get(name) !=null) {
            BaseService baseService = (BaseService) mAliveServices.get(name);
            baseService.sendMessage(message);
        }

    }

}
