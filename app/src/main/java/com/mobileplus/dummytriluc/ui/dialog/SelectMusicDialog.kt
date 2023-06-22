package com.mobileplus.dummytriluc.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseDialogBinding
import com.core.BaseViewHolderZ
import com.mobileplus.dummytriluc.data.model.ItemMusic
import com.mobileplus.dummytriluc.databinding.DialogSelectMusicBinding
import com.mobileplus.dummytriluc.databinding.ItemMusicBinding

/**
 * Created by KO Huyn on 19/06/2023.
 */
class SelectMusicDialog(val data: List<ItemMusic>) : BaseDialogBinding<DialogSelectMusicBinding>() {
    private val adapter by lazy { MusicAdapter() }
    override fun getLayoutBinding(inflater: LayoutInflater): DialogSelectMusicBinding {
        return DialogSelectMusicBinding.inflate(inflater)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        adapter.items = data
        binding.rcvMusicDialog.adapter = adapter
        binding.rcvMusicDialog.layoutManager = LinearLayoutManager(context)
        binding.rcvMusicDialog.setHasFixedSize(true)
    }

    fun setOnMusicSelected(callback: (music: ItemMusic) -> Unit) = apply {
        adapter.onItemClick = {
            dismiss()
            callback(it)
        }
    }

    class MusicAdapter : RecyclerView.Adapter<BaseViewHolderZ<ItemMusicBinding>>() {
        var items = listOf<ItemMusic>()
            set(value) {
                field = value
                notifyDataSetChanged()
            }
        var onItemClick: (ItemMusic) -> Unit = {}
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
            val item = items[position]
            holder.binding.tvNameMusic.text = item.name ?: ""
            holder.itemView.setOnClickListener { onItemClick(item) }
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

}