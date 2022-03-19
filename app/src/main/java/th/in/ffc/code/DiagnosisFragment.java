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

package th.in.ffc.code;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import th.in.ffc.R;
import th.in.ffc.app.FFCListFragment;
import th.in.ffc.provider.CodeProvider.Diagnosis;
import th.in.ffc.widget.HighLightCursorAdapter;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class DiagnosisFragment extends FFCListFragment implements TextWatcher,
        LoaderCallbacks<Cursor> {

    private HighLightCursorAdapter mAdapter;
    private TextView mInput;
    private String mFilter;

    private static final String[] PROJECTION = new String[]{Diagnosis._ID, Diagnosis.CODE,
            Diagnosis.NAME_ENG,};

    private static final String[] FROM = new String[]{Diagnosis.CODE,
            Diagnosis.NAME_ENG,};
    private static final int[] TO = new int[]{R.id.fullname, R.id.citizenID,};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.person_list_fragment, container, false);
        super.findProgessLayout(view);
        mInput = (TextView) view.findViewById(R.id.search);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        super.showProgess(true);
        if (mInput != null) {
            mInput.addTextChangedListener(this);
            mInput.setVisibility(View.VISIBLE);
        }

        mAdapter = new HighLightCursorAdapter(getActivity(),
                R.layout.person_list_item, null, FROM, TO);
        this.setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Uri uri = null;
        String selection = null;

        if (TextUtils.isEmpty(mFilter) || mFilter.equalsIgnoreCase("-hit")) {
            uri = Uri.withAppendedPath(Diagnosis.CONTENT_URI, "top");
        } else {
            uri = Diagnosis.CONTENT_URI;
            if (mFilter.equals("*")) {
                // do nothing it will automatic get all item
            } else {
                selection = Diagnosis.CODE + " LIKE '%" + mFilter + "%' or "
                        + Diagnosis.NAME_ENG + " LIKE '%" + mFilter + "%' ";
            }
        }

        CursorLoader cl = new CursorLoader(getActivity(), uri, PROJECTION,
                selection, null, null);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {

        mAdapter.swapCursor(c, mFilter);
        showProgess(false);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mAdapter.swapCursor(null);
        showProgess(true);
    }

    @Override
    public void afterTextChanged(Editable s) {
        mFilter = s.toString();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

}
