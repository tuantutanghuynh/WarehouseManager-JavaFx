# Nhật Ký Học Tập — WarehouseManagerApp

> Ghi lại toàn bộ nội dung đã học, giải thích kỹ như cho người mới bắt đầu. Các mục đánh dấu 🎯 là kiến thức **gần như chắc chắn gặp khi phỏng vấn Java** — nên đọc kỹ, học thuộc cách diễn đạt, và tự luyện nói lại bằng lời của mình.

---

## NGÀY 1

### Đã làm gì hôm nay

1. Đánh giá kiến trúc + nghiệp vụ của project gốc, phát hiện và quyết định sửa 6 vấn đề
2. Bước 0 — Setup project (VS Code + Maven + SQL Server)
3. Bước 1 — Core Models (`IGoods`, `Goods`, `RawMaterial`, `FinishedProduct`, `User`, `LoginRequest`)

---

## PHẦN A — Đánh Giá Kiến Trúc & Nghiệp Vụ

### A.1 Kiến trúc phân lớp (Layered Architecture) 🎯

Ứng dụng chia làm 4 tầng, tầng trên chỉ được gọi tầng ngay dưới, không nhảy cóc:

```
UI (Controller)  →  Service (business logic)  →  Repository/DAO (SQL)  →  Database
```

**Vì sao phải tách tầng?** Nguyên tắc gọi là **Separation of Concerns** (tách biệt mối quan tâm) — mỗi tầng chỉ có đúng 1 lý do để thay đổi. Nếu đổi database (SQL Server → MySQL), chỉ sửa tầng Repository, không đụng vào Controller hay Service. Đây là câu hỏi phỏng vấn rất hay gặp: *"Vì sao không gọi thẳng SQL trong Controller cho nhanh?"* — trả lời: vì sẽ làm code khó bảo trì, khó test, khó thay đổi công nghệ sau này (tight coupling giữa các tầng).

**DAO Pattern (Data Access Object):** `GoodsRepository`, `UserRepository` là ví dụ — che giấu toàn bộ chi tiết SQL đằng sau các method có tên rõ nghĩa (`insert`, `findAll`, `delete`...). Tầng Service gọi các method này mà không cần biết SQL viết ra sao.

### A.2 Các vấn đề đã phát hiện và vì sao phải sửa

| Vấn đề | Vì sao là vấn đề thật sự | Đã sửa thế nào |
|---|---|---|
| Field `public` trong `Goods` | Vi phạm **Encapsulation** — bất kỳ code nào cũng gán được giá trị bậy (VD `quantity = -999`) mà không qua kiểm tra | Chuyển hết sang `private` + getter/setter (chi tiết Phần C) |
| Password hash SHA-256 không salt | Dễ bị tấn công **rainbow table** (bảng tra cứu hash sẵn của các password phổ biến) | Thêm `salt` ngẫu nhiên riêng từng user (Phần D) |
| 1 `Connection` JDBC dùng chung cho cả app | JDBC `Connection` **không thread-safe** — 2 luồng cùng dùng 1 Connection có thể sinh lỗi ngẫu nhiên | Mở `Connection` mới mỗi lần gọi, đóng ngay sau khi dùng (Phần E) |
| Đăng ký công khai được tự chọn role `admin` | Lỗi logic bảo mật thật sự — ai cũng tự phong mình làm admin được | `AuthService.register()` không nhận tham số `role` nữa, luôn gán `"user"` |
| Ngưỡng cảnh báo tồn kho cố định theo *loại* hàng (`<10`/`<5`) | Không đúng thực tế — mỗi mặt hàng cần ngưỡng khác nhau | Thêm field `minStockLevel` riêng từng mặt hàng |
| Serialization (`saveToFile`/`loadFromFile`) không có nút bấm nào gọi tới | Code "chết" — tồn tại nhưng không ai dùng được | Nối vào nút "Sao lưu"/"Phục hồi" thật trên UI |

---

## PHẦN B — Bước 0: Setup Project

### B.1 Maven là gì, vì sao dùng? 🎯

Maven là **công cụ quản lý dependency (thư viện) và build project** cho Java. Trước Maven, muốn dùng 1 thư viện, bạn phải tự tải file `.jar` từ internet, bỏ vào thư mục `lib/`, rồi tự thêm vào classpath — làm thủ công, dễ thiếu, khó đồng bộ giữa các máy.

Với Maven, bạn chỉ khai báo trong `pom.xml`:
```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.6.2.jre11</version>
</dependency>
```
Maven tự tải đúng phiên bản đó (và các thư viện phụ thuộc của nó) về máy, cache lại ở `~/.m2/repository`, rồi tự thêm vào classpath khi build. Ai clone project về, chỉ cần chạy `mvn clean compile` là có đủ thư viện — không cần gửi kèm file `.jar`.

**`groupId`/`artifactId`/`version`** — đây là bộ 3 định danh duy nhất 1 thư viện trên "kho" Maven Central (giống như tên nhà xuất bản + tên sách + số phiên bản).

### B.2 Vì sao JavaFX cần cấu hình `--module-path`/`--add-modules`? 🎯

Từ Java 9 trở đi, Java giới thiệu **hệ thống module (JPMS — Java Platform Module System)**. Từ Java 11, JavaFX bị tách ra khỏi JDK (trước đó JavaFX được "bundle" sẵn trong JDK 8), trở thành module riêng phải khai báo tường minh:
- `--module-path`: chỉ đường dẫn tới nơi chứa module JavaFX
- `--add-modules javafx.controls,javafx.fxml`: khai báo module nào được phép dùng

`javafx-maven-plugin` trong `pom.xml` tự động thêm 2 cờ này khi bạn chạy `mvn javafx:run`, nên bạn không cần tự gõ tay.

### B.3 JDBC là gì? Vì sao cần "driver" riêng? 🎯

**JDBC (Java Database Connectivity)** là một **API chuẩn** (interface) do Java định nghĩa để giao tiếp với database — nó **không tự biết nói chuyện với SQL Server, MySQL hay PostgreSQL**. JDBC chỉ định nghĩa các interface như `Connection`, `Statement`, `ResultSet`... Mỗi loại database cần 1 **driver** — là bản cài đặt cụ thể (implementation) của các interface đó cho đúng loại DB. Đây chính là ví dụ thực tế của khái niệm OOP: "lập trình theo interface, không lập trình theo implementation cụ thể" — code Java gọi `Connection conn = DriverManager.getConnection(...)`, không cần biết bên dưới là driver nào, miễn có driver đúng trong classpath.

