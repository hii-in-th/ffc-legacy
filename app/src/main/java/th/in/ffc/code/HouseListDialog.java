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
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.provider.HouseProvider.Village;
import th.in.ffc.widget.CursorStringIdAdapter;
import th.in.ffc.widget.HighLightCursorAdapter;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector plus
 */
public class HouseListDialog extends FFCSearchListDialog {

    private static final String[] PROJECTION = new String[]{
            House._ID,
            House.HNO,
            Village.VILLNAME,
    };

    private static final String[] FROM = new String[]{
            House._ID,
            House.HNO,
            Village.VILLNAME
    };

    private static final int[] TO = new int[]{
            R.id.code,
            R.id.hno,
            R.id.fullname,
    };

    private static final Uri uri = Uri.withAppendedPath(House.CONTENT_URI, "village");

    @Override
    public HighLightCursorAdapter getCursorAdapter() {
        HighLightCursorAdapter adapter = new HighLightCursorAdapter(getActivity(),
                R.layout.house_list_item, null, FROM, TO);
        return adapter;
    }

    @Override
    public CursorStringIdAdapter getBaseAdapter() {
        return null;
    }

    @Override
    public Loader<Cursor> onLoadCursor(String filter) {
        String selection = null;
        String[] selectionArgs = null;

        if (!TextUtils.isEmpty(filter)) {
            selection = "house.hno LIKE '%" + filter + "%' or " +
                    "house.hcode LIKE '" + filter + "%' or " +
                    "village.villname LIKE '%" + filter + "%'";
        }
        selection = appendWhere(selection);
        selection = appendDefaultWhere(selection);

        CursorLoader cl = new CursorLoader(getActivity(), uri,
                PROJECTION, selection, selectionArgs, House.DEFAULT_SORTING);
        return cl;
    }

    @Override
    public Uri getContentUri() {
        return uri;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public String[] getFrom() {
        return FROM;
    }

    @Override
    public int[] getTo() {
        return TO;
    }

    @Override
    public int getLayout() {
        return R.layout.house_list_item;
    }

}
