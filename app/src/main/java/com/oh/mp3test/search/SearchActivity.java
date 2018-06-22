package com.oh.mp3test.search;

import android.app.SearchManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.oh.mp3test.R;

public class SearchActivity extends AppCompatActivity{
    SearchView searchView;
    SearchManager searchManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search,menu);
        MenuItem item=menu.findItem(R.id.search_menu);
        searchView= (SearchView) item.getActionView();
        searchManager= (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setQueryHint("검색");
        return super.onCreateOptionsMenu(menu);
    }
}
