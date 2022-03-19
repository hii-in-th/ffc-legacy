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
import android.database.Cursor;
import android.net.Uri;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;
import th.in.ffc.R;

import java.util.ArrayList;
import java.util.List;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class EditFormFragment extends ViewFormFragment {

    private List<EditableForm> mList = new ArrayList<EditableForm>();

    protected boolean buildForm() {
        if (mList.size() == 0)
            return false;

        for (EditableForm ef : mList) {
            addSubject(ef.getSubject());

            ef.addForm(mForm);
        }
        return true;
    }

    protected ContentValues getContentValues() {
        if (mList.size() == 0)
            return null;

        ContentValues cv = new ContentValues();
//		for (EditableForm ef : mList) {
//			//cv.put(ef.getColumnName(), ef.getValue());
//		}
        return cv;
    }

    protected int addEditForm(EditableForm edit) {
        mList.add(edit);
        return mList.size();
    }

    protected LinearLayout findFromLayout(View parent) {
        return findFormLayout(parent, android.R.id.list);
    }

    protected LinearLayout findFormLayout(View parent, int id) {
        return mForm = (LinearLayout) parent.findViewById(id);
    }

    public static abstract class EditableForm {

        private String mColumn;
        private String mSubject;
        //private View View;

        public EditableForm(String subject, String column) {
            if (TextUtils.isEmpty(subject) || TextUtils.isEmpty(column))
                throw new IllegalArgumentException("subject or column name is empty!");
            this.mColumn = column;
            this.mSubject = subject;
        }


        abstract public String getValue();

        abstract void addForm(LinearLayout parent);

        public String getSubject() {
            return mSubject;
        }

        public String getColumnName() {
            return mColumn;
        }


    }

    public class TextEditableForm extends EditableForm {


        protected String mDefaultValue;
        protected String mHint;
        protected EditText mEdit;
        protected LayoutParams mParams;

        //		private String regex;
        public void setRegEx(String regex) {
//			this.regex = regex;
        }

        public TextEditableForm(String subject, String column,
                                String defaultValue, String hint) {
            super(subject, column);

            this.mEdit = new EditText(mContext);
            if (!TextUtils.isEmpty(defaultValue))
                mEdit.setText(defaultValue);

            if (!TextUtils.isEmpty(hint))
                mEdit.setHint(hint);

//			mParams = new LayoutParams(LayoutParams.MATCH_PARENT,
//					LayoutParams.WRAP_CONTENT);
//			mParams.setMargins(PADDING, 2, PADDING, PADDING);
//			mParams.weight = 1;
        }


        @Override
        public String getValue() {
            if (mEdit != null)
                return mEdit.getText().toString();
            return null;
        }


        @Override
        public void addForm(LinearLayout parent) {
            parent.addView(mEdit, mParams);
        }

    }

    public class TextWithSpinnerEditableForm extends TextEditableForm implements
            OnItemSelectedListener {

        /**
         * @param subject
         * @param column
         * @param defaultValue
         * @param hint
         */
        public TextWithSpinnerEditableForm(String subject, String column,
                                           String defaultValue, String hint) {
            super(subject, column, defaultValue, hint);

        }

        private String[] mStrArray;
        private String[] mFrom;
        private final int[] TO = new int[]{R.id.content, R.id.subcontent};
        private Uri mUri;
        private Spinner mSpinner;
        private String mSorting;

        /**
         * @param mSubject
         * @param mColumnName
         * @param mDefaultValue
         * @param mHint
         */


        @Override
        public void addForm(LinearLayout parent) {
            LinearLayout ly = new LinearLayout(mContext);
            ly.setOrientation(LinearLayout.HORIZONTAL);
            mParams.weight = 1;
            mParams.width = 0;
            mParams.height = LayoutParams.MATCH_PARENT;
            ly.addView(mEdit, mParams);

            mSpinner = new Spinner(mContext);
            if (mStrArray != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        mContext, android.R.layout.simple_spinner_item,
                        mStrArray);
                mSpinner.setAdapter(adapter);
                mSpinner.setOnItemSelectedListener(this);
                LayoutParams para = new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
                ly.addView(mSpinner, para);
            } else if (mUri != null && mFrom != null) {
                Cursor c = mContext.getContentResolver().query(mUri, mFrom,
                        null, null, mSorting);
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(mContext,
                        R.layout.default_spinner_item, c, mFrom, TO,
                        CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
                mSpinner.setAdapter(adapter);
                mSpinner.setOnItemSelectedListener(this);

                LayoutParams para = new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
                ly.addView(mSpinner, para);
            }
            parent.addView(ly);

        }

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            if (mStrArray != null) {
                mEdit.setText(mEdit.getText().toString()
                        .concat(mStrArray[arg2]));
            } else {
                TextView tv = (TextView) arg1.findViewById(R.id.content);
                mEdit.setText(mEdit.getText().toString()
                        .concat(tv.getText().toString()));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }

    }

    public class SpinnerEditableForm extends EditableForm {

        /**
         * @param subject
         * @param column
         */
        public SpinnerEditableForm(String subject, String column) {
            super(subject, column);

        }

        private String mColumnName;
        private int mSubject;
        private Spinner mSpinner;
        protected LayoutParams mParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);


        @Override
        public String getValue() {
            return "" + mSpinner.getSelectedItemId();
        }

        @Override
        public String getColumnName() {
            return mColumnName;
        }

        @Override
        public void addForm(LinearLayout parent) {
            parent.addView(mSpinner, mParams);
        }

        @Override
        public String getSubject() {
            return mContext.getString(mSubject);
        }
    }

}
