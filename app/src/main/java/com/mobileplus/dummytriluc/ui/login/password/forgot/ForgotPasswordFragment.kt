package com.mobileplus.dummytriluc.ui.login.password.forgot

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.databinding.FragmentForgotPasswordBinding
import com.mobileplus.dummytriluc.ui.login.password.PasswordViewModel
import com.mobileplus.dummytriluc.ui.login.password.change.ChangePasswordFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentLogin
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPopForgotPassword
import com.mobileplus.dummytriluc.ui.utils.extensions.isEmail
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.KeyboardUtils
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.register
import com.utils.ext.unregister
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class ForgotPasswordFragment : BaseFragmentZ<FragmentForgotPasswordBinding>() {

    override fun getLayoutBinding(): FragmentForgotPasswordBinding =
        FragmentForgotPasswordBinding.inflate(layoutInflater)

    private val vm by viewModel<PasswordViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        disposeViewModel()
        initAction()
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
                startCount.subscribe {
                    val bundle = Bundle()
                    bundle.putString(
                        ChangePasswordFragment.ARG_EMAIL,
                        binding.editEmail.text.toString()
                    )
                    postNormal(
                        EventNextFragmentLogin(
                            ChangePasswordFragment::class.java,
                            bundle,
                            true
                        )
                    )
                }
            )
        }
    }

    private fun initAction() {
        binding.tvSend.clickWithDebounce {
            val email = binding.editEmail.text.toString()
            hideKeyboard()
            if (email.isBlank()) {
                toast(loadStringRes(R.string.error_empty_email))
                showKeyBoard(binding.editEmail)
                return@clickWithDebounce
            }
            if (!email.isEmail()) {
                toast(loadStringRes(R.string.error_wrong_email))
                showKeyBoard(binding.editEmail)
                return@clickWithDebounce
            }
            vm.requestCode(email)
        }
        binding.tvCancel.clickWithDebounce { onBackPressed() }
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
    fun popStackCurrFrag(ev: EventPopForgotPassword) {
        Handler(Looper.getMainLooper()).postDelayed({ onBackPressed() }, 500)
    }

    private fun showKeyBoard(editText: View) {
        KeyboardUtils.showKeyboard(requireActivity())
        editText.requestFocus()
    }

    @Subscribe
    fun onLanguageChange(ev: EventChangeLanguage) {
        loadLanguageResource()
    }

    private fun loadLanguageResource() {
        binding.tvHeader.text = loadStringRes(R.string.label_forgot_password)
        binding.tvHeader2.text = loadStringRes(R.string.hint_forgot_password)
        binding.editEmail.hint = loadStringRes(R.string.email)
        binding.tvSend.text = loadStringRes(R.string.label_send_code)
        binding.tvCancel.text = loadStringRes(R.string.cancel)
    }
}