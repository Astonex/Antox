package im.tox.antox.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import java.util.Random;

import im.tox.antox.R;
import im.tox.antox.data.AntoxDB;
import im.tox.antox.tox.ToxDoService;
import im.tox.antox.tox.ToxSingleton;
import im.tox.antox.utils.Options;
import im.tox.jtoxcore.ToxException;

/**
 * A {@link android.preference.PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class Settings extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("language"));

        Preference nospamPreference = findPreference("nospam");
        nospamPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ToxSingleton toxSingleton = ToxSingleton.getInstance();
                try {
                    Random random = new Random();
                    int nospam = random.nextInt(1234567890);
                    toxSingleton.jTox.setNospam(nospam);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Settings.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("tox_id", toxSingleton.jTox.getAddress());
                    editor.apply();
                    bindPreferenceSummaryToValue(findPreference("tox_id"));
                } catch (ToxException e) {
                    e.printStackTrace();
                }

                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /* Callback will handle updating the new settings on the tox network */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("enable_udp")) {
            ToxSingleton toxSingleton = ToxSingleton.getInstance();

            Options.udpEnabled = sharedPreferences.getBoolean("enable_udp", false);

            // Stop service
            Intent service = new Intent(this, ToxDoService.class);
            this.stopService(service);
            // Start service
            this.startService(service);
        }

        if (key.equals("wifi_only")) {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            final ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            boolean wifiOnly = sharedPreferences.getBoolean("wifi_only", true);

            // Set all offline as we wont receive callbacks for them by not doing doTox()
            if (wifiOnly && !mWifi.isConnected()) {
                AntoxDB antoxDB = new AntoxDB(this);
                antoxDB.setAllOffline();
                antoxDB.close();
            }
        }

        if (key.equals("language")) {
            Intent intent = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
