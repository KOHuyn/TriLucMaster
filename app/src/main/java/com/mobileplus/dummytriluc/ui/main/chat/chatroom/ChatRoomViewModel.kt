package com.mobileplus.dummytriluc.ui.main.chat.chatroom

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemChatRoom
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.lang.Exception


/**
 * Created by KO Huyn on 1/12/2021.
 */
class ChatRoomViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxListChatRoom: PublishSubject<Pair<List<ItemChatRoom>, Page?>> = PublishSubject.create()

    fun getListChatRoom(page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getListChatRoom(page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    try {
                        if (response.isEmptyArray()) {
                            rxListChatRoom.onNext(Pair(emptyList(), null))
                        } else {
                            val listChatRoom: List<ItemChatRoom> =
                                gson.toList(response.dataObject().dataArray())
                            rxListChatRoom.onNext(Pair(listChatRoom, response.dataObject().page()))
                        }
                    } catch (e: Exception) {
                        e.logErr()
                    }
                    logErr(response.toString())
                } else {
                    rxMessage.onNext(response.message())
                }
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

}