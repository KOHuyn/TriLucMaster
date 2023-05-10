package com.mobileplus.dummytriluc.bluetooth

import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes

object CommandBle {
    ///Chế độ test khi đăng ký cần truyền dạng: 9,[time mili second]
    const val REGISTER_MODE = "9,%d~"

    ///Chế độ đánh tự do cần truyền dạng: 5,[time mili second]
    const val FREESTYLE_MODE = "5,%d,%d,%d~"

    ///Chế độ đánh led sáng ngẫu nhiên cần truyền dạng: 4,[time mili second]
    const val RANDOM_LED_MODE = "4,%d,%d,%d~"

    ///Chế độ đánh theo bài cần truyền dạng: 6,[time mili second],[lessonId],[chuỗi đòn đánh],[delay đòn đánh],[Lực trung bình],[đòn trung bình]~
    const val LESSON_MODE = "6,%d,%d,%s*%s,%d,%d~"

    /*
    bắt đầu bài tập, truyền xuống: “
        7,
        [time milisecond],
        [challenge_id],
        [hitting_type(loại bài đánh)],
        - 1: đánh tự do theo các vị trí
        - 2: đánh led ngẫu nhiên
        - 3: đánh theo bài
        [hit_data (chuỗi vị trí cần đánh)],  chỉ đánh theo bài mới có, không có thì truyền 0
        [hit_limit(giới hạn số lượng đòn đánh)],  không giới hạn truyền 0
        [time_limit(giới hạn thời gian)],  không giới hạn truyền xuống 0 NOTE: máy sẽ check đạt đến thời gian
        trước hay số đòn trước để dừng tập, app không dừng tập
        [position_limit (giới hạn vị trí đánh)],  sẽ sáng led các vị trí giới hạn, không giới hạn truyền 0
        [weight (mức cân)],  tạm thời chưa xử lý trường này nhưng vẫn truyền xuống
        [min_power(lực tối thiểu)],  truyền xuống để máy báo cú đánh đó có đủ lực hay không
        [thời gian giãn cách giữa các đòn đánh]~”
         [chuỗi vị trí cần đánh]:
        - = 0: vị trí đánh tự do
        - = 123456789ab: vị trí đánh tuỳ chọn của thách đấu, cần sáng led ở các vị trí đánh tuỳ chọn này
     */
    const val CHALLENGE_MODE = "7,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s~"

    ///Kết nối thiết bị khi đăng ký lần đầu, cần truyền dạng: 8,[time mili second],[user_id]~
    const val FIRST_CONNECT = "8,%d,%d~"

    ///Kết nối tới thiết bị sau khi đã đăng ký, cần truyền dạng 0,[user_id],[lực],[thời gian(second)]~
    const val CONNECT = "0,%d,%d,%d~"

    ///Kết thúc -- truyền lên khi đã nhận được dữ liệu
    const val END = "3~"

    ///Kết thúc -- truyền lên để kết thúc bài tập
    const val FINISH = "2~"

    ///Chế độ huấn luyện viên cần truyền dạng: 1,[time mili second]
    const val COACH_MODE = "1,%d~"

}
