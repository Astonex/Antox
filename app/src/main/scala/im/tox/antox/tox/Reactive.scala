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

import java.sql.Timestamp

import im.tox.antox.data.State
import im.tox.antox.utils.{Friend, FriendInfo, FriendRequest}
import rx.lang.scala.subjects.BehaviorSubject

object Reactive {
  val chatActive = BehaviorSubject[Boolean](false)
  val chatActiveSub = chatActive.subscribe(x => State.chatActive(x))
  val activeKey = BehaviorSubject[Option[String]](None)
  val activeKeySub = activeKey.subscribe(x => State.activeKey(x))
  val friendList = BehaviorSubject[Array[Friend]](new Array[Friend](0))
  val friendRequests = BehaviorSubject[Array[FriendRequest]](new Array[FriendRequest](0))
  val lastMessages = BehaviorSubject[Map[String, (String, Timestamp)]](Map.empty[String, (String, Timestamp)])
  val unreadCounts = BehaviorSubject[Map[String, Integer]](Map.empty[String, Integer])
  val typing = BehaviorSubject[Boolean](false)
  val updatedMessages = BehaviorSubject[Boolean](true)
  val friendInfoList = friendList
    .combineLatestWith(lastMessages)((fl, lm) => (fl, lm))
    .combineLatestWith(unreadCounts)((tup, uc) => {
      tup match {
        case (fl, lm) => {
          fl.map(f => {
            val lastMessageTup: Option[(String, Timestamp)] = lm.get(f.friendKey)
            val unreadCount: Option[Integer] = uc.get(f.friendKey)
            (lastMessageTup, unreadCount) match {
              case (Some((lastMessage, lastMessageTimestamp)), Some(unreadCount)) => {
                new FriendInfo(f.isOnline, f.friendName, f.friendStatus, f.personalNote, f.friendKey, lastMessage, lastMessageTimestamp, unreadCount, f.alias)
              }
              case (Some((lastMessage, lastMessageTimestamp)), None) => {
                new FriendInfo(f.isOnline, f.friendName, f.friendStatus, f.personalNote, f.friendKey, lastMessage, lastMessageTimestamp, 0, f.alias)
              }
              case _ => {
                new FriendInfo(f.isOnline, f.friendName, f.friendStatus, f.personalNote, f.friendKey, "", new Timestamp(0, 0, 0, 0, 0, 0, 0), 0, f.alias)
              }
            }
          })
        }
      }
    })
  val friendListAndRequests = friendInfoList.combineLatestWith(friendRequests)((fi, fr) => (fi, fr))
}
