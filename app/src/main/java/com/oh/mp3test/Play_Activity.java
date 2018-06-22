package com.oh.mp3test;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class Play_Activity extends AppCompatActivity implements View.OnClickListener{
    Mp3Adapter mp3Adapter;
    ImageView albumart,iv_play;
    TextView tv_title,tv_artist,tv_album, duration_end;
    public  static TextView duration_start;
    public static SeekBar sb,volume;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_);
        mp3Adapter=new Mp3Adapter(this,null);
        albumart = findViewById(R.id.p_album);
        tv_title = findViewById(R.id.songtitle);
        tv_artist = findViewById(R.id.songartist);
        tv_album = findViewById(R.id.albumtitle);
        iv_play=findViewById(R.id.play);
        duration_start = findViewById(R.id.starttime);
        duration_end = findViewById(R.id.endtime);
        sb=findViewById(R.id.p_seekBar);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.next).setOnClickListener(this);
        findViewById(R.id.songlist).setOnClickListener(this);
        findViewById(R.id.more).setOnClickListener(this);
        albumart.setOnClickListener(this);
        iv_play.setOnClickListener(this);
        sb=findViewById(R.id.p_seekBar);
        volume=findViewById(R.id.seekBar2);
        getAudioMDB();
        registerBroadcast();
        updateUI();
        final AudioManager audioManager= (AudioManager) getSystemService(AUDIO_SERVICE);
        int nMax=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int nCurrentVolumn=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        sb.setMax(MediaPlayerService.mp.getDuration());
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    MediaPlayerService.mp.seekTo(progress);
                    seekBar.setProgress(progress);
                    updateUI();
                }
                duration_start.setText(android.text.format.DateFormat.format("mm:ss",progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        volume.setMax(nMax);
        volume.setProgress(nCurrentVolumn);
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.p_album:
                //가사 보여주기
                break;
            case R.id.back:
                mp3Application.getInstance().getServiceInterface().rewind();
                break;
            case R.id.next:
                mp3Application.getInstance().getServiceInterface().forward();
                break;
            case R.id.play:
                mp3Application.getInstance().getServiceInterface().toggleplay();
                break;
            case R.id.songlist:
                //재생목록 보여주기

                break;
            case R.id.more:
                //다이얼로그 보여주기
                Intent intent=new Intent(this,Dialog_Activity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager manager= (AudioManager) getSystemService(AUDIO_SERVICE);
        int index=volume.getProgress();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                manager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);
                volume.setProgress(index + 1);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                manager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);
                volume.setProgress(index - 1);
                return true;
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };
    public void registerBroadcast(){
        IntentFilter filter=new IntentFilter();
        filter.addAction(BroadcastActions.PLAY_STATE_CHANGED);
        filter.addAction(BroadcastActions.PREPARED);
        registerReceiver(broadcastReceiver,filter);
    }
    public void unregisterBroadcast(){
        unregisterReceiver(broadcastReceiver);
    }
    private void updateUI(){
        if (mp3Application.getInstance().getServiceInterface().isPlaying()){
                iv_play.setImageResource(R.drawable.ic_pause);
                Thread();
        }else {
            iv_play.setImageResource(R.drawable.ic_play);

        }

        Mp3Adapter.Mp3Item item=mp3Application.getInstance().getServiceInterface().getmp3Item();
        if (item!=null){
            Uri albumArtUri= ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),item.albumID);
            Glide.with(this).load(albumArtUri).error(Glide.with(this).load(R.drawable.musicicon)).into(albumart);
            tv_title.setText(item.music_title);
            tv_title.setSingleLine();
            tv_title.setSelected(true);
            tv_title.setEllipsize(TextUtils.TruncateAt.MARQUEE);

            tv_artist.setText(item.music_artist);
            tv_artist.setSingleLine();
            tv_artist.setSelected(true);
            tv_artist.setEllipsize(TextUtils.TruncateAt.MARQUEE);

            tv_album.setText(item.music_album);
            tv_album.setSingleLine();
            tv_album.setFocusableInTouchMode(true);
            tv_album.requestFocus();
            sb.setMax(MediaPlayerService.mp.getDuration());
            duration_end.setText(android.text.format.DateFormat.format("mm:ss",item.music_duration));
        }else {
            albumart.setImageResource(R.drawable.musicicon);
            tv_title.setText("재생중인 음악이 없습니다.");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }
    public void getAudioMDB(){

        getSupportLoaderManager().initLoader(1, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] proj=new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA
                };
                String selection=MediaStore.Audio.Media.IS_MUSIC+"=1";
                String sortOrder=MediaStore.Audio.Media.TITLE+" COLLATE LOCALIZED ASC";
                return new CursorLoader(getApplicationContext(),uri,proj,selection,null,sortOrder);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mp3Adapter.swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mp3Adapter.swapCursor(null);
            }
        });
    }
    public void Thread(){
        Runnable task=new Runnable() {
            @Override
            public void run() {
                while (mp3Application.getInstance().getServiceInterface().isPlaying()) {
                    int currentPosition=MediaPlayerService.mp.getCurrentPosition();
                    sb.setProgress(currentPosition);
                }
            }
        };
        thread=new Thread(task);
        thread.start();
    }
}
