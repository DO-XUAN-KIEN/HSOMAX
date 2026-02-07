package Game.core;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Game.io.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import Game.client.Clan;
import Game.client.Player;
import Game.io.Message;
import Game.map.Map;
import Game.template.Item3;
import Game.template.Part_player;
import Game.template.Level;

public class Rank {

    public static final List<PlayerInfo> top_activity = new ArrayList<>();
    public static final List<PlayerInfo> top_arena = new ArrayList<>();
    public static final List<PlayerInfo> cay = new ArrayList<>();
    public static final List<ClanInfo> top_level_clan = new ArrayList<>();
    public static final List<ClanInfo> top_gold_clan = new ArrayList<>();
    public static final List<ClanInfo> top_gems_clan = new ArrayList<>();
    public static final List<PlayerInfo> top_z6 = new ArrayList<>();
    public static final List<PlayerInfo> top_nap = new ArrayList<>();

    public static void send(Session conn, int b) {
        switch (b) {
            case 0: {
                Rank.sendTopPlayer(conn, top_activity, "Top Danh Vọng");
                break;
            }
            case 1: {
                Rank.sendTopClan(conn, top_level_clan, "Bang hùng mạnh nhất");
                break;
            }
            case 2: {
                Rank.sendTopClan(conn, top_gold_clan, "Bang giàu có nhất");
                break;
            }
            case 3: {
                Rank.sendTopClan(conn, top_gems_clan, "Bang nhiều châu báu nhất");
                break;
            }
            case 4: {
                Rank.sendTopPlayer(conn, top_arena, "Top Chiến Trường");
                break;
            }
            case 5: {
                Rank.sendTopPlayer(conn, top_z6, "Top Thương Nhân");
                break;
            }
            case 6: {
                Rank.sendTopPlayer(conn, cay, "Top cao thủ");
                break;
            }
            case 7: {
                Rank.sendTopPlayer(conn, top_nap, "Top Nạp");
            }
        }
    }

    public static void init(Connection conn) {
        try {
            Rank.top_activity.clear();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT `id`, `level`, `exp`, `name`, `body`, `itemwear`, `point_activity` FROM `player` WHERE `point_activity` >= 0 ORDER BY `point_activity` DESC LIMIT 20;");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                PlayerInfo temp = new PlayerInfo();
                temp.level = rs.getShort("level");
                temp.exp = rs.getLong("exp");
                temp.name = rs.getString("name");
                temp.point_activity = rs.getLong("point_activity");
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("body"));
                if (jsar == null) {
                    continue;
                }
                temp.head = Byte.parseByte(jsar.get(0).toString());
                temp.hair = Byte.parseByte(jsar.get(2).toString());
                temp.eye = Byte.parseByte(jsar.get(1).toString());
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("itemwear"));
                if (jsar == null) {
                    continue;
                }
                temp.item_wear = new ArrayList<>();

                for (Object o : jsar) {

                    JSONArray jsar2 = (JSONArray) JSONValue.parse(o.toString());

                    // ⚠️ BẮT BUỘC: kiểm tra dữ liệu
                    if (jsar2 == null || jsar2.size() <= 9) {
                        continue; // bỏ item lỗi / item cũ
                    }

                    byte index_wear;
                    try {
                        index_wear = Byte.parseByte(jsar2.get(9).toString());
                    } catch (Exception e) {
                        continue;
                    }

                    if (index_wear != 0 && index_wear != 1
                            && index_wear != 6 && index_wear != 7
                            && index_wear != 10) {
                        continue;
                    }

                    // Kiểm tra thêm các field cần dùng
                    if (jsar2.size() <= 6) {
                        continue;
                    }

                    Part_player temp2 = new Part_player();

                    temp2.type = Byte.parseByte(jsar2.get(2).toString());
                    temp2.part = Byte.parseByte(jsar2.get(6).toString());

                    temp.item_wear.add(temp2);
                }

