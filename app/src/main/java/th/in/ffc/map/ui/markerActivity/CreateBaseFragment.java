package th.in.ffc.map.ui.markerActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.ibus.phototaker.ImageResizer;
import com.ibus.phototaker.PhotoTaker;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import th.in.ffc.R;
import th.in.ffc.app.FFCFragment;
import th.in.ffc.map.FGActivity;
import th.in.ffc.map.database.FGDatabaseManager;
import th.in.ffc.map.database.SpinnerItem;
import th.in.ffc.map.gps.FGGPSManager;
import th.in.ffc.map.overlay.FGOverlayManager;
import th.in.ffc.map.service.GeneralAsyncTask;
import th.in.ffc.map.system.FGSystemManager;
import th.in.ffc.map.value.FinalValue;
import th.in.ffc.map.value.MARKER_TYPE;
import th.in.ffc.map.village.spot.Spot;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class CreateBaseFragment extends FFCFragment implements OnClickListener,
        OnFocusChangeListener, OnItemClickListener, OnItemSelectedListener,
        OnCheckedChangeListener {

    private FGSystemManager fgSystemManager;

    // private Spinner spinnerVillage;
    private AutoCompleteTextView autoTextInfo;
    private Spinner spinner_misc;

    private EditText editTextLatitude;
    private EditText editTextLongitude;

    private TextView textView_Second;

    private CheckBox checkBoxAutoGPS;

    private Button buttonOK;
    private Button buttonCancel;
    private Button delete_marker_button;

    // private Button add_btn1;
    private Button add_btn2;
    private Button remove_btn;

    private ImageButton button_photo;

    private ImageView image_location;

    // private double doubleLatitudeOriginal;
    // private double doubleLongitudeOriginal;

    private PhotoTaker pt;

    private MARKER_TYPE type;

    private Bundle bundle;
    private boolean edit;

    private SpinnerItem infoResult;
    private boolean validationEnabled;

    private SpinnerItem[] items;

    private final Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case FGActivity.INITIALIZE:
                    ArrayAdapter<SpinnerItem> adapter = new ArrayAdapter<SpinnerItem>(
                            fgSystemManager.getFGActivity(), R.layout.list_item,
                            items);
                    CreateBaseFragment.this.autoTextInfo.setAdapter(adapter);
                    adapter = new ArrayAdapter<SpinnerItem>(
                            fgSystemManager.getFGActivity(), R.layout.list_item,
                            items);
                    CreateBaseFragment.this.spinner_misc.setAdapter(adapter);
                    items = null;
                    break;
                case 1:
                    CreateBaseFragment.this.button_photo.setEnabled(false);
                    CreateBaseFragment.this.buttonOK.setEnabled(false);

                    CreateBaseFragment.this.spinner_misc.setEnabled(false);
                    CreateBaseFragment.this.autoTextInfo.setEnabled(false);
                    break;
                case 2:
                    CreateBaseFragment.this.buttonOK.setEnabled(true);
                    CreateBaseFragment.this.button_photo.setEnabled(true);
                    CreateBaseFragment.this.autoTextInfo.setEnabled(true);
                    CreateBaseFragment.this.spinner_misc.setEnabled(true);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fgSystemManager = FGActivity.fgsys;
        bundle = this.getActivity().getIntent().getExtras();
        Log.d("TAG", "*" + bundle + "*");

        type = (MARKER_TYPE) bundle.get("type");

        if (type == null) // Default Marker Type
            type = MARKER_TYPE.HOUSE;

        edit = bundle.getBoolean("edit");
        pt = FGActivity.fgsys.getPhotoTaker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // bundle = this.getArguments();
        // Log.d("TAG", "*"+bundle+"*");

        View v = inflater.inflate(R.layout.create_marker_fragment, container,
                false);

        // this.spinnerVillage = (Spinner) v.findViewById(R.id.spinner_village);
        this.autoTextInfo = (AutoCompleteTextView) v
                .findViewById(R.id.autotext_misc);
        this.autoTextInfo.setThreshold(1);
        this.autoTextInfo.setValidator(new InfoValidator());
        this.autoTextInfo.setOnItemClickListener(this);
        this.autoTextInfo.setOnFocusChangeListener(this);

        this.spinner_misc = (Spinner) v.findViewById(R.id.spinner_misc);
        this.spinner_misc.setVisibility(View.VISIBLE);
        this.spinner_misc.setOnItemSelectedListener(this);

        this.editTextLatitude = (EditText) v
                .findViewById(R.id.edittext_latitude);
        this.editTextLatitude.setText(bundle.getDouble("doubleLatitude") + "");
        // this.doubleLatitudeOriginal = bundle.getDouble("doubleLatitude");

        this.editTextLongitude = (EditText) v
                .findViewById(R.id.edittext_longitude);
        this.editTextLongitude
                .setText(bundle.getDouble("doubleLongitude") + "");
        // this.doubleLongitudeOriginal = bundle.getDouble("doubleLongitude");

        this.checkBoxAutoGPS = (CheckBox) v
                .findViewById(R.id.checkbox_auto_gps);
        // this.checkBoxAutoGPS.setChecked(false);

        this.buttonOK = (Button) v.findViewById(R.id.button_ok);
        this.buttonCancel = (Button) v.findViewById(R.id.button_cancel);
        this.delete_marker_button = (Button) v
                .findViewById(R.id.delete_marker_button);
        this.delete_marker_button.setVisibility(View.GONE);

        // this.add_btn1 = (Button) v.findViewById(R.id.AddBtn1);
        this.add_btn2 = (Button) v.findViewById(R.id.AddBtn2);
        this.remove_btn = (Button) v.findViewById(R.id.remove_image_button);
        this.remove_btn.setEnabled(false);

        this.button_photo = (ImageButton) v.findViewById(R.id.button_photo);

        this.image_location = (ImageView) v.findViewById(R.id.image_location);
        this.image_location.setVisibility(View.GONE);

        this.textView_Second = (TextView) v.findViewById(R.id.textview_second);

        this.buttonOK.setOnClickListener(this);
        this.buttonCancel.setOnClickListener(this);

        // this.add_btn1.setOnClickListener(this);
        this.add_btn2.setOnClickListener(this);

        this.remove_btn.setOnClickListener(this);

        this.button_photo.setOnClickListener(this);

        this.delete_marker_button.setOnClickListener(this);

        this.checkBoxAutoGPS.setSelected(false);

        this.checkBoxAutoGPS.setOnCheckedChangeListener(this);

        initializeContent();

        return v;
    }

    private void initializeContent() {

        this.textView_Second
                .setText(getResources().getString(type.getNameID()));

        if (edit) {
            // this.add_btn1.setVisibility(View.GONE);
            this.add_btn2.setVisibility(View.GONE);
            this.delete_marker_button.setVisibility(View.VISIBLE);

            this.autoTextInfo.setText(bundle.getString("text2"));
            this.autoTextInfo.setEnabled(false);

            this.spinner_misc.setSelection(Adapter.NO_SELECTION);
            this.spinner_misc.setEnabled(false);
            this.spinner_misc.setVisibility(View.GONE);

            return;
        }

        Runnable r = new Runnable() {
            @Override
            public void run() {
                LinkedHashSet<SpinnerItem> temp = new LinkedHashSet<SpinnerItem>();

                FGDatabaseManager db = FGActivity.fgsys.getFGDatabaseManager();
                Iterator<Spot> item = db.getAvailable().values().iterator();
                int i = 0;
                while (item.hasNext()) {
                    Spot current = item.next();
                    if (current.getUid().equals(type.name())) {

                        SpinnerItem tmp = new SpinnerItem(
                                current.getRepresentativeString(type
                                        .getSpinnerText()), current.getID());

                        tmp.setInt(i++);
                        temp.add(tmp);
                    }
                }

                Log.d("TAG!", " size is " + temp.size());

                if (temp.size() == 0) {
                    item = null;
                    temp = null;
                    handler.sendEmptyMessage(1);
                    return;
                } else if (!CreateBaseFragment.this.buttonOK.isEnabled()) {
                    handler.sendEmptyMessage(2);
                }

                CreateBaseFragment.this.items = temp
                        .toArray(new SpinnerItem[temp.size()]);

                item = null;
                temp.clear();
                temp = null;
                handler.sendEmptyMessage(0);
            }
        };

        new GeneralAsyncTask(this.getActivity(), null, null, 0, -1).execute(r,
                null);

    }

    public void setType(MARKER_TYPE type) {
        this.type = type;

        this.autoTextInfo.setText("");

        this.spinner_misc.setSelection(Adapter.NO_SELECTION);

        this.initializeContent();
    }

    public MARKER_TYPE getType() {
        return type;
    }

    @Override
    public void onResume() {
        super.onResume();

        File file = pt.getOutput();
        if (file != null && file.exists()) {
            Bitmap bitmap = ImageResizer.decodeFileSubSampled(file, 480, 320);

            this.image_location.setImageBitmap(bitmap);
            this.image_location.setVisibility(View.VISIBLE);
            this.remove_btn.setEnabled(true);

            // bitmap.recycle();
        } else if (edit) {
            Bitmap bmp = FGActivity.getPicture(type,
                    bundle.getString("id_filename"));
            if (bmp != null) {
                this.image_location.setImageBitmap(bmp);
                this.image_location.setVisibility(View.VISIBLE);
                this.remove_btn.setEnabled(true);
                // bmp.recycle();
            } else {
                this.image_location.setImageDrawable(null);
                this.image_location.setVisibility(View.GONE);
                this.remove_btn.setEnabled(false);
            }

        } else {
            this.image_location.setImageDrawable(null);
            this.image_location.setVisibility(View.GONE);
            this.remove_btn.setEnabled(false);
        }
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.button_ok:
                button_OK_Click();
                break;
            case R.id.button_cancel:
                button_cancel_Click();
                break;
            case R.id.button_photo:
                button_photo_Click();
                break;
            case R.id.AddBtn2:
                add_btn2_Click();
                break;
            case R.id.remove_image_button:
                remove_btn_Click();
                break;
            case R.id.delete_marker_button:
                delete_marker_button_Click();
                break;
        }

    }

    private void delete_marker_button_Click() {
        if (edit) {
            Resources res = this.getResources();
            Builder builder = new Builder(getActivity(),android.R.style.Theme_Material_Light_Dialog_Alert);
            builder.setTitle(res
                    .getString(R.string.STRING_CONFIRM_DELETE_HOUSE_MARKER_TITLE));
            builder.setMessage(res
                    .getString(R.string.STRING_CONFIRM_DELETE_HOUSE_MARKER_MESSAGE));
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String key = type + "_"
                                    + bundle.getString("id_filename");
                            Spot spot = CreateBaseFragment.this.fgSystemManager
                                    .getFGDatabaseManager()
                                    .getSpotInMarked(key);
                            if (CreateBaseFragment.this.fgSystemManager
                                    .removeMarkerOnMap(spot)) {

                                String pic_path = FGActivity.getPicturePath(
                                        Enum.valueOf(MARKER_TYPE.class,
                                                spot.getUid()), spot.getID());
                                File file = new File(pic_path);
                                file.delete();

                                String pic_path1 = FGActivity.getPicturePath(
                                        Enum.valueOf(MARKER_TYPE.class,
                                                spot.getUid()), spot.getID()
                                                + "_thumb");
                                File file1 = new File(pic_path1);
                                file1.delete();

                                dialog.dismiss();
                                CreateBaseFragment.this.button_cancel_Click();
                            }
                        }
                    });

            builder.setNegativeButton(R.string.cancel, null);
            builder.create().show();

        }
    }

    private void remove_btn_Click() {
        FGOverlayManager.pic_changed = true;
        PhotoTaker.clearCache();
        this.image_location.setImageDrawable(null);
        this.image_location.setVisibility(View.GONE);
        this.remove_btn.setEnabled(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PhotoTaker.IMAGE_CAPTURE
                || requestCode == PhotoTaker.PICK_FROM_FILE) {
            this.resultForPicture(requestCode, resultCode, data);
        } else if (requestCode == FinalValue.INT_REQUEST_NEW_INFO
                && resultCode == Activity.RESULT_OK) {
            this.initializeContent();
        }
    }

    private void resultForPicture(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            FGOverlayManager.pic_changed = true;

            File file = null;

            switch (requestCode) {
                case PhotoTaker.IMAGE_CAPTURE:
                    file = pt.getCameraTempFile();
                    break;
                case PhotoTaker.PICK_FROM_FILE:
                    Uri img = data.getData();
                    if (img != null) {
                        Log.d("TAG!", "Raw uri is " + img.toString());
                        String realPath = getRealPathFromURI(img);
                        Log.d("TAG!", "Resolved path is " + realPath);
                        file = new File(realPath);
                    }
                    break;
            }

            if (file != null && file.exists()) {
                pt.setOutput(file);
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};

        Cursor cursor = this.getActivity().getContentResolver()
                .query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(proj[0]);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();

        return result;
    }

    private void button_photo_Click() {
        pt.setActivity(this.getActivity());
        pt.doShowDialog();
    }

    public void button_cancel_Click() {
        FGOverlayManager.pic_changed = false;
        PhotoTaker.clearCache();
        this.getActivity().finish();
    }

    private void startInfoActivity(Class<? extends Activity> c) {
        getFFCActivity().startActivityForResult(
                new Intent(this.getActivity(), c),
                FinalValue.INT_REQUEST_NEW_INFO);
    }

    private void button_OK_Click() {
        // SpinnerItem vill =
        // (SpinnerItem)this.spinnerVillage.getSelectedItem();

        if (!edit) {

            infoResult = null;
            validationEnabled = true;
            autoTextInfo.performValidation();
            if (infoResult == null) {
                Toast.makeText(
                        getActivity(),
                        getActivity().getResources().getString(
                                R.string.incomplete_info), Toast.LENGTH_LONG)
                        .show();
                return;
            }

            SpinnerItem selected = infoResult;

            Spot spot = this.fgSystemManager.getFGDatabaseManager().getSpotInAvailable(type + "_" + selected.getID());

            String stringLatitude = this.editTextLatitude.getText().toString();
            String stringLongitude = this.editTextLongitude.getText().toString();
            double doubleLatitude = Double.parseDouble(stringLatitude);
            double doubleLongitude = Double.parseDouble(stringLongitude);

            Spot builder = new Spot(spot.pcucode, type, spot.stringVillCode, spot.intPartialID, doubleLatitude, doubleLongitude, spot.getBundle());
//
//            spot.setLatitude(doubleLatitude);
//            spot.setLongitude(doubleLongitude);
            Drawable drawable = image_location.getDrawable();
            Bitmap bmp = null;
            if (drawable != null) {
                if (drawable instanceof BitmapDrawable) {
                    bmp = ((BitmapDrawable) drawable).getBitmap();
                    pt.writeBitmapToFile(type, spot.getID(), bmp);
                    File myFilesDir = new File(FGActivity.getPictureDir()
                            + type);
                    File thumbOut = new File(myFilesDir, spot.getID()
                            + "_thumb.jpg");
                    ImageResizer.doResizeImage(bmp, thumbOut);
                }
            }

            // Toast.makeText(this.fgSystemManager.getFGActivity(),
            // "success="+b, Toast.LENGTH_LONG).show();

            this.fgSystemManager.markMarkerOnMap(builder);
//            IMapController mapController = this.fgSystemManager
//                    .getFGMapManager().getMapController();
//            mapController.setCenter(builder.getPoint());


        } else {
            Spot spot = this.fgSystemManager.getFGDatabaseManager()
                    .getSpotInMarked(
                            type + "_" + bundle.getString("id_filename"));

            String stringLatitude = this.editTextLatitude.getText().toString();
            double doubleLatitude = Double.parseDouble(stringLatitude);

            String stringLongitude = this.editTextLongitude.getText()
                    .toString();
            double doubleLongitude = Double.parseDouble(stringLongitude);

            spot.setLatitude(doubleLatitude);
            spot.setLongitude(doubleLongitude);

            Log.d("TAG!", "pic_changed=" + FGOverlayManager.pic_changed);
            if (FGOverlayManager.pic_changed) {
                Drawable drawable = image_location.getDrawable();
                if (drawable == null) {
                    FGActivity.removePicture(type, spot.getID());
                } else {
                    Bitmap bmp = null;

                    if (drawable instanceof BitmapDrawable) {
                        bmp = ((BitmapDrawable) drawable).getBitmap();
                        pt.writeBitmapToFile(type, spot.getID(), bmp);

                        File myFilesDir = new File(FGActivity.getPictureDir()
                                + type);
                        File thumbOut = new File(myFilesDir, spot.getID()
                                + "_thumb.jpg");

                        ImageResizer.doResizeImage(bmp, thumbOut);

                    }

                }

                FGOverlayManager.pic_changed = false;
            }

            this.fgSystemManager.editMarkerOnMap(spot);
        }
        PhotoTaker.clearCache();
        Log.d("TAG!", "Finish!");
        this.getActivity().finish();
    }

    private void add_btn2_Click() {
        this.startInfoActivity(this.type.getIncreaseClass());
    }

    private class InfoValidator implements AutoCompleteTextView.Validator {

        @Override
        public boolean isValid(CharSequence text) {

            if (!validationEnabled && text.length() != 0)
                return true;

            infoResult = null;

            Log.d("TAG!", "Begin work!");

            String str = text.toString().toLowerCase();

            // for (int i = 0; i < autoTextInfo.getAdapter().getCount(); i++) {
            // SpinnerItem item = (SpinnerItem)
            // autoTextInfo.getAdapter().getItem(i);
            // Log.d("TAG!", "-- " + item.getName());
            // }

            if (autoTextInfo.getAdapter().getCount() != 0) {
                SpinnerItem item = (SpinnerItem) autoTextInfo.getAdapter()
                        .getItem(0);
                Log.d("TAG!", item.getName());
                if (item.getName().toLowerCase().indexOf(str) != -1) {
                    infoResult = item;
                    // Toast.makeText(getActivity(),
                    // "Successful : " + item.getID(), Toast.LENGTH_LONG)
                    // .show();
                }
            }

            validationEnabled = false;

            return false;

        }

        @Override
        public CharSequence fixText(CharSequence invalidText) {
            // Log.d("TAG!", "this word: *" + invalidText + "*");
            if (infoResult != null) {
                spinner_misc.setSelection(infoResult.getInt());
                return infoResult.getName();
            }

            return invalidText;
        }

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!edit && v.getId() == R.id.autotext_misc && !hasFocus) {
            validationEnabled = true;
            autoTextInfo.performValidation();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        infoResult = (SpinnerItem) parent.getItemAtPosition(position);
        autoTextInfo.setText(infoResult.getName());
        spinner_misc.setSelection(infoResult.getInt());
        autoTextInfo.dismissDropDown();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        SpinnerItem item = (SpinnerItem) parent.getItemAtPosition(position);
        Log.d("TAG!", "Selected : " + item.toString());
        autoTextInfo.setText(item.getName());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.isChecked()) {

            FGGPSManager fgGPSManager = this.fgSystemManager.getFGGPSManager();
            final Location location = fgGPSManager.getLastKnownLocation();
            Resources res = this.getResources();

            if (location != null) {
                Builder b = new Builder(
                        this.getActivity(),android.R.style.Theme_Material_Light_Dialog_Alert);
                b.setTitle(res.getString(R.string.STRING_AUTO_GPS_TITLE));
                b.setIcon(android.R.drawable.ic_input_add);
                b.setCancelable(false);
                double doubleLatitude = location.getLatitude();
                double doubleLongitude = location.getLongitude();

                String stringMessage = res
                        .getString(R.string.STRING_AUTO_GPS_MESSAGE_HEADER)
                        + "\n"
                        + "     "
                        + res.getString(R.string.latitude)
                        + "  : "
                        + doubleLatitude
                        + "\n"
                        + "     "
                        + res.getString(R.string.longitude)
                        + " : "
                        + doubleLongitude
                        + "\n"
                        + res.getString(R.string.STRING_AUTO_GPS_MESSAGE_FOOTER);

                b.setMessage(stringMessage);
                b.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                CreateBaseFragment.this.editTextLatitude
                                        .setText(location.getLatitude()
                                                + "");
                                CreateBaseFragment.this.editTextLongitude
                                        .setText(location
                                                .getLongitude() + "");

                            }
                        });

                b.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                CreateBaseFragment.this.checkBoxAutoGPS
                                        .setChecked(false);
                            }
                        });
