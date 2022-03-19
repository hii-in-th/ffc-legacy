package th.in.ffc.googlemap.visit506;

import android.database.Cursor;
import android.net.Uri;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.TextUtils;
import th.in.ffc.R;
import th.in.ffc.app.FFCSearchListDialog;
import th.in.ffc.provider.PersonProvider.Person;
import th.in.ffc.provider.PersonProvider.Visit506_Person;
import th.in.ffc.widget.CursorStringIdAdapter;

public class PatietListDialogSetSearchBar extends
        FFCSearchListDialog.BaseAdapter {

    String oldSelection;
    public static final String[] PROJECTION = new String[]{Person._ID, Visit506_Person._NAME, "diseasenamethai", "sickdatestart"};

    public static final String[] FROM = new String[]{Visit506_Person._NAME, "diseasenamethai", "sickdatestart"};

    public static final int[] TO = new int[]{R.id.fname, R.id.plaugename, R.id.sickdate};

    String selection;
    String value[] = {"2012-09-06", "2013-09-06"};

    public PatietListDialogSetSearchBar() {
        selection = "villno !=0 AND status NOT NULL ";
        if (!TextUtils.isEmpty(PatientDialogFragment.settingTimeQuery)) {
            selection += PatientDialogFragment.settingTimeQuery;

        }
        if (!TextUtils.isEmpty(PatientDialogFragment.settingPlaugeQuery)) {
            selection += PatientDialogFragment.settingPlaugeQuery;
        }
        //selection = "villno !=0 AND status not null";
        oldSelection = selection;
    }

    @Override
    public Loader<Cursor> onLoadCursor(String filter) {
        Uri uri = Visit506_Person.CONTENT_URI;
        if (!TextUtils.isEmpty(filter)) {
            selection = "(villno!=0 AND status not null) AND (fname LIKE '%" + filter + "%' or lname LIKE '" + filter + "%')";
        } else {
            selection = oldSelection;
        }
        CursorLoader cl = new CursorLoader(getActivity(), uri, PROJECTION,
                selection, null, "visit.visitno");
        return cl;
    }

    @Override
    public Uri getContentUri() {
        return Visit506_Person.CONTENT_URI;
    }

    @Override
    public CursorStringIdAdapter getBaseAdapter() {
        CursorStringIdAdapter adapter = new CursorStringIdAdapter(
                getActivity(), R.layout.map560_spinner_item, null,
                FROM, TO);
        return adapter;
    }

}
