package im.tox.antox.callbacks

import android.app.{Notification, PendingIntent}
import android.content.{Context, Intent}
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import im.tox.antox.R
import im.tox.antox.activities.MainActivity
import im.tox.antox.data.AntoxDB
import im.tox.antox.tox.ToxSingleton
import im.tox.jtoxcore.callbacks.OnFriendRequestCallback

//remove if not needed

object AntoxOnFriendRequestCallback {

  val FRIEND_KEY = "im.tox.antox.FRIEND_KEY"
  val FRIEND_MESSAGE = "im.tox.antox.FRIEND_MESSAGE"
  private val TAG = "im.tox.antox.TAG"
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
