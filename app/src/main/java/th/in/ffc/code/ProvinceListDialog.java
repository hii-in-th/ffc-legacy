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
import th.in.ffc.provider.CodeProvider.Province;
import th.in.ffc.widget.CursorStringIdAdapter;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class ProvinceListDialog extends FFCSearchListDialog.BaseAdapter {


    private static final String[] PROJECTION = new String[]{
            Province._ID,
            Province._NAME,

    };

    private static final String[] FROM = new String[]{
            Province._NAME,
            Province._ID
    };
    private static final int[] TO = new int[]{
            R.id.content,
            R.id.subcontent
    };


    @Override
    public CursorStringIdAdapter getBaseAdapter() {
        CursorStringIdAdapter mAdapter = new CursorStringIdAdapter(getActivity(),
                R.layout.default_spinner_item_twoline, null, FROM, TO);
        return mAdapter;
    }

    @Override
    public Loader<Cursor> onLoadCursor(String filter) {

        Uri uri = getContentUri();
        String selection = null;
        if (!TextUtils.isEmpty(filter)) {
            selection = Province._NAME + " LIKE '%" + filter + "%' or " +
                    Province._ID + " LIKE '" + filter + "%'";
        }
        selection = appendWhere(selection);
        selection = appendDefaultWhere(selection);

        CursorLoader cl = new CursorLoader(getActivity(), uri,
                PROJECTION, selection, null,
                Province._ID);

        return cl;
    }

    @Override
    public Uri getContentUri() {
        return Province.CONTENT_URI;
    }


}
