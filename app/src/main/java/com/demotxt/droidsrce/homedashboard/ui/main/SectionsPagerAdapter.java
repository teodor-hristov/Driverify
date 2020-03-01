package com.demotxt.droidsrce.homedashboard.ui.main;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.demotxt.droidsrce.homedashboard.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    private final Context mContext;
    private String fileName;

    public SectionsPagerAdapter(Context context, FragmentManager fm, String fileName) {
        super(fm);
        mContext = context;
        this.fileName = fileName;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;

        Bundle bundle = new Bundle();
        bundle.putString("file_name", fileName);
        switch (position) {
            case 0:
                fragment = new MapTripFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 1:
                fragment = new ChartTripFragment();
                fragment.setArguments(bundle);
                return fragment;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return 2;
    }
}