/* ***********************************************************************
 *                                                                 _ _ _
 *                                                               ( _ _  |
 *                                                           _ _ _ _  | |
 *                                                          (_ _ _  | |_|
 *  _     _   _ _ _ _     _ _ _   _ _ _ _ _   _ _ _ _     _ _ _   | | 
 * |  \  | | |  _ _ _|   /  _ _| |_ _   _ _| |  _ _ _|   /  _ _|  | |
 * | | \ | | | |_ _ _   /  /         | |     | |_ _ _   /  /      |_|
 * | |\ \| | |  _ _ _| (  (          | |     |  _ _ _| (  (    
 * | | \ | | | |_ _ _   \  \_ _      | |     | |_ _ _   \  \_ _ 
 * |_|  \__| |_ _ _ _|   \_ _ _|     |_|     |_ _ _ _|   \_ _ _| 
 *  a member of NSTDA, @Thailand
 *  
 * ***********************************************************************
 *
 *
 * FFC-Plus Project
 *
 * Copyright (C) 2010-2012 National Electronics and Computer Technology Center
 * All Rights Reserved.
 * 
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * 
 */

package th.in.ffc.person.visit;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import th.in.ffc.R;
import th.in.ffc.code.AncRiskListDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.PersonProvider.PregnancyColumns;
import th.in.ffc.provider.PersonProvider.VisitAncPregnancy;
import th.in.ffc.provider.PersonProvider.VisitAncRisk;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.widget.ArrayFormatSpinner;
import th.in.ffc.widget.SearchableButton;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector plus
 */