### B.4 Single Table Inheritance / Discriminator Column 🎯

Database quan hệ (SQL Server) **không có khái niệm "kế thừa class"** như Java. Để lưu 2 loại hàng (`RawMaterial`, `FinishedProduct`) có phần dữ liệu chung + phần riêng, ta dùng kỹ thuật gọi là **Single Table Inheritance**: gộp tất cả vào 1 bảng `Goods`, thêm 1 cột **discriminator** (`GoodsType`, giá trị `'R'` hoặc `'F'`) để biết dòng đó thuộc loại nào khi đọc lên. Cột nào chỉ 1 loại dùng (`Supplier`, `SellPrice`) thì để `NULL` ở dòng còn lại.

Đây là 1 trong 3 chiến lược phổ biến để ánh xạ kế thừa OOP xuống bảng quan hệ (2 chiến lược còn lại: Class Table Inheritance — mỗi subclass 1 bảng riêng, join qua khóa ngoại; Concrete Table Inheritance — mỗi subclass 1 bảng đầy đủ, không join). Single Table đơn giản nhất, phù hợp quy mô nhỏ.

---

## PHẦN C — Bước 1: Core Models (TRỌNG TÂM OOP — Đọc kỹ nhất)

### C.1 Interface vs Abstract Class 🎯🎯🎯 (câu hỏi phỏng vấn kinh điển, gần như chắc chắn gặp)

| | Interface (`IGoods`) | Abstract Class (`Goods`) |
|---|---|---|
| Có field (biến instance)? | Không (chỉ hằng số `public static final` ngầm định) | Có (`code`, `name`, `quantity`...) |
| Có method đã cài đặt sẵn? | Không (trừ `default`/`static` từ Java 8+, không dùng trong project này) | Có thể có (VD `IsLow()` viết sẵn hoàn chỉnh) |
| 1 class kế thừa được bao nhiêu? | `implements` được **nhiều** interface cùng lúc | `extends` được **đúng 1** class (Java không đa kế thừa class) |
| Dùng khi nào? | Định nghĩa 1 "khả năng"/"hợp đồng hành vi" mà nhiều nhóm class không liên quan cùng cam kết (VD `Serializable`, `Comparable`) | Các subclass thực sự "là một loại của" nhau (is-a) và muốn **chia sẻ code chung**, chỉ để lại phần khác biệt cho subclass tự viết |

**Câu trả lời mẫu khi phỏng vấn hỏi "khi nào dùng interface, khi nào dùng abstract class":**
> "Interface khi tôi muốn định nghĩa một khả năng mà nhiều class không họ hàng với nhau đều có thể có — ví dụ `IGoods` chỉ nói 'phải biết Input, PrintInfo, IsLow', không quan tâm class đó là gì. Abstract class khi các subclass chắc chắn cùng 1 họ và tôi muốn viết sẵn code dùng chung để tránh lặp lại — ví dụ `Goods` viết sẵn `IsLow()` một lần, `RawMaterial`/`FinishedProduct` không cần viết lại."

**Trong project:** `Goods implements IGoods` — tách `IGoods` ra riêng vì lý do: nếu sau này có 1 class hoàn toàn khác `Goods` (VD `Warehouse` hoặc 1 entity mới) cũng muốn có hành vi `Input/PrintInfo/IsLow`, nó có thể `implements IGoods` mà không phải kế thừa toàn bộ field của `Goods`.

### C.2 Encapsulation (Tính đóng gói) 🎯🎯

**Định nghĩa:** Giấu dữ liệu nội bộ của object (field `private`), chỉ cho truy cập/thay đổi thông qua method công khai (getter/setter). Đây là 1 trong 4 trụ cột OOP (cùng với Inheritance, Polymorphism, Abstraction).

**Vì sao quan trọng?**
1. **Kiểm soát tính hợp lệ** — nếu field `public`, ai cũng gán `quantity = -999` được. Với `setQuantity(int quantity)`, ta có chỗ để sau này chèn kiểm tra (`if (quantity < 0) throw ...`) mà không ảnh hưởng code đã gọi nó.
2. **Information hiding** — code bên ngoài không cần biết dữ liệu được lưu trữ như thế nào bên trong, chỉ cần biết "gọi method nào để lấy/đổi giá trị". Nếu sau này đổi cách lưu trữ nội bộ (VD đổi `int` sang `BigDecimal`), code gọi getter/setter không cần sửa.
3. Đây cũng là điều kiện tiên quyết để làm **validate dữ liệu tập trung 1 chỗ** thay vì rải rác khắp nơi gọi đến field đó.

**Trong project:** mọi field của `Goods`, `RawMaterial`, `FinishedProduct`, `User` đều chuyển từ `public` sang `private`, đi kèm getter/setter (VD `getCode()`/`setCode()`).

### C.3 Inheritance (Kế thừa) và từ khóa `super` 🎯

`class RawMaterial extends Goods` — `RawMaterial` **kế thừa** toàn bộ field và method không phải `private` của `Goods`. Đây là quan hệ **is-a** (RawMaterial LÀ MỘT loại Goods).

`super.Input(sc)` — gọi lại đúng method `Input()` của lớp cha `Goods` (nhập `code`, `name`, `unit`, `quantity`, `minStockLevel`), rồi mới viết thêm code nhập `supplier` riêng của `RawMaterial`. Đây là cách tái sử dụng code chuẩn — tránh copy-paste logic giống nhau ở `RawMaterial` và `FinishedProduct`.

**Lưu ý quan trọng:** Java chỉ hỗ trợ **đơn kế thừa class** (1 class chỉ `extends` được 1 class khác) — khác C++ (có đa kế thừa). Đây là lý do Java có interface: để "bù đắp" cho việc không đa kế thừa được, 1 class vẫn có thể `implements` nhiều interface cùng lúc.

### C.4 Polymorphism (Đa hình) 🎯🎯🎯 (phần hay bị hỏi sâu nhất)

