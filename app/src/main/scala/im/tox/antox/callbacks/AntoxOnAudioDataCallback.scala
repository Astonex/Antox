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
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import im.tox.antox.utils.AntoxFriend
import im.tox.jtoxcore.callbacks.OnAudioDataCallback
//remove if not needed
import scala.collection.JavaConversions._

class AntoxOnAudioDataCallback(private var ctx: Context) extends OnAudioDataCallback[AntoxFriend] {

  def execute(callID: Int, data: Array[Byte]) {
    Log.d("OnAudioDataCallback", "Received callback from: " + callID)
    try {
      val audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 48000, AudioFormat.CHANNEL_OUT_DEFAULT,
        AudioFormat.ENCODING_PCM_16BIT, data.length, AudioTrack.MODE_STREAM)
      audioTrack.play()
      audioTrack.write(data, 0, data.length)
      audioTrack.stop()
      audioTrack.release()
    } catch {
      case e: Exception => Log.e("AudioPlayback", e.getMessage)
    }
  }
}
