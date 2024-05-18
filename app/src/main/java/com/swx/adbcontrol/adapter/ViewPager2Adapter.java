package com.swx.adbcontrol.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class ViewPager2Adapter extends FragmentStateAdapter {

    private List<Fragment> fragments;

    public ViewPager2Adapter(FragmentActivity activity, List<Fragment> fragments) {
        super(activity);
        this.fragments =fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