**Đa hình lúc runtime (Runtime Polymorphism / Dynamic Dispatch):** Khai báo biến kiểu lớp cha, nhưng gán instance thực tế là 1 trong các lớp con. Khi gọi method đã bị override, JVM tự tra đúng phiên bản của **class thật sự đứng sau biến đó lúc chạy chương trình** (runtime), không phải theo kiểu đã khai báo lúc viết code (compile time).

```java
Goods g = new RawMaterial();
g.calcStockValue();   // Dù g khai báo kiểu Goods, JVM vẫn chạy đúng công thức của RawMaterial
```

Cơ chế bên dưới gọi là **virtual method invocation** (hay dynamic method dispatch) — JVM tra bảng phương thức ảo (vtable) của object thực tế lúc runtime để tìm đúng implementation.

**Vì sao quan trọng với project này?** Đây chính là nền tảng để `WarehouseService<T extends Goods>` (học ở Bước 3) có thể xử lý đồng nhất mọi loại hàng — vòng lặp `for (T g : list) g.calcStockValue()` chạy đúng công thức cho từng phần tử mà code không cần viết `if (g instanceof RawMaterial) ... else if (g instanceof FinishedProduct) ...` ở khắp nơi.

**Phân biệt 2 loại "đa hình" — CÂU HỎI PHỎNG VẤN GẦN NHƯ CHẮC CHẮN GẶP:**

| | Method Overriding (Ghi đè) | Method Overloading (Nạp chồng) |
|---|---|---|
| Xảy ra ở đâu | Giữa lớp cha và lớp con (`extends`) | Trong cùng 1 class |
| Chữ ký method | **Giống hệt** (tên + tham số) | **Cùng tên, khác tham số** (số lượng/kiểu) |
| Khi nào quyết định gọi hàm nào | Lúc **runtime** (dynamic — phụ thuộc kiểu thực sự của object) | Lúc **compile-time** (static — phụ thuộc kiểu tham số truyền vào lúc gọi) |
| Ví dụ trong project | `calcStockValue()` của `RawMaterial` ghi đè `Goods` | Chưa có ví dụ rõ trong project — nhưng ví dụ kinh điển: `System.out.println(int)` vs `println(String)` |
| Annotation liên quan | `@Override` | Không có annotation riêng |

### C.5 `@Override` annotation

Không bắt buộc về mặt cú pháp — code vẫn chạy nếu bỏ đi. Nhưng **luôn nên dùng**: nếu bạn gõ sai tên method (VD gõ nhầm `calcStockvalue` thường/hoa) mà tưởng đang override, compiler sẽ báo lỗi ngay lập tức thay vì âm thầm tạo ra 1 method **mới** hoàn toàn không liên quan — một loại bug rất khó phát hiện nếu không có annotation này.

### C.6 Access Modifiers (Bổ ngữ truy cập) 🎯

| Modifier | Truy cập được từ đâu |
|---|---|
| `private` | Chỉ trong cùng 1 class |
| (không ghi gì / package-private) | Trong cùng package |
| `protected` | Cùng package + subclass ở package khác |
| `public` | Mọi nơi |

Nguyên tắc chung: luôn chọn phạm vi truy cập **hẹp nhất có thể** — đây cũng là lý do field nên `private`, chỉ mở rộng ra `public` khi thật sự cần (VD chính method getter/setter).

### C.7 `Serializable` và `serialVersionUID` 🎯

`Serializable` là 1 **marker interface** — interface hoàn toàn rỗng, không có method nào cả. Nó không "bắt" class phải làm gì, chỉ đơn thuần **đánh dấu** cho JVM biết: "object của class này được phép chuyển thành một chuỗi byte (serialize) để lưu xuống file hoặc gửi qua mạng, và đọc ngược lại (deserialize) sau này".

`serialVersionUID` là 1 số định danh phiên bản của class. Nếu không khai báo tường minh, JVM tự tính 1 giá trị dựa theo cấu trúc hiện tại của class — nghĩa là chỉ cần sửa class (thêm/bớt field) là giá trị này đổi theo. Hậu quả: nếu bạn serialize 1 object rồi sau đó sửa class rồi thử đọc lại file cũ, JVM phát hiện `serialVersionUID` không khớp và ném `InvalidClassException`. Khai báo tay 1 giá trị cố định (`1L`) giúp bạn **chủ động kiểm soát** khi nào coi là "phiên bản không tương thích", thay vì để JVM tự quyết theo cấu trúc class.

### C.8 Java `record` (Java 16+)

`public record LoginRequest(String username, String password) {}` — cú pháp rút gọn để khai báo 1 **data carrier bất biến (immutable)**. Chỉ với 1 dòng, Java tự sinh:
- Constructor `LoginRequest(String, String)`
- 2 "accessor" là `username()`/`password()` (**không** có tiền tố `get`, khác getter JavaBean thông thường)
- `equals()`, `hashCode()`, `toString()` đã cài đặt sẵn dựa theo toàn bộ field

So với 1 class thường: `record` **không có setter** (đúng nghĩa bất biến — tạo xong không đổi được), và **ngầm định là `final`** (không thể bị kế thừa thêm). Phù hợp cho các DTO chỉ có nhiệm vụ "chở dữ liệu" từ nơi này sang nơi khác, không có hành vi nghiệp vụ riêng.

---

## PHẦN D — Bảo Mật: Password Hashing + Salt (đã bàn hôm nay, sẽ code ở Bước 4)

### D.1 Vì sao không lưu password dạng plain text? 🎯

Nếu database bị lộ (hack, backup rò rỉ...), kẻ tấn công có ngay toàn bộ password thật của user — rất nguy hiểm vì nhiều người dùng lại 1 password cho nhiều nơi khác nhau.

### D.2 Hash là gì? 🎯

**Hash function** (hàm băm) là hàm **1 chiều** (one-way): biến input (password) thành 1 chuỗi cố định độ dài (VD SHA-256 luôn ra 256 bit), nhưng **không thể tính ngược lại** từ hash để ra password gốc. Khi đăng nhập, hệ thống không "giải mã" hash để so sánh — mà hash lại password người dùng vừa nhập, rồi so 2 chuỗi hash với nhau.

### D.3 Salt là gì, vì sao cần? 🎯🎯

