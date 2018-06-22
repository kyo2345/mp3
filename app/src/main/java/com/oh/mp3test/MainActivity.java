package com.oh.mp3test;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.oh.mp3test.fragment.DummyFragement;
import com.oh.mp3test.fragment.FlagmentAdapter;
import com.oh.mp3test.search.SearchActivity;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    FlagmentAdapter flagmentAdapter;
    android.support.v7.widget.Toolbar toolbar;
    Mp3Adapter mp3Adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabLayout=findViewById(R.id.tablayout);
        viewPager=findViewById(R.id.pager);
        flagmentAdapter=new FlagmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(flagmentAdapter);
        check();
        mp3Adapter=new Mp3Adapter(this,null);
        getAudioMDB();

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toolbar_search:
                Intent intent=new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.edit:
                break;
            case R.id.Equalizer:
                break;
            case R.id.setting:
                break;
        }
        return super.onOptionsItemSelected(item);
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
    public void check(){
        String state= Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, "Sd card is not munted", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int checkSelfPermission=checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int checkPermission=checkSelfPermission(Manifest.permission.WAKE_LOCK);
            if (checkSelfPermission== PackageManager.PERMISSION_DENIED){
                String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions,100);
            }
            if (checkPermission == PackageManager.PERMISSION_DENIED){
                String[] permission={Manifest.permission.WAKE_LOCK};
                requestPermissions(permission,101);
            }
        }
    }


}
