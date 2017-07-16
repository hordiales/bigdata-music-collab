package ar.com.ordia.collaborativesamples;

import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/*
public class APISettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apisettings);
    }
}
*/

public class APISettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        addPreferencesFromResource(R.xml.preferences);  //FIXME: check, method deprecated

        final ListPreference itemList = (ListPreference) findPreference("pref_apiType");
        final EditTextPreference itemAPIEdit = (EditTextPreference) findPreference("pref_key_freesound_api_key");
        final EditTextPreference itemCustomUrlEdit = (EditTextPreference) findPreference("pref_api_custom_url");
        if (itemList==null || itemAPIEdit==null) {
            return;
        }
        itemList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
               public boolean onPreferenceChange(Preference preference, Object newValue) {
                   final String val = newValue.toString();
                   int index = itemList.findIndexOfValue(val);
                   if (index == 0) //Freesound API
                       itemAPIEdit.setEnabled(true);
                   else
                       itemAPIEdit.setEnabled(false);

                   if (index == 2) //custom URL service
                       itemCustomUrlEdit.setEnabled(true);
                   else
                       itemCustomUrlEdit.setEnabled(false);
                   return true;
               }
        });
    }


}
