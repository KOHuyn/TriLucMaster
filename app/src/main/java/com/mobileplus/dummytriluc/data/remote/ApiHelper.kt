package com.mobileplus.dummytriluc.data.remote

import com.google.gson.JsonObject
import com.mobileplus.dummytriluc.bluetooth.request.BleErrorRequest
import com.mobileplus.dummytriluc.data.request.*
import com.mobileplus.dummytriluc.data.request.session.CoachSessionCreateRequest
import com.mobileplus.dummytriluc.data.request.session.CoachSessionSavedListRequest
import io.reactivex.Single
import java.io.File

interface ApiHelper {
    /**
     * Tài khoản
     */
    fun login(request: LoginRequest): Single<JsonObject>
    fun register(request: RegisterRequest): Single<JsonObject>
    fun updateAvatar(src: String): Single<JsonObject>
    fun updateUserInfo(data: UpdateInfo): Single<JsonObject>
    fun getSubjectData(): Single<JsonObject>
    fun getUserInfoSever(): Single<JsonObject>
    fun logoutServer(uuid: String): Single<JsonObject>
    fun requestOTPPassword(email: String): Single<JsonObject>
    fun postNewPassword(
        email: String,
        code: String,
        password: String,
        rePassword: String
    ): Single<JsonObject>

    fun loginSocial(body: SocialLoginRequest): Single<JsonObject>

    fun updatePassword(oldPassword: String, newPassword: String): Single<JsonObject>
    fun getHomeList(): Single<JsonObject>
    fun getUserGuest(id: Int): Single<JsonObject>

    /**
     * Media
     */
    fun uploadAvatar(file: File): Single<JsonObject>
    fun uploadVideo(file: File): Single<JsonObject>

    /**
     * Practice
     */
    fun postSubmitPracticeResult(request: SubmitPracticeResultRequest): Single<JsonObject>
    fun postSubmitMultiPracticeResult(request: String): Single<JsonObject>
    fun postSubmitModeFreedomPractice(request: SubmitModeFreedomPractice): Single<JsonObject>
    fun getListPractice(): Single<JsonObject>
    fun getListPracticeMore(
        id: Int,
        subject: Int?,
        level: Int?,
        sort: String?,
        keyword: String?,
        page: Int?,
    ): Single<JsonObject>

    fun getLessonPracticeDetail(id: Int): Single<JsonObject>
    fun searchPractice(
        subject: Int?,
        level: Int?,
        sort: String?,
        keyword: String?,
        page: Int?
    ): Single<JsonObject>

    fun searchMaster(
        subject: Int?,
        level: Int?,
        sort: String?,
        keyword: String?,
        page: Int?
    ): Single<JsonObject>

    fun getResultPractices(idPractice: Int, page: Int?): Single<JsonObject>
    fun savePractice(request: SaveExerciseRequest): Single<JsonObject>
    fun getListPracticeFolder(id: Int, page: Int?, isPaginate: Boolean? = null): Single<JsonObject>
    fun getPracticeAvg(practiceId: Int): Single<JsonObject>

    /**
     * Coach
     */
    fun getStatisticalCoach(): Single<JsonObject>
    fun getTrainerList(page: Int?): Single<JsonObject>
    fun getTrainerDraft(page: Int?, folderId: Int?): Single<JsonObject>
    fun getTrainerDraftFolder(parentId: Int): Single<JsonObject>
    fun createTrainerDraftFolder(name: String, parentId: Int): Single<JsonObject>
    fun renameTrainerDraftFolder(name: String, parentId: Int): Single<JsonObject>
    fun getPracticeGuest(idGuest: Int, page: Int?): Single<JsonObject>
    fun getTrainerDraftDetail(id: Int): Single<JsonObject>
    fun postSubmitCoachDraft(request: SubmitCoachDraftRequest): Single<JsonObject>
    fun deleteDraft(id: Int): Single<JsonObject>
    fun postTrainerRequest(message: String, masterId: Int): Single<JsonObject>
    fun postTrainerRequestRemove(masterId: Int): Single<JsonObject>
    fun checkMaster(): Single<JsonObject>
    fun registerCoach(request: CoachRegisterRequest): Single<JsonObject>
    fun getCoachAssignExercise(idClass: Int, page: Int?): Single<JsonObject>
    fun postCoachAssign(request: CoachAssignExerciseRequest): Single<JsonObject>
    fun deleteAssign(assignId: Int, classId: Int?): Single<JsonObject>
    fun getClassJoin(page: Int?): Single<JsonObject>
    fun getAssigned(page: Int?): Single<JsonObject>
    fun updateDraft(id: Int, title: String, folderId: Int): Single<JsonObject>
    fun saveFolderPractice(request: CreateCourseRequest): Single<JsonObject>
    fun updateFolderPractice(idCourse: Int, request: CreateCourseRequest): Single<JsonObject>
    fun updatePractice(request: SaveExerciseRequest, parentId: Int): Single<JsonObject>
    fun deletePractice(id: Int): Single<JsonObject>

