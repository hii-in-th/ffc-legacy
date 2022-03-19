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
import androidx.cursoradapter.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import th.in.ffc.R;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since 1.0
 */
public class HighLightCursorAdapter extends CursorAdapter {

    private int mLayout;
    private String[] mFrom;
    private int[] mTo;
    protected String mHighLight;
    protected int mColor;

    /**
     * @param context
     * @param c
     */
    public HighLightCursorAdapter(Context context, int layout, Cursor c,
                                  String[] from, int[] to) {
        super(context, c, false);
        mLayout = layout;
        mFrom = from;
        mTo = to;

        mColor = context.getResources().getColor(R.color.holo_green_light);

    }

    public Cursor swapCursor(Cursor newCursor, String hl) {
        mHighLight = hl;
        if (!TextUtils.isEmpty(mHighLight))
            mHighLight = mHighLight.toLowerCase();

        return super.swapCursor(newCursor);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        mHighLight = null;
        return super.swapCursor(newCursor);
    }

    public String getData(int position, int columnIndex) {
        Cursor c = getCursor();
        if (c.moveToPosition(position)) {
            return c.getString(columnIndex);
        }
        return null;
    }

    public String getData(int position, String columnName) {
        int columnIndex = getCursor().getColumnIndex(columnName);
        return getData(position, columnIndex);
    }


    @Override
    public void bindView(View view, Context context, Cursor c) {
        for (int i = 0; i < mFrom.length; i++) {
            TextView tv = (TextView) view.findViewById(mTo[i]);
            if (tv != null) {

                String text = c.getString(mCursor.getColumnIndex(mFrom[i]));

                TextViewHighLighter.highLight(tv, text, mHighLight, mColor);
            }
        }
    }

    @Override
    public View newView(Context context, Cursor c, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(mLayout, parent, false);
        bindView(v, context, c);

        return v;
    }

}
