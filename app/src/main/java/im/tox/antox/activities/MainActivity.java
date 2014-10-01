package im.tox.antox.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import im.tox.antox.R;
import im.tox.antox.data.AntoxDB;
import im.tox.antox.tox.ToxSingleton;
import im.tox.antox.utils.AntoxFriend;
import im.tox.antox.utils.BitmapManager;
import im.tox.antox.utils.Constants;
import im.tox.antox.utils.DrawerArrayAdapter;
import im.tox.antox.utils.DrawerItem;
import im.tox.antox.utils.Triple;
import im.tox.jtoxcore.ToxCallType;
import im.tox.jtoxcore.ToxCodecSettings;
import im.tox.jtoxcore.ToxException;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * @author Mark Winter (Astonex)
 */

public class MainActivity extends ActionBarActivity {

    private final ToxSingleton toxSingleton = ToxSingleton.getInstance();
    public DrawerLayout pane;
    public View chat;
    public View request;
    Subscription activeKeySub;
    Subscription chatActiveSub;
    Subscription doClosePaneSub;

    SharedPreferences preferences;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private void selectItem(int position) {
        if (position == 0) {
            Intent intent = new Intent(this,
                    Settings.class);
            startActivity(intent);
        } else if (position == 1) {
            Intent intent = new Intent(this,
                    ProfileSettings.class);
            startActivity(intent);
        } else if (position == 2) {
            Toast.makeText(this, "Coming soon...", Toast.LENGTH_LONG).show();
            //TODO: support groups
        } else if (position == 3) {
            Intent intent = new Intent(this,
                    About.class);
            startActivity(intent);
        } else if (position == 4) {
            Intent intent = new Intent(this,
                    License.class);
            startActivity(intent);
        }
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pressing the volume keys will affect STREAM_MUSIC played from this app
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        /* Check if a language has been set or not */
        String language = preferences.getString("language", "-1");
        if (language.equals("-1")) {
            SharedPreferences.Editor editor = preferences.edit();
            String currentLanguage = getResources().getConfiguration().locale.getCountry().toLowerCase();
            editor.putString("language", currentLanguage);
            editor.apply();
        } else {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getApplicationContext().getResources().updateConfiguration(config, getApplicationContext().getResources().getDisplayMetrics());
        }

        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.slidingpane_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ArrayList<DrawerItem> list = new ArrayList<DrawerItem>();
        list.add(new DrawerItem(getString(R.string.n_settings), R.drawable.ic_menu_settings));
        list.add(new DrawerItem(getString(R.string.n_profile_options), R.drawable.ic_profile));
        list.add(new DrawerItem(getString(R.string.n_create_group), R.drawable.ic_social_add_group));
        list.add(new DrawerItem(getString(R.string.n_about), R.drawable.ic_menu_help));
        list.add(new DrawerItem(getString(R.string.n_open_source), R.drawable.ic_opensource));
        ListAdapter drawerListAdapter = new DrawerArrayAdapter(
                this,
                R.layout.rowlayout_drawer,
                list);
        mDrawerList.setAdapter(drawerListAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }

            public void onDrawerOpened(View drawerView) {
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().hide();

        /* Fix for an android 4.1.x bug */
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            );
        }

