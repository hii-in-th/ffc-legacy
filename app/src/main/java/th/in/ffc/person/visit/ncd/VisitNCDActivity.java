package th.in.ffc.person.visit.ncd;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import th.in.ffc.R;
import th.in.ffc.app.FFCEditActivity;
import th.in.ffc.provider.PersonProvider.*;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.Log;
import th.in.ffc.widget.ArrayFormatSpinner;

public class VisitNCDActivity extends FFCEditActivity implements
        LoaderCallbacks<Cursor>, OnCheckedChangeListener, OnClickListener, OnItemSelectedListener {
    private static final int LOAD_VISIT = 100;
    private static final int LOAD_LAST = 101;

    RadioGroup ptype;
    RadioGroup pgroup;
    EditText weight;
    EditText height;
    EditText waist;
    EditText bmi;
    EditText bph1;
    EditText bpl1;
    EditText bph2;
    EditText bpl2;
    EditText bsl;
    ArrayFormatSpinner bstest;
    CheckBox q1;
    CheckBox q2;
    CheckBox q3;
    CheckBox q4;
    CheckBox q5;
    CheckBox q6;
    CheckBox qhtfam;
    CheckBox qarmpit;
    ArrayFormatSpinner smoke;
    ArrayFormatSpinner alchohol;
    RadioGroup servplace;
    CheckBox asService;
    // Symptom
    CheckBox hasSymptom;
    CheckBox eyeCheck;
    CheckBox urinalCheck;
    CheckBox footCheck;
    CheckBox heartCheck;
    CheckBox brainCheck;

    EditText eyeNote;
    EditText urinalNote;
    EditText footNote;
    EditText heartNote;
    EditText brainNote;
    Uri info;
    // holder
    LinearLayout symptomHolder;
    LinearLayout bslHolder;

    private int mAge;
    private int mSex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ncdscreen_activity);
        info = getIntent().getData();
        share_visitno = info.getLastPathSegment();
        mAge = getIntent().getIntExtra("AGE", 1);
        mSex = getIntent().getIntExtra("SEX", 1);
        initheader(share_pid);
        init();

        //For update latest patient info
        getSupportLoaderManager().restartLoader(LOAD_VISIT, null, this);
    }

    private void initheader(String pid) {
        System.out.println(pid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        Uri personUri = Uri.withAppendedPath(Person.CONTENT_URI, pid);

        String[] projection = new String[]{Person.PID, Person.CITIZEN_ID,
                Person.FULL_NAME};
        Cursor c1 = getContentResolver().query(personUri, projection, null,
                null, Person.DEFAULT_SORTING);
        if (c1.moveToFirst()) {
            if (!TextUtils.isEmpty(c1.getString(c1
                    .getColumnIndex(Person.FULL_NAME))))
                getSupportActionBar().setTitle(getString(R.string.ncd));
            getSupportActionBar().setSubtitle(
                    c1.getString(c1.getColumnIndex(Person.FULL_NAME)));
        }
    }

    private void init() {
        ptype = (RadioGroup) findViewById(R.id.patient_type);
        pgroup = (RadioGroup) findViewById(R.id.patient_group);
        weight = (EditText) findViewById(R.id.weight);
        weight.setClickable(false);
        weight.setKeyListener(null);

        height = (EditText) findViewById(R.id.height);
        height.setClickable(false);
        height.setKeyListener(null);

        waist = (EditText) findViewById(R.id.waist);
        bmi = (EditText) findViewById(R.id.bmi);
        bmi.setClickable(false);
        bmi.setKeyListener(null);

        bph1 = (EditText) findViewById(R.id.bp_h1);
        bpl1 = (EditText) findViewById(R.id.bp_l1);
        bph2 = (EditText) findViewById(R.id.bp_h2);
        bpl2 = (EditText) findViewById(R.id.bp_l2);
        bsl = (EditText) findViewById(R.id.bsl);
        bstest = (ArrayFormatSpinner) findViewById(R.id.bstest);
        q1 = (CheckBox) findViewById(R.id.q1);
        q2 = (CheckBox) findViewById(R.id.q2);
        q3 = (CheckBox) findViewById(R.id.q3);
        q4 = (CheckBox) findViewById(R.id.q4);
        q5 = (CheckBox) findViewById(R.id.q5);
        q6 = (CheckBox) findViewById(R.id.q6);
        qhtfam = (CheckBox) findViewById(R.id.qht);
        qarmpit = (CheckBox) findViewById(R.id.qarmpit);
        smoke = (ArrayFormatSpinner) findViewById(R.id.smoke);
        alchohol = (ArrayFormatSpinner) findViewById(R.id.alcohol);
        servplace = (RadioGroup) findViewById(R.id.servplace);

        symptomHolder = (LinearLayout) findViewById(R.id.symtom_holder);
        symptomHolder.setVisibility(View.GONE);
        bslHolder = (LinearLayout) findViewById(R.id.bsl_holder);

        hasSymptom = (CheckBox) findViewById(R.id.hasSymptom);
        eyeCheck = (CheckBox) findViewById(R.id.eyecheck);
        urinalCheck = (CheckBox) findViewById(R.id.urinalcheck);
        footCheck = (CheckBox) findViewById(R.id.footcheck);
        heartCheck = (CheckBox) findViewById(R.id.heartcheck);
        brainCheck = (CheckBox) findViewById(R.id.braincheck);

        eyeNote = (EditText) findViewById(R.id.eyeNote);
        urinalNote = (EditText) findViewById(R.id.urinalNote);
        footNote = (EditText) findViewById(R.id.footNote);
        heartNote = (EditText) findViewById(R.id.heartNote);
        brainNote = (EditText) findViewById(R.id.brainNote);

        asService = (CheckBox) findViewById(R.id.asServiceCheck);

        Log.d("NCD_FFC", "LOADER TRIGGER");

        //hide unwanted to change widget

        getSupportLoaderManager().restartLoader(LOAD_LAST, null, this);
    }

    private void initdata(Cursor c, Boolean isEdit) {
        // TODO Auto-generated method stub
        bstest.setArray(R.array.bstest);
        smoke.setArray(R.array.smoke);
        alchohol.setArray(R.array.drunk);

        bstest.setOnItemSelectedListener(this);
        hasSymptom.setOnCheckedChangeListener(this);
        eyeCheck.setOnCheckedChangeListener(this);
        urinalCheck.setOnCheckedChangeListener(this);
        footCheck.setOnCheckedChangeListener(this);
        heartCheck.setOnCheckedChangeListener(this);
        brainCheck.setOnCheckedChangeListener(this);

        // Insert
        if (!isEdit) {
            weight.setText(c.getString(c.getColumnIndex(Visit.WEIGHT)));
            height.setText(c.getString(c.getColumnIndex(Visit.HEIGHT)));
            waist.setText(c.getString(c.getColumnIndex(Visit.WAIST)));
            Double weight_val = c.getDouble(c.getColumnIndex(Visit.WEIGHT));
            Double height_val = c.getDouble(c.getColumnIndex(Visit.HEIGHT));
            height_val = height_val / 100;
            height_val = height_val * height_val;
            Double bmi_val = weight_val / height_val;
            //DecimalFormat df = new DecimalFormat("#.##");
            bmi.setText(Double.toString(bmi_val));
            String bp = c.getString(c.getColumnIndex(Visit.PRESSURE));
            String bp2 = c.getString(c.getColumnIndex(Visit.PRESSURE_2));
            Log.d("pressure1:2", bp + ":" + bp2);
            if (!TextUtils.isEmpty(bp)) {
                String[] bp_arr = bp.split("/");
                if(bp_arr.length>1) {
                    bph1.setText(bp_arr[0]);
                    bpl1.setText(bp_arr[1]);
                }
            }

            if (!TextUtils.isEmpty(bp2)) {
                String[] bp_arr2 = bp2.split("/");
                if(bp_arr2.length>1) {
                    bph2.setText(bp_arr2[0]);
                    bpl2.setText(bp_arr2[1]);
                }
            }
        } else { // Edit

            weight.setText(c.getString(c.getColumnIndex(NCDScreenJOIN.WEIGHT)));
            height.setText(c.getString(c.getColumnIndex(NCDScreenJOIN.HEIGHT)));
            waist.setText(c.getString(c.getColumnIndex(NCDScreenJOIN.WAIST)));
            bmi.setText(c.getString(c.getColumnIndex(NCDScreenJOIN.BMI)));
            bph1.setText(c.getString(c.getColumnIndex(NCDScreenJOIN.HBP_H1)));
            bpl1.setText(c.getString(c.getColumnIndex(NCDScreenJOIN.HBP_L1)));
            bph2.setText(c.getString(c.getColumnIndex(NCDScreenJOIN.HBP_H2)));
            bpl2.setText(c.getString(c.getColumnIndex(NCDScreenJOIN.HBP_L2)));
            bsl.setText(c.getString(c.getColumnIndex(NCDScreenJOIN.BSL)));

            ptype.check(c.getInt(c.getColumnIndex(NCDScreenJOIN.CHRONIC_FLAG)) == 0 ? R.id.rad1 : R.id.rad2);
            pgroup.check(getPatientGroup(c));
            servplace.check(c.getInt(c.getColumnIndex(NCDScreenJOIN.SERVICEPLACE)) == 1 ? R.id.rad1 : R.id.rad2);

            bstest.setSelection(c.getString(c.getColumnIndex(NCDScreenJOIN.BSTEST)));
            smoke.setSelection(c.getString(c.getColumnIndex(NCDScreenJOIN.SMOKE)));
            alchohol.setSelection(c.getString(c.getColumnIndex(NCDScreenJOIN.ALCOHOL)));

            q1.setChecked(c.getInt(c.getColumnIndex(NCDScreenJOIN.SCREEN_Q1)) == 1 ? true : false);
            q2.setChecked(c.getInt(c.getColumnIndex(NCDScreenJOIN.SCREEN_Q2)) == 1 ? true : false);
            q3.setChecked(c.getInt(c.getColumnIndex(NCDScreenJOIN.SCREEN_Q3)) == 1 ? true : false);
            q4.setChecked(c.getInt(c.getColumnIndex(NCDScreenJOIN.SCREEN_Q4)) == 1 ? true : false);
            q5.setChecked(c.getInt(c.getColumnIndex(NCDScreenJOIN.SCREEN_Q5)) == 1 ? true : false);
            q6.setChecked(c.getInt(c.getColumnIndex(NCDScreenJOIN.SCREEN_Q6)) == 1 ? true : false);
            qarmpit.setChecked(c.getInt(c.getColumnIndex(NCDScreenJOIN.BLACKARMPIT)) == 2 ? true : false);
            qhtfam.setChecked(c.getInt(c.getColumnIndex(NCDScreenJOIN.HTFAM)) == 1 ? true : false);
            hasSymptom.setChecked(c.getInt(c.getColumnIndex(NCDScreenJOIN.HAS_SYMPTOM)) == 1 ? true : false);
            if (hasSymptom.isChecked())
                doGetNCDHistDetail();

        }
    }

    private void doGetNCDHistDetail() {
        Cursor c = getContentResolver().query(NCDPersonNCDHistDetail.CONTENT_URI,
                new String[]{NCDPersonNCDHistDetail.ORGAN, NCDPersonNCDHistDetail.REMARK},
                NCDPersonNCDHistDetail.PID + "=" + share_pid, null,
                NCDPersonNCDHistDetail.PID);
        if (c.moveToFirst()) {
            do {
                if (c.getString(0).equals("01")) {
                    eyeCheck.setChecked(true);
                    eyeNote.setText(c.getString(1));
                    continue;
                }
                if (c.getString(0).equals("02")) {
                    urinalCheck.setChecked(true);
                    urinalNote.setText(c.getString(1));
                    continue;
                }
                if (c.getString(0).equals("03")) {
                    footCheck.setChecked(true);
                    footNote.setText(c.getString(1));
                    continue;
                }
                if (c.getString(0).equals("04")) {
                    heartCheck.setChecked(true);
                    heartNote.setText(c.getString(1));
                    continue;
                }
                if (c.getString(0).equals("05") || c.getString(0).equals("99")) {
                    brainCheck.setChecked(true);
                    brainNote.setText(c.getString(1));
                    continue;
                }


            } while (c.moveToNext());
        }
    }

    private int getPatientGroup(Cursor c) {
        int dm = c.getInt(c.getColumnIndex(NCDScreenJOIN.DM_FLAG));
        int hbp = c.getInt(c.getColumnIndex(NCDScreenJOIN.HBP_FLAG));
        if (dm == 1 && hbp == 0)
            return R.id.rad1;
        if (dm == 0 && hbp == 1)
            return R.id.rad2;
        if (dm == 1 && hbp == 1)
            return R.id.rad3;
        return R.id.rad1;
    }

    public static final String[] NCD_PROJ = new String[]{
            NCDScreenJOIN.WEIGHT,
            NCDScreenJOIN.HEIGHT,
            NCDScreenJOIN.WAIST,
            NCDScreenJOIN.HBP_H1,
            NCDScreenJOIN.HBP_L1,
            NCDScreenJOIN.HBP_H2,
            NCDScreenJOIN.HBP_L2,
            NCDScreenJOIN.SCREEN_Q1,
            NCDScreenJOIN.SCREEN_Q2,
            NCDScreenJOIN.SCREEN_Q3,
            NCDScreenJOIN.SCREEN_Q4,
            NCDScreenJOIN.SCREEN_Q5,
            NCDScreenJOIN.SCREEN_Q6,
            NCDScreenJOIN.BLACKARMPIT,
            NCDScreenJOIN.BSL,
            NCDScreenJOIN.BMI,
            NCDScreenJOIN.SMOKE,
            NCDScreenJOIN.ALCOHOL,
            NCDScreenJOIN.HTFAM,
            NCDScreenJOIN.BSTEST,
            NCDScreenJOIN.SERVICEPLACE,
            NCDScreenJOIN.HAS_SYMPTOM,
            NCDScreenJOIN.CHRONIC_FLAG,
            NCDScreenJOIN.DM_FLAG,
            NCDScreenJOIN.HBP_FLAG
    };

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub
        CursorLoader cl = null;
        switch (arg0) {
            case LOAD_LAST:
                Log.d("NCD_FFC", "LOAD LAST");
                cl = new CursorLoader(this, NCDScreenJOIN.CONTENT_URI,
                        NCD_PROJ, NCDScreenJOIN.PCUCODE + "=? AND " + NCDScreenJOIN.PID + "=? AND " + NCDScreenJOIN.NO + "=?",
                        new String[]{share_pcucode, share_pid, "1"}, NCDScreenJOIN.VISITNO);
                return cl;
            case LOAD_VISIT:
                Log.d("NCD_FFC", "LOAD VISIT");
                cl = new CursorLoader(this, Visit.CONTENT_URI, new String[]{
                        Visit.WEIGHT, Visit.HEIGHT, Visit.WAIST, Visit.BMI,
                        Visit.PRESSURE, Visit.PRESSURE_2}, Visit.NO + "="
                        + share_visitno, null, Visit.NO);
                return cl;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        // TODO Auto-generated method stub
        Log.d("NCD_FFC", "LOADER FINISH");
        switch (arg0.getId()) {
            case LOAD_LAST:
                if (c.moveToFirst()) {
                    Log.d("NCD_FFC", "Found last NCDScreen for this person");
                    initdata(c, true);
                } else {
                    Log.d("NCD_FFC", "Not Found Insert new NCD");
                    getSupportLoaderManager().restartLoader(LOAD_VISIT, null,
                            VisitNCDActivity.this);
                }
                break;
            case LOAD_VISIT:
                if (c.moveToFirst()) {
                    initdata(c, false);
                }
                break;
            default:
                Log.d("NCD_FFC", "SHIT");
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void Delete() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void Edit() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void Update() {
        // TODO Auto-generated method stub
        ContentValues cv1 = new ContentValues();
        ContentValues cv2 = new ContentValues();
        ContentValues cv3 = new ContentValues();
        ContentValues cv4 = new ContentValues();
        ContentValues cv5 = new ContentValues();
        ContentValues cv6 = new ContentValues();

        if (checkRequiredField()) {
            updateNCDPersonInfo(cv1); // NCDPersonNCD
            updateNCDPersonType(cv2);// NCDPersonNCD
            updateNCDPersonPatient(cv3);// NCDPersonNCDHist
            if (hasSymptom.isChecked())
                updateNCDPersonSymptom(cv4, true);// NCDPersonNCDHistDetail
            else
                updateNCDPersonSymptom(cv4, false);
            updateNCDPersonScreen(cv5);// NCDPersonNCDScreen
            if (asService.isChecked())
                updateDiag(cv6);
            finish();
        }

    }

    private void updateDiag(ContentValues cv) {
        // TODO Auto-generated method stub
        cv.put(VisitDiag.PCUCODE, share_pcucode);
        cv.put(VisitDiag.NO, share_visitno);
        cv.put(VisitDiag.CONTINUE, "0");
        cv.put(VisitDiag.CLINIC, "00000");
        cv.put(VisitDiag.DATEUPDATE, DateTime.getCurrentDateTime());
        cv.put(VisitDiag.TYPE, "02");

        ContentValues r030 = new ContentValues(cv);
        r030.put(VisitDiag.CODE, "R03.0");

        ContentValues r739 = new ContentValues(cv);
        r739.put(VisitDiag.CODE, "R73.9");

        ContentValues z131 = new ContentValues(cv);
        z131.put(VisitDiag.CODE, "Z13.1");
        z131.put(VisitDiag.TYPE, "01");

        ContentValues z138 = new ContentValues(cv);
        z138.put(VisitDiag.CODE, "Z13.8");
        z138.put(VisitDiag.TYPE, "01");

        //remove previous diag
        int count = getContentResolver().delete(VisitDiag.CONTENT_URI,
                VisitDiag.NO + "=? AND ( " + VisitDiag.CODE + "=? OR " + VisitDiag.CODE + "=? OR " + VisitDiag.CODE + "=? OR " + VisitDiag.CODE + "=? )",
                new String[]{share_visitno, "Z13.1", "Z13.8", "R03.0", "R73.9"});

        switch (pgroup.getCheckedRadioButtonId()) {
            case R.id.rad1:
                getContentResolver().insert(VisitDiag.CONTENT_URI, z131);
                getContentResolver().insert(VisitDiag.CONTENT_URI, r739);
                break;
            case R.id.rad2:
                getContentResolver().insert(VisitDiag.CONTENT_URI, z138);
                getContentResolver().insert(VisitDiag.CONTENT_URI, r030);
                break;
            case R.id.rad3:
                getContentResolver().insert(VisitDiag.CONTENT_URI, z131);
                getContentResolver().insert(VisitDiag.CONTENT_URI, r739);
                getContentResolver().insert(VisitDiag.CONTENT_URI, r030);
                break;
            default:
                break;
        }
    }

    private boolean checkRequiredField() {
        if (!TextUtils.isEmpty(bph1.getText()) && !TextUtils.isEmpty(bpl1.getText())) {
            Double h1 = Double.parseDouble(bph1.getText().toString());
            Double l1 = Double.parseDouble(bpl1.getText().toString());
            if ((h1 > 119 || l1 > 79) && TextUtils.isEmpty(bph2.getText()) && TextUtils.isEmpty(bpl2.getText())) {
                alert(getString(R.string.ncd_err1));
                return false;
            }
        }
        if (!bstest.getSelectionId().equals("0")) {
            if (TextUtils.isEmpty(bsl.getText())) {
                alert(getString(R.string.ncd_err2));
                return false;
            }
        }
        if (hasSymptom.isChecked()) {
            if (eyeCheck.isChecked() || urinalCheck.isChecked() || footCheck.isChecked() || heartCheck.isChecked() || brainCheck.isChecked()) {
                return checkSymptomInput();
            } else {
                alert(getString(R.string.ncd_err3));
                return false;
            }
        }
        return true;
    }

    private boolean checkSymptomInput() {
        // TODO Auto-generated method stub
        String errMsg = "NONE";
        if (eyeCheck.isChecked()) {
            if (TextUtils.isEmpty(eyeNote.getText()))
                errMsg += getResources().getString(R.string.ncd_err_org1);
        }
        if (urinalCheck.isChecked()) {
            if (TextUtils.isEmpty(urinalNote.getText()))
                errMsg += getResources().getString(R.string.ncd_err_org2);
        }
        if (footCheck.isChecked()) {
            if (TextUtils.isEmpty(footNote.getText()))
                errMsg += getResources().getString(R.string.ncd_err_org3);
        }
        if (heartCheck.isChecked()) {
            if (TextUtils.isEmpty(heartNote.getText()))
                errMsg += getResources().getString(R.string.ncd_err_org4);
        }
        if (brainCheck.isChecked()) {
            if (TextUtils.isEmpty(brainNote.getText()))
                errMsg += getResources().getString(R.string.ncd_err_org5);
        }
        if (errMsg.equals("NONE")) {
            return true;
        } else {
            alert(errMsg.replace("NONE", ""));
            return false;
        }
    }

    private void alert(String title, String msg) {
        AlertDialog.Builder d = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
        d.setTitle(title);
        d.setMessage(msg);
        d.setCancelable(true);
        d.show();
    }

    private void alert(String msg) {
        alert("Warning", msg);
    }

    private void updateNCDPersonScreen(ContentValues cv) {

        Cursor c = getContentResolver().query(NCDPersonNCDScreen.CONTENT_URI,
                new String[]{NCDPersonNCDScreen.VISITNO},
                NCDScreenJOIN.PCUCODE + "=? AND " + NCDScreenJOIN.PID + "=? AND " + NCDScreenJOIN.NO + "=?", new String[]{share_pcucode, share_pid, "1"},
                NCDPersonNCDScreen.VISITNO);

        cv.put(NCDPersonNCDScreen.VISITNO, share_visitno);
        cv.put(NCDPersonNCDScreen.AGE, mAge);
        cv.put(NCDPersonNCDScreen.SCREEN_DATE, DateTime.getCurrentDate());
        cv.put(NCDPersonNCDScreen.HEIGHT,
                !TextUtils.isEmpty(height.getText()) ? height.getText()
                        .toString() : "0");
        cv.put(NCDPersonNCDScreen.WEIGHT,
                !TextUtils.isEmpty(weight.getText()) ? weight.getText()
                        .toString() : "0");
        cv.put(NCDPersonNCDScreen.WAIST,
                !TextUtils.isEmpty(waist.getText()) ? waist.getText()
                        .toString() : "0");
        cv.put(NCDPersonNCDScreen.HBP_H1,
                !TextUtils.isEmpty(bph1.getText()) ? bph1.getText().toString()
                        : "0");
        cv.put(NCDPersonNCDScreen.HBP_L1,
                !TextUtils.isEmpty(bpl1.getText()) ? bpl1.getText().toString()
                        : "0");
        cv.put(NCDPersonNCDScreen.HBP_H2,
                !TextUtils.isEmpty(bph2.getText()) ? bph2.getText().toString()
                        : "0");
        cv.put(NCDPersonNCDScreen.HBP_L2,
                !TextUtils.isEmpty(bpl2.getText()) ? bpl2.getText().toString()
                        : "0");
        cv.put(NCDPersonNCDScreen.SCREEN_Q1, q1.isChecked() ? "1" : "0");
        cv.put(NCDPersonNCDScreen.SCREEN_Q2, q2.isChecked() ? "1" : "0");
        cv.put(NCDPersonNCDScreen.SCREEN_Q3, q3.isChecked() ? "1" : "0");
        cv.put(NCDPersonNCDScreen.SCREEN_Q4, q4.isChecked() ? "1" : "0");
        cv.put(NCDPersonNCDScreen.SCREEN_Q5, q5.isChecked() ? "1" : "0");
        cv.put(NCDPersonNCDScreen.SCREEN_Q6, q6.isChecked() ? "1" : "0");
        cv.put(NCDPersonNCDScreen.BLACKARMPIT, qarmpit.isChecked() ? "2" : "1");
        cv.put(NCDPersonNCDScreen.BMI, !TextUtils.isEmpty(bmi.getText()) ? bmi
                .getText().toString() : "0");
        cv.put(NCDPersonNCDScreen.RESULT_DM, getDMResult()); // 1,2,3
        cv.put(NCDPersonNCDScreen.RESULT_HBP, getHBPResult());// 1,2,3
        cv.put(NCDPersonNCDScreen.RESULT_WAIST, getWaistResult());// 1,2
        cv.put(NCDPersonNCDScreen.RESULT_OBESITY, getObesityResult());// 1,2,3
        cv.put(NCDPersonNCDScreen.SMOKE, smoke.getSelectionId());
        cv.put(NCDPersonNCDScreen.ALCOHOL, alchohol.getSelectionId());
        cv.put(NCDPersonNCDScreen.HTFAM, qhtfam.isChecked() ? "1" : "0");
        if (!bstest.getSelectionId().equals("0")) {
            cv.put(NCDPersonNCDScreen.BSTEST, bstest.getSelectionId());
            cv.put(NCDPersonNCDScreen.BSL, !TextUtils.isEmpty(bsl.getText()) ? bsl
                    .getText().toString() : "0");
        }
        cv.put(NCDPersonNCDScreen.SERVICEPLACE,
                servplace.getCheckedRadioButtonId() == R.id.rad1 ? "1" : "2");
        cv.put(NCDPersonNCDScreen.DATEUPDATE2, DateTime.getCurrentDateTime());
        cv.put(NCDPersonNCDScreen.DATEUPDATE, DateTime.getCurrentDate());
        cv.put(NCDPersonNCDScreen.USERNAME, getUser());

        if (c.moveToFirst()) {
            Log.d("NCD_FFC",
                    "THIS PERSON ALREADY HAVE INFORMATION IN NCDPersonNCDScreen");
            Log.d("NCD_FFC", "UPDATE NCDPersonNCDScreen INSTEAD");
            getContentResolver().update(NCDPersonNCDScreen.CONTENT_URI, cv,
                    NCDScreenJOIN.PCUCODE + "=? AND " + NCDScreenJOIN.PID + "=? AND " + NCDScreenJOIN.NO + "=?",
                    new String[]{share_pcucode, share_pid, "1"});

            return;
        } else {
            cv.put(NCDPersonNCDScreen.PCUCODE, share_pcucode);
            cv.put(NCDPersonNCDScreen.PID, share_pid);
            cv.put(NCDPersonNCDScreen.NO, "1");// Still had no idea
            getContentResolver().insert(NCDPersonNCDScreen.CONTENT_URI, cv);
            return;
        }
    }

    private String getDMResult() {
        if (!TextUtils.isEmpty(bsl.getText())) {
            Double bsl_val = Double.parseDouble(bsl.getText().toString());
            if (bsl_val < 100)
                return "1";
            else if (bsl_val >= 100 || bsl_val < 125)
                return "2";
            else
                return "3";
        } else
            return "1";
    }

    private String getHBPResult() {
        if (!TextUtils.isEmpty(bph1.getText()) && !TextUtils.isEmpty(bpl1.getText())) {
            Double bph1_val = Double.parseDouble(bph1.getText().toString());
            Double bpl1_val = Double.parseDouble(bpl1.getText().toString());

            if (bph1_val >= 100 && bph1_val <= 129 && bpl1_val >= 65 && bpl1_val <= 84)
                return "1";
            else if (bph1_val >= 130 && bph1_val <= 139 && bpl1_val >= 85 && bpl1_val <= 89)
                return "2";
            else
                return "3";
        } else
            return "1";
    }

    private String getWaistResult() {
        if (!TextUtils.isEmpty(waist.getText())) {
            Double waist_val = Double.parseDouble(waist.getText().toString());
            if (getSex() == 1) {
                if (waist_val > 90)
                    return "2";
                else
                    return "1";
            } else {
                if (waist_val > 80)
                    return "2";
                else
                    return "1";
            }
        } else
            return "1";

    }

    private int getSex() {
        return mSex;
    }

    private String getObesityResult() {
        if (TextUtils.isEmpty(bmi.getText())) {
            Double bmi_val = Double.parseDouble(bmi.getText().toString());
            if (bmi_val < 25)
                return "1";
            else
                return "2";
        } else
            return "1";
    }

    private void updateNCDPersonSymptom(ContentValues cv, boolean flags) {
        Log.d("NCD_FFC", "REMOVE PREVIOUS NCDPersonNCDHistDetail");
        getContentResolver().delete(NCDPersonNCDHistDetail.CONTENT_URI,
                NCDPersonNCDHistDetail.PID + "=" + share_pid, null);
        if (flags) {
            // INSERT CHECKED ORGAN
            cv.put(NCDPersonNCDHistDetail.PCUCODE, share_pcucode);
            cv.put(NCDPersonNCDHistDetail.PID, share_pid);
            cv.put(NCDPersonNCDHistDetail.DATEUPDATE, DateTime.getCurrentDate());
            cv.put(NCDPersonNCDHistDetail.USERNAME, getUser());

            if (eyeCheck.isChecked()) {
                cv.put(NCDPersonNCDHistDetail.ORGAN, "01");
                cv.put(NCDPersonNCDHistDetail.REMARK, !TextUtils.isEmpty(eyeNote
                        .getText()) ? eyeNote.getText().toString() : "");
                getContentResolver().insert(NCDPersonNCDHistDetail.CONTENT_URI, cv);
            }
            if (urinalCheck.isChecked()) {
                cv.put(NCDPersonNCDHistDetail.ORGAN, "02");
                cv.put(NCDPersonNCDHistDetail.REMARK, !TextUtils.isEmpty(urinalNote
                        .getText()) ? urinalNote.getText().toString() : "");
                getContentResolver().insert(NCDPersonNCDHistDetail.CONTENT_URI, cv);
            }
            if (footCheck.isChecked()) {
                cv.put(NCDPersonNCDHistDetail.ORGAN, "03");
                cv.put(NCDPersonNCDHistDetail.REMARK, !TextUtils.isEmpty(footNote
                        .getText()) ? footNote.getText().toString() : "");
                getContentResolver().insert(NCDPersonNCDHistDetail.CONTENT_URI, cv);
            }
            if (heartCheck.isChecked()) {
                cv.put(NCDPersonNCDHistDetail.ORGAN, "04");
                cv.put(NCDPersonNCDHistDetail.REMARK, !TextUtils.isEmpty(heartNote
                        .getText()) ? heartNote.getText().toString() : "");
                getContentResolver().insert(NCDPersonNCDHistDetail.CONTENT_URI, cv);
            }
            if (brainCheck.isChecked()) {
                cv.put(NCDPersonNCDHistDetail.ORGAN,
                        pgroup.getCheckedRadioButtonId() == R.id.rad1 ? "99" : "05");
                cv.put(NCDPersonNCDHistDetail.REMARK, !TextUtils.isEmpty(brainNote
                        .getText()) ? brainNote.getText().toString() : "");
                getContentResolver().insert(NCDPersonNCDHistDetail.CONTENT_URI, cv);
            }
        }
        return;
    }

    private void updateNCDPersonPatient(ContentValues cv) {
        switch (pgroup.getCheckedRadioButtonId()) {
            case R.id.rad1:
                cv.put(NCDPersonNCDHist.DM_FLAG, "1");
                cv.put(NCDPersonNCDHist.HBP_FLAG, "0");
                break;
            case R.id.rad2:
                cv.put(NCDPersonNCDHist.DM_FLAG, "0");
                cv.put(NCDPersonNCDHist.HBP_FLAG, "1");
                break;
            case R.id.rad3:
                cv.put(NCDPersonNCDHist.DM_FLAG, "1");
                cv.put(NCDPersonNCDHist.HBP_FLAG, "1");
                break;
            default:
                break;
        }

        cv.put(NCDPersonNCDHist.HAS_SYMPTOM, hasSymptom.isChecked() ? "1" : "0");
        cv.put(NCDPersonNCDHist.DATEUPDATE, DateTime.getCurrentDate());
        cv.put(NCDPersonNCDHist.USERNAME, getUser());

        Cursor c = getContentResolver().query(NCDPersonNCDHist.CONTENT_URI,
                new String[]{NCDPersonNCDHist.PID},
                NCDPersonNCDHist.PID + "=" + share_pid, null,
                NCDPersonNCDHist.PID);

        if (c.moveToFirst()) {
            Log.d("NCD_FFC",
                    "THIS PERSON ALREADY HAVE INFORMATION IN NCDPersonNCDHist");
            Log.d("NCD_FFC", "UPDATE NCDPersonNCDHist INSTEAD");
            getContentResolver().update(NCDPersonNCDHist.CONTENT_URI, cv,
                    NCDPersonNCDHist.PID + "=" + share_pid, null);
            return;
        } else {
            cv.put(NCDPersonNCDHist.PCUCODE, share_pcucode);
            cv.put(NCDPersonNCDHist.PID, share_pid);
            getContentResolver().insert(NCDPersonNCDHist.CONTENT_URI, cv);
            return;
        }
    }

    private void updateNCDPersonType(ContentValues cv) {

        cv.put(NCDPersonNCD.CHRONIC_FLAG,
                ptype.getCheckedRadioButtonId() == R.id.rad1 ? "0" : "1");
        cv.put(NCDPersonNCD.DATEUPDATE, DateTime.getCurrentDate());
        cv.put(NCDPersonNCD.USERNAME, getUser());

        Cursor c = getContentResolver().query(NCDPersonNCD.CONTENT_URI,
                new String[]{NCDPersonNCD.PID},
                NCDPersonNCD.PID + "=" + share_pid, null, NCDPersonNCD.PID);

        if (c.moveToFirst()) {
            Log.d("NCD_FFC",
                    "THIS PERSON ALREADY HAVE INFORMATION IN NCDPersonNCD");
            Log.d("NCD_FFC", "UPDATE NCDPersonNCD INSTEAD");
            getContentResolver().update(NCDPersonNCD.CONTENT_URI, cv,
                    NCDPersonNCD.PID + "=" + share_pid, null);

            return;
        } else {
            cv.put(NCDPersonNCD.PCUCODE, share_pcucode);
            cv.put(NCDPersonNCD.PID, share_pid);
            cv.put(NCDPersonNCD.CHRONIC_START_DATE,
                    DateTime.getCurrentDateTime());
            getContentResolver().insert(NCDPersonNCD.CONTENT_URI, cv);
            return;
        }
    }

    private void updateNCDPersonInfo(ContentValues cv) {
        Cursor c = getContentResolver().query(NCDPerson.CONTENT_URI,
                new String[]{NCDPerson.PID},
                NCDPerson.PID + "=" + share_pid, null, NCDPerson.PID);
        if (c.moveToFirst()) {
            Log.d("NCD_FFC",
                    "THIS PERSON ALREADY HAVE INFORMATION IN NCDPerson");
            return;
        } else {
            cv = getPersonContentValue();

            if (!TextUtils.isEmpty(height.getText()))
                cv.put(NCDPerson.HEIGHT, height.getText().toString());
            if (!TextUtils.isEmpty(weight.getText()))
                cv.put(NCDPerson.WEIGHT, weight.getText().toString());
            if (!TextUtils.isEmpty(waist.getText()))
                cv.put(NCDPerson.WAIST, waist.getText().toString());
            cv.put(NCDPerson.PCUCODE, share_pcucode);
            cv.put(NCDPerson.PID, share_pid);
            cv.put(NCDPerson.HD, share_pid);
            cv.put(NCDPerson.USERNAME, getUser());
            cv.put(NCDPerson.DATEUPDATE, DateTime.getCurrentDate());

            getContentResolver().insert(NCDPerson.CONTENT_URI, cv);
            return;
        }
    }

    private ContentValues getPersonContentValue() {
        ContentValues cv = new ContentValues();
        Cursor c = getContentResolver().query(
                Person.CONTENT_URI,
                new String[]{Person.CITIZEN_ID, Person.PRENAME,
                        Person.FIRST_NAME, Person.LAST_NAME, Person.BIRTH,
                        Person.SEX, Person.ADDR_NO, Person.ADDR_MU,
                        Person.ADDR_SUBDIST, Person.ADDR_DIST,
                        Person.ADDR_PROVICE}, "pid = ?",
                new String[]{share_pid}, Person.DEFAULT_SORTING);

        if (c.moveToFirst()) {
            cv.put(NCDPerson.CID, c.getString(0));
            cv.put(NCDPerson.PRENAME, c.getString(1));
            cv.put(NCDPerson.NAME, c.getString(2));
            cv.put(NCDPerson.LNAME, c.getString(3));
            cv.put(NCDPerson.BIRTH, c.getString(4));
            cv.put(NCDPerson.SEX, c.getString(5));
            cv.put(NCDPerson.HOUSE, c.getString(6));
            cv.put(NCDPerson.VILLAGE, c.getString(10) + c.getString(9) + c.getString(8) + c.getString(7));
            cv.put(NCDPerson.TAMBOL, c.getString(10) + c.getString(9) + c.getString(8));
            cv.put(NCDPerson.AMPUR, c.getString(10) + c.getString(9));
            cv.put(NCDPerson.CHANGWAT, c.getString(10));
            return cv;
        } else {
            return null;
        }
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        // TODO Auto-generated method stub
        switch (arg0.getId()) {
            case R.id.hasSymptom:
                symptomHolder.setVisibility(hasSymptom.isChecked() ? View.VISIBLE
                        : View.GONE);
                break;
            case R.id.eyecheck:
                eyeNote.setEnabled(eyeCheck.isChecked() ? true : false);
                break;
            case R.id.urinalcheck:
                urinalNote.setEnabled(urinalCheck.isChecked() ? true : false);
                break;
            case R.id.footcheck:
                footNote.setEnabled(footCheck.isChecked() ? true : false);
                break;
            case R.id.heartcheck:
                heartNote.setEnabled(heartCheck.isChecked() ? true : false);
                break;
            case R.id.braincheck:
                brainNote.setEnabled(brainCheck.isChecked() ? true : false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {
        // TODO Auto-generated method stub
        switch (arg0.getId()) {
            case R.id.bstest:
                if (bstest.getSelectionId().equals("0")) {
                    bsl.setText("");
                    bsl.setEnabled(false);
                    bslHolder.setVisibility(View.GONE);
                } else {
                    bsl.setEnabled(true);
                    bslHolder.setVisibility(View.VISIBLE);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

}
