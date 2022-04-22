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

package th.in.ffc.building.house;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import th.in.ffc.R;
import th.in.ffc.app.form.ViewFormFragment;
import th.in.ffc.provider.HouseProvider.House;

/**
 * add description here! please
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since Family Folder Collector 2.0
 */
public abstract class HouseFragment extends ViewFormFragment {

    public static final String EXTRA_HCODE = House.HCODE;
    public static final String EXTRA_PCUCODE = House.PCUCODE;
    protected Intent go;
    protected Bundle box, saveInstanceStage;

    public String getHcode() {
        Bundle args = getArguments();
        if (args != null)
            return args.getString(EXTRA_HCODE);
        return null;
    }

    protected int checkStangerForIntegerContent(String content, int defaultV) {
        if (TextUtils.isEmpty(content) || content.matches("[-_!@#]"))
            return defaultV;
        else
            return Integer.parseInt(content);
    }

    protected int checkStangerForIntegerContent(String content) {
        return checkStangerForIntegerContent(content, 2);
    }

    protected String checkStrangerForStringContent(String content,
                                                   String defaultV) {
        if (TextUtils.isEmpty(content) || content.matches("[-_!@#]"))
            return defaultV;
        else
            return content;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.editable_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                Edit();
                break;
            case R.id.back:
                getActivity().finish();
                break;
        }
        return true;
    }

    /*		 THIS METHOD FOR
             Create dialogue to confirm when user click edit button to ensure that
             user is surely to delete the view he is currently access
             When user confirm by click OK button Then will call a method "Edit()"
             that will interact with Intent to new Activity that contain edit content
             for each from
             else Dialogue will be dismiss and nothing happen
             MUST OVERRIDE METHOD Delete() FOR EACH FORM
             */
    public void onEditPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setMessage(getResources().getString(R.string.dialog_neededit))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Edit();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    abstract void Edit();

}
