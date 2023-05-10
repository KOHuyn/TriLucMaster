package com.mobileplus.dummytriluc.ui.utils.language

import com.google.gson.annotations.Expose

data class Language(
        @Expose var name: String,
        @Expose var code: String,
        @Expose var flag: String,
        @Expose var selected: Boolean
)