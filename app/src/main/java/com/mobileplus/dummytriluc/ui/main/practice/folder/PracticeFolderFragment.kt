package com.mobileplus.dummytriluc.ui.main.practice.folder

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.response.DetailPracticeFolderResponse
import com.mobileplus.dummytriluc.data.response.MediaPractice
import com.mobileplus.dummytriluc.databinding.FragmentPracticeMasterBinding
import com.mobileplus.dummytriluc.transceiver.command.MachineFreePunchCommand
import com.mobileplus.dummytriluc.transceiver.command.MachineLedPunchCommand
import com.mobileplus.dummytriluc.transceiver.command.MachineLessonCommand
import com.mobileplus.dummytriluc.ui.dialog.ReceiveMasterDialog
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailFragment
import com.mobileplus.dummytriluc.ui.main.practice.detail.adapter.LevelPracticeAdapter
import com.mobileplus.dummytriluc.ui.main.practice.folder.adapter.PracticeFolderAdapter
import com.mobileplus.dummytriluc.ui.main.practice.test.PracticeTestFragment
import com.mobileplus.dummytriluc.ui.main.practice.test.dialog.SelectMethodPracticeDialog
import com.mobileplus.dummytriluc.ui.main.practice.video.PracticeWithVideoFragment
import com.mobileplus.dummytriluc.ui.main.user.UserInfoFragment
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.StatusReceiveMaster
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.ext.*
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by KOHuyn on 3/9/2021
 */
class PracticeFolderFragment : BaseFragmentZ<FragmentPracticeMasterBinding>() {
    override fun getLayoutBinding(): FragmentPracticeMasterBinding =
        FragmentPracticeMasterBinding.inflate(layoutInflater)

    private val roundNumberAdapter by lazy { LevelPracticeAdapter() }
    private var roundNumber: Int = 1
    private val practiceFolderViewModel by viewModel<PracticeFolderViewModel>()
    private var responseDetailFolder: DetailPracticeFolderResponse? = null
    private val page: Page by lazy { Page() }
    private val adapter by lazy { PracticeFolderAdapter() }
    private val btnReceiveMaster by lazy { binding.btnReceiveMasterPractice }
    private var statusReceiveMaster: Int? = null
        set(value) {
            field = value
            when (field) {
                null, StatusReceiveMaster.STATUS_REJECT -> {
                    btnReceiveMaster.setBackgroundResource(R.drawable.gradient_orange)
                    btnReceiveMaster.text = getText(R.string.receive_master)
                }

                StatusReceiveMaster.STATUS_BLOCK -> {
                    btnReceiveMaster.hide()
                }

                StatusReceiveMaster.STATUS_COMPLETE -> {
                    btnReceiveMaster.setBackgroundResource(R.color.clr_tab)
                    btnReceiveMaster.text = getText(R.string.unreceive_master)
                }

                StatusReceiveMaster.STATUS_REQUEST -> {
                    btnReceiveMaster.text = loadStringRes(R.string.wait_for_confirmation)
                    btnReceiveMaster.setBackgroundResource(R.color.clr_tab)
                }
            }
        }

    companion object {
        private const val ID_PRACTICE_ARG = "ID_PRACTICE_ARG"
        fun openFragment(id: Int) {
            val bundle = Bundle().apply {
                putInt(ID_PRACTICE_ARG, id)
            }
            postNormal(EventNextFragmentMain(PracticeFolderFragment::class.java, bundle, true))
        }
    }

    private val idFolder: Int by argument(ID_PRACTICE_ARG, -1)

    override fun updateUI(savedInstanceState: Bundle?) {
        disposableViewModel()
        practiceFolderViewModel.getDetailPracticeFolder(idFolder)
        practiceFolderViewModel.getListItemFolder(idFolder)
        configRcv()
        handleClick()
    }

