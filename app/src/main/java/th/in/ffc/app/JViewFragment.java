package th.in.ffc.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import th.in.ffc.R;
import th.in.ffc.persist.PersonPersist;
import th.in.ffc.provider.DbOpenHelper;

import java.util.ArrayList;

public class JViewFragment extends FFCFragment {
    public String share_pcucode = null;
    public String share_hcode = null;
    public String share_visitno = null;
    public String share_pid = null;
    public String[] array;
    public Bundle b;
    public DbOpenHelper db = new DbOpenHelper(this.getActivity());

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                //onEditPressed();
                break;
            case R.id.back:
                getActivity().finish();
                break;
        }
        return true;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getParameter();

    }

    public void getParameter() {

        share_pcucode = getFFCActivity().getPcuCode();
        b = getArguments();
        if (b != null) {
            share_visitno = b.getString("visitno");
            share_hcode = b.getString("hcode");
            share_pid = b.getString("pid");
        }
    }

    // THIS METHOD FOR
    // Create dialogue to confirm when user click delete button to ensure that
    // user is surely to delete the view he is currently access
    // When user confirm by click OK button Then will call a method "Delete()"
    // that will interact with JhcisAdapter.getJhcis().delete() method
    // else Dialogue will be dismiss and nothing happen
    // MUST OVERRIDE METHOD Delete() FOR EACH FORM
    public void onDeletePressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setMessage(getResources().getString(R.string.dialog_delete))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Delete();
                                Toast.makeText(getActivity(),
                                        getString(R.string.toast_deletecommit),
                                        Toast.LENGTH_SHORT).show();
                                getActivity().finish();
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

    // THIS METHOD FOR
    // Create dialogue to confirm when user click edit button to ensure that
    // user is surely to delete the view he is currently access
    // When user confirm by click OK button Then will call a method "Edit()"
    // that will interact with Intent to new Activity that contain edit content
    // for each from
    // else Dialogue will be dismiss and nothing happen
    // MUST OVERRIDE METHOD Delete() FOR EACH FORM
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

    // OVERRIDE THIS 2 METHOD FOR EACH FORM
    protected void Delete() {

    }

    protected void Edit() {

    }

    // THIS METHOD IS FOR
    // Query database from String sql recived and put any dataset to String
    // array[];
    protected void setContentQuery(String sql) {
        // TODO Auto-generated method stub


        try {
            Cursor c = db.getReadableDatabase().rawQuery(sql, null);
            int size = c.getColumnCount();
            this.array = new String[size];
            if (c.moveToFirst()) {
                for (int i = 0; i < size; i++) {
                    array[i] = c.getString(i);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("LOG", e.toString());
        }

    }

    // THIS METHOD IS FOR
    // Check the data which will be display in Each TextView that has values to
    // display
    // Else it will be show data from R.string.no_input ("����բ�����")
    protected void setTextViewisNull(TextView obj, String data) {
        if (data != null) {
            obj.setText(data);
        } else {
            obj.setText(getString(R.string.no_input));
            //obj.setTextColor(R.color.darker_gray);
        }
    }

    // setTextView by look up to date which get from sql statement
    protected void setSpinnerQueryToTextView(TextView obj, String data,
                                             String sql, int type) {
        String output = null;
        Log.d("LOG SQL", sql);
        switch (type) {
            case 1:
                output = SpinnerQuery(data, sql);
                break;
            case 2:
                output = SpinnerQueryPerson(data, sql);
                break;
        }
        if (output.equals(getString(R.string.no_input))) {
            //obj.setTextColor(R.color.darker_gray);
            obj.setText(output);
        } else {
            obj.setText(output);
        }
    }

    // setTextView by look up to date which get from resource
    protected void setSpinnerResourceToTextView(TextView obj, String data,
                                                int res) {
        String output = SpinnerResource(obj, data, res);
        if (output.equals(getString(R.string.no_input))) {
            //obj.setTextColor(R.color.darker_gray);
            obj.setText(output);
        } else {
            obj.setText(output);
        }
    }

    // THIS METHOD IS FOR
    // Check the data which will be display in Each TextView that come from a
    // set of data that have to lookup in other table will has values to display
    // andreturn KeyValue
    // Else it will be return data from R.string.no_input ("����բ�����")
    protected String SpinnerQuery(String data, String sql) {
        if (data != null) {
            String output = null;
            try {
                Cursor c = db.getReadableDatabase().rawQuery(sql, null);
                if (c.moveToFirst() && c.getString(1) != null) {
                    output = c.getString(1);
                } else {
                    output = getString(R.string.db_err);
                }


                return output;
            } catch (Exception e) {
                // TODO: handle exception
                return getString(R.string.no_input);
            }
        } else {
            return getString(R.string.no_input);
        }

    }

    // THIS METHOD IS FOR
    // Check the data which will be display in Each TextView that have to lookup
    // in a person table has a value to display and return
    // prename+firstname+lastname
    // Else it will be return data from R.string.no_input ("����բ�����")
    protected String SpinnerQueryPerson(String data, String sql) {

        if (data != null) {
            try {
                ArrayList<PersonPersist> list = new ArrayList<PersonPersist>();

                Cursor c = db.getReadableDatabase().rawQuery(sql, null);
                if (c.moveToFirst()) {
                    do {
                        PersonPersist item = new PersonPersist(
                                c.getString(0), c.getString(1),
                                c.getString(2));
                        list.add(item);
                    } while (c.moveToNext());
                }


                int select = list.size() - 1;
                for (int i = 0; i < list.size(); i++) {
                    if (data.equals(list.get(i).getPid())) {
                        select = i;
                        break;
                    }
                }
                return list.get(select).toString();
            } catch (Exception e) {
                // TODO: handle exception
                return getString(R.string.no_input);
            }
        } else {
            return getString(R.string.no_input);
        }

    }

    // THIS METHOD IS FOR
    // Check the data which will be display in Each TextView that have to query
    // from String_Array in string_array.xml has a value to display and return
    // String value
    // Else it will be returndata from R.string.no_input ("����բ�����")
    protected String SpinnerResource(TextView obj, String data, int res) {
        // TODO Auto-generated method stub
        if (data != null) {
            String[] array2 = getResources().getStringArray(res);
            String output = array2[Integer.parseInt(data)];
            setTextSpinnerColor(obj, res, data);
            return output;
        } else {
            return getString(R.string.no_input);
        }
    }

    // THIS METHOD IS FOR SET DATA WHICH IS QUERY FROM SPINNER AND DETERMINE
    // TEXT COLOR
    protected void setTextSpinnerColor(TextView obj, int Res, String data) {
        int choice = Integer.parseInt(data);
        int color = 0;
        switch (Res) {
            case R.array.houseEnough:
                switch (choice) {
                    case 0:
                        color = getResources().getColor(R.color.holo_red_light);
                        break;
                    case 1:
                        color = getResources().getColor(R.color.holo_green_light);
                        break;
                    case 2:
                        color = getResources().getColor(R.color.lighter_gray);
                        break;
                }
                break;
            case R.array.houseEndure:
                switch (choice) {
                    case 0:
                        color = getResources().getColor(R.color.holo_red_light);
                        break;
                    case 1:
                        color = getResources().getColor(R.color.holo_orange_light);
                        break;
                    case 2:
                        color = getResources().getColor(R.color.holo_green_light);
                        break;
                    case 3:
                        color = getResources().getColor(R.color.lighter_gray);
                        break;
                }
                break;
            case R.array.houseClean:
                switch (choice) {
                    case 0:
                        color = getResources().getColor(R.color.holo_red_light);
                        break;
                    case 1:
                        color = getResources().getColor(R.color.holo_green_light);
                        break;
                    case 2:
                        color = getResources().getColor(R.color.lighter_gray);
                        break;
                }
                break;
            case R.array.houseHave:
                switch (choice) {
                    case 0:
                        color = getResources().getColor(R.color.holo_red_light);
                        break;
                    case 1:
                        color = getResources().getColor(R.color.holo_green_light);
                        break;
                    case 2:
                        color = getResources().getColor(R.color.lighter_gray);
                        break;
                }
                break;
            case R.array.houseCorrect:
                switch (choice) {
                    case 0:
                        color = getResources().getColor(R.color.holo_red_light);
                        break;
                    case 1:
                        color = getResources().getColor(R.color.holo_green_light);
                        break;
                    case 2:
                        color = getResources().getColor(R.color.lighter_gray);
                        break;
                }
                break;
            case R.array.houseWaste:
                switch (choice) {
                    case 0:
                        color = getResources().getColor(R.color.holo_red_light);
                        break;
                    case 1:
                    case 2:
                    case 5:
                        color = getResources().getColor(R.color.holo_orange_light);
                        break;
                    case 3:
                    case 4:
                        color = getResources().getColor(R.color.holo_green_light);
                        break;
                    case 6:
                        color = getResources().getColor(R.color.lighter_gray);
                        break;
                }
                break;
            case R.array.housePet:
                switch (choice) {
                    case 0:
                        color = getResources().getColor(R.color.holo_red_light);
                        break;
                    case 1:
                        color = getResources().getColor(R.color.holo_green_light);
                        break;
                    case 2:
                        color = getResources().getColor(R.color.lighter_gray);
                        break;
                }
                break;
            case R.array.houseComplete:
                switch (choice) {
                    case 0:
                        color = getResources().getColor(R.color.holo_red_light);
                        break;
                    case 1:
                        color = getResources().getColor(R.color.holo_green_light);
                        break;
                    case 2:
                        color = getResources().getColor(R.color.lighter_gray);
                        break;
                }
                break;
            case R.array.houseUse2:
                switch (choice) {
                    case 0:
                        color = getResources().getColor(R.color.holo_red_light);
                        break;
                    case 1:
                        color = getResources().getColor(R.color.holo_green_light);
                        break;
                    case 2:
                        color = getResources().getColor(R.color.lighter_gray);
                        break;
                }
                break;
            case R.array.houseUse:
                switch (choice) {
                    case 0:
                        color = getResources().getColor(R.color.holo_red_light);
                        break;
                    case 1:
                        color = getResources().getColor(R.color.holo_orange_light);
                        break;
                    case 2:
                        color = getResources().getColor(R.color.holo_green_light);
                        break;
                    case 3:
                        color = getResources().getColor(R.color.lighter_gray);
                        break;
                }
                break;
            default:
                color = getResources().getColor(R.color.holo_blue_dark);
                break;
        }
        obj.setTextColor(color);
    }
}
