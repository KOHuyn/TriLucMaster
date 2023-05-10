package com.mobileplus.dummytriluc.ui.utils.test

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.lifecycleScope
import com.mobileplus.dummytriluc.ui.main.MainActivity
import kotlinx.coroutines.*

const val MODE_COURSE_TEST = """0|[{"mode":4,"start_time1":1641527847,"lesson_id":289,"start_time2":78430,"user_id":365,"machine_id":71,"data":[|110*1|],"end_time":1641527849}]|25*~"""

const val MODE_FREE_TEST =   """0|[{"mode":2,"start_time1":1637565405,"start_time2":246839,"user_id":220,"machine_id":68,"data":[|95*1|{"f":41.97,"p":"5","t":253082}|30*2|,{"f":40.49,"p":"4","t":253374}|31*3|,{"f":44.93,"p":"5","t":253793}|31*4|,{"f":70.12,"p":"b","t":254465}|31*5|,{"f":41.04,"p":"a","t":255779}|31*6|,{"f":78.51,"p":"9","t":257159}|31*7|,{"f":32.53,"p":"5","t":258083}|31*8|,{"f":28.10,"p":"4","t":258350}|31*9|,{"f":45.28,"p":"1","t":258895}|31*10|,{"f":43.99,"p":"3","t":259973}|31*11|,{"f":46.25,"p":"3","t":261506}|31*12|,{"f":34.27,"p":"1","t":262684}|31*13|,{"f":44.31,"p":"1","t":264650}|31*14|,{"f":47.26,"p":"b","t":265929}|31*15|,{"f":56.58,"p":"a","t":267386}|31*16|,{"f":43.27,"p":"2","t":268868}|31*17|,{"f":29.02,"p":"2","t":269641}|31*18|,{"f":35.31,"p":"2","t":271478}|31*19|,{"f":39.71,"p":"b","t":272757}|31*20|,{"f":26.62,"p":"8","t":273936}|31*21|,{"f":36.42,"p":"5","t":274936}|31*22|,{"f":36.79,"p":"4","t":276191}|31*23|,{"f":33.09,"p":"2","t":277217}|31*24|,{"f":31.98,"p":"2","t":278218}|31*25|,{"f":31.80,"p":"2","t":279574}|31*26|,{"f":36.79,"p":"4","t":281739}|31*27|,{"f":30.50,"p":"4","t":283095}|31*28|,{"f":43.48,"p":"b","t":284323}|31*29|,{"f":40.82,"p":"a","t":285654}|31*30|,{"f":26.80,"p":"5","t":286401}|31*31|,{"f":27.54,"p":"4","t":286770}|31*32|,{"f":48.81,"p":"b","t":287720}|31*33|,{"f":40.60,"p":"b","t":288416}|31*34|,{"f":38.46,"p":"8","t":289722}|31*35|,{"f":33.83,"p":"2","t":292126}|31*36|,{"f":30.87,"p":"5","t":293228}|31*37|,{"f":26.99,"p":"4","t":293521}|31*38|,{"f":28.28,"p":"5","t":294040}|31*39|,{"f":30.87,"p":"2","t":295168}|31*40|],"end_time":1637565457}]|25*~"""

fun MainActivity.testNextResponseBluetooth(arrJson:String) {
    lifecycleScope.launch {
        for (i in arrJson.indices step 20) {
            delay(20)
            withContext(Dispatchers.Main) {
                if (i > arrJson.length - 20) {
                    rxResponseDataBle
                        .onNext(arrJson.substring(i, arrJson.length))
                } else {
                    rxResponseDataBle
                        .onNext(arrJson.substring(i, i + 20))
                }
            }
        }
    }
    Handler(Looper.getMainLooper()).postDelayed({
       rxResponseDataBle
            .onNext("asdhhu success asd")
        toast("Success")
    }, 10000)
}