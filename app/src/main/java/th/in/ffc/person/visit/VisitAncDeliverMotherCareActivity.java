package th.in.ffc.person.visit;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import th.in.ffc.R;
import th.in.ffc.app.FFCEditActivity;
import th.in.ffc.code.HospitalListDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.persist.otherListPersist;
import th.in.ffc.provider.PersonProvider.*;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.util.ThaiTimePicker;
import th.in.ffc.widget.ArrayFormatSpinner;
import th.in.ffc.widget.SearchableButton;

import java.util.ArrayList;
import java.util.Calendar;

public class VisitAncDeliverMotherCareActivity extends FFCEditActivity {

    EditText pregno;
    SearchableButton hosservice;
    ThaiDatePicker datedeliver;
    ThaiTimePicker delivertime;
    ArrayFormatSpinner deliverresult;
    ArrayFormatSpinner operater;
    ArrayFormatSpinner delivertype;
    EditText numdeadinpreg;
    ArrayFormatSpinner deliverplace;
    Spinner deliverend;
    EditText signabnormalanc;
    EditText signabnormaldeliver;
    CheckBox checkappoint;
    ThaiDatePicker dateappoint;

    TextView AbortionL;
    RadioGroup locatecare;
    RadioGroup funduslevel;
    RadioGroup wabad;
    RadioGroup breast;
    RadioGroup milk;
    RadioGroup men;
    ArrayFormatSpinner alb;
    ArrayFormatSpinner sugar;
    ArrayFormatSpinner tear;
    RadioGroup resultcheck;

    String[] arrays;

    ArrayAdapter<CharSequence> deliverendAdapter;
    ArrayList<otherListPersist> deliverArray = new ArrayList<otherListPersist>();

    LinearLayout mothercare;
    LinearLayout deadfetus;
    Button getVaccine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        share_visitno = data.getLastPathSegment();
        System.out.println(share_visitno);
        System.out.println(getPcuCode());

