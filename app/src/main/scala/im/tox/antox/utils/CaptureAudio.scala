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

import android.media.{MediaRecorder, AudioFormat, AudioRecord}
import android.os.AsyncTask
import android.util.Log
import im.tox.antox.tox.ToxSingleton

class CaptureAudio extends AsyncTask[String, Void, Void] {

  private var bufferSizeBytes: Int = _
  private val sampleRates: Array[Int] = Array(8000, 11025, 22050, 44100)

  protected override def doInBackground(params: String*): Void = {
    val audioRecord = findAudioRecord()

    audioRecord.startRecording()

    while (true) {
      if (isCancelled)
        null

      try {
        val buffer = Array.ofDim[Byte](bufferSizeBytes)
        audioRecord.read(buffer, 0, bufferSizeBytes)
        ToxSingleton.jTox.avSendAudio(java.lang.Integer.parseInt(params(0)), buffer)
        Log.d("Mic", "Sending audio to:" + params(0))
      } catch {
        case e: Exception =>
      }
    }

    audioRecord.stop()
    audioRecord.release()

    null
  }

  private def findAudioRecord(): AudioRecord = {
    for (rate <- sampleRates; audioFormat <- Array(AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT);
         channelConfig <- Array(AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO)) {
      try {
        Log.d("CaptureAudio", "Attempting rate " + rate + "Hz, bits: " + audioFormat +
          ", channel: " +
          channelConfig)
        val bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat)
        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
          val recorder = new AudioRecord(0, rate, channelConfig, audioFormat,
            bufferSize)
          if (recorder.getState == AudioRecord.STATE_INITIALIZED) {
            bufferSizeBytes = bufferSize
            return recorder
          }
        }
      } catch {
        case e: Exception =>
      }
    }
    null
  }
}
