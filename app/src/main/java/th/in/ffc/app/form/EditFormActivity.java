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

package th.in.ffc.app.form;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import me.piruin.quickaction.ActionItem;
import me.piruin.quickaction.QuickAction;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.app.form.EditFormActivity.EditFragment.RemoveRequestListener;
import th.in.ffc.intent.Action;
import th.in.ffc.util.DateTime.Date;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.widget.ArrayFormatSpinner;
import th.in.ffc.widget.SearchableButton;

/**
 * add description here!
 *
 * @author piruin panichphol
 * @version 1.0
 * @since 1.0
 */
public class EditFormActivity extends FFCFragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mEditList = savedInstanceState.getStringArrayList("edit");
            mDeleteList = savedInstanceState.getStringArrayList("delete");

            for (String tag : mEditList) {
                EditFragment f = (EditFragment) getSupportFragmentManager().findFragmentByTag(tag);
                if (f != null)
                    f.setRemoveRequestListener(this.mRemoveListener);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("edit", mEditList);
        outState.putStringArrayList("delete", mDeleteList);
    }

    private ArrayList<String> mEditList = new ArrayList<String>();
    private ArrayList<String> mDeleteList = new ArrayList<String>();


    /**
     * @return List of fragment's tag was added by addEditFragment()
     */
    public ArrayList<String> getEditList() {
        return mEditList;
    }

    /**
     * @return List of EditFragment added by addEditFragment()
     * @since Family Folder Collector Plus
     */
    public ArrayList<EditFragment> getEditFragmentList() {
        ArrayList<EditFragment> list = new ArrayList<EditFragment>();
        for (String tag : mEditList) {
            EditFragment f = (EditFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (f != null)
                list.add(f);
        }
        return list;
    }

    /**
     * @return List of Action Edit fragmetn's tag that was delete by user
     */
    public ArrayList<String> getDeleteList() {
        return mDeleteList;
    }

    private RemoveRequestListener mRemoveListener = new RemoveRequestListener() {

        @Override
        public void onRemove(String action, String tag, String key) {
            Log.d(TAG, "remove call tag=" + tag);
            EditFragment f = (EditFragment) getSupportFragmentManager()
                    .findFragmentByTag(tag);
            if (f != null) {
                preRemoveEditFragment(f);

                FragmentTransaction ft = getSupportFragmentManager()
                        .beginTransaction();
                ft.remove(f);
                ft.commit();
            }
        }
    };

    protected void preRemoveEditFragment(EditFragment f) {
        if (f.action.equals(Action.EDIT)) {
            mDeleteList.add(f.key);
        }

        mEditList.remove(f.tag);
    }

    protected void preAddEditFregment(EditFragment f, String tag) {
        f.setRemoveRequestListener(this.mRemoveListener);
        mEditList.add(tag);

        //Log.d(TAG, "post tag="+f.tag);
    }

    /**
     * @param f   EditFragment for add to R.id.content Layout
     * @param tag Tag for add Fragment must use as same as tag that for f
     * @since Family Folder Collector Plus
     */
    protected void addEditFragment(EditFragment f, String tag) {
        addEditFragment(R.id.content, f, tag);
    }

    /**
     * @param layout Id of layout to add fragment
     * @param f      EditFragment for add to R.id.content Layout
     * @param tag    Tag for add Fragment must use as same as tag that for f
     * @since Family Folder Collector Plus
     */
    protected void addEditFragment(int layout, EditFragment f, String tag) {
        EditFragment prev = (EditFragment) getSupportFragmentManager()
                .findFragmentByTag(tag);
        if (prev == null) {

            FragmentTransaction ft = getSupportFragmentManager()
                    .beginTransaction();
            ft.add(layout, f, tag);
            ft.commit();

            preAddEditFregment(f, tag);
        }
    }

    public static abstract class EditFragment extends ViewFormFragment {

        public String tag;
        public String action;
        public String key;


        public Bundle getBaseArguments(String action, String tag, String key) {
            Bundle args = new Bundle();
            args.putString("arg_action", action);
            args.putString("arg_key", key);
            args.putString("arg_tag", tag);

            return args;
        }


        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            Bundle args = getArguments();
            if (args == null)
                throw new IllegalStateException("EditFragment need argument");

            action = args.getString("arg_action");
            if (TextUtils.isEmpty(action))
                throw new IllegalStateException("must use getBaseArguments in getInstance()");
            key = args.getString("arg_key");
            tag = args.getString("arg_tag");

            if (savedInstanceState != null) {
                action = savedInstanceState.getString("action");
                key = savedInstanceState.getString("key");
                tag = savedInstanceState.getString("tag");
            }

        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            outState.putString("action", action);
            outState.putString("key", key);
            outState.putString("tag", tag);
        }

        protected void setAsRemoveButton(View view) {
            if (view != null)
                view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "remove click");
                        if (getmRemoveListener() != null)
                            getmRemoveListener().onRemove(action, tag, key);


                    }
                });
        }


        private RemoveRequestListener mRemoveListener;

        public void setRemoveRequestListener(RemoveRequestListener listener) {
            setmRemoveListener(listener);
        }

        public interface RemoveRequestListener {
            void onRemove(String action, String tag, String key);
        }

        public abstract boolean onSave(EditTransaction et);


        public RemoveRequestListener getmRemoveListener() {
            return mRemoveListener;
        }


        public void setmRemoveListener(RemoveRequestListener mRemoveListener) {
            this.mRemoveListener = mRemoveListener;
        }
    }

    private EditTransaction mEditTrans;

    public EditTransaction beginTransaction() {
        mEditTrans = new EditTransaction(this);
        return mEditTrans;
    }

    public EditTransaction getOpenedTransaction() {
        return mEditTrans;
    }

    public static class EditTransaction {
        private ContentValues mCv;
        private boolean mHaveData;
        private boolean mError;
        private View mErrorView;
        private Context mContext;

        protected EditTransaction(Context context) {
            mContext = context;
            beginTransaction();
        }

        private void beginTransaction() {
            mCv = new ContentValues();
            mError = false;
            mHaveData = false;
        }

        public ContentValues getContentValues() {
            return mCv;
        }

        private boolean checkInput(ContentValues cv, String col, String data,
                                   boolean allowNull, View view) {
            if (cv == null || col == null || mError)
                return false;
            if (TextUtils.isEmpty(data)) {
                if (!allowNull) {
                    QuickAction qa = new QuickAction(mContext);
                    qa.addActionItem(new ActionItem(0, "null"));
                    qa.show(view);

                    mError = true;
                }
                return false;
            } else {
                return true;
            }
        }

        public void showErrorMessage(View view, String msg) {
            if (!TextUtils.isEmpty(msg)) {
                mErrorView = view;
                QuickAction qa = new QuickAction(mContext);
                qa.addActionItem(new ActionItem(0, msg));
                qa.show(mErrorView);
            }
        }


        private boolean retrieveData(ContentValues cv, String col,
                                     EditText view, boolean allowNull, String regex, String msg) {
            String data = view.getText().toString().trim();
            if (!checkInput(cv, col, data, allowNull, view))
                return false;

            if (!TextUtils.isEmpty(regex)) {
                if (!data.matches(regex)) {
                    showErrorMessage(view, msg);
                    mError = true;
                    return false;
                }
            }
            cv.put(col, data);
            mHaveData = true;
            return true;
        }

        private boolean retrieveData(ContentValues cv, String col,
                                     TextView view, boolean allowNull, String regex, String msg) {
            String data = view.getText().toString().trim();
            if (!checkInput(cv, col, data, allowNull, view))
                return false;

            if (!TextUtils.isEmpty(regex)) {
                if (!data.matches(regex)) {
                    showErrorMessage(view, msg);
                    mError = true;
                    return false;
                }
            }
            cv.put(col, data);
            mHaveData = true;
            return true;
        }

        private boolean retrieveData(ContentValues cv, String col,
                                     SearchableButton view, boolean allowNull, String regex,
                                     String msg) {
            String data = view.getSelectId();
            if (!checkInput(cv, col, data, allowNull, view))
                return false;
            if (!TextUtils.isEmpty(regex)) {
                if (!data.matches(regex)) {
                    showErrorMessage(view, msg);
                    mError = true;
                    return false;
                }
            }
            cv.put(col, data);
            mHaveData = true;
            return true;
        }

        private boolean retrieveData(ContentValues cv, String col,
                                     ThaiDatePicker view, boolean allowNull, String regex, String msg) {
            String data = view.getDate().toString();
            if (!checkInput(cv, col, data, allowNull, view))
                return false;

            if (!TextUtils.isEmpty(regex)) {
                if (!data.matches(regex)) {
                    showErrorMessage(view, msg);
                    mError = true;
                    return false;
                }
            }
            cv.put(col, data);
            mHaveData = true;
            return true;
        }

        private boolean retrieveData(ContentValues cv, String col,
                                     EditText view, boolean allowNull, int minRange, int maxRange,
                                     String msg) {
            String data = view.getText().toString().trim();
            if (!checkInput(cv, col, data, allowNull, view))
                return false;

            try {
                int iData = Integer.parseInt(data);
                if (iData < minRange || iData > maxRange) {
                    showErrorMessage(view, msg);
                    mError = true;
                    return false;
                } else {
                    cv.put(col, iData);
                    mHaveData = true;
                    return true;
                }
            } catch (NumberFormatException nfe) {
                showErrorMessage(view,
                        mContext.getString(R.string.invalid_number_format));
                mError = true;
                return false;
            }
        }

        private boolean retrieveData(ContentValues cv, String col,
                                     EditText view, boolean allowNull, float min, float max,
                                     String msg) {
            String data = view.getText().toString().trim();
            if (!checkInput(cv, col, data, allowNull, view))
                return false;

            try {
                float fData = Float.parseFloat(data);
                if (fData < min || fData > max) {
                    showErrorMessage(view, msg);
                    mError = true;
                    return false;
                } else {
                    String number = new DecimalFormat("#.##").format(fData);
                    cv.put(col, number);

                    mHaveData = true;
                    return true;
                }
            } catch (NumberFormatException nfe) {
                showErrorMessage(view,
                        mContext.getString(R.string.invalid_number_format));
                mError = true;
                return false;
            }

        }

        public boolean retrieveDate(String col, Spinner view, int stringArrayId) {
            if (!checkInput(mCv, col, null, true, view))
                return false;

            String[] array = mContext.getResources().getStringArray(
                    stringArrayId);
            int select = view.getSelectedItemPosition();
            if (select > (array.length - 1) || select < 0) {
                throw new InvalidParameterException("array select out of range");
            }

            String item = array[select];
            mCv.put(col, item.substring(0, item.indexOf(":")));
            mHaveData = true;
            return true;

        }

        private boolean retrieveData(ContentValues cv, String col, ThaiDatePicker view,
                                     Date min, Date max, String msg) {
            Date select = view.getDate();
            if (min != null && select.compareTo(min) == Date.LESS_THAN) {
                showErrorMessage(view, msg);
                mError = true;
                return false;
            }
            if (max != null && select.compareTo(max) == Date.MORETHAN) {
                showErrorMessage(view, msg);
                mError = true;
                return false;
            }
            cv.put(col, select.toString());
            mHaveData = true;
            return true;


        }

        public boolean retrieveData(String col, ThaiDatePicker view,
                                    boolean allowNull, String regex, String msg) {
            if (mCv == null)
                throw new IllegalStateException(
                        "Must start Transection with beginTransection() before");
            return retrieveData(mCv, col, view, allowNull, regex, msg);
        }

        public boolean retrieveData(String col, SearchableButton view,
                                    boolean allowNull, String regex, String msg) {
            if (mCv == null)
                throw new IllegalStateException(
                        "Must start Transection with beginTransection() before");
            return retrieveData(mCv, col, view, allowNull, regex, msg);
        }

        public boolean retrieveData(String col, EditText view,
                                    boolean allowNull, float min, float max, String msg) {
            if (mCv == null)
                throw new IllegalStateException(
                        "Must start Transection with beginTransection() before");
            return retrieveData(mCv, col, view, allowNull, min, max, msg);
        }

        public boolean retrieveData(String col, EditText view,
                                    boolean allowNull, int minRange, int maxRange, String msg) {
            if (mCv == null)
                throw new IllegalStateException(
                        "Must start Transection with beginTransection() before");
            return retrieveData(mCv, col, view, allowNull, minRange, maxRange,
                    msg);
        }

        public boolean retrieveData(String col, EditText view,
                                    boolean allowNull, String regex, String msg) {
            if (mCv == null)
                throw new IllegalStateException(
                        "Must start Transection with beginTransection() before");
            return retrieveData(mCv, col, view, allowNull, regex, msg);
        }

        public boolean retrieveData(String col, ThaiDatePicker view,
                                    Date min, Date max, String msg) {
            if (mCv == null)
                throw new IllegalStateException(
                        "Must start Transection with beginTransection() before");
            return retrieveData(mCv, col, view, min, max, msg);
        }

        public boolean retrieveData(String col, ArrayFormatSpinner view, boolean allowNull, String regex, String msg) {
            if (mCv == null)
                throw new IllegalStateException(
                        "Must start Transection with beginTransection() before");
            return retrieveData(mCv, col, view, allowNull, regex, msg);
        }

        private boolean retrieveData(ContentValues cv, String col,
                                     ArrayFormatSpinner view, boolean allowNull, String regex,
                                     String msg) {
            String data = view.getSelectionId();
            if (!checkInput(cv, col, data, allowNull, view))
                return false;
            if (!TextUtils.isEmpty(regex)) {
                if (!data.matches(regex)) {
                    showErrorMessage(view, msg);
                    mError = true;
                    return false;
                }
            }
            cv.put(col, data);
            mHaveData = true;
            return true;
        }

        public boolean retrieveData(String col, TextView view, boolean allowNull, String regex, String msg) {
            if (mCv == null)
                throw new IllegalStateException(
                        "Must start Transection with beginTransection() before");
            return retrieveData(mCv, col, view, allowNull, regex, msg);
        }

        /**
         * this commit will insert Transaction to database
         *
         * @param uri for insert values
         * @return URI of content that just insert or null if update not success
         * @since 1.0
         */
        public Uri commit(Uri uri) {
            if (mCv == null)
                throw new IllegalStateException(
                        "Must start Transection with beginTransection() before");
            if (mError || !mHaveData)
                return null;
            return mContext.getContentResolver().insert(uri, mCv);
        }

        /**
         * Like commit(Uri uri) but this method not check any Error.So, use it wisely.
         *
         * @since Family Folder Collector Plus
         */
        public Uri forceCommit(Uri uri) {
            if (mCv == null)
                throw new IllegalStateException(
                        "Must start Transection with beginTransection() before");
            return mContext.getContentResolver().insert(uri, mCv);
        }

        /**
         * this commit will update Transaction to database
         *
         * @param uri           for update value
         * @param selection
         * @param selectionArgs
         * @return number of record that was updated
         * @since 1.0
         */
        public int commit(Uri uri, String selection, String[] selectionArgs) {
            if (mCv == null)
                throw new IllegalStateException(
                        "Must start Transection with beginTransection() before");
            if (mError || !mHaveData)
                return 0;

            return mContext.getContentResolver().update(uri, mCv, selection,
                    selectionArgs);
        }

        /**
         * Like commit(Uri uri, String selection, String[] selectionArgs) but this
         * method not check any Error. So, use it wisely.
         *
         * @since Family Folder Collector Plus
         */
        public int forceCommit(Uri uri, String selection, String[] selectionArgs) {
            if (mCv == null)
                throw new IllegalStateException(
                        "Must start Transection with beginTransection() before");

            return mContext.getContentResolver().update(uri, mCv, selection,
                    selectionArgs);
        }

        /**
         * check weather transaction can commit or not
         *
         * @return true if can commit
         * @since 1.0
         */
        public boolean canCommit() {
            return !mError && mHaveData;
        }

        public boolean isError() {
            return mError;
        }

        public static interface CommitListener {
            public void onCommit(Uri uri, ContentValues Values);
        }
    }
}
