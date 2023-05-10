package com.mobileplus.dummytriluc.ui.main.news.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.core.BaseViewHolder
import com.core.BaseViewHolderZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.DataNewsChallenge
import com.mobileplus.dummytriluc.data.model.DataNewsFeed
import com.mobileplus.dummytriluc.data.model.ItemNewsFeed
import com.mobileplus.dummytriluc.databinding.ItemNewsArticleBinding
import com.mobileplus.dummytriluc.databinding.ItemNewsChallengeBinding
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.item_news_article.view.*
import kotlinx.android.synthetic.main.item_news_challenge.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by ThaiNV on 1/22/2021.
 */
class NewsFeedAdapter : RecyclerView.Adapter<BaseViewHolderZ<ViewBinding>>(), KoinComponent {

    companion object {
        const val TYPE_NEWS_FEED = 1
        const val TYPE_NEWS_CHALLENGE = 2
    }

    private val gson by inject<Gson>()

    var items = mutableListOf<ItemNewsFeed>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var listener: NewFeedAdapterListener? = null

    override fun getItemViewType(position: Int): Int {
        return items[position].getType()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolderZ<ViewBinding> {
        return when (viewType) {
            TYPE_NEWS_FEED -> BaseViewHolderZ(
                ItemNewsArticleBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            TYPE_NEWS_CHALLENGE -> BaseViewHolderZ(
                ItemNewsChallengeBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
            else -> BaseViewHolderZ(
                ItemNewsArticleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BaseViewHolderZ<ViewBinding>, position: Int) {
        val item = items[position]
        when (getItemViewType(position)) {
            TYPE_NEWS_FEED -> {
                with(holder as BaseViewHolderZ<ItemNewsArticleBinding>) {
                    try {
                        val data = gson.fromJson(item.data, DataNewsFeed::class.java)
                       binding. imgNewsArticle.show(data.image)
                        binding. txtNewTitle.setTextNotNull(data.title)
                        binding.txtNewSummary.setTextNotNull(data.summary)
                        binding.txtNewTime.setTextNotNull(data.timeAgo)
                        binding.root.clickWithDebounce {
                            data.id?.let { listener?.detailNewsFeed(it, TYPE_NEWS_FEED) }
                        }
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }
            }

            TYPE_NEWS_CHALLENGE -> {
                with(holder as BaseViewHolderZ<ItemNewsChallengeBinding>) {
                    try {
                        val data = gson.fromJson(item.data, DataNewsChallenge::class.java)
                        binding.txtTimeTitle.text = loadStringRes(R.string.challenge)
                        binding.imgTimeFighting.show(data.image)
                        binding.txtTimeTournament.setTextNotNull(data.title)
                        binding.txtTimeKick.setTextNotNull(data.getWinCountType())
                        binding.root.clickWithDebounce {
                            data.id?.let { listener?.detailNewsFeed(it, TYPE_NEWS_CHALLENGE) }
                        }
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }
            }
            else -> {
            }
        }
    }

    fun interface NewFeedAdapterListener {
        fun detailNewsFeed(id: Int, type: Int)
    }
}