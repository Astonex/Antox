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

package im.tox.antox.callbacks

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import im.tox.antox.R
import im.tox.antox.activities.MainActivity
import im.tox.antox.data.AntoxDB
import im.tox.antox.tox.ToxSingleton
import im.tox.jtoxcore.callbacks.OnFriendRequestCallback
import AntoxOnFriendRequestCallback._
//remove if not needed
import scala.collection.JavaConversions._

object AntoxOnFriendRequestCallback {

  private val TAG = "im.tox.antox.TAG"

  val FRIEND_KEY = "im.tox.antox.FRIEND_KEY"

  val FRIEND_MESSAGE = "im.tox.antox.FRIEND_MESSAGE"
}

class AntoxOnFriendRequestCallback(private var ctx: Context) extends OnFriendRequestCallback {

  override def execute(publicKey: String, message: String) {
    val db = new AntoxDB(this.ctx)
    if (!db.isFriendBlocked(publicKey)) db.addFriendRequest(publicKey, message)
    db.close()
    ToxSingleton.updateFriendRequests(ctx)
    Log.d("FriendRequestCallback", "")
    val preferences = PreferenceManager.getDefaultSharedPreferences(this.ctx)
    if (preferences.getBoolean("notifications_enable_notifications", true) !=
      false &&
      preferences.getBoolean("notifications_friend_request", true) !=
      false) {
      val vibratePattern = Array[Long](0, 500)
      if (preferences.getBoolean("notifications_new_message_vibrate", true) ==
        false) {
        vibratePattern(1) = 0
      }
      val mBuilder = new NotificationCompat.Builder(this.ctx)
        .setSmallIcon(R.drawable.ic_actionbar)
        .setContentTitle(this.ctx.getString(R.string.friend_request))
        .setContentText(message)
        .setVibrate(vibratePattern)
        .setDefaults(Notification.DEFAULT_ALL)
        .setAutoCancel(true)
      val targetIntent = new Intent(this.ctx, classOf[MainActivity])
      val contentIntent = PendingIntent.getActivity(this.ctx, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      mBuilder.setContentIntent(contentIntent)
      ToxSingleton.mNotificationManager.notify(0, mBuilder.build())
    }
  }
}
