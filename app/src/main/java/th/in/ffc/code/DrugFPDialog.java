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
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import th.in.ffc.R;
import th.in.ffc.app.FFCSearchListDialog;
import th.in.ffc.provider.CodeProvider.Drug;
import th.in.ffc.widget.CursorStringIdAdapter;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class DrugFPDialog extends FFCSearchListDialog.BaseAdapter {

    public static final String[] PROJECTION = new String[]{
            Drug._NAME, Drug._ID, Drug.COST, Drug.SELL,
    };

    public static final int[] TO = new int[]{
            R.id.name,
            R.id.code,
            R.id.content,
            R.id.subcontent,
    };


    @Override
    public CursorStringIdAdapter getBaseAdapter() {
        CursorStringIdAdapter adapter = new CursorStringIdAdapter(getActivity(),
                R.layout.drug_list_item, null, PROJECTION, TO);
        return adapter;
    }

    @Override
    public Loader<Cursor> onLoadCursor(String filter) {
        Uri uri = Drug.CONTENT_URI;
        String selection = Drug.TYPE + " = '04'";
        selection = appendDefaultWhere(selection);
        if (!TextUtils.isEmpty(filter)) {
            selection = selection + " AND (" + Drug.CODE + " LIKE '"
                    + filter + "%' or " + Drug.NAME + " LIKE '%" + filter
                    + "%' ) ";
        }


        Log.d(getTag() + "DIALOG", selection);
        CursorLoader cl = new CursorLoader(getActivity(), uri, PROJECTION, selection, null, Drug.DEFAULT_SORTING);
        return cl;
    }

    @Override
    public Uri getContentUri() {
        return Drug.CONTENT_URI;
    }

}

	
