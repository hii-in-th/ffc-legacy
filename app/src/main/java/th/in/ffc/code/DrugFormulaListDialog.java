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
import th.in.ffc.app.FFCSearchListDialog;
import th.in.ffc.provider.CodeProvider.DrugFormula;
import th.in.ffc.widget.CursorStringIdAdapter;
import th.in.ffc.widget.HighLightCursorAdapter;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector Plus
 */
public class DrugFormulaListDialog extends FFCSearchListDialog {

    private static final String[] FROM = new String[]{DrugFormula._NAME,
            DrugFormula._ID};
    private static final int[] TO = new int[]{R.id.content, R.id.subcontent};

    @Override
    public HighLightCursorAdapter getCursorAdapter() {
        HighLightCursorAdapter adapter = new HighLightCursorAdapter(
                getActivity(), R.layout.default_spinner_item_twoline, null,
                FROM, TO);
        return adapter;
    }

    @Override
    public CursorStringIdAdapter getBaseAdapter() {
        return null;
    }

    @Override
    public Loader<Cursor> onLoadCursor(String filter) {
        String selection = null;
        if (!TextUtils.isEmpty(filter)) {
            selection = DrugFormula._ID + "=" + filter + " OR "
                    + DrugFormula._NAME + " LIKE '%" + filter + "%'";
        }
        CursorLoader cl = new CursorLoader(getActivity(),
                DrugFormula.CONTENT_URI, FROM, selection, null,
                DrugFormula.DEFAULT_SORTING);
        return cl;
    }

    @Override
    public Uri getContentUri() {
        return DrugFormula.CONTENT_URI;
    }

    @Override
    public String[] getProjection() {
        return FROM;
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
        return R.layout.default_spinner_item;
    }

}
