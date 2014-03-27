package im.tox.antox.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import im.tox.QR.IntentIntegrator;
import im.tox.QR.IntentResult;
import im.tox.antox.data.AntoxDB;
import im.tox.antox.utils.Constants;
import im.tox.antox.R;
import im.tox.antox.tox.ToxService;

/**
 * Activity to allow the user to add a friend. Also as a URI handler to automatically insert public
 * keys from tox:// links. See AndroidManifest.xml for more information on the URI handler.
 *
 * @author Mark Winter (Astonex)
 */

public class AddFriendActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        EditText friendID = (EditText) findViewById(R.id.addfriend_key);
        Intent intentURI = getIntent();
        Uri uri;
        if (Intent.ACTION_VIEW.equals(intentURI.getAction())
                && intentURI != null) {
            uri = intentURI.getData();
            if (uri != null)
                friendID.setText(uri.getHost());
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
        Context context = getApplicationContext();
        CharSequence text = getString(R.string.addfriend_friend_added);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);

        /* Send intent to ToxService */
        EditText friendID = (EditText) findViewById(R.id.addfriend_key);
        EditText friendMessage = (EditText) findViewById(R.id.addfriend_message);
        EditText friendAlias = (EditText) findViewById(R.id.addfriend_friendAlias);

        /*validates key*/
        if (validateFriendKey(friendID.getText().toString())) {
            String ID = friendID.getText().toString();
            String message = friendMessage.getText().toString();
            String alias = friendAlias.getText().toString();

            String[] friendData = {ID, message, alias};

            AntoxDB db = new AntoxDB(getApplicationContext());
            if (!db.doesFriendExist(friendID.getText().toString())) {
                Intent addFriend = new Intent(this, ToxService.class);
                addFriend.setAction(Constants.ADD_FRIEND);
                addFriend.putExtra("friendData", friendData);
                this.startService(addFriend);

                if(!alias.equals(""))
                    ID = alias;

                db.addFriend(ID, "Friend Request Sent", alias);
                SharedPreferences pref = getSharedPreferences("orderlist",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                String serialized = pref.getString("PREF_KEY_STRINGS", null);//if the list is null, add the same order as in DB
                List<String> list = new LinkedList(Arrays.asList(TextUtils.split(serialized, ",")));
                list.add(ID);
                editor.remove("PREF_KEY_STRINGS");
                editor.commit();
                editor.putString("PREF_KEY_STRINGS", TextUtils.join(",", list));
                editor.commit();
            } else {
                toast = Toast.makeText(context, getString(R.string.addfriend_friend_exists), Toast.LENGTH_SHORT);
            }
            db.close();

            toast.show();

        } else {
            toast = Toast.makeText(context, getResources().getString(R.string.invalid_friend_ID), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Intent update = new Intent(Constants.BROADCAST_ACTION);
        update.putExtra("action", Constants.UPDATE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(update);
        Intent i = new Intent();
        setResult(RESULT_OK, i);

        // Close activity
        finish();
    }

    /*
    * handle intent to read a friend QR code
    * */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            if (scanResult.getContents() != null) {
                EditText addFriendKey = (EditText) findViewById(R.id.addfriend_key);
                String friendKey = (scanResult.getContents().contains("tox://") ? scanResult.getContents().substring(6) : scanResult.getContents());
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

}
