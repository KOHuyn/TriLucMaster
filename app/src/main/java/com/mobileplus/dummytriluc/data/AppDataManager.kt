package com.mobileplus.dummytriluc.data

import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.bluetooth.request.BleErrorRequest
import com.mobileplus.dummytriluc.data.local.db.DbHelper
import com.mobileplus.dummytriluc.data.local.prefs.PrefsHelper
import com.mobileplus.dummytriluc.data.model.MachineInfo
import com.mobileplus.dummytriluc.data.model.UserInfo
import com.mobileplus.dummytriluc.data.model.entity.DataBluetoothRetryEntity
import com.mobileplus.dummytriluc.data.model.entity.TableConfig
import com.mobileplus.dummytriluc.data.model.entity.TableLevel
import com.mobileplus.dummytriluc.data.model.entity.TableSubject
import com.mobileplus.dummytriluc.data.remote.ApiHelper
import com.mobileplus.dummytriluc.data.request.*
import com.mobileplus.dummytriluc.data.request.session.CoachSessionCreateRequest
import com.mobileplus.dummytriluc.data.request.session.CoachSessionSavedListRequest
import com.mobileplus.dummytriluc.data.response.HomeListResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.io.IOException

class AppDataManager constructor(
    private val apiHelper: ApiHelper,
    private val prefsHelper: PrefsHelper,
    private val dbHelper: DbHelper
) :
    DataManager {
    override fun logout() {
        clearUser()
        logoutLocal()
        logoutFacebook()
        logoutGoogle()
        deleteTokenFirebase()
        logErr("logout")
    }

    private fun logoutFacebook() {
        LoginManager.getInstance().logOut()
    }

    private fun logoutGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient =
            GoogleSignIn.getClient(DummyTriLucApplication.getInstance(), gso)
        googleSignInClient.signOut()
    }

    private fun deleteTokenFirebase() {
        Thread {
            try {
                FirebaseMessaging.getInstance().deleteToken()
            } catch (e: IOException) {
                e.logErr()
            }
        }.start()
    }

    /**
     * [apiHelper]
     */
    override fun login(request: LoginRequest): Single<JsonObject> = apiHelper.login(request)

    override fun register(request: RegisterRequest): Single<JsonObject> {
        return apiHelper.register(request)
    }

    override fun updateAvatar(src: String): Single<JsonObject> = apiHelper.updateAvatar(src)

    override fun updateUserInfo(data: UpdateInfo): Single<JsonObject> =
        apiHelper.updateUserInfo(data)

    override fun getSubjectData(): Single<JsonObject> = apiHelper.getSubjectData()

    override fun getUserInfoSever(): Single<JsonObject> = apiHelper.getUserInfoSever()
    override fun logoutServer(uuid: String): Single<JsonObject> = apiHelper.logoutServer(uuid)
    override fun requestOTPPassword(email: String): Single<JsonObject> =
        apiHelper.requestOTPPassword(email)

    override fun postNewPassword(
        email: String,
        code: String,
        password: String,
        rePassword: String
    ): Single<JsonObject> = apiHelper.postNewPassword(email, code, password, rePassword)

    override fun uploadAvatar(file: File): Single<JsonObject> =
        apiHelper.uploadAvatar(file)

    override fun uploadVideo(file: File): Single<JsonObject> = apiHelper.uploadVideo(file)

    override fun postSubmitPracticeResult(request: SubmitPracticeResultRequest): Single<JsonObject> =
        apiHelper.postSubmitPracticeResult(request)

    override fun postSubmitMultiPracticeResult(sessionId: String,request: String): Single<JsonObject> =
        apiHelper.postSubmitMultiPracticeResult(sessionId, request)

    override fun postSubmitModeFreedomPractice(request: SubmitModeFreedomPractice): Single<JsonObject> =
        apiHelper.postSubmitModeFreedomPractice(request)

    override fun getListPractice(): Single<JsonObject> = apiHelper.getListPractice()

    override fun getListPracticeMore(
        id: Int,
        subject: Int?,
        level: Int?,
        sort: String?,
        keyword: String?,
        page: Int?,
    ): Single<JsonObject> =
        apiHelper.getListPracticeMore(id, subject, level, sort, keyword, page)

    override fun getLessonPracticeDetail(id: Int): Single<JsonObject> =
        apiHelper.getLessonPracticeDetail(id)

    override fun searchPractice(
        subject: Int?,
        level: Int?,
        sort: String?,
        keyword: String?,
        page: Int?
    ): Single<JsonObject> = apiHelper.searchPractice(subject, level, sort, keyword, page)

    override fun searchMaster(
        subject: Int?,
        level: Int?,
        sort: String?,
        keyword: String?,
        page: Int?
    ): Single<JsonObject> = apiHelper.searchMaster(subject, level, sort, keyword, page)

    override fun getResultPractices(idPractice: Int, page: Int?): Single<JsonObject> =
        apiHelper.getResultPractices(idPractice, page)

    override fun savePractice(request: SaveExerciseRequest): Single<JsonObject> =
        apiHelper.savePractice(request)

    override fun getListPracticeFolder(
        id: Int,
        page: Int?,
        isPaginate: Boolean?
    ): Single<JsonObject> =
        apiHelper.getListPracticeFolder(id, page, isPaginate)

    override fun getPracticeAvg(practiceId: Int): Single<JsonObject> =
        apiHelper.getPracticeAvg(practiceId)

    override fun getStatisticalCoach(): Single<JsonObject> = apiHelper.getStatisticalCoach()

    override fun getTrainerList(page: Int?): Single<JsonObject> = apiHelper.getTrainerList(page)

    override fun getTrainerDraft(page: Int?, folderId: Int?): Single<JsonObject> =
        apiHelper.getTrainerDraft(page, folderId)

    override fun getTrainerDraftFolder(parentId: Int): Single<JsonObject> =
        apiHelper.getTrainerDraftFolder(parentId)

    override fun createTrainerDraftFolder(name: String, parentId: Int): Single<JsonObject> =
        apiHelper.createTrainerDraftFolder(name, parentId)

    override fun renameTrainerDraftFolder(name: String, parentId: Int): Single<JsonObject> =
        apiHelper.renameTrainerDraftFolder(name, parentId)

    override fun getPracticeGuest(idGuest: Int, page: Int?): Single<JsonObject> =
        apiHelper.getPracticeGuest(idGuest, page)

    override fun getTrainerDraftDetail(id: Int): Single<JsonObject> =
        apiHelper.getTrainerDraftDetail(id)

    override fun postSubmitCoachDraft(request: SubmitCoachDraftRequest): Single<JsonObject> =
        apiHelper.postSubmitCoachDraft(request)

    override fun deleteDraft(id: Int): Single<JsonObject> = apiHelper.deleteDraft(id)

    override fun postTrainerRequest(message: String, masterId: Int): Single<JsonObject> =
        apiHelper.postTrainerRequest(message, masterId)

    override fun postTrainerRequestRemove(masterId: Int): Single<JsonObject> =
        apiHelper.postTrainerRequestRemove(masterId)

    override fun checkMaster(): Single<JsonObject> = apiHelper.checkMaster()

    override fun registerCoach(request: CoachRegisterRequest): Single<JsonObject> =
        apiHelper.registerCoach(request)

    override fun getCoachAssignExercise(
        idClass: Int,
        page: Int?
    ): Single<JsonObject> =
        apiHelper.getCoachAssignExercise(idClass, page)

    override fun postCoachAssign(request: CoachAssignExerciseRequest): Single<JsonObject> =
        apiHelper.postCoachAssign(request)

    override fun deleteAssign(assignId: Int, classId: Int?): Single<JsonObject> =
        apiHelper.deleteAssign(assignId, classId)

    override fun getClassJoin(page: Int?): Single<JsonObject> = apiHelper.getClassJoin(page)

    override fun getAssigned(page: Int?): Single<JsonObject> = apiHelper.getAssigned(page)

    override fun updateDraft(id: Int, title: String, folderId: Int): Single<JsonObject> =
        apiHelper.updateDraft(id, title, folderId)

    override fun saveFolderPractice(request: CreateCourseRequest): Single<JsonObject> =
        apiHelper.saveFolderPractice(request)

    override fun updateFolderPractice(
        idCourse: Int,
        request: CreateCourseRequest
    ): Single<JsonObject> = apiHelper.updateFolderPractice(idCourse, request)

    override fun updatePractice(request: SaveExerciseRequest, parentId: Int): Single<JsonObject> =
        apiHelper.updatePractice(request, parentId)

    override fun deletePractice(id: Int): Single<JsonObject> = apiHelper.deletePractice(id)

    override fun getDiscipleGroupList(page: Int?): Single<JsonObject> =
        apiHelper.getDiscipleGroupList(page)

    override fun postCreateGroup(groupName: String): Single<JsonObject> =
        apiHelper.postCreateGroup(groupName)

    override fun deleteGroup(groupID: String): Single<JsonObject> = apiHelper.deleteGroup(groupID)

    override fun postRenameGroup(id: Int, newName: String): Single<JsonObject> =
        apiHelper.postRenameGroup(id, newName)

    override fun getMemberInGroup(idGroup: Int, dayRange: Int?, page: Int?): Single<JsonObject> =
        apiHelper.getMemberInGroup(idGroup, dayRange, page)

    override fun getAllMemberInGroup(idGroup: Int): Single<JsonObject> =
        apiHelper.getAllMemberInGroup(idGroup)

    override fun deleteMemberInGroup(classID: Int, userID: Int): Single<JsonObject> =
        apiHelper.deleteMemberInGroup(classID, userID)

    override fun addMemberInGroup(arrClassId: List<Int>, userID: Int): Single<JsonObject> =
        apiHelper.addMemberInGroup(arrClassId, userID)

    override fun addManyMemberIntoGroup(classID: Int, userIds: List<Int>): Single<JsonObject> =
        apiHelper.addManyMemberIntoGroup(classID, userIds)

    override fun getDiscipleRanking(page: Int?): Single<JsonObject> =
        apiHelper.getDiscipleRanking(page)

    override fun getDiscipleList(page: Int?): Single<JsonObject> = apiHelper.getDiscipleList(page)

    override fun getDiscipleWaiting(page: Int?): Single<JsonObject> =
        apiHelper.getDiscipleWaiting(page)

    override fun acceptDisciple(id: Int): Single<JsonObject> = apiHelper.acceptDisciple(id)

    override fun rejectDisciple(id: Int): Single<JsonObject> = apiHelper.rejectDisciple(id)

    override fun deleteDisciple(id: Int): Single<JsonObject> = apiHelper.deleteDisciple(id)

    override fun createCoachSession(createRequest: CoachSessionCreateRequest): Single<JsonObject> =
        apiHelper.createCoachSession(createRequest)

    override fun savedCoachSessionListPractice(request: CoachSessionSavedListRequest): Single<JsonObject> =
        apiHelper.savedCoachSessionListPractice(request)

    override fun getCoachSessionPracticeSaved(page: Int?): Single<JsonObject> =
        apiHelper.getCoachSessionPracticeSaved(page)

    override fun deleteCoachSessionPractice(id: Int): Single<JsonObject> =
        apiHelper.deleteCoachSessionPractice(id)

    override fun getCoachSessionDetailSavedList(id: Int): Single<JsonObject> =
        apiHelper.getCoachSessionDetailSavedList(id)

    override fun getCoachSessionList(page: Int?): Single<JsonObject> =
        apiHelper.getCoachSessionList(page)

    override fun deleteCoachSession(id: Int): Single<JsonObject> = apiHelper.deleteCoachSession(id)

    override fun getSessionDetail(id: Int): Single<JsonObject> = apiHelper.getSessionDetail(id)

    override fun saveResultSession(data: String): Single<JsonObject> =
        apiHelper.saveResultSession(data)

    override fun getChallenge(): Single<JsonObject> = apiHelper.getChallenge()

    override fun getChallengeMore(type: String, page: Int?): Single<JsonObject> =
        apiHelper.getChallengeMore(type, page)

    override fun getChallengeDetail(id: Int): Single<JsonObject> = apiHelper.getChallengeDetail(id)

    override fun postSubmitChallenge(request: SubmitChallengeRequest): Single<JsonObject> =
        apiHelper.postSubmitChallenge(request)

    override fun postJoinChallenge(id: Int): Single<JsonObject> = apiHelper.postJoinChallenge(id)

    override fun startChallenge(id: Int): Single<JsonObject> = apiHelper.startChallenge(id)

    override fun getFeed(page: Int?): Single<JsonObject> = apiHelper.getFeed(page)

    override fun getFeedDetail(id: Int): Single<JsonObject> = apiHelper.getFeedDetail(id)

    override fun getNotifications(page: Int?): Single<JsonObject> = apiHelper.getNotifications(page)

    override fun getNotificationDetail(id: Int): Single<JsonObject> =
        apiHelper.getNotificationDetail(id)

    override fun getPunch(type: String?, page: Int?): Single<JsonObject> =
        apiHelper.getPunch(type, page)

    override fun getPower(type: String?, page: Int?): Single<JsonObject> =
        apiHelper.getPower(type, page)

    override fun getRanking(page: Int?): Single<JsonObject> = apiHelper.getRanking(page)

    override fun getListChatRoom(page: Int?): Single<JsonObject> = apiHelper.getListChatRoom(page)

    override fun getChatRoom(keyId: Int?, type: String?, roomId: Int): Single<JsonObject> =
        apiHelper.getChatRoom(keyId, type, roomId)

    override fun postChat(chat: ChatSendRequest): Single<JsonObject> =
        apiHelper.postChat(chat)

    override fun getListSubject(): Single<JsonObject> = apiHelper.getListSubject()

    override fun getListLevel(): Single<JsonObject> = apiHelper.getListLevel()

    override fun getConfig(): Single<JsonObject> = apiHelper.getConfig()

    override fun removeToken(): Single<JsonObject> = apiHelper.removeToken()

    override fun refreshToken(): Single<JsonObject> = apiHelper.refreshToken()

    override fun logErrorMachine(data: BleErrorRequest): Single<JsonObject> =
        apiHelper.logErrorMachine(data)

    override fun connectMachine(machineName: String): Single<JsonObject> =
        apiHelper.connectMachine(machineName)

    override fun forceConnectMachine(machineName: String): Single<JsonObject> =
        apiHelper.forceConnectMachine(machineName)

    override fun getDataPracticeResult(practiceId: String): Single<JsonObject> {
        return apiHelper.getDataPracticeResult(practiceId)
    }

    override fun createTarget(request: CreateTargetRequest): Single<JsonObject> {
        return apiHelper.createTarget(request)
    }

    override fun getListMusic(): Single<JsonObject> {
        return apiHelper.getListMusic()
    }

    override fun loginSocial(body: SocialLoginRequest): Single<JsonObject> =
        apiHelper.loginSocial(body)

    override fun updatePassword(oldPassword: String, newPassword: String): Single<JsonObject> =
        apiHelper.updatePassword(oldPassword, newPassword)

    override fun getHomeList(): Single<JsonObject> = apiHelper.getHomeList()

    override fun getUserGuest(id: Int): Single<JsonObject> = apiHelper.getUserGuest(id)

    /**
     * [prefsHelper]
     */

    override fun getHomeResponse(): HomeListResponse? = prefsHelper.getHomeResponse()

    override fun saveHomeResponse(response: HomeListResponse) =
        prefsHelper.saveHomeResponse(response)

    override fun getToken(): String? = prefsHelper.getToken()

    override fun setToken(token: String) {
        prefsHelper.setToken(token)
    }

    override fun saveUser(userInfo: UserInfo) = prefsHelper.saveUser(userInfo)

    override fun getUserInfo(): UserInfo? = prefsHelper.getUserInfo()

    override fun clearUser() = prefsHelper.clearUser()

    override fun logoutLocal() = prefsHelper.logoutLocal()

    override fun setIsLoggedIn(isLogged: Boolean) = prefsHelper.setIsLoggedIn(isLogged)

    override fun isLoggedIn(): Boolean = prefsHelper.isLoggedIn()

    override var numberHotLine: String
        get() = prefsHelper.numberHotLine
        set(value) {
            prefsHelper.numberHotLine = value
        }
    override var isDataSecurityBle: Boolean
        get() = prefsHelper.isDataSecurityBle
        set(value) {
            prefsHelper.isDataSecurityBle = value
        }
    override var userName: String
        get() =
            prefsHelper.userName
        set(value) {
            prefsHelper.userName = value
        }
    override var password: String
        get() = prefsHelper.password
        set(value) {
            prefsHelper.password = value
        }
    override var isOpenFirstApp: Boolean
        get() = prefsHelper.isOpenFirstApp
        set(value) {
            prefsHelper.isOpenFirstApp = value
        }
    override var expiredTokenInDay: String
        get() = prefsHelper.expiredTokenInDay
        set(value) {
            prefsHelper.expiredTokenInDay = value
        }
    override var versionUpdateApp: String
        get() = prefsHelper.versionUpdateApp
        set(value) {
            prefsHelper.versionUpdateApp = value
        }

    override var isConnectedMachine: Boolean
        get() = prefsHelper.isConnectedMachine
        set(value) {
            prefsHelper.isConnectedMachine = value
        }

    override var machineCodeConnectLasted: MachineInfo?
        get() = prefsHelper.machineCodeConnectLasted
        set(value) {
            prefsHelper.machineCodeConnectLasted = value
        }

    /**
     * [dbHelper]
     */
    override fun saveLevels(levels: List<TableLevel>): Observable<List<Long>> =
        dbHelper.saveLevels(levels)

    override fun saveSubjects(subjects: List<TableSubject>): Observable<List<Long>> =
        dbHelper.saveSubjects(subjects)

    override fun getAllLevel(): Observable<List<TableLevel>> = dbHelper.getAllLevel()

    override fun getAllSubject(): Observable<List<TableSubject>> = dbHelper.getAllSubject()

    override fun saveConfigs(configs: List<TableConfig>): Observable<List<Long>> =
        dbHelper.saveConfigs(configs)

    override fun getAllConfig(): Observable<List<TableConfig>> = dbHelper.getAllConfig()

    override fun getAllBluetoothDataRetry(): Observable<List<DataBluetoothRetryEntity>> =
        dbHelper.getAllBluetoothDataRetry()

    override fun saveBluetoothDataRetry(data: DataBluetoothRetryEntity): Observable<Long> =
        dbHelper.saveBluetoothDataRetry(data)

    override fun deleteAllDataBluetoothRetry(): Observable<Boolean> =
        dbHelper.deleteAllDataBluetoothRetry()

    override fun deleteDataBluetoothRetryByContent(content: String): Observable<Boolean> =
        dbHelper.deleteDataBluetoothRetryByContent(content)

    override fun getAllBluetoothDataError(): Observable<List<BleErrorRequest>> =
        dbHelper.getAllBluetoothDataError()

    override fun saveBluetoothDataError(data: BleErrorRequest): Observable<Long> =
        dbHelper.saveBluetoothDataError(data)

    override fun deleteAllDataBluetoothError(): Observable<Boolean> =
        dbHelper.deleteAllDataBluetoothError()

    override fun deleteDataBluetoothErrorByContent(content: String): Observable<Boolean> =
        dbHelper.deleteDataBluetoothErrorByContent(content)

    override fun deleteDataBluetoothError(data: BleErrorRequest): Observable<Boolean> =
        dbHelper.deleteDataBluetoothError(data)

}