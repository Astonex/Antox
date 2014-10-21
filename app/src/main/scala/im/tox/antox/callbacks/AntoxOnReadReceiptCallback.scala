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
import android.util.Log
import im.tox.antox.data.AntoxDB
import im.tox.antox.tox.ToxSingleton
import im.tox.antox.utils.AntoxFriend
import im.tox.jtoxcore.callbacks.OnReadReceiptCallback
import AntoxOnReadReceiptCallback._
//remove if not needed
import scala.collection.JavaConversions._

object AntoxOnReadReceiptCallback {

  private val TAG = "im.tox.antox.callbacks.AntoxOnReadReceiptCallback"
}

class AntoxOnReadReceiptCallback(private var ctx: Context) extends OnReadReceiptCallback[AntoxFriend] {

  override def execute(friend: AntoxFriend, receipt: Int) {
    val db = new AntoxDB(this.ctx)
    val key = db.setMessageReceived(receipt)
    Log.d(TAG, "read receipt, for key: " + key)
    db.close()
    ToxSingleton.updateMessages(ctx)
  }
}
