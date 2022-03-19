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
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.Protagonist;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class PersonProtagonistListDialog extends PersonListDialog {

    private static final String[] PROJECTION = new String[]{
            Protagonist._ID,
            Person.PCUPERSONCODE,
            Person.CITIZEN_ID,
            Person.FULL_NAME,
    };

    public static final String KEY_TYPE = "persontype";
    public static final String TYPE_VOLA = "09";
    public static final String TYPE_HEALTH_HEAD = "10";


    @Override
    public Loader<Cursor> onLoadCursor(String filter) {
        String selection = null;
        Bundle args = getArguments();

        if (args != null) {
            String type = args.getString(KEY_TYPE);
            if (!TextUtils.isEmpty(type))
                selection = Protagonist.TYPE + "='" + type + "'";
        }

        Uri uri = Protagonist.CONTENT_URI;
        if (!TextUtils.isEmpty(filter)) {
            selection = (selection == null) ? "" : selection + " AND ";
            selection += Person.FULL_NAME + " LIKE '%" + filter + "%' or " +
                    Person.CITIZEN_ID + " LIKE '" + filter + "%'";
        }
        selection = appendWhere(selection);
        selection = appendDefaultWhere(selection);

        CursorLoader cl = new CursorLoader(getActivity(), uri,
                PROJECTION, selection, null,
                Protagonist.DEFAULT_SORTING);

        return cl;
    }

    @Override
    public Uri getContentUri() {
        return Protagonist.CONTENT_URI;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }


}
