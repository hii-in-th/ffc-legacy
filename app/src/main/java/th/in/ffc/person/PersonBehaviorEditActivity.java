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
import android.net.Uri;
import android.os.Bundle;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import th.in.ffc.R;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.PersonProvider.Behavior;
import th.in.ffc.util.DateTime;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector plus
 */
public class PersonBehaviorEditActivity extends PersonActivity implements LoaderCallbacks<Cursor> {

    Spinner mExercise;
    Spinner mWiskey;
    Spinner mCiga;
    Spinner mTonic;
    CheckBox mAccident;
    CheckBox mDrugBySelf;
    CheckBox mSweet;
    CheckBox mSalt;

    String mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.person_behavior_edit_activity);

        Log.d(TAG, String.format("pcucode=%s pic=%s", getPcucodePerson(), getPid()));
        doInitailizeView();


        if (savedInstanceState == null) {
            mAction = getIntent().getAction();
            if (!TextUtils.isEmpty(mAction) && mAction.equals(Action.EDIT))
                getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("action", mAction);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
            mAction = savedInstanceState.getString("action");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                doSave();
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doSave() {
        EditTransaction et = beginTransaction();
        ContentValues cv = et.getContentValues();
        cv.put(Behavior.EXERCISE, mExercise.getSelectedItemPosition());
        cv.put(Behavior.CIGA, mCiga.getSelectedItemPosition());
        cv.put(Behavior.WISKY, mWiskey.getSelectedItemPosition());
        cv.put(Behavior.TONIC, mTonic.getSelectedItemPosition());
        cv.put(Behavior.ACCIDENT, mAccident.isChecked() ? 1 : 0);
        cv.put(Behavior.DRUGBYSELF, mDrugBySelf.isChecked() ? 1 : 0);
        cv.put(Behavior.SALT, mSalt.isChecked() ? 1 : 0);
        cv.put(Behavior.SUGAR, mSweet.isChecked() ? 1 : 0);
        cv.put(Behavior.DATEUPDATE, DateTime.getCurrentDateTime());

        if (mAction.equals(Action.EDIT)) {
            int update = et.forceCommit(Behavior.CONTENT_URI, "pcucodeperson=? AND pid=?", new String[]{
                    getPcucodePerson(),
                    getPid(),
            });
            Log.d(TAG, "update=" + update);
        } else if (mAction.equals(Action.INSERT)) {
            String pcucodeperson = getPcucodePerson();
            String pid = getPid();
            cv.put(Behavior.PCUCODE, pcucodeperson);
            cv.put(Behavior.PID, pid);

            Uri insert = et.forceCommit(Behavior.CONTENT_URI);
            Log.d(TAG, "insert=" + insert.toString());
        }

        this.finish();
    }


    public void doInitailizeView() {
        mExercise = (Spinner) findViewById(R.id.answer1);
        mExercise.setAdapter(new ArrayAdapter<String>(this,
                R.layout.default_spinner_item,
                getResources().getStringArray(R.array.execise)));

        mCiga = (Spinner) findViewById(R.id.answer2);
        mCiga.setAdapter(new ArrayAdapter<String>(this,
                R.layout.default_spinner_item,
                getResources().getStringArray(R.array.ciga)));

        mWiskey = (Spinner) findViewById(R.id.answer3);
        mWiskey.setAdapter(new ArrayAdapter<String>(this,
                R.layout.default_spinner_item,
                getResources().getStringArray(R.array.wiskey)));

        mTonic = (Spinner) findViewById(R.id.answer4);
        mTonic.setAdapter(new ArrayAdapter<String>(this,
                R.layout.default_spinner_item,
                getResources().getStringArray(R.array.tonic)));

        mAccident = (CheckBox) findViewById(R.id.answer5);
        mDrugBySelf = (CheckBox) findViewById(R.id.answer6);
        mSweet = (CheckBox) findViewById(R.id.answer7);
        mSalt = (CheckBox) findViewById(R.id.answer8);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cl = new CursorLoader(this,
                Uri.withAppendedPath(Behavior.CONTENT_URI, getPid()),
                PROJECTION, "pcucodeperson=?", new String[]{getPcucodePerson()},
                Behavior.DEFAULT_SORTING);
        return cl;
    }

    private static final String[] PROJECTION = new String[]{
            Behavior.EXERCISE,
            Behavior.CIGA,
            Behavior.WISKY,
            Behavior.TONIC,
            Behavior.ACCIDENT,
            Behavior.DRUGBYSELF,
            Behavior.SUGAR,
            Behavior.SALT,
    };

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        if (arg1.moveToFirst()) {
            mAction = Action.EDIT;
            mExercise.setSelection(arg1.getInt(0));
            mCiga.setSelection(arg1.getInt(1));
            mWiskey.setSelection(arg1.getInt(2));
            mTonic.setSelection(arg1.getInt(3));
            mAccident.setChecked(arg1.getInt(4) == 1 ? true : false);
            mDrugBySelf.setChecked(arg1.getInt(5) == 1 ? true : false);
            mSweet.setChecked(arg1.getInt(6) == 1 ? true : false);
            mSalt.setChecked(arg1.getInt(7) == 1 ? true : false);
        } else {
            mAction = Action.INSERT;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }
}
