package com.mobileplus.dummytriluc.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseDialogBinding
import com.core.BaseViewHolderZ
import com.mobileplus.dummytriluc.data.model.ItemMusic
import com.mobileplus.dummytriluc.databinding.DialogChangePressureBinding
import com.mobileplus.dummytriluc.databinding.DialogSelectMusicBinding
import com.mobileplus.dummytriluc.databinding.ItemMusicBinding

/**
 * Created by KO Huyn on 19/06/2023.
 */
class ChangePressureDialog : BaseDialogBinding<DialogChangePressureBinding>() {
    private val adapter by lazy { PressureAdapter() }
    override fun getLayoutBinding(inflater: LayoutInflater): DialogChangePressureBinding {
        return DialogChangePressureBinding.inflate(inflater)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        adapter.items =
            listOf(15 to "15- 45kg", 20 to "20 - 65kg", 25 to "25 - 90kg", 30 to "30 - 150kg")
        binding.rcvChangePressure.adapter = adapter
        binding.rcvChangePressure.layoutManager = LinearLayoutManager(context)
        binding.rcvChangePressure.setHasFixedSize(true)
    }

    fun setOnPressureSelected(callback: (music: Int) -> Unit) = apply {
        adapter.onItemClick = {
            dismiss()
            callback(it)
        }
    }

    class PressureAdapter : RecyclerView.Adapter<BaseViewHolderZ<ItemMusicBinding>>() {
        var items = listOf<Pair<Int, String>>()
            set(value) {
                field = value
                notifyDataSetChanged()
            }
        var onItemClick: (Int) -> Unit = {}
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BaseViewHolderZ<ItemMusicBinding> {
            return BaseViewHolderZ(
                ItemMusicBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: BaseViewHolderZ<ItemMusicBinding>, position: Int) {
            val (kg,title) = items[position]
            holder.binding.tvNameMusic.text = title
            holder.itemView.setOnClickListener { onItemClick(kg) }
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

}