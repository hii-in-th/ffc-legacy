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
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.appcompat.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blayzupe.phototaker.PhotoTaker;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.io.File;

import me.piruin.quickaction.ActionItem;
import me.piruin.quickaction.QuickAction;
import th.in.ffc.FamilyFolderCollector;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.provider.CodeProvider.Diagnosis;
import th.in.ffc.provider.PersonProvider.Death;
import th.in.ffc.provider.PersonProvider.FFC506RADIUS;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.Visit;
import th.in.ffc.provider.PersonProvider.VisitAnc;
import th.in.ffc.provider.PersonProvider.VisitAncDeliver;
import th.in.ffc.provider.PersonProvider.VisitAncMotherCare;
import th.in.ffc.provider.PersonProvider.VisitBabycare;
import th.in.ffc.provider.PersonProvider.VisitDentalCheck;
import th.in.ffc.provider.PersonProvider.VisitDiag;
import th.in.ffc.provider.PersonProvider.VisitDiag506address;
import th.in.ffc.provider.PersonProvider.VisitDiagAppoint;
import th.in.ffc.provider.PersonProvider.VisitDrug;
import th.in.ffc.provider.PersonProvider.VisitDrugDental;
import th.in.ffc.provider.PersonProvider.VisitDrugDentalDiag;
import th.in.ffc.provider.PersonProvider.VisitEpi;
import th.in.ffc.provider.PersonProvider.VisitFamilyplan;
import th.in.ffc.provider.PersonProvider.VisitIndividual;
import th.in.ffc.provider.PersonProvider.VisitLabcancer;
import th.in.ffc.provider.PersonProvider.VisitNutrition;
import th.in.ffc.provider.PersonProvider.VisitOldter;
import th.in.ffc.provider.PersonProvider.VisitScreenspecialdisease;
import th.in.ffc.util.AgeCalculator;
import th.in.ffc.util.DateTime;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.ThaiCitizenID;
import th.in.ffc.widget.IntentBaseAdapter;

/**
 * add description here! please
 *
 * @author piruin panichphol
 * @version 1.1
 * @since Family Folder Collector 2.0
 */
