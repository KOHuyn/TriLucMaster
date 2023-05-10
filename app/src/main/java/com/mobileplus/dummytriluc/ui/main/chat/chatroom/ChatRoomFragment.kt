package com.mobileplus.dummytriluc.ui.main.chat.chatroom

import android.os.Bundle
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.databinding.FragmentChatRoomBinding
import com.mobileplus.dummytriluc.ui.main.chat.chatmessage.ChatMessageFragment
import com.mobileplus.dummytriluc.ui.main.chat.chatroom.adapter.ChatRoomAdapter
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventReloadRoomChat
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel


/**
 * Created by KO Huyn on 1/12/2021.
 */
class ChatRoomFragment : BaseFragmentZ<FragmentChatRoomBinding>() {

    override fun getLayoutBinding(): FragmentChatRoomBinding =
        FragmentChatRoomBinding.inflate(layoutInflater)

    private val chatRoomViewModel by viewModel<ChatRoomViewModel>()
    private val adapterRoomChat by lazy { ChatRoomAdapter() }
    private val rcv by lazy { binding.rcvChatRoom }
    private val page by lazy { Page() }
    private var isReload = true
    private val gson by inject<Gson>()

    companion object {
        fun openFragment() {
            postNormal(EventNextFragmentMain(ChatRoomFragment::class.java, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel(chatRoomViewModel)
        chatRoomViewModel.getListChatRoom()
        setupRcvChatRoom()
        binding.btnBackChatRoom.clickWithDebounce { onBackPressed() }
    }

    private fun setupRcvChatRoom() {
        rcv.setUpRcv(adapterRoomChat)
        rcv.getRcv().addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_8).toInt()
            )
        )
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            chatRoomViewModel.getListChatRoom()
        }
        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    chatRoomViewModel.getListChatRoom(currPage)
                }
            }
        }
        adapterRoomChat.onItemClick = OnClickItemAdapter { _, position ->
            ChatMessageFragment.openFragment(adapterRoomChat.items[position], gson)
        }
    }

    private fun callbackViewModel(vm: ChatRoomViewModel) {
        addDispose(vm.rxListChatRoom.subscribe {
            val itemsChat = it.first.toMutableList()
            val pageResponse = it.second
            page.updatePage(pageResponse)
            if (page.isDefaultPage || isReload) {
                adapterRoomChat.items = itemsChat
                binding.noDataChatRoom.root.setVisibility(itemsChat.isEmpty())
            } else {
                adapterRoomChat.items.addAll(itemsChat)
            }

        }, vm.isLoading.subscribe {
            if (page.isLoading) {
                binding.loadMoreChatRoom.root.setVisibility(it)
            } else {
                if (it) rcv.enableRefresh() else rcv.cancelRefresh()
            }
            rcv.getRcv().requestLayout()
        },
            vm.rxMessage.subscribe { toast(it) }
        )
    }

    override fun onStart() {
        super.onStart()
        register(this)
    }

    override fun onStop() {
        super.onStop()
        unregister(this)
    }

    @Subscribe
    fun reloadRoomChat(ev: EventReloadRoomChat) {
        isReload = true
        chatRoomViewModel.getListChatRoom()
    }

}