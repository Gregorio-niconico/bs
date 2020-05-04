package com.example.walkwalk.ViewPaper;

import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class BottomAdapter extends FragmentPagerAdapter{
    private static String TAG="Adapter";
    private List<Fragment> fragments = new ArrayList<>();
    public static Fragment currentFragment;
    public BottomAdapter(FragmentManager fm){
        super(fm);
    }
    @NonNull
    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem:已执行 ");
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount: 已执行");
        return fragments.size();
    }

    public void addFragment(Fragment fragment){
        Log.d(TAG, "addFragment: 已执行");
        fragments.add(fragment);
    }

//    @Override
//    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        Log.d(TAG, "setPrimaryItem: 已执行");
//        currentFragment=(Fragment)object;
//        super.setPrimaryItem(container, position, object);
//    }

    public Fragment getCurrentFragment(){
        return currentFragment;
    }

    //    @Override
//    public int getItemPosition(@NonNull Object object) {
//        return POSITION_NONE;
//    }
}
