package ar.com.ordia.collaborativesamples;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.ordia.collaborativesamples.dto.SoundResourceDTO;

public class ShowSamplesOfTheSongActivity extends AppCompatActivity {

    private ListView listViewSamples;
    private ArrayAdapter<SoundResourceDTO> arrayAdapter;

    private static final String LOGTAG = "listView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_samples_of_the_song);

        listViewSamples = (ListView) findViewById(R.id.listViewSamples);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference samplesRef = database.getReference( getString(R.string.server_db_path) );

        final List<SoundResourceDTO> samplesList;
        samplesList = new ArrayList<SoundResourceDTO>();

        final ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, samplesList);
        listViewSamples.setAdapter(arrayAdapter);
        //arrayAdapter.notifyDataSetChanged();

        samplesRef.child("canción1")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        samplesList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            SoundResourceDTO sample = snapshot.getValue(SoundResourceDTO.class);
                            Log.d(LOGTAG, String.valueOf(sample.getId())+": "+sample.getName() );
                            samplesList.add(sample);
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        /*
        arrayAdapter = new ArrayAdapter<SoundResourceDTO>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                samples);

        listViewSamples.setAdapter(arrayAdapter);

        listViewSamples.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                verSample(position);

            }
        });*/
    }

    private void verSample(int position) {
        SoundResourceDTO sample = arrayAdapter.getItem(position);
        //Toast.makeText(this, contacto.toString(), Toast.LENGTH_SHORT).show();

        //TODO: implementar! -> y cuando muestra que muestre todos los descriptores del sample!
        /*
        Intent intentIrAFormulario = new Intent(this, FormContactoActivity.class);
        intentIrAFormulario.putExtra( "idContacto", contacto.getId() );
        startActivity(intentIrAFormulario);
        */
    }
}
