package Game.client;

import Game.Helps._Time;
import Game.History.His_COIN;
import Game.admin.GiftcodeDAO;
import Game.core.Util;
import Game.map.Map;
import Game.map.MapService;
import Game.core.GameSrc;
import Game.event.EventManager;
import Game.event.Event_3;
import Game.event.LunarNewYear;
import Game.event.Event_1;

import java.sql.PreparedStatement;


import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import Game.core.Log;
import Game.core.Manager;
import Game.core.SQL;
import Game.core.Service;

import Game.io.Message;
import Game.io.Session;

import java.util.ArrayList;
import java.util.List;

import Game.map.Vgo;
import Game.template.Item3;
import Game.template.Item47;
import Game.template.ItemTemplate3;
import Game.template.ItemTemplate4;
import Game.template.ItemTemplate7;
import Game.template.Level;
import Game.template.BoxItem;

public class TextFromClient {

    public static void process(Session conn, Message m2) throws IOException {
        short idnpc = m2.reader().readShort();
        short idmenu = m2.reader().readShort();
        byte size = m2.reader().readByte();
        if (idmenu != 0) {
            return;
        }
        switch (idnpc) {

            case 999: {
                if (size != 3) {
                    return;
                }
                String value1 = m2.reader().readUTF();
                String value2 = m2.reader().readUTF();
                String value3 = m2.reader().readUTF();

                if (!value1.equals(conn.pass)) {
                    Service.send_notice_box(conn, "Mật khẩu không đúng");
                    return;
                }
                if (value2.equals(value1) || !value2.equals(value3)) {
                    Service.send_notice_box(conn, "Mật khẩu mới không hợp lệ");
                    return;
                }
                try (Connection connection = SQL.gI().getConnection(); Statement st = connection.createStatement()) {
                    st.execute("UPDATE `account` SET `pass` = '" + value2 + "' WHERE `user` = '" + conn.user + "';");
                    connection.commit();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Service.send_notice_box(conn, "Có lỗi xảy ra");
                    return;
                }
                Service.send_notice_box(conn, " Đổi mật khẩu mới thành công");
                break;
            }
            // Thêm vào Controller xử lý Message
            case 997: {
                try {
                    String inputPass = m2.reader().readUTF(); // Đọc mật khẩu người chơi nhập
                    // Kiểm tra null hoặc rỗng
                    if (inputPass == null || inputPass.isEmpty()) {
                        Service.send_notice_box(conn, "Vui lòng nhập mật khẩu!");
                        return;
                    }
                    // So sánh với passbox trong database (đã load vào conn)
                    if (inputPass.equals(conn.passbox)) {
                        conn.p.item.char_chest(3);
                        conn.p.item.char_chest(4);
                        conn.p.item.char_chest(7);
                        conn.p.type_process_chest = 0;
                        Message m = new Message(23);
                        m.writer().writeUTF("Rương đồ");
                        m.writer().writeByte(3);
                        m.writer().writeShort(0);
                        conn.addmsg(m);
                        m.cleanup();
                    } else {
                        Service.send_notice_box(conn, "Mật khẩu rương không đúng!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case 996: {
                if (size != 3) {
                    return;
                }
                String value1 = m2.reader().readUTF();
                String value2 = m2.reader().readUTF();
                String value3 = m2.reader().readUTF();

                if (conn.passbox != null && !conn.passbox.isEmpty()) {
                    if (!value1.equals(conn.passbox)) {
                        Service.send_notice_box(conn, "Mật khẩu rương cũ không đúng");
                        return;
                    }
                }

                if (value2.equals(value1)) {
                    Service.send_notice_box(conn, "Mật khẩu mới không được trùng mật khẩu cũ");
                    return;
                }
                if (!value2.equals(value3)) {
                    Service.send_notice_box(conn, "Mật khẩu nhập lại không khớp");
                    return;
                }

                // Kiểm tra độ dài (tránh đặt quá ngắn hoặc rỗng)
                if (value2.length() < 6) { // Ví dụ tối thiểu 6 ký tự
                    Service.send_notice_box(conn, "Mật khẩu rương phải từ 6 ký tự trở lên");
                    return;
                }

                // Cập nhật vào Database
                try (Connection connection = SQL.gI().getConnection();
                     PreparedStatement ps = connection.prepareStatement("UPDATE `account` SET `passbox` = ? WHERE `user` = '" + conn.user + "';")) {

                    ps.setString(1, value2);     // Set passbox mới
                    ps.executeUpdate();
                    connection.commit();
                    conn.passbox = value2;

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Service.send_notice_box(conn, "Có lỗi xảy ra khi lưu mật khẩu rương");
                    return;
                }
                Service.send_notice_box(conn, "Đổi mật khẩu rương thành công!");
                break;
            }
            case 995: { // ID packet: Tạo mật khẩu rương
                if (size != 2) { return; } // Cần 2 trường: Pass mới + Confirm Pass

                String newPass = m2.reader().readUTF();
                String confirmPass = m2.reader().readUTF();

                // 1. Kiểm tra xem đã có mật khẩu chưa
                if (conn.passbox != null && !conn.passbox.isEmpty()) {
                    Service.send_notice_box(conn, "Bạn đã có mật khẩu rương rồi. Hãy dùng chức năng đổi mật khẩu.");
                    return;
                }

                // 2. Validate dữ liệu
                if (newPass.length() < 6) {
                    Service.send_notice_box(conn, "Mật khẩu phải từ 6 ký tự trở lên.");
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    Service.send_notice_box(conn, "Mật khẩu nhập lại không khớp.");
                    return;
                }

                // 3. Lưu vào DB
                try (Connection connection = SQL.gI().getConnection();
                     PreparedStatement ps = connection.prepareStatement("UPDATE `account` SET `passbox` = ? WHERE `user` = '" + conn.user + "';")) {

                    ps.setString(1, newPass);

                    int rowAffected = ps.executeUpdate(); // Trả về số dòng được update
                    connection.commit(); // <--- QUAN TRỌNG: Thêm dòng này để lưu thay đổi

                    if (rowAffected > 0) {
                        conn.passbox = newPass;
                        Service.send_notice_box(conn, "Tạo mật khẩu rương thành công!");
                    } else {
                        Service.send_notice_box(conn, "Lỗi: Không tìm thấy tài khoản để update.");
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace(); // Xem kỹ log trong console (Netbeans/Eclipse) xem có báo lỗi gì không
                    Service.send_notice_box(conn, "Lỗi hệ thống (SQL).");
                }
                break;
            }
            case 994: { // ID packet: Xóa mật khẩu rương
                if (size != 1) { return; } // Chỉ cần 1 trường: Mật khẩu hiện tại để xác thực

                String currentPass = m2.reader().readUTF();

                // 1. Kiểm tra xem có mật khẩu để xóa không
                if (conn.passbox == null || conn.passbox.isEmpty()) {
                    Service.send_notice_box(conn, "Bạn chưa thiết lập mật khẩu rương.");
                    return;
                }

                // 2. Xác thực mật khẩu
                if (!currentPass.equals(conn.passbox)) {
                    Service.send_notice_box(conn, "Mật khẩu rương không đúng, không thể xóa!");
                    return;
                }

                // 3. Xóa trong DB (Set về NULL)
                try (Connection connection = SQL.gI().getConnection();
                     PreparedStatement ps = connection.prepareStatement("UPDATE `account` SET `passbox` = NULL WHERE `user` = ?")) {

                    ps.setString(1, conn.user);
                    ps.executeUpdate();
                    connection.commit(); // <--- QUAN TRỌNG: Thêm dòng này để lưu thay đổi
                    conn.passbox = null;     // Xóa pass trong ram

                    Service.send_notice_box(conn, "Đã xóa mật khẩu rương thành công.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Service.send_notice_box(conn, "Lỗi hệ thống.");
                }
                break;
            }

            case 0: {
                // Đệ tử
                if (!conn.p.isOwner) {
                    return;
                }
                if (size != 1) {
                    return;
                }
                String text = m2.reader().readUTF();
                text = text.toLowerCase();
                Pattern p = Pattern.compile("^[a-zA-Z0-9]{1,15}$");
                if (!p.matcher(text).matches()) {
                    Service.send_notice_box(conn, "Đã xảy ra lỗi");
                    return;
                }
                for (String txt : conn.p.giftcode) {
                    txt = txt.toLowerCase();
                    if (txt.equals((text)) && conn.ac_admin < 10) {
                        Service.send_notice_box(conn, "Bạn đã nhập giftcode này rồi");
                        return;
                    }
                }
                try (Connection connection = SQL.gI().getConnection(); Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM `giftcode` WHERE `giftname` = '" + text + "';")) {
                    byte empty_box = (byte) 0;
                    if (!rs.next()) {
                        Service.send_notice_box(conn, "Giftcode đã được nhập hoặc không tồn tại");
                    } else {
                        List<Short> IDs = new ArrayList<>();
                        List<Integer> Quants = new ArrayList<>();
                        List<Short> Types = new ArrayList<>();
                        empty_box = rs.getByte("empty_box");
                        int limit = rs.getInt("limit");
                        byte date = rs.getByte("date");
                        String gift_for = rs.getString("gift_for");
                        int level = rs.getInt("level");
                        int needActive = rs.getInt("needActive");
                        if (needActive == 0 && conn.status != 0) {
                            Service.send_notice_box(conn, "Cần kích hoạt để nhập GIFTCODE này");
                        } else if (!gift_for.isEmpty() && !gift_for.equals(conn.user)) {
                            Service.send_notice_box(conn, "Gift code này không dành cho bạn");
                        } else if (level > conn.p.level) {
                            Service.send_notice_box(conn, "Để nhập giftcode cần đạt level " + level);
                        } else if (limit < 1) {
                            Service.send_notice_box(conn, "Đã hết lượt dùng giftcode này");
                        } else if (conn.p.item.get_inventory_able() >= empty_box) {
                            conn.p.giftcode.add(text);
                            JSONArray jsar = (JSONArray) JSONValue.parse(rs.getString("item3"));
                            for (Object o : jsar) {
                                JSONArray jsar2 = (JSONArray) JSONValue.parse(o.toString());
                                Item3 itInventory = new Item3();
                                short it = Short.parseShort(jsar2.get(0).toString());
                                itInventory.id = it;
                                itInventory.name = ItemTemplate3.item.get(it).getName();
                                itInventory.clazz = ItemTemplate3.item.get(it).getClazz();
                                itInventory.type = ItemTemplate3.item.get(it).getType();
                                itInventory.level = ItemTemplate3.item.get(it).getLevel();
                                itInventory.icon = ItemTemplate3.item.get(it).getIcon();
                                itInventory.op = new ArrayList<>();
                                itInventory.op.addAll(ItemTemplate3.item.get(it).getOp());
                                itInventory.color = ItemTemplate3.item.get(it).getColor();
                                itInventory.part = ItemTemplate3.item.get(it).getPart();
                                itInventory.tier = 0;
                                if (date != 0) {
                                    itInventory.expiry_date = date * 24 * 60 * 60 * 1000L + System.currentTimeMillis();
                                }
                                short expiry = Short.parseShort(jsar2.get(1).toString());
                                if (itInventory.type == 14) {
                                    itInventory.time_use = expiry * 24 * 60 * 60 * 1000L;
                                }
//                                else {
//                                    if (expiry != 0) {
//                                        itInventory.expiry_date = expiry * 24 * 60 * 60 * 1000L + System.currentTimeMillis();
//                                    }
//                                }
                                itInventory.islock = false;
                                if (Item3.isBook(itInventory.id)) {
                                    itInventory.color = 5;
                                }
                                IDs.add(it);
                                Quants.add((int) 1);
                                Types.add((short) 3);
                                conn.p.item.add_item_inventory3(itInventory);
                            }
                            jsar.clear();
                            //
                            jsar = (JSONArray) JSONValue.parse(rs.getString("item4"));
                            for (Object o : jsar) {
                                JSONArray jsar2 = (JSONArray) JSONValue.parse(o.toString());
                                Item47 itbag = new Item47();
                                itbag.id = Short.parseShort(jsar2.get(0).toString());
                                itbag.quantity = Short.parseShort(jsar2.get(1).toString());
                                itbag.category = 4;
                                IDs.add(itbag.id);
                                Quants.add((int) itbag.quantity);
                                Types.add((short) itbag.category);
                                conn.p.item.add_item_inventory47(4, itbag);
                            }
                            jsar.clear();
                            //
                            jsar = (JSONArray) JSONValue.parse(rs.getString("item7"));
                            for (Object o : jsar) {
                                JSONArray jsar2 = (JSONArray) JSONValue.parse(o.toString());
                                Item47 itbag = new Item47();
                                itbag.id = Short.parseShort(jsar2.get(0).toString());
                                itbag.quantity = Short.parseShort(jsar2.get(1).toString());
                                itbag.category = 7;
                                IDs.add(itbag.id);
                                Quants.add((int) itbag.quantity);
                                Types.add((short) itbag.category);
                                conn.p.item.add_item_inventory47(7, itbag);

                            }
                            jsar.clear();

                            long vang_up = rs.getLong("vang");
                            int ngoc_up = rs.getInt("ngoc");
                            conn.p.update_vang(vang_up, "Nhận %s vàng từ nhập giftcode " + text);
                            conn.p.update_ngoc(ngoc_up);
                            if (vang_up != 0) {
                                IDs.add((short) -1);
                                Quants.add((int) (vang_up > 2_000_000_000 ? 2_000_000_000 : vang_up));
                                Types.add((short) 4);
                            }
                            if (ngoc_up != 0) {
                                IDs.add((short) -2);
                                Quants.add((int) (ngoc_up > 2_000_000_000 ? 2_000_000_000 : ngoc_up));
                                Types.add((short) 4);
                            }

                            short[] ar_id = new short[IDs.size()];
                            int[] ar_quant = new int[Quants.size()];
                            short[] ar_type = new short[Types.size()];
                            for (int i = 0; i < ar_id.length; i++) {
                                ar_id[i] = IDs.get(i);
                                ar_quant[i] = Quants.get(i);
                                ar_type[i] = Types.get(i);
                            }
                            conn.p.item.char_inventory(5);
                            if (limit > 0) {
                                try (PreparedStatement psUpdate = connection.prepareStatement("UPDATE `giftcode` SET `limit` = `limit` - 1 WHERE `giftname` = ?")) {
                                    psUpdate.setString(1, text);
                                    psUpdate.executeUpdate();
                                    connection.commit();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ar_id, ar_quant, ar_type);
                        } else {
                            Service.send_notice_box(conn, "Hành trang phải trống " + empty_box + " ô trở lên!");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            case 1: {
                if (conn.ac_admin > 3) {
                    if (size != 3) {
                        return;
                    }
                    String type = m2.reader().readUTF();
                    String id = m2.reader().readUTF();
                    String quantity = m2.reader().readUTF();
                    if (!(Util.isnumber(id) && Util.isnumber(quantity))) {
                        Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                        return;
                    }
                    Short sl = Short.parseShort(quantity);
                    if (sl > 32_000 || sl <= 0) {
                        Service.send_notice_box(conn, "Số lượng không hợp lệ!");
                        return;
                    }
                    if (conn.p.item.get_inventory_able() > 0) {
                        switch (type) {
                            case "3": {
                                short iditem = (short) Integer.parseInt(id);
                                if (iditem > (ItemTemplate3.item.size() - 1) || iditem < 0) {
                                    return;
                                }
                                Item3 itbag = new Item3();
                                itbag.id = iditem;
                                itbag.name = ItemTemplate3.item.get(iditem).getName();
                                itbag.clazz = ItemTemplate3.item.get(iditem).getClazz();
                                itbag.type = ItemTemplate3.item.get(iditem).getType();
                                itbag.level = ItemTemplate3.item.get(iditem).getLevel();
                                itbag.icon = ItemTemplate3.item.get(iditem).getIcon();
                                itbag.op = new ArrayList<>();
                                itbag.op.addAll(ItemTemplate3.item.get(iditem).getOp());
                                itbag.color = ItemTemplate3.item.get(iditem).getColor();
                                itbag.part = ItemTemplate3.item.get(iditem).getPart();
                                itbag.tier = 0;
                                itbag.islock = false;
                                itbag.time_use = 0;
                                conn.p.item.add_item_inventory3(itbag);
                                conn.p.item.char_inventory(3);
                                break;
                            }
                            case "4": {
                                short iditem = (short) Integer.parseInt(id);
                                if (iditem > (ItemTemplate4.item.size() - 1) || iditem < 0) {
                                    return;
                                }
                                Item47 itbag = new Item47();
                                itbag.id = iditem;
                                itbag.quantity = sl;
                                itbag.category = 4;
                                conn.p.item.add_item_inventory47(4, itbag);
                                break;
                            }
                            case "7": {
                                short iditem = (short) Integer.parseInt(id);
                                if (iditem > (ItemTemplate7.item.size() - 1) || iditem < 0) {
                                    return;
                                }
                                Item47 itbag = new Item47();
                                itbag.id = iditem;
                                itbag.quantity = Short.parseShort(quantity);
                                itbag.category = 7;
                                conn.p.item.add_item_inventory47(7, itbag);
                                conn.p.item.char_inventory(7);
                                break;
                            }
                        }
                        Service.send_notice_box(conn, "Nhận Item thành công");
                    }
                }
                break;
            }
            case 2: {
                if (conn.ac_admin > 3) {
                    if (size != 1) {
                        return;
                    }
                    String level = m2.reader().readUTF();
                    if (!(Util.isnumber(level))) {
                        Service.send_notice_box(conn, "Sai định dạng");
                        return;
                    }
                    int levelchange = Integer.parseInt(level);
                    if (levelchange > 32000 || levelchange <= 0) {
                        Service.send_notice_box(conn, "Số lượng không hợp lệ!");
                        return;
                    }
                    if (levelchange < 2) {
                        levelchange = 2;
                    }
                    if (levelchange > Manager.gI().lvmax) {
                        levelchange = Manager.gI().lvmax;
                    }
                    conn.p.level = (short) (levelchange - 1);
                    conn.p.exp = Level.entry.get(levelchange - 2).exp - 1;
                    conn.p.tiemnang = (short) (1 + Level.get_tiemnang_by_level(conn.p.level - 1));
                    conn.p.kynang = (short) (1 + Level.get_kynang_by_level(conn.p.level - 1));
                    conn.p.point1 = (short) (4 + conn.p.level);
                    conn.p.point2 = (short) (4 + conn.p.level);
                    conn.p.point3 = (short) (4 + conn.p.level);
                    conn.p.point4 = (short) (4 + conn.p.level);
                    conn.p.update_Exp(1, false);
                    Service.send_char_main_in4(conn.p);
                    for (int i = 0; i < conn.p.map.players.size(); i++) {
                        Player p0 = conn.p.map.players.get(i);
                        if (p0.ID != conn.p.ID && (Math.abs(p0.x - conn.p.x) < 200) && (Math.abs(p0.y - conn.p.y) < 200)) {
                            MapService.send_in4_other_char(p0.map, p0, conn.p);
                        }
                    }
                    Service.send_notice_box(conn, "Up level thành công");
                }
                break;
            }
            case 3: {
                // Vòng xoay VIP
                if (!conn.p.isOwner || size != 1) {
                    return;
                }

                String vang_join = m2.reader().readUTF();
                if (!Util.isnumber(vang_join)) {
                    Service.send_notice_box(conn, "Sai định dạng số vàng.");
                    return;
                }

                int vang_join_vxmm = Integer.parseInt(vang_join);

                // Không cho tham gia nếu vàng đang có vượt quá 10 tỷ
                if (conn.p.get_vang() >= 10_000_000_000L) {
                    Service.send_notice_box(conn, "Bạn đang mang quá 10 tỷ vàng. Hãy sử dụng bớt vàng trước khi tham gia.");
                    return;
                }

                // Giới hạn vàng đặt cược tối đa
                if (vang_join_vxmm > Manager.gI().lucky_draw_vip.max_join) {
                    Service.send_notice_box(conn, "Chỉ được cược tối đa " + Util.number_format(Manager.gI().lucky_draw_vip.max_join) + " vàng.");
                    return;
                }

                // Giới hạn vàng đặt cược tối thiểu
                if (vang_join_vxmm < Manager.gI().lucky_draw_vip.min_join) {
                    Service.send_notice_box(conn, "Số vàng cược tối thiểu là " + Util.number_format(Manager.gI().lucky_draw_vip.min_join) + ".");
                    return;
                }

                // Kiểm tra có đủ vàng không
                if (conn.p.get_vang() < vang_join_vxmm) {
                    Service.send_notice_box(conn, "Bạn không đủ vàng để tham gia.");
                    return;
                }

                // Tham gia vòng xoay
                Manager.gI().lucky_draw_vip.join_lucky_draw(conn.p, vang_join_vxmm);
                break;
            }

            case 4: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                if (size != 1) {
                    return;
                }
                String xp = m2.reader().readUTF();
                if (!(Util.isnumber(xp))) {
                    Service.send_notice_box(conn, "Sai định dạng");
                    return;
                }
                int xp_ = Integer.parseInt(xp);
                if (xp_ <= 0 || xp_ > 2000000000) {
                    Service.send_notice_box(conn, "Số lượng nhập vào không hợp lệ!");
                    return;
                }
                if (xp_ > 1) {
                    Manager.gI().exp = xp_;
                }
                Service.send_notice_box(conn, "Thay đổi xp thành công x" + Util.number_format(xp_));
                break;
            }
            case 6: {
                if (size != 2) {
                    return;
                }
                String value1 = m2.reader().readUTF();
                String value2 = m2.reader().readUTF();
                Pattern p = Pattern.compile("^[a-zA-Z0-9]{5,15}$");
                if (!p.matcher(value1).matches() || !p.matcher(value2).matches()) {
                    Service.send_notice_box(conn, "Ký tự không hợp lệ, hãy thử lại");
                    return;
                }
                String query = "UPDATE `account` SET `user` = '" + value1 + "', `pass` = '" + value2 + "'  WHERE `user` = '"
                        + conn.user + "' LIMIT 1";
                try (Connection connnect = SQL.gI().getConnection(); Statement statement = connnect.createStatement();) {
                    if (statement.executeUpdate(query) > 0) {
                        connnect.commit();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    Service.send_notice_box(conn, conn.language.tendadung);
                    return;
                }
                Message md = new Message(31);
                md.writer().writeUTF(value1);
                md.writer().writeUTF(value2);
                conn.addmsg(md);
                md.cleanup();
                conn.user = value1;
                conn.pass = value2;
                Service.send_notice_box(conn, String.format(conn.language.dangkythanhcong, value1, value2));
                break;
            }

            case 7: {
                if (size != 1 || conn.p.fusion_material_medal_id == -1) {
                    return;
                }
                String value = m2.reader().readUTF();
                if (!(Util.isnumber(value))) {
                    Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                    return;
                }
                int quant = Integer.parseInt(value);
                if (quant > 32000 || quant <= 0) {
                    Service.send_notice_box(conn, "Số lượng không hợp lệ");
                    return;
                }
                if (conn.p.item.get_inventory_able() < 1) {
                    Service.send_notice_box(conn, "Hành trang đầy!");
                    return;
                }
                int quant_inbag = conn.p.item.total_item_by_id(7, conn.p.fusion_material_medal_id);
                int quant_real = quant_inbag / 5;
                short id_next_material = (short) (conn.p.fusion_material_medal_id + 100);
                String name_next_material = ItemTemplate7.item.get(id_next_material).getName();
                if ((quant_real - quant) >= 0) {
                    if ((quant * 5000) > conn.p.get_vang()) {
                        Service.send_notice_box(conn, "Vàng không đủ!");
                        return;
                    }

                    conn.p.update_vang(-(quant * 5000), "Trừ %s vàng hợp nguyên liệu mề đay");
                    conn.p.item.remove(7, conn.p.fusion_material_medal_id, (quant * 5));
                    Item47 it = new Item47();
                    it.id = id_next_material;
                    it.quantity = (short) quant;
                    conn.p.item.add_item_inventory47(7, it);
                    conn.p.item.char_inventory(7);
                    //
                    Message m = new Message(-105);
                    m.writer().writeByte(2);
                    m.writer().writeByte(3);
                    m.writer().writeUTF("Chúc mừng bạn nhận được " + quant + " " + name_next_material);
                    m.writer().writeShort(id_next_material);
                    m.writer().writeByte(7);
                    conn.addmsg(m);
                    m.cleanup();
                } else {
                    Service.send_notice_box(conn, "Chỉ có thể hợp thành tối đa " + quant_real + " " + name_next_material);
                }
                conn.p.fusion_material_medal_id = -1;
                break;
            }
//            case 8: {
//                if (size != 1) {
//                    return;
//                }
//                String value = m2.reader().readUTF();
//                if (!(Util.isnumber(value))) {
//                    Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
//                    return;
//                }
//                int coin_exchange = Integer.parseInt(value);
//                if (coin_exchange < 1000 || coin_exchange > 300_000) {
//                    Service.send_notice_box(conn, "Chỉ có thể đổi tối thiểu là 1.000 và tối đa là 300.000");
//                    return;
//                }
//                if (conn.p.update_coin(-coin_exchange)) {
//                    long gold = coin_exchange * 5000L;
//                    conn.p.update_vang(gold);
//                    Service.send_notice_box(conn, "Đổi thành công " + Util.number_format(gold) + " vàng");
//                    Log.gI().add_Log_Server("Đổi coin", conn.p.name + " Đổi thành công " + coin_exchange + " coin ra vàng");
//                } else {
//                    Service.send_notice_box(conn, "Thất bại xin hãy thử lại");
//                }
//                break;
//            }
            case 9: {
                if (size != 1) {
                    return;
                }
                String value = m2.reader().readUTF();
                if (!(Util.isnumber(value))) {
                    Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                    return;
                }
                int quant = Integer.parseInt(value);
                if (quant > 2_000_000_000 || quant <= 0) {
                    Service.send_notice_box(conn, "Số lượng không hợp lệ!");
                    return;
                }
                if (idnpc == 8) {
                    if (quant > conn.p.get_vang()) {
                        Service.send_notice_box(conn, "Vàng không đủ!");
                        return;
                    }
                    conn.p.myclan.member_contribute_vang(conn, quant);
                } else {
                    if (quant > conn.p.get_ngoc()) {
                        Service.send_notice_box(conn, "Ngọc không đủ!");
                        return;
                    }
                    conn.p.myclan.member_contribute_ngoc(conn, quant);
                }
                break;
            }
            case 10: {
                if (Manager.gI().event == 1) {
                    if (size != 1) {
                        return;
                    }
                    String value = m2.reader().readUTF();
                    if (!(Util.isnumber(value))) {
                        Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                        return;
                    }
                    int quant = Integer.parseInt(value);
                    if (quant > 500 || quant <= 0) {
                        Service.send_notice_box(conn, "Số lượng không hợp lệ!");
                        return;
                    }
                    //
                    if (conn.p.get_vang() < (quant * 20_000)) {
                        Service.send_notice_box(conn, "Vàng không đủ!");
                        return;
                    }
                    short[] id = new short[]{118, 119, 120, 121, 122};
                    for (int i = 0; i < id.length; i++) {
                        if (conn.p.item.total_item_by_id(4, id[i]) < (quant * 50)) {
                            Service.send_notice_box(conn, (ItemTemplate4.item.get(id[i]).getName() + " không đủ!"));
                            return;
                        }
                    }
                    conn.p.update_vang(-(quant * 20_000), "Trừ %s vàng đổi hộp đồ chơi");
                    for (int i = 0; i < id.length; i++) {
                        conn.p.item.remove(4, id[i], quant * 50);
                    }
                    Item47 it = new Item47();
                    it.category = 4;
                    it.id = (short) 158;
                    it.quantity = (short) quant;
                    conn.p.item.add_item_inventory47(4, it);
                    //
                    Service.send_notice_box(conn, "Đổi thành công " + quant + " hộp đồ chơi");
                }
                break;
            }
            case 11: {
                if (Manager.gI().event == 1) {
                    if (size != 1) {
                        return;
                    }
                    String value = m2.reader().readUTF();
                    if (!(Util.isnumber(value))) {
                        Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                        return;
                    }
                    int quant = Integer.parseInt(value);
                    if (quant > 500 || quant <= 0) {
                        Service.send_notice_box(conn, "Số lượng không hợp lệ!");
                        return;
                    }
                    //
                    short[] id = new short[]{153, 154, 155, 156};
                    for (int i = 0; i < id.length; i++) {
                        if (conn.p.item.total_item_by_id(4, id[i]) < (quant)) {
                            Service.send_notice_box(conn, (ItemTemplate4.item.get(id[i]).getName() + " không đủ!"));
                            return;
                        }
                    }
                    for (int i = 0; i < id.length; i++) {
                        conn.p.item.remove(4, id[i], quant);
                    }
                    Event_1.add_material(conn.p.name, quant);
                    //
                    Service.send_notice_box(conn, "Đóng góp nguyên liệu tạo " + quant + " kẹo");
                }
                break;
            }
            case 12: {
                if (Manager.gI().event == 1) {
                    if (size != 1) {
                        return;
                    }
                    String value = m2.reader().readUTF();
                    if (!(Util.isnumber(value))) {
                        Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                        return;
                    }
                    int quant = Integer.parseInt(value);
                    if (quant > 500 || quant <= 0) {
                        Service.send_notice_box(conn, "Số lượng không hợp lệ!");
                        return;
                    }
                    //
                    if (conn.p.get_vang() < (quant * 50_000)) {
                        Service.send_notice_box(conn, "Vàng không đủ!");
                        return;
                    }
                    if (conn.p.item.total_item_by_id(4, 162) < (quant * 5)) {
                        Service.send_notice_box(conn, (ItemTemplate4.item.get(162).getName() + " không đủ!"));
                        return;
                    }
                    conn.p.update_vang(-(quant * 50_000), "Trừ %s vàng đổi túi kẹo");
                    conn.p.item.remove(4, 162, quant * 5);
                    //
                    Item47 it = new Item47();
                    it.category = 4;
                    it.id = 157;
                    it.quantity = (short) quant;
                    conn.p.item.add_item_inventory47(4, it);
                    //
                    Service.send_notice_box(conn, "Đổi thành công " + quant + " túi kẹo");
                }
                break;
            }
            case 13: {
                if (size != 1) {
                    return;
                }
                String name = m2.reader().readUTF();
                Pattern p = Pattern.compile("^[a-zA-Z0-9]{6,10}$");
                if (!p.matcher(name).matches()) {
                    Service.send_notice_box(conn, "tên không hợp lệ, nhập lại đi!!");
                    return;
                }
                if (conn.p.myclan != null && !conn.p.myclan.mems.get(0).name.equals(name)) {

                    conn.p.name_mem_clan_to_appoint = name;
                    Service.send_box_input_yesno(conn, 113, "Xác nhận nhường thủ lĩnh cho " + name);
                }
                break;
            }
            case 14:
            case 15: {
                if (size != 1) {
                    return;
                }
                String value = m2.reader().readUTF();
                if (!(Util.isnumber(value))) {
                    Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                    return;
                }
                int quant = Integer.parseInt(value);
                if (quant > 32_000 || quant <= 0) {
                    Service.send_notice_box(conn, "Số lượng không hợp lệ!");
                    return;
                }
                if (conn.p.item.get_inventory_able() < 1) {
                    Service.send_notice_box(conn, "Hành trang đầy!");
                    return;
                }
                int quant_ngoc_can_create = conn.p.item.total_item_by_id(7, conn.p.id_hop_ngoc) / 5;
                if (quant > quant_ngoc_can_create) {
                    Service.send_notice_box(conn, "Số lượng trong hành trang không đủ!");
                    return;
                }
                int vang_required = GameSrc.get_vang_hopngoc(conn.p.id_hop_ngoc) * quant;
                if (conn.p.get_vang() < vang_required) {
                    Service.send_notice_box(conn, "Không đủ " + vang_required + " vàng");
                    return;
                }
                conn.p.update_vang(-vang_required, "Trừ %s vàng hợp ngọc");
                conn.p.item.remove(7, conn.p.id_hop_ngoc, (quant * 5));
                Item47 itbag = new Item47();
                itbag.id = (short) (conn.p.id_hop_ngoc + 1);
                itbag.quantity = (short) quant;
                itbag.category = 7;
                conn.p.item.add_item_inventory47(7, itbag);
                conn.p.id_hop_ngoc = -1;
                //
                Message m = new Message(-100);
                m.writer().writeByte(3);
                m.writer().writeUTF("Nhận được " + quant + " " + ItemTemplate7.item.get(itbag.id).getName());
                m.writer().writeShort(itbag.id);
                m.writer().writeByte(7);
                conn.addmsg(m);
                m.cleanup();
                break;
            }
            case 16: {
                if (size != 1) {
                    return;
                }
                String value = m2.reader().readUTF();
                if (!Util.isnumber(value)) {
                    Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                    return;
                }

                int quant = Integer.parseInt(value);

                // Giới hạn số lượng hợp lệ
                if (quant <= 0 || quant > 32000) {
                    Service.send_notice_box(conn, "Số lượng không hợp lệ! (1-32000)");
                    return;
                }

                int quant_ngoc_can_create = conn.p.item.total_item_by_id(7, conn.p.id_ngoc_tinh_luyen);
                if (quant > quant_ngoc_can_create) {
                    Service.send_notice_box(conn, "Số lượng trong hành trang không đủ!");
                    return;
                }

                long vang_per_item = GameSrc.get_vang_hopngoc(conn.p.id_ngoc_tinh_luyen) / 50_000L * 1_000_000L;
                long vang_required = quant * vang_per_item;

                // Nếu vàng không đủ, tự động giới hạn số lượng
                if (conn.p.get_vang() < vang_required) {
                    quant = (int) (conn.p.get_vang() / vang_per_item);
                    if (quant == 0) {
                        Service.send_notice_box(conn, "Không đủ vàng để tinh luyện!");
                        return;
                    }
                    Service.send_notice_box(conn, "Vàng không đủ, chỉ tinh luyện được " + quant + " viên.");
                    vang_required = quant * vang_per_item;
                }

                // Trừ vàng
                conn.p.update_vang(-vang_required, "Trừ %s vàng tinh luyện ngọc");

                // Tạo ngọc mới
                Item47 it = new Item47();
                it.id = (short) (conn.p.id_ngoc_tinh_luyen + 30);
                it.quantity = (short) quant;
                conn.p.item.add_item_inventory47(7, it);
                conn.p.item.remove(7, conn.p.id_ngoc_tinh_luyen, quant);

                Service.send_notice_box(conn, "Tinh luyện thành công " + quant + " " + ItemTemplate7.item.get(it.id).getName());
                conn.p.id_ngoc_tinh_luyen = -1;
                break;
            }
            case 17: {
                if (size != 1) {
                    return;
                }
                String vang_join = m2.reader().readUTF();
                if (!(Util.isnumber(vang_join))) {
                    Service.send_notice_box(conn, "Sai định dạng");
                    return;
                }
                int vang_join_vxmm = Integer.parseInt(vang_join);
                if (vang_join_vxmm > Manager.gI().lucky_draw_normal.max_join) {
                    Service.send_notice_box(conn, "Chỉ được gia tối đa " + Util.number_format(Manager.gI().lucky_draw_normal.max_join));
                    return;
                }
                if (vang_join_vxmm < Manager.gI().lucky_draw_normal.min_join) {
                    Service.send_notice_box(conn, "Số vàng đặt cược tối thiểu là " + Util.number_format(Manager.gI().lucky_draw_normal.min_join));
                    return;
                }
                if (conn.p.get_vang() < vang_join_vxmm) {
                    Service.send_notice_box(conn, "Bạn không có đủ vàng để tham gia");
                    return;
                }
                Manager.gI().lucky_draw_normal.join_lucky_draw(conn.p, vang_join_vxmm);
                break;
            }
            case 18: {
                if (conn.ac_admin <= 3) {
                    Service.send_notice_box(conn, "Không đủ thẩm quyền!");
                    return;
                }
                String nameUser = m2.reader().readUTF();
                //Pattern p = Pattern.compile("^[a-zA-Z0-9@.]{1,15}$");
//                if ( !p.matcher(nameUser).matches() ) {
//                    Service.send_notice_box(conn,"ký tự nhập vào không hợp lệ!!");
//                    return;
//                }
                for (int i = Session.client_entry.size() - 1; i >= 0; i--) {
                    Session s = Session.client_entry.get(i);
                    if (s != null && s.p != null && s.p.name != null && s.p.name.equals(nameUser)) {
                        Session.client_entry.get(i).p.timeBlockCTG = _Time.GetTimeNextDay();
                        Service.send_notice_box(conn, "Khóa mõm nhân vật " + nameUser + " 1 ngày thành công.");
                        return;
                    }
                }
                Service.send_notice_box(conn, "Không tìm thấy nhân vật hoặc không online");

                break;
            }
            case 19: {
                if (conn.ac_admin <= 3) {
                    Service.send_notice_box(conn, "Không đủ thẩm quyền!");
                    return;
                }
                String nameUser = m2.reader().readUTF();
                for (int i = Session.client_entry.size() - 1; i >= 0; i--) {
                    Session s = Session.client_entry.get(i);
                    if (s != null && s.p != null && s.p.name != null && s.p.name.equals(nameUser)) {
                        Session.client_entry.get(i).p.timeBlockCTG = 0;
                        Service.send_notice_box(conn, "Đã gỡ mõm nhân vật " + nameUser);
                        return;
                    }
                }
                break;
            }
            case 20: {
                String namePlayer = m2.reader().readUTF();
                conn.p.Store_Sell_ToPL = namePlayer;
                Service.send_notice_box(conn, "Đã cài đặt chỉ bán cho nhân vật " + namePlayer);
                break;
            }
            case 21: {
                if (conn.ac_admin > 3) {
                    if (size != 3) {
                        return;
                    }
                    try {
                        Vgo v = new Vgo();
                        v.id_map_go = Byte.parseByte(m2.reader().readUTF());
                        v.x_new = Short.parseShort(m2.reader().readUTF());
                        v.y_new = Short.parseShort(m2.reader().readUTF());
                        conn.p.change_map(conn.p, v);
                    } catch (Exception e) {
                        Service.send_notice_box(conn, "Đã xảy ra lỗi!");
                    }

                }
                break;
            }
            case 22: {
                if (size != 1) {
                    return;
                }
                String thue = m2.reader().readUTF();
                if (!(Util.isnumber(thue))) {
                    Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                    return;
                }
                int thuechange = Byte.parseByte(thue);
                if (thuechange < 5 || thuechange > 15) {
                    Service.send_notice_box(conn, "Chỉ có thể cài đặt thuế trong khoảng từ 5 đến 15%");
                    return;
                }
                if (conn.p.myclan == null || Manager.ClanThue == null || !conn.p.myclan.equals(Manager.ClanThue)) {
                    Service.send_notice_box(conn, "Chỉ clan chiếm được thành mới có thể đặt thuế!");
                } else if (!Manager.ClanThue.mems.get(5).name.equals(conn.p.name)) {
                    Service.send_notice_box(conn, "Chỉ chủ bang mới có quyền thực hiện hành động này!");
                } else {
                    Manager.thue = (byte) thuechange;
                    Service.send_notice_box(conn, "Bạn đã thay đổi mức thuế lên " + Manager.thue + " %");
                }
                break;
            }
            case 23: {
                if (conn.p.level < 30) {
                    Service.send_notice_box(conn, "Yêu cầu cấp độ 30");
                    return;
                }
                String[] value = new String[]{m2.reader().readUTF(), m2.reader().readUTF()};
                if (!value[0].equals("") && !value[1].equals("")) {
                    if (value[0].contains("_") || value[0].contains("-") || value[0].contains("@") || value[0].contains("#")
                            || value[0].contains("^") || value[0].contains("$") || value[0].length() > 20
                            || value[0].length() < 4) {
                        Service.send_notice_box(conn, "Tên nhập vào không hợp lệ");
                        return;
                    }
                    Pattern p = Pattern.compile("^[a-zA-Z0-9]{3,3}$");
                    if (!p.matcher(value[1]).matches()) {
                        Service.send_notice_box(conn, "Tên rút gọn nhập vào không hợp lệ");
                        return;
                    }
                    if (conn.p.get_ngoc() < 25000) {
                        Service.send_notice_box(conn, "Không đủ 25.000 ngọc!");
                        return;
                    }
                    if (Clan.start_create_clan(conn, value[0], value[1])) {
                        conn.p.item.char_inventory(5);
                        Service.send_box_UI(conn, 20);
                        Service.send_notice_box(conn, "Hãy chọn một icon bất kỳ đặt làm biểu tượng");
                    }
                } else {
                    Service.send_notice_box(conn, "Không được bỏ trống các trường");
                }
                break;
            }
            case 24: {
                if (conn.ac_admin <= 3) {
                    Service.send_notice_box(conn, "Không đủ quyền!");
                    return;
                }
                try {
                    String type = m2.reader().readUTF();
                    String nameUser = m2.reader().readUTF();
                    if (type == null || type.isEmpty() || nameUser == null || nameUser.isEmpty()) {
                        Service.send_notice_box(conn, "Không được bỏ trống trường dữ liệu!");
                        return;
                    }
                    int count = 0;
                    switch (type) {
                        case "1":
                            for (int i = Session.client_entry.size() - 1; i >= 0; i--) {
                                Session s = Session.client_entry.get(i);
                                if (s != null && s.user != null && s.user.toLowerCase().equals(nameUser.toLowerCase())) {
                                    count++;
                                    System.out.println("=============close session " + s.user);
                                    Session.client_entry.get(i).close();
                                }
                            }
                            Service.send_notice_box(conn, "Đã disconnect " + count + " session có tên tài khoản: " + nameUser);
                            break;
                        case "2":
                            for (int i = Session.client_entry.size() - 1; i >= 0; i--) {
                                Session s = Session.client_entry.get(i);
                                if (s != null && s.p != null && s.p.name != null && s.p.name.toLowerCase().equals(nameUser.toLowerCase())) {
                                    count++;
                                    System.out.println("=============close session " + s.user);
                                    Session.client_entry.get(i).close();
                                }
                            }
                            Service.send_notice_box(conn, "Đã disconnect " + count + " session có tên nhân vật: " + nameUser);
                            break;
                        default:
                            Service.send_notice_box(conn, "Không đúng định dạng loại:\n1: Tên tài khoản \n2: Tên nhân vật");
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                    Service.send_notice_box(conn, "lỗi cmnr3!");
                }

                break;
            }
            case 25:
            case 26:
            case 27: {
                String value = m2.reader().readUTF();
                if (!(Util.isnumber(value))) {
                    Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                    return;
                }
                int quant = Integer.parseInt(value);
                if (quant > 200 || quant <= 0) {
                    Service.send_notice_box(conn, "Số lượng không hợp lệ!");
                    return;
                }
                if (conn.p.item.get_inventory_able() < 1) {
                    Service.send_notice_box(conn, "Hành trang đầy!");
                    return;
                }
                short id_cu = 306, id_moi = 307, chuyendoi = 30;
                long vag = quant * 100_000;
                if (idnpc == 26) {
                    id_cu = 306;
                    id_moi = 304;
                    chuyendoi = 10;
                    vag = quant * 25_000;
                } else if (idnpc == 27) {
                    id_cu = 304;
                    id_moi = 305;
                    chuyendoi = 5;
                    vag = quant * 500;
                }
                if (idnpc == 27 && vag > conn.p.get_ngoc()) {
                    Service.send_notice_box(conn, "Không đủ " + vag + " ngọc để đổi " + quant + " bó sen");
                    return;
                } else if (vag > conn.p.get_vang()) {
                    Service.send_notice_box(conn, "Không đủ " + vag + " vàng để đổi " + quant + " bó sen");
                    return;
                }
                if (id_cu > (ItemTemplate4.item.size() - 1) || id_cu < 0 || id_moi > (ItemTemplate4.item.size() - 1) || id_moi < 0) {
                    Service.send_notice_box(conn, "Đã xảy ra lỗi...");
                    return;
                }
                int quant_inbag = conn.p.item.total_item_by_id(4, id_cu);
                int quant_real = quant_inbag / chuyendoi;
                if (quant_real < quant) {
                    Service.send_notice_box(conn, "Chỉ có thể đổi tối đa " + quant_real + " " + ItemTemplate4.item.get(id_moi).getName());
                    return;
                }

                if (idnpc == 27) {
                    conn.p.update_ngoc(-(vag));
                } else {
                    conn.p.update_vang(-(vag), "Trừ %s vàng đổi bó sen");
                }
                Item47 itbag = new Item47();
                itbag.id = id_moi;
                itbag.quantity = (short) quant;
                itbag.category = 4;
                conn.p.item.remove(4, id_cu, quant * chuyendoi);
                conn.p.item.add_item_inventory47(4, itbag);

                Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", new short[]{id_moi}, new int[]{quant}, new short[]{4});
                break;
            }
            case 28: {
                String namep = m2.reader().readUTF();
                Player p0 = null;
                for (Player p1 : conn.p.map.players) {
                    if (p1.conn != null && p1.conn.connected && p1.name.equals(namep) && Math.abs(conn.p.x - p1.x) < 70 && Math.abs(conn.p.y - p1.y) < 70) {
                        p0 = p1;
                        break;
                    }
                }
                if (p0 == null) {
                    Service.send_notice_box(conn, "Bạn và người thả cùng cần phải đứng gần nhau");
                    break;
                }
                if (conn.p.item.get_inventory_able() < 3) {
                    Service.send_notice_box(conn, "Cần 3 ô trống trong hành trang!");
                    return;
                }
                if (conn.p.item.total_item_by_id(4, 303) > 0) {
//                    try{
                    conn.p.item.remove(4, 303, 1);
                    List<BoxItem> ids = new ArrayList<>();

                    List<Integer> it7 = new ArrayList<>(java.util.Arrays.asList(12, 13, 11, 3, 4, 8, 9, 10));
                    List<Integer> it7_vip = new ArrayList<>(java.util.Arrays.asList(14, 471, 346, 33));

                    List<Integer> it4 = new ArrayList<>(java.util.Arrays.asList(294, 275, 52, 18));
                    List<Integer> it4_vip = new ArrayList<>(java.util.Arrays.asList(206, 147, 131, 304, 306));
                    for (int i = 0; i < Util.random(1, 4); i++) {
                        int ran = Util.random(100);
                        if (ran < 0) {
                            short id = Util.random(it7, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(2, 5);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 5) {//nlmd vang tim
                            short id = (short) Util.random(126, 146);
                            short quant = (short) 1;
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 15) { // nltt
                            short id = (short) Util.random(417, 464);
                            short quant = (short) Util.random(2);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 28) {
                            short id = Util.random(it7_vip, new ArrayList<>()).shortValue();
                            short quant = (short) 1;
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 45) {
                            short id = Util.random(it4_vip, new ArrayList<>()).shortValue();
                            short quant = (short) 1;
                            ids.add(new BoxItem(id, quant, (byte) 4));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 4);
                        } else if (ran < 70) {
                            short id = Util.random(it4, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(1, 3);
                            ids.add(new BoxItem(id, quant, (byte) 4));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 4);
                        } else {
                            short id = Util.random(it7, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(1, 3);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        }
                    }
                    Event_3.add_DoiQua(conn.p.name, 1);
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
//                    }catch(Exception e){e.printStackTrace();}
                }


                if (p0.item.get_inventory_able() < 3) {
                    Service.send_notice_box(p0.conn, "Cần 3 ô trống trong hành trang để có thể nhận quà hoa đăng từ " + conn.p.name);
                    return;
                }

                List<BoxItem> ids = new ArrayList<>();

                List<Integer> it7 = new ArrayList<>(java.util.Arrays.asList(1, 2, 3));
                List<Integer> it7_vip = new ArrayList<>(java.util.Arrays.asList(12, 8, 9, 10));
                List<Integer> it4 = new ArrayList<>(java.util.Arrays.asList(48, 49, 50, 51, 18, 10));
                List<Integer> it4_vip = new ArrayList<>(java.util.Arrays.asList(205, 207, 24, 52, 275, 84));
                for (int i = 0; i < Util.random(1, 3); i++) {
                    int ran = Util.random(100);
                    if (ran < 0) {
                        short id = Util.random(it7, new ArrayList<>()).shortValue();
                        short quant = (short) Util.random(2, 5);
                        ids.add(new BoxItem(id, quant, (byte) 7));
                        p0.item.add_item_inventory47(id, quant, (byte) 7);
                    } else if (ran < 2) { // nltt
                        short id = (short) Util.random(417, 464);
                        short quant = (short) Util.random(2);
                        ids.add(new BoxItem(id, quant, (byte) 7));
                        p0.item.add_item_inventory47(id, quant, (byte) 7);
                    } else if (ran < 12) {
                        short id = Util.random(it4_vip, new ArrayList<>()).shortValue();
                        short quant = (short) 1;
                        ids.add(new BoxItem(id, quant, (byte) 4));
                        p0.item.add_item_inventory47(id, quant, (byte) 4);
                    } else if (ran < 27) {
                        short id = Util.random(it7_vip, new ArrayList<>()).shortValue();
                        short quant = (short) 1;
                        ids.add(new BoxItem(id, quant, (byte) 7));
                        p0.item.add_item_inventory47(id, quant, (byte) 7);
                    } else if (ran < 45) {
                        short id = Util.random(it4, new ArrayList<>()).shortValue();
                        short quant = (short) Util.random(1, 3);
                        ids.add(new BoxItem(id, quant, (byte) 4));
                        p0.item.add_item_inventory47(id, quant, (byte) 4);
                    } else if (ran < 70) {
                        short id = Util.random(it7, new ArrayList<>()).shortValue();
                        short quant = (short) Util.random(1, 3);
                        ids.add(new BoxItem(id, quant, (byte) 7));
                        p0.item.add_item_inventory47(id, quant, (byte) 7);
                    } else {
                        short id = (short) Util.random(new int[]{2, 5});
                        short quant = (short) Util.random(100, 300);
                        ids.add(new BoxItem(id, quant, (byte) 4));
                        p0.item.add_item_inventory47(id, quant, (byte) 4);
                    }
                }
                Service.Show_open_box_notice_item(p0, "Quà hoa đăng từ " + conn.p.name, ids);
                break;
            }
            case 29:
            case 30:
            case 31:
            case 32: {
                if (Manager.gI().event == 0) {
                    if (size != 1) {
                        return;
                    }
                    if (!EventManager.check(EventManager.registerList, conn.p.name)) {
                        return;
                    }
                    if (EventManager.notCanRegister()) {
                        Service.send_notice_box(conn, "Đang trong thời gian nấu");
                        return;
                    }
                    String value = m2.reader().readUTF();
                    if (!(Util.isnumber(value))) {
                        Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                        return;
                    }
                    int quant = Integer.parseInt(value);
                    if (quant > 500 || quant <= 0) {
                        Service.send_notice_box(conn, "Số lượng không hợp lệ!");
                        return;
                    }
                    short id = EventManager.item_drop[0][idnpc - 29];
                    if (conn.p.item.total_item_by_id(4, id) < quant && conn.ac_admin < 3) {
                        Service.send_notice_box(conn, "Không đủ " + quant + " " + ItemTemplate4.item.get(id).getName());
                        return;
                    }
                    LunarNewYear.add_material(conn.p.name, 1, idnpc - 29, quant);
                    conn.p.item.remove(4, id, quant);
                    conn.p.item.char_inventory(4);
                    Service.send_notice_box(conn, "Đã góp " + quant + " " + ItemTemplate4.item.get(id).getName());
                }
                break;
            }
            case 33: {
                if (Manager.gI().event == 0) {
                    if (size != 1) {
                        return;
                    }
                    String value = m2.reader().readUTF();
                    if (!(Util.isnumber(value))) {
                        Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                        return;
                    }
                    int quant = Integer.parseInt(value);
                    if (quant <= 0) {
                        Service.send_notice_box(conn, "Số lượng không hợp lệ!");
                        return;
                    }
                    short gao = EventManager.item_drop[0][0];
                    short dau_xanh = EventManager.item_drop[0][2];
                    short la = EventManager.item_drop[0][3];
                    if (conn.p.item.total_item_by_id(4, gao) < quant * 5 || conn.p.item.total_item_by_id(4, dau_xanh) < quant * 5
                            || conn.p.item.total_item_by_id(4, la) < quant * 5) {
                        Service.send_notice_box(conn, "Đổi bánh dày cần 5 Gạo nếp, 5 Đậu xanh, 5 Lá dong và 25.000 vàng");
                        return;
                    }
                    if (conn.p.get_vang() < quant * 25000L) {
                        Service.send_notice_box(conn, "Không đủ " + quant * 25000 + " vàng");
                        return;
                    }
                    Item47 it = new Item47();
                    it.category = 4;
                    it.id = 195;
                    it.quantity = (short) quant;
                    conn.p.item.add_item_inventory47(4, it);
                    conn.p.item.remove(4, gao, quant * 5);
                    conn.p.item.remove(4, dau_xanh, quant * 5);
                    conn.p.item.remove(4, la, quant * 5);
                    conn.p.update_vang(-quant * 25000L, "Trừ %s vàng đổi bánh dày");
                    conn.p.item.char_inventory(4);
                    Service.send_notice_box(conn, "Nhận được " + quant + " bánh dày");
                }
                break;
            }
            case 34: {
                if (Manager.gI().event == 0) {
                    if (size != 1) {
                        return;
                    }
                    String value = m2.reader().readUTF();
                    if (!(Util.isnumber(value))) {
                        Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                        return;
                    }
                    int quant = Integer.parseInt(value);
                    if (quant <= 0) {
                        Service.send_notice_box(conn, "Số lượng không hợp lệ!");
                        return;
                    }
                    if (conn.p.item.total_item_by_id(4, 184) < quant
                            || conn.p.item.total_item_by_id(4, 185) < quant * 2
                            || conn.p.item.total_item_by_id(4, 186) < quant * 2
                            || conn.p.item.total_item_by_id(4, 187) < quant * 2
                            || conn.p.item.total_item_by_id(4, 188) < quant
                            || conn.p.item.total_item_by_id(4, 189) < quant * 2
                            || conn.p.item.total_item_by_id(4, 190) < quant
                            || conn.p.item.total_item_by_id(4, 191) < quant) {
                        Service.send_notice_box(conn, "Không đủ chữ");
                        return;
                    }
                    if (conn.p.get_vang() < quant * 25000L) {
                        Service.send_notice_box(conn, "Không đủ " + quant * 25000 + " vàng");
                        return;
                    }
                    Item47 it = new Item47();
                    it.category = 4;
                    it.id = 194;
                    it.quantity = (short) quant;
                    conn.p.item.add_item_inventory47(4, it);
                    conn.p.item.remove(4, 184, quant);
                    conn.p.item.remove(4, 185, quant * 2);
                    conn.p.item.remove(4, 186, quant * 2);
                    conn.p.item.remove(4, 187, quant * 2);
                    conn.p.item.remove(4, 188, quant);
                    conn.p.item.remove(4, 189, quant * 2);
                    conn.p.item.remove(4, 190, quant);
                    conn.p.item.remove(4, 191, quant);
                    conn.p.update_vang(-quant * 25000L, "Trừ %s vàng đổi hộp quà thọ");
                    conn.p.item.char_inventory(4);
                    Service.send_notice_box(conn, "Nhận được " + quant + " Hộp quà thọ");
                }
                break;
            }
            case 35: {
                try {
                    String value = m2.reader().readUTF();
                    if (!(Util.isnumber(value))) {
                        Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                        return;
                    }
                    int token = Integer.parseInt(value);
                    if (token <= 0 || token > 1000) {
                        Service.send_notice_box(conn, "Chỉ có thể mua tối thiểu là 1 và tối đa là 1000");
                        return;
                    }
                    if (conn.p.getCoin() < token * conn.p.getGiaToken()) {
                        Service.send_notice_box(conn, "không đủ coin");
                        return;
                    }
                    if (conn.p.getTongToken() < token){
                        Service.send_notice_box(conn, "Hết token sẵn");
                        return;
                    }
                    if (conn.p.getTongNapByID() > 999999999){
                        Service.send_notice_box(conn,"Pool nạp trong bể đã đến giới hạn không thể mua token");
                        return;
                    }
                    int gia_token = (int) conn.p.getGiaToken();
                    int tong_coin = token * gia_token;
                    conn.p.Mua_TongNapBYID(tong_coin);
                    conn.p.update_coin(-tong_coin);
                    conn.p.mua_token(token);
                    conn.p.history_coin(-tong_coin,"(TRỪ COIN) mua token");
//                    His_COIN hisc = new His_COIN(conn.user ,conn.p.name);
//                    hisc.coin_change = tong_coin;
//                    hisc.coin_last = conn.p.getCoin();
//                    hisc.Logger = "(TRỪ COIN) mua token";
//                    hisc.Flus();
                    Service.send_notice_box(conn, "Mua thành công " + token + " token");
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case 36: {
                String value = m2.reader().readUTF();
                if (!(Util.isnumber(value))) {
                    Service.send_notice_box(conn, "Dữ liệu nhập không phải số!!");
                    return;
                }
                int token = Integer.parseInt(value);

                // Kiểm tra giới hạn số lượng
                if (token <= 0 || token > 1000) {
                    Service.send_notice_box(conn, "Chỉ có thể bán tối thiểu là 1 và tối đa là 1000");
                    return;
                }
                // Kiểm tra số dư token
                if (conn.p.getToken() < token) {
                    Service.send_notice_box(conn, "Không đủ token");
                    return;
                }
                if (conn.p.getTongNapByID() <= 0){
                    Service.send_notice_box(conn,"Pool nạp trong bể đã chạm đáy");
                    return;
                }

                // --- BẮT ĐẦU TÍNH TOÁN ---
                int gia_token = (int) conn.p.getGiaToken();
                int tong_coin = token * gia_token;
                int thue = (int) (tong_coin * 5 / 100);
                int thuc_nhan = tong_coin - thue;
                if (conn.p.getTongNapByID() < tong_coin){
                    Service.send_notice_box(conn,"Pool nạp trong bể đã hết không thể bán token");
                    return;
                }
                conn.p.Ban_TongNapBYID(thuc_nhan);
                conn.p.update_coin(thuc_nhan);
                conn.p.ban_token(token);
                conn.p.history_coin(thuc_nhan,"(CỘNG COIN) Bán token");
//                His_COIN hisc = new His_COIN(conn.user ,conn.p.name);
//                hisc.coin_change = thuc_nhan;
//                hisc.coin_last = conn.p.getCoin();
//                hisc.Logger = "(CỘNG COIN) Bán token";
//                hisc.Flus();
                // Thông báo rõ ràng cho người chơi
                Service.send_notice_box(conn, "Bán thành công " + token + " token.\n" +
                        "Tổng: " + tong_coin + " coin\n" +
                        "Thuế (5%): -" + thue + " coin\n" +
                        "Thực nhận: " + thuc_nhan + " coin");
                break;
            }
            case 38: {
                // Sửa size từ 2 thành 1 vì chỉ còn 1 ô nhập
                if (size != 1) {
                    return;
                }
                String value2 = m2.reader().readUTF();
                Player p0 = Map.get_player_by_name(value2);
                if (p0 != null) {
                    if (p0.item.wear[23] != null) {
                        Service.send_notice_box(conn, "Đối phương đã kết đôi với người khác!");
                        return;
                    }
                    if (p0.level < 60) {
                        Service.send_notice_box(conn, "Yêu cầu level trên 60");
                        return;
                    }
                    if (p0.getCoin() < 100_000){
                        Service.send_notice_box(conn,"đối phương Không đủ coin");
                        return;
                    }
                    conn.p.item.char_inventory(5);
                    p0.in4_wedding = new String[]{"" + 4, conn.p.name};
                    Service.send_box_input_yesno(p0.conn, 110, conn.p.name + " muốn cầu hôn bạn, đồng ý lấy mình nhé?");
                } else {
                    Service.send_notice_box(conn, "Không tìm thấy đối phương!");
                }
                break;
            }
            case 39: {
                if (!conn.p.checkIsTop1Nap()) {
                    return;
                }
                try {
                    // 2. Đọc dữ liệu từ 4 ô nhập
                    // Client sẽ gửi lên lần lượt theo thứ tự mảng String[] lúc gọi
                    if (size != 4) {
                        Service.send_notice_box(conn, "Lỗi dữ liệu (Thiếu dòng nhập).");
                        return;
                    }
                    String nameCode = m2.reader().readUTF().trim(); // Ô 1: Tên
                    String vangStr  = m2.reader().readUTF().trim(); // Ô 2: Vàng
                    String ngocStr  = m2.reader().readUTF().trim(); // Ô 3: Ngọc
                    String limitStr = m2.reader().readUTF().trim(); // Ô 4: Số lượng

                    // 3. Kiểm tra định dạng số (Vàng, Ngọc, Limit phải là số)
                    if (!Util.isnumber(vangStr) || !Util.isnumber(ngocStr) || !Util.isnumber(limitStr)) {
                        Service.send_notice_box(conn, "Vàng, Ngọc và Số lượng phải là số nguyên!");
                        return;
                    }
                    // 4. Parse sang số
                    long vang = Long.parseLong(vangStr);
                    int ngoc  = Integer.parseInt(ngocStr);
                    int limit = Integer.parseInt(limitStr);
                    // 5. Gọi hàm logic (Hàm này bạn đã thêm ở bài trước vào Manager)
                    GiftcodeDAO.taoGiftcodeTop1(conn.p, vang, ngoc, limit, nameCode);
                } catch (Exception e) {
                    e.printStackTrace();
                    Service.send_notice_box(conn, "Lỗi nhập liệu: " + e.getMessage());
                }
                break;
            }
            case 40: { // ID 888: Xử lý Giảm Giá Server (Top 1)
                // 1. Bảo mật: Check lại Top 1 lần nữa
                if (!conn.p.checkIsTop1Nap()) {
                    return;
                }

                try {
                    // 2. Đọc dữ liệu từ 2 dòng nhập
                    // Dòng 1: % Giảm giá
                    // Dòng 2: Thời gian (phút)
                    if (size != 2) {
                        Service.send_notice_box(conn, "Lỗi dữ liệu nhập.");
                        return;
                    }

                    String phanTramStr = m2.reader().readUTF().trim();
                    String thoiGianStr = m2.reader().readUTF().trim();

                    // 3. Kiểm tra số nguyên
                    if (!Util.isnumber(phanTramStr) || !Util.isnumber(thoiGianStr)) {
                        Service.send_notice_box(conn, "Vui lòng chỉ nhập số!");
                        return;
                    }

                    int phanTram = Integer.parseInt(phanTramStr);
                    int thoiGian = Integer.parseInt(thoiGianStr);

                    // 4. Validate giới hạn (Tránh nhập bậy phá server)
                    if (phanTram <= 0 || phanTram > 5) {
                        Service.send_notice_box(conn, "Chỉ được giảm từ 1% đến 5% thôi đại gia ơi!");
                        return;
                    }
                    if (thoiGian <= 0 || thoiGian > 1440) { // Max 24h
                        Service.send_notice_box(conn, "Thời gian tối thiểu 1 phút, tối đa 1440 phút (24h).");
                        return;
                    }

                    // 5. KÍCH HOẠT (Lưu vào Manager)
                    Manager.gI().percent_discount = phanTram;
                    Manager.gI().time_discount_server = System.currentTimeMillis() + (thoiGian * 60000L); // Đổi phút ra mili giây

                    // 6. Thông báo
                    Service.send_notice_box(conn, "Đã kích hoạt Giảm Giá " + phanTram + "% trong " + thoiGian + " phút!");

                    // Chat thế giới cho cả server biết ơn
                    String msgKTG = "[💲ĐẠI GIA] " + conn.p.name.toUpperCase() + " chơi lớn!\n"
                            + "🔥 Kích hoạt GIẢM GIÁ SHOP: -" + phanTram + "%\n"
                            + "⏳ Thời gian hiệu lực: " + thoiGian + " phút.\n"
                            + "Mau vào Shop mua đồ đi anh em ơi!";
                    Manager.gI().chatKTGprocess(msgKTG);

                } catch (Exception e) {
                    e.printStackTrace();
                    Service.send_notice_box(conn, "Lỗi định dạng: " + e.getMessage());
                }
                break;
            }
        }
    }
}