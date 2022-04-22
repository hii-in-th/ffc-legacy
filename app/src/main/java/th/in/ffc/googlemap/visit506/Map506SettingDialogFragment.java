package th.in.ffc.googlemap.visit506;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import th.in.ffc.R;
import th.in.ffc.util.ThaiDatePicker;

public class Map506SettingDialogFragment extends DialogFragment {

    private Switch switchMarker;
    public settingCallBack settingCallBack;
    private CheckBox chkPlauge;
    private CheckBox chkTime;
    private CheckBox chkDisplaySt;
    private CheckBox chkDisplayHi;
    private boolean checkDisHi;
    private boolean checkDisSt;
    private boolean oldCheckDisHi;
    private boolean oldCheckDisSt;
    private ThaiDatePicker datePickerStart;
    private ThaiDatePicker datePickerEnd;
    private LinearLayout linearstart;
    private LinearLayout linearend;
    private LinearLayout linearspinner;
    private Spinner spinner;
    private boolean queryTime;
    private boolean queryPlauge;
    private boolean markerState;
    private boolean oldQueryTime;
    private boolean oldQueryPlauge;
    private boolean oldMarkerState;
    private String dateStart;
    private String dateEnd;
    private String plauge;
    private int spinnerPosition;
    private HashMap<String, String> listNamePlague;
    private settingCallBack callback;
    private int displayMode;

    static Map506SettingDialogFragment newInstance(
            HashMap<String, String> listNamePlague) {
        Map506SettingDialogFragment f = new Map506SettingDialogFragment();
        f.listNamePlague = listNamePlague;
        f.checkDisHi = false;
        f.checkDisSt = true;
        f.queryTime = false;
        f.queryPlauge = false;
        f.markerState = true;
        f.dateStart = "";
        f.dateEnd = "";
        f.plauge = "";
        f.displayMode = 0;
        return f;
    }

    public interface settingCallBack {
        void settingCallBack(boolean chkSwitch, String queryNamePlauge,
                String startDate, String endDate, int displayMode);
    }

