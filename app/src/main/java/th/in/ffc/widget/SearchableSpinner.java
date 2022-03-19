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

package th.in.ffc.widget;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.cursoradapter.widget.CursorAdapter;
import android.util.AttributeSet;
import android.widget.Spinner;
import th.in.ffc.app.FFCSearchListDialog;
import th.in.ffc.app.FFCSearchListDialog.ItemClickListener;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class SearchableSpinner extends Spinner implements ItemClickListener {

    FFCSearchListDialog f;
    FragmentManager fm;
    String tag;
    HighLightCursorAdapter mAdapter;
    static int count = 0;

    public SearchableSpinner(Context context, FragmentManager fm,
                             Class<? extends FFCSearchListDialog> cls, String tag) {
        super(context);

        setDialog(fm, cls, tag);
    }

    public SearchableSpinner(Context context) {
        super(context);
    }

    public SearchableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchableSpinner setDialog(FragmentManager fm,
                                       Class<? extends FFCSearchListDialog> cls, Bundle args, String tag) {

        this.tag = tag;

        this.fm = fm;
        Fragment prev = fm.findFragmentByTag(tag);
        if (prev != null)
            f = (FFCSearchListDialog) prev;
        else
            f = (FFCSearchListDialog) Fragment.instantiate(getContext(),
                    cls.getName(), args);

        Cursor c = getContext().getContentResolver().query(f.getContentUri(),
                f.getProjection(), null, null, BaseColumns._ID);

        mAdapter = new HighLightCursorAdapter(getContext(), f.getLayout(), c,
                f.getFrom(), f.getTo());
        f.setItemClickListener(this);
        this.setAdapter(mAdapter);

        return this;
    }

    public SearchableSpinner setDialog(FragmentManager fm,
                                       Class<? extends FFCSearchListDialog> cls, String tag) {
        return setDialog(fm, cls, null, tag);
    }

    @Override
    public boolean performClick() {
        boolean handle = false;
        if (!handle && fm != null) {
            FragmentTransaction ft = fm.beginTransaction();
            Fragment prev = fm.findFragmentByTag(tag);
            if (prev != null)
                ft.remove(prev);
            ft.addToBackStack(null);

            if (f != null) {
                f.setItemClickListener(this);
                f.show(ft, tag);

            }
        }
        return handle;
    }


    public void setSelectionById(long id) {
        this.setSelection(binarySearch(mAdapter, id, 0, mAdapter.getCount()),
                true);
    }

    private static final int binarySearch(CursorAdapter adapter, long id, int min,
                                          int max) {
        if (max < min) {
            return 0;
        }
        int mid = (max + min) / 2;

        long curId = adapter.getItemId(mid);
        if (curId > id)
            return binarySearch(adapter, id, min, mid - 1);
        else if (curId < id)
            return binarySearch(adapter, id, mid + 1, max);
        else
            return mid;
    }

    @Override
    public void onItemClick(HighLightCursorAdapter adapter, long id,
                            int position) {
        f.dismiss();
        this.setSelection(binarySearch(mAdapter, id, 0, mAdapter.getCount()),
                true);
    }

    @Override
    public void onItemClick(CursorStringIdAdapter adapter, String id,
                            String text) {
        throw new IllegalArgumentException("SearchableSpinner not support for CursorStringIdAdapter dialog");
    }

}
