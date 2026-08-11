# Roadmap — WarehouseManagerApp

> Chia nhỏ theo phiên làm việc **1-2 tiếng/ngày**. Mỗi "Giai đoạn" = khoảng 1 buổi. Tick vào `[ ]` khi xong để tự theo dõi tiến độ. Không cần làm liên tục mỗi ngày — cứ theo tốc độ của bạn, thứ tự Phase/Giai đoạn là thứ bắt buộc phải giữ (giai đoạn sau phụ thuộc giai đoạn trước).

**Tổng cộng ước tính:** 7 Phase, 17 Giai đoạn ≈ 17-25 buổi (tùy tốc độ) ≈ 3-5 tuần nếu làm đều 5 buổi/tuần.

---

## Phase 1 — Nền Tảng & Dữ Liệu Lõi ✅ ĐÃ XONG

- [x] **Giai đoạn 1.1** — Setup VS Code + Maven + pom.xml + JavaFX + JDBC driver
- [x] **Giai đoạn 1.2** — Tạo database SQL Server, chạy script schema + seed cà phê
- [x] **Giai đoạn 1.3** — Core Models: `IGoods`, `Goods`, `RawMaterial`, `FinishedProduct`, `User`, `LoginRequest`

---

## Phase 2 — Tầng Dữ Liệu (Data Layer)

- [x] **Giai đoạn 2.1** (~1-2h) — `DatabaseConfig.java`
  - Viết class, hiểu vì sao mở Connection mới mỗi lần gọi (không cache)
  - **Test nhanh:** viết 1 class `Main.java` tạm, gọi `DatabaseConfig.getConnection()`, in ra `"Ket noi thanh cong"` nếu không lỗi — xác nhận kết nối DB hoạt động **trước khi** viết Repository, đỡ debug lẫn lộn nhiều lỗi cùng lúc — ✅ đã test, `DB connected: true`

- [ ] **Giai đoạn 2.2** (~1-2h) — `GoodsRepository.java` (nửa đầu)
  - `insert()`, `findAll()`, `findByCode()`, `mapRow()`
  - Test bằng `Main.java` tạm: gọi `findAll()`, in từng `Goods` ra console

- [ ] **Giai đoạn 2.3** (~1-2h) — `GoodsRepository.java` (nửa sau) + `UserRepository.java`
  - `findByType()`, `updateQuantity()`, `update()`, `delete()`, `calcTotalStockValue()`
  - `UserRepository`: `findByUsername()`, `existsByUsername()`, `insert()`, `updateStatus()`, `mapRow()`
  - Test: gọi `findByUsername("admin")`, in ra `User` — xác nhận đọc đúng `Salt`/`PasswordHash`

---

## Phase 3 — Tầng Nghiệp Vụ (Services) ✅ ĐÃ XONG

> Đây là phần nặng nhất — cố tình chia làm 4 giai đoạn nhỏ thay vì 1 giai đoạn to.

- [x] **Giai đoạn 3.1** (~1-2h) — `WarehouseService` phần State + Singleton + CRUD
  - `getInstance()`, `reset()`, field `map`/`list`/`lock`
  - `loadFromDB()`, `importGoods()`, `exportGoods()`, `delete()`, `findByCode()`
  - Trọng tâm lý thuyết: Generic `<T extends Goods>`, `synchronized(lock)`, `Map` vs `List` dùng khi nào
  - ✅ Test end-to-end pass — phát hiện + sửa lỗi `exportGoods()` quên ghi xuống DB (chỉ trừ trong cache)

- [x] **Giai đoạn 3.2** (~1-2h) — `WarehouseService` phần Query/Algorithm
  - `getAll()`, `filterByType()`, `calcTotalStockValue()`, `findLowStock()`
  - `sortByQuantityDesc()` (Selection Sort tự viết), `sortedByStockValueDesc()` (Comparator), `findMinQuantity()` (`Collections.min`)
  - ✅ Test end-to-end pass — phát hiện + sửa lỗi nested loop dùng nhầm biến (`i` thay vì `j`) và thiếu `;`

- [x] **Giai đoạn 3.3** (~1-2h) — `WarehouseService` phần Serialization + Switch Expression
  - `saveToFile()`, `loadFromFile()` (`ObjectOutputStream`/`ObjectInputStream`)
  - `calcUrgency()` (switch expression `->`/`yield`)

- [x] **Giai đoạn 3.4** (~1-2h) — `WarehouseService` phần Async (Threading) — **giai đoạn khó nhất, đừng vội**
  - `loadFromDBAsync()`, `importAsync()`, `exportAsync()`, `calcTotalStockValueAsync()`, `sortAndDisplayAsync()`, `saveToFileAsync()`, `loadFromFileAsync()`
  - Trọng tâm lý thuyết: `Thread`, `Platform.runLater()`, vì sao không được cập nhật UI trực tiếp từ thread nền

- [x] **Giai đoạn 3.5** (~1h) — `AuthService.java`
  - `login()`, `register()` (không nhận tham số `role`)
  - Ôn lại: vì sao chặn role ở tầng Service chứ không chỉ ở UI

---

## Phase 4 — Tiện Ích & Bảo Mật (Utils / Exception / Session) ✅ ĐÃ XONG

- [x] **Giai đoạn 4.1** (~1h) — `AppException.java` + `PasswordHasher.java`
  - `generateSalt()`, `hash(password, salt)`, `verify()`
  - Test nhanh: viết `main()` tạm in ra salt+hash của 1 password bất kỳ, so với bảng đã tính sẵn trong tài liệu

