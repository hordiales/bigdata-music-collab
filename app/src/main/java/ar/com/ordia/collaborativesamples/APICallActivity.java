package ar.com.ordia.collaborativesamples;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import ar.com.ordia.collaborativesamples.dto.SoundResourceDTO;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import ar.com.ordia.collaborativesamples.dto.FreesoundResourceDTO;

/*
APICall HOME

En esta activity se recuperan los json con descriptores de los sonidos

 */
public class APICallActivity extends AppCompatActivity {

    private static final String LOGTAG = "Okhttp";

    private TextView textViewRespuesta;
    private Button buttonLlamarHttp;
    private Button buttonBajarSonido;
    private Button buttonAddSound;

    private SeekBar valueBPM;
    private SeekBar valueDuration;
    private SeekBar valueSpectralCentroid;
    private SeekBar valueInharmonicity;


    private EditText editTextID;

    private String tmpSoundFilename = null; //filename or file extension (to download)

    private String API = null; // Selected API (webservice)
    private String API_KEY = null; // Freesound API KEY

    private String URL_SOUND_RESOURCE = null;
    private String URL_SOUND_DOWNLOAD_PRE = null;
    private String URL_SOUND_DOWNLOAD_POST = null;

    ProgressDialog mProgressDialog; //download file progress