                temp.clan = Clan.get_clan_of_player(temp.name);
                temp.info = "Điểm danh vọng: " + Util.number_format(temp.point_activity);
                Rank.top_activity.add(temp);
            }
            rs.close();
            Rank.cay.clear();
            rs = ps.executeQuery(
                    "SELECT `id`, `level`, `exp`, `name`, `body`, `itemwear` FROM `player` WHERE `level` > 10 ORDER BY `level` DESC, exp DESC LIMIT 20;");
            while (rs.next()) {
                PlayerInfo temp = new PlayerInfo();
                temp.level = rs.getShort("level");
                temp.exp = rs.getLong("exp");
                temp.name = rs.getString("name");
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("body"));
                if (jsar == null) {
                    continue;
                }
                temp.head = Byte.parseByte(jsar.get(0).toString());
                temp.hair = Byte.parseByte(jsar.get(2).toString());
                temp.eye = Byte.parseByte(jsar.get(1).toString());
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("itemwear"));
                if (jsar == null) {
                    continue;
                }
                temp.item_wear = new ArrayList<>();

                for (Object o : jsar) {

                    JSONArray jsar2;
                    try {
                        jsar2 = (JSONArray) JSONValue.parse(o.toString());
                    } catch (Exception e) {
                        continue;
                    }

                    // ⛔ BẮT BUỘC: chặn item rỗng / item lỗi
                    if (jsar2 == null || jsar2.size() <= 9) {
                        continue;
                    }

                    byte index_wear;
                    try {
                        index_wear = Byte.parseByte(jsar2.get(9).toString());
                    } catch (Exception e) {
                        continue;
                    }

                    if (index_wear != 0 && index_wear != 1
                            && index_wear != 6 && index_wear != 7
                            && index_wear != 10) {
                        continue;
                    }

                    // Kiểm tra tiếp index 2 & 6
                    if (jsar2.size() <= 6) {
                        continue;
                    }

                    Part_player temp2 = new Part_player();
                    temp2.type = Byte.parseByte(jsar2.get(2).toString());
                    temp2.part = Byte.parseByte(jsar2.get(6).toString());

                    temp.item_wear.add(temp2);
                }

                temp.clan = Clan.get_clan_of_player(temp.name);
                String percent
                        = String.format("%.1f", (((float) temp.exp * 1000) / Level.entry.get(temp.level - 1).exp) / 10f);
                temp.info = "Level : " + (temp.level) + "\t-\t" + percent + "%";
                Rank.cay.add(temp);
            }
            rs.close();
            Rank.top_level_clan.clear();
            rs = ps.executeQuery("SELECT `id`, `name`, `icon`, `name_short` FROM `clan` WHERE `level` >= 0 ORDER BY `level` DESC LIMIT 20;");
            while (rs.next()) {
                ClanInfo temp = new ClanInfo();
                temp.idClan = rs.getShort("id");
                temp.name = rs.getString("name");
                temp.shortName = rs.getString("name_short");
                temp.idIcon = rs.getShort("icon");
                Clan clan = Clan.get_clan_by_name(temp.name);
                if (clan != null) {
                    temp.info = "Cấp độ: " + clan.level + "+" + clan.get_percent_level() / 10 + "% - " + clan.mems.size() + "/" + clan.max_mem + " thành viên";
                }
                Rank.top_level_clan.add(temp);
            }
            rs.close();

            Rank.top_gold_clan.clear();
            rs = ps.executeQuery("SELECT `id`, `name`, `icon`, `name_short` FROM `clan` WHERE `level` >= 0 ORDER BY `vang` DESC LIMIT 20;");
            while (rs.next()) {
                ClanInfo temp = new ClanInfo();
                temp.idClan = rs.getShort("id");
                temp.name = rs.getString("name");
                temp.shortName = rs.getString("name_short");
                temp.idIcon = rs.getShort("icon");
                Clan clan = Clan.get_clan_by_name(temp.name);
                if (clan != null) {
                    temp.info = "Vàng: " + Util.number_format(clan.get_vang()) + " - " + clan.mems.size() + "/" + clan.max_mem + " thành viên";
                }
                Rank.top_gold_clan.add(temp);
            }
            rs.close();

            Rank.top_gems_clan.clear();
            rs = ps.executeQuery("SELECT `id`, `name`, `icon`, `name_short` FROM `clan` WHERE `level` >= 0 ORDER BY `kimcuong` DESC LIMIT 20;");
            while (rs.next()) {
                ClanInfo temp = new ClanInfo();
                temp.idClan = rs.getShort("id");
                temp.name = rs.getString("name");
                temp.shortName = rs.getString("name_short");
                temp.idIcon = rs.getShort("icon");
                Clan clan = Clan.get_clan_by_name(temp.name);
                if (clan != null) {
                    temp.info = "Ngọc: " + Util.number_format(clan.get_ngoc()) + " - " + clan.mems.size() + "/" + clan.max_mem + " thành viên";
                }
                Rank.top_gems_clan.add(temp);
            }
            rs.close();

            Rank.top_arena.clear();
            rs = ps.executeQuery("SELECT `id`, `level`, `exp`, `name`, `body`, `itemwear`, `point_arena` FROM `player` WHERE `point_arena` >= 0 ORDER BY `point_arena` DESC LIMIT 20;");
            while (rs.next()) {
                PlayerInfo temp = new PlayerInfo();
                temp.level = rs.getShort("level");
                temp.exp = rs.getLong("exp");
                temp.name = rs.getString("name");
                temp.point_arena = rs.getLong("point_arena");
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("body"));
                if (jsar == null) {
                    continue;
                }
                temp.head = Byte.parseByte(jsar.get(0).toString());
                temp.hair = Byte.parseByte(jsar.get(2).toString());
                temp.eye = Byte.parseByte(jsar.get(1).toString());
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("itemwear"));
                if (jsar == null) {
                    continue;
                }
                temp.item_wear = new ArrayList<>();

                for (Object o : jsar) {

                    JSONArray jsar2;
                    try {
                        jsar2 = (JSONArray) JSONValue.parse(o.toString());
                    } catch (Exception e) {
                        continue;
                    }

                    // ⛔ CHẶN 100% ITEM LỖI / ITEM RỖNG
                    if (jsar2 == null || jsar2.size() < 10) {
                        continue;
                    }

                    byte index_wear;
                    try {
                        index_wear = Byte.parseByte(jsar2.get(9).toString());
                    } catch (Exception e) {
                        continue;
                    }

                    if (index_wear != 0 && index_wear != 1
                            && index_wear != 6 && index_wear != 7
                            && index_wear != 10) {
                        continue;
                    }

                    // đảm bảo index 2 & 6 tồn tại
                    if (jsar2.size() <= 6) {
                        continue;
                    }

                    Part_player temp2 = new Part_player();
                    temp2.type = Byte.parseByte(jsar2.get(2).toString());
                    temp2.part = Byte.parseByte(jsar2.get(6).toString());

                    temp.item_wear.add(temp2);
                }

                temp.clan = Clan.get_clan_of_player(temp.name);
                temp.info = "Điểm: " + Util.number_format(temp.point_arena / 10);
                Rank.top_arena.add(temp);
            }
            rs.close();

            Rank.top_z6.clear();
            rs = ps.executeQuery("SELECT `id`, `level`, `exp`, `name`, `body`, `itemwear`, `point_z6` FROM `player` WHERE `point_z6` >= 0 ORDER BY `point_z6` DESC LIMIT 20;");
            while (rs.next()) {
                PlayerInfo temp = new PlayerInfo();
                temp.level = rs.getShort("level");
                temp.exp = rs.getLong("exp");
                temp.name = rs.getString("name");
                temp.point_z6 = rs.getLong("point_z6");
                JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("body"));
                if (jsar == null) {
                    continue;
                }
                temp.head = Byte.parseByte(jsar.get(0).toString());
                temp.hair = Byte.parseByte(jsar.get(2).toString());
                temp.eye = Byte.parseByte(jsar.get(1).toString());
                jsar.clear();
                jsar = (JSONArray) JSONValue.parse(rs.getString("itemwear"));
                if (jsar == null) {
                    continue;
                }
                temp.item_wear = new ArrayList<>();

                for (Object o : jsar) {

                    JSONArray jsar2;
                    try {
                        jsar2 = (JSONArray) JSONValue.parse(o.toString());
                    } catch (Exception e) {
                        continue;
                    }

                    // ⛔ CHẶN ITEM RỖNG / ITEM CŨ
                    if (jsar2 == null || jsar2.size() < 10) {
                        continue;
                    }

                    byte index_wear;
                    try {
                        index_wear = Byte.parseByte(jsar2.get(9).toString());
                    } catch (Exception e) {
                        continue;
                    }

                    if (index_wear != 0 && index_wear != 1
                            && index_wear != 6 && index_wear != 7
                            && index_wear != 10) {
                        continue;
                    }

                    // đảm bảo index cần dùng tồn tại
                    if (jsar2.size() <= 6) {
                        continue;
                    }

                    Part_player temp2 = new Part_player();
                    temp2.type = Byte.parseByte(jsar2.get(2).toString());
                    temp2.part = Byte.parseByte(jsar2.get(6).toString());

                    temp.item_wear.add(temp2);
                }

                temp.clan = Clan.get_clan_of_player(temp.name);
                temp.info = "Điểm: " + Util.number_format(temp.point_z6);
                Rank.top_z6.add(temp);
            }
            Rank.top_nap.clear();

            // ====== CODE RANK MỚI (FIX LỖI UNKNOWN COLUMN) ======

            // Bước 1: Lấy Top Nạp từ bảng ACCOUNT trước (vì tiền nằm ở đây)
            String sqlAccount = "SELECT `char`, `tongnap` FROM `account` WHERE `tongnap` > 0 ORDER BY `tongnap` DESC LIMIT 100";

            try (PreparedStatement psAcc = conn.prepareStatement(sqlAccount);
                 ResultSet rsAcc = psAcc.executeQuery()) {

                // Chuẩn bị câu lệnh lấy thông tin nhân vật (sẽ dùng lại nhiều lần trong vòng lặp)
                String sqlPlayer = "SELECT id, name, level, exp, body, itemwear FROM player WHERE name = ?";

                try (PreparedStatement psPlayer = conn.prepareStatement(sqlPlayer)) {

                    while (rsAcc.next()) {
                        long tongNap = rsAcc.getLong("tongnap");
                        String charJson = rsAcc.getString("char");

                        // Phân tích JSON cột 'char' để lấy tên nhân vật chính
                        // Cấu trúc thường là ["TenNhanVat"] hoặc ["TenNV1", "TenNV2"]
                        JSONArray ja = null;
                        try {
                            ja = (JSONArray) JSONValue.parse(charJson);
                        } catch (Exception e) {}

                        if (ja == null || ja.isEmpty()) {
                            continue; // Account nạp tiền nhưng chưa tạo nhân vật hoặc lỗi data
                        }

                        // Lấy tên nhân vật đầu tiên trong danh sách (Nhân vật chính)
                        String mainCharName = ja.get(0).toString();

                        // Bước 2: Query lấy thông tin hiển thị từ bảng PLAYER
                        psPlayer.setString(1, mainCharName);

                        try (ResultSet rsP = psPlayer.executeQuery()) {
                            if (rsP.next()) {
                                PlayerInfo temp = new PlayerInfo();

                                // --- GÁN DỮ LIỆU ---
                                // Tiền lấy từ bảng Account
                                temp.tongnap = tongNap;

                                // Thông tin hiển thị lấy từ bảng Player
                                temp.id = rsP.getShort("id");
                                temp.name = rsP.getString("name");
                                temp.level = rsP.getShort("level");
                                temp.exp = rsP.getLong("exp");

                                // --- XỬ LÝ BODY & ITEM (Giữ nguyên logic cũ) ---
                                JSONArray jsar = (JSONArray) JSONValue.parse(rsP.getString("body"));
                                if (jsar != null) {
                                    temp.head = Byte.parseByte(jsar.get(0).toString());
                                    temp.hair = Byte.parseByte(jsar.get(2).toString());
                                    temp.eye = Byte.parseByte(jsar.get(1).toString());
                                }

                                jsar = (JSONArray) JSONValue.parse(rsP.getString("itemwear"));
                                temp.item_wear = new ArrayList<>();
                                if (jsar != null) {
                                    for (Object o : jsar) {
                                        JSONArray jsar2;
                                        try { jsar2 = (JSONArray) JSONValue.parse(o.toString()); } catch (Exception e) { continue; }
                                        if (jsar2 == null || jsar2.size() < 10) continue;

                                        byte index_wear;
                                        try { index_wear = Byte.parseByte(jsar2.get(9).toString()); } catch (Exception e) { continue; }

                                        if (index_wear != 0 && index_wear != 1 && index_wear != 6 && index_wear != 7 && index_wear != 10) continue;
                                        if (jsar2.size() <= 6) continue;

                                        Part_player temp2 = new Part_player();
                                        temp2.type = Byte.parseByte(jsar2.get(2).toString());
                                        temp2.part = Byte.parseByte(jsar2.get(6).toString());
                                        temp.item_wear.add(temp2);
                                    }
                                }

                                temp.clan = Clan.get_clan_of_player(temp.name);
                                temp.info = "Tổng Nạp: " + Util.number_format(temp.tongnap);

                                // Thêm vào BXH
                                Rank.top_nap.add(temp);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            rs.close();
            ps.close();
//            System.out.println("Load Rank Completed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void chatKTGLogin(Player p) throws IOException {
//        if (!top_activity.isEmpty()) {
//            if (p.name.equals(top_activity.get(0).name)) {
//                Manager.gI().chatKTGprocess("Óc chó  top 1 danh vọng  " + p.name.toUpperCase() + " đã  vào game");
//            } //else if (top_activity.size() > 1 && p.name.equals(top_activity.get(1).name)) {
//            //  Manager.gI().chatKTGprocess("Óc chó  top 2 danh vọng  " + p.name.toUpperCase() + " đã  vào game");
//            // } else if (top_activity.size() > 2 && p.name.equals(top_activity.get(2).name)) {
//            //    Manager.gI().chatKTGprocess("Óc chó top 3 danh vọng " + p.name.toUpperCase() + " đã vào game");
//            // }
//        }

        // if (!cay.isEmpty()) {
        //   if (p.name.equals(cay.get(0).name)) {
        //     Manager.gI().chatKTGprocess("Chào mừng TOP 1 cấp độ " + p.name.toUpperCase() + " đăng nhập vào game");
        //    } else if (cay.size() > 1 && p.name.equals(cay.get(1).name)) {
        //        Manager.gI().chatKTGprocess("Chào mừng TOP 2 cấp độ " + p.name.toUpperCase() + " đăng nhập vào game");
        //    } else if (cay.size() > 2 && p.name.equals(cay.get(2).name)) {
        //        Manager.gI().chatKTGprocess("Chào mừng TOP 3 cấp độ " + p.name.toUpperCase() + " đăng nhập vào game");
        //   }
        // }

        // if (!top_arena.isEmpty()) {
        //   if (p.name.equals(top_arena.get(0).name)) {
        //       Manager.gI().chatKTGprocess("Chào mừng TOP 1 chiến trường " + p.name.toUpperCase() + " đăng nhập vào game");
        //   } else if (top_arena.size() > 1 && p.name.equals(top_arena.get(1).name)) {
        //       Manager.gI().chatKTGprocess("Chào mừng TOP 2 chiến trường " + p.name.toUpperCase() + " đăng nhập vào game");
        //    } else if (top_arena.size() > 2 && p.name.equals(top_arena.get(2).name)) {
        //        Manager.gI().chatKTGprocess("Chào mừng TOP 3 chiến trường " + p.name.toUpperCase() + " đăng nhập vào game");
        //   }
        // }

        //if (!top_z6.isEmpty()) {
        //    if (p.name.equals(top_z6.get(0).name)) {
        //       Manager.gI().chatKTGprocess("Chào mừng TOP 1 khu 6 " + p.name.toUpperCase() + " đăng nhập vào game");
        //   } else if (top_z6.size() > 1 && p.name.equals(top_z6.get(1).name)) {
        //      Manager.gI().chatKTGprocess("Chào mừng TOP 2 khu 6 " + p.name.toUpperCase() + " đăng nhập vào game");
        //   } else if (top_z6.size() > 2 && p.name.equals(top_z6.get(2).name)) {
        //       Manager.gI().chatKTGprocess("Chào mừng TOP 3 khu 6 " + p.name.toUpperCase() + " đăng nhập vào game");
        //  }
        // }
//        if (p.name.equalsIgnoreCase("có em bên đời")) {
//            Manager.gI().chatKTGprocess("Thượng tiên  " + p.name + " đã đăng nhập vào game với ae mình nè ahihihi đồ troá !");
//        }
//        if (p.name.equalsIgnoreCase("đồ sát")) {
//            Manager.gI().chatKTGprocess(" Con chó   " + p.name + " on game anh em cẩn thận !!!!!!!!!");
//        }
//        if (p.name.equalsIgnoreCase("hoang thiên đế")) {  // Tao là yanyan zai đẹp nhất sever đã online
//            Manager.gI().chatKTGprocess(" Bá chủ vạn giới " + p.name + " đăng nhập vào game <3");
//        }
//
//        if (p.name.equalsIgnoreCase("yanyan")) {  // Tao là yanyan zai đẹp nhất sever đã online
//            Manager.gI().chatKTGprocess(" Tao là " + p.name + " zai đẹp nhất sever đã online <3 <3 <3");
//        }
//        if (p.name.equalsIgnoreCase("tiêu viêm")) {  // Tao là yanyan zai đẹp nhất sever đã online Tiêu Viêm Đại Đế đã giáng lâm xuống Chu Tước Quốc
//            Manager.gI().chatKTGprocess(" " + p.name + " Đại Đế đã giáng lâm xuống Chu Tước Quốc tất cả mau ra hành lễ ");
//        }
//        if (p.myclan != null && p.myclan.name_clan.equalsIgnoreCase("Mãi là anh em")) {
//            Manager.gI().chatKTGprocess("💥 Thành viên bang " + p.myclan.name_clan + " " + p.name + " đã đăng nhập. Chào mừng  chiến thần của chúng ta đã trở lại!!!!!!");
//        }
    }


    public static void sendTopClan(Session conn, List<ClanInfo> list, String rank_name) {
        try {
            Message m = new Message(56);
            m.writer().writeByte(3);
            m.writer().writeUTF(rank_name);
            m.writer().writeByte(99); // page
            int my_index = -1;
            if (conn.p.myclan != null) {
                my_index = Rank.clan_index(conn.p.myclan, list);
            }
            m.writer().writeInt(my_index - 1); // my index in bxh
            int size = list.size();
            if (size > 20) {
                size = 20;
            }
            if (my_index > size) {
                size += 1;
            }
            m.writer().writeByte(size); // num2
            for (int i = 0; i < size; i++) {
                if (i >= 20) continue;
                ClanInfo clan = list.get(i);
                m.writer().writeUTF(clan.name);
                m.writer().writeInt(clan.idClan);
                m.writer().writeShort(clan.idIcon);
                m.writer().writeUTF(clan.shortName);
                m.writer().writeUTF(clan.info);
            }
            if (size > 20) {
                ClanInfo cif = ClanInfo.my_clan(conn, list);
                m.writer().writeUTF(cif.name);
                m.writer().writeInt(cif.idClan);
                m.writer().writeShort(cif.idIcon);
                m.writer().writeUTF(cif.shortName);
                m.writer().writeUTF(cif.info);
            }
            conn.addmsg(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendTopPlayer(Session conn, List<PlayerInfo> list, String rank_name) {
        try {
            Message m = new Message(56);
            m.writer().writeByte(1);
            m.writer().writeUTF(rank_name);
            m.writer().writeByte(99); // page
            int my_index = PlayerInfo.my_index(conn, list);
            m.writer().writeInt(my_index); // my index in bxh
            int size = list.size();
            if (size > 20) {
                size = 20;
            }
            if (my_index > size) {
                size += 1;
            }
            m.writer().writeByte(size); // num2
            for (int i = 0; i < size; i++) {
                if (i >= 20) continue;
                PlayerInfo temp = list.get(i);
                Player p0 = Map.get_player_by_name(temp.name);
                if (p0 != null) {
                    temp.head = p0.head;
                    temp.eye = p0.eye;
                    temp.hair = p0.hair;
                    temp.level = p0.level;
                    temp.item_wear.clear();
                    for (int i1 = 0; i1 < p0.item.wear.length; i1++) {
                        Item3 it = p0.item.wear[i1];
                        if (it != null && (i1 == 0 || i1 == 1 || i1 == 6 || i1 == 7 || i1 == 10)) {
                            Part_player part = new Part_player();
                            part.type = (byte) it.type;
                            part.part = (byte) it.part;

                            temp.item_wear.add(part);
                        }
                    }
                    temp.clan = p0.myclan;
                }
                m.writer().writeUTF(temp.name);
                m.writer().writeByte(temp.head);
                m.writer().writeByte(temp.eye);
                m.writer().writeByte(temp.hair);
                m.writer().writeShort(temp.level);
                m.writer().writeByte(temp.item_wear.size());
                for (Part_player it : temp.item_wear) {
                    m.writer().writeByte(it.part);
                    m.writer().writeByte(it.type);
                }
                m.writer().writeByte((p0 != null) ? (byte) 1 : (byte) 0); // type online
                m.writer().writeUTF(temp.info);
                if (temp.clan != null) {
                    m.writer().writeShort(temp.clan.icon);
                    m.writer().writeUTF(temp.clan.name_clan_shorted);
                    m.writer().writeByte(temp.clan.get_mem_type(temp.name));
                } else {
                    m.writer().writeShort(-1);
                }
            }
            if (size > 20) {
                PlayerInfo temp = list.get(my_index);
                Player p0 = Map.get_player_by_name(temp.name);
                if (p0 != null) {
                    temp.head = p0.head;
                    temp.eye = p0.eye;
                    temp.hair = p0.hair;
                    temp.level = p0.level;
                    temp.item_wear.clear();
                    for (int i1 = 0; i1 < p0.item.wear.length; i1++) {
                        Item3 it = p0.item.wear[i1];
                        if (it != null && (i1 == 0 || i1 == 1 || i1 == 6 || i1 == 7 || i1 == 10)) {
                            Part_player part = new Part_player();
                            part.type = (byte) it.type;
                            part.part = (byte) it.part;

                            temp.item_wear.add(part);
                        }
                    }
                    temp.clan = p0.myclan;
                }
                m.writer().writeUTF(temp.name);
                m.writer().writeByte(temp.head);
                m.writer().writeByte(temp.eye);
                m.writer().writeByte(temp.hair);
                m.writer().writeShort(temp.level);
                m.writer().writeByte(temp.item_wear.size());
                for (Part_player it : temp.item_wear) {
                    m.writer().writeByte(it.part);
                    m.writer().writeByte(it.type);
                }
                m.writer().writeByte((p0 != null) ? (byte) 1 : (byte) 0); // type online
                m.writer().writeUTF(temp.info);
                if (temp.clan != null) {
                    m.writer().writeShort(temp.clan.icon);
                    m.writer().writeUTF(temp.clan.name_clan_shorted);
                    m.writer().writeByte(temp.clan.get_mem_type(temp.name));
                } else {
                    m.writer().writeShort(-1);
                }
            }
            conn.addmsg(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int clan_index(Clan clan, List<ClanInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            if (clan.ID == list.get(i).idClan) {
                return i;
            }
        }
        return -1;
    }

    public static class PlayerInfo {
        public long tongnap;
        public short level;
        public long exp;
        public String name;
        public long point_activity;
        public long point_z6;
        public long point_arena;
        public byte head;
        public byte eye;
        public byte hair;
        public List<Part_player> item_wear;
        public Clan clan;
        public String info;
        public short id;

        public static int my_index(Session conn, List<PlayerInfo> list) {
            for (int i = 0; i < list.size(); i++) {
                if (conn.p.name.equals(list.get(i).name)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public static class ClanInfo {
        public String name;
        public int idClan;
        public short idIcon;
        public String shortName;
        public String info;

        public static ClanInfo my_clan(Session conn, List<ClanInfo> list) {
            for (ClanInfo clanInfo : list) {
                if (conn.p.myclan != null && conn.p.myclan.ID == clanInfo.idClan) {
                    return clanInfo;
                }
            }
            return null;
        }
    }
}
