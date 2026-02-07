// ItemsLookupDAO.java
package Game.admin;

import Game.core.SQL;
import java.sql.*;
import java.util.*;

public class ItemsLookupDAO {

    // Class chứa đầy đủ thông tin item3
    public static class ItemRowFull {
        public final int id;
        public final String name;
        public final int type, part, clazz, iconid, level, color;
        public final String data;

        public ItemRowFull(int id, String name, int type, int part, int clazz,
                           int iconid, int level, String data, int color) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.part = part;
            this.clazz = clazz;
            this.iconid = iconid;
            this.level = level;
            this.data = data;
            this.color = color;
        }
    }

    // Class đơn giản cho item4/item7
    public static class ItemRow {
        public final int id;
        public final String name;
        public ItemRow(int id, String name) { this.id = id; this.name = name; }
    }

    // Truy vấn item3 full 9 cột
    private static List<ItemRowFull> queryItem3Full(Connection c, String keyword, int limit) throws SQLException {
        boolean isNumeric = keyword != null && keyword.matches("\\d+");
        String sql = isNumeric ?
                "SELECT * FROM item3 WHERE id = ? ORDER BY id ASC LIMIT ?" :
                "SELECT * FROM item3 WHERE name LIKE ? ORDER BY id ASC LIMIT ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (isNumeric) ps.setInt(1, Integer.parseInt(keyword));
            else ps.setString(1, (keyword == null || keyword.isBlank()) ? "%" : "%" + keyword.trim() + "%");
            ps.setInt(2, limit);

            List<ItemRowFull> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ItemRowFull(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("type"),
                            rs.getInt("part"),
                            rs.getInt("clazz"),
                            rs.getInt("iconid"),
                            rs.getInt("level"),
                            rs.getString("data"),
                            rs.getInt("color")
                    ));
                }
            }
            return list;
        }
    }

    // Truy vấn item4/item7 chỉ 2 cột
    private static List<ItemRow> queryItemsSimple(Connection c, String table, String keyword, int limit) throws SQLException {
        boolean isNumeric = keyword != null && keyword.matches("\\d+");
        String sql = isNumeric ?
                "SELECT id,name FROM " + table + " WHERE id = ? ORDER BY id ASC LIMIT ?" :
                "SELECT id,name FROM " + table + " WHERE name LIKE ? ORDER BY id ASC LIMIT ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (isNumeric) ps.setInt(1, Integer.parseInt(keyword));
            else ps.setString(1, (keyword == null || keyword.isBlank() ? "%" : "%" + keyword.trim() + "%"));
            ps.setInt(2, limit);

            List<ItemRow> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new ItemRow(rs.getInt("id"), rs.getString("name")));
            }
            return list;
        }
    }

    // Public gọi từ panel
    public List<ItemRowFull> listItem3(String keyword, int limit) throws SQLException {
        try (Connection c = SQL.gI().getConnection()) {
            return queryItem3Full(c, keyword, limit);
        }
    }

    public List<ItemRow> listItem4(String keyword, int limit) throws SQLException {
        try (Connection c = SQL.gI().getConnection()) {
            return queryItemsSimple(c, "item4", keyword, limit);
        }
    }

    public List<ItemRow> listItem7(String keyword, int limit) throws SQLException {
        try (Connection c = SQL.gI().getConnection()) {
            return queryItemsSimple(c, "item7", keyword, limit);
        }
    }
}
