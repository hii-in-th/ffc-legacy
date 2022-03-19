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

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import th.in.ffc.R;
import th.in.ffc.app.FFCIntentDiaglog;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.provider.CodeProvider.Hospital;
import th.in.ffc.provider.CodeProvider.Nation;
import th.in.ffc.provider.CodeProvider.Occupation;
import th.in.ffc.provider.CodeProvider.Right;
import th.in.ffc.provider.PersonProvider.Death;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.PersonColumns;
import th.in.ffc.util.AgeCalculator;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.ThaiCitizenID;

/**
 * add description here! please
 *
 * @author piruin panichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public class PersonDetailViewFragment extends PersonFragment implements
        LoaderCallbacks<Cursor> {

    private static final int LOAD_DATA = 10;
    private static final int LOAD_DEATH = 11;

    public static final String[] PROJECTION = new String[]{Person.PID,
            Person.FULL_NAME, Person.CITIZEN_ID, Person.NICKNAME, Person.SEX,
            Person.BIRTH, Person.BLOOD_GROUP, Person.BLOOD_RH, Person.ALLERGIC,
            Person.MARRY_STATUS, Person.NATION, Person.ORIGIN, Person.RELIGION,
            Person.EDUCATION, Person.OCCUPA, Person.INCOME, Person.TEL,

            Person.FATHER, Person.FATHER_ID, Person.MOTHER, Person.MOTHER_ID,
            Person.MATE, Person.MATE_ID,

            Person.RIGHT_CODE, Person.RIGHT_NO, Person.RIGHT_HMAIN,
            Person.RIGHT_HSUB, Person.RIGHT_REGIS, Person.RIGHT_START,
            Person.RIGHT_EXPIRE,

    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        doShowContent();

    }

    public void doShowContent() {

        mForm.removeAllViewsInLayout();

        showProgess(true);
        getFFCActivity().getSupportLoaderManager().initLoader(LOAD_DATA, null,
                this);

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.editable_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                Intent intent = new Intent(Action.EDIT);
                intent.setData(Uri.withAppendedPath(Person.CONTENT_URI, getPID()));
                intent.addCategory(Category.PERSON);
                intent.addCategory(Category.DEFAULT);
                intent.putExtra(PersonColumns._PID, getPID());
                intent.putExtra(PersonColumns._PCUCODEPERSON, getPcucodePerson());
//			PersonActivity.startPersonActivity(getActivity(), intent, getPID(),
//					getPcucodePerson());
                FFCIntentDiaglog f = (FFCIntentDiaglog) Fragment.instantiate(getActivity(), FFCIntentDiaglog.class.getName());
                f.setIntent(intent);
                f.showDialog(getFFCActivity());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    TextView mDeath;
    Date mBorn;

    private void doShowRegularData(Cursor c) {
        addTitle(getString(R.string.personal_data));

        addContentArrayFormat(getString(R.string.sex),
                c.getString(c.getColumnIndex(Person.SEX)), R.array.sex);

        addContent(R.string.nickname,
                c.getString(c.getColumnIndex(Person.NICKNAME)));

        String birth = c.getString(c.getColumnIndex(Person.BIRTH));
        mBorn = Date.newInstance(birth);
        addContent(R.string.birthday,
                DateTime.getFullFormatThai(getActivity(), mBorn));

        Date current = Date.newInstance(DateTime.getCurrentDate());
        if (mBorn != null) {
            AgeCalculator cal = new AgeCalculator(current, mBorn);
            Date age = cal.calulate();
            mDeath = addContent(R.string.age,
                    AgeCalculator.toAgeFormat(this.getFFCActivity(), age));
            getActivity().getSupportLoaderManager().initLoader(LOAD_DEATH, null,
                    this);
        }

        addContent(
                R.string.blood,
                PersonUtils.getBlood(
                        c.getString(c.getColumnIndex(Person.BLOOD_GROUP)),
                        c.getString(c.getColumnIndex(Person.BLOOD_RH))));

        addContent(R.string.allergic,
                c.getString(c.getColumnIndex(Person.ALLERGIC)));

        String nation = c.getString(c.getColumnIndex(Person.NATION));
        if (!TextUtils.isEmpty(nation))
            addContentQuery(R.string.nation, Nation.NAME,
                    Uri.withAppendedPath(Nation.CONTENT_URI, nation), null);

        String origin = c.getString(c.getColumnIndex(Person.ORIGIN));
        if (!TextUtils.isEmpty(origin))
            addContentQuery(R.string.origin, Nation.NAME,
                    Uri.withAppendedPath(Nation.CONTENT_URI, origin), null);

        addContentArrayFormat(getString(R.string.religion),
                c.getString(c.getColumnIndex(Person.RELIGION)),
                R.array.religion);

        addContentArrayFormat(getString(R.string.education),
                c.getString(c.getColumnIndex(Person.EDUCATION)),
                R.array.education);

        String occupa = c.getString(c.getColumnIndex(Person.OCCUPA));
        if (!TextUtils.isEmpty(occupa))
            addContentQuery(R.string.occupa, Occupation.NAME,
                    Uri.withAppendedPath(Occupation.CONTENT_URI, occupa), null);

        addContent(
                R.string.income,
                PersonUtils.getIncome(this.getActivity(),
                        c.getFloat(c.getColumnIndex(Person.INCOME))));
        addContent(R.string.tel, c.getString(c.getColumnIndex(Person.TEL)));

    }

    private void doShowRelationShipData(Cursor c) {
        addTitle(getString(R.string.relation));

        addContent(R.string.father,
                c.getString(c.getColumnIndex(Person.FATHER)));
        addSubContent(ThaiCitizenID.parse(c.getString(c
                .getColumnIndex(Person.FATHER_ID))));

        addContent(R.string.mother,
                c.getString(c.getColumnIndex(Person.MOTHER)));
        addSubContent(ThaiCitizenID.parse(c.getString(c
                .getColumnIndex(Person.MOTHER_ID))));

        addContentArrayFormat(getString(R.string.marry_status),
                c.getString(c.getColumnIndex(Person.MARRY_STATUS)),
                R.array.marry_status);

        addContent(R.string.mate, c.getString(c.getColumnIndex(Person.MATE)));
        addSubContent(ThaiCitizenID.parse(c.getString(c
                .getColumnIndex(Person.MATE_ID))));
    }

    private void doShowRightStatus(Cursor c) {
        addTitle(R.string.right);
        String right = c.getString(c.getColumnIndex(Person.RIGHT_CODE));
        if (right != null)
            addContentQuery(null, Right.NAME,
                    Uri.withAppendedPath(Right.CONTENT_URI, right),
                    c.getString(c.getColumnIndex(Person.RIGHT_NO)));

        String hosMain = c.getString(c.getColumnIndex(Person.RIGHT_HMAIN));
        if (hosMain != null)
            addContentQuery(R.string.hosmain, Hospital.NAME,
                    Uri.withAppendedPath(Hospital.CONTENT_URI, hosMain), "#"
                            + hosMain);

        String hosSub = c.getString(c.getColumnIndex(Person.RIGHT_HSUB));
        if (hosSub != null)
            addContentQuery(R.string.hossub, Hospital.NAME,
                    Uri.withAppendedPath(Hospital.CONTENT_URI, hosSub), "#"
                            + hosSub);

        addContent(
                R.string.regis,
                DateTime.getFullFormatThai(getActivity(),
                        c.getString(c.getColumnIndex(Person.RIGHT_REGIS))));
        addContent(
                R.string.active,
                DateTime.getFullFormatThai(getActivity(),
                        c.getString(c.getColumnIndex(Person.RIGHT_START))));
        addContent(
                R.string.expire,
                DateTime.getFullFormatThai(getActivity(),
                        c.getString(c.getColumnIndex(Person.RIGHT_EXPIRE))));

    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cl;
        switch (arg0) {
            case LOAD_DATA:
                Uri uri = Uri.withAppendedPath(Person.CONTENT_URI, getPID());
                cl = new CursorLoader(getActivity(), uri, PROJECTION,
                        "pcucodeperson=?", new String[]{getPcucodePerson()},
                        Person.DEFAULT_SORTING);
                return cl;
            case LOAD_DEATH:
                Uri deathUri = Uri.withAppendedPath(Death.CONTENT_URI, getPID());
                cl = new CursorLoader(getActivity(), deathUri,
                        new String[]{Death.DATE}, null, null,
                        Death.DEFAULT_SORTING);
                return cl;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        switch (loader.getId()) {
            case LOAD_DATA:
                if (c.moveToFirst()) {
                    final Cursor k = c;
                    Handler handler = new Handler();
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            doShowRegularData(k);
                            doShowRelationShipData(k);
                            doShowRightStatus(k);
                            showProgess(false);


                        }
                    });

                }
                break;
            case LOAD_DEATH:
                if (c.moveToFirst()) {
                    String death = c.getString(0);
                    Date deathDate = Date.newInstance(death);

                    if (deathDate != null) {
                        AgeCalculator cal = new AgeCalculator(deathDate, mBorn);
                        Date age = cal.calulate();
                        mDeath.setText(AgeCalculator.toAgeFormat(getFFCActivity(), age));
                    }

                }
                PersonMainActivity act = (PersonMainActivity) getFFCActivity();
                act.showNotification();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mBorn = null;
        mDeath = null;

        getActivity().getSupportLoaderManager().destroyLoader(LOAD_DATA);
        getActivity().getSupportLoaderManager().destroyLoader(LOAD_DEATH);
    }

    ;

}
