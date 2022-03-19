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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import th.in.ffc.R;

import java.util.ArrayList;

/**
 * Demonstrates combining a TabHost with a ViewPager to implement a tab UI that
 * switches between tabs and also allows the user to perform horizontal flicks
 * to move between the tabs.
 * <p/>
 * [Example]
 * <p/>
 * public class TestTabsPager extends FFCTabsPagerActivity {
 *
 * @author Piruin Panichphol
 * @Override protected void onCreate(Bundle savedInstanceState) {
 * <p/>
 * super.onCreate(savedInstanceState);
 * <p/>
 * TabsPagerAdapter adapter = new TabsPagerAdapter(this, getTabHost(),getViewPager());
 * adapter.addTab("Person", PersonListFragment.class,null);
 * adapter.addTab("House", HouseListFragment.class, null);
 * <p/>
 * setTabsPagerAdapter(adapter);
 * } }
 * @since 1.0
 * <p/>
 * * Mod. from Android Open Source Project
 */
public class FFCTabsPagerActivity extends FFCFragmentActivity {
    TabHost mTabHost;
    TabWidget mTabWidget;
    ViewPager mViewPager;
    PagerTabStrip mTabStrip;
    TabsPagerAdapter mTabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_tabs_pager);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.getTabHost().setCurrentTabByTag(
                    savedInstanceState.getString("tab"));
        }
    }

    public TabHost getTabHost() {
        if (mTabHost != null) {
            return mTabHost;
        } else {
            mTabHost = (TabHost) findViewById(android.R.id.tabhost);
            mTabHost.setup();
            return mTabHost;
        }
    }

    public ViewPager getViewPager() {
        if (mViewPager != null)
            return mViewPager;
        else {
            mViewPager = (ViewPager) findViewById(R.id.pager);
            return mViewPager;
        }
    }

    public void setShowPagerTitleStrip(boolean show) {
        int visible = (show) ? View.VISIBLE : View.GONE;
        if (mTabStrip == null)
            mTabStrip = (PagerTabStrip) findViewById(R.id.title);
        mTabStrip.setVisibility(visible);

    }

    public void setShowTabWidget(boolean show) {
        int visible = (show) ? View.VISIBLE : View.GONE;
        if (mTabWidget == null)
            mTabWidget = (TabWidget) findViewById(android.R.id.tabs);
        mTabWidget.setVisibility(visible);

    }

    public void setTabsPagerAdapter(TabsPagerAdapter adapter) {
//		TabWidget tabWidget = getTabHost().getTabWidget();
//		for(int i=0; i<tabWidget.getChildCount(); i++)
//			  tabWidget.getChildAt(i).setBackgroundResource(R.drawable.actionbar_tab_bg);
        mTabsAdapter = adapter;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mTabHost = null;
        mViewPager = null;
        if (mTabsAdapter != null)
            mTabsAdapter.close();
        mTabsAdapter = null;
    }

    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost. It relies on a
     * trick. Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show. This is not sufficient for switching
     * between pages. So instead we make the content part of the tab host 0dp
     * high (it is not shown) and the TabsAdapter supplies its own dummy view to
     * show as the tab content. It listens to changes in tabs, and takes care of
     * switch to the correct paged in the ViewPager whenever the selected tab
     * changes.
     */
    public static class TabsPagerAdapter extends FragmentPagerAdapter implements
            TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
        private Context mContext;
        private TabHost mTabHost;
        private ViewPager mViewPager;
        private ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        private ViewPager.OnPageChangeListener mListener;


        static final class TabInfo {
            protected final String tag;
            protected final Class<?> clss;
            protected final Bundle args;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public void setOnPagerChangeListener(
                ViewPager.OnPageChangeListener listener) {
            this.mListener = listener;
        }

        public TabsPagerAdapter(FragmentActivity activity, TabHost tabHost,
                                ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mContext));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();

            TabWidget tabWidget = mTabHost.getTabWidget();
            int tabCount = tabWidget.getChildCount();
            if (tabCount > 0) {
                View tab = tabWidget.getChildAt(tabCount - 1);
                tab.setBackgroundResource(R.drawable.actionbar_tab_bg);

                //tab.setPadding(sidePadding, topDownPadding, sidePadding, topDownPadding);
                //tab.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            }
        }

        public void addTab(String tag, Class<?> clss, Bundle args) {
            this.addTab(mTabHost.newTabSpec(tag).setIndicator(tag), clss, args);
        }

        public void addTab(String tag, Drawable icon, Class<?> clss, Bundle args) {
            this.addTab(mTabHost.newTabSpec(tag).setIndicator(tag, icon), clss,
                    args);
        }

        @Override
        public int getCount() {
            if (mTabs != null)
                return mTabs.size();
            else
                return 0;
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(),
                    info.args);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            TabInfo info = mTabs.get(position);
            return info.tag;
        }

        @Override
        public void onTabChanged(String tabId) {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
            if (mListener != null)
                mListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            // Unfortunately when TabHost changes the current tab, it kindly
            // also takes care of putting focus on it when not in touch mode.
            // The jerk.
            // This hack tries to prevent this from pulling focus out of our
            // ViewPager.
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);

            if (mListener != null)
                mListener.onPageSelected(position);
        }


        @Override
        public void onPageScrollStateChanged(int state) {
            if (mListener != null)
                mListener.onPageScrollStateChanged(state);
        }

        public void close() {
            mContext = null;
            mTabHost = null;
            mViewPager = null;
            mListener = null;
            mTabs = null;
        }

    }
}
