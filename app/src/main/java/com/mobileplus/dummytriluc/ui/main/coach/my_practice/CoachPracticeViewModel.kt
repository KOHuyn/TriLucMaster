package com.mobileplus.dummytriluc.ui.main.coach.my_practice

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemPracticeItemContent
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject


/**
 * Created by KO Huyn on 12/17/2020.
 */
class CoachPracticeViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxListCoachMore: PublishSubject<Pair<List<ItemPracticeItemContent>, Page?>> =
        PublishSubject.create()

    val rxDeleteAt: PublishSubject<Pair<Int, Boolean>> = PublishSubject.create()

    fun getGuestById(idGuest: Int, page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getPracticeGuest(idGuest, page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        rxListCoachMore.onNext(Pair(emptyList(), null))
                    } else {
                        try {
                            val items = gson.toList<ItemPracticeItemContent>(
                                response.dataObject().dataArray()
                            )
                            rxListCoachMore.onNext(
                                Pair(
                                    items.toMutableList(), response.dataObject().page()
                                )
                            )
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                }
                if (response.isDataEmpty()) {
                    rxListCoachMore.onNext(Pair(emptyList(), null))
                }
                logErr("getTrainerList: $response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun deletePractice(id: Int, pos: Int): Disposable {
        return dataManager.deletePractice(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxMessage.onNext(response.message())
                rxDeleteAt.onNext(Pair(pos, response.isSuccess()))
            }, {
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getTrainerList(page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getTrainerList(page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        rxListCoachMore.onNext(Pair(emptyList(), null))
                    } else {
                        try {
                            val items = gson.toList<ItemPracticeItemContent>(
                                response.dataObject().dataArray()
                            )
                            rxListCoachMore.onNext(
                                Pair(
                                    items.toMutableList(), response.dataObject().page()
                                )
                            )
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                }
                if (response.isDataEmpty()) {
                    rxListCoachMore.onNext(Pair(emptyList(), null))
                }
                logErr("getTrainerList: $response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}