import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.app.Activity
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import com.example.heroadmin.MyBroadcastReceiver
import com.example.heroadmin.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AppUpdater(private val context: Context) {

    private val client = OkHttpClient()
    private val apkName = "heroadmin"
    private val appAddress = "https://www.talltales.nu/$apkName.apk"
    private val appVersionAddress = "https://www.talltales.nu/appversion.txt"

    fun checkForUpdates() {
        val request = Request.Builder()
            .url(appVersionAddress)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val latestVersionCode = it.body!!.string().toInt()

                    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_META_DATA)
                    }

                    val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode.toInt() // getLongVersionCode() for Java
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.versionCode // getVersionCode() for Java
                    }

                    if (latestVersionCode > currentVersionCode) {
                        downloadAndInstallApk()
                    }
                }
            }

        })
    }

    private fun downloadAndInstallApk() {
        val request = DownloadManager.Request(Uri.parse(appAddress))
            .setTitle("Update available")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(context, null, apkName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                    val fileUri = downloadManager.getUriForDownloadedFile(downloadId)

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(context, MyBroadcastReceiver::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    val sessionParams = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                    val sessionId = context.packageManager.packageInstaller.createSession(sessionParams)
                    val session = context.packageManager.packageInstaller.openSession(sessionId)
                    val outputStream = session.openWrite(context.getString(R.string.app_name), 0, -1)
                    val inputStream = context.contentResolver.openInputStream(fileUri)

                    inputStream?.copyTo(outputStream)
                    session.fsync(outputStream)
                    outputStream.close()
                    inputStream?.close()

                    session.commit(pendingIntent.intentSender)
                    session.close()
                }
            }
        }

        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}
