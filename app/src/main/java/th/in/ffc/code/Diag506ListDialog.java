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
import th.in.ffc.provider.CodeProvider.Diagnosis;
import th.in.ffc.widget.CursorStringIdAdapter;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class Diag506ListDialog extends FFCSearchListDialog.BaseAdapter {

    private static final String[] PROJECTION = new String[]{Diagnosis._ID,
            Diagnosis._NAME, Diagnosis.NAME_ENG};

    private static final String[] FROM = new String[]{Diagnosis._NAME, Diagnosis.NAME_ENG, Diagnosis._ID,
    };

    private static final int[] TO = new int[]{R.id.content, R.id.subcontent, R.id.code};


    @Override
    public CursorStringIdAdapter getBaseAdapter() {
        CursorStringIdAdapter mAdapter = new CursorStringIdAdapter(getActivity(),
                R.layout.diag_list_item, null, FROM, TO);
        return mAdapter;
    }

    @Override
    public Loader<Cursor> onLoadCursor(String filter) {
        Uri uri = Diagnosis.CONTENT_URI;
        String selection = Diagnosis.CODE506 + " IS NOT NULL";
        if (!TextUtils.isEmpty(filter)) {
            selection = Diagnosis.CODE506 + " IS NOT NULL AND (" + Diagnosis.CODE + " LIKE '%" + filter + "%' or "
                    + Diagnosis.NAME_ENG + " LIKE '%" + filter + "%' or "
                    + Diagnosis.NAME_TH + " LIKE '%" + filter + "%')";
            System.out.println(selection);
        }

        CursorLoader cl = new CursorLoader(getActivity(), uri, PROJECTION,
                selection, null, null);
        return cl;
    }

    @Override
    public Uri getContentUri() {
        return Diagnosis.CONTENT_URI;
    }


}
