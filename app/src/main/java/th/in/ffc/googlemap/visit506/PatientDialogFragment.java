package th.in.ffc.googlemap.visit506;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import th.in.ffc.R;
import th.in.ffc.widget.SearchableButton;
import th.in.ffc.widget.SearchableButton.OnItemSelectListener;

public class PatientDialogFragment extends DialogFragment {
    private SearchableButton searchBar;
    private String hcode;
    private PatientListCallback mCallBack;
    public static String settingTimeQuery;
    public static String settingPlaugeQuery;

    static PatientDialogFragment newInstance() {
        PatientDialogFragment f = new PatientDialogFragment();
        return f;
    }

    public PatientDialogFragment() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(R.string.click_to_show_patients);
        builder.setView(getContentView());
        builder.setPositiveButton(R.string.ok, positiveButton);
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        Dialog dialog = builder.create();
        return dialog;
    }

    public View getContentView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.map506_patientlist, null);
        searchBar = (SearchableButton) view.findViewById(R.id.patientlist);
        searchBar.setDialog(getActivity().getSupportFragmentManager(),
                PatietListDialogSetSearchBar.class, "list");
        searchBar.setOnItemSelectListener(onItemSelectCallBack);
        //	searchBar.setFocusable(false);
        //	searchBar.setPressed(false);
        //searchBar.setShowList();
        return view;
    }

    OnItemSelectListener onItemSelectCallBack = new OnItemSelectListener() {
        @Override
        public void onItemSelect(String id) {
            if (id != null) {
                hcode = id;
                //		mCallBack.listCallBack(hcode);
            } else {
                onDissMiss();
            }
        }
    };

    public void onDissMiss() {
        this.dismiss();
    }

    public static interface PatientListCallback {
        public void listCallBack(String hcode, String name);
    }

    public void setOnClickListenerCallBack(PatientListCallback callback) {
        mCallBack = callback;
    }

    OnClickListener positiveButton = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mCallBack != null) {
                mCallBack.listCallBack(hcode, searchBar.getText().toString());
            }
        }
    };

    public static void settingTimeQuery(String TimeQuery) {
        settingTimeQuery = TimeQuery;
    }

    public static void settingPlaugeQuery(String PlaugeQuery) {
        settingPlaugeQuery = PlaugeQuery;
    }

}
