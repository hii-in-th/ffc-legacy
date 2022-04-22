package th.in.ffc.person.visit;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import th.in.ffc.R;
import th.in.ffc.provider.PersonProvider.Women;

public class WomenView extends WomenFragment {
    private static final String[] PROJECTION = new String[]{
            Women.FPTYPE,
            Women.REASONNOFP,
            Women.CHILDALIVE,
            Women.DATESURVEY
    };

    @Override
    void Edit() {
        // TODO Auto-generated method stub
        go = new Intent(getActivity(), WomenEditActivity.class);
        box = new Bundle();
        box.putString("pid", getPID());
        go.putExtras(box);
        startActivity(go);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mForm.removeAllViewsInLayout();
        mForm.refreshDrawableState();
        Uri uri = Uri.withAppendedPath(Women.CONTENT_URI, getPID());
        ContentResolver cr = getFFCActivity().getContentResolver();
        Cursor c = cr
                .query(uri, PROJECTION, null, null, Women.DEFAULT_SORTING);
        if (c.moveToFirst()) {
            doShowRegularData(c);
        } else {
            addTitle(getString(R.string.women));
//			doDialogMsgBuilder();
        }
    }


    private void doShowRegularData(Cursor c) {
        addTitle(getString(R.string.women));
        addContentArrayFormat(getString(R.string.women_fptype), checkStrangerForStringContent(c.getString(0), "0"), R.array.fptype);
        addContentArrayFormat(getString(R.string.women_reasonnofp), checkStrangerForStringContent(c.getString(1), null), R.array.reasonnofp);
        addContent(R.string.women_childalive, c.getString(2));
        addContent(R.string.women_datesurvey, c.getString(3));

    }

    protected void doDialogMsgBuilder() {
        // TODO Auto-generated method stub
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setMessage(getResources().getString(R.string.women_dialog_null))
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
}
