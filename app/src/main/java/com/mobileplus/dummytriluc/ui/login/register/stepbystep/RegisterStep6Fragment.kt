package com.mobileplus.dummytriluc.ui.login.register.stepbystep

import android.os.Bundle
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.UpdateInfo
import com.mobileplus.dummytriluc.databinding.FragmentRegisterStep6Binding
import com.mobileplus.dummytriluc.ui.login.register.RegisterViewModel
import com.mobileplus.dummytriluc.ui.login.register.adapter.SubjectAdapter
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.user.UserInfoFragment
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventUpdateProfile
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class RegisterStep6Fragment : BaseFragmentZ<FragmentRegisterStep6Binding>() {

    override fun getLayoutBinding(): FragmentRegisterStep6Binding =
        FragmentRegisterStep6Binding.inflate(layoutInflater)

    private val vm by sharedViewModel<RegisterViewModel>()
    private val subjectAdapter: SubjectAdapter by lazy { SubjectAdapter() }

    private val jsonRegister by argument(ARG_UPDATE_INFORMATION_STEP_6, "")
    private lateinit var updateInfo: UpdateInfo
    private val gson by inject<Gson>()

    private var isEditUser = false

    companion object {
        const val ARG_UPDATE_INFORMATION_STEP_6 = "ARG_UPDATE_INFORMATION_STEP_6"
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        checkArg()
        setUpRcv(binding.rcvSubjectStep6, subjectAdapter)
        binding.rcvSubjectStep6.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_8).toInt()
            )
        )
        disposableViewModel()
        initAction()
        vm.getSubjectList()
    }

    private fun checkArg() {
        if (arguments != null) {
            if (arguments?.containsKey(ARG_UPDATE_INFORMATION_STEP_6) == true) {
                isEditUser = false
                updateInfo = gson.fromJson(jsonRegister, UpdateInfo::class.java)
            }
            if (arguments?.containsKey(UserInfoFragment.ARG_EDIT_PROFILE) == true) {
                isEditUser = true
                updateInfo = gson.fromJson(
                    arguments?.getString(UserInfoFragment.ARG_EDIT_PROFILE),
                    UpdateInfo::class.java
                )
            }
        }
    }


    private fun disposableViewModel() {
        vm.apply {
            addDispose(
                isLoading.subscribe {
                    if (it) showDialog()
                    else hideDialog()
                },
                rxMessage.subscribe { toast(it) },
                subjectData.subscribe {
                    subjectAdapter.items = it.toMutableList()
                    if (updateInfo.subjectId != null) {
                        subjectAdapter.setCheckedItemByID(updateInfo.subjectId!!)
                    }
                },
                rxUpdateInfoSuccess.subscribe {
                    if (it) {
                        vm.setFirstConnect(true)
                        startActivity(MainActivity::class.java)
                        finish()
                    }
                }
            )
        }
    }

    private fun initAction() {
        binding.txtNextStep6.clickWithDebounce {
            if (subjectAdapter.getItemChecked() != null) {
                updateInfo.subjectId = subjectAdapter.getItemChecked()?.id ?: 1
                if (isEditUser) {
                    postNormal(EventUpdateProfile(updateInfo))
                    onBackPressed()
                } else {
                    vm.updateUserInfo(updateInfo)
                }
            } else {
                toast("Bạn chưa chọn bộ môn nào")
            }
        }
        binding.txtBackStep6.clickWithDebounce { onBackPressed() }
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
    fun onLanguageChange(ev: EventChangeLanguage) {
        loadLanguageResource()
    }

    private fun loadLanguageResource() {
        binding.header1.text = loadStringRes(R.string.label_favorite_subject)
        binding.txtNextStep6.text = loadStringRes(R.string.label_continue)
        binding.txtBackStep6.text = loadStringRes(R.string.label_back)
    }

}