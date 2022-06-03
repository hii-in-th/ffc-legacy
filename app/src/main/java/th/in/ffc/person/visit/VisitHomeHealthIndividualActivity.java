package th.in.ffc.person.visit;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import th.in.ffc.R;
import th.in.ffc.app.FFCEditActivity;
import th.in.ffc.provider.CodeProvider.HomeHealthEvalPlan;
import th.in.ffc.provider.CodeProvider.HomeHealthServiceCare;
import th.in.ffc.provider.CodeProvider.HomeHealthSign;
import th.in.ffc.provider.CodeProvider.HomeHealthType;
import th.in.ffc.provider.PersonProvider;
import th.in.ffc.provider.PersonProvider.VisitIndividual;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.widget.CursorStringIdAdapter;
import th.in.ffc.widget.J_InstantAutoComplete;
import th.in.ffc.provider.PersonProvider.Visit;
import java.util.Calendar;

public class VisitHomeHealthIndividualActivity extends FFCEditActivity {


    Spinner type;
    J_InstantAutoComplete patientsign;
    J_InstantAutoComplete detail;
    J_InstantAutoComplete result;
    J_InstantAutoComplete plan;
    CheckBox checkappoint;
    ThaiDatePicker dateappoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        share_visitno = data.getLastPathSegment();
        System.out.println(share_visitno);
        System.out.println(getPcuCode());

        setContentView(R.layout.visit_homehealthindividual_activity);
        prepareSpinner();
        prepareDatePicker();
        setContentDisplay();

    }

    private void prepareDatePicker() {
        // TODO Auto-generated method stub
        checkappoint = (CheckBox) findViewById(R.id.checkappoint);
        dateappoint = (ThaiDatePicker) findViewById(R.id.answer6);
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        dateappoint.updateDate(year, month, day);
        checkappoint.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                dateappoint.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

    }

    private void prepareSpinner() {
        // TODO Auto-generated method stub
//		generateFromDatabase(HomeHealthType.CONTENT_URI, new String[] {
//				HomeHealthType._ID, HomeHealthType.NAME }, null, null, null,
//				type, R.id.answer1);
        type = (Spinner) findViewById(R.id.answer1);
        Cursor c = getContentResolver().query(HomeHealthType.CONTENT_URI, new String[]{
                HomeHealthType._ID, HomeHealthType.NAME}, null, null, null);

        CursorStringIdAdapter csa = new CursorStringIdAdapter(this, R.layout.default_spinner_item,
                c, new String[]{HomeHealthType.NAME}, new int[]{R.id.content});
        type.setAdapter(csa);


        generateAutoCompleteTextView(HomeHealthSign.CONTENT_URI, new String[]{
                        HomeHealthSign._ID, HomeHealthSign.SIGN}, null, null, null,
                patientsign, R.id.answer2);
        generateAutoCompleteTextView(HomeHealthServiceCare.CONTENT_URI,
                new String[]{HomeHealthServiceCare._ID,
                        HomeHealthServiceCare.SERVICE}, null, null, null,
                detail, R.id.answer3);
        generateAutoCompleteTextView(
                HomeHealthEvalPlan.CONTENT_URI,
                new String[]{HomeHealthEvalPlan._ID, HomeHealthEvalPlan.PLAN},
                null, null, null, result, R.id.answer4);
        generateAutoCompleteTextView(
                HomeHealthEvalPlan.CONTENT_URI,
                new String[]{HomeHealthEvalPlan._ID, HomeHealthEvalPlan.PLAN},
                null, null, null, plan, R.id.answer5);

    }

    private void setContentDisplay() {
        // TODO Auto-generated method stub
        type = (Spinner) findViewById(R.id.answer1);
        patientsign = (J_InstantAutoComplete) findViewById(R.id.answer2);
        detail = (J_InstantAutoComplete) findViewById(R.id.answer3);
        result = (J_InstantAutoComplete) findViewById(R.id.answer4);
        plan = (J_InstantAutoComplete) findViewById(R.id.answer5);
        checkappoint = (CheckBox) findViewById(R.id.checkappoint);
        dateappoint = (ThaiDatePicker) findViewById(R.id.answer6);

        setContentQuery(VisitIndividual.CONTENT_URI, new String[]{
                        VisitIndividual.PCUCODE, VisitIndividual.VISITNO,
                        VisitIndividual.TYPE, VisitIndividual.PATIENTSIGN,
                        VisitIndividual.DETAIL, VisitIndividual.RESULT,
                        VisitIndividual.PLAN, VisitIndividual.DATEAPPOINT
                },
                "visitno = ?", new String[]{share_visitno},
                VisitIndividual.DEFAULT_SORTING);
        if (cursorChecker == true) {
            setContentForUpdate();
        }
    }

    private void setContentForUpdate() {
        // TODO Auto-generated method stub
        for (String a : array) {
            System.out.println("ARRAY GET" + a);
        }

        type.setSelection(checkSpinnerQueryString(array[2], type));
        patientsign.setText(checkEditText(array[3]));
        detail.setText(checkEditText(array[4]));
        result.setText(checkEditText(array[5]));
        plan.setText(checkEditText(array[6]));

        checkappoint.setChecked(array[7] != null ? true : false);
        if (checkappoint.isChecked()) {
            updatePicker(dateappoint, R.id.answer6, array[7]);
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
        cv.put("pcucode", getPcuCode());
        cv.put("visitno", share_visitno);

        canCommit = true;
        CursorStringIdAdapter selected = (CursorStringIdAdapter) type.getAdapter();
        Log.d("HomeVisit", "visit type=" + selected.getStringId(type.getSelectedItemPosition()));
        cv.put("homehealthtype", selected.getStringId(type.getSelectedItemPosition()));
        //retrieveDataFromSpinnerWithQuery(type, cv, "homehealthtype");
        retrieveDataFromEditText(patientsign, cv, "patientsign");
        retrieveDataFromEditText(detail, cv, "homehealthdetail");
        retrieveDataFromEditText(result, cv, "homehealthresult");
        retrieveDataFromEditText(plan, cv, "homehealthplan");

        if (checkappoint.isChecked()) {
            retrieveDataFromThaiDatePicker(dateappoint, cv, "dateappoint", checkappoint.isChecked());
//			canCommit = checkSickDate(dateappoint);
        } else
            cv.putNull("dateappoint");
        cv.put("user", getIntent().getStringExtra(EXTRA_USER));
        doCommit(cv, VisitIndividual.CONTENT_URI, canCommit);
    }

}
