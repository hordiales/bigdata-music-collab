package ar.com.ordia.collaborativesamples;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

/*
  En esta activity ...
*/
public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    private static final String LOGTAG = "main-activity";

    private Button buttonrPushToken;
    private Button buttonrStart;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private GoogleApiClient mGoogleApiClient;

    private String mUsername;
    private String mPhotoUrl;
    public static final String ANONYMOUS = "anonymous";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonrStart = (Button) findViewById(R.id.buttonStart);
        buttonrStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView();
                //startActivity(new Intent(this, APICallActivity.class));
                Log.d(LOGTAG, "Web composition start");
            }
        });

        ////////////////////////////////
        // Firebase get token
        ////////////////////////////////
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

        ////////////////////////////////
        // Firebase auth
        ////////////////////////////////
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_form_app, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_config:
                configurarApp();
                return true;
            case R.id.action_search:
                searchView();
                return true;
            //case R.id.action_listar:
            //listarContactos();
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void searchView() {
        Intent intentPreferences = new Intent(this, APICallActivity.class);
        startActivity(intentPreferences);
    }
    public void configurarApp() {
        //Intent intentPreferences = new Intent(this, AppCompatPreferenceActivity.class);
        Intent intentPreferences = new Intent(this, APISettingsActivity.class); //TODO: check, method deprecated

        startActivity(intentPreferences);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOGTAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
