package com.mobileplus.dummytriluc.ui.utils.language

import java.util.*

enum class LanguageEnum(val locale: Locale, val code: String,val countryName: String) {
    EN(Locale("en", "US"), "en", "English"),
    VI(Locale("vi", "VN"), "vi", "Tiếng Việt")
}