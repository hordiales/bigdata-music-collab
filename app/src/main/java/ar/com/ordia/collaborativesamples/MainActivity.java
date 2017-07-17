package ar.com.ordia.collaborativesamples;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.iid.FirebaseInstanceId;

/*
  En esta activity ...
*/
public class MainActivity extends AppCompatActivity {

    private Button buttonrPushToken;

    private static final String LOGTAG = "android-fcm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //WARNING: el tag puede cambiar
        buttonrPushToken = (Button) findViewById(R.id.buttonRecuperarPushToken);
        buttonrPushToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Se obtiene el token actualizado
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();

                Log.d(LOGTAG, "Token actualizado: " + refreshedToken);
            }
        });
    }

}
