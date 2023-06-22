package com.mobileplus.dummytriluc.ui.main.practice.detail

import bolts.Bolts
import com.core.BaseViewModel
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.*
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.ChatSendRequest
import com.mobileplus.dummytriluc.data.response.DetailPracticeResponse
import com.mobileplus.dummytriluc.data.response.PracticeAvgResponse
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject

class PracticeDetailViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxDetailResponse: PublishSubject<DetailPracticeResponse> = PublishSubject.create()
    val rxStatusSendMessage: PublishSubject<Pair<Long, ChatSendStatus>> = PublishSubject.create()

    val rxStatusReceiveMaster: PublishSubject<Boolean> = PublishSubject.create()
    val rxStatusUnReceiveMaster: PublishSubject<Boolean> = PublishSubject.create()

    val rxChats: PublishSubject<List<ItemChat>> = PublishSubject.create()
    val rxResultPractices: PublishSubject<Pair<List<ItemPracticeDetailMain>, Page?>> =
        PublishSubject.create()
    val userInfo = dataManager.getUserInfo()

    val rxLoadingResultPractice: PublishSubject<Boolean> = PublishSubject.create()
    val rxLoadingChat: PublishSubject<Boolean> = PublishSubject.create()

    fun getResultPractices(idPractice: Int, page: Int? = null): Disposable {
        rxLoadingResultPractice.onNext(true)
        return dataManager.getResultPractices(idPractice, page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxLoadingResultPractice.onNext(false)
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        rxResultPractices.onNext(Pair(emptyList(), null))
                    } else {
                        try {
                            val items =
                                gson.toList<ItemPracticeDetailMain>(
                                    response.dataObject().dataArray()
                                )
                            rxResultPractices.onNext(Pair(items, response.dataObject().page()))
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                }
                if (response.isDataEmpty()) {
                    rxResultPractices.onNext(Pair(emptyList(), null))
                }
                logErr(response.toString())
            }, {
                rxLoadingResultPractice.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getDataDetailPractice(id: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.getLessonPracticeDetail(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                try {
                    if (response.isSuccess()) {
                        val dataResponse =
                            gson.fromJson(response.dataObject(), DetailPracticeResponse::class.java)
                        rxDetailResponse.onNext(dataResponse)
                        logErr("getDataDetailPractice:${response.dataObject()}")
                    } else {
                        rxMessage.onNext(response.message())
                    }
                } catch (e: Exception) {
                    rxMessage.onNext(loadStringRes(R.string.data_practice_not_available))
                    e.logErr()
                }
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
                it.printStackTrace()
            })
    }

    fun requestMaster(msg: String, masterId: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.postTrainerRequest(msg, masterId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                rxStatusReceiveMaster.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                rxStatusReceiveMaster.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun unReceiverMaster(masterId: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.postTrainerRequestRemove(masterId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                rxStatusUnReceiveMaster.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                rxStatusUnReceiveMaster.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getChatRoom(
        roomId: Int,
        keyId: Int? = null,
        type: String? = ApiConstants.DOWN
    ): Disposable {
        rxLoadingChat.onNext(true)
        return dataManager.getChatRoom(keyId, type, roomId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxLoadingChat.onNext(false)
                logErr("getChatRoom:$response")
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
                rxLoadingChat.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun sendChat(chat: ChatSendRequest, idChatLocal: Long): Disposable {
        rxStatusSendMessage.onNext(Pair(idChatLocal, ChatSendStatus.IS_LOADING))
        return dataManager.postChat(chat)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                logErr("sendChat:$response")
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

    private val hashmapAvg = hashMapOf<Int, Pair<Int, Int>>()
    fun getAvgPractice(practiceId: Int, callback: (avgPower: Int, avgHit: Int) -> Unit) {
        if (hashmapAvg.contains(practiceId)) {
            val (power, hit) = hashmapAvg[practiceId]!!
            callback(power, hit)
            return
        }
        if (practiceId == AppConstants.INTEGER_DEFAULT) {
            callback(50, 20)
            return
        }
        dataManager.getPracticeAvg(practiceId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    val avgResponse = gson.fromJsonSafe<PracticeAvgResponse>(response.dataObject())
                    val avgPower = avgResponse?.avgPower
                    val avgHit = avgResponse?.avgHit
                    if (avgPower != null && avgHit != null) {
                        hashmapAvg[practiceId] = avgPower to avgHit
                    }
                    callback(avgPower ?: 50, avgHit ?: 20)
                } else {
                    callback(50, 20)
                }
            }, {
                callback(50, 20)
            }).addTo(disposable)
    }

}