public class VisitMainActivity extends FFCFragmentActivity implements
    PhotoTaker.OnCropFinishListener, View.OnClickListener,
    th.in.ffc.widget.IntentBaseAdapter.OnItemClickListener,
    View.OnLongClickListener, LoaderCallbacks<Cursor> {

    public static final String EXTRA_VISIT_NO = "visitno";
    public static final String EXTRA_VISIT_PATH = "visitpath";
    public static final String EXTRA_VISIT_TABLE = "visittable";

    private static final int REQUEST_VISIT_DEFAULT = 0;
    private static final int REQUEST_VISIT_DIAG = 1;
    private static final int REQUEST_VISIT_OTHER = 2;

    private static final int LOAD_DEATH = 0;
    private static final int LOAD_PERSON = 1;

    private String[] PROJECTION = new String[]{Person.PCUPERSONCODE,
        Person.FULL_NAME, Person.CITIZEN_ID, Person.BIRTH, Person.SEX,
        Person.RIGHT_CODE};

    private String mVisitNo;
    private String mPid;
    private String mPcuCodePerson;
    private PhotoTaker mPhotoTaker;
    private String mPDX;
    private int mAge;
    private int mSex;
    // private TextView mRight;
    private TextView mVisitText;
    private TextView mDX;
    private ImageView mVisitImage;
    private ImageButton mVisitButton;
    private GridView mVisitGrid;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.visit_main_activity);

        mVisitText = (TextView) findViewById(R.id.view1);
        mDX = (TextView) findViewById(R.id.view4);
        mVisitImage = (ImageView) findViewById(R.id.image);
        mVisitImage.setOnClickListener(this);
        mVisitImage.setOnLongClickListener(this);

        mVisitButton = (ImageButton) findViewById(R.id.visit);
        mVisitButton.setOnClickListener(this);

        mVisitGrid = (GridView) findViewById(R.id.grid);
        mProgress = (ProgressBar) findViewById(android.R.id.progress);

        mPhotoTaker = new PhotoTaker(this, this);
        Uri uri = getIntent().getData();
        mPid = uri.getLastPathSegment();
        mVisitNo = getIntent().getStringExtra(Visit.NO);
        if (!TextUtils.isEmpty(mVisitNo))
            mVisitText.setText(getString(R.string.visit) + " #" + mVisitNo);


        mVisitButton.setEnabled(false);
        // Uri deathUri = Uri.withAppendedPath(Death.CONTENT_URI, mPid);
        // Cursor dc = getContentResolver().query(deathUri, new String[]{
        // Death.CAUSE}, null, null, Death.UPDATE);
        // if(dc.moveToFirst()){
        //
        // mVisitImage.setImageResource(R.drawable.ic_launcher_death);
        // mVisitImage.setEnabled(false);
        // }

        if (savedInstanceState != null) {
            mVisitNo = savedInstanceState.getString(Visit.NO);
            if (!TextUtils.isEmpty(mVisitNo))
                mVisitText.setText("Visit #" + mVisitNo);
        }

        getSupportLoaderManager().initLoader(LOAD_PERSON, null, this);

        // Cursor c = getContentResolver().query(uri, PROJECTION, null, null,
        // Person.DEFAULT_SORTING);
        // if (c.moveToFirst()) {
        // mPcuCodePerson = c
        // .getString(c.getColumnIndex(Person.PCUPERSONCODE));
        // String id = c.getString(c.getColumnIndex(Person.CITIZEN_ID));
        // doSetupActionBar(c.getString(c.getColumnIndex(Person.FULL_NAME)),
        // (!TextUtils.isEmpty(id)) ? ThaiCitizenID.parse(id) : null);
        // doSetupGrid(c);
        // } else {
        // this.finish();
        // }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        // Query for Principle DX
        if (!TextUtils.isEmpty(mVisitNo)) {
            ContentResolver cr = getContentResolver();
            Cursor c = cr.query(VisitDiag.CONTENT_URI, new String[]{
                    VisitDiag.CODE, VisitDiag.TYPE},
                "visitno=? AND dxtype='01'", new String[]{mVisitNo,},
                VisitDiag.DEFAULT_SORTING);
            if (c.moveToFirst()) {
                mPDX = c.getString(0);
                mDX.setText(mPDX);
                mDX.setTag((c.getCount() > 1 ? "tag" : null));
                mDX.setVisibility(View.VISIBLE);
                mDX.setOnClickListener(dxListener);
            } else {
                mPDX = null;
                mDX.setVisibility(View.INVISIBLE);
                mDX.setOnClickListener(null);
            }

            File img = new File(FamilyFolderCollector.PHOTO_DIRECTORY_SERVICE,
                mVisitNo.concat(".jpg"));
            if (img.exists())
                mVisitImage.setImageDrawable(Drawable.createFromPath(img
                    .getAbsolutePath()));
        }
    }

    View.OnClickListener dxListener = new View.OnClickListener() {

        String[] projection = new String[]{Diagnosis._ID, Diagnosis._NAME,};

        @Override
        public void onClick(View v) {

            if (!TextUtils.isEmpty(mPDX)) {
                Cursor c = getContentResolver().query(
                    Uri.withAppendedPath(Diagnosis.CONTENT_URI, mPDX),
                    projection, null, null, Diagnosis._ID);
                if (c.moveToFirst()) {
                    QuickAction qa = new QuickAction(VisitMainActivity.this);
                    qa.addActionItem(new ActionItem(R.string.diagnosis, c
                        .getString(1)));
                    qa.show(v);
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                if (!TextUtils.isEmpty(mPDX)) {
                    if (mDX.getTag() != null) {
                        Toast.makeText(this, R.string.hint_more_dx,
                            Toast.LENGTH_SHORT).show();
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put(Visit.TIME_END, DateTime.getCurrentTime());
                        Uri uri = Uri.withAppendedPath(Visit.CONTENT_URI, mVisitNo);
                        int u = getContentResolver().update(uri, cv, null, null);
                        Log.d(TAG, "update = " + u);
                        super.onBackPressed();
                    }
                } else {
                    Toast.makeText(this, R.string.hint_no_dx, Toast.LENGTH_SHORT).show();
                }
                ;
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            String path = savedInstanceState.getString("image");
            if (!TextUtils.isEmpty(path)) {
                OnCropFinsh(path, null);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(mVisitNo))
            outState.putString(Visit.NO, mVisitNo);
        outState.putString("image", mPath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mPhotoTaker != null)
            mPhotoTaker.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_VISIT_DEFAULT:
                if (resultCode == RESULT_OK && data != null) {
                    if (data.getAction().equals(Action.INSERT)) {
                        mVisitNo = data.getData().getLastPathSegment();
                        mVisitText.setText(getString(R.string.visit) + " #"
                            + mVisitNo);
                    }
                }
                break;
            default:
                break;
        }

    }

    public void doSetupGrid(Cursor c) {
        Intent visit = new Intent(Action.INSERT);
        visit.addCategory(Category.VISIT);
        visit.setType(Visit.CONTENT_ITEM_TYPE);

        visit.addCategory((c.getInt(c.getColumnIndex(Person.SEX)) == 1) ? Category.MALE
            : Category.FEMALE);

        mSex = c.getInt(c.getColumnIndex(Person.SEX));

        Date current = Date.newInstance(DateTime.getCurrentDate());
        Date born = Date
            .newInstance(c.getString(c.getColumnIndex(Person.BIRTH)));
        String ageRange = "undefine";
        if (born != null) {
            AgeCalculator cal = new AgeCalculator(current, born);
            Date age = cal.calulate();
            mAge = age.year;
            if (age.year < 2) {
                ageRange = "baby";
                visit.addCategory(Category.BABY);
            } else if (age.year < 12) {
                ageRange = "children";
                visit.addCategory(Category.CHILDREN);
            } else if (age.year < 20) {
                ageRange = "teenage";
                visit.addCategory(Category.TEENAGE);
            } else if (age.year < 60) {
                ageRange = "adult";
                visit.addCategory(Category.ADULT);
            } else {
                ageRange = "elder";
                visit.addCategory(Category.ELDER);
            }
        }


        CustomEvent visitEvent = new CustomEvent("Visit")
            .putCustomAttribute("pcu", getPcuCode())
            .putCustomAttribute("user", getUser())
            .putCustomAttribute("sex", mSex == 1 ? "male" : "female")
            .putCustomAttribute("type", TextUtils.isEmpty(mVisitNo) ? "new" : "edit");
        if (born != null) {
            visitEvent.putCustomAttribute("age", mAge).putCustomAttribute("age-range", ageRange);
        }
        Answers.getInstance().logCustom(visitEvent);

        IntentBaseAdapter adapter = new IntentBaseAdapter(this, visit,
            R.layout.grid_item, R.id.image, R.id.text);
        OnItemClickListener listener = adapter.getOnItemClickListener(this);

        mVisitGrid.setAdapter(adapter);
        mVisitGrid.setOnItemClickListener(listener);

    }

    public boolean doSetupImage() {
        if (TextUtils.isEmpty(mVisitNo))
            return false;

        // File dir = new File(FamilyFolderCollector.PHOTO_DIRECTORY_SERVICE);
        // FilenameFilter filter = new FilenameFilter() {
        //
        // @Override
        // public boolean accept(File dir, String filename) {
        // return filename.startsWith(mVisitNo + "_");
        // }
        // };
        // String[] photo = dir.list(filter);
        String filename = mVisitNo.concat(".jpg");
        // if (photo == null)
        // filename = mVisitNo.concat(".jpg");
        // else
        // filename = mVisitNo.concat("_" + (photo.length + 1) + ".jpg");
        mPhotoTaker.setOutput(FamilyFolderCollector.PHOTO_DIRECTORY_SERVICE,
            filename);
        return true;
    }

    public void doSetupActionBar(String fullname, String citizenId) {

        ActionBar ab = getSupportActionBar();
        if (!TextUtils.isEmpty(fullname))
            ab.setTitle(fullname);
        if (!TextUtils.isEmpty(citizenId))
            ab.setSubtitle(citizenId);
        ab.setDisplayShowHomeEnabled(false);

    }

    private String mPath;

    @Override
    public boolean OnCropFinsh(String path, Uri uri) {
        mVisitImage.setImageDrawable(Drawable.createFromPath(path));
        mPath = path;
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image:
                break;
            case R.id.visit:
                if (TextUtils.isEmpty(mVisitNo)) {
                    Intent visit = new Intent(Action.INSERT);
                    visit.setData(Uri.withAppendedPath(Person.CONTENT_URI, mPid));
                    visit.addCategory(Category.DEFAULT);
                    visit.addCategory(Category.VISIT);
                    VisitActivity.startVisitActivityForResult(this, visit,
                        REQUEST_VISIT_DEFAULT, null, mPid, mPcuCodePerson);
                } else {
                    Intent visit = new Intent(Action.EDIT);
                    visit.addCategory(Category.DEFAULT);
                    visit.addCategory(Category.VISIT);
                    VisitActivity.startVisitActivityForResult(this, visit,
                        REQUEST_VISIT_DEFAULT, mVisitNo, mPid, mPcuCodePerson);
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id, String name, String packageName) {
        if (!TextUtils.isEmpty(mVisitNo)) {
            Intent visit = new Intent();
            visit.setComponent(new ComponentName(packageName, name));
            visit.setAction(Action.VISIT);
            visit.putExtra("AGE", mAge);
            visit.putExtra("SEX", mSex);
            int requestCode = REQUEST_VISIT_OTHER;
            if (packageName.equals("th.in.ffc.person.visit.VisitDiagActivity"))
                requestCode = REQUEST_VISIT_DIAG;

            VisitActivity.startVisitActivityForResult(this, visit, requestCode,
                mVisitNo, mPid, mPcuCodePerson);

        } else {
            QuickAction qa = new QuickAction(this);
            qa.addActionItem(new ActionItem(0,
                    getString(R.string.visit_default_first)));
            qa.show(mVisitButton);
        }

    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.image:
                if (doSetupImage()) {
                    mPhotoTaker.doShowDialog();
                }
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        if (TextUtils.isEmpty(mVisitNo)) {
            super.onBackPressed();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.discard));
            builder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        ProgressDialog p = new ProgressDialog(
                            VisitMainActivity.this);
                        p.setMessage(getString(R.string.please_wait));
                        p.show();

                        Thread t = new Thread(DiscardThread);
                        t.start();
                    }
                });
            builder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            builder.create().show();

        }
    }

    Handler DiscardFinishHandler = new Handler();

    Runnable DiscardThread = new Runnable() {

        @Override
        public void run() {

            doDiscardVisit();

            DiscardFinishHandler.post(new Runnable() {

                @Override
                public void run() {
                    VisitMainActivity.super.onBackPressed();
                }
            });
        }
    };

    public void doDiscardVisit() {
        if (!TextUtils.isEmpty(mVisitNo)) {
            String where = "visitno=? and pcucode=?";
            String[] selectionArgs = new String[]{mVisitNo, getPcuCode()};
            ContentResolver cr = getContentResolver();

            String wheres = "visitno=?";
            String[] selectionArgss = new String[]{mVisitNo};
            // remove ffcmap506radius
            cr.delete(FFC506RADIUS.CONTENT_URI, wheres, selectionArgss);

            // remove default visit
            cr.delete(Visit.CONTENT_URI, where, selectionArgs);
            // remove visit Diagnosis
            cr.delete(VisitDiag.CONTENT_URI, where, selectionArgs);
            // remove visit Drug
            cr.delete(VisitDrug.CONTENT_URI, where, selectionArgs);
            // remove Visit Anc
            cr.delete(VisitAnc.CONTENT_URI, where, selectionArgs);
            // remove Visit Epi
            cr.delete(VisitEpi.CONTENT_URI, where, selectionArgs);
            // remove Visit Individual
            cr.delete(VisitIndividual.CONTENT_URI, where, selectionArgs);
            // remove Visit FP
            cr.delete(VisitFamilyplan.CONTENT_URI, where, selectionArgs);
            // remove Visit Labcancer
            cr.delete(VisitLabcancer.CONTENT_URI, where, selectionArgs);
            // remove Visit Babycare
            cr.delete(VisitBabycare.CONTENT_URI, where, selectionArgs);
            // remove Visit Diag506
            cr.delete(VisitDiag506address.CONTENT_URI, where, selectionArgs);
            // //remove Visit Specialpeson
            // cr.delete(VisitSpecialperson.CONTENT_URI,
            // "pid = ? AND pcucode =?", new String[]{mPid,mPcuCodePerson});
            // //remove Visit Persongrow
            // cr.delete(VisitPersongrow.CONTENT_URI,
            // "pid =? AND pcucodeperson=?", new String[]{mPid,mPcuCodePerson});
            // remove Visit Screenspecialdisease
            cr.delete(VisitScreenspecialdisease.CONTENT_URI, where,
                selectionArgs);
            // remove Visit AncDeliver
            cr.delete(VisitAncDeliver.CONTENT_URI, where, selectionArgs);
            // remove Visit AncMothercare
            cr.delete(VisitAncMotherCare.CONTENT_URI, where, selectionArgs);
            // remove Visit Oldter
            cr.delete(VisitOldter.CONTENT_URI, where, selectionArgs);
            // remove Visit Nutrition
            cr.delete(VisitNutrition.CONTENT_URI, where, selectionArgs);
            // remove Visit DiagAppoint
            cr.delete(VisitDiagAppoint.CONTENT_URI, where, selectionArgs);
            // remove Visit DentalCheck
            cr.delete(VisitDentalCheck.CONTENT_URI, where, selectionArgs);
            // remove Visit Drugdental
            cr.delete(VisitDrugDental.CONTENT_URI, where, selectionArgs);
            cr.delete(VisitDrugDentalDiag.CONTENT_URI, where, selectionArgs);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl;
        switch (id) {
            case LOAD_DEATH:
                Uri deathUri = Uri.withAppendedPath(Death.CONTENT_URI, mPid);
                cl = new CursorLoader(this, deathUri, new String[]{Death.CAUSE},
                    null, null, Death.UPDATE);
                return cl;
            case LOAD_PERSON:
                mProgress.setVisibility(View.VISIBLE);
                Uri uri = getIntent().getData();
                cl = new CursorLoader(this, uri, PROJECTION, null, null,
                    Person.DEFAULT_SORTING);
                return cl;
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        switch (arg0.getId()) {
            case LOAD_DEATH:
                if (c.moveToFirst()) {
                    mVisitImage.setImageResource(R.drawable.ic_launcher_death);
                    mVisitImage.setEnabled(false);
                    mVisitButton.setEnabled(false);
                } else {
                    mVisitButton.setEnabled(true);
                }
                mProgress.setVisibility(View.GONE);
                break;
            case LOAD_PERSON:
                if (c.moveToFirst()) {
                    mPcuCodePerson = c.getString(c
                        .getColumnIndex(Person.PCUPERSONCODE));
                    String id = c.getString(c.getColumnIndex(Person.CITIZEN_ID));
                    doSetupActionBar(
                        c.getString(c.getColumnIndex(Person.FULL_NAME)),
                        (!TextUtils.isEmpty(id)) ? ThaiCitizenID.parse(id)
                            : null);
                    doSetupGrid(c);

                    getSupportLoaderManager().initLoader(LOAD_DEATH, null, this);
                } else {
                    this.finish();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }
}
