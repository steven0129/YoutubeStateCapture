package com.example.youtubestatecap;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class MyApplication extends Application {
    public static final String TAG = "YoutubeStateCap";


    @Override
    public void onCreate() {
        Log.i(TAG, "Application Created!!");
        super.onCreate();
        startForegroundService(new Intent(this, MediaControllerService.class));
    }
}
