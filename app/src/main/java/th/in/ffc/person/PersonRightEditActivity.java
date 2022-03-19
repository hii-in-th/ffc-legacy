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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import me.piruin.quickaction.ActionItem;
import me.piruin.quickaction.QuickAction;
import th.in.ffc.R;
import th.in.ffc.code.HospitalListDialog;
import th.in.ffc.code.RightListDialog;
import th.in.ffc.person.PersonDetailEditActivity.Saveable;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.PersonColumns;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.widget.SearchableButton;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector plus
 */
public class PersonRightEditActivity extends PersonActivity {

    PersonRightEditFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.default_scrollable_activity);

        Bundle args = new Bundle();
        args.putString(PersonColumns._PCUCODEPERSON, getPcucodePerson());
        args.putString(PersonColumns._PID, getPid());

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment = (PersonRightEditFragment) fm.findFragmentByTag("right");
        if (mFragment == null) {
            mFragment = (PersonRightEditFragment) Fragment.instantiate(this,
                    PersonRightEditFragment.class.getName(), args);
            ft.add(R.id.content, mFragment, "right");
            ft.commit();
        }
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
                EditTransaction et = beginTransaction();
                if (mFragment.onSave(et)) {
                    String selection = "pid=? AND pcucodeperson=?";
                    String[] selectionArgs = new String[]{
                            getPid(),
                            getPcucodePerson(),
                    };
                    int update = et.commit(Person.CONTENT_URI, selection, selectionArgs);
                    Log.d(TAG, "updated=" + update);

                    this.finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * add description here!
     *
     * @author Piruin Panichphol
     * @version 1.0
     * @since Family Folder Collector plus
     */
    public static class PersonRightEditFragment extends PersonFragment implements Saveable {

        SearchableButton mRight;
        EditText mRightNo;

        SearchableButton mMainHos;
        SearchableButton mSubHos;

        ThaiDatePicker mRegis;
        ThaiDatePicker mActive;
        ThaiDatePicker mExpire;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.person_right_edit_fragment, container, false);
            mRight = (SearchableButton) view.findViewById(R.id.answer1);
            mRight.setDialog(getFragmentManager(), RightListDialog.class, "rightcode");
            mRightNo = (EditText) view.findViewById(R.id.answer2);

            mMainHos = (SearchableButton) view.findViewById(R.id.answer3);
            mMainHos.setDialog(getFragmentManager(), HospitalListDialog.class, "main");

            mSubHos = (SearchableButton) view.findViewById(R.id.answer4);
            mSubHos.setDialog(getFragmentManager(), HospitalListDialog.class, "sub");


            mRegis = (ThaiDatePicker) view.findViewById(R.id.answer5);
            mActive = (ThaiDatePicker) view.findViewById(R.id.answer6);
            mExpire = (ThaiDatePicker) view.findViewById(R.id.answer7);
            Date current = Date.newInstance(DateTime.getCurrentDate());
            mRegis.updateDate(current);
            mActive.updateDate(current);
            mExpire.updateDate(current);

            return view;
        }

        private final String[] PROJECTION = new String[]{
                Person.RIGHT_CODE,
                Person.RIGHT_NO,
                Person.RIGHT_HMAIN,
                Person.RIGHT_HSUB,
                Person.RIGHT_REGIS,
                Person.RIGHT_START,
                Person.RIGHT_EXPIRE,
        };

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            Uri personUri = Uri.withAppendedPath(Person.CONTENT_URI, getPID());
            ContentResolver cr = getActivity().getContentResolver();
            Cursor c = cr.query(personUri, PROJECTION, "pcucodeperson=?", new String[]{getPcucodePerson()}, Person.DEFAULT_SORTING);
            if (c.moveToFirst()) {
                String right = c.getString(0);
                if (!TextUtils.isEmpty(right))
                    mRight.setSelectionById(c.getString(0));
                else
                    mRight.setText(getString(R.string.right));

                mRightNo.setText(c.getString(1));
                String hosMain = c.getString(2);
                mMainHos.setSelectionById(!TextUtils.isEmpty(hosMain) ?
                        hosMain : getPcucodePerson());

                String hosSub = c.getString(3);
                mSubHos.setSelectionById(!TextUtils.isEmpty(hosSub) ?
                        hosSub : getPcucodePerson());

                String regis = c.getString(4);
                String active = c.getString(5);
                String expire = c.getString(6);

                if (!TextUtils.isEmpty(regis)) {
                    Date d = Date.newInstance(regis);
                    mRegis.updateDate(d);
                }

                if (!TextUtils.isEmpty(active)) {
                    Date d = Date.newInstance(active);
                    mActive.updateDate(d);
                }

                if (!TextUtils.isEmpty(expire)) {
                    Date d = Date.newInstance(expire);
                    mExpire.updateDate(d);
                }
            } else {
                mSubHos.setSelectionById(getPcucodePerson());
                mSubHos.setSelectionById(getPcucodePerson());


            }
        }

        @Override
        public boolean onSave(EditTransaction et) {
            et.retrieveData(Person.RIGHT_CODE, mRight, false, null, null);
            et.retrieveData(Person.RIGHT_NO, mRightNo, false, null, null);
            et.retrieveData(Person.RIGHT_HMAIN, mMainHos, false, null, null);
            et.retrieveData(Person.RIGHT_HSUB, mSubHos, true, null, null);

            Date regis = mRegis.getDate();
            et.retrieveData(Person.RIGHT_REGIS, mRegis, null, null, null);
            et.retrieveData(Person.RIGHT_START, mActive, regis, null, null);
            et.getContentValues().put(Person._DATEUPDATE, DateTime.getCurrentDateTime());

            Date expire = mExpire.getDate();
            if (expire.compareTo(regis) == Date.MORETHAN) {
                et.retrieveData(Person.RIGHT_EXPIRE, mExpire, regis, null, null);
                return et.canCommit();
            } else {
                QuickAction qa = new QuickAction(this.getActivity());
                qa.addActionItem(new ActionItem(0, getString(R.string.hint_expire_early)));
                qa.show(mExpire);
                return false;
            }


        }


    }

}
