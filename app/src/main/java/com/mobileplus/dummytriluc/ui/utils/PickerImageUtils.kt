package com.mobileplus.dummytriluc.ui.utils

import android.app.Activity
import androidx.fragment.app.Fragment
import com.luck.picture.lib.PictureSelectionModel
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.tools.SdkVersionUtils
import com.mobileplus.dummytriluc.ui.widget.GlideEngine

object PickerImageUtils {
    fun openCamera(fragment: Fragment): PictureSelectionModel {
        return PictureSelector.create(fragment)
            .openCamera(PictureMimeType.ofImage())
            .imageEngine(GlideEngine.createGlideEngine())
            .isAndroidQTransform(true)
    }

    fun createSingleImageGallery(fragment: Fragment): PictureSelectionModel {
        return PictureSelector.create(fragment)
            .openGallery(PictureMimeType.ofImage())
            .imageEngine(GlideEngine.createGlideEngine())
            .isCamera(true)
            .isAndroidQTransform(true)
            .selectionMode(PictureConfig.SINGLE)
    }

    fun createSingleImageGallery(activity: Activity): PictureSelectionModel {
        return PictureSelector.create(activity)
            .openGallery(PictureMimeType.ofImage())
            .imageEngine(GlideEngine.createGlideEngine())
            .isAndroidQTransform(true)
            .selectionMode(PictureConfig.SINGLE)
    }

    fun openCamera(activity: Activity): PictureSelectionModel {
        return PictureSelector.create(activity)
            .openCamera(PictureMimeType.ofImage())
            .imageEngine(GlideEngine.createGlideEngine())
            .isAndroidQTransform(true)
    }

    fun getPathImage(media:LocalMedia):String{
        return when {
            SdkVersionUtils.isQ() -> media.androidQToPath
            media.isCompressed -> media.compressPath
            media.isCut -> media.cutPath
            else -> media.path
        }
    }
}