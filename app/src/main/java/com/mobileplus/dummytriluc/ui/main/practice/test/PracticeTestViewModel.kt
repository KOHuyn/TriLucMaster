package com.mobileplus.dummytriluc.ui.main.practice.test

import android.annotation.SuppressLint
import com.core.BaseViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.bluetooth.request.TransferBluetoothData
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.entity.DataBluetoothRetryEntity
import com.mobileplus.dummytriluc.data.request.SubmitPracticeResultRequest
import com.mobileplus.dummytriluc.data.response.DataSubmitPracticeResponse
import com.mobileplus.dummytriluc.data.response.LevelPractice
import com.mobileplus.dummytriluc.data.response.PracticeAvgResponse
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.KoinComponent
import org.koin.core.inject

class PracticeTestViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    val isSuccessSubmit: PublishSubject<Pair<Boolean, DataSubmitPracticeResponse?>> =
        PublishSubject.create()

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

    fun submit(request: List<BluetoothResponse>, level: LevelPractice?) {
        isLoading.onNext(true)
        val dataTransform = TransferBluetoothData.transferDataArrayToDataString(request, level)
        if (dataTransform == null) {
            rxMessage.onNext(loadStringRes(R.string.no_data))
            isLoading.onNext(false)
            isSuccessSubmit.onNext(Pair(false, null))
            return
        }
        dataManager.postSubmitMultiPracticeResult(dataTransform)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                logErr(response.toString())
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    try {
                        val data = gson.fromJson(
                            response.dataObject(),
                            DataSubmitPracticeResponse::class.java
                        )
                        isSuccessSubmit.onNext(Pair(true, data))
                    } catch (e: Exception) {
                        isSuccessSubmit.onNext(Pair(false, null))
                        e.logErr()
                    }
                } else {
                    isSuccessSubmit.onNext(Pair(false, null))
                    rxMessage.onNext(response.message())
                }
                logErr("response: $response")
            }, {
                isLoading.onNext(false)
                isSuccessSubmit.onNext(Pair(false, null))
                it.logErr()
                logErr("${it.message}")
                rxMessage.onNext(it.getErrorMsg())
                saveDataWhenPushServerFail(dataTransform)
            }).addTo(compositeDisposable)
    }

    private fun saveDataWhenPushServerFail(data: String) {
        dataManager.saveBluetoothDataRetry(DataBluetoothRetryEntity(data))
            .compose(schedulerProvider.ioToMainObservableScheduler())
            .subscribe({
                logErr("saveDataWhenError:$data")
            }, {
                logErr(it.getErrorMsg())
            }).addTo(compositeDisposable)
    }
}