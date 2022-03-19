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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import th.in.ffc.R;
import th.in.ffc.app.FFCSearchListDialog;
import th.in.ffc.code.FamilyPositionListDialog;
import th.in.ffc.code.PersonListDialog;
import th.in.ffc.person.PersonDetailEditActivity.Saveable;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.PersonColumns;
import th.in.ffc.util.DateTime;
import th.in.ffc.widget.ArrayFormatSpinner;
import th.in.ffc.widget.HighLightCursorAdapter;
import th.in.ffc.widget.SearchableButton;
import th.in.ffc.widget.SearchableSpinner;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.2
 * @since Family Folder Collector plus
 */
public class PersonRelationEditActivity extends PersonActivity {

    PersonRelationEditFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.default_scrollable_activity);

        Bundle args = new Bundle();
        args.putString(PersonColumns._PCUCODEPERSON, getPcucodePerson());
        args.putString(PersonColumns._PID, getPid());

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment = (PersonRelationEditFragment) fm
                .findFragmentByTag("relation");
        if (mFragment == null) {
            mFragment = (PersonRelationEditFragment) Fragment.instantiate(this,
                    PersonRelationEditFragment.class.getName(), args);
            ft.add(R.id.content, mFragment, "relation");
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
                    String[] selectionArgs = new String[]{getPid(),
                            getPcucodePerson(),};
                    int update = et.commit(Person.CONTENT_URI, selection,
                            selectionArgs);
                    Log.d(TAG, "updated=" + update);

                    this.finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class PersonRelationEditFragment extends PersonFragment
            implements Saveable, LoaderCallbacks<Cursor> {

        EditText mFamilyNo;
        SearchableButton mFamilyPos;
        View mTextFather;
        SearchableSpinner mFather;
        View mTextMother;
        SearchableSpinner mMother;
        View mTextMate;
        SearchableSpinner mMate;
        ArrayFormatSpinner mMarryStatus;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(
                    R.layout.person_relation_edit_fragment, container, false);
            mFamilyNo = (EditText) view.findViewById(R.id.answer1);
            mFamilyPos = (SearchableButton) view.findViewById(R.id.answer2);
            mFamilyPos.setDialog(getActivity().getSupportFragmentManager(),
                    FamilyPositionListDialog.class, "fampos");
            mTextFather = view.findViewById(R.id.view1);
            mFather = (SearchableSpinner) view.findViewById(R.id.answer3);

            mTextMother = view.findViewById(R.id.view2);
            mMother = (SearchableSpinner) view.findViewById(R.id.answer4);

            mMarryStatus = (ArrayFormatSpinner) view.findViewById(R.id.answer5);
            mMarryStatus.setArray(R.array.marry_status);

            mTextMate = view.findViewById(R.id.view3);
            mMate = (SearchableSpinner) view.findViewById(R.id.answer6);

            return view;
        }

        private static final String[] PROJECTION = new String[]{Person.BIRTH,
                Person.HCODE, Person.SEX, Person.FAMILY_NO,
                Person.FAMILY_POSITION, Person.FATHER, Person.FATHER_ID,
                Person.MOTHER, Person.MOTHER_ID, Person.MARRY_STATUS,
                Person.MATE, Person.MATE_ID,};

        private String mHcode;
        private int mMateSex;
        private String mBirth;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setHasOptionsMenu(true);

            Uri uri = Uri.withAppendedPath(Person.CONTENT_URI, getPID());
            Cursor c = getActivity().getContentResolver().query(uri,
                    PROJECTION, null, null, Person.BIRTH);

            if (savedInstanceState == null && c.moveToFirst()) {
                mFamilyNo.setText(c.getString(c
                        .getColumnIndex(Person.FAMILY_NO)));
                mFamilyPos.setSelectionById(c.getString(c
                        .getColumnIndex(Person.FAMILY_POSITION)));
                mMarryStatus.setSelection(c.getString(c
                        .getColumnIndex(Person.MARRY_STATUS)));

                mHcode = c.getString(c.getColumnIndex(Person.HCODE));
                String baseWhere = "hcode='" + mHcode + "'";

                mBirth = c.getString(c.getColumnIndex(Person.BIRTH));
                String fatherWhere = "sex=1 AND birth < '" + mBirth + "'";
                String motherWhere = "sex=2 AND birth < '" + mBirth + "'";

                mMateSex = c.getInt(c.getColumnIndex(Person.SEX)) == 1 ? 2 : 1;
                String mateWhere = "sex = " + mMateSex + "";

                FragmentManager fm = getActivity().getSupportFragmentManager();
                Bundle args = new Bundle();
                args.putString(FFCSearchListDialog.EXTRA_DEFAULT_WHERE,
                        baseWhere);
                args.putString(FFCSearchListDialog.EXTRA_APPEND_WHERE,
                        fatherWhere);
                mFather.setDialog(fm, PersonListDialog.class, args, "father");

                args = new Bundle(args);
                args.putString(FFCSearchListDialog.EXTRA_APPEND_WHERE,
                        motherWhere);
                mMother.setDialog(fm, PersonListDialog.class, args, "mother");

                args = new Bundle(args);
                args.putString(FFCSearchListDialog.EXTRA_APPEND_WHERE,
                        mateWhere);
                mMate.setDialog(fm, PersonListDialog.class, args, "mate");


                LoaderManager lm = getActivity().getSupportLoaderManager();

                // / Father section
                String fname = c.getString(c.getColumnIndex(Person.FATHER));
                String fid = c.getString(c.getColumnIndex(Person.FATHER_ID));

                Cursor father = findPersonBy(fid, fname);
                if (father.moveToFirst()) {
                    mFather.setSelectionById(father.getLong(0));

                } else {
                    mTextFather.setVisibility(View.GONE);
                    mFather.setVisibility(View.GONE);

                    if (savedInstanceState == null)
                        lm.initLoader(LOAD_FATHER, null, this);
                }

                // / Mother section
                String mname = c.getString(c.getColumnIndex(Person.MOTHER));
                String mid = c.getString(c.getColumnIndex(Person.MOTHER_ID));
                Cursor mother = findPersonBy(mid, mname);
                if (mother.moveToFirst()) {
                    Log.d(TAG, "found mother");
                    mMother.setSelectionById(mother.getLong(0));
                } else {
                    mTextMother.setVisibility(View.GONE);
                    mMother.setVisibility(View.GONE);

                    if (savedInstanceState == null)
                        lm.initLoader(LOAD_MOTHER, null, this);
                }

                // / Mate section
                String cname = c.getString(c.getColumnIndex(Person.MATE));
                String cid = c.getString(c.getColumnIndex(Person.MATE_ID));
                Cursor couple = findPersonBy(cid, cname);
                if (couple.moveToFirst()) {
                    Log.d(TAG, "found mate");
                    mMate.setSelectionById(couple.getLong(0));
                } else {
                    mTextMate.setVisibility(View.GONE);
                    mMate.setVisibility(View.GONE);

                    if (savedInstanceState == null)
                        lm.initLoader(LOAD_MATE, null, this);
                }
            }

        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // TODO Auto-generated method stub
            super.onCreateOptionsMenu(menu, inflater);

            SubMenu sub = menu.addSubMenu(R.string.add);
            sub.setIcon(R.drawable.ic_action_add);
            sub.add(0, R.string.father, 0, R.string.father);
            sub.add(0, R.string.mother, 0, R.string.mother);
            sub.add(0, R.string.mate, 0, R.string.mate);
            sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // TODO Auto-generated method stub
            switch (item.getItemId()) {
                case R.string.father:
                    mTextFather.setVisibility(mTextFather.isShown() ? View.GONE
                            : View.VISIBLE);
                    mFather.setVisibility(mFather.isShown() ? View.GONE
                            : View.VISIBLE);
                    return true;
                case R.string.mother:
                    mTextMother.setVisibility(mTextMother.isShown() ? View.GONE
                            : View.VISIBLE);
                    mMother.setVisibility(mMother.isShown() ? View.GONE
                            : View.VISIBLE);
                    return true;
                case R.string.mate:
                    mTextMate.setVisibility(mTextMate.isShown() ? View.GONE
                            : View.VISIBLE);
                    mMate.setVisibility(mMate.isShown() ? View.GONE : View.VISIBLE);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        private static final String[] PERSON_PROJ = new String[]{Person.PID,
                Person.CITIZEN_ID, Person.FIRST_NAME, Person.FULL_NAME,};

        public Cursor findPersonBy(String idcard, String name) {
            String selection = null;
            if (!TextUtils.isEmpty(idcard) && idcard.length() == 13) {
                selection = Person.CITIZEN_ID + "='" + idcard + "'";
            } else if (!TextUtils.isEmpty(name)) {
                selection = Person.FIRST_NAME + "='" + name + "' OR "
                        + Person.FULL_NAME + " LIKE '%" + name + "%'";
            } else {
                selection = "1 <> 1";
            }
            Cursor f = getActivity().getContentResolver().query(
                    Person.CONTENT_URI, PERSON_PROJ, selection, null,
                    Person.PID);
            return f;

        }

        @Override
        public boolean onSave(EditTransaction et) {

            et.retrieveData(Person.FAMILY_NO, mFamilyNo, false, 1, 99, "1-99");
            et.retrieveData(Person.FAMILY_POSITION, mFamilyPos, false, null,
                    "select one");

            ContentValues cv = et.getContentValues();
            String nu = null;
            if (mFather.isShown()) {
                HighLightCursorAdapter adapter = (HighLightCursorAdapter) mFather
                        .getAdapter();
                int pos = mFather.getSelectedItemPosition();
                String name = adapter.getData(pos, Person.FIRST_NAME);
                String id = adapter.getData(pos, Person.CITIZEN_ID);

                cv.put(Person.FATHER, name);
                cv.put(Person.FATHER_ID, id);
            } else {
                cv.put(Person.FATHER, nu);
                cv.put(Person.FATHER_ID, nu);
            }

            if (mMother.isShown()) {
                HighLightCursorAdapter adapter = (HighLightCursorAdapter) mMother
                        .getAdapter();
                int pos = mMother.getSelectedItemPosition();
                String name = adapter.getData(pos, Person.FIRST_NAME);
                String id = adapter.getData(pos, Person.CITIZEN_ID);

                cv.put(Person.MOTHER, name);
                cv.put(Person.MOTHER_ID, id);
            } else {
                cv.put(Person.MOTHER, nu);
                cv.put(Person.MOTHER_ID, nu);
            }

            cv.put(Person.MARRY_STATUS, mMarryStatus.getSelectionId());

            if (mMate.isShown()) {
                HighLightCursorAdapter adapter = (HighLightCursorAdapter) mMate
                        .getAdapter();
                int pos = mMate.getSelectedItemPosition();
                String name = adapter.getData(pos, Person.FIRST_NAME);
                String id = adapter.getData(pos, Person.CITIZEN_ID);

                cv.put(Person.MATE, name);
                cv.put(Person.MATE_ID, id);
            } else {
                cv.put(Person.MATE, nu);
                cv.put(Person.MATE_ID, nu);
            }

            cv.put(Person._DATEUPDATE, DateTime.getCurrentDateTime());

            return et.canCommit();
        }

        private static final int LOAD_FATHER = 0;
        private static final int LOAD_MOTHER = 1;
        private static final int LOAD_MATE = 2;

        @Override
        public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
            CursorLoader cl;
            String baseWhere = "hcode='" + mHcode + "' AND ";
            String Additional = null;
            switch (arg0) {
                case LOAD_FATHER:
                    Additional = "sex=1 AND birth < '" + mBirth + "'";
                    break;
                case LOAD_MOTHER:
                    Additional = "sex=2 AND birth < '" + mBirth + "'";
                    break;
                case LOAD_MATE:
                    Additional = "sex=" + mMateSex;
                    break;
            }
            cl = new CursorLoader(getActivity(), Person.CONTENT_URI,
                    new String[]{Person._ID}, baseWhere.concat(Additional),
                    null, Person.DEFAULT_SORTING);
            return cl;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> l, Cursor c) {
            if (c.moveToFirst()) {
                switch (l.getId()) {
                    case LOAD_FATHER:
                        mFather.setSelectionById(c.getLong(0));
                        break;
                    case LOAD_MOTHER:
                        mMother.setSelectionById(c.getLong(0));
                        break;
                    case LOAD_MATE:
                        mMate.setSelectionById(c.getLong(0));
                        break;
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
        }

    }
}
