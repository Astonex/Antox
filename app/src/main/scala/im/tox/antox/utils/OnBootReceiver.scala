/*
 * Copyright (c) 2014 Emil Suleymanov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package im.tox.antox.utils

import java.util.Locale

import android.app.NotificationManager
import android.content.res.Configuration
import android.content.{SharedPreferences, BroadcastReceiver, Context, Intent}
import android.net.ConnectivityManager
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import im.tox.antox.data.AntoxDB
import im.tox.antox.tox.{ToxDoService, ToxSingleton}

class OnBootReceiver extends BroadcastReceiver {
  def onReceive(context: Context, intent: Intent) {

    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    if (!preferences.contains("auto_start") || preferences.getBoolean("auto_start", true)) {
      Log.d("AUTO START", "Starting!")

      // Set the right language
      val language = preferences.getString("language", "-1")
      if (language == "-1") {
        val editor = preferences.edit()
        val currentLanguage = context.getResources.getConfiguration.locale.getCountry.toLowerCase
        editor.putString("language", currentLanguage)
        editor.apply()
      } else {
        val locale = new Locale(language)
        Locale.setDefault(locale)
        val config = new Configuration()
        config.locale = locale
        context.getResources.updateConfiguration(config, context.getResources.getDisplayMetrics)
      }

      ToxSingleton.mNotificationManager
        = context.getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) new BitmapManager

      Constants.epoch = System.currentTimeMillis / 1000

      ToxSingleton.updateFriendsList(context)
      ToxSingleton.updateLastMessageMap(context)
      ToxSingleton.updateUnreadCountMap(context)

      val db = new AntoxDB(context)
      db.clearFileNumbers()
      db.close()

      ToxSingleton.updateFriendRequests(context)
      ToxSingleton.updateFriendsList(context)
      ToxSingleton.updateMessages(context)

      val connectivityManager
      = context.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
      val networkInfo = connectivityManager.getActiveNetworkInfo
      if (networkInfo != null && networkInfo.isConnected) {
        if (ToxSingleton.dhtNodes.size == 0) {
          ToxSingleton.updateDhtNodes(context)
        }
      }

      val service = new Intent(context, classOf[ToxDoService])
      context.startService(service)
    }
  }
}

