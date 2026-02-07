package Game.admin;

import Game.client.Player;
import Game.core.Manager;
import Game.core.SQL;
import Game.core.Service;
import Game.core.Util;

import java.io.IOException;
import java.sql.*;
import java.util.*;

/** DAO thao tác với bảng giftcode
 *  LƯU Ý: item4 & item7 sẽ được lưu dạng MẢNG CẶP [[id,quantity], ...]
 *  ví dụ: [[2,10000],[5,10000],[124,95]]
 */
public class GiftcodeDAO {

    public static class GiftItem {
        public int id;        // id item trong template
        public int quantity;  // số lượng
        public GiftItem(int id, int quantity) { this.id = id; this.quantity = quantity; }
    }

    public static class Giftcode {
        public Integer id;           // auto
        public String giftname;      // UNIQUE
        public List<GiftItem> items4 = new ArrayList<>();
        public List<GiftItem> items7 = new ArrayList<>();
        public Long vang;            // bigint
        public Integer ngoc;         // int
        public boolean emptyBox;     // tinyint 0/1
        public Integer limit;        // int (từ khoá -> dùng `limit` khi query)
        public String giftFor = "";  // varchar
        public Integer level = 0;    // int
        public boolean needActive;   // tinyint 0/1
    }

    /** Chuyển danh sách GiftItem -> JSON MẢNG CẶP: [[id,quantity], [id,quantity], ...] */
    private static String toPairsArray(List<GiftItem> items) {
        if (items == null || items.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < items.size(); i++) {
            GiftItem it = items.get(i);
            sb.append("[").append(it.id).append(",").append(it.quantity).append("]");
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public boolean giftnameExists(String giftname) throws SQLException {
        String sql = "SELECT 1 FROM giftcode WHERE giftname = ? LIMIT 1";
        try (Connection c = SQL.gI().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, giftname);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    /** INSERT KHÔNG còn cột item3, và item4/item7 ghi mảng cặp [[id,qty]] */
    public int create(Giftcode g) throws SQLException {
        String sql = "INSERT INTO giftcode (giftname, item4, item7, vang, ngoc, empty_box, `limit`, gift_for, level, needActive) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = SQL.gI().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            boolean prevAuto = c.getAutoCommit();
            try {
                c.setAutoCommit(false);

                ps.setString(1, g.giftname);
                ps.setString(2, toPairsArray(g.items4));  // <<<<<<<<<<
                ps.setString(3, toPairsArray(g.items7));  // <<<<<<<<<<
                if (g.vang == null) ps.setNull(4, Types.BIGINT); else ps.setLong(4, g.vang);
                if (g.ngoc == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, g.ngoc);
                ps.setBoolean(6, g.emptyBox);
                if (g.limit == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, g.limit);
                ps.setString(8, (g.giftFor == null) ? "" : g.giftFor);
                ps.setInt(9, (g.level == null) ? 0 : g.level);
                ps.setBoolean(10, g.needActive);

                int affected = ps.executeUpdate();
                if (affected == 0) { c.rollback(); throw new SQLException("Insert giftcode failed, no rows affected."); }
                int newId;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) newId = rs.getInt(1); else throw new SQLException("No generated key returned");
                }
                c.commit();
                return newId;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                try { c.setAutoCommit(prevAuto); } catch (SQLException ignore) {}
            }
        }
    }

