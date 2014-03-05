package im.tox.antox;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import im.tox.QR.IntentIntegrator;
import im.tox.QR.IntentResult;

/**
 * Activity to allow the user to add a friend. Also as a URI handler to automatically insert public
 * keys from tox:// links. See AndroidManifest.xml for more information on the URI handler.
 *
 * @author Mark Winter (Astonex)
 */

public class AddFriendActivity extends ActionBarActivity {

    EditText friendID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //scanQR button to call the barcode reader app
        Button scanQR = (Button)findViewById(R.id.scanFriendQR);
        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanIntent();
            }
        });

        friendID = (EditText) findViewById(R.id.addfriend_key);
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
        CharSequence text = "Friend Added";
        int duration = Toast.LENGTH_SHORT;
        if(friendID.getText().length() == 0)
        {
            Toast.makeText(context, "Enter Friend Public Key", duration).show();
            return;
        }
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        /* Send intent to ToxService */
        EditText friendID = (EditText) findViewById(R.id.addfriend_key);
        EditText friendMessage = (EditText) findViewById(R.id.addfriend_message);

        String[] friendData = { friendID.getText().toString(), friendMessage.getText().toString()};

        Intent addFriend = new Intent(this, ToxService.class);
        addFriend.setAction(Constants.ADD_FRIEND);
        addFriend.putExtra("friendData", friendData);
        this.startService(addFriend);

        // Close activity
        finish();
    }
    /*
    * handle intent to read a friend QR code
    * */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            if(scanResult.getContents().contains("tox://")) {
                String friendKey = scanResult.getContents().substring(6);
                EditText addFriendKey = (EditText)findViewById(R.id.addfriend_key);
                addFriendKey.setText(friendKey);
            } else {
                EditText addFriendKey = (EditText)findViewById(R.id.addfriend_key);
                addFriendKey.setText(scanResult.getContents());
            }
        }

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
        }
        return super.onOptionsItemSelected(item);
    }

}
