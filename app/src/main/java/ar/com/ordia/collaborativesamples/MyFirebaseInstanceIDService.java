package ar.com.ordia.collaborativesamples;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/*
Actualización de token

"El token de registro se asigna a nuestra aplicación en el momento de su primera conexión con los
 servicios de mensajería, y en condiciones normales se mantiene invariable en el tiempo.
 Sin embargo, en determinadas circunstancias este dato puede cambiar durante la vida de
 la aplicación (por ejemplo cuando se reinstala, cuando el usuario borra los datos de la aplicación,
 por refrescos de seguridad…), por lo que debemos estar preparados para detectar estos posibles cambios"
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String LOGTAG = "MyFirebaseInstanceIDService";

    public MyFirebaseInstanceIDService() {
    }

    @Override
    public void onTokenRefresh() {
        //Se obtiene el token actualizado
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.d(LOGTAG, "Token actualizado: " + refreshedToken);
    }
}
