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

public class AddDetailHousePositionFragmentActivity extends FFCFragmentActivity {

    private TextView txthouseDetail;
    private String visit;
    private TextView txtLat;
    private TextView txtLng;
    private String hcode;
    private boolean onBackPess;
    private String oldLat;
    private String oldLng;
    private LinearLayout laywrite;
    private LinearLayout laymap;
    private CheckBox chkWrite;
    private CheckBox chkMap;
    private String pid;
    private String pcucode;
    private String hno;
    private String villno;
    private String villname;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.googlemap_addhouse_detail);
        setFindViewById();
        Intent a = getIntent();
        pid = a.getExtras().getString("pid");
        hno = a.getExtras().getString("hno");
        villno = a.getExtras().getString("villno");
        villname = a.getExtras().getString("villname");
        pcucode = a.getExtras().getString("pcucode");
        hcode = getIntent().getExtras().getString("hcode");
        visit = getIntent().getExtras().getString("visitno");
        oldLat = "";
        oldLng = "";
        txthouseDetail.setText("บ้านเลขที่ "+hno+" หมู่ที่ "+villno+" หมู่บ้าน "+villname);

    }

    private void setFindViewById() {
        txthouseDetail = (TextView) findViewById(R.id.housedetail);
        txtLat = (TextView) findViewById(R.id.txtlat);
        txtLng = (TextView) findViewById(R.id.txtlong);
        laywrite = (LinearLayout) findViewById(R.id.laywrite);
        laymap = (LinearLayout) findViewById(R.id.laymap);
        chkWrite = (CheckBox) findViewById(R.id.chkwritelatlng);
        chkWrite.setOnCheckedChangeListener(onCheckList);
        chkMap = (CheckBox) findViewById(R.id.chkmap);
        chkMap.setOnCheckedChangeListener(onCheckList);
        laywrite.setVisibility(View.GONE);
        laymap.setVisibility(View.GONE);
    }

    private void clearData() {
        //	if (onDatachangeFromMap) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("xgis", "");
        contentValues.put("ygis", "");
        String where = "hcode =?";
        String selectionArgs[] = {hcode};
        ContentResolver conResolver = getContentResolver();
        conResolver.update(House.CONTENT_URI, contentValues, where,
                selectionArgs);
        //	}
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
                onCommit();
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
                onBackPess = true;
                Intent a = new Intent(Action.INSERT);
                a.addCategory(Category.MAP_ADD_EDITPOSITION);
                a.putExtra("hcode", hcode);
                a.putExtra("mode", "0");
                a.putExtra("visitno", visit);
                String LatLng = getHousePosition(hcode);
                String detailRatius = getRadius(visit);
                if (!TextUtils.isEmpty(LatLng)) {
                    a.putExtra("LatLng", LatLng);
                }
                if (!TextUtils.isEmpty(detailRatius)) {
                    a.putExtra("detailRatius", detailRatius);
                }
                startActivity(a);
                break;
            default:
                break;
        }
    }

    private String getRadius(String visitno) {
        String detailRadius = null;
        String[] projection = {"level", "radius", "colorcode"};
        String where = "visitno=?";
        String[] selectionArgs = {visitno};
        Cursor c = getContentResolver().query(FFC506RADIUS.CONTENT_URI, projection, where, selectionArgs, "visitno");
        if (c.moveToFirst()) {
            detailRadius = c.getString(c.getColumnIndex("level")) + "," + c.getString(c.getColumnIndex("radius")) + "," + c.getString(c.getColumnIndex("colorcode"));
        }
        return detailRadius;
    }

    private String getHousePosition(String hcode) {
        String position = null;
        String[] projection = {"xgis", "ygis"};
        String where = "hcode=?";
        String[] selectionArgs = {hcode};
        Cursor c = getContentResolver().query(House.CONTENT_URI, projection, where, selectionArgs, null);
        if (c.moveToFirst()) {
//            String lat = c.getString(c.getColumnIndex("ygis"));
//            String lng = c.getString(c.getColumnIndex("xgis"));
            String lat = c.getString(c.getColumnIndex("xgis"));
            String lng = c.getString(c.getColumnIndex("ygis"));
            if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lng)) {
                position = lat + "," + lng;
            }
        }
        return position;
    }

    @Override
    protected void onResume() {
        if (onBackPess) {
            checkDataChange();
        }
        super.onResume();
    }

    private void onCommit() {
        if (chkWrite.isChecked()) {
            if (!TextUtils.isEmpty(txtLat.getText().toString()) && !TextUtils.isEmpty(txtLng.getText().toString())) {
                commitByText();
            } else {
                Toast.makeText(getApplicationContext(), R.string.please_define_house_position, Toast.LENGTH_SHORT).show();
            }
        } else {
            commitByMap();
        }
    }

    private void commitByText() {
        ContentValues contentValues = new ContentValues();
//        contentValues.put("xgis", txtLng.getText().toString());
//        contentValues.put("ygis", txtLat.getText().toString());
        contentValues.put("xgis", txtLat.getText().toString());
        contentValues.put("ygis", txtLng.getText().toString());
        String where = "hcode =?";
        String selectionArgs[] = {hcode};
        ContentResolver conResolver = getContentResolver();
        conResolver.update(House.CONTENT_URI, contentValues, where,
                selectionArgs);

        contentValues = new ContentValues();
        contentValues.put("visitno", visit);
        contentValues.put("radius", 1000);
        contentValues.put("colorcode", "#EE0000");
        conResolver = getContentResolver();
        conResolver.insert(FFC506RADIUS.CONTENT_URI, contentValues);
        if (pid == null) {
            setResult(RESULT_OK);
            this.finish();
        } else {
            dialogAskCommit();
        }

    }

    private void commitByMap() {
        String projection[] = {"xgis", "ygis"};
        String selection = "hcode=?";
        String selectionArgs[] = {hcode};
        Cursor c = getContentResolver().query(House.CONTENT_URI, projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            if (!TextUtils.isEmpty(c.getString(c.getColumnIndex("xgis"))) && !TextUtils.isEmpty(c.getString(c.getColumnIndex("ygis")))) {
                if (pid == null) {
                    setResult(RESULT_OK);
                    this.finish();
                } else {
                    dialogAskCommit();
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.please_define_house_position, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.please_define_house_position, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        String projection[] = {"xgis", "ygis"};
        String selection = "hcode=?";
        String selectionArgs[] = {hcode};
        Cursor c = getContentResolver().query(House.CONTENT_URI,
                projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            warningCommit(1);
        } else {
            onFinish();
        }
    }

    private void warningCommit(final int whtcommit) {
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
                dialog.dismiss();
                onFinish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void checkDataChange() {
        String projection[] = {"xgis", "ygis"};
        String selection = "hcode=?";
        String selectionArgs[] = {hcode};
        Cursor c = getContentResolver().query(House.CONTENT_URI, projection,
                selection, selectionArgs, null);
        if (c.moveToFirst()) {
//            String newlat = c.getString(c.getColumnIndex("ygis"));
//            String newlng = c.getString(c.getColumnIndex("xgis"));
            String newlat = c.getString(c.getColumnIndex("xgis"));
            String newlng = c.getString(c.getColumnIndex("ygis"));
            boolean onDatachangeFromMap;
            if (!TextUtils.isEmpty(oldLat) && !TextUtils.isEmpty(oldLng)) {
                onDatachangeFromMap = !newlat.equals(oldLat)
                        || !newlat.equals(oldLng) ? true : false;
                txtLat.setText(newlat);
                txtLng.setText(newlng);
                onBackPess = false;
            } else {
                txtLat.setText(newlat);
                txtLng.setText(newlng);
                onBackPess = false;
                onDatachangeFromMap = true;
            }
            oldLat = newlat;
            oldLng = newlng;
        }
    }

    private void dialogAskCommit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setCancelable(true);
        builder.setTitle("คุณได้บันทึกข้อมูลเรียบร้อยแล้ว และต้องการไปดูแผนที่ต่อหรือไม่ ?");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("ใช่และเข้าไปดูแผนที่", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Double tempLat = Double.parseDouble(txtLat.getText().toString());
                Double tempLng = Double.parseDouble(txtLng.getText().toString());
                Intent a = new Intent(Action.VIEW);
                a.addCategory(Category.MAP506);
                a.putExtra("hcode", hcode);
                a.putExtra("pid", pid);
                a.putExtra("pcucodeperson", pcucode);
                a.putExtra("lat", tempLat);
                a.putExtra("lng", tempLng);
                startActivity(a);
                onFinish();

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onFinish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onFinish() {
        this.finish();
    }

    OnCheckedChangeListener onCheckList = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.chkwritelatlng:
                    if (isChecked) {
                        chkMap.setChecked(false);
                        laywrite.setVisibility(View.VISIBLE);
                    } else {
                        laywrite.setVisibility(View.GONE);
                    }
                    break;
                case R.id.chkmap:
                    if (isChecked) {
                        chkWrite.setChecked(false);
                        laymap.setVisibility(View.VISIBLE);
                    } else {
                        laymap.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

}
