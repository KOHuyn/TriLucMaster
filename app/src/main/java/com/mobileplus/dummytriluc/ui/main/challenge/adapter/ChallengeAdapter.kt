package com.mobileplus.dummytriluc.ui.main.challenge.adapter

import android.graphics.Color
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.BaseItemChallenge
import com.mobileplus.dummytriluc.data.model.ItemChallengeAchievement
import com.mobileplus.dummytriluc.data.model.ItemChallengeMatch
import com.mobileplus.dummytriluc.data.model.TypeChallenge
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_achievement_challenge.view.*
import kotlinx.android.synthetic.main.item_match_challenge.view.*

class ChallengeAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var items = mutableListOf<BaseItemChallenge>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemClick: OnClickItemAdapter? = null

    override fun getItemViewType(position: Int): Int {
        return when (items[position].typeItemChallenge()) {
            TypeChallenge.TYPE_MATCH -> TypeChallenge.TYPE_MATCH
            TypeChallenge.TYPE_ACHIEVEMENT -> TypeChallenge.TYPE_ACHIEVEMENT
            else -> TypeChallenge.TYPE_ACHIEVEMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TypeChallenge.TYPE_ACHIEVEMENT -> BaseViewHolder(parent.inflateExt(R.layout.item_achievement_challenge))
            TypeChallenge.TYPE_MATCH -> BaseViewHolder(parent.inflateExt(R.layout.item_match_challenge))
            else -> BaseViewHolder(parent.inflateExt(R.layout.item_achievement_challenge))
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {

        when (getItemViewType(position)) {
            TypeChallenge.TYPE_ACHIEVEMENT -> {
                val item = items[position] as ItemChallengeAchievement
                with(holder.itemView) {
                    imgAchievementChallenge.show(item.icon)
                    txtTitleAchievementChallenge.text = item.title
                    txtDescriptionAchievementChallenge.text = item.description
                    clickWithDebounce { onItemClick?.setOnClickListener(this, position) }
                }
            }
            TypeChallenge.TYPE_MATCH -> {
                val item = items[position] as ItemChallengeMatch
                with(holder.itemView) {
                    imgAvatarUserRequestMatchChallenge.show(item.avatar)
                    txtTitleRequestMatchChallenge.text = item.title
                    txtTypeRequestMatchChallenge.text = item.typeMatch
                    txtAuthorRequestMatchChallenge.text = item.author
                    txtStatusRequestMatchChallenge.run {
                        text = item.getAcceptMatch()
                        setTextColor(Color.parseColor(item.getColorAccept()))
                        val drawable = DrawableCompat.wrap(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.ic_dot,
                                null
                            )!!
                        )
                        drawable.setTint(Color.parseColor(item.getColorAccept()))
                        setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
                    }
                }
            }
            else -> {
            }
        }
    }

    override fun getItemCount(): Int = items.size
}