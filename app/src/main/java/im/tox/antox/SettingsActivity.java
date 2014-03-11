package im.tox.antox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import im.tox.jtoxcore.ToxUserStatus;

/**
 * Settings Activity DHT nodes.
 * Allows the user to specify their own DHT Node, or to pick one from a downloaded list of known
 * working nodes.
 *
 * @author Mark Winter (Astonex)
 */

public class SettingsActivity extends ActionBarActivity{
    /**
     * Spinner for displaying acceptable statuses (online/away/busy) to the users
     */
    private Spinner statusSpinner;
    /**
     * Checkbox that inflates a DHTDialog where the user can enter their own DHT settings
     */
    private CheckBox dhtBox;
    /**
     * String that store's the user's DHT IP address entry
     */
    private String dhtIP;
    /**
     * String that store's the user's DHT Port entry
     */
    private String dhtPort;
    /**
     * String that store's the user's DHT Public Key address entry
     */
    private String dhtKey;
    /**
     * 2D string array to store DHT node details
     */
    String[][] downloadedDHTNodes;
    LinearLayout advancedOptions;
    View advancedView;
    EditText etdhtIP,etdhtPort,etdhtKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


//        statusSpinner = (Spinner) findViewById(R.id.settings_spinner_status);
        dhtBox = (CheckBox) findViewById(R.id.settings_dht_box);
        advancedOptions = (LinearLayout)findViewById(R.id.showAdvanced);
        SharedPreferences pref = getSharedPreferences("settings",
                Context.MODE_PRIVATE);

		/* If the preferences aren't blank, then add them to text fields
         * otherwise it will display the predefined hints in strings.xml
         */

        if (!pref.getString("saved_dht_ip", "").equals("")) {
            dhtIP = pref.getString("saved_dht_ip", "");
        }

        if (!pref.getString("saved_dht_port", "").equals("")) {
            dhtPort = pref.getString("saved_dht_port", "");
        }

        if (!pref.getString("saved_dht_key", "").equals("")) {
            dhtKey = pref.getString("saved_dht_key", "");
        }

        // Set dhtBox as checked if it is set
        dhtBox.setChecked(pref.getBoolean("saved_custom_dht", false));
        if(dhtBox.isChecked())
            openAdvanced();
        }


    /**
     * This method is called when the user updates their settings. It will check all the text fields
     * to see if they contain default values, and if they don't, save them using SharedPreferences
     *
     * @param view
     */
    public void updateSettings(View view) {
        /**
         * String array to store updated details to be passed by intent to ToxService
         */
        String[] updatedSettings = { null, null, null};

        //EditText statusHintText = (EditText) findViewById(R.id.settings_status_hint);

		/* Save settings to file */

        SharedPreferences pref = getSharedPreferences("settings",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        //first check if any field is not equal to the default string, then only save it
        /* Also save DHT details to DhtNode class */
        if(etdhtIP!=null) {
            dhtPort = etdhtPort.getText().toString();
            dhtKey = etdhtKey.getText().toString();
            dhtIP = etdhtIP.getText().toString();
        }

        editor.putBoolean("saved_custom_dht", dhtBox.isChecked());
        if(dhtBox.isChecked() && !dhtIP.equals("") && !dhtKey.equals("") && !dhtPort.equals(""))
        {
            editor.putString("saved_dht_ip", dhtIP);

            editor.putString("saved_dht_key", dhtKey);

            editor.putString("saved_dht_port", dhtPort);

            editor.commit();

            Context context = getApplicationContext();
            CharSequence text = "Settings updated";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            finish();
        }
        //condition when the user enters no or partial details
        else if(dhtBox.isChecked())
        {
            Toast.makeText(getApplicationContext(),"Please enter all the details",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"User-defined settings removed",Toast.LENGTH_SHORT).show();
            editor.putString("saved_dht_ip", "");

            editor.putString("saved_dht_key", "");

            editor.putString("saved_dht_port", "");

            editor.commit();
            finish();
        }


    }
    /**
     * This method is called when the user clicks on the check button for entering their own DHT
     * settings
     *
     * @param view
     */
    public void onDhtBoxClicked(View view) {
        //If the user is checking the box, create a dialog prompting the user for the information
        boolean checked = ((CheckBox) view).isChecked();
        if(checked)
        {
            openAdvanced();
        }
        else
            closeAdvanced();
    }

    void openAdvanced()
    {
        LayoutInflater mInflater;
        mInflater = LayoutInflater.from(getApplicationContext());
        advancedView = mInflater.inflate(R.layout.dialog_settings_dht, null);
        advancedOptions.addView(advancedView);
        etdhtIP=(EditText)advancedView.findViewById(R.id.settings_dht_ip);
        etdhtPort=(EditText)advancedView.findViewById(R.id.settings_dht_port);
        etdhtKey=(EditText)advancedView.findViewById(R.id.settings_dht_key);
        etdhtKey.setText(dhtKey);
        etdhtPort.setText(dhtPort);
        etdhtIP.setText(dhtIP);

    }

    void closeAdvanced()
    {
        advancedOptions.removeView(advancedView);
    }

    @Override
    public void onBackPressed() {
        if(!dhtBox.isChecked())
        {
            SharedPreferences pref = getSharedPreferences("settings",
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            Toast.makeText(getApplicationContext(),"User-defined settings removed",Toast.LENGTH_SHORT).show();
            editor.putString("saved_dht_ip", "");

            editor.putString("saved_dht_key", "");

            editor.putString("saved_dht_port", "");

            editor.putBoolean("saved_custom_dht", false);
            editor.commit();

        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}