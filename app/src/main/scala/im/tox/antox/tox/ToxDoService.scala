/*
 * Copyright (c) 2014 Mark Winter (Astonex)
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


package im.tox.antox.tox

import android.app.Service
import android.content.{Context, Intent}
import android.net.ConnectivityManager
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log

class ToxDoService extends Service() {

  private var serviceThread: Thread = _

  private var keepRunning: Boolean = true

  override def onCreate() {
    if (!ToxSingleton.isInited) {
      ToxSingleton.initTox(getApplicationContext)
      Log.d("ToxDoService", "Initting ToxSingleton")
    }
    keepRunning = true
    val start = new Runnable() {

      override def run() {
        while (keepRunning) {
          val preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext)
          val connManager = getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
          val wifiOnly = preferences.getBoolean("wifi_only", true)
          val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
          if (wifiOnly && !mWifi.isConnected) {
            try {
              Thread.sleep(10000)
            } catch {
              case e: Exception =>
            }
          } else {
            try {
              Thread.sleep(ToxSingleton.jTox.doToxInterval())
              ToxSingleton.jTox.doTox()
            } catch {
              case e: Exception =>
            }
          }
        }
      }
    }
    serviceThread = new Thread(start)
    serviceThread.start()
  }

  override def onBind(intent: Intent): IBinder = null

  override def onStartCommand(intent: Intent, flags: Int, id: Int): Int = Service.START_STICKY

  override def onDestroy() {
    super.onDestroy()
    keepRunning = false
    serviceThread.interrupt()
    ToxSingleton.isInited = false
    Log.d("ToxDoService", "onDestroy() called")
  }
}
