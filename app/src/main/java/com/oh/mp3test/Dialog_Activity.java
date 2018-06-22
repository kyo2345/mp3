package com.oh.mp3test;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Dialog_Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_);
        findViewById(R.id.playlistadd);
        findViewById(R.id.shared);
        findViewById(R.id.songinfo);
        findViewById(R.id.del);
        findViewById(R.id.cancle_tv);
    }
    public void clickaddplaylist(View view) {

    }

    public void clicksns(View view) {
        shareTwitter();
    }
    public void clickinfo(View view) {
        Intent intent=new Intent(this,Musicinfo_Activity.class);
        startActivity(intent);
    }

    public void clickdel(View view) {
        del();
    }
    public void clickcancel(View view) {
        finish();
    }

    private void del(){
      File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator);
      file.delete();
    }
    private void shareTwitter(){
        String strLink=null;
        try{
            Mp3Adapter.Mp3Item item=mp3Application.getInstance().getServiceInterface().getmp3Item();
            strLink=String.format("http://twitter.com/intent/tweet?text=%s", URLEncoder.encode("#Nowplaying "+item.music_title+"-"+item.music_artist+"듣는중!"+" #Ohmusic","utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(strLink));
        startActivity(intent);
    }
    public void makeplaylist(){
;       String name="playlist";
        ContentResolver resolver=getContentResolver();
        Cursor c=resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,null,MediaStore.Audio.Playlists.NAME+"=?",new String[]{name},null);
        c.moveToFirst();
        if (!c.isAfterLast()){
            c.close();
            return;
        }c.close();
        ContentValues values=new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME,"playlist");
        resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,values);

    }
    public void playlist(){
        String name="playlist";
        ContentResolver resolver=getContentResolver();
        Cursor cursor=resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,new String[]{MediaStore.Audio.Playlists._ID},MediaStore.Audio.Playlists.NAME+"=?",new String[]{name},null);
        cursor.moveToFirst();
        int listid=cursor.getInt(0);
        cursor.close();

        Uri uri=MediaStore.Audio.Playlists.Members.getContentUri("external",listid);
        ContentValues values=new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER," COLLATE LOCALIZED ASC");
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID,1);
        resolver.insert(uri,values);
    }
}