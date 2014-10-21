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

import android.content.Context
import im.tox.antox.data.AntoxDB
import im.tox.antox.tox.ToxSingleton
import im.tox.antox.utils.AntoxFriend
import im.tox.jtoxcore.callbacks.OnStatusMessageCallback
import AntoxOnStatusMessageCallback._
//remove if not needed
import scala.collection.JavaConversions._

object AntoxOnStatusMessageCallback {

  private val TAG = "im.tox.antox.TAG"
}

class AntoxOnStatusMessageCallback(private var ctx: Context) extends OnStatusMessageCallback[AntoxFriend] {

  override def execute(friend: AntoxFriend, newStatus: String) {
    val db = new AntoxDB(ctx)
    db.updateStatusMessage(friend.getId, newStatus)
    db.close()
    ToxSingleton.updateFriendsList(ctx)
  }
}
