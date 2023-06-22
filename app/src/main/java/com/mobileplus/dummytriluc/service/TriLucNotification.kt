package com.mobileplus.dummytriluc.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BleConstants
import com.mobileplus.dummytriluc.bluetooth.ble.BluetoothLeService
import com.mobileplus.dummytriluc.data.model.NotificationObjService
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import org.koin.core.KoinComponent

/**
 * Created by KOHuyn on 2/19/2021
 */
class TriLucNotification constructor(private val context: Context) : KoinComponent {
    companion object {
        const val NOTIFICATION_ARG = "NOTIFICATION_ARG"
        const val NOTIFICATION_REQUEST_CODE = 1
        private const val FOREGROUND_CHANNEL_ID = "foreground_channel_id"
        val FLAG_NOTIFICATION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    var notificationManager: NotificationManager? = null

    @SuppressLint("InvalidWakeLockTag")
    fun showNotificationTriLuc(notifi: NotificationObjService, gson: Gson) {
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = notifi.type
        val bundle = Bundle().apply {
            putString(NOTIFICATION_ARG, gson.toJson(notifi))
        }
        val channelName = when (channelId) {
            MessageService.TYPE_MESSAGE -> loadStringRes(R.string.message)
            MessageService.TYPE_CHALLENGES -> loadStringRes(R.string.challenge)
            MessageService.TYPE_NEWS -> loadStringRes(R.string.news)
            MessageService.TYPE_DETAIL -> loadStringRes(R.string.see_detail)
            else -> "Unknown"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager!!.createNotificationChannel(channel)
        }
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_logo)
            .setChannelId(channelId)
            .setContentTitle(notifi.title)
            .setContentText(notifi.content)
            .setPriority(NotificationManagerCompat.IMPORTANCE_MAX)
            .setDefaults(Notification.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 100, 200, 300))
            .setShowWhen(true)
            .setColor(ContextCompat.getColor(context, R.color.clr_primary))
            .setAutoCancel(true)
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtras(bundle)

        val pIntent = PendingIntent.getActivity(
            context,
            notifi.id,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
        )
        builder.setContentIntent(pIntent)

