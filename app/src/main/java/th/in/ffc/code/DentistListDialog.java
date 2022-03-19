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
import th.in.ffc.provider.CodeProvider.UserOfficerType;
import th.in.ffc.widget.CursorStringIdAdapter;

/**
 * add description here!
 *
 * @author Atiwat Vacharapongkosin
 * @version 1.0
 * @since 1.0
 */
public class DentistListDialog extends FFCSearchListDialog.BaseAdapter {


    private static final String[] PROJECTION = new String[]{
            UserOfficerType._ID,
            UserOfficerType._NAME,
            UserOfficerType.USERNAME,
            UserOfficerType.FULLNAME,
            UserOfficerType.OFFICERTYPE,
            UserOfficerType.OFFICERPOSITIONNAME
    };

    private static final String[] FROM = new String[]{
            UserOfficerType.FULLNAME,
            UserOfficerType.OFFICERPOSITIONNAME,
    };
    private static final int[] TO = new int[]{
            R.id.fullname,
            R.id.citizenID,
    };

    @Override
    public Loader<Cursor> onLoadCursor(String filter) {
        Uri uri = UserOfficerType.CONTENT_URI;
        String selection = "(" + UserOfficerType.OFFICERTYPE + " = '6' OR " + UserOfficerType.OFFICERTYPE + " ='7')";
        if (!TextUtils.isEmpty(filter)) {
            selection = selection + " AND ( " + UserOfficerType._ID + " LIKE '" + filter + "%' OR "
                    + UserOfficerType.FULLNAME + " LIKE '%" + filter + "%' )";
        }

        CursorLoader cl = new CursorLoader(getActivity(), uri, PROJECTION, selection, null, UserOfficerType._ID);
        return cl;
    }

    @Override
    public Uri getContentUri() {
        return UserOfficerType.CONTENT_URI;
    }


    @Override
    public CursorStringIdAdapter getBaseAdapter() {
        CursorStringIdAdapter adapter = new CursorStringIdAdapter(getActivity(),
                R.layout.person_list_no_pic_item, null, FROM, TO);
        return adapter;
    }

}
