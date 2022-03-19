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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.text.TextUtils;
import th.in.ffc.app.form.EditFormActivity;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.PersonColumns;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector plus
 */
public class PersonActivity extends EditFormActivity {
    private String mPid;
    private String mPcucodePerson;
    private boolean mCheckData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mCheckData) {
            Uri data = getIntent().getData();
            if (data != null)
                mPid = data.getLastPathSegment();

            if (TextUtils.isEmpty(mPid))
                throw new IllegalStateException("Data was null");

            mPcucodePerson = getIntent().getExtras().getString(PersonColumns._PCUCODEPERSON);

            if (TextUtils.isEmpty(mPcucodePerson))
                throw new IllegalStateException("pid or pcucodeperson was null");
        }
    }

    /**
     * call before super.oncreate()
     *
     * @param false for skip data checking
     * @since Family Folder Collector Plus
     */
    public void setCheckData(boolean data) {
        mCheckData = data;
    }

    public void startPersonActivityForResult(Intent intent, int requestCode) {
        intent.setData(Uri.withAppendedPath(Person.CONTENT_URI, mPid));
        intent.putExtra(PersonColumns._PCUCODEPERSON, mPcucodePerson);
        super.startActivityForResult(intent, requestCode);
    }

    public void startPersonActivity(Intent intent) {
        intent.setData(Uri.withAppendedPath(Person.CONTENT_URI, mPid));
        intent.putExtra(PersonColumns._PCUCODEPERSON, mPcucodePerson);
        super.startActivity(intent);
    }

    public static void startPersonActivity(FragmentActivity activity, Intent intent, String pid, String pcucodeperson) {
        intent.setData(Uri.withAppendedPath(Person.CONTENT_URI, pid));
        intent.putExtra(PersonColumns._PCUCODEPERSON, pcucodeperson);
        activity.startActivity(intent);
    }

    public static void startPersonActivityForResult(FragmentActivity activity, Intent intent, int requestCode, String pid, String pcucodeperson) {
        intent.setData(Uri.withAppendedPath(Person.CONTENT_URI, pid));
        intent.putExtra(PersonColumns._PCUCODEPERSON, pcucodeperson);
        activity.startActivityForResult(intent, requestCode);
    }

    public String getPid() {
        return mPid;
    }

    public String getPcucodePerson() {
        return mPcucodePerson;
    }
}
