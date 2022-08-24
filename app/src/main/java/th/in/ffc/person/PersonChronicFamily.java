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
 * th.in.ffc.security.LoginActivity Project
 *
 * Copyright (C) 2010-2012 National Electronics and Computer Technology Center
 * All Rights Reserved.
 * 
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * 
 */

package th.in.ffc.person;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import th.in.ffc.R;
import th.in.ffc.provider.CodeProvider.Diagnosis;
import th.in.ffc.provider.PersonProvider.FamilyChronic;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector Plus
 */
public class PersonChronicFamily extends PersonFragment implements
        LoaderCallbacks<Cursor> {

    private static final int LOAD_FAMILY = 43;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        addTitle(R.string.familychronic);
        showProgess(true);
        getActivity().getSupportLoaderManager().initLoader(LOAD_FAMILY, null,
                this);
    }

    private static final String[] PROJ = new String[]{
            FamilyChronic.RELATION_CODE, FamilyChronic.CHRONIC_CODE,};

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        switch (arg0) {
            case LOAD_FAMILY:
                Uri uri = Uri.withAppendedPath(FamilyChronic.CONTENT_URI, getPID());
                CursorLoader cl = new CursorLoader(getActivity(), uri, PROJ, null,
                        null, FamilyChronic.DEFAULT_SORTING);
                return cl;
        }
        return null;
    }

    private void doShowFamilyChronic(Cursor c) {
        showProgess(false);
        mForm.removeAllViewsInLayout();

        addTitle(R.string.familychronic);
        if(!c.isClosed()) {
            if (c != null && c.moveToFirst()) {
                String lastChronic = "";
                do {
                    String chronic = c.getString(0);
                    if (!lastChronic.equals(chronic)) {
                        addSubject(chronic);
                        addContentQuery(
                                null,
                                Diagnosis.NAME_TH,
                                Uri.withAppendedPath(Diagnosis.CONTENT_URI, chronic),
                                null);

                        lastChronic = chronic;
                    }
                    addSubContent(c.getString(1));

                } while (c.moveToNext());

//                c.close();
            }
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
        final Cursor cur = c;
        switch (arg0.getId()) {
            case LOAD_FAMILY:
                Handler handler = new Handler();
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        doShowFamilyChronic(cur);
                    }
                });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

}
