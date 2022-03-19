package th.in.ffc.building.house;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import th.in.ffc.R;
import th.in.ffc.app.form.EditFormActivity;
import th.in.ffc.intent.Action;
import th.in.ffc.person.visit.VisitActivity;
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.provider.HouseProvider.HouseVesselWater;
import th.in.ffc.util.DateTime;
import th.in.ffc.widget.ArrayFormatSpinner;

import java.util.ArrayList;

public class HouseWaterVesselActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    String mHCODE;
    String mPCUCODEPERSON;

    public static final String[] PROJECTION = new String[]{
            HouseVesselWater.VESSELCODE, HouseVesselWater.QUANTITY};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        mHCODE = getIntent().getStringExtra(HouseVesselWater.HCODE);
        mPCUCODEPERSON = getIntent().getStringExtra(House.PCUCODE);
        setContentView(R.layout.house_water_vessel_activity);
        setSupportProgressBarIndeterminateVisibility(false);

        if (savedInstanceState == null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_add_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                addPersonVesselFragment(Action.INSERT, null, null, null, DateTime.getCurrentTime());
                break;
            case R.id.save:
                setSupportProgressBarIndeterminateVisibility(true);
                doSave();
                setSupportProgressBarIndeterminateVisibility(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //DO SAVE HERE
    public void doSave() {

        boolean finishable = true;

        for (String vesselType : getDeleteList()) {
            Uri uri = HouseVesselWater.CONTENT_URI;
            ContentResolver cr = getContentResolver();
            int count = cr.delete(uri, "hcode=? AND pcucode=? AND vessel =?", new String[]{mHCODE, mPCUCODEPERSON, vesselType});

            System.out.println("deleted diag count=" + count + " +uri="
                    + uri.toString());
        }

        ArrayList<String> codeList = new ArrayList<String>();

        for (String tag : getEditList()) {
            Log.d(TAG, "tag=" + tag);
            VesselFragment f = (VesselFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (f != null) {
                EditTransaction et = beginTransaction();
                f.onSave(et);

                if (!et.canCommit()) {
                    finishable = false;
                    break;
                }

                String code = et.getContentValues().getAsString(HouseVesselWater.VESSELCODE);
                if (isAddedCode(codeList, code)) {
                    finishable = false;
                    Toast.makeText(this, R.string.err_dup_Vessel, Toast.LENGTH_SHORT).show();
                    break;
                } else
                    codeList.add(code);

                ContentValues cv = et.getContentValues();
                cv.put(HouseVesselWater.HCODE, mHCODE);
                cv.put(HouseVesselWater.PCUPERSONCODE, mPCUCODEPERSON);

                String action = f.action;
                if (action.equals(Action.INSERT)) {
                    et.commit(HouseVesselWater.CONTENT_URI);
                    f.action = Action.EDIT;
                    f.key = code;


                } else if (action.equals(Action.EDIT)) {
                    Uri updateUri = HouseVesselWater.CONTENT_URI;
                    et.commit(updateUri, "hcode=? AND vessel=?", new String[]{mHCODE, f.key});

                }

            }
        }

        if (finishable) {
            this.finish();
        }

    }

    public boolean isAddedCode(ArrayList<String> codeList, String code) {
        for (String c : codeList) {
            if (code.equals(c))
                return true;
        }
        return false;
    }

    public void addPersonVesselFragment(String Action, String type, String can,
                                        String abno, String tag) {

        addEditFragment(VesselFragment.newInstance(Action, type, can, tag), tag);
    }

    private void generateVessel(Cursor c) {

        String type = c.getString(0);
        String can = c.getString(1);

        System.out.println("I've got this grow type = " + type);
        addEditFragment(
                VesselFragment.newInstance(Action.EDIT, type, can, type),
                type);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub

        setSupportProgressBarIndeterminateVisibility(true);
        CursorLoader cl = new CursorLoader(this, HouseVesselWater.CONTENT_URI,
                PROJECTION, "hcode =? AND pcucode=?", new String[]{mHCODE, mPCUCODEPERSON},
                HouseVesselWater.DEFAULT_SORTING);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        // TODO Auto-generated method stub
        setSupportProgressBarIndeterminateVisibility(false);

        if (arg1.moveToFirst()) {
            final Cursor c = arg1;

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    do {
                        generateVessel(c);
                    } while (c.moveToNext());
                }
            });

        } else {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String tag = DateTime.getCurrentDateTime();
                    VesselFragment f = VesselFragment.newInstance(Action.INSERT, null, null, tag);
                    addEditFragment(f, tag);

                }
            });
        }
    }

    public Handler mHandler = new Handler();

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub

    }

    public static class VesselFragment extends EditFormActivity.EditFragment {
        ArrayFormatSpinner vesselType;
        EditText quality;
        ImageButton mClose;

        private String type;
        private String can;

        public static VesselFragment newInstance(String action, String type,
                                                 String can, String tag) {

            VesselFragment f = new VesselFragment();

            // Bundle args = new Bundle(f.getBaseArguments(action, tag, code));
            Bundle args = new Bundle(f.getBaseArguments(action, tag, type));

            args.putString("type", type);
            args.putString("can", can);


            f.setArguments(args);

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.house_water_vessel_fragment,
                    container, false);

            vesselType = (ArrayFormatSpinner) v.findViewById(R.id.type);
            vesselType.setArray(R.array.vesselItem);
            quality = (EditText) v.findViewById(R.id.value);
            mClose = (ImageButton) v.findViewById(R.id.deleted);
            setAsRemoveButton(mClose);

            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle args = getArguments();

            type = args.getString("type");
            can = args.getString("can");
            doInitializeView();

        }

        private void doInitializeView() {

            vesselType.setSelection(type);
            quality.setText(can);

        }

        @Override
        public boolean onSave(EditTransaction et) {
            // TODO Auto-generated method stub
            et.retrieveData(HouseVesselWater.VESSELCODE, vesselType, false, null, null);
            et.retrieveData(HouseVesselWater.QUANTITY, quality, false, 0, 99999, getString(R.string.err_no_value));

            ContentValues cv = et.getContentValues();
            cv.put(HouseVesselWater.VESSELCODE, vesselType.getSelectionId());
            cv.put(HouseVesselWater.QUANTITY, quality.getText().toString());
            return true;
        }

    }
}
