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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;

import th.in.ffc.R;
import th.in.ffc.code.ClinicListDialog;
import th.in.ffc.code.DiagnosisListDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.provider.CodeProvider.Diagnosis;
import th.in.ffc.provider.PersonProvider.FFC506RADIUS;
import th.in.ffc.provider.PersonProvider.Visit506_Person;
import th.in.ffc.provider.PersonProvider.VisitDiag;
import th.in.ffc.provider.PersonProvider.VisitDiag506address;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.widget.ArrayFormatSpinner;
import th.in.ffc.widget.SearchableButton;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since 1.0
 */
public class VisitDiagActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    private final String RADIUS_DEFAULT = "500";
    private final String COLORCODE_DEFAULT = "#ffc500";
    private final String LEVEL_DEFALUT = "0";

    public static final String[] PROJECTION = new String[]{VisitDiag.CODE,
            VisitDiag.TYPE, VisitDiag.CONTINUE, VisitDiag.CLINIC,
            VisitDiag.APPOINT_DATE, VisitDiag.APPOINT_TYPE};

    private SearchableButton mClinic;
    private CheckBox mCheck;
    private ThaiDatePicker mAppoint;
    private Spinner mAppointType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visit_diag_activity);
        setSupportProgressBarIndeterminateVisibility(false);
        doSetupAppoint(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("date", mAppoint.getDate().toString());
    }

    private void doSetupAppoint(Bundle saveInstanceState) {
        mClinic = (SearchableButton) findViewById(R.id.clinic);
        mClinic.setDialog(getSupportFragmentManager(), ClinicListDialog.class,
                "clinic");

        mAppoint = (ThaiDatePicker) findViewById(R.id.date);
        mAppointType = (Spinner) findViewById(R.id.type);
        mAppointType.setAdapter(new ArrayAdapter<String>(this,
                R.layout.default_spinner_item, getResources().getStringArray(
                R.array.diag_appoint_type)));

        mCheck = (CheckBox) findViewById(R.id.check);
        mCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mAppoint.setVisibility(mCheck.isChecked() ? View.VISIBLE
                        : View.GONE);
                mAppointType.setVisibility(mCheck.isChecked() ? View.VISIBLE
                        : View.GONE);
            }
        });

        if (saveInstanceState == null) {
            mClinic.setSelectionById("00000");

            Date date = Date.newInstance(DateTime.getCurrentDate());
            mAppoint.updateDate(date);
        } else {
            Date date = Date.newInstance(saveInstanceState.getString("date"));
            mAppoint.updateDate(date);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_activity, menu);
        String[] diagType = getResources().getStringArray(R.array.diag_DX);
        SubMenu sub = menu.addSubMenu(R.string.add);
        sub.add(0, 2, 0, diagType[2]);
        sub.add(0, 4, 0, diagType[4]);
        sub.add(0, 5, 0, diagType[5]);
        sub.setIcon(R.drawable.ic_action_add);
        sub.getItem().setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 2:
            case 4:
            case 5:
                addDiagFragment(Action.INSERT, item.getItemId(), null, null, null,
                        DateTime.getCurrentTime());
                break;
            case R.id.save:
                setSupportProgressBarIndeterminateVisibility(true);
                doSave();
                setSupportProgressBarIndeterminateVisibility(false);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void doSave() {

        boolean finishable = true;

        for (String code : getDeleteList()) {
            Uri uri = VisitDiag.getContentUriId(Long.parseLong(getVisitNo()),
                    code);
            ContentResolver cr = getContentResolver();
            int count = cr.delete(uri, null, null);
            Log.d(TAG, "deleted diag count=" + count + " uri=" + uri.toString());
        }

        ArrayList<String> codeList = new ArrayList<String>();
        boolean check = mCheck.isChecked();
        for (String tag : getEditList()) {
            Log.d(TAG, "tag=" + tag);
            DiagFragment f = (DiagFragment) getSupportFragmentManager()
                    .findFragmentByTag(tag);
            if (f != null) {
                EditTransaction et = beginTransaction();
                f.onSave(et);

                if (!et.canCommit()) {
                    finishable = false;
                    break;
                }
                System.out.println("do add code");
                String code = et.getContentValues().getAsString(VisitDiag.CODE);
                if (isAddedCode(codeList, code)) {
                    finishable = false;
                    Toast.makeText(this, R.string.duplicate_drug, Toast.LENGTH_SHORT).show();
                    break;
                } else
                    codeList.add(code);

                ContentValues cv = et.getContentValues();
                cv.put(VisitDiag.NO, getVisitNo());
                cv.put(VisitDiag.CLINIC, mClinic.getSelectId());
                cv.put(VisitDiag.PCUCODE, getPcuCode());
                cv.put(VisitDiag.DATEUPDATE, DateTime.getCurrentDateTime());
                cv.put(VisitDiag.DOCTOR, getUser());
                if (check) {
                    cv.put(VisitDiag.APPOINT_DATE, mAppoint.getDate()
                            .toString());
                    cv.put(VisitDiag.APPOINT_TYPE,
                            mAppointType.getSelectedItemPosition());
                    check = false;
                }

                String action = f.action;
                if (action.equals(Action.INSERT)) {
                    Uri insert = et.commit(VisitDiag.CONTENT_URI);
                    f.action = Action.EDIT;
                    f.key = code;
                    Log.d(TAG, "insert diag=" + insert.toString());

                    Answers.getInstance().logCustom(new CustomEvent("Diagnosis")
                        .putCustomAttribute("code", code)
                        .putCustomAttribute("type", cv.getAsString(VisitDiag.TYPE))
                        .putCustomAttribute("continue", f.conti ? "yes" : "no")
                        .putCustomAttribute("pcu", getPcuCode())
                        .putCustomAttribute("user", getUser()));
                } else if (action.equals(Action.EDIT)) {
                    Uri updateUri = VisitDiag.getContentUriId(
                            Long.parseLong(getVisitNo()), f.key);
                    int update = et.commit(updateUri, null, null);
                    Log.d(TAG, "update drug=" + update + " uri=" + updateUri.toString());
                }
            }
        }
        if (finishable) {
            checkVisit506();
        }
    }

    String pcucodeperson;
    String lat;
    String lng;
    String pid;
    String visitno;

    private void checkVisit506() {
        visitno = getVisitNo();
        Uri uriDiag506 = VisitDiag506address.CONTENT_URI;
        String[] projectionDiag506 = {"visitno"};
        ContentResolver contentResolverDiag506 = getContentResolver();
        String selectionDiag506 = "visitno =?";
        String[] selectionArgsDiag506 = {visitno};
        Cursor cDiag506 = contentResolverDiag506.query(uriDiag506, projectionDiag506, selectionDiag506, selectionArgsDiag506, "visitno");
        if (cDiag506.moveToFirst()) {
            Uri uri = Visit506_Person.CONTENT_URI;
            String[] projection = {"pcucodeperson", "hcode", "visitno", "xgis", "ygis", "pid", "hno", "villno", "villname"};
            ContentResolver contentResolver = getContentResolver();
            String selection = "visit.visitno =?";
            String[] selectionArgs = {visitno};
            Cursor c = contentResolver.query(uri, projection, selection, selectionArgs, "visit.visitno");
            if (c.moveToFirst()) {
                pcucodeperson = c.getString(c.getColumnIndex("pcucodeperson"));
                lat = c.getString(c.getColumnIndex("ygis"));
                lng = c.getString(c.getColumnIndex("xgis"));
                pid = c.getString(c.getColumnIndex("pid"));
                if (TextUtils.isEmpty(lng) && TextUtils.isEmpty(lat)) {
                    insertRadiusDetail();
                    String title = "��辺���˹觢ͧ��ҹ��ѧ���س��ͧ��÷����������˹觢ͧ��ҹ��ѧ����������";
                    Toast.makeText(getApplicationContext(), title, Toast.LENGTH_SHORT).show();
                    dialogAskGoHouse(title, false, c);
                } else {
                    insertRadiusDetail();
                    String title = "������ա�õ�Ǩ���ä�кҴ �س��ͧ������仴ٵ��˹觺�ҹ����������";
                    Toast.makeText(getApplicationContext(), title, Toast.LENGTH_SHORT).show();
                    dialogAskGoHouse(title, true, c);
                }
            }
        } else {
            this.finish();
        }
    }

    private void insertRadiusDetail() {
        String where = "visitno=?";
        String[] selectionArgs = {visitno};
        ContentValues conValues = new ContentValues();
        conValues.put("visitno", visitno);
        conValues.put("radius", RADIUS_DEFAULT);
        conValues.put("colorcode", COLORCODE_DEFAULT);
        conValues.put("level", LEVEL_DEFALUT);
        ContentResolver cr = getContentResolver();
        int update = cr.update(FFC506RADIUS.CONTENT_URI, conValues, where, selectionArgs);
        if (update < 1) {
            cr.insert(FFC506RADIUS.CONTENT_URI, conValues);
        }
    }

    private void dialogAskGoHouse(String title, final boolean viewHouse, final Cursor c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (viewHouse) {
                    Double tempLat = Double.parseDouble(c.getString(c.getColumnIndex("ygis")));
                    Double tempLng = Double.parseDouble(c.getString(c.getColumnIndex("xgis")));
                    Intent a = new Intent(Action.VIEW);
                    a.addCategory(Category.MAP506);
                    a.putExtra("hcode", c.getString(c.getColumnIndex("hcode")));
                    a.putExtra("pid", c.getString(c.getColumnIndex("pid")));
                    a.putExtra("pcucodeperson", c.getString(c.getColumnIndex("pcucodeperson")));
                    a.putExtra("lat", tempLat);
                    a.putExtra("lng", tempLng);
                    startActivity(a);
                    finish();
                } else {
                    Intent a = new Intent(Action.INSERT);
                    a.addCategory(Category.ADD_EDITPOSITION);
                    a.putExtra("pid", c.getString(c.getColumnIndex("pid")));
                    a.putExtra("hno", c.getString(c.getColumnIndex("hno")));
                    a.putExtra("villno", c.getString(c.getColumnIndex("villno")));
                    a.putExtra("villname", c.getString(c.getColumnIndex("villname")));
                    a.putExtra("pcucodeperson", c.getString(c.getColumnIndex("pcucodeperson")));
                    a.putExtra("hcode", c.getString(c.getColumnIndex("hcode")));
                    a.putExtra("visitno", c.getString(c.getColumnIndex("visitno")));
                    startActivity(a);
                    finish();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                //onBackPresseds();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public boolean isAddedCode(ArrayList<String> codeList, String code) {
        for (String c : codeList) {
            if (code.equals(c))
                return true;
        }
        return false;
    }

    public void addDiagFragment(String Action, int type, String code,
                                String appointDate, String appointType, String tag) {

        addEditFragment(DiagFragment.newInstance(Action, type, code, false,
                appointDate, appointType, tag), tag);
    }

    private void doGenerateDiag(Cursor c) {
        int type = c.getInt(c.getColumnIndex(VisitDiag.TYPE));
        String tag = c.getString(c.getColumnIndex(VisitDiag.CODE));
        boolean conti = (c.getInt(c.getColumnIndex(VisitDiag.CONTINUE)) == 1) ? true
                : false;
        String appointDate = c.getString(c
                .getColumnIndex(VisitDiag.APPOINT_DATE));
        String appointType = c.getString(c
                .getColumnIndex(VisitDiag.APPOINT_TYPE));

        addEditFragment(DiagFragment.newInstance(Action.EDIT, type, tag, conti,
                appointDate, appointType, tag), tag);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        setSupportProgressBarIndeterminateVisibility(true);
        CursorLoader cl = new CursorLoader(this, VisitDiag.CONTENT_URI,
                PROJECTION, "visitno=" + getVisitNo(), null,
                VisitDiag.DEFAULT_SORTING);
        return cl;
    }

    public Handler mHandler = new Handler();

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        setSupportProgressBarIndeterminateVisibility(false);
        if (arg1.moveToFirst()) {
            final Cursor c = arg1;
            String clinic = c.getString(c.getColumnIndex(VisitDiag.CLINIC));
            mClinic.setSelectionById(clinic);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    do {
                        doGenerateDiag(c);
                    } while (c.moveToNext());
                }
            });
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    addDiagFragment(Action.INSERT, DiagFragment.PRINCIPLE_DX,
                            null, null, null, "DX");
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

    }

    public static class DiagFragment extends EditFragment {

        public static final int PRINCIPLE_DX = 1;
        public static final int CO_MORBIDITY = 2;
        public static final int COMPLICATION = 3;
        public static final int OTHER = 4;
        public static final int EXTERNAL_CAUSE = 5;

        int type;
        boolean conti;
        String appointDate;
        String appointType;

        private SearchableButton mDiag;
        private CheckBox mContinue;
        private TextView mText;
        private ImageButton mAppointBtn;
        private ImageButton mClose;
        private ThaiDatePicker mAppoint;
        private ArrayFormatSpinner mAppointType;

        private String lastKey;

        public static DiagFragment newInstance(String action, int type,
                                               String code, boolean conti, String appointDate,
                                               String appointType, String tag) {

            DiagFragment f = new DiagFragment();

            Bundle args = new Bundle(f.getBaseArguments(action, tag, code));

            args.putInt("type", type);
            args.putBoolean("conti", conti);
            args.putString("app_date", appointDate);
            args.putString("app_type", appointType);

            f.setArguments(args);

            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.visit_diag_fragment, container,
                    false);


            mDiag = (SearchableButton) v.findViewById(R.id.code);
            mContinue = (CheckBox) v.findViewById(R.id.check);
            mText = (TextView) v.findViewById(R.id.text);
            mClose = (ImageButton) v.findViewById(R.id.deleted);
            setAsRemoveButton(mClose);

            mAppointBtn = (ImageButton) v.findViewById(R.id.appoint);
            mAppointBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int view = mAppoint.isShown() ? View.GONE : View.VISIBLE;
                    mAppoint.setVisibility(view);
                    mAppointType.setVisibility(view);
                }
            });
            mAppoint = (ThaiDatePicker) v.findViewById(R.id.date);
            Date date = Date.newInstance(DateTime.getCurrentDate());
            mAppoint.updateDate(date);

            mAppointType = (ArrayFormatSpinner) v.findViewById(R.id.type);
            mAppointType.setArray(R.array.diag_appoint_type);

            mContinue.setEnabled(false);

            mDiag.setOnItemSelectListener(new SearchableButton.OnItemSelectListener() {

                @Override
                public void onItemSelect(String id) {
                    doRemoveLastKey();

                    if (!conti
                            && doCheckIsThisCaseIsCode506Diseasee(id)
                            && !doCheckIsDiagThisCaseBefore(getVisitActivity()
                            .getVisitNo(), id)) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                getActivity());
                        builder.setMessage(
                                getResources().getString(R.string.dialog_isCon))
                                .setCancelable(false)
                                .setPositiveButton(
                                        getResources().getString(R.string.con),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                mContinue.setChecked(true);
                                            }
                                        })
                                .setNegativeButton(
                                        getResources().getString(
                                                R.string.discon),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int id) {
                                                // dialog.cancel();
                                                Intent intent = new Intent(
                                                        getActivity(),
                                                        VisitDiag506addressActivity.class);
                                                intent.putExtra(
                                                        VisitDiag506address.PCUCODE,
                                                        getVisitActivity()
                                                                .getPcuCode());
                                                intent.putExtra(
                                                        VisitDiag506address.VISITNO,
                                                        getVisitActivity()
                                                                .getVisitNo());
                                                intent.putExtra("pid",
                                                        getVisitActivity()
                                                                .getPid());
                                                intent.putExtra(
                                                        VisitDiag506address.DIAGCODE,
                                                        mDiag.getSelectId());
                                                intent.putExtra("tag", tag);
                                                // startActivityForResult(intent,
                                                // 333);
                                                startActivity(intent);
                                                lastKey = mDiag.getSelectId();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }

                }
            });

            return v;
        }

        private boolean doRemoveLastKey() {
            String id = mDiag.getSelectId();

            if (TextUtils.isEmpty(id))
                return false;

            String OldKey = TextUtils.isEmpty(lastKey) ? key : lastKey;
            if (TextUtils.isEmpty(OldKey))
                return false;

            if (!id.equals(OldKey)) {
                String vn = getVisitActivity().getVisitNo();
                conti = false;
                int row = getActivity().getContentResolver().delete(
                        VisitDiag506address.CONTENT_URI,
                        "visitno=? AND diagcode=?",
                        new String[]{vn, (TextUtils.isEmpty(lastKey) ? key : lastKey)});
                Log.d(TAG, "DELETE " + row);
                return true;
            }
            return false;
        }

        private Boolean doCheckIsDiagThisCaseBefore(String vn, String dc) {
            Boolean isDiag = false;

            Cursor c = getActivity().getContentResolver().query(
                    VisitDiag506address.CONTENT_URI,
                    new String[]{VisitDiag506address.VISITNO,
                            VisitDiag506address.DIAGCODE},
                    "visitno = ? AND diagcode=?", new String[]{vn, dc},
                    "visitno,diagcode desc");

            if (c.moveToFirst()) {
                isDiag = true;
            }
            return isDiag;
        }

        private String doGetCode506Diseasee(String id) {
            Cursor c = getActivity().getContentResolver().query(
                    Diagnosis.CONTENT_URI,
                    new String[]{Diagnosis.CODE, Diagnosis.CODE506},
                    "code506 is not null AND diseasecode = ?",
                    new String[]{id}, Diagnosis.DEFAULT_SORTING);

            if (c.moveToFirst())
                return c.getString(0);
            else
                return null;
        }

        private Boolean doCheckIsThisCaseIsCode506Diseasee(String id) {
            Boolean isCode506 = false;
            String code = doGetCode506Diseasee(id);

            if (!TextUtils.isEmpty(code))
                isCode506 = true;
            else
                isCode506 = false;

            return isCode506;
        }

        @Override
        protected void setAsRemoveButton(View view) {
            if (view != null)
                view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        String vn = getVisitActivity().getVisitNo();

                        if (getmRemoveListener() != null)
                            getmRemoveListener().onRemove(action, tag, key);
                        String diagcode = mDiag.getSelectId();

                        if (diagcode != null)
                            getActivity().getContentResolver().delete(
                                    VisitDiag506address.CONTENT_URI,
                                    "visitno =? AND diagcode =?",
                                    new String[]{vn, diagcode});
                    }
                });

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Bundle args = getArguments();

            type = args.getInt("type");
            conti = args.getBoolean("conti");
            appointDate = args.getString("app_date");
            appointType = args.getString("app_type");


            doInitializeView();

        }

        private void doInitializeView() {
            String[] dx = getResources().getStringArray(R.array.diag_DX);
            mText.setText(dx[type]);

            mDiag.setDialog(getActivity().getSupportFragmentManager(),
                    DiagnosisListDialog.class, "diag_" + tag);

            if (!TextUtils.isEmpty(this.key))
                mDiag.setSelectionById(this.key);

            mContinue.setChecked(conti);

            //mClose.setEnabled(type == PRINCIPLE_DX ? false : true);

            if (!TextUtils.isEmpty(appointDate)) {
                Date app = Date.newInstance(appointDate);
                mAppoint.updateDate(app);
                mAppoint.setVisibility(View.VISIBLE);
                mAppointType.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(appointType)) {
                mAppointType.setSelection(appointType);
            }
        }

        @Override
        public boolean onSave(EditTransaction et) {

            et.retrieveData(VisitDiag.CODE, mDiag, false, null, null);

            ContentValues cv = et.getContentValues();

            cv.put(VisitDiag.CONTINUE, mContinue.isChecked() ? 1 : 0);
            cv.put(VisitDiag.TYPE, "0" + type);

            if (mAppoint.isShown()) {
                Date current = Date.newInstance(DateTime.getCurrentDate());
                et.retrieveData(VisitDiag.APPOINT_DATE, mAppoint, current, null, "Invalid Appoint Date");
                cv.put(VisitDiag.APPOINT_TYPE, mAppointType.getSelectionId());
            }

            return et.canCommit();

        }

        public VisitActivity getVisitActivity() {
            return (VisitActivity) getActivity();
        }
    }

}