Nếu chỉ hash trần (không salt), 2 user cùng dùng password `"123456"` sẽ ra **cùng 1 chuỗi hash** — kẻ tấn công có thể dùng 1 bảng tra cứu sẵn (**rainbow table**: hash của hàng triệu password phổ biến đã tính trước) để tra ngược ra password gốc chỉ bằng cách so khớp chuỗi hash.

**Salt** là 1 chuỗi ngẫu nhiên, **sinh riêng cho mỗi user**, được trộn vào trước khi hash: `hash(salt + password)`. Vì salt khác nhau, 2 user cùng password `"123456"` sẽ ra 2 chuỗi hash **hoàn toàn khác nhau** — vô hiệu hóa hoàn toàn kiểu tấn công rainbow table (kẻ tấn công phải tính lại từ đầu cho từng salt riêng, tốn kém đến mức không khả thi).

Salt **không cần giữ bí mật** — lưu thẳng trong DB cùng hàng với password hash cũng không sao, vì tác dụng của nó là chống tính sẵn (rainbow table), không phải để giấu.

---

## PHẦN E — JDBC Connection & Thread-Safety (đã bàn hôm nay, sẽ code ở Bước 2-3)

### E.1 "Thread-safe" nghĩa là gì? 🎯🎯

Một đoạn code/object được gọi là **thread-safe** nếu nó cho ra kết quả đúng ngay cả khi **nhiều luồng (thread) cùng truy cập đồng thời**. Nếu không thread-safe, 2 luồng cùng thao tác lên cùng 1 object cùng lúc có thể sinh ra **race condition** — kết quả sai lệch, khó tái hiện lại lỗi (vì phụ thuộc thời điểm 2 luồng chen nhau chạy).

### E.2 Vì sao `Connection` của JDBC không nên dùng chung giữa nhiều thread?

JDBC `Connection`, `Statement`, `ResultSet` **không được đảm bảo an toàn khi dùng đa luồng**. Ứng dụng này có nhiều thao tác chạy trên thread nền song song (`loadFromDBAsync`, `importAsync`, `exportAsync`...) — nếu tất cả dùng chung 1 `Connection` tĩnh, 2 luồng có thể vô tình "giẫm chân" lên nhau (VD 1 luồng đang đọc `ResultSet` thì luồng khác gửi câu lệnh SQL khác trên cùng Connection đó), gây lỗi khó dự đoán.

**Giải pháp đã áp dụng:** `DatabaseConfig.getConnection()` mở 1 `Connection` **mới hoàn toàn** mỗi lần được gọi, và mỗi method trong Repository tự đóng nó lại ngay sau khi dùng xong (`try-with-resources`). Mỗi luồng có Connection riêng của mình → không còn tranh chấp.

---

## Tổng Kết — Checklist Nên Tự Kiểm Tra Trước Khi Phỏng Vấn

Tự hỏi bản thân, cố gắng trả lời thành lời (nói to hoặc viết ra) không nhìn tài liệu:

- [ ] Interface khác abstract class ở điểm nào? Cho ví dụ thực tế bạn đã viết.
- [ ] Encapsulation là gì, tại sao không nên để field `public`?
- [ ] Polymorphism runtime hoạt động thế nào ở tầng JVM (dynamic dispatch)?
- [ ] Overriding khác Overloading ở điểm nào — quyết định lúc nào (compile-time hay runtime)?
- [ ] `super` dùng để làm gì, cho ví dụ?
- [ ] `Serializable` là loại interface gì (marker interface)? `serialVersionUID` để làm gì?
- [ ] Vì sao cần salt khi hash password? Nếu không có salt thì bị tấn công kiểu gì?
- [ ] Thread-safe nghĩa là gì? Vì sao JDBC Connection không nên share giữa nhiều thread?
- [ ] Vì sao tách kiến trúc thành nhiều tầng (Controller/Service/Repository)?

---

## NGÀY 2

### Đã làm gì hôm nay

1. **Giai đoạn 2.1** — `DatabaseConfig.java`, test kết nối DB thành công (`DB connected: true`)
2. **Giai đoạn 2.2** — `GoodsRepository.java` nửa đầu (`insert`, `findAll`, `findByCode`, `mapRow`)
3. Thêm comment tiếng Anh (tổng quan file + mô tả từng method) cho toàn bộ file đã viết ở Phase 1 + Phase 2
4. **Giai đoạn 2.3** — `GoodsRepository.java` nửa sau (`findByType`, `updateQuantity`, `update`, `delete`, `calcTotalStockValue`) + toàn bộ `UserRepository.java`
5. Test end-to-end với database thật — xác nhận **Phase 2 (Data Layer) hoàn tất**

Hôm nay gõ tay code nên phát sinh vài lỗi thật (compile lỗi + SQL sai cú pháp) — phần dưới phân tích kỹ từng lỗi, vì **đọc hiểu thông báo lỗi compiler/SQL là kỹ năng quan trọng ngang với viết code đúng ngay từ đầu**.

---

## PHẦN F — Checked Exception vs Unchecked Exception 🎯🎯🎯 (câu hỏi phỏng vấn Java gần như chắc chắn gặp)

Đây là kiến thức nền tảng giải thích vì sao `DatabaseConfig.getConnection()` phải khai báo `throws SQLException`, còn `Goods`/`Validator` ném `IllegalArgumentException` thì không cần khai báo gì.

**Checked Exception:**
- Là exception mà compiler **bắt buộc** bạn phải xử lý — hoặc `catch`, hoặc khai báo tiếp `throws` lên trên. Nếu không, code **không compile được**.
- Kế thừa từ `Exception` (nhưng không kế thừa từ `RuntimeException`).
- Dùng cho lỗi **có thể lường trước và caller nên chủ động xử lý** — ví dụ `SQLException` (kết nối DB có thể chập chờn, đây là điều kiện bên ngoài chương trình không kiểm soát được, nhưng caller cần biết để xử lý, VD hiện thông báo "mất kết nối DB").
- Ví dụ trong project: `SQLException` (JDBC), `IOException` (đọc/ghi file — sẽ gặp ở `WarehouseService` Phase 3).

