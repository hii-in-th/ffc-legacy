package th.in.ffc.app;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import android.view.View;
import th.in.ffc.R;
import th.in.ffc.app.FFCActionBarTabsPagerActivity.ActionBarTabPagersAdapter.onTabChangeCallback;

import java.util.ArrayList;
import java.util.List;

public class FFCActionBarTabsPagerActivity extends FFCFragmentActivity {

    private ActionBarTabPagersAdapter mAdapter;
    private ViewPager mPager;
    private PagerTabStrip mTabStrip;
    public String TabName;

    int lastTab = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //this.setContentView(R.layout.default_actionbar_tabs_pager);
        setContentView(R.layout.actionbar_tabs_pager_modify);
        if (savedInstanceState != null)
            this.lastTab = savedInstanceState.getInt("tab");
    }

    protected void build() {
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mPager = getViewPager();
        if (mPager != null) {
            mPager.setAdapter(this.mAdapter);
            mAdapter.setOnTabChange(onTabCallback);
            mPager.setOnPageChangeListener(this.mAdapter);
            if (showLastPage > 0) {
                mPager.setCurrentItem(mAdapter.getCount());
            }
        }
    }

    public void setShowPagerTitleStrip(boolean show) {
        int visible = (show) ? View.VISIBLE : View.GONE;
        if (mTabStrip == null)
            mTabStrip = (PagerTabStrip) findViewById(R.id.title);
        mTabStrip.setVisibility(visible);
    }

    protected ViewPager getViewPager() {
        if (mPager == null)
            mPager = (ViewPager) this.findViewById(android.R.id.tabcontent);
        return mPager;
    }

    protected ViewPager setViewPager(int id) {
        mPager = (ViewPager) this.findViewById(id);
        return mPager;
    }

    protected void setTabPagerAdapter(ActionBarTabPagersAdapter adapter) {
        this.mAdapter = adapter;
        if (mAdapter != null) {
            this.build();
            if (lastTab > -1) {
                getSupportActionBar().setSelectedNavigationItem(this.lastTab);
                mPager.setCurrentItem(this.lastTab);
            }
        }
    }

    int showLastPage;

    // for activitiy view;
    protected void setTabPagerAdapter(ActionBarTabPagersAdapter adapter, int lastPage) {
        showLastPage = lastPage;
        this.mAdapter = adapter;
        if (mAdapter != null) {
            this.build();
            if (lastTab > -1) {
                getSupportActionBar().setSelectedNavigationItem(this.lastTab);
                mPager.setCurrentItem(this.lastTab);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        this.lastTab = getSupportActionBar().getSelectedNavigationIndex();
        outState.putInt("tab", this.lastTab);
    }

    public static class ActionBarTabPagersAdapter extends FragmentStatePagerAdapter
            implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

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

        /**
         * @param fm
         */
        public ActionBarTabPagersAdapter(Context context, FragmentManager fm,
                                         ActionBar actionBar, ViewPager pager) {
            super(fm);
            this.mContext = context;
            this.mActionBar = actionBar;
            this.mPager = pager;
        }

        private ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        private Context mContext;
        private ActionBar mActionBar;
        private List<Fragment> fragment = new ArrayList<Fragment>();
        private ViewPager mPager;
        int currentpage;
        private boolean mShowTab = true;
        onTabChangeCallback tabChangeCallback;

        public interface onTabChangeCallback {
            public void onTabChangeCallBack(Fragment getItem, int position);

            public void setCuttentPage(int currentPage);
        }

        public void setOnTabChange(onTabChangeCallback tabChangeCallback) {
            this.tabChangeCallback = tabChangeCallback;
        }

        public void addTab(String tag, Class<?> clss, Bundle args) {

            TabInfo info = new TabInfo(tag, clss, args);
            mTabs.add(info);

            ActionBar.Tab tab = mActionBar.newTab();
            tab.setText(tag);
            tab.setTabListener(this);
            mActionBar.addTab(tab);
            fragment.add(Fragment.instantiate(mContext, info.clss.getName(), info.args));
            notifyDataSetChanged();
        }

        public void removeTabPager() {
            int tabid = mPager.getCurrentItem();
            mActionBar.removeTabAt(tabid);
            mTabs.remove(tabid);
        }

        public void removeAll() {
            mActionBar.removeAllTabs();
            mTabs.removeAll(mTabs);
        }

        /**
         * to set ActionBar's navigation mode to STANDARD or TABS so you can use only
         * ViewPager function
         * <p/>
         * !WARNNING only call this method after already setTabPagerAdapter()

         */
        public void setTabVisible(boolean visible) {
            this.mShowTab = visible;
            if (!visible)
                this.mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            else
                this.mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

        /* The following are each of the ActionBar.TabListener call backs */
        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (mPager != null)
                mPager.setCurrentItem(this.mActionBar
                        .getSelectedNavigationIndex());
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }

        /*
         * The following are each of the ViewPager.OnPageChangeListener call
         * backs
         */
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageSelected(int position) {
            if (mShowTab)
                mActionBar.setSelectedNavigationItem(position);
            currentpage = position;
            tabChangeCallback.setCuttentPage(currentpage);
        }

        /*
         * The following are each of the FragmentStatePagerAdapter
         */
        @Override
        public Fragment getItem(int position) {
            if (fragment.get(position) != null) {
                TabInfo info = mTabs.get(position);
                fragment.add(position, Fragment.instantiate(mContext, info.clss.getName(), info.args));
                if (tabChangeCallback != null) {
                    tabChangeCallback.onTabChangeCallBack(fragment.get(position), position);
                }
            }
            return fragment.get(position);
        }


        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            TabInfo info = mTabs.get(position);
            return info.tag;
        }
    }

    onTabChangeCallback onTabCallback = new onTabChangeCallback() {
        @Override
        public void onTabChangeCallBack(Fragment getItem, int position) {
            onTabChangeGetItem(getItem, position);
        }

        @Override
        public void setCuttentPage(int currentPage) {
            setCurrentPage(currentPage);
        }
    };

    public void setCurrentPage(int currentpage) {

    }

    public void onTabChangeGetItem(Fragment getItem, int position) {

    }

}
