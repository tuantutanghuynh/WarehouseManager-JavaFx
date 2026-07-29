package com.warehousemanager.utils;

// Static utility class: chỉ chứa các hàm static, không bao giờ tạo instance (new Validator()).
// Mọi hàm requireXxx đều theo 1 khuôn: kiểm tra 1 điều kiện, sai thì throw IllegalArgumentException
// kèm message rõ ràng -> controller chỉ cần catch (IllegalArgumentException e) rồi hiển thị e.getMessage().
public class Validator {

    // private constructor -> không ai new Validator() được, chỉ gọi qua Validator.tenHam(...)
    private Validator() {
    }

    // Kiểm tra không được để rỗng
    public static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be empty.");
        }
    }

    // kiểm tra đọ dài tối thiểu
    // Lưu ý: không tự gọi requireNonBlank ở đây -> nếu value null thì điều kiện "value != null && ..."
    // là false nên hàm này BỎ QUA, không throw. Muốn chắc chắn value vừa không rỗng vừa đủ dài
    // thì phải gọi cả 2 hàm (xem requireGoodsCode bên dưới làm mẫu).
    public static void requireMinLength(String value, String field, int min) {
        if (value != null && value.length() < min) {
            throw new IllegalArgumentException(field + " must have at least " + min + " characters.");
        }
    }

    // kiểm tra chuỗi không được chưa chữ số
    // ".*\\d.*" nghĩa là: bất kỳ đâu trong chuỗi có ít nhất 1 chữ số (\\d) thì match -> bị chặn.
    public static void requireNoDigits(String value, String field) {
        if (value != null && value.matches(".*\\d.*")) {
            throw new IllegalArgumentException(field + " must not contain numbers.");
        }
    }

    // kiểm tra chỉ chứa chữ cái và chữ số
    // Regex "[A-Za-z0-9]{minLen,maxLen}" nghĩa là: toàn bộ chuỗi chỉ gồm chữ/số,
    // và độ dài phải nằm trong khoảng [minLen, maxLen] (2 số này được nối thẳng vào regex).
    public static void requireAlphanumeric(String value, String field, int minLen, int maxLen) {
        if (value == null || !value.matches("[A-Za-z0-9]{" + minLen + "," + maxLen + "}")) {
            throw new IllegalArgumentException(
                    field + " must be " + minLen + "-" + maxLen + " alphanumeric characters.");
        }
    }

    // kiểm tra số thực double phải lớn hơn 0 - dùng cho giá bán, đơn giá
    public static void requirePositive(double value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0.");
        }
    }

    // kiểm tra số nguyên int không được âm (>= 0, dùng cho Ngưỡng tồn kho tối thiểu)
    // Khác với requirePositive: value = 0 ở đây là HỢP LỆ (VD tồn kho tối thiểu = 0
    // nghĩa là mặt hàng không cần cảnh báo), chỉ chặn số âm.
    public static void requireNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must be >= 0.");
        }
    }

    // kiểm tra giá trị nằm trong khoảng [min, max] (cả 2 đầu đều hợp lệ)
    public static void requireRange(int value, String field, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max + ".");
        }
    }

    // kiểm tra value phải thuộc 1 trong các option cho phép (không phân biệt hoa/thường)
    // Cách đọc: duyệt từng option, hễ khớp thì return NGAY (coi như hợp lệ, thoát hàm sớm).
    // Nếu duyệt hết vòng lặp mà không option nào khớp -> chạy tới dòng throw bên dưới.
    public static void requireOneOf(String value, String field, String... options) {
        for (String opt : options) {
            if (opt.equalsIgnoreCase(value)) {
                return;
            }
        }
        throw new IllegalArgumentException(field + " must be one of: " + String.join(", ", options));
    }

    // ép chuỗi nhập vào sang số thực double dương (>0)
    // Mục đích của try/catch: Double.parseDouble ném NumberFormatException (1 loại lỗi khác),
    // ta "bọc" lại thành IllegalArgumentException để controller chỉ cần bắt 1 loại exception
    // duy nhất cho mọi lỗi validate (không phải nhớ bắt thêm NumberFormatException riêng).
    public static double parsePositiveDouble(String raw, String field) {
        try {
            // cắt khoản trắng thừa và parse sang double
            double v = Double.parseDouble(raw.trim());
            // kiểm tra điều kiện >0
            requirePositive(v, field);
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a valid number.");
        }
    }

    // ép chuỗi nhập vào sang số nguyên dương int (>0, dùng cho số lượng nhập xuất)
    public static int parsePositiveInt(String raw, String field) {
        try {
            int v = Integer.parseInt(raw.trim());
            if (v <= 0) {
                throw new IllegalArgumentException(field + " must be > 0.");
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a valid integer.");
        }
    }

    // ép chuỗi nhập vào sang số nguyên int cho phép = 0 (dùng cho MinStockLevel)
    // Giống parsePositiveInt nhưng gọi requireNonNegative thay vì tự check "<= 0" ->
    // cho phép giá trị 0, chỉ chặn số âm.
    public static int parseNonNegativeInt(String raw, String field) {
        try {
            int v = Integer.parseInt(raw.trim());
            requireNonNegative(v, field);
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a valid integer.");
        }
    }

    // ép chuỗi nhập vào sang số nguyên int, kiểm tra nằm trong khoảng [min, max]
    public static int parseIntRange(String raw, String field, int min, int max) {
        try {
            int v = Integer.parseInt(raw.trim());
            requireRange(v, field, min, max);
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a valid integer.");
        }
    }

    // kiểm tra mã hàng hóa
    // Ví dụ điển hình của TÁI SỬ DỤNG: không viết lại logic check rỗng/check alphanumeric,
    // mà gọi lại 2 hàm cơ bản đã có sẵn ở trên -> code ngắn, sửa 1 chỗ là áp dụng mọi nơi.
    public static void requireGoodsCode(String code, String field) {
        requireNonBlank(code, field);
        requireAlphanumeric(code, field, 2, 10);
    }
}