**Unchecked Exception:**
- Compiler **không bắt buộc** xử lý — chương trình vẫn compile được dù không `catch`, chỉ là nếu không bắt thì nó bay lên và làm crash luồng hiện tại lúc runtime.
- Kế thừa từ `RuntimeException`.
- Dùng cho lỗi **do lập trình sai / vi phạm hợp đồng** — về nguyên tắc "không nên xảy ra nếu code gọi đúng cách", nên không bắt buộc phải `catch` ở khắp mọi nơi gọi.
- Ví dụ trong project: `IllegalArgumentException`, `IllegalStateException` (đã dùng ở `Goods`/`Validator`), `NullPointerException`, `ArrayIndexOutOfBoundsException`.

**Bảng so sánh nhanh:**

| | Checked | Unchecked |
|---|---|---|
| Lớp cha | `Exception` (không phải `RuntimeException`) | `RuntimeException` |
| Compiler bắt buộc xử lý? | Có — phải `catch` hoặc `throws` | Không |
| Dùng khi nào | Lỗi từ môi trường ngoài (DB, file, mạng...) mà caller **nên** chủ động xử lý | Lỗi logic/lập trình sai, không kỳ vọng xảy ra nếu dùng đúng API |
| Ví dụ trong project | `SQLException` | `IllegalArgumentException`, `IllegalStateException` |

**Câu trả lời mẫu khi phỏng vấn hỏi "khi nào dùng checked, khi nào dùng unchecked":**
> "Tôi dùng checked exception khi lỗi đến từ tài nguyên bên ngoài mà tôi muốn ép caller phải nghĩ đến trường hợp thất bại — ví dụ thao tác DB, đọc file. Tôi dùng unchecked exception cho lỗi do gọi sai tham số hoặc sai trạng thái — vì bắt buộc `catch` ở khắp nơi cho những lỗi lẽ ra không nên xảy ra sẽ làm code rối, chỗ nào thực sự cần xử lý thì tự bắt bằng `try/catch` cụ thể."

---

## PHẦN G — `PreparedStatement` chống SQL Injection 🎯🎯 (nhắc lại kỹ, đây là kiến thức bảo mật backend cơ bản nhất)

Hôm nay viết `GoodsRepository`/`UserRepository`, mọi câu SQL có tham số đều dùng `PreparedStatement` với dấu `?`, không nối chuỗi tay. Nhắc lại lý do:

Nếu viết kiểu nối chuỗi:
```java
String sql = "SELECT * FROM Users WHERE Username = '" + username + "'";
```
Một input độc hại như `username = "x' OR '1'='1"` sẽ biến câu lệnh thành:
```sql
SELECT * FROM Users WHERE Username = 'x' OR '1'='1'
```
Điều kiện `'1'='1'` luôn đúng → trả về **toàn bộ** bảng Users thay vì đúng 1 user — kẻ tấn công vượt qua được kiểm tra đăng nhập hoặc đọc lộ toàn bộ dữ liệu. Đây gọi là **SQL Injection**, một trong những lỗ hổng bảo mật web/backend phổ biến và nguy hiểm nhất (nằm trong OWASP Top 10).

`PreparedStatement` gửi cấu trúc câu SQL (`SELECT ... WHERE Username = ?`) và giá trị tham số (`username`) tách biệt xuống DB engine ngay từ đầu — DB hiểu rõ đâu là code, đâu là dữ liệu thuần túy, nên dữ liệu không bao giờ bị "diễn giải lại" thành cú pháp SQL, dù người dùng nhập ký tự đặc biệt gì.

**Nguyên tắc nhớ:** *bất kỳ giá trị nào đến từ bên ngoài (form nhập liệu, tham số API...) mà đưa vào câu SQL đều phải qua `PreparedStatement`.* Không bao giờ nối chuỗi SQL bằng `+` với dữ liệu người dùng.

---

## PHẦN H — try-with-resources với nhiều resource cùng lúc

```java
try (Connection conn = DatabaseConfig.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    ...
}
```

Khai báo nhiều resource trong cùng 1 `try (...)`, ngăn cách bằng dấu `;`. Khi ra khỏi block (dù chạy xong bình thường hay có exception), Java tự gọi `close()` cho từng resource theo thứ tự **ngược lại** với lúc khai báo — ở đây đóng `ps` trước, `conn` sau. Điều kiện bắt buộc: resource phải implement interface `AutoCloseable` (`Connection`, `PreparedStatement`, `ResultSet` đều có).

Không cần viết `finally { ps.close(); conn.close(); }` tay — vừa dài dòng, vừa dễ quên đóng nếu có nhiều đường return khác nhau trong method (dễ xảy ra bug rò rỉ resource — "resource leak").

---

## PHẦN I — Debug thực chiến: đọc hiểu lỗi compiler

### I.1 Lỗi `';' expected`

Gặp hôm nay: `return u` thiếu dấu `;`. Thông báo lỗi Java **luôn chỉ đúng dòng/cột** nơi trình biên dịch phát hiện ra vấn đề — không phải lúc nào cũng là chỗ "sai" theo trực giác, nhưng gần như luôn là gợi ý chính xác nơi cần nhìn vào trước tiên.

### I.2 Lỗi `package ... does not exist` (đã gặp ở Giai đoạn 2.1)

Nhắc lại ngắn gọn: xảy ra khi chạy `javac` từ sai thư mục (đứng bên trong package thay vì đứng ở gốc package `src/main/java`). Bài học chung: **khi project dùng Maven, luôn để Maven/VS Code Run lo việc biên dịch — không tự gọi `javac`/`java` tay** trừ khi hiểu rõ khái niệm classpath/sourcepath.

### I.3 Các lỗi gõ tay hay gặp nhất hôm nay (rút kinh nghiệm)

| Lỗi đã gặp | Loại lỗi | Hậu quả |
|---|---|---|
| `VALUE(...)` thay vì `VALUES(...)` | Sai cú pháp SQL (T-SQL yêu cầu số nhiều) | Compile Java qua được, nhưng `SQLException` lúc chạy |
| `SELECT * FORM Users...` | Gõ nhầm `FORM`/`FROM` | Compile Java qua được, `SQLException` lúc chạy |
| `ps =m conn.prepareStatement(...)` | Thừa ký tự, sai cú pháp Java | Lỗi **compile**, không chạy được |
| `return u` thiếu `;` | Thiếu cú pháp Java | Lỗi **compile** |
| `rs.getNString(...)` cho cột `VARCHAR` | Dùng nhầm method (dành cho `NVARCHAR`) | Không lỗi ngay, nhưng sai kiểu dữ liệu về mặt ý nghĩa — nên dùng `getString` |

