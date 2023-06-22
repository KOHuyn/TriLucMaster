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
}