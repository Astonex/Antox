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

import java.sql.Timestamp
import im.tox.jtoxcore.ToxUserStatus
//remove if not needed
import scala.collection.JavaConversions._

class LeftPaneItem(
  val viewType: Int,
  val key: String,
  val first: String,
  val second: String,
  val isOnline: Boolean,
  val status: ToxUserStatus,
  val count: Int,
  val timestamp: Timestamp) {

  def this(
    key: String,
    first: String,
    second: String,
    isOnline: Boolean,
    status: ToxUserStatus,
    count: Int,
    timestamp: Timestamp) = this(Constants.TYPE_CONTACT, key, first, second, isOnline, status, count, timestamp)

  def this(header: String) = this(Constants.TYPE_HEADER, "", header, null, false, null, 0, null)

  def this(key: String, message: String) = this(Constants.TYPE_FRIEND_REQUEST, key, key, message, false, null, 0, null)

}
