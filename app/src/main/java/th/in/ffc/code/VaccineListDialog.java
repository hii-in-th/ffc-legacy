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
import th.in.ffc.provider.CodeProvider.Drug;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector plus
 */
public class VaccineListDialog extends DrugListDialog {

    @Override
    public Loader<Cursor> onLoadCursor(String filter) {
        Uri uri = null;
        String selection = Drug.TYPE + " = '05'";

        uri = Drug.CONTENT_URI;
        if (!TextUtils.isEmpty(filter)) {
            if (filter.equals("*")) {
                // do nothing it will automatic get all item
            } else {
                selection = selection + " AND (" + Drug.CODE + " LIKE '"
                        + filter + "%' or " + Drug.NAME + " LIKE '%" + filter
                        + "%' ) ";
            }
        }
        selection = appendWhere(selection);
        selection = appendDefaultWhere(selection);

        CursorLoader cl = new CursorLoader(getActivity(), uri, DrugListDialog.PROJECTION,
                selection, null, Drug.DEFAULT_SORTING);
        return cl;
    }

}
