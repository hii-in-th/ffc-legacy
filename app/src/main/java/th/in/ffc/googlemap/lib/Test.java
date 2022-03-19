package th.in.ffc.googlemap.lib;


import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

public class Test extends FragmentActivity {
    private TextView txtradius;
    private TextView txtsetRadius;
    private GoogleMapLib test;
    private int radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        //	txtradius = (TextView)findViewById(R.id.radius);
        //	txtsetRadius = (TextView)findViewById(R.id.setradius);
        //	test = new GoogleMapLib(this, R.id.map);
    }

    public void onClick(View v) {
/*		switch (v.getId()) {
        case R.id.clear:
			test.clear();
			break;
		case R.id.polygon:
			test.drawPolygon();
			break;
		case R.id.circle:
			test.drawCircle();
			break;
		case R.id.setradius:
			break;
		}*/

    }
}
