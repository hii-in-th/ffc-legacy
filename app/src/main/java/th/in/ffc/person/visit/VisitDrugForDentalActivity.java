package th.in.ffc.person.visit;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import th.in.ffc.R;
import th.in.ffc.code.DentcodeListDialog;
import th.in.ffc.code.DentistListDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.CodeProvider.Drug;
import th.in.ffc.provider.PersonProvider.PersonColumns;
import th.in.ffc.provider.PersonProvider.VisitDrug;
import th.in.ffc.provider.PersonProvider.VisitDrugDental;
import th.in.ffc.provider.PersonProvider.VisitDrugDentalDiag;
import th.in.ffc.util.DateTime;
import th.in.ffc.widget.SearchableButton;

import java.util.ArrayList;

public class VisitDrugForDentalActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    public String mVISITNO;
    public String mPCUCODE;
    public String dentcode;

    public static final String[] PROJECTION = new String[]{
            VisitDrug.CODE, VisitDrug.DOCTOR1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        mVISITNO = getVisitNo();
        mPCUCODE = getPcuCode();

        System.out.println("HELLO I'M FROM " + mVISITNO + ":" + mPCUCODE);
        setContentView(R.layout.visit_drugfordental_activity);
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

        for (String typecode : getDeleteList()) {
            Uri uri = VisitDrug.CONTENT_URI;
            ContentResolver cr = getContentResolver();
            cr.delete(uri, "visitno=? AND pcucode=? AND drugcode =?", new String[]{mVISITNO, mPCUCODE, typecode});
            cr.delete(VisitDrugDental.CONTENT_URI, "visitno=? AND pcucode=? AND dentcode=?", new String[]{mVISITNO, mPCUCODE, typecode});
            cr.delete(VisitDrugDentalDiag.CONTENT_URI, "visitno=? AND pcucode=? AND dentcode=?", new String[]{mVISITNO, mPCUCODE, typecode});
//			System.out.println("deleted drugForDental count=" + count + " +uri="+ uri.toString());
//			System.out.println("deleted drugDental count=" + count2 + " +uri="+ VisitDrugDental.CONTENT_URI.toString());
//			System.out.println("deleted drugDentalDiag count=" + count3 + " +uri="+ VisitDrugDentalDiag.CONTENT_URI.toString());
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

                String code = et.getContentValues().getAsString(VisitDrug.CODE);
                if (isAddedCode(codeList, code)) {
                    finishable = false;
                    Toast.makeText(this, R.string.err_duplicate_dentcode, Toast.LENGTH_SHORT).show();
                    break;
                } else
                    codeList.add(code);

                ContentValues cv = et.getContentValues();
                cv.put(VisitDrug.NO, mVISITNO);
                cv.put(VisitDrug._PCUCODE, mPCUCODE);

                String action = f.action;
                if (action.equals(Action.INSERT)) {
                    et.commit(VisitDrug.CONTENT_URI);
                    f.action = Action.EDIT;
                    f.key = code;

                    System.out.println("I'M HERE");
                } else if (action.equals(Action.EDIT)) {
                    Uri updateUri = VisitDrug.CONTENT_URI;
                    et.commit(updateUri, "visitno=? AND drugcode=?", new String[]{mVISITNO, f.key});

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

        addEditFragment(customFragment.newInstance(Action, arg1, arg2, arg3, tag), tag);
    }

    private void generateanimaltype(Cursor c) {

        String arg1 = c.getString(0);
        String arg2 = c.getString(1);
        String arg3 = null;

        System.out.println("I've got this type arg1 = " + arg1);
        addEditFragment(
                customFragment.newInstance(Action.EDIT, arg1, arg2, arg3, arg1),
                arg1);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub

        setSupportProgressBarIndeterminateVisibility(true);
        CursorLoader cl = new CursorLoader(this, VisitDrug.CONTENT_URI,
                PROJECTION, "visitno =? AND pcucode=? AND dentalcode is not null", new String[]{mVISITNO, mPCUCODE},
                VisitDrug.DEFAULT_SORTING);
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
                        generateanimaltype(c);
                    } while (c.moveToNext());
                }
            });

        } else {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String tag = DateTime.getCurrentDateTime();
                    customFragment f = customFragment.newInstance(Action.INSERT, null, null, null, tag);
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

    public void StartDrugDental(Intent intent) {
        intent.putExtra(VisitDrug.NO, mVISITNO);
        intent.putExtra(VisitDrug._PCUCODE, mPCUCODE);
        startActivity(intent);
    }

    public void deleteInner(String typecode) {
        int count2 = getContentResolver().delete(VisitDrugDental.CONTENT_URI, "visitno=? AND pcucode=? AND dentcode=?", new String[]{mVISITNO, mPCUCODE, typecode});
        int count3 = getContentResolver().delete(VisitDrugDentalDiag.CONTENT_URI, "visitno=? AND pcucode=? AND dentcode=?", new String[]{mVISITNO, mPCUCODE, typecode});


        System.out.println("TYPECODE : " + typecode + "DD:" + count2 + " DDD:" + count3);

    }

    public static class customFragment extends EditFragment {
        SearchableButton typecode;
        SearchableButton dentist;
        EditText money;
        Button goMap;
        ImageButton mClose;

        private String arg1;
        private String arg2;
        //private String arg3;

        private String cost;
        private String real;
        private String toothType;

        public static customFragment newInstance(String action, String type,
                                                 String arg2, String arg3, String tag) {

            customFragment f = new customFragment();

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
            View v = inflater.inflate(R.layout.visit_drugfordental_fragment,
                    container, false);
            goMap = (Button) v.findViewById(R.id.goTeeth);
            typecode = (SearchableButton) v.findViewById(R.id.type);
            typecode.setDialog(getFragmentManager(), DentcodeListDialog.class, "Dentcode");
            typecode.addTextChangedListener(tw);
            dentist = (SearchableButton) v.findViewById(R.id.dentist);
            dentist.setDialog(getFragmentManager(), DentistListDialog.class, "DENTIST");
            money = (EditText) v.findViewById(R.id.service_charge);

            mClose = (ImageButton) v.findViewById(R.id.deleted);
            setAsRemoveButton(mClose);

            return v;
        }

        @Override
        protected void setAsRemoveButton(View view) {
            if (view != null)
                view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "remove click");
                        if (getmRemoveListener() != null) {
                            getmRemoveListener().onRemove(action, tag, key);
                            VisitDrugForDentalActivity activity = (VisitDrugForDentalActivity) getActivity();
                            activity.deleteInner(typecode.getSelectId());
                        }


                    }
                });
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle args = getArguments();

            arg1 = args.getString("arg1");
            arg2 = args.getString("arg2");
            //arg3 = args.getString("arg3");

            doInitializeView();

        }

        private void doInitializeView() {
            if (arg1 != null)
                typecode.setSelectionById(arg1);
            if (arg2 != null)
                dentist.setSelectionById(arg2);
            goMap.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (!TextUtils.isEmpty(typecode.getSelectId())) {
                        Intent intent = null;
                        if (toothType != null)
                            intent = new Intent(getActivity(), VisitDrugDentalActivity.class);
                        else
                            intent = new Intent(getActivity(), ToothSelectorActivity.class);

                        intent.setData(Uri.withAppendedPath(VisitDrugDental.CONTENT_URI, VisitDrug.NO));
                        intent.putExtra(PersonColumns._PID, "00000");
                        intent.putExtra(PersonColumns._PCUCODEPERSON, "00000");
                        intent.putExtra(VisitDrugDental.DENTCODE, typecode.getSelectId());
                        intent.putExtra(Drug.TOOTHTYPE, toothType);
//						Toast.makeText(getActivity(), "SELECT "+typecode.getSelectId(), Toast.LENGTH_LONG).show();
                        VisitDrugForDentalActivity go = (VisitDrugForDentalActivity) getActivity();
                        go.StartDrugDental(intent);
                    } else {
                        Toast.makeText(getActivity(), R.string.err_no_dentcode, Toast.LENGTH_LONG).show();
                    }
                }
            });

        }

        @Override
        public boolean onSave(EditTransaction et) {
            // TODO Auto-generated method stub
            et.retrieveData(VisitDrug.CODE, typecode, false, null, null);

            ContentValues cv = et.getContentValues();
            cv.put(VisitDrug.CODE, typecode.getSelectId());
            cv.put("dentalcode", typecode.getSelectId());
            if (dentist.getSelectId() != null)
                cv.put(VisitDrug.DOCTOR1, dentist.getSelectId());
            cv.put("unit", "1");
            cv.put("costprice", cost);
            cv.put("realprice", real);
            //cv.put(VisitDrug.DOCTOR2, "waitForMethod");
            cv.put(VisitDrug._DATEUPDATE, DateTime.getCurrentDateTime());
            return true;
        }

        TextWatcher tw = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (!TextUtils.isEmpty(typecode.getSelectId())) {
                    String code = typecode.getSelectId();
                    Cursor c = getActivity().getContentResolver().query(Drug.CONTENT_URI,
                            new String[]{Drug.CODE, Drug.SELL, Drug.COST, Drug.TOOTHTYPE}, "drugcode = ?", new String[]{code}, Drug.DEFAULT_SORTING);
                    if (c.moveToFirst()) {
                        cost = c.getString(1);
                        real = c.getString(2);
                        toothType = c.getString(3);

                        if (!TextUtils.isEmpty(real)) {
                            money.setText(real);

                        }

                        setAsRemoveButton(mClose);

                    }


                }


            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

        };


    }
}
