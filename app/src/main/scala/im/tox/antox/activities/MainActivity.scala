package im.tox.antox.activities

import java.util

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActionBarDrawerToggle
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarActivity
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.Toast
import java.util.ArrayList
import java.util.Locale
import im.tox.antox.R
import im.tox.antox.data.AntoxDB
import im.tox.antox.tox.ToxSingleton
import im.tox.antox.utils.AntoxFriend
import im.tox.antox.utils.BitmapManager
import im.tox.antox.utils.Constants
import im.tox.antox.utils.DrawerArrayAdapter
import im.tox.antox.utils.DrawerItem
import im.tox.antox.utils.Triple
import im.tox.jtoxcore.ToxCallType
import im.tox.jtoxcore.ToxCodecSettings
import im.tox.jtoxcore.ToxException
//remove if not needed
import scala.collection.JavaConversions._

class MainActivity extends ActionBarActivity {

  var request: View = _

  var preferences: SharedPreferences = _

  private var mDrawerLayout: DrawerLayout = _

  private var mDrawerList: ListView = _

  private var mDrawerToggle: ActionBarDrawerToggle = _

  private def selectItem(position: Int) {
    if (position == 0) {
      val intent = new Intent(this, classOf[Settings])
      startActivity(intent)
    } else if (position == 1) {
      val intent = new Intent(this, classOf[ProfileSettingsActivity])
      startActivity(intent)
    } else if (position == 2) {
      Toast.makeText(this, "Coming soon...", Toast.LENGTH_LONG)
        .show()
    } else if (position == 3) {
      val intent = new Intent(this, classOf[About])
      startActivity(intent)
    } else if (position == 4) {
      val intent = new Intent(this, classOf[License])
      startActivity(intent)
    }
    mDrawerList.setItemChecked(position, true)
    mDrawerLayout.closeDrawer(mDrawerList)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val id = item.getItemId
    if (id == android.R.id.home) {
      if (mDrawerToggle.onOptionsItemSelected(item)) {
        return true
      }
    }
    super.onOptionsItemSelected(item)
  }

  protected override def onPostCreate(savedInstanceState: Bundle) {
    super.onPostCreate(savedInstanceState)
    mDrawerToggle.syncState()
  }