    SoundResourceDTO currentSound = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apicall);

        textViewRespuesta = (TextView) findViewById(R.id.textViewRespuesta);
        editTextID = (EditText) findViewById(R.id.editTextID);
        buttonLlamarHttp = (Button) findViewById(R.id.buttonSearch);
        buttonLlamarHttp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consultaSonido();
            }
        });
        buttonBajarSonido = (Button) findViewById(R.id.buttonDownload);
        buttonBajarSonido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOGTAG, "Download call");
                bajarSonido();
            }
        });

        buttonAddSound = (Button) findViewById(R.id.buttonAdd);
        buttonAddSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOGTAG, "Add sound to the project ID: "); //TODO: complete with id number and API source
                agregarSonido();
            }
        });

        valueBPM              = (SeekBar) findViewById(R.id.seekBarTempo);
        valueDuration         = (SeekBar) findViewById(R.id.seekBarDuration);
        valueSpectralCentroid = (SeekBar) findViewById(R.id.seekBarSpectralCentroid);
        valueInharmonicity    = (SeekBar) findViewById(R.id.seekBarInharmonicity);

        //Download progress file
        mProgressDialog = new ProgressDialog(APICallActivity.this);
        mProgressDialog.setMessage( getString(R.string.download_sound) );
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        String locale = getResources().getConfiguration().locale.getDisplayName();

        configurarAPIDownload();

        //Topic suscription (notifications)
        FirebaseMessaging.getInstance().subscribeToTopic("news");
    }

    // Firebase instance variables
    //private DatabaseReference mFirebaseDatabaseReference;
    private static final String SONGS_CHILD = "songs";
    private static final String SERVER_PATH = "server/";
    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private void agregarSonido() {
        if (currentSound==null) {
            Log.d(LOGTAG, "There is no sound selected to add");
            Toast.makeText(this, getString(R.string.not_yet_available), Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = database.getReference(SERVER_PATH);

        DatabaseReference songsRef = ref.child(SONGS_CHILD + "/canción1");

        songsRef.child(String.valueOf(currentSound.getId())).setValue(currentSound);

        textViewRespuesta.setText(getString(R.string.added_to_db));

        String topic = "/topics/news";
        sendNotification(topic);
    }

    private void showSoundLocation() {
        //Search geotag info
        String soundId = editTextID.getText().toString();

        configurarAPIporID();
        if( URL_SOUND_RESOURCE==null ) {
            Toast.makeText(this, getString(R.string.not_yet_available), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            new consultaPorIdHandler().execute(URL_SOUND_RESOURCE+soundId); //WARNING: en el postExecute llama a startMapActivity
        } catch (Exception e) {
            Log.e(LOGTAG, "error", e);
        }
    }

    private void configurarAPISearch() {

        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //PreferenceManager.getDefaultSharedPreferencesName(this);

        API = appPreferences.getString("pref_apiType", "");
        Log.d(LOGTAG, "Selected API: "+API );

        //temporally solution custom api searchs over freesound DB and returns search ID
        if( API.equals("freesound") || API.equals("custom") ) {
            /*
            URL_SOUND_RESOURCE = "http://www.freesound.org/apiv2/";
            URL_SOUND_DOWNLOAD_PRE = URL_SOUND_RESOURCE;
            URL_SOUND_DOWNLOAD_POST = "/";
            //FIXME: no funciona la auth en freesound (VER) necesita oauth2?
            */

            API_KEY = appPreferences.getString("pref_key_freesound_api_key", null);
            if( API_KEY==null || API_KEY.equals("") ){
                API_KEY = getString(R.string.freesound_api_key); //default by config
            }

            URL_SOUND_RESOURCE = appPreferences.getString("pref_apiCustomUrl", null)+"/search";
            //URL_SOUND_RESOURCE = "http://5.0.0.100:5000"+"/search";

            URL_SOUND_DOWNLOAD_PRE = URL_SOUND_RESOURCE;
            URL_SOUND_DOWNLOAD_POST = "";
        }
        else if ( API.equals("redpanal") ) {
            //TODO: implement
            API_KEY = null;
            URL_SOUND_RESOURCE = null;
            URL_SOUND_DOWNLOAD_PRE = URL_SOUND_RESOURCE;
            URL_SOUND_DOWNLOAD_POST = null;
            Log.d(LOGTAG, "RedPanal SoundResource: " + getString(R.string.not_yet_available));
        }
        Log.d(LOGTAG, "URL_SOUND_RESOURCE: " +  URL_SOUND_RESOURCE);
        Log.d(LOGTAG, "API_KEY: " + API_KEY);
    }

    private void configurarAPIDownload() {
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        API = appPreferences.getString("pref_apiType", "");
        Log.d(LOGTAG, "Selected API: "+API );

        if( API.equals("freesound") ) {
            URL_SOUND_RESOURCE = "http://www.freesound.org/apiv2/sounds/";
            URL_SOUND_DOWNLOAD_PRE = URL_SOUND_RESOURCE;
            URL_SOUND_DOWNLOAD_POST = "/download/";
            //FIXME: no funciona la auth en freesound (VER) necesita oauth2?

            API_KEY = appPreferences.getString("pref_key_freesound_api_key", null);
            if( API_KEY==null || API_KEY.equals("") ) {
                API_KEY = getString(R.string.freesound_api_key); //default by config
            }
        }
        else if( API.equals("redpanal") ) {
            //TODO: implement
            API_KEY = null;
            URL_SOUND_RESOURCE = null;
            URL_SOUND_DOWNLOAD_PRE = URL_SOUND_RESOURCE;
            URL_SOUND_DOWNLOAD_POST = null;
            Log.d(LOGTAG, "RedPanal SoundResource: " + getString(R.string.not_yet_available));
        }
        else if( API.equals("custom") ) {
            //API_KEY = null; //lets use same freesound api-key
            URL_SOUND_RESOURCE = appPreferences.getString("pref_apiCustomUrl", null)+"/sounds/";
            //URL_SOUND_RESOURCE = "http://5.0.0.100:5000"+"/sounds/"; //FIXME: temporal
            URL_SOUND_DOWNLOAD_PRE = URL_SOUND_RESOURCE;
            URL_SOUND_DOWNLOAD_POST = "/audio";
        }
        Log.d(LOGTAG, "URL_SOUND_RESOURCE: " +  URL_SOUND_RESOURCE);
        Log.d(LOGTAG, "API_KEY: " + API_KEY);
    }

    private void configurarAPIporID() {
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        API = appPreferences.getString("pref_apiType", "");
        Log.d(LOGTAG, "Selected API: "+API );

        if( API.equals("freesound") ) {
            URL_SOUND_RESOURCE = "http://www.freesound.org/apiv2/sounds/";
            URL_SOUND_DOWNLOAD_PRE = URL_SOUND_RESOURCE;
            URL_SOUND_DOWNLOAD_POST = "";
            //FIXME: no funciona la auth en freesound (VER) necesita oauth2?

            API_KEY = appPreferences.getString("pref_key_freesound_api_key", null);
            if( API_KEY==null || API_KEY.equals("") ) {
                API_KEY = getString(R.string.freesound_api_key); //default by config
            }
        }
        else if( API.equals("redpanal") ) {
            //TODO: implement
            API_KEY = null;
            URL_SOUND_RESOURCE = null;
            URL_SOUND_DOWNLOAD_PRE = URL_SOUND_RESOURCE;
            URL_SOUND_DOWNLOAD_POST = null;
            Log.d(LOGTAG, "RedPanal SoundResource: " + getString(R.string.not_yet_available));
        }
        else if( API.equals("custom") ) {
            //API_KEY = null; //lets use same freesound api-key
            URL_SOUND_RESOURCE = appPreferences.getString("pref_apiCustomUrl", null)+"/sounds/";
            //URL_SOUND_RESOURCE = "http://5.0.0.100:5000"+"/sounds/"; //FIXME: TEMPORAL
            URL_SOUND_DOWNLOAD_PRE = URL_SOUND_RESOURCE;
            URL_SOUND_DOWNLOAD_POST = "";
        }
        Log.d(LOGTAG, "URL_SOUND_RESOURCE: " +  URL_SOUND_RESOURCE);
        Log.d(LOGTAG, "API_KEY: " + API_KEY);
    }

    private void startMapActivity() {
        //Start map activity
        Intent intentMaps = new Intent(this, MapsActivity.class);

        if (currentSound==null) {
            Log.d(LOGTAG, "There is no sound selected to get geotag position");
            Toast.makeText(this, getString(R.string.not_yet_available), Toast.LENGTH_SHORT).show();
            return;
        }
        String geodesc = currentSound.getGeotag();
        if (geodesc==null) {
            Log.d(LOGTAG, "There is no sound selected to get geotag position");
            Toast.makeText(this, getString(R.string.not_yet_available), Toast.LENGTH_SHORT).show();
            return;
        }
        String[] geotag = geodesc.split(",");
        double longitude = Double.parseDouble( geotag[0] );
        double latitude = Double.parseDouble( geotag[1] );

        String title = "Web sound name or title";
        intentMaps.putExtra("longitude", longitude);
        intentMaps.putExtra("latitude", latitude);
        intentMaps.putExtra("title", title);
        startActivity(intentMaps);
    }

    private void consultaSonido() {
        float bpm = valueBPM.getProgress();
        float duration = valueDuration.getProgress();
        float spectral_centroid = valueSpectralCentroid.getProgress();
        float inharmonicity = valueInharmonicity.getProgress();

        Log.d(LOGTAG, "BPM: " + bpm + ", Duration: " + duration);
        Log.d(LOGTAG, "SpectralCentroid: " + spectral_centroid + ", Inharmonicity: " + inharmonicity);

        configurarAPISearch();

        if (URL_SOUND_RESOURCE == null) {
            Toast.makeText(this, getString(R.string.not_yet_available), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String[] search_params = new String[5];
            search_params[0] = URL_SOUND_RESOURCE;
            search_params[1] = Float.toString(bpm);
            search_params[2] = Float.toString(duration);
            search_params[3] = Float.toString(spectral_centroid);
            search_params[4] = Float.toString(inharmonicity);

            new consultaPorJsonHandler().execute(search_params);
        } catch (Exception e) {
            Log.e(LOGTAG, "error", e);
        }
    }

    private void bajarSonido() {
        String soundId = editTextID.getText().toString();

        //WARNING TODO: solicitar al usuario que otorgue permisos de escritura en el filesystem externo
        // ( Como esta ahora hay que hacerlo manualmente )

        configurarAPIDownload();

        if( URL_SOUND_RESOURCE==null ) {
            Toast.makeText(this, getString(R.string.not_yet_available), Toast.LENGTH_SHORT).show();
            return;
        }

        if (tmpSoundFilename==null) {
            tmpSoundFilename = soundId + ".wav";
        }

        final DownloadTask downloadTask = new DownloadTask(APICallActivity.this, tmpSoundFilename);
        downloadTask.execute(URL_SOUND_DOWNLOAD_PRE+soundId+URL_SOUND_DOWNLOAD_POST);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }


    /*
        This is because get() method of AsyncTask waits for the computation to finish in
        doInBackground method and then retrieves its result. See this link.
        This will make your main UIThread in wait mode until doInBackground finish its execution or
        here is some exception occur(i.e. CancellationException,ExecutionException and InterruptedException).

        You should use onPostExecute(Result) override method of AsyncTask.
    */
    private class consultaPorIdHandler extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient();

        //se va a hacer en un thread aparte para que no bloquee la interfaz gráfica
        @Override
        protected String doInBackground(String... params) {
            Request request = new Request.Builder()
                    .header("Authorization", "Token " + API_KEY) //FIXME no hace falta en las api's que no sean freesound
                    .url(params[0])
                    .build();

            try {
                Response response = client.newCall(request).execute();

                if (response.code() == HttpURLConnection.HTTP_OK) { //code 200
                    String contenidoRespuesta = response.body().string();

                    //TODO: chequear el json que llega, puede ser de error
                    //  {"detail":"Authentication credentials were not provided."}
                    Log.d(LOGTAG, "JSON: " + contenidoRespuesta);
                    return contenidoRespuesta;
                } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) { //code 400
                    return "NULL";
                } else {
                    throw new Exception("Error with api connection. Code: " + response.code());
                }
            } catch (java.net.ConnectException e) {
                Log.e(LOGTAG, "error de conexión", e);
            } catch (Exception e) {
                Log.e(LOGTAG, "error", e);
            }
            return null;
        }

        //cuando esta la respuesta se la envia a la interfaz
        @Override
        protected void onPostExecute(String respuesta) {
            super.onPostExecute(respuesta);

            if (respuesta==null) {
                return;
            }

            if( respuesta.equals("NULL") ) {
                respuesta = getString(R.string.not_found);
                textViewRespuesta.setText( respuesta );
                return;
            }

            Gson gson = new Gson();

            SoundResourceDTO sound = null;

            //FIXME use an interface + class implementation
            if ( API.equals("freesound")) {
                sound = gson.fromJson(respuesta, FreesoundResourceDTO.class);
                tmpSoundFilename = sound.getName();
            }
            else if ( API.equals("custom")) {
                sound = gson.fromJson(respuesta, SoundResourceDTO.class);
                tmpSoundFilename = sound.getFilename();
            }
            else { //not yet implemented (redpanal, etc)
                throw new RuntimeException("Not yet implemented");
            }

            String jsonSound = ""
                    //+ sound.getId() + "\n"
                    + getString(R.string.desc_name)+": " + sound.getName() + "\n\n"
                    //+ getString(R.string.filename)+ sound.getId() + ".wav\n\n"
                    + getString(R.string.description)+": "+sound.getDescription() + "\n\n"
                    + getString(R.string.license)+": "+sound.getLicense() + "\n";

            textViewRespuesta.setText(jsonSound);
            editTextID.setText( String.valueOf(sound.getId()) );

            currentSound = sound;

            startMapActivity();
        }

    }

    /*
        This is because get() method of AsyncTask waits for the computation to finish in
        doInBackground method and then retrieves its result. See this link.
        This will make your main UIThread in wait mode until doInBackground finish its execution or
        here is some exception occur(i.e. CancellationException,ExecutionException and InterruptedException).

        You should use onPostExecute(Result) override method of AsyncTask.
    */
    private class consultaPorJsonHandler extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient();

        //se va a hacer en un thread aparte para que no bloquee la interfaz gráfica
        @Override
        protected String doInBackground(String... params) {

            JsonObject json_descriptors = new JsonObject();
            json_descriptors.addProperty("BPM", params[1]);
            json_descriptors.addProperty("duration", params[2]);
            json_descriptors.addProperty("spectral_centroid", params[3]);
            json_descriptors.addProperty("inharmonicity", params[4]);

            Request request = new Request.Builder()
                    .header("Authorization", "Token " + API_KEY) //FIXME no hace falta en las api's que no sean freesound
                    .url(params[0])
                    .post(
                            RequestBody
                                    .create(MediaType
                                            .parse("application/json"),
                                            json_descriptors.toString()
                                    )
                    )
                    .build();

            try {
                Response response = client.newCall(request).execute();

                if (response.code() == HttpURLConnection.HTTP_OK) { //code 200
                    String contenidoRespuesta = response.body().string();

                    //TODO: chequear el json que llega, puede ser de error
                    //  {"detail":"Authentication credentials were not provided."}
                    Log.d(LOGTAG, "JSON: " + contenidoRespuesta);
                    return contenidoRespuesta;
                } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) { //code 400
                    return "NULL";
                } else {
                    throw new Exception("Error with api connection. Code: " + response.code());
                }
            } catch (java.net.ConnectException e) {
                Log.e(LOGTAG, "error de conexión", e);
            } catch (Exception e) {
                Log.e(LOGTAG, "error", e);
            }
            return null;
        }

        //cuando esta la respuesta se la envia a la interfaz
        @Override
        protected void onPostExecute(String respuesta) {
            super.onPostExecute(respuesta);

            if (respuesta==null) {
                return;
            }

            if( respuesta.equals("NULL") ) {
                respuesta = getString(R.string.not_found);
                textViewRespuesta.setText( respuesta );
                return;
            }

            Gson gson = new Gson();

            SoundResourceDTO sound = null;

            //FIXME use an interface + class implementation
            if ( API.equals("freesound")) {
                sound = gson.fromJson(respuesta, FreesoundResourceDTO.class);
                tmpSoundFilename = sound.getName();
            }
            else if ( API.equals("custom")) {
                sound = gson.fromJson(respuesta, SoundResourceDTO.class);
                tmpSoundFilename = sound.getFilename();
            }
            else { //not yet implemented (redpanal, etc)
                throw new RuntimeException("Not yet implemented");
            }

            String jsonSound = ""
                    //+ sound.getId() + "\n"
                    + getString(R.string.desc_name)+": " + sound.getName() + "\n\n"
                    //+ getString(R.string.filename)+ sound.getId() + ".wav\n\n"
                    + getString(R.string.description)+": "+sound.getDescription() + "\n\n"
                    + getString(R.string.license)+": "+sound.getLicense() + "\n";

            textViewRespuesta.setText(jsonSound);
            editTextID.setText( String.valueOf(sound.getId()) );
            currentSound = sound;
        }

    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private String filename;

        public DownloadTask(Context context, String filename) {
            this.context = context;
            this.filename = filename;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);

                connection = (HttpURLConnection) url.openConnection();
                //FIXME (freesound  auth no works) y para el resto talv ez no es necesario estar mandando esto
                connection.setRequestProperty("Authorization", "Token "+API_KEY);
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();

                //FIXME: usar espacio interno de la app? o /tmp????
                // esto requiere los permisos de escritura en el manifest (external drive)
                //    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
                //https://developer.android.com/training/basics/data-storage/files.html
                //TODO: chequear que exista ese path, etc
                output = new FileOutputStream( Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MUSIC)+"/"+filename );
                Log.d(LOGTAG, "Destination path: "+Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MUSIC)+"/"+filename);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) { //TODO: revisar
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context, getString(R.string.download_error)+": "+result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,getString(R.string.file_downloaded_to)+" "+Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MUSIC)+"/"+filename, Toast.LENGTH_SHORT).show();
        }
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private void sendNotification(final String reg_token) {
        AsyncTask<Void, Void, Void> execute = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JsonObject json = new JsonObject();
                    JsonObject dataJson = new JsonObject();

                    dataJson.addProperty("title", getString(R.string.new_sample));
                    dataJson.addProperty("body", getString(R.string.new_sample_body));

                    json.add("notification", dataJson);
                    json.addProperty("to", reg_token);

                    Log.d(LOGTAG, "Sending notification NEW SAMPLE ADDED");
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization", "key=" + getString(R.string.legacy_server_key))
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                    Log.d(LOGTAG, "Response: "+finalResponse);
                } catch (Exception e) {
                    Log.e(LOGTAG,e+"");
                }
                return null;
            }
        }.execute();
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
            case R.id.action_listar:
                startActivity(new Intent(this, ShowSamplesOfTheSongActivity.class));
                return true;
            case R.id.action_maps:
                showSoundLocation();
                return true;
            case R.id.sign_out_menu:
                Intent intentMain = new Intent(this, MainActivity.class);
                intentMain.putExtra("action", "logout");
                startActivity(intentMain);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void configurarApp() {
        //Intent intentPreferences = new Intent(this, AppCompatPreferenceActivity.class);
        Intent intentPreferences = new Intent(this, APISettingsActivity.class); //TODO: check, method deprecated

        startActivity(intentPreferences);
    }
}