        setContentView(R.layout.visit_ancdelivermothercare_activity);
        preparePicker();
        prepareSpinner();
        setContentDisplay();
    }

    private void preparePicker() {
        datedeliver = (ThaiDatePicker) findViewById(R.id.answer3);
        delivertime = (ThaiTimePicker) findViewById(R.id.answer4);
        checkappoint = (CheckBox) findViewById(R.id.checkappointcare);
        dateappoint = (ThaiDatePicker) findViewById(R.id.answer13);
        Calendar c = Calendar.getInstance();
        Date date = Date.newInstance(DateTime.getCurrentDate());
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);

        datedeliver.updateDate(date);
        delivertime.updateTime(hour, min);
        dateappoint.updateDate(date);
        checkappoint.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                dateappoint.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void prepareSpinner() {
        hosservice = (SearchableButton) findViewById(R.id.answer2);
        hosservice.setDialog(getSupportFragmentManager(),
                HospitalListDialog.class, "hosservice");
        generateFromDatabase(deliverend, deliverendAdapter, deliverArray,
                R.id.answer10,
                "SELECT diseasecode,diseasenamethai FROM cdisease WHERE diseasecode Like 'O%'");

    }

    private void setContentDisplay() {
        pregno = (EditText) findViewById(R.id.answer1);
        pregno.setText(getLastPreg());
        pregno.setEnabled(false);

        hosservice = (SearchableButton) findViewById(R.id.answer2);
        datedeliver = (ThaiDatePicker) findViewById(R.id.answer3);
//        Spinner day = (Spinner) datedeliver.getChildAt(0);
//        Spinner month = (Spinner) datedeliver.getChildAt(1);
//        Spinner year = (Spinner) datedeliver.getChildAt(2);

        Spinner day = (Spinner) datedeliver.getChildAt(0);
        Spinner month = (Spinner) datedeliver.getChildAt(1);
//        Spinner year = (Spinner) datedeliver.getChildAt(2);

        day.setOnItemSelectedListener(AbortionListener);
        month.setOnItemSelectedListener(AbortionListener);
//        year.setOnItemSelectedListener(AbortionListener);

        delivertime = (ThaiTimePicker) findViewById(R.id.answer4);
        deliverresult = (ArrayFormatSpinner) findViewById(R.id.answer5);
        operater = (ArrayFormatSpinner) findViewById(R.id.answer6);
        delivertype = (ArrayFormatSpinner) findViewById(R.id.answer7);
        numdeadinpreg = (EditText) findViewById(R.id.answer8);
        deliverplace = (ArrayFormatSpinner) findViewById(R.id.answer9);
        deliverend = (Spinner) findViewById(R.id.answer10);
        signabnormaldeliver = (EditText) findViewById(R.id.answer11);
        signabnormalanc = (EditText) findViewById(R.id.answer12);
        dateappoint = (ThaiDatePicker) findViewById(R.id.answer13);
        checkappoint = (CheckBox) findViewById(R.id.checkappointcare);
        AbortionL = (TextView) findViewById(R.id.abortionLength);

        locatecare = (RadioGroup) findViewById(R.id.answer21);
        funduslevel = (RadioGroup) findViewById(R.id.answer22);
        wabad = (RadioGroup) findViewById(R.id.answer23);
        breast = (RadioGroup) findViewById(R.id.answer24);
        milk = (RadioGroup) findViewById(R.id.answer25);
        men = (RadioGroup) findViewById(R.id.answer26);
        alb = (ArrayFormatSpinner) findViewById(R.id.answer27);
        sugar = (ArrayFormatSpinner) findViewById(R.id.answer28);
        tear = (ArrayFormatSpinner) findViewById(R.id.answer29);
        resultcheck = (RadioGroup) findViewById(R.id.answer30);

        mothercare = (LinearLayout) findViewById(R.id.mothercare);
        deadfetus = (LinearLayout) findViewById(R.id.deadfetus);

        getVaccine = (Button) findViewById(R.id.getViccine);
        getVaccine.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(VisitAncDeliverMotherCareActivity.this, VisitEpiActivity.class);
                intent.setData(Uri.withAppendedPath(Visit.CONTENT_URI, share_visitno));
                intent.setAction(Action.EPI_PREGNANCY);
                intent.putExtra(PersonColumns._PID, share_pid);
                intent.putExtra(PersonColumns._PCUCODEPERSON, share_pcucode);
                startActivity(intent);
            }
        });
        deliverresult.setArray(R.array.deliverresult);
        operater.setArray(R.array.operater);
        delivertype.setArray(R.array.delivertype);
        deliverplace.setArray(R.array.deliverplace);
        alb.setArray(R.array.mothercare_level);
        sugar.setArray(R.array.mothercare_level);
        tear.setArray(R.array.tear);

        deliverresult.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                if (deliverresult.getSelectedItemPosition() == 0 || deliverresult.getSelectedItemPosition() == 1) {
                    mothercare.setVisibility(View.VISIBLE);
                    deadfetus.setVisibility(View.GONE);
                } else {
                    mothercare.setVisibility(View.GONE);
                    deadfetus.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        // DefaultQuery AncDeliver
        setContentQuery(VisitAncDeliver.CONTENT_URI, new String[]{
                        VisitAncDeliver.PREGNO, VisitAncDeliver.HOSSERVICE,
                        VisitAncDeliver.DATEDELIVER, VisitAncDeliver.DELIVERTIME,
                        VisitAncDeliver.DELIVERRESULT, VisitAncDeliver.OPERATER,
                        VisitAncDeliver.DELIVERTYPE, VisitAncDeliver.NUMDATEINPREG,
                        VisitAncDeliver.DELIVERPLACE, VisitAncDeliver.DELIVEREND,
                        VisitAncDeliver.SIGNABNORMALANC,
                        VisitAncDeliver.SIGNABNORMALDELIVER,
                        VisitAncDeliver.DATEAPPOINTCARE}, "visitno = ?",
                new String[]{share_visitno}, VisitAncDeliver.DEFAULT_SORTING);
        // Query AncMothercare
        Cursor mC = getContentResolver().query(
                VisitAncMotherCare.CONTENT_URI,
                new String[]{VisitAncMotherCare.PREGNO,
                        VisitAncMotherCare.LOCATECARE,
                        VisitAncMotherCare.FUNDUSLEVEL,
                        VisitAncMotherCare.WABAD, VisitAncMotherCare.BREAST,
                        VisitAncMotherCare.MILK, VisitAncMotherCare.MEN,
                        VisitAncMotherCare.ALB, VisitAncMotherCare.SUGAR,
                        VisitAncMotherCare.TEAR, VisitAncMotherCare.HOSSERVICE,
                        VisitAncMotherCare.RESULTCHECK,
                        VisitAncMotherCare.DATEAPPOINTCARE}, "visitno = ?",
                new String[]{share_visitno},
                VisitAncMotherCare.DEFAULT_SORTING);
        if (mC.moveToFirst()) {
            int size = mC.getColumnCount();
            this.arrays = new String[size];
            if (mC.moveToFirst()) {
                System.out.println("Query Mothercare Success");
                System.out
                        .println("This is a form where Id = " + share_visitno);
                cursorChecker = true;

                for (int i = 0; i < size; i++) {
                    arrays[i] = mC.getString(i);
                }
            }
        }
        // END AncMothercare Query

        // Try to fill form if exists
        if (cursorChecker == true) {
            setContentForUpdate();
        }
    }

    OnItemSelectedListener AbortionListener = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            // TODO Auto-generated method stub
            int week = 0;
            Cursor c = getContentResolver().query(VisitAncPregnancy.CONTENT_URI, new String[]{VisitAncPregnancy.LMP}, " pid =? AND pcucodeperson =? AND pregno =? ", new String[]{share_pid, share_pcucode, pregno.getText().toString()}, "pregno DESC");
            if (c.moveToFirst()) {
                if (!TextUtils.isEmpty(c.getString(0))) {
                    System.out.println("LAST PERIOD DATE " + c.getString(0));
                    Date lmp = Date.newInstance(c.getString(0));
                    Date born = Date.newInstance(datedeliver.getDate()
                            .toString());
                    int days = lmp.distanceTo(born);
                    week = days / 7;
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.getVisitAncFirst), Toast.LENGTH_LONG).show();
                    doClose();
                }
            }
            if (week < 1)
                week += 1;
            AbortionL.setText(week + " " + getString(R.string.week));
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }
    };


    private String getLastPreg() {
        String pregno = "1";
        Cursor c = getContentResolver().query(VisitAncPregnancy.CONTENT_URI,
                new String[]{VisitAncPregnancy._PREGNO, VisitAncPregnancy.EDC},
                " pid=? AND pcucodeperson=?",
                new String[]{share_pid, share_pcucode}, "pregno DESC");

        if (c.moveToFirst()) {
            pregno = c.getString(0);
            if (!TextUtils.isEmpty(c.getString(1))) {
                Date d = Date.newInstance(c.getString(1));
                datedeliver.updateDate(d);
            }

            System.out.println("Found Last Pregno = " + pregno);
            return pregno;
        } else {
            System.out.println("new pregno");
            Toast.makeText(getApplicationContext(), getString(R.string.getVisitAncFirst), Toast.LENGTH_LONG).show();
            doClose();
            return pregno;
        }
    }

    @SuppressWarnings("deprecation")
    private void setContentForUpdate() {
        // TODO Auto-generated method stu5b
        // codeScreen

        pregno.setText(checkEditText(array[0]));
        if (array[1] != null)
            hosservice.setSelectionById(array[1]);
        if (array[2] != null)
            updatePicker(datedeliver, R.id.answer3, array[2]);
        if (array[3] != null)
            updateTime(delivertime, R.id.answer4, array[3]);
        // Invalid
        if (array[4] != null)
            deliverresult.setSelection(checkStrangerForStringContent(array[4],
                    "�"));
        if (array[5] != null)
            operater.setSelection(checkStrangerForStringContent(array[5], "1"));
        if (array[6] != null)
            delivertype.setSelection(checkStrangerForStringContent(array[6],
                    "1"));
        numdeadinpreg.setText(checkEditText(array[7]));
        if (array[8] != null)
            deliverplace.setSelection(checkStrangerForStringContent(array[8],
                    "1"));
        deliverend.setSelection(checkSpinnerQuery(array[9], deliverArray));
        signabnormalanc.setText(checkEditText(array[10]));
        signabnormaldeliver.setText(checkEditText(array[11]));
        if (array[12] != null) {
            checkappoint.setChecked(true);
            updatePicker(dateappoint, R.id.answer13, array[12]);
        }
        if (deliverresult.getSelectedItemPosition() == 1) {
            setCheckRadio(arrays[0], locatecare);
            setCheckRadio(arrays[1], funduslevel);
            setCheckRadio(arrays[2], wabad);
            setCheckRadio(arrays[3], breast);
            setCheckRadio(arrays[4], milk);
            setCheckRadio(arrays[5], men);
            if (arrays[6] != null)
                alb.setSelection(checkStrangerForStringContent(arrays[6], "1"));
            if (arrays[7] != null)
                sugar.setSelection(checkStrangerForStringContent(arrays[7], "1"));
            if (arrays[8] != null)
                tear.setSelection(checkStrangerForStringContent(arrays[8], "1"));
            setCheckRadio(arrays[9], resultcheck);
        }
    }

    private void setCheckRadio(String data, RadioGroup Rg) {
        if (TextUtils.isEmpty(data) && data.equals("-")) {
            Rg.check(R.id.rad1);
        } else {
            int select = Integer.parseInt(data);
            switch (select) {
                case 1:
                    Rg.check(R.id.rad1);
                    break;
                case 2:
                    Rg.check(R.id.rad2);
                    break;
                case 3:
                    Rg.check(R.id.rad3);
                    break;
                default:
                    Rg.check(R.id.rad1);
                    break;
            }
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

    @SuppressWarnings("deprecation")
    @Override
    protected void Update() {
        // TODO Auto-generated method stub
        ContentValues cv = new ContentValues();
        cv.put("pcucode", getPcuCode());
        cv.put("visitno", share_visitno);
        cv.put("pcucodeperson", getPcuCode());
        cv.put("pid", share_pid);


        ContentValues cv2 = new ContentValues();
        cv2.put("pcucode", getPcuCode());
        cv2.put("pcucodeperson", getPcuCode());
        cv2.put("visitno", share_visitno);
        cv2.put("pid", share_pid);
        cv2.put("datecare", DateTime.getCurrentDate());
        retrieveDataFromEditText(pregno, cv2, VisitAncDeliver.PREGNO);


        retrieveDataFromEditText(pregno, cv, VisitAncDeliver.PREGNO);
        if (hosservice.getSelectId() != null)
            cv.put(VisitAncDeliver.HOSSERVICE, hosservice.getSelectId());
        else
            cv.put(VisitAncDeliver.HOSSERVICE, "00000");
        retrieveDataFromThaiDatePicker(datedeliver, cv,
                VisitAncDeliver.DATEDELIVER, true);
        cv.put(VisitAncDeliver.DELIVERTIME, delivertime.toString());
        cv.put(VisitAncDeliver.DELIVERRESULT, deliverresult.getSelectionId());
        cv.put(VisitAncDeliver.OPERATER, operater.getSelectionId());
        cv.put(VisitAncDeliver.DELIVERTYPE, delivertype.getSelectionId());
        cv.put(VisitAncDeliver.DELIVERPLACE, deliverplace.getSelectionId());
        retrieveDataFromEditText(numdeadinpreg, cv,
                VisitAncDeliver.NUMDATEINPREG);
        retrieveDataFromSpinnerWithQuery(deliverend, cv,
                VisitAncDeliver.DELIVEREND, deliverArray);
        retrieveDataFromEditText(signabnormalanc, cv,
                VisitAncDeliver.SIGNABNORMALANC);
        retrieveDataFromEditText(signabnormaldeliver, cv,
                VisitAncDeliver.SIGNABNORMALDELIVER);
        retrieveDataFromThaiDatePicker(dateappoint, cv,
                VisitAncDeliver.DATEAPPOINTCARE, checkappoint.isChecked());
        updateTimeStamp(cv);

        if (TextUtils.isEmpty(pregno.getText().toString()))
            ERROR_MSG += "\n - " + getString(R.string.err_no_pregno);
        else
            canCommit = true;

        if (deliverresult.getSelectedItemPosition() == 1) {
            cv.putNull(VisitAncDeliver.NUMDATEINPREG);
            cv.putNull(VisitAncDeliver.DELIVEREND);

            if (hosservice.getSelectId() != null)
                cv2.put(VisitAncMotherCare.HOSSERVICE, hosservice.getSelectId());
            else
                cv2.put(VisitAncMotherCare.HOSSERVICE, "00000");
            cv2.put(VisitAncMotherCare.ALB, alb.getSelectionId());
            cv2.put(VisitAncMotherCare.SUGAR, sugar.getSelectionId());
            cv2.put(VisitAncMotherCare.TEAR, tear.getSelectionId());

            retrieveDataFromRadioGroup(locatecare, cv2,
                    VisitAncMotherCare.LOCATECARE);
            retrieveDataFromRadioGroup(funduslevel, cv2,
                    VisitAncMotherCare.FUNDUSLEVEL);
            retrieveDataFromRadioGroup(wabad, cv2, VisitAncMotherCare.WABAD);
            retrieveDataFromRadioGroup(breast, cv2, VisitAncMotherCare.BREAST);
            retrieveDataFromRadioGroup(milk, cv2, VisitAncMotherCare.MILK);
            retrieveDataFromRadioGroup(men, cv2, VisitAncMotherCare.MEN);
            retrieveDataFromRadioGroup(resultcheck, cv2,
                    VisitAncMotherCare.RESULTCHECK);
            retrieveDataFromThaiDatePicker(dateappoint, cv2,
                    VisitAncMotherCare.DATEAPPOINTCARE,
                    checkappoint.isChecked());
            updateTimeStamp(cv2);
            doUpdateMothercare(cv2, canCommit);
        } else {
            cv2.putNull(VisitAncMotherCare.HOSSERVICE);
            cv2.putNull(VisitAncMotherCare.ALB);
            cv2.putNull(VisitAncMotherCare.SUGAR);
            cv2.putNull(VisitAncMotherCare.TEAR);
            cv2.putNull(VisitAncMotherCare.LOCATECARE);
            cv2.putNull(VisitAncMotherCare.FUNDUSLEVEL);
            cv2.putNull(VisitAncMotherCare.WABAD);
            cv2.putNull(VisitAncMotherCare.BREAST);
            cv2.putNull(VisitAncMotherCare.MILK);
            cv2.putNull(VisitAncMotherCare.MEN);
            cv2.putNull(VisitAncMotherCare.RESULTCHECK);
            cv2.putNull(VisitAncMotherCare.DATEAPPOINTCARE);
            doUpdateMothercare(cv2, canCommit);
        }
        if (deliverresult.getSelectionId().equals("0")) {
            canCommit = TextUtils.isEmpty(numdeadinpreg.getText()) ? false : true;
            ERROR_MSG += getString(R.string.err_no_numdeadinpreg);
        }
        doCheckBeForeCommit(cv, canCommit);
    }

    private void doUpdateMothercare(ContentValues cv, Boolean commit) {
        // TODO Auto-generated method stub
        if (cv != null && commit) {
            System.out.println("Begin Update . . .");
            ContentResolver cr = getContentResolver();
            int rows = cr.update(VisitAncMotherCare.CONTENT_URI, cv,
                    "visitno=?", new String[]{share_visitno});
            System.out.println("Update " + rows + " rows");
            if (rows == 1) {
                System.out.println("Update Success");
            } else {
                System.out.println("Update Failed swap to Insert . . .");
                cr.insert(VisitAncMotherCare.CONTENT_URI, cv);
                System.out.println("Insert Success");
            }
        }
    }

    private void doCheckBeForeCommit(ContentValues cv, Boolean commit) {
        // Update
        if (cv != null && commit) {
            System.out.println("Begin Update . . .");
            ContentResolver cr = getContentResolver();
            int rows = cr.update(VisitAncDeliver.CONTENT_URI, cv, "visitno=?",
                    new String[]{share_visitno});
            System.out.println("Update " + rows + " rows");
            if (rows == 1) {
                System.out.println("Update Success");
            } else {
                System.out.println("Update Failed swap to Insert . . .");
                cr.insert(VisitAncDeliver.CONTENT_URI, cv);
                System.out.println("Insert Success");
            }
            doClose();
        } else {
            Toast.makeText(this, getString(R.string.toast_abort) + ERROR_MSG,
                    Toast.LENGTH_SHORT).show();
            ERROR_MSG = "";
        }
    }

    void doClose() {
        this.finish();
    }
}
