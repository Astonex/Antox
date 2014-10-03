package im.tox.antox.tox

import java.io.{BufferedInputStream, BufferedReader, File, FileInputStream, FileOutputStream, InputStreamReader, Reader}
import java.net.URL
import java.nio.charset.Charset
import java.util
import java.util.{ArrayList, HashMap, HashSet}

import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.{Environment, SystemClock}
import android.preference.PreferenceManager
import android.util.Log
import im.tox.antox.callbacks.{AntoxOnActionCallback, AntoxOnAudioDataCallback, AntoxOnAvCallbackCallback, AntoxOnConnectionStatusCallback, AntoxOnFileControlCallback, AntoxOnFileDataCallback, AntoxOnFileSendRequestCallback, AntoxOnFriendRequestCallback, AntoxOnMessageCallback, AntoxOnNameChangeCallback, AntoxOnReadReceiptCallback, AntoxOnStatusMessageCallback, AntoxOnTypingChangeCallback, AntoxOnUserStatusCallback, AntoxOnVideoDataCallback}
import im.tox.antox.data.AntoxDB
import im.tox.antox.tox.ToxSingleton.FileStatus.FileStatus
import im.tox.antox.utils.{AntoxFriend, AntoxFriendList, Constants, DhtNode, Friend, FriendRequest, Options, Tuple, UserStatus}
import im.tox.jtoxcore.callbacks.CallbackHandler
import im.tox.jtoxcore.{JTox, ToxException, ToxFileControl, ToxOptions, ToxUserStatus}
import org.json.JSONObject
import rx.lang.scala.Observable
import rx.lang.scala.schedulers.{AndroidMainThreadScheduler, IOScheduler}

object ToxSingleton {

  private val TAG = "im.tox.antox.tox.ToxSingleton"
  var jTox: JTox[AntoxFriend] = _
  var callbackHandler: CallbackHandler[AntoxFriend] = _

  var mNotificationManager: NotificationManager = _
  var dataFile: ToxDataFile = _
  var qrFile: File = _
  var progressMap: util.HashMap[Integer, Integer] = new util.HashMap[Integer, Integer]()
  var progressHistoryMap: util.HashMap[Integer, util.ArrayList[Tuple[Integer, Long]]] = new util.HashMap[Integer, ArrayList[Tuple[Integer, Long]]]()
  var fileStatusMap: util.HashMap[Integer, FileStatus] = new util.HashMap[Integer, FileStatus]()
  var fileSizeMap: util.HashMap[Integer, Integer] = new util.HashMap[Integer, Integer]()
  var fileStreamMap: util.HashMap[Integer, FileOutputStream] = new util.HashMap[Integer, FileOutputStream]()
  var fileMap: util.HashMap[Integer, File] = new util.HashMap[Integer, File]()
  var fileIds: util.HashSet[Integer] = new util.HashSet[Integer]()
  var typingMap: util.HashMap[String, Boolean] = new util.HashMap[String, Boolean]()
  var isInited: Boolean = false
  var activeKey: String = _
  var chatActive: Boolean = _
  var dhtNodes: Array[DhtNode] = Array()
  private var antoxFriendList: AntoxFriendList = _

  def getInstance() = this

  def sendFileSendRequest(path: String, key: String, context: Context) {
    val file = new File(path)
    val splitPath = path.split("/")
    val fileName = splitPath(splitPath.length - 1)
    Log.d("sendFileSendRequest", "name: " + fileName)
    if (fileName != null) {
      require(key != null)
      getAntoxFriend(key)
        .map(_.getFriendnumber())
        .flatMap(friendNumber => {
        try {
          Log.d(TAG, "Creating tox file sender")
          val fn = jTox.newFileSender(friendNumber, file.length, fileName)
          fn match {
            case -1 => None
            case x => Some(x)
          }
        } catch {
          case e: Exception => {
            e.printStackTrace()
            None
          }
        }
      }).foreach(fileNumber => {
        val antoxDB = new AntoxDB(context)
        Log.d(TAG, "adding File Transfer")
        val id = antoxDB.addFileTransfer(key, path, fileNumber, file.length.toInt, true)
        fileIds.add(id.toInt)
        antoxDB.close()
      })
    }
  }

