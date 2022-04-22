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
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;

import th.in.ffc.R;
import th.in.ffc.code.DrugListDialog;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.CodeProvider.Drug;
import th.in.ffc.provider.PersonProvider.VisitDrug;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.Log;
import th.in.ffc.widget.InstantAutoComplete;
import th.in.ffc.widget.SearchableButton;
import th.in.ffc.widget.SearchableButton.OnItemSelectListener;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class VisitDrugActivity extends VisitActivity implements
        LoaderCallbacks<Cursor> {

    public static final String[] PROJECTION = new String[]{VisitDrug.CODE,
            VisitDrug.DOSE, VisitDrug.COST, VisitDrug.REAL, VisitDrug.UNIT,};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.visit_drug_activity);
        setSupportProgressBarIndeterminateVisibility(false);

        if (savedInstanceState == null)
            getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_add_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                String tag = DateTime.getCurrentTime();
                DrugFragment f = DrugFragment.getNewInstance(Action.INSERT, null,
                        null, 0, 0, 0, tag);
                addEditFragment(f, tag);
                break;
            case R.id.save:
                setSupportProgressBarIndeterminateVisibility(true);
                doSave();
                setSupportProgressBarIndeterminateVisibility(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doSave() {
        boolean finishable = true;
        for (String code : getDeleteList()) {

            Uri uri = VisitDrug.getContentUriId(Long.parseLong(getVisitNo()),
                    code);

            ContentResolver cr = getContentResolver();
            int count = cr.delete(uri, null, null);
            Log.d(TAG, "deleted drug count=" + count + " uri=" + uri.toString());
        }

        ArrayList<String> codeList = new ArrayList<String>();
        for (String tag : getEditList()) {
            DrugFragment f = (DrugFragment) getSupportFragmentManager()
                    .findFragmentByTag(tag);
            if (f == null)
                continue;

            EditTransaction et = beginTransaction();
            f.onSave(et);

            if (!et.canCommit()) {
                finishable = false;
                break;
            }

            String code = et.getContentValues().getAsString(VisitDrug.CODE);
            if (isAddedCode(codeList, code)) {
                finishable = false;
                Toast.makeText(this, R.string.duplicate_drug,
                        Toast.LENGTH_SHORT).show();
                break;
            } else
                codeList.add(code);

            et.getContentValues().put(VisitDrug.NO, getVisitNo());
            et.getContentValues().put(VisitDrug._PCUCODE, getPcuCode());
            et.getContentValues().put(VisitDrug._DATEUPDATE,
                    DateTime.getCurrentDateTime());
            et.getContentValues().put(VisitDrug.DOCTOR1, getUser());

            if (f.action.equals(Action.INSERT)) {

                Uri uri = et.commit(VisitDrug.CONTENT_URI);

                f.action = Action.EDIT;
                f.key = code;

                Log.d(TAG, "insert drug=" + uri.toString());

                Answers.getInstance().logCustom(new CustomEvent("Drug")
                    .putCustomAttribute("cost", et.getContentValues().getAsFloat(VisitDrug.COST))
                    .putCustomAttribute("user", getUser())
                    .putCustomAttribute("pcu", getPcuCode())
                    .putCustomAttribute("code", code));
            } else if (f.action.equals(Action.EDIT)) {

                Uri updateUri = VisitDrug.getContentUriId(
                        Long.parseLong(getVisitNo()), f.key);
                int update = et.commit(updateUri, null, null);
                Log.d(TAG,
                        "update drug=" + update + " uri="
                                + updateUri.toString());
            }

        }

        if (finishable)
            this.finish();

    }

    public boolean isAddedCode(ArrayList<String> codeList, String code) {
        for (String c : codeList) {

            if (code.equals(c))
                return true;
        }
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        setSupportProgressBarIndeterminateVisibility(true);
        CursorLoader cl = new CursorLoader(this, VisitDrug.CONTENT_URI,
                PROJECTION, "visitno=" + getVisitNo() + " AND  dentalcode is null", null,
                VisitDrug.DEFAULT_SORTING);
        return cl;
    }

    Handler mHandler = new Handler();

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        final Cursor c = arg1;
        setSupportProgressBarIndeterminateVisibility(false);
        if (c.moveToFirst()) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    generateDrugFragment(c);
                }
            });
        } else {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String tag = DateTime.getCurrentDateTime();
                    DrugFragment f = DrugFragment.getNewInstance(Action.INSERT,
                            null, null, 0, 0, 0, tag);
                    addEditFragment(f, tag);

                }
            });
        }
    }

    private void generateDrugFragment(Cursor c) {
        String action = Action.EDIT;
        do {
            String code = c.getString(c.getColumnIndex(VisitDrug.CODE));
            String dose = c.getString(c.getColumnIndex(VisitDrug.DOSE));
            float cost = c.getFloat(c.getColumnIndex(VisitDrug.COST));
            float real = c.getFloat(c.getColumnIndex(VisitDrug.REAL));
            int unit = c.getInt(c.getColumnIndex(VisitDrug.UNIT));

            DrugFragment f = DrugFragment.getNewInstance(action, code, dose,
                    cost, real, unit, code);
            // addDrugFragment(f, code);
            addEditFragment(f, code);
        } while (c.moveToNext());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

    /**
     * Fragment class for handle each drug that user choosen in
     * VisitDrugActivity
     *
     * @author Piruin Panichphol
     * @version 1.0
     * @since 1.0
     */
    public static class DrugFragment extends EditFragment
            implements OnItemSelectListener {

        private SearchableButton mDrug;
        private AutoCompleteTextView mDose;
        private EditText mCost;
        private EditText mUnit;
        private ImageButton mClose;

        String dose;
        float cost;
        float real;
        int unit;

        public static DrugFragment getNewInstance(String action, String code,
                                                  String dose, float cost, float real, int unit, String tag) {
            DrugFragment f = new DrugFragment();

            Bundle args = new Bundle(f.getBaseArguments(action, tag, code));

            args.putString("dose", dose);
            args.putFloat("cost", cost);
            args.putFloat("real", real);
            args.putInt("unit", unit);

            f.setArguments(args);
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.visit_drug_fragmet, container,
                    false);
            mDrug = (SearchableButton) v.findViewById(R.id.code);
            mDrug.setOnItemSelectListener(this);
            mDose = (AutoCompleteTextView) v.findViewById(R.id.text);
            mCost = (EditText) v.findViewById(R.id.view1);
            mUnit = (EditText) v.findViewById(R.id.view2);
            mClose = (ImageButton) v.findViewById(R.id.deleted);
            setAsRemoveButton(mClose);
            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            Bundle args = getArguments();
            if (args == null)
                throw new IllegalStateException(
                        "DrugFragment must have arguments");

            dose = args.getString("dose");
            cost = args.getFloat("cost");
            real = args.getFloat("real");
            unit = args.getInt("unit");
            doInitialView(savedInstanceState);

        }

        public void doInitialView(Bundle saveInstanceState) {
            mDrug.setDialog(getActivity().getSupportFragmentManager(),
                    DrugListDialog.class, "drug_" + tag);
            if (saveInstanceState == null) {
                if (!TextUtils.isEmpty(key))
                    mDrug.setSelectionById(key);

                if (!TextUtils.isEmpty(dose))
                    mDose.setText(dose);

                mCost.setText("" + cost);
                mUnit.setText("" + unit);
            }
        }

        @Override
        public boolean onSave(EditTransaction et) {
            et.retrieveData(VisitDrug.CODE, mDrug, false, null,
                    "please select drug!");
            // et.retrieveData(VisitDrug.DOSE, mDose, true, null, null);
            et.retrieveData(VisitDrug.COST, mCost, false, 0.0f, 1000.0f, null);
            et.retrieveData(VisitDrug.UNIT, mUnit, false, 1, 999,
                    "drug unit out of range");

            et.getContentValues().put(VisitDrug.DOSE,
                    mDose.getText().toString());
            return true;
        }

        private static final String[] DRUG_PROJ = new String[]{Drug.COST,
                Drug.SELL, Drug.AMOUNT_DEFAULT};

        @Override
        public void onItemSelect(String id) {
            Uri uri = Uri.withAppendedPath(Drug.CONTENT_URI, id);
            Cursor c = getActivity().getContentResolver().query(uri, DRUG_PROJ,
                    null, null, Drug.CODE);
            if (c.moveToFirst()) {
                real = c.getFloat(0);
                mCost.setText(c.getString(1));
                int unit = c.getInt(2);
                mUnit.setText("" + (unit < 1 ? 1 : unit));
            }

            Uri uri_dose = Uri.withAppendedPath(Drug.CONTENT_URI, "dose");
            Cursor dose = getActivity().getContentResolver().query(uri_dose,
                    new String[]{Drug.DOSE}, "cdrug.drugcode=?",
                    new String[]{id}, "sysdrugdose.doseno");
            if (dose.moveToFirst()) {
                final ArrayAdapter<String> adapter = InstantAutoComplete
                        .getAdapter(getActivity(), dose, 0);
                mDose.setAdapter(adapter);
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity(),android.R.style.Theme_Material_Light_Dialog_Alert);
                builder.setAdapter(adapter,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mDose.setText(adapter.getItem(which));
                            }
                        });
                builder.create().show();
            }
        }
    }

}
