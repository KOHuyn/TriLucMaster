package com.mobileplus.dummytriluc.ui.main.coach.assign_exercise

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.data.model.ItemPracticeItemContent
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.request.CoachAssignExerciseRequest
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 3/15/2021
 */
class CoachAssignExerciseViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxItems: PublishSubject<Pair<List<ItemCoachPractice>, Page?>> = PublishSubject.create()
    val rxSendSuccess: PublishSubject<Boolean> = PublishSubject.create()
    val rxSendLoading: PublishSubject<Boolean> = PublishSubject.create()
    fun getTrainerList(page: Int? = null, idFolder: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.run {
            if (idFolder == null) getTrainerList(page)
            else getListPracticeFolder(idFolder, page)
        }
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                when {
                    response.isSuccess() -> {
                        try {
                            val items = gson.toList<ItemCoachPractice>(
                                response.dataObject().dataArray()
                            )
                            rxItems.onNext(
                                Pair(
                                    items.toMutableList(), response.dataObject().page()
                                )
                            )
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                    response.isDataEmpty() -> {
                        rxItems.onNext(Pair(emptyList(), null))
                    }
                    else -> {
                        rxMessage.onNext(response.message())
                    }
                }
                logErr("getTrainerList: $response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun sendAssignExercise(request: CoachAssignExerciseRequest): Disposable {
        rxSendLoading.onNext(true)
        return dataManager.postCoachAssign(request)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxSendLoading.onNext(false)
                rxSendSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                rxSendLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}