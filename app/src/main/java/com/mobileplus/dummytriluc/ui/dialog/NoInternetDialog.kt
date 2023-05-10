package com.mobileplus.dummytriluc.ui.dialog

import android.os.Bundle
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.dialog_no_internet.*

/**
 * Created by KOHuyn on 1/26/2021
 */
class NoInternetDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_no_internet

    override fun updateUI(savedInstanceState: Bundle?) {
        btnUnderstandNoInternetDialog.clickWithDebounce { dismiss() }
        imgCancelNoInternet.clickWithDebounce { dismiss() }
        skipNoInternetDialog.clickWithDebounce { dismiss() }
    }
}