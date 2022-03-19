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
import android.util.Log;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import th.in.ffc.R;
import th.in.ffc.code.DiagnosisListDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.PersonProvider.VisitDrug;
import th.in.ffc.provider.PersonProvider.VisitDrugDental;
import th.in.ffc.provider.PersonProvider.VisitDrugDentalDiag;
import th.in.ffc.util.DateTime;
import th.in.ffc.widget.SearchableButton;

import java.util.ArrayList;

public class VisitDrugDentalDiagActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    public String mVISITNO;
    public String mPCUCODE;
    public String dentcode;
    public String tootharea;

    public static final String[] PROJECTION = new String[]{
            VisitDrugDentalDiag.DIAGCODE, VisitDrugDentalDiag.DXTYPE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        mVISITNO = getIntent().getStringExtra(VisitDrug.NO);
        mPCUCODE = getIntent().getStringExtra(VisitDrug._PCUCODE);
        dentcode = getIntent().getStringExtra(VisitDrugDental.DENTCODE);
        tootharea = getIntent().getStringExtra(VisitDrugDental.TOOTHAREA);

        System.out.println("HELLO I'M FROM " + mVISITNO + ":" + mPCUCODE + ":" + dentcode + ":" + tootharea);
        setContentView(R.layout.visit_drugfordental_activity);
        setSupportProgressBarIndeterminateVisibility(false);

        if (savedInstanceState == null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_activity, menu);
        String[] diagType = getResources().getStringArray(R.array.diag_DX);
        SubMenu sub = menu.addSubMenu(R.string.add);
        sub.add(0, 2, 0, diagType[2]);
        sub.add(0, 4, 0, diagType[4]);
        sub.add(0, 5, 0, diagType[5]);
        sub.setIcon(R.drawable.ic_action_add);
        sub.getItem().setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 2:
            case 4:
            case 5:
                addFragment(Action.INSERT, null, item.getItemId(), null, DateTime.getCurrentTime());
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

        for (String typecode : getDeleteList()) {
            //Uri uri = VisitDrugDentalDiag.CONTENT_URI;
            ContentResolver cr = getContentResolver();
            cr.delete(VisitDrugDentalDiag.CONTENT_URI, "visitno=? AND pcucode=? AND dentcode=? AND tootharea=? AND diagcode=?", new String[]{mVISITNO, mPCUCODE, dentcode, tootharea, typecode});
            //System.out.println("deleted diagcode count=" + count3 + " +uri="+ VisitDrugDentalDiag.CONTENT_URI.toString());
        }

        ArrayList<String> codeList = new ArrayList<String>();

        for (String tag : getEditList()) {
            Log.d(TAG, "tag=" + tag);
            customFragment f = (customFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (f != null) {
                EditTransaction et = beginTransaction();
                f.onSave(et);

                if (!et.canCommit()) {
                    finishable = false;
                    break;
                }

                String code = et.getContentValues().getAsString(VisitDrugDentalDiag.DIAGCODE);
                if (isAddedCode(codeList, code)) {
                    finishable = false;
                    Toast.makeText(this, R.string.err_duplicate_diag, Toast.LENGTH_SHORT).show();
                    break;
                } else
                    codeList.add(code);

                ContentValues cv = et.getContentValues();
                cv.put(VisitDrugDentalDiag.VISITNO, mVISITNO);
                cv.put(VisitDrugDentalDiag.PCUCODE, mPCUCODE);
                cv.put(VisitDrugDentalDiag.DENTCODE, dentcode);
                cv.put(VisitDrugDentalDiag.TOOTHAREA, tootharea);
                String action = f.action;
                if (action.equals(Action.INSERT)) {
                    et.commit(VisitDrugDentalDiag.CONTENT_URI);
                    f.action = Action.EDIT;
                    f.key = code;

                    System.out.println("I'M HERE");
                } else if (action.equals(Action.EDIT)) {
                    Uri updateUri = VisitDrugDentalDiag.CONTENT_URI;
                    et.commit(updateUri, "visitno=? AND pcucode=? AND dentcode=? AND tootharea=? AND diagcode=?", new String[]{mVISITNO, mPCUCODE, dentcode, tootharea, f.key});

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

    public void addFragment(String Action, String arg1, int arg2,
                            String arg3, String tag) {

        addEditFragment(customFragment.newInstance(Action, arg1, arg2, arg3, tag), tag);
    }

    private void generateFragment(Cursor c) {

        String arg1 = c.getString(0);
        int arg2 = c.getInt(1);
        String arg3 = null;

        System.out.println("I've got this type arg1 = " + arg1);
        System.out.println("I've got this type arg2 = " + arg2);
        addEditFragment(
                customFragment.newInstance(Action.EDIT, arg1, arg2, arg3, arg1),
                arg1);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub

        setSupportProgressBarIndeterminateVisibility(true);
        CursorLoader cl = new CursorLoader(this, VisitDrugDentalDiag.CONTENT_URI,
                PROJECTION, "visitno =? AND pcucode=? AND dentcode=? AND tootharea=?", new String[]{mVISITNO, mPCUCODE, dentcode, tootharea},
                VisitDrugDentalDiag.DEFAULT_SORTING);
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
                        generateFragment(c);
                    } while (c.moveToNext());
                }
            });

        } else {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String tag = DateTime.getCurrentDateTime();
                    customFragment f = customFragment.newInstance(Action.INSERT, null, 1, null, tag);
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

    public static class customFragment extends EditFragment {
        SearchableButton typecode;
        TextView dxtype;
        ImageButton mClose;

        private String arg1;
        private int arg2;


        public static customFragment newInstance(String action, String type,
                                                 int arg2, String arg3, String tag) {

            customFragment f = new customFragment();

            // Bundle args = new Bundle(f.getBaseArguments(action, tag, code));
            Bundle args = new Bundle(f.getBaseArguments(action, tag, type));

            args.putString("arg1", type);
            args.putInt("arg2", arg2);
            //args.putString("arg3", arg3);

            f.setArguments(args);

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.visit_drugdentaldiag_fragment,
                    container, false);

            typecode = (SearchableButton) v.findViewById(R.id.type);
            typecode.setDialog(getFragmentManager(), DiagnosisListDialog.class, "Diagcode");
            dxtype = (TextView) v.findViewById(R.id.dxtype);

            mClose = (ImageButton) v.findViewById(R.id.deleted);
            setAsRemoveButton(mClose);

            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle args = getArguments();

            arg1 = args.getString("arg1");
            arg2 = args.getInt("arg2");
            //arg3 = args.getString("arg3");
            doInitializeView();

        }

        private void doInitializeView() {
            String[] dx = getResources().getStringArray(R.array.diag_DX);
            dxtype.setText(dx[arg2]);
            if (arg1 != null)
                typecode.setSelectionById(arg1);
            //dxtype.setSelection(arg2);
        }

        @Override
        public boolean onSave(EditTransaction et) {
            // TODO Auto-generated method stub
            et.retrieveData(VisitDrugDentalDiag.DIAGCODE, typecode, false, null, null);

            ContentValues cv = et.getContentValues();
            cv.put(VisitDrugDentalDiag.DIAGCODE, typecode.getSelectId());
            cv.put(VisitDrugDentalDiag.DXTYPE, arg2);

            return true;
        }

    }
}