        /* Check if connected to the Internet */
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && !networkInfo.isConnected()) {
                /* Display lack of internet connection warning */
            showAlertDialog(MainActivity.this, getString(R.string.main_no_internet),
                    getString(R.string.main_not_connected));
        }

        chat = (View) findViewById(R.id.fragment_chat);
        pane = (DrawerLayout) findViewById(R.id.slidingpane_layout);
        DrawerLayout.DrawerListener paneListener = new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                Log.d("MainActivity", "Drawer listener, drawer open");
                toxSingleton.rightPaneActiveSubject.onNext(true);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                Log.d("MainActivity", "Drawer listener, drawer closed");
                toxSingleton.rightPaneActiveSubject.onNext(false);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        };
        pane.setDrawerListener(paneListener);

        toxSingleton.mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Init Bitmap Manager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            new BitmapManager();

        //Get epoch time for online/offline messages
        Constants.epoch = System.currentTimeMillis() / 1000; // Current time in seconds

        //Initialize the RxJava Subjects in tox singleton;
        toxSingleton.initSubjects(this);

        //Update lists
        toxSingleton.updateFriendsList(this);
        toxSingleton.updateLastMessageMap(this);
        toxSingleton.updateUnreadCountMap(this);

        AntoxDB db = new AntoxDB(getApplicationContext());
        db.clearFileNumbers();
        db.close();

        updateLeftPane();
    }

    public void updateLeftPane() {
        toxSingleton.updateFriendRequests(getApplicationContext());
        toxSingleton.updateFriendsList(getApplicationContext());
        toxSingleton.updateMessages(getApplicationContext());
    }

    public void onClickAddFriend(View v) {
        Intent intent = new Intent(this, AddFriendActivity.class);
        startActivityForResult(intent, Constants.ADD_FRIEND_REQUEST_CODE);
    }

    public void onClickVoiceCallFriend(View v) {
        ToxCodecSettings toxCodecSettings = new ToxCodecSettings(ToxCallType.TYPE_AUDIO, 0, 0, 0, 64000, 20, 48000, 1);
        AntoxFriend friend = toxSingleton.getAntoxFriend(toxSingleton.activeKey);
        int userID = friend.getFriendnumber();
        try {
            toxSingleton.jTox.avCall(userID, toxCodecSettings, 10);
        } catch (ToxException e) {
        }
    }

    public void onClickVideoCallFriend(View v) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ADD_FRIEND_REQUEST_CODE && resultCode == RESULT_OK) {
            toxSingleton.updateFriendsList(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toxSingleton.activeKey = "";
    }

    @Override
    public void onResume() {
        super.onResume();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        doClosePaneSub = toxSingleton.doClosePaneSubject.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean close) {
                        if (close) {
                            pane.openDrawer(Gravity.RIGHT);
                        } else {
                            pane.closeDrawer(Gravity.RIGHT);
                        }
                    }
                });
        activeKeySub = toxSingleton.rightPaneActiveAndKeyAndIsFriendSubject.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Triple<Boolean, String, Boolean>>() {
                    @Override
                    public void call(Triple<Boolean, String, Boolean> rightPaneActiveAndActiveKeyAndIfFriend) {
                        boolean rightPaneActive = rightPaneActiveAndActiveKeyAndIfFriend.x;
                        String activeKey = rightPaneActiveAndActiveKeyAndIfFriend.y;
                        boolean isFriend = rightPaneActiveAndActiveKeyAndIfFriend.z;
                        Log.d("activeKeySub", "oldkey: " + toxSingleton.activeKey + " newkey: " + activeKey + " isfriend: " + isFriend);
                        if (activeKey.equals("")) {
                            chat.setVisibility(View.GONE);
                        } else {
                            if (!activeKey.equals(toxSingleton.activeKey)) {
                                toxSingleton.doClosePaneSubject.onNext(true);
                                if (isFriend) {
                                    chat.setVisibility(View.VISIBLE);
                                } else {
                                    chat.setVisibility(View.GONE);
                                }
                            }
                        }
                        toxSingleton.activeKey = activeKey;
                        if (!activeKey.equals("") && rightPaneActive && isFriend) {
                            AntoxDB antoxDB = new AntoxDB(getApplicationContext());
                            antoxDB.markIncomingMessagesRead(activeKey);
                            toxSingleton.clearUselessNotifications(activeKey);
                            toxSingleton.updateMessages(getApplicationContext());
                            antoxDB.close();
                            toxSingleton.chatActive = true;
                        } else {
                            toxSingleton.chatActive = false;
                        }
                    }
                });

    }

    @Override
    public void onPause() {
        super.onPause();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("beenLoaded", false)) {
            activeKeySub.unsubscribe();
            doClosePaneSub.unsubscribe();
            toxSingleton.chatActive = false;
        }
    }

    void showAlertDialog(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (pane.isDrawerOpen(Gravity.RIGHT))
            pane.closeDrawers();
        else
            finish();
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}
