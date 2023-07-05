package com.mobileplus.dummytriluc.ui.main.practice.test.dialog

import android.os.Bundle
import android.view.LayoutInflater
import com.core.BaseDialogBinding
import com.mobileplus.dummytriluc.databinding.DialogSelectMethodPracticeBinding

/**
 * Created by KO Huyn on 05/07/2023.
 */
class SelectMethodPracticeDialog : BaseDialogBinding<DialogSelectMethodPracticeBinding>() {

    private var onClickPracticeWithVideo: () -> Unit = {}
    private var onClickPracticeNormal: () -> Unit = {}

    override fun getLayoutBinding(inflater: LayoutInflater): DialogSelectMethodPracticeBinding {
        return DialogSelectMethodPracticeBinding.inflate(inflater)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        binding.txtPracticeNormal.setOnClickListener {
            onClickPracticeNormal()
            dismiss()
        }
        binding.txtPracticeWithVideo.setOnClickListener {
            onClickPracticeWithVideo()
            dismiss()
        }
    }

    fun onPracticeWithVideo(callback: () -> Unit) = apply {
        onClickPracticeWithVideo = callback
    }

    fun onPracticeNormal(callback: () -> Unit) = apply {
        onClickPracticeNormal = callback
    }
}