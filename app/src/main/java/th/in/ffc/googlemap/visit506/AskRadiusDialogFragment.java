package th.in.ffc.googlemap.visit506;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import th.in.ffc.R;

public class AskRadiusDialogFragment extends DialogFragment {

    private TextView txtRadius;
    private Spinner spinner;
    private onDialogListener listener;
    private final String COLOR_RED = "#EE0000";
    private final String COLOR_ORANGE = "#ff8c00";
    private final String COLOR_YELLOW = "#ffc500";
    private String choice[] = { "ปกติ", "ปานกลาง" ,"รุนแรง" };
    String radius;
    String radiusSet;
    int levelSet;
    int textSpinner;

    static AskRadiusDialogFragment newInstance(String radiusSet, int levelSet) {
        AskRadiusDialogFragment f = new AskRadiusDialogFragment();
        f.radiusSet = radiusSet;
        f.levelSet = levelSet;
        f.radius = "1000";
        f.textSpinner = 0;
        return f;
    }

    public interface onDialogListener {
        void onDialogListener(String radius, String colorcode, int levelSet);
    }

    public void setOnDialogListener(onDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("Setting Radius");
        builder.setView(getContentView());
        builder.setPositiveButton(R.string.ok, positiveButton);
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                dismiss();
            }
        });
        Dialog dialog = builder.create();
        return dialog;
    }

    private View getContentView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.map506_radius_dialogfragment, null);
        spinner = (Spinner) v.findViewById(R.id.level);
        txtRadius = (TextView) v.findViewById(R.id.txtradius);
        txtRadius.setText(radius);
        ArrayAdapter<String> arrAd = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, choice);
        arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrAd);
        spinner.setSelection(textSpinner);
        if (!TextUtils.isEmpty(radiusSet)) {
            txtRadius.setText(radiusSet);
            spinner.setSelection(levelSet);
        }
        return v;
    }

    OnClickListener positiveButton = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            radius = txtRadius.getText().toString();
            textSpinner = spinner.getSelectedItemPosition();
            String colorcode = getColor(spinner.getSelectedItemPosition());
            int levelSet = spinner.getSelectedItemPosition();
            if (listener != null) {
                listener.onDialogListener(radius, colorcode, levelSet);
            }
        }
    };

    private String getColor(int levelSelect) {
        String color = "";
        switch (levelSelect) {
            case 0:
                color = COLOR_YELLOW;
                return color;
            case 1:
                color = COLOR_ORANGE;
                return color;
            case 2:
                color = COLOR_RED;
                return color;
            default:
                break;
        }
        return color;
    }


}
