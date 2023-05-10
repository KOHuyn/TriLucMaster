package com.mobileplus.dummytriluc.ui.main.practice.detail.question

import android.os.Bundle
import android.view.View
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.response.DetailPracticeResponse
import com.mobileplus.dummytriluc.data.response.UserCreatePractice
import com.mobileplus.dummytriluc.databinding.LayoutPracticeDetailQuestionTopBinding
import com.mobileplus.dummytriluc.ui.dialog.ReceiveMasterDialog
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailViewModel
import com.mobileplus.dummytriluc.ui.main.user.UserInfoFragment
import com.mobileplus.dummytriluc.ui.utils.StatusReceiveMaster
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventEnableTopPractice
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventReceiveMaster
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.setVisibleViewWhen
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.applyClickShrink
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.sharedViewModel

class PracticeDetailQuestionTopFragment : BaseFragmentZ<LayoutPracticeDetailQuestionTopBinding>() {
    override fun getLayoutBinding(): LayoutPracticeDetailQuestionTopBinding =
        LayoutPracticeDetailQuestionTopBinding.inflate(layoutInflater)

    private val viewModel by sharedViewModel<PracticeDetailViewModel>()
    private var masterUser: UserCreatePractice? = null
    private var statusReceiveMaster: Int? = null
        set(value) {
            field = value
            when (field) {
                null, StatusReceiveMaster.STATUS_REJECT -> {
                    binding.btnReceiveMaster.setBackgroundResource(R.drawable.gradient_orange)
                    binding.btnReceiveMaster.text = getText(R.string.receive_master)
                }
                StatusReceiveMaster.STATUS_BLOCK -> {
                    binding.btnReceiveMaster.hide()
                }
                StatusReceiveMaster.STATUS_COMPLETE -> {
                    binding.btnReceiveMaster.setBackgroundResource(R.drawable.gradient_orange)
                    binding.btnReceiveMaster.text = getText(R.string.unreceive_master)
                }
                StatusReceiveMaster.STATUS_REQUEST -> {
                   binding. btnReceiveMaster.text = loadStringRes(R.string.wait_for_confirmation)
                   binding. btnReceiveMaster.setBackgroundResource(R.color.clr_grey)
                }
            }
        }

    fun setDataQuestionTop(data: DetailPracticeResponse) {
       binding. txtTitleQuestionTop.text = data.title
       binding. txtNameExerciseQuestionTop.text = String.format(getString(R.string.value_of_exercise), data.title)
       binding. txtLevelQuestionTop.text = String.format(getString(R.string.value_of_level), data.level?.title ?: "---")
       binding. txtSubjectQuestionTop.text =
            String.format(getString(R.string.value_of_subject), data.subject?.title ?: getString(R.string.system))
        setVisibleViewWhen(binding.txtSubjectQuestionTop) { data.subject?.title != null }
        binding. txtCreateAtQuestionTop.text =
            String.format(getString(R.string.value_of_date_created), data.getCreatedAt() ?: "--/--/----")
        binding.txtCountPeoplePracticeQuestionTop.text =
            String.format(getString(R.string.value_of_number_people_practice), data.trainingNumber ?: 0)
        data.userCreate?.avatarPath?.let { binding.imgAvatarCoachQuestionTop.show(it) }
        binding.txtNameCoachQuestionTop.setTextNotNull(data.userCreate?.fullName)
        setVisibleViewWhen(binding.lnMasterQuestionTop, binding.titleMasterQuestionTop) { data.userCreate != null }
        binding.btnReceiveMaster.setVisibility(data.userCreate?.id != viewModel.userInfo?.id)
        masterUser = data.userCreate
        statusReceiveMaster = data.userCreate?.statusReceiveMaster
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
//        if (parentFragment is PracticeDetailFragment)
//            (parentFragment as PracticeDetailFragment).hideKeyBoardWhenClick(view,lnMasterQuestionTop)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel(viewModel)
       binding.btnReceiveMaster.applyClickShrink()
       binding.visibilityQuestionTop.clickWithDebounce { postNormal(EventEnableTopPractice(false)) }
       binding.layoutHideQuestionTop.clickWithDebounce { postNormal(EventEnableTopPractice(true)) }
       binding.btnReceiveMaster.clickWithDebounce {
            when (statusReceiveMaster) {
                null, StatusReceiveMaster.STATUS_REJECT -> {
                    receiveMaster()
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
                            masterUser?.id?.let { viewModel.unReceiverMaster(it) }
                        }
                }
            }
        }
        binding. lnMasterQuestionTop.clickWithDebounce {
            if (viewModel.dataManager.getUserInfo()?.id == masterUser?.id) {
                UserInfoFragment.openInfoUser()
            } else {
                masterUser?.id?.let { idMaster ->
                    UserInfoFragment.openInfoGuest(idMaster)
                }
            }
        }
    }

    private fun callbackViewModel(vm: PracticeDetailViewModel) {
        addDispose(vm.isLoading.subscribe { if (it) showDialog() else hideDialog() })
        addDispose(vm.rxMessage.subscribe { toast(it) })
        addDispose(vm.rxStatusReceiveMaster.subscribe { isSuccess ->
            statusReceiveMaster = if (isSuccess) StatusReceiveMaster.STATUS_REQUEST
            else null
        })
        addDispose(vm.rxStatusUnReceiveMaster.subscribe { isSuccess ->
            statusReceiveMaster =
                if (isSuccess) StatusReceiveMaster.STATUS_REJECT else StatusReceiveMaster.STATUS_COMPLETE
        })
    }

    private fun receiveMaster() {
        val dialog = ReceiveMasterDialog()
        dialog.isCancelable = false
        dialog.nameMaster = masterUser?.fullName
        dialog.show(parentFragmentManager, dialog.tag)
        dialog.onSendReceiveMaster = ReceiveMasterDialog.OnSendReceiveMaster { msg ->
            masterUser?.id?.let {
                viewModel.requestMaster(msg, it)
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
    fun statusReceiveResponse(ev: EventReceiveMaster) {
        statusReceiveMaster = ev.statusReceiveMaster
    }

    @Subscribe
    fun hideShowLayout(ev: EventEnableTopPractice) {
       binding. layoutShowQuestionTop.setVisibility(ev.isEnable)
       binding. layoutHideQuestionTop.setVisibility(!ev.isEnable)
    }


}