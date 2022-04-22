package th.in.ffc.person.visit;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import th.in.ffc.R;
import th.in.ffc.app.FFCEditActivity;
import th.in.ffc.app.FFCSearchListDialog;
import th.in.ffc.code.DrugFPDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.VisitDiag;
import th.in.ffc.provider.PersonProvider.VisitFamilyplan;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.Log;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.widget.ArrayFormatSpinner;
import th.in.ffc.widget.SearchableButton;

import java.util.Calendar;

public class VisitFamilyplanActivity extends FFCEditActivity {

    SearchableButton pregtest;
    EditText pregtestUnit;
    ArrayFormatSpinner pregtestResult;
    RadioGroup typefp;
    SearchableButton fpcode;
    EditText Unit;
    CheckBox checkdue;
    ThaiDatePicker datedue;
    String sexual;
    RadioGroup userOrnot;
    LinearLayout userOrnotContainer;
    ContentValues globalC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        share_visitno = data.getLastPathSegment();
        share_pid = getIntent().getExtras().getString(Person.PID);
        System.out.println(share_visitno);
        System.out.println(getPcuCode());

        setContentView(R.layout.visit_familyplan_activity);
        checkSex();
        prepareDatePicker();
        setContentDisplay();
    }

    private void checkSex() {
        // TODO Auto-generated method stub
        Cursor c = getContentResolver().query(Person.CONTENT_URI, new String[]{Person.SEX}, "pid =? AND pcucodeperson=?", new String[]{share_pid, share_pcucode}, Person.DEFAULT_SORTING);
        if (c.moveToFirst()) {
            sexual = c.getString(0);
            System.out.println("I'M A " + (sexual.equals("1") ? "GUY" : "GIRL"));
        }
    }

    private void prepareDatePicker() {
        // TODO Auto-generated method stub
        checkdue = (CheckBox) findViewById(R.id.checkdue);
        datedue = (ThaiDatePicker) findViewById(R.id.answer7);
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        datedue.updateDate(year, month, day);

        checkdue.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                datedue.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

    }


    private void setContentDisplay() {

        pregtest = (SearchableButton) findViewById(R.id.answer1);
        pregtestUnit = (EditText) findViewById(R.id.answer2);
        pregtestResult = (ArrayFormatSpinner) findViewById(R.id.answer3);
        pregtestResult.setArray(sexual.equals("2") ? R.array.pregtestresult : R.array.pregtestresult_male);
        typefp = (RadioGroup) findViewById(R.id.answer4);
        fpcode = (SearchableButton) findViewById(R.id.answer5);
        Unit = (EditText) findViewById(R.id.answer6);
        checkdue = (CheckBox) findViewById(R.id.checkdue);
        datedue = (ThaiDatePicker) findViewById(R.id.answer7);
        userOrnot = (RadioGroup) findViewById(R.id.userOrnot);
        userOrnotContainer = (LinearLayout) findViewById(R.id.isUseFP);

        Bundle bb = new Bundle();
        bb.putString(FFCSearchListDialog.EXTRA_DEFAULT_WHERE, "drugcode in ('16','17')");
        pregtest.setDialog(getSupportFragmentManager(), DrugFPDialog.class, bb, "pregtest");
        Bundle bb2 = new Bundle();
        bb2.putString(FFCSearchListDialog.EXTRA_DEFAULT_WHERE, "drugcode not in ('16','17')");
        fpcode.setDialog(getSupportFragmentManager(), DrugFPDialog.class, bb2, "fpcode");

        userOrnot.setOnCheckedChangeListener(radiochecker);
        typefp.setOnCheckedChangeListener(radiochecker);
        typefp.check(R.id.rad1);

        setContentQuery(VisitFamilyplan.CONTENT_URI, new String[]{VisitFamilyplan.DATEFP, VisitFamilyplan.PREGTEST, VisitFamilyplan.PREGTESTUNIT, VisitFamilyplan.PREGTESTRESULT,
                VisitFamilyplan.TYPEFP, VisitFamilyplan.FPCODE, VisitFamilyplan.UNIT, VisitFamilyplan.DATEDUE}, "visitno =?", new String[]{share_visitno}, VisitFamilyplan.DEFAULT_SORTING);
        if (cursorChecker == true) {
            setContentForUpdate();
        }
    }

    TextWatcher tw = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
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
            if (!TextUtils.isEmpty(Unit.getText().toString())) {
                Log.d("DATA", "I'M HERE NAKA");
                String data = Unit.getText().toString();
                int day = Integer.parseInt(data);
                day = 28 * day;
                String d = DateTime.getCurrentDate();
                Date date = Date.newInstance(d);
                Log.d("DATA", day + "");
                date.increaseDay(day);
                Log.d("DATA", date.toString());
                if (checkdue.isChecked())
                    datedue.updateDate(date);
                Log.d("DATA", "END NAKA");
            }
        }
    };
    RadioGroup.OnCheckedChangeListener radiochecker = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // TODO Auto-generated method stub
            int gId = group.getId();
            switch (gId) {
                case R.id.userOrnot:
                    userOrnotContainer.setVisibility(checkedId == R.id.rad1 ? View.VISIBLE : View.GONE);
                    if (checkedId == R.id.rad1)
                        Unit.addTextChangedListener(tw);
                    else
                        Unit.removeTextChangedListener(tw);
                    break;
                case R.id.answer4:
                    pregtestUnit.setText((checkedId == R.id.rad1 ? "3" : "1"));
                    Unit.setText((checkedId == R.id.rad1 ? "3" : "1"));
                default:
                    break;
            }


        }
    };

    private void setContentForUpdate() {
        // TODO Auto-generated method stub
        if (array[1] != null)
            pregtest.setSelectionById(array[1]);
        pregtestUnit.setText(checkEditText(array[2]));
        if (array[3] != null)
            pregtestResult.setSelection(array[3]);
        if (array[4] != null) {
            int select = Integer.parseInt(array[4]);
            if (select == 1)
                typefp.check(R.id.rad2);
            else
                typefp.check(R.id.rad1);

        }
        userOrnot.check(TextUtils.isEmpty(array[5]) ? R.id.rad2 : R.id.rad1);
        if (array[5] != null) {
            fpcode.setSelectionById(array[5]);
        }

        Unit.setText(checkEditText(array[6]));
        checkdue.setChecked(array[7] != null ? true : false);
        if (checkdue.isChecked())
            updatePicker(array[7]);
    }

    private void updatePicker(String data) {
        if (!TextUtils.isEmpty(data)) {
        } else {
            String[] Date = data.split("-");
            int year = Integer.parseInt(Date[0]);
            int month = Integer.parseInt(Date[1]);
            int day = Integer.parseInt(Date[2]);
            datedue.updateDate(year, month, day);
        }
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
        ContentValues cv = new ContentValues();
        cv.put("pcucodeperson", getPcuCode());
        cv.put("pcucode", getPcuCode());
        cv.put("visitno", share_visitno);
        cv.put("pid", share_pid);
        //Auto generate datefp by System
        setCurrentDate(cv, "datefp");
        retrieveDataFromSearchableButton(pregtest, cv, VisitFamilyplan.PREGTEST);
        retrieveDataFromEditText(pregtestUnit, cv, "pregtestunit");
        cv.put("pregtestresult", pregtestResult.getSelectionId());
        cv.put("typefp", typefp.getCheckedRadioButtonId() == R.id.rad1 ? "0" : "1");
        if (userOrnot.getCheckedRadioButtonId() == R.id.rad1) {
            retrieveDataFromSearchableButton(fpcode, cv, VisitFamilyplan.FPCODE);
            retrieveDataFromEditText(Unit, cv, "unit");
        }


        if (checkdue.isChecked()) {
            retrieveDataFromThaiDatePicker(datedue, cv, "datedue", checkdue.isChecked());
//			canCommit = checkSickDate(datedue);
        } else
            cv.putNull("datedue");
        updateTimeStamp(cv);


        canCommit = doCheckConstraints(userOrnot.getCheckedRadioButtonId());
        System.out.println("PREG RESULT = " + pregtestResult.getSelectionId());
        if (pregtestResult.getSelectionId().equals("1") && canCommit) {
            ContentValues mCvs = new ContentValues();
            mCvs.put(VisitDiag.PCUCODE, share_pcucode);
            mCvs.put(VisitDiag.NO, share_visitno);
            mCvs.put(VisitDiag.CODE, "Z32");
            mCvs.put(VisitDiag.CONTINUE, "0");
            mCvs.put(VisitDiag.CLINIC, "00401");

            mCvs.put(VisitDiag.DATEUPDATE, DateTime.getCurrentDateTime());

            Cursor c = getContentResolver().query(VisitDiag.CONTENT_URI, new String[]{VisitDiag.MAX}, null, null, VisitDiag.DEFAULT_SORTING);
            if (c.moveToFirst()) {
                if (!c.getString(0).equals(share_visitno))
                    mCvs.put(VisitDiag.TYPE, "01");
                else
                    mCvs.put(VisitDiag.TYPE, "02");
                getContentResolver().insert(VisitDiag.CONTENT_URI, mCvs);

            }
            this.globalC = cv;
            AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
            builder.setMessage(getResources().getString(R.string.dialog_goPreg))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    goPreg();
                                    doCommit(globalC, VisitFamilyplan.CONTENT_URI, canCommit);
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
//									dialog.cancel();
                                    doCommit(globalC, VisitFamilyplan.CONTENT_URI, canCommit);
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();

        } else
            doCommit(cv, VisitFamilyplan.CONTENT_URI, canCommit);
    }

    private boolean doCheckConstraints(int checkID) {
        // TODO Auto-generated method stub
        Boolean ed1 = false, ed2 = false, ed3 = false, ed4 = false;
        Boolean isUse = checkID == R.id.rad1 ? true : false;


        ed1 = pregtest.getSelectId() != null ? true : false;
        ed2 = !TextUtils.isEmpty(pregtestUnit.getText());

        if (isUse) {
            ed3 = fpcode.getSelectId() != null ? true : false;
            ed4 = !TextUtils.isEmpty(Unit.getText());
        } else {
            ed3 = true;
            ed4 = true;
        }

        if (!ed1)
            ERROR_MSG += "\n - " + getString(R.string.visitfp_pregtest) + " " + getString(R.string.err_no_select);
        if (!ed2)
            ERROR_MSG += "\n - " + getString(R.string.visitfp_pregtestunit) + " " + getString(R.string.err_no_value);
        if (!ed3)
            ERROR_MSG += "\n - " + getString(R.string.visitfp_fpcode) + " " + getString(R.string.err_no_select);
        if (!ed4)
            ERROR_MSG += "\n - " + getString(R.string.visitfp_unit) + " " + getString(R.string.err_no_value);

        return ed1 && ed2 && ed3 && ed4;
    }

    protected void doCommit(ContentValues cv, Uri uri, Boolean canCommit) {
        if (cv != null && canCommit) {
            System.out.println("Begin Update . . .");
            ContentResolver cr = getContentResolver();
            int rows = cr.update(uri, cv, "visitno=?",
                    new String[]{share_visitno});
            System.out.println("Update " + rows + " rows");
            if (rows == 1) {
                System.out.println("Update Success");
            } else {
                System.out.println("Update Failed swap to Insert . . .");
                cr.insert(uri, cv);
                System.out.println("Insert Success");
            }

            this.finish();
        } else {
            Toast.makeText(this, getString(R.string.toast_abort) + ERROR_MSG,
                    Toast.LENGTH_LONG).show();
            ERROR_MSG = "";
        }
    }

    void goPreg() {
        Intent intent = new Intent(VisitFamilyplanActivity.this, VisitAncPregnancyActivity.class);
        intent.setAction(Action.INSERT);
        VisitActivity.startVisitActivity(this, intent, share_visitno, share_pid, share_pcucode);
    }


}
