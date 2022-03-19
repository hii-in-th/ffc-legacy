package th.in.ffc.map.ui.markerActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.map.value.MARKER_TYPE;

public class EditMarkerActivity extends FFCFragmentActivity {

    private CreateBaseFragment cb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.create_marker_fragment_activity);

        Bundle b = this.getIntent().getExtras();
        MARKER_TYPE type = (MARKER_TYPE) b.get("type");

        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getStringArray(R.array.type_list)[type.ordinal()]);

        cb = (CreateBaseFragment) this.getSupportFragmentManager().findFragmentById(R.id.base_fragment);
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        cb.onActivityResult(arg0, arg1, arg2);
    }

    @Override
    public void onBackPressed() {
        if (cb != null)
            cb.button_cancel_Click();
    }
}
