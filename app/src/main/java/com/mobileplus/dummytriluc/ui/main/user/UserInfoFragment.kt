package com.mobileplus.dummytriluc.ui.main.user

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.core.BaseFragmentZ
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.google.gson.Gson
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemChart
import com.mobileplus.dummytriluc.data.model.UserInfo
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.UpdateInfo
import com.mobileplus.dummytriluc.databinding.FragmentUserInfoBinding
import com.mobileplus.dummytriluc.ui.dialog.ReceiveMasterDialog
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.login.register.stepbystep.*
import com.mobileplus.dummytriluc.ui.main.challenge.adapter.AppellationAdapter
import com.mobileplus.dummytriluc.ui.main.coach.adapter.CoachAdapter
import com.mobileplus.dummytriluc.ui.main.coach.my_practice.CoachPracticeFragment
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailFragment
import com.mobileplus.dummytriluc.ui.main.practice.folder.PracticeFolderFragment
import com.mobileplus.dummytriluc.ui.utils.*
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventReceiveMaster
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventUpdateInfo
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventUpdateProfile
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.applyClickShrink
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File

class UserInfoFragment : BaseFragmentZ<FragmentUserInfoBinding>() {
    override fun getLayoutBinding(): FragmentUserInfoBinding =
        FragmentUserInfoBinding.inflate(layoutInflater)

    companion object {
        const val ARG_EDIT_PROFILE = "ARG_EDIT_PROFILE"
        const val ARG_USER_ID_GUEST = "ARG_USER_ID_GUEST"

        fun openInfoGuest(idGuest: Int) {
            val bundle = Bundle().apply {
                putInt(ARG_USER_ID_GUEST, idGuest)
            }
            postNormal(
                EventNextFragmentMain(
                    UserInfoFragment::class.java,
                    bundle,
                    true
                )
            )
        }

        fun openInfoUser() {
            postNormal(
                EventNextFragmentMain(UserInfoFragment::class.java, true)
            )
        }
    }

