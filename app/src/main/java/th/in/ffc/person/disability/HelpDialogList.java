package th.in.ffc.person.disability;
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

import android.database.Cursor;
import android.net.Uri;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import th.in.ffc.R;
import th.in.ffc.app.FFCSearchListDialog;
import th.in.ffc.provider.CodeProvider.PersonHelp;
import th.in.ffc.widget.CursorStringIdAdapter;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class HelpDialogList extends FFCSearchListDialog.BaseAdapter {


    private static final String[] PROJECTION = new String[]{PersonHelp._ID,
            PersonHelp._NAME,

    };

    private static final String[] FROM = new String[]{PersonHelp._NAME,
            PersonHelp._ID};
    private static final int[] TO = new int[]{R.id.content, R.id.subcontent};

    @Override
    public CursorStringIdAdapter getBaseAdapter() {
        CursorStringIdAdapter mAdapter = new CursorStringIdAdapter(
                getActivity(), R.layout.default_spinner_item_twoline, null,
                FROM, TO);
        return mAdapter;
    }

    @Override
    public Loader<Cursor> onLoadCursor(String filter) {

        Uri uri = getContentUri();
        String selection = "";
        if (!TextUtils.isEmpty(filter)) {
            selection = "(" + PersonHelp._NAME + " LIKE '%" + filter + "%' or "
                    + PersonHelp._ID + " LIKE '" + filter + "%')";
        }

        CursorLoader cl = new CursorLoader(getActivity(), uri, PROJECTION,
                selection, null, PersonHelp._ID);

        return cl;
    }

    @Override
    public Uri getContentUri() {
        return PersonHelp.CONTENT_URI;
    }

}
