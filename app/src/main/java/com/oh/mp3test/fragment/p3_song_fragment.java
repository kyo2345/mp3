package com.oh.mp3test.fragment;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.oh.mp3test.BroadcastActions;
import com.oh.mp3test.Mp3Adapter;
import com.oh.mp3test.Play_Activity;
import com.oh.mp3test.R;
import com.oh.mp3test.mp3Application;

import java.time.LocalTime;
import java.util.Random;

/**
 * Created by OhInHwan on 2018-03-22.
 */

public class p3_song_fragment extends Fragment implements View.OnClickListener {
    Context context;
    RecyclerView recyclerView;
    Mp3Adapter mp3Adapter;
    ArrayAdapter arrayAdapter;
    Spinner spinner;
    TextView tv_title, minititie, miniartist;
    ImageView minialbumart, play_pause;
    TabLayout tabLayout;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_song, container, false);
        context = getContext();
        recyclerView = view.findViewById(R.id.m_rv);
        spinner = view.findViewById(R.id.spinner);
        mp3Adapter = new Mp3Adapter(context, null);
        recyclerView.setAdapter(mp3Adapter);
        tv_title = view.findViewById(R.id.m_title);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        minititie = view.findViewById(R.id.txt_title);
        tabLayout = view.findViewById(R.id.tablayout);
        miniartist = view.findViewById(R.id.mini_artist);
        minialbumart = view.findViewById(R.id.img_albumart);
        play_pause = view.findViewById(R.id.btn_play_pause);
        play_pause.setOnClickListener(this);
        view.findViewById(R.id.miniplayer).setOnClickListener(this);
        view.findViewById(R.id.btn_rewind).setOnClickListener(this);
        view.findViewById(R.id.btn_forward).setOnClickListener(this);
        view.findViewById(R.id.shuffle).setOnClickListener(this);
        view.findViewById(R.id.loop).setOnClickListener(this);
        arrayAdapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.datas, android.R.layout.simple_list_item_1);
        getAudioMDB();
        registerBroadcast();
        updateUI();
        spinner.setBackgroundColor(Color.WHITE);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
                switch (position) {
                    case 0:
                        getAudioMDB();
                        break;
                    case 1:
                        sortAtist();
                        break;
                    case 2:
                       sortDate();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }

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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.miniplayer:
                //플레이어 화면이동
                Intent intent = new Intent(context, Play_Activity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.btn_rewind:
                mp3Application.getInstance().getServiceInterface().rewind();
                break;
            case R.id.btn_play_pause:
                mp3Application.getInstance().getServiceInterface().toggleplay();
                break;
            case R.id.btn_forward:
                mp3Application.getInstance().getServiceInterface().forward();
                break;
            case R.id.m_list:
                //플레이 리스트 보여주기
                break;
            case R.id.shuffle:
                mp3Application.getInstance().getServiceInterface().shuffleplay();
                break;
            case R.id.loop:
                mp3Application.getInstance().getServiceInterface().Alllooping();
                break;
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    public void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastActions.PLAY_STATE_CHANGED);
        filter.addAction(BroadcastActions.PREPARED);
        context.registerReceiver(broadcastReceiver, filter);

    }

    private void updateUI() {
        if (mp3Application.getInstance().getServiceInterface().isPlaying()) {
            play_pause.setImageResource(R.drawable.ic_pause);
        } else {
            play_pause.setImageResource(R.drawable.ic_play);
        }
        Mp3Adapter.Mp3Item item = mp3Application.getInstance().getServiceInterface().getmp3Item();
        if (item != null) {
            Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), item.albumID);
            Glide.with(context).load(uri).error(Glide.with(context).load(R.drawable.musicicon)).into(minialbumart);
            minititie.setText(item.music_title);
            minititie.setSingleLine();
            minititie.setSelected(true);
            minititie.setEllipsize(TextUtils.TruncateAt.MARQUEE);

            miniartist.setText(item.music_artist);
            miniartist.setSingleLine();
            miniartist.setFocusableInTouchMode(true);
            miniartist.requestFocus();
            miniartist.setEllipsize(TextUtils.TruncateAt.MARQUEE);

        } else {
            minialbumart.setImageResource(R.drawable.musicicon);
            minititie.setText("재생중인 음악이 없습니다.");

        }
    }

    public void unregisterBroadcast() {
        context.unregisterReceiver(broadcastReceiver);
    }

    public void sortAtist(){
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
                String sortOrder = MediaStore.Audio.Media.ARTIST + " COLLATE LOCALIZED ASC";
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
    public void sortDate(){
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
                String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " COLLATE LOCALIZED ASC";
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


}
