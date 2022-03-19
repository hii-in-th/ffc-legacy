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
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import th.in.ffc.R;
import th.in.ffc.app.FFCSearchListDialog;
import th.in.ffc.person.PersonFragment;
import th.in.ffc.provider.CodeProvider.District;
import th.in.ffc.provider.CodeProvider.Province;
import th.in.ffc.provider.CodeProvider.Subdistrict;
import th.in.ffc.widget.CursorStringIdAdapter;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class SubDistrictListDialog extends FFCSearchListDialog.BaseAdapter {

    private String mDistcode;
    private String mProvcode;

    private static final String[] PROJECTION = new String[]{Subdistrict._ID,
            Subdistrict._NAME,

    };

    private static final String[] FROM = new String[]{Subdistrict._NAME,
            Subdistrict._ID};
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
        Bundle args = getArguments();
        if (args != null) {
            mProvcode = args.getString(PersonFragment.EXTRA_PROVINCE);
            mDistcode = args.getString(PersonFragment.EXTRA_DISTRICT);
        }

        Uri uri = getContentUri();
        String selection = "";
        if (!TextUtils.isEmpty(filter)) {
            selection = "(" + Subdistrict._NAME + " LIKE '%" + filter + "%' or "
                    + Subdistrict._ID + " LIKE '" + filter + "%')";
        }
        selection = appendWhere(selection);
        if (!TextUtils.isEmpty(mDistcode) && !TextUtils.isEmpty(mProvcode)) {
            selection = "(" + District.DISTCODE + "=" + mDistcode + " AND " + Province.PROVCODE + "=" + mProvcode + ") AND";
        }

        CursorLoader cl = new CursorLoader(getActivity(), uri, PROJECTION,
                selection, null, Subdistrict._ID);

        return cl;
    }

    @Override
    public Uri getContentUri() {
        return Subdistrict.CONTENT_URI;
    }

    public void setMDISTCODE(String code) {
        this.mDistcode = code;
    }

    public void setMPROVCODE(String code) {
        this.mProvcode = code;
    }
}
