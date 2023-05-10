package com.mobileplus.dummytriluc.ui.main.coach.session.result.adapter

import android.view.ViewGroup
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.google.android.flexbox.FlexboxLayout
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.response.session.CoachSessionResultResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.widget.PositionPowerCoachSessionResult
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_coach_session_result.view.*

/**
 * Created by KOHuyn on 4/23/2021
 */
class CoachSessionResultAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<CoachSessionResultResponse>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemClick: OnClickItemAdapter? = null

    fun clearData(){
        items.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        BaseViewHolder(parent.inflateExt(R.layout.item_coach_session_result))

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = items[position]
        with(holder.itemView) {
            item.setRankTop(txtItemRankNumberCoachSession, position + 1)
            txtItemNamePersonCoachSession.text = item.userCreated?.fullName
            imgItemAvatarPersonCoachSession.show(item.userCreated?.avatarPath)
            txtItemScorePersonRankCoachSession.text = "${item.score ?: "--"}"
            clickWithDebounce { onItemClick?.setOnClickListener(this, position) }
            flexboxPosition.removeAllViews()
            item.getArrPosScore()
                .mapIndexed { index, it ->
                    if (index != 0) {
                        flexboxPosition.addView(Space(context).apply {
                            layoutParams = FlexboxLayout.LayoutParams(
                                resources.getDimensionPixelOffset(R.dimen.space_8),
                                0
                            )
                        })
                    }
                    val view = PositionPowerCoachSessionResult(context)
                    view.setTitlePosition(it.first)
                    view.setScore(it.second)
                    flexboxPosition.addView(view)
                }
        }
    }

    override fun getItemCount(): Int = items.size
}