package com.oh.mp3test;

import android.app.Application;

public class mp3Application extends Application {
    private static mp3Application Instance;
    private Mp3ServiceInterFace Interface;

    @Override
    public void onCreate() {
        super.onCreate();
        Instance=this;
        Interface=new Mp3ServiceInterFace(getApplicationContext());
    }
    public static mp3Application getInstance(){
        return Instance;
    }
    public Mp3ServiceInterFace getServiceInterface(){
        return Interface;
    }
}
