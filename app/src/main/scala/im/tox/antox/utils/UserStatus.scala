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

import im.tox.jtoxcore.ToxUserStatus
//remove if not needed
import scala.collection.JavaConversions._

object UserStatus {

  def getToxUserStatusFromString(status: String): ToxUserStatus = {
    if (status == "online") return ToxUserStatus.TOX_USERSTATUS_NONE
    if (status == "away") return ToxUserStatus.TOX_USERSTATUS_AWAY
    if (status == "busy") return ToxUserStatus.TOX_USERSTATUS_BUSY
    ToxUserStatus.TOX_USERSTATUS_NONE
  }

  def getStringFromToxUserStatus(status: ToxUserStatus): String = {
    if (status == ToxUserStatus.TOX_USERSTATUS_NONE) return "online"
    if (status == ToxUserStatus.TOX_USERSTATUS_AWAY) return "away"
    if (status == ToxUserStatus.TOX_USERSTATUS_BUSY) return "busy"
    "invalid"
  }
}
