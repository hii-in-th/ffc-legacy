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


import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import th.in.ffc.R;
import th.in.ffc.app.FFCSearchListDialog;
import th.in.ffc.code.VaccineListDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.CodeProvider.Drug;
import th.in.ffc.provider.PersonProvider.VisitEpi;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.Log;
import th.in.ffc.util.StringArrayUtils;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.widget.SearchableButton;

import java.util.ArrayList;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector plus
 */
public class VisitEpiActivity extends VisitActivity implements LoaderCallbacks<Cursor> {

    ProgressBar mProgress;
    TextView mEmpty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.visit_epi_activity);

        mProgress = (ProgressBar) findViewById(android.R.id.progress);
        mEmpty = (TextView) findViewById(R.id.text);

        getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_add_activity, menu);

        MenuItem item = menu.add(0, R.string.appoint, 2, R.string.appoint);
        item.setIcon(R.drawable.ic_action_epi_appoint);
        item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                String tag = DateTime.getCurrentTime();
                VaccineFragment f = VaccineFragment.getInstance(Action.INSERT, null, tag);
                addEditFragment(R.id.content, f, tag);
                break;
            case R.id.save:
                doSave();
                break;
            case R.string.appoint:
                Intent appoint = new Intent(this, VisitEpiAppointActivity.class);
                VisitActivity.startVisitActivity(this, appoint, getVisitNo(),
                        getPid(), getPcucodePerson());
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void doSave() {
        boolean finishable = true;

        for (String key : getDeleteList()) {
            Uri uri = VisitEpi.getContentUri(getVisitNo(), key);

            ContentResolver cr = getContentResolver();
            cr.delete(uri, null, null);
        }

        ArrayList<String> codeList = new ArrayList<String>();
        for (EditFragment f : getEditFragmentList()) {
            EditTransaction et = beginTransaction();


            if (!f.onSave(et)) {
                finishable = false;
                break;
            }

            String key = et.getContentValues().getAsString(VisitEpi.VACCINE_CODE);
            if (StringArrayUtils.indexOf(codeList, key) >= 0) {
                finishable = false;
                Toast.makeText(this, R.string.duplicate_drug,
                        Toast.LENGTH_SHORT).show();
                break;
            } else
                codeList.add(key);

            et.getContentValues().put(VisitEpi._PCUCODE, getPcuCode());
            et.getContentValues().put(VisitEpi._VISITNO, getVisitNo());
            et.getContentValues().put(VisitEpi._PCUCODEPERSON, getPcucodePerson());
            et.getContentValues().put(VisitEpi._PID, getPid());
            et.getContentValues().put(VisitEpi.DATE_EPI, DateTime.getCurrentDate());
            et.getContentValues().put(VisitEpi._DATEUPDATE, DateTime.getCurrentDateTime());

            if (f.action.equals(Action.INSERT)) {

                Uri uri = et.commit(VisitEpi.CONTENT_URI);

                f.action = Action.EDIT;
                f.key = key;

                Log.d(TAG, "insert " + uri.toString());

            } else if (f.action.equals(Action.EDIT)) {

                Uri uri = VisitEpi.getContentUri(getVisitNo(), f.key);
                int update = et.commit(uri, null, null);

                Log.d(TAG, "update epi=" + update);
            }
        }

        if (finishable) {
            this.finish();
            setResult(RESULT_OK);
        }
    }


    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    public static final String[] projection = new String[]{
            VisitEpi.VACCINE_CODE,
            VisitEpi.LOT_NO,
            VisitEpi.DATE_VACCINE_EXPIRE,
    };

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        mProgress.setVisibility(View.VISIBLE);
        CursorLoader cl = new CursorLoader(this, VisitEpi.getContentUri(getVisitNo()), projection, null,
                null, VisitEpi.DEFAULT_SORTING);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        mProgress.setVisibility(View.GONE);
        final Cursor c = cursor;
        if (c.moveToFirst()) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    do {

                        String key = c.getString(0);
                        String lotno = c.getString(1);
                        String dateExpire = c.getString(2);
                        VaccineFragment f = VaccineFragment.getInstance(Action.EDIT, key, lotno, dateExpire, key);
                        addEditFragment(f, key);

                    } while (c.moveToNext());
                }
            });
        } else {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String tag = DateTime.getCurrentTime();
                    VaccineFragment f = VaccineFragment.getInstance(Action.INSERT, null, tag);
                    addEditFragment(R.id.content, f, tag);
                }
            });

        }
    }

    private Handler mHandler = new Handler();


    public static class VaccineFragment extends EditFragment {

        private SearchableButton mVaccine;
        private ImageButton mClose;
        private EditText mLotNo;
        private CheckBox mExpireCheck;
        private ThaiDatePicker mExpireDate;

        public static VaccineFragment getInstance(String action, String key, String tag) {

            return getInstance(action, key, null, null, tag);
        }

        public static VaccineFragment getInstance(String action, String key, String lotNo,
                                                  String expireDate, String tag) {
            VaccineFragment f = new VaccineFragment();

            Bundle args = new Bundle(f.getBaseArguments(action, tag, key));
            args.putString(Drug.LOT_NO, lotNo);
            args.putString(Drug.DATE_EXPIRE, expireDate);
            f.setArguments(args);

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.visit_epi_fragment, container, false);
            mVaccine = (SearchableButton) view.findViewById(R.id.code);
            mVaccine.setOnItemSelectListener(new SearchableButton.OnItemSelectListener() {
                private String[] PROJ = new String[]{
                        Drug.LOT_NO,
                        Drug.DATE_EXPIRE,
                };

                @Override
                public void onItemSelect(String id) {
                    if (!TextUtils.isEmpty(id)) {
                        Uri drugUri = Uri.withAppendedPath(Drug.CONTENT_URI, id);
                        Cursor c = getActivity().getContentResolver().query(drugUri, PROJ, null, null,
                                Drug.DEFAULT_SORTING);
                        if (c.moveToFirst()) {
                            mLotNo.setText(c.getString(0));
                            Date expire = Date.newInstance(c.getString(1));
                            if (expire != null) {
                                mExpireDate.updateDate(expire);
                                mExpireCheck.setChecked(true);
                            } else {
                                mExpireCheck.setChecked(false);
                                //mExpireDate.setVisibility(View.GONE);
                                mExpireDate.updateDate(Date.newInstance(DateTime.getCurrentDate()));
                            }
                        }
                    }
                }
            });

            mClose = (ImageButton) view.findViewById(R.id.deleted);
            mLotNo = (EditText) view.findViewById(R.id.text);
            mExpireCheck = (CheckBox) view.findViewById(R.id.check);
            mExpireCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mExpireDate.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
            });
            mExpireDate = (ThaiDatePicker) view.findViewById(R.id.date);
            mExpireDate.updateDate(Date.newInstance(DateTime.getCurrentDate()));

            mExpireCheck.setChecked(false);
            mExpireDate.setVisibility(View.GONE);
            setAsRemoveButton(mClose);

            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            Bundle vaccineBundle = null;
            if (getActivity().getIntent().getAction().equals(Action.EPI_PREGNANCY)) {
                vaccineBundle = new Bundle();
                vaccineBundle.putString(FFCSearchListDialog.EXTRA_DEFAULT_WHERE,
                        "drugcode LIKE '%ANC%'");
            }
            if (getActivity().getIntent().getAction().equals(Action.EPI_CHRILDEN)) {
                vaccineBundle = new Bundle();
                vaccineBundle.putString(FFCSearchListDialog.EXTRA_DEFAULT_WHERE,
                        "drugname LIKE '%dt%'");
            }
            mVaccine.setDialog(getActivity().getSupportFragmentManager(),
                    VaccineListDialog.class, vaccineBundle, tag + "_vag");

            if (!TextUtils.isEmpty(key)) {
                mVaccine.setSelectionById(key);
            }

            Bundle args = getArguments();
            String lotno = args.getString(Drug.LOT_NO);
            String dateExpire = args.getString(Drug.DATE_EXPIRE);

            if (!TextUtils.isEmpty(lotno)) {
                mLotNo.setText(lotno);
            }

            if (!TextUtils.isEmpty(dateExpire) && dateExpire.length() >= 8) {
                mExpireCheck.setChecked(true);
                mExpireDate.setVisibility(View.VISIBLE);
                Date expire = Date.newInstance(dateExpire);
                mExpireDate.updateDate(expire);
            }
        }

        @Override
        public boolean onSave(EditTransaction et) {
            et.retrieveData(VisitEpi.VACCINE_CODE, mVaccine, false, null, null);
            et.retrieveData(VisitEpi.LOT_NO, mLotNo, true, null, null);
            if (mExpireCheck.isChecked()) {
                Date cur = Date.newInstance(DateTime.getCurrentDate());
                et.retrieveData(VisitEpi.DATE_VACCINE_EXPIRE, mExpireDate, cur,
                        null, "Vaccine already expire?");
            }
            return et.canCommit();
        }

    }

}