**Rút ra quy luật:** lỗi cú pháp **SQL** (viết trong chuỗi String) không bao giờ bị Java compiler bắt được — vì với Java, đó chỉ là 1 chuỗi ký tự bình thường. Lỗi SQL chỉ lộ ra **lúc chạy chương trình** (runtime), khi DB engine thực sự phân tích cú pháp câu lệnh. Đây là lý do phải **luôn test chạy thử** (không chỉ dựa vào "compile được là chắc đúng") — điều mà Roadmap đã nhắc ở mỗi giai đoạn có ghi "Test nhanh".

---

## Tổng Kết Bổ Sung — Checklist Phỏng Vấn (cập nhật thêm sau Ngày 2)

- [ ] Checked exception khác Unchecked exception ở điểm nào? Cho ví dụ mỗi loại trong project.
- [ ] Vì sao `SQLException` là checked còn `IllegalArgumentException` là unchecked?
- [ ] SQL Injection là gì? `PreparedStatement` ngăn chặn nó bằng cách nào (khác `Statement` ở điểm nào)?
- [ ] try-with-resources hoạt động thế nào khi có nhiều resource? Thứ tự đóng ra sao?
- [ ] Vì sao lỗi sai cú pháp SQL không bị Java compiler phát hiện ra ngay?

---

## NGÀY 3

### Đã làm gì hôm nay

1. **Giai đoạn 3.1** — `WarehouseService`: phần Singleton + CRUD (Generic `<T extends Goods>`, `Map`+`List`, `synchronized(lock)`)
2. Phát hiện và sửa 3 lỗi thật trong code tự gõ, test end-to-end xác nhận cache (RAM) và database luôn đồng bộ đúng sau import/export/delete

---

## PHẦN J — Generic Type `<T extends Goods>` (Bounded Type Parameter) 🎯🎯

**Generic là gì?** Cho phép viết 1 class/method dùng được với **nhiều kiểu dữ liệu khác nhau**, mà vẫn giữ được kiểm tra kiểu lúc compile (type-safe) — không cần ép kiểu (cast) thủ công như kiểu Java đời cũ (trước Java 5, mọi collection đều chứa `Object`, phải tự cast, dễ lỗi lúc runtime).

```java
public class WarehouseService<T extends Goods> { ... }
```

- `T` là **type parameter** — 1 "biến kiểu dữ liệu", được xác định cụ thể lúc dùng: `WarehouseService<Goods>`, `WarehouseService<RawMaterial>`...
- `extends Goods` là **bounded type parameter** (ràng buộc kiểu) — bắt `T` phải là `Goods` hoặc con cháu của nó. Nếu không ràng buộc gì (chỉ viết `<T>`), bên trong class không thể gọi `g.getCode()`/`g.calcStockValue()` vì trình biên dịch không biết chắc `T` có những method này hay không — ràng buộc `extends Goods` chính là lời hứa "dù T là gì, nó luôn có tối thiểu các method của Goods".

**Vì sao không dùng thẳng `WarehouseService` (không generic), lưu `Map<String, Goods>`?**
Cách đó cũng chạy được, nhưng generic cho phép về mặt lý thuyết bạn viết `WarehouseService<RawMaterial>` chỉ chứa đúng 1 loại, mọi thao tác `getAll()` trả `List<RawMaterial>` sẵn, không phải tự lọc `instanceof` hay ép kiểu lại. Trong project này thực tế chỉ dùng `WarehouseService<Goods>` (chứa mọi loại), nhưng thiết kế generic vẫn có giá trị: nó là minh chứng hiểu về **type-safety** — một khái niệm chắc chắn được hỏi khi phỏng vấn về Collections Framework.

**Câu trả lời mẫu khi phỏng vấn hỏi "Generic để làm gì, cho ví dụ":**
> "Generic giúp code dùng lại được cho nhiều kiểu dữ liệu mà vẫn kiểm tra kiểu lúc compile, tránh phải ép kiểu tay và tránh lỗi `ClassCastException` lúc runtime. Ví dụ tôi viết `WarehouseService<T extends Goods>` — cùng logic CRUD, Map+List, threading dùng được cho `Goods` hay bất kỳ subclass nào, miễn là subclass đó `extends Goods`."

---

## PHẦN K — `synchronized` hoạt động thế nào bên dưới? (Intrinsic Lock / Monitor) 🎯🎯

Mỗi object trong Java có sẵn 1 "khóa nội tại" (intrinsic lock, còn gọi monitor) đi kèm — không cần khai báo gì thêm. `synchronized (lock) { ... }` nghĩa là:

1. Luồng muốn vào khối code phải **giành được khóa** của object `lock` trước
2. Nếu khóa đang bị 1 luồng khác giữ, luồng này phải **đợi (block)** đến khi khóa được nhả ra
3. Khi vào được, chạy xong khối code, luồng tự động **nhả khóa** (kể cả khi có exception ném ra giữa chừng — tương tự cách `finally` luôn chạy)

**Vì sao dùng `private final Object lock = new Object();` riêng, thay vì `synchronized` cả method (tương đương khóa trên `this`)?**
Nếu khóa trên `this` (`synchronized void method() {...}`, ngầm hiểu là khóa trên chính instance `WarehouseService`), bất kỳ đoạn code nào khác (kể cả code bên ngoài class, nếu lỡ có tham chiếu tới cùng instance) cũng có thể vô tình `synchronized(warehouseServiceInstance)` cho mục đích khác, dẫn tới tranh chấp khóa không liên quan tới nhau, dễ gây **deadlock** khó debug. Dùng 1 object `lock` riêng tư, chỉ class này biết tới, đảm bảo khóa đó **chỉ** dùng cho đúng mục đích bảo vệ `map`/`list`.

**Lưu ý thực tế đã áp dụng:** khối `synchronized` trong `loadFromDB()` chỉ bọc đúng đoạn "swap" (`map.clear()/putAll()`, `list.clear()/addAll()`), KHÔNG bọc luôn cả `repo.findAll()` (thao tác DB chậm) — giữ khối `synchronized` **càng ngắn càng tốt** là nguyên tắc quan trọng: khóa giữ càng lâu, các luồng khác phải đợi càng lâu, làm giảm hiệu năng đa luồng dù vẫn đúng.

