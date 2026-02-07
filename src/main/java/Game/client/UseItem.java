package Game.client;

import Game.activities.Maze;
import Game.core.Util;
import Game.map.MapService;
import Game.core.Manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import Game.template.Item47; // hoặc đúng package chứa class đó
import Game.io.Session; // Giả sử Session nằm trong package Game.io
import Game.io.Message; // Giả sử Message nằm trong package Game.io
import Game.client.Player; // Giả sử Player nằm trong package Game.Player hoặc Game.model

import java.io.IOException;

import Game.core.Service;
import Game.event.LunarNewYear;
import Game.io.Message;
import Game.io.Session;
import Game.map.LeaveItemMap;
import Game.map.Vgo;
import Game.map.Map;
import Game.map.Mob_in_map;
import Game.template.*;
import Game.template.EffectService;
import Game.event.Topmc;

public class UseItem {

    public static void ProcessItem4(Session conn, Message m2) throws IOException {
        short id = m2.reader().readShort();
        if (conn.p.item.total_item_by_id(4, id) > 0) {
            switch (ItemTemplate4.item.get(id).getType()) {
                case 44: {
                    if (conn.p.id_horse != -1) {
                        conn.p.id_horse = -1;
                        MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                        Service.send_char_main_in4(conn.p);
                    }
                    use_item_mount(conn, id);
                    break;
                }
//                case 52: {
//                    use_special_item_type_53(conn, id);
//                    break;
//                }
                default: {
                    use_item4_default(conn, id);
                    break;
                }


            }
            conn.p.item.char_inventory(4);
            conn.p.item.char_inventory(7);
            conn.p.item.char_inventory(3);
        } else if (conn.p.myclan != null && conn.p.myclan.check_id(id)) {
            if (ItemTemplate4.item.get(id).getType() == 44) {
                if (conn.p.id_horse != -1) {
                    conn.p.id_horse = -1;
                    MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                    Service.send_char_main_in4(conn.p);
                }
                use_item_mount(conn, id);
            } else {
                use_item4_default(conn, id);
                conn.p.item.char_inventory(4);
                conn.p.item.char_inventory(7);
                conn.p.item.char_inventory(3);
            }
        }


    }

    public static void use_lantern_setup(Session conn, short itemId) throws IOException {
        Player p = conn.p;
        if (p.id_eff_lantern > 0) {
            // Gửi thời gian = 0 để Client tự xóa hiệu ứng này
            p.send_eff_111(p.id_eff_lantern, 0);
        }
        // 1. CẤU HÌNH THỜI GIAN (Tính bằng GIÂY)
        int duration = 24 * 60 * 60; // Mặc định 24 giờ

        // Nhóm Vĩnh Viễn
        if (itemId == 263 || itemId == 264 || itemId == 265 || itemId == 266 || itemId == 267) {
            duration = 2000000000; // ~63 năm (Coi như vĩnh viễn)
        }
        // Nhóm 30 Ngày
        else if ((itemId >= 208 && itemId <= 214) || (itemId >= 355 && itemId <= 357)) {
            duration = 30 * 24 * 60 * 60;
        }
        // Nhóm 7 Ngày (Lồng đèn bươm bướm VIP 7 ngày - ID 260)
        else if (itemId == 260) {
            duration = 7 * 24 * 60 * 60;
        }
        // Nhóm 10 Tiếng (Lồng đèn đỏ - ID 308)
        else if (itemId == 308) {
            duration = 10 * 60 * 60;
        }

        // 2. CẤU HÌNH HIỆU ỨNG HÌNH ẢNH (Eff ID)
        int effId = 123; // Mặc định bóng vàng
        switch (itemId) {
            // Lồng đèn vàng (Exp)
            case 144: case 208: case 263: effId = 72; break;
            // Hoa sen (Rớt vàng)
            case 173: case 210: case 264: effId = 81; break; // (Thay ID eff hoa sen nếu có)
            // Cá chép (Phòng thủ)
            case 174: case 211: case 265: effId = 140; break; // Eff cá chép/cá koi
            // Bươm bướm (Sát thương/VIP)
            case 175: case 212: case 266: case 214: case 260: effId = 114; break;
            // Trăng khuyết (HP)
            case 176: case 213: case 267: effId = 141; break; // (Thay ID eff trăng nếu có)
            // Cá Koi, Thỏ, Lân (Event)
            case 355: effId = 145; break;
            case 356: effId = 147; break;
            case 357: effId = 148; break;
            // Lồng đèn đỏ (10h)
            case 308: effId = 150; break; // Bóng halloween?
            default: effId = 126; break;
        }
        // 4. LƯU DỮ LIỆU VÀ KÍCH HOẠT
        p.id_lantern = itemId;      // Lưu ID item để tính chỉ số (Exp, HP...)
        p.id_eff_lantern = effId;   // Lưu ID hiệu ứng để hiển thị
        p.send_eff_111(effId, duration * 1000);
        p.updatePoint();
        if (p.hp > p.maxhp) p.hp = p.maxhp;
        Service.send_char_main_in4(p);
        // Thông báo
        String timeStr = (duration > 864000) ? "Vĩnh viễn" : (duration / 3600) + " giờ";
        Service.send_notice_box(conn, "✅ Đã sử dụng " + ItemTemplate4.item.get(itemId).getName()
                + "\nThời gian: " + timeStr);
    }
    public static void use_bubble_setup(Session conn, short itemId) throws IOException {
        Player p = conn.p;
        // 1. CẤU HÌNH THỜI GIAN & HIỆU ỨNG
        int duration = 24 * 60 * 60; // Mặc định 24h
        int effId = 114; // Mặc định

        // Cấu hình theo ID Bong bóng
        switch (itemId) {
            case 309: // Galaxy
                effId = 121; // (Chọn ID eff phù hợp)
                duration = 24 * 60 * 60; // 24h
                break;
            case 310: // Đỏ
                effId = 122;
                duration = 24 * 60 * 60;
                break;
            case 311: // Vàng
                effId = 123;
                duration = 24 * 60 * 60;
                break;
            case 312: // Hồng
                effId = 136;
                duration = 24 * 60 * 60;
                break;
            case 358: // Halowin VIP
                effId = 151;
                duration = 30 * 24 * 60 * 60; // 30 Ngày
                break;
        }
        // 3. LƯU DỮ LIỆU (Vào biến bubble riêng)
        p.id_bubble = itemId;
        p.id_eff_bubble = effId;
        p.send_eff_111(effId, duration * 1000);
        p.updatePoint();
        if (p.hp > p.maxhp) p.hp = p.maxhp;
        Service.send_char_main_in4(p);
        Service.send_notice_box(conn, "✅ Đã sử dụng " + ItemTemplate4.item.get(itemId).getName());
    }


