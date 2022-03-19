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
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;

import th.in.ffc.R;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.CodeProvider.HealthSuggest;
import th.in.ffc.provider.CodeProvider.Symtom;
import th.in.ffc.provider.CodeProvider.VitalSign;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.Visit;
import th.in.ffc.util.BMILevel;
import th.in.ffc.util.DateTime;
import th.in.ffc.widget.InstantAutoComplete;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.2
 * @since Family Folder Collector Plus
 */
public class VisitDefaultActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {
    public static final String[] PROJECTION = new String[]{Visit._ID,
            Visit.INCUP,

            Visit.WEIGHT, Visit.HEIGHT, Visit.ASS, Visit.WAIST, Visit.PRESSURE,
            Visit.PRESSURE_2, Visit.TEMPERATURE, Visit.PULSE,

            Visit.SYMPTOMS, Visit.VITAL, Visit.HEALTH_SUGGEST_1,};

    public static final String[] PERSON_PROJ = new String[]{
            Person.RIGHT_CODE, Person.RIGHT_NO, Person.RIGHT_HMAIN,
            Person.RIGHT_HSUB,};

    private static final int LOAD_MAXVISIT = 0;
    private static final int LOAD_LAST_MEASURE = 1;
    private static final int LOAD_PERSONRIGHT = 2;
    private static final int LOAD_SYMTOM = 3;
    private static final int LOAD_VITALCHECK = 4;
    private static final int LOAD_HEALTSUGGEST = 5;
    private static final int LOAD_SAVED_VISIT = 6;

    private String mVisitNo;
    private String mPid;

    private CheckBox mInCup;
    private CheckBox mServiceTime;
    private CheckBox mServiceType;

    private EditText mHeight;
    private EditText mWeight;
    private EditText mAss;
    private EditText mWaist;
    private EditText mPressure;
    private EditText mPressure2;
    private EditText mPulse;
    private EditText mTemp;

    private String mTimeStart;

    private InstantAutoComplete mSymtom;
    private InstantAutoComplete mVitalCheck;
    private InstantAutoComplete mHealthSuggest;

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setRetriveVisitData(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visit_default_activity);

        setSupportProgressBarIndeterminateVisibility(false);
        doSetupMeasurement(null);
        doSetupDiagnose(null);

        mTimeStart = DateTime.getCurrentTime();

        String action = getIntent().getAction();
        Uri data = getIntent().getData();

