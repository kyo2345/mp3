package com.oh.mp3test;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class P4_mp3Adapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {
    Context context;
    public P4_mp3Adapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context=context;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        Mp3Item mp3Item=Mp3Item.bindCursor(cursor);
        ((mp3ViewHolder)viewHolder).setMp3Item(mp3Item,cursor.getPosition());

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.p4_rec_item,parent,false);
        return new mp3ViewHolder(v);
    }
    public static class Mp3Item {
        public long audioID;
        public long albumID;
        public String music_title;
        public String music_artist;
        public String music_album;
        public long music_duration;
        public String data_path;

        public static P4_mp3Adapter.Mp3Item bindCursor(Cursor cursor){
            Mp3Item mp3Item=new Mp3Item();
            mp3Item.audioID=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
            mp3Item.albumID=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
            mp3Item.music_title=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
            mp3Item.music_artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
            mp3Item.music_album=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
            mp3Item.music_duration=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));
            mp3Item.data_path=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
            return mp3Item;
        }

    }
    public ArrayList<Long> getAudioIds(){
        int count=getItemCount();
        ArrayList<Long> audioIds=new ArrayList<>();
        for (int i=0;i<count;i++){
            audioIds.add(getItemId(i));
        }
        return audioIds;
    }
    public class mp3ViewHolder extends RecyclerView.ViewHolder {
        private final Uri albumuri=Uri.parse("content://media/external/audio/albumart");
        private Mp3Item item;
        private ImageView p4_albumart;
        private TextView albumname;
        private TextView albumartist;
        private TextView albumnum;
        private int mposition;
        public mp3ViewHolder(final View itemView) {
            super(itemView);
            p4_albumart=itemView.findViewById(R.id.p4_albumart);
            albumname=itemView.findViewById(R.id.p4_album);
            albumartist=itemView.findViewById(R.id.p4_artist);
            albumnum=itemView.findViewById(R.id.p4_num);

        }
        public void setMp3Item(P4_mp3Adapter.Mp3Item Item, int position){
            item=Item;
            mposition=position;
            Uri albumArturi= ContentUris.withAppendedId(albumuri,item.albumID);
            Glide.with(itemView.getContext()).load(albumArturi).error(Glide.with(itemView).load(R.drawable.musicicon)).into(p4_albumart);
            albumname.setText(Item.music_album);
            albumartist.setText(Item.music_artist);
        }

    }//ViewHolder

}