  def getAntoxFriend(key: String): Option[AntoxFriend] = {
    try {
      antoxFriendList.getByKey(key)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        None
    }
  }

  def fileSendRequest(key: String,
                      fileNumber: Int,
                      fileName: String,
                      fileSize: Long,
                      context: Context) {
    Log.d("fileSendRequest, fileNumber: ", java.lang.Integer.toString(fileNumber))
    var fileN = fileName
    val fileSplit = fileName.split("\\.")
    var filePre = ""
    val fileExt = fileSplit(fileSplit.length - 1)
    for (j <- 0 until fileSplit.length - 1) {
      filePre = filePre.concat(fileSplit(j))
      if (j < fileSplit.length - 2) {
        filePre = filePre.concat(".")
      }
    }
    val dirfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
      Constants.DOWNLOAD_DIRECTORY)
    if (!dirfile.mkdirs()) {
      Log.e("acceptFile", "Directory not created")
    }
    var file = new File(dirfile.getPath, fileN)
    if (file.exists()) {
      var i = 1
      do {
        fileN = filePre + "(" + java.lang.Integer.toString(i) + ")" +
          "." +
          fileExt
        file = new File(dirfile.getPath, fileN)
        i += 1
      } while (file.exists())
    }
    val antoxDB = new AntoxDB(context)
    val id = antoxDB.addFileTransfer(key, fileN, fileNumber, fileSize.toInt, false)
    fileIds.add(id.toInt)
    antoxDB.close()
  }

  def changeActiveKey(key: String) {
    Reactive.activeKey.onNext(Some(key))
  }

  def clearActiveKey() {
    Reactive.activeKey.onNext(None)
  }

  def acceptFile(key: String, fileNumber: Int, context: Context) = fileAcceptReject(key, fileNumber, context, true)

  def fileAcceptReject(key: String, fileNumber: Integer, context: Context, accept: Boolean) {
    val antoxDB = new AntoxDB(context)
    val id = antoxDB.getFileId(key, fileNumber)
    if (id != -1) {
      val mFriend = antoxFriendList.getByKey(key)
      mFriend.foreach(friend => {
        try {
          jTox.fileSendControl(friend.getFriendnumber(), false, fileNumber, if (accept) {
            ToxFileControl.TOX_FILECONTROL_ACCEPT.ordinal()
          } else {
            ToxFileControl.TOX_FILECONTROL_KILL.ordinal()
          }, Array.ofDim[Byte](0))
          antoxDB.fileTransferStarted(key, fileNumber)
          fileStatusMap.put(id, if (accept) FileStatus.INPROGRESS else FileStatus.CANCELLED)
        } catch {
          case e: Exception => e.printStackTrace()
        }
      })
    }
    antoxDB.close()
    Reactive.updatedMessages.onNext(true)
  }

  def rejectFile(key: String, fileNumber: Int, context: Context) = fileAcceptReject(key, fileNumber, context, false)

  def receiveFileData(key: String,
                      fileNumber: Int,
                      data: Array[Byte],
                      context: Context) {
    val antoxDB = new AntoxDB(context)
    val id = antoxDB.getFileId(key, fileNumber)
    val state = Environment.getExternalStorageState
    if (Environment.MEDIA_MOUNTED == state) {
      if (!fileStreamMap.containsKey(id)) {
        val fileName = antoxDB.getFilePath(key, fileNumber)
        val dirfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
          Constants.DOWNLOAD_DIRECTORY)
        if (!dirfile.mkdirs()) {
          Log.e("acceptFile", "Directory not created")
        }
        val file = new File(dirfile.getPath, fileName)
        var output: FileOutputStream = null
        try {
          output = new FileOutputStream(file, true)
        } catch {
          case e: Exception => e.printStackTrace()
        }
        fileMap.put(id, file)
        fileStreamMap.put(id, output)
      }
      antoxDB.close()
      try {
        fileStreamMap.get(id).write(data)
      } catch {
        case e: Exception => e.printStackTrace()
      } finally {
        incrementProgress(id, data.length)
      }
      Log.d("ToxSingleton", "file size so far: " + fileMap.get(id).length + " final file size: " +
        fileSizeMap.get(id))
      if (fileMap.get(id).length == fileSizeMap.get(id)) {
        fileStreamMap.get(id).close()
        val mFriend = antoxFriendList.getByKey(key)
        mFriend.foreach(friend => {
          try {
            jTox.fileSendControl(friend.getFriendnumber(), false, fileNumber, ToxFileControl.TOX_FILECONTROL_FINISHED.ordinal(), Array.ofDim[Byte](0))
            fileFinished(key, fileNumber, context)
            Log.d("ToxSingleton", "receiveFileData finished receiving file")
          } catch {
            case e: Exception => e.printStackTrace()
          }
        })
      }
    }
  }

  def incrementProgress(id: Int, length: Int) {
    val idObject = id
    if (id != -1) {
      val time = System.currentTimeMillis()
      if (!progressMap.containsKey(idObject)) {
        progressMap.put(idObject, length)
        val a = new util.ArrayList[Tuple[Integer, Long]]()
        a.add(new Tuple[Integer, Long](length, time))
        progressHistoryMap.put(idObject, a)
      } else {
        val current = progressMap.get(idObject)
        progressMap.put(idObject, current + length)
        val a = progressHistoryMap.get(idObject)
        a.add(new Tuple[Integer, Long](current + length, time))
        progressHistoryMap.put(idObject, a)
      }
    }
    Reactive.updatedProgress.onNext(true)
  }

  def getProgressSinceXAgo(id: Int, ms: Int): Tuple[Integer, Long] = {
    if (progressHistoryMap.containsKey(id)) {
      val progressHistory = progressHistoryMap.get(id)
      if (progressHistory.size <= 1) {
        return null
      }
      val current = progressHistory.get(progressHistory.size - 1)
      var before: Tuple[Integer, Long] = null
      var timeDifference: Long = 0l
      val i = progressHistory.size - 2
      while (i >= 0) {
        before = progressHistory.get(i)
        timeDifference = current.y - before.y
        if (timeDifference > ms || i == 0) {
          return new Tuple[Integer, Long](current.x - before.x, System.currentTimeMillis() - before.y)
        }
        i
      }
    }
    null
  }

  def cancelFile(key: String, fileNumber: Int, context: Context) {
    Log.d("ToxSingleton", "cancelFile")
    val db = new AntoxDB(context)
    val id = db.getFileId(key, fileNumber)
    if (id != -1) {
      fileStatusMap.put(id, FileStatus.CANCELLED)
    }
    db.clearFileNumber(key, fileNumber)
    db.close()
    Reactive.updatedMessages.onNext(true)
  }

  def getProgress(id: Int): Int = {
    if (id != -1 && progressMap.containsKey(id)) {
      progressMap.get(id)
    } else {
      0
    }
  }

  def sendFileData(key: String,
                   fileNumber: Int,
                   startPosition: Int,
                   context: Context) {
    Observable[Boolean](subscriber => {
      val result = doSendFileData(key, fileNumber, startPosition, context)
      Log.d(TAG, "doSendFileData finished, result: " + result)
      val db = new AntoxDB(context)
      db.clearFileNumber(key, fileNumber)
      db.close()
      subscriber.onCompleted()
    }).subscribeOn(IOScheduler()).subscribe()
  }

  def doSendFileData(key: String,
                     fileNumber: Int,
                     startPosition: Int,
                     context: Context): Boolean = {
    var path = ""
    val antoxDB = new AntoxDB(context)
    path = antoxDB.getFilePath(key, fileNumber)
    val id = antoxDB.getFileId(key, fileNumber)
    antoxDB.close()
    if (id != -1) {
      fileStatusMap.put(id, FileStatus.INPROGRESS)
    }
    var result = -1
    if (path != "") {
      var chunkSize = 1
      val mFriend = antoxFriendList.getByKey(key)
      mFriend.foreach(friend => {
        try {
          chunkSize = jTox.fileDataSize(friend.getFriendnumber())
        } catch {
          case e: Exception => e.printStackTrace()
        }
      })
      val file = new File(path)
      val bytes = Array.ofDim[Byte](file.length.toInt)
      var buf: BufferedInputStream = null
      try {
        buf = new BufferedInputStream(new FileInputStream(file))
      } catch {
        case e: Exception => e.printStackTrace()
      }
      var i = startPosition
      if (buf != null) {
        i = startPosition
        while (i < bytes.length) {
          val data = Array.ofDim[Byte](chunkSize)
          try {
            buf.mark(chunkSize * 2)
          } catch {
            case e: Exception =>
              e.printStackTrace()
              //break
          }
          val mFriend = antoxFriendList.getByKey(key)
          mFriend.foreach(friend => {
            try {
              result = jTox.fileSendData(friend.getFriendnumber(), fileNumber, data)
            } catch {
              case e: Exception =>
                e.printStackTrace()
            }
          })
          if (!(fileStatusMap.containsKey(id) && fileStatusMap.get(id) == FileStatus.INPROGRESS)) {
            //break
          }
          if (result == -1) {
            Log.d("sendFileDataTask", "toxFileSendData failed")
            try {
              jTox.doTox()
            } catch {
              case e: Exception => e.printStackTrace()
            }
            SystemClock.sleep(50)
            i = i - chunkSize
            try {
              buf.reset()
            } catch {
              case e: Exception => e.printStackTrace()
            }
          }
          if (i > bytes.length) {
            i = bytes.length
          }
          setProgress(id, i)
          i = i + chunkSize
        }
        try {
          buf.close()
        } catch {
          case e: Exception => e.printStackTrace()
        }
      }
      if (result != -1 && fileStatusMap.get(id) == FileStatus.INPROGRESS) {
        val mFriend = antoxFriendList.getByKey(key)
        mFriend.foreach(friend => {
          try {
            Log.d("toxFileSendControl", "FINISHED")
            jTox.fileSendControl(friend.getFriendnumber(), true, fileNumber, ToxFileControl.TOX_FILECONTROL_FINISHED.ordinal(),
              Array.ofDim[Byte](0))
            fileFinished(key, fileNumber, context)
            return true
          } catch {
            case e: Exception => Log.d("toxFileSendControl error", e.toString)
          }
        })
      } else {
        return false
      }
    }
    false
  }

  def fileFinished(key: String, fileNumber: Int, context: Context) {
    Log.d("ToxSingleton", "fileFinished")
    val db = new AntoxDB(context)
    val id = db.getFileId(key, fileNumber)
    if (id != -1) {
      fileStatusMap.put(id, FileStatus.FINISHED)
      fileIds.remove(id)
    }
    db.fileFinished(key, fileNumber)
    db.close()
    Reactive.updatedMessages.onNext(true)
  }

  def setProgress(id: Int, progress: Int) {
    val idObject = id
    if (id != -1) {
      val time = System.currentTimeMillis()
      progressMap.put(idObject, progress)
      var a: util.ArrayList[Tuple[Integer, Long]] = null
      a = if (!progressHistoryMap.containsKey(idObject)) new util.ArrayList[Tuple[Integer, Long]]() else progressHistoryMap.get(idObject)
      a.add(new Tuple[Integer, Long](progress, time))
      progressHistoryMap.put(idObject, a)
      Reactive.updatedProgress.onNext(true)
    }
  }

  def updateFriendsList(ctx: Context) {
    try {
      val antoxDB = new AntoxDB(ctx)
      val friendList = antoxDB.getFriendList()
      antoxDB.close()
      Reactive.friendList.onNext(friendList)
    } catch {
      case e: Exception => Reactive.friendList.onError(e)
    }
  }

  def clearUselessNotifications(key: String) {
    if (key != null && key != "") {
      val mFriend = antoxFriendList.getByKey(key)
      mFriend.foreach(friend => {
        try {
          mNotificationManager.cancel(friend.getFriendnumber())
        } catch {
          case e: Exception => e.printStackTrace()
        }
      })
    }
  }

  def updateFriendRequests(ctx: Context) {
    try {
      val antoxDB = new AntoxDB(ctx)
      val friendRequest = antoxDB.getFriendRequestsList()
      antoxDB.close()
      Reactive.friendRequests.onNext(friendRequest.toArray(new Array[FriendRequest](friendRequest.size)))
    } catch {
      case e: Exception => Reactive.friendRequests.onError(e)
    }
  }

  def updateMessages(ctx: Context) {
    Reactive.updatedMessages.onNext(true)
    updateLastMessageMap(ctx)
    updateUnreadCountMap(ctx)
  }

  def updateLastMessageMap(ctx: Context) {
    try {
      val antoxDB = new AntoxDB(ctx)
      val map = antoxDB.getLastMessages()
      antoxDB.close()
      Reactive.lastMessages.onNext(map)
    } catch {
      case e: Exception => Reactive.lastMessages.onError(e)
    }
  }

  def updateUnreadCountMap(ctx: Context) {
    try {
      val antoxDB = new AntoxDB(ctx)
      val map = antoxDB.getUnreadCounts()
      antoxDB.close()
      Reactive.unreadCounts.onNext(map)
    } catch {
      case e: Exception => Reactive.unreadCounts.onError(e)
    }
  }

  def initTox(ctx: Context) {
    antoxFriendList = new AntoxFriendList()
    callbackHandler = new CallbackHandler(antoxFriendList)
    qrFile = ctx.getFileStreamPath("userkey_qr.png")
    dataFile = new ToxDataFile(ctx)
    val preferences = PreferenceManager.getDefaultSharedPreferences(ctx)
    val udpEnabled = preferences.getBoolean("enable_udp", false)
    val options = new ToxOptions(Options.ipv6Enabled, udpEnabled, Options.proxyEnabled)
    if (!dataFile.doesFileExist()) {
      try {
        jTox = new JTox(antoxFriendList, callbackHandler, options)
        dataFile.saveFile(jTox.save())
        val editor = preferences.edit()
        editor.putString("tox_id", jTox.getAddress)
        editor.commit()
      } catch {
        case e: ToxException => e.printStackTrace()
      }
    } else {
      try {
        jTox = new JTox(dataFile.loadFile(), antoxFriendList, callbackHandler, options)
        val editor = preferences.edit()
        editor.putString("tox_id", jTox.getAddress)
        editor.commit()
      } catch {
        case e: ToxException => e.printStackTrace()
      }
    }
    val db = new AntoxDB(ctx)
    db.setAllOffline()
    val friends = db.getFriendList()
    db.close()
    if (friends.size > 0) {
      for (friend <- friends) {
        try {
          jTox.confirmRequest(friend.friendKey)
        } catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
    val antoxOnMessageCallback = new AntoxOnMessageCallback(ctx)
    val antoxOnFriendRequestCallback = new AntoxOnFriendRequestCallback(ctx)
    val antoxOnActionCallback = new AntoxOnActionCallback(ctx)
    val antoxOnConnectionStatusCallback = new AntoxOnConnectionStatusCallback(ctx)
    val antoxOnNameChangeCallback = new AntoxOnNameChangeCallback(ctx)
    val antoxOnReadReceiptCallback = new AntoxOnReadReceiptCallback(ctx)
    val antoxOnStatusMessageCallback = new AntoxOnStatusMessageCallback(ctx)
    val antoxOnUserStatusCallback = new AntoxOnUserStatusCallback(ctx)
    val antoxOnTypingChangeCallback = new AntoxOnTypingChangeCallback(ctx)
    val antoxOnFileSendRequestCallback = new AntoxOnFileSendRequestCallback(ctx)
    val antoxOnFileControlCallback = new AntoxOnFileControlCallback(ctx)
    val antoxOnFileDataCallback = new AntoxOnFileDataCallback(ctx)
    val antoxOnAudioDataCallback = new AntoxOnAudioDataCallback(ctx)
    val antoxOnAvCallbackCallback = new AntoxOnAvCallbackCallback(ctx)
    val antoxOnVideoDataCallback = new AntoxOnVideoDataCallback(ctx)
    callbackHandler.registerOnMessageCallback(antoxOnMessageCallback)
    callbackHandler.registerOnFriendRequestCallback(antoxOnFriendRequestCallback)
    callbackHandler.registerOnActionCallback(antoxOnActionCallback)
    callbackHandler.registerOnConnectionStatusCallback(antoxOnConnectionStatusCallback)
    callbackHandler.registerOnNameChangeCallback(antoxOnNameChangeCallback)
    callbackHandler.registerOnReadReceiptCallback(antoxOnReadReceiptCallback)
    callbackHandler.registerOnStatusMessageCallback(antoxOnStatusMessageCallback)
    callbackHandler.registerOnUserStatusCallback(antoxOnUserStatusCallback)
    callbackHandler.registerOnTypingChangeCallback(antoxOnTypingChangeCallback)
    callbackHandler.registerOnFileSendRequestCallback(antoxOnFileSendRequestCallback)
    callbackHandler.registerOnFileControlCallback(antoxOnFileControlCallback)
    callbackHandler.registerOnFileDataCallback(antoxOnFileDataCallback)
    callbackHandler.registerOnAudioDataCallback(antoxOnAudioDataCallback)
    callbackHandler.registerOnAvCallbackCallback(antoxOnAvCallbackCallback)
    callbackHandler.registerOnVideoDataCallback(antoxOnVideoDataCallback)
    try {
      jTox.setName(preferences.getString("nickname", ""))
      jTox.setStatusMessage(preferences.getString("status_message", ""))
      var newStatus: ToxUserStatus = null
      val newStatusString = preferences.getString("status", "")
      newStatus = UserStatus.getToxUserStatusFromString(newStatusString)
      jTox.setUserStatus(newStatus)
    } catch {
      case e: ToxException =>
    }
    updateDhtNodes(ctx)
  }

  def updateDhtNodes(ctx: Context) {
    Log.d(TAG, "updateDhtNodes")
    val connMgr = ctx.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    val networkInfo = connMgr.getActiveNetworkInfo
    if (networkInfo != null && networkInfo.isConnected) {
      Log.d(TAG, "updateDhtNodes: connected")
      Observable[JSONObject](subscriber => {
        Log.d(TAG, "updateDhtNodes: in observable")
        object JsonReader {

          private def readAll(rd: Reader): String = {
            val sb = new StringBuilder()
            var cp: Int = rd.read()
            while (cp != -1) {
              sb.append(cp.toChar)
              cp = rd.read()
            }
            sb.toString()
          }

          def readJsonFromUrl(url: String): JSONObject = {
            val is = new URL(url).openStream()
            try {
              val rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))
              val jsonText = readAll(rd)
              val json = new JSONObject(jsonText)
              json
            } catch {
              case e: Exception =>
                Log.e(TAG, "JsonReader readJsonFromUrl error: " + e)
                new JSONObject()
            } finally {
              is.close()
            }
          }
        }
        try {
          Log.d(TAG, "updateDhtNodes: about to readJsonFromUrl")
          val json = JsonReader.readJsonFromUrl("http://jfk.us.cdn.libtoxcore.so/elizabeth_remote/config/Nodefile.json")
          subscriber.onNext(json)
          subscriber.onCompleted()
        } catch {
          case e: Exception =>
            Log.e(TAG, "update dht nodes error: " + e)
            subscriber.onError(e)
        }
      }).map(json => {
        Log.d(TAG, json.toString)
        var dhtNodes: Array[DhtNode] = Array()
        val serverArray = json.getJSONArray("servers")
        for (i <- 0 until serverArray.length) {
          val jsonObject = serverArray.getJSONObject(i)
          dhtNodes +:= new DhtNode(
            jsonObject.getString("owner"),
            jsonObject.getString("ipv6"),
            jsonObject.getString("ipv4"),
            jsonObject.getString("pubkey"),
            jsonObject.getInt("port"))
        }
        dhtNodes
      }).subscribeOn(IOScheduler())
        .observeOn(AndroidMainThreadScheduler())
        .subscribe(nodes => {
        dhtNodes = nodes
        Log.d(TAG, "Trying to bootstrap")
        try {
          for (i <- 0 until nodes.size) {
            jTox.bootstrap(nodes(i).ipv4, nodes(i).port, nodes(i).key)
          }
        } catch {
          case e: Exception =>
        }
        Log.d(TAG, "Successfully bootstrapped")
      }, error => {
        Log.e(TAG, "Failed bootstrapping " + error)
      })
    }
  }

  object FileStatus extends Enumeration {
    type FileStatus = Value
    val REQUESTSENT, CANCELLED, INPROGRESS, FINISHED, PAUSED = Value
  }

}

