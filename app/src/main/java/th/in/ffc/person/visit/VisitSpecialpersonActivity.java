package th.in.ffc.person.visit;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import th.in.ffc.R;
import th.in.ffc.code.DiagDisableListDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.PersonProvider.VisitSpecialperson;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.widget.ArrayFormatSpinner;
import th.in.ffc.widget.SearchableButton;

import java.util.ArrayList;

public class VisitSpecialpersonActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {


    public static final String[] PROJECTION = new String[]{
            VisitSpecialperson.DISABLE_ID, VisitSpecialperson.DATEDETECT,
            VisitSpecialperson.DATEEXPIRE, VisitSpecialperson.DISABLE_TYPE,
            VisitSpecialperson.DISABLE_CAUSE, VisitSpecialperson.DIAGCODE};

    private EditText disabid;
    private ThaiDatePicker datedetect;
    private ThaiDatePicker dateexpire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.visit_specialperson_activity);
        setSupportProgressBarIndeterminateVisibility(false);

        doSetupDatepicker(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("datedetect", datedetect.getDate().toString());
        outState.putString("datedeexpire", dateexpire.getDate().toString());
    }


    private void doSetupDatepicker(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        disabid = (EditText) findViewById(R.id.answer1);
        datedetect = (ThaiDatePicker) findViewById(R.id.answer2);
        dateexpire = (ThaiDatePicker) findViewById(R.id.answer3);

        if (savedInstanceState == null) {
            Date date = Date.newInstance(DateTime.getCurrentDate());
            datedetect.updateDate(date.year, date.month - 1, date.day + 1);
            dateexpire.updateDate(date.year, date.month - 1, date.day + 1);
        } else {
            Date date = Date.newInstance(savedInstanceState.getString("datedetect"));
            Date date2 = Date.newInstance(savedInstanceState.getString("dateexpire"));

            datedetect.updateDate(date.year, date.month - 1, date.day);
            dateexpire.updateDate(date2.year, date2.month - 1, date2.day);
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
                addSpecialpersonFragment(Action.INSERT, null, null, null, DateTime.getCurrentTime(), getPcuCode(), getPid());
                break;
            case R.id.save:
                setSupportProgressBarIndeterminateVisibility(true);
                doSave();
                setSupportProgressBarIndeterminateVisibility(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void doSave() {

        boolean finishable = true;

        for (String disabtype : getDeleteList()) {
            Uri uri = VisitSpecialperson.CONTENT_URI;
            ContentResolver cr = getContentResolver();
            int count = cr.delete(uri, "disabtype = ?", new String[]{disabtype});

            System.out.println("deleted diag count=" + count + " +uri=" + uri.toString());
        }

        ArrayList<String> codeList = new ArrayList<String>();

        for (String tag : getEditList()) {
            Log.d(TAG, "tag=" + tag);
            SpecialpersonFragment f = (SpecialpersonFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (f != null) {
                EditTransaction et = beginTransaction();
                f.onSave(et);

                if (!et.canCommit()) {
                    finishable = false;
                    break;
                }

                String code = et.getContentValues().getAsString(VisitSpecialperson.DISABLE_TYPE);
                if (isAddedCode(codeList, code)) {
                    finishable = false;
                    Toast.makeText(this, R.string.err_duplicate_disabletype,
                            Toast.LENGTH_SHORT).show();
                    break;
                } else
                    codeList.add(code);

                ContentValues cv = et.getContentValues();
                cv.put(VisitSpecialperson.PCUCODE, getPcuCode());
                cv.put(VisitSpecialperson.PID, getPid());

                String id = disabid.getText().toString();
                if (!TextUtils.isEmpty(id))
                    cv.put(VisitSpecialperson.DISABLE_ID, id);
                else
                    cv.putNull(VisitSpecialperson.DISABLE_ID);

                cv.put(VisitSpecialperson.DATEDETECT, datedetect.getDate().toString());
                cv.put(VisitSpecialperson.DATEEXPIRE, dateexpire.getDate().toString());
                cv.put(VisitSpecialperson.DATEUPDATE, DateTime.getCurrentDateTime());


                String action = f.action;
                if (action.equals(Action.INSERT)) {
                    et.commit(VisitSpecialperson.CONTENT_URI);
                    f.action = Action.EDIT;
                    f.key = code;


                } else if (action.equals(Action.EDIT)) {
                    Uri updateUri = VisitSpecialperson.CONTENT_URI;
                    et.commit(updateUri, "pid=? AND disabtype=?", new String[]{getPid(), f.key});

                    //Log.d(TAG,"update drug=" + update + " uri="+ updateUri.toString());

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

    public void addSpecialpersonFragment(String Action, String type, String cause, String code, String tag, String pcucode, String pid) {

        addEditFragment(SpecialpersonFragment.newInstance(Action, type, cause,
                code, tag, pcucode, pid), tag);
    }

    private void generateSpecialperson(Cursor c) {
        String type = c.getString(c.getColumnIndex(VisitSpecialperson.DISABLE_TYPE));
        String cause = c.getString(c.getColumnIndex(VisitSpecialperson.DISABLE_CAUSE));
        String code = c.getString(c.getColumnIndex(VisitSpecialperson.DIAGCODE));

        addEditFragment(SpecialpersonFragment.newInstance(Action.EDIT, type, cause, code, code, getPcuCode(), getPid()), code);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub
        setSupportProgressBarIndeterminateVisibility(true);
        CursorLoader cl = new CursorLoader(this, VisitSpecialperson.CONTENT_URI,
                PROJECTION, "pid=" + getPid(), null,
                VisitSpecialperson.DEFAULT_SORTING);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        // TODO Auto-generated method stub
        setSupportProgressBarIndeterminateVisibility(false);
        if (arg1.moveToFirst()) {
            final Cursor c = arg1;
            String dID = c.getString(c.getColumnIndex(VisitSpecialperson.DISABLE_ID));
            disabid.setText(dID);

            String dd = c.getString(c.getColumnIndex(VisitSpecialperson.DATEDETECT));
            String de = c.getString(c.getColumnIndex(VisitSpecialperson.DATEEXPIRE));

            if (!TextUtils.isEmpty(dd)) {
                DateTime.Date app_date = DateTime.Date.newInstance(dd);
                datedetect.updateDate(app_date.year, (app_date.month - 1), app_date.day);
            }

            if (!TextUtils.isEmpty(de)) {
                DateTime.Date app_date = DateTime.Date.newInstance(de);
                dateexpire.updateDate(app_date.year, (app_date.month - 1), app_date.day);
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    do {
                        generateSpecialperson(c);
                    } while (c.moveToNext());
                }
            });
        }
    }

    public Handler mHandler = new Handler();

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub

    }

    public static class SpecialpersonFragment extends EditFragment {

        private ArrayFormatSpinner disabtype;
        private ArrayFormatSpinner disabcause;
        private SearchableButton diagcode;
        private ImageButton mClose;


        private String type;
        private String cause;
        private String code;

        public static SpecialpersonFragment newInstance(String action, String type,
                                                        String cause, String code, String tag, String pcucode, String pid) {

            SpecialpersonFragment f = new SpecialpersonFragment();

            //Bundle args = new Bundle(f.getBaseArguments(action, tag, code));
            Bundle args = new Bundle(f.getBaseArguments(action, tag, type));

            args.putString("pid", pid);
            args.putString("p", pcucode);


            args.putString("type", type);
            args.putString("cause", cause);
            args.putString("code", code);

            f.setArguments(args);

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.visit_special_fragment, container,
                    false);

            disabtype = (ArrayFormatSpinner) v.findViewById(R.id.type);
            disabcause = (ArrayFormatSpinner) v.findViewById(R.id.cause);
            diagcode = (SearchableButton) v.findViewById(R.id.code);
            mClose = (ImageButton) v.findViewById(R.id.deleted);
            setAsRemoveButton(mClose);

            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle args = getArguments();

            type = args.getString("type");
            cause = args.getString("cause");
            code = args.getString("code");

            doInitializeView();

        }

        private void doInitializeView() {

            disabtype.setArray(R.array.disabtype);
            disabcause.setArray(R.array.disabcause);
            diagcode.setDialog(getFragmentManager(), DiagDisableListDialog.class, "diagcode" + tag);

            disabtype.setSelection(type);
            disabcause.setSelection(cause);


            if (!TextUtils.isEmpty(code))
                diagcode.setSelectionById(code);

        }


        @Override
        public boolean onSave(EditTransaction et) {
            // TODO Auto-generated method stub

            et.retrieveData(VisitSpecialperson.DISABLE_TYPE, disabtype, false, null, null);

            ContentValues cv = et.getContentValues();
            //Put What


            cv.put(VisitSpecialperson.DISABLE_TYPE, disabtype.getSelectionId());
            cv.put(VisitSpecialperson.DISABLE_CAUSE, disabcause.getSelectionId());
            cv.put(VisitSpecialperson.DIAGCODE, diagcode.getSelectId());


            return true;
        }


    }

}
