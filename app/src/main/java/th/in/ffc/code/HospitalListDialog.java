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
import th.in.ffc.R;
import th.in.ffc.app.FFCSearchListDialog;
import th.in.ffc.provider.CodeProvider.Hospital;
import th.in.ffc.widget.CursorStringIdAdapter;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class HospitalListDialog extends FFCSearchListDialog.BaseAdapter {

    public static final String[] PROJECTION = new String[]{
            Hospital._ID,
            Hospital.NAME,
    };

    public static final String[] FROM = new String[]{
            Hospital.NAME,
            Hospital._ID,
    };

    public static final int[] TO = new int[]{
            R.id.content,
            R.id.subcontent,
    };


    @Override
    public Loader<Cursor> onLoadCursor(String filter) {
        Uri uri = Hospital.CONTENT_URI;
        String selection = null;
        if (!TextUtils.isEmpty(filter)) {
            selection = Hospital._ID + " LIKE '" + filter + "%' OR "
                    + Hospital.NAME + " LIKE '%" + filter + "%' ";
        }
        selection = appendWhere(selection);
        selection = appendDefaultWhere(selection);

        CursorLoader cl = new CursorLoader(getActivity(), uri, PROJECTION, selection, null, Hospital._ID);
        return cl;
    }

    @Override
    public Uri getContentUri() {
        return Hospital.CONTENT_URI;
    }


    @Override
    public CursorStringIdAdapter getBaseAdapter() {
        CursorStringIdAdapter adapter = new CursorStringIdAdapter(getActivity(),
                R.layout.default_spinner_item_twoline, null, FROM, TO);
        return adapter;
    }

}
