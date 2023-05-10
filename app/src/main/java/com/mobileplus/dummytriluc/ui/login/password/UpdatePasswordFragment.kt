package com.mobileplus.dummytriluc.ui.login.password

import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.databinding.FragmentUpdatePasswordBinding
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.KeyboardUtils.showKeyboard
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.register
import com.utils.ext.unregister
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class UpdatePasswordFragment : BaseFragmentZ<FragmentUpdatePasswordBinding>() {

    private val vm: PasswordViewModel by viewModel()

    override fun getLayoutBinding(): FragmentUpdatePasswordBinding =
        FragmentUpdatePasswordBinding.inflate(layoutInflater)

    companion object {
        fun openFragment() {
            postNormal(
                EventNextFragmentMain(
                    UpdatePasswordFragment::class.java,
                    true
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        disposeViewModel()
        initAction()
    }

    private fun initAction() {
        binding.btnConfirmPassword.clickWithDebounce {
            valid()
        }
        binding.txtBackUpdatePassword.clickWithDebounce { onBackPressed() }
    }

    private fun valid() {
        hideKeyboard()
        val oldPassword = binding.editOldPassword.text.toString()
        val newPassword = binding.editNewPassword.text.toString()
        val confirmPassword = binding.editConfirmPassword.text.toString()
        if (oldPassword.isBlank()) {
            toast(loadStringRes(R.string.error_empty_old_password))
            showKeyboard(binding.editOldPassword)
            return
        }
        if (newPassword.isBlank()) {
            toast(loadStringRes(R.string.error_empty_new_password))
            showKeyboard(binding.editNewPassword)
            return
        }

        if (confirmPassword.isBlank()) {
            toast(loadStringRes(R.string.error_empty_confirm_new_password))
            showKeyboard(binding.editConfirmPassword)
            return
        } else {
            if (confirmPassword != newPassword) {
                toast(loadStringRes(R.string.error_wrong_confirm_password))
                showKeyboard(binding.editConfirmPassword)
                return
            }
        }
        vm.updatePassword(oldPassword, newPassword)
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
                        onBackPressed()
                    }
                }
            )
        }
    }

    private fun showKeyboard(editText: EditText) {
        showKeyboard(requireActivity())
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
        binding.editOldPassword.hint = loadStringRes(R.string.hint_old_password)
        binding.editNewPassword.hint = loadStringRes(R.string.hint_new_password)
        binding.editConfirmPassword.hint = loadStringRes(R.string.hint_confirm_password)
        binding.btnConfirmPassword.text = loadStringRes(R.string.label_confirm)
        binding.txtBackUpdatePassword.text = loadStringRes(R.string.back)
    }

}