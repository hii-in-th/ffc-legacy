package th.in.ffc.person.disability;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import th.in.ffc.R;
import th.in.ffc.code.DiagnosisListDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.person.visit.VisitActivity;
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.provider.PersonProvider.Personunable;
import th.in.ffc.provider.PersonProvider.PersonunableType;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.widget.ArrayFormatSpinner;
import th.in.ffc.widget.SearchableButton;

import java.util.ArrayList;

public class PersonunableTypeEdit extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    String mPID;
    String mPCUCODEPERSON;

    public static final String[] PROJECTION = new String[]{
            PersonunableType.TYPECODE, PersonunableType.UNABLELEVEL,
            PersonunableType.DATEFOUND, PersonunableType.DISABCAUSE,
            PersonunableType.DIAGCAUSE, PersonunableType.DATESTART};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        mPID = getIntent().getStringExtra(PersonunableType.PID);
        mPCUCODEPERSON = getIntent().getStringExtra(House.PCUCODE);

        System.out.println("HELLO I'M FROM " + mPID + ":" + mPCUCODEPERSON);
        setContentView(R.layout.personunable_edit_activity);
        TextView topic = (TextView) findViewById(R.id.topic);
        topic.setText(getString(R.string.pu_typecode));
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
                addFragment(Action.INSERT, null, null, null, null, null, null,
                        DateTime.getCurrentTime());
                break;
            case R.id.save:
                setSupportProgressBarIndeterminateVisibility(true);
                doSave();
                setSupportProgressBarIndeterminateVisibility(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // DO SAVE HERE
    public void doSave() {

        boolean finishable = true;

        for (String typecode : getDeleteList()) {
            Uri uri = PersonunableType.CONTENT_URI;
            ContentResolver cr = getContentResolver();
            int count = cr.delete(uri,
                    "pid=? AND pcucodeperson=? AND typecode =?", new String[]{
                            mPID, mPCUCODEPERSON, typecode});

            System.out.println("deleted diag count=" + count + " +uri="
                    + uri.toString());
        }

        ArrayList<String> codeList = new ArrayList<String>();

        for (String tag : getEditList()) {
            Log.d(TAG, "tag=" + tag);
            unableFragment f = (unableFragment) getSupportFragmentManager()
                    .findFragmentByTag(tag);
            if (f != null) {
                EditTransaction et = beginTransaction();
                f.onSave(et);

                if (!et.canCommit()) {
                    finishable = false;
                    break;
                }

                String code = et.getContentValues().getAsString(
                        PersonunableType.TYPECODE);
                if (isAddedCode(codeList, code)) {
                    finishable = false;
                    Toast.makeText(this, R.string.err_duplicate_disabletype,
                            Toast.LENGTH_SHORT).show();
                    break;
                } else
                    codeList.add(code);

                ContentValues cv = et.getContentValues();
                cv.put(PersonunableType.PID, mPID);
                cv.put(PersonunableType.PCUCODEPERSON, mPCUCODEPERSON);
                cv.put(PersonunableType.DATEUPDATE,
                        DateTime.getCurrentDateTime());

                String action = f.action;
                if (action.equals(Action.INSERT)) {
                    et.commit(PersonunableType.CONTENT_URI);
                    f.action = Action.EDIT;
                    f.key = code;

                    System.out.println("I'M HERE");
                } else if (action.equals(Action.EDIT)) {
                    Uri updateUri = PersonunableType.CONTENT_URI;
                    et.commit(updateUri, "pid=? AND typecode=?", new String[]{
                            mPID, f.key});

                }

            }
        }

        if (!checkHasThisPerson()) {
            addToPersonUnable();
        }
        if (finishable) {
            this.finish();
        }

    }

    public boolean checkHasThisPerson() {
        Cursor c = getContentResolver().query(Personunable.CONTENT_URI,
                new String[]{Personunable.PCUCODEPERSON, Personunable.PID},
                "pid = ?", new String[]{getPid()}, Personunable.PID);
        if (c.moveToFirst()) {
            System.out.println("Has " + getPid() + " as Pid");
            return true;
        }
        System.out.println("Don't Has " + getPid() + " as Pid");
        return false;
    }

    private boolean addToPersonUnable() {
        ContentValues cv = new ContentValues();
        cv.put(Personunable.PCUCODEPERSON, getPcucodePerson());
        cv.put(Personunable.PID, getPid());
        cv.put(Personunable.DATEREG, DateTime.getCurrentDate());
        System.out.println("Add " + getPid() + " to Personunable");
        getContentResolver().insert(Personunable.CONTENT_URI, cv);
        return true;
    }

    protected String removeFromPersonUnable(String pid) {
        return "remove : "
                + getContentResolver().delete(Personunable.CONTENT_URI,
                "pcucodeperson = ? AND pid = ?",
                new String[]{getPcucodePerson(), getPid()});
    }

    public boolean isAddedCode(ArrayList<String> codeList, String code) {
        for (String c : codeList) {
            if (code.equals(c))
                return true;
        }
        return false;
    }

    public void addFragment(String Action, String arg1, String arg2,
                            String arg3, String arg4, String arg5, String arg6, String tag) {

        addEditFragment(unableFragment.newInstance(Action, arg1, arg2, arg3,
                arg4, arg5, arg6, tag), tag);
    }

    private void generateanimaltype(Cursor c) {

        for (int i = 0; i <= 5; i++) {
            System.out.println("arg" + i + " " + c.getString(i));
        }

        String arg1 = c.getString(0);
        String arg2 = c.getString(1);
        String arg3 = c.getString(2);
        String arg4 = c.getString(3);
        String arg5 = c.getString(4);
        String arg6 = c.getString(5);

        System.out.println("I've got this grow arg1 = " + arg1);
        addEditFragment(unableFragment.newInstance(Action.EDIT, arg1, arg2,
                arg3, arg4, arg5, arg6, arg1), arg1);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub

        setSupportProgressBarIndeterminateVisibility(true);
        CursorLoader cl = new CursorLoader(this, PersonunableType.CONTENT_URI,
                PROJECTION, "pid =? AND pcucodeperson=?", new String[]{mPID,
                mPCUCODEPERSON}, PersonunableType.DEFAULT_SORTING);
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
                        generateanimaltype(c);
                    } while (c.moveToNext());
                }
            });

        } else {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String tag = DateTime.getCurrentDateTime();
                    unableFragment f = unableFragment.newInstance(
                            Action.INSERT, null, null, null, null, null, null,
                            tag);
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

    public static class unableFragment extends EditFragment {
        SearchableButton typecode;
        ArrayFormatSpinner quality;
        ThaiDatePicker quality2;
        ArrayFormatSpinner quality3;
        SearchableButton quality4;
        ThaiDatePicker quality5;
        ImageButton mClose;

        private String arg1;
        private String arg2;
        private String arg3;
        private String arg4;
        private String arg5;
        private String arg6;

        public static unableFragment newInstance(String action, String type,
                                                 String arg2, String arg3, String arg4, String arg5,
                                                 String arg6, String tag) {

            unableFragment f = new unableFragment();

            // Bundle args = new Bundle(f.getBaseArguments(action, tag, code));
            Bundle args = new Bundle(f.getBaseArguments(action, tag, type));

            args.putString("arg1", type);
            args.putString("arg2", arg2);
            args.putString("arg3", arg3);
            args.putString("arg4", arg4);
            args.putString("arg5", arg5);
            args.putString("arg6", arg6);
            f.setArguments(args);

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.pesonunable_type_fragment,
                    container, false);

            typecode = (SearchableButton) v.findViewById(R.id.type);
            typecode.setDialog(getFragmentManager(),
                    IncomepleteDialogList.class, "Incomplete");
            quality = (ArrayFormatSpinner) v.findViewById(R.id.value1);
            quality.setArray(R.array.unablelevel);

            quality2 = (ThaiDatePicker) v.findViewById(R.id.value2);

            quality3 = (ArrayFormatSpinner) v.findViewById(R.id.value3);
            quality3.setArray(R.array.disabcause);
            quality3.setOnItemSelectedListener(discauseListener);

            quality4 = (SearchableButton) v.findViewById(R.id.value4);
            quality4.setDialog(getFragmentManager(), DiagnosisListDialog.class,
                    "Diagcode");

            quality5 = (ThaiDatePicker) v.findViewById(R.id.value5);

            // SET CURRENT DATE
            Date cDate = Date.newInstance(DateTime.getCurrentDate());
            quality2.updateDate(cDate);
            quality5.updateDate(cDate);
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
            arg4 = args.getString("arg4");
            arg5 = args.getString("arg5");
            arg6 = args.getString("arg6");
            doInitializeView();

        }

        private void doInitializeView() {
            if (arg1 != null)
                typecode.setSelectionById(arg1);
            quality.setSelection(arg2);
            if (arg3 != null)
                quality2.updateDate(Date.newInstance(arg3));
            quality3.setSelection(arg4);
            if (arg5 != null)
                quality4.setSelectionById(arg5);
            if (arg6 != null)
                quality5.updateDate(Date.newInstance(arg6));

        }

        @Override
        public boolean onSave(EditTransaction et) {
            // TODO Auto-generated method stub
            et.retrieveData(PersonunableType.TYPECODE, typecode, false, null,
                    null);
            if (quality3.getSelectionId().equals("3"))
                et.retrieveData(PersonunableType.DIAGCAUSE, quality4, false,
                        null, null);
            ContentValues cv = et.getContentValues();
            cv.put(PersonunableType.TYPECODE, typecode.getSelectId());
            cv.put(PersonunableType.UNABLELEVEL, quality.getSelectionId());
            cv.put(PersonunableType.DATEFOUND, quality2.getDate().toString());

            cv.put(PersonunableType._DATEUPDATE, DateTime.getCurrentDateTime());

            cv.put(PersonunableType.DISABCAUSE, quality3.getSelectionId());
            if (quality4.isShown() && TextUtils.isEmpty(quality4.getSelectId()))
                cv.put(PersonunableType.DIAGCAUSE, quality4.getSelectId());
            cv.put(PersonunableType.DATESTART, quality5.getDate().toString());
            return true;
        }

        OnItemSelectedListener discauseListener = new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                String data = quality3.getSelectionId();
                quality4.setVisibility(data.equals("3") ? View.VISIBLE
                        : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        };

    }
}
