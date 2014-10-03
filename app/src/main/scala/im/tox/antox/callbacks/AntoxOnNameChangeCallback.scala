package im.tox.antox.callbacks

import android.content.Context
import im.tox.antox.data.AntoxDB
import im.tox.antox.tox.ToxSingleton
import im.tox.antox.utils.AntoxFriend
import im.tox.jtoxcore.callbacks.OnNameChangeCallback

//remove if not needed

object AntoxOnNameChangeCallback {

  private val TAG = "im.tox.antox.TAG"
}

class AntoxOnNameChangeCallback(private var ctx: Context) extends OnNameChangeCallback[AntoxFriend] {

  override def execute(friend: AntoxFriend, newName: String) {
    val db = new AntoxDB(ctx)
    db.updateFriendName(friend.getId, newName)
    db.close()
    ToxSingleton.updateFriendsList(ctx)
  }
}
