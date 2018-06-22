package com.oh.mp3test;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import android.os.Handler;

public class MediaPlayerService extends Service {
    boolean isReady;
    boolean isPrepared;
    boolean isForeground;
    public static MediaPlayer mp;
    boolean shuffle;
    private final IBinder binder= new AudioServiceBinder();
    private static final String MEDIA_PATH=new String("/sdcard/");
    private boolean ongoingCall=false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private int Position;
    private MediaSessionCompat mediaSessionCompat;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    public class AudioServiceBinder extends Binder {
        MediaPlayerService getService(){
            return MediaPlayerService.this;
        }
    }//AudioServiceBinder

    @Override
    public void onCreate() {
        super.onCreate();

        mp=new MediaPlayer();
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isReady=true;
                mp.start();
                sendBroadcast(new Intent(BroadcastActions.PREPARED)); // 원본 : BroadcastActions.PREPARED

            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                    isReady=false;
                    sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));

            }
        });
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                    isReady=false;
                    sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
                    return false;
            }
        });
        mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {

            }
        });
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared=true;
                mp.start();
                sendBroadcast(new Intent(BroadcastActions.PREPARED)); // 원본 : BroadcastActions.PREPARED

            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPrepared=false;
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));

            }
        });
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isPrepared=false;
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));

                return false;
            }
        });
        callStateListener();
    }//onCreate


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mp!=null){
            mp.stop();
            mp.release();
            mp=null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationmp();
        }
    }
    private ArrayList<Long> AudioIds=new ArrayList<>();
    public void setPlayList(ArrayList<Long> audioIds){
        if (AudioIds.size()!=audioIds.size()){
            if (!AudioIds.equals(audioIds)){
                AudioIds.clear();
                AudioIds.addAll(audioIds);
            }
        }
    }
    private int mCurrentPosition;
    private Mp3Adapter.Mp3Item mp3Item;
    private P4_mp3Adapter.Mp3Item p4mp3Item;
    private void queryMp3Item(int position){
        mCurrentPosition=position;
        long audioId=AudioIds.get(position);
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
        String selection=MediaStore.Audio.Media._ID+" = ?";
        String[] selectionArgs={String.valueOf(audioId)};
        Cursor cursor=getContentResolver().query(uri,proj,selection,selectionArgs,null);
        if (cursor!=null){
            if (cursor.getCount()>0) {
                cursor.moveToFirst();
                mp3Item = Mp3Adapter.Mp3Item.bindCursor(cursor);
                p4mp3Item=P4_mp3Adapter.Mp3Item.bindCursor(cursor);
            }
        }
        cursor.close();
    }//query
