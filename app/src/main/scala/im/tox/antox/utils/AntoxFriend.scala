package im.tox.antox.utils

import java.util.ArrayList

import im.tox.jtoxcore.{ToxFriend, ToxUserStatus}

import scala.beans.{BeanProperty, BooleanBeanProperty}

//remove if not needed

class AntoxFriend(friendnumber: Int) extends ToxFriend {

  @BeanProperty
  var id: String = _
  @BeanProperty
  var name: String = _
  @BeanProperty
  var status: ToxUserStatus = ToxUserStatus.TOX_USERSTATUS_NONE
  @BeanProperty
  var statusMessage: String = _
  @BooleanBeanProperty
  var online: Boolean = false
  var isTyping: Boolean = _
  @BeanProperty
  var nickname: String = _
  @BeanProperty
  var previousNames: ArrayList[String] = _
  @transient private var friendNumber: Int = friendnumber

  override def getFriendnumber(): Int = this.friendNumber

  override def setTyping(isTyping: Boolean) {
    this.isTyping = isTyping
  }
}
