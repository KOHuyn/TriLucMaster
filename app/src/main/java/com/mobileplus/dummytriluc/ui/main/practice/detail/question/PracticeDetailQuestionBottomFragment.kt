package com.mobileplus.dummytriluc.ui.main.practice.detail.question

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.DummyResult
import com.mobileplus.dummytriluc.data.model.ItemChat
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.databinding.LayoutPracticeDetailQuestionBottomBinding
import com.mobileplus.dummytriluc.socket.SocketHelper
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.adapter.ChatAdapter
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailViewModel
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.video.result.VideoResultActivity
import com.utils.ext.setVisibility
import com.utils.ext.startActivity
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel


class PracticeDetailQuestionBottomFragment :
    BaseFragmentZ<LayoutPracticeDetailQuestionBottomBinding>() {
    override fun getLayoutBinding(): LayoutPracticeDetailQuestionBottomBinding =
        LayoutPracticeDetailQuestionBottomBinding.inflate(layoutInflater)

    private val detailPracticeViewModel by sharedViewModel<PracticeDetailViewModel>()
    val adapter by lazy { ChatAdapter() }
    private val rcvChat by lazy { binding.rcvFAQ }
    private var lastMessageId = AppConstants.INTEGER_DEFAULT
    var roomId: Int? = null
    var roomKeyId: String? = null
    private var isLoadMoreMsg: Boolean = false
    private lateinit var linearLayoutManage: LinearLayoutManager
    var initialized = false
    private val gson by inject<Gson>()
    lateinit var socketHelper: SocketHelper
    fun initView() {
        if (!initialized) {
            roomId?.let {
                detailPracticeViewModel.getChatRoom(roomId = it)
                initialized = true
            }
            roomKeyId?.let {
                setupSocketIO(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
//        if (parentFragment is PracticeDetailFragment)
//            (parentFragment as PracticeDetailFragment).hideKeyBoardWhenClick(rcvChat)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel(detailPracticeViewModel)
        setupRcvChat()
    }

    private fun setupSocketIO(id: String) {
        socketHelper = SocketHelper((activity as MainActivity).getSocket())
            .setSocketID(id)
            .build()
            .setOnCallbackSocket {
                try {
                    logErr("SOCKET_IO", it[0].toString())
                    val itemChat = gson.fromJson(it[0].toString(), ItemChat::class.java)
                    val idProfile = detailPracticeViewModel.dataManager.getUserInfo()?.id ?: 0
                    itemChat.isSend = itemChat.userId == idProfile
                    if (itemChat.userId != idProfile) {
                        addItemChat(itemChat)
                    }
                } catch (e: Exception) {
                    logErr(e.message ?: "")
                }
            }
    }

    private fun setupRcvChat() {
        rcvChat.post {
            linearLayoutManage = LinearLayoutManager(requireContext())
            linearLayoutManage.stackFromEnd = true
            rcvChat.layoutManager = linearLayoutManage
            rcvChat.adapter = adapter
            rcvChat.addItemDecoration(
                MarginItemDecoration(resources.getDimension(R.dimen.space_4).toInt())
            )
        }
        rcvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isLoadMoreMsg && linearLayoutManage.findFirstVisibleItemPosition() in 0..4) {
                    isLoadMoreMsg = true
                    loadMore()
                }
            }
        })
        adapter.onChatMessageListener = object : ChatAdapter.OnChatMessageListener {
            override fun onVideoClick(view: View, position: Int) {
                val item = adapter.chatList[position].dummyResult
                if (item != null) {
                    if (item.videoPath != null && item.dataVideo != null && item.startTime2 != null) {
                        val bundle = Bundle().apply {
                            putString(VideoResultActivity.PRACTICE_FAQ_URL, item.videoPath)
                            putString(VideoResultActivity.PRACTICE_FAQ_DATA, item.dataVideo)
                            putLong(
                                VideoResultActivity.PRACTICE_FAQ_START_TIME,
                                item.startTime2 ?: -1
                            )
                        }
                        startActivity(VideoResultActivity::class.java, bundle)
                    } else {
                        toast(loadStringRes(R.string.error_not_find_video))
                    }
                } else {
                    toast(loadStringRes(R.string.error_not_find_video))
                }
            }
        }
    }

    private fun callbackViewModel(vm: PracticeDetailViewModel) {
        addDispose(
            vm.rxLoadingChat.subscribe {
                binding.loadingChatPractice.root.setVisibility(it)
            }
        )
        addDispose(vm.rxChats.subscribe {
            if (it.isNotEmpty()) {
                lastMessageId = it[0].id?.toInt() ?: AppConstants.INTEGER_DEFAULT
                if (adapter.chatList.isEmpty()) {
                    adapter.chatList = it.toMutableList()
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.addChatMore(it.toMutableList())
                    isLoadMoreMsg = false
                }
            }
        })
    }

    private fun scrollToBottom() {
        rcvChat.post {
            rcvChat.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    fun addChatMessageLocalToList(message: String, timeSendMessage: Long) {
        requireActivity().runOnUiThread {
            adapter.addChatMessageLocalToList(
                message, timeSendMessage,
                detailPracticeViewModel.userInfo
            )
            scrollToBottom()
        }
    }


    fun addChatVideoToList(data: DummyResult, timeSendMessage: Long, videoObjectId: Int) {
        requireActivity().runOnUiThread {
            adapter.addChatVideoLocalToList(
                data,
                timeSendMessage,
                videoObjectId,
                detailPracticeViewModel.userInfo
            )
            scrollToBottom()
        }
    }

    private fun addItemChat(chat: ItemChat) {
        requireActivity().runOnUiThread {
            adapter.add(chat)
            adapter.notifyDataSetChanged()
            scrollToBottom()
        }
    }

    fun loadMore() {
        if (lastMessageId != AppConstants.INTEGER_DEFAULT) {
            roomId?.let {
                detailPracticeViewModel.getChatRoom(
                    it,
                    lastMessageId,
                    ApiConstants.UP
                )
            }
        }
    }

    override fun onDestroy() {
        if (this::socketHelper.isInitialized) socketHelper.destroy()
        super.onDestroy()
    }


}