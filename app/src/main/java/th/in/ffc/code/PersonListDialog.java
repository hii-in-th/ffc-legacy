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
import th.in.ffc.person.PersonCursorAdapter;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.widget.CursorStringIdAdapter;
import th.in.ffc.widget.HighLightCursorAdapter;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class PersonListDialog extends FFCSearchListDialog {


    protected static final String[] PROJECTION = new String[]{
            Person._ID,
            Person.CITIZEN_ID,
            Person.PCUPERSONCODE,
            Person.FULL_NAME,
            Person.FIRST_NAME,
    };

    private static final String[] FROM = new String[]{
            Person.FULL_NAME,
            Person.CITIZEN_ID,
    };
    private static final int[] TO = new int[]{
            R.id.fullname,
            R.id.citizenID,
    };

    @Override
    public HighLightCursorAdapter getCursorAdapter() {
        PersonCursorAdapter adapter = new PersonCursorAdapter(
                getActivity(), R.layout.person_list_item, null, FROM, TO);
        return adapter;
    }

    @Override
    public CursorStringIdAdapter getBaseAdapter() {
        return null;
    }

    @Override
    public Loader<Cursor> onLoadCursor(String filter) {
        Uri uri = getContentUri();
        String selection = null;

        if (!TextUtils.isEmpty(filter)) {
            selection = Person.FULL_NAME + " LIKE '%" + filter + "%' or " +
                    Person.CITIZEN_ID + " LIKE '" + filter + "%'";
        }
        selection = appendWhere(selection);
        selection = appendDefaultWhere(selection);

        CursorLoader cl = new CursorLoader(getActivity(), uri,
                PROJECTION, selection, null,
                Person._ID);

        return cl;
    }

    @Override
    public Uri getContentUri() {
        return Person.CONTENT_URI;
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
        return R.layout.person_list_no_pic_item;
    }

}
