package com.mobileplus.dummytriluc.ui.video.record

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.response.PracticeAvgResponse
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 1/27/2021
 */
class VideoRecordViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    fun startChallenge(id: Int): Disposable {
        return dataManager.startChallenge(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({
                logErr(it.toString())
            }, {
                it.logErr()
            })
    }


    val rxAvgResponse: PublishSubject<Pair<Boolean, PracticeAvgResponse?>> = PublishSubject.create()

    fun getAvgPractice(practiceId: Int) {
        if (practiceId == AppConstants.INTEGER_DEFAULT) {
            rxAvgResponse.onNext(false to null)
            return
        }
        dataManager.getPracticeAvg(practiceId)
            .doOnSubscribe { isLoading.onNext(true) }
            .doFinally { isLoading.onNext(false) }
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    val avgResponse = gson.fromJsonSafe<PracticeAvgResponse>(response.dataObject())
                    rxAvgResponse.onNext(true to avgResponse)
                } else {
                    rxAvgResponse.onNext(false to null)
                }
            }, {
                rxAvgResponse.onNext(false to null)
            }).addTo(disposable)
    }
}