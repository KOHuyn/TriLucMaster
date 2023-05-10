package com.mobileplus.dummytriluc.ui.utils

import android.content.Context
import android.content.res.Resources
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Environment
import android.os.PowerManager
import android.util.Log
import android.util.TypedValue
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventReceiveVideoUrl
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.postNormal
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.math.floor


object VideoUtils {
    private const val MAIN_STORAGE_APP = "tri_luc_storage";
    private var urlDownload = "";
    private var fileN = "";
    var videoUrl = "";

    /**
     * checking internet status
     */
    fun isConnectingToInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting
    }

    fun setUrlDownload(url: String?) {
        if (url != null) {
            urlDownload = url
        }
    }


    public class VideoAsyncTask : AsyncTask<Int, String, String?> {
        private var context: Context? = null
        private val mWakeLock: PowerManager.WakeLock? = null

        constructor(context: Context?) {
            this.context = context
        }

        fun getVideoUrl(): String {
            return videoUrl;
        }

        private var resp: String? = null


        override fun onPreExecute() {
            val filePath = Environment.getExternalStorageDirectory()
                .absolutePath + '/' + MAIN_STORAGE_APP;
            val dirSource = File(filePath)
            if (dirSource.isDirectory && !dirSource.list().isNullOrEmpty()) {
                val children: Array<String> = dirSource.list()!!
                for (i in children.indices) {
                    File(dirSource, children[i]).delete()
                }
            }
        }

        override fun doInBackground(vararg params: Int?): String? {
            var input: InputStream? = null
            var output: OutputStream? = null
            var connection: HttpURLConnection? = null
            try {
                val url: URL = URL(urlDownload)
                connection = url.openConnection() as HttpURLConnection
                connection.connect()
                if (connection.responseCode !== HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.responseCode
                        .toString() + " " + connection.responseMessage
                }
                val fileLength: Int = connection.contentLength
                input = connection.inputStream

                fileN = "vid_" + UUID.randomUUID().toString().substring(0, 10)
                    .toString() + ".mp4";

                videoUrl = Environment.getExternalStorageDirectory()
                    .absolutePath + '/' + MAIN_STORAGE_APP + '/' + fileN;
                val filePath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + '/' + MAIN_STORAGE_APP;

                logErr("Video URL IS: $videoUrl")

                val filename = File(
                    filePath, fileN
                )

                val folder = File(
                    filePath
                )

                if (!folder.exists()) {
                    folder.mkdirs()
                }

                output = FileOutputStream(filename)
                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    if (isCancelled) {
                        input.close()
                        return null
                    }
                    total += count.toLong()

                    output.write(data, 0, count)
                }
            } catch (e: Exception) {
                videoUrl = "";
                return e.toString()
            } finally {
                try {
                    if (output != null) output.close()
                    if (input != null) input.close()
                } catch (ignored: IOException) {

                }

                if (connection != null)
                    connection.disconnect()
            }

            return null;
        }


        override fun onPostExecute(result: String?) {
            if (result != null)
                logErr("Download error: $result")

            MediaScannerConnection.scanFile(
                context, arrayOf(
                    Environment.getExternalStorageDirectory().absolutePath +
                            MAIN_STORAGE_APP + '/' + fileN
                ), null
            ) { newpath, newuri ->
                Log.i("ExternalStorage", "Scanned $newpath:")
                Log.i("ExternalStorage", "-> uri=$newuri")
            }

            postNormal(EventReceiveVideoUrl(videoUrl))

        }

        override fun onProgressUpdate(vararg text: String?) {

        }
    }

    fun convertPxToDp(context: Context, px: Float): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }

    fun convertDpToPx(context: Context, dip: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            context.resources.displayMetrics
        )
    }


    fun convertDate(time: Int): String {
        if (time > 3600) return "";
        var minutes = floor((time / 60).toDouble()).toInt();
        var minuteString = "00";
        var secondString = "00";
        var second = time;

        if (time >= 60) {
            second = (time - (minutes * 60));
        }

        if (minutes >= 10) {
            minuteString = minutes.toString();
        } else {
            minuteString = "0$minutes";
        }

        if (second >= 10) {
            secondString = second.toString();
        } else {
            secondString = "0$second";
        }

        return "$minuteString:$secondString";
    }

    fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels;
    }


    fun getTime(secondInTen: Int): String {

        val second: Int = secondInTen / 10

        val minutes = second / 60

        val minuteString: String = if (minutes < 10) {
            "0$minutes"
        } else {
            minutes.toString()
        }

        val secondString = if (second < 10) {
            "0$second"
        } else {
            (second - (minutes * 60)).toString()
        }
        val tick = secondInTen % 10
        val tickString = if (tick < 10) {
            "0$tick"
        } else tick.toString()

        return "$minuteString:$secondString\"$tickString"
    }


    fun showTimeByIdAndIndex(id: Int, index: Int): String {
        val minutes = floor((id / 60).toDouble()).toInt();
        val minuteString: String
        val secondString: String
        var second = id

        if (id >= 60) {
            second = (id - (minutes * 60))
        }

        minuteString = if (minutes >= 10) {
            minutes.toString();
        } else {
            "0$minutes";
        }

        secondString = if (second >= 10) {
            second.toString();
        } else {
            "0$second";
        }

        return "$minuteString:$secondString\"$index";
    }

    fun showMinute(id: Int): String {
        var minutes = floor((id / 60).toDouble()).toInt();
        var minuteString = "00";
        if (minutes >= 10) {
            minuteString = minutes.toString();
        } else {
            minuteString = "0$minutes";
        }

        return minuteString;
    }

    fun showSecond(id: Int): String {
        var minutes = floor((id / 60).toDouble()).toInt();
        var secondString = "00"
        var second = id

        if (id >= 60) {
            second = (id - (minutes * 60));
        }

        if (second >= 10) {
            secondString = second.toString();
        } else {
            secondString = "0$second";
        }

        return secondString;
    }
}