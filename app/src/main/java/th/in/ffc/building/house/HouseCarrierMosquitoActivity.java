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
import th.in.ffc.provider.HouseProvider.HouseGenusculex;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.ThaiDatePicker;

import java.util.ArrayList;

public class HouseCarrierMosquitoActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    String mHCODE;
    String mPCUCODEPERSON;

    public static final String[] PROJECTION = new String[]{
            HouseGenusculex.DATESURVEY, HouseGenusculex.NOOFGENUSCULEX, HouseGenusculex.NOOFVES};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        mHCODE = getIntent().getStringExtra(HouseGenusculex.HCODE);
        mPCUCODEPERSON = getIntent().getStringExtra(House.PCUCODE);

        System.out.println("HELLO I'M FROM " + mHCODE + ":" + mPCUCODEPERSON);
        setContentView(R.layout.house_carrier_mosquito_activity);
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
                addFragment(Action.INSERT, null, null, null, DateTime.getCurrentTime());
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

        for (String datesurvey : getDeleteList()) {
            Uri uri = HouseGenusculex.CONTENT_URI;
            ContentResolver cr = getContentResolver();
            int count = cr.delete(uri, "hcode=? AND pcucode=? AND datesurvey =?", new String[]{mHCODE, mPCUCODEPERSON, datesurvey});

            System.out.println("deleted diag count=" + count + " +uri="
                    + uri.toString());
        }

        ArrayList<String> codeList = new ArrayList<String>();

        for (String tag : getEditList()) {
            Log.d(TAG, "tag=" + tag);
            datesurveyFragment f = (datesurveyFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (f != null) {
                EditTransaction et = beginTransaction();
                f.onSave(et);

                if (!et.canCommit()) {
                    finishable = false;
                    break;
                }

                String code = et.getContentValues().getAsString(HouseGenusculex.DATESURVEY);
                if (isAddedCode(codeList, code)) {
                    finishable = false;
                    Toast.makeText(this, R.string.err_dup_Animal, Toast.LENGTH_SHORT).show();
                    break;
                } else
                    codeList.add(code);

                ContentValues cv = et.getContentValues();
                cv.put(HouseGenusculex.HCODE, mHCODE);
                cv.put(HouseGenusculex.PCUPERSONCODE, mPCUCODEPERSON);

                String action = f.action;
                if (action.equals(Action.INSERT)) {
                    et.commit(HouseGenusculex.CONTENT_URI);
                    f.action = Action.EDIT;
                    f.key = code;

                    System.out.println("I'M HERE");
                } else if (action.equals(Action.EDIT)) {
                    Uri updateUri = HouseGenusculex.CONTENT_URI;
                    et.commit(updateUri, "hcode=? AND datesurvey=?", new String[]{mHCODE, f.key});

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

    public void addFragment(String Action, String arg1, String arg2,
                            String arg3, String tag) {

        addEditFragment(datesurveyFragment.newInstance(Action, arg1, arg2, arg3, tag), tag);
    }

    private void generatedatesurvey(Cursor c) {

        String arg1 = c.getString(0);
        String arg2 = c.getString(1);
        String arg3 = c.getString(2);

        System.out.println("I've got this grow arg1 = " + arg1);
        addEditFragment(
                datesurveyFragment.newInstance(Action.EDIT, arg1, arg2, arg3, arg1),
                arg1);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub

        setSupportProgressBarIndeterminateVisibility(true);
        CursorLoader cl = new CursorLoader(this, HouseGenusculex.CONTENT_URI,
                PROJECTION, "hcode =? AND pcucode=?", new String[]{mHCODE, mPCUCODEPERSON},
                HouseGenusculex.DEFAULT_SORTING);
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
                        System.out.println("I'm gening");
                        generatedatesurvey(c);
                    } while (c.moveToNext());
                }
            });

        } else {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String tag = DateTime.getCurrentDateTime();
                    datesurveyFragment f = datesurveyFragment.newInstance(Action.INSERT, null, null, null, tag);
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

    public static class datesurveyFragment extends EditFormActivity.EditFragment {
        ThaiDatePicker datesurvey;
        EditText quality;
        EditText quality2;
        ImageButton mClose;

        private String arg1;
        private String arg2;
        private String arg3;

        public static datesurveyFragment newInstance(String action, String type,
                                                     String arg2, String arg3, String tag) {

            datesurveyFragment f = new datesurveyFragment();

            // Bundle args = new Bundle(f.getBaseArguments(action, tag, code));
            Bundle args = new Bundle(f.getBaseArguments(action, tag, type));

            args.putString("arg1", type);
            args.putString("arg2", arg2);
            args.putString("arg3", arg3);

            f.setArguments(args);

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.house_carrier_mosquito_fragment,
                    container, false);

            datesurvey = (ThaiDatePicker) v.findViewById(R.id.type);
            Date date = Date.newInstance(DateTime.getCurrentDate());
            datesurvey.updateDate(date.year, date.month - 1, date.day + 1);

            quality = (EditText) v.findViewById(R.id.value1);
            quality2 = (EditText) v.findViewById(R.id.value2);
            mClose = (ImageButton) v.findViewById(R.id.deleted);
            setAsRemoveButton(mClose);

            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle args = getArguments();

            arg1 = args.getString("arg1");
            arg2 = args.getString("arg2");
            arg3 = args.getString("arg3");
            doInitializeView();

        }

        private void doInitializeView() {
            if (arg1 != null) {
                Date date = Date.newInstance(arg1);
                datesurvey.updateDate(date.year, date.month - 1, date.day);
            }
            quality.setText(arg2);
            quality2.setText(arg3);

        }

        @Override
        public boolean onSave(EditTransaction et) {
            // TODO Auto-generated method stub
            et.retrieveData(HouseGenusculex.DATESURVEY, datesurvey, false, null, null);
            et.retrieveData(HouseGenusculex.NOOFGENUSCULEX, quality, false, 0, 99999, getString(R.string.err_no_value));
            et.retrieveData(HouseGenusculex.NOOFVES, quality2, false, 0, 99999, getString(R.string.err_no_value));

            ContentValues cv = et.getContentValues();
            cv.put(HouseGenusculex.DATESURVEY, datesurvey.getDate().toString());
            cv.put(HouseGenusculex.NOOFVES, quality.getText().toString());
            cv.put(HouseGenusculex.NOOFGENUSCULEX, quality2.getText().toString());
            return true;
        }

    }

}