private void prepare(){
    try {
        mp.setDataSource(mp3Item.data_path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mp.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            mp.prepareAsync();
        }else {
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.prepareAsync();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    public void playmusic(int position){
        queryMp3Item(position);
        stop();
        mp3prepad();
        prepare();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationmp();////아이콘이 바뀌는곳
                    }
                }
            },500);
        }
    }
    public void playmusic(){
       if (isPrepared){
           mp.start();
           sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
               notificationmp();
           }
       }

    }
    public void pause(){
       if (isPrepared){
           mp.pause();
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
               notificationmp();
           }
           sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));

       }
    }
    public void stop(){
        if (mp!=null){
            mp.stop();
            mp.release();
            mp=null;
        }
    }
    public void Allloop(){
     mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
         @Override
         public void onCompletion(MediaPlayer mp) {
             mp.setLooping(true);
             mp.start();
         }
     });
    }
    public void loop(){
        if (!mp.isLooping()){
            mp.setLooping(true);
            mp.start();
        }else {
            mp.setLooping(false);
        }
    }
    public void forward(){
        if (AudioIds.size()- 1>mCurrentPosition){
            mCurrentPosition++;
        }else {
            mCurrentPosition=0;
        }
        playmusic(mCurrentPosition);
    }
    public void rewind(){
        if (mCurrentPosition>0){
            mCurrentPosition--;
        }else {
            mCurrentPosition= AudioIds.size()-1;
        }
        playmusic(mCurrentPosition);
    }
    public Mp3Adapter.Mp3Item getMp3Item(){
        return mp3Item;
    }
    public P4_mp3Adapter.Mp3Item getP4mp3Item(){return p4mp3Item;}
    public boolean isPlaying(){
       return mp.isPlaying();
    }
    public void mp3prepad(){
        mp=new MediaPlayer();
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isReady=true;
                mp.start();
            }
        });
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isReady=false;
                return false;
            }
        });
        mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                    mp.start();
            }
        });
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared=true;
                mp.start();
                sendBroadcast(new Intent(BroadcastActions.PREPARED));
            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (onError == true) {
                    onError = false;
                    return;
                }
                isPrepared=false;
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
                nextSong();
            }
        });
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                onError = true;
                isPrepared=false;
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED));
                return false;
            }
        });
    }

    boolean onError = false;

    private void nextSong(){
        if (Play_Activity.sb!=null)             Play_Activity.sb.setProgress(0);
        if (++mCurrentPosition >= AudioIds.size()) mCurrentPosition = 0;
        playmusic(mCurrentPosition);
    }
    private void callStateListener(){
        telephonyManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        phoneStateListener=new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state){
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mp!=null){
                            pause();
                            ongoingCall=true;
                        }
                        break;
                     case TelephonyManager.CALL_STATE_IDLE:
                         if (mp!=null){
                             if (ongoingCall){
                                 ongoingCall=false;
                                 playmusic();
                             }
                         }
                         break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);
    }
   public void shuffleplay(){
       Collections.shuffle(AudioIds);
       playmusic();
   }
   public void onAudioFocusChange(int focusState) {
       switch (focusState) {
           case AudioManager.AUDIOFOCUS_GAIN:
               if (mp == null) mp.start();
               else if (!mp.isPlaying()) mp.start();
               break;
           case AudioManager.AUDIOFOCUS_LOSS:
               if (mp.isPlaying()) mp.stop();
               mp.release();
               mp = null;
               break;
           case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
               if (mp.isPlaying()) mp.pause();
               break;
           case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
               if (mp.isPlaying()) mp.setVolume(0.5f, 0.5f);
               break;
       }
   }
    private BroadcastReceiver becomingNoisyReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pause();
        }
    };
    private void registerBecomingNoisyReceiver(){
        IntentFilter intentFilter=new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver,intentFilter);
    }
   @RequiresApi(api = Build.VERSION_CODES.O)
   public void notificationmp(){
       mediaSessionCompat=new MediaSessionCompat(this,"Musicservice");
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS|MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        PlaybackStateCompat playbackStateCompat=new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS|PlaybackStateCompat.ACTION_SKIP_TO_NEXT|
                PlaybackStateCompat.ACTION_PLAY_PAUSE|PlaybackStateCompat.ACTION_STOP
        ).setState(isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,mCurrentPosition,1.0f).build();
        mediaSessionCompat.setPlaybackState(playbackStateCompat);
        mediaSessionCompat.setActive(true);

        String CHANNEL_ID="mp_channel";
        android.support.v4.app.NotificationCompat.Builder builder=new android.support.v4.app.NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID);
       final NotificationManager notificationManager= (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
       String id=CHANNEL_ID;
       CharSequence name="Media playback";
       String description="Media playback controls";
       Mp3Adapter.Mp3Item item = mp3Application.getInstance().getServiceInterface().getmp3Item();

          Intent actionTogglePlay=new Intent(CommandActions.TOGGLE_PLAY);
          Intent actionForward=new Intent(CommandActions.FORWARD);
          Intent actionRewind=new Intent(CommandActions.REWIND);
          Intent actionClose=new Intent(CommandActions.CLOSE);
          PendingIntent togglePlay=PendingIntent.getService(getApplicationContext(),0,actionTogglePlay,0);
          PendingIntent forward=PendingIntent.getService(getApplicationContext(),0,actionForward,0);
          PendingIntent rewind=PendingIntent.getService(getApplicationContext(),0,actionRewind,0);
          PendingIntent close=PendingIntent.getService(getApplicationContext(),0,actionClose,0);
          int importance = NotificationManager.IMPORTANCE_LOW;
          NotificationChannel channel = new NotificationChannel(id, name, importance);
          channel.setDescription(description);
          channel.setShowBadge(false);
          channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
          notificationManager.createNotificationChannel(channel);
          final android.support.v4.app.NotificationCompat.Builder notificationBuilder = new android.support.v4.app.NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
          notificationBuilder.setStyle(new NotificationCompat.MediaStyle()
                  .setMediaSession(mediaSessionCompat.getSessionToken())
                  .setShowCancelButton(true)
                  .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_STOP)))
                  .setColor(ContextCompat.getColor(getApplicationContext(), R.color.white))
                  .setSmallIcon(R.drawable.musicicon2)
                  .setVisibility(android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC)
                  .setOnlyAlertOnce(true)
                  .setContentIntent(PendingIntent.getActivity(getApplicationContext(),2,new Intent(getApplicationContext(),Play_Activity.class),0))
                  .setContentTitle(mp3Item.music_title)
                  .setContentText(mp3Item.music_artist)
                  .setSubText(mp3Item.music_title)
                  .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_STOP))
                  .addAction(new android.support.v4.app.NotificationCompat.Action(R.drawable.ic_back,"",rewind))
                  .addAction(new android.support.v4.app.NotificationCompat.Action(isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play,"",togglePlay))
                  .addAction(new android.support.v4.app.NotificationCompat.Action(R.drawable.ic_next,"",forward))
                  .addAction(new android.support.v4.app.NotificationCompat.Action(R.drawable.ic_close,"",close));
                    Log.d("로그",""+isPlaying());


          Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
          ContextCompat.startForegroundService(getApplicationContext(), intent);
       if (mp!=null) {
           Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), item.albumID);
           RequestBuilder<Bitmap> bitmapRequestBuilder = Glide.with(this).asBitmap().load(R.drawable.musicicon);
           Glide.with(getApplicationContext()).asBitmap().error(bitmapRequestBuilder).load(albumArtUri).into(new SimpleTarget<Bitmap>() {
               @Override
               public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                   notificationBuilder.setLargeIcon(resource);
                   notificationManager.notify(3,notificationBuilder.build());
               }
           });
      }
      if (!isForeground) {
           isForeground=true;
          startForeground(3, notificationBuilder.build());
      }
    }

    public class CommandActions{
        public final static String REWIND="REWIND";
        public final static String TOGGLE_PLAY="TOGGLE_PLAY";
        public final static String FORWARD="FORWARD";
        public final static String CLOSE="CLOSE";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent !=null){
            String action=intent.getAction();
            if (CommandActions.TOGGLE_PLAY.equals(action)){
                if (isPlaying()){
                    pause();
                }else {
                    playmusic();
                }
            }else if (CommandActions.REWIND.equals(action)){
                rewind();
            }else if (CommandActions.FORWARD.equals(action)){
                forward();
            }else if (CommandActions.CLOSE.equals(action)){
                stopForeground(true);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }



}