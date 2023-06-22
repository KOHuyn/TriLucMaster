package com.mobileplus.dummytriluc.ui.main.home

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.bluetooth.request.BleErrorRequest
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemMusic
import com.mobileplus.dummytriluc.data.model.TargetType
import com.mobileplus.dummytriluc.data.model.TargetUnit
import com.mobileplus.dummytriluc.data.request.CreateTargetRequest
import com.mobileplus.dummytriluc.data.response.HomeListResponse
import com.mobileplus.dummytriluc.data.response.PracticeAvgResponse
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
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

    fun updateTarget(
        targetUnit: TargetUnit,
        targetTime: TargetType,
        targetPoint: Int,
        onSuccess: (Boolean) -> Unit
    ) {
        isLoading.onNext(true)
        dataManager.createTarget(
            CreateTargetRequest(
                targetUnit = targetUnit.value,
                targetType = targetTime.value,
                targetPoint = targetPoint.toString()
            )
        ).compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                onSuccess(response.isSuccess())
                if (!response.isSuccess()) {
                    rxMessage.onNext(response.message())
                }
            }, {
                isLoading.onNext(false)
                onSuccess(false)
            }).addTo(disposable)
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
        return dataManager.postSubmitMultiPracticeResult("",data)
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

    private val hashmapAvg = hashMapOf<Int, Pair<Int, Int>>()
    fun getAvgPractice(practiceId: Int, callback: (avgPower: Int, avgHit: Int) -> Unit) {
        if (hashmapAvg.contains(practiceId)) {
            val (power, hit) = hashmapAvg[practiceId]!!
            callback(power, hit)
            return
        }
        if (practiceId == AppConstants.INTEGER_DEFAULT) {
            callback(50, 20)
            return
        }
        dataManager.getPracticeAvg(practiceId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    val avgResponse = gson.fromJsonSafe<PracticeAvgResponse>(response.dataObject())
                    val avgPower = avgResponse?.avgPower
                    val avgHit = avgResponse?.avgHit
                    if (avgPower != null && avgHit != null) {
                        hashmapAvg[practiceId] = avgPower to avgHit
                    }
                    callback(avgPower ?: 50, avgHit ?: 20)
                } else {
                    callback(50, 20)
                }
            }, {
                callback(50, 20)
            }).addTo(disposable)
    }

    var listMusic = emptyList<ItemMusic>()
    fun getListMusic(callback: (List<ItemMusic>?) -> Unit) {
        if (listMusic.isNotEmpty()) {
            callback(listMusic)
            return
        }
        dataManager.getListMusic()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    val listMusic = gson.toList<ItemMusic>(response.dataArray())
                    this.listMusic = listMusic
                    callback(listMusic)
                } else {
                    callback(null)
                    rxMessage.onNext(response.message())
                }
            }, {
                callback(null)
                rxMessage.onNext(it.getErrorMsg())
            }).addTo(disposable)
    }
}