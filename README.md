# Giọt Nước — App nhắc uống nước (Android)

Ứng dụng Android gốc, nhắc uống nước mỗi 30 phút từ 8:00 đến 17:00, chạy nền bằng `AlarmManager` nên vẫn nhắc kể cả khi tắt app hoặc khởi động lại máy.

## Cách 1 (dễ nhất, không cần cài gì) — để GitHub tự build hộ

1. Tạo tài khoản GitHub miễn phí tại https://github.com (nếu chưa có).
2. Tạo repository mới (nút **New**), để ở chế độ Public hoặc Private đều được.
3. Vào tab **Add file > Upload files**, kéo thả **toàn bộ nội dung** thư mục `water-reminder-android` (giữ nguyên cấu trúc thư mục, kể cả thư mục ẩn `.github`) rồi bấm **Commit changes**.
4. Vào tab **Actions** ở đầu trang repo — GitHub sẽ tự động chạy quy trình build (mất khoảng 2–4 phút).
5. Khi thấy dấu ✓ xanh, bấm vào lượt chạy đó → cuộn xuống mục **Artifacts** → tải file **giot-nuoc-debug-apk** (đây chính là file `.apk`).
6. Chuyển file `.apk` này vào điện thoại (qua Zalo, Google Drive, USB...) rồi mở để cài. Điện thoại sẽ hỏi cho phép "Cài từ nguồn không xác định" — đồng ý là cài được.

> Lưu ý: nếu trình duyệt của bạn bỏ qua thư mục ẩn `.github` khi kéo-thả, vào repo → **Add file > Create new file**, gõ đường dẫn `.github/workflows/build-apk.yml` rồi dán nội dung file đó vào (mở file bằng trình soạn thảo văn bản để copy).

## Cách 2 — Build bằng Android Studio trên máy tính

1. Cài **Android Studio** (bản mới nhất): https://developer.android.com/studio
2. Mở Android Studio → **Open** → chọn thư mục `water-reminder-android` (thư mục chứa file này).
3. Chờ Android Studio tự đồng bộ Gradle. Nếu nó hỏi tạo Gradle Wrapper, chọn **OK/Yes** để nó tự tải về (dự án này chưa kèm sẵn file `gradlew` nhị phân).
4. Cắm điện thoại Android qua USB (bật **Chế độ nhà phát triển** + **Gỡ lỗi USB**), hoặc dùng máy ảo (Emulator).
5. Bấm nút **Run ▶** (hoặc `Build > Build Bundle(s) / APK(s) > Build APK(s)`) để build và cài trực tiếp lên máy.
6. File APK sau khi build nằm ở: `app/build/outputs/apk/debug/app-debug.apk`

## Sau khi cài lên điện thoại

1. Mở app **Giọt Nước**, bấm **Bật nhắc nhở**.
2. App sẽ xin quyền thông báo (Android 13+) và quyền "Báo thức & lời nhắc chính xác" (Android 12+) — cho phép cả hai để nhắc đúng giờ.
3. Xong! App sẽ tự động thông báo lúc 8:00, 8:30, 9:00 ... đến 17:00 mỗi ngày, kể cả khi đã đóng app.

## Tùy chỉnh khung giờ / tần suất

Mở file `app/src/main/java/com/example/waterreminder/ReminderScheduler.kt`, sửa 3 hằng số ở đầu file:

```kotlin
private const val START_HOUR = 8       // giờ bắt đầu
private const val END_HOUR = 17        // giờ kết thúc
private const val STEP_MINUTES = 30    // khoảng cách giữa các lần nhắc (phút)
```

## Một số lưu ý kỹ thuật

- Yêu cầu Android 7.0 (API 24) trở lên.
- Trên một số máy Android bị tối ưu pin mạnh (Xiaomi, Oppo, Samsung...), nên vào **Cài đặt > Pin > Không tối ưu hóa pin** cho app này để tránh hệ thống chặn báo thức nền.
- Muốn phát hành app (không chỉ chạy debug), cần tạo **keystore** ký ứng dụng rồi build bản `release` — phần này Android Studio có hướng dẫn sẵn trong menu `Build > Generate Signed Bundle / APK`.
