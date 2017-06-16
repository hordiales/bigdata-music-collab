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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ar.com.ordia.collaborativesamples.dto.SoundResourceDTO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import ar.com.ordia.collaborativesamples.dto.FreesoundResourceDTO;

/*
APICall HOME
 */
public class APICallActivity extends AppCompatActivity {

    private static final String LOGTAG = "Okhttp";

    private TextView textViewRespuesta;
    private Button buttonLlamarHttp;
    private Button buttonBajarSonido;
    private EditText editTextID;

    private String tmpSoundFilename = null; //filename or file extension (to download)

    private String API = null; // Selected API (webservice)
    private String API_KEY = null; // Freesound API KEY

    private String URL_SOUND_RESOURCE = null;
    private String URL_SOUND_DOWNLOAD_PRE = null;
    private String URL_SOUND_DOWNLOAD_POST = null;

    ProgressDialog mProgressDialog; //download file progress

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

        //download progress file
        mProgressDialog = new ProgressDialog(APICallActivity.this);
        mProgressDialog.setMessage("Bajar sonido"); //FIXME: internacionalizar txt
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        configurarAPI();
    }

    private void configurarAPI() {
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        API = appPreferences.getString("pref_apiType", null);
        Log.d(LOGTAG, "Selected API: "+API );

        if( API.equals("freesound") ) {
            URL_SOUND_RESOURCE = "http://www.freesound.org/apiv2/sounds/";
            URL_SOUND_DOWNLOAD_PRE = URL_SOUND_RESOURCE;
            URL_SOUND_DOWNLOAD_POST = "/download/";
            //FIXME: no funciona la auth en freesound (VER) necesita oauth2?

            API_KEY = appPreferences.getString("pref_key_freesound_api_key", null);
        }
        else if( API.equals("redpanal") ) {
            //TODO: implement
            API_KEY = null;
            URL_SOUND_RESOURCE = null;
            URL_SOUND_DOWNLOAD_PRE = URL_SOUND_RESOURCE;
            URL_SOUND_DOWNLOAD_POST = null;
        }
        else if( API.equals("custom") ) {
            API_KEY = null;
            URL_SOUND_RESOURCE = appPreferences.getString("pref_apiCustomUrl", null)+"/sounds/";
            URL_SOUND_DOWNLOAD_PRE = URL_SOUND_RESOURCE;
            URL_SOUND_DOWNLOAD_POST = "/audio";
        }
        Log.d(LOGTAG, "URL_SOUND_RESOURCE: " +  URL_SOUND_RESOURCE);
        Log.d(LOGTAG, "API_KEY: " + API_KEY);
    }

    private void consultaSonido() {
        String soundId = editTextID.getText().toString();
        configurarAPI();
        //TODO: add try/catch en freesound si esta mal la api estalla
        new consultaPorIdHandler().execute(URL_SOUND_RESOURCE+soundId);
    }

    private void bajarSonido() {
        String soundId = editTextID.getText().toString();
        //TODO: solicitar al usuario que otorgue permisos de escritura en el filesystem externo?
        configurarAPI();

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
                String contenidoRespuesta = response.body().string();

                //TODO: chequear el json que llega, puede ser de error
                //  {"detail":"Authentication credentials were not provided."}
                Log.d(LOGTAG, "JSON: "+contenidoRespuesta);
                return contenidoRespuesta;
            } catch (Exception e) {
                Log.e(LOGTAG, "error", e);
            }
            return null;
        }

        //cuando esta la respuesta se la envia a la interfaz
        @Override
        protected void onPostExecute(String respuesta) {
            super.onPostExecute(respuesta);

            Gson gson = new Gson();

            SoundResourceDTO sound = null;

            //FIXME use an interface + class implementation
            if ( API.equals("freesound")) {
                sound = gson.fromJson(respuesta, FreesoundResourceDTO.class);
                tmpSoundFilename = sound.getName()+"."+((FreesoundResourceDTO)sound).getType();
            }
            else if ( API.equals("custom")) {
                sound = gson.fromJson(respuesta, SoundResourceDTO.class);
                tmpSoundFilename = sound.getFilename();
            }
            else { //not yet implemented (redpanal, etc)
                throw new RuntimeException("Not yet implemented");
            }

            String jsonSound = ""
                + sound.getId() + "\n"
                + sound.getName() + "\n\n"
                + sound.getDescription() + "\n\n"
                + sound.getLicense() + "\n";

            textViewRespuesta.setText(jsonSound);

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
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
            //TODO: internationalizar el texto
        }

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
                //borrarContacto();
                return true;
            //case R.id.action_listar:
                //listarContactos();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void configurarApp() {
        //Intent intentPreferences = new Intent(this, AppCompatPreferenceActivity.class);
        Intent intentPreferences = new Intent(this, APISettingsActivity.class); //TODO: check, method deprecated

        startActivity(intentPreferences);
    }
}
