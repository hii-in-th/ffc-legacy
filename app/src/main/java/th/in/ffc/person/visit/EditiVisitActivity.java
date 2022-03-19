package th.in.ffc.person.visit;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.appcompat.app.ActionBar;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.provider.DbOpenHelper;
import th.in.ffc.widget.InstantAutoComplete;

public class EditiVisitActivity extends FFCFragmentActivity {

    public String share_pcucode = null;
    public String share_hcode = null;
    public String share_visitno = null;
    public String share_Date = null;
    public String share_pid = null;
    public DbOpenHelper db = new DbOpenHelper(this);
    public String[] array;
    ActionBar abr;
    Bundle b;


    protected void generateSpinnerQuery(Spinner obj, int viewid, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        obj = (Spinner) findViewById(viewid);
        SimpleCursorAdapter adapter = null;
        Cursor c = this.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        if (c.moveToFirst()) {

            adapter = new SimpleCursorAdapter(this, R.layout.default_spinner_item, c, new String[]{projection[1]}, new int[]{R.id.content}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            adapter.setDropDownViewResource(R.layout.default_spinner_item);
            obj.setAdapter(adapter);
        }
    }

    protected void generateAutoCompleteTextView(AutoCompleteTextView obj, int viewid, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        obj = (InstantAutoComplete) findViewById(viewid);
        Cursor c = getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        SimpleCursorAdapter adapter = null;
        if (c.moveToFirst()) {
            adapter = new SimpleCursorAdapter(this, R.layout.default_spinner_item, c, new String[]{projection[1]}, new int[]{R.id.content}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            adapter.setDropDownViewResource(R.layout.default_spinner_item);
            obj.setAdapter(adapter);
        }

    }
}