        notificationManager!!.notify(notifi.id, builder.build())

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn =
            powerManager.isInteractive
        if (isScreenOn) {
            logErr("Screen On")
        } else {
            val wl = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "MH24_SCREENLOCK"
            )
            wl.acquire(2000)
            val wlCpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MH24_SCREENLOCK")
            wlCpu.acquire(2000)
        }
    }

    fun updateNotificationBle(state: Int, titleBle: String) {
        notificationManager?.notify(
            BleConstants.NOTIFICATION_ID_FOREGROUND_SERVICE,
            prepareNotificationBle(state, titleBle)
        )
    }

    fun updateNotificationLang() {
        updateNotificationBle(_lastStateNotification.first, _lastStateNotification.second)
    }

    private var _lastStateNotification: Pair<Int, String> =
        Pair(BleConstants.STATE_SERVICE.PREPARE, "")

    fun prepareNotificationBle(
        state: Int = BleConstants.STATE_SERVICE.PREPARE,
        titleBle: String
    ): Notification {

        _lastStateNotification = Pair(state, titleBle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && notificationManager!!.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null
        ) { // The user-visible name of the channel.
            val name: CharSequence = "Bluetooth"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance)
            mChannel.setSound(null, null)
            mChannel.enableVibration(false)
            notificationManager?.createNotificationChannel(mChannel)
        }
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.action = BleConstants.ACTION.MAIN_ACTION
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                FLAG_NOTIFICATION
            )

        val lTurnOffIntent = Intent(context, BluetoothLeService::class.java)
        lTurnOffIntent.action = BleConstants.ACTION.TURN_OFF_BLE_ACTION
        lTurnOffIntent.putExtra(BleConstants.ARG_CLICK_FROM_NOTIFICATION, true)
        val lPendingTurnOffIntent =
            PendingIntent.getService(context, 0, lTurnOffIntent, FLAG_NOTIFICATION)

        val lRequestConnectIntent = Intent(context, BluetoothLeService::class.java)
        lRequestConnectIntent.action = BleConstants.ACTION.REQUEST_CONNECT_BLE_ACTION
        lRequestConnectIntent.putExtra(BleConstants.ARG_CLICK_FROM_NOTIFICATION, true)
        val lPendingConnectingIntent =
            PendingIntent.getService(
                context,
                0,
                lRequestConnectIntent,
                FLAG_NOTIFICATION
            )

        val lRemoteViews = RemoteViews(context.packageName, R.layout.notification_on_off_ble)
        when (state) {
            BleConstants.STATE_SERVICE.OFF -> {
                lRemoteViews.setViewVisibility(R.id.btnOffBleService, View.VISIBLE)
                lRemoteViews.setViewVisibility(R.id.btnOnBleService, View.GONE)
                lRemoteViews.setViewVisibility(R.id.btnConnectingBleService, View.GONE)
                lRemoteViews.setTextViewText(R.id.txtNameTriLucBle, titleBle)
                lRemoteViews.setOnClickPendingIntent(
                    R.id.btnOffBleService,
                    lPendingConnectingIntent
                )
                lRemoteViews.setTextViewText(
                    R.id.btnOffBleService,
                    loadStringRes(R.string.connect)
                )
            }
            BleConstants.STATE_SERVICE.ON -> {
                lRemoteViews.setViewVisibility(R.id.btnOffBleService, View.GONE)
                lRemoteViews.setViewVisibility(R.id.btnConnectingBleService, View.GONE)
                lRemoteViews.setViewVisibility(R.id.btnOnBleService, View.VISIBLE)
                lRemoteViews.setTextViewText(
                    R.id.txtNameTriLucBle,
                    titleBle
                )
                lRemoteViews.setOnClickPendingIntent(
                    R.id.btnOnBleService,
                    lPendingTurnOffIntent
                )
                lRemoteViews.setTextViewText(
                    R.id.btnOnBleService,
                    loadStringRes(R.string.disconnect)
                )
            }

            BleConstants.STATE_SERVICE.CONNECTING -> {
                lRemoteViews.setViewVisibility(R.id.btnConnectingBleService, View.VISIBLE)
                lRemoteViews.setViewVisibility(R.id.btnOnBleService, View.GONE)
                lRemoteViews.setViewVisibility(R.id.btnOffBleService, View.GONE)
                lRemoteViews.setTextViewText(
                    R.id.btnConnectingBleService,
                    loadStringRes(R.string.connecting)
                )
                lRemoteViews.setTextViewText(
                    R.id.txtNameTriLucBle,
                    titleBle
                )
            }

            BleConstants.STATE_SERVICE.REQUEST_CONNECT -> {
                lRemoteViews.setViewVisibility(R.id.btnConnectingBleService, View.VISIBLE)
                lRemoteViews.setViewVisibility(R.id.btnOnBleService, View.GONE)
                lRemoteViews.setViewVisibility(R.id.btnOffBleService, View.GONE)
                lRemoteViews.setTextViewText(
                    R.id.btnConnectingBleService,
                    loadStringRes(R.string.searching)
                )
            }

            BleConstants.STATE_SERVICE.PREPARE -> {
                lRemoteViews.setViewVisibility(R.id.btnOnBleService, View.GONE)
                lRemoteViews.setViewVisibility(R.id.btnOffBleService, View.VISIBLE)
                lRemoteViews.setViewVisibility(R.id.btnConnectingBleService, View.GONE)

                lRemoteViews.setOnClickPendingIntent(
                    R.id.btnOffBleService,
                    lPendingConnectingIntent
                )
                lRemoteViews.setTextViewText(R.id.btnOffBleService, loadStringRes(R.string.connect))
            }
        }
        val lNotificationBuilder: NotificationCompat.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            } else {
                NotificationCompat.Builder(context)
            }
        lNotificationBuilder
            .setCustomContentView(lRemoteViews)
            .setSmallIcon(R.drawable.ic_logo)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        lNotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        return lNotificationBuilder.build()
    }

}