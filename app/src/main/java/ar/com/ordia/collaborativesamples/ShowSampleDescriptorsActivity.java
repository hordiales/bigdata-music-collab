package ar.com.ordia.collaborativesamples;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import ar.com.ordia.collaborativesamples.dto.SoundResourceDTO;

public class ShowSampleDescriptorsActivity extends AppCompatActivity {

    private TextView textViewId;
    private TextView textViewBPM;
    private TextView textViewDuration;

    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView textView6;

    private int idActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sample_descriptors);

        textViewId = (TextView) findViewById(R.id.textViewId);
        //TODO: refactor names!
        textView2 = (TextView) findViewById(R.id.textViewBPM);
        textView3 = (TextView) findViewById(R.id.textViewDuration);
        textView4 = (TextView) findViewById(R.id.textViewSpectralCentroid);
        textView5 = (TextView) findViewById(R.id.textViewInharmonicity);
        textView6 = (TextView) findViewById(R.id.textView6);

        Intent intentDatosActuales = this.getIntent();
        if( intentDatosActuales!=null ) {
            idActual = intentDatosActuales.getIntExtra("idSample", 0);

            ArrayList<String> descList;
            descList = intentDatosActuales.getStringArrayListExtra("descriptors");


            if( idActual!=0 && descList!=null ) {
                textViewId.setText( "Id: "+String.valueOf(idActual) );
                textView2.setText( "Filename: " + descList.get(0) );

                textView3.setText( "Username: " + descList.get(1) );
                textView4.setText( "Duration: " + descList.get(2));
                textView5.setText( "License: " + descList.get(3));
                //textView6.setText( "Geotag: " + descList.get(4));
            }
        }
    }
}