- [x] **Giai đoạn 4.2** (~1-2h) — `Validator.java` + `UserSession.java`
  - Toàn bộ method validate (`requireNonBlank`, `parsePositiveInt`, `parseNonNegativeInt`...)
  - `UserSession`: `set/get/isLoggedIn/isAdmin/clear`

---

## Phase 5 — Giao Diện & Điều Khiển (Controllers) ✅ ĐÃ XONG

- [x] **Giai đoạn 5.1** (~1h) — `App.java` + `SceneSwitcher.java`
  - Chưa chạy được UI thật vì chưa có FXML — chỉ viết code, để dành chạy thử ở Phase 6

- [x] **Giai đoạn 5.2** (~1-2h) — `LoginController.java` + `RegisterController.java`
  - Chú ý: `RegisterController` không còn `cbRole`

- [x] **Giai đoạn 5.3** (~1-2h) — `DashboardController.java` + `AddGoodsController.java`
  - Chú ý field mới `txtMinStock`

- [x] **Giai đoạn 5.4** (~1-2h) — `GoodsListController.java` (nửa đầu — TableView setup)
  - Field, `initialize()`, cell value factory + cell factory cho từng cột (`colType`, `colExtra`, `colValue`, `colQty`)
  - `btnDelete.setDisable(!UserSession.isAdmin())`

- [x] **Giai đoạn 5.5** (~1-2h) — `GoodsListController.java` (nửa sau — handlers)
  - `handleFilter`, `handleSearch`, `handleClearSearch`, `handleSort`, `handleExport`, `handleShowLowStock`, `handleDelete`, `handleReload`, `handleBackup`, `handleRestore`


---

## Phase 6 — FXML & CSS (chạy được giao diện thật) ✅ ĐÃ XONG

- [x] **Giai đoạn 6.1** (~1-2h) — `login.fxml`, `register.fxml`, `main.css`
  - **Chạy thử lần đầu** (`mvn clean javafx:run`) — chỉ cần thấy màn hình Login/Register hiện ra đúng layout là thành công bước này, chưa cần login được (còn thiếu FXML khác)

- [x] **Giai đoạn 6.2** (~1-2h) — `dashboard.fxml` + `add_goods.fxml`
  - Test login bằng tài khoản seed (`admin`/`admin123`) → phải vào được Dashboard

- [x] **Giai đoạn 6.3** (~1-2h) — `goods_list.fxml`
  - Test toàn bộ luồng: Nhập kho → Danh sách → Tìm kiếm/Lọc/Sắp xếp → Cảnh báo tồn kho thấp → Xuất kho → Xóa (thử cả 2 tài khoản `admin` và `user1` để thấy khác biệt phân quyền) → Sao lưu/Phục hồi


---

## Phase 7 — Tích Hợp, Kiểm Thử, Hoàn Thiện

- [ ] **Giai đoạn 7.1** (~1-2h) — Test toàn bộ luồng nghiệp vụ end-to-end theo checklist:
  - [ ] Đăng ký tài khoản mới → chắc chắn tạo ra `role=user` (không có cách nào chọn admin)
  - [ ] Đăng nhập sai password → hiện đúng lỗi
  - [ ] Đăng nhập tài khoản bị khóa (`Status=0`) → bị chặn
  - [ ] Nhập kho mã đã tồn tại → cộng dồn số lượng đúng
  - [ ] Xuất kho vượt quá tồn kho → báo lỗi đúng (`IllegalStateException`)
  - [ ] Mặt hàng có `quantity < minStockLevel` → hiển thị đỏ trong bảng + xuất hiện ở "Cảnh báo"
  - [ ] Tài khoản `user` thường → nút "Xóa" bị khóa; tài khoản `admin` → xóa được
  - [ ] Sao lưu → Phục hồi → dữ liệu khớp lại đúng
- [ ] **Giai đoạn 7.2** (~1h) — Sửa bug phát sinh từ test ở 7.1
- [ ] **Giai đoạn 7.3** (~1h, tùy chọn) — Ôn lại toàn bộ `NhatKy_HocTap.md`, tự trả lời checklist phỏng vấn cuối file đó trước khi nộp bài/bảo vệ đồ án

---

## Gợi Ý Sắp Xếp Thời Gian

- Nếu học buổi tối sau giờ làm/học: **1 giai đoạn/buổi** là hợp lý, đừng cố gộp 2 giai đoạn cùng lúc — dễ nản vì Phase 3 (Services) khá nặng.
- Giai đoạn nào có ghi "**Test nhanh**" — đừng bỏ qua, chạy thử ngay bằng 1 `Main.java` tạm hoặc chạy app thật. Bắt lỗi sớm ở từng bước nhỏ dễ hơn nhiều so với dồn lại debug ở Phase 7.
- Phase 3 (Services) là nặng nhất về khái niệm (generic, thread, serialization, switch expression) — nếu thấy đuối, có thể tách Giai đoạn 3.4 (Async/Threading) ra làm riêng 1 buổi cuối tuần khi đầu óc tỉnh táo hơn, thay vì làm buổi tối trong tuần.
- Sau mỗi Phase xong, quay lại hỏi tôi để tôi bổ sung "NGÀY N" tương ứng vào `NhatKy_HocTap.md` — giữ file đó làm tài liệu ôn tập trước phỏng vấn.