    private static void use_item4_default(Session conn, short id_potion) throws IOException {

        switch (id_potion) {
            case 309: case 310: case 311: case 312: case 358:
            {
                use_bubble_setup(conn, id_potion); // Gọi hàm Bong bóng riêng
                break;
            }
            // Nhóm 24 giờ
            case 144: case 173: case 174: case 175: case 176:
                // Nhóm 30 ngày
            case 208: case 210: case 211: case 212: case 213: case 214:
                // Nhóm Vĩnh viễn
            case 263: case 264: case 265: case 266: case 267:
                // Nhóm đặc biệt khác (7 ngày, 10 tiếng, Event)
            case 260: case 308: case 355: case 356: case 357: {
                use_lantern_setup(conn, id_potion);
                break;
            }
            case 57:
            case 58:
            case 59:
            case 60: {
                if (Map.is_map_chien_truong(conn.p.map.map_id)) {
                    MapService.use_item_arena(conn.p.map, conn.p, id_potion);
                }
                break;
            }
            case 126: {
                if (Manager.gI().event == 0) {
                    LunarNewYear.ban_phao(conn);
                } else {
                    Service.send_notice_box(conn, "Chưa đến sự kiện cần dùng");
                }
                break;
            }
            case 84: {
                if (conn.p.map.zone_id != 5) {
                    Service.send_notice_box(conn, "Chỉ dùng được trong khu đi buôn");
                    return;
                }
                if (!conn.p.isTrader()) {
                    Service.send_notice_box(conn, "Chỉ dùng được khi là thương nhân ");
                    return;
                }
                if (conn.p.pet_di_buon == null) {
                    conn.p.pet_di_buon = new Pet_di_buon(131, Manager.gI().get_index_mob_new(), conn.p.x, conn.p.y,
                            conn.p.map.map_id, conn.p.name, conn.p);
                    Pet_di_buon_manager.add(conn.p.name, conn.p.pet_di_buon);
                    //
                    Message m22 = new Message(4);
                    m22.writer().writeByte(1);
                    m22.writer().writeShort(131);
                    m22.writer().writeShort(conn.p.pet_di_buon.ID);
                    m22.writer().writeShort(conn.p.pet_di_buon.x);
                    m22.writer().writeShort(conn.p.pet_di_buon.y);
                    m22.writer().writeByte(-1);
                    conn.addmsg(m22);
                    m22.cleanup();
                    //
                    conn.p.item.remove(4, id_potion, 1);
                } else {
                    Service.send_notice_box(conn,
                            "Vật đi buôn của bạn đang ở\nVị trí:\n" + Map.get_map_by_id(conn.p.pet_di_buon.id_map)[0].name + "\n"
                                    + conn.p.pet_di_buon.x + " " + conn.p.pet_di_buon.y);
                }
                break;
            }
            case 86: {
                if (conn.p.map.zone_id != 5) {
                    Service.send_notice_box(conn, "Chỉ dùng được trong khu đi buôn");
                    return;
                }
                if (!conn.p.isRobber()) {
                    Service.send_notice_box(conn, "Chỉ dùng được khi là cướp ");
                    return;
                }
                if (conn.p.pet_di_buon == null) {
                    conn.p.pet_di_buon = new Pet_di_buon(132, Manager.gI().get_index_mob_new(), conn.p.x, conn.p.y,
                            conn.p.map.map_id, conn.p.name, conn.p);
                    Pet_di_buon_manager.add(conn.p.name, conn.p.pet_di_buon);
                    //
                    Message m22 = new Message(4);
                    m22.writer().writeByte(1);
                    m22.writer().writeShort(132);
                    m22.writer().writeShort(conn.p.pet_di_buon.ID);
                    m22.writer().writeShort(conn.p.pet_di_buon.x);
                    m22.writer().writeShort(conn.p.pet_di_buon.y);
                    m22.writer().writeByte(-1);
                    conn.addmsg(m22);
                    m22.cleanup();
                    //
                    conn.p.item.remove(4, id_potion, 1);
                } else {
                    Service.send_notice_box(conn,
                            "Vật đi buôn của bạn đang ở\nVị trí:\n" + Map.get_map_by_id(conn.p.pet_di_buon.id_map)[0].name + "\n"
                                    + conn.p.pet_di_buon.x + " " + conn.p.pet_di_buon.y);
                }
                break;
            }
            case 0:
            case 1:
            case 25:
            case 2: {
                EffTemplate vet_thuong_sau = conn.p.get_EffDefault(StrucEff.VET_THUONG_SAU);
                EffTemplate te_cong = conn.p.get_EffDefault(StrucEff.TE_CONG);
                if (conn.p.time_use_poition_hp < System.currentTimeMillis() && te_cong == null && vet_thuong_sau == null) {
                    conn.p.time_use_poition_hp = System.currentTimeMillis() + 2000L;
                    conn.p.item.remove(4, id_potion, 1);
                    int param = ItemTemplate4.item.get(id_potion).getValue();
                    param += ((param * 5 * conn.p.body.get_skill_point(9)) / 100);
                    Service.usepotion(conn.p, 0, param);
                }
                break;
            }
            case 3:
            case 4:
            case 5: {
                if (conn.p.time_use_poition_mp < System.currentTimeMillis()) {
                    conn.p.time_use_poition_mp = System.currentTimeMillis() + 2000L;
                    conn.p.item.remove(4, id_potion, 1);
                    int param = ItemTemplate4.item.get(id_potion).getValue();
                    param += ((param * 5 * conn.p.body.get_skill_point(10)) / 100);
                    Service.usepotion(conn.p, 1, param);
                }
                break;
            }
            case 6: {
                Service.send_box_input_yesno(conn, 125, "Bạn muốn tẩy tiềm năng?");
                break;
            }
            case 7: {
                Service.send_box_input_yesno(conn, 124, "Bạn muốn tẩy kỹ năng?");
                break;
            }
            case 10: {
                conn.p.item.remove(4, id_potion, 1);
                EffTemplate ef = conn.p.get_EffDefault(-125);
                if (ef != null) {
                    long time_extra = (ef.time - System.currentTimeMillis()) + (1000 * 60 * 120 - 1);
                    if (time_extra > (1000 * 60 * 60 * 24 * 3 - 1)) {
                        time_extra = 1000 * 60 * 60 * 24 * 3 - 1;
                    }
                    conn.p.add_EffDefault(-125, 5000, (int) time_extra);
                } else {
                    conn.p.add_EffDefault(-125, 5000, (1000 * 60 * 120 - 1));
                }
                conn.p.set_x2_xp(1);
                break;
            }
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16: {
                if (conn.p.item.get_inventory_able() < 1) {
                    Service.send_notice_nobox_white(conn, "Hành trang đầy!");
                    return;
                }
                Item47 it = new Item47();
                it.id = (short) (id_potion - 11);
                it.quantity = ItemTemplate4.item.get(id_potion).getValue();
                conn.p.item.add_item_inventory47(4, it);
                conn.p.item.remove(4, id_potion, 1);
                break;
            }
            case 225: {
                try {
                    AutoAttackService.toggleAutoAttack(conn);
                    if (conn.isAutoAttackOn()) {
                        Service.send_notice_nobox_white(conn, "Đã BẬT chế độ Auto Attack.");
                    } else {
                        Service.send_notice_nobox_white(conn, "Đã TẮT chế độ Auto Attack.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Service.send_notice_nobox_white(conn, "Lỗi bật/tắt Auto Attack.");
                }
                break;
            }
            case 101: {
                //TopItem101.log(conn.p.name);
                conn.p.kimcuong += 1; // Cộng 1 ngọc
                conn.p.item.remove(4, id_potion, 1);
                Service.send_notice_box(conn, "Bạn nhận được 1 ngọc");
                break;
            }
            case 19: {
                if (conn.p.myclan != null && conn.p.myclan.get_mem_type(conn.p.name) == 127
                        && conn.p.myclan.check_id((short) 19)) {
                    EffTemplate ef = conn.p.myclan.getEffect(Clan.TIME_SACH);
                    if (ef != null) {
                        ef.time += 1000 * 60 * 30 - 1;
                        Service.chat_clan(conn.p.myclan, "Thời gian sách kinh nghiệm còn lại là " + (ef.time - System.currentTimeMillis()) / 60000L + " phút");
                    } else {
                        long time_extra = System.currentTimeMillis() + 1000 * 60 * 30 - 1;
                        conn.p.myclan.time_items.add(new EffTemplate(Clan.TIME_SACH, 0, time_extra));
                        Service.chat_clan(conn.p.myclan, "Thời gian sách kinh nghiệm còn lại là 30 phút");
                    }
                    conn.p.myclan.remove_item(19);
                    conn.p.myclan.open_box_clan(conn);
                }
                break;
            }
            case 20: {
                if (conn.p.myclan != null && conn.p.myclan.get_mem_type(conn.p.name) == 127
                        && conn.p.myclan.check_id((short) 20)) {
                    EffTemplate ef = conn.p.myclan.getEffect(Clan.TIME_KINH_LUP);
                    if (ef != null) {
                        ef.time += 1000 * 60 * 30 - 1;
                        Service.chat_clan(conn.p.myclan, "Thời gian kính lúp còn lại là " + (ef.time - System.currentTimeMillis()) / 60000L + " phút");
                    } else {
                        long time_extra = System.currentTimeMillis() + 1000 * 60 * 30 - 1;
                        conn.p.myclan.time_items.add(new EffTemplate(Clan.TIME_SACH, 0, time_extra));
                        Service.chat_clan(conn.p.myclan, "Thời gian kính lúp còn lại là 30 phút");
                    }
                    conn.p.myclan.remove_item(20);
                    conn.p.myclan.open_box_clan(conn);
                }
                break;
            }
            case 24: {
                if (conn.p.hieuchien == 0) {
                    Service.send_notice_box(conn, "Bạn chưa có điểm pk");
                    return;
                }
                conn.p.item.remove(4, id_potion, 1);
                conn.p.hieuchien -= 1000;
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 26: {
                if (conn.p.item.get_inventory_able() < 1) {
                    Service.send_notice_nobox_white(conn, "Hành trang đầy!");
                    return;
                }
                Item47 it = new Item47();
                it.id = (short) (id_potion - 1);
                it.quantity = ItemTemplate4.item.get(id_potion).getValue();
                conn.p.item.add_item_inventory47(4, it);
                conn.p.item.remove(4, id_potion, 1);
                break;
            }
            case 67: {
                if (conn.p.myclan == null) {
                    Service.send_notice_nobox_white(conn, "Item dành cho người có bang hội");
                    return;
                }
                if (conn.p.map.mobs.length > 0) {
                    if (conn.p.map.luathieng == null) {
                        LuaThieng lua = new LuaThieng(conn.p.map, conn.p);
                        conn.p.map.luathieng = lua;
                        conn.p.item.remove(4, id_potion, 1);

                        for (Player p : conn.p.map.players) {
                            lua.send_move(p);
                            lua.sendInfo(p);
                        }
                    } else {
                        Service.send_notice_nobox_white(conn, "Map đã có lửa không thể đốt thêm");
                    }
                } else {
                    Service.send_notice_nobox_white(conn, "Map này không thể đốt lửa");
                }
                break;
            }
            case 68: {
                if (conn.p.map.luathieng != null
                        && conn.p.myclan != null
                        && conn.p.map.luathieng.clan.ID == conn.p.myclan.ID
                        && conn.p.map.luathieng.binhdau < 4
                        && conn.p.map.luathieng.isPointInCircle(conn.p.x, conn.p.y)) {
                    conn.p.map.luathieng.binhdau++;
                    for (Player p : conn.p.map.players) {
                        conn.p.map.luathieng.sendInfo(p);
                    }
                    conn.p.item.remove(4, id_potion, 1);
                }
                break;
            }
            case 69: {
                if (conn.p.pet_follow_id == -1) {
                    return;
                }
                Pet pet = conn.p.get_pet_follow();
                if (pet != null) {
                    if (pet.can_revolution()) {
                        pet.UpgradeLevel();
                        pet.exp = 0;
                        pet.icon++;
                        Service.send_wear(conn.p);
                        conn.p.item.remove(4, id_potion, 1);
                    } else if (pet.level >= 29) {
                        Service.send_notice_box(conn, "Thú nuôi đã đạt cấp tối đa");
                    } else {
                        Service.send_notice_nobox_white(conn, "Thú nuôi chưa đủ cấp độ");
                    }
                }
                break;
            }
            case 142: {
                if (conn.p.isKnight() || conn.p.isRobber() || conn.p.isTrader()) {
                    Service.send_notice_nobox_white(conn, "Không thể sử dụng");
                    return; // dừng, không dùng được
                }
                Vgo vgo = null;
                vgo = new Vgo();
                vgo.id_map_go = 1;
                vgo.x_new = 492;
                vgo.y_new = 366;
                conn.p.change_map(conn.p, vgo);
                conn.p.item.remove(4, id_potion, 1);
                break;
            }
            case 164: {
                Service.send_notice_nobox_white(conn, "Không thể sử dụng");
                if (conn.p.isKnight() || conn.p.isRobber() || conn.p.isTrader()) {
                    //   Service.sendAlert(conn, "Bạn không thể sử dụng vật phẩm này khi đang mặc trang bị này!");
                    break; // dừng, không dùng được
                }

                // Nếu không mặc item bị cấm, thực hiện như bình thường
                Vgo vgo = new Vgo();
                vgo.id_map_go = 88;
                vgo.x_new = 456;
                vgo.y_new = 360;
                conn.p.change_map(conn.p, vgo);
                conn.p.item.remove(4, id_potion, 1);
                break;
            }


            case 166: {
                if (conn.p.isKnight() || conn.p.isRobber() || conn.p.isTrader()) {
                    Service.send_notice_nobox_white(conn, "Không thể sử dụng");
                    break; // dừng, không dùng được
                }
                Vgo vgo = null;
                vgo = new Vgo();
                vgo.id_map_go = 89;
                vgo.x_new = 456;
                vgo.y_new = 360;
                conn.p.change_map(conn.p, vgo);
                conn.p.item.remove(4, id_potion, 1);
                break;
            }
            case 168: {
                if (conn.p.isKnight() || conn.p.isRobber() || conn.p.isTrader()) {
                    Service.send_notice_nobox_white(conn, "Không thể sử dụng");
                    break; // dừng, không dùng được
                }
                Vgo vgo = null;
                vgo = new Vgo();
                vgo.id_map_go = 90;
                vgo.x_new = 456;
                vgo.y_new = 360;
                conn.p.change_map(conn.p, vgo);
                conn.p.item.remove(4, id_potion, 1);
                break;
            }
            case 170: {
                if (conn.p.isKnight() || conn.p.isRobber() || conn.p.isTrader()) {
                    Service.send_notice_nobox_white(conn, "Không thể sử dụng");
                    break; // dừng, không dùng được
                }
                Vgo vgo = null;
                vgo = new Vgo();
                vgo.id_map_go = 91;
                vgo.x_new = 456;
                vgo.y_new = 360;
                conn.p.change_map(conn.p, vgo);
                conn.p.item.remove(4, id_potion, 1);
                break;
            }
            case 162: {
                int exp_add = Util.random(conn.p.level * 100);
                conn.p.update_Exp(exp_add, false);
                int quant_item = Util.random(2, 5);
                short[] item_add
                        = new short[]{13, 5, 25, 8, 9, 10, 11, 18, 131, 132, 133, 33, 44, 48, 49, 50, 51, 205, 206, 207, 142};
                byte[] item_type = new byte[]{7, 4, 4, 7, 7, 7, 7, 4, 4, 4, 4, 7, 7, 4, 4, 4, 4, 4, 4, 4, 4};
                //
                Message m = new Message(78);
                m.writer().writeUTF("Bạn nhận được: " + exp_add + " exp");
                m.writer().writeByte(quant_item); // size
                for (int i = 0; i < quant_item; i++) {
                    int index = Util.random(item_add.length);
                    m.writer().writeUTF(""); // name
                    m.writer().writeShort((item_type[index] == 4) ? ItemTemplate4.item.get(item_add[index]).getIcon()
                            : ItemTemplate7.item.get(item_add[index]).getIcon()); // icon
                    int quant_ = Util.random(1, 3);
                    m.writer().writeInt(quant_); // quantity
                    m.writer().writeByte(item_type[index]); // type in bag
                    m.writer().writeByte(0); // tier
                    m.writer().writeByte(0); // color
                    Item47 itbag = new Item47();
                    itbag.id = item_add[index];
                    itbag.quantity = (short) quant_;
                    itbag.category = item_type[index];
                    conn.p.item.add_item_inventory47(item_type[index], itbag);
                }
                m.writer().writeUTF("");
                m.writer().writeByte(1);
                m.writer().writeByte(0);
                conn.addmsg(m);
                m.cleanup();
                //
                conn.p.item.remove(4, id_potion, 1);
                break;
            }
            case 194: {
                if (conn.p.item.get_inventory_able() < 3) {
                    Service.send_notice_nobox_white(conn, "Hành trang đầy!");
                    return;
                }
                List<BoxItem> ids = new ArrayList<>();
                List<Integer> it7 = new ArrayList<>(java.util.Arrays.asList(2, 3, 33, 13, 2, 3, 33, 13, 14, 349, 2, 3, 33, 13, 2, 3, 33, 13, 2, 3, 33, 13, 2, 3, 33, 13, 2, 3, 33, 13, 2, 3, 33, 13));
                for (int i = 417; i < 457; i++) {
                    it7.add(i);
                }
                List<Integer> it4 = new ArrayList<>(java.util.Arrays.asList(2, 25, 48, 49, 50, 51, 52, 67, 68, 10, 24, 2, 25, 48, 49, 50, 51, 52, 2, 25, 48, 49, 50, 51, 52));
                for (int i = 0; i < Util.random(1, 4); i++) {
                    int ran = Util.random(100);
                    if (ran < 60) {
                        short id = Util.random(it4, new ArrayList<>()).shortValue();
                        short quant = (short) Util.random(1, 4);
                        ids.add(new BoxItem(id, quant, (byte) 4));
                        conn.p.item.add_item_inventory47(id, quant, (byte) 4);
                    } else {
                        short id = Util.random(it7, new ArrayList<>()).shortValue();
                        short quant = 1;
                        ids.add(new BoxItem(id, quant, (byte) 7));
                        conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                    }
                }
                if (Util.nextInt(1000) < 3) {
                    List<Integer> it3 = new ArrayList<>(java.util.Arrays.asList(2940, 2941));
                    short id3 = Util.random(it3, new ArrayList<>()).shortValue();
                    ItemTemplate3 temp3 = ItemTemplate3.item.get(id3);
                    Item3 it = new Item3();
                    it.id = temp3.getId();
                    it.name = temp3.getName();
                    it.clazz = temp3.getClazz();
                    it.type = temp3.getType();
                    it.level = temp3.getLevel();
                    it.icon = temp3.getIcon();
                    it.op = temp3.getOp();
                    it.color = 5;
                    it.part = temp3.getPart();
                    if (Util.random(100) != 55) {
                        if (it.type == 14) {
                            it.time_use = 7 * 24 * 60 * 60 * 1000L;
                        } else {
                            it.expiry_date = 7 * 24 * 60 * 60 * 1000L + System.currentTimeMillis();
                        }
                    }
                    conn.p.item.add_item_inventory3(it);
                    ids.add(new BoxItem(id3, (short) 1, (byte) 3));
                }
                Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                conn.p.item.remove(4, id_potion, 1);
                conn.p.item.char_inventory(4);
                conn.p.item.char_inventory(7);
                break;
            }
            case 207: // ruong tim
            case 205: { // ruong do
                if (conn.p.item.get_inventory_able() < 1) {
                    Service.send_notice_nobox_white(conn, "Hành trang đầy!");
                    return;
                }
                List<Short> it_ = new ArrayList<>();
                if (conn.p.level < 30) {
                    it_.addAll(LeaveItemMap.item2x);
                } else if (conn.p.level < 40) {
                    it_.addAll(LeaveItemMap.item3x);
                } else if (conn.p.level < 50) {
                    it_.addAll(LeaveItemMap.item4x);
                } else if (conn.p.level < 60) {
                    it_.addAll(LeaveItemMap.item5x);
                } else if (conn.p.level < 70) {
                    it_.addAll(LeaveItemMap.item6x);
                } else if (conn.p.level < 80) {
                    it_.addAll(LeaveItemMap.item7x);
                } else if (conn.p.level < 90) {
                    it_.addAll(LeaveItemMap.item8x);
                } else if (conn.p.level < 100) {
                    it_.addAll(LeaveItemMap.item9x);
                } else if (conn.p.level < 110) {
                    it_.addAll(LeaveItemMap.item10x);
                } else if (conn.p.level < 120) {
                    it_.addAll(LeaveItemMap.item11x);
                } else if (conn.p.level < 130) {
                    it_.addAll(LeaveItemMap.item12x);
                } else if (conn.p.level < 140) {
                    it_.addAll(LeaveItemMap.item13x);
                }
                if (it_.size() < 1) {
                    Service.send_notice_box(conn, "rương rỗng!");
                    conn.p.item.remove(4, id_potion, 1);
                    return;
                }
                int id_item_can_drop = it_.get(Util.random(it_.size()));
                int dem = 0;
                if (id_potion == 207) {
                    while (dem < 50 && it_.size() > 2 && conn.p.level > 0
                            && !(ItemTemplate3.item.get(id_item_can_drop).getType() == 3)
                            && !(ItemTemplate3.item.get(id_item_can_drop).getType() == 4)
                            && !(ItemTemplate3.item.get(id_item_can_drop).getType() == 5)
                            && !(ItemTemplate3.item.get(id_item_can_drop).getType() == 6)) {
                        id_item_can_drop = it_.get(Util.random(it_.size()));
                        dem++;
                    }
                } else {
                    while (dem < 50 && it_.size() > 2 && conn.p.level > 0
                            && !(ItemTemplate3.item.get(id_item_can_drop).getType() == 2
                            && ItemTemplate3.item.get(id_item_can_drop).getClazz() == conn.p.clazz)
                            && !(ItemTemplate3.item.get(id_item_can_drop).getType() == 0
                            && ItemTemplate3.item.get(id_item_can_drop).getClazz() == conn.p.clazz)
                            && !(ItemTemplate3.item.get(id_item_can_drop).getType() == 1
                            && ItemTemplate3.item.get(id_item_can_drop).getClazz() == conn.p.clazz)
                            && !(ItemTemplate3.item.get(id_item_can_drop).getType() == 8
                            && ItemTemplate3.item.get(id_item_can_drop).getClazz() == conn.p.clazz)
                            && !(ItemTemplate3.item.get(id_item_can_drop).getType() == 9
                            && ItemTemplate3.item.get(id_item_can_drop).getClazz() == conn.p.clazz)
                            && !(ItemTemplate3.item.get(id_item_can_drop).getType() == 10
                            && ItemTemplate3.item.get(id_item_can_drop).getClazz() == conn.p.clazz)
                            && !(ItemTemplate3.item.get(id_item_can_drop).getType() == 11
                            && ItemTemplate3.item.get(id_item_can_drop).getClazz() == conn.p.clazz)) {
                        id_item_can_drop = it_.get(Util.random(it_.size()));
                        dem++;
                    }
                }
                if (dem >= 50) {
                    Service.send_notice_box(conn, "rương rỗng kkk");
                    conn.p.item.remove(4, id_potion, 1);
                    return;
                }
                byte color_ = (byte) Util.random(5);
                byte tier_ = (byte) 0;
                //
                short index_real = 0;
                String name = ItemTemplate3.item.get(id_item_can_drop).getName();
                for (int i = id_item_can_drop - 5; i < id_item_can_drop + 5; i++) {
                    if (ItemTemplate3.item.get(i).getName().equals(name) && ItemTemplate3.item.get(i).getColor() == color_) {
                        index_real = (short) i;
                        break;
                    }
                }
                ItemTemplate3 item = ItemTemplate3.item.get(index_real);
                //
                Message m = new Message(78);
                if (id_potion == 207) {
                    m.writer().writeUTF("Rương tím");
                } else if (id_potion == 205) {
                    m.writer().writeUTF("Rương đỏ");
                }
                m.writer().writeByte(1); // size
                for (int i = 0; i < 1; i++) {
                    m.writer().writeUTF(item.getName()); // name
                    m.writer().writeShort(item.getIcon()); // icon
                    m.writer().writeInt(1); // quantity
                    m.writer().writeByte(3); // type in bag
                    m.writer().writeByte(tier_); // tier
                    m.writer().writeByte(color_); // color
                }
                m.writer().writeUTF("");
                m.writer().writeByte(1);
                m.writer().writeByte(0);
                conn.addmsg(m);
                m.cleanup();
                //
                Item3 itInventory = new Item3();
                itInventory.id = item.getId();
                itInventory.name = item.getName();
                itInventory.clazz = item.getClazz();
                itInventory.type = item.getType();
                itInventory.level = item.getLevel();
                itInventory.icon = item.getIcon();
                itInventory.op = new ArrayList<>();
                itInventory.op.addAll(item.getOp());
                itInventory.color = item.getColor();
                itInventory.part = item.getPart();
                itInventory.tier = tier_;
                itInventory.islock = false;
                itInventory.time_use = 0;
                conn.p.item.add_item_inventory3(itInventory);
                conn.p.item.remove(4, id_potion, 1);
                break;
            }
            case 206: {
                ItemTemplate7 item1 = ItemTemplate7.item.get(Util.random(8, 12));
                ItemTemplate7 item2 = ItemTemplate7.item.get((50 > Util.random(0, 100)) ? 0 : 3);
                int quant1_ = Util.random(1, 6);
                int quant2_ = Util.random(1, 6);
                int quant3_ = Util.random(300, 500);
                //
                Message m = new Message(78);
                m.writer().writeUTF("Rương vàng");
                m.writer().writeByte(3); // size
//                for (int i = 0; i < 3; i++) {
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item1.getIcon()); // icon
                m.writer().writeInt(quant1_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
                //
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item2.getIcon()); // icon
                m.writer().writeInt(quant2_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
                //
                m.writer().writeUTF(""); // name
                m.writer().writeShort(0); // icon
                m.writer().writeInt(quant3_); // quantity
                m.writer().writeByte(4); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
//                }
                m.writer().writeUTF("");
                m.writer().writeByte(1);
                m.writer().writeByte(0);
                conn.addmsg(m);
                m.cleanup();
                //
                Item47 itbag = new Item47();
                itbag.id = item1.getId();
                itbag.quantity = (short) quant1_;
                itbag.category = 7;
                conn.p.item.add_item_inventory47(7, itbag);
                //
                Item47 itbag2 = new Item47();
                itbag2.id = item2.getId();
                itbag2.quantity = (short) quant2_;
                itbag2.category = 7;
                conn.p.item.add_item_inventory47(7, itbag2);
                //
                conn.p.update_vang(quant3_, "Nhận %s vàng từ rương vàng");
                //
                conn.p.item.char_inventory(7);
                conn.p.item.remove(4, id_potion, 1);
                break;
            }
            case 273: {
                ItemTemplate7 item1 = ItemTemplate7.item.get(Util.random(246, 345));
                ItemTemplate7 item2 = ItemTemplate7.item.get((50 > Util.random(0, 100)) ? 0 : 3);
                int quant1_ = Util.random(1, 2);
                int quant2_ = Util.random(1, 6);
                int quant3_ = Util.random(30000, 50000);
                //
                Message m = new Message(78);
                m.writer().writeUTF("Rương boss phe");
                m.writer().writeByte(3); // size
//                for (int i = 0; i < 3; i++) {
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item1.getIcon()); // icon
                m.writer().writeInt(quant1_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
                //
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item2.getIcon()); // icon
                m.writer().writeInt(quant2_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
                //
                m.writer().writeUTF(""); // name
                m.writer().writeShort(0); // icon
                m.writer().writeInt(quant3_); // quantity
                m.writer().writeByte(4); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
//                }
                m.writer().writeUTF("");
                m.writer().writeByte(1);
                m.writer().writeByte(0);
                conn.addmsg(m);
                m.cleanup();
                //
                Item47 itbag = new Item47();
                itbag.id = item1.getId();
                itbag.quantity = (short) quant1_;
                itbag.category = 7;
                conn.p.item.add_item_inventory47(7, itbag);
                //
                Item47 itbag2 = new Item47();
                itbag2.id = item2.getId();
                itbag2.quantity = (short) quant2_;
                itbag2.category = 7;
                conn.p.item.add_item_inventory47(7, itbag2);
                //
                conn.p.update_vang(quant3_, "Nhận %s vàng từ rương boss phe");
                //
                conn.p.item.char_inventory(7);
                conn.p.item.remove(4, id_potion, 1);
                break;
            }
            case 274: {
                ItemTemplate7 item1 = ItemTemplate7.item.get(Util.random(417, 456));
                ItemTemplate7 item2 = ItemTemplate7.item.get(Util.random(457, 463));
                ItemTemplate7 item3 = ItemTemplate7.item.get(Util.random(246, 345));
                int quant1_ = Util.random(10, 11);
                int quant2_ = Util.random(2, 3);
                int quant3 = Util.random(1, 2);
                //
                Message m = new Message(78);
                m.writer().writeUTF("Rương chiến công");
                m.writer().writeByte(3); // size
//                for (int i = 0; i < 3; i++) {
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item1.getIcon()); // icon
                m.writer().writeInt(quant1_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
                //
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item2.getIcon()); // icon
                m.writer().writeInt(quant2_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
                //
                m.writer().writeUTF(""); // name
                m.writer().writeShort(0); // icon

                m.writer().writeByte(4); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
//                }
                m.writer().writeUTF("");
                m.writer().writeByte(1);
                m.writer().writeByte(0);
                conn.addmsg(m);
                m.cleanup();
                //
                Item47 itbag = new Item47();
                itbag.id = item1.getId();
                itbag.quantity = (short) quant1_;
                itbag.category = 7;
                conn.p.item.add_item_inventory47(7, itbag);
                //
                Item47 itbag2 = new Item47();
                itbag2.id = item2.getId();
                itbag2.quantity = (short) quant2_;
                itbag2.category = 7;
                conn.p.item.add_item_inventory47(7, itbag2);
                //
                conn.p.item.char_inventory(7);
                conn.p.item.char_inventory(4);
                conn.p.item.remove(4, id_potion, 1);
                break;
            }
            case 242: {
                Topmc.log(conn.p.name);
                List<BoxItem> boxItems = new ArrayList<>();

                ItemTemplate7 item1 = ItemTemplate7.item.get(Util.random(124, 345));
                ItemTemplate7 item2 = ItemTemplate7.item.get((50 > Util.random(0, 100)) ? 0 : 3);
                int quant1_ = Util.random(1, 2);
                int quant2_ = Util.random(1, 6);
                int quant3_ = Util.random(30000, 50000);
                Message m = new Message(78);
                m.writer().writeUTF("Thần đèn ADM");
                m.writer().writeByte(3); // size
                // Item 1
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item1.getIcon()); // icon
                m.writer().writeInt(quant1_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color

                boxItems.add(new BoxItem(item1.getId(), (short) quant1_, (byte) 7));

                // Item 2
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item2.getIcon()); // icon
                m.writer().writeInt(quant2_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color

                boxItems.add(new BoxItem(item2.getId(), (short) quant2_, (byte) 7));

                // Vàng
                m.writer().writeUTF(""); // name
                m.writer().writeShort(0); // icon
                m.writer().writeInt(quant3_); // quantity
                m.writer().writeByte(4); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color

                m.writer().writeUTF("");
                m.writer().writeByte(1);
                m.writer().writeByte(0);
                conn.addmsg(m);
                m.cleanup();

                boxItems.add(new BoxItem((short) 0, (short) quant3_, (byte) 4));

                // Thêm item vào inventory
                Item47 itbag = new Item47();
                itbag.id = item1.getId();
                itbag.quantity = (short) quant1_;
                itbag.category = 7;
                conn.p.item.add_item_inventory47(7, itbag);

                Item47 itbag2 = new Item47();
                itbag2.id = item2.getId();
                itbag2.quantity = (short) quant2_;
                itbag2.category = 7;
                conn.p.item.add_item_inventory47(7, itbag2);

                // Cộng vàng
                conn.p.update_vang(quant3_, "Nhận %s vàng từ Thần đèn ADM");

                // Tỉ lệ rơi item đặc biệt 5%
                if (Util.random(0, 10000) < 1 || conn.ac_admin > 111) { // 5%
                    List<Integer> theGD = new ArrayList<>(java.util.Arrays.asList(4649, 4650, 4651, 4652, 4728));
                    short iditem = Util.random(theGD, new ArrayList<>()).shortValue();
                    conn.p.item.add_item_inventory3_default(iditem, 0, true);
                }

                // Item3 đặc biệt (thời hạn 1 ngày)
                int[] itemIds = {4654, 4655};
                int randIndex = Util.random(itemIds.length);
                int randId = itemIds[randIndex];
                ItemTemplate3 temp3 = ItemTemplate3.item.get(randId);
                if (temp3 != null) {
                    Item3 it = new Item3();
                    it.id = temp3.getId();
                    it.name = temp3.getName();
                    it.clazz = temp3.getClazz();
                    it.type = temp3.getType();
                    it.level = temp3.getLevel();
                    it.icon = temp3.getIcon();
                    it.op = temp3.getOp();
                    it.color = 5;
                    it.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 1;
                    it.part = temp3.getPart();

                    conn.p.item.add_item_inventory3(it);
                    conn.p.item.char_inventory(3);
                    Service.send_notice_box(conn, "Đã nhận " + temp3.getName() + " (1 ngày)");

                    boxItems.add(new BoxItem((short) temp3.getId(), (short) 1, (byte) 3));
                } else {
                    Service.send_notice_box(conn, "Item không tồn tại!");
                }

                conn.p.item.char_inventory(7);
                conn.p.item.remove(4, id_potion, 1);

                // Hiển thị danh sách quà nhận được
                Service.Show_open_box_notice_item(conn.p, "Phần quà Thần đèn ADM", boxItems);

                break;
            }
            case 328: {
                int han; // số ngày hạn dùng (-1 là vĩnh viễn)
                int tile = Util.random(100); // random từ 0 đến 99

                // Tỷ lệ:
                // <80  → 80% nhận 1 ngày
                // <99  → 19% nhận 3 ngày
                // còn lại → 1% vĩnh viễn
                if (tile < 80) {
                    han = 1;
                } else if (tile < 99) {
                    han = 3;
                } else {
                    han = -1;
                }

                // ID vật phẩm thưởng
                int item_id = 4852;
                ItemTemplate3 temp3 = ItemTemplate3.item.get(item_id);
                if (temp3 == null) {
                    Service.send_notice_box(conn, "Item không tồn tại!");
                    break;
                }

                // Tạo vật phẩm mới
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = new ArrayList<>(temp3.getOp());
                it.color = 5;
                it.part = temp3.getPart();

                // Nếu không phải vĩnh viễn → set hạn dùng
                if (han != -1) {
                    it.expiry_date = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * han);
                }

                // Thêm vào túi đồ
                conn.p.item.add_item_inventory3(it);
                conn.p.item.char_inventory(3);

                // Tạo danh sách quà để hiện popup
                List<BoxItem> gifts = new ArrayList<>();
                gifts.add(new BoxItem((short) it.id, (short) 1, (byte) 3));

                // Hiển thị popup
                String title = "Bạn nhận được " + it.name + (han == -1 ? " (vĩnh viễn)" : " (" + han + " ngày)") + "!";
                Service.Show_open_box_notice_item(conn.p, title, gifts);

                // Nếu là vĩnh viễn → gửi KTG
                if (han == -1) {
                    Manager.gI().chatKTGprocess("🎉 " + conn.p.name + " đã nhận được " + it.name + " vĩnh viễn!");
                }

                // Xoá vật phẩm đã sử dụng
                conn.p.item.remove(4, 328, 1);

                break;
            }

            case 92: {
                int han;
                int tile = Util.random(1000); // random 0 đến 999

                // 50% nhận vĩnh viễn
                if (tile < 250) {  // 25% 1 ngày
                    han = 1;
                } else if (tile < 500) { // 25% 3 ngày
                    han = 3;
                } else { // 50% vĩnh viễn
                    han = -1;
                }

                int item_id = 2940;
                ItemTemplate3 temp3 = ItemTemplate3.item.get(item_id);
                if (temp3 == null) {
                    Service.send_notice_box(conn, "Item không tồn tại!");
                    break;
                }

                // Tạo item nhận
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();

                if (han != -1) {
                    it.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * han;
                }

                // Thêm item vào inventory
                conn.p.item.add_item_inventory3(it);
                conn.p.item.char_inventory(3);

                // Tạo danh sách quà để hiện popup
                List<BoxItem> gifts = new ArrayList<>();
                gifts.add(new BoxItem((short) it.id, (short) 1, (byte) 3));

                // Hiển thị popup mở hộp quà
                Service.Show_open_box_notice_item(conn.p, "Bạn nhận được phần quà:", gifts);

                // Nếu nhận vĩnh viễn thì thông báo lên KTG
                if (han == -1) {
                    Manager.gI().chatKTGprocess("🎉 " + conn.p.name + " đã nhận được " + it.name + " vĩnh viễn!");
                }

                // Xóa vật phẩm dùng để đổi
                conn.p.item.remove(4, 92, 1);

                break;
            }


            case 93: {
                int han;
                int tile = Util.random(1000); // random 0-999

                if (tile < 800) {
                    han = 1; // 1 ngày (80%)
                } else if (tile < 990) {
                    han = 3; // 3 ngày (19%)
                } else {
                    han = -1; // vĩnh viễn (1%)
                }

                // Danh sách item có thể nhận
                int[] itemIds = {2940, 4850, 4851, 4825}; // có thể thêm bớt tùy ý
                int randId = itemIds[Util.random(itemIds.length)];

                ItemTemplate3 temp3 = ItemTemplate3.item.get(randId);
                if (temp3 == null) {
                    Service.send_notice_box(conn, "Item không tồn tại!");
                    break;
                }

                List<BoxItem> gifts = new ArrayList<>();

                // Tạo item nhận
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.part = temp3.getPart();

                if (han != -1) {
                    it.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * han;
                }

                // Thêm item vào inventory
                conn.p.item.add_item_inventory3(it);
                conn.p.item.char_inventory(3);

                // Thêm vào danh sách hiện quà
                gifts.add(new BoxItem((short) it.id, (short) 1, (byte) 3));

                // Hiển thị popup mở hộp quà cho người chơi
                Service.Show_open_box_notice_item(conn.p, "Bạn nhận được phần quà:", gifts);

                // Nếu nhận vĩnh viễn thì thông báo lên KTG
                if (han == -1) {
                    Manager.gI().chatKTGprocess("🎉 " + conn.p.name + " đã nhận được " + it.name + " vĩnh viễn!");
                }

                // Xóa vật phẩm dùng để đổi
                conn.p.item.remove(4, 93, 1);

                break;
            }


            case 318: {
                ItemTemplate7 item1 = ItemTemplate7.item.get(Util.random(316, 345));
                ItemTemplate4 item2 = ItemTemplate4.item.get(246);
                ItemTemplate7 item3 = ItemTemplate7.item.get(Util.random(316, 345));
                ItemTemplate7 item4 = ItemTemplate7.item.get(Util.random(316, 345));
                ItemTemplate7 item5 = ItemTemplate7.item.get(Util.random(316, 345));
                ItemTemplate7 item6 = ItemTemplate7.item.get(Util.random(316, 345));
                int quant1_ = Util.random(0, 3);
                int quant2_ = Util.random(0, 3);
                int quant3_ = Util.random(0, 3);
                int quant4_ = Util.random(0, 3);
                int quant5_ = Util.random(0, 3);
                int quant6_ = Util.random(0, 3);
                //
                Message m = new Message(78);
                m.writer().writeUTF("");
                m.writer().writeByte(3); // size
//                for (int i = 0; i < 3; i++) {
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item1.getIcon()); // icon
                m.writer().writeInt(quant1_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
                //
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item2.getIcon()); // icon
                m.writer().writeInt(quant2_); // quantity
                m.writer().writeByte(4); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
                //
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item3.getIcon()); // icon
                m.writer().writeInt(quant3_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
                //
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item4.getIcon()); // icon
                m.writer().writeInt(quant4_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
                //
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item5.getIcon()); // icon
                m.writer().writeInt(quant5_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
                //
                m.writer().writeUTF(""); // name
                m.writer().writeShort(item6.getIcon()); // icon
                m.writer().writeInt(quant6_); // quantity
                m.writer().writeByte(7); // type in bag
                m.writer().writeByte(0); // tier
                m.writer().writeByte(0); // color
                //
                m.writer().writeUTF("");
                m.writer().writeByte(1);
                m.writer().writeByte(0);
                conn.addmsg(m);
                m.cleanup();
                //
                Item47 itbag = new Item47();
                itbag.id = item1.getId();
                itbag.quantity = (short) quant1_;
                itbag.category = 7;
                conn.p.item.add_item_inventory47(7, itbag);
                //
                Item47 itbag2 = new Item47();
                itbag2.id = item2.getId();
                itbag2.quantity = (short) quant2_;
                itbag2.category = 4;
                conn.p.item.add_item_inventory47(4, itbag2);
                //
                Item47 itbag3 = new Item47();
                itbag.id = item3.getId();
                itbag.quantity = (short) quant1_;
                itbag.category = 7;
                conn.p.item.add_item_inventory47(7, itbag);
                //
                Item47 itbag4 = new Item47();
                itbag.id = item4.getId();
                itbag.quantity = (short) quant1_;
                itbag.category = 7;
                conn.p.item.add_item_inventory47(7, itbag);
                //
                Item47 itbag5 = new Item47();
                itbag.id = item5.getId();
                itbag.quantity = (short) quant1_;
                itbag.category = 7;
                conn.p.item.add_item_inventory47(7, itbag);
                //
                Item47 itbag6 = new Item47();
                itbag.id = item6.getId();
                itbag.quantity = (short) quant1_;
                itbag.category = 7;
                conn.p.item.add_item_inventory47(7, itbag);
                //
                //
                conn.p.item.char_inventory(7);
                conn.p.item.char_inventory(4);
                conn.p.item.remove(4, id_potion, 1);
                break;
            }
            case 261: {
                if (conn.p.level < 10 || conn.p.level == 20 || conn.p.level == 30 || conn.p.level == 40) {
                    Service.send_notice_nobox_white(conn, "Level không phù hợp");
                    return;
                }
                conn.p.item.remove(4, id_potion, 1);
                if (conn.p.getlevelpercent() >= 500) {
                    conn.p.update_Exp(-(Level.entry.get(conn.p.level - 1).exp / 2), false);
                } else {
                    int levelchange = conn.p.level - 1;
                    long exp_add = (Level.entry.get(levelchange - 1).exp * (500 + conn.p.getlevelpercent())) / 1000;
                    conn.p.level = (short) (levelchange - 1);
                    conn.p.exp = Level.entry.get(levelchange - 2).exp - 1;
                    conn.p.tiemnang = (short) (1 + Level.get_tiemnang_by_level(conn.p.level - 1));
                    conn.p.kynang = (short) (1 + Level.get_kynang_by_level(conn.p.level - 1));
                    conn.p.point1 = (short) (4 + conn.p.level);
                    conn.p.point2 = (short) (4 + conn.p.level);
                    conn.p.point3 = (short) (4 + conn.p.level);
                    conn.p.point4 = (short) (4 + conn.p.level);
                    conn.p.skill_point = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                    conn.p.update_Exp(1 + exp_add, false);
                    //
                    Service.send_char_main_in4(conn.p);
                    for (int i = 0; i < conn.p.map.players.size(); i++) {
                        Player p0 = conn.p.map.players.get(i);
                        if (p0.ID != conn.p.ID && (Math.abs(p0.x - conn.p.x) < 200) && (Math.abs(p0.y - conn.p.y) < 200)) {
                            MapService.send_in4_other_char(p0.map, p0, conn.p);
                        }
                    }
                }
                break;
            }
            case 228:
            case 229:
            case 230:
            case 231:
            case 232:
            case 233:
            case 234: {
                short id_wing_receive = (short) (id_potion + 4414);
                boolean check = false;
                for (int i = 0; i < conn.p.myclan.item_clan.size(); i++) {
                    if (id_potion == conn.p.myclan.item_clan.get(i).id) {
                        check = true;
                        break;
                    }
                }
                if (conn.p.item.wear[14] != null && conn.p.item.wear[14].id == id_wing_receive) {
                    check = false;
                }
                if (check) {
                    Item3 itInventory = new Item3();
                    itInventory.id = id_wing_receive;
                    itInventory.name = ItemTemplate3.item.get(id_wing_receive).getName();
                    itInventory.clazz = ItemTemplate3.item.get(id_wing_receive).getClazz();
                    itInventory.type = ItemTemplate3.item.get(id_wing_receive).getType();
                    itInventory.level = 20;
                    itInventory.icon = ItemTemplate3.item.get(id_wing_receive).getIcon();
                    itInventory.op = ItemTemplate3.item.get(id_wing_receive).getOp();
                    itInventory.color = 0;
                    itInventory.part = ItemTemplate3.item.get(id_wing_receive).getPart();
                    itInventory.tier = 0;
                    itInventory.islock = true;
                    itInventory.time_use = 0;
                    conn.p.player_wear(itInventory, -1, (byte) 13);
                }
                break;
            }
            case 245: {
                conn.p.item.char_bag(3);
                conn.p.item.char_bag(4);
                conn.p.item.char_bag(7);
                conn.p.type_process_chest = 1;
                Message m = new Message(23);
                m.writer().writeUTF("Túi Hành Trang");
                m.writer().writeByte(3);
                m.writer().writeShort(0);
                conn.addmsg(m);
                m.cleanup();
                break;
            }

            case 253: {
                if (conn.p.item.total_item_by_id(4, id_potion) > 0) {
                    conn.p.item.remove(4, id_potion, 1);
                    conn.p.item.add_item_inventory47((short) 252, (short) 10, (byte) 4);
                    conn.p.item.char_inventory(4);
                }
                break;
            }
            case 303: { // đèn hoa đăng
                Service.send_box_input_text(conn, 28, "Thả đèn", new String[]{"Tên bạn thả cùng"});
                break;
            }
            case 305: { // bó sen hồng
                if (conn.p.item.get_inventory_able() < 3) {
                    Service.send_notice_box(conn, "Cần 3 ô trống trong hành trang!");
                    return;
                }
                if (conn.p.item.total_item_by_id(4, id_potion) > 0) {
//                    try{
                    conn.p.item.remove(4, id_potion, 1);
                    List<BoxItem> ids = new ArrayList<>();

                    List<Integer> it7 = new ArrayList<>(java.util.Arrays.asList(12, 13, 11));
                    List<Integer> it7_vip = new ArrayList<>(java.util.Arrays.asList(14, 471, 346, 33));
                    List<Integer> it4 = new ArrayList<>(java.util.Arrays.asList(294, 275, 52, 18));
                    List<Integer> it4_vip = new ArrayList<>(java.util.Arrays.asList(206, 147));
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
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                }
                break;
            }
            case 307: { // bó sen trắng
                if (conn.p.item.get_inventory_able() < 3) {
                    Service.send_notice_box(conn, "Cần 3 ô trống trong hành trang!");
                    return;
                }
                if (conn.p.item.total_item_by_id(4, id_potion) > 0) {
                    conn.p.item.remove(4, id_potion, 1);
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
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 6) { // nltt
                            short id = (short) Util.random(417, 464);
                            short quant = (short) Util.random(2);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 16) {
                            short id = Util.random(it4_vip, new ArrayList<>()).shortValue();
                            short quant = (short) 1;
                            ids.add(new BoxItem(id, quant, (byte) 4));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 4);
                        } else if (ran < 30) {
                            short id = Util.random(it7_vip, new ArrayList<>()).shortValue();
                            short quant = (short) 1;
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 45) {
                            short id = Util.random(it4, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(1, 3);
                            ids.add(new BoxItem(id, quant, (byte) 4));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 4);
                        } else if (ran < 70) {
                            short id = Util.random(it7, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(1, 3);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else {
                            short id = (short) Util.random(new int[]{2, 5});
                            short quant = (short) Util.random(100, 300);
                            ids.add(new BoxItem(id, quant, (byte) 4));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 4);
                        }
                    }
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                }
                break;
            }
//case 298: {
            // Player player = conn.p;

            // Mob_in_map.getInstance().addMob(player.map, (short) 197);
            // Mob_in_map.getInstance().addMob(player.map, (short) 194);


            // break;
//}


            // Đệ tử
            case 219: {
                if (conn.p.squire != null) {
                    conn.p.item.remove(4, 219, 1);
                    Squire.callSquire(conn);
                } else {
                    Service.send_notice_box(conn, "Chưa có đệ tử, hãy đến NPC Oda để đổi");
                }
                break;
            }
            // Mê cung
            case 241: {
                if (!conn.p.isRobber() && !conn.p.isTrader() && !conn.p.isKnight()) {
                    conn.p.item.remove(4, 241, 1);
                    Vgo vgo = Maze.defaultVgo((short) 105);
                    conn.p.change_map(conn.p, vgo);
                    conn.p.time_maze = System.currentTimeMillis() + 5 * 60 * 1000L;
                } else {
                    Service.send_notice_nobox_white(conn, "Hãy tháo cải trang trước");
                }
                break;
            }
        }
    }

    private static void use_item_mount(Session conn, short id) throws IOException {
        switch (id) {
            case 62:
            case 63:
            case 64:
            case 65:
            case 66: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = (byte) (id - 62);
                conn.p.map.send_horse(conn.p);
                break;
            }
            case 70:
            case 71:
            case 72:
            case 73:
            case 74: {
                if (conn.p.item.get_inventory_able() < 1) {
                    Service.send_notice_nobox_white(conn, "Hành trang đầy!");
                    return;
                }
                conn.p.item.remove(4, id, 1);
                Item47 itbag = new Item47();
                itbag.id = (short) (id - 8);
                itbag.quantity = 99;
                conn.p.item.add_item_inventory47(4, itbag);
                break;
            }
            case 124: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.TUAN_LOC;
                conn.p.map.send_horse(conn.p);
                break;
            }
            case 125, 223, 247, 272, 276, 295, 282, 280, 270: {
                if (conn.p.item.get_inventory_able() < 1) {
                    Service.send_notice_nobox_white(conn, "Hành trang đầy!");
                    return;
                }
                conn.p.item.remove(4, id, 1);
                Item47 itbag = new Item47();
                itbag.id = (short) (id - 1);
                itbag.quantity = 99;
                conn.p.item.add_item_inventory47(4, itbag);
                break;
            }
            case 146: {
                conn.p.type_use_horse = Horse.SOI_XAM;
                conn.p.map.send_horse(conn.p);
                break;
            }
            case 159: {
                conn.p.type_use_horse = Horse.SOI_GIO_TUYET;
                conn.p.map.send_horse(conn.p);
                break;
            }
            case 160: {
                conn.p.type_use_horse = Horse.SOI_BAO_LUA;
                conn.p.map.send_horse(conn.p);
                break;
            }
            case 161: {
                conn.p.type_use_horse = Horse.SOI_BONG_MA;
                conn.p.map.send_horse(conn.p);
                break;
            }
            case 163: {
                conn.p.type_use_horse = Horse.SU_TU;
                conn.p.map.send_horse(conn.p);
                break;
            }
            case 222: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.HEO_RUNG;
                conn.p.map.send_horse(conn.p);
                break;
            }
            case 246: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.CON_LAN;
                conn.p.map.send_horse(conn.p);
                break;
            }
            case 248: {
                conn.p.type_use_horse = Horse.CON_LAN;
                conn.p.map.send_horse(conn.p);
                break;
            }
            case 250: {
                conn.p.type_use_horse = Horse.SKELETON;
                conn.p.map.send_horse(conn.p);
                break;
            }
            case 251: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.CHUOT_TUYET;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 69;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 268: {
                conn.p.type_use_horse = Horse.CHUOT_TUYET;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 69;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 271: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.TRAU_RUNG;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 107;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }

            case 275: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.CAN_DAU_VAN;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 111;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 294: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.HOA_KY_LAN;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 116;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;

            }
            case 334: {

                conn.p.type_use_horse = Horse.HOA_KY_LAN;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 116;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;

            }
            case 281: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.XE_TRUOT_TUYET;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 115;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 279: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.MA_TOC_DO;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 114;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }

            case 317: {
                conn.p.type_use_horse = Horse.XE_TRUOT_TUYET;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 115;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 299: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.PHUONG_HOANG_LUA;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 117;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 269: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.VOI_MA_MUT;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 106;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 296: {
                conn.p.type_use_horse = Horse.VOI_MA_MUT;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 106;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 301: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.RONG_BANG;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 121;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 313: {
                conn.p.type_use_horse = Horse.PHUONG_HOANG_LUA;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 117;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 314: {
                conn.p.type_use_horse = Horse.RONG_BANG;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 121;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 316: {
                conn.p.type_use_horse = Horse.MA_TOC_DO;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 114;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 323: {
                conn.p.item.remove(4, id, 1);
                conn.p.type_use_horse = Horse.CA_CHEP;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 145;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 325: {
                conn.p.type_use_horse = Horse.CA_CHEP;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 145;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 326: {
                conn.p.type_use_horse = Horse.CORGI;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 172;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 302: {
                conn.p.type_use_horse = Horse.HUOU;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 186;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 300: {
                conn.p.type_use_horse = Horse.CHUOT_CONG;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 185;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 324: {
                conn.p.type_use_horse = Horse.GAU;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 187;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 359: {
                conn.p.type_use_horse = Horse.RONG_RED;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 183;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 360: {
                conn.p.type_use_horse = Horse.RONG_BLUE;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 184;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
            case 361: {
                conn.p.type_use_horse = Horse.RONG_YELLOW;
                conn.p.map.send_horse(conn.p);
                conn.p.id_horse = 186;
                MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                Service.send_char_main_in4(conn.p);
                break;
            }
        }
    }

    public static void ProcessItem3(Session conn, Message m2) throws IOException {
        byte id = m2.reader().readByte();
        byte index = m2.reader().readByte();
        if (id < 0 || id > (conn.p.maxInventory - 1)) {
            return;
        }
        //
        Item3 temp3 = conn.p.item.inventory3[id];
        if (temp3 != null) {
            if (temp3.id >= 2880 && temp3.id <= 2935 && temp3.time_use > 0) {
                long time = temp3.time_use - System.currentTimeMillis();
                int gems = (int) (time / 3_600_000) + 1;
                conn.p.id_remove_time_use = id;
                Service.send_box_input_yesno(conn, -125, "Bạn có muốn hoàn thành nhanh với giá " + gems + " ngọc?");
                return;
            }
            if (temp3.clazz != 4 && temp3.clazz != conn.p.clazz) {
                Service.send_notice_nobox_white(conn, "Class không hợp lệ");
                return;
            }
            if (temp3.level > conn.p.level) {
                Service.send_notice_nobox_white(conn, "Level quá thấp");
                return;
            }
            if (temp3.type == 14) {
                Service.send_notice_nobox_white(conn, "Hãy đem trứng đến chuồng thú để ấp");
                return;
            }
            if (temp3.time_use > 0) {
                long time_ = temp3.time_use - System.currentTimeMillis();
                time_ /= 60_000;
                Service.send_notice_nobox_white(conn, "Sử dụng sau " + ((time_ > 0) ? time_ : 1) + "p nữa");
                return;
            }
            if (Item3.isBook(temp3.id) && temp3.color != 5) {
                Service.send_notice_nobox_white(conn, "Không thể dùng vật phẩm này.");
                return;
            }
            int id_skill = -1;
            if ((temp3.id == 4577 || temp3.id == 4579 || temp3.id == 4581 || temp3.id == 4583) && temp3.color == 5) {
                id_skill = 0;
            } else if (temp3.id == 4578 || temp3.id == 4580 || temp3.id == 4582 || temp3.id == 4584 && temp3.color == 5) {
                id_skill = 1;
            }
            if (id_skill > -1 && conn.p.skill_110[id_skill] >= 0 && Item3.isBook(temp3.id)) {
                Service.send_notice_nobox_white(conn, "Bạn đã học kỹ năng này rồi");
                return;
            }
            if (conn.p.isRobber() || conn.p.isKnight() || conn.p.isTrader()){
                return;
            }
            if (temp3.islock) {
                switch (temp3.type) {
                    case 0: // coat
                    case 1: // pant
                    case 2: // crown
                    case 3: // grove
                    case 4: // ring
                    case 5: // chain
                    case 6: // shoes
                    case 7: // wing
                    case 15:
                    case 8:
                    case 9:
                    case 10:
                    case 16:
                    case 21:
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                    case 26:
                    case 27:
                    case 11:
                    case 28: { // weapon
                        conn.p.player_wear(temp3, id, index);
                        break;
                    }
//                    case 55: { // weapon
//                        conn.p.player_wear(temp3, id, index);
//                        break;
//                    }
//                    case 102: { // weapon
//                        conn.p.player_wear(temp3, id, index);
//                        break;
//                    }
                    case 103: { // weapon
                        conn.p.player_wear(temp3, id, index);
                        break;
                    }
                    case 104: { // weapon
                        conn.p.player_wear(temp3, id, index);
                        break;
                    }
                    case 105: { // weapon
                        conn.p.player_wear(temp3, id, index);
                        break;
                    }
                }
            } else {
                if (temp3.type != 53) {
                    conn.p.id_buffer_126 = id;
                    conn.p.id_temp_byte = index;
                    Service.send_box_input_yesno(conn, 126, "Sử dụng vật phẩm này sẽ khóa, hãy xác nhận!");
                } else {
                    conn.p.skill_110[id_skill] = 0;
                    Service.send_notice_nobox_white(conn, "Đã học kỹ năng");
                    conn.p.item.remove(3, id, 1);
                    conn.p.item.char_inventory(3);
                    conn.p.item.char_inventory(4);
                    conn.p.item.char_inventory(7);
                }
            }
        }
    }

    public static void ProcessItem7(Session conn, Message m2) throws IOException {
        short id = m2.reader().readShort();

        // 🔒 CHẶN ID RÁC / ÂM
        if (id <= 0) {
            return;
        }

        // 🔒 RESET NGỌC TINH LUYỆN TRƯỚC
        conn.p.id_ngoc_tinh_luyen = -1;

        if (conn.p.item.total_item_by_id(7, id) <= 0) {
            return;
        }

        // ✅ NGỌC TINH LUYỆN
        if (id >= 352 && id <= 381) {

            if (conn.p.item.get_inventory_able() < 1) {
                Service.send_notice_nobox_white(conn, "Hành trang đầy!");
                return;
            }

            conn.p.id_ngoc_tinh_luyen = id;
            Service.send_box_input_text(
                    conn,
                    16,
                    "Nhập số lượng",
                    new String[]{"Nhập số lượng"}
            );

        } else {
            // ✅ TÁCH NGỌC THƯỜNG
            switch (id) {
                case 5:
                case 6:
                case 7: {

                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_nobox_white(conn, "Hành trang đầy!");
                        return;
                    }

                    Item47 it = new Item47();
                    it.id = (short) (id - 4);
                    it.quantity = ItemTemplate7.item.get(id).getValue();

                    conn.p.item.add_item_inventory47(7, it);
                    conn.p.item.remove(7, id, 1);
                    break;
                }
            }
        }

        conn.p.item.char_inventory(4);
        conn.p.item.char_inventory(7);
        conn.p.item.char_inventory(3);
    }
}
