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
 * th.in.ffc.security.LoginActivity Project
 *
 * Copyright (C) 2010-2012 National Electronics and Computer Technology Center
 * All Rights Reserved.
 * 
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * 
 */

package th.in.ffc.app;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import th.in.ffc.R;
import th.in.ffc.widget.IntentBaseAdapter;

/**
 * add description here!
 *
 * @author Piruin Panichphol
 * @version 1.0
 * @since Family Folder Collector Plus
 */
public class FFCIntentDiaglog extends DialogFragment {

    private GridView mGrid;
    private Intent mIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        if (savedInstanceState != null)
            mIntent = savedInstanceState.getParcelable("intent");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.intent_dialog, container);
        mGrid = (GridView) view.findViewById(android.R.id.list);
        IntentBaseAdapter adapter = new IntentBaseAdapter(getActivity(), mIntent, R.layout.default_grid_item, R.id.image, R.id.text);
        mGrid.setAdapter(adapter);
        mGrid.setOnItemClickListener(adapter.getOnItemClickListener(new IntentBaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id, String name, String packageName) {
                Intent intent = mIntent;
                intent.setComponent(new ComponentName(packageName, name));
                FFCFragmentActivity context = (FFCFragmentActivity) getActivity();
                context.startActivity(intent);
                FFCIntentDiaglog.this.dismiss();
            }
        }));
        return view;
    }

    public void setIntent(Intent intent) {
        this.mIntent = intent;
    }


    public void showDialog(FFCFragmentActivity context) {
        FragmentManager fm = context.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("Intent");
        if (prev != null)
            ft.remove(prev);
        ft.addToBackStack(null);
        this.show(fm, "Intent");
    }

    @Override
    public void onSaveInstanceState(Bundle arg0) {
        super.onSaveInstanceState(arg0);
        arg0.putParcelable("intent", mIntent);
    }

}
