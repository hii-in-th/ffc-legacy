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

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import th.in.ffc.R;
import th.in.ffc.app.FFCListFragment;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.provider.HouseProvider.Village;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.widget.HighLightCursorAdapter;

/**
 * add description here! please
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public class HouseListFragment extends FFCListFragment implements
        OnItemSelectedListener, TextWatcher, LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private Spinner mVillageSpinner;
    private TextView mHouseTextView;

    private static final String[] VILL_PROJECTION = new String[]{Village._ID,
            Village.VILLNAME, Village.HOUSE_COUNT, Village.POSTCODE};
    private static final String[] VILL_FROM = new String[]{Village.VILLNAME,
            Village.HOUSE_COUNT,};

    private static final int[] VILL_TO = new int[]{R.id.village,
            R.id.content};

    private static final String[] HOUSE_PROJECTION = new String[]{House._ID,
            House.HNO, Person.FULL_NAME};
    private static final String[] HOUSE_FROM = new String[]{House.HNO,
            Person.FULL_NAME, House._ID,};
    private static final int[] HOUSE_TO = new int[]{R.id.hno, R.id.fullname,
            R.id.code,};

    private HighLightCursorAdapter mHouseAdapter;

    private long mVillId = -1;
    private String mHouseFilter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.house_list_fragment, container,
                false);

        mVillageSpinner = (Spinner) view.findViewById(R.id.village);
        mHouseTextView = (TextView) view.findViewById(R.id.search);

        this.findProgessLayout(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ContentResolver cr = getFFCActivity().getContentResolver();

        if (mVillageSpinner != null) {
            Cursor villCursor = cr.query(
                    Uri.withAppendedPath(Village.CONTENT_URI, "house"),
                    VILL_PROJECTION, null, null, "village.villcode DESC");
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    getFFCActivity(), R.layout.village_list_item, villCursor,
                    VILL_FROM, VILL_TO, SimpleAdapter.NO_SELECTION);
            mVillageSpinner.setAdapter(adapter);
            mVillageSpinner.setOnItemSelectedListener(this);

            if (villCursor.moveToFirst())
                mVillId = villCursor.getLong(0);
        }

        if (mHouseTextView != null) {
            mHouseTextView.addTextChangedListener(this);
        }

        showProgess(true);
        mHouseAdapter = new HighLightCursorAdapter(getFFCActivity(),
                R.layout.house_list_item, null, HOUSE_FROM, HOUSE_TO);
        this.setListAdapter(mHouseAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCursor = null;
        mVillageSpinner = null;
        mHouseFilter = null;
        mHouseTextView = null;
        mHouseAdapter = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent house = new Intent(Action.VIEW);
        house.addCategory(Category.HOUSE);
        house.setType(House.CONTENT_ITEM_TYPE);
        house.setData(Uri.withAppendedPath(House.CONTENT_URI, "" + id));
        getFFCActivity().startActivity(house);
    }

    // implementation of OnItemSelectionListener
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long id) {

        mVillId = id;
        Log.d(TAG, "villcode=" + mVillId);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }

    // implementation of TextWatcher
    @Override
    public void afterTextChanged(Editable arg0) {

        mHouseFilter = arg0.toString();
        getLoaderManager().restartLoader(0, null, this);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    // LoaderCallbacks<Cursor>
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

        showProgess(true);
        Uri uri = House.getHouseUriByVillCode(mVillId);

        String selection = null;
        if (!TextUtils.isEmpty(mHouseFilter))
            selection = House.HNO + " LIKE '%" + mHouseFilter + "%' or "
                    + Person.FULL_NAME + " LIKE '%" + mHouseFilter + "%' or "
                    + House._ID + " LIKE '" + mHouseFilter + "%'";

        return new CursorLoader(getFFCActivity(), uri, HOUSE_PROJECTION,
                selection, null, House.DEFAULT_SORTING);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        mCursor = c;
        Log.d(TAG, "houseCount" + c.getCount());
        showProgess(false);
        if (mHouseAdapter != null) {
            mHouseAdapter.swapCursor(mCursor, mHouseFilter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mHouseAdapter != null) {
            showProgess(true);
            mHouseAdapter.swapCursor(null);
        }
    }

}
