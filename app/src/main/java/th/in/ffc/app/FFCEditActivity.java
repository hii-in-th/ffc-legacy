package th.in.ffc.app;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.appcompat.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import th.in.ffc.R;
import th.in.ffc.persist.PersonPersist;
import th.in.ffc.persist.otherListPersist;
import th.in.ffc.provider.CodeProvider.HomeHealthType;
import th.in.ffc.provider.DbOpenHelper;
import th.in.ffc.util.ThaiDatePicker;
import th.in.ffc.util.ThaiTimePicker;
import th.in.ffc.widget.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Atiwat
 */
abstract public class FFCEditActivity extends FFCFragmentActivity {

    public String share_pcucode = null;
    public String share_hcode = null;
    public String share_visitno = null;
    public String share_Date = null;
    public String share_pid = null;
    public DbOpenHelper db = new DbOpenHelper(this);
    public String[] array;
    protected boolean cursorChecker = false;
    protected boolean canCommit = false;
    protected String ERROR_MSG = "";
    ActionBar abr;
    Bundle b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = getIntent().getExtras();
        share_pcucode = getPcuCode();
        share_visitno = b.getString("visitno");
        share_hcode = b.getString("hcode");
        share_pid = b.getString("pid");
        this.array = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save:
                Update();
                //onSavePressed();
                break;

            case R.id.cancel:
            case R.id.back:
                //onBackPressed();
                break;

            case R.id.deleted:
                //onDeletePressed();
                break;
        }
        return true;
    }

    /**
     * THIS METHOD FOR Create dialogue to confirm when user click delete button
     * to ensure that user is surely to delete the view he is currently access
     * When user confirm by click OK button Then will call a method "Delete()"
     * that will interact with JhcisAdapter.getJhcis().delete() method else
     * Dialogue will be dismiss and nothing happen MUST OVERRIDE METHOD Delete()
     * FOR EACH FORM
     **/

    protected final void onDeletePressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.dialog_delete))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Delete();
                                // finish();
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

    /**
     * THIS METHOD FOR Create dialogue to confirm when user click edit button to
     * ensure that user is surely to delete the view he is currently access When
     * user confirm by click OK button Then will call a method "Edit()" that
     * will interact with Intent to new Activity that contain edit content for
     * each from else Dialogue will be dismiss and nothing happen MUST OVERRIDE
     * METHOD Delete() FOR EACH FORM
     **/
    protected void onSavePressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.dialog_edit))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Update();
                                // finish();
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