---

## PHẦN L — Bài Học Từ Lỗi Thật: Đồng Bộ Cache và Database 🎯 (lỗi rất hay gặp trong dự án thực tế)

Lỗi phát hiện hôm nay trong `exportGoods()`:

```java
// Trước khi sửa:
g.setQuantity(g.getQuantity() - qty);
// → THIẾU: repo.updateQuantity(code, g.getQuantity());
```

Code chỉ trừ số lượng trên **object trong RAM** (`map`/`list` — cache), nhưng **quên ghi xuống database**. Hậu quả cực kỳ nguy hiểm dù không có lỗi compile hay lỗi SQL nào cả:
- App vẫn chạy bình thường, `findByCode()` vẫn trả số liệu "đúng" — vì đọc từ cache
- Nhưng tắt app đi mở lại (`loadFromDB()` load lại từ DB) → số lượng quay về **giá trị cũ trước khi xuất kho** — dữ liệu xuất kho bị "mất" hoàn toàn
- Đây là loại bug **im lặng, không ném exception nào**, chỉ phát hiện được khi test kỹ (kiểm tra DB thật, không chỉ tin vào giá trị trong RAM) — giống bài học "Rút ra quy luật" ở Ngày 2 về việc phải test thực tế.

**Nguyên tắc chung cần khắc cốt ghi tâm khi thiết kế có cache:** Bất kỳ khi nào hệ thống giữ **2 nguồn dữ liệu song song** (ở đây: cache RAM và Database), **mọi thao tác ghi phải cập nhật ĐỦ CẢ HAI**, nếu không 2 nguồn sẽ "trôi" lệch nhau theo thời gian (gọi là **cache inconsistency** / cache bị "stale"). Đây là lý do khi review code có cache, luôn phải tự hỏi: *"Thao tác này có sửa dữ liệu không? Nếu có, đã cập nhật đủ mọi nơi lưu trữ dữ liệu đó chưa?"*

---

## Tổng Kết Bổ Sung — Checklist Phỏng Vấn (cập nhật thêm sau Ngày 3)

- [ ] Generic type là gì? Bounded type parameter (`<T extends X>`) khác gì so với `<T>` không ràng buộc?
- [ ] `synchronized` hoạt động thế nào bên dưới (intrinsic lock/monitor)? Vì sao nên khóa trên 1 object riêng thay vì khóa trên `this`?
- [ ] Vì sao khối `synchronized` nên giữ càng ngắn càng tốt?
- [ ] Cache inconsistency là gì? Cho ví dụ 1 lỗi thực tế đã gặp trong project.

---

## NGÀY 4

### Đã làm gì hôm nay

1. **Giai đoạn 3.2** — `WarehouseService`: phần Query/Algorithm (`filterByType`, `calcTotalStockValue`, `findLowStock`, `sortByQuantityDesc`, `sortedByStockValueDesc`, `findMinQuantity`)
2. **Đổi quy ước từ hôm nay:** code (comment, message, log) chuyển sang **tiếng Anh** — khớp với ngôn ngữ thực tế của source code project (bạn đã tự viết message tiếng Anh từ Giai đoạn 3.1). Nhật ký này vẫn giải thích bằng tiếng Việt.
3. Phát hiện và sửa 2 lỗi + dọn 1 import thừa, test end-to-end xác nhận cả 6 hàm chạy đúng với dữ liệu thật

---

## PHẦN M — `Comparator` vs `Comparable` 🎯🎯 (câu hỏi phỏng vấn Collections kinh điển)

Cả 2 đều dùng để so sánh 2 object nhằm mục đích sắp xếp, nhưng khác nhau ở **ai định nghĩa cách so sánh**:

| | `Comparable<T>` | `Comparator<T>` |
|---|---|---|
| Method chính | `compareTo(T other)` | `compare(T a, T b)` |
| Ai implement | Chính class đó tự implement (VD `Goods implements Comparable<Goods>`) | Một class/lambda **riêng biệt**, tách rời khỏi class được so sánh |
| Số cách sắp xếp | Chỉ 1 — gọi là "thứ tự tự nhiên" (natural ordering) của class đó | Nhiều tùy ý — mỗi tiêu chí 1 `Comparator` khác nhau |
| Dùng trong project | Không dùng — `Goods` không implement `Comparable` | `sortedByStockValueDesc()` dùng lambda `(a,b) -> Double.compare(...)`; `findMinQuantity()` dùng `Comparator.comparingInt(T::getQuantity)` |

**Vì sao project chọn `Comparator` thay vì bắt `Goods implements Comparable`?** Vì app cần sắp xếp theo **nhiều tiêu chí khác nhau** tùy thao tác của người dùng (theo số lượng, theo giá trị tồn kho...). Nếu dùng `Comparable`, `Goods` chỉ có được đúng 1 `compareTo()` cố định — muốn đổi tiêu chí sort phải sửa code trong chính class `Goods`. Dùng `Comparator` cho phép định nghĩa "cách so sánh" ngay tại nơi gọi sort, không đụng vào `Goods` chút nào.

**`T::getQuantity` là gì?** Gọi là **method reference** — cách viết rút gọn của lambda `g -> g.getQuantity()`, khi lambda chỉ đơn giản là "gọi 1 method có sẵn trên tham số truyền vào". Cùng ý nghĩa, ngắn hơn.

---

## PHẦN N — Bài Học Từ Lỗi Thật: Nhầm Biến Trong Nested Loop 🎯 (lỗi rất dễ mắc, rất khó tự nhìn ra)

Lỗi hôm nay trong `sortByQuantityDesc()`:

```java
// Trước khi sửa:
for (int j = i + 1; i < n; j++) { ... }
//                   ^^^^^ dùng nhầm biến vòng NGOÀI (i) thay vì biến vòng TRONG (j)
```

Đây là lỗi **cực kỳ dễ mắc** khi viết vòng lặp lồng nhau (nested loop) — gõ nhanh tay, mắt nhìn `i` quen từ dòng khai báo phía trên, gõ nhầm sang điều kiện dừng của vòng `j`. Hậu quả nghiêm trọng hơn thiếu dấu `;` nhiều:

