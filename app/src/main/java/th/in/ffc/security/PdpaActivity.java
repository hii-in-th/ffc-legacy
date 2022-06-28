package th.in.ffc.security;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;

import th.in.ffc.R;

public class PdpaActivity extends AppCompatActivity {
    Button btnOK;
    Button btnCancel;
    TextView tvpdpa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdpa);
        tvpdpa = (TextView) findViewById(R.id.tvPdpa);
        btnOK = (Button) findViewById(R.id.btnOK);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        tvpdpa.setMovementMethod(new ScrollingMovementMethod());
//        btnOK.setEnabled(false);
//        CheckBox chkAllow = (CheckBox) findViewById(R.id.chkAllow);
//        chkAllow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                btnOK = (Button) findViewById(R.id.btnOK);
//                btnOK.setEnabled(b);
//            }
//        });
//        btnCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
//        btnOK.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
//                startActivity(intent);
//            }
//        });
    }
}