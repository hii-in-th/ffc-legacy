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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.ActionBar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.blayzupe.phototaker.ImageResizer;
import com.blayzupe.phototaker.PhotoTaker;
import com.blayzupe.phototaker.PhotoTaker.OnCropFinishListener;

import me.piruin.quickaction.ActionItem;
import me.piruin.quickaction.QuickAction;
import th.in.ffc.FamilyFolderCollector;
import th.in.ffc.R;
import th.in.ffc.app.FFCActionBarTabsPagerActivity;
import th.in.ffc.app.FFCIntentDiaglog;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.person.visit.WomenView;
import th.in.ffc.provider.CodeProvider.Diagnosis;
import th.in.ffc.provider.CodeProvider.PersonIncomplete;
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.provider.PersonProvider.*;
import th.in.ffc.util.AgeCalculator;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.Log;
import th.in.ffc.util.ThaiCitizenID;

import java.io.File;

/**
 * add description here! please
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public class PersonMainActivity extends FFCActionBarTabsPagerActivity implements
        LoaderCallbacks<Cursor>, OnCropFinishListener {

    private static final int LOAD_DEATH = 0;
    private static final int LOAD_CHRONIC = 1;
    private static final int LOAD_HANDICAP = 2;
    private static final int LOAD_PERSON = 3;


    PersonDetailViewFragment mViewFragment;
    ActionBarTabPagersAdapter mAdapter;
    Bundle mArgs;

    PhotoTaker mPhotoTaker;
    ImageView mImage;
    String mPhotoPath;
    LinearLayout mNotifacate;

    String mPid;
    String mPcuCodePerson;

    private Uri mData;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mData = null;
        mPid = null;
        mPcuCodePerson = null;
        mNotifacate = null;
        mPhotoPath = null;
        mImage = null;
        mPhotoTaker = null;
        mArgs = null;
        mAdapter = null;
        mViewFragment = null;
    }

    private String[] PROJECTION = new String[]{Person.PCUPERSONCODE,
            Person.FULL_NAME, Person.CITIZEN_ID, Person.BIRTH, Person.SEX,};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setSupportProgressBarIndeterminateVisibility(false);
        setContentView(R.layout.person_main_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mData = getIntent().getData();
        mPid = mData.getLastPathSegment();
        Bundle args = new Bundle();

        args.putString(PersonColumns._PID, mPid);
        args.putString(PersonColumns._PCUCODEPERSON, getIntent()
                .getStringExtra(PersonColumns._PCUCODEPERSON));
        mArgs = args;

        mAdapter = new ActionBarTabPagersAdapter(this,
                getSupportFragmentManager(), getSupportActionBar(),
                getViewPager());
        mAdapter.addTab("Infomation", PersonDetailViewFragment.class, args);
        mAdapter.addTab("Behavior", PersonBehaviorViewFragment.class, args);
        mAdapter.addTab("Chronic", PersonChronicFamily.class, args);

        getSupportLoaderManager().initLoader(LOAD_PERSON, null, this);

        setTabPagerAdapter(mAdapter);
        mAdapter.setTabVisible(false);

        TextView code = (TextView) findViewById(R.id.code);
        if (code != null)
            code.setText(getString(R.string.shape).concat(mPid));
        doSetupImage();

    }

    public void showNotification() {
        if (mNotifacate == null || !mNotifacate.isShown()) {
            setSupportProgressBarIndeterminateVisibility(true);
            Log.d(TAG, "Load Notifaciton");
            getSupportLoaderManager().initLoader(LOAD_DEATH, null,
                    PersonMainActivity.this);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mImage != null) {
            File path = new File(mPhotoPath);
            if (path.exists())
                mImage.setImageDrawable(Drawable.createFromPath(mPhotoPath));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.person_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.camera:
                mPhotoTaker.doShowDialog();
                break;
            case R.id.visit:
                Intent visit = new Intent(Action.MAIN);
                visit.addCategory(Category.VISIT);
                visit.putExtra(PersonColumns._PCUCODEPERSON, mPcuCodePerson);
                visit.putExtra(Person.CITIZEN_ID, getSupportActionBar().getSubtitle());
                visit.putExtra(Person.FULL_NAME, getSupportActionBar().getTitle());
                visit.setData(Uri.withAppendedPath(Person.CONTENT_URI, getIntent().getData().getLastPathSegment()));
                FFCIntentDiaglog f = (FFCIntentDiaglog) Fragment.instantiate(this, FFCIntentDiaglog.class.getName());
                f.setIntent(visit);
                f.showDialog(this);
                break;
            case android.R.id.home:
                Cursor c = getContentResolver().query(getIntent().getData(),
                        new String[]{Person.HCODE}, null, null,
                        Person.DEFAULT_SORTING);
                if (c.moveToFirst()) {
                    Intent house = new Intent(Action.MAIN);
                    house.setData(Uri.withAppendedPath(House.CONTENT_URI,
                            c.getString(0)));
                    house.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(house);
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public void doSetupImage() {

        String name = getPcuCode().concat(mPid).concat("_720p.jpg");
        File pick = new File(FamilyFolderCollector.PHOTO_DIRECTORY_PERSON, name);
        mPhotoPath = pick.getAbsolutePath();

        mImage = (ImageView) findViewById(R.id.image);
        mImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                File path = new File(mPhotoPath);
                if (path.exists())
                    mImage.setImageDrawable(Drawable.createFromPath(mPhotoPath));

                if (qa != null && mNotifacate != null) {
                    qa.show(mNotifacate);
                }
            }
        });
        mImage.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                mPhotoTaker.doShowDialog();
                return true;
            }
        });

        if (pick.exists())
            mImage.setImageDrawable(Drawable.createFromPath(mPhotoPath));

        mPhotoTaker = new PhotoTaker(this,
                FamilyFolderCollector.PHOTO_DIRECTORY_PERSON, name);
        mPhotoTaker.setCropfinishListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (mPhotoTaker == null)
            doSetupImage();
        // mPhotoTaker = new PhotoTaker(this,
        // FamilyFolderCollector.PHOTO_DIRECTORY_PERSON, getPcuCode()
        // .concat(getIntent().getData().getLastPathSegment())
        // .concat("_720p.jpg"));
        mPhotoTaker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean OnCropFinsh(String path, Uri uri) {
        String realName = getPcuCode().concat(mPid).concat(".jpg");
        File realFile = new File(FamilyFolderCollector.PHOTO_DIRECTORY_PERSON,
                realName);
        File hdFile = new File(path);

        ImageResizer pImageResizer = new ImageResizer();
        return pImageResizer.doResizeImage(hdFile, realFile, false);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cl;
        Uri uri;
        switch (arg0) {
            case LOAD_PERSON:
                uri = Uri.withAppendedPath(Person.CONTENT_URI, mPid);
                cl = new CursorLoader(this, uri, PROJECTION, null, null,
                        Person.DEFAULT_SORTING);
                return cl;
            case LOAD_CHRONIC:
                uri = Uri.withAppendedPath(Chronic.CONTENT_URI, mPid);
                cl = new CursorLoader(this, uri, new String[]{Chronic.FIRST_DIAG,
                        Chronic.CODE, Diagnosis.NAME_TH, Diagnosis.NAME_ENG},
                        null, null, Chronic.FIRST_DX);
                return cl;
            case LOAD_DEATH:
                uri = Uri.withAppendedPath(Death.CONTENT_URI, mPid);
                cl = new CursorLoader(this, uri, new String[]{Death.DATE,
                        Death.CAUSE, Diagnosis.NAME_TH, Diagnosis.NAME_ENG}, null,
                        null, Death.DEFAULT_SORTING);
                return cl;
            case LOAD_HANDICAP:
                uri = Uri.withAppendedPath(PersonunableType.CONTENT_URI, mPid);
                cl = new CursorLoader(this, uri, new String[]{
                        PersonunableType.DATEFOUND, PersonunableType.TYPECODE},
                        null, null, PersonunableType.DEFAULT_SORTING);
                return cl;
            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> l, Cursor c) {

        switch (l.getId()) {
            case LOAD_PERSON:
                if (c.moveToFirst()) {
                    mPcuCodePerson = c.getString(c
                            .getColumnIndex(Person.PCUPERSONCODE));
                    if (c.getInt(c.getColumnIndex(Person.SEX)) == 2) {
                        Date cur = Date.newInstance(DateTime.getCurrentDate());
                        Date born = Date.newInstance(c.getString(c
                                .getColumnIndex(Person.BIRTH)));
                        if (born != null) {
                            AgeCalculator cal = new AgeCalculator(cur, born);
                            Date age = cal.calulate();
                            if (age.year >= 11 && age.year <= 45)
                                mAdapter.addTab("Women", WomenView.class, mArgs);
                        }
                    }

                    doSetupActionBar(
                            c.getString(c.getColumnIndex(Person.FULL_NAME)),
                            ThaiCitizenID.parse(c.getString(c
                                    .getColumnIndex(Person.CITIZEN_ID))));
                }
                break;
            case LOAD_DEATH:
                if (c.moveToFirst()) {
                    String date = c.getString(0);
                    if (TextUtils.isEmpty(date))
                        date = getString(R.string.not_available);
                    String msg = c.getString(1);
                    String name = c.getString(2);
                    if (TextUtils.isEmpty(name)) {
                        name = c.getString(3);
                    }
                    if (!TextUtils.isEmpty(name))
                        msg = msg + " : " + name;
                    addNotification(LOAD_DEATH, R.drawable.ic_stat_death, date, msg);
                }
                c.close();
                getSupportLoaderManager().initLoader(LOAD_CHRONIC, null, this);
                break;
            case LOAD_CHRONIC:
                if (c.moveToFirst()) {
                    do {
                        String msg = c.getString(1);
                        String name = c.getString(2);
                        if (TextUtils.isEmpty(name)) {
                            name = c.getString(3);
                        }
                        if (!TextUtils.isEmpty(name))
                            msg = msg + " : " + name;

                        addNotification(LOAD_CHRONIC, R.drawable.ic_stat_chronic,
                                c.getString(0), msg);
                    } while (c.moveToNext());
                }
                c.close();
                getSupportLoaderManager().initLoader(LOAD_HANDICAP, null, this);
                break;

            case LOAD_HANDICAP:
                if (c.moveToFirst()) {
                    do {
                        String msg = c.getString(1);
                        Uri unableType = Uri.withAppendedPath(PersonIncomplete.CONTENT_URI, msg);
                        Cursor u = getContentResolver().query(unableType, new String[]{PersonIncomplete.NAME},
                                null, null, PersonIncomplete.DEFAULT_SORTING);
                        if (u.moveToFirst()) {
                            msg += " : " + u.getString(0);
                        }
                        addNotification(LOAD_HANDICAP, R.drawable.ic_stat_handicap,
                                c.getString(0), msg);
                    } while (c.moveToNext());
                }

                setSupportProgressBarIndeterminateVisibility(false);
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

    private QuickAction qa;

    private void addNotification(int id, int icon, String msg, String subMSg) {
        if (qa == null) {
            qa = new QuickAction(this, QuickAction.VERTICAL);
        }

        ActionItem item = new ActionItem(id, msg, icon);
        qa.addActionItem(item);

        if (!TextUtils.isEmpty(subMSg)) {
            ActionItem subItem = new ActionItem(id, subMSg);
            qa.addActionItem(subItem);
        }

        if (mNotifacate == null) {
            mNotifacate = (LinearLayout) findViewById(R.id.notifacation);
            mNotifacate.setVisibility(View.VISIBLE);
            mNotifacate.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    qa.show(mNotifacate);
                }
            });
        }

        ImageView img = (ImageView) mNotifacate.findViewById(id);
        if (img == null) {
            img = new ImageView(this);
            img.setId(id);
            img.setImageResource(icon);
            mNotifacate.addView(img, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }
    }

    private void doSetupActionBar(String fullname, String citizenId) {
        ActionBar ab = getSupportActionBar();
        if (!TextUtils.isEmpty(fullname))
            ab.setTitle(fullname);
        if (!TextUtils.isEmpty(citizenId))
            ab.setSubtitle(citizenId);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(false);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSupportLoaderManager().destroyLoader(LOAD_CHRONIC);
        getSupportLoaderManager().destroyLoader(LOAD_DEATH);
        getSupportLoaderManager().destroyLoader(LOAD_HANDICAP);
        getSupportLoaderManager().destroyLoader(LOAD_PERSON);
    }

}
