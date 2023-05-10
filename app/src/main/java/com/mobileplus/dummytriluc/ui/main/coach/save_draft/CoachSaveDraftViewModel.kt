package com.mobileplus.dummytriluc.ui.main.coach.save_draft

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.ui.utils.extensions.getErrorMsg
import com.mobileplus.dummytriluc.ui.utils.extensions.isSuccess
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.extensions.message
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 3/25/2021
 */
class CoachSaveDraftViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxUpdateSuccess: PublishSubject<Boolean> = PublishSubject.create()

    fun getDetailDraft(id: Int): Disposable {
        return dataManager.getTrainerDraftDetail(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->

            }, {
                rxMessage.onNext(it.getErrorMsg())
                it.logErr()
            })
    }

    fun updateDraft(id: Int, title: String, folderId: Int = 0): Disposable {
        isLoading.onNext(true)
        return dataManager.updateDraft(id, title, folderId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                logErr("updateDraft(id: $id, title: $title, folderId: $folderId):\n $response")
                rxUpdateSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                rxUpdateSuccess.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}