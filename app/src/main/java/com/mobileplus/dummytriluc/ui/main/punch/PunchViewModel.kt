package com.mobileplus.dummytriluc.ui.main.punch

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.response.DataPunchResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.lang.Exception

class PunchViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val responsePunch: PublishSubject<DataPunchResponse> = PublishSubject.create()

    fun getDataPunch(type: String? = ApiConstants.MONTH, page: Int? = 1): Disposable {
        isLoading.onNext(true)
        return dataManager.getPunch(type, page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                try {
                    if (response.isSuccess()) {
                        responsePunch.onNext(
                            gson.fromJson(
                                response.dataObject(),
                                DataPunchResponse::class.java
                            )
                        )
                    } else {
                        rxMessage.onNext(response.message())
                    }
                } catch (e: Exception) {
                    e.logErr()
                    rxMessage.onNext(loadStringRes(R.string.data_not_available))
                }
                logErr("$response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}