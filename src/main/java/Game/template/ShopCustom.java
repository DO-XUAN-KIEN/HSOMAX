package Game.template;

import Game.client.Player;
import Game.core.Admin;
import Game.core.SQL;
import Game.core.Service;
import Game.core.Util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class ShopCustom {

    public static final HashMap<Integer, Long> items_gems = new HashMap<>();

    public static final HashMap<Integer, Integer> items_days = new HashMap<>();

    public static void loadShopGems() {
        items_gems.clear();
        items_days.clear(); // Reset map date
        try (Connection connection = SQL.gI().getConnection();
             Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM `shop_custom`;")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                long price = rs.getLong("price");

                // Đọc cột date (xử lý trường hợp chưa có cột date thì mặc định là 0)
                int date = 0;
                try {
                    date = rs.getInt("date");
                } catch (SQLException e) {
                    date = 0;
                }

                // Lưu vào 2 map riêng biệt
                items_gems.put(id, price); // Map cũ lưu giá
                items_days.put(id, date);  // Map mới lưu ngày
            }
        } catch (SQLException e) {
            System.err.println("Lỗi load ShopCustom: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void buy(Player p, int idbuy) throws IOException {
        if (!items_gems.containsKey(idbuy)) {
            Service.send_notice_box(p.conn, "Vật phẩm không tồn tại");
            return;
        }

        // --- [SỬA ĐOẠN NÀY] ---
        // 1. Lấy giá gốc từ Map
        long originalPrice = items_gems.get(idbuy);

        // 2. Tính giá thực tế sau khi đã giảm (theo sự kiện Top 1)
        long price = Service.calcPrice(originalPrice);
        // -----------------------

        int date = items_days.getOrDefault(idbuy, 0);

        if (p.get_ngoc() < price) {
            // Cập nhật thông báo cho rõ ràng
            Service.send_notice_box(p.conn, "Không đủ ngọc. Cần: " + Util.number_format(price));
            return;
        }

        // Trừ tiền theo giá đã giảm
        p.update_ngoc(-price);

        // --- (ĐOẠN DƯỚI GIỮ NGUYÊN KHÔNG ĐỔI) ---
        if (ItemTemplate3.item.get(idbuy).getType() == 16) {
            Admin.randomMedal(p, (byte) 4, (byte) 0, true);
        } else {
            Item3 itbag = new Item3();
            itbag.id = (short) idbuy;
            itbag.clazz = ItemTemplate3.item.get(idbuy).getClazz();
            itbag.type = ItemTemplate3.item.get(idbuy).getType();
            itbag.level = ItemTemplate3.item.get(idbuy).getLevel();
            itbag.icon = ItemTemplate3.item.get(idbuy).getIcon();
            itbag.color = 5;
            itbag.part = ItemTemplate3.item.get(idbuy).getPart();
            itbag.islock = true;
            itbag.name = ItemTemplate3.item.get(idbuy).getName();
            itbag.tier = 0;

            itbag.op = new ArrayList<>();
            for (Option o : ItemTemplate3.item.get(idbuy).getOp()) {
                itbag.op.add(new Option(o.id, o.param));
            }

            itbag.time_use = 0;

            if (date > 0) {
                itbag.expiry_date = System.currentTimeMillis() + (long) date * 60 * 60 * 1000;
            } else {
                itbag.expiry_date = 0;
            }

            p.item.add_item_inventory3(itbag);
        }
        Service.send_notice_box(p.conn, "Đã mua thành công");
    }
}