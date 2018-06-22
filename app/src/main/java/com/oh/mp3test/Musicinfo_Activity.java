package com.oh.mp3test;

import android.content.ContentUris;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

public class Musicinfo_Activity extends AppCompatActivity {
    TextView info_title,info_artist,info_album,info_Duration,info_genre,info_date,info_songnum,info_type,info_bit,info_route;
    ImageView info_albumart;
    Mp3Adapter.Mp3Item mmp3Item =mp3Application.getInstance().getServiceInterface().getmp3Item();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicinfo_);
        info_albumart=findViewById(R.id.info_albumart);
        info_title=findViewById(R.id.info_title);
        info_artist=findViewById(R.id.info_artist);
        info_album=findViewById(R.id.info_album);
        info_Duration=findViewById(R.id.info_Du);
        info_genre=findViewById(R.id.info_genre);
        info_date=findViewById(R.id.info_Date);
        info_songnum=findViewById(R.id.info_songnum);
        info_type=findViewById(R.id.info_type);
        info_bit=findViewById(R.id.info_bit);
        info_route=findViewById(R.id.info_route);
        setAudioItem();



    }

    public void setAudioItem(){
        Uri artworkUri=Uri.parse("content://media/external/audio/albumart");
        Uri albumArtUri=ContentUris.withAppendedId(artworkUri,mmp3Item.albumID);
        Glide.with(getApplicationContext()).load(albumArtUri).error(Glide.with(this).load(R.drawable.musicicon)).into(info_albumart);
        info_title.setText(mmp3Item.music_title);
        info_artist.setText(mmp3Item.music_artist);
        info_Duration.setText(android.text.format.DateFormat.format("mm:ss",mmp3Item.music_duration));
        info_album.setText(mmp3Item.music_album);

       MediaMetadataRetriever mr=new MediaMetadataRetriever();
        Uri trackUri=ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,mmp3Item.audioID);
        mr.setDataSource(getApplicationContext(),trackUri);
        String Genre=mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
        String Date=mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
        String songnum=mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
        String type=mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
        String bit=mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);

        info_genre.setText(Genre);
        info_date.setText(Date);
        info_songnum.setText(songnum);
        info_type.setText(type);
        info_bit.setText(bit);
        info_route.setText(mmp3Item.data_path);
        info_route.setSingleLine();
        info_route.setSelected(true);
        info_route.setEllipsize(TextUtils.TruncateAt.MARQUEE);



    }



}