//2. now setup to change color of the button

                 b.create().show();

//                Button positiveButton = myDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                Button negativeButton = myDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
////                Button neutralButton = myDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
//                positiveButton.setTextColor(Color.parseColor("#FFFFFFFF"));
//                positiveButton.setBackgroundColor(Color.parseColor("#FFB8B8B8"));
//
//                negativeButton.setTextColor(Color.parseColor("#FFFFFFFF"));
//                negativeButton.setBackgroundColor(Color.parseColor("#FFB8B8B8"));

//                neutralButton.setTextColor(Color.parseColor("#FF1B5AAC"));
//                neutralButton.setBackgroundColor(Color.parseColor("#FFD9E9FF"));

            } else {

                final AlertDialog dialog = this.fgSystemManager.getFGDialogManager()
                        .getAlertDialogLocationNotFound(this.getActivity());


                dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @SuppressLint("ResourceType")
                    @Override
                    public void onShow(DialogInterface arg0) {
//                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(COLOR_I_WANT);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText(R.color.black);
                    }
                });

                this.checkBoxAutoGPS.setChecked(false);
            }
        } else {
            this.editTextLatitude.setText(bundle.getDouble("doubleLatitude")
                    + "");
            this.editTextLongitude.setText(bundle.getDouble("doubleLongitude")
                    + "");
        }
    }
}
