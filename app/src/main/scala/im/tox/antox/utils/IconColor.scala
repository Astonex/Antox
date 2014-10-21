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

import android.graphics.Color
import im.tox.jtoxcore.ToxUserStatus
import im.tox.antox.R
//remove if not needed
import scala.collection.JavaConversions._

object IconColor {

  def iconDrawable(isOnline: java.lang.Boolean, status: ToxUserStatus): Int = {
    val color = if (!isOnline) {
      R.drawable.circle_offline
    } else if (status == ToxUserStatus.TOX_USERSTATUS_NONE) {
      R.drawable.circle_online
    } else if (status == ToxUserStatus.TOX_USERSTATUS_AWAY) {
      R.drawable.circle_away
    } else if (status == ToxUserStatus.TOX_USERSTATUS_BUSY) {
      R.drawable.circle_busy
    } else {
      R.drawable.circle_offline
    }
    color
  }

}
