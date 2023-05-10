package com.mobileplus.dummytriluc.ui.main.challenge.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemInvitePeople
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import com.utils.ext.setBackgroundColorz
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.item_invite_people.view.*

class InvitePeopleAdapter constructor(
    private val isDelete: Boolean = false,
    private val isChoicePeople: Boolean = false
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemInvitePeople>()
        set(value) {
            logErr("click")
            field = value
            notifyDataSetChanged()
        }

    var onDeleteChangeCallback: OnDeleteChangeCallback? = null

        fun getItemSelected(): MutableList<ItemInvitePeople> {
        return items.filter { it.isInvite }.toMutableList()
    }

    fun isCheckedInviteItem(): Boolean {
        return items.any { it.isInvite }
    }

    fun isCheckOneItem(): Boolean {
        return items.filter { it.isInvite }.size == 1
    }

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_invite_people))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            imgDeleteInvitePeople.setVisibility(isDelete)
            cbInvitePeople.setVisibility(isChoicePeople)
            if (isChoicePeople) {
                backgroundAvatarInvite.setBackgroundColorz(R.color.white)
            } else {
                backgroundAvatarInvite.setBackgroundColorz(R.color.clr_tab)
            }
            cbInvitePeople.isChecked = item.isInvite
            cbInvitePeople.setOnCheckedChangeListener { _, isChecked -> item.isInvite = isChecked }
            imgDeleteInvitePeople.clickWithDebounce {
                delete(holder.adapterPosition)
                onDeleteChangeCallback?.deleteChange()
            }
            imgAvatarInvitePeople.show(item.avatar)
            txtNameInvitePeople.text = item.name
            txtIdInvitePeople.text = item.getIdInvite()
        }
    }

    override fun getItemCount(): Int = items.size

    fun interface OnDeleteChangeCallback {
        fun deleteChange()
    }
}