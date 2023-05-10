package com.mobileplus.dummytriluc.ui.main.coach.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.core.BaseViewHolderZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDiscipleWaiting
import com.mobileplus.dummytriluc.databinding.ItemDiscipleWaitingBinding
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.item_disciple_waiting.view.*

class DiscipleWaitingAdapter : RecyclerView.Adapter<BaseViewHolderZ<ItemDiscipleWaitingBinding>>() {

    var items: MutableList<ItemDiscipleWaiting> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var listener: WaitingDiscipleClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolderZ<ItemDiscipleWaitingBinding> {
        return BaseViewHolderZ(
            ItemDiscipleWaitingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = items.size

    fun delete(position: Int) {
        try {
            if (position != RecyclerView.NO_POSITION && items.size > position) {
                items.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, items.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBindViewHolder(
        holder: BaseViewHolderZ<ItemDiscipleWaitingBinding>,
        position: Int
    ) {
        val item = items[position]
        with(holder) {
            binding.imgAvatarWaitingDisciple.show(item.userCreated?.avatarPath)
            binding.txtNameWaitingDisciple.setTextNotNull(item.userCreated?.fullName)
            binding.txtDescriptionWaitingDisciple.setTextNotNull(item.message)
            binding.imgRejectWaitingDisciple.clickWithDebounce {
                listener?.onRejectClick(position, item)
            }
            binding.imgAcceptWaitingDisciple.clickWithDebounce {
                listener?.onAcceptClick(position, item)
            }
            binding.root.clickWithDebounce {
                listener?.onDetailClick(position, item)
            }
            binding.viewBottomWaitingDisciple.setVisibility(position != itemCount - 1)
        }
    }

    interface WaitingDiscipleClickListener {
        fun onRejectClick(position: Int, item: ItemDiscipleWaiting)
        fun onAcceptClick(position: Int, item: ItemDiscipleWaiting)
        fun onDetailClick(position: Int, item: ItemDiscipleWaiting)
    }
}