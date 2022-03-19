package th.in.ffc.map.ui.markerActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBar.Tab;
import com.ibus.phototaker.PhotoTaker;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.map.FGActivity;
import th.in.ffc.map.overlay.FGOverlayManager;
import th.in.ffc.map.value.MARKER_TYPE;

public class CreateMarkerActivity extends FFCFragmentActivity implements ActionBar.TabListener {

    private CreateBaseFragment cb;
    private boolean first;
    public static PhotoTaker pt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //this.setTheme(R.style.Theme_Sherlock_ForceOverflow);
        // Window window = this.getWindow();
        // window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.setContentView(R.layout.create_marker_fragment_activity);

        pt = FGActivity.fgsys.getPhotoTaker();

        first = true;

        // ArrayAdapter<CharSequence> listAdapter = ArrayAdapter
        // .createFromResource(this, R.array.type_list,
        // R.layout.sherlock_spinner_item);
        // listAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

        ActionBar ab = getSupportActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        String arr[] = getResources().getStringArray(R.array.type_list);
        MARKER_TYPE types[] = MARKER_TYPE.values();
        for (int i = 0; i < arr.length; i++) {
            Tab tab = ab.newTab();
            tab.setText(arr[i]);
            tab.setIcon(types[i].getDrawableID());
            // tab.setTag(i);
            tab.setTabListener(this);
            ab.addTab(tab);

        }
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowHomeEnabled(false);
        // ab.setListNavigationCallbacks(listAdapter, this);

        Bundle b = this.getIntent().getExtras();
        MARKER_TYPE type = (MARKER_TYPE) b.get("type");
        if (type == null)
            type = MARKER_TYPE.HOUSE;

        ab.setSelectedNavigationItem(type.ordinal());

        // ab.setTitle(R.string.choose);

        cb = (CreateBaseFragment) this.getSupportFragmentManager().findFragmentById(R.id.base_fragment);
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        cb.onActivityResult(arg0, arg1, arg2);
    }

    @Override
    public void onBackPressed() {
        FGOverlayManager.pic_changed = false;
        PhotoTaker.clearCache();
        this.finish();
    }

    // @Override
    // public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    //
    //
    //
    // return false;
    // }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        if (!first) {
            MARKER_TYPE selected = MARKER_TYPE.values()[tab.getPosition()];
            if (selected != cb.getType())
                cb.setType(selected);
        } else {
            first = false;
        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {

    }
}