public class VisitAncPregnancyActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    private static final int LOAD_LAST_PREG = 0;
    private static final int LOAD_PREG = 2;
    private static final int LOAD_RISK = 1;

    //	private String mPid;
    private String mPregNo;
    private String mAction;
    private int mMinPregNo = 1;

    private static final String[] PROJECTION = new String[]{
            VisitAncPregnancy._PREGNO, VisitAncPregnancy.LMP,
            VisitAncPregnancy.EDC, VisitAncPregnancy.FP_BEFORE,
            VisitAncPregnancy.FIRST_DATE_CHECK,
            VisitAncPregnancy.FIRST_ABNORMAL,};

    EditText PregNo;
    ArrayFormatSpinner FpBefore;
    ArrayFormatSpinner FirstAbNormal;
    ThaiDatePicker lmp;
    ThaiDatePicker edc;
    ImageView refresh;
    LinearLayout RiskLayout;
    TextView Hint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.visit_pragnancy_activity);
        setSupportProgressBarIndeterminateVisibility(false);


        mAction = getIntent().getAction();
        if (TextUtils.isEmpty(mAction))
            throw new IllegalStateException("VisitAncPregnancyActivity not support null Action Intent");

        doInitializeView(savedInstanceState);
        if (savedInstanceState == null) {
            //do by request action
            if (mAction.equals(Action.INSERT))
                getSupportLoaderManager().initLoader(LOAD_LAST_PREG, null, this);
            else if (mAction.equals(Action.EDIT)) {
                mPregNo = getIntent().getStringExtra(PregnancyColumns._PREGNO);

                if (TextUtils.isEmpty(mPregNo))
                    throw new InvalidParameterException("Edit Action must have \'pregno\'");
                if (!TextUtils.isDigitsOnly(mPregNo))
                    throw new InvalidParameterException("pregno must be digits only");

                PregNo.setEnabled(false);
                PregNo.setText(mPregNo);
                getSupportLoaderManager().initLoader(LOAD_PREG, null, this);
                getSupportLoaderManager().initLoader(LOAD_RISK, null, this);
            }
        } else {
            mAction = savedInstanceState.getString("action");
            mMinPregNo = savedInstanceState.getInt("min");
            Hint.setText(savedInstanceState.getCharSequence("hint"));
            mPregNo = savedInstanceState.getString("pregno");

            PregNo.setEnabled(savedInstanceState.getBoolean("enable"));
            RiskLayout.setVisibility(savedInstanceState.getBoolean("showRisk") ?
                    View.VISIBLE : View.GONE);

        }


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("action", mAction);
        outState.putInt("min", mMinPregNo);
        outState.putCharSequence("hint", Hint.getText());
        outState.putString("pregno", mPregNo);
        outState.putBoolean("enable", PregNo.isEnabled());
        outState.putBoolean("showRisk", RiskLayout.isShown());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_add_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                doSave();
                return true;
            case R.id.add:
                String tag = DateTime.getCurrentTime();
                AncRiskFragment f = AncRiskFragment.getNewInstance(Action.INSERT,
                        tag, null, null);
                addEditFragment(f, tag);
                break;
            default:
                return false;
        }
        return false;
    }

    private void doSave() {
        EditTransaction et = beginTransaction();
        et.retrieveData(VisitAncPregnancy._PREGNO, PregNo, false, mMinPregNo, 99, mMinPregNo + "-99");
        if (et.canCommit()) {
            ContentValues cv = et.getContentValues();
            cv.put(VisitAncPregnancy._PCUCODEPERSON, getPcucodePerson());
            cv.put(VisitAncPregnancy._PID, getPid());
            cv.put(VisitAncPregnancy.LMP, lmp.getDate().toString());
            cv.put(VisitAncPregnancy.EDC, edc.getDate().toString());
            cv.put(VisitAncPregnancy.FP_BEFORE, FpBefore.getSelectionId());
            cv.put(VisitAncPregnancy.FIRST_ABNORMAL, FirstAbNormal.getSelectionId());
            cv.put(VisitAncPregnancy._DATEUPDATE, DateTime.getCurrentDateTime());

            Uri uri = null;
            Intent data = new Intent();
            if (mAction.equals(Action.INSERT)) {
                cv.put(VisitAncPregnancy.FIRST_DATE_CHECK, DateTime.getCurrentDate());
                uri = et.commit(VisitAncPregnancy.CONTENT_URI);
                Log.d("FFC", uri.toString());

                data.setAction(Action.INSERT);
                mAction = Action.EDIT;

            } else if (mAction.equals(Action.EDIT)) {
                uri = VisitAncPregnancy.getContentUri(getPid(), mPregNo);
                int count = et.commit(uri, null, null);
                Log.d(TAG, "edit=" + count);

                data.setAction(Action.EDIT);
            }

            data.setData(uri);
            data.putExtra(PregnancyColumns._PREGNO, cv.getAsString(VisitAncPregnancy._PREGNO));


            boolean success = doSaveRiskFragment();
            if (success) {
                this.setResult(RESULT_OK, data);
                this.finish();
            } else {
                this.mPregNo = PregNo.getText().toString();
                this.PregNo.setEnabled(false);
            }
        }
    }

    private boolean doSaveRiskFragment() {
        FragmentManager fm = getSupportFragmentManager();
        for (String tag : getDeleteList()) {
            Uri uri = VisitAncRisk.getContentUri(getPid(), mPregNo, tag);
            int del = getContentResolver().delete(uri, null, null);
            Log.d(TAG, "deleted=" + del + " uri=" + uri.toString());
        }


        ArrayList<String> codeList = new ArrayList<String>();
        for (String tag : getEditList()) {
            AncRiskFragment f = (AncRiskFragment) fm.findFragmentByTag(tag);
            if (f != null) {
                EditTransaction subEt = beginTransaction();
                f.onSave(subEt);
                if (subEt.canCommit()) {

                    // check for duplication RiskCode
                    String code = subEt.getContentValues().getAsString(VisitAncRisk.CODE);
                    if (!isAddedCode(codeList, code)) {
                        codeList.add(code);
                    } else {
                        Toast.makeText(this, "duplicate", Toast.LENGTH_SHORT).show();
                        return false;
                    }


                    if (f.action.equals(Action.INSERT)) {
                        ContentValues sCv = subEt.getContentValues();

                        String riskCode = sCv.getAsString(VisitAncRisk.CODE);
                        f.action = Action.EDIT;
                        f.key = riskCode;

                        sCv.put(VisitAncPregnancy._PID, getPid());
                        sCv.put(VisitAncPregnancy._PCUCODEPERSON, getPcuCode());
                        subEt.retrieveData(VisitAncPregnancy._PREGNO, PregNo, false, 1, 99, "1-99");

                        Uri insert = subEt.commit(VisitAncRisk.CONTENT_URI);
                        Log.d(TAG, "insert=" + insert.toString());

                    } else if (f.action.equals(Action.EDIT)) {

                        Uri updateUri = VisitAncRisk.getContentUri(getPid(), mPregNo, f.key);
                        int u = subEt.commit(updateUri, null, null);
                        Log.d(TAG, "uri=" + u);
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isAddedCode(ArrayList<String> codeList, String code) {
        for (String c : codeList) {
            if (code.equals(c))
                return true;
        }
        return false;
    }

    private void doInitializeView(Bundle savedInstanceState) {
        PregNo = (EditText) findViewById(R.id.code);
        Hint = (TextView) findViewById(R.id.hint);
        FpBefore = (ArrayFormatSpinner) findViewById(R.id.contracep);
        FpBefore.setArray(R.array.contracep);
        FirstAbNormal = (ArrayFormatSpinner) findViewById(R.id.abnormal);
        FirstAbNormal.setArray(R.array.abnormal);
        RiskLayout = (LinearLayout) findViewById(R.id.risk);

        lmp = (ThaiDatePicker) findViewById(R.id.lmp);
        edc = (ThaiDatePicker) findViewById(R.id.edc);
        refresh = (ImageView) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Date l = lmp.getDate();
                edc.updateDate(l.year + 1, (l.month - 3 - 1) % 11, l.day);
            }
        });

        if (savedInstanceState == null) {
            Date date = Date.newInstance(DateTime.getCurrentDate());
            lmp.updateDate(date.year, date.month - 1, date.day);
            edc.updateDate(date.year + 1, (date.month - 3 - 1) % 11, date.day);
        }
    }

    public void doUpdateView(Cursor c) {
        String action = getIntent().getAction();
        if (!TextUtils.isEmpty(action) && action.equals(Action.INSERT)) {
            String preg_no = c.getString(c.getColumnIndex(VisitAncPregnancy._PREGNO));
            int no = 1;
            if (TextUtils.isDigitsOnly(preg_no)) {
                no = Integer.parseInt(preg_no);
                no += 1;
            }
            PregNo.setText("" + no);

        } else {
            PregNo.setText(c.getString(c
                    .getColumnIndex(VisitAncPregnancy._PREGNO)));


            String sLmp = c.getString(c.getColumnIndex(VisitAncPregnancy.LMP));
            if (!TextUtils.isEmpty(sLmp)) {
                Date lmp_date = Date.newInstance(sLmp);
                lmp.updateDate(lmp_date);
            }

            String sEdc = c.getString(c.getColumnIndex(VisitAncPregnancy.EDC));
            if (!TextUtils.isEmpty(sEdc)) {
                Date edc_date = Date.newInstance(sEdc);
                edc.updateDate(edc_date);
            }
        }
        FpBefore.setSelection(c.getString(c
                .getColumnIndex(VisitAncPregnancy.FP_BEFORE)));
        FirstAbNormal.setSelection(c.getString(c
                .getColumnIndex(VisitAncPregnancy.FIRST_ABNORMAL)));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cl;
        switch (arg0) {
            case LOAD_LAST_PREG:
                setSupportProgressBarIndeterminateVisibility(true);
                cl = new CursorLoader(this, VisitAncPregnancy.CONTENT_URI,
                        PROJECTION, "pid=" + getPid(), null, VisitAncPregnancy._PREGNO
                        + " DESC");
                return cl;
            case LOAD_RISK:
                setSupportProgressBarIndeterminateVisibility(true);
                String[] projection = new String[]{VisitAncRisk.CODE, VisitAncRisk.REMARK};
                cl = new CursorLoader(this,
                        VisitAncRisk.getContentUri(getPid(), mPregNo), projection, null,
                        null, VisitAncRisk.CODE);
                return cl;
            case LOAD_PREG:
                setSupportProgressBarIndeterminateVisibility(true);
                String[] selectionArgs = new String[]{getPid(), mPregNo};
                cl = new CursorLoader(this, VisitAncPregnancy.CONTENT_URI,
                        PROJECTION, "pid=? AND pregno=?", selectionArgs,
                        VisitAncPregnancy._PREGNO + " DESC");
                return cl;
            default:
                break;
        }

        return null;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        setSupportProgressBarIndeterminateVisibility(false);

        switch (loader.getId()) {
            case LOAD_LAST_PREG:
                if (c.moveToFirst()) {
                    doUpdateView(c);
                    if (TextUtils.isEmpty(mPregNo)) {
                        mPregNo = c.getString(c.getColumnIndex(PregnancyColumns._PREGNO));
                        if (TextUtils.isDigitsOnly(mPregNo)) {
                            mMinPregNo = Integer.parseInt(mPregNo) + 1;
                            Hint.setText("> " + mMinPregNo);
                        }
                        getSupportLoaderManager().initLoader(LOAD_RISK, null, this);
                    }
                } else {
                    mMinPregNo = 1;
                    PregNo.setText("" + mMinPregNo);
                }
                break;
            case LOAD_RISK:
                RiskAdder run = new RiskAdder(c);
                mHandle.post(run);
                break;
            case LOAD_PREG:
                if (c.moveToFirst()) {
                    doUpdateView(c);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }


    Handler mHandle = new Handler();

    public class RiskAdder implements Runnable {
        Cursor cursor;

        public RiskAdder(Cursor c) {
            cursor = c;
        }

        @Override
        public void run() {
            if (cursor == null)
                throw new IllegalStateException("cursor is null");
            if (cursor.moveToFirst()) {
                do {
                    String code = cursor.getString(cursor
                            .getColumnIndex(VisitAncRisk.CODE));
                    String remark = cursor.getString(cursor
                            .getColumnIndex(VisitAncRisk.REMARK));
                    AncRiskFragment f = AncRiskFragment.getNewInstance(
                            getIntent().getAction(), code, code, remark);
                    addEditFragment(f, code);
                } while (cursor.moveToNext());
            }
        }
    }


    @Override
    protected void addEditFragment(EditFragment f, String tag) {
        RiskLayout.setVisibility(View.VISIBLE);
        super.addEditFragment(R.id.risk, f, tag);
    }


    /**
     * Fragment for contain each AncRisk
     *
     * @author Piruin Panichphol
     * @version 1.0
     * @since Family Folder Collector plus
     */
    public static class AncRiskFragment extends EditFragment {

        String remark;

        private EditText mRemark;
        private SearchableButton mAncRiskCode;
        private ImageButton mClose;


        public static AncRiskFragment getNewInstance(String action, String tag,
                                                     String code, String remark) {
            AncRiskFragment f = new AncRiskFragment();

            Bundle args = new Bundle(f.getBaseArguments(action, tag, code));
            args.putString("remark", remark);

            f.setArguments(args);

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.visit_ancrisk_fragment,
                    container, false);
            mAncRiskCode = (SearchableButton) view.findViewById(R.id.code);
            mRemark = (EditText) view.findViewById(R.id.answer1);
            mClose = (ImageButton) view.findViewById(R.id.deleted);
            setAsRemoveButton(mClose);

            return view;
        }


        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            Bundle args = getArguments();
            if (args == null)
                throw new IllegalArgumentException(
                        "AncRiskFragment must alway have arguments");

            remark = args.getString("remark");

            mAncRiskCode.setDialog(getFragmentManager(),
                    AncRiskListDialog.class, "ancrisk_" + tag);
            if (savedInstanceState == null) {
                if (!TextUtils.isEmpty(key))
                    mAncRiskCode.setSelectionById(key);
                mRemark.setText(remark);
            }
        }


        @Override
        public boolean onSave(EditTransaction et) {
            et.retrieveData(VisitAncRisk.CODE, mAncRiskCode, false, null, null);
            et.retrieveData(VisitAncRisk.REMARK, mRemark, true, null, null);
            return true;
        }

    }

}