  override def onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    mDrawerToggle.onConfigurationChanged(newConfig)
  }

  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    preferences = PreferenceManager.getDefaultSharedPreferences(this)
    setVolumeControlStream(AudioManager.STREAM_VOICE_CALL)
    val language = preferences.getString("language", "-1")
    if (language == "-1") {
      val editor = preferences.edit()
      val currentLanguage = getResources.getConfiguration.locale.getCountry.toLowerCase()
      editor.putString("language", currentLanguage)
      editor.apply()
    } else {
      val locale = new Locale(language)
      Locale.setDefault(locale)
      val config = new Configuration()
      config.locale = locale
      getApplicationContext.getResources.updateConfiguration(config, getApplicationContext.getResources.getDisplayMetrics)
    }
    setContentView(R.layout.activity_main)
    mDrawerLayout = findViewById(R.id.drawer_layout).asInstanceOf[DrawerLayout]
    mDrawerList = findViewById(R.id.left_drawer).asInstanceOf[ListView]
    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
    val list = new util.ArrayList[DrawerItem]()
    list.add(new DrawerItem(getString(R.string.n_settings), R.drawable.ic_menu_settings))
    list.add(new DrawerItem(getString(R.string.n_profile_options), R.drawable.ic_profile))
    list.add(new DrawerItem(getString(R.string.n_create_group), R.drawable.ic_social_add_group))
    list.add(new DrawerItem(getString(R.string.n_about), R.drawable.ic_menu_help))
    list.add(new DrawerItem(getString(R.string.n_open_source), R.drawable.ic_opensource))
    val drawerListAdapter = new DrawerArrayAdapter(this, R.layout.rowlayout_drawer, list)
    mDrawerList.setAdapter(drawerListAdapter)
    mDrawerList.setOnItemClickListener(new DrawerItemClickListener())
    if (getSupportActionBar != null) {
      getSupportActionBar.setDisplayHomeAsUpEnabled(true)
      getSupportActionBar.setHomeButtonEnabled(true)
    }
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
      R.string.drawer_close) {

      override def onDrawerClosed(view: View) {
        ActivityCompat.invalidateOptionsMenu(MainActivity.this)
      }

      override def onDrawerOpened(drawerView: View) {
        ActivityCompat.invalidateOptionsMenu(MainActivity.this)
      }
    }
    mDrawerLayout.setDrawerListener(mDrawerToggle)
    getSupportActionBar.hide()
    if (Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN &&
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      getWindow.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
    }
    val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    val networkInfo = connMgr.getActiveNetworkInfo
    if (networkInfo != null && !networkInfo.isConnected) {
      showAlertDialog(MainActivity.this, getString(R.string.main_no_internet), getString(R.string.main_not_connected))
    }
    ToxSingleton.mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) new BitmapManager()
    Constants.epoch = System.currentTimeMillis() / 1000
    ToxSingleton.updateFriendsList(this)
    ToxSingleton.updateLastMessageMap(this)
    ToxSingleton.updateUnreadCountMap(this)
    val db = new AntoxDB(getApplicationContext)
    db.clearFileNumbers()
    db.close()
    updateLeftPane()
  }

  def updateLeftPane() {
    ToxSingleton.updateFriendRequests(getApplicationContext)
    ToxSingleton.updateFriendsList(getApplicationContext)
    ToxSingleton.updateMessages(getApplicationContext)
  }

  def onClickAddFriend(v: View) {
    val intent = new Intent(this, classOf[AddFriendActivity])
    startActivityForResult(intent, Constants.ADD_FRIEND_REQUEST_CODE)
  }

  def onClickVoiceCallFriend(v: View) {
    val toxCodecSettings = new ToxCodecSettings(ToxCallType.TYPE_AUDIO, 0, 0, 0, 64000, 20, 48000, 1)
    val mFriend = ToxSingleton.getAntoxFriend(ToxSingleton.activeKey)
    mFriend.foreach(friend => {
      val userID = friend.getFriendnumber()
      try {
        ToxSingleton.jTox.avCall(userID, toxCodecSettings, 10)
      } catch {
        case e: ToxException =>
      }
    })
  }

  def onClickVideoCallFriend(v: View) {
  }

  protected override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == Constants.ADD_FRIEND_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      ToxSingleton.updateFriendsList(this)
    }
  }

  override def onPause() {
    super.onPause()
    if (preferences.getBoolean("beenLoaded", false)) {
      ToxSingleton.chatActive = false
    }
  }

  def onTButtonClick(view: View): Unit = {
    if(mDrawerLayout.isDrawerOpen(Gravity.LEFT))
      mDrawerLayout.closeDrawers()
    else
      mDrawerLayout.openDrawer(Gravity.LEFT)
  }

  override def onBackPressed() {
    if(mDrawerLayout.isDrawerOpen(Gravity.LEFT))
      mDrawerLayout.closeDrawers()
    else
      finish()
  }

  def showAlertDialog(context: Context, title: String, message: String) {
    val alertDialog = new AlertDialog.Builder(context).create()
    alertDialog.setTitle(title)
    alertDialog.setMessage(message)
    alertDialog.setIcon(R.drawable.ic_launcher)
    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

      def onClick(dialog: DialogInterface, which: Int) {
      }
    })
    alertDialog.show()
  }

  private class DrawerItemClickListener extends AdapterView.OnItemClickListener {

    override def onItemClick(parent: AdapterView[_],
      view: View,
      position: Int,
      id: Long) {
      selectItem(position)
    }
  }
}