//	@Override
//	public void onBackPressed() {
//		// TODO Auto-generated method stub
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setMessage(getResources().getString(R.string.dialog_cancel))
//				.setCancelable(false)
//				.setPositiveButton(getResources().getString(R.string.ok),
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog, int id) {
//								finish();
//							}
//						})
//				.setNegativeButton(getResources().getString(R.string.cancel),
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog, int id) {
//								dialog.cancel();
//							}
//						});
//		AlertDialog alert = builder.create();
//		alert.show();
//	}

    abstract protected void Delete();

    abstract protected void Edit();

    abstract protected void Update();

    /**
     * THIS METHOD IS FOR Query database from String sql recived and put any
     * dataset to String array[];
     *
     * @param sql SQL STATEMENT FOR QUERY
     */
    protected void setContentQuery(String sql) {
        // TODO Auto-generated method stub

        Cursor c = db.getReadableDatabase().rawQuery(sql, null);
        int size = c.getColumnCount();
        this.array = new String[size];
        if (c.moveToFirst()) {
            for (int i = 0; i < size; i++) {
                array[i] = c.getString(i);
            }
        }
    }

    /**
     * Query a set of data via ContentPovider and put to Array
     *
     * @param uri           Content Provider 's CONTENT_URI
     * @param projection    Column name which you want to query
     * @param selection     refers to Where statement (do not append "WHERE" in this
     *                      statement and also value of column) Ex. hcode = ? AND pid = ?
     * @param selectionArgs A set of data which will be replace to ? in -selection
     * @param sortOrder     refers to Order By statement
     */
    protected void setContentQuery(Uri uri, String[] projection,
                                   String selection, String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub

        Cursor c = this.getContentResolver().query(uri, projection, selection,
                selectionArgs, sortOrder);
        int size = c.getColumnCount();
        this.array = new String[size];
        if (c.moveToFirst()) {
            System.out.println("Query Success");
            System.out.println("This is a form where Id = " + selectionArgs[0]);
            cursorChecker = true;
            for (int i = 0; i < size; i++) {
                array[i] = c.getString(i);
            }
        } else {
            System.out.println("Query Failed !!");
            System.out.println("Don't Have row id = " + selectionArgs[0]);
            System.out.println("This is new form instead");
            cursorChecker = false;
        }

    }

    /**
     * Query a set of data and populate in spinner Deprecated by FFC PLUS
     *
     * @param obj    Spinner object
     * @param array  String[] array to maintain a set of data
     * @param viewid Resource Id of Spinner object
     * @param sql    SQL statement to query a dataset
     */
    @Deprecated
    protected void generateAutoCompleteTextView(AutoCompleteTextView obj,
                                                String[] array, int viewid, String sql) {
        // TODO Auto-generated method stub
        obj = (InstantAutoComplete) findViewById(viewid);

        Cursor c = db.getReadableDatabase().rawQuery(sql, null);
        if (c.moveToFirst()) {
            array = new String[c.getCount()];
            int count = 0;
            do {
                array[count] = c.getString(0);
                count++;
            } while (c.moveToNext());

        }

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                this, android.R.layout.simple_dropdown_item_1line, array);
        obj.setAdapter(adapter);
    }

    /**
     * Query a set of data and populate in spinner
     *
     * @param uri           Content Provider's Content URI
     * @param projection    Column Name
     * @param selection     refers to Where statement (do not append "WHERE" in this
     *                      statement and also value of column) Ex. hcode = ? AND pid = ?
     * @param selectionArgs a set of data that will be replace at ? sequencely
     * @param sortOrder     refers to an Order By statement
     * @param obj           Spinner Object
     * @param viewid        Spinner Object's Resource Id
     */
    protected void generateAutoCompleteTextView(Uri uri, String[] projection,
                                                String selection, String[] selectionArgs, String sortOrder,
                                                J_InstantAutoComplete obj, int viewid) {
        // TODO Auto-generated method stub
        obj = (J_InstantAutoComplete) findViewById(viewid);
        obj.setEnabled(true);
        Cursor c = getContentResolver().query(uri, projection, selection,
                selectionArgs, sortOrder);
        if (c.moveToFirst()) {
            String[] array = new String[c.getCount()];
            int count = 0;
            do {
                array[count] = c.getString(1);
                count++;
            } while (c.moveToNext());
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    this, android.R.layout.simple_dropdown_item_1line, array);
            obj.setAdapter(adapter);
        }

    }

    // prepare spinner by sql statement
    // Only 2 columns ( id+name)
    @Deprecated
    protected void generateFromDatabase(Spinner obj,
                                        ArrayAdapter<CharSequence> adapter,
                                        ArrayList<otherListPersist> array, int viewid, String sql) {
        // TODO Auto-generated method stub
        obj = (Spinner) findViewById(viewid);
        adapter = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Cursor c = db.getReadableDatabase().rawQuery(sql, null);
        if (c.moveToFirst()) {
            do {
                otherListPersist item = new otherListPersist(c.getString(0),
                        c.getString(1));
                array.add(item);
                adapter.add(item.getKeyName());
            } while (c.moveToNext());
            obj.setAdapter(adapter);
            c.close();
        }

    }

    protected void generateFromDatabase(Uri uri, String[] projection,
                                        String selection, String[] selectionArgs, String sortOrder,
                                        Spinner obj, int viewid) {

        obj = (Spinner) findViewById(viewid);
        SimpleCursorAdapter adapter = null;
        Cursor c = this.getContentResolver().query(uri, projection, selection,
                selectionArgs, sortOrder);
        if (c.moveToFirst()) {

            adapter = new SimpleCursorAdapter(this,
                    R.layout.default_spinner_item, c,
                    new String[]{projection[1]}, new int[]{R.id.content},
                    CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            adapter.setDropDownViewResource(R.layout.default_spinner_item);
            obj.setAdapter(adapter);
            if (uri.equals(HomeHealthType.CONTENT_URI))
                obj.setSelection(c.getCount() - 1);
        }

    }

    // prepare spinner which is person by sql statement
    // Only 4 columns ( pid+prename+first+last)
    protected void generatePersonFromDatabase(Spinner obj,
                                              ArrayAdapter<CharSequence> adapter, ArrayList<PersonPersist> array,
                                              int viewid, String sql) {
        // TODO Auto-generated method stub
        obj = (Spinner) findViewById(viewid);
        adapter = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Cursor c = db.getReadableDatabase().rawQuery(sql, null);
        if (c.moveToFirst()) {
            do {
                PersonPersist item = new PersonPersist(c.getString(0),
                        c.getString(1), c.getString(2));
                array.add(item);
                adapter.add(item.toString());
            } while (c.moveToNext());
            adapter.add(getString(R.string.no_input));
        }

        c.close();
        obj.setAdapter(adapter);
        obj.setSelection(adapter.getCount());
    }

    // prepare spinner from resource
    protected void generateFromResource(Spinner obj,
                                        ArrayAdapter<CharSequence> adapter, int viewid, int resource) {
        // TODO Auto-generated method stub
        obj = (Spinner) findViewById(viewid);
        adapter = ArrayAdapter.createFromResource(this, resource,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        obj.setAdapter(adapter);
        // obj.setSelection(adapter.getCount());
    }

    // For none 0 - Based
    protected void generateFromResourceNonZeroBased(Spinner obj,
                                                    ArrayAdapter<CharSequence> adapter, int viewid, int resource) {
        // TODO Auto-generated method stub
        obj = (Spinner) findViewById(viewid);
        adapter = ArrayAdapter.createFromResource(this, resource,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        obj.setAdapter(adapter);
        // obj.setSelection(adapter.getCount() - 1);
    }

    // check data before put in editText;
    protected String checkEditText(String data) {
        if (TextUtils.isEmpty(data)) {
            return null;
        } else {
            return data;
        }
    }

    // check data before set to spinner resource

    protected int checkSpinnerResource(String data, int res) {
        // TODO Auto-generated method stub
        String[] array = getResources().getStringArray(res);
        int size = array.length - 1;
        if (TextUtils.isEmpty(data) || data.matches("[-_!@#]")) {
            return size;
        } else {
            return Integer.parseInt(data);
        }

    }

    // For none 0 - Based
    protected int checkSpinnerResourceNonZeroBased(String data, int res) {
        // TODO Auto-generated method stub
        String[] array = getResources().getStringArray(res);
        int size = array.length - 1;
        if (TextUtils.isEmpty(data) || data.matches("[-_!@#]")) {
            return size;
        } else {
            return Integer.parseInt(data) - 1;
        }
    }

    @Deprecated
    // check data before set to spinner query
    protected int checkSpinnerQuery(String data,
                                    ArrayList<otherListPersist> array) {
        System.out.println("id is = " + data);
        int select = array.size() - 1;
        if (TextUtils.isEmpty(data) || data.matches("[-_!@#]")) {
            return select;
        } else {
            for (int i = 0; i < array.size(); i++) {
                if (data.equals(array.get(i).getKeyValue())) {
                    System.out.println("FOUND " + data + " at index = " + i);
                    select = i;
                    break;
                } else {
                    System.out.println("SYS COMPARE "
                            + array.get(i).getKeyValue() + " with " + data);
                }
            }
            return select;
        }

    }

    protected int checkSpinnerQuery(String data, Spinner obj) {
        int selection = 0;
        if (TextUtils.isEmpty(data) || data.matches("[-_!@#]")) {

        } else {
            for (int i = 0; i < obj.getCount(); i++) {
                if (Long.toString(obj.getItemIdAtPosition(i)).equals(data)) {
                    return i;
                }
            }
        }
        return selection;
    }

    protected int checkSpinnerQueryString(String data, Spinner obj) {
        int selection = 0;
        if (TextUtils.isEmpty(data) || data.matches("[-_!@#]")) {

        } else {
            CursorStringIdAdapter csa = (CursorStringIdAdapter) obj.getAdapter();
            for (int i = 0; i < obj.getCount(); i++) {
                if (csa.getStringId(i).equals(data)) {
                    return i;
                }
            }
        }
        return selection;
    }

    // check data before set to spinner person
    protected int checkSpinnerPersonQuery(String data,
                                          ArrayList<PersonPersist> array) {
        int select = array.size() - 1;
        if (TextUtils.isEmpty(data) || data.matches("[-_!@#]")) {
            return select;
        } else {
            for (int i = 0; i < array.size(); i++) {
                if (data.equals(array.get(i).getPid())) {
                    select = i;
                    break;
                }
            }
            return select;
        }
    }

    // prepare datepicker to current date
    protected void updateDatePicker(TextView tv, ThaiDatePicker obj, String data) {
        if (data != null) {
            Log.d("LOG SPLIT", data);
            String[] preCheck = data.split(" ");
            String[] date = preCheck[0].split("-");
            Log.d("LOG", data);
            int year = Integer.parseInt(date[0]);
            int month = Integer.parseInt(date[1]) - 1;
            int day = Integer.parseInt(date[2]);
            // Change B.E. to C.E.
            if (year > 2500)
                year -= 543;
            Log.d("LOG", year + " " + month + " " + day);
            obj.updateDate(year, month, day);
            setTextViewisNull(tv, data);
        } else {
            Log.d("LOG", "no date");
            setTextViewisNull(tv, data);
        }
    }

    // prepare timepicker to current time
    protected void updateDateTimePicker(TextView tv, ThaiDatePicker obj,
                                        ThaiTimePicker obj2, String data) {
        if (data != null) {
            Log.d("LOG SPLIT", data);
            String[] preCheck = data.split(" ");
            String[] date = preCheck[0].split("-");
            Log.d("LOG", data);
            int year = Integer.parseInt(date[0]);
            int month = Integer.parseInt(date[1]) - 1;
            int day = Integer.parseInt(date[2]);
            // Change B.E. to C.E.
            if (year > 2500)
                year -= 543;
            Log.d("LOG", year + " " + month + " " + day);
            obj.updateDate(year, month, day);

            String[] time = preCheck[1].split(":");
            int h = Integer.parseInt(time[0]);
            int m = Integer.parseInt(time[1]);
            obj2.updateTime(h, m);
            setTextViewisNull(tv, data);
        } else {
            Log.d("LOG", "no date");
            setTextViewisNull(tv, data);
        }
    }

    // check data before display in textview
    protected void setTextViewisNull(TextView obj, String data) {
        if (data != null) {
            setTextColor(obj, data);
        } else {
            setTextColor(obj, getString(R.string.no_input));
        }
    }

    // set textview color
    protected void setTextColor(TextView obj, String data) {
        if (data.equals(getString(R.string.no_input))) {
            obj.setTextColor(getResources().getColor(R.color.darker_gray));
            obj.setText(data);
        } else {
            obj.setText(data);
        }
    }

    // check that has visitno
    public boolean checkHasVisitno() {
        String visitno = share_visitno;
        String sql = "SELECT visitno FROM visithomehealthindividual WHERE visitno ='"
                + visitno + "'";
        Cursor c = db.getReadableDatabase().rawQuery(sql, null);

        if (c.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    // get data from textview for update
    public void retrieveDataFromTextView(TextView obj, ContentValues cv,
                                         String colName) {
        String content = obj.getText().toString();
        if (TextUtils.isEmpty(content)
                || content.equals(getResources().getString(R.string.no_input))) {
            System.out.println("none");
        } else {

            cv.put(colName, content);
            System.out.println("GET " + content);
        }
    }

    public void retrieveDate(TextView obj, ContentValues cv, String colName) {
        String content = obj.getText().toString();
        if (TextUtils.isEmpty(content)
                || content.equals(getResources().getString(R.string.no_input))) {
            System.out.println("none");
        } else {

            cv.put(colName, content);
            System.out.println("GET " + content);
        }
    }

    public void retrieveDataFromEditText(EditText obj, ContentValues cv,
                                         String colName) {
        String content = obj.getText().toString();
        if (TextUtils.isEmpty(content)
                || content.equals(getResources().getString(R.string.no_input))) {
            System.out.println("none");
        } else {

            cv.put(colName, content);
            System.out.println("GET " + content);
        }
    }

    public void retrieveDataFromSpinnerWithResource(Spinner obj,
                                                    ContentValues cv, String colName, int Resource) {
        int content = obj.getSelectedItemPosition();
        String[] array = getResources().getStringArray(Resource);
        if (content >= array.length)
            throw new IndexOutOfBoundsException(
                    "Can't retrieve data because of invalid array index");

        cv.put(colName, Integer.toString(content));
        System.out.println("GET " + content);
    }

    /*
     * To Get Data From Non 0-Base SpinnerResource
     */
    public void retrieveDataFromSpinnerWithResourceNonZeroBased(Spinner obj,
                                                                ContentValues cv, String colName, int Resource) {
        int content = obj.getSelectedItemPosition();
        String[] array = getResources().getStringArray(Resource);
        if (content >= array.length)
            throw new IndexOutOfBoundsException(
                    "Can't retrieve data because of invalid array index");
        cv.put(colName, Integer.toString(content + 1));
        System.out.println("GET " + content);

    }

    public void retrieveDataFromSpinnerWithQuery(Spinner obj, ContentValues cv,
                                                 String colName) {
        long data = obj.getSelectedItemId();
        System.out.println("Spinner Id is :" + data);
        cv.put(colName, data);
    }

    @Deprecated
    public void retrieveDataFromSpinnerWithQuery(Spinner obj, ContentValues cv,
                                                 String colName, ArrayList<otherListPersist> array) {
        int content = obj.getSelectedItemPosition();
        if (content >= array.size())
            throw new IndexOutOfBoundsException(
                    "Can't retrieve data because of invalid array index");

        cv.put(colName, array.get(content).getKeyValue());
        System.out.println("GET " + content);

    }

    public void retrieveDataFromSpinnerPerson(Spinner obj, ContentValues cv,
                                              String colName, ArrayList<PersonPersist> array) {
        int content = obj.getSelectedItemPosition();

        if (array.size() > 0) {
            if (content < array.size())
                cv.put(colName, array.get(content).getPid());
            System.out.println("GET " + content);
        }
    }

    public void retrieveDataFromSearchableButton(SearchableButton obj,
                                                 ContentValues cv, String colName) {
        String content = obj.getSelectId();
        if (obj.getSelectId() != null)
            cv.put(colName, content);
        System.out.println("GET " + content);
    }

    public void retrieveDataFromSearchableSpinner(SearchableSpinner obj,
                                                  ContentValues cv, String colName) {
        long content = obj.getSelectedItemId();

        cv.put(colName, content + "");
        System.out.println("GET " + content);

    }

    public void retrieveDataFromRadioGroup(RadioGroup Rg, ContentValues cv,
                                           String colName) {
        int check = Rg.getCheckedRadioButtonId();
        String puts = null;
        switch (check) {
            case R.id.rad1:
                puts = "1";
                break;
            case R.id.rad2:
                puts = "2";
                break;
            case R.id.rad3:
                puts = "3";
                break;
            default:
                break;
        }
        if (puts != null) {
            cv.put(colName, puts);
        } else {
            cv.putNull(colName);
        }
    }

    /**
     * get data from ThaiDatePicker object
     *
     * @param obj        ThaiDatePicker object which is already pointed by findView
     * @param cv         ContentValues to keep dataset
     * @param colName    Column Name
     * @param constrains true or false (for somepicker whice upons checkbox)
     */
    public void retrieveDataFromThaiDatePicker(ThaiDatePicker obj,
                                               ContentValues cv, String colName, Boolean constrains) {
        if (constrains) {
            String content = obj.getDate().toString();
            if (content != null) {
                cv.put(colName, content);
            }
        } else {
            cv.putNull(colName);
        }

    }

    protected void updateTimeStamp(ContentValues cv) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        int mSec = c.get(Calendar.SECOND);

        String data = new StringBuilder().append(mYear).append("-")
                .append(pad(mMonth + 1)).append("-").append(pad(mDay))
                .append(" ").append(pad(mHour)).append(":")
                .append(pad(mMinute)).append(":").append(pad(mSec)).toString();

        cv.put("dateupdate", data);
        Log.d("LAST UPDATE@", data);
    }

    protected void setCurrentTime(ThaiTimePicker obj, int Res) {
        obj = (ThaiTimePicker) findViewById(Res);

        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        int mSec = c.get(Calendar.SECOND);

        String data = new StringBuilder().append(pad(mHour)).append(":")
                .append(pad(mMinute + 1)).append(":").append(pad(mSec))
                .toString();

        String[] date = data.split(":");
        int h = Integer.parseInt(date[0]);
        int m = Integer.parseInt(date[1]);
        obj.updateTime(h, m);
    }

    protected void setCurrentDate(ContentValues cv, String colname) {

        final Calendar c = Calendar.getInstance();
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mMonth = c.get(Calendar.MONTH);
        int mYear = c.get(Calendar.YEAR);

        String data = new StringBuilder().append(mYear).append("-")
                .append(pad(mMonth + 1)).append("-").append(pad(mDay))
                .toString();

        cv.put(colname, data);
    }

    protected void updatePicker(ThaiDatePicker obj, int ResId, String data) {
        obj = (ThaiDatePicker) findViewById(ResId);
        if (TextUtils.isEmpty(data)) {
        } else {
            String[] Date = data.split("-");
            int year = Integer.parseInt(Date[0]);
            int month = Integer.parseInt(Date[1]) - 1;
            int day = Integer.parseInt(Date[2]);
            obj.updateDate(year, month, day);
        }
    }

    protected void updateTime(ThaiTimePicker obj, int ResId, String data) {
        obj = (ThaiTimePicker) findViewById(ResId);
        if (TextUtils.isEmpty(data))
            System.out.println("Null Time");
        else {

            String[] time = data.split(":");
            int h = Integer.parseInt(time[0]);
            int m = Integer.parseInt(time[1]);
            obj.updateTime(h, m);
        }
    }

    protected static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    protected int checkStrangerForIntegerContent(String content, int defaultV) {
        if (TextUtils.isEmpty(content) || content.matches("[-_!@#]"))
            return defaultV;
        else
            return Integer.parseInt(content);
    }

    protected Long checkStrangerForLongContent(String content, Long defaultV) {
        if (TextUtils.isEmpty(content) || content.matches("[-_!@#]"))
            return defaultV;
        else
            return Long.parseLong(content);
    }

    protected String checkStrangerForStringContent(String content,
                                                   String defaultV) {
        if (TextUtils.isEmpty(content) || content.matches("[-_!@#]"))
            return defaultV;
        else
            return content;
    }

    protected Boolean checkDate(ThaiDatePicker dateappoint) {
        // TODO Auto-generated method stub
        Boolean result = false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date Date = null;
        Date current = null;

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String cDate = year + "-" + month + "-" + day;

        try {
            Date = format.parse(dateappoint.getDate().toString());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            current = format.parse(cDate.toString());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        result = Date.compareTo(current) >= 0 ? true : false;
        if (!result)
            ERROR_MSG += "\n -" + getString(R.string.err_date_invalid);
        return result;
    }

    protected Boolean checkOverDate(ThaiDatePicker date1, ThaiDatePicker date2) {
        Boolean result = false;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date Date1 = null;
        Date Date2 = null;
        try {
            Date1 = format.parse(date1.getDate().toString());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            Date2 = format.parse(date2.toString());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int choice = Date2.compareTo(Date1);
        System.out.println(Date2 + "\n compare to \n" + Date1);
        System.out.println(choice + " :" + (result ? "TRUE" : "FALSE"));
        result = choice >= 0 ? true : false;

        return result;
    }

    protected Boolean checkSickDate(ThaiDatePicker dateappoint) {
        // TODO Auto-generated method stub
        Boolean result = false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date appoint = null;
        Date current = null;

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String cDate = year + "-" + month + "-" + day;

        try {
            appoint = format.parse(dateappoint.getDate().toString());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            current = format.parse(cDate.toString());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        result = appoint.compareTo(current) >= 0 ? true : false;
        return result;

    }

    /**
     * call When need to commit a transaction to databases It's will try to
     * update first then insert
     *
     * @param cv        ContentValues;
     * @param uri       Table CONTENT_URI
     * @param canCommit constrains of commit (Check ERROR) always canCommit
     */
    protected void doCommit(ContentValues cv, Uri uri, Boolean canCommit) {
        if (cv != null && canCommit) {
            System.out.println("Begin Update . . .");
            ContentResolver cr = getContentResolver();
            int rows = cr.update(uri, cv, "visitno=?",
                    new String[]{share_visitno});
            System.out.println("Update " + rows + " rows");
            if (rows == 1) {
                System.out.println("Update Success");
            } else {
                System.out.println("Update Failed swap to Insert . . .");
                cr.insert(uri, cv);
                System.out.println("Insert Success");
            }
            this.finish();

        } else {
            Toast.makeText(this, getString(R.string.toast_abort) + ERROR_MSG,
                    Toast.LENGTH_SHORT).show();
            ERROR_MSG = "";
        }
    }

    protected void doHouseCommit(ContentValues cv, Uri uri, Boolean canCommit) {
        if (cv != null && canCommit) {
            System.out.println("Begin Update . . .");
            ContentResolver cr = getContentResolver();
            int rows = cr.update(uri, cv, "hcode = ?",
                    new String[]{share_hcode});
            System.out.println("Update " + rows + " rows");
            if (rows == 1) {
                System.out.println("Update Success");
                this.finish();
            } else {
                Toast.makeText(this,
                        getString(R.string.toast_abort) + ERROR_MSG,
                        Toast.LENGTH_SHORT).show();
                ERROR_MSG = "";
            }
        }
    }
}
