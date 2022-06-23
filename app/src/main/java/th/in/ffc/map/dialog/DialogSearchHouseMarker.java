package th.in.ffc.map.dialog;

import android.app.Dialog;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import th.in.ffc.R;
import th.in.ffc.map.FGActivity;
import th.in.ffc.map.database.DatabaseManager;
import th.in.ffc.map.service.GeneralAsyncTask;
import th.in.ffc.map.system.FGSystemManager;
import th.in.ffc.map.value.FinalValue;
import th.in.ffc.map.value.MARKER_TYPE;
import th.in.ffc.map.village.spot.Spot;

import java.util.Collection;

public class DialogSearchHouseMarker extends Dialog implements View.OnClickListener {

    private FGSystemManager fgSystemManager;

    private EditText editTextHouse;

    private Button buttonOK;
    private Button buttonCancel;

    private Handler handler;

    public DialogSearchHouseMarker(final FGSystemManager fgSystemManager) {
        super(fgSystemManager.getFGActivity());

        this.fgSystemManager = fgSystemManager;

        this.setContentView(R.layout.dialog_search_house);

        this.setCancelable(true);
        this.setCanceledOnTouchOutside(true);
        this.setTitle(FinalValue.STRING_SEARCH_HOUSE_MARKER_TITLE);

        editTextHouse = (EditText) findViewById(R.id.edittext_house);

        buttonOK = (Button) findViewById(R.id.OKButton);
        buttonCancel = (Button) findViewById(R.id.CancelButton);

        buttonOK.setOnClickListener(this);

        this.buttonCancel.setOnClickListener(this);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case FGActivity.INITIALIZE:
                        Spot item = (Spot) msg.obj;
                        fgSystemManager.getFGMapManager().getMapController().animateTo(item.getPoint());
                        //fgSystemManager.getFGMapManager().getGesture().onItemDoubleTap(-1, item);
                        break;
                    case FGActivity.FAILED:
                        Toast.makeText(fgSystemManager.getFGActivity().getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
    }

    private void searchHelper(final String query) {
        DatabaseManager db = fgSystemManager.getFGDatabaseManager().getDatabaseManager();
        Spot item = null;
        if (db.openDatabase()) {
            Cursor cur = db
                    .getCursor("SELECT distinct h.hno,h.hcodee FROM person p,house h WHERE p.hcode=h.hcode and (h.xgis is not null and not h.xgis = '0.0' and not h.xgis = '0' and not h.xgis = ' ' and not h.xgis = '  ' and not h.xgis = '') and (h.ygis is not null and not h.ygis = '0.0' and not h.ygis = '0' and not h.ygis = ' ' and not h.ygis = '  ' and not h.ygis = '') and "
                            + query);

            if (cur.moveToFirst()) {
                String hno = cur.getString(0);
                item = normalSearch(hno);
            }

            showResult(item);

            cur.close();
            cur = null;
            db.closeDatabase();
        } else {
            Message msg = new Message();
            msg.what = FGActivity.FAILED;
            msg.obj = "Database cannot be opened. Please try again.";
            handler.sendMessage(msg);
        }

    }

    private Spot normalSearch(String str) {
        // ItemizedIconOverlay<Spot> marker = fgSystemManager
        // .getFGOverlayManager().getMarker();
        Collection<Spot> marked = this.fgSystemManager.getFGDatabaseManager().getMarked().values();
        String house_codename = MARKER_TYPE.HOUSE.name();

        for (Spot spot : marked) {
            if (spot.getUid().equals(house_codename) && spot.getBundle().getString("HNo").equals(str)) {
                return spot;
            }
        }

        return null;
    }

    private void showResult(Spot item) {
        Message msg = new Message();
        if (item != null) {
            msg.what = FGActivity.INITIALIZE;
            msg.obj = item;
        } else {
            msg.what = FGActivity.FAILED;
            msg.obj = "Location Not Found.";
        }
        handler.sendMessage(msg);
    }

    public boolean isNumeric(String str) {
        try {
            Long.parseLong(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.OKButton:
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        String str = editTextHouse.getText().toString().trim().replaceAll("\\s+", " ");

                        if (str.length() == 13 && isNumeric(str)) {
                            searchHelper("p.idcard = '" + str + "'");
                        } else if (Character.isLetter(str.charAt(0))) {
                            searchHelper("(p.fname || ' ' || p.lname) like '%" + str + "%'");
                        } else {
                            showResult(normalSearch(str));
                        }
                    }
                };
                new GeneralAsyncTask(fgSystemManager.getFGActivity(), null, null, 0, -1).execute(r, null);
                break;
        }
        dismiss();
    }
}
