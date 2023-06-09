package com.mobileplus.dummytriluc.ui.dialog

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseDialogBinding
import com.core.BottomSheetDialogZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.databinding.DialogSelectTimeBinding
import com.mobileplus.dummytriluc.databinding.ItemSelectTimeBinding

/**
 * Created by KO Huyn on 10/06/2023.
 */
class SelectTimePracticeDialog : BottomSheetDialogZ<DialogSelectTimeBinding>() {
    override fun getBindingLayout(): DialogSelectTimeBinding {
        return DialogSelectTimeBinding.inflate(layoutInflater)
    }

    private val adapter by lazy { SelectTimeAdapter() }
    private var onDismissListener: () -> Unit = {}

    override fun isCancelable(): Boolean {
        return false
    }

    override fun isCanceledOnTouchOutside(): Boolean {
        return false
    }

    override fun updateUI() {
        isCancelable = false
        adapter.items = List(10) { index -> 30 * (index + 1) }.filter { it > 0 }
        binding.rcvSelectTime.adapter = adapter
        binding.rcvSelectTime.layoutManager = LinearLayoutManager(context)
        binding.rcvSelectTime.setHasFixedSize(true)
        binding.imgCancel.setOnClickListener {
            dismiss()
            onDismissListener()
        }
        binding.tvSkip.setOnClickListener {
            dismiss()
            onDismissListener()
        }
    }

    fun onChooseListener(listener: (ms: Int) -> Unit) = apply {
        adapter.onItemClick = {
            dismiss()
            listener(it)
        }
    }

    fun onDismiss(listener: () -> Unit) = apply {
        this.onDismissListener = listener
    }

    private class SelectTimeAdapter():RecyclerView.Adapter<SelectTimeViewHolder>(){
        var onItemClick: (Int) -> Unit = {}
        var items = listOf<Int>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectTimeViewHolder {
            return SelectTimeViewHolder(
                ItemSelectTimeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: SelectTimeViewHolder, position: Int) {
            holder.vBinding.tvTime.text =
                "${items[position]} ${holder.itemView.context.getString(R.string.second)}"
            holder.itemView.setOnClickListener { onItemClick(items[position]) }
        }
    }

    private class SelectTimeViewHolder(val vBinding: ItemSelectTimeBinding) :
        RecyclerView.ViewHolder(vBinding.root)
}