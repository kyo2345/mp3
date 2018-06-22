package com.oh.mp3test.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FlagmentAdapter extends FragmentPagerAdapter {
    Fragment[] frags = new Fragment[4];
    String[] title = {"즐겨찾기", "플레이리스트", "곡", "앨범"};

    public FlagmentAdapter(FragmentManager fm) {
        super(fm);
        frags[0] = new p1_fav_fragment();
        frags[1] = new p2_pl_fragment();
        frags[2] = new p3_song_fragment();
        frags[3] = new p4album_fragment();
    }

    @Override
    public Fragment getItem(int position) {
        return frags[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }

    @Override
    public int getCount() {
        return frags.length;
    }
}
