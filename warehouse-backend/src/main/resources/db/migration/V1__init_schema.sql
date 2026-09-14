-- Flyway Database Migration V1: Initial Schema
-- Target DB: SQL Server 2019+

-- 1. Bảng Users
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users')
BEGIN
    CREATE TABLE users (
        id            BIGINT IDENTITY(1,1) PRIMARY KEY,
        username      VARCHAR(50)  NOT NULL UNIQUE,
        password_hash VARCHAR(100) NOT NULL,
        salt          VARCHAR(50)  NOT NULL,
        email         VARCHAR(100) NULL,
        role          VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
        status        BIT          NOT NULL DEFAULT 1,
        created_at    DATETIME2    NOT NULL DEFAULT SYSDATETIME()
    );
END;

-- 2. Bảng Goods (Lưu cả RawMaterial và FinishedProduct)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'goods')
BEGIN
    CREATE TABLE goods (
        goods_code       VARCHAR(20)  NOT NULL PRIMARY KEY,
        goods_name       NVARCHAR(100) NOT NULL,
        unit             NVARCHAR(20)  NOT NULL,
        quantity         INT          NOT NULL DEFAULT 0 CHECK (quantity >= 0),
        mins_stock_level INT          NOT NULL DEFAULT 5 CHECK (mins_stock_level >= 0),
        goods_type       CHAR(1)      NOT NULL CHECK (goods_type IN ('R', 'F')),
        supplier         NVARCHAR(100) NULL,
        sell_price       FLOAT        NULL,
        created_at       DATETIME2    NOT NULL DEFAULT SYSDATETIME(),
        updated_at       DATETIME2    NOT NULL DEFAULT SYSDATETIME()
    );
END;

-- 3. Tạo các Chỉ mục (Indexes) tối ưu hóa truy vấn tìm kiếm tốc độ cao
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_goods_type')
    CREATE INDEX idx_goods_type ON goods(goods_type);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_goods_name')
    CREATE INDEX idx_goods_name ON goods(goods_name);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_goods_supplier')
    CREATE INDEX idx_goods_supplier ON goods(supplier) WHERE supplier IS NOT NULL;

-- 4. Chèn dữ liệu mẫu mặc định
INSERT INTO users(username, password_hash, salt, email, role, status)
VALUES
    ('admin', 'Xz4Mtocu39jx2t6grpt2RfjNLdqRdP1wd6GgrC4E2k4=', 'd2FyZWhvdXNlLWFkbWluLXNhbHQ=', 'admin@warehouse.com', 'ROLE_ADMIN', 1),
    ('user1', 'rPSgD+R1bTfGCVytzXutoqy38lq6GNRy/W3PI/Lcqw4=', 'd2FyZWhvdXNlLXVzZXItc2FsdC0=', 'user1@warehouse.com', 'ROLE_USER', 1);

INSERT INTO goods(goods_code, goods_name, unit, quantity, mins_stock_level, goods_type, supplier, sell_price)
VALUES
    ('RM001', N'Robusta Coffee Beans', N'Kg', 50, 15, 'R', N'Trung Nguyen Coffee Co.', NULL),
    ('RM002', N'Arabica Coffee Beans', N'Kg', 30, 15, 'R', N'Trung Nguyen Coffee Co.', NULL),
    ('RM003', N'Condensed Milk', N'Can', 40, 20, 'R', N'Vinamilk Co.', NULL),
    ('FP001', N'Roasted Ground Coffee 500g', N'Pack', 20, 8, 'F', NULL, 95000),
    ('FP002', N'Instant Coffee 3in1', N'Box', 12, 5, 'F', NULL, 65000);