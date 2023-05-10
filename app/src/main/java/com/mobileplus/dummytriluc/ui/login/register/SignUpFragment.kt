package com.mobileplus.dummytriluc.ui.login.register

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import com.core.BaseFragmentZ
import com.google.firebase.messaging.FirebaseMessaging
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.RegisterRequest
import com.mobileplus.dummytriluc.databinding.FragmentRegisterBinding
import com.mobileplus.dummytriluc.ui.login.main.LoginActivity
import com.mobileplus.dummytriluc.ui.login.register.stepbystep.RegisterStep1Fragment
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentLogin
import com.mobileplus.dummytriluc.ui.utils.extensions.deviceId
import com.mobileplus.dummytriluc.ui.utils.extensions.isEmail
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.KeyboardUtils
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class SignUpFragment : BaseFragmentZ<FragmentRegisterBinding>() {
    private val vm by viewModel<RegisterViewModel>()

    override fun getLayoutBinding(): FragmentRegisterBinding =
        FragmentRegisterBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        disposeViewModel()
        initAction()
    }

    private fun configLoginText() {
        val str = loadStringRes(R.string.question_navigate_login)
        val wordToSpan: Spannable = SpannableString(str)
        wordToSpan.setSpan(
            ForegroundColorSpan(
                ResourcesCompat.getColor(
                    resources,
                    R.color.white,
                    null
                )
            ),
            0,
            str.indexOf("?") + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        wordToSpan.setSpan(
            StyleSpan(Typeface.BOLD),
            str.indexOf("?") + 1,
            str.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                startActivity(LoginActivity::class.java)
                finish()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = Color.parseColor("#E32828")
            }
        }

        wordToSpan.setSpan(
            clickableSpan,
            str.indexOf("?") + 1,
            str.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvLogin.text = wordToSpan
        binding.tvLogin.movementMethod = LinkMovementMethod.getInstance()
        binding.tvLogin.highlightColor = Color.TRANSPARENT
    }

    private fun initAction() {
        binding.btnBackSignUp.clickWithDebounce { onBackPressed() }
        binding.tvSignUp.clickWithDebounce {
            validData()
        }
    }

    private fun validData() {
        hideKeyboard()
        binding.parent.requestFocus()
        val fullName = binding.editFullName.text.toString()
        val email = binding.editEmail.text.toString()
        val password = binding.editPassword.text.toString()
        val confirmPassword = binding.editConfirmPassword.text.toString()
        if (fullName.isBlank()) {
            toast(loadStringRes(R.string.error_empty_fullname))
            showKeyBoard(binding.editFullName)
            return
        }
        if (email.isBlank()) {
            toast(loadStringRes(R.string.error_empty_email))
            showKeyBoard(binding.editEmail)
            return
        } else {
            if (!email.isEmail()) {
                toast(loadStringRes(R.string.error_wrong_email))
                showKeyBoard(binding.editEmail)
                return
            }
        }
        if (password.isBlank()) {
            toast(loadStringRes(R.string.error_empty_password))
            showKeyBoard(binding.editPassword)
            return
        }
        if (confirmPassword.isBlank()) {
            toast(loadStringRes(R.string.error_empty_confirm_password))
            showKeyBoard(binding.editConfirmPassword)
            return
        } else {
            if (confirmPassword != password) {
                toast(loadStringRes(R.string.error_wrong_confirm_password))
                showKeyBoard(binding.editConfirmPassword)
                return
            }
        }

        val request = RegisterRequest(
            fullName,
            email,
            password,
            confirmPassword,
            deviceId
        )
        showDialog()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            hideDialog()
            request.tokenPush = if (task.isSuccessful) task.result else "fake_token"
            vm.registerUser(request)
        }.addOnFailureListener { e ->
            hideDialog()
            request.tokenPush = "fake_token"
            vm.registerUser(request)
            e.logErr()
        }
    }

    private fun showKeyBoard(editText: EditText) {
        KeyboardUtils.showKeyboard(requireActivity())
        editText.requestFocus()
    }

    private fun disposeViewModel() {
        addDispose(
            vm.isLoading.subscribe {
                if (it) showDialog()
                else hideDialog()
            },
            vm.rxRegisterNextStep.subscribe {
                if (it) {
                    postNormal(
                        EventNextFragmentLogin(
                            RegisterStep1Fragment::class.java,
                            true
                        )
                    )
                }
            },
            vm.rxOpenMain.subscribe {
                if (it) {
                    startActivity(MainActivity::class.java)
                    finish()
                }
            },
            vm.rxMessage.subscribe { toast(it) },
        )
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
        configLoginText()
        binding.tvHeader.text = loadStringRes(R.string.label_register)
        binding.tvHeader2.text = loadStringRes(R.string.label_register_account)
        binding.editFullName.hint = loadStringRes(R.string.hint_full_name)
        binding.editEmail.hint = loadStringRes(R.string.email)
        binding.editPassword.hint = loadStringRes(R.string.password)
        binding.editConfirmPassword.hint = loadStringRes(R.string.hint_confirm_password)
        binding.tvSignUp.text = loadStringRes(R.string.label_register)
        binding.btnBackSignUp.text = loadStringRes(R.string.back)
    }

}