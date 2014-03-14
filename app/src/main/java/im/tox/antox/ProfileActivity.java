package im.tox.antox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import im.tox.QR.Contents;
import im.tox.QR.QRCodeEncode;
import im.tox.jtoxcore.ToxUserStatus;

/**
 * Profile Activity where the user can change their username, status, and note.

 * @author Mark Winter (Astonex) & David Lohle (Proplex)
 */

public class ProfileActivity extends ActionBarActivity {
    /**
     * Spinner for displaying acceptable statuses (online/away/busy) to the users
     */
    private Spinner statusSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        statusSpinner = (Spinner) findViewById(R.id.settings_spinner_status);

        /* Add acceptable statuses to the drop down menu */
        String[] statusItems = new String[]{ getResources().getString(R.string.status_online),
                getResources().getString(R.string.status_away),
                getResources().getString(R.string.status_busy)
        };

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item, statusItems);
        statusSpinner.setAdapter(statusAdapter);

		/* Get saved preferences */
        SharedPreferences pref = getSharedPreferences("settings",
                Context.MODE_PRIVATE);

        /* Sets the user key to the saved user key */
        TextView userKey = (TextView) findViewById(R.id.settings_user_key);
        userKey.setText(pref.getString("user_key", ""));

        /* Looks for the userkey qr.png if it doesn't exist then it creates it with the generateQR method.
         * adds onClickListener to the ImageButton to add share the QR
          * */
        ImageButton qrCode = (ImageButton) findViewById(R.id.qr_code);

        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/Antox/");
        if(!file.exists()){
            file.mkdirs();
        }
        file = new File(Environment.getExternalStorageDirectory().getPath()+"/Antox/userkey_qr.png");
        generateQR(pref.getString("user_key", ""));
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        qrCode.setImageBitmap(bmp);
        qrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath() + "/Antox/userkey_qr.png")));
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_with)));
            }
        });


        LinearLayout avatar = (LinearLayout)findViewById(R.id.avatar_image_layout);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_PICK);
                shareIntent.setType("image/*");
                startActivityForResult(shareIntent, 1);
            }
        });


        /*
         * If the preference for user profile image isn't blank then set the user profile
         * image...
         */
        if(!pref.getString("saved_user_image","").equals("")){
            file = new File(Environment.getExternalStorageDirectory().getPath() + "/Antox/Avatar.jpeg");
            ImageButton profileImage = (ImageButton)findViewById(R.id.avatar_image);
            try{
                if(file!=null){
                    if(file.exists()){
                        UserDetails.image = BitmapFactory.decodeFile(file.getPath());
                        profileImage = (ImageButton)findViewById(R.id.avatar_image);
                        UserDetails.image = Bitmap.createScaledBitmap(UserDetails.image,256,256,true);
                        profileImage.setImageBitmap(UserDetails.image);
                    }
                }
            }catch(Exception e){
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_user_avatar));
            }
        }




		/* If the preferences aren't blank, then add them to text fields
         * otherwise it will display the predefined hints in strings.xml
         */

        if (!pref.getString("saved_name_hint", "").equals("")) {
            EditText nameHint = (EditText) findViewById(R.id.settings_name_hint);
            nameHint.setText(pref.getString("saved_name_hint", ""));
        }

        if (!pref.getString("saved_note_hint", "").equals("")) {
            EditText noteHint = (EditText) findViewById(R.id.settings_note_hint);
            noteHint.setText(pref.getString("saved_note_hint", ""));
        }

        if (!pref.getString("saved_status_hint", "").equals("")) {
            String savedStatus = pref.getString("saved_status_hint", "");
            int statusPos = statusAdapter.getPosition(savedStatus);
            statusSpinner.setSelection(statusPos);
        }
    }

    /*
    * generates the QR using the ZXING library (core.jar in libs folder)
     */
    private void generateQR(String userKey) {
        String qrData = "tox://" + userKey;
        int qrCodeSize = 500;
        QRCodeEncode qrCodeEncoder = new QRCodeEncode(qrData, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeSize);
        FileOutputStream out;
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            out = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/antox/userkey_qr.png");
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

		/* Get all text from the fields */
        EditText nameHintText = (EditText) findViewById(R.id.settings_name_hint);
        EditText noteHintText = (EditText) findViewById(R.id.settings_note_hint);
        //EditText statusHintText = (EditText) findViewById(R.id.settings_status_hint);

		/* Save settings to file */

        SharedPreferences pref = getSharedPreferences("settings",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

		/*
		 * If the fields aren't equal to the default strings in strings.xml then
		 * they contain user entered data so they need saving
		 */
        if (!nameHintText.getText().toString().equals(getString(R.id.settings_name_hint))) {
            editor.putString("saved_name_hint", nameHintText.getText().toString());
            UserDetails.username = nameHintText.getText().toString();
            updatedSettings[0] = nameHintText.getText().toString();
        }

        if (!noteHintText.getText().toString().equals(getString(R.id.settings_note_hint))) {
            editor.putString("saved_note_hint", noteHintText.getText().toString());
            UserDetails.note = noteHintText.getText().toString();
            updatedSettings[2] = noteHintText.getText().toString();
        }
        editor.putString("saved_status_hint", statusSpinner.getSelectedItem().toString());
        if (statusSpinner.getSelectedItem().toString().equals("online"))
            UserDetails.status = ToxUserStatus.TOX_USERSTATUS_NONE;
        if (statusSpinner.getSelectedItem().toString().equals("away"))
            UserDetails.status = ToxUserStatus.TOX_USERSTATUS_AWAY;
        if (statusSpinner.getSelectedItem().toString().equals("busy"))
            UserDetails.status = ToxUserStatus.TOX_USERSTATUS_BUSY;

        updatedSettings[1] = statusSpinner.getSelectedItem().toString();

        editor.commit();

        /* Send an intent to ToxService notifying change of settings */
        Intent updateSettings = new Intent(this, ToxService.class);
        updateSettings.setAction(Constants.UPDATE_SETTINGS);
        updateSettings.putExtra("newSettings", updatedSettings);
        this.startService(updateSettings);

        Context context = getApplicationContext();
        CharSequence text = getResources().getString(R.string.profile_updated);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        finish();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                if(imageReturnedIntent==null){
                    Toast.makeText(ProfileActivity.this,"Failed to fetch the image",Toast.LENGTH_SHORT).show();
                }else {
                    //ACTUAL IMAGE LOADING TAKES AT THIS PLACE....
                    try{
                        Uri imageuri = imageReturnedIntent.getData();
                        InputStream stream = getContentResolver().openInputStream(imageuri);
                        FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/antox/Avatar.jpeg");
                        Bitmap img = BitmapFactory.decodeStream(stream);
                        img.compress(Bitmap.CompressFormat.JPEG,70,out);
                        out.close();

                        UserDetails.image = Bitmap.createScaledBitmap(img,256,256,true);

                        SharedPreferences prefs = getSharedPreferences("settings" , Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("saved_user_image","image_saved");
                        editor.commit();

                        ImageButton profileImage = (ImageButton)findViewById(R.id.avatar_image);
                        profileImage.setImageBitmap(UserDetails.image);
                    }catch(FileNotFoundException e){
                        Toast.makeText(ProfileActivity.this,"Failed to fetch the image",Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                        Toast.makeText(ProfileActivity.this,"Failed to fetch the image",Toast.LENGTH_SHORT).show();
                    }
                }
            }else{
                Toast.makeText(ProfileActivity.this,"Image Not Selected ",Toast.LENGTH_SHORT).show();
            }
        }
    }
}