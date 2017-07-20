package ar.com.ordia.collaborativesamples;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, samplesList);
        listViewSamples.setAdapter(arrayAdapter);

        listViewSamples.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                verSample(position);
            }
        });

        String currentSong = "canción1";
        samplesRef.child(currentSong)
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

    }

    private void verSample(int position) {
        Log.d(LOGTAG, "ListView item position: "+position);
        SoundResourceDTO sample = arrayAdapter.getItem(position);

        try {
            //Toast.makeText(this, sample.toString(), Toast.LENGTH_SHORT).show();

            Intent intentIrAFormulario = new Intent(this, ShowSampleDescriptorsActivity.class);
            intentIrAFormulario.putExtra("idSample", sample.getId());

            ArrayList<String> descList = new ArrayList<String>();
            descList.add( sample.getName() );
            descList.add( sample.getUsername() );
            descList.add( sample.getDuration() );
            descList.add( sample.getLicense() );
            //descList.add( sample.getGeotag() );

            intentIrAFormulario.putStringArrayListExtra("descriptors", descList);

            startActivity(intentIrAFormulario);
        }
        catch(NullPointerException e) {
            Log.e(LOGTAG, "Nullpointer exception trying to get sample item");
        }
    }
}
