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
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import th.in.ffc.app.form.EditFormFragment;
import th.in.ffc.provider.PersonProvider.Person;

/**
 * add description here!
 *
 * @author piruinpanichphol
 * @version 1.0
 * @since 1.0
 */
public class PersonEditFragment extends EditFormFragment {

    public String getPID() {
        Bundle args = getArguments();
        if (args != null)
            return args.getString(Person.PID);
        return null;
    }

    public static Fragment getNewInstance(Context context, String pid, Class<? extends PersonEditFragment> cls) {
        Fragment f = Fragment.instantiate(context, cls.getName());
        Bundle bundle = new Bundle();
        bundle.putString(Person.PID, pid);

        f.setArguments(bundle);
        return f;
    }
}
