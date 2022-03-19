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
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import th.in.ffc.R;
import th.in.ffc.app.FFCSearchListDialog;
import th.in.ffc.app.FFCSearchListDialog.ItemClickListener;
import th.in.ffc.provider.CodeProvider.NameColumn;

/**
 * Custom view to replace Searchable Spinner for use with non-long id data
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since 1.0
 */
public class SearchableButton extends Button implements ItemClickListener {

    public SearchableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setBackgroundResource(R.drawable.spinner_background);
        this.setGravity(Gravity.LEFT);
        this.setMaxLines(2);

        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        this.setTextColor(getResources().getColor(
                android.R.color.primary_text_light));
        float density = getResources().getDisplayMetrics().density;
        int paddingLR = (int) (getResources()
                .getDimension(R.dimen.text_padding) * density);
        int paddingT = (int) (getResources().getDimensionPixelSize(
                R.dimen.edittext_padding_top) * density);
        int paddingB = (int) (getResources().getDimensionPixelSize(
                R.dimen.edittext_padding_buttom) * density);
        this.setPadding(paddingLR, paddingT, paddingLR, paddingB);
    }

    private String selectId;

    private String tag;
    private FragmentManager fm;
    private FFCSearchListDialog f;

    /**
     * @param fm   support Fragment manager for Activity or Fragment
     * @param cls  class of Search List Dialog to show when button was click
     * @param args arguments for Search List Dialog @see FFCSearchListDialog
     * @param tag  String tag to use with Fragment manager for handle fragment
     *             transaction
     * @return this object
     * @since 1.0
     */
    public SearchableButton setDialog(FragmentManager fm,
                                      Class<? extends FFCSearchListDialog.BaseAdapter> cls, Bundle args,
                                      String tag) {

        this.tag = tag;

        this.fm = fm;
        Fragment prev = fm.findFragmentByTag(tag);
        if (prev != null)
            f = (FFCSearchListDialog) prev;
        else
            f = (FFCSearchListDialog) Fragment.instantiate(getContext(),
                    cls.getName(), args);

        f.setItemClickListener(this);

        return this;
    }

    public SearchableButton setDialog(FragmentManager fm,
                                      Class<? extends FFCSearchListDialog.BaseAdapter> cls, String tag) {
        return this.setDialog(fm, cls, null, tag);
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
                handle = true;
            }
        }
        return handle;
    }


    public String getSelectId() {
        return this.selectId;
    }

    /**
     * use String ID for set selection ID and show content of that ID
     *
     * @param id to set as select id
     * @since 1.0
     */
    public void setSelectionById(String id) {
        if (!TextUtils.isEmpty(id)) {

            Uri uri = Uri.withAppendedPath(f.getContentUri(), id);
            Cursor c = getContext().getContentResolver().query(uri,
                    new String[]{NameColumn._NAME}, null, null, null);

            if (c.moveToFirst()) {
                this.selectId = id;
                this.setText(c.getString(0));
                Log.d("SB", c.getString(0));
            } else {
                this.setText("not found");
            }
        } else {
            this.setText("Null id");
        }
    }

    // ADD BY JEY FOR DISTRICT AND SUBDISTRICT QUERY
    public void setSelectionById(String id, String partialSelection) {
        Uri uri = f.getContentUri();
        Cursor c = getContext().getContentResolver().query(uri,
                new String[]{NameColumn._NAME}, partialSelection,
                new String[]{id}, null);
        if (c.moveToFirst()) {
            this.selectId = id;
            this.setText(c.getString(0));
            Log.d("SB", c.getString(0));
        } else {
            this.setText("not found");
        }
    }

    @Override
    public void onItemClick(HighLightCursorAdapter adapter, long id,
                            int position) {
        throw new IllegalArgumentException(
                "SearchableButton not support for HighLightCursorAdapter");
    }

    @Override
    public void onItemClick(CursorStringIdAdapter adapter, String id,
                            String text) {
        f.dismiss();
        this.selectId = id;
        this.setText(text);
        this.getSelectId();

        if (mListener != null) {
            mListener.onItemSelect(id);

        }
    }

    private OnItemSelectListener mListener;

    public void setOnItemSelectListener(OnItemSelectListener listener) {
        this.mListener = listener;
    }

    public static interface OnItemSelectListener {
        public void onItemSelect(String id);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.id = this.selectId;
        ss.text = this.getText().toString();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        // end

        this.selectId = ss.id;
        this.setText(ss.text);
    }

    static class SavedState extends BaseSavedState {
        String id;
        String text;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.id = in.readString();
            this.text = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(this.id);
            out.writeString(this.text);
        }

        // required field that makes Parcelables from a Parcel
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

    }

}
