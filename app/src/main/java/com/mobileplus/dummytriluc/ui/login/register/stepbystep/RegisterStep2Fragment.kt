package com.mobileplus.dummytriluc.ui.login.register.stepbystep

import android.os.Bundle
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.UpdateInfo
import com.mobileplus.dummytriluc.databinding.FragmentRegisterStep2Binding
import com.mobileplus.dummytriluc.ui.main.user.UserInfoFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentLogin
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventUpdateProfile
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.register
import com.utils.ext.unregister
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject

class RegisterStep2Fragment : BaseFragmentZ<FragmentRegisterStep2Binding>() {

    override fun getLayoutBinding(): FragmentRegisterStep2Binding =
        FragmentRegisterStep2Binding.inflate(layoutInflater)

    private lateinit var updateInfo: UpdateInfo
    private var isEditUser = false
    private val gson: Gson by inject()
    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        checkArg()
        initAction()
    }

    private fun checkArg() {
        if (arguments != null && arguments?.containsKey(UserInfoFragment.ARG_EDIT_PROFILE) == true) {
            isEditUser = true
            updateInfo = gson.fromJson(
                arguments?.getString(UserInfoFragment.ARG_EDIT_PROFILE),
                UpdateInfo::class.java
            )
            when (updateInfo.gender) {
                UpdateInfo.MALE -> binding.rbMaleStep2.isChecked = true
                UpdateInfo.FEMALE -> binding.rbFemaleStep2.isChecked = true
                UpdateInfo.OTHER -> binding.rbOtherStep2.isChecked = true
            }
        } else {
            isEditUser = false
            updateInfo = UpdateInfo()
        }
    }

    private fun validateGender(): Boolean {
        if (binding.rbMaleStep2.isChecked || binding.rbFemaleStep2.isChecked || binding.rbOtherStep2.isChecked) {
            when {
                binding.rbMaleStep2.isChecked -> {
                    updateInfo.gender = UpdateInfo.MALE
                }
                binding.rbFemaleStep2.isChecked -> {
                    updateInfo.gender = UpdateInfo.FEMALE
                }
                binding.rbOtherStep2.isChecked -> {
                    updateInfo.gender = UpdateInfo.OTHER
                }
            }
            return true
        } else {
            toast(loadStringRes(R.string.error_require_select_gender))
            return false
        }
    }

    private fun initAction() {
        binding.txtBackStep2.clickWithDebounce {
            onBackPressed()
        }

        binding.txtNextStep2.clickWithDebounce {
            if (validateGender()) {
                if (isEditUser) {
                    postNormal(EventUpdateProfile(updateInfo))
                    onBackPressed()
                } else {
                    val bundle = Bundle().apply {
                        putString(
                            RegisterStep3Fragment.ARG_UPDATE_INFORMATION_STEP_3,
                            gson.toJson(updateInfo)
                        )
                    }
                    postNormal(
                        EventNextFragmentLogin(
                            RegisterStep3Fragment::class.java,
                            bundle,
                            true
                        )
                    )
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
    fun onLanguageChange(ev: EventChangeLanguage) {
        loadLanguageResource()
    }

    private fun loadLanguageResource() {
        binding.labelChooseGender.text = loadStringRes(R.string.label_select_gender)
        binding.rbMaleStep2.text = loadStringRes(R.string.gender_male)
        binding.rbFemaleStep2.text = loadStringRes(R.string.gender_female)
        binding.rbOtherStep2.text = loadStringRes(R.string.gender_other)
        binding.txtNextStep2.text = loadStringRes(R.string.label_continue)
        binding.txtBackStep2.text = loadStringRes(R.string.label_back)
    }
}