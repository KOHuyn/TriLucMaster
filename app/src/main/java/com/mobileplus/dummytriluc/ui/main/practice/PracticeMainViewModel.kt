package com.mobileplus.dummytriluc.ui.main.practice

import com.core.BaseViewModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.*
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class PracticeMainViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxItemPracticeMain: PublishSubject<MutableList<BasePracticeItem>> = PublishSubject.create()
    val dataSample =
        """[{"id":1,"type":"practice","name":"Bài tập của tôi","practice":[{"id":2,"subject_id":1,"level_id":1,"user_id":1,"type":"folder","title":"Bài 2:  Roundhouse","image_path":"http://dummy-api.mobileplus.info/image/w1300/subject/202011/JanjZrPPJ6lM.png","content":"Lưu ý:  Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s."},{"id":1,"subject_id":1,"level_id":1,"user_id":1,"title":"Bài 1:  UpperCut UpperCut","image_path":"http://dummy-api.mobileplus.info/image/w1300/subject/202011/JanjZrPPJ6lM.png","content":"Lưu ý:  Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s."},{"id":2,"subject_id":1,"level_id":1,"user_id":1,"title":"Bài 2:  Roundhouse","image_path":"http://dummy-api.mobileplus.info/image/w1300/subject/202011/JanjZrPPJ6lM.png","content":"Lưu ý:  Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s."}]},{"id":2,"type":"practice","name":"Khuyến nghị tập luyện","practice":[]},{"id":3,"type":"master","name":"HLV nổi bật","master":[{"id":1,"full_name":"Bách Lee Bách Lee Bách Lee","number_practice":30,"number_student":100,"avartar_path":"http://dummy-api.mobileplus.info/image/w1300/subject/202011/JanjZrPPJ6lM.png"}]},{"id":4,"type":"practice","name":"Bài tập phổ biến","practice":[{"id":1,"subject_id":1,"level_id":1,"user_id":1,"title":"Bài 1:  UpperCut UpperCut","image_path":"http://dummy-api.mobileplus.info/image/w1300/subject/202011/JanjZrPPJ6lM.png","content":"Lưu ý:  Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s."},{"id":2,"subject_id":1,"level_id":1,"user_id":1,"title":"Bài 2:  Roundhouse","image_path":"http://dummy-api.mobileplus.info/image/w1300/subject/202011/JanjZrPPJ6lM.png","content":"Lưu ý:  Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s."}]}]"""

    fun getDataPractice(): Disposable {
        isLoading.onNext(true)
        return dataManager.getListPractice()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                try {
                    if (response.isSuccess()) {
                        val items = mutableListOf<BasePracticeItem>()
                        val data: List<PracticeData> = gson.toList(response.dataArray())
//                        data.map { category ->
//                            if (category.practice?.isNotEmpty() == true) {
//                                items.add(ItemTitlePractice(category.id, category.name))
//                                items.add(ItemPracticeContent(category.practice as MutableList<BasePracticeItem>))
//                            }
//                        }
//                        val data: List<PracticeData> = gson.toList(dataSample)
                        data.map { category ->
                            when (category.type) {
                                PracticeData.TYPE_PRACTICE, PracticeData.TYPE_FOLDER -> {
                                    if (category.practice?.isNotEmpty() == true) {
                                        items.add(
                                            ItemTitlePractice(
                                                category.id,
                                                category.name,
                                                category.type
                                            )
                                        )
                                        items.add(ItemPracticeContent(category.practice as MutableList<BasePracticeItem>))
                                    }
                                }
                                PracticeData.TYPE_MASTER -> {
                                    if (category.master?.isNotEmpty() == true) {
                                        items.add(
                                            ItemTitlePractice(
                                                category.id,
                                                category.name,
                                                category.type
                                            )
                                        )
                                        items.add(ItemPracticeContent(category.master as MutableList<BasePracticeItem>))
                                    }
                                }
                            }
                        }
                        val itemLocal = mutableListOf<BasePracticeItem>()
                        itemLocal.add(
                            ItemPracticeLocal(
                                1,
                                R.drawable.img_thumb_free_fight,
                                loadStringRes(R.string.free_fight),
                                loadStringRes(R.string.free_fight_raw)
                            )
                        )
                        itemLocal.add(
                            ItemPracticeLocal(
                                2,
                                R.drawable.img_thumb_according_to_led,
                                loadStringRes(R.string.according_to_led),
                                loadStringRes(R.string.according_to_led_raw)
                            )
                        )
                        items.add(0, ItemPracticeContent(itemLocal, isLocal = true))
                        rxItemPracticeMain.onNext(items)
                    }
                } catch (e: JsonSyntaxException) {
                    e.logErr()
                } catch (e: ClassCastException) {
                    e.logErr()
                }
            }, {
                isLoading.onNext(false)
                it.printStackTrace()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    data class PracticeData(
        @Expose
        @SerializedName("id")
        val id: Int,
        @Expose
        @SerializedName("name")
        val name: String,
        @Expose
        val type: String? = null,
        @Expose
        @SerializedName("practice")
        val practice: MutableList<ItemPracticeItemContent>? = null,
        @Expose
        @SerializedName("master")
        val master: MutableList<ItemPracticeItemMaster>? = null,
    ) {
        companion object {
            const val TYPE_PRACTICE = "practice"
            const val TYPE_FOLDER = "folder"
            const val TYPE_MASTER = "master"
        }
    }
}