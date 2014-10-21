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

import android.util.Log
import rx.lang.scala.Observable
import rx.lang.scala.schedulers.IOScheduler
import CallManager._

object CallManager {
  private val TAG = "im.tox.antox.utils.CallManager"
}

class CallManager () {
  private var _calls: Map[Integer, Call] = Map[Integer, Call]()

  def add(c: Call) = {
    Log.d(TAG, "Adding call")
    _calls = _calls + (c.id -> c)
  }

  def get(id: Integer): Option[Call] = {
    _calls.get(id).asInstanceOf[Option[Call]]
  }

  def remove(id: Integer): Unit = {
    Log.d(TAG, "Removing call")
    val mCall = this.get(id)
    mCall match {
      case Some(c) => 
        c.subscription.unsubscribe()
        _calls = _calls - id
      case None =>
    }
  }
}
