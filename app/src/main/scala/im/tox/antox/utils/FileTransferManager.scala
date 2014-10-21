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
import FileTransferManager._

object FileTransferManager {
  private val TAG = "im.tox.antox.utils.FileTransferManager"
}

class FileTransferManager () {
  private var _transfers: Map[Long, FileTransfer] = Map[Long, FileTransfer]()
  private var _keyAndFileNumberToId: Map[(String, Integer), Long] = Map[(String, Integer), Long]()

  def add(t: FileTransfer) = {
    Log.d(TAG, "Adding file transfer")
    _transfers = _transfers + (t.id -> t)
    _keyAndFileNumberToId = _keyAndFileNumberToId + ((t.key, t.fileNumber) -> t.id)
  }

  def remove(id: Long): Unit = {
    Log.d(TAG, "Removing file transfer")
    val mTransfer = this.get(id)
    mTransfer match {
      case Some(t) => 
    _transfers = _transfers - id
    _keyAndFileNumberToId = _keyAndFileNumberToId - ((t.key, t.fileNumber))
      case None =>
    }
  }

  def remove(key: String, fileNumber: Integer): Unit = {
    val mId = _keyAndFileNumberToId.get(key, fileNumber)
    mId match {
      case Some(id) => this.remove(id)
      case None => 
    }
  }

  def get(id: Long): Option[FileTransfer] = {
    _transfers.get(id).asInstanceOf[Option[FileTransfer]]
  }

  def get(key: String, fileNumber: Integer): Option[FileTransfer] = {
    val mId = _keyAndFileNumberToId.get(key, fileNumber)
    mId match {
      case Some(id) => this.get(id)
      case None => None
    }
  }
}