- **Compiler KHÔNG báo lỗi gì cả** — vì `i` và `n` đều là biến `int` hợp lệ, đang tồn tại đúng phạm vi (scope) tại đó. Về mặt cú pháp và kiểu dữ liệu, dòng này hoàn toàn hợp lệ.
- Vì `i` **không đổi** trong suốt vòng lặp `j`, và tại thời điểm vào vòng `j` thì `i < n` luôn đúng (do vòng ngoài đã đảm bảo `i < n - 1`), điều kiện dừng gần như không bao giờ tự sai — vòng lặp `j` chạy mãi, `j` tăng vô hạn, đến khi `sorted.get(j)` vượt quá kích thước danh sách sẽ ném `IndexOutOfBoundsException` lúc runtime.

**Đây là ví dụ thực tế sinh động nhất cho bài học ở Ngày 2 và Ngày 3:** lỗi logic (không phải lỗi cú pháp, không phải lỗi kiểu dữ liệu) **không bao giờ bị compiler bắt được** — chỉ lộ ra khi chạy thử thực tế. Với vòng lặp lồng nhau, luôn tự kiểm tra lại: *"Điều kiện dừng của vòng trong có đang dùng đúng biến đếm của chính vòng trong không, hay lỡ tay dùng biến của vòng ngoài?"*

---

## NGÀY 5

### Đã làm gì hôm nay

1. Cập nhật và hoàn thiện toàn bộ **Phase 3, Phase 4 và Phase 5**:
   - `AuthService.java`: Viết `register()` nâng cao bảo mật (cố định `role = "user"` ở tầng Service).
   - `UserSession.java`: Tạo Static Singleton lưu thông tin người dùng đang đăng nhập trong RAM JVM.
   - `SceneSwitcher.java` & `App.java`: Khởi tạo và thiết lập điểm khởi chạy JavaFX.
2. Hoàn thiện đủ 5 Controllers giao diện: `LoginController`, `RegisterController`, `DashboardController`, `AddGoodsController`, `GoodsListController`.
3. Chuẩn hóa toàn bộ câu chữ, thông báo lỗi, trạng thái và nhãn hiển thị trong toàn bộ mã nguồn ứng dụng sang **Tiếng Anh chuẩn**.
4. Biên dịch thử nghiệm thành công (`mvn clean compile` — 23/23 file Java pass).

---

## PHẦN O — Phân Quyền 2 Lớp (Two-Layer Authorization Guard) 🎯🎯

Trong `GoodsListController`, nút "Delete" chỉ dành riêng cho tài khoản Admin:
- **Lớp 1 (UI Guard)**: `btnDelete.setDisable(!UserSession.isAdmin())` ngay trong `initialize()`. Nếu người dùng không phải Admin, nút Xóa sẽ bị khóa mờ trên giao diện.
- **Lớp 2 (Logic Guard)**: Trong `handleDelete()`, kiểm tra lại `if (!UserSession.isAdmin()) return;`. Đây là nguyên tắc phòng thủ đa lớp (Defense in depth) — không chỉ dựa vào việc nút bị ẩn/khóa trên giao diện, vì trong thực tế hàm xử lý nghiệp vụ có thể bị kích hoạt gián tiếp từ bên ngoài.

---

## PHẦN P — JavaFX TableView: `setCellValueFactory` vs `setCellFactory` 🎯🎯

- **`setCellValueFactory`**: Định nghĩa **dữ liệu gốc** lấy từ đối tượng model (`Goods`) để gắn vào cột (VD `colCode` đọc `g.getCode()`).
- **`setCellFactory`**: Định nghĩa **cách vẽ và tô màu giao diện động (Custom Rendering)** cho từng ô trong cột.
  - Cột `colType`: Tô màu cam cho `Raw Material` và màu xanh lá cho `Finished Product`.
  - Cột `colQty`: Khi `g.IsLow() == true` (số lượng nhỏ hơn ngưỡng `minStockLevel` riêng), ô sẽ tự động đổi sang màu chữ đỏ nổi bật để cảnh báo thủ kho.

---

---

## NGÀY 6

### Đã làm gì hôm nay

1. Hoàn thành toàn bộ các file FXML & CSS trong **Phase 6**:
   - `main.css`: Định nghĩa giao diện hiện đại với bộ màu Catppuccin Mocha, bo góc button, kiểu bảng TableView và thanh cuộn ScrollBar.
   - `login.fxml`: Form đăng nhập tối giản có `defaultButton="true"`.
   - `register.fxml`: Form đăng ký công khai (đã bỏ ComboBox chọn role).
   - `dashboard.fxml`: Giao diện điều hướng chính BorderPane với Header và Sidebar.
   - `add_goods.fxml`: Form nhập hàng hoá động hỗ trợ cuộn `ScrollPane`.
   - `goods_list.fxml`: Bảng danh sách hàng hoá TableView kèm toolbar 2 hàng và thống kê.
2. Kiểm tra biên dịch và copy tài nguyên Maven thành công (`6 resources copied`, `BUILD SUCCESS`).

---

## PHẦN Q — FXML & CSS Path Mapping Trong JavaFX 🎯🎯

1. **Đường dẫn tương đối trong FXML**:
   - `stylesheets="@../styles/main.css"`: Dấu `@` biểu thị đường dẫn tương đối tính từ thư mục chứa file `.fxml` (`com/warehousemanager/ui/views/`). Đi lên một cấp `..` vào `styles/` để tìm `main.css`.
2. **Đường dẫn tuyệt đối trong SceneSwitcher**:
   - `App.class.getResource("/com/warehousemanager/ui/views/" + fxml)`: Dấu `/` ở đầu biểu thị đường dẫn tuyệt đối tính từ gốc của Classpath. Khi Maven build, toàn bộ file trong `src/main/resources` sẽ được copy phẳng vào gốc `target/classes`.

---

## Tổng Kết Bổ Sung — Checklist Phỏng Vấn (cập nhật thêm sau Ngày 6)

- [ ] Đường dẫn `@../styles/main.css` trong FXML khác gì với `/com/...` trong Java code?
- [ ] Vì sao đặt `defaultButton="true"` trên nút bấm lại nâng cao trải nghiệm người dùng (UX)?
- [ ] Cấu trúc BorderPane phân chia màn hình thành 5 vùng như thế nào?

---

*Phase 6 đã hoàn thành! Tiếp tục Phase 7 — Kiểm thử End-to-End toàn bộ hệ thống...*


