package com.warehousemanager.repositories;

import com.warehousemanager.config.DatabaseConfig;
import com.warehousemanager.models.entity.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO for the Goods table — every SQL statement for reading/writing goods
// lives here. Each method opens its own Connection (via DatabaseConfig) and
// closes it via try-with-resources, so calls from different background
// threads never share the same Connection.
public class GoodsRepository {

    // Insert a new goods row. RawMaterial fills Supplier and leaves SellPrice
    // NULL; FinishedProduct does the opposite.
    public boolean insert(Goods g) {
        String sql = "INSERT INTO Goods(GoodsCode, GoodsName, Unit, Quantity, MinsStockLevel, GoodsType, Supplier, SellPrice)"
                + "VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, g.getCode());
            ps.setString(2, g.getName());
            ps.setString(3, g.getUnit());
            ps.setInt(4, g.getQuantity());
            ps.setInt(5, g.getMinStockLevel());
            ps.setString(6, g.getTypeCode());
            if (g instanceof RawMaterial rm) {
                ps.setString(7, rm.getSupplier());
                ps.setNull(8, Types.FLOAT);
            } else {
                FinishedProduct fp = (FinishedProduct) g;
                ps.setNull(7, Types.VARCHAR);
                ps.setDouble(8, fp.getSellPrice());
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Return every goods row, ordered by name, mapped back into
    // RawMaterial/FinishedProduct.
    public List<Goods> findAll() {
        List<Goods> list = new ArrayList<>();
        String sql = "SELECT * FROM Goods ORDER BY GoodsName";
        try (Connection conn = DatabaseConfig.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Look up a single item by its code; returns null if not found.
    public Goods findByCode(String code) {
        String sql = "SELECT * FROM Goods WHERE GoodsCode = ?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Return all goods rows for one discriminator type (for example R or F),
    // still ordered by GoodsName for stable table rendering.
    public List<Goods> findByType(String type) {
        List<Goods> list = new ArrayList<>();
        String sql = "SELECT * FROM Goods where GoodsType = ? ORDER BY GoodsName";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Update only the current stock quantity for one goods code without
    // touching the rest of the row.
    public boolean updateQuantity(String code, int newQuantity) {
        String sql = " UPDATE Goods SET Quantity = ? WHERE GoodsCode = ?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setString(2, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Persist editable fields for an existing goods row; the subclass decides
    // whether Supplier or SellPrice is written.
    public boolean update(Goods g) {
        String sql = "UPDATE Goods SET GoodsName=?, Unit=?, Quantity=?, MinsStockLevel=?, Supplier=?, SellPrice =? Where GoodsCode=?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, g.getName());
            ps.setString(2, g.getUnit());
            ps.setInt(3, g.getQuantity());
            ps.setInt(4, g.getMinStockLevel());
            if (g instanceof RawMaterial rm) {
                ps.setString(5, rm.getSupplier());
                ps.setNull(6, Types.FLOAT);
            } else {
                FinishedProduct fn = (FinishedProduct) g;
                ps.setNull(5, Types.VARCHAR);
                ps.setDouble(6, fn.getSellPrice());
            }
            ps.setString(7, g.getCode());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete one goods row by its primary code; returns false when no row is
    // removed or the database call fails.
    public boolean delete(String code) {
        String sql = "DELETE FROM Goods WHERE GoodsCode=?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Sum the stock value reported by each mapped Goods instance to produce a
    // repository-level inventory total.
    public double calcTotalStockValue() {
        double total = 0;
        for (Goods g : findAll())
            total += g.calcStockValue();
        return total;
    }

    // Read the GoodsType discriminator to rebuild the correct subclass
    // (RawMaterial vs FinishedProduct) from one result row.
    private Goods mapRow(ResultSet rs) throws SQLException {
        String type = rs.getString("GoodsType");
        Goods g;
        if ("R".equals(type)) {
            RawMaterial rm = new RawMaterial();
            rm.setSupplier(rs.getString("Supplier"));
            g = rm;
        } else {
            FinishedProduct fp = new FinishedProduct();
            fp.setSellPrice(rs.getDouble("SellPrice"));
            g = fp;
        }
        g.setCode(rs.getString("GoodsCode"));
        g.setName(rs.getString("GoodsName"));
        g.setUnit(rs.getString("Unit"));
        g.setQuantity(rs.getInt("Quantity"));
        g.setMinStockLevel(rs.getInt("MinsStockLevel"));
        return g;
    }

}
