package com.oh.mp3test;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
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

public class Mp3Adapter extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {
    Context context;
    public Mp3Adapter(Context context, Cursor cursor) {
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
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item_layout,parent,false);
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

        public static Mp3Item bindCursor(Cursor cursor){
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
    public ArrayList<Long>getAudioIds(){
        int count=getItemCount();
        ArrayList<Long> audioIds=new ArrayList<>();
        for (int i=0;i<count;i++){
            audioIds.add(getItemId(i));
        }
        return audioIds;
    }
    public class mp3ViewHolder extends RecyclerView.ViewHolder {
        private final Uri albumuri=Uri.parse("content://media/external/audio/albumart");
        private ImageView albumart;
        private TextView song_title;
        private TextView song_artist;
        private TextView song_duration;
        private Mp3Item item;
        private int mposition;
        public mp3ViewHolder(final View itemView) {
            super(itemView);
            albumart=itemView.findViewById(R.id.m_titleimg);
            song_title=itemView.findViewById(R.id.m_title);
            song_artist=itemView.findViewById(R.id.m_artist);
            song_duration=itemView.findViewById(R.id.m_du);
            itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mp3Application.getInstance().getServiceInterface().setPlayList(getAudioIds());
                            mp3Application.getInstance().getServiceInterface().play(mposition);

                }
            });

        }
        public void setMp3Item(Mp3Item Item,int position){
            item=Item;
            mposition=position;
            song_title.setText(Item.music_title);
            song_artist.setText(Item.music_artist);
            song_duration.setText(android.text.format.DateFormat.format("mm:ss",item.music_duration));
            Uri albumArturi=ContentUris.withAppendedId(albumuri,item.albumID);
            Glide.with(itemView.getContext()).load(albumArturi).error(Glide.with(itemView).load(R.drawable.musicicon)).into(albumart);

        }

    }//ViewHolder

}

