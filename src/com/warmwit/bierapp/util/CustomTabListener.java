package com.warmwit.bierapp.util;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

public class CustomTabListener<T extends Fragment> implements TabListener {
    private Fragment mFragment;
    private int mResource;
    private final Activity mActivity;
    private final String mTag;
    private final Class<T> mClass;

    /** Constructor used each time a new tab is created.
      * @param activity  The host Activity, used to instantiate the fragment
      * @param tag  The identifier tag for the fragment
      * @param clz  The fragment's Class, used to instantiate the fragment
      */
    public CustomTabListener(Activity activity, int resource, String tag, Class<T> clz) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
        mResource = resource;
        
        // Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
        
        if (mFragment != null) {
            FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
            ft.remove(mFragment);
            ft.commit();
        }
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mFragment = Fragment.instantiate(mActivity, mClass.getName());
        ft.add(this.mResource, mFragment, mTag);
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        if (mFragment != null) {
            ft.remove(mFragment);
        }
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {}
}