    private fun configRcv() {
        setUpRcv(binding.rcvPracticeFolder, adapter, false)
        binding.rcvPracticeFolder.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_8).toInt()
            )
        )
        roundNumberAdapter.items = mutableListOf(1, 2, 3, 4, 5)
        setUpRcv(binding.rcvLevelPractice, roundNumberAdapter)
        binding.rcvLevelPractice.show()

        binding.nestedScrollPracticeFolder.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                if (v.getChildAt(v.childCount - 1) != null) {
                    if ((scrollY >= (v.getChildAt(v.childCount - 1)).measuredHeight - v.measuredHeight) && scrollY > oldScrollY) {
                        page.run {
                            if (currPage < totalPage && !isLoading) {
                                isLoading = true
                                currPage++
                                practiceFolderViewModel.getListItemFolder(idFolder, currPage)
                            }
                        }
                    }
                }
            })
    }

    private fun handleClick() {
        binding.btnBackPracticeMaster.clickWithDebounce { onBackPressed() }
        binding.cbViewMoreContentPracticeMaster.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.text = loadStringRes(R.string.more)
                binding.txtDescriptionPracticeMaster.maxLines = 4
            } else {
                buttonView.text = loadStringRes(R.string.collapse)
                binding.txtDescriptionPracticeMaster.maxLines = Int.MAX_VALUE
            }
        }
        binding.btnStartPracticeMain.setOnClickListener {
            roundNumber = roundNumberAdapter.getLevelCurrent() ?: 1
            openFragmentPractice()
        }
        adapter.onClickFolder = PracticeFolderAdapter.OnClickFolder { item, type ->
            when (type) {
                PracticeFolderAdapter.TYPE_SINGLE -> {
                    item.id?.let { PracticeDetailFragment.openFragment(it) }
                }

                PracticeFolderAdapter.TYPE_MULTI -> {
                    item.id?.let { Companion.openFragment(it) }
                }
            }
        }
        btnReceiveMaster.clickWithDebounce {
            val idMaster = responseDetailFolder?.userCreated?.id
            if (idMaster != null) {
                when (statusReceiveMaster) {
                    null, StatusReceiveMaster.STATUS_REJECT -> {
                        receiveMaster(idMaster)
                    }

                    StatusReceiveMaster.STATUS_COMPLETE, StatusReceiveMaster.STATUS_REQUEST -> {
                        YesNoButtonDialog()
                            .setTitle(getString(R.string.unreceive_master))
                            .setMessage(getString(R.string.do_you_want_unreceive_master))
                            .setTextCancel(getString(R.string.no))
                            .setTextAccept(getString(R.string.yes))
                            .showDialog(
                                parentFragmentManager,
                                YesNoButtonDialog::class.java.simpleName
                            )
                            .setOnCallbackAcceptButtonListener {
                                practiceFolderViewModel.unReceiverMaster(idMaster)
                            }
                    }
                }
            }
        }
        binding.lnMasterPractice.clickWithDebounce {
            if (practiceFolderViewModel.dataManager.getUserInfo()?.id == responseDetailFolder?.userId) {
                UserInfoFragment.openInfoUser()
            } else {
                responseDetailFolder?.userId?.let { userId -> UserInfoFragment.openInfoGuest(userId) }
            }
        }
    }

    private fun openFragmentPractice() {
        if ((requireActivity() as MainActivity).isConnectedBle) {
            val data = responseDetailFolder
            if (data != null) {
                val level = data.level
                    ?.let { it.title to it.id }
                    ?.let { (level, value) ->
                        level to value
                    }
                val dataIdLesson = adapter.items.map {
                    MachineLessonCommand.LessonWithVideoPath(
                        lessonId = it.id ?: -1,
                        videoPath = it.videoPathReal
                    )
                }
                val command = MachineLessonCommand(
                    lessonId = dataIdLesson,
                    round = roundNumber,
                    courseId = data.id,
                    isPlayWithVideo = false,
                    level = level,
                    avgHit = 20,
                    avgPower = 50
                )
                SelectMethodPracticeDialog()
                    .onPracticeNormal {
                        PracticeTestFragment.openFragment(command.copy(isPlayWithVideo = false))
                    }
                    .onPracticeWithVideo {
                        PracticeWithVideoFragment.openFragment(command.copy(isPlayWithVideo = true))
                    }
                    .show(parentFragmentManager, "")
            } else {
                toast(loadStringRes(R.string.feature_not_available))
            }
        } else {
            (activity as MainActivity).showDialogRequestConnect()
        }
    }

    private fun disposableViewModel() {
        practiceFolderViewModel.run {
            addDispose(isLoading.subscribe { if (it) showDialog() else hideDialog() })
            addDispose(rxMessage.subscribe { toast(it) })
            addDispose(rxDetailResponse.subscribe { detail ->
                btnReceiveMaster.setVisibility(dataManager.getUserInfo()?.id != detail.userCreated?.id)
                binding.nestedScrollPracticeFolder.show()
                responseDetailFolder = detail
                binding.txtLabelFolder.setTextNotNull(detail.title)
                detail.userCreated?.let { user ->
                    binding.imgAvatarPracticeMaster.show(user.avatarPath)
                    binding.txtNamePracticeMaster.setTextNotNull(user.fullName)
                    statusReceiveMaster = user.statusReceiveMaster
                }
                binding.txtLevelPracticeMasterPractice.setTextNotNull(detail.getLevelFolder())
                binding.txtNameSubjectMasterPractice.setTextNotNull(detail.getSubjectFolder())
                binding.txtDateCreatedMasterPractice.setTextNotNull(detail.getCreatedAtFolder())
                binding.imgDescriptionPracticeMaster.show(detail.imagePath)
                detail.content?.let {
                    binding.txtDescriptionPracticeMaster.setHtmlWithImage(
                        it,
                        requireContext()
                    )
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.cbViewMoreContentPracticeMaster?.setVisibility(binding.txtDescriptionPracticeMaster.lineCount >= 4)
                }, 500)
                setVisibleViewWhen(
                    binding.lnMasterPractice,
                    binding.titleMasterFolder
                ) { detail?.userCreated != null }
            })
            addDispose(rxItemsInFolder.subscribe {
                val items = it.first
                val pageResponse = it.second
                pageResponse?.run {
                    page.currPage = currPage
                    page.totalPage = totalPage
                    page.isLoading = false
                }
                if (adapter.items.isEmpty()) {
                    adapter.items = items.toMutableList()
                } else {
                    adapter.items.addAll(items)
                }
                binding.rcvPracticeFolder.requestLayout()
            })
            addDispose(rxLoadMore.subscribe {
                binding.loadMoreFolder.setVisibility(it)
            })
            addDispose(rxStatusReceiveMaster.subscribe {
                statusReceiveMaster = if (it) StatusReceiveMaster.STATUS_REQUEST
                else null
            }, rxStatusUnReceiveMaster.subscribe {
                statusReceiveMaster =
                    if (it) StatusReceiveMaster.STATUS_REJECT else StatusReceiveMaster.STATUS_COMPLETE
            })
        }
    }

    private fun receiveMaster(idMaster: Int) {
        val dialog = ReceiveMasterDialog()
        dialog.isCancelable = false
        dialog.nameMaster = responseDetailFolder?.userCreated?.fullName ?: "unknown"
        dialog.show(parentFragmentManager, dialog.tag)
        dialog.onSendReceiveMaster = ReceiveMasterDialog.OnSendReceiveMaster { msg ->
            practiceFolderViewModel.requestMaster(msg, idMaster)
        }
    }
}