package com.mobileplus.dummytriluc.ui.main.practice.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemPracticeItemMaster
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_practice_filter_master.view.*

/**
 * Created by KOHuyn on 3/12/2021
 */
class PracticeMasterMoreAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemPracticeItemMaster>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemClick: OnClickItemAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_practice_filter_master))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            imgMasterPracticeFilter.show(item.imgMaster)
            txtNameMasterPracticeFilter.setTextNotNull(item.nameMaster)
            txtSubjectMasterPracticeFilter.setTextNotNull(item.subject?.title)
            txtCountPracticeDiscipleFilter.setTextNotNull(item.getCountPracticeDisciple())
            clickWithDebounce { onItemClick?.setOnClickListener(this, position) }
        }
    }

    override fun getItemCount(): Int = items.size
}