    public void setOnClickListenerCallBack(settingCallBack callback) {
        this.callback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("การตั้งค่าการแสดงผล");
        builder.setView(getContentView());
        builder.setPositiveButton("ตกลง", positiveButton);
        builder.setNegativeButton("ยกเลิก", new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                queryTime = oldQueryTime;
                queryPlauge = oldQueryPlauge;
                markerState = oldMarkerState;
                checkDisHi = oldCheckDisHi;
                checkDisSt = oldCheckDisSt;
            }
        });
        Dialog dialog = builder.create();
        return dialog;
    }

    private View getContentView() {
        oldQueryTime = queryTime;
        oldQueryPlauge = queryPlauge;
        oldMarkerState = markerState;
        oldCheckDisHi = checkDisHi;
        oldCheckDisSt = checkDisSt;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = new java.util.Date();
        String dateInit[] = dateFormat.format(date).split("-");
        int year = Integer.parseInt(dateInit[0]);
        int month = Integer.parseInt(dateInit[1]) - 1;
        int day = Integer.parseInt(dateInit[2]);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.map506_setting, null);
        linearstart = (LinearLayout) v.findViewById(R.id.startdate);
        linearend = (LinearLayout) v.findViewById(R.id.enddate);
        linearspinner = (LinearLayout) v.findViewById(R.id.spinnerlist);
        chkTime = (CheckBox) v.findViewById(R.id.filltertime);
        chkPlauge = (CheckBox) v.findViewById(R.id.fillternameplauge);
        chkDisplaySt = (CheckBox) v.findViewById(R.id.displayst);
        chkDisplayHi = (CheckBox) v.findViewById(R.id.displayhi);
        datePickerStart = (ThaiDatePicker) v.findViewById(R.id.thaiDatePicker1);
        datePickerEnd = (ThaiDatePicker) v.findViewById(R.id.ThaiDatePicker02);
        datePickerStart.setLocale(ThaiDatePicker.LOCALE_THAI);
        datePickerStart.updateDate(year - 1, month, day);
        datePickerEnd.setLocale(ThaiDatePicker.LOCALE_THAI);
        datePickerEnd.updateDate(year, month, day);
        chkDisplaySt.setChecked(checkDisSt);
        chkDisplaySt.setOnCheckedChangeListener(onCheckList);
        chkDisplayHi.setChecked(checkDisHi);
        chkDisplayHi.setOnCheckedChangeListener(onCheckList);
        chkTime.setOnCheckedChangeListener(onCheckList);
        chkTime.setChecked(queryTime);
        chkPlauge.setOnCheckedChangeListener(onCheckList);
        chkPlauge.setChecked(queryPlauge);
        switchMarker = (Switch) v.findViewById(R.id.switchnormalhome);
        switchMarker.setChecked(markerState);
        if (queryTime) {
            linearstart.setVisibility(View.VISIBLE);
            linearend.setVisibility(View.VISIBLE);
            if (!dateStart.equals("") && !dateEnd.equals("")) {
                Log.d("TEST", "DATE START:" + dateStart + "&DATE END:"
                        + dateEnd);
                String tempDateStart[] = dateStart.split("-");
                int tempYear = Integer.parseInt(tempDateStart[0]);
                int tempMonth = Integer.parseInt(tempDateStart[1]) - 1;
                int tempDay = Integer.parseInt(tempDateStart[2]);
                datePickerStart.updateDate(tempYear, tempMonth, tempDay);
                String tempDateEnd[] = dateEnd.split("-");
                tempYear = Integer.parseInt(tempDateEnd[0]);
                tempMonth = Integer.parseInt(tempDateEnd[1]) - 1;
                tempDay = Integer.parseInt(tempDateEnd[2]);
                datePickerEnd.updateDate(tempYear, tempMonth, tempDay);
            }
        } else {
            linearstart.setVisibility(View.GONE);
            linearend.setVisibility(View.GONE);
        }
        if (queryPlauge) {
            linearspinner.setVisibility(View.VISIBLE);
            if (!plauge.equals("")) {
                spinner.setSelection(spinnerPosition);
            }
        } else {
            linearspinner.setVisibility(View.GONE);
        }
        switchMarker.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (arg1) {
                    markerState = true;
                } else {
                    markerState = false;
                }
            }
        });
        spinner = (Spinner) v.findViewById(R.id.listplauge);
        setSpinner();
        return v;
    }

    public void setSpinner() {
        Iterator<Map.Entry<String, String>> iterator = listNamePlague
                .entrySet().iterator();
        ArrayList<String> params = new ArrayList<String>();
        // params.add("ทั้งหมด");
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            params.add(entry.getValue());
        }
        String listOnSpinner[] = new String[params.size()];
        listOnSpinner = params.toArray(listOnSpinner);
        ArrayAdapter<String> arrAd = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, listOnSpinner);
        arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrAd);

    }

    OnCheckedChangeListener onCheckList = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.filltertime:
                    if (isChecked) {
                        linearstart.setVisibility(View.VISIBLE);
                        linearend.setVisibility(View.VISIBLE);
                        queryTime = true;
                    } else {
                        linearstart.setVisibility(View.GONE);
                        linearend.setVisibility(View.GONE);
                        queryTime = false;
                    }
                    break;
                case R.id.fillternameplauge:
                    if (isChecked) {
                        linearspinner.setVisibility(View.VISIBLE);
                        queryPlauge = true;
                    } else {
                        linearspinner.setVisibility(View.GONE);
                        queryPlauge = false;
                    }
                    break;
                case R.id.displayst:
                    if (isChecked) {
                        checkDisSt = true;
                        checkDisHi = false;
                        chkDisplayHi.setChecked(checkDisHi);
                        displayMode = 0;
                    }
                    break;
                case R.id.displayhi:
                    if (isChecked) {
                        checkDisSt = false;
                        checkDisHi = true;
                        chkDisplaySt.setChecked(checkDisSt);
                        displayMode = 1;
                    }
                    break;
            }

        }
    };
    OnClickListener positiveButton = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (queryPlauge) {
                plauge = spinner.getSelectedItem().toString();
                spinnerPosition = spinner.getSelectedItemPosition();
            } else {
                plauge = "";
            }
            if (queryTime) {
                dateStart = datePickerStart.getYear() + "-";
                dateStart += chkNumber(datePickerStart.getMonth()) + "-";
                dateStart += chkNumber(datePickerStart.getDayOfMonth());
                dateEnd = datePickerEnd.getYear() + "-";
                dateEnd += chkNumber(datePickerEnd.getMonth()) + "-";
                dateEnd += chkNumber(datePickerEnd.getDayOfMonth());
            } else {
                dateStart = "";
                dateEnd = "";
            }
            if (callback != null) {
                callback.settingCallBack(markerState, plauge, dateStart,
                        dateEnd, displayMode);
            }

        }
    };

    private String chkNumber(int a) {
        String paramreturn = "";
        if (a < 10) {
            paramreturn += "0" + a;
        } else {
            paramreturn = "" + a;
        }
        return paramreturn;
    }
}
