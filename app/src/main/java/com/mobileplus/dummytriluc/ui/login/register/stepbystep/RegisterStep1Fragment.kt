package com.mobileplus.dummytriluc.ui.login.register.stepbystep

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import com.core.BaseFragmentZ
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.databinding.FragmentRegisterStep1Binding
import com.mobileplus.dummytriluc.ui.login.register.RegisterViewModel
import com.mobileplus.dummytriluc.ui.utils.PickerImageUtils
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentLogin
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.register
import com.utils.ext.unregister
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File


class RegisterStep1Fragment : BaseFragmentZ<FragmentRegisterStep1Binding>() {

    override fun getLayoutBinding(): FragmentRegisterStep1Binding =
        FragmentRegisterStep1Binding.inflate(layoutInflater)

    private val vm by viewModel<RegisterViewModel>()
    private var imagePath: String = ""

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        disposableViewModel()
        initAction()
    }

    private fun initAction() {
        binding.txtBackStep1.clickWithDebounce {
            onBackPressed()
        }
        binding.txtPickCameraStep1.clickWithDebounce {
            PickerImageUtils.openCamera(this)
                .forResult(PictureConfig.REQUEST_CAMERA)
        }
        binding.txtPickGalleryStep1.clickWithDebounce {
            PickerImageUtils.createSingleImageGallery(this)
                .forResult(PictureConfig.CHOOSE_REQUEST)
        }
        binding.txtNextStep1.clickWithDebounce {
            if (imagePath.isNotBlank()) {
                vm.updateAvatar(File(imagePath))
            } else {
                openFragStep2()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    val selectList: MutableList<LocalMedia>? =
                        PictureSelector.obtainMultipleResult(data)
                    selectList?.let {
                        val media = selectList[0]
                        imagePath = PickerImageUtils.getPathImage(media)
                        binding.imgAvatarMemberGroup.show(imagePath)
                    }
                }

                PictureConfig.REQUEST_CAMERA -> {
                    val selectList: MutableList<LocalMedia>? =
                        PictureSelector.obtainMultipleResult(data)
                    if (selectList != null && selectList.isNotEmpty()) {
                        val media = selectList[0]
                        imagePath = PickerImageUtils.getPathImage(media)
                        binding.imgAvatarMemberGroup.show(imagePath)
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun disposableViewModel() {
        addDispose(
            vm.isLoading.subscribe {
                if (it) showDialog()
                else hideDialog()
            },
            vm.rxMessage.subscribe { toast(it) },
            vm.rxUploadImageSuccess.subscribe { openFragStep2() },
        )
    }

    private fun openFragStep2() {
        postNormal(EventNextFragmentLogin(RegisterStep2Fragment::class.java, true))
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
    fun onLanguageChange(ev: EventChangeLanguage) {
        loadLanguageResource()
    }

    private fun loadLanguageResource() {
        binding.header1.text = loadStringRes(R.string.label_setup_avatar)
        binding.txtDescriptionAvatar.text = loadStringRes(R.string.hint_setup_avatar)
        binding.txtPickCameraStep1.text = loadStringRes(R.string.label_camera)
        binding.txtPickGalleryStep1.text = loadStringRes(R.string.label_gallery)
        binding.txtNextStep1.text = loadStringRes(R.string.label_continue)
        binding.txtBackStep1.text = loadStringRes(R.string.label_back)
    }

}