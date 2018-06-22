package com.oh.mp3test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class Mp3ServiceInterFace {
    private ServiceConnection serviceConnection;
    private MediaPlayerService mservice;

    public Mp3ServiceInterFace(Context context){
        serviceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mservice=((MediaPlayerService.AudioServiceBinder)service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceConnection=null;
                mservice=null;
            }
        };
        context.bindService(new Intent(context,MediaPlayerService.class)
                .setPackage(context.getPackageName()),serviceConnection,Context.BIND_AUTO_CREATE);
    }
    public void setPlayList(ArrayList<Long> mp3ids){
        if (mservice!=null){
            mservice.setPlayList(mp3ids);
        }
    }
    public void play(int position){
        if (mservice!=null){
            mservice.playmusic(position);
        }
    }
    public void play(){
        if (mservice!=null){
            mservice.playmusic();
        }
    }
    public void pause(){
        if (mservice!=null){
            mservice.pause();
        }
    }
    public void forward(){
        if (mservice!=null){
            mservice.forward();
        }
    }
    public void rewind(){
        if (mservice!=null){
            mservice.rewind();
        }
    }
    public void loop(){
        if (mservice!=null){
            mservice.loop();
        }
    }
    public void toggleplay(){
        if (isPlaying()){
            mservice.pause();
        }else {
            mservice.playmusic();
        }
    }
    public boolean isPlaying(){
     if (mservice!=null){
         return mservice.isPlaying();
     }
     return false;
    }
    public void Alllooping(){
       mservice.Allloop();
    }
    public Mp3Adapter.Mp3Item getmp3Item(){
        if (mservice!=null){
            return mservice.getMp3Item();
        }
        return null;
    }
    public P4_mp3Adapter.Mp3Item getP4_mp3Item(){
        if (mservice!=null){
            return mservice.getP4mp3Item();
        }
        return null;
    }
    public void shuffleplay(){
      mservice.shuffleplay();

    }

}
