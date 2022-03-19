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

import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import th.in.ffc.R;
import th.in.ffc.intent.Action;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.PersonColumns;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector plus
 */
public class PersonDetailEditActivity extends PersonActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getAction().equals(Action.INSERT))
            setCheckData(false);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);


        setSupportProgressBarIndeterminateVisibility(false);

        setContentView(R.layout.default_scrollable_activity);

        Bundle args = new Bundle();
        args.putString(PersonColumns._PCUCODEPERSON, getPcucodePerson());
        args.putString(PersonColumns._PID, getPid());
        args.putString("action", getIntent().getAction());
        args.putString(Person.HCODE, getIntent().getStringExtra(Person.HCODE));

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment pd = fm.findFragmentByTag("detail");
        if (pd == null) {
            pd = Fragment.instantiate(this, PersonDetailEditFragment.class.getName(), args);
            ft.add(R.id.content, pd, "detail");
        }
        ft.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_activity, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                doSave();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doSave() {
        String action = getIntent().getAction();
        FragmentManager fm = getSupportFragmentManager();
        PersonDetailEditFragment f = (PersonDetailEditFragment) fm.findFragmentByTag("detail");
        if (f != null) {
            EditTransaction et = beginTransaction();
            if (f.onSave(et)) {
                if (action.equals(Action.INSERT)) {
                    et.getContentValues().put(Person.FAMILY_NO, 1);
                    et.getContentValues().put(Person.FAMILY_POSITION, 0);
                    Uri uri = et.commit(Person.CONTENT_URI);
                    Log.d(TAG, uri.toString());

                } else if (action.equals(Action.EDIT)) {
                    String[] selectionArgs = new String[]{
                            getPid(),
                            getPcucodePerson(),
                    };
                    String selection = "pid=? AND pcucodeperson=?";
                    int update = et.commit(Person.CONTENT_URI, selection, selectionArgs);
                    Log.d(TAG, "update=" + update);
                }
                this.finish();
            } else {

            }

        }
    }

    public interface Saveable {
        /**
         * @param et
         * @return true if all data OK, false if something error
         * @since Family Folder Collector Plus
         */
        public boolean onSave(EditTransaction et);
    }


}
