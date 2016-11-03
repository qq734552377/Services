package com.project.services.tool;

import android.app.Application;
import android.content.Context;

import org.apache.log4j.Logger;


public class ExceptionApplication extends Application {
    public static Context context;
    public static Logger gLogger;

    public void onCreate() {
        super.onCreate();
        //设置Thread Exception Handler

        context = this;//getApplicationContext();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());


        LogUtil.configLog();
        gLogger= Logger.getLogger(ExceptionApplication.class);


        //输出MyApplication的信息
        gLogger.info("Log4j Is Ready and My Services Application Was Created Successfully! ");


    }
}


