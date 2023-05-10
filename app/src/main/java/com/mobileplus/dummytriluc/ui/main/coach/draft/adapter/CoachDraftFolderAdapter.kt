package com.mobileplus.dummytriluc.ui.main.coach.draft.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemCoachDraftFolder
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_coach_draft_folder.view.*

/**
 * Created by KOHuyn on 3/23/2021
 */
class CoachDraftFolderAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<ItemCoachDraftFolder>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemClick: OnClickItemAdapter? = null
    var onLongClick: OnClickItemAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_coach_draft_folder))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        with(holder.itemView) {
            txtTitleCoachFolderDraft.setTextNotNull(items[position].name)
            clickWithDebounce { onItemClick?.setOnClickListener(this, position) }
            setOnLongClickListener {
                onLongClick?.setOnClickListener(this, position)
                true
            }
        }
    }

    override fun getItemCount(): Int = items.size
}