    private val vm: UserInfoViewModel by viewModel()
    private var imagePath: String = ""
    private var userInfo: UserInfo? = null
    private var updateInfo = UpdateInfo()
    private val gson: Gson by inject()
    private val adapterExercise by lazy { CoachAdapter() }
    private val rewardAdapter by lazy { AppellationAdapter() }
    private val idGuest by argument(ARG_USER_ID_GUEST, AppConstants.INTEGER_DEFAULT)
    private var isGuest: Boolean = false
    private var statusReceiveMaster: Int? = null
        set(value) {
            field = value
            runOnUiThread {
                when (field) {
                    null, StatusReceiveMaster.STATUS_REJECT -> {
                        binding.btnReceiveUser.setBackgroundResource(R.drawable.gradient_orange)
                        binding.txtReceiveUser.text = loadStringRes(R.string.receive_master)
                    }
                    StatusReceiveMaster.STATUS_BLOCK -> {
                        binding.btnReceiveUser.hide()
                    }
                    StatusReceiveMaster.STATUS_COMPLETE -> {
                        binding.btnReceiveUser.setBackgroundResource(R.drawable.gradient_orange)
                        binding.txtReceiveUser.text = loadStringRes(R.string.unreceive_master)
                    }
                    StatusReceiveMaster.STATUS_REQUEST -> {
                        binding.txtReceiveUser.text = loadStringRes(R.string.wait_for_confirmation)
                        binding.btnReceiveUser.setBackgroundResource(R.color.clr_grey)
                    }
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtUserNameInfo.hideKeyBoardWhenClick(view)
    }

    private fun EditText?.hideKeyBoardWhenClick(view: View) {
        if (view !is EditText && view !is Button) {
            view.setOnTouchListener { v, _ ->
                v.performClick()
                hideKeyboard()
                if (this?.isFocused == true) {
                    this?.clearFocus()
                }
                false
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                this.hideKeyBoardWhenClick(innerView)
            }
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        disposeViewModel()
        checkArg()
        configView()
        initAction()
    }

    private fun checkArg() {
        if (arguments?.containsKey(ARG_USER_ID_GUEST) == true) {
            isGuest = true
            vm.getUserGuest(idGuest)
        } else {
            isGuest = false
            vm.getUserInfo()
            if (vm.homeResponse != null && vm.homeResponse?.chart != null) {
                parseAbility(vm.homeResponse?.chart!!)
            } else {
                binding.lnDetailIndex.hide()
            }
        }
    }

    private fun parseAbility(chart: MutableList<ItemChart> = mutableListOf()) {
        val titles = arrayListOf<String>()
        val entries = arrayListOf<RadarEntry>()
        val backgroundEntries = arrayListOf<RadarEntry>()
        if (chart.isEmpty()) {
            for (i in 0..5) {
                titles.add("-")
                entries.add(RadarEntry(5f))
                backgroundEntries.add(RadarEntry(10f))
            }
        } else {
            for (item in chart) {
                titles.add(item.titleZ)
                entries.add(RadarEntry(item.scoreChart))
                backgroundEntries.add(RadarEntry(item.max ?: 0f))
            }
        }

        var titleArray = arrayOf<String>()
        titleArray = titles.toArray(titleArray)
        ChartUtils.createRadarChart(binding.chartIndex, titleArray)
        val set = RadarDataSet(entries, "Target")
        set.apply {
            color = resources.getColor(R.color.clr_blue)
            lineWidth = 2f
            setDrawHighlightIndicators(false)
        }
        val bgSet = RadarDataSet(backgroundEntries, "background")
        bgSet.apply {
            highlightCircleFillColor = Color.GRAY
            color = Color.TRANSPARENT
            setDrawFilled(true)
            setDrawHighlightIndicators(false)
        }
        val sets = arrayListOf<IRadarDataSet>(bgSet, set)
        val radarData = RadarData(sets).apply {
            setDrawValues(false)
        }
        binding.chartIndex.apply {
            data = radarData
            invalidate()
            animateXY(1000, 1000, Easing.EaseInOutQuad)
        }
    }

    private fun configView() {
        binding.tvDiscipleCount.fillGradientPrimary()
        binding.tvLessonCount.fillGradientPrimary()
        binding.tvParticipants.fillGradientPrimary()
        binding.tvRankingUserInfo.fillGradientPrimary()
        binding.btnAddFriendUser.applyClickShrink()
        binding.btnReceiveUser.applyClickShrink()
        binding.cbUpdateUserInfo.setVisibility(!isGuest)
        binding.lnDetailIndex.setVisibility(!isGuest)
        binding.btnEditAvatarInfo.setVisibility(!isGuest)
        binding.lnSubjectUserInfo.isEnabled = !isGuest
        binding.rvTrainerExerciseMasterInfo.run {
            adapter = adapterExercise
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_12).toInt(),
                    isHorizontalRecyclerView = true
                )
            )
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rcvRewardUserInfo.run {
            setHasFixedSize(true)
            adapter = rewardAdapter
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_8).toInt(),
                    isHorizontalRecyclerView = true
                )
            )
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

    }

    private fun nextFragEditProfile(frag: Class<*>) {
        val bundle = Bundle()
        bundle.putString(ARG_EDIT_PROFILE, gson.toJson(updateInfo))
        postNormal(
            EventNextFragmentMain(
                frag,
                bundle,
                true
            )
        )
    }

    private fun initAction() {
        binding.btnEditAvatarInfo.clickWithDebounce {
            PickerImageUtils.createSingleImageGallery(this)
                .forResult(PictureConfig.CHOOSE_REQUEST)
        }

        binding.tvEditBirthdayUserInfo.clickWithDebounce {
            nextFragEditProfile(RegisterStep5Fragment::class.java)
        }
        binding.tvEditGenderUserInfo.clickWithDebounce {
            nextFragEditProfile(RegisterStep2Fragment::class.java)
        }

        binding.tvEditHeightUserInfo.clickWithDebounce {
            nextFragEditProfile(RegisterStep3Fragment::class.java)
        }
        binding.tvEditWeightUserInfo.clickWithDebounce {
            nextFragEditProfile(RegisterStep4Fragment::class.java)
        }

        addDispose(RxCompoundButton.checkedChanges(binding.cbUpdateUserInfo)
            .skipInitialValue()
            .subscribe { isChecked ->
                listOf<View>(
                    binding.edtUserNameInfo,
                    binding.tvEditBirthdayUserInfo,
                    binding.tvEditGenderUserInfo,
                    binding.tvEditHeightUserInfo,
                    binding.tvEditWeightUserInfo
                ).setEditProfileField(isChecked)
                if (!isChecked) {
                    val newName = binding.edtUserNameInfo.text.toString()
                    if (newName != userInfo?.fullName) {
                        updateInfo.fullName = newName
                    } else {
                        updateInfo.fullName = null
                    }
                    vm.updateUserInfo(updateInfo)
                }
            })

        binding.btnBackProfile.clickWithDebounce {
            onBackPressed()
        }

        binding.lnSubjectUserInfo.clickWithDebounce {
            nextFragEditProfile(RegisterStep6Fragment::class.java)
        }
        binding.tvShowMore.clickWithDebounce {
            if (isGuest) {
                CoachPracticeFragment.openFragmentGuest(idGuest)
            } else {
                CoachPracticeFragment.openFragment()
            }
        }
        binding.btnReceiveUser.clickWithDebounce {
            if (idGuest != AppConstants.INTEGER_DEFAULT) {
                when (statusReceiveMaster) {
                    null, StatusReceiveMaster.STATUS_REJECT -> {
                        receiveMaster(idGuest)
                    }
                    StatusReceiveMaster.STATUS_COMPLETE, StatusReceiveMaster.STATUS_REQUEST -> {
                        YesNoButtonDialog()
                            .setTitle(getString(R.string.cancel_request_master))
                            .setMessage(getString(R.string.do_you_want_cancel_request_master))
                            .setTextCancel(getString(R.string.no))
                            .setTextAccept(getString(R.string.yes))
                            .showDialog(
                                parentFragmentManager,
                                YesNoButtonDialog::class.java.simpleName
                            )
                            .setOnCallbackAcceptButtonListener {
                                vm.unReceiverMaster(idGuest)
                            }
                    }
                }
            }
        }
    }

    private fun receiveMaster(idMaster: Int) {
        val dialog = ReceiveMasterDialog()
        dialog.isCancelable = false
        dialog.nameMaster = userInfo?.fullName
        dialog.show(parentFragmentManager, dialog.tag)
        dialog.onSendReceiveMaster = ReceiveMasterDialog.OnSendReceiveMaster { msg ->
            vm.requestMaster(msg, idMaster)
        }
    }

    private fun List<View>.setEditProfileField(isEdit: Boolean) {
        this.forEach { view ->
            view.isEnabled = isEdit
            if (isEdit) {
                view.setBackgroundResource(R.drawable.bg_edit_profile)
            } else {
                view.background = null
            }
        }
    }

    private fun setViewIsMaster(isMaster: Boolean) {
//        lnDetailIndex.setVisibility(!isMaster)
        binding.layoutHeaderExerciseMaster.setVisibility(isMaster)
        binding.rvTrainerExerciseMasterInfo.setVisibility(isMaster)
        binding.lnDiscipleGenerateMasterUser.setVisibility(isMaster)
    }

    private fun disposeViewModel() {
        vm.apply {
            addDispose(
                isLoading.subscribe {
                    if (it) {
                        showDialog()
                    } else {
                        hideDialog()
                    }
                },
                rxMessage.subscribe { toast(it) },
                rxUserInfo.subscribe { user ->
                    updateInfo.run {
                        gender = user.gender
                        height = user.height
                        heightUnit = user.heightUnit
                        weight = user.weight
                        weightUnit = user.weightUnit
                        birthday = user.birthday
                        subjectId = user.subjectId
                    }
                    fillFormEditUser(updateInfo)
                    setViewIsMaster(user.isMaster == ApiConstants.MASTER)
                    if (user.isMaster == ApiConstants.MASTER) {
                        processTrainerData(user)
                    }
                    this@UserInfoFragment.userInfo = user
                    binding.lnViewGuestAddFriend.setVisibility(isGuest && idGuest != dataManager.getUserInfo()?.id)
                    binding.imgAvatarUserProfile.show(user.avatarPath)
                    binding.tvUserNameInfo.setTextNotNull(user.fullName)
                    binding.edtUserNameInfo.setValue(user.fullName ?: "")
                    if (user.subject != null) {
                        binding.txtSubjectInfoMaster.text = user.subject.name
                        binding.imageSubjectInfoMaster.show(user.subject.thumb)
                    } else {
                        binding.txtSubjectInfoMaster.text = getString(R.string.select_subject)
                    }
                    binding.tvRankingUserInfo.setTextNotNull(user.level?.title)

                    rewardAdapter.items = user.reward ?: mutableListOf()
                    binding.viewRewardUserInfo.setVisibility(user.reward?.isNotEmpty() == true)
                    binding.rcvRewardUserInfo.setVisibility(user.reward?.isNotEmpty() == true)
                    binding.noDataMyRewardProfile.root.setVisibility(user.reward?.isEmpty() == true)
                },
                updateSuccess.subscribe {
                    postNormal(EventUpdateInfo(it))
                }, rxStatusReceiveMaster.subscribe {
                    statusReceiveMaster = if (it) StatusReceiveMaster.STATUS_REQUEST
                    else null
                    postNormal(EventReceiveMaster(statusReceiveMaster))
                }, rxStatusUnReceiveMaster.subscribe {
                    statusReceiveMaster =
                        if (it) StatusReceiveMaster.STATUS_REJECT else StatusReceiveMaster.STATUS_COMPLETE
                    postNormal(EventReceiveMaster(statusReceiveMaster))
                }
            )
        }
    }

    private fun fillFormEditUser(updateInfo: UpdateInfo) {
        binding.tvEditGenderUserInfo.text = updateInfo.getGenderInfo()
        binding.tvEditWeightUserInfo.text = updateInfo.getFullWeight()
        binding.tvEditHeightUserInfo.text = updateInfo.getFullHeight()
        if (updateInfo.birthday != null) {
            binding.tvEditBirthdayUserInfo.text =
                DateTimeUtil.convertDate(updateInfo.birthday!!, "yyyy-MM-dd", "dd/MM/yyyy")
        } else {
            binding.tvEditBirthdayUserInfo.text = "--/--/----"
        }
    }

    private fun processTrainerData(userInfo: UserInfo) {
        userInfo.run {
            if (masterInfo != null) {
                binding.tvDiscipleCount.text = "${masterInfo.discipleCount ?: 0}"
                binding.tvLessonCount.text = "${masterInfo.lessonCount ?: 0}"
                binding.tvParticipants.text = "${masterInfo.participants ?: 0}"
                if (isGuest) statusReceiveMaster = masterInfo.statusReceiveMaster
            }
            if (practice?.isNotEmpty() == true) {
                adapterExercise.items = practice
                adapterExercise.onClickItem = CoachAdapter.OnItemClick { item, type ->
                    when (type) {
                        CoachAdapter.TYPE_SINGLE -> {
                            item.id?.let { PracticeDetailFragment.openFragment(it) }
                        }
                        CoachAdapter.TYPE_MULTI -> {
                            item.id?.let { PracticeFolderFragment.openFragment(it) }
                        }
                    }
                }
            } else {
                binding.layoutHeaderExerciseMaster.hide()
                binding.rvTrainerExerciseMasterInfo.hide()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST ->
                    // onResult Callback
                {
                    val selectList: MutableList<LocalMedia>? =
                        PictureSelector.obtainMultipleResult(data)
                    selectList?.let {
                        val media = selectList[0]
                        imagePath = PickerImageUtils.getPathImage(media)
                        binding.imgAvatarUserProfile.show(imagePath)

                        YesNoButtonDialog()
                            .setMessage(getString(R.string.title_alert_update_avatar))
                            .setTextAccept(getString(R.string.yes))
                            .setTextCancel(getString(R.string.cancel))
                            .showDialog(parentFragmentManager, YesNoButtonDialog.TAG)
                            .setOnCallbackAcceptButtonListener {
                                vm.updateAvatar(File(imagePath))
                            }.setOnCallbackCancelButtonListener {
                                binding.imgAvatarUserProfile.show(userInfo?.avatarPath)
                            }
                    }
                }
                else -> {
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        register(this)
    }

    override fun onStop() {
        super.onStop()
        unregister(this)
    }

    @Subscribe
    fun onUpdateProfileCallback(ev: EventUpdateProfile) {
        if (updateInfo.subjectId != ev.profile.subjectId) {
            vm.updateUserInfo(ev.profile)
        }
        updateInfo = ev.profile
        fillFormEditUser(ev.profile)
    }

    @Subscribe
    fun onLanguageChange(ev: EventChangeLanguage) {
        loadLanguageResource()
    }

    private fun loadLanguageResource() {
        binding.txtHeader.text = loadStringRes(R.string.label_profile)
        binding.txtLabelAddFriendUser.text = loadStringRes(R.string.add_friend)
        binding.txtLabelAddFriendUser.text = loadStringRes(R.string.add_friend)
        statusReceiveMaster = statusReceiveMaster
        binding.txtLabelInformationGeneral.text = loadStringRes(R.string.information_generate)
        binding.tvTitleDiscipleCount.text = loadStringRes(R.string.label_disciple)
        binding.tvTitleLessonCount.text = loadStringRes(R.string.lesson)
        binding.tvTitleParticipants.text = loadStringRes(R.string.participants_practice)
        binding.txtLabelIndex.text = loadStringRes(R.string.label_index)
        binding.txtLabelInformationForm.text = loadStringRes(R.string.file)

        binding.txtLabelProfileName.text = loadStringRes(R.string.name)
        binding.txtLabelProfileDateOfBirth.text = loadStringRes(R.string.date_of_birth)
        binding.txtLabelProfileGender.text = loadStringRes(R.string.gender)
        binding.txtLabelProfileHeight.text = loadStringRes(R.string.height)
        binding.txtLabelProfileWeight.text = loadStringRes(R.string.weight)

        binding.txtLabelAppellation.text = loadStringRes(R.string.appellation)
        binding.tvMyPractices.text = loadStringRes(R.string.exercise)
        binding.tvShowMore.text = loadStringRes(R.string.more)

        binding.noDataMyRewardProfile.txtNoData.text = loadStringRes(R.string.no_data)
    }
}