    //class student
    fun getDiscipleGroupList(page: Int?): Single<JsonObject>
    fun postCreateGroup(groupName: String): Single<JsonObject>
    fun deleteGroup(groupID: String): Single<JsonObject>
    fun postRenameGroup(id: Int, newName: String): Single<JsonObject>
    fun getMemberInGroup(idGroup: Int, dayRange: Int?, page: Int?): Single<JsonObject>
    fun getAllMemberInGroup(idGroup: Int): Single<JsonObject>
    fun deleteMemberInGroup(classID: Int, userID: Int): Single<JsonObject>
    fun addMemberInGroup(arrClassId: List<Int>, userID: Int): Single<JsonObject>
    fun addManyMemberIntoGroup(classID: Int, userIds: List<Int>): Single<JsonObject>

    //student
    fun getDiscipleRanking(page: Int?): Single<JsonObject>
    fun getDiscipleList(page: Int?): Single<JsonObject>
    fun getDiscipleWaiting(page: Int?): Single<JsonObject>
    fun acceptDisciple(id: Int): Single<JsonObject>
    fun rejectDisciple(id: Int): Single<JsonObject>
    fun deleteDisciple(id: Int): Single<JsonObject>

    //session
    fun createCoachSession(createRequest: CoachSessionCreateRequest): Single<JsonObject>
    fun savedCoachSessionListPractice(request: CoachSessionSavedListRequest): Single<JsonObject>
    fun getCoachSessionPracticeSaved(page: Int?): Single<JsonObject>
    fun deleteCoachSessionPractice(id: Int): Single<JsonObject>
    fun getCoachSessionDetailSavedList(id: Int): Single<JsonObject>
    fun getCoachSessionList(page: Int?): Single<JsonObject>
    fun deleteCoachSession(id: Int): Single<JsonObject>
    fun getSessionDetail(id: Int): Single<JsonObject>
    fun saveResultSession(data: String): Single<JsonObject>

    /**
     * Challenge
     */
    fun getChallenge(): Single<JsonObject>
    fun getChallengeMore(type: String, page: Int?): Single<JsonObject>
    fun getChallengeDetail(id: Int): Single<JsonObject>
    fun postSubmitChallenge(request: SubmitChallengeRequest): Single<JsonObject>
    fun postJoinChallenge(id: Int): Single<JsonObject>
    fun startChallenge(id: Int): Single<JsonObject>

    /**
     * News
     */
    fun getFeed(page: Int?): Single<JsonObject>
    fun getFeedDetail(id: Int): Single<JsonObject>

    /**
     * Notification
     */
    fun getNotifications(page: Int?): Single<JsonObject>
    fun getNotificationDetail(id: Int): Single<JsonObject>

    /**
     * Punch
     */
    fun getPunch(type: String?, page: Int?): Single<JsonObject>

    /**
     * Power
     */
    fun getPower(type: String?, page: Int?): Single<JsonObject>

    /**
     * Rank
     */
    fun getRanking(page: Int?): Single<JsonObject>

    /**
     * Chat
     */
    fun getListChatRoom(page: Int?): Single<JsonObject>
    fun getChatRoom(keyId: Int?, type: String? = ApiConstants.DOWN, roomId: Int): Single<JsonObject>
    fun postChat(chat: ChatSendRequest): Single<JsonObject>

    /**
     * Utils
     */
    fun getListSubject(): Single<JsonObject>
    fun getListLevel(): Single<JsonObject>
    fun getConfig(): Single<JsonObject>
    fun removeToken(): Single<JsonObject>
    fun refreshToken(): Single<JsonObject>
    fun logErrorMachine(data: BleErrorRequest): Single<JsonObject>
    fun connectMachine(machineName: String): Single<JsonObject>
    fun forceConnectMachine(machineName: String): Single<JsonObject>
    fun getDataPracticeResult(practiceId: String): Single<JsonObject>
    fun createTarget(request: CreateTargetRequest): Single<JsonObject>
    fun getListMusic(): Single<JsonObject>
}