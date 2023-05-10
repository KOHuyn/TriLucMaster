package com.mobileplus.dummytriluc.ui.video.result

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.DummyResult
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.SubmitChallengeRequest
import com.mobileplus.dummytriluc.data.request.SubmitCoachDraftRequest
import com.mobileplus.dummytriluc.data.request.SubmitPracticeResultRequest
import com.mobileplus.dummytriluc.data.response.DataSendDraftResponse
import com.mobileplus.dummytriluc.data.response.DetailDraftResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.io.File

class VideoResultViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxDetailDraftResponse: PublishSubject<DetailDraftResponse> = PublishSubject.create()
    val rxSubmitSuccess: PublishSubject<Boolean> = PublishSubject.create()
    val rxPushIdPractice: PublishSubject<Pair<Int, DummyResult>> = PublishSubject.create()
    val rxDraftResponse: PublishSubject<DataSendDraftResponse> = PublishSubject.create()
    var fullPath: String? = null

    fun submitPracticeResult(video: File, request: SubmitPracticeResultRequest): Disposable {
        isLoading.onNext(true)
        return dataManager.run {
            if (request.videoPath.isNullOrBlank()) {
                uploadVideo(video)
                    .flatMap { responseVideo ->
                        logErr("responseVideo:$responseVideo")
                        if (responseVideo.isSuccess()) {
                            fullPath =
                                responseVideo.dataObject().get(ApiConstants.FULL_PATH).asString
                            request.videoPath =
                                responseVideo.dataObject().get(ApiConstants.SRC).asString
                            logErr("videoLink:" + request.videoPath)
                        }
                        logErr("jsonData:${gson.toJson(request)}")
                        postSubmitPracticeResult(request)
                    }
            } else postSubmitPracticeResult(request)
        }
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                rxSubmitSuccess.onNext(response.isSuccess())
                if (response.isSuccess()) {
                    try {
                        val dummyResult = DummyResult().apply {
                            id = -1
                            practiceId = request.practiceId
                            mode = request.mode
                            dummyId = request.dummyId
                            startTime1 = request.startTime1
                            startTime2 = request.startTime2
                            videoPath = fullPath
                            videoThumb = null
                            dataVideo = request.data
                        }
                        val rxId = response.getAsJsonObject(ApiConstants.DATA)
                            .get(ApiConstants.RESULT_ID).asInt
                        rxPushIdPractice.onNext(Pair(rxId, dummyResult))
                    } catch (e: Exception) {
                        e.logErr()
                    }
                } else {
                    rxMessage.onNext(response.message())
                }
                logErr(response.toString())
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getDetailDraft(id: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.getTrainerDraftDetail(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                try {
                    if (response.isSuccess()) {
                        val data =
                            gson.fromJson(response.dataObject(), DetailDraftResponse::class.java)
                        rxDetailDraftResponse.onNext(data)
                    } else {
                        rxMessage.onNext(response.message())
                    }
                } catch (e: Exception) {
                    rxMessage.onNext(loadStringRes(R.string.error_unknown_error))
                    e.logErr()
                }
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun submitCoachDraft(video: File, request: SubmitCoachDraftRequest): Disposable {
        isLoading.onNext(true)
        return dataManager.run {
            if (request.videoPath.isNullOrBlank()) {
                dataManager.uploadVideo(video)
                    .flatMap { responseVideo ->
                        if (responseVideo.isSuccess()) {
                            request.videoPath =
                                responseVideo.dataObject().get(ApiConstants.SRC).asString
                            logErr("videoLink:" + request.videoPath)
                        }
                        logErr("jsonData:${gson.toJson(request)}")
                        dataManager.postSubmitCoachDraft(request)
                    }
            } else dataManager.postSubmitCoachDraft(request)
        }
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                rxSubmitSuccess.onNext(response.isSuccess())
                if (response.isSuccess()) {
                    try {
                        rxDraftResponse.onNext(
                            gson.fromJson(
                                response.dataObject(),
                                DataSendDraftResponse::class.java
                            )
                        )
                    } catch (e: Exception) {
                        e.logErr()
                    }

                } else {
                    rxMessage.onNext(response.message())
                }
                logErr(response.toString())
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun submitChallenge(video: File, request: SubmitChallengeRequest): Disposable {
        isLoading.onNext(true)
        return dataManager.run {
            if (request.videoPath.isNullOrBlank()) {
                dataManager.uploadVideo(video)
                    .flatMap { responseVideo ->
                        if (responseVideo.isSuccess()) {
                            request.videoPath =
                                responseVideo.dataObject().get(ApiConstants.SRC).asString
                            logErr("videoLink:" + request.videoPath)
                        }
                        logErr("jsonData:${gson.toJson(request)}")
                        dataManager.postSubmitChallenge(request)
                    }
            } else dataManager.postSubmitChallenge(request)
        }
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                rxSubmitSuccess.onNext(response.isSuccess())
                if (!response.isSuccess()) {
                    rxMessage.onNext(response.message())
                }
                logErr(response.toString())
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}