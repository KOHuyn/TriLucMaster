package com.mobileplus.dummytriluc.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import com.core.BaseDialogBinding
import com.mobileplus.dummytriluc.data.model.TargetType
import com.mobileplus.dummytriluc.data.model.TargetUnit
import com.mobileplus.dummytriluc.databinding.DialogSetupTargetBinding
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.utils.ext.clickWithDebounce
import com.utils.ext.onTextChanged

/**
 * Created by KO Huyn on 05/06/2023.
 */
class SetupTargetDialog : BaseDialogBinding<DialogSetupTargetBinding>() {
    override fun getLayoutBinding(inflater: LayoutInflater): DialogSetupTargetBinding {
        return DialogSetupTargetBinding.inflate(inflater)
    }

    override fun isCancelable(): Boolean {
        return false
    }

    private var targetUnit: TargetUnit = TargetUnit.getDefault()
    private var targetType: TargetType = TargetType.getDefault()
    private var targetPoint: Int = 0
    private var callback: ((TargetUnit, TargetType, Int) -> Unit)? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        binding.spTargetType.text = getString(targetType.stringRes)
        binding.spTargetUnit.text = getString(targetUnit.stringRes)
        binding.edtTargetPoint.setText(targetPoint.toString())
        binding.spTargetType.clickWithDebounce { v ->
            CustomSpinner(v, requireContext())
                .setDataSource(TargetType.values().map {
                    CustomSpinner.SpinnerItem(getString(it.stringRes), it.value)
                })
                .build()
                .setOnSelectedItemCallback { item ->
                    TargetType.getType(item.id)?.let {
                        targetType = it
                    }
                }
        }
        binding.edtTargetPoint.onTextChanged { point ->
            point?.toIntOrNull()?.let {
                targetPoint = it
            }
        }
        binding.tvSave.clickWithDebounce {
            callback?.invoke(targetUnit, targetType, targetPoint)
            dismiss()
        }
        binding.tvClose.clickWithDebounce { dismiss() }
    }

    fun setTargetUnit(targetType: TargetUnit) = apply {
        this.targetUnit = targetType
    }

    fun setTargetType(targetTime: TargetType) = apply {
        this.targetType = targetTime
    }

    fun setTargetPoint(targetPoint: Int) = apply {
        this.targetPoint = targetPoint
    }

    fun setOnResultListener(callback: (targetUnit: TargetUnit, targetTime: TargetType, targetPoint: Int) -> Unit) = apply {
        this.callback = callback
    }
}