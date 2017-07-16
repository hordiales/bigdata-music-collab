package ar.com.ordia.collaborativesamples;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.firebase.iid.FirebaseInstanceId;

/*
  En esta activity ...
*/
public class MainActivity extends AppCompatActivity {

    private Button buttonRecuperarPushToken;

    private static final String LOGTAG = "android-fcm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //buttonRecuperarPushToken =
    }

/* TODO: review this commented code
    @Override
    public void onTokenRefresh() {
        //Se obtiene el token actualizado
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.d(LOGTAG, "Token actualizado: " + refreshedToken);
    }
*/

    //el tag puede cambiar

    //correr servicio andriod que va a correr en background, para hacer algo con ese mensaje
    //hacer un servicio es mucho más complicado, pero con esto es más simple (tiene que ser no bloqueante,
    //correr en thread separado, no pullear tan seguido porquee te quedas sin bateria)

}
