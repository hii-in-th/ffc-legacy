package th.in.ffc.person.visit;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import th.in.ffc.R;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.PersonProvider.VisitScreenspecialdisease;
import th.in.ffc.util.DateTime;
import th.in.ffc.widget.ArrayFormatSpinner;

import java.util.ArrayList;

public class VisitScreenspecialdiseaseActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    public static final String[] PROJECTION = new String[]{
            VisitScreenspecialdisease.CODESCREEN, VisitScreenspecialdisease.CODERESULT,
            VisitScreenspecialdisease.REMART};
    ScrollView scroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visit_screenspecialdisease_activity);
        setSupportProgressBarIndeterminateVisibility(false);

        scroller = (ScrollView) findViewById(R.id.scroller);
        if (savedInstanceState == null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    private void doDialogMsgBuilder() {
        // TODO Auto-generated method stub
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.OverAge))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });

        AlertDialog alert = builder.create();
        alert.show();
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
                addTHEFragment(Action.INSERT, null, null, null,
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

    public void doSave() {

        boolean finishable = true;

        for (String type : getDeleteList()) {
            Uri uri = VisitScreenspecialdisease.CONTENT_URI;
            ContentResolver cr = getContentResolver();
            int count = cr.delete(uri, "visitno=? AND codescreen = ?",
                    new String[]{getVisitNo(), type});

            System.out.println("deleted diag count=" + count + " +uri="
                    + uri.toString());
        }


        ArrayList<String> codeList = new ArrayList<String>();

        for (String tag : getEditList()) {
            Log.d(TAG, "tag=" + tag);
            THEFragment f = (THEFragment) getSupportFragmentManager()
                    .findFragmentByTag(tag);
            if (f != null) {
                EditTransaction et = beginTransaction();
                f.onSave(et);

                if (!et.canCommit()) {
                    finishable = false;
                    break;
                }

                String code = et.getContentValues().getAsString(
                        VisitScreenspecialdisease.CODESCREEN);
                if (isAddedCode(codeList, code)) {
                    finishable = false;
                    Toast.makeText(this, R.string.err_duplicate_diag, Toast.LENGTH_SHORT).show();
                    break;
                } else
                    codeList.add(code);

                ContentValues cv = et.getContentValues();
                cv.put(VisitScreenspecialdisease.PCUCODE, getPcuCode());
                cv.put(VisitScreenspecialdisease.VISITNO, getVisitNo());

                String action = f.action;
                if (action.equals(Action.INSERT)) {
                    et.commit(VisitScreenspecialdisease.CONTENT_URI);
                    f.action = Action.EDIT;
                    f.key = code;


                } else if (action.equals(Action.EDIT)) {
                    Uri updateUri = VisitScreenspecialdisease.CONTENT_URI;
                    et.commit(updateUri, "visitno=? AND codescreen=?", new String[]{getVisitNo(), f.key});

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

    public void addTHEFragment(String Action, String type, String can,
                               String abno, String tag) {

        addEditFragment(THEFragment.newInstance(Action, type, can, abno, tag),
                tag);
        scroller.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                moveScrollDown();
            }
        }, 500);
    }

    private void generateFragment(Cursor c) {

        String type = c.getString(0);
        String can = c.getString(1);
        String abno = c.getString(2);

        System.out.println("I've got this type = " + type);
        addEditFragment(
                THEFragment.newInstance(Action.EDIT, type, can, abno, type),
                type);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub

        setSupportProgressBarIndeterminateVisibility(true);
        CursorLoader cl = new CursorLoader(this, VisitScreenspecialdisease.CONTENT_URI,
                PROJECTION, "pcucode =? AND visitno =? ", new String[]{getPcuCode(), getVisitNo()},
                VisitScreenspecialdisease.DEFAULT_SORTING);
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
                        generateFragment(c);
                    } while (c.moveToNext());
                }
            });

        } else if (getEditList().size() == 0) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String tag = DateTime.getCurrentDateTime();
                    THEFragment f = THEFragment.newInstance(Action.INSERT, null, null, null, tag);
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

    private void moveScrollDown() {


        scroller.fullScroll(View.FOCUS_DOWN);
    }

    public static class THEFragment extends EditFragment {

        private ArrayFormatSpinner Type;
        private ImageButton mClose;
        private ArrayFormatSpinner value1;
        private EditText value2;
        private LinearLayout sad;
        private Button goQuestion;
        private String type;
        private String val1;
        private String val2;

        public static THEFragment newInstance(String action, String type,
                                              String can, String abno, String tag) {

            THEFragment f = new THEFragment();

            // Bundle args = new Bundle(f.getBaseArguments(action, tag, code));
            Bundle args = new Bundle(f.getBaseArguments(action, tag, type));

            args.putString("type", type);
            args.putString("can", can);
            args.putString("ab", abno);

            f.setArguments(args);

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.visit_screenspecialdisease_fragment,
                    container, false);

            Type = (ArrayFormatSpinner) v.findViewById(R.id.type);
            value1 = (ArrayFormatSpinner) v.findViewById(R.id.value1);
            value2 = (EditText) v.findViewById(R.id.value2);
            mClose = (ImageButton) v.findViewById(R.id.deleted);
            sad = (LinearLayout) v.findViewById(R.id.header);
            goQuestion = (Button) v.findViewById(R.id.goToQuestion);

            goQuestion.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mClose.performClick();
                    System.out.println("Clicked");
                    VisitScreenspecialdiseaseActivity atv = (VisitScreenspecialdiseaseActivity) getActivity();
                    Intent intent = new Intent(getActivity(), VisitScreenspecialquestionActivity.class);
                    intent.putExtra(VisitScreenspecialdisease.PCUCODE, atv.getPcuCode());
                    intent.putExtra(VisitScreenspecialdisease.VISITNO, atv.getVisitNo());
                    startActivity(intent);


                }
            });

            Type.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {
                    // TODO Auto-generated method stub
                    if (!TextUtils.isEmpty(Type.getSelectionId())) {
                        String id = Type.getSelectionId();
                        if (id.equals("c01")) {
                            sad.setVisibility(View.VISIBLE);
                        } else {
                            sad.setVisibility(View.GONE);
                        }

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub

                }
            });
            setAsRemoveButton(mClose);

            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle args = getArguments();

            type = args.getString("type");
            val1 = args.getString("can");
            val2 = args.getString("ab");
            doInitializeView();

        }

        private void doInitializeView() {


            Type.setArray(R.array.codeScreen);
            value1.setArray(R.array.coderesult);
            if (!TextUtils.isEmpty(type))
                Type.setSelection(type);

            if (!TextUtils.isEmpty(val1))
                value1.setSelection(val1);

            if (!TextUtils.isEmpty(val2))
                value2.setText(val2);

        }


        @Override
        public boolean onSave(EditTransaction et) {
            // TODO Auto-generated method stub
            et.retrieveData(VisitScreenspecialdisease.CODESCREEN, Type, false, null, null);

            ContentValues cv = et.getContentValues();
            // Put What
            String type = Type.getSelectionId();
            String val1 = value1.getSelectionId();
            String val2 = value2.getText().toString();


            System.out.println("TYPE = " + type);
            cv.put(VisitScreenspecialdisease.CODESCREEN, type);
            cv.put(VisitScreenspecialdisease.CODERESULT, val1);
            cv.put(VisitScreenspecialdisease.REMART, val2);

            return true;
        }

    }

}
