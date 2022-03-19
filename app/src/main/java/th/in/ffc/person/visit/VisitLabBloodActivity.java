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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import th.in.ffc.R;
import th.in.ffc.code.HospitalListDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.VisitLabBlood;
import th.in.ffc.util.DateTime;
import th.in.ffc.widget.ArrayFormatSpinner;
import th.in.ffc.widget.SearchableButton;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector plus
 */
public class VisitLabBloodActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    String action = Action.INSERT;

    private SearchableButton mHosService;
    private SearchableButton mHosLab;
    private CheckBox mClinic;
    private EditText mHct;
    private ArrayFormatSpinner mVdrl;
    private ArrayFormatSpinner mHiv;
    private ArrayFormatSpinner mHbag;
    private ArrayFormatSpinner mTalas1;
    private ArrayFormatSpinner mTalas2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.visit_labblood);

        initailizeView(savedInstanceState);

        if (savedInstanceState == null) {
            mHosService.setSelectionById(getPcuCode());
            mHosLab.setSelectionById(getPcuCode());
            getSupportLoaderManager().initLoader(0, null, this);
        }
        Uri personUri = Uri.withAppendedPath(Person.CONTENT_URI, getPid());
        Cursor c = getContentResolver().query(personUri, new String[]{Person.SEX},
                null, null, Person.DEFAULT_SORTING);
        if (c.moveToFirst())
            mClinic.setVisibility(View.GONE);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                doSave();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doSave() {

        EditTransaction et = beginTransaction();
        et.retrieveData(VisitLabBlood.HOS_SERVICE, mHosService, true, null, null);
        et.retrieveData(VisitLabBlood.HOS_LAB, mHosLab, true, null, null);
        et.retrieveData(VisitLabBlood.HCT, mHct, true, 15f, 80f, "15-80%");
        if (!et.isError()) {
            ContentValues cv = et.getContentValues();
            cv.put(VisitLabBlood.CLINIC, mClinic.isChecked() ? 1 : 2);
            cv.put(VisitLabBlood.HIV, mHiv.getSelectionId());
            cv.put(VisitLabBlood.HBsAg, mHbag.getSelectionId());
            cv.put(VisitLabBlood.VDRL, mVdrl.getSelectionId());
            cv.put(VisitLabBlood.TALAS_OF, mTalas1.getSelectionId());
            cv.put(VisitLabBlood.TALAS_DCIP, mTalas2.getSelectionId());
            cv.put(VisitLabBlood.DATEUPDATE, DateTime.getCurrentDateTime());

            if (action.equals(Action.INSERT)) {
                cv.put(VisitLabBlood._PCUCODE, getPcuCode());
                cv.put(VisitLabBlood._PCUCODEPERSON, getPcucodePerson());
                cv.put(VisitLabBlood._PID, getPid());
                cv.put(VisitLabBlood._VISITNO, getVisitNo());
                cv.put(VisitLabBlood.DATECHECK, DateTime.getCurrentDate());
                Uri up = et.forceCommit(VisitLabBlood.CONTENT_URI);
                Log.d(TAG, up.toString());
            } else if (action.equals(Action.EDIT)) {
                Uri uri = Uri.withAppendedPath(VisitLabBlood.CONTENT_URI, getVisitNo());
                int count = et.forceCommit(uri, null, null);
                Log.d(TAG, "update=" + count + " uri=" + uri.toString());
            }
            this.finish();
        }
    }

    public void initailizeView(Bundle savedInstanceState) {
        mHosService = (SearchableButton) findViewById(R.id.answer1);
        mHosService.setDialog(getSupportFragmentManager(),
                HospitalListDialog.class, "hosserve");
        mHosLab = (SearchableButton) findViewById(R.id.answer2);
        mHosLab.setDialog(getSupportFragmentManager(),
                HospitalListDialog.class, "hoslab");

        mClinic = (CheckBox) findViewById(R.id.clinic);
        mHct = (EditText) findViewById(R.id.answer3);

        mVdrl = (ArrayFormatSpinner) findViewById(R.id.answer4);
        mVdrl.setArray(R.array.VDRL);
        mHiv = (ArrayFormatSpinner) findViewById(R.id.answer5);
        mHiv.setArray(R.array.Negative_Positive_Not_Wait);
        mHbag = (ArrayFormatSpinner) findViewById(R.id.answer6);
        mHbag.setArray(R.array.Negative_Positive_Not_Wait);
        mTalas1 = (ArrayFormatSpinner) findViewById(R.id.answer7);
        mTalas1.setArray(R.array.Negative_Positive_Not_Wait);
        mTalas2 = (ArrayFormatSpinner) findViewById(R.id.answer8);
        mTalas2.setArray(R.array.Negative_Positive_Not_Wait);

    }

    public static final String[] PROJECTION = new String[]{VisitLabBlood._ID,
            VisitLabBlood.HOS_SERVICE, VisitLabBlood.HOS_LAB,
            VisitLabBlood.CLINIC, VisitLabBlood.HCT, VisitLabBlood.VDRL,
            VisitLabBlood.HIV, VisitLabBlood.HBsAg, VisitLabBlood.TALAS_OF,
            VisitLabBlood.TALAS_DCIP,};

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cl = new CursorLoader(this, VisitLabBlood.CONTENT_URI,
                PROJECTION, "visitno=" + getVisitNo(), null,
                VisitLabBlood.DEFAULT_SORTING);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cl, Cursor c) {
        if (c.moveToFirst()) {
            action = Action.EDIT;
            int clinic = c.getInt(c.getColumnIndex(VisitLabBlood.CLINIC));
            mClinic.setChecked(clinic == 1 ? true : false);

            mHosService.setSelectionById(c.getString(c
                    .getColumnIndex(VisitLabBlood.HOS_SERVICE)));
            mHosLab.setSelectionById(c.getString(c
                    .getColumnIndex(VisitLabBlood.HOS_LAB)));

            mHct.setText(c.getString(c.getColumnIndex(VisitLabBlood.HCT)));

            String vdrl = c.getString(c.getColumnIndex(VisitLabBlood.VDRL));
            mVdrl.setSelection(vdrl);
            String hiv = c.getString(c.getColumnIndex(VisitLabBlood.HIV));
            mHiv.setSelection(hiv);
            String hbag = c.getString(c.getColumnIndex(VisitLabBlood.HBsAg));
            mHbag.setSelection(hbag);
            String talas1 = c.getString(c
                    .getColumnIndex(VisitLabBlood.TALAS_OF));
            mTalas1.setSelection(talas1);
            String talas2 = c.getString(c
                    .getColumnIndex(VisitLabBlood.TALAS_DCIP));
            mTalas2.setSelection(talas2);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

}
