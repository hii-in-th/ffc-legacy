package th.in.ffc.person.visit;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import th.in.ffc.R;
import th.in.ffc.code.GrowCodeListDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.CodeProvider.Grow;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.VisitPersongrow;
import th.in.ffc.util.AgeCalculator;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.widget.SearchableButton;

import java.util.ArrayList;

public class VisitPersongrowActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    public static final String[] PROJECTION = new String[]{
            VisitPersongrow.GROWCODE, VisitPersongrow.AGEMONTHCAN,
            VisitPersongrow.ABNORMAL};
    ScrollView scroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        getAge();
        setContentView(R.layout.visit_persongrow);
        setSupportProgressBarIndeterminateVisibility(false);

        scroller = (ScrollView) findViewById(R.id.scroller);

        if (savedInstanceState == null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    private void getAge() {
        Cursor c = getContentResolver().query(Person.CONTENT_URI, new String[]{Person.BIRTH}, "pid=?", new String[]{getPid()}, Person.DEFAULT_SORTING);
        if (c.moveToFirst()) {
            Date current = Date.newInstance(DateTime.getCurrentDate());
            Date born = Date.newInstance(c.getString(0));
            AgeCalculator ac = new AgeCalculator(current, born);
            Date age = ac.calulate();
            int monthTotal = (age.year * 12) + age.month;
            if (monthTotal > 72 + 6) {

                doDialogMsgBuilder();
            }
        }
    }

    private void doDialogMsgBuilder() {
        // TODO Auto-generated method stub
        AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
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
                addPersongrowFragment(Action.INSERT, null, null, null,
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

        for (String growtype : getDeleteList()) {
            Uri uri = VisitPersongrow.CONTENT_URI;
            ContentResolver cr = getContentResolver();
            int count = cr.delete(uri, "pid=? AND growcode = ?",
                    new String[]{getPid(), growtype});

            System.out.println("deleted diag count=" + count + " +uri="
                    + uri.toString());
        }

        ArrayList<String> codeList = new ArrayList<String>();

        for (String tag : getEditList()) {
            Log.d(TAG, "tag=" + tag);
            GrowFragment f = (GrowFragment) getSupportFragmentManager()
                    .findFragmentByTag(tag);
            if (f != null) {
                EditTransaction et = beginTransaction();
                f.onSave(et);

                if (!et.canCommit()) {
                    finishable = false;
                    break;
                }

                String code = et.getContentValues().getAsString(
                        VisitPersongrow.GROWCODE);
                if (isAddedCode(codeList, code)) {
                    finishable = false;
                    Toast.makeText(this, R.string.err_duplicate_growtype, Toast.LENGTH_SHORT).show();
                    break;
                } else
                    codeList.add(code);

                ContentValues cv = et.getContentValues();
                cv.put(VisitPersongrow.PCUCODEPERSON, getPcucodePerson());
                cv.put(VisitPersongrow.PID, getPid());
                cv.put(VisitPersongrow.DATESURVEY, DateTime.getCurrentDateTime());

                String action = f.action;
                if (action.equals(Action.INSERT)) {
                    et.commit(VisitPersongrow.CONTENT_URI);
                    f.action = Action.EDIT;
                    f.key = code;


                } else if (action.equals(Action.EDIT)) {
                    Uri updateUri = VisitPersongrow.CONTENT_URI;
                    et.commit(updateUri, "pid=? AND growcode=?", new String[]{getPid(), f.key});

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

    public void addPersongrowFragment(String Action, String type, String can,
                                      String abno, String tag) {

        addEditFragment(GrowFragment.newInstance(Action, type, can, abno, tag),
                tag);
        scroller.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                moveScrollDown();
            }
        }, 500);
    }

    private void generatePersongrow(Cursor c) {

        String type = c.getString(0);
        String can = c.getString(1);
        String abno = c.getString(2);

        System.out.println("I've got this grow type = " + type);
        addEditFragment(
                GrowFragment.newInstance(Action.EDIT, type, can, abno, type),
                type);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub

        setSupportProgressBarIndeterminateVisibility(true);
        CursorLoader cl = new CursorLoader(this, VisitPersongrow.CONTENT_URI,
                PROJECTION, "pid=" + getPid(), null,
                VisitPersongrow.DEFAULT_SORTING);
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
                        generatePersongrow(c);
                    } while (c.moveToNext());
                }
            });

        } else {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String tag = DateTime.getCurrentDateTime();
                    GrowFragment f = GrowFragment.newInstance(Action.INSERT, null, null, null, tag);
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

    public static class GrowFragment extends EditFragment {

        private SearchableButton growType;
        private ImageButton mClose;
        private ImageView imgStatus;
        private TextView ageStd;
        private EditText ageCan;
        private TextView abnomal;

        private String type;
        private String can;
        private String ab;

        public static GrowFragment newInstance(String action, String type,
                                               String can, String abno, String tag) {

            GrowFragment f = new GrowFragment();

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
            View v = inflater.inflate(R.layout.visit_persongrow_fragment,
                    container, false);

            growType = (SearchableButton) v.findViewById(R.id.type);
            ageStd = (TextView) v.findViewById(R.id.ageStd);
            ageCan = (EditText) v.findViewById(R.id.ageCan);
            abnomal = (EditText) v.findViewById(R.id.abnormal);
            mClose = (ImageButton) v.findViewById(R.id.deleted);
            imgStatus = (ImageView) v.findViewById(R.id.ImgStatus);
            growType.addTextChangedListener(tw);
            setAsRemoveButton(mClose);

            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle args = getArguments();

            type = args.getString("type");
            can = args.getString("can");
            ab = args.getString("ab");
            doInitializeView();

        }

        private void doInitializeView() {

            ageStd.setText("0");
            abnomal.setText(ab);
            growType.setDialog(getFragmentManager(), GrowCodeListDialog.class,
                    "tag" + tag);
            if (!TextUtils.isEmpty(type)) {
                growType.setSelectionById(type);
            }
            growType.addTextChangedListener(tw);
            ageCan.addTextChangedListener(ageWatcher);
            ageCan.setText(can);

        }

        TextWatcher ageWatcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                afterTextChanged(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
                afterTextChanged(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                System.out.println("STD " + ageStd.getText() + " CAN " + ageCan.getText());
                if (!TextUtils.isEmpty(ageStd.getText()) && !TextUtils.isEmpty(ageCan.getText())) {
                    int aStd = Integer.parseInt(ageStd.getText().toString());
                    int aCan = Integer.parseInt(ageCan.getText().toString());
                    imgStatus.setImageResource((aStd < aCan ? R.drawable.sad : R.drawable.happy));
                }
            }
        };

        TextWatcher tw = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                String type = growType.getSelectId();
                if (!TextUtils.isEmpty(type)) {

                    Cursor c = getActivity().getContentResolver().query(
                            Grow.CONTENT_URI, new String[]{Grow.STD},
                            "growcode = ?", new String[]{type},
                            Grow.DEFAULT_SORTING);
                    if (c.moveToFirst()) {
                        ageStd.setText(c.getString(0));
                    }
                }
            }
        };

        @Override
        public boolean onSave(EditTransaction et) {
            // TODO Auto-generated method stub
            et.retrieveData(VisitPersongrow.GROWCODE, growType, false, null, null);
            et.retrieveData(VisitPersongrow.AGEMONTHCAN, ageCan, false, 0, 99, getString(R.string.err_no_value));

            ContentValues cv = et.getContentValues();
            // Put What
            String growtype = growType.getSelectId();
            String acan = ageCan.getText().toString();
            String abnom = abnomal.getText().toString();

            if (!TextUtils.isEmpty(growtype) && TextUtils.getTrimmedLength(growtype) == 1) {
                // Add Zero for 1 digit
                growtype = "0" + growtype;
            }

            System.out.println("my growtype = " + growtype);
            cv.put(VisitPersongrow.GROWCODE, growtype);
            cv.put(VisitPersongrow.AGEMONTHCAN, acan);
            cv.put(VisitPersongrow.ABNORMAL, abnom);

            return true;
        }

    }

}
