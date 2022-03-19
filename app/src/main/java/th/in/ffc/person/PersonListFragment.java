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

package th.in.ffc.person;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;
import th.in.ffc.R;
import th.in.ffc.app.FFCListFragment;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.util.Log;
import th.in.ffc.widget.HighLightCursorAdapter;

/**
 * add description here! please
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public class PersonListFragment extends FFCListFragment implements TextWatcher,
        LoaderCallbacks<Cursor> {

    private static final String[] PROJECTION = new String[]{
            Person._ID,
            Person.PCUPERSONCODE,
            Person.CITIZEN_ID,
            Person.FULL_NAME,
    };

    private static final String[] FROM = new String[]{
            Person.FULL_NAME,
            Person.CITIZEN_ID,
    };
    private static final int[] TO = new int[]{
            R.id.fullname,
            R.id.citizenID,
    };

    public static final String EXTRA_DISABLE_SEARCH = "search";

    private PersonCursorAdapter mAdapter;
    private TextView mInput;
    private String mFilter;
    private String mHcode;

    private boolean mDisableSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.person_list_fragment, container,
                false);
        mInput = (TextView) view.findViewById(R.id.search);
        super.findProgessLayout(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        this.setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null) {
            mHcode = args.getString(PersonFragment.EXTRA_HCODE);
            mDisableSearch = args.getBoolean(PersonListFragment.EXTRA_DISABLE_SEARCH);
        }

        if (mInput != null)
            mInput.addTextChangedListener(this);

        super.showProgess(true);
        mAdapter = new PersonCursorAdapter(getFFCActivity(),
                R.layout.person_list_item, null, FROM, TO);
        this.setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        HighLightCursorAdapter apt = (HighLightCursorAdapter) l.getAdapter();
        String pcucodeperson = apt.getData(position, Person.PCUPERSONCODE);
        Intent intent = new Intent(Action.VIEW);
        intent.addCategory(Category.PERSON);
        PersonActivity.startPersonActivity(getActivity(), intent, "" + id, pcucodeperson);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter = null;
        mInput = null;
        mFilter = null;
        mHcode = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.person_list, menu);
        if (mDisableSearch) {
            MenuItem item = menu.findItem(R.id.search);
            item.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case R.id.add:
                Intent add = new Intent(Action.INSERT);
                add.addCategory(Category.PERSON);
                add.addCategory(Category.DEFAULT);
                if (mHcode != null) {
                    add.putExtra(Person.HCODE, mHcode);
                }
                getFFCActivity().startActivity(add);
                break;
            case R.id.search:
                InputMethodManager imm = (InputMethodManager) getFFCActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (!mInput.isShown()) {
                    mInput.setVisibility(View.VISIBLE);
                    mInput.requestFocus();
                    imm.showSoftInput(mInput, InputMethodManager.SHOW_FORCED);
                    item.setIcon(R.drawable.ic_action_close);
                } else {
                    imm.hideSoftInputFromInputMethod(mInput.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    mInput.setVisibility(View.GONE);
                    mInput.setText(null);
                    item.setIcon(R.drawable.ic_action_search);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSearchRequest() {
        if (!mInput.isShown()) {
            mInput.setVisibility(View.VISIBLE);
            mInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getFFCActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mInput, InputMethodManager.SHOW_FORCED);
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

        // this.setListShown(false);

        Uri uri = Person.CONTENT_URI;
        if (mHcode != null) {
            uri = Uri.withAppendedPath(Person.CONTENT_URI, "house/" + mHcode);
        }

        String selection = null;
        String[] selectArgs = null;
        if (!TextUtils.isEmpty(mFilter)) {
            if (mFilter.matches("\\d{1,13}")) {
                selection = "person.idcard LIKE '" + mFilter + "%'";
            } else if (mFilter.matches("\\d-\\d{4}-\\d{5}-\\d{2}-\\d")) {
                Log.d(TAG, "Match");
                mFilter = mFilter.replaceAll("-", "");
                selection = "person.idcard LIKE '" + mFilter + "%'";

            } else {
                selection = Person.FULL_NAME + " LIKE '%" + mFilter + "%'";
            }
        }
        CursorLoader cl = new CursorLoader(getFFCActivity(), uri,
                PersonListFragment.PROJECTION, selection, selectArgs,
                Person.DEFAULT_SORTING);

        return cl;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {

        if (mAdapter != null)
            mAdapter.swapCursor(c, mFilter);
        super.showProgess(false);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

        super.showProgess(true);
        if (mAdapter != null)
            mAdapter.swapCursor(null);

    }

    @Override
    public String toString() {
        return "PersonListFragment [mInput=" + mInput + ", mFilter=" + mFilter
                + ", mHcode=" + mHcode + "]";
    }

}
