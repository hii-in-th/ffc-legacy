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
 * th.in.ffc.security.LoginActivity Project
 *
 * Copyright (C) 2010-2012 National Electronics and Computer Technology Center
 * All Rights Reserved.
 * 
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * 
 */

package th.in.ffc.person.visit;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import th.in.ffc.R;
import th.in.ffc.app.form.ViewFormFragment;
import th.in.ffc.code.DrugFormulaListDialog;
import th.in.ffc.provider.CodeProvider.Diagnosis;
import th.in.ffc.provider.CodeProvider.Drug;
import th.in.ffc.provider.CodeProvider.DrugFormula;
import th.in.ffc.provider.PersonProvider.VisitDiag;
import th.in.ffc.provider.PersonProvider.VisitDrug;
import th.in.ffc.util.DateTime;
import th.in.ffc.widget.SearchableSpinner;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector Plus
 */
public class VisitDrugFormulaActivity extends VisitActivity implements
        OnItemSelectedListener, LoaderCallbacks<Cursor> {

    private SearchableSpinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visit_drug_formula);

        mSpinner = (SearchableSpinner) findViewById(R.id.code);
        mSpinner.setDialog(getSupportFragmentManager(),
                DrugFormulaListDialog.class, "drug-formula");
        mSpinner.setOnItemSelectedListener(this);
        mSpinner.setSelection(0);

        getSupportLoaderManager().initLoader(LOAD_DIAG, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                final Detail f = (Detail) getSupportFragmentManager()
                        .findFragmentByTag("detail");
                if (f != null) {
                    new Runnable() {

                        @Override
                        public void run() {
                            f.showProgess(true);
                            f.doSave();
                            finish();
                        }
                    }.run();

                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class Detail extends ViewFormFragment {

        public static final int LOAD_FORMULA = 3;
        public static final int LOAD_DRUG = 0;
        public static final int LOAD_DIAG = 1;

        public static final String[] FORMULA_PROJ = new String[]{
                DrugFormula.DIAGCODE, DrugFormula._ID};

        public static final String[] DIAG_PROJ = new String[]{
                DrugFormula.Diagnosis.DIAGCODE, DrugFormula.Diagnosis.DXTYPE,};

        public static final String[] DRUG_PROJ = new String[]{
                DrugFormula.Drug.DRUG, DrugFormula.Drug.AMOUNT,};

        public static final String SELECTION = "formulano=? AND pcucode=? ";
        public static final String SORT = "formulano";

        private String[] mSelectionArgs;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            Bundle args = getArguments();
            if (args == null)
                throw new IllegalArgumentException(
                        "Detail Fragment must have arguments");
            mSelectionArgs = new String[]{"" + args.getLong(DrugFormula._ID),
                    getFFCActivity().getPcuCode(),};

            mLoader.run();
            showProgess(true);

        }

        Runnable mLoader = new Runnable() {

            @Override
            public void run() {
                Detail v = Detail.this;

                ContentResolver cr = getActivity().getContentResolver();

                Cursor c = null;
                c = cr.query(DrugFormula.CONTENT_URI, FORMULA_PROJ, SELECTION,
                        mSelectionArgs, DrugFormula.DEFAULT_SORTING);
                mHandler.post(new Displayer(v, LOAD_FORMULA, c));

                c = cr.query(DrugFormula.Diagnosis.CONTENT_URI, DIAG_PROJ,
                        SELECTION, mSelectionArgs, SORT);
                mHandler.post(new Displayer(v, LOAD_DIAG, c));

                c = cr.query(DrugFormula.Drug.CONTENT_URI, DRUG_PROJ,
                        SELECTION, mSelectionArgs, SORT);
                mHandler.post(new Displayer(v, LOAD_DRUG, c));

            }
        };

        Handler mHandler = new Handler();

        public static class Displayer implements Runnable {

            int id;
            Cursor c;
            Detail v;

            public Displayer(Detail v, int id, Cursor c) {
                this.id = id;
                this.c = c;
                this.v = v;
            }

            @Override
            public void run() {
                switch (id) {
                    case LOAD_FORMULA:
                        v.doShowVisitDiagX(c);
                        break;
                    case LOAD_DIAG:
                        v.doShowVisitDiag(c);
                        break;
                    case LOAD_DRUG:
                        v.doShowVisitDrug(c);
                        break;
                }

                c = null;
                v = null;
            }

        }

        public void doShowVisitDiagX(Cursor c) {

            addTitle(R.string.diagnosis);
            if (c.moveToFirst()) {

                String[] array = getResources().getStringArray(R.array.diag_DX);
                do {
                    String dxType = array[1];
                    String dxCode = c.getString(c
                            .getColumnIndex(DrugFormula.DIAGCODE));
                    addContentQuery(
                            dxType,
                            Diagnosis.NAME_TH,
                            Uri.withAppendedPath(Diagnosis.CONTENT_URI, dxCode),
                            dxCode);

                } while (c.moveToNext());
                mDiagCursor = c;
            }
        }

        public void doShowVisitDiag(Cursor c) {

            // addTitle(R.string.diagnosis);
            if (c.moveToFirst()) {

                String[] array = getResources().getStringArray(R.array.diag_DX);
                do {
                    String dxType = array[c.getInt(c
                            .getColumnIndex(DrugFormula.Diagnosis.DXTYPE))];
                    String dxCode = c.getString(c
                            .getColumnIndex(DrugFormula.Diagnosis.DIAGCODE));
                    addContentQuery(
                            dxType,
                            Diagnosis.NAME_TH,
                            Uri.withAppendedPath(Diagnosis.CONTENT_URI, dxCode),
                            dxCode);

                } while (c.moveToNext());
                mDiagDetialCursor = c;
            }
        }

        public void doShowVisitDrug(Cursor c) {

            if (c.moveToFirst()) {
                addTitle(R.string.drug);
                do {
                    String drugCode = c.getString(c
                            .getColumnIndex(DrugFormula.Drug.DRUG));
                    addContentQuery(drugCode, Drug.NAME,
                            Uri.withAppendedPath(Drug.CONTENT_URI, drugCode),
                            null);
                    String unit = c.getString(c
                            .getColumnIndex(DrugFormula.Drug.AMOUNT));
                    if (!TextUtils.isEmpty(unit))
                        addSubContent("x " + unit);
                } while (c.moveToNext());
                mDrugCursor = c;
            }
            showProgess(false);
        }

        private Cursor mDiagCursor;
        private Cursor mDiagDetialCursor;
        private Cursor mDrugCursor;

        public void doSave() {
            VisitActivity act = (VisitActivity) getActivity();

            String delWhere = "visitno=" + act.getVisitNo();
            ContentResolver cr = act.getContentResolver();
            cr.delete(VisitDiag.CONTENT_URI, delWhere, null);
            cr.delete(VisitDrug.CONTENT_URI, delWhere, null);

            if (mDiagCursor != null && mDiagCursor.moveToFirst()) {

                ContentValues cv = new ContentValues();
                cv.put(VisitDiag.PCUCODE, act.getPcuCode());
                cv.put(VisitDiag.NO, act.getVisitNo());
                cv.put(VisitDiag.DOCTOR, act.getUser());
                cv.put(VisitDiag.CONTINUE, "0");
                cv.put(VisitDiag.CLINIC, "00000");
                cv.put(VisitDiag.DATEUPDATE, DateTime.getCurrentDateTime());
                cv.put(VisitDiag.TYPE, "01");
                cv.put(VisitDiag.CODE, mDiagCursor.getString(mDiagCursor
                        .getColumnIndex(DrugFormula.DIAGCODE)));
                cr.insert(VisitDiag.CONTENT_URI, cv);
            }
            if (mDiagDetialCursor != null && mDiagDetialCursor.moveToFirst()) {

                ContentValues cv = new ContentValues();
                cv.put(VisitDiag.PCUCODE, act.getPcuCode());
                cv.put(VisitDiag.NO, act.getVisitNo());
                cv.put(VisitDiag.DOCTOR, act.getUser());
                cv.put(VisitDiag.CONTINUE, "0");
                cv.put(VisitDiag.CLINIC, "00000");
                cv.put(VisitDiag.DATEUPDATE, DateTime.getCurrentDateTime());
                do {
                    cv.put(VisitDiag.TYPE,
                            mDiagDetialCursor.getString(mDiagDetialCursor
                                    .getColumnIndex(DrugFormula.Diagnosis.DXTYPE)));
                    cv.put(VisitDiag.CODE,
                            mDiagDetialCursor.getString(mDiagDetialCursor
                                    .getColumnIndex(DrugFormula.Diagnosis.DIAGCODE)));
                    cr.insert(VisitDiag.CONTENT_URI, cv);
                } while (mDiagDetialCursor.moveToNext());
            }

            if (mDrugCursor != null && mDrugCursor.moveToFirst()) {
                ContentValues cv = new ContentValues();
                cv.put(VisitDrug._PCUCODE, act.getPcuCode());
                cv.put(VisitDrug._VISITNO, act.getVisitNo());
                cv.put(VisitDrug._DATEUPDATE, DateTime.getCurrentDateTime());
                cv.put(VisitDrug.DOCTOR1, act.getUser());
                do {
                    String drugCode = mDrugCursor.getString(mDrugCursor
                            .getColumnIndex(DrugFormula.Drug.DRUG));
                    cv.put(VisitDrug.CODE, drugCode);
                    cv.put(VisitDrug.UNIT, mDrugCursor.getString(mDrugCursor
                            .getColumnIndex(DrugFormula.Drug.AMOUNT)));
                    Uri drugUri = Uri.withAppendedPath(Drug.CONTENT_URI,
                            drugCode);
                    Cursor c = cr.query(drugUri, new String[]{Drug.COST,
                            Drug.SELL}, null, null, Drug.DEFAULT_SORTING);
                    if (c.moveToFirst()) {
                        cv.put(VisitDrug.COST, c.getString(1));
                        cv.put(VisitDrug.REAL, c.getString(0));
                    }
                    cr.insert(VisitDrug.CONTENT_URI, cv);
                } while (mDrugCursor.moveToNext());
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {

        FragmentManager fm = getSupportFragmentManager();
        Fragment prev = fm.findFragmentByTag("detail");
        if (prev != null)
            fm.beginTransaction().remove(prev).commit();

        Bundle args = new Bundle();
        args.putLong(DrugFormula._ID, arg3);
        Detail f = (Detail) Fragment.instantiate(this, Detail.class.getName(),
                args);
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(android.R.id.list, f, "detail");
        ft.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    private static final int LOAD_DIAG = 0;
    private static final int LOAD_FORMULA_BY_DIAG = 1;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        CursorLoader cl;
        switch (id) {
            case LOAD_FORMULA_BY_DIAG:
                String dx = arg1.getString(Diagnosis.CODE);
                cl = new CursorLoader(this, DrugFormula.CONTENT_URI,
                        new String[]{DrugFormula._ID}, DrugFormula.DIAGCODE
                        + "=?", new String[]{dx}, DrugFormula.DEFAULT_SORTING);
                return cl;
            case LOAD_DIAG:
                cl = new CursorLoader(this, VisitDiag.CONTENT_URI, new String[]{VisitDiag.CODE},
                        "visitno=? AND dxtype='01'", new String[]{getVisitNo()},
                        VisitDiag.DEFAULT_SORTING);
                return cl;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> l, Cursor c) {
        switch (l.getId()) {
            case LOAD_DIAG:
                if (c.moveToFirst()) {
                    Bundle args = new Bundle();
                    args.putString(Diagnosis.CODE, c.getString(0));
                    getSupportLoaderManager().initLoader(LOAD_FORMULA_BY_DIAG,
                            args, this);

                }
                break;
            case LOAD_FORMULA_BY_DIAG:
                if (c.moveToFirst() && mSpinner != null) {
                    mSpinner.setSelectionById(c.getLong(0));
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }
}
