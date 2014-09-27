package im.tox.antox.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

import im.tox.QR.IntentIntegrator;
import im.tox.QR.IntentResult;
import im.tox.antox.R;
import im.tox.antox.data.AntoxDB;
import im.tox.antox.fragments.PinDialogFragment;
import im.tox.antox.tox.ToxSingleton;
import im.tox.antox.utils.Constants;
import im.tox.jtoxcore.FriendExistsException;
import im.tox.jtoxcore.ToxException;

public class AddFriendActivity extends ActionBarActivity implements PinDialogFragment.PinDialogListener {

    String _friendID = "";
    String _friendCHECK = "";
    String _originalUsername = "";

    boolean isV2 = false;

    Context context;
    CharSequence text;
    int duration = Toast.LENGTH_SHORT;
    Toast toast;

    EditText friendID;
    EditText friendMessage;
    EditText friendAlias;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Fix for an android 4.1.x bug */
        if(Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            );
        }

        setContentView(R.layout.activity_add_friend);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setIcon(R.drawable.ic_actionbar);
        }

        context = getApplicationContext();
        text = getString(R.string.addfriend_friend_added);

        friendID = (EditText) findViewById(R.id.addfriend_key);
        friendMessage = (EditText) findViewById(R.id.addfriend_message);
        friendAlias = (EditText) findViewById(R.id.addfriend_friendAlias);

        Intent intent = getIntent();
        //If coming from tox uri link
        if (Intent.ACTION_VIEW.equals(intent.getAction())
                && intent != null) {
            EditText friendID = (EditText) findViewById(R.id.addfriend_key);
            Uri uri;
            uri = intent.getData();
            if (uri != null)
                friendID.setText(uri.getHost());
            //TODO: ACCEPT DNS LOOKUPS FROM URI

        } else if (intent.getAction().equals("toxv2")) {
            //else if it came from toxv2 restart

            friendID.setText(intent.getStringExtra("originalUsername"));
            friendAlias.setText(intent.getStringExtra("alias"));
            friendMessage.setText(intent.getStringExtra("message"));

            if(checkAndSend(intent.getStringExtra("key"), intent.getStringExtra("originalUsername")) == 0) {
                toast = Toast.makeText(context, text, duration);
                toast.show();
            } else if (checkAndSend(intent.getStringExtra("key"), intent.getStringExtra("originalUsername")) == -1) {
                toast = Toast.makeText(context, getResources().getString(R.string.invalid_friend_ID), Toast.LENGTH_SHORT);
                toast.show();
                return;
            } else if (checkAndSend(intent.getStringExtra("key"), intent.getStringExtra("originalUsername")) == -2) {
                toast = Toast.makeText(context, getString(R.string.addfriend_friend_exists), Toast.LENGTH_SHORT);
                toast.show();
            }

            Intent update = new Intent(Constants.BROADCAST_ACTION);
            update.putExtra("action", Constants.UPDATE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(update);
            Intent i = new Intent();
            setResult(RESULT_OK, i);

            // Close activity
            finish();
        }
    }

    private boolean isKeyOwn(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String tmp = preferences.getString("tox_id", "");
        if(tmp.toLowerCase().startsWith("tox:"))
            tmp = tmp.substring(4);
        if(tmp.equals(key))
            return true;
        else
            return false;
    }

    private int checkAndSend(String key, String originalUsername) {
        if(!isKeyOwn(key)) {
            if (validateFriendKey(key)) {
                String ID = key;
                String message = friendMessage.getText().toString();
                String alias = friendAlias.getText().toString();

                // Check to see if message was blank, if so set a default
                if(message.equals(""))
                    message = getString(R.string.addfriend_default_message);

                String[] friendData = {ID, message, alias};

                AntoxDB db = new AntoxDB(getApplicationContext());
                if (!db.doesFriendExist(ID)) {
                    try {
                        ToxSingleton toxSingleton = ToxSingleton.getInstance();
                        toxSingleton.jTox.addFriend(friendData[0], friendData[1]);
                    } catch (ToxException e) {
                        e.printStackTrace();
                    } catch (FriendExistsException e) {
                        e.printStackTrace();
                    }

                    Log.d("AddFriendActivity", "Adding friend to database");
                    db.addFriend(ID, "Friend Request Sent", alias, originalUsername);
                } else {
                    return -2;
                }
                db.close();
                return 0;
            } else {
                return -1;
            }
        } else {
            return -3;
        }
    }
    /*
    * method is outside so that the intent can be passed this object
     */
    private void scanIntent() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    public void addFriend(View view) {

        if(friendID.getText().toString().contains("@") || friendID.getText().length() != 76) {
            _originalUsername = friendID.getText().toString();
            // Get the first TXT record
            try {
                //.get() is a possible ui lag on very slow internet connections where dns lookup takes a long time
                new DNSLookup().execute(friendID.getText().toString()).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(isV2) {
            DialogFragment dialog = new PinDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(getResources().getString(R.string.addfriend_friend_pin_title), getResources().getString(R.string.addfriend_friend_pin_text));
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
        }

        String finalFriendKey = friendID.getText().toString();

        if(!_friendID.equals(""))
            finalFriendKey = _friendID;

        if(!isV2) {

            int result = checkAndSend(finalFriendKey, _originalUsername);

            if(result == 0) {
                toast = Toast.makeText(context, text, duration);
                toast.show();
            } else if(result == -1) {
                toast = Toast.makeText(context, getResources().getString(R.string.invalid_friend_ID), Toast.LENGTH_SHORT);
                toast.show();
                return;
            } else if(result == -2) {
                toast = Toast.makeText(context, getResources().getString(R.string.addfriend_friend_exists), Toast.LENGTH_SHORT);
                toast.show();
            } else if(result == -3) {
                toast = Toast.makeText(context, getResources().getString(R.string.addfriend_own_key), Toast.LENGTH_SHORT);
                toast.show();
            }

            Intent update = new Intent(Constants.BROADCAST_ACTION);
            update.putExtra("action", Constants.UPDATE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(update);
            Intent i = new Intent();
            setResult(RESULT_OK, i);

            finish();
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String pin) {
        pin = pin + "==";
        //Base64 to Bytes
        try {
            byte[] decoded = Base64.decode(pin, Base64.DEFAULT);

            //Bytes to Hex
            StringBuilder sb = new StringBuilder();
            for(byte b: decoded)
                sb.append(String.format("%02x", b&0xff));
            String encodedString = sb.toString();

            //Finally set the correct ID to add
            _friendID = _friendID + encodedString + _friendCHECK;

            //Restart activity with info needed
            Intent restart = new Intent(this, AddFriendActivity.class);
            restart.putExtra("key", _friendID);
            restart.putExtra("alias", friendAlias.getText().toString());
            restart.putExtra("message", friendMessage.getText().toString());
            restart.putExtra("originalUsername", _originalUsername);
            restart.setAction("toxv2");
            startActivity(restart);

            finish();

        } catch (IllegalArgumentException e) {
            Context context = getApplicationContext();
            CharSequence text = getString(R.string.addfriend_invalid_pin);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            e.printStackTrace();
        }

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {}

    /*
    * handle intent to read a friend QR code
    * */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            if (scanResult.getContents() != null) {
                EditText addFriendKey = (EditText) findViewById(R.id.addfriend_key);
                String friendKey = (scanResult.getContents().toLowerCase().contains("tox:") ? scanResult.getContents().substring(4) : scanResult.getContents());
                if (validateFriendKey(friendKey)) {
                    addFriendKey.setText(friendKey);
                } else {
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context, getResources().getString(R.string.invalid_friend_ID), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }

    private boolean validateFriendKey(String friendKey) {
        if (friendKey.length() != 76 || friendKey.matches("[[:xdigit:]]")) {
            return false;
        }
        int x = 0;
        try {
            for (int i = 0; i < friendKey.length(); i += 4) {
                x = x ^ Integer.valueOf(friendKey.substring(i, i + 4), 16);
            }
        }
        catch (NumberFormatException e) {
            return false;
        }
        return x == 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_friend, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
            //scanQR button to call the barcode reader app
            case R.id.scanFriend:
                scanIntent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DNSLookup extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... params) {

            // If just a username was passed and not a full domain
            String user, domain, lookup;
            if(!params[0].contains("@")) {
                user = params[0];
                domain = "toxme.se";
                lookup = user + "._tox." + domain;
            } else {
                user = params[0].substring(0, params[0].indexOf("@"));
                domain = params[0].substring(params[0].indexOf("@") + 1);
                lookup = user + "._tox." + domain;
            }

            TXTRecord txt = null;
            try {
                Record[] records = new Lookup(lookup, Type.TXT).run();
                txt = (TXTRecord) records[0];
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(txt != null) {
                String txtString = txt.toString().substring(txt.toString().indexOf('"'));

                if(txtString.contains("tox1")) {
                    String key = txtString.substring(11, 11+76);
                    _friendID = key;

                } else if (txtString.contains("tox2")) {
                    isV2 = true;
                    String key = txtString.substring(12, 12+64);
                    String check = txtString.substring(12+64+7,12+64+7+4);
                    _friendID = key;
                    _friendCHECK = check;
                }
            }

            return null;
        }
    }

}
