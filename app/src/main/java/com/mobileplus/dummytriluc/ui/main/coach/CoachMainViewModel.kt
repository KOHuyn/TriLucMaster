package com.mobileplus.dummytriluc.ui.main.coach

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.data.model.ItemPracticeItemContent
import com.mobileplus.dummytriluc.data.model.ItemRankingDisciple
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.response.StatisticalCoachResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class CoachMainViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxArrayMyExercise: PublishSubject<List<ItemCoachPractice>> =
        PublishSubject.create()

    val rxStatisticalCoach: PublishSubject<List<StatisticalCoachResponse>> = PublishSubject.create()

    val rxRankingDisciple: PublishSubject<Pair<List<ItemRankingDisciple>, Page?>> =
        PublishSubject.create()

    val rxDeleteAt: PublishSubject<Pair<Int, Boolean>> = PublishSubject.create()

    fun getListRankingDisciple(page: Int? = null): Disposable {
        return dataManager.getDiscipleRanking(page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        rxRankingDisciple.onNext(Pair(emptyList(), null))
                    } else {
                        try {
                            val items =
                                gson.toList<ItemRankingDisciple>(response.dataObject().dataArray())
                            rxRankingDisciple.onNext(Pair(items, response.dataObject().page()))
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                }
                if (response.isDataEmpty()) {
                    rxRankingDisciple.onNext(Pair(emptyList(), null))
                }
                logErr("getListRankingDisciple: $response")
            }, {
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getStatistical(): Disposable {
        return dataManager.getStatisticalCoach()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    val items = gson.toList<StatisticalCoachResponse>(response.dataArray())
                    rxStatisticalCoach.onNext(items)
                } else {
                    rxStatisticalCoach.onNext(emptyList())
                }
                logErr("getStatistical:$response")
            }, {
                logErr(it.message ?: "null")
                rxStatisticalCoach.onNext(emptyList())
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
                        rxArrayMyExercise.onNext(emptyList())
                    } else {
                        try {
                            val items =
                                gson.toList<ItemCoachPractice>(
                                    response.dataObject().dataArray()
                                )
                            rxArrayMyExercise.onNext(items.toMutableList())
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                }
                if (response.isDataEmpty()) {
                    rxArrayMyExercise.onNext(emptyList())
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
}