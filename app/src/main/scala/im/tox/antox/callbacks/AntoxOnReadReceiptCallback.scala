package im.tox.antox.callbacks

import android.content.Context
import android.util.Log
import im.tox.antox.callbacks.AntoxOnReadReceiptCallback._
import im.tox.antox.data.AntoxDB
import im.tox.antox.tox.ToxSingleton
import im.tox.antox.utils.AntoxFriend
import im.tox.jtoxcore.callbacks.OnReadReceiptCallback

//remove if not needed

object AntoxOnReadReceiptCallback {

  private val TAG = "im.tox.antox.callbacks.AntoxOnReadReceiptCallback"
}

class AntoxOnReadReceiptCallback(private var ctx: Context) extends OnReadReceiptCallback[AntoxFriend] {

  override def execute(friend: AntoxFriend, receipt: Int) {
    val db = new AntoxDB(this.ctx)
    val key = db.setMessageReceived(receipt)
    Log.d(TAG, "read receipt, for key: " + key)
    db.close()
    ToxSingleton.updateMessages(ctx)
  }
}
