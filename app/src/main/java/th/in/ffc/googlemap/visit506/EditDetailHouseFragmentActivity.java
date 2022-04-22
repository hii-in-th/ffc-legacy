package th.in.ffc.googlemap.visit506;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragmentActivity;
import th.in.ffc.intent.Action;
import th.in.ffc.intent.Category;
import th.in.ffc.provider.HouseProvider.House;
import th.in.ffc.provider.PersonProvider.FFC506RADIUS;

public class EditDetailHouseFragmentActivity extends FFCFragmentActivity {


    private final String COLOR_RED = "#EE0000";
    private final String COLOR_ORANGE = "#ff8c00";
    private final String COLOR_YELLOW = "#ffc500";
    private TextView txthouseDetail;
    private TextView txtRadius;
    private String visit;
    private Spinner spnLevel;
    private TextView txtLat;
    private TextView txtLng;
    private LinearLayout linear;
    private String houseDetail[];
    private String LatLng;
    private String status;
    private String hcode;
    private boolean onBackPess;
    private boolean onDatachangeFromMap;
    private boolean onCommit;
    private int level;
    private String radius;
    private String oldRadius;
    private String oldLat;
    private String oldLng;
    private String choice[] = { "ปกติ", "ปานกลาง", "รุนแรง" };
    private LinearLayout laywrite;
    private LinearLayout laymap;
    private CheckBox chkWrite;
    private CheckBox chkMap;

//	private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.googlemap_edithouse_detail);
        setFindViewById();
        onBackPess = false;
        onDatachangeFromMap = false;
        onCommit = false;
        Intent a = getIntent();
        status = a.getExtras().getString("status");
        houseDetail = a.getExtras().getString("detail").split(",");
        LatLng = a.getExtras().getString("LatLng");
        visit = a.getExtras().getString("visitno");
        hcode = houseDetail[1];
        String setDetail = "บ้านเลขที่" + houseDetail[2];
        if (TextUtils.isEmpty(status) || status.equals("0")) {
            linear.setVisibility(View.GONE);
        } else {
            radius = a.getExtras().getString("radius");
            txtRadius.setText(radius);
            oldRadius = radius;
            ArrayAdapter<String> arrAd = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, choice);
            arrAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnLevel.setAdapter(arrAd);
            int level = getLevel(visit);
            if (level != -1) {
                spnLevel.setSelection(level);
            }
        }
        txthouseDetail.setText(setDetail);
        String temp[] = LatLng.split(",");
        oldLat = temp[0];
        oldLng = temp[1];
        txtLat.setText(temp[0]);
        txtLng.setText(temp[1]);
    }

    private void setFindViewById() {
        txthouseDetail = (TextView) findViewById(R.id.housedetail);
        txtRadius = (TextView) findViewById(R.id.txtradius);
        spnLevel = (Spinner) findViewById(R.id.level);
        txtLat = (TextView) findViewById(R.id.txtlat);
        txtLng = (TextView) findViewById(R.id.txtlong);
        linear = (LinearLayout) findViewById(R.id.layout_);
        laywrite = (LinearLayout) findViewById(R.id.laywrite);
        laymap = (LinearLayout) findViewById(R.id.laymap);
        chkWrite = (CheckBox) findViewById(R.id.chkwritelatlng);
        chkWrite.setOnCheckedChangeListener(onCheckList);
        chkMap = (CheckBox) findViewById(R.id.chkmap);
        chkMap.setOnCheckedChangeListener(onCheckList);
        laywrite.setVisibility(View.GONE);
        laymap.setVisibility(View.GONE);
//		progressBar = (ProgressBar)findViewById(R.id.nowLoading);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem direction = menu.add(Menu.NONE, 1, Menu.NONE, "listPatient");
        direction.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        direction.setIcon(R.drawable.ic_action_done);
        MenuItem setting = menu.add(Menu.NONE, 2, Menu.NONE, "listPatient");
        setting.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        setting.setIcon(R.drawable.ic_action_close);
        return super.onCreateOptionsMenu(menu);
    }

    public int getLevel(String visitno) {
        int level = -1;
        String projection[] = {"level"};
        String selection = "visitno=?";
        String selectionArgs[] = {visitno};
        Cursor c = getContentResolver().query(FFC506RADIUS.CONTENT_URI, projection, selection, selectionArgs, "visitno");
        if (c.moveToFirst()) {
            level = Integer.parseInt(c.getString(c.getColumnIndex("level")));
        }
        return level;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                onCommit();
                break;
            case 2:
                askDelete();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnedit:
                editPosition();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (onBackPess) {
            checkDataChange();
        }
    }

    public void checkDataChange() {
        String projection[] = {"xgis", "ygis"};
        String selection = "hcode=?";
        String selectionArgs[] = {hcode};
        Cursor c = getContentResolver().query(House.CONTENT_URI, projection,
                selection, selectionArgs, null);
        if (c.moveToFirst()) {
            String newlat = c.getString(c.getColumnIndex("ygis"));
            String newlng = c.getString(c.getColumnIndex("xgis"));
            String temp[] = LatLng.split(",");
            onDatachangeFromMap = !newlat.equals(temp[0])
                    || !newlat.equals(temp[1]) ? true : false;
            LatLng = newlat + "," + newlng;
            temp = LatLng.split(",");
            txtLat.setText(temp[1]);
            txtLng.setText(temp[0]);
            onBackPess = false;
        }
    }

    @Override
    public void onBackPressed() {
        if (onDatachangeFromMap) {
            if (!onCommit) {
                warningCommit();
            }
        } else {
            if (TextUtils.isEmpty(status) || status.equals("0")) {
                if (!oldLat.equals(txtLat.getText().toString()) || !oldLng.equals(txtLng.getText().toString())) {
                    warningCommit();
                } else {
                    setResult(RESULT_CANCELED);
                    onFinish();
                }
            } else {
                if (!oldRadius.equals(txtRadius.getText().toString()) || !oldLat.equals(txtLat.getText().toString())
                        || !oldLng.equals(txtLng.getText().toString())) {
                    warningCommit();
                } else {
                    setResult(RESULT_CANCELED);
                    this.finish();
                }
            }
        }
    }

    private void editPosition() {
        onBackPess = true;
        Intent a = new Intent(Action.EDIT);
        a.addCategory(Category.MAP_ADD_EDITPOSITION);
        a.putExtra("hcode", houseDetail[1]);
        a.putExtra("LatLng", LatLng);
        a.putExtra("status", status);
        a.putExtra("mode", "1");
        startActivity(a);
    }

    private void delPosition() {
        txtLat.setText("");
        txtLng.setText("");
        ContentValues contentValues = new ContentValues();
        contentValues.put("xgis", "");
        contentValues.put("ygis", "");
        String where = "hcode =?";
        String selectionArgs[] = {hcode};
        ContentResolver conResolver = getContentResolver();
        conResolver.update(House.CONTENT_URI, contentValues, where,
                selectionArgs);
        setResult(RESULT_OK);
        this.finish();
    }

    private void onCommit() {
        if (chkWrite.isChecked()) {
            String lat = txtLat.getText().toString();
            String lng = txtLng.getText().toString();
            ContentValues contentValues = new ContentValues();
            contentValues.put("xgis", lng);
            contentValues.put("ygis", lat);
            String where = "hcode =?";
            String selectionArgs[] = {hcode};
            ContentResolver conResolver = getContentResolver();
            conResolver.update(House.CONTENT_URI, contentValues, where,
                    selectionArgs);
            addRadius();
        } else if (chkMap.isChecked()) {
            addRadius();
        } else {
            addRadius();
            //Toast.makeText(getApplicationContext(), "��س����͡��úѹ�֡���˹�", Toast.LENGTH_SHORT).show();
        }

		/*onCommit = true;
        super.onBackPressed();*/
    }

    private void addRadius() {
        if (!TextUtils.isEmpty(status) && !status.equals("0")) {
            level = spnLevel.getSelectedItemPosition();
            String colorCode = getLevelColor(level);
            radius = !TextUtils.isEmpty(txtRadius.getText().toString()) ? txtRadius.getText().toString() : radius;
            ContentValues conValues = new ContentValues();
            conValues.put("radius", radius + "");
            conValues.put("colorcode", colorCode);
            conValues.put("level", level + "");
            String where = "visitno =?";
            String selectionArgs[] = {visit};
            ContentResolver conResolver = getContentResolver();
            int count = conResolver.update(FFC506RADIUS.CONTENT_URI, conValues, where, selectionArgs);
            if (count == 0) {
                conValues.put("visitno", visit);
                conResolver.insert(FFC506RADIUS.CONTENT_URI, conValues);
            }
        }
        setResult(RESULT_OK);
        this.finish();
    }

    private void clearData() {
        if (onDatachangeFromMap) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("xgis", oldLng);
            contentValues.put("ygis", oldLat);
            String where = "hcode =?";
            String selectionArgs[] = {hcode};
            ContentResolver conResolver = getContentResolver();
            conResolver.update(House.CONTENT_URI, contentValues, where, selectionArgs);
            setResult(RESULT_CANCELED);
            this.onFinish();
            //super.onBackPressed();
        } else {
            setResult(RESULT_CANCELED);
            this.onFinish();
        }
    }

    private void onFinish() {
        this.finish();
    }

    private void warningCommit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setTitle(getString(R.string.cancelcommit));
        builder.setMessage(getString(R.string.notcommit));
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                clearData();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void askDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setTitle(getString(R.string.deleteposition));
        builder.setMessage(getString(R.string.wantdeleteposition));
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                delPosition();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private String getLevelColor(int levelSelect) {
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

    OnCheckedChangeListener onCheckList = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.chkwritelatlng:
                    if (isChecked) {
                        laywrite.setVisibility(View.VISIBLE);
                        laymap.setVisibility(View.GONE);
                        chkMap.setChecked(false);
                    } else {
                        laywrite.setVisibility(View.GONE);
                    }
                    break;
                case R.id.chkmap:
                    if (isChecked) {
                        laymap.setVisibility(View.VISIBLE);
                        laywrite.setVisibility(View.GONE);
                        chkWrite.setChecked(false);
                    } else {
                        laymap.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

}
