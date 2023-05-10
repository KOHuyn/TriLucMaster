package com.mobileplus.dummytriluc.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.model.ItemChatRoom
import com.mobileplus.dummytriluc.data.model.NotificationObjService
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import org.koin.android.ext.android.inject

class MessageService : FirebaseMessagingService() {

    val TAG = MessageService::class.java.canonicalName

    companion object {
        const val TYPE_MESSAGE = "MESSAGE"
        const val TYPE_CHALLENGES = "CHALLENGES"
        const val TYPE_NEWS = "NEWS"
        const val TYPE_DETAIL = "DETAIL"
        const val TYPE_RECEIVE_MASTER = "BAI_SU"
        const val TYPE_ACCEPT_BAI_SU = "ACCEPT_BAI_SU"
        const val TYPE_ASSIGN = "ASSIGN"
    }

    private val notification by inject<TriLucNotification>()
    private val gson by inject<Gson>()

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        logErr("new token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val notifi = NotificationObjService()
        logErr(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            logErr(TAG, "Message data payload: ${remoteMessage.data}")
            remoteMessage.data[ApiConstants.ID]?.let { notifi.id = it.toInt() }
            remoteMessage.data[ApiConstants.TYPE]?.let { notifi.type = it }
            if (remoteMessage.data[ApiConstants.TYPE] == TYPE_MESSAGE) {
                remoteMessage.data[ApiConstants.DATA_JSON]?.let {
                    try {
                        notifi.itemChat = gson.fromJson(it, ItemChatRoom::class.java)
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            logErr(TAG, "Message Notification Body: ${it.body}")
            notifi.title = remoteMessage.notification?.title ?: ""
            notifi.content = remoteMessage.notification?.body ?: ""
        }

        notification.showNotificationTriLuc(notifi, gson)

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}