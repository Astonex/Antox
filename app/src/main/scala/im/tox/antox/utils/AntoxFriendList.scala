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
import java.util.Collections
import java.util.Iterator
import java.util.List
import java.util.Locale
import im.tox.jtoxcore.FriendExistsException
import im.tox.jtoxcore.FriendList
import im.tox.jtoxcore.ToxFriend
import im.tox.jtoxcore.ToxUserStatus
import rx.lang.scala.JavaConversions
import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subscriber
import rx.lang.scala.Subscription
import rx.lang.scala.Subject
import rx.lang.scala.schedulers.IOScheduler
import rx.lang.scala.schedulers.AndroidMainThreadScheduler
//remove if not needed
import scala.collection.JavaConversions._

class AntoxFriendList extends FriendList[AntoxFriend] {

  private var friends: List[AntoxFriend] = Collections.synchronizedList(new ArrayList[AntoxFriend]())

  def this(friends: ArrayList[AntoxFriend]) {
    this()
    this.friends = friends
  }

  override def getByFriendNumber(friendnumber: Int): AntoxFriend = {
    friends.filter(friend => friend.getFriendnumber == friendnumber).headOption match {
      case Some(f) => f
      case None => null
    }
  }

  def getByKey(key: String): Option[AntoxFriend] = {
    friends.filter(friend => friend.getId == key).headOption
  }

  override def getById(id: String): AntoxFriend = {
    getByKey(id) match {
      case Some(x) => x
      case None => null
    }
  }

  override def getByName(name: String, ignorecase: Boolean): List[AntoxFriend] = {
    if (ignorecase) {
      return getByNameIgnoreCase(name)
    } else {
      friends.filter(friend => (friend.name == null && name == null) || (name != null && name == friend.name))
    }
  }

  private def getByNameIgnoreCase(name: String): List[AntoxFriend] = {
    friends.filter(friend => (friend.name == null && name == null) || (name != null && name.equalsIgnoreCase(friend.name)))
  }

  override def searchFriend(partial: String): List[AntoxFriend] = {
    val partialLowered = partial.toLowerCase(Locale.US)
    if (partial == null) {
      throw new IllegalArgumentException("Cannot search for null")
    }
    friends.filter(friend => (friend.name != null && friend.name.contains(partialLowered)))
  }

  override def getByStatus(status: ToxUserStatus): List[AntoxFriend] = {
    friends.filter(friend => friend.isOnline && friend.getStatus == status)
  }

  override def getOnlineFriends(): List[AntoxFriend] = {
    friends.filter(friend => friend.isOnline)
  }

  override def getOfflineFriends(): List[AntoxFriend] = {
    friends.filter(friend => !friend.isOnline)
  }

  override def all(): List[AntoxFriend] = {
    new ArrayList[AntoxFriend](this.friends)
  }

  override def addFriend(friendnumber: Int): AntoxFriend = {
    friends.filter(friend => friend.getFriendnumber == friendnumber).headOption match {
      case Some(f) => throw new FriendExistsException(f.getFriendnumber)
      case None => {
        val f = new AntoxFriend(friendnumber)
        this.friends.add(f)
        f
      }
    }
  }

  override def addFriendIfNotExists(friendnumber: Int): AntoxFriend = {
    friends.filter(friend => friend.getFriendnumber == friendnumber).headOption match {
      case Some(f) => f
      case None => {
        val f = new AntoxFriend(friendnumber)
        this.friends.add(f)
        f
      }
    }
  }

  override def removeFriend(friendnumber: Int) {
    friends.remove(friends.find(friend => friend.getFriendnumber == friendnumber))
  }
}
