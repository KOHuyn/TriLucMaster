package com.mobileplus.dummytriluc.ui.main.coach.group.detail.message

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ChatSendStatus
import com.mobileplus.dummytriluc.data.model.ItemChat
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.ChatSendRequest
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 3/12/2021
 */
class CoachGroupDetailMessageViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider, private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxStatusSendMessage: PublishSubject<Pair<Long, ChatSendStatus>> = PublishSubject.create()
    val rxChats: PublishSubject<List<ItemChat>> = PublishSubject.create()
    val userInfo = dataManager.getUserInfo()

    fun getChatRoom(
        roomId: Int,
        keyId: Int? = null,
        type: String? = ApiConstants.DOWN
    ): Disposable {
        isLoading.onNext(true)
        return dataManager.getChatRoom(keyId, type, roomId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                logErr("$response")
                if (response.isSuccess()) {
                    try {
                        if (response.isEmptyArray()) {
                            rxChats.onNext(emptyList())
                        } else {
                            val dataChat = gson.toList<ItemChat>(response.dataArray())
                            val idProfile = dataManager.getUserInfo()?.id ?: 0
                            dataChat.forEach { it.isSend = it.userId == idProfile }
                            rxChats.onNext(dataChat.toMutableList())
                        }
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun sendChat(chat: ChatSendRequest, idChatLocal: Long): Disposable {
        rxStatusSendMessage.onNext(Pair(idChatLocal, ChatSendStatus.IS_LOADING))
        return dataManager.postChat(chat)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                logErr("$response")
                if (response.isSuccess()) {
                    rxStatusSendMessage.onNext(Pair(idChatLocal, ChatSendStatus.SEND_SUCCESS))
                } else {
                    rxStatusSendMessage.onNext(Pair(idChatLocal, ChatSendStatus.SEND_ERROR))
                }
            }, {
                rxStatusSendMessage.onNext(Pair(idChatLocal, ChatSendStatus.SEND_ERROR))
                it.logErr()
            })
    }
}