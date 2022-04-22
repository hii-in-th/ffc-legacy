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

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import th.in.ffc.R;
import th.in.ffc.code.HospitalListDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.PersonProvider.PregnancyColumns;
import th.in.ffc.provider.PersonProvider.Visit;
import th.in.ffc.provider.PersonProvider.VisitAnc;
import th.in.ffc.provider.PersonProvider.VisitAncPregnancy;
import th.in.ffc.util.BMILevel;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.widget.ArrayFormatSpinner;
import th.in.ffc.widget.SearchableButton;

import java.util.ArrayList;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector plus
 */
public class VisitAncActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    private static final int REQUEST_PREG_AUTO = 0;
    private static final int REQUEST_PREG_MANUAL = 1;
    private static final int REQUEST_EPI = 2;

    private static final int LOAD_PREGNANT = 0;
    private static final int LOAD_LAST_ANC = 1;
    private static final int LOAD_WEIGHT_HEIGHT = 2;
    String mAction = Action.INSERT;

    EditText mPregno;
    ImageButton mPregAdd, mEpiBtn;
    EditText mPregAge;
    ArrayFormatSpinner mAncNo;
    SearchableButton mServiceAt;
    ArrayFormatSpinner mDTanc;

    EditText mWeight;
    EditText mHeight;
    TextView mBmi;
    CheckBox mTooth;
    EditText mCaries;
    ArrayFormatSpinner mTarTar;
    ArrayFormatSpinner mGum;

    CheckBox mNipple;
    CheckBox mHeadAche;
    CheckBox mSickening;
    CheckBox mTyroid;
    CheckBox mLeucorrhea;
    CheckBox mEdema;
    CheckBox mUterus;
    CheckBox mCramp;
    CheckBox mUrinary;
    CheckBox mHeartAtk;
    ArrayFormatSpinner mFundus;
    ArrayFormatSpinner mSugar;
    ArrayFormatSpinner mAlbumin;

    CheckBox mBabyDance;
    ArrayFormatSpinner mBabyPostion;
    ArrayFormatSpinner mBabyPilot;
    ArrayFormatSpinner mBabyHear;
    EditText mBabyCount;
    ArrayFormatSpinner mBabyTalas;

    RadioGroup mSummarize;
    CheckBox mAppointChk;
    ThaiDatePicker mAppointDate;

    String mPreg;
    String mDtAnc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.visit_anc_activity);
        setSupportProgressBarIndeterminateVisibility(false);

        doInitailizeView(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportLoaderManager().initLoader(LOAD_PREGNANT, null, this);
            getSupportLoaderManager().initLoader(LOAD_LAST_ANC, null, this);
            getSupportLoaderManager().initLoader(LOAD_WEIGHT_HEIGHT, null, this);
        } else {
            mAction = savedInstanceState.getString("action");
            mPreg = savedInstanceState.getString("pregno");
            mDtAnc = savedInstanceState.getString("dtanc");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PREG_MANUAL:
                case REQUEST_PREG_AUTO:
                    mPreg = data.getExtras().getString(PregnancyColumns._PREGNO);
                    getSupportLoaderManager().restartLoader(LOAD_PREGNANT, null,
                            this);
                    Log.d(TAG, "update Ancno");
                    break;
                case REQUEST_EPI:
                    mDtAnc = "tt_at_epi";
                default:
                    break;
            }

        } else {
            switch (requestCode) {
                case REQUEST_PREG_AUTO:
                    this.finish();
                    break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("action", mAction);
        outState.putString("pregno", mPreg);
        outState.putString("dtanc", mDtAnc);
    }

    MenuItem mBloodMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.visit_anc, menu);
        mBloodMenu = menu.findItem(R.id.blood);
        // mBloodMenu.setEnabled(false);
        // mBloodMenu.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                doStartInsertPregnancyActivity();
                break;
            case R.id.save:
                doSave();
                break;
            case R.id.blood:
                Intent blood = new Intent(this, VisitLabBloodActivity.class);
                startVisitActivity(blood);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void doSave() {
        EditTransaction et = beginTransaction();
        et.retrieveData(VisitAnc.WEIGHT, mWeight, true, 0.0f, 300.0f,
                "0.0-300.0");
        et.retrieveData(VisitAnc.HEIGHT, mHeight, true, 0.0f, 250.0f,
                "0.0-250.0");
        et.retrieveData(VisitAnc.CARIES, mCaries, true, 0, 32, "0-32");
        et.retrieveData(VisitAnc.HEART_BABY_COUNT, mBabyCount, true, 0, 99,
                "0-99");
        et.retrieveData(VisitAnc.HOSPITAL_SERVICE, mServiceAt, false, null,
                null);
        if (mAppointChk.isChecked()) {
            Date current = Date.newInstance(DateTime.getCurrentDate());
            et.retrieveData(VisitAnc.DATE_APPOINT, mAppointDate, current, null,
                    "Can't Appoint to the past");
            // cv.put(VisitAnc.DATE_APPOINT, mAppointDate.getDate().toString());
        }

        if (!et.isError()) {
            ContentValues cv = et.getContentValues();

            if (!TextUtils.isEmpty(mDtAnc))
                cv.put(VisitAnc.DTANC, mDtAnc);
            else
                cv.put(VisitAnc.DTANC, mDTanc.getSelectionId());

            cv.put(VisitAnc._DATEUPDATE, DateTime.getCurrentDateTime());

            cv.put(VisitAnc.TOOTH_CHECK, mTooth.isChecked() ? 1 : 0);
            cv.put(VisitAnc.TARTAR, mTarTar.getSelectionId());
            cv.put(VisitAnc.GUM_FAIL, mGum.getSelectionId());

            cv.put(VisitAnc.BREAST_CHECK, mNipple.isChecked() ? 0 : 1);
            cv.put(VisitAnc.URINARY, mUrinary.isChecked() ? 0 : 1);
            cv.put(VisitAnc.HEADAHCE, mHeadAche.isChecked() ? 1 : 0);
            cv.put(VisitAnc.SICKENING, mSickening.isChecked() ? 1 : 0);
            cv.put(VisitAnc.TYROID, mTyroid.isChecked() ? 1 : 0);
            cv.put(VisitAnc.UTMUGO, mLeucorrhea.isChecked() ? 1 : 0);
            cv.put(VisitAnc.EDEMA, mEdema.isChecked() ? 1 : 0);
            cv.put(VisitAnc.UTBLOOD, mUterus.isChecked() ? 1 : 0);
            cv.put(VisitAnc.CRAMP, mCramp.isChecked() ? 1 : 0);
            cv.put(VisitAnc.HEART_ATTACK, mHeartAtk.isChecked() ? 1 : 0);
            cv.put(VisitAnc.FUNDUS, mFundus.getSelectionId());
            cv.put(VisitAnc.SUGAR, mSugar.getSelectionId());
            cv.put(VisitAnc.ALBUMIN, mAlbumin.getSelectionId());

            cv.put(VisitAnc.DANCE_BABY, mBabyDance.isChecked() ? 1 : 0);
            cv.put(VisitAnc.PILOT, mBabyPilot.getSelectionId());
            cv.put(VisitAnc.POSITION_BABY, mBabyPostion.getSelectionId());
            cv.put(VisitAnc.HEART_BABY, mBabyHear.getSelectionId());
            cv.put(VisitAnc.THALAS_BABY_CHECK, mBabyTalas.getSelectionId());

            cv.put(VisitAnc.ANCRES,
                    (mSummarize.getCheckedRadioButtonId() == R.id.answer1) ? 1
                            : 2);

            if (mAction.equals(Action.INSERT)) {
                cv.put(VisitAnc._PCUCODE, getPcuCode());
                cv.put(VisitAnc._PCUCODEPERSON, getPcucodePerson());
                cv.put(VisitAnc._PID, getPid());
                cv.put(VisitAnc._PREGNO, mPreg);
                cv.put(VisitAnc._VISITNO, getVisitNo());
                cv.put(VisitAnc.DATECHECK, DateTime.getCurrentDate());

                Uri insert = et.forceCommit(VisitAnc.CONTENT_URI);
                Log.d(TAG, insert.toString());
            } else if (mAction.equals(Action.EDIT)) {

                int update = et.forceCommit(VisitAnc.CONTENT_URI, "visitno=?",
                        new String[]{getVisitNo()});
                Log.d(TAG, "update=" + update);
            }

            this.finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cl;
        String[] projection;
        switch (arg0) {
            case LOAD_PREGNANT:
                projection = new String[]{VisitAncPregnancy._PREGNO,
                        VisitAncPregnancy.LMP, VisitAncPregnancy.EDC};
                cl = new CursorLoader(this, VisitAncPregnancy.CONTENT_URI,
                        projection, "pid=" + getPid(), null,
                        VisitAncPregnancy._PREGNO + " DESC");
                return cl;
            case LOAD_LAST_ANC:
                projection = new String[]{VisitAnc._VISITNO,
                        VisitAnc.HOSPITAL_SERVICE, VisitAnc.ANCRES,
                        VisitAnc.WEIGHT, VisitAnc.HEIGHT, VisitAnc.BMI,
                        VisitAnc.TOOTH_CHECK, VisitAnc.CARIES, VisitAnc.TARTAR,
                        VisitAnc.GUM_FAIL, VisitAnc.SUGAR, VisitAnc.ALBUMIN,
                        VisitAnc.BREAST_CHECK, VisitAnc.SICKENING, VisitAnc.CRAMP,
                        VisitAnc.EDEMA, VisitAnc.TYROID, VisitAnc.URINARY,
                        VisitAnc.UTMUGO, VisitAnc.UTBLOOD, VisitAnc.FUNDUS,
                        VisitAnc.HEART_ATTACK, VisitAnc.HEADAHCE,
                        VisitAnc.POSITION_BABY, VisitAnc.THALAS_BABY_CHECK,
                        VisitAnc.DANCE_BABY, VisitAnc.HEART_BABY,
                        VisitAnc.HEART_BABY_COUNT, VisitAnc.PILOT,
                        VisitAnc.DATE_APPOINT, VisitAnc.DTANC};
                cl = new CursorLoader(this, VisitAnc.CONTENT_URI, projection,
                        "pid=" + getPid(), null, VisitAnc._VISITNO + " DESC");
                return cl;
            case LOAD_WEIGHT_HEIGHT:
                projection = new String[]{Visit.WEIGHT, Visit.HEIGHT,};
                cl = new CursorLoader(this, Visit.CONTENT_URI, projection,
                        "visitno=" + getVisitNo(), null, Visit.DEFAULT_SORTING);
                return cl;
            default:
                throw new IllegalArgumentException("Load id not match");
        }
    }

    ArrayList<String> mPregList = new ArrayList<String>();

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {

        switch (loader.getId()) {
            case LOAD_PREGNANT:
                if (c.moveToFirst()) {
                    mPreg = c.getString(0);
                    mPregno.setText(mPreg);

                    c.moveToFirst();
                    String lmp = c.getString(1);
                    String edc = c.getString(2);
                    if (!TextUtils.isEmpty(lmp) && !TextUtils.isEmpty(edc))
                        doUpdateAncAge(lmp, edc);

                    c.moveToFirst();
                    do {
                        mPregList = new ArrayList<String>();
                        mPregList.add(c.getString(0));
                    } while (c.moveToNext());
                } else
                    doStartInsertPregnancyActivity();
                break;
            case LOAD_LAST_ANC:
                if (c.moveToFirst()) {
                    if (c.getString(0).equals(getVisitNo()))
                        mAction = Action.EDIT;

                    doInitailizeViewByCursor(c);
                }
                break;
            case LOAD_WEIGHT_HEIGHT:
                if (c.moveToFirst()) {
                    mWeight.setText(c.getString(0));
                    mHeight.setText(c.getString(0));
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

    public void doUpdateAncAge(String lmp, String edc) {
        Date dateLmp = Date.newInstance(lmp);
        Date edcDate = Date.newInstance(edc);
        Date current = Date.newInstance(DateTime.getCurrentDate());
        if (edcDate.compareTo(current) > 0 && current.compareTo(dateLmp) >= 0) {
            int dis_day = dateLmp.distanceTo(current);
            int dis_week = dis_day / 7;
            if (dis_week == 0)
                dis_week = 1;
            mPregAge.setText("" + dis_week);

            int ancno = 0;
            if (dis_week < 28) {
                ancno = 1;
            } else if (dis_week < 32) {
                ancno = 2;
            } else if (dis_week < 36) {
                ancno = 3;
            } else if (dis_week < 40) {
                ancno = 4;
            } else {
                ancno = 4;

                AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
                builder.setCancelable(false);
                builder.setTitle(R.string.ask_to_continue);
                builder.setMessage(R.string.hint_over_anc_age);
                builder.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        });
                builder.setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                VisitAncActivity.this.finish();
                            }
                        });
                builder.create().show();
            }
            mAncNo.setSelection("" + ancno);

        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
            builder.setMessage(R.string.ask_to_pregnanc);
            builder.setPositiveButton(R.string.yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doStartInsertPregnancyActivity();
                        }
                    });
            builder.setNegativeButton(R.string.no,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            VisitAncActivity.this.finish();
                        }
                    });
            builder.setCancelable(false);
            builder.create().show();
        }

    }

    public void doStartInsertPregnancyActivity() {
        Intent intent = new Intent(this, VisitAncPregnancyActivity.class);
        intent.setAction(Action.INSERT);
        intent.setData(Uri.withAppendedPath(Visit.CONTENT_URI, getVisitNo()));
        // intent.putExtra(PersonColumns._PID, getPid());
        startVisitActivityForResult(intent, REQUEST_PREG_MANUAL);
    }

    public void doInitailizeViewByCursor(Cursor c) {
        String hos_serv = c.getString(c
                .getColumnIndex(VisitAnc.HOSPITAL_SERVICE));
        if (!TextUtils.isEmpty(hos_serv))
            mServiceAt.setSelectionById(hos_serv);
        String dtAnc = c.getString(c.getColumnIndex(VisitAnc.DTANC));
        if (!TextUtils.isEmpty(dtAnc))
            mDTanc.setSelection(dtAnc);

        // Anc general vital check
        mWeight.setText(c.getString(c.getColumnIndex(VisitAnc.WEIGHT)));
        mHeight.setText(c.getString(c.getColumnIndex(VisitAnc.HEIGHT)));
        mBmi.setText(c.getString(c.getColumnIndex(VisitAnc.BMI)));
        mTooth.setChecked(c.getInt(c.getColumnIndex(VisitAnc.TOOTH_CHECK)) == 1 ? true
                : false);
        mCaries.setText(c.getString(c.getColumnIndex(VisitAnc.CARIES)));
        mTarTar.setSelection(c.getString(c.getColumnIndex(VisitAnc.TARTAR)));
        mGum.setSelection(c.getString(c.getColumnIndex(VisitAnc.GUM_FAIL)));

        // abnormal checkboxs
        mNipple.setChecked(c.getInt(c.getColumnIndex(VisitAnc.BREAST_CHECK)) == 0 ? true
                : false);
        mUrinary.setChecked(c.getInt(c.getColumnIndex(VisitAnc.URINARY)) == 0 ? true
                : false);
        mHeadAche
                .setChecked(c.getInt(c.getColumnIndex(VisitAnc.HEADAHCE)) == 1 ? true
                        : false);
        mSickening
                .setChecked(c.getInt(c.getColumnIndex(VisitAnc.SICKENING)) == 1 ? true
                        : false);
        mTyroid.setChecked(c.getInt(c.getColumnIndex(VisitAnc.TYROID)) == 1 ? true
                : false);
        mLeucorrhea
                .setChecked(c.getInt(c.getColumnIndex(VisitAnc.UTMUGO)) == 1 ? true
                        : false);
        mEdema.setChecked(c.getInt(c.getColumnIndex(VisitAnc.EDEMA)) == 1 ? true
                : false);
        mUterus.setChecked(c.getInt(c.getColumnIndex(VisitAnc.UTBLOOD)) == 1 ? true
                : false);
        mCramp.setChecked(c.getInt(c.getColumnIndex(VisitAnc.CRAMP)) == 1 ? true
                : false);
        mHeartAtk
                .setChecked(c.getInt(c.getColumnIndex(VisitAnc.HEART_ATTACK)) == 1 ? true
                        : false);
        // abnormal spinner
        mFundus.setSelection(c.getString(c.getColumnIndex(VisitAnc.FUNDUS)));
        mSugar.setSelection(c.getString(c.getColumnIndex(VisitAnc.SUGAR)));
        mAlbumin.setSelection(c.getString(c.getColumnIndex(VisitAnc.ALBUMIN)));

        // baby health section
        mBabyDance
                .setChecked(c.getInt(c.getColumnIndex(VisitAnc.DANCE_BABY)) == 1 ? true
                        : false);
        mBabyHear.setSelection(c.getString(c
                .getColumnIndex(VisitAnc.HEART_BABY)));
        mBabyCount.setText(c.getString(c
                .getColumnIndex(VisitAnc.HEART_BABY_COUNT)));
        mBabyPostion.setSelection(c.getString(c
                .getColumnIndex(VisitAnc.POSITION_BABY)));
        mBabyPilot.setSelection(c.getString(c.getColumnIndex(VisitAnc.PILOT)));
        mBabyTalas.setSelection(c.getString(c
                .getColumnIndex(VisitAnc.THALAS_BABY_CHECK)));

        boolean usaully = c.getInt(c.getColumnIndex(VisitAnc.ANCRES)) == 1 ? true
                : false;
        if (usaully) {
            RadioButton rUsually = (RadioButton) findViewById(R.id.answer1);
            rUsually.setChecked(true);
        } else {
            RadioButton rUnusually = (RadioButton) findViewById(R.id.answer2);
            rUnusually.setChecked(true);
        }

        if (mAction.equals(Action.EDIT)) {
            String appoint = c.getString(c
                    .getColumnIndex(VisitAnc.DATE_APPOINT));
            if (!TextUtils.isEmpty(appoint)) {
                mAppointChk.setChecked(true);
                Date da = Date.newInstance(appoint);
                mAppointDate.updateDate(da.year, da.month - 1, da.day);
            }

            String dtanc = c.getString(c.getColumnIndex(VisitAnc.DTANC));
            if (!TextUtils.isEmpty(dtanc) && !dtanc.equals("tt_in_scope")) {
                mDTanc.setSelection(0);
            }

        }

    }

    public void doInitailizeView(Bundle savedInstanceState) {
        mPregno = (EditText) findViewById(R.id.code);
        mPregAdd = (ImageButton) findViewById(R.id.edit);
        mPregAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mPreg)) {
                    Intent intent = new Intent(VisitAncActivity.this,
                            VisitAncPregnancyActivity.class);
                    intent.setAction(Action.EDIT);
                    intent.putExtra(PregnancyColumns._PREGNO, mPreg);
                    startVisitActivityForResult(intent, REQUEST_PREG_MANUAL);
                }
            }
        });
        mPregAge = (EditText) findViewById(R.id.age);
        mAncNo = (ArrayFormatSpinner) findViewById(R.id.anc);
        mAncNo.setArray(R.array.ancno);
        mAncNo.setEnabled(false);
        mServiceAt = (SearchableButton) findViewById(R.id.hospital);
        mServiceAt.setDialog(getSupportFragmentManager(),
                HospitalListDialog.class, "hos");
        mDTanc = (ArrayFormatSpinner) findViewById(R.id.epi);
        mDTanc.setArray(R.array.dtanc);
        mDTanc.setSelection(1);
        mDTanc.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                mEpiBtn.setEnabled(arg2 == 0 ? true : false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        mEpiBtn = (ImageButton) findViewById(R.id.answer3);
        mEpiBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mDTanc.getSelectedItemPosition() == 0) {
                    Intent epi = new Intent(VisitAncActivity.this,
                            VisitEpiActivity.class);
                    epi.setAction(Action.EPI_PREGNANCY);
                    epi.setData(Uri.withAppendedPath(Visit.CONTENT_URI,
                            getVisitNo()));
                    startVisitActivityForResult(epi, REQUEST_EPI);
                }
            }
        });

        mWeight = (EditText) findViewById(R.id.weight);
        mWeight.addTextChangedListener(bmiWatcher);
        mHeight = (EditText) findViewById(R.id.height);
        mHeight.addTextChangedListener(bmiWatcher);
        mBmi = (TextView) findViewById(R.id.bmi);
        mBmi.setVisibility(View.GONE);

        mTooth = (CheckBox) findViewById(R.id.tooth);
        mTooth.setChecked(true);
        mTooth.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                mCaries.setEnabled(isChecked);
                mTarTar.setEnabled(isChecked);
                mGum.setEnabled(isChecked);
            }
        });
        mCaries = (EditText) findViewById(R.id.caries);
        mTarTar = (ArrayFormatSpinner) findViewById(R.id.tartar);
        mTarTar.setArray(R.array.have_nothave);
        mGum = (ArrayFormatSpinner) findViewById(R.id.gum);
        mGum.setArray(R.array.have_nothave);

        mNipple = (CheckBox) findViewById(R.id.nipple);
        mHeadAche = (CheckBox) findViewById(R.id.head);
        mSickening = (CheckBox) findViewById(R.id.sickening);
        mTyroid = (CheckBox) findViewById(R.id.tyroid);
        mLeucorrhea = (CheckBox) findViewById(R.id.leucorrhea);
        mEdema = (CheckBox) findViewById(R.id.edema);
        mUterus = (CheckBox) findViewById(R.id.uterus);
        mCramp = (CheckBox) findViewById(R.id.cramp);
        mUrinary = (CheckBox) findViewById(R.id.urinary);
        mHeartAtk = (CheckBox) findViewById(R.id.heart);

        mFundus = (ArrayFormatSpinner) findViewById(R.id.fundus);
        mFundus.setArray(R.array.fundus);
        mSugar = (ArrayFormatSpinner) findViewById(R.id.sugar);
        mSugar.setArray(R.array.urinary_lvl);
        mAlbumin = (ArrayFormatSpinner) findViewById(R.id.albumin);
        mAlbumin.setArray(R.array.urinary_lvl);

        mBabyDance = (CheckBox) findViewById(R.id.dance);
        mBabyHear = (ArrayFormatSpinner) findViewById(R.id.hear);
        mBabyHear.setArray(R.array.baby_hear);
        mBabyPostion = (ArrayFormatSpinner) findViewById(R.id.position);
        mBabyPostion.setArray(R.array.baby_position);
        mBabyPilot = (ArrayFormatSpinner) findViewById(R.id.pilot);
        mBabyPilot.setArray(R.array.baby_pilot);
        mBabyCount = (EditText) findViewById(R.id.count);
        mBabyTalas = (ArrayFormatSpinner) findViewById(R.id.talas);
        mBabyTalas.setArray(R.array.baby_talas);

        mSummarize = (RadioGroup) findViewById(R.id.summarize);
        mAppointChk = (CheckBox) findViewById(R.id.check);
        mAppointChk.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                mAppointDate
                        .setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        mAppointDate = (ThaiDatePicker) findViewById(R.id.date);
        Date date = Date.newInstance(DateTime.getCurrentDateTime());
        mAppointDate.updateDate(date.year, date.month - 1, date.day);

        if (savedInstanceState == null) {
            mServiceAt.setSelectionById(getPcuCode());
            mTooth.setChecked(true);
            mBabyDance.setChecked(true);
            mBabyHear.setSelection("2");
            RadioButton usually = (RadioButton) findViewById(R.id.answer1);
            usually.setChecked(true);
        } else {
            mAppointDate.setVisibility(mAppointChk.isChecked() ? View.VISIBLE
                    : View.GONE);
        }
    }

    private void updateBmiLevel() {
        try {
            String height = mHeight.getText().toString();
            float h = Float.parseFloat(height);
            String weight = mWeight.getText().toString();
            float w = Float.parseFloat(weight);

            int bmi = BMILevel.calculatePregnancyBMI(w, h);
            mBmi.setText(BMILevel.MappingBMImeaning(this, bmi));

        } catch (NumberFormatException ne) {

        }

    }

    private TextWatcher bmiWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            updateBmiLevel();
        }
    };

}
