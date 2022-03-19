/* ***********************************************************************
 *                                                                 _ _ _
 *                                                               ( _ _  |
 *                                                           _ _ _ _  | |
 *                                                          (_ _ _  | |_|
 *  _     _   _ _ _ _     _ _ _   _ _ _ _ _   _ _ _ _     _ _ _   | | 
 * |  \  | | |  _ _ _|   /  _ _| |_ _   _ _| |  _ _ _|   /  _ _|  | |
 * | | \ | | | |_ _ _   /  /         | |     | |_ _ _   /  /      |_|
 * | |\ \| | |  _ _ _| (  (          | |     |  _ _ _| (  (    
 * | | \ | | | |_ _ _   \  \_ _      | |     | |_ _ _   \  \_ _ 
 * |_|  \__| |_ _ _ _|   \_ _ _|     |_|     |_ _ _ _|   \_ _ _| 
 *  a member of NSTDA, @Thailand
 *  
 * ***********************************************************************
 *
 *
 * FFC-Plus Project
 *
 * Copyright (C) 2010-2012 National Electronics and Computer Technology Center
 * All Rights Reserved.
 * 
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * 
 */

package th.in.ffc.app;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBar.Tab;
import th.in.ffc.R;

import java.util.ArrayList;

/**
 * add description here! please
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public class FFCActionBarTabsActivity extends FFCFragmentActivity {

    private static final String KEY_CURRENT_TAB = "tab";
    private int mLastSelectedTab = 0;
    private ActionBarTabsAdapter mTabAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.default_actionbar_tabs);

        if (savedInstanceState != null)
            mLastSelectedTab = savedInstanceState.getInt(KEY_CURRENT_TAB);
    }


    public void setTabAdapter(ActionBarTabsAdapter adapter) {
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        this.mTabAdapter = adapter;
        this.mTabAdapter.build(this);

        getSupportActionBar().setSelectedNavigationItem(mLastSelectedTab);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_TAB, getSupportActionBar().getSelectedNavigationIndex());

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (mTabAdapter != null) {
            this.mTabAdapter.close();
        }
        mTabAdapter = null;
    }

    public static class ActionBarTabsAdapter {

        public static class TabInfo {
            protected String text;
            protected Object tag;
            protected Class<?> cls;
            protected Bundle args;

            public TabInfo(Class<?> cls, String text, Object tag) {
                this(cls, text, tag, null);
            }

            public TabInfo(Class<?> cls, String text, Object tag, Bundle args) {
                this.cls = cls;
                this.text = text;
                this.tag = tag;
                this.args = args;
            }

        }

        ArrayList<TabInfo> mTabList;
        ActionBar mActionBar;

        public ActionBarTabsAdapter(ActionBar ab) {
            mActionBar = ab;
            mTabList = new ArrayList<TabInfo>();

        }

        public void addTab(TabInfo info) {
            mTabList.add(info);
        }

        protected void build(FragmentActivity activity) {
            if (mActionBar == null)
                throw new NullPointerException("ActionBar was null");

            int size = mTabList.size();
            if (size > 0) {

                for (int index = 0; index < size; index++) {
                    TabInfo info = mTabList.get(index);
                    Tab tab = mActionBar.newTab();
                    tab.setText(info.text);
                    tab.setTag(info.tag);
                    tab.setTabListener(new TabListener<Fragment>(activity, info.text, info.cls.getName(), info.args));
                    // tab.setTabListener(this);
                    mActionBar.addTab(tab);
                }
            }
        }

        public ArrayList<TabInfo> getTabList() {
            return mTabList;
        }

        public void close() {
            mTabList = null;
            mActionBar = null;
        }
    }

    public static class TabListener<T extends Fragment> implements
            ActionBar.TabListener {
        private final FragmentActivity mActivity;
        private final String mTag;
        private final String mClass;
        private final Bundle mArgs;
        private Fragment mFragment;

        public TabListener(FragmentActivity activity, String tag, String clz) {
            this(activity, tag, clz, null);
        }

        public TabListener(FragmentActivity activity, String tag, String clz,
                           Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state. If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = activity.getSupportFragmentManager().findFragmentByTag(
                    mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager()
                        .beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass,
                        mArgs);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            //Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
        }

    }
}
