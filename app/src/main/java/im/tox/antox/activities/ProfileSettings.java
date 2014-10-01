package im.tox.antox.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import im.tox.QR.Contents;
import im.tox.QR.QRCodeEncode;
import im.tox.antox.R;
import im.tox.antox.data.UserDB;
import im.tox.antox.tox.ToxDoService;
import im.tox.antox.tox.ToxSingleton;
import im.tox.antox.utils.UserStatus;
import im.tox.jtoxcore.ToxException;
import im.tox.jtoxcore.ToxUserStatus;

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
public class ProfileSettings extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener
            = new Preference.OnPreferenceChangeListener() {
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

        addPreferencesFromResource(R.xml.pref_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("nickname"));
        bindPreferenceSummaryToValue(findPreference("status"));
        bindPreferenceSummaryToValue(findPreference("status_message"));
        bindPreferenceSummaryToValue(findPreference("tox_id"));
        bindPreferenceSummaryToValue(findPreference("active_account"));

        /* Override the Tox ID click functionality to display a dialog with the qr image
         * and copy to clipboard button
         */
        Preference toxIDPreference = findPreference("tox_id");
        toxIDPreference.setOnPreferenceClickListener(new EditTextPreference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                createDialog();
                return true;
            }
        });

        Preference logoutPreference = findPreference("logout");
        logoutPreference.setOnPreferenceClickListener(new EditTextPreference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ProfileSettings.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("loggedin", false);
                editor.apply();

                // Stop the Tox Service
                Intent startTox = new Intent(ProfileSettings.this.getApplicationContext(), ToxDoService.class);
                ProfileSettings.this.getApplicationContext().stopService(startTox);

                // Launch login activity
                Intent login = new Intent(ProfileSettings.this.getApplicationContext(), LoginActivity.class);
                ProfileSettings.this.startActivity(login);

                // Finish this activity
                ProfileSettings.this.finish();

                return true;
            }
        });
    }

    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSettings.this);
        LayoutInflater inflater = ProfileSettings.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_tox_id, null);
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.button_ok), null);
        builder.setNeutralButton(getString(R.string.dialog_tox_id), new Dialog.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int ID) {
                /* Copy ID to clipboard */
                SharedPreferences sharedPreferences
                        = PreferenceManager.getDefaultSharedPreferences(ProfileSettings.this);
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ProfileSettings.this
                        .getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(sharedPreferences.getString("tox_id", ""));
            }
        });

        /* Generate or load QR image of Tox ID */
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Antox/");
        if (!file.exists()) {
            file.mkdirs();
        }

        File noMedia = new File(Environment.getExternalStorageDirectory().getPath() + "/Antox/", ".nomedia");
        if (!noMedia.exists()) {
            try {
                noMedia.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        file = new File(Environment.getExternalStorageDirectory().getPath()
                + "/Antox/userkey_qr.png");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(
                ProfileSettings.this.getApplicationContext());
        generateQR(pref.getString("tox_id", ""));
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());

        ImageButton qrCode = (ImageButton) view.findViewById(R.id.qr_image);
        qrCode.setImageBitmap(bmp);
        qrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM,
                        Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()
                                + "/Antox/userkey_qr.png")));
                shareIntent.setType("image/jpeg");
                view.getContext().startActivity(Intent.createChooser(shareIntent,
                        getResources().getString(R.string.share_with)));
            }
        });

        builder.create().show();
    }

    private void generateQR(String userKey) {
        String qrData = "tox:" + userKey;
        int qrCodeSize = 400;

        QRCodeEncode qrCodeEncoder = new QRCodeEncode(qrData, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeSize);

        FileOutputStream out;
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            out = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()
                    + "/Antox/userkey_qr.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        UserDB db = new UserDB(this);

        if (key.equals("nickname")) {

            ToxSingleton toxSingleton = ToxSingleton.getInstance();
            try {
                toxSingleton.jTox.setName(sharedPreferences.getString(key, ""));
            } catch (ToxException e) {
                e.printStackTrace();
            }

            // Update user DB
            db.updateUserDetail(sharedPreferences.getString("active_account", ""), "nickname",
                    sharedPreferences.getString(key, ""));
        }

        if (key.equals("status")) {

            String newStatusString = sharedPreferences.getString(key, "");
            ToxUserStatus newStatus = UserStatus.getToxUserStatusFromString(newStatusString);

            ToxSingleton toxSingleton = ToxSingleton.getInstance();
            try {
                toxSingleton.jTox.setUserStatus(newStatus);
            } catch (ToxException e) {
                e.printStackTrace();
            }

            // Update user DB
            db.updateUserDetail(sharedPreferences.getString("active_account", ""), "status",
                    sharedPreferences.getString(key, ""));
        }

        if (key.equals("status_message")) {

            ToxSingleton toxSingleton = ToxSingleton.getInstance();
            try {
                toxSingleton.jTox.setStatusMessage(sharedPreferences.getString(key, ""));
            } catch (ToxException e) {
                e.printStackTrace();
            }

            // Update user DB
            db.updateUserDetail(sharedPreferences.getString("active_account", ""),
                    "status_message", sharedPreferences.getString(key, ""));
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