    public List<Giftcode> list(int offset, int pageSize) throws SQLException {
        String sql = "SELECT id, giftname, vang, ngoc, empty_box, `limit`, gift_for, level, needActive " +
                     "FROM giftcode ORDER BY id DESC LIMIT ? OFFSET ?";
        List<Giftcode> out = new ArrayList<>();
        try (Connection c = SQL.gI().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Giftcode g = new Giftcode();
                    g.id = rs.getInt("id");
                    g.giftname = rs.getString("giftname");
                    long vang = rs.getLong("vang"); if (!rs.wasNull()) g.vang = vang;
                    int ngoc = rs.getInt("ngoc"); if (!rs.wasNull()) g.ngoc = ngoc;
                    g.emptyBox = rs.getBoolean("empty_box");
                    int lim = rs.getInt("limit"); if (!rs.wasNull()) g.limit = lim;
                    g.giftFor = rs.getString("gift_for");
                    g.level = rs.getInt("level");
                    g.needActive = rs.getBoolean("needActive");
                    out.add(g);
                }
            }
        }
        return out;
    }

    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM giftcode WHERE id = ?";
        try (Connection c = SQL.gI().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            boolean prevAuto = c.getAutoCommit();
            try {
                c.setAutoCommit(false);
                ps.setInt(1, id);
                int a = ps.executeUpdate();
                if (a > 0) { c.commit(); return true; } else { c.rollback(); return false; }
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                try { c.setAutoCommit(prevAuto); } catch (SQLException ignore) {}
            }
        }
    }
    public static void taoGiftcodeTop1(Player p, long vangMoiNguoi, int ngocMoiNguoi, int soLuongGioiHan, String codeNameInput) throws IOException {
        // 1. Kiểm tra quyền Top 1
        if (!p.checkIsTop1Nap()) {
            Service.send_notice_box(p.conn, "Chỉ Top 1 Nạp mới được sử dụng quyền năng này!");
            return;
        }

        // --- [LOGIC MỚI] XỬ LÝ SỐ LIỆU ĐẦU VÀO ---

        // a. Xử lý Vàng: Không âm, tối đa 1 Tỷ
        long vangFinal = vangMoiNguoi;
        if (vangFinal < 0) vangFinal = 0;
        if (vangFinal > 1_000_000_000) vangFinal = 1_000_000_000; // Max 1 tỷ

        // b. Xử lý Ngọc: Không âm
        int ngocFinal = ngocMoiNguoi;
        if (ngocFinal < 0) ngocFinal = 0;

        // c. Xử lý Số lượng (Limit): Không âm
        int limitFinal = soLuongGioiHan;
        if (limitFinal < 0) limitFinal = 0;

        // Kiểm tra tên code
        if (!codeNameInput.matches("^[a-zA-Z0-9]{3,20}$")) {
            Service.send_notice_box(p.conn, "Tên Code không hợp lệ (Chỉ chữ số, 3-20 ký tự).");
            return;
        }

        // Kiểm tra trùng tên
        try (Connection connCheck = SQL.gI().getConnection();
             PreparedStatement psCheck = connCheck.prepareStatement("SELECT * FROM `giftcode` WHERE `giftname` = ?")) {
            psCheck.setString(1, codeNameInput);
            if (psCheck.executeQuery().next()) {
                Service.send_notice_box(p.conn, "Tên Giftcode này đã tồn tại!");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 2. Tính toán chi phí (10% phí server)
        // Lưu ý: Tính trên số liệu thực tế (vangFinal) chứ không phải số liệu nhập bậy bạ
        long tongVangCan = (long) ((vangFinal * limitFinal) * 1.1);
        long tongNgocCan = (long) ((ngocFinal * limitFinal) * 1.1); // Dùng long để tránh tràn số int

        // Kiểm tra số dư
        if (p.vang < tongVangCan) {
            Service.send_notice_box(p.conn, "Không đủ Vàng. Cần: " + Util.number_format(tongVangCan));
            return;
        }
        if (p.kimcuong < tongNgocCan) {
            Service.send_notice_box(p.conn, "Không đủ Ngọc. Cần: " + Util.number_format(tongNgocCan));
            return;
        }

        // 3. Trừ tiền Top 1
        p.update_vang(-tongVangCan, "Tạo Giftcode Top 1");
        p.update_ngoc((int)-tongNgocCan);

        // 4. INSERT VÀO DATABASE (Đã sửa thêm item3 và bỏ Null)
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = SQL.gI().getConnection();
            // THÊM `item3` VÀO CÂU LỆNH SQL
            String sql = "INSERT INTO giftcode (giftname, item3, item4, item7, vang, ngoc, empty_box, `limit`, gift_for, level, needActive) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            ps = c.prepareStatement(sql);

            ps.setString(1, codeNameInput);
            ps.setString(2, "[]"); // item3: Fix lỗi Null -> Mặc định là mảng rỗng
            ps.setString(3, "[]"); // item4
            ps.setString(4, "[]"); // item7

            // Vàng & Ngọc: Luôn set số, không bao giờ setNull
            ps.setLong(5, vangFinal);
            ps.setInt(6, ngocFinal);

            ps.setBoolean(7, false); // empty_box
            ps.setInt(8, limitFinal); // limit
            ps.setString(9, "");     // gift_for
            ps.setInt(10, 0);        // level
            ps.setBoolean(11, false);// needActive

            if (ps.executeUpdate() > 0) {
                c.commit();

                // Thông báo thành công
                Service.send_notice_box(p.conn, "Tạo thành công!\nMã: " + codeNameInput);

                String msgKTG = "[💲ĐẠI GIA] " + p.name.toUpperCase() + " phát Giftcode!\n"
                        + "🎁 Code: " + codeNameInput + "\n"
                        + "💎 Quà: " + (vangFinal > 0 ? Util.number_format(vangFinal) + " Vàng " : "")
                        + (ngocFinal > 0 ? Util.number_format(ngocFinal) + " Ngọc" : "") + "\n"
                        + "⚡ Số lượng giftcode: " + limitFinal;

                Manager.gI().chatKTGprocess(msgKTG);
            } else {
                // Hoàn tiền nếu lỗi
                p.update_vang(tongVangCan, "Hoàn tiền lỗi code");
                p.update_ngoc((int)tongNgocCan);
                Service.send_notice_box(p.conn, "Lỗi Database, đã hoàn tiền.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            p.update_vang(tongVangCan, "Hoàn tiền lỗi code");
            p.update_ngoc((int)tongNgocCan);
            Service.send_notice_box(p.conn, "Lỗi: " + e.getMessage());
        } finally {
            try { if (ps != null) ps.close(); if (c != null) c.close(); } catch (SQLException e) {}
        }
    }
}
