package com.mobileplus.dummytriluc.ui.login.password.change

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.databinding.FragmentChangePasswordBinding
import com.mobileplus.dummytriluc.ui.login.main.LoginActivity
import com.mobileplus.dummytriluc.ui.login.password.PasswordViewModel
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPopForgotPassword
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.KeyboardUtils
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class ChangePasswordFragment : BaseFragmentZ<FragmentChangePasswordBinding>() {
    override fun getLayoutBinding(): FragmentChangePasswordBinding =
        FragmentChangePasswordBinding.inflate(layoutInflater)

    private val vm: PasswordViewModel by viewModel()
    private val email: String by argument(ARG_EMAIL, "")

    companion object {
        const val ARG_EMAIL = "ARG_EMAIL"
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        disposeViewModel()
        initAction()
    }

    private fun initAction() {
        hideKeyboardOutSide(binding.root)
        binding.btnConfirmPassword.clickWithDebounce {
            validData()
        }
        binding.tvCancel.clickWithDebounce {
            popToLoginMain()
        }
    }

    private fun popToLoginMain(){
        (activity as? LoginActivity)?.popBackToMainLogin() ?: onBackPressed()
    }

    private fun validData() {
        val newPassword = binding.editNewPassword.text.toString()
        val confirmPassword = binding.editConfirmPassword.text.toString()
        if (binding.editCode.text.toString().isBlank()) {
            toast(loadStringRes(R.string.errror_empty_code))
            showKeyBoard(binding.editCode)
            return
        }

        if (newPassword.isBlank()) {
            toast(loadStringRes(R.string.error_empty_new_password))
            showKeyBoard(binding.editNewPassword)
            return
        }

        if (confirmPassword.isBlank()) {
            toast(loadStringRes(R.string.error_empty_confirm_new_password))
            showKeyBoard(binding.editConfirmPassword)
            return
        }

        if (confirmPassword != newPassword) {
            toast(loadStringRes(R.string.error_wrong_confirm_password))
            showKeyBoard(binding.editConfirmPassword)
            return
        }

        if (email != "") {
            vm.changePassword(
                email,
                binding.editCode.text.toString(),
                newPassword,
                confirmPassword
            )
            hideKeyboard()
        }
    }

    private fun disposeViewModel() {
        vm.run {
            addDispose(
                isLoading.subscribe {
                    if (it) {
                        showDialog()
                    } else {
                        hideDialog()
                    }
                },
                rxMessage.subscribe {
                    toast(it)
                },
                updateSuccess.subscribe {
                    if (it) {
                        popToLoginMain()
                    }
                }
            )
        }
    }

    private fun showKeyBoard(editText: View) {
        KeyboardUtils.showKeyboard(requireActivity())
        editText.requestFocus()
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
        binding.tvHeader.text = loadStringRes(R.string.label_change_password)
        binding.editCode.hint = loadStringRes(R.string.hint_input_code)
        binding.editNewPassword.hint = loadStringRes(R.string.hint_new_password)
        binding.editConfirmPassword.hint = loadStringRes(R.string.hint_confirm_password)
        binding.btnConfirmPassword.text = loadStringRes(R.string.label_confirm)
        binding.tvCancel.text = loadStringRes(R.string.label_back)
    }
}