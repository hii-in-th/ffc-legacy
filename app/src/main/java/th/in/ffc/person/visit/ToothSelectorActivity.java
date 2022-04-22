package th.in.ffc.person.visit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import th.in.ffc.R;
import th.in.ffc.provider.CodeProvider.Drug;
import th.in.ffc.provider.PersonProvider.PersonColumns;
import th.in.ffc.provider.PersonProvider.VisitDrug;
import th.in.ffc.provider.PersonProvider.VisitDrugDental;
import th.in.ffc.widget.ImageMap;

public class ToothSelectorActivity extends VisitActivity {
    String mVISITNO;
    String mPCUCODE;
    String dentcode;
    String toothtype;
    ImageMap map;
    public static final String[] PROJECTION = new String[]{
            VisitDrugDental.TOOTHAREA, VisitDrugDental.SURFACE, VisitDrugDental.COMMENT};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        mVISITNO = getIntent().getStringExtra(VisitDrug.NO);
        mPCUCODE = getIntent().getStringExtra(VisitDrug._PCUCODE);
        dentcode = getIntent().getStringExtra(VisitDrugDental.DENTCODE);
        toothtype = getIntent().getStringExtra(Drug.TOOTHTYPE);
        setToothType(toothtype);

        setContentView(R.layout.default_activity);
        setSupportProgressBarIndeterminateVisibility(false);
    }

    private void StartToothMapActivity() {


        Intent intent = new Intent(this, VisitDrugDentalActivity.class);
        intent.setData(Uri.withAppendedPath(VisitDrugDental.CONTENT_URI, VisitDrug.NO));
        intent.putExtra(PersonColumns._PID, "00000");
        intent.putExtra(PersonColumns._PCUCODEPERSON, "00000");
        intent.putExtra(VisitDrugDental.DENTCODE, dentcode);
        intent.putExtra(Drug.TOOTHTYPE, toothtype);
        intent.putExtra(VisitDrug.NO, mVISITNO);
        intent.putExtra(VisitDrug._PCUCODE, mPCUCODE);
        startActivity(intent);
    }

    private void setToothType(String tt) {
        //For no tooth type code
        if (TextUtils.isEmpty(tt)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
            builder.setMessage(getResources().getString(R.string.dialog_noToothType))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.primaryTooth),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    toothtype = "1";
                                    dialog.cancel();
                                    StartToothMapActivity();
                                    finish();
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.permanentTooth),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    toothtype = "2";
                                    dialog.cancel();
                                    StartToothMapActivity();
                                    finish();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }
}
