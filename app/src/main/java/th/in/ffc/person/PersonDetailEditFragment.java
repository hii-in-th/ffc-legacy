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

package th.in.ffc.person;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import th.in.ffc.R;
import th.in.ffc.app.FFCSearchListDialog;
import th.in.ffc.app.form.EditFormActivity.EditTransaction;
import th.in.ffc.code.*;
import th.in.ffc.intent.Action;
import th.in.ffc.person.PersonDetailEditActivity.Saveable;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.ThaiCitizenID;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.widget.ArrayFormatSpinner;
import th.in.ffc.widget.SearchableButton;
import th.in.ffc.widget.SearchableSpinner;

/**
 * add description here! please
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public class PersonDetailEditFragment extends PersonFragment implements
        Saveable, LoaderCallbacks<Cursor> {

    EditText citizenId;
    RadioGroup sex;
    ArrayFormatSpinner prename;
    EditText fname;
    EditText lname;
    ThaiDatePicker birthday;
    ArrayFormatSpinner bloodType;
    ArrayFormatSpinner bloodRh;
    EditText allergic;
    ArrayFormatSpinner religion;
    SearchableSpinner house;
    SearchableButton nation;
    SearchableButton origin;
    SearchableButton occupa;
    ArrayFormatSpinner education;
    EditText income;
    EditText tel;

    EditText hno;
    EditText mu;
    EditText road;
    SearchableButton subdistcode;
    SearchableButton distcode;
    SearchableButton provcode;
    EditText postcode;

    int newPid;
    String mHcode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.person_detail_edit_fragment,
                container, false);

        citizenId = (EditText) view.findViewById(R.id.answer1);
        sex = (RadioGroup) view.findViewById(R.id.group);
        prename = (ArrayFormatSpinner) view.findViewById(R.id.answer2);
        prename.setArray(R.array.prename);
        fname = (EditText) view.findViewById(R.id.answer3);
        lname = (EditText) view.findViewById(R.id.answer4);
        birthday = (ThaiDatePicker) view.findViewById(R.id.answer5);
        Date cur = Date.newInstance(DateTime.getCurrentDate());
        birthday.updateDate(cur.year, cur.month - 1, cur.day);
        bloodType = (ArrayFormatSpinner) view.findViewById(R.id.answer6);
        bloodType.setArray(R.array.blood_type);
        bloodRh = (ArrayFormatSpinner) view.findViewById(R.id.answer7);
        bloodRh.setArray(R.array.blood_rh);
        religion = (ArrayFormatSpinner) view.findViewById(R.id.answer8);
        religion.setArray(R.array.religion);
        nation = (SearchableButton) view.findViewById(R.id.answer9);
        nation.setDialog(getActivity().getSupportFragmentManager(),
                NationListDialog.class, "nation");
        origin = (SearchableButton) view.findViewById(R.id.answer11);
        origin.setDialog(getActivity().getSupportFragmentManager(),
                NationListDialog.class, "origin");
        house = (SearchableSpinner) view.findViewById(R.id.answer10);
        house.setDialog(getActivity().getSupportFragmentManager(),
                HouseListDialog.class, "house");
        income = (EditText) view.findViewById(R.id.answer12);
        allergic = (EditText) view.findViewById(R.id.answer13);
        tel = (EditText) view.findViewById(R.id.answer14);
        education = (ArrayFormatSpinner) view.findViewById(R.id.answer15);
        education.setArray(R.array.education);
        occupa = (SearchableButton) view.findViewById(R.id.answer16);
        occupa.setDialog(getActivity().getSupportFragmentManager(),
                OccupaListDialog.class, "occupa");

        // House
        hno = (EditText) view.findViewById(R.id.hno);
        mu = (EditText) view.findViewById(R.id.mu);
        road = (EditText) view.findViewById(R.id.road);
        subdistcode = (SearchableButton) view.findViewById(R.id.subdistcode);
        distcode = (SearchableButton) view.findViewById(R.id.distcode);
        provcode = (SearchableButton) view.findViewById(R.id.provcode);
        postcode = (EditText) view.findViewById(R.id.postcode);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        provcode.setDialog(getFFCActivity().getSupportFragmentManager(),
                ProvinceListDialog.class, "prov");
        distcode.setDialog(getFFCActivity().getSupportFragmentManager(),
                DistrictListDialog.class, "dist");
        subdistcode.setDialog(getFFCActivity().getSupportFragmentManager(),
                SubDistrictListDialog.class, "subdist");

        provcode.addTextChangedListener(watchprov);
        distcode.addTextChangedListener(watchdist);

        String action = getArguments().getString("action");
        if (savedInstanceState == null) {
            if (action.equals(Action.EDIT))
                getActivity().getSupportLoaderManager().initLoader(0, null,
                        this);
            else if (action.equals(Action.INSERT)) {
                // set default hcode if it have
                String hcode = getArguments().getString("hcode");
                if (!TextUtils.isEmpty(hcode) && TextUtils.isDigitsOnly(hcode)) {
                    house.setSelectionById(Long.parseLong(hcode));
                    house.setEnabled(false);
                    mHcode = hcode;

                    getDefaultHouseDetail(hcode);
                }

                getActivity().getSupportLoaderManager().initLoader(1, null,
                        this);
            }
        }

        citizenId.addTextChangedListener(idcardWatcher);
    }

    String mCitizenID;
    boolean isUseableId = true;
    boolean isCurrentId = true;

    TextWatcher watchprov = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // TODO Auto-generated method stub
            Bundle bb = new Bundle();
            bb.putString(FFCSearchListDialog.EXTRA_APPEND_WHERE, "provcode = '"
                    + provcode.getSelectId() + "'");
            distcode.setDialog(getFFCActivity().getSupportFragmentManager(),
                    DistrictListDialog.class, bb, "dist");
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub

        }
    };

    TextWatcher watchdist = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // TODO Auto-generated method stub
            Bundle bb = new Bundle();
            bb.putString(FFCSearchListDialog.EXTRA_APPEND_WHERE, "provcode = '"
                    + provcode.getSelectId() + "' AND " + "distcode = '"
                    + distcode.getSelectId() + "'");
            subdistcode.setDialog(getFFCActivity().getSupportFragmentManager(),
                    SubDistrictListDialog.class, bb, "subdist");
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub

        }
    };

    private TextWatcher idcardWatcher = new TextWatcher() {

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
            if (!TextUtils.isEmpty(s) && TextUtils.isDigitsOnly(s)) {
                if (s.length() == 13) {
                    mCitizenID = s.toString();
                    getActivity().getSupportLoaderManager().restartLoader(2,
                            null, PersonDetailEditFragment.this);
                } else {
                    isUseableId = false;
                }
            }
        }
    };

    private static final String[] PROJECTION = new String[]{
            Person.CITIZEN_ID, Person.SEX, Person.PRENAME, Person.FIRST_NAME,
            Person.LAST_NAME, Person.BIRTH, Person.BLOOD_GROUP,
            Person.BLOOD_RH, Person.RELIGION, Person.NATION, Person.HCODE,
            Person.ALLERGIC, Person.ORIGIN, Person.INCOME, Person.TEL,
            Person.EDUCATION, Person.OCCUPA, Person.ADDR_NO, Person.ADDR_MU,
            Person.ADDR_ROAD, Person.ADDR_SUBDIST, Person.ADDR_DIST,
            Person.ADDR_PROVICE, Person.POSTCODE};

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cl;
        String selection;
        String[] selectionArgs;
        switch (arg0) {
            case 0:
                selection = "pid=? AND pcucodeperson=?";
                selectionArgs = new String[]{getPID(), getPcucodePerson()};
                cl = new CursorLoader(getActivity(), Person.CONTENT_URI,
                        PROJECTION, selection, selectionArgs,
                        Person.DEFAULT_SORTING);
                return cl;
            case 1:
                cl = new CursorLoader(getActivity(), Person.CONTENT_URI,
                        new String[]{Person.MAX_PID}, null, null, "pid");
                return cl;
            case 2:
                cl = new CursorLoader(getActivity(), Person.CONTENT_URI,
                        new String[]{Person.CITIZEN_ID, Person.PID}, "idcard='"
                        + mCitizenID + "'", null, Person.DEFAULT_SORTING);
                return cl;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        switch (arg0.getId()) {
            case 0:
                if (c.moveToFirst()) {
                    citizenId.setText(c.getString(0));

                    int id = (c.getInt(1) == 1) ? R.id.male : R.id.female;
                    RadioButton btn = (RadioButton) sex.findViewById(id);
                    btn.setChecked(true);

                    prename.setSelection(c.getString(2));
                    fname.setText(c.getString(3));
                    lname.setText(c.getString(4));

                    String birth = (c.getString(5));
                    if (!TextUtils.isEmpty(birth)) {
                        Date b = Date.newInstance(birth);
                        birthday.updateDate(b);
                    }

                    bloodType.setSelection(c.getString(6));
                    bloodRh.setSelection(c.getString(7));
                    religion.setSelection(c.getString(8));
                    nation.setSelectionById(c.getString(9));
                    house.setSelectionById(c.getLong(10));
                    allergic.setText(c.getString(11));
                    origin.setSelectionById(c.getString(12));

                    String sIncome = c.getString(13);
                    if (!TextUtils.isEmpty(sIncome))
                        income.setText(sIncome);

                    tel.setText(c.getString(14));

                    education.setSelection(c.getString(15));
                    occupa.setSelectionById(c.getString(16));

                    hno.setText(c.getString(17));
                    mu.setText(c.getString(18));
                    road.setText(c.getString(19));

                    if (!TextUtils.isEmpty(c.getString(21))
                            && !TextUtils.isEmpty(c.getString(22))
                            && !TextUtils.isEmpty(c.getString(20))) {

                        provcode.setSelectionById(c.getString(22));
                        distcode.setSelectionById(c.getString(21),
                                "distcode =? AND provcode ='" + c.getString(22)
                                        + "'");
                        subdistcode.setSelectionById(c.getString(20),
                                "subdistcode=? AND distcode='" + c.getString(21)
                                        + "' AND provcode='" + c.getString(22)
                                        + "'");

                        postcode.setText(c.getString(23));
                    }

                }
                break;
            case 1:
                if (c.moveToFirst()) {
                    newPid = c.getInt(0) + 1;
                    getFFCActivity().getSupportActionBar().setSubtitle(
                            "pid #" + newPid);

                    nation.setSelectionById("99");
                    origin.setSelectionById("99");
                    bloodType.setSelection("O");
                    religion.setSelection("01");

                }
                break;
            case 2:
                if (c.moveToFirst()) {
                    if (!TextUtils.isEmpty(getPID())
                            && c.getString(1).equals(getPID())) {
                        isUseableId = true;
                        isCurrentId = true;
                    } else {
                        isUseableId = false;
                        isCurrentId = false;
                    }
                } else {
                    isUseableId = true;
                    isCurrentId = false;
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

    @Override
    public boolean onSave(EditTransaction et) {
        String id = citizenId.getText().toString();
        if (!TextUtils.isEmpty(id)) {
            if (!ThaiCitizenID.Validate(id) && !isCurrentId) {
                et.showErrorMessage(citizenId, "Invalid checksum citizen id");
                return false;
            } else if (!isUseableId) {
                et.showErrorMessage(citizenId, "Duplicate Id");
                return false;
            }
            et.retrieveData(Person.CITIZEN_ID, citizenId, true, "\\d{13}",
                    "Invalid citizen id Lenght");
        } else {
            if (newPid > 0) {
                String cid = "";
                String pid = "" + newPid;
                int loop = (13 - pid.length());
                for (int i = 1; i <= loop; i++)
                    cid += "0";
                cid += newPid;
                et.getContentValues().put(Person.CITIZEN_ID, cid);
            }
        }

        et.retrieveData(Person.FIRST_NAME, fname, false, null, null);
        et.retrieveData(Person.LAST_NAME, lname, false, null, null);
        et.retrieveData(Person.BIRTH, birthday, null,
                Date.newInstance(DateTime.getCurrentDate()),
                "can't create person of the future");
        et.retrieveData(Person.NATION, nation, false, null,
                "please select one nation");
        et.retrieveData(Person.ORIGIN, origin, false, null,
                "please select one origin nation");
        et.retrieveData(Person.OCCUPA, occupa, false, null,
                "please select one occupa");
        et.retrieveData(Person.INCOME, income, true, 0.00f, Float.MAX_VALUE,
                "Is that to much income");
        et.retrieveData(Person.TEL, tel, true, "\\d{9,12}",
                "tel number out of lenght");
        et.retrieveData(Person.ALLERGIC, allergic, true, null, null);

        et.retrieveData(Person.ADDR_NO, hno, false, null,
                "please insert house number");
        et.retrieveData(Person.ADDR_MU, mu, false, null, null);
        et.retrieveData(Person.ADDR_ROAD, road, true, null, null);
        et.retrieveData(Person.ADDR_SUBDIST, subdistcode, false, null,
                "please select sub-distict");
        et.retrieveData(Person.ADDR_DIST, distcode, false, null,
                "please select distict");
        et.retrieveData(Person.ADDR_PROVICE, provcode, false, null,
                "please select Provice");
        et.retrieveData(Person.POSTCODE, postcode, false, "\\d{5}",
                "please insert 5 digit");

        ContentValues cv = et.getContentValues();

        RadioButton maleRadio = (RadioButton) sex.findViewById(R.id.male);
        cv.put(Person.SEX, maleRadio.isChecked() ? 1 : 2);
        cv.put(Person.PRENAME, prename.getSelectionId());
        cv.put(Person.BLOOD_GROUP, bloodType.getSelectionId());
        cv.put(Person.BLOOD_RH, bloodRh.getSelectionId());
        cv.put(Person.RELIGION, religion.getSelectionId());
        cv.put(Person.EDUCATION, education.getSelectionId());
        cv.put(Person.HCODE, house.getSelectedItemId());
        cv.put(Person._DATEUPDATE, DateTime.getCurrentDateTime());

        if (newPid > 0) {
            cv.put(Person.PID, newPid);
            cv.put(Person.PCUPERSONCODE, getFFCActivity().getPcuCode());
        }

        return et.canCommit();
    }

    private void getDefaultHouseDetail(String hcode) {
        // TODO get default house parameter

        String selection = null;
        String[] selectionArgs = null;

        if (!TextUtils.isEmpty(hcode)) {
            selection = "hcode = ? AND provcodemoi IS NOT NULL";
            selectionArgs = new String[]{mHcode};
        }

        Cursor c = getFFCActivity().getContentResolver()
                .query(Person.CONTENT_URI,
                        new String[]{Person.ADDR_NO, Person.ADDR_MU,
                                Person.ADDR_ROAD, Person.ADDR_SUBDIST,
                                Person.ADDR_DIST, Person.ADDR_PROVICE,
                                Person.POSTCODE}, selection, selectionArgs,
                        Person.DEFAULT_SORTING);

        if (c.moveToFirst()) {

            hno.setText(c.getString(0));
            mu.setText(c.getString(1));
            road.setText(c.getString(2));
            System.out.println("Prov:" + c.getString(5) + " Dist:"
                    + c.getString(4) + " S.Dist:" + c.getString(3));

            provcode.setSelectionById(c.getString(5));
            distcode.setSelectionById(c.getString(4),
                    "distcode =? AND provcode ='" + c.getString(5) + "'");
            subdistcode.setSelectionById(c.getString(3),
                    "subdistcode=? AND distcode='" + c.getString(4)
                            + "' AND provcode='" + c.getShort(5) + "'");
            postcode.setText(c.getString(6));
        }
    }
}
