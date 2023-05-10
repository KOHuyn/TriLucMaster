package com.mobileplus.dummytriluc.ui.main.practice.folder

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemPracticeFolder
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.response.DetailPracticeFolderResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 3/11/2021
 */
class PracticeFolderViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxDetailResponse: PublishSubject<DetailPracticeFolderResponse> = PublishSubject.create()
    val rxItemsInFolder: PublishSubject<Pair<List<ItemPracticeFolder>, Page?>> =
        PublishSubject.create()
    val rxLoadMore: PublishSubject<Boolean> = PublishSubject.create()

    val rxStatusReceiveMaster: PublishSubject<Boolean> = PublishSubject.create()
    val rxStatusUnReceiveMaster: PublishSubject<Boolean> = PublishSubject.create()

    fun getDetailPracticeFolder(id: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.getLessonPracticeDetail(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    rxDetailResponse.onNext(
                        gson.fromJson(
                            response.dataObject(),
                            DetailPracticeFolderResponse::class.java
                        )
                    )
                } else {
                    rxMessage.onNext(response.message())
                }
                logErr("getDetailPracticeFolder:$response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getListItemFolder(id: Int, page: Int? = null): Disposable {
        rxLoadMore.onNext(true)
        return dataManager.getListPracticeFolder(id, page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxLoadMore.onNext(false)
                if (response.isSuccess()) {
                    if (response.dataObject().isEmptyArray()) {
                        rxItemsInFolder.onNext(Pair(emptyList(), null))
                    } else {
                        try {
                            val items =
                                gson.toList<ItemPracticeFolder>(response.dataObject().dataArray())
                            rxItemsInFolder.onNext(Pair(items, response.dataObject().page()))
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                }
                if (response.isDataEmpty()) {
                    rxItemsInFolder.onNext(Pair(emptyList(), null))
                }
            }, {
                rxLoadMore.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
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

}