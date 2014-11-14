/*
 * Copyright (c) 2014 Emil Suleymanov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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

import android.app.NotificationManager
import android.content.{BroadcastReceiver, Context, Intent, SharedPreferences}
import android.preference.PreferenceManager
import im.tox.antox.tox.{ToxDoService, ToxSingleton}

class OnBootReceiver extends BroadcastReceiver {
  def onReceive(context: Context, intent: Intent) {

    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    if (!preferences.contains("auto_start") || preferences.getBoolean("auto_start", true)) {
      if (preferences.getBoolean("loggedin", false)) {
        ToxSingleton.mNotificationManager
          = context.getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
        val startTox = new Intent(context.getApplicationContext, classOf[ToxDoService])
        context.getApplicationContext.startService(startTox)
      }
    }
  }
}
