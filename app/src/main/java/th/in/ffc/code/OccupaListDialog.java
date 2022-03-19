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

package th.in.ffc.code;

import android.database.Cursor;
import android.net.Uri;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import th.in.ffc.R;
import th.in.ffc.app.FFCSearchListDialog.BaseAdapter;
import th.in.ffc.provider.CodeProvider.Occupation;
import th.in.ffc.widget.CursorStringIdAdapter;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector Plus
 */
public class OccupaListDialog extends BaseAdapter {

    public static final String[] PROJECTION = new String[]{
            Occupation._ID,
            Occupation.NAME
    };

    public static final String[] FROM = new String[]{
            Occupation.NAME,
            Occupation._ID,
    };

    public static final int[] TO = new int[]{
            R.id.content,
            R.id.subcontent,
    };

    @Override
    public CursorStringIdAdapter getBaseAdapter() {
        CursorStringIdAdapter adapter = new CursorStringIdAdapter(getActivity(),
                R.layout.default_spinner_item_twoline, null, FROM, TO);
        return adapter;
    }

    @Override
    public Loader<Cursor> onLoadCursor(String filter) {
        Uri uri = Occupation.CONTENT_URI;
        String selection = null;
        if (!TextUtils.isEmpty(filter)) {
            selection = Occupation.NAME + " LIKE '%" + filter + "%' or " +
                    Occupation._ID + " LIKE '" + filter + "%'";
        }
        CursorLoader cl = new CursorLoader(getActivity(), uri,
                PROJECTION, selection, null,
                Occupation._ID);
        return cl;
    }

    @Override
    public Uri getContentUri() {
        return Occupation.CONTENT_URI;
    }

}
