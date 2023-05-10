package com.mobileplus.dummytriluc.ui.main.home

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.bluetooth.request.BleErrorRequest
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.response.HomeListResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by ThaiNV on 12/7/2020.
 */

class HomeViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val homeListResponse: PublishSubject<HomeListResponse> = PublishSubject.create()

    val user = dataManager.getUserInfo()

    fun getHomeList(): Disposable {
        checkDataBleFailCache()
        checkJSONErrorBleFailCache()
        isLoading.onNext(true)
        dataManager.getHomeResponse()?.let { homeListResponse.onNext(it) }
        return dataManager.getHomeList()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    try {
                        val homeResponse =
                            gson.fromJson(response.dataObject(), HomeListResponse::class.java)
                        homeListResponse.onNext(homeResponse)
                        dataManager.saveHomeResponse(homeResponse)
                    } catch (e: Exception) {
                        e.logErr()
                    }
                } else {
                    rxMessage.onNext(response.message())
                }
                logErr("homeList:$response")
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    private fun checkDataBleFailCache(): Disposable {
        return dataManager.getAllBluetoothDataRetry()
            .compose(schedulerProvider.ioToMainObservableScheduler())
            .subscribe({ arrDataCahce ->
                if (arrDataCahce.isNotEmpty()) {
                    arrDataCahce.map {
                        it.data?.let { dataCache -> pushCacheDataServer(dataCache) }
                    }
                }
            }, { it.logErr() })
    }

    private fun pushCacheDataServer(data: String): Disposable {
        return dataManager.postSubmitMultiPracticeResult(data)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                logErr("$response")
                if (response.isSuccess()) {
                    deleteDataCacheByContent(data)
                }
            }, {
                it.logErr()
            })
    }

    private fun deleteDataCacheByContent(content: String): Disposable {
        return dataManager.deleteDataBluetoothRetryByContent(content)
            .compose(schedulerProvider.ioToMainObservableScheduler()).subscribe({}, {})
    }

    private fun checkJSONErrorBleFailCache(): Disposable {
        return dataManager.getAllBluetoothDataError()
            .compose(schedulerProvider.ioToMainObservableScheduler())
            .subscribe({ arrDataCahce ->
                if (arrDataCahce.isNotEmpty()) {
                    arrDataCahce.map {
                        pushJSONErrorCache(it)
                    }
                }
            }, { it.logErr() })
    }

    private fun pushJSONErrorCache(data: BleErrorRequest): Disposable {
        return dataManager.logErrorMachine(data)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                logErr("$response")
                if (response.isSuccess()) {
                    deleteJSONErrorCacheByContent(data)
                }
            }, {
                it.logErr()
            })
    }

    private fun deleteJSONErrorCacheByContent(data: BleErrorRequest): Disposable {
        return dataManager.deleteDataBluetoothError(data)
            .compose(schedulerProvider.ioToMainObservableScheduler()).subscribe({
                logErr("deleteJSONErrorCacheByContent success")
            }, {
                logErr("deleteJSONErrorCacheByContent fail")
            })
    }
}