package im.tox.antox.utils

import java.util.{ArrayList, Collections, List, Locale}

import im.tox.jtoxcore.{FriendExistsException, FriendList, ToxUserStatus}

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

  override def getById(id: String): AntoxFriend = {
    getByKey(id) match {
      case Some(x) => x
      case None => null
    }
  }

  def getByKey(key: String): Option[AntoxFriend] = {
    friends.filter(friend => friend.getId == key).headOption
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
