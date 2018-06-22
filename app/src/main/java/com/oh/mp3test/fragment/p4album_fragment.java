package com.oh.mp3test.fragment;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.oh.mp3test.BroadcastActions;
import com.oh.mp3test.P4_mp3Adapter;
import com.oh.mp3test.R;
import com.oh.mp3test.mp3Application;

/**
 * Created by OhInHwan on 2018-03-22.
 */

public class p4album_fragment extends Fragment {
    RecyclerView recyclerView;
    P4_mp3Adapter mp3Adapter;
    Context context;
    ImageView p4_albumart;
    TextView p4_album, p4_artist, p4_songnum;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.p4tab_layout, container, false);
        recyclerView = v.findViewById(R.id.album_rv);
        context = getContext();
        mp3Adapter = new P4_mp3Adapter(context, null);
        recyclerView.setAdapter(mp3Adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        p4_albumart = v.findViewById(R.id.p4_albumart);
        p4_album = v.findViewById(R.id.p4_album);
        p4_artist = v.findViewById(R.id.p4_artist);
        p4_songnum = v.findViewById(R.id.p4_num);
        getAudioMDB();
        registerBroadcast();
        updateUI();
        return v;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    public void getAudioMDB() {

        getActivity().getSupportLoaderManager().initLoader(1, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] proj = new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA
                };
                String selection = MediaStore.Audio.Media.IS_MUSIC + "=1";
                String sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC";
                return new CursorLoader(getActivity(), uri, proj, selection, null, sortOrder);
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

    private BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           // updateUI();
        }
    };

    public void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastActions.PLAY_STATE_CHANGED);
        filter.addAction(BroadcastActions.PREPARED);
        context.registerReceiver(broadcastReceiver, filter);

    }


    public void updateUI() {
        mp3Adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }

    public void unregisterBroadcast() {
        context.unregisterReceiver(broadcastReceiver);
    }
}
