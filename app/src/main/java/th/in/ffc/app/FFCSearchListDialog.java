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

package th.in.ffc.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import th.in.ffc.R;
import th.in.ffc.provider.CodeProvider.NameColumn;
import th.in.ffc.widget.CursorStringIdAdapter;
import th.in.ffc.widget.HighLightCursorAdapter;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since 1.0
 */
public abstract class FFCSearchListDialog extends DialogFragment implements
        TextWatcher, LoaderCallbacks<Cursor>, OnItemClickListener {

    public static final String EXTRA_NOT_SEARCHABLE = "not_searchable";
    /**
     * Where cause that always append to tail of selection
     */
    public static final String EXTRA_APPEND_WHERE = "append";
    /**
     * This where will add only when no user's filter
     */
    public static final String EXTRA_DEFAULT_WHERE = "default";

    private ListView mListView;
    private LinearLayout mProgessLayout;
    private ImageButton mClose;
    private EditText mEditText;
    private HighLightCursorAdapter mAdapter;
    private CursorStringIdAdapter mBadapter;
    private ItemClickListener mListener;
    private Cursor mCursor;

    private String mFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.default_searchable_list,
                container, false);
        mListView = (ListView) view.findViewById(android.R.id.list);
        mAdapter = getCursorAdapter();
        if (mAdapter != null)
            mListView.setAdapter(mAdapter);
        else {
            mBadapter = getBaseAdapter();
            if (mBadapter != null)
                mListView.setAdapter(mBadapter);
            else
                throw new IllegalArgumentException(
                        "not found any adapter for listView");
        }
        mListView.setOnItemClickListener(this);

        mEditText = (EditText) view.findViewById(R.id.search);
        mEditText.addTextChangedListener(this);

        Bundle args = getArguments();
        if (args != null) {
            mEditText
                    .setVisibility(args.getBoolean(EXTRA_NOT_SEARCHABLE) ? View.GONE
                            : View.VISIBLE);
        }
        mProgessLayout = (LinearLayout) view
                .findViewById(android.R.id.progress);

        mClose = (ImageButton) view.findViewById(R.id.deleted);
        mClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mEditText.setText(null);
            }
        });
        getLoaderManager().initLoader(0, null, this);

        return view;
    }

    protected String appendWhere(String selection) {

        if (getArguments() != null) {
            String extra = getArguments().getString(EXTRA_APPEND_WHERE);
            if (TextUtils.isEmpty(extra))
                return selection;
            String append = " (" + extra + ") ";
            if (!TextUtils.isEmpty(selection))
                selection = " (" + selection + ") AND " + append;
            else
                selection = append;
        }
        return selection;
    }

    /**
     * Add where cause that
     */
    protected String appendDefaultWhere(String selection) {
        if (!TextUtils.isEmpty(mFilter)) {
            return selection;
        }

        if (getArguments() != null) {
            String extra = getArguments().getString(EXTRA_DEFAULT_WHERE);
            if (TextUtils.isEmpty(extra))
                return selection;
            String append = " (" + extra + ") ";
            if (!TextUtils.isEmpty(selection))
                selection = " (" + selection + ") AND " + append;
            else
                selection = append;
        }
        return selection;
    }

    public void setAdapter(HighLightCursorAdapter adapter) {
        this.mAdapter = adapter;
    }

    public void setAdapter(CursorStringIdAdapter adapter) {
        this.mBadapter = adapter;
    }

    protected void setShowEditText(boolean show) {
        mEditText.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (mListener != null) {
            if (mAdapter != null)
                mListener.onItemClick(mAdapter, arg3, arg2);
            else if (mBadapter != null) {
                int name_col = mCursor.getColumnIndex(NameColumn._NAME);
                mCursor.moveToPosition(arg2);
                mListener.onItemClick(mBadapter, (String) mBadapter.getItem(arg2),
                        (name_col > -1) ? mCursor.getString(name_col) : null);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(0);
        mListView = null;
        mListener = null;
        mEditText = null;
        mAdapter = null;
        mBadapter = null;
        if (mCursor != null)
            mCursor.close();
        mCursor = null;
        mProgessLayout = null;
        mFilter = null;
    }

    public abstract HighLightCursorAdapter getCursorAdapter();

    public abstract CursorStringIdAdapter getBaseAdapter();

    public void setItemClickListener(ItemClickListener listener) {
        this.mListener = listener;
    }

    protected void showProgess(boolean show) {
        if (mProgessLayout != null) {
            if (show)
                mProgessLayout.setVisibility(View.VISIBLE);
            else
                mProgessLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        mFilter = s.toString();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        showProgess(true);
        return onLoadCursor(mFilter);
    }

    public abstract Loader<Cursor> onLoadCursor(String filter);

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        if (mAdapter != null) {
            mAdapter.swapCursor(null);
        }
        if (mBadapter != null) {
            mBadapter.swapCursor(null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        mCursor = arg1;
        if (mAdapter != null) {
            mAdapter.swapCursor(arg1, mFilter);
            showProgess(false);
        }
        if (mBadapter != null) {
            mBadapter.swapCursor(arg1, mFilter);
            showProgess(false);
        }
    }

    public static interface ItemClickListener {
        public void onItemClick(HighLightCursorAdapter adapter, long id,
                                int position);

        public void onItemClick(CursorStringIdAdapter adapter, String id,
                                String text);
    }

    public abstract Uri getContentUri();

    public abstract String[] getProjection();

    public abstract String[] getFrom();

    public abstract int[] getTo();

    public abstract int getLayout();

    /**
     * abstract class that already implements some method of FFCSearchListDialog
     * for use with SearchableButton
     *
     * @author Piruin Panichphol
     * @version 1.0
     * @since Family Folder Collector plus
     */
    public abstract static class BaseAdapter extends FFCSearchListDialog {

        @Override
        final public HighLightCursorAdapter getCursorAdapter() {
            return null;
        }

        @Override
        final public String[] getProjection() {
            return null;
        }

        @Override
        final public String[] getFrom() {
            return null;
        }

        @Override
        final public int[] getTo() {
            return null;
        }

        @Override
        final public int getLayout() {
            return 0;
        }

    }

}
