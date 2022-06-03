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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import th.in.ffc.R;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.1
 * @since Family Folder Collector plus
 */
public class ArrayFormatSpinner extends androidx.appcompat.widget.AppCompatSpinner {

    /**
     * @param context
     * @param attrs
     */
    public ArrayFormatSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    String[] mArray;

    public void setArray(int stringArrayId) {
        this.setArray(getResources().getStringArray(stringArrayId));
    }

    public void setArray(String[] array) {
        mArray = array;
        ArrayList<String> list = new ArrayList<String>();
        for (String item : mArray) {
            if (!item.matches(".*:.{1,}"))
                throw new InvalidParameterException("Array Item invalid format");
            list.add(item.substring(item.indexOf(":") + 1));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                R.layout.default_spinner_item, list);
        this.setAdapter(adapter);
    }

    public void setSelection(String id) {
        if (!TextUtils.isEmpty(id)) {
            for (int i = 0; i < mArray.length; i++) {
                String item = mArray[i];
                String k = item.substring(0, item.indexOf(":"));
                if (k.equals(id)) {
                    this.setSelection(i, true);
                    break;
                }
            }
        }
    }

    public String getSelectionId() {
        String id = null;
        String item = mArray[this.getSelectedItemPosition()];
        id = item.substring(0, item.indexOf(":"));
        if (TextUtils.isEmpty(id))
            return null;
        else
            return id;
    }

}
