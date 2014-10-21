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

package im.tox.antox.utils

import java.util.ArrayList
import im.tox.jtoxcore.JTox
import im.tox.jtoxcore.ToxFriend
import im.tox.jtoxcore.ToxUserStatus
import scala.beans.BeanProperty
import scala.beans.BooleanBeanProperty
//remove if not needed
import scala.collection.JavaConversions._

class AntoxFriend(friendnumber: Int) extends ToxFriend {

  @transient private var friendNumber: Int = friendnumber

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

  override def getFriendnumber(): Int = this.friendNumber

  override def setTyping(isTyping: Boolean) {
    this.isTyping = isTyping
  }
}
