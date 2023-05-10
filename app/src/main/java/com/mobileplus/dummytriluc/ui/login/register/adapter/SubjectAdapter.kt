package com.mobileplus.dummytriluc.ui.login.register.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.SubjectItem
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_subject.view.*

class SubjectAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items: MutableList<SubjectItem> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(parent.inflateExt(R.layout.item_subject))
    }

    fun setCheckedItemByID(id: Int) {
        items.map { subject ->
            subject.isSelected = (subject.id == id)
        }
        notifyDataSetChanged()
    }

    private fun setCheckedItem(position: Int) {
        items.map { subject ->
            subject.isSelected = (subject.id == items[position].id)
        }
        notifyDataSetChanged()
    }

    fun getItemChecked(): SubjectItem? {
        return items.firstOrNull { it.isSelected }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            imageThumbSubject.show(item.thumb)
            txtSubjectName.setTextNotNull(item.name)
            viewMarkSubject.isSelected = item.isSelected
            clickWithDebounce { setCheckedItem(position) }
        }
    }

}