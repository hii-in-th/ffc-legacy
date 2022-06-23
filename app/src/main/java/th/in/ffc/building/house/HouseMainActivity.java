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

package th.in.ffc.building.house;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;
import com.blayzupe.phototaker.PhotoTaker;
import th.in.ffc.FamilyFolderCollector;
import th.in.ffc.R;
import th.in.ffc.app.FFCTabsPagerActivity;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.map.FGActivity;
import th.in.ffc.map.database.DatabaseManager;
import th.in.ffc.map.system.FGSystemManager;
import th.in.ffc.map.value.MARKER_TYPE;
import th.in.ffc.map.village.spot.Spot;
import th.in.ffc.person.PersonFragment;
import th.in.ffc.person.PersonListFragment;
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.provider.HouseProvider.Village;
import th.in.ffc.provider.PersonProvider.Person;

import java.io.*;
import java.util.Collection;

import th.in.ffc.security.LoginActivity;

/**
 * add description here! please
 *
 * @author piruin panichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public class HouseMainActivity extends FFCTabsPagerActivity {

    PhotoTaker mPhotoTaker;
    ImageView mImage;
    String mPhotoPath;
    String mPhotoThrumb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.house_main_activity);

        setShowPagerTitleStrip(true);
        setShowTabWidget(false);

        Uri data = getIntent().getData();

        Bundle personListArgs = new Bundle();
        personListArgs.putString(PersonFragment.EXTRA_HCODE,
                data.getLastPathSegment());
        personListArgs
                .putBoolean(PersonListFragment.EXTRA_DISABLE_SEARCH, true);

        Bundle houseArgs = new Bundle();
        houseArgs.putString("pcucode", getPcuCode());
        houseArgs.putString("hcode", data.getLastPathSegment());

        TabsPagerAdapter adapter = new TabsPagerAdapter(this, getTabHost(),
                getViewPager());

        adapter.addTab("Person", PersonListFragment.class, personListArgs);
        adapter.addTab("Detail", HouseDetailViewFragment.class, houseArgs);
        adapter.addTab("Food", HouseFoodViewFragment.class, houseArgs);
        adapter.addTab("Sanitation", HouseSanitationViewFragment.class,
                houseArgs);
        adapter.addTab("Carrier", HouseCarrierViewFragment.class, houseArgs);
        adapter.addTab("Water", HouseWaterViewFragment.class, houseArgs);

        setTabsPagerAdapter(adapter);

        doSetupActionBar(data.getLastPathSegment());

        // Uri houseUri = Uri.withAppendedPath(House.CONTENT_URI,
        // data.getLastPathSegment());
        // Cursor c = getContentResolver().query(houseUri,
        // new String[] { House.VILLCODE }, null, null,
        // House.DEFAULT_SORTING);
        // if (c.moveToFirst()) {

        Toast.makeText(HouseMainActivity.this , getPcuCode()+data.getLastPathSegment(), Toast.LENGTH_LONG).show();
        doSetupImage(getPcuCode().concat("h" + data.getLastPathSegment())
                .concat(".jpg"));
        mPhotoThrumb = getPcuCode().concat("h" + data.getLastPathSegment())
                .concat("_thumb.jpg");
        // }
    }

    private void doSetupActionBar(String hcode) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Uri houseUri = Uri.withAppendedPath(House.CONTENT_URI, hcode);

        String[] projection = new String[]{House.HCODE, House.HNO, House.PID, House.VILLCODE};
        Cursor c1 = getContentResolver().query(houseUri, projection, null,
                null, House.DEFAULT_SORTING);
        if (c1.moveToFirst()) {

            String villcode = c1.getString(3);
            Uri villageUri = Uri
                    .withAppendedPath(Village.CONTENT_URI, villcode);
            Cursor villcursor = getContentResolver().query(villageUri,
                    new String[]{Village.VILLNO}, null, null, Village.DEFAULT_SORTING);
            if (villcursor.moveToFirst()) {
                String villno = villcursor.getString(0);
                if (villno.equals("0"))
                    mGenogramable = false;

            }

            TextView code = (TextView) findViewById(R.id.code);
            code.setText(getString(R.string.shape).concat(c1.getString(0)));
            String hno = c1.getString(1);
            if (hno.matches("\\d.*"))
                hno = getString(R.string.houseNo) + " " + hno;
            getSupportActionBar().setTitle(hno);

            if (!TextUtils.isEmpty(c1.getString(2))) {
                Uri personUri = Uri.withAppendedPath(Person.CONTENT_URI,
                        c1.getString(2));
                Cursor c2 = getContentResolver().query(personUri,
                        new String[]{Person.FULL_NAME}, null, null,
                        Person.DEFAULT_SORTING);
                if (c2.moveToFirst()) {
                    getSupportActionBar().setSubtitle(c2.getString(0));
                }
            }
        }
    }
    boolean mGenogramable = true;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.house_main, menu);
        if (!mGenogramable) {
            menu.removeItem(R.id.genogram);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.genogram:

                Intent genome = new Intent(Action.GENOGRAM);
                genome.putExtra(House.HCODE, Integer.parseInt(getIntent().getData()
                        .getLastPathSegment()));
                genome.putExtra(House.PCUCODE, getPcuCode());
                startActivity(genome);
                return true;
            case R.id.map:
                Intent map = new Intent(Action.VIEW);
                map.addCategory(Category.MAP);
                map.putExtra("hcode", getIntent().getData().getLastPathSegment());
                map.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(map);
                return true;
            case android.R.id.home:
                startHomeActivity();
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void doSetupImage(String name) {
        mImage = (ImageView) findViewById(R.id.image);
        if (mImage == null)
            throw new IllegalArgumentException("Ivalid Resource Layout");

        mImage.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                mPhotoTaker.doShowDialog();
                return true;
            }
        });

        File pick = new File(FamilyFolderCollector.PHOTO_DIR_HOUSE, name.indexOf("tmp_")>0?name:"tmp_"+name);
        mPhotoPath = pick.getAbsolutePath();
        if (pick.exists()) {
            mImage.setImageDrawable(Drawable.createFromPath(mPhotoPath));
            if (getResources().getBoolean(R.bool.landscape))
                mImage.setScaleType(ScaleType.FIT_CENTER);
            else
                mImage.setScaleType(ScaleType.CENTER_CROP);
        }

        mPhotoTaker = new PhotoTaker(this,
                FamilyFolderCollector.PHOTO_DIR_HOUSE, name.indexOf("tmp_")>0?name:"tmp_"+name);

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (mImage != null) {

            File pic = new File(mPhotoPath);
            if (pic.exists()) {
                mImage.setImageDrawable(Drawable.createFromPath(mPhotoPath));
                File thumb = new File(FamilyFolderCollector.PHOTO_DIR_HOUSE,
                        mPhotoThrumb);

                copyFile(pic, thumb);

            }
        }

        Drawable img = Drawable.createFromPath(mPhotoPath);
        if (img != null) {
            mImage.setImageDrawable(img);
            if (getResources().getBoolean(R.bool.landscape))
                mImage.setScaleType(ScaleType.FIT_CENTER);
            else
                mImage.setScaleType(ScaleType.CENTER_CROP);
        }
    }

    public void copyFile(File afile, File bfile) {
        InputStream inStream = null;
        OutputStream outStream = null;

        try {

            inStream = new FileInputStream(afile);
            outStream = new FileOutputStream(bfile);

            byte[] buffer = new byte[1024];

            int length;
            // copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {

                outStream.write(buffer, 0, length);

            }

            inStream.close();
            outStream.close();

            System.out.println("File is copied successful!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        mPhotoTaker.setContext(getBaseContext());
        mPhotoTaker.onActivityResult(arg0, arg1, arg2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPhotoTaker = null;
    }
}
