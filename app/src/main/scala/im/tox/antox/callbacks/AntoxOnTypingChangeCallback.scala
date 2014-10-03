package im.tox.antox.callbacks

import android.content.Context
import im.tox.antox.tox.{Reactive, ToxSingleton}
import im.tox.antox.utils.AntoxFriend
import im.tox.jtoxcore.callbacks.OnTypingChangeCallback

//remove if not needed

object AntoxOnTypingChangeCallback {

  private val TAG = "OnTypingChangeCallback"
}

class AntoxOnTypingChangeCallback(private var ctx: Context) extends OnTypingChangeCallback[AntoxFriend] {

  def execute(friend: AntoxFriend, typing: Boolean) {
    ToxSingleton.typingMap.put(friend.getId, typing)
    Reactive.typing.onNext(true)
  }
}