        if (action.equals(Action.INSERT)) {
            mPid = data.getLastPathSegment();
            if (savedInstanceState != null) {
                mVisitNo = savedInstanceState.getString(Visit.NO);
            }

            if (TextUtils.isEmpty(mVisitNo)) {
                getSupportLoaderManager().initLoader(LOAD_MAXVISIT, null, this);
                getSupportLoaderManager().initLoader(LOAD_LAST_MEASURE, null,
                        this);
            } else {
                getSupportActionBar().setSubtitle("Visit #" + mVisitNo);
            }

        } else if (action.equals(Action.EDIT)) {
            mVisitNo = data.getLastPathSegment();
            mPid = getIntent().getStringExtra(Person.PID);
            if (TextUtils.isEmpty(mPid))
                throw new IllegalArgumentException(
                        "need person.pid to edit visit");
            getSupportActionBar().setSubtitle("Visit #" + mVisitNo);
            getSupportLoaderManager().initLoader(LOAD_SAVED_VISIT, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(mVisitNo))
            outState.putString(Visit.NO, mVisitNo);
        outState.putString(Visit.TIME_START, mTimeStart);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
            mTimeStart = savedInstanceState.getString(Visit.TIME_START);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_activity, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVisitNo = null;
        mVitalCheck = null;
        mSymtom = null;
        mHealthSuggest = null;
        mWaist = null;
        mAss = null;
        mWeight = null;
        mHeight = null;
        mPulse = null;
        mPressure = null;
        mTimeStart = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                item.setEnabled(false);
                doSave();
                item.setEnabled(true);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean doSave() {
        if (TextUtils.isEmpty(mVisitNo))
            return false;
        EditTransaction et = beginTransaction();
        boolean weight = et.retrieveData(Visit.WEIGHT, mWeight, false, 0.0f, 300.0f,
                "weight, out of range");
        boolean height = et.retrieveData(Visit.HEIGHT, mHeight, false, 0.0f, 300.0f,
                "height, out of range");
        et.retrieveData(Visit.ASS, mAss, true, 0.0f, 300.0f,
                "Ass, out of range");
        et.retrieveData(Visit.WAIST, mWaist, true, 0.0f, 300.0f,
                "Waist out of range");
        et.retrieveData(Visit.PRESSURE, mPressure, true,
                "\\d{1,3}/{0,1}\\d{0,2}\\b", "invalid format");
        et.retrieveData(Visit.PRESSURE_2, mPressure2, true,
                "\\d{1,3}/{0,1}\\d{0,2}\\b", "invalid format");
        et.retrieveData(Visit.PULSE, mPulse, true, 0, 250, "Pulse out of rage");
        et.retrieveData(Visit.TEMPERATURE, mTemp, true, 30.0f, 45.0f,
                "Is he/her already dead");
        if (weight && height) {
            ContentValues cv = et.getContentValues();
            int bmi = BMILevel.calculateBMILevel(cv.getAsFloat(Visit.WEIGHT),
                    cv.getAsFloat(Visit.HEIGHT));
            cv.put(Visit.BMI, bmi);
        }
        et.retrieveData(Visit.SYMPTOMS, mSymtom, true, null, null);
        et.retrieveData(Visit.VITAL, mVitalCheck, true, null, null);
        et.retrieveData(Visit.HEALTH_SUGGEST_1, mHealthSuggest, true, null,
                null);

        ContentValues value = et.getContentValues();
        value.put(Visit.SERVICE_TYPE, mServiceType.isChecked() ? 1 : 2);
        value.put(Visit.TIME_SERIVICE, mServiceTime.isChecked() ? 1 : 2);
        value.put(Visit.INCUP, mInCup.isChecked() ? 1 : 2);

        if (getIntent().getAction().equals(Action.INSERT)) {
            if (et.canCommit()) {

                value.put(Visit.PCUCODE, getPcuCode());
                value.put(Visit.PCUCODE_PERSON, getPcuCode());
                value.put(Visit.PID, mPid);
                value.put(Visit.NO, mVisitNo);
                value.put(Visit.DATE, DateTime.getCurrentDate());
                value.put(Visit.UPDATE, DateTime.getCurrentDateTime());
                value.put(Visit.USERNAME, getUser());
                value.put(Visit.TIME_START, mTimeStart);
                getSupportLoaderManager().initLoader(LOAD_PERSONRIGHT, null,
                        this);
            }
        } else {
            value.put(Visit.UPDATE, DateTime.getCurrentDateTime());
            Uri visit = Uri.withAppendedPath(Visit.CONTENT_URI, mVisitNo);
            int update = et.commit(visit, null, null);
            if (update > 0) {
                setResult(RESULT_OK, new Intent(Action.EDIT, visit));
                finish();
            } else {
                setResult(RESULT_CANCELED);
                Log.d(TAG, "update failre");
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    public void doSetupDiagnose(Cursor c) {
        mSymtom = (InstantAutoComplete) findViewById(R.id.symtom);
        mVitalCheck = (InstantAutoComplete) findViewById(R.id.vitalcheck);
        mHealthSuggest = (InstantAutoComplete) findViewById(R.id.healthsuggest);

        if (c == null) {
            getSupportLoaderManager().initLoader(LOAD_SYMTOM, null, this);
            getSupportLoaderManager().initLoader(LOAD_VITALCHECK, null, this);
            getSupportLoaderManager().initLoader(LOAD_HEALTSUGGEST, null, this);
        } else {
            if (TextUtils.isEmpty(mSymtom.getText()))
                mSymtom.setText(c.getString(c.getColumnIndex(Visit.SYMPTOMS)));
            if (TextUtils.isEmpty(mVitalCheck.getText()))
                mVitalCheck.setText(c.getString(c.getColumnIndex(Visit.VITAL)));
            if (TextUtils.isEmpty(mHealthSuggest.getText()))
                mHealthSuggest.setText(c.getString(c
                        .getColumnIndex(Visit.HEALTH_SUGGEST_1)));
        }
    }

    public void doSetupMeasurement(Cursor c) {
        mServiceType = (CheckBox) findViewById(R.id.serivce);
        mServiceTime = (CheckBox) findViewById(R.id.time);
        mInCup = (CheckBox) findViewById(R.id.cup);
        mHeight = (EditText) findViewById(R.id.height);
        mWeight = (EditText) findViewById(R.id.weight);
        mAss = (EditText) findViewById(R.id.ass);
        mWaist = (EditText) findViewById(R.id.waist);
        mPressure = (EditText) findViewById(R.id.pressure1);
        mPressure2 = (EditText) findViewById(R.id.pressure2);
        mPulse = (EditText) findViewById(R.id.pulse);
        mTemp = (EditText) findViewById(R.id.temperature);

        if (c != null) {
            mInCup.setChecked(c.getInt(c.getColumnIndex(Visit.INCUP)) == 1 ? true
                    : false);

            if (TextUtils.isEmpty(mHeight.getText()))
                mHeight.setText(c.getString(c.getColumnIndex(Visit.HEIGHT)));
            if (TextUtils.isEmpty(mWeight.getText()))
                mWeight.setText(c.getString(c.getColumnIndex(Visit.WEIGHT)));
            if (TextUtils.isEmpty(mAss.getText()))
                mAss.setText(c.getString(c.getColumnIndex(Visit.ASS)));
            if (TextUtils.isEmpty(mWaist.getText()))
                mWaist.setText(c.getString(c.getColumnIndex(Visit.WAIST)));

            if (TextUtils.isEmpty(mPressure.getText()))
                mPressure
                        .setText(c.getString(c.getColumnIndex(Visit.PRESSURE)));
            if (TextUtils.isEmpty(mPressure2.getText()))
                mPressure2.setText(c.getString(c
                        .getColumnIndex(Visit.PRESSURE_2)));

            if (TextUtils.isEmpty(mPulse.getText()))
                mPulse.setText(c.getString(c.getColumnIndex(Visit.PULSE)));
            if (TextUtils.isEmpty(mTemp.getText()))
                mTemp.setText(c.getString(c.getColumnIndex(Visit.TEMPERATURE)));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

        switch (arg0) {
            case LOAD_MAXVISIT:
                this.setSupportProgressBarIndeterminateVisibility(true);
                return new CursorLoader(this, Visit.CONTENT_URI,
                        new String[]{Visit.MAX}, null, null,
                        "visit.visitno DESC");
            case LOAD_SAVED_VISIT:
                this.setSupportProgressBarIndeterminateVisibility(true);
            case LOAD_LAST_MEASURE:
                return new CursorLoader(this, Visit.CONTENT_URI, PROJECTION,
                        "visit.pid=" + mPid, null, "visit.visitno DESC");
            case LOAD_PERSONRIGHT:
                return new CursorLoader(this, Uri.withAppendedPath(
                        Person.CONTENT_URI, mPid), PERSON_PROJ, null, null,
                        Person.DEFAULT_SORTING);
            case LOAD_SYMTOM:
                return new CursorLoader(this, Symtom.CONTENT_URI, new String[]{
                        Symtom._ID, Symtom.NAME}, null, null,
                        Symtom.DEFAULT_SORTING);
            case LOAD_VITALCHECK:
                return new CursorLoader(this, VitalSign.CONTENT_URI,
                        new String[]{VitalSign.NAME}, null, null,
                        VitalSign.DEFAULT_SORTING);
            case LOAD_HEALTSUGGEST:
                return new CursorLoader(this, HealthSuggest.CONTENT_URI,
                        new String[]{HealthSuggest.SUGGEST}, null, null,
                        HealthSuggest.DEFAULT_SORTING);
            default:
                this.setSupportProgressBarIndeterminateVisibility(true);

        }
        return null;
    }

    int flag = SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {

        switch (loader.getId()) {
            case LOAD_MAXVISIT:
                this.setSupportProgressBarIndeterminateVisibility(false);
                if (c.moveToFirst()) {
                    if (getIntent().getAction().equals(Action.INSERT))
                        mVisitNo = "" + (c.getLong(0) + 1);
                    else
                        mVisitNo = "" + c.getLong(0);
                    getSupportActionBar().setSubtitle("Visit #" + mVisitNo);
                }
                break;
            case LOAD_LAST_MEASURE:
                if (c.moveToFirst())
                    doSetupMeasurement(c);
                break;
            case LOAD_SAVED_VISIT:
                this.setSupportProgressBarIndeterminateVisibility(false);
                if (c.moveToFirst()) {
                    doSetupMeasurement(c);
                    doSetupDiagnose(c);
                }
                break;
            case LOAD_SYMTOM:
                mSymtom.setAdapter(InstantAutoComplete.getAdapter(this, c,
                        c.getColumnIndex(Symtom.NAME)));
                break;
            case LOAD_VITALCHECK:
                mVitalCheck.setAdapter(InstantAutoComplete.getAdapter(this, c,
                        c.getColumnIndex(VitalSign.NAME)));
                break;
            case LOAD_HEALTSUGGEST:
                mHealthSuggest.setAdapter(InstantAutoComplete.getAdapter(this, c,
                        c.getColumnIndex(HealthSuggest.SUGGEST)));
                break;
            case LOAD_PERSONRIGHT:
                EditTransaction et = getOpenedTransaction();
                if (et.canCommit()) {
                    ContentValues value = et.getContentValues();
                    if (c.moveToFirst()) {
                        value.put(Visit.RIGHT_CODE,
                                c.getString(c.getColumnIndex(Person.RIGHT_CODE)));
                        value.put(Visit.RIGHT_NO,
                                c.getString(c.getColumnIndex(Person.RIGHT_NO)));
                        value.put(Visit.RIGHT_HMAIN,
                                c.getString(c.getColumnIndex(Person.RIGHT_HMAIN)));
                        value.put(Visit.RIGHT_HSUB,
                                c.getString(c.getColumnIndex(Person.RIGHT_HSUB)));
                    }
                    Uri uri = et.commit(Visit.CONTENT_URI);
                    if (uri != null) {
                        setResult(RESULT_OK, new Intent(Action.INSERT, uri));
                        this.finish();
                    }
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

}
