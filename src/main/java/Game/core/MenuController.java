package Game.core;

import Game.Boss.BossServer;
import Game.Helps.CheckItem;
import Game.Helps.Save_Log;
import Game.History.His_COIN;
import Game.NPC.NpcTemplate;
import Game.Quest.DailyQuest;
import Game.client.Clan;
import Game.client.Player;
import Game.client.Squire;
import Game.event.*;
import Game.activities.*;
import Game.io.Session;
import Game.map.*;
import Game.template.*;

import java.util.Arrays;
import java.util.Iterator;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import Game.template.Item47;
import Game.client.Pet;
import Game.event.Event_1;
import Game.io.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map.Entry;

public class MenuController {

    public static void request_menu(Session conn, Message m) throws IOException {
        byte idnpc = m.reader().readByte();
//        if (conn.p.map.find_npc_in_map(idnpc) == null) {
//            Service.send_notice_nobox_white(conn, "Không thấy npc");
//            return;
//        }
        if (idnpc == -43 || idnpc == -45 || idnpc == -48 || idnpc == -46 || idnpc == -47) {
            Menu_ChangeZone(conn);
            return;
        }
        if (conn.status != 0 && idnpc != -127) {
            Service.send_notice_box(conn, "Tài khoản chưa được kích hoạt,");
            return;
        }
        String[] menu;
        switch (idnpc) {
            case -127: {
//                Npc.chat(conn.p.map, "Có thẻ GD đồ không dùng thì vào đây", idnpc);
                //menu = conn.language.menu_ADMIN;

                menu = new String[]{"Nhiệm vụ hàng ngày", "Nhận quà mốc nạp", "Đổi coin sang ngọc, vàng", "Shop Trang Bị Ngọc", "Shop Trang Bị Mới", "Kích hoạt tài khoản"};    //, "Thành tích", "Nhận quà top Level", "Nhận quà top event", "Shop Trang Bị Ngọc", "Đồ tinh tú"};
                break;
            }
            case -126: {
                menu = new String[]{"ĐẶC QUYỀN ĐỘC NHẤT TOP 1 DONATE","Menu dành cho top 1 donate", "Quỹ đầu tư", "Nạp Coin,Mua Token,Bán Token"};
                break;
            }
            case -104:
                if (conn.p.map.map_id == 135) {
                    menu = new String[]{"Về Làng sói trắng", "Vượt Làng phủ sương"};
                } else {
                    menu = new String[]{"Về Làng sói trắng"};
                }
                break;
            case -67: {
                if (1 == 1) {
                    Service.send_notice_box(conn, "Chức năng bảo trì");
                    return;
                }
                menu = new String[]{"Trả mị nương", "Giáp sơn tinh", "Giáp thủy tinh", "Giáp sơn tinh đặc biệt",
                        "Giáp thủy tinh đặc biệt", "Đổi quà may mắn"};
                break;
            }
            case -86: {
                menu = new String[]{"Nhận đệ tử", "Hủy đệ tử"};
                break;
            }
            case -89: { //
                menu = new String[]{"Bắn pháo"};
                break;
            }
            case -87: {
                menu = new String[]{"Điều ước"};
                break;
            }
            case -81: {
                Npc.chat(conn.p.map, "Để đăng ký lôi đài yêu cầu tối thiểu cấp 65 và 1000 ngọc", idnpc);
                menu = conn.language.menu_Oda;
                break;
            }
            case -63: {
                menu = new String[]{""};
                if (Manager.gI().event == 0) {
                    menu = LunarNewYear.menu;
                }
                break;
            }
            case -3, -20: { // Lisa
                menu = conn.language.menu_Lisa;
                break;
            }
            case -5, -21, -75: { // Hammer
                menu = conn.language.menu_Hammer;
                break;
            }
            case -4, -22, -77: {// Doubar
                menu = conn.language.menu_Doubar;
                break;
            }
            case -33: { // da dich chuyen
                menu = conn.language.menu_Tele33;
                break;
            }
            case -55: { // da dich chuyen
                menu = conn.language.menu_Tele55;
                break;
            }
            case -10: { // da dich chuyen
                menu = conn.language.menu_Tele10;
                break;
            }
            case -8: {
                menu = new String[]{""};
                if (conn.p.maxInventory < 126) {
                    menu = conn.language.menu_Zulu;
                } else {
                    Menu_Zulu(conn, (byte) 0);
                    return;
                }
                break;
            }
            case -36: {
                menu = conn.language.menu_PhapSu[0];
                Item3 item = conn.p.item.wear[12];
                if (item != null) {
                    if (item.hasOpPercentDame()) {
                        menu = conn.language.menu_PhapSu[1];
                    } else {
                        menu = conn.language.menu_PhapSu[2];
                    }
                }
                break;
            }
            case -44: {
                Item3 item = conn.p.item.wear[11];
                Item3 item_ = conn.p.item.wear[21];
                Item3 item__ = conn.p.item.wear[22];
                if (item != null || item_ != null || item__ != null) {
                    menu = conn.language.menu_Anna[0];
                } else {
                    menu = conn.language.menu_Anna[1];
                }
                break;
            }
            case -32: {
                menu = menu = conn.language.menu_Rank;
                break;
            }
            case -7: {
                if (conn.user.contains("knightauto_hsr_")) {
                    menu = conn.language.menu_Aman[0];
                } else {
                    menu = conn.language.menu_Aman[1];
                }
                break;
            }
            case -34: { // cuop bien
                menu = conn.language.menu_CuopBien;
                break;
            }
            case -2, -19: { // zoro
                if (conn.p.myclan != null) {
                    if (conn.p.myclan.mems.get(0).name.equals(conn.p.name)) {
                        menu = conn.language.menu_Zoro[0];
                    } else {
                        menu = conn.language.menu_Zoro[1];
                    }
                } else {
                    menu = conn.language.menu_Zoro[2];
                }
                break;
            }
            case -85: { // mr edgar
                menu = new String[]{"Báo Thù", "Hướng dẫn báo thù"};
                break;
            }
            case -42: { // pet
                menu = new String[]{"Chuồng thú", "Shop thức ăn", "Shop trứng", "Tháo pet"};
                break;
            }
            case -37: {
                menu = conn.language.menu_PhoChiHuy;
                break;
            }
            case -41: {
                menu = conn.language.menu_TienCanh;
                break;
            }
            case -49: {
                menu = new String[]{"LIKE", "Kết hôn"};
                break;
            }
            case -82: {
                menu = new String[]{"Rời khỏi đây"};
                break;
            }
            case -69: {
                if (Manager.gI().event == 1) { // sự kiện noel
                    menu = new String[]{"Đổi hộp đồ chơi", "Hướng dẫn", "Đăng ký nấu kẹo", "Bỏ nguyên liệu vào nồi kẹo",
                            "Lấy kẹo đã nấu", "Đổi túi kẹo", "Đổi trứng phượng hoàng băng", "Đổi trứng đại bàng",
                            "Đổi giày băng giá", "Đổi mặt nạ băng giá", "Đổi kẹo gậy", "Đổi gậy tuyết", "Đổi xe trượt tuyết",
                            "Đổi trứng khỉ nâu"};

                } else if (Manager.gI().event == 2) { // sự kiện hè
                    menu = new String[]{"Mâm trái cây", "Top sự kiện", "Đổi quà may mắn"};
                    send_menu_select(conn, -69, menu, (byte) Manager.gI().event);
                    return;
                    //menu = new String[]{"Coming soon", infoServer.Website};
                } else if (Manager.gI().event == 3) { // sự kiện vu lan
                    menu = new String[]{"Đổi bó sen trắng", "Đổi hoa sen hồng", "Đổi bó sen hồng", "Xem top", "Đổi con lân", "Đổi trứng khỉ nâu", "Đổi trứng tiểu yêu", "Đổi cánh thời trang"};
                    send_menu_select(conn, -69, menu, (byte) Manager.gI().event);
                    return;

                } else {
                    menu = conn.language.menu_Sophia_Normal;
                    send_menu_select(conn, -69, menu, (byte) 0);
                    return;
                }

                break;
            }
            case -62: {
                if (Manager.gI().event == 1) {
                    menu = new String[]{"Tăng tốc nấu", "Hướng dẫn", "Thông tin", "Top Nguyên Liệu"};
                    send_menu_select(conn, -62, menu, (byte) 1);
                } else if (Manager.gI().event != -1) {
                    menu = new String[]{"Thêm củi", "Thông tin"};
                    send_menu_select(conn, -62, menu, (byte) Manager.gI().event);
                } else {
                    Service.send_notice_box(conn, "Chưa có chức năng :(.");
                    return;
                }
                break;
            }

            case -66: {
                if (Manager.gI().event == 1) {
                    // Menu event 1 (cây thông)
                    menu = new String[]{"Hoa tuyết", "Ngôi sao", "Quả châu", "Thiệp", "Top trang trí cây thông"};
                } else if (Manager.gI().event == 0) {
                    // Menu event 0
                    menu = new String[]{"Top sự kiện"};
                } else {
                    // Mặc định hoặc event khác
                    menu = new String[]{""};
                }
                break;
            }

            case -57: {
                menu = new String[]{"Mua bán"};
                break;
            }
            case -54: {
                menu = new String[]{"Đến Thành Phó Kho Báu"};
                break;
            }
            case -58: {
                menu = new String[]{"Mua lạc đà", "Bán đá quý", "Đồ thương nhân"};
                break;
            }
            case -59: {
                menu = new String[]{"Mua lạc đà", "Bán đá quý", "Đồ cướp"};
                break;
            }
            case -53: {
                menu = new String[]{" Đăng Ký Chiến trường", "Hướng dẫn", "Đổi đại bàng", "Vào Chiến Trường"};
                break;
            }
            default: {
                return;
            }
        }
        //
        send_menu_select(conn, idnpc, menu);
    }

    public static void processmenu(Session conn, Message m) throws IOException {
        short idnpc = m.reader().readShort();
        @SuppressWarnings("unused")
        byte idmenu = m.reader().readByte();
        byte index = m.reader().readByte();
        if (index < 0) {
            return;
        }
        if (idnpc == -56) {
            send_menu_select(conn, 119, new String[]{"Thông tin", "Bảo hộ", "Hồi máu", "Tăng tốc"});
            return;
        }
//        if (conn.p.map.find_npc_in_map(idnpc) == null && NpcTemplate.getNpcById(idnpc) != null) {
//            Service.send_notice_nobox_white(conn, "Không thấy npc");
//            return;
//        }
        if (idnpc >= 30000 && idmenu >= 111) {
            if (idmenu == 111) {
                Menu_DuaBe(conn, idnpc, idmenu, index);
            }
            return;
        }
        if (idnpc >= 30000 && idmenu == Manager.gI().event) {
            if (Manager.gI().event == 4) {
                Menu_MobMy(conn, idnpc, idmenu, index);
            } else {
                Menu_MobEvent(conn, idnpc, idmenu, index);
            }
            return;
        }
        switch (idnpc) {
            case -43: {
                if (idmenu == 1) {
                    switch (index) {
                        case 0:
                            if (conn.p.item.total_item_by_id(4, 54) >= 1) {
                                Map map = Map.get_map_by_id(conn.p.map.map_id)[1];
                                if (map != null && map.players.size() >= map.maxplayer) {
                                    Service.send_notice_box(conn, conn.language.khuvucday);
                                    return;
                                }
                                conn.p.item.remove(4, 54, 1);
                                conn.p.add_EffDefault(-127, 1, 2 * 60 * 60 * 1000);
                                MapService.leave(conn.p.map, conn.p);
                                conn.p.map = map;
                                MapService.enter(conn.p.map, conn.p);
                            } else {
                                Service.send_notice_box(conn, "Không đủ Đồng bạc Tyche");
                            }
                            break;
                        case 1:
                            Service.send_box_input_yesno(conn, -112, "Bạn có muốn vào khu 2 với " + Map.NGOC_KHU_2 + " ngọc cho 2 giờ?");
                            break;
                    }
                }
                break;
            }
            case -310: {
                short[] cupIds = {202, 203, 204};
                int[] durationsInDays = {7, 5, 1};  // thời hạn item theo loại cúp
                short rewardItemId = 4856;

                if (index < 0 || index >= cupIds.length) {
                    Service.send_notice_box(conn, "Lựa chọn không hợp lệ!");
                    break;
                }

                // Kiểm tra đủ cúp
                if (conn.p.item.total_item_by_id(4, cupIds[index]) < 1) {
                    Service.send_notice_box(conn, "Bạn không có đủ cúp để đổi!");
                    break;
                }
                // Trừ cúp
                conn.p.item.remove(4, cupIds[index], 1);
                // Lấy mẫu item3
                ItemTemplate3 temp3 = ItemTemplate3.item.get(rewardItemId);
                if (temp3 == null) {
                    Service.send_notice_box(conn, "Không tìm thấy vật phẩm đổi thưởng!");
                    break;
                }
                // Tạo item3 mới
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

                long now = System.currentTimeMillis();
                it.expiry_date = now + durationsInDays[index] * 86400000L;

                // Thêm item vào kho
                conn.p.item.add_item_inventory3(it);
                conn.p.item.char_inventory(3);

                // Tạo danh sách BoxItem để hiện phần thưởng
                List<BoxItem> boxItems = new ArrayList<>();
                boxItems.add(new BoxItem((short) temp3.getId(), (short) 1, (byte) 3));

                // Hiển thị phần thưởng
                Service.Show_open_box_notice_item(conn.p, "Phần quà Đổi cúp", boxItems);
                break;
            }
            case -309: {
                if (index == 0) {
//                    String msg = TopItem101.getTop();
//                    Service.send_notice_box(conn, TopItem101.getTop());
                } else if (index == 1) {
                    String msg = Topmc.getTop();
                    Service.send_notice_box(conn, Topmc.getTop());
                }
                break;
            }
            case -305: {
                MenuMissAnna_SubGift(conn, index);
                break;
            }
            case -303: {
                Menu_Miss_Anna_Sub(conn, index); // hoặc tên khác nếu bạn đặt khác
                break;
            }
            case -105: {
                try {
                    switch (index) {
                        case 0:
                            doiVangSangNgoc(conn.p, 1_000_000L, 10);
                            break;
                        case 1:
                            doiVangSangNgoc(conn.p, 10_000_000L, 100);
                            break;
                        case 2:
                            doiVangSangNgoc(conn.p, 100_000_000L, 1000);
                            break;
                        case 3:
                            doiVangSangNgoc(conn.p, 1_000_000_000L, 10_000);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // Hoặc log lại
                }
                break;
            }
            case -300: { // 👉 xử lý menu chọn đổi điểm tiêu xài của Doubar
                if (idmenu == 3) {
                    MenuController.Menu_Doubar(conn, index, (byte) 3); // ← ép kiểu tại đây
                }
                break;
            }
            case -301: {
                int[] ids = {4700, 4703, 4704, 4705, 4706};
                if (index >= 0 && index < ids.length) {
                    int itemId = ids[index];
                    ItemTemplate3 temp = ItemTemplate3.item.get(itemId);

                    if (temp == null) {
                        Service.send_notice_box(conn, "Không tìm thấy vật phẩm!");
                        return;
                    }

                    Item3 it = new Item3();
                    it.id = temp.getId();
                    it.name = temp.getName();
                    it.clazz = temp.getClazz();
                    it.type = temp.getType();
                    it.level = temp.getLevel();
                    it.icon = temp.getIcon();
                    it.op = temp.getOp();
                    it.color = 5;
                    it.expiry_date = 0; // vĩnh viễn
                    it.part = temp.getPart();

                    conn.p.item.add_item_inventory3(it);
                    conn.p.item.char_inventory(3);
                    Service.send_notice_box(conn, "Đã nhận " + it.name + " (vĩnh viễn)");
                } else {
                    Service.send_notice_box(conn, "Lựa chọn không hợp lệ!");
                }
                break;
            }
            case -302: {
                int[] ids = {4833, 4834, 4835, 4836, 4837, 4838};
                if (index >= 0 && index < ids.length) {
                    int itemId = ids[index];
                    ItemTemplate3 temp = ItemTemplate3.item.get(itemId);

                    if (temp == null) {
                        Service.send_notice_box(conn, "Không tìm thấy vật phẩm!");
                        return;
                    }

                    Item3 it = new Item3();
                    it.id = temp.getId();
                    it.name = temp.getName();
                    it.clazz = temp.getClazz();
                    it.type = temp.getType();
                    it.level = temp.getLevel();
                    it.icon = temp.getIcon();
                    it.op = temp.getOp();
                    it.color = 5;
                    it.expiry_date = 0; // vĩnh viễn
                    it.part = temp.getPart();

                    conn.p.item.add_item_inventory3(it);
                    conn.p.item.char_inventory(3);

                    // Tạo danh sách BoxItem để hiển thị phần thưởng
                    List<BoxItem> boxItems = new ArrayList<>();
                    boxItems.add(new BoxItem((short) temp.getId(), (short) 1, (byte) 3));

                    // Hiển thị phần thưởng với tiêu đề
                    Service.Show_open_box_notice_item(conn.p, "Phần quà nhận được", boxItems);

                } else {
                    Service.send_notice_box(conn, "Lựa chọn không hợp lệ!");
                }
                break;
            }

            case -128: {
                Menu_Nang_Skill(conn, index);
                break;
            }
            case -129: {
                Mob_MoTaiNguyen moTaiNguyen = Manager.gI().chiem_mo.get_mob_in_map(conn.p.map);
                if (moTaiNguyen != null) {
                    if (index == 0) {
                        if (conn.p.getCoin() > 1000) {
                            if (moTaiNguyen.nhanBans.size() < 10) {
                                conn.p.update_coin(-1000);
                                ChiemMo.trieu_hoi(conn.p, moTaiNguyen);
                                conn.p.history_coin(-1000,"(TRỪ COIN) triệu hồi nhân bản");
                            } else {
                                Service.send_notice_box(conn, "Đã triệu hồi tối đa.");
                            }
                        } else {
                            Service.send_notice_box(conn, "Không đủ coin");
                        }
                    } else if (index == 1) {
                        int cnt = moTaiNguyen.nhanBans.size();
                        if (conn.p.getCoin() > cnt * 1000) {
                            Service.send_box_input_yesno(conn, -107, "Bạn có muốn dùng: " + cnt * 1000 + " coin để nâng " + cnt + " không?");
                        } else {
                            Service.send_notice_box(conn, "Không đủ coin");
                        }
                    }
                }
                break;
            }
            case -130: {
                Mob_MoTaiNguyen mo = Manager.gI().chiem_mo.get_mob_in_map(conn.p.map);
                if (index == 0) {
                    int[] res = Manager.gI().chiem_mo.getMoResource(mo.sql_id);
                    int vang = res[0], ngoc = res[1], exp = res[2];

                    if (vang > 0) {
                        Service.send_notice_box(conn, "Đang có " + vang + "/75.000.000 VÀNG");
                    } else if (ngoc > 0) {
                        Service.send_notice_box(conn, "Đang có " + ngoc + "/3500 NGỌC");
                    } else if (exp > 0) {
                        Service.send_notice_box(conn, "Đang có " + exp + "/135.000 EXP");
                    } else {
                        Service.send_notice_box(conn, "Mỏ hiện không có tài nguyên");
                    }
                } else if (index == 1) {
                    int[] res = Manager.gI().chiem_mo.getMoResource(mo.sql_id);
                    int vang = res[0], ngoc = res[1], exp = res[2];
                    if (vang <= 0 && ngoc <= 0 && exp <= 0) {
                        Service.send_notice_box(conn, "Có gì đâu mà thu hoạch");
                        return;
                    }
                    if (vang > 0) conn.p.myclan.update_vang(vang);
                    if (ngoc > 0) conn.p.myclan.update_ngoc(ngoc);
                    if (exp > 0)  conn.p.myclan.update_exp(exp);

                    Manager.gI().chiem_mo.resetMo(mo.sql_id);

                    Service.send_notice_box(conn, "Thu hoạch thành công");
                }
                break;
            }
            case 996: {
                if (conn.p.mynuong != null && index == 0) {
                    synchronized (conn.p.mynuong) {
                        if (!conn.p.mynuong.owner.isBlank()) {
                            conn.p.change_map_my_nuong(conn.p);
                        }
                    }
                }
                if (conn.p.mynuong != null && conn.p.mynuong.power < 1 && index == 1) {
                    if (conn.p.get_ngoc() < 10) {
                        Service.send_notice_box(conn, "Ta cần 10 ngọc");
                        return;
                    }
                    conn.p.update_ngoc(-10);
                    conn.p.item.char_inventory(5);
                    conn.p.mynuong.power += 1000;
                    Service.send_notice_box(conn, "Ta đi tiếp thôi");
                }
                break;
            }
            case -67: {
                Menu_VuaHung_Event_2(conn, index);
                break;
            }
            case -104: {
                Menu_Serena(conn, index);
                break;
            }
            case -63: {
                if (Manager.gI().event == 0) {
                    Menu_Ong_Do(conn, index);
                }
                break;
            }
            case 4: {
                Menu_DoiDongMeDaySTG(conn, index);
                break;
            }
            case 5: {
                Menu_DoiDongMeDaySTPT(conn, index);
                break;
            }
            case 117: {
                Menu_ThaoKhamNgoc(conn, index);
                break;
            }
            case -54: {
                Menu_Mr_Haku(conn, index);
                break;
            }
            case -81: {
                Menu_Mrs_Oda(conn, index, idmenu);
                break;
            }
            case -127: {
                Menu_ADMIN_SHARINGAN(conn, idnpc, index, idmenu);
                break;
            }
            case -126: {
                Menu_Quyen_Luc(conn, index, idmenu);
                break;
            }
            case 114: {
                Menu_Wedding(conn, index);
                break;
            }
            case -82: {
                Menu_Miss_Anwen(conn, index);
                break;
            }
            case -53: {
                Menu_Mr_Ballard(conn, idnpc, idmenu, index);
                break;
            }
            case 210: {
                Menu_Kich_Hoat_Canh(conn, index);
                break;
            }
            case 119: {
                Menu_Pet_di_buon(conn, index);
                break;
            }
            case -57: {
                Menu_Mr_Dylan(conn, index);
                break;
            }
            case -58: {
                Menu_Graham(conn, index);
                break;
            }
            case -59: {
                Menu_Mr_Frank(conn, index);
                break;
            }
            case -3, -20: { // Lisa
                Menu_Lisa(conn, index);
                break;
            }
            case -90: { // keva
//                Menu_keva(conn, index);
                break;
            }
            case -86: {
                Menu_Master(conn, index);
                break;
            }
            case -89: {
                if (Manager.gI().event == 0) {
                    LunarNewYear.ban_phao(conn);
                }
                break;
            }
            case -4, -22, -77: {
                Menu_Doubar(conn, index, idmenu);
                break;
            }
            case -5, -21, -75: {
                Menu_Hammer(conn, index, idmenu);
                break;
            }
            case -33: {
                Menu_DaDichChuyen33(conn, index);
                break;
            }
            case -55: {
                Menu_DaDichChuyen55(conn, index);
                break;
            }
            case -10: {
                Menu_DaDichChuyen10(conn, index);
                break;
            }
            case -8: {
                Menu_Zulu(conn, index);
                break;
            }
            case 126: {
                Menu_Admin(conn, index);
                break;
            }
            case -36: {
                Menu_Phap_Su(conn, index);
                break;
            }
            case -44: {
                Menu_Miss_Anna(conn, index);
                break;
            }
            case -32: {
                Menu_Rank(conn, index, idmenu);
                break;
            }
            case -7: {
                Menu_Aman(conn, index);
                break;
            }
            case -34: {
                Menu_CuopBien(conn, index);
                break;
            }
            case 125: { // vxmm
                menuLuckyDrawVip(conn, index);
                break;
            }
            case 132: { // vxmm
                menuLuckyDrawNormal(conn, index);
                break;
            }
            case -2, -19: { // vxmm
                Menu_Zoro(conn, index);
                break;
            }
            case -85: { //
                Menu_Mr_Edgar(conn, index);
                break;
            }
            case 124: {
                Service.revenge(conn, index);
                break;
            }
            case 122: {
                Menu_Clan_Manager(conn, index);
                break;
            }
            case 127: {
                Menu_Shop_Clan(conn, index);
                break;
            }
            case -42: {
                Menu_Pet_Manager(conn, index);
                break;
            }
            case -37: {
                Menu_PhoChiHuy(conn, index);
                break;
            }
            case -38:
            case -40: {
                break;
            }
            case -41: {
                Menu_TienCanh(conn, index);
                break;
            }
            case -49: {
                Menu_Vua_Chien_Truong(conn, index);
                break;
            }
            case -69: {
                if (Manager.gI().event == 1) {
                    Menu_Event(conn, index);
                } else if (Manager.gI().event == 2) {
                    Menu_MissSophia(conn, idnpc, idmenu, index);
                } else if (Manager.gI().event == 3) {
                    Menu_MissSophia(conn, idnpc, idmenu, index);
                } else {
                    Menu_MissSophia(conn, idnpc, idmenu, index);
                }
                break;
            }
            case -62: {
                if (index == 0) {
                    if (EventManager.notCanRegister()) {
                        if (conn.p.get_vang() < 500000) {
                            Service.send_notice_box(conn, "Không đủ 500,000 vàng");
                            return;
                        }
                        conn.p.update_vang(-500000L, "Trừ %s vàng tăng tốc nấu");
                        if (Manager.gI().event == 0) {
                            EventManager.update(1);
                        } else if (Manager.gI().event == 1) {
                            Event_1.update(1);
                        }
                        Service.send_notice_box(conn, "Thời gian nấu còn lại " + EventManager.time + " phút");
                    } else {
                        Service.send_notice_box(conn, "Chưa đến thời gian nấu");
                    }
                } else if (index == 1) {
                    EventManager.send_info(conn);
                }
                break;
            }

            case -66: {
                if (Manager.gI().event == 1) {
                    Menu_CayThong(conn, index);
                } else if (Manager.gI().event == 0 && idmenu == 0) {
                    EventManager.top_event(conn);
                } else {
                    Service.send_notice_box(conn, "Sự kiện hiện không hoạt động.");
                }
                break;
            }
            case -91: {
                Menu_Khac(conn, idmenu, index);
                break;
            }
            case 111: {
                Menu_Krypton(conn, idmenu, index);
                break;
            }
            case -87: {
                if (conn.p.item.get_inventory_able() < 1) {
                    Service.send_notice_box(conn.p.conn, "Hành trang đầy!");
                    return;
                }
                short iditem = 242;
                Item47 itbag = new Item47();
                itbag.id = iditem;
                itbag.quantity = 1;
                itbag.category = 4;
                conn.p.item.add_item_inventory47(itbag);
                Service.send_notice_box(conn.p.conn, "Bạn nhận được Đèn thần ");
                List<BoxItem> ids = new ArrayList<>();
                ids.add(new BoxItem(iditem, (short) 1, (byte) 4));
                Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                if (conn.p.isRobber() || conn.p.isKnight() || conn.p.isTrader()) {
                    return;
                } else {
                    conn.p.veLang();
                }
                break;
            }
            default: {
                Service.send_notice_box(conn, "Đã xảy ra lỗi!");
                break;
            }
        }
    }


    private static void Menu_Serena(Session conn, byte index) throws IOException {
        if (conn.p.map.map_id == 135) {
            if (index == 0) {
                conn.p.veLang();
            } else if (index == 1) {
                if (conn.p.get_EffDefault(-128) == null) {
                    if (conn.p.item.total_item_by_id(4, 315) < 1) {
                        Service.send_notice_box(conn, "Không có Vé vào làng phủ sương trong hành trang");
                        return;
                    } else {
                        conn.p.add_EffDefault(-128, 1, 4 * 60 * 60 * 1000);
                        conn.p.item.remove(4, 315, 1);
                    }
                }
                EffTemplate eff = conn.p.get_EffDefault(-128);
                if (eff != null) {
                    Service.send_time_box(conn.p, (byte) 1, new short[]{(short) ((eff.time - System.currentTimeMillis()) / 1000)}, new String[]{"Làng phủ sương"});
                    if (100 <= conn.p.level && conn.p.level < 110) {
                        Vgo vgo = new Vgo();
                        vgo.id_map_go = 125;
                        vgo.x_new = 100;
                        vgo.y_new = 100;
                        conn.p.change_map(conn.p, vgo);
                    } else if (110 <= conn.p.level && conn.p.level < 120) {
                        Vgo vgo = new Vgo();
                        vgo.id_map_go = 127;
                        vgo.x_new = 100;
                        vgo.y_new = 100;
                        conn.p.change_map(conn.p, vgo);
                    } else if (120 <= conn.p.level && conn.p.level < 130) {
                        Vgo vgo = new Vgo();
                        vgo.id_map_go = 129;
                        vgo.x_new = 200;
                        vgo.y_new = 200;
                        conn.p.change_map(conn.p, vgo);
                    } else if (130 <= conn.p.level) {
                        Vgo vgo = new Vgo();
                        vgo.id_map_go = 132;
                        vgo.x_new = 100;
                        vgo.y_new = 100;
                        conn.p.change_map(conn.p, vgo);
                    }
                }
            }
        } else {
            conn.p.veLang();
        }
    }

    private static void Menu_MobMy(Session conn, int idmob, byte idmenu, byte index) throws IOException {
        if (idmenu == 4) {
            if (index != 0) {
                return;
            }
            if (conn.p.mynuong == null) {
                MobMy.Mob_My mob = MobMy.getMob(idmob);
                if (mob == null || !mob.map.equals(conn.p.map)) {
                    Message m2 = new Message(17);
                    m2.writer().writeShort(-1);
                    m2.writer().writeShort(176);
                    conn.addmsg(m2);
                    m2.cleanup();
                    Service.send_notice_box(conn, "Không tìm thấy");
                    return;
                }
                if (!(mob.map.equals(conn.p.map) && Math.abs(mob.x - conn.p.x) < 150 && Math.abs(mob.y - conn.p.y) < 150)) {
                    Service.send_notice_box(conn, "Khoảng cách quá xa.\nNếu thực sự ở gần hãy thử load lại map.");
                    return;
                }
                mob.setOwner(conn.p);
                conn.p.mynuong = new MyNuong(177, Manager.gI().get_index_mob_new(), conn.p.x, conn.p.y,
                        conn.p.map.map_id, conn.p.name, conn.p);
                MyNuong_manager.add(conn.p.name, conn.p.mynuong);
                //
                Message m22 = new Message(4);
                m22.writer().writeByte(1);
                m22.writer().writeShort(177);
                m22.writer().writeShort(conn.p.mynuong.ID);
                m22.writer().writeShort(conn.p.mynuong.x);
                m22.writer().writeShort(conn.p.mynuong.y);
                m22.writer().writeByte(-1);
                conn.addmsg(m22);
                m22.cleanup();
            } else {
                Service.send_notice_box(conn,
                        "Mỵ Nương của bạn đang ở\nVị trí:\n" + Map.get_map_by_id(conn.p.mynuong.id_map)[0].name + "\n"
                                + conn.p.mynuong.x + " " + conn.p.mynuong.y);
            }
        }
    }
    private static void Menu_Master(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                if (conn.p.duabe == null || Math.abs(conn.p.duabe.x - conn.p.x) > 75
                        && Math.abs(conn.p.duabe.y - conn.p.y) > 75) {
                    Service.send_notice_box(conn, "Ta không thấy đứa bé đâu cả");
                } else {
                    if (!conn.p.isOwner) {
                        return;
                    }
                    if (conn.p.level < 10) {
                        Service.send_notice_box(conn, conn.language.yeucaucap + 10);
                        return;
                    }
                    if (conn.p.squire == null) {
                        Squire.create(conn.p);
                        conn.p.squire = new Squire(conn, conn.p.ID);
                        conn.p.squire.load();
                        Service.send_notice_box(conn, conn.language.nhandetu);
                        Squire.callSquire(conn);
                        DuaBe_manager.remove(conn.p.duabe.name);
                        conn.p.duabe = null;
                    } else {
                        Service.send_notice_box(conn, "Bạn có đệ tử rồi");
                    }
                }
                break;
            }
            case 1: {
                if (conn.p.squire != null) {
                    Service.send_box_input_yesno(conn, -124, "Huỷ đệ tử sẽ mất hết trang bị đang mặc.Bạn có muốn huỷ?");
                } else {
                    Service.send_notice_box(conn, "Chưa có đệ tử");
                }
                break;
            }
        }

    }
    private static void Menu_DuaBe(Session conn, int idmob, byte idmenu, byte index) throws IOException {
        try {
            if (idmenu == 111) {
//                if (index != 0) {
//                    return;
//                }
                switch (index) {
                    case 0: {
                        if (conn.p.squire != null) {
                            Service.send_notice_box(conn, "Bạn đã có đệ tử rồi");
                            return;
                        }
                        if (conn.p.duabe == null) {
                            if (conn.p.item.total_item_by_id(4, 220) < 1) {
                                Service.send_notice_box(conn, "Bạn cần có 1 kẹo hồ lô");
                                return;
                            }
                            MobDuaBe.Mob_duabe mob = MobDuaBe.getMob(idmob);
                            if (mob == null || !mob.map.equals(conn.p.map)) {
                                Message m2 = new Message(17);
                                m2.writer().writeShort(-1);
                                m2.writer().writeShort(175);
                                conn.addmsg(m2);
                                m2.cleanup();
                                Service.send_notice_box(conn, "Không tìm thấy");
                                return;
                            }
                            if (!(mob.map.equals(conn.p.map) && Math.abs(mob.x - conn.p.x) < 150 && Math.abs(mob.y - conn.p.y) < 150)) {
                                Service.send_notice_box(conn, "Khoảng cách quá xa.\nNếu thực sự ở gần hãy thử load lại map.");
                                return;
                            }
                            conn.p.item.remove(4, 220, 1);
                            mob.setOwner(conn.p);
                            conn.p.duabe = new DuaBe(175, Manager.gI().get_index_mob_new(), conn.p.x, conn.p.y,
                                    conn.p.map.map_id, conn.p.name, conn.p);
                            DuaBe_manager.add(conn.p.name, conn.p.duabe);
                            //
                            Message m22 = new Message(4);
                            m22.writer().writeByte(1);
                            m22.writer().writeShort(175);
                            m22.writer().writeShort(conn.p.duabe.ID);
                            m22.writer().writeShort(conn.p.duabe.x);
                            m22.writer().writeShort(conn.p.duabe.y);
                            m22.writer().writeByte(-1);
                            conn.addmsg(m22);
                            m22.cleanup();
                        } else {
                            Service.send_notice_box(conn,
                                    "Đứa bé của bạn đang ở\nVị trí:\n" + Map.get_map_by_id(conn.p.duabe.id_map)[0].name + "\n"
                                            + conn.p.duabe.x + " " + conn.p.duabe.y);
                        }
                        break;
                    }
                    case 1: {
                        if (conn.ac_admin < 111){
                            return;
                        }
                        Service.send_box_input_yesno(conn, -127, "Bạn có muốn nhận đệ tử với giá 100.000 ngọc?");
                        break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void Menu_VuaHung_Event_2(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                if (conn.p.mynuong == null || Math.abs(conn.p.mynuong.x - conn.p.x) > 75
                        && Math.abs(conn.p.mynuong.y - conn.p.y) > 75) {
                    Service.send_notice_box(conn, "Ta không thấy mị nương đâu cả");
                } else {
                    conn.p.change_map_my_nuong(conn.p);
                    conn.p.mynuong = null;
                    //
                    if (50 > Util.random(120)) {
                        short[] id_receiv = new short[]{48, 49, 50, 51, 54, 0, 1, 2, 3, 4, 5, 53, 205, 207};
                        short id = id_receiv[Util.random(id_receiv.length)];
                        conn.p.item.add_item_inventory47(id, (short) 1, (byte) 7);
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", new short[]{id}, new int[]{1}, new short[]{7});
                    } else {
                        short[] id_receiv = new short[]{0, 1, 2, 3, 8, 9, 10, 11, 12, 13};
                        short id = id_receiv[Util.random(id_receiv.length)];
                        conn.p.item.add_item_inventory47(id, (short) 1, (byte) 7);
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", new short[]{id}, new int[]{1}, new short[]{7});
                    }
                    conn.p.item.char_inventory(4);
                    conn.p.item.char_inventory(7);
                    conn.p.item.char_inventory(3);
                }
                break;
            }
            case 1: {
                for (int i = 137; i < 140; i++) {
                    if (conn.p.item.total_item_by_id(4, i) < 50 && conn.ac_admin < 1) {
                        Service.send_notice_box(conn, "Không đủ " + 50 + " " + ItemTemplate4.item.get(i).getName());
                        return;
                    }
                }
                if (conn.p.item.total_item_by_id(4, 141) < 50 && conn.ac_admin < 1) {
                    Service.send_notice_box(conn, "Không đủ " + 50 + " " + ItemTemplate4.item.get(141).getName());
                    return;
                }
                for (int i = 137; i < 140; i++) {
                    conn.p.item.remove(4, i, 50);
                }
                conn.p.item.remove(4, 141, 1);
                ItemTemplate3 temp3 = ItemTemplate3.item.get(4585);
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 3;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);
                conn.p.item.char_inventory(3);
                Service.send_notice_box(conn, "Nhận được " + temp3.getName());
                break;
            }
            case 2: {
                for (int i = 137; i < 140; i++) {
                    if (conn.p.item.total_item_by_id(4, i) < 50 && conn.ac_admin < 1) {
                        Service.send_notice_box(conn, "Không đủ " + 50 + " " + ItemTemplate4.item.get(i).getName());
                        return;
                    }
                }
                if (conn.p.item.total_item_by_id(4, 141) < 50 && conn.ac_admin < 1) {
                    Service.send_notice_box(conn, "Không đủ " + 50 + " " + ItemTemplate4.item.get(141).getName());
                    return;
                }
                for (int i = 137; i < 140; i++) {
                    conn.p.item.remove(4, i, 50);
                }
                conn.p.item.remove(4, 141, 1);
                ItemTemplate3 temp3 = ItemTemplate3.item.get(4586);
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = temp3.getOp();
                it.color = 5;
                it.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 3;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);
                conn.p.item.char_inventory(3);
                Service.send_notice_box(conn, "Nhận được " + temp3.getName());
                break;
            }
            case 3: {
                for (int i = 137; i < 140; i++) {
                    if (conn.p.item.total_item_by_id(4, i) < 50 && conn.ac_admin < 1) {
                        Service.send_notice_box(conn, "Không đủ " + 50 + " " + ItemTemplate4.item.get(i).getName());
                        return;
                    }
                }
                if (conn.p.item.total_item_by_id(4, 140) < 50 && conn.ac_admin < 1) {
                    Service.send_notice_box(conn, "Không đủ " + 50 + " " + ItemTemplate4.item.get(140).getName());
                    return;
                }
                for (int i = 137; i < 140; i++) {
                    conn.p.item.remove(4, i, 50);
                }
                conn.p.item.remove(4, 140, 20);
                ItemTemplate3 temp3 = ItemTemplate3.item.get(4585);
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = new ArrayList<>();
                for (int i = 0; i < temp3.getOp().size(); i++) {
                    Option op_temp = temp3.getOp().get(i);
                    it.op.add(new Option(op_temp.id, ((op_temp.getParam(0) * 15) / 10)));
                }
                it.color = 5;
                it.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 3;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);
                conn.p.item.char_inventory(4);
                conn.p.item.char_inventory(7);
                conn.p.item.char_inventory(3);
                Service.send_notice_box(conn, "Nhận được " + temp3.getName());
                break;
            }
            case 4: {
                for (int i = 137; i < 140; i++) {
                    if (conn.p.item.total_item_by_id(4, i) < 9 && conn.ac_admin < 1) {
                        Service.send_notice_box(conn, "Không đủ " + 50 + " " + ItemTemplate4.item.get(i).getName());
                        return;
                    }
                }
                if (conn.p.item.total_item_by_id(4, 140) < 50 && conn.ac_admin < 1) {
                    Service.send_notice_box(conn, "Không đủ " + 50 + " " + ItemTemplate4.item.get(140).getName());
                    return;
                }
                for (int i = 137; i < 140; i++) {
                    conn.p.item.remove(4, i, 50);
                }
                conn.p.item.remove(4, 140, 20);
                ItemTemplate3 temp3 = ItemTemplate3.item.get(4586);
                Item3 it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = new ArrayList<>();
                for (int i = 0; i < temp3.getOp().size(); i++) {
                    Option op_temp = temp3.getOp().get(i);
                    it.op.add(new Option(op_temp.id, ((op_temp.getParam(0) * 15) / 10)));
                }
                it.color = 5;
                it.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 3;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);
                conn.p.item.char_inventory(4);
                conn.p.item.char_inventory(7);
                conn.p.item.char_inventory(3);
                Service.send_notice_box(conn, "Nhận được " + temp3.getName());
                break;
            }
            case 5: {
                boolean spec = false;
                if (conn.p.item.wear[11] != null) {
                    for (Option o : conn.p.item.wear[11].op) {
                        if (o.getParam(0) == 1500) {
                            spec = true;
                            break;
                        }
                    }
                }
                if (spec) { // dawc biet
                    for (int i = 137; i < 140; i++) {
                        if (conn.p.item.total_item_by_id(4, i) < 100 && conn.ac_admin < 1) {
                            Service.send_notice_box(conn, "Không đủ 100 " + ItemTemplate4.item.get(i).getName());
                            return;
                        }
                    }
                    if (conn.p.get_ngoc() < 1000) {
                        Service.send_notice_box(conn, "Không đủ 1000 ngọc");
                        return;
                    }
                    for (int i = 137; i < 140; i++) {
                        conn.p.item.remove(4, i, 50);
                    }
                    conn.p.update_ngoc(-1000);
                    //
                    if (10 == Util.random(1000)) { // skill
                        ItemTemplate3 temp3 = ItemTemplate3.item.get((short) Util.random(4577, 4585));
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
                        conn.p.item.add_item_inventory3(it);
                        conn.p.item.char_inventory(3);
                        Service.send_notice_box(conn, "Nhận được " + temp3.getName());
                    } else if (50 > Util.random(120)) { // item 7
                        short id = (short) Util.random(2, 4);
                        conn.p.item.add_item_inventory47(id, (short) 1, (byte) 7);
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", new short[]{id}, new int[]{1}, new short[]{7});
                    } else {
                        short[] id_receiv = new short[]{206, 84, 10};
                        short id = id_receiv[Util.random(id_receiv.length)];
                        conn.p.item.add_item_inventory47(id, (short) 1, (byte) 4);
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", new short[]{id}, new int[]{1}, new short[]{4});
                    }
                    conn.p.item.char_inventory(4);
                    conn.p.item.char_inventory(7);
                    conn.p.item.char_inventory(3);
                    break;

                } else { // k
                    //
                    for (int i = 137; i < 140; i++) {
                        if (conn.p.item.total_item_by_id(4, i) < 100 && conn.ac_admin < 1) {
                            Service.send_notice_box(conn, "Không đủ 100 " + ItemTemplate4.item.get(i).getName());
                            return;
                        }
                    }
                    if (conn.p.get_ngoc() < 1000) {
                        Service.send_notice_box(conn, "Không đủ 1000 ngọc");
                        return;
                    }
                    for (int i = 137; i < 140; i++) {
                        conn.p.item.remove(4, i, 50);
                    }
                    conn.p.update_ngoc(-1000);
                    //
                    if (10 == Util.random(1000)) { // skill
                        ItemTemplate3 temp3 = ItemTemplate3.item.get((short) Util.random(4577, 4585));
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
                        conn.p.item.add_item_inventory3(it);
                        conn.p.item.char_inventory(3);
                        Service.send_notice_box(conn, "Nhận được " + temp3.getName());
                    } else if (50 > Util.random(120)) { // item 7
                        short id = (short) Util.random(2, 4);
                        conn.p.item.add_item_inventory47(id, (short) 1, (byte) 7);
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", new short[]{id}, new int[]{1}, new short[]{7});
                    } else {
                        short[] id_receiv = new short[]{206, 84, 10};
                        short id = id_receiv[Util.random(id_receiv.length)];
                        conn.p.item.add_item_inventory47(id, (short) 1, (byte) 4);
                        Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", new short[]{id}, new int[]{1}, new short[]{7});
                    }
                    conn.p.item.char_inventory(4);
                    conn.p.item.char_inventory(7);
                    conn.p.item.char_inventory(3);
                    break;
                }
            }
        }
    }

    private static void Menu_Mr_Ballard(Session conn, int idNPC, byte idmenu, byte index) throws IOException {
        // Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        switch (idmenu) {
            case 0: {
                switch (index) {
                    case 0: { // dang ky
                        if (ChienTruong.gI().getStatus() == 1) {
                            ChienTruong.gI().register(conn.p);
                        } else {
                            Service.send_notice_box(conn, "Chiến trường mở đăng ký vào 20h45 Thứ 3,5,7 hàng tuần");
                        }
                        break;
                    }
                    case 1: {
                        break;
                    }
                    case 3: {
                        if (ChienTruong.gI().getStatus() == 2) {
                            MemberBattlefields info = ChienTruong.gI().get_infor_register(conn.p.name);
                            if (info != null) {
                                Vgo vgo = new Vgo();
                                switch (info.village) {
                                    case 2: { // lang gio
                                        vgo.id_map_go = 55;
                                        vgo.x_new = 224;
                                        vgo.y_new = 256;
                                        MapService.change_flag(conn.p.map, conn.p, 2);
                                        break;
                                    }
                                    case 3: { // lang lua
                                        vgo.id_map_go = 59;
                                        vgo.x_new = 240;
                                        vgo.y_new = 224;
                                        MapService.change_flag(conn.p.map, conn.p, 1);
                                        break;
                                    }
                                    case 4: { // lang set
                                        vgo.id_map_go = 57;
                                        vgo.x_new = 264;
                                        vgo.y_new = 272;
                                        MapService.change_flag(conn.p.map, conn.p, 4);
                                        break;
                                    }
                                    default: { // 5 lang anh sang
                                        vgo.id_map_go = 53;
                                        vgo.x_new = 276;
                                        vgo.y_new = 246;
                                        MapService.change_flag(conn.p.map, conn.p, 5);
                                        break;
                                    }
                                }
                                conn.p.change_map(conn.p, vgo);
                            } else {
                                Service.send_notice_box(conn, "Chưa đăng ký");
                            }
                        } else {
                            Service.send_notice_box(conn, "Chiến trường chưa bắt đầu");
                        }
                        break;
                    }
                    case 2: {
                        if (conn.p.pointarena < 30000) {
                            Service.send_notice_box(conn, "Phải cần tối thiểu 3000 điểm tích lũy chiến trường để có thể đổi trứng đại bàng.");
                        } else if (conn.p.item.get_inventory_able() < 1) {
                            Service.send_notice_box(conn, "Cần tối thiểu 1 ô trống để có thể đổi.");
                        } else {
                            try (Connection connection = SQL.gI().getConnection(); Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM `history_doi_dai_bang` WHERE `user` = '" + conn.user + "' AND `time` >= DATE_SUB(NOW(), INTERVAL 1 WEEK);")) {
                                if (rs.next()) {
                                    Service.send_notice_box(conn, "Trong vòng 1 tuần 1 tài khoản chỉ có thể đổi 1 lần.");
                                    return;
                                } else {
                                    int last_point = conn.p.pointarena;
                                    short iditem = 3269;
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
                                    conn.p.pointarena -= 30000;
                                    String query
                                            = "INSERT INTO `history_doi_dai_bang` (`user`, `name_player`, `last_point` , `point_arena`) VALUES ('"
                                            + conn.user + "', '" + conn.p.name + "', '" + last_point + "', '" + conn.p.pointarena + "')";
                                    if (st.executeUpdate(query) > 0) {
                                        connection.commit();
                                    }
                                    List<BoxItem> ids = new ArrayList<>();
                                    ids.add(new BoxItem(iditem, (short) 1, (byte) 3));
                                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    private static void Menu_Ong_Do(Session conn, byte index) throws IOException {
        EventManager.processMenu(conn, index);
    }

    private static void Menu_MissSophia(Session conn, int idNPC, byte idmenu, byte index) throws IOException {
        if (idmenu == 0) {
            switch (index) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5: {
                    byte index_remove = (byte) (index + 13);
                    Item3 item = conn.p.item.wear[index_remove];
                    if (item != null) {
                        conn.p.id_temp_byte = index_remove;
                        Service.send_box_input_yesno(conn, (byte) (-119 + index), "Bạn có muốn tháo " + item.name + "?");
                    } else {
                        Service.send_notice_nobox_white(conn, "Không thể thực hiện");
                    }
                    break;
                }

//                case 6: {
//                    // Hiển thị hướng dẫn
//                    String huongDanSuKien = "🎉 Sự kiện Trung Thu 2025 🎉\n\n"
//                            + "- Khi train quái +-5 level tại các bản đồ, bạn sẽ nhận được các mảnh ghép Vàng, Tím, Cam (mảnh 1, 2, 3, 4).\n"
//                            + "- Dùng 100 mảnh ghép cùng màu để đổi ra Đá Mặt Trăng tương ứng (Vàng, Tím, Cam).\n"
//                            + "- Dùng Đá Mặt Trăng Vàng/Tím/Cam (mỗi loại 100 viên) + 100 ngọc để đổi Bánh Trung Thu tương ứng.\n\n"
//                            + "🍰 Các loại bánh trung thu đặc biệt:\n"
//                            + "🎁 Hãy chăm chỉ train quái và sưu tập đủ mảnh ghép để nhận những phần thưởng giá trị nhé!";
//                    Service.send_notice_box(conn, huongDanSuKien);
//                    break;
//                }
//
//                case 7: {
//                    // Đổi nguyên liệu => Đá Mặt Trăng
//                    int[] requiredIds = {38, 39, 40, 41, 42, 43, 105, 106, 107, 108, 109, 110};
//                    for (int id : requiredIds) {
//                        if (conn.p.item.total_item_by_id(4, id) < 200) {
//                            Service.send_notice_box(conn, "Bạn cần đủ 200 " + ItemTemplate4.item.get(id).getName());
//                            return;
//                        }
//                    }
//
//                    if (conn.p.kimcuong < 100) {
//                        Service.send_notice_box(conn, "Bạn cần 100 ngọc để đổi.");
//                        return;
//                    }
//
//                    if (conn.p.item.get_inventory_able() < 1) {
//                        Service.send_notice_box(conn, "Không đủ ô trống!");
//                        return;
//                    }
//
//                    for (int id : requiredIds) {
//                        conn.p.item.remove(4, id, 200);
//                    }
//
//                    conn.p.update_ngoc(-100);
//
//                    int[] moonStones = {45, 46, 47};
//                    short idReward = (short) Util.random(moonStones);
//
//                    conn.p.item.add_item_inventory47(idReward, (short) 1, (byte) 4);
//                    List<BoxItem> ids = new ArrayList<>();
//                    ids.add(new BoxItem(idReward, (short) 1, (byte) 4));
//                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
//                    break;
//                }
//
//                case 8: {
//                    // Đổi đá mặt trăng => Bánh Trung Thu
//                    int[] requiredIds = {45, 46, 47};
//                    for (int id : requiredIds) {
//                        if (conn.p.item.total_item_by_id(4, id) < 100) {
//                            Service.send_notice_box(conn, "Bạn cần đủ 100 " + ItemTemplate4.item.get(id).getName());
//                            return;
//                        }
//                    }
//
//                    if (conn.p.vang < 100_000) {
//                        Service.send_notice_box(conn, "Bạn cần 100.000 vàng để đổi.");
//                        return;
//                    }
//
//                    if (conn.p.kimcuong < 100) {
//                        Service.send_notice_box(conn, "Bạn cần 100 ngọc để đổi.");
//                        return;
//                    }
//
//                    if (conn.p.item.get_inventory_able() < 1) {
//                        Service.send_notice_box(conn, "Không đủ ô trống!");
//                        return;
//                    }
//
//                    for (int id : requiredIds) {
//                        conn.p.item.remove(4, id, 100);
//                    }
//
//                    conn.p.vang -= 100_000;
//                    conn.p.update_ngoc(-100);
//
//                    int[] rewardIds = {92, 93};
//                    short id = (short) Util.random(rewardIds);
//
//                    conn.p.item.add_item_inventory47(id, (short) 1, (byte) 4);
//                    List<BoxItem> ids = new ArrayList<>();
//                    ids.add(new BoxItem(id, (short) 1, (byte) 4));
//                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
//                    break;
//                }
//
//                case 9: {
//                    // Đăng ký sự kiện rơi nguyên liệu
//                    if (1 == 1) {
//                        Service.send_notice_box(conn, "Chức năng bảo trì");
//                        return;
//                    }
//                    //  if (1 == 1) {
//                    //     Service.send_notice_box(conn, "Chức năng bảo trì");
//                    //    return;
//                    //   }
//                    long now = System.currentTimeMillis();
//
//                    if (conn.p.allowDropNL && now < conn.p.timeAllowDropNL) {
//                        long minutesLeft = (conn.p.timeAllowDropNL - now) / 60000;
//                        Service.send_notice_box(conn, "Bạn đã đăng ký rồi! Thời hạn còn lại: " + minutesLeft + " phút.");
//                        return;
//                    }
//
//                    if (conn.p.kimcuong < 1000) {
//                        Service.send_notice_box(conn, "Bạn cần 1000 ngọc để đăng ký tham gia sự kiện.");
//                        return;
//                    }
//
//                    conn.p.kimcuong -= 1000;
//                    conn.p.allowDropNL = true;
//                    conn.p.timeAllowDropNL = now + (12 * 60 * 60 * 1000L); // 12 giờ
//                    Service.updateKimCuong(conn.p);
//                    Service.send_notice_box(conn, "🎉 Đăng ký thành công! Trong 12 giờ tới, quái sẽ rơi nguyên liệu đặc biệt.");
//                    break;
//                }
            }
        }

        // Nếu bạn có idmenu == 2 thì viết thêm else if bên dưới:
        else if (idmenu == 2 && Manager.gI().event == 2) {
            switch (index) {
                case 0: {
                    if (conn.p.level < 40) {
                        Service.send_notice_box(conn, "Level quá thấp.");
                        return;
                    }
                    if (conn.p.item.get_inventory_able() < 4) {
                        Service.send_notice_box(conn, "Hành trang đầy");
                        return;
                    }

                    if (conn.p.item.total_item_by_id(4, 141) < 1 && (!Manager.BuffAdminMaterial || conn.ac_admin < 40)) {
                        Service.send_notice_box(conn, "Thiếu " + ItemTemplate4.item.get(141).getName());
                        return;
                    }
                    for (int i = 254; i <= 258; i++) {
                        if (conn.p.item.total_item_by_id(4, i) < 1 && (!Manager.BuffAdminMaterial || conn.ac_admin < 40)) {
                            Service.send_notice_box(conn, "Thiếu " + ItemTemplate4.item.get(i).getName());
                            return;
                        }
                    }

                    conn.p.item.remove(4, 141, 1);
                    for (int i = 254; i <= 258; i++) {
                        conn.p.item.remove(4, i, 1);
                    }
                    List<BoxItem> ids = new ArrayList<>();

                    List<Integer> it7 = new ArrayList<>(java.util.Arrays.asList(0, 1, 4, 8, 9, 10, 11, 12, 13, 14));
                    List<Integer> it7_vip = new ArrayList<>(java.util.Arrays.asList(33, 346, 347, 349));
                    List<Integer> it4 = new ArrayList<>(java.util.Arrays.asList(2, 5, 61, 67, 269));
                    List<Integer> it4_vip = new ArrayList<>(java.util.Arrays.asList(131, 123, 132, 133, 52, 235, 147));
                    for (int i = 0; i < Util.random(1, 5); i++) {
                        int ran = Util.random(100);
                        if (ran < 0) {
                            short id = Util.random(it7, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(2, 5);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 6) {
                            short idsach = (short) 4762;
                            ids.add(new BoxItem(idsach, (short) 1, (byte) 3));
                            conn.p.item.add_item_inventory3_default(idsach, Util.random(10, 20), true);
                        } else if (ran < 14) {
                            short id = (short) Util.random(46, 246);
                            short quant = (short) 1;
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 24) {
                            short id = (short) Util.random(417, 464);
                            short quant = (short) Util.random(3);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 41) {
                            short id = Util.random(it7_vip, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(1, 2);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        } else if (ran < 57) {
                            short id = Util.random(it4_vip, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(1, 2);
                            ids.add(new BoxItem(id, quant, (byte) 4));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 4);
                        } else if (ran < 77) {
                            short id = Util.random(it4, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(2, 5);
                            ids.add(new BoxItem(id, quant, (byte) 4));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 4);
                        } else {
                            short id = Util.random(it7, new ArrayList<>()).shortValue();
                            short quant = (short) Util.random(2, 5);
                            ids.add(new BoxItem(id, quant, (byte) 7));
                            conn.p.item.add_item_inventory47(id, quant, (byte) 7);
                        }
                    }
                    Event_2.add_caythong(conn.p.name, 1);
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                    break;
                }
                case 1: {
                    send_menu_select(conn, 120, Event_2.get_top());
                    break;
                }
                case 2: {
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Hành trang đầy");
                        return;
                    }
                    if (conn.p.item.total_item_by_id(4, 123) < 5) {
                        Service.send_notice_box(conn, "Cần tối thiểu 5 chuông vàng");
                        return;
                    }
                    List<BoxItem> ids = new ArrayList<>();
                    conn.p.item.remove(4, 123, 5);
                    List<Integer> it = new ArrayList<>(java.util.Arrays.asList(4612, 4632, 4633, 4634, 4635));
                    List<Integer> it4 = new ArrayList<>(java.util.Arrays.asList(299, 205, 207));
                    if (Util.random(100) < 60) {
                        short id = Util.random(it4, new ArrayList<>()).shortValue();
                        short quant = (short) Util.random(1, 3);
                        ids.add(new BoxItem(id, quant, (byte) 4));
                        conn.p.item.add_item_box47(id, quant, (byte) 4);
                    } else {
                        short id = Util.random(it, new ArrayList<>()).shortValue();
                        ids.add(new BoxItem(id, (short) 1, (byte) 3));
                        conn.p.item.add_item_inventory3_default(id, Util.random(5, 7), true);
                    }

                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                    break;
                }
                default:
                    Service.send_notice_box(conn, "Chưa có chức năng ev2!");
                    break;
            }
        } else if (idmenu == 3 && Manager.gI().event == 3) {
            switch (index) {
                case 0: {
                    Service.send_box_input_text(conn, 25, "Đổi bó sen trắng", new String[]{"30 sen trắng + 100k vàng"});
                    break;
                }
                case 1: {
                    Service.send_box_input_text(conn, 26, "Đổi hoa sen hồng", new String[]{"10 sen trắng + 25k vàng"});
                    break;
                }
                case 2: {
                    Service.send_box_input_text(conn, 27, "Đổi bó sen hồng", new String[]{"5 sen hồng + 30 ngọc"});
                    break;
                }
                case 3: {
                    send_menu_select(conn, 120, Event_3.get_top());
                    break;
                }
                case 4: {
                    if (conn.p.get_ngoc() < 30 || conn.p.item.total_item_by_id(4, 304) < 10) {
                        Service.send_notice_box(conn, "Cần tối thiểu 50 ngọc và 10 bông sen hồng để đổi!");
                        return;
                    }
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Không đủ ô trống!");
                        return;
                    }
                    conn.p.update_ngoc(-30);
                    conn.p.item.remove(4, 304, 10);
                    Item47 itbag = new Item47();
                    itbag.id = 246;
                    itbag.quantity = (short) 100;
                    itbag.category = 4;
                    conn.p.item.add_item_inventory47(4, itbag);

                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", new short[]{246}, new int[]{100}, new short[]{4});
                    break;
                }
                case 5: {
                    if (conn.p.get_ngoc() < 100 || conn.p.item.total_item_by_id(4, 304) < 50) {
                        Service.send_notice_box(conn, "Cần tối thiểu 100 ngọc và 50 bông sen hồng để đổi!");
                        return;
                    }
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Không đủ ô trống!");
                        return;
                    }
                    conn.p.update_ngoc(-100);
                    conn.p.item.remove(4, 304, 50);
                    short iditem = 3616;
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
                    itbag.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 15;
                    conn.p.item.add_item_inventory3(itbag);

                    List<BoxItem> ids = new ArrayList<>();
                    ids.add(new BoxItem(iditem, (short) 1, (byte) 3));
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                    break;
                }
                case 6: {
                    if (conn.p.get_ngoc() < 100 || conn.p.item.total_item_by_id(4, 304) < 50) {
                        Service.send_notice_box(conn, "Cần tối thiểu 100 ngọc và 50 bông sen hồng để đổi!");
                        return;
                    }
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Không đủ ô trống!");
                        return;
                    }
                    conn.p.update_ngoc(-100);
                    conn.p.item.remove(4, 304, 50);
                    short iditem = 4761;
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
                    itbag.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 15;
                    conn.p.item.add_item_inventory3(itbag);

                    List<BoxItem> ids = new ArrayList<>();
                    ids.add(new BoxItem(iditem, (short) 1, (byte) 3));
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                    break;
                }
                case 7: {
                    if (conn.p.get_ngoc() < 100 || conn.p.item.total_item_by_id(4, 304) < 50) {
                        Service.send_notice_box(conn, "Cần tối thiểu 100 ngọc và 50 bông sen hồng để đổi!");
                        return;
                    }
                    if (conn.p.item.get_inventory_able() < 1) {
                        Service.send_notice_box(conn, "Không đủ ô trống!");
                        return;
                    }
                    conn.p.update_ngoc(-100);
                    conn.p.item.remove(4, 304, 50);
                    short iditem = 4642;
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
                    itbag.expiry_date = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30;
                    conn.p.item.add_item_inventory3(itbag);

                    List<BoxItem> ids = new ArrayList<>();
                    ids.add(new BoxItem(iditem, (short) 1, (byte) 3));
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", ids);
                    break;
                }
                default:
                    Service.send_notice_box(conn, "Chưa có chức năng ev3!");
                    break;
            }
        }

    }


    private static void Menu_MobEvent(Session conn, int idmob, byte idmenu, byte index) throws IOException {
        if (idmenu == 2) {
            if (index != 0) {
                return;
            }
            if (conn.p.level < 40) {
                Service.send_notice_box(conn, "Cần lên level 40 để có thể chơi sự kiện.");
                return;
            }
            MobCay mob = Event_2.getMob(idmob);
            if (mob == null || !mob.map.equals(conn.p.map)) {
                Message m2 = new Message(17);
                m2.writer().writeShort(-1);
                m2.writer().writeShort(idmob);
                conn.addmsg(m2);
                m2.cleanup();
                Service.send_notice_box(conn, "Không tìm thấy");
                return;
            }
            if (!(mob.map.equals(conn.p.map) && Math.abs(mob.x - conn.p.x) < 150 && Math.abs(mob.y - conn.p.y) < 150)) {
                Service.send_notice_box(conn, "Khoảng cách quá xa.\nNếu thực sự ở gần hãy thử load lại map.");
                return;
            }
            if (mob.Owner != null) {
                Service.send_notice_box(conn, "Đã có người khác hái quả.");
                return;
            }
            if (conn.p.item.get_inventory_able() < 1) {
                Service.send_notice_nobox_white(conn, "Hành trang đầy.");
                return;
            }
            if (conn.p.item.total_item_by_id(4, 252) < 1) {
                Service.send_notice_nobox_white(conn, "Hãy mua giỏ hái quả để chứa.");
                return;
            }
            conn.p.item.remove(4, 252, 1);
            mob.setOwner(conn.p);
            short id = (short) Util.random(254, 259);
            conn.p.item.add_item_inventory47(id, (short) 1, (byte) 4);
            Service.Show_open_box_notice_item(conn.p, "Bạn nhận được", new short[]{id}, new int[]{1}, new short[]{4});
            //Service.send_notice_box(conn, "Nhận quả: "+mob.nameOwner);
        }
    }


    private static void Menu_Krypton(Session conn, byte idmenu, byte index) throws IOException {
        if (idmenu == 0) {
            GameSrc.UpgradeMedal(conn, index);
        } else if (idmenu == 1) {
            GameSrc.UpgradeItemStar(conn, index);
        }
        conn.p.id_Upgrade_Medal_Star = -1;
    }

    private static void Menu_Khac(Session conn, byte idmenu, byte index) throws IOException {
        // ===== MENU CHÍNH =====
        if (idmenu == 0) {
            switch (index) {
                case 0: { // bật/tắt rơi nguyên liệu mề đay
                    conn.p.isDropMaterialMedal = !conn.p.isDropMaterialMedal;

                    try (Connection connection = SQL.gI().getConnection();
                         java.sql.Statement st = connection.createStatement()) {
                        st.executeUpdate(
                                "UPDATE player SET isDropMaterialMedal = "
                                        + (conn.p.isDropMaterialMedal ? 1 : 0)
                                        + " WHERE id = " + conn.p.ID + " LIMIT 1"
                        );
                        connection.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Service.send_notice_box(conn,
                            "Rơi nguyên liệu mề đay đã " + (conn.p.isDropMaterialMedal ? "Bật" : "Tắt"));
                    break;
                }

                case 1: { // bật/tắt chỉ rơi đồ cam
                    conn.p.isDropItemColor4 = !conn.p.isDropItemColor4;

                    try (Connection connection = SQL.gI().getConnection();
                         java.sql.Statement st = connection.createStatement()) {
                        st.executeUpdate(
                                "UPDATE player SET isDropItemColor4 = "
                                        + (conn.p.isDropItemColor4 ? 1 : 0)
                                        + " WHERE id = " + conn.p.ID + " LIMIT 1"
                        );
                        connection.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Service.send_notice_box(conn,
                            "Chỉ rơi đồ cam đã " + (conn.p.isDropItemColor4 ? "Bật" : "Tắt"));
                    break;
                }

                case 2: { // bật/tắt nhận exp
                    if (conn.p.type_exp == 0) {
                        conn.p.type_exp = 1;
                        Service.send_notice_box(conn, "Đã bật nhận exp");
                    } else {
                        conn.p.type_exp = 0;
                        Service.send_notice_box(conn, "Đã tắt nhận exp");
                    }
                    break;
                }

                case 3: { // mở menu Vòng xoay
                    String[] menu = {
                            "Diễn đàn",
                            "Hướng dẫn",
                            "Vòng xoay Vip",
                            "Hướng dẫn Vòng xoay Vip",
                            "Vòng xoay thường",
                            "Hướng dẫn Vòng xoay thường"
                    };
                    MenuController.send_menu_select(conn, (byte) -34, menu);
                    break;
                }

                case 4: { // bật/tắt hiển thị cây sự kiện
                    conn.p.isShowMobEvents = !conn.p.isShowMobEvents;
                    Service.send_notice_box(conn,
                            "Đã " + (conn.p.isShowMobEvents ? "bật" : "tắt") + " hiển thị cây sự kiện");
                    break;
                }

                case 5: { // về làng
                    if (conn.p.isRobber() || conn.p.isKnight() || conn.p.isTrader() && conn.ac_admin < 111) {
                        return;
                    } else {
                        conn.p.veLang();
                    }
                    break;
                }
            }

        } else if (idmenu == -34) { // menu Vòng xoay trực tiếp
            switch (index) {
                case 2:
                    menuLuckyDrawVip(conn, (byte) 0);
                    break;
                case 4:
                    menuLuckyDrawNormal(conn, (byte) 0);
                    break;
                case 3:
                    menuLuckyDrawVip(conn, (byte) 1);
                    break;
                case 5:
                    menuLuckyDrawNormal(conn, (byte) 1);
                    break;
                default:
                    Service.send_notice_box(conn, "Chúc bạn chơi game vui vẻ");
                    break;
            }
        }
    }

    private static void Menu_Mrs_Oda(Session conn, byte index, byte idMenu) throws IOException {
        // Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (idMenu == 0) {
            switch (index) {
                case 0: {
                    if (conn.p.type_reward_king_cup != 0) {
                        Service.send_notice_box(conn, "Phải nhận quà lôi đài trước");
                        return;
                    }
                    if (KingCupManager.list_name.contains(conn.p.name)) {
                        Service.send_notice_box(conn, "Bạn đã đăng ký rồi.");
                    } else {
                        KingCupManager.register(conn.p);
                    }
                    break;
                }
                case 1: {
                    if (!KingCupManager.list_name.contains(conn.p.name)) {
                        Service.send_notice_box(conn, "Bạn không thể vào khi chưa đăng ký tham gia lôi đài");
                        return;
                    }
                    conn.p.goMapTapKet();
                    break;
                }
                case 2: {
                    if (KingCup.kingCup != null && KingCup.kingCups != null) {
                        String[] arrKingCup = new String[KingCup.kingCups.size()];
                        for (int i = 0; i < KingCup.kingCups.size(); i++) {
                            KingCup ld = KingCup.kingCups.get(i);
                            arrKingCup[i] = ld.name1 + "(" + ld.players_attack.get(0).level + ") vs " + ld.name2 + "(" + ld.players_attack.get(1).level + ")";
                        }
                        send_menu_select(conn, -81, arrKingCup, (byte) 1);
                        break;
                    } else {
                        Service.send_notice_box(conn, "Chưa tới giờ thi đấu lôi đài");
                    }
                    break;
                }
                case 3: {
                    if (!KingCupManager.list_name.contains(conn.p.name)) {
                        Service.send_notice_box(conn, "Bạn chưa đăng ký tham gia lôi đài");
                        return;
                    }
                    Service.send_notice_box(conn, "Điểm lôi đài : " + conn.p.point_king_cup);
                    break;
                }
                case 4:
                    KingCupManager.rewardKingCup(conn.p);
                    break;
                // Đệ tử
                case 5:
                    if (conn.p.squire != null) {
                        conn.p.squire.switchToSquire(conn.p);
                    } else {
                        Service.send_notice_box(conn,"Chú ý KTG và tìm đứa bé để dắt đi");
                        //.send_box_input_yesno(conn, -127, "Bạn có muốn nhận đệ tử với giá 5000 ngọc?");
                    }
                    break;
                case 6:
                    if (conn.p.squire != null) {
                        Service.send_notice_box(conn,"Muốn hủy đi ra NPC Master để hủy");
                      //  Service.send_box_input_yesno(conn, -124, "Huỷ đệ tử sẽ mất hết trang bị đang mặc.Bạn có muốn huỷ?");
                    } else {
                        Service.send_notice_box(conn, "Chưa có đệ tử");
                    }
                    break;
                case 7: {
                    conn.p.list_thao_kham_ngoc.clear();
                    for (int i = 0; i < conn.p.item.wear.length; i++) {
                        Item3 it = conn.p.item.wear[i];
                        if (it != null) {
                            short[] b = conn.p.item.check_kham_ngoc(it);
                            boolean check = false;
                            if ((b[0] != -2 && b[0] != -1) || (b[1] != -2 && b[1] != -1) || (b[2] != -2 && b[2] != -1)) {
                                check = true;
                            }
                            if (check) {
                                conn.p.list_thao_kham_ngoc.add(it);
                            }
                        }
                    }
                    String[] list_show = new String[]{"Trống"};
                    if (!conn.p.list_thao_kham_ngoc.isEmpty()) {
                        list_show = new String[conn.p.list_thao_kham_ngoc.size()];
                        for (int i = 0; i < list_show.length; i++) {
                            list_show[i] = conn.p.list_thao_kham_ngoc.get(i).name;
                        }
                    }
                    MenuController.send_menu_select(conn, 117, list_show);
                    break;
                }
                case 8: {
                    if (conn.p.level < 100) {
                        Service.send_notice_box(conn, "Bạn phải đạt từ cấp độ 100 mới có thể thực hiện chức năng này");
                        return;
                    }
                    conn.p.langPhuSuong();
                    break;
                }
            }
        } else if (idMenu == 1) {
            viewKingCup(conn, index);
        }
    }

    private static void viewKingCup(Session conn, byte index) {
        Vgo vgo = new Vgo();
        vgo.id_map_go = 102;
        vgo.x_new = 365;
        vgo.y_new = 395;
        KingCup.goToLD(conn.p, vgo, index);
    }

    private static void Menu_Pet_di_buon(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        switch (index) {
            case 0: {
                String notice = null;
                if (conn.p.pet_di_buon != null && conn.p.pet_di_buon.item.size() > 0) {
                    notice = "%s " + ItemTemplate3.item.get(3590).getName() + "\n";
                    notice += "%s " + ItemTemplate3.item.get(3591).getName() + "\n";
                    notice += "%s " + ItemTemplate3.item.get(3592).getName() + "\n";
                    int n1 = 0, n2 = 0, n3 = 0;
                    for (int i = 0; i < conn.p.pet_di_buon.item.size(); i++) {
                        if (null == conn.p.pet_di_buon.item.get(i)) {
                            n3++;
                        } else switch (conn.p.pet_di_buon.item.get(i)) {
                            case 3590 -> n1++;
                            case 3591 -> n2++;
                            default -> n3++;
                        }
                    }
                    notice = String.format(notice, n1, n2, n3);
                } else {
                    notice = "Trống";
                }
                Service.send_notice_box(conn, notice);
                break;
            }
            case 1: {
                break;
            }
            case 2: {
                if (conn.p.get_ngoc() > 50) {
                    conn.p.pet_di_buon.update_hp(conn.p, 100);
                } else {
                    Service.send_notice_box(conn, "Không đủ 5 ngọc");
                }
                break;
            }
            case 3: {
                if (conn.p.get_ngoc() > 50) {
                    conn.p.pet_di_buon.update_speed(conn.p);
                } else {
                    Service.send_notice_box(conn, "Không đủ 5 ngọc");
                }
                break;
            }
        }
    }

    private static void Menu_Mr_Frank(Session conn, byte index) throws IOException {// Đệ tử
        if (conn.p.level < 40) {
            Service.send_notice_box(conn, "Yeu cau cap do 40");
            return;
        }
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.map.map_id != 17) {
            return;
        }
        if (conn.status != 0) {
            Service.send_notice_box(conn, conn.language.chuakichhoat);
            return;
        }
        switch (index) {
            case 0: {
                Service.send_box_UI(conn, 39);
                break;
            }
            case 1: {
                if (conn.p.isRobber()) {
                    if (conn.p.pet_di_buon != null && Math.abs(conn.p.pet_di_buon.x - conn.p.x) < 75
                            && Math.abs(conn.p.pet_di_buon.y - conn.p.y) < 75) {
                        //
                        int vang_recei = 0;
                        for (int i = 0; i < conn.p.pet_di_buon.item.size(); i++) {
                            vang_recei += (conn.p.pet_di_buon.item.get(i) - 3589) * 100_000;
                        }
                        if (vang_recei > 0) {
                            conn.p.update_vang(vang_recei, "Nhận %s vàng từ việc cướp.");
                            conn.p.point_z6 += vang_recei;
                            //
                            Message mout = new Message(8);
                            mout.writer().writeShort(conn.p.pet_di_buon.ID);
                            for (int i = 0; i < conn.p.map.players.size(); i++) {
                                Player p0 = conn.p.map.players.get(i);
                                if (p0 != null) {
                                    p0.conn.addmsg(mout);
                                }
                            }
                            mout.cleanup();
                            //
                            Pet_di_buon_manager.remove(conn.p.pet_di_buon.name);
                            conn.p.pet_di_buon = null;
                            Service.send_notice_box(conn, "Nhận được " + vang_recei + " vàng!");
                        }
                    } else {
                        Service.send_notice_box(conn, "Ta không thấy con vật đi buôn của ngươi");
                    }
                } else {
                    Service.send_notice_box(conn, "Không phải là cướp đừng nói chuyện với ta.");
                }
                break;
            }
            case 2: {
                Item3 itbag = new Item3();
                itbag.id = 3593;
                itbag.clazz = ItemTemplate3.item.get(3593).getClazz();
                itbag.type = ItemTemplate3.item.get(3593).getType();
                itbag.level = ItemTemplate3.item.get(3593).getLevel();
                itbag.icon = ItemTemplate3.item.get(3593).getIcon();
                itbag.op = new ArrayList<>();
                itbag.op.addAll(ItemTemplate3.item.get(3593).getOp());
                itbag.color = 5;
                itbag.part = ItemTemplate3.item.get(3593).getPart();
                itbag.tier = 0;
                itbag.islock = true;
                itbag.time_use = 0;
                // thao do
                if (conn.p.item.wear[11] != null && conn.p.item.wear[11].id != 3593 && conn.p.item.wear[11].id != 3599
                        && conn.p.item.wear[11].id != 3596) {
                    Item3 buffer = conn.p.item.wear[11];
                    conn.p.item.wear[11] = null;
                    conn.p.item.add_item_inventory3(buffer);
                }
                itbag.name = ItemTemplate3.item.get(3593).getName() + " [Khóa]";
                itbag.UpdateName();
                conn.p.item.wear[11] = itbag;
                conn.p.fashion = Part_fashion.get_part(conn.p);
                Service.send_notice_box(conn, "Nhận thành công");
                break;
            }
        }
    }

    private static void Menu_Graham(Session conn, byte index) throws IOException {// Đệ tử
        if (conn.p.level < 40) {
            Service.send_notice_box(conn, "Yeu cau cap do 40");
            return;
        }
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.map.map_id != 8) {
            return;
        }
        if (conn.status != 0) {
            Service.send_notice_box(conn, conn.language.chuakichhoat);
            return;
        }
        switch (index) {
            case 0: {
                Service.send_box_UI(conn, 32);
                break;
            }
            case 1: {
                if (conn.p.isTrader()) {
                    if (conn.p.pet_di_buon != null && Math.abs(conn.p.pet_di_buon.x - conn.p.x) < 75
                            && Math.abs(conn.p.pet_di_buon.y - conn.p.y) < 75) {
                        //
                        int vang_recei = 0;
                        for (int i = 0; i < conn.p.pet_di_buon.item.size(); i++) {
                            vang_recei += (conn.p.pet_di_buon.item.get(i) - 3589) * 50_000;
                        }
                        if (vang_recei > 0) {
                            conn.p.update_vang(vang_recei, "Nhận %s vàng từ đi buôn");
                            conn.p.point_z6 += vang_recei;
                            //
                            Message mout = new Message(8);
                            mout.writer().writeShort(conn.p.pet_di_buon.ID);
                            for (int i = 0; i < conn.p.map.players.size(); i++) {
                                Player p0 = conn.p.map.players.get(i);
                                if (p0 != null) {
                                    p0.conn.addmsg(mout);
                                }
                            }
                            mout.cleanup();
                            //
                            Pet_di_buon_manager.remove(conn.p.pet_di_buon.name);
                            conn.p.pet_di_buon = null;
                            Service.send_notice_box(conn, "Nhận được " + vang_recei + " vàng!");
                        } else {
                            Service.send_notice_box(conn, "Vật đi buôn không có hàng để bán");
                        }
                    } else {
                        Service.send_notice_box(conn, "Ta không thấy con vật đi buôn của ngươi");
                    }
                } else {
                    Service.send_notice_box(conn, "Ta chỉ tiếp các thương nhân");
                }
                break;
            }
            case 2: {
                Item3 itbag = new Item3();
                itbag.id = 3599;
                itbag.clazz = ItemTemplate3.item.get(3599).getClazz();
                itbag.type = ItemTemplate3.item.get(3599).getType();
                itbag.level = ItemTemplate3.item.get(3599).getLevel();
                itbag.icon = ItemTemplate3.item.get(3599).getIcon();
                itbag.op = new ArrayList<>();
                itbag.op.addAll(ItemTemplate3.item.get(3599).getOp());
                itbag.color = 5;
                itbag.part = ItemTemplate3.item.get(3599).getPart();
                itbag.tier = 0;
                itbag.islock = true;
                itbag.time_use = 0;
                // tháo đồ
                if (conn.p.item.wear[11] != null && conn.p.item.wear[11].id != 3593 && conn.p.item.wear[11].id != 3599
                        && conn.p.item.wear[11].id != 3596) {
                    Item3 buffer = conn.p.item.wear[11];
                    conn.p.item.wear[11] = null;
                    conn.p.item.add_item_inventory3(buffer);
                }
                itbag.name = ItemTemplate3.item.get(3599).getName() + " [Khóa]";
                itbag.UpdateName();
                conn.p.item.wear[11] = itbag;
                conn.p.fashion = Part_fashion.get_part(conn.p);
                Service.send_notice_box(conn, "Nhận thành công");
                break;
            }
            default: {
                Service.send_notice_box(conn, "Chưa có chức năng");
                break;
            }
        }
    }

    private static void Menu_Mr_Dylan(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.map.map_id != 52) {
            return;
        }
        if (!conn.p.isTrader()) {
            Service.send_notice_box(conn, "Không phải là thương nhân đừng nói chuyện với ta.");
            return;
        }
        if (conn.p.pet_di_buon != null && Math.abs(conn.p.pet_di_buon.x - conn.p.x) < 75
                && Math.abs(conn.p.pet_di_buon.y - conn.p.y) < 75) {
            if (index == 0) {
                Service.send_box_UI(conn, 31);
            }
        } else {
            Service.send_notice_box(conn, "Ta không thấy con vật đi buôn của ngươi");
        }
    }

    private static void Menu_NauKeo(Session conn, byte index) throws IOException {
        if (Manager.gI().event == 1) {
            switch (index) {
                case 0: {
                    // Service.send_box_input_text(conn, 11, "Nhập số lượng", new String[] {"Số lượng :"});
                    if (conn.p.get_ngoc() < 10) {
                        Service.send_notice_box(conn, "Không đủ 10 ngọc");
                        return;
                    }
                    if (EventManager.eventManager.time <= 30) {
                        Service.send_notice_box(conn, "Không thể tăng tốc");
                        return;
                    }
                    conn.p.update_ngoc(-10);
                    EventManager.eventManager.update(1);
                    Service.send_notice_box(conn, "Tăng tốc thành công");
                    break;
                }
                case 1: {
                    Service.send_notice_box(conn, "Nguyên liệu cần để nấu kẹo như sau: Đường, Sữa, Bơ, Vani\r\n"
                            + "- Mỗi ngày server cho nấu kẹo 1 lần vào lúc 17h , thời gian nấu là 2 tiếng.\r\n"
                            + "- Thời gian đăng ký là từ 19h ngày hôm trước đến 16h30 ngày hôm sau. Phí đăng ký là 5 ngọc\r\n"
                            + "- Một lần tăng tốc mất 10 ngọc và sẽ giảm được 2 phút nấu\r\n"
                            + "- Số kẹo tối đa nhận được là 20 kẹo.Tuy nhiên nếu các hiệp sĩ góp càng nhiều thì càng có lợi vì 10 người chơi góp nhiều nguyên liệu nhất sẽ được cộng thêm 20 cái\r\n"
                            + "+ Số kẹo nhận được sẽ tính theo công thức 1 Kẹo = 1 Đường + 1 Sữa + 1 Bơ+ 1 Vani");
                    break;
                }
                case 2: {
                    Service.send_notice_box(conn,
                            "Thông tin:\nĐã góp : " + Event_1.get_keo_now(conn.p.name) + "\nThời gian nấu còn lại : "
                                    + ((EventManager.eventManager.time == 0) ? "Không trong thời gian nấu"
                                    : ("Còn lại " + EventManager.eventManager.time + "p")));
                    break;
                }
                case 3: {
                    send_menu_select(conn, 120, Event_1.get_top_naukeo());
                    break;
                }
            }
        }
    }

    private static void Menu_Event(Session conn, byte index) throws IOException {
        if (Manager.gI().event == 1) {
            switch (index) {
                case 0: {
                    Service.send_box_input_text(conn, 10, "Nhập số lượng", new String[]{"Số lượng :"});
                    break;
                }
                case 1: {
                    Service.send_notice_box(conn,
                            "Để đổi thành Hộp đồ chơi hoàn chỉnh theo công thức: 20.000 vàng + 50 Bức tượng rồng + 50 Kiếm đồ chơi + 50 Đôi giày nhỏ xíu + 50 Trang phục tí hon + 50 Mũ lính chì."
                                    + "\nĐể đổi thành Túi kẹo hoàn chỉnh theo công thức: 50.000 vàng + 5 Kẹo.");
                    break;
                }
                case 2: {
                    // System.out.println("DEBUG: Vào case 2 đăng ký event");
                    if (!Event_1.check_time_can_register()) {
                        //    System.out.println("DEBUG: Không trong thời gian đăng ký");
                        Service.send_notice_box(conn, "Không trong thời gian đăng ký!");
                        return;
                    }
                    // System.out.println("DEBUG: Đang kiểm tra đã đăng ký chưa");
                    if (EventManager.check(EventManager.registerList, conn.p.name)) {
                        //  System.out.println("DEBUG: Đã đăng ký rồi");
                        Service.send_notice_box(conn, "Đã đăng ký rồi, quên à!");
                        return;
                    }
                    //  System.out.println("DEBUG: Thêm người chơi đăng ký mới");
                    if (EventManager.registerList == null) {
                        System.out.println("DEBUG: registerList null!");
                        EventManager.registerList = new ArrayList<>();
                    }
                    EventManager.registerList.add(new EventManager.PlayerRegister(conn.p.name));

                    if (Event_1.list_naukeo == null) {
                        //   System.out.println("DEBUG: list_naukeo null! Khởi tạo mới");
                        Event_1.list_naukeo.clear(); // Xóa tất cả phần tử bên trong mà không gán lại biến

                    }
                    Event_1.list_naukeo.put(conn.p.name, 0);

                    // System.out.println("DEBUG: Đăng ký thành công");
                    Service.send_notice_box(conn, "Đăng ký thành công, có thể góp nguyên liệu rồi");
                    break;
                }


                case 3: {
                    if (!Event_1.check_time_can_register()) {
                        Service.send_notice_box(conn, "Không trong thời gian đăng ký!");
                        return;
                    }
                    if (Event_1.check(conn.p.name)) {
                        Service.send_box_input_text(conn, 11, "Nhập số lượng", new String[]{"Số lượng :"});
                    } else {
                        Service.send_notice_box(conn, "Chưa đăng ký nấu kẹo, hãy đăng ký!");
                    }
                    break;
                }
                case 4: {
                    int quant = Event_1.get_keo(conn.p.name);
                    if (quant > 0) {
                        quant = Math.min(quant, 20);
                        if (Event_1.list_bxh_naukeo_name.contains(conn.p.name)) {
                            quant += 20;
                        }
                        quant *= 3;

                        int freeSlot = conn.p.item.get_inventory_able();
                        //  System.out.println("DEBUG: Người chơi " + conn.p.name + " nhận kẹo, số lượng = " + quant + ", chỗ trống inventory = " + freeSlot);

                        if (freeSlot < 1) {
                            Service.send_notice_box(conn, "Hành trang không đủ chỗ trống để nhận kẹo!");
                            return;
                        }

                        Item47 it = new Item47();
                        it.category = 4;
                        it.id = 162;
                        it.quantity = (short) quant;

                        conn.p.item.add_item_inventory47(4, it);
                        Service.send_notice_box(conn, "Nhận được " + quant + " kẹo");

                        // Nếu có hàm update inventory hoặc gửi packet, gọi ở đây
                        // Ví dụ: conn.p.item.updateInventory();
                        // hoặc Service.updateInventory(conn);

                    } else {
                        Service.send_notice_box(conn, "Đã nhận rồi hoặc chưa tham gia!");
                    }
                    break;
                }


                case 5: {
                    Service.send_box_input_text(conn, 12, "Nhập số lượng", new String[]{"Số lượng :"});
                    break;
                }
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13: {
                    //  System.out.println("DEBUG: Người chơi " + conn.p.name + " chọn mục " + index);

                    // Kiểm tra chỗ trống hành trang
                    int bagAble = conn.p.item.get_bag_able();
                    //  System.out.println("DEBUG: Chỗ trống hành trang: " + bagAble);
                    if (bagAble < 1) {
                        Service.send_notice_box(conn, "Hành trang không đủ chỗ trống!");
                        //   System.out.println("DEBUG: Hành trang không đủ chỗ trống, thoát.");
                        return;
                    }

                    short[] id_receiv = new short[]{4626, 3269, 3610, 4636, 4709, 4710, 281, 3616};
                    short[] tuikeo_required = new short[]{1200, 1000, 60, 60, 30, 30, 15, 60};
                    short[] hopdochoi_required = new short[]{120, 120, 60, 60, 30, 30, 15, 60};
                    int[] ngoc_required = new int[]{360, 330, 60, 60, 60, 60, 15, 300};

                    int idx = index - 6;

                    // Kiểm tra nguyên liệu và ngọc
                    int tuikeo_have = conn.p.item.total_item_by_id(4, 157);
                    int hopdochoi_have = conn.p.item.total_item_by_id(4, 158);
                    int ngoc_have = conn.p.get_ngoc();

                    // System.out.println("DEBUG: Nguyên liệu túi kẹo hiện có: " + tuikeo_have + ", yêu cầu: " + tuikeo_required[idx]);
                    //  System.out.println("DEBUG: Nguyên liệu hộp đồ chơi hiện có: " + hopdochoi_have + ", yêu cầu: " + hopdochoi_required[idx]);
                    // System.out.println("DEBUG: Ngọc hiện có: " + ngoc_have + ", yêu cầu: " + ngoc_required[idx]);

                    if (tuikeo_required[idx] > tuikeo_have) {
                        Service.send_notice_box(conn, "Không đủ " + tuikeo_required[idx] + " túi kẹo!");
                        //   System.out.println("DEBUG: Không đủ túi kẹo, thoát.");
                        return;
                    }
                    if (hopdochoi_required[idx] > hopdochoi_have) {
                        Service.send_notice_box(conn, "Không đủ " + hopdochoi_required[idx] + " hộp đồ chơi!");
                        //   System.out.println("DEBUG: Không đủ hộp đồ chơi, thoát.");
                        return;
                    }
                    if (ngoc_required[idx] > ngoc_have) {
                        Service.send_notice_box(conn, "Không đủ " + ngoc_required[idx] + " ngọc!");
                        //   System.out.println("DEBUG: Không đủ ngọc, thoát.");
                        return;
                    }

                    if (index != 12) {
                        // Tạo item3
                        Item3 itbag = new Item3();
                        ItemTemplate3 it_temp = ItemTemplate3.item.get(id_receiv[idx]);
                        itbag.id = it_temp.getId();
                        itbag.name = it_temp.getName();
                        itbag.clazz = it_temp.getClazz();
                        itbag.type = it_temp.getType();
                        itbag.level = 10;
                        itbag.icon = it_temp.getIcon();
                        itbag.op = new ArrayList<>();
                        itbag.op.addAll(it_temp.getOp());
                        itbag.color = it_temp.getColor();
                        itbag.part = it_temp.getPart();
                        itbag.tier = 0;
                        itbag.islock = false;

                        // Kiểm tra id có phải 4626 hoặc 4761 thì set vĩnh viễn, ngược lại set 7 ngày
                        if (itbag.id == 4626 || itbag.id == 3269) {
                            itbag.time_use = 0;  // hoặc ko gán gì nếu mặc định vĩnh viễn
                            //   System.out.println("DEBUG: Thêm item vĩnh viễn id=" + itbag.id + ", tên=" + itbag.name);
                        } else {
                            // Gán thời gian hết hạn 7 ngày tính bằng timestamp
                            itbag.expiry_date = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000;
                            //  System.out.println("DEBUG: Thêm item có hạn 7 ngày id=" + itbag.id + ", tên=" + itbag.name);
                        }

                        conn.p.item.add_item_inventory3(itbag);
                        //  System.out.println("DEBUG: Đã thêm item3 vào inventory.");
                        if (itbag.time_use == 0 && itbag.expiry_date == 0) {
                            // Hiển thị vĩnh viễn
                            Service.send_notice_box(conn, "Nhận được " + itbag.name + " (vĩnh viễn).");
                        } else {
                            // Hiển thị có hạn
                            Service.send_notice_box(conn, "Nhận được " + itbag.name + " (7 ngày).");
                        }
                    } else {
                        // Tạo item47
                        Item47 itbag = new Item47();
                        itbag.id = id_receiv[idx];
                        itbag.quantity = (short) 20;
                        itbag.category = 4;

                        // Xử lý nếu cần set thời gian dùng cho item47 (tùy hệ thống)
                        // Ví dụ:
                        // if (itbag.id != 4626 && itbag.id != 4761) {
                        //     itbag.expiry_date = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000;
                        // }

                        // System.out.println("DEBUG: Thêm item47 id=" + itbag.id + ", số lượng=" + itbag.quantity);
                        conn.p.item.add_item_inventory47(4, itbag);
                        //  System.out.println("DEBUG: Đã thêm item47 vào inventory.");
                        Service.send_notice_box(conn, "Nhận được 20 xe trượt tuyết.");
                    }


                    //  System.out.println("DEBUG: Trước khi trừ nguyên liệu và ngọc");
                    // // System.out.println("DEBUG: Túi kẹo trước trừ: " + conn.p.item.total_item_by_id(4, 157));
                    //  System.out.println("DEBUG: Hộp đồ chơi trước trừ: " + conn.p.item.total_item_by_id(4, 158));
                    //  System.out.println("DEBUG: Ngọc trước trừ: " + conn.p.get_ngoc());

                    conn.p.item.remove(4, 157, tuikeo_required[idx]);
                    conn.p.item.remove(4, 158, hopdochoi_required[idx]);
                    conn.p.update_ngoc(-ngoc_required[idx]);

                    //  System.out.println("DEBUG: Sau khi trừ nguyên liệu và ngọc");
                    //  System.out.println("DEBUG: Túi kẹo sau trừ: " + conn.p.item.total_item_by_id(4, 157));
                    //  System.out.println("DEBUG: Hộp đồ chơi sau trừ: " + conn.p.item.total_item_by_id(4, 158));
                    //   System.out.println("DEBUG: Ngọc sau trừ: " + conn.p.get_ngoc());

                    // Cập nhật giao diện char inventory (nếu có)
                    conn.p.item.char_inventory(4);
                    conn.p.item.char_inventory(3);

                    break;
                }


            }
        }
    }

    private static void Menu_Miss_Anwen(Session conn, byte index) throws IOException {
        if (index == 0) {
            conn.p.veLang();
        }
    }

    private static void Menu_Vua_Chien_Truong(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        //sua duoi day
        switch (index) {
            case 0: {
                if (conn.p.diemdanh == 1) {
                    conn.p.diemdanh = 0;
                    int ngoc_ = Util.random(200, 800);
                    int vang_ = Util.random(10000, 100000);
                    conn.p.update_ngoc(ngoc_);
                    conn.p.update_vang(vang_, "Nhan vang");
                    conn.p.item.char_inventory(5);
                    Service.send_notice_box(conn,
                            "Tặng bạn nè: " + ngoc_ + " ngọc," + vang_ + "Vàng.");
                } else {
                    Service.send_notice_box(conn, "Ăn xin thế thôi!!!");
                }
                break;
            }
            case 1: {
                String[] options = new String[]{
                        "Cầu hôn",
                        "Ly hôn",
                        "Nâng cấp nhẫn",
                        "Hướng dẫn"
                };
                MenuController.send_menu_select(conn, 114, options);
                break;
            }
        }
    }

    private static void Menu_TienCanh(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                Service.send_msg_data(conn, 23, "create_wings");
                break;
            }
            case 1: {
                Message m2 = new Message(77);
                m2.writer().writeByte(6);
                conn.addmsg(m2);
                m2.cleanup();
                //
                m2 = new Message(77);
                m2.writer().writeByte(1);
                m2.writer().writeUTF("Nâng cấp cánh");
                conn.addmsg(m2);
                m2.cleanup();
                conn.p.is_create_wing = false;
                break;
            }
            case 2: {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < conn.p.item.inventory3.length; i++) {
                    Item3 it = conn.p.item.inventory3[i];
                    if (it != null && it.type == 7 && it.tier > 10) {
                        list.add(it.name + " +" + it.tier);
                    }
                }

                String[] list_2 = new String[]{"Trống"};
                if (!list.isEmpty()) {
                    list_2 = new String[list.size()];
                    for (int i = 0; i < list_2.length; i++) {
                        list_2[i] = list.get(i);
                    }
                }
                MenuController.send_menu_select(conn, 210, list_2);
                break;
            }
            case 3: {
                if (conn.p.item.wear[10] != null) {
                    Item3 item = conn.p.item.wear[10];
                    int quant1 = 40;
                    int quant2 = 10;
                    int quant3 = 50;
                    for (int i = 0; i < item.tier; i++) {
                        quant1 += GameSrc.wing_upgrade_material_long_khuc_xuong[i];
                        quant2 += GameSrc.wing_upgrade_material_kim_loai[i];
                        quant3 += GameSrc.wing_upgrade_material_da_cuong_hoa[i];
                    }
                    if (item.tier > 15) {
                        quant1 /= 2;
                        quant2 /= 2;
                        quant3 /= 2;
                    } else {
                        quant1 /= 3;
                        quant2 /= 3;
                        quant3 /= 3;
                    }
                    Service.send_box_input_yesno(conn, 114, "Bạn có muốn tách cánh này và nhận được: " + quant1
                            + " lông và khúc xương, " + quant2 + " kim loại, " + quant3 + " đá cường hóa?");
                } else {
                    Service.send_notice_nobox_white(conn, "Mặc cánh lên người để tách");
                }
                break;
            }
            case 4: {
                if (conn.p.item.get_inventory_able() < 1) {
                    Service.send_notice_nobox_white(conn, "Hành trang đầy");
                    return;
                }
                Item3 item_remove = conn.p.item.wear[10];
                if (item_remove != null) {
                    conn.p.item.wear[10] = null;
                    conn.p.item.add_item_inventory3(item_remove);
                    conn.p.fashion = Part_fashion.get_part(conn.p);
                    Service.send_wear(conn.p);
                    Service.send_char_main_in4(conn.p);
                    MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                    Service.send_notice_box(conn, "Đã tháo " + item_remove.name);
                } else {
                    Service.send_notice_nobox_white(conn, "Không thể thực hiện");
                }
                break;
            }
        }
    }

    private static int OptionId() {
        int[] validOptionIds = new int[]{
                0, 1, 2, 3, 4, 5

        };
        return validOptionIds[Util.nextInt(0, validOptionIds.length)];
    }

    private static int randomTaiNgheOptionId() {
        int[] validOptionIds = new int[]{
                29, 30
        };
        return validOptionIds[Util.nextInt(0, validOptionIds.length)];
    }

    private static int randomOptionId() {
        int[] validOptionIds = new int[]{
                7, 8, 9, 10, 11, 27

        };
        return validOptionIds[Util.nextInt(0, validOptionIds.length)];
    }

    private static void Menu_Kich_Hoat_Canh(Session conn, byte index) throws IOException {
        if (conn.p.get_ngoc() < 500) {
            Service.send_notice_box(conn, "Không đủ 500 ngọc");
            return;
        }
        conn.p.update_ngoc(-500);
        Log.gI().add_log(conn.p.name, "hết 500 ngọc");
        Item3 it_process = null;
        for (int i = 0; i < conn.p.item.inventory3.length; i++) {
            Item3 it = conn.p.item.inventory3[i];
            if (it != null && it.type == 7 && it.tier >= 10) {
                if (index == 0) {
                    it_process = it;
                    break;
                }
                index--;
            }
        }
        if (it_process != null) {
            Option[] process = new Option[2];
            for (int i = 0; i < it_process.op.size(); i++) {
                if (it_process.op.get(i).id >= 7 && it_process.op.get(i).id <= 11) {
                    if (process[0] == null) {
                        process[0] = it_process.op.get(i);
                    } else if (process[1] == null) {
                        process[1] = it_process.op.get(i);
                    } else {
                        break;
                    }
                }
            }
            if (process[1] == null) {
                Option option = new Option(Util.random(7, 12), 0);
                while (option.id == process[0].id) {
                    option.id = (byte) Util.random(7, 12);
                }
                option.param = process[0].param;
                it_process.op.add(option);
            } else if (process[0] != null) {
                process[1].id = (byte) Util.random(7, 12);
                while (process[1].id == process[0].id) {
                    process[1].id = (byte) Util.random(7, 12);
                }
            }
            Service.send_notice_box(conn, "Thành công");
            conn.p.item.char_inventory(3);
        }
    }


    private static void Menu_Clan_Manager(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.myclan.mems.get(0).name.equals(conn.p.name)) {
            switch (index) {
                case 0: {
                    conn.p.myclan.open_box_clan(conn);
                    break;
                }
                case 1: {
                    if (conn.p.myclan.get_percent_level() >= 1000) {
                        Service.send_box_input_yesno(conn, 118,
                                "Bạn có muốn nâng cấp bang lên level " + (conn.p.myclan.level + 1) + " với "
                                        + (Clan.vang_upgrade[1] * conn.p.myclan.level) + " vàng "
                                        + " với " + (Clan.ngoc_upgrade[1] * conn.p.myclan.level) + " ngọc không?");
                    } else {
                        Service.send_notice_box(conn, "Chưa đủ exp để nâng cấp!");
                    }
                    break;
                }
                case 2: {
                    Service.send_box_input_yesno(conn, 116,
                            "Hãy xác nhận việc hủy bang?");
                    break;
                }
                case 3: {
                    Service.send_box_input_text(conn, 13, "Nhập tên :", new String[]{"Nhập tên :"});
                    break;
                }
            }
        }
    }

    private static void Menu_Shop_Clan(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.myclan.mems.get(0).name.equals(conn.p.name)) {
            switch (index) {
                case 0: {
                    Service.send_box_UI(conn, 30);
                    break;
                }
                case 1: {
                    Service.send_box_UI(conn, 29);
                    break;
                }
            }
        }
    }

    private static void Menu_PhoChiHuy(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                if (conn.p.level < 30) {
                    Service.send_notice_box(conn, "Đạt level 30 mới có thể vào phó bản");
                    return;
                }

                if (conn.p.party != null) {
                    Service.send_notice_box(conn, "Phó bản hiện tại chỉ đi 1 mình");
                    return;
                }

                boolean hasItem53 = conn.p.hasItem47_53();
                int free = conn.p.count_dungeon;


                // ===== HIỂN THỊ ĐÚNG TRẠNG THÁI =====
                if (free > 0) {
                    Service.send_box_input_yesno(
                            conn,
                            119,
                            "Bạn đang còn " + free + " lượt miễn phí.\n"
                                    + "Bạn có muốn vào phó bản ngay?"
                    );

                } else if (hasItem53) {
                    Service.send_box_input_yesno(
                            conn,
                            119,
                            "Bạn đã hết lượt miễn phí.\n"
                                    + "Sử dụng 1 Đồng tiền Horae để vào phó bản?"
                    );

                } else {
                    Service.send_box_input_yesno(
                            conn,
                            119,
                            "Bạn đã hết lượt miễn phí.\n"
                                    + "Sử dụng 1000 ngọc để vào phó bản?"
                    );
                }
                break;
            }

            case 1: {
                break;
            }

            case 2: {
                Service.send_box_input_yesno(
                        conn,
                        -126,
                        "Để trở thành hiệp sĩ bạn cần có 2 điểm chiến trường,\n"
                                + "bạn có muốn thực hiện?"
                );
                break;
            }

            case 4: { // Đệ tử
                if (!conn.p.isOwner) {
                    return;
                }
                ChiemThanhManager.ClanRegister(conn.p);
                break;
            }
            case 5: {
                Service.send_notice_box(conn,
                        "|7|HƯỚNG DẪN CHIẾM THÀNH\n" +
                                "1. Thời gian:\n" +
                                "- Đăng ký: 20h45 - 21h30 (Thứ 2, 4, 6).\n" +
                                "- Bắt đầu chiến: 21h30 - 23h00.\n" +
                                "2. Điều kiện:\n" +
                                "- Chủ bang đăng ký tốn 10.000.000 quỹ bang.\n" +
                                "- Thành viên tham gia phải đạt Level 60+.\n" +
                                "3. Luật chơi:\n" +
                                "- Phá Trụ Chính để tranh giành Đá.\n" +
                                "- Bang nào giữ Đá liên tục 10 phút hoặc giữ Đá khi hết giờ sẽ Chiến Thắng.\n" +
                                "4. Phần thưởng:\n" +
                                "- Bang thắng nhận Thuế thu nhập server.\n" +
                                "- Thành viên nhận Hộp quà may mắn."
                );
                break;
            }
            case 6: { // Đệ tử
                if (!conn.p.isOwner) {
                    return;
                }
                ChiemThanhManager.huyDangKyChiemThanh(conn.p);
                break;
            }
        }
    }


    private static void Menu_Pet_Manager(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                Service.send_box_UI(conn, 21);
                break;
            }
            case 1: {
                Service.send_box_UI(conn, 22);
                break;
            }
            case 2: {
                Service.send_box_UI(conn, 23);
                break;
            }
            case 3: {
                if (conn.p.pet_follow_id != -1) {
                    for (Pet temp : conn.p.mypet) {
                        if (temp.is_follow) {
                            temp.is_follow = false;
                            Message m = new Message(44);
                            m.writer().writeByte(28);
                            m.writer().writeByte(1);
                            m.writer().writeByte(9);
                            m.writer().writeByte(9);
                            m.writer().writeUTF(temp.name);
                            m.writer().writeByte(temp.type);
                            m.writer().writeShort(conn.p.mypet.indexOf(temp)); // id
                            m.writer().writeShort(temp.level);
                            m.writer().writeShort(temp.getlevelpercent()); // exp
                            m.writer().writeByte(temp.type);
                            m.writer().writeByte(temp.icon);
                            m.writer().writeByte(temp.nframe);
                            m.writer().writeByte(temp.color);
                            m.writer().writeInt(temp.get_age());
                            m.writer().writeShort(temp.grown);
                            m.writer().writeShort(temp.maxgrown);
                            m.writer().writeShort(temp.sucmanh);
                            m.writer().writeShort(temp.kheoleo);
                            m.writer().writeShort(temp.theluc);
                            m.writer().writeShort(temp.tinhthan);
                            m.writer().writeShort(temp.maxpoint);
                            m.writer().writeByte(temp.op.size());
                            for (int i2 = 0; i2 < temp.op.size(); i2++) {
                                OptionPet temp2 = temp.op.get(i2);
                                m.writer().writeByte(temp2.id);
                                m.writer().writeInt(temp.getParam(temp2.id));
                                m.writer().writeInt(temp.getMaxDame(temp2.id));
                            }
                            conn.p.conn.addmsg(m);
                            m.cleanup();
                            Service.send_notice_box(conn, "Đã tháo " + temp.name);
                            break;
                        }
                    }
                    conn.p.pet_follow_id = -1;
                    Service.send_wear(conn.p);
                    Service.send_char_main_in4(conn.p);
                }
                break;
            }
        }
    }

    private static void Menu_Mr_Edgar(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                if (!conn.p.list_enemies.isEmpty()) {
                    String[] name = new String[conn.p.list_enemies.size()];
                    for (int i = 0; i < name.length; i++) {
                        name[i] = conn.p.list_enemies.get(name.length - i - 1);
                    }
                    send_menu_select(conn, 124, name);
                } else {
                    Service.send_notice_box(conn, "Danh sách chưa có ai");
                }
                break;
            }
            case 1: {
                Service.send_notice_box(conn,
                        "Bị người chơi khác pk thì sẽ được lưu vào danh sách, "
                                + "mỗi lần trả thù sẽ được đưa tới nơi kẻ thù đang đứng với chi phí chỉ vỏn vẹn 10 ngọc.\n"
                                + "Sau khi được đưa tới nơi, tên kẻ thù sẽ được loại ra khỏi danh sách");
                break;
            }
        }
    }

    private static void Menu_Zoro(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.myclan != null) {
            if (conn.p.myclan.mems.get(0).name.equals(conn.p.name)) {
                switch (index) {
                    case 0: {
                        send_menu_select(conn, 122,
                                new String[]{"Kho bang", "Nâng cấp bang", "Hủy bang hội", "Chuyển thủ lĩnh"});
                        break;
                    }
                    case 1: {
                        send_menu_select(conn, 127, new String[]{"Shop vật phẩm bang", "Shop Icon Bang Hội"});
                        break;
                    }
                    case 2: {
                        String guide = "--- HƯỚNG DẪN BANG ---\n"
                                + "1. Mua Vật Phẩm:\n"
                                + "   - Có thể mua icon trong shop icon bang\n"
                                + "   - Mua thú cưỡi, v.v ở shop vật phẩm bang.\n"
                                + "   - Mọi thứ đều có thể mua bằng vàng, ngọc trong bang.\n"

                                + "2. Cách kiếm EXP Bang:\n"
                                + "   - Các thành viên cùng nhau đánh quái, đi phó bản.\n"

                                + "3. Nâng cấp bang:\n"
                                + "   - Khi nâng cấp đến cấp 2, 10, 20, 30 sẽ được cộng thêm thành viên.\n"
                                + "   - Max thành viên là 5 và max level là 100.";

                        // Gửi thông báo về máy người chơi
                        Service.send_notice_box(conn, guide);
                        break;
                    }
                }
            } else {
                switch (index) {
                    case 0: {
                        conn.p.myclan.open_box_clan(conn);
                        break;
                    }
                }
            }
        } else {
            if (index == 0) {
                if (conn.p.level < 30) {
                    Service.send_notice_box(conn, "Yêu cầu cấp độ 30");
                    return;
                }
                Service.send_box_input_yesno(conn, 70, "Bạn có muốn đăng ký tạo bang với phí là 25.000 ngọc");
            }else if(index == 1) {
                // Soạn nội dung hướng dẫn
                String guide = "--- HƯỚNG DẪN BANG ---\n"
                        + "1. Tạo bang:\n"
                        + "   - Tạo bang cần tối thiểu level 30\n"
                        + "   - Tạo bang cần 25.000 ngọc.\n"
                        + "   - Khi tạo bang chỉ có 1 thành viên.\n"

                        + "2. Cách kiếm EXP Bang:\n"
                        + "   - Các thành viên cùng nhau đánh quái, đi phó bản.\n"

                        + "3. Nâng cấp bang:\n"
                        + "   - Khi nâng cấp đến cấp 2, 10, 20, 30 sẽ được cộng thêm thành viên.\n"
                        + "   - Max thành viên là 5 và max level là 100.";

                // Gửi thông báo về máy người chơi
                Service.send_notice_box(conn, guide);
            }
        }
    }

    private static void menuLuckyDrawVip(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        switch (index) {
            case 0: {
                Manager.gI().lucky_draw_vip.send_in4(conn.p);
                break;
            }
            case 1: {
                Service.send_box_input_text(conn, 3, "Vòng xoay Vip", new String[]{"Tham gia"});
                break;
            }
        }
    }

    private static void menuLuckyDrawNormal(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        switch (index) {
            case 0: {
                Manager.gI().lucky_draw_normal.send_in4(conn.p);
                break;
            }
            case 1: {
                Service.send_box_input_text(conn, 17, "Vòng xoay thường", new String[]{"Tham gia"});
                break;
            }
        }
    }

    private static void Menu_CuopBien(Session conn, byte index) throws IOException {// Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        switch (index) {
            case 2: {
                send_menu_select(conn, 125, new String[]{"Xem thông tin", "Tham gia"});
                break;
            }
            case 4: {
                send_menu_select(conn, 132, new String[]{"Xem thông tin", "Tham gia"});
                break;
            }
            default: {
                Service.send_notice_box(conn, "Chúc bạn chơi game vui vẻ");
                break;
            }
        }
    }

    public static void send_menu_select(Session conn, int idnpc, String[] menu) throws IOException {
        if (!conn.p.isdie) {
            if (menu != null && menu.length > 0) {
                Message m2 = new Message(-30);
                m2.writer().writeShort(idnpc);
                m2.writer().writeByte(0);
                m2.writer().writeByte(menu.length);
                for (int i = 0; i < menu.length; i++) {
                    m2.writer().writeUTF(menu[i]);
                }
                if (conn.ac_admin > 111) {
                    m2.writer().writeUTF("MENU : " + idnpc);
                } else {
                    m2.writer().writeUTF("MENU");
                }
                conn.addmsg(m2);
                m2.cleanup();
            }
        }
    }

    public static void send_menu_select(Session conn, int idnpc, String[] menu, byte idmenu) throws IOException {
        if (!conn.p.isdie) {
            if (menu != null && menu.length > 0) {
                Message m2 = new Message(-30);
                m2.writer().writeShort(idnpc);
                m2.writer().writeByte(idmenu);
                m2.writer().writeByte(menu.length);
                for (int i = 0; i < menu.length; i++) {
                    m2.writer().writeUTF(menu[i]);
                }
                if (conn.ac_admin > 111) {
                    m2.writer().writeUTF("MENU : " + idnpc);
                } else {
                    m2.writer().writeUTF("MENU");
                }
                conn.addmsg(m2);
                m2.cleanup();
            }
        }
    }

    private static void Menu_Aman(Session conn, byte index) throws IOException {
        if (conn.user.contains("knightauto_hsr_")) {
            switch (index) {
                case 0: { // Đăng ký tài khoản
                    if (conn.user.contains("knightauto_hsr_")) {
                        if (conn.p.level < 1) {
                            Service.send_notice_box(conn, "Đạt level 1 mới có thể đăng ký tài khoản");
                            return;
                        }
                        Service.send_box_input_text(conn, 6, "Đăng ký tài khoản",
                                new String[]{"Tên đăng nhập", "Mật khẩu"});
                    }
                    break;
                }
                default:
                    Service.send_notice_box(conn, "Tính năng chưa được hỗ trợ.");
            }
        } else {
            switch (index) {
                case 0: { // Rương đồ
                    if (conn.passbox != null && !conn.passbox.isEmpty()) {
                        Service.send_box_input_text(conn, 997, "Mật khẩu rương",
                                new String[]{"Nhập mật khẩu"});
                    } else {
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
                    }
                    break;
                }
                case 1: { // Nâng cấp rương
                    int count_update = (conn.p.maxBox - 14) / 7;
                    int gems_need = (count_update + 1) * 20;
                    if (count_update < 10) {
                        Service.send_box_input_yesno(conn, -128,
                                "Bạn có muốn nâng cấp thêm 7 ô giá " + gems_need + " ngọc?");
                    } else {
                        Service.send_notice_box(conn, "Rương đã nâng cấp tối đa!");
                    }
                    break;
                }

                case 2: { // Shop rương may mắn
                    Service.send_box_UI(conn, 48);
                    break;
                }
                case 3: {
                    Service.send_box_input_text(conn, 995, "Thêm Mật khẩu",
                            new String[]{"Mật khẩu mới", "Xác nhận"});
                    break;
                }
                case 4: { // Đổi mật khẩu tài khoản
                    Service.send_box_input_text(conn, 999, "Đổi mật khẩu",
                            new String[]{"Mật khẩu cũ", "Mật khẩu mới", "Xác nhận"});
                    break;
                }
                case 5: { // Đổi mật khẩu rương
                    Service.send_box_input_text(conn, 996, "Đổi mật khẩu rương",
                            new String[]{"Mật khẩu cũ", "Mật khẩu mới", "Xác nhận"});
                    break;
                }
                case 6: { // Xoá mật khẩu rương
                    Service.send_box_input_text(conn, 994, "Xoá mật khẩu rương",
                            new String[]{"Mật khẩu hiện tại"});
                    break;
                }
                case 7: { // Quên mật khẩu rương
                    // Đường link Fanpage và Zalo hỗ trợ
                    //String fanpageLink = "https://www.facebook.com/toicutie";  // Đường link Fanpage hỗ trợ
                    String zaloLink = "https://zalo.me/g/qoysmr777";  // Đường link Zalo hỗ trợ

                    // Thông báo liên hệ Fanpage và Zalo
                    String message = "Liên hệ Zalo tại: " + zaloLink + " để được hỗ trợ.\n";

                    // Lấy thông tin tài khoản và tài sản của người chơi
                    String userName = conn.user;  // Tên đăng nhập của người chơi
                    String playerName = conn.p.name;  // Tên nhân vật của người chơi
                    String playerId = String.valueOf(conn.id);  // ID của nhân vật

                    // Lấy thông tin vàng, ngọc, điểm tiêu xài và điểm danh
                    long gold = conn.p.vang;  // Lấy số vàng của người chơi (kiểu long)
                    int gems = conn.p.kimcuong;  // Lấy số ngọc của người chơi
                    int coin = conn.p.getCoin();
                    int spendingPoints = (int) conn.p.diemdanh;  // Ép kiểu long sang int
                    int spending = (int) conn.p.diem_tieu_sai;   // Điểm tiêu xài (kiểu int)

                    // Tạo thông tin tài khoản và tài sản để gửi cho người chơi
                    String accountInfo = "Thông tin tài khoản của bạn:\n" +
                            "Tên đăng nhập: " + userName + "\n" +
                            "Tên nhân vật: " + playerName + "\n" +
                            "ID nhân vật: " + playerId + "\n" +
                            "Vàng hiện có: " + gold + " vàng\n" +
                            "Ngọc hiện có: " + gems + " ngọc\n" +
                            "Coin hiện có: " + coin + " ngọc\n" +
                            "Điểm tiêu xài: " + spending + " điểm\n" +
                            "Điểm danh: " + spendingPoints + " điểm";

                    // Kết hợp thông tin tài khoản và đường link vào thông báo
                    message += "\n" + accountInfo; // Thêm thông tin tài khoản vào cuối thông báo

                    // Hiển thị thông báo cho người chơi
                    Service.send_notice_box(conn, message);  // Gửi cả thông tin tài khoản và link Fanpage + Zalo

                    break;
                }
                default:
                    Service.send_notice_box(conn, "Tính năng chưa được hỗ trợ.");
            }
        }
    }


    private static void Menu_Rank(Session conn, byte index, byte idMenu) throws IOException {
        if (idMenu == 0) {
            switch (index) {
                case 0: {
                    Rank.send(conn, 0);
                    break;
                }
                case 1: {
                    Rank.send(conn, 6);
                    break;
                }
                case 2: {
                    send_menu_select(conn, -32, new String[]{"Bang giàu có nhất", "Bang nhiều châu báu nhất", "Bang hùng mạnh nhất"}, (byte) 1);
                    break;
                }
                case 3: {
                    Rank.send(conn, 4);
                    break;
                }
                case 4: {
                    Rank.send(conn, 5);
                    break;
                }
                case 5: {
                    Rank.send(conn, 7); // Top Nạp → bạn chọn ID nào trong Rank
                    break;
                }
            }
        } else if (idMenu == 1) {
            switch (index) {
                case 0: {
                    Rank.send(conn, 2);
                    break;
                }
                case 1: {
                    Rank.send(conn, 3);
                    break;
                }
                case 2: {
                    Rank.send(conn, 1);
                    break;
                }
            }
        }
    }

    private static void Menu_Miss_Anna(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                Service.send_box_UI(conn, 38);
                break;
            }
            case 1: {
                List<String> list = new ArrayList<>();
                list.add("Dùng ngọc rồng 1 sao");
                list.add("Dùng ngọc rồng 2 sao");
                list.add("Dùng ngọc rồng 3 sao");
                list.add("Dùng ngọc rồng 4 sao");
                list.add("Dùng ngọc rồng 5 sao");
                list.add("Dùng ngọc rồng 6 sao");
                list.add("Dùng ngọc rồng 7 sao");
                list.add("Đổi quà đặc biệt");

                send_menu_select(conn, -303, list.toArray(new String[0]), (byte) 1); // ✅ ép kiểu rõ ràng
                break;
            }
            case 2: {
                Item3 item = conn.p.item.wear[11];
                if (item != null) {
                    Service.send_box_input_yesno(conn, 11, "Bạn có muốn tháo " + item.name);
                } else {
                    Service.send_box_input_text(conn, 0, "Nhập mã code", new String[]{"Code"});
                }
                break;
            }
            case 3: {
                Item3 item = conn.p.item.wear[21];
                if (item != null) {
                    Service.send_box_input_yesno(conn, 21, "Bạn có muốn tháo " + item.name);
                } else {
                    Service.send_box_input_text(conn, 0, "Nhập mã code", new String[]{"Code"});
                }
                break;
            }
            case 4: {
                Item3 item = conn.p.item.wear[22];
                if (item != null) {
                    Service.send_box_input_yesno(conn, 22, "Bạn có muốn tháo " + item.name);
                } else {
                    Service.send_box_input_text(conn, 0, "Nhập mã code", new String[]{"Code"});
                }
                break;
            }
            case 5: {
                Service.send_box_input_text(conn, 0, "Nhập mã code", new String[]{"Code"});
                break;
            }
        }
    }

    private static void Menu_Miss_Anna_Sub(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                doiAoChoang(conn, 1);
                break;
            }
            case 1: {
                doiAoChoang(conn, 2);
                break;
            }
            case 2: {
                doiAoChoang(conn, 3);
                break;
            }
            case 3: {
                doiAoChoang(conn, 4);
                break;
            }
            case 4: {
                doiAoChoang(conn, 5);
                break;
            }
            case 5: {
                doiAoChoang(conn, 6);
                break;
            }
            case 6: {
                doiAoChoang(conn, 7);
                break;
            }
            case 7: {
                doiQuaDacBiet(conn);
                break;
            }
        }
    }

    private static void doiAoChoang(Session conn, int sao) throws IOException {
        short idNgoc = (short) (463 + sao); // ngọc rồng 1 sao là 464, 7 sao là 470

        // Kiểm tra ngọc rồng có đủ 1 cái không
        if (conn.p.item.total_item_by_id(7, idNgoc) < 1) {
            Service.send_notice_box(conn, "Bạn không có đủ ngọc rồng " + sao + " sao.");
            return;
        }

        // Trừ 1 ngọc rồng tương ứng
        conn.p.item.remove(7, idNgoc, 1);

        // Phần còn lại như cũ...
        int idAoChoang = 4676 + (sao - 1);
        ItemTemplate3 temp3 = ItemTemplate3.item.get(idAoChoang);
        if (temp3 == null) {
            Service.send_notice_box(conn, "Không tìm thấy áo choàng tương ứng.");
            return;
        }

        Item3 aoChoang = new Item3();
        aoChoang.id = temp3.getId();
        aoChoang.name = temp3.getName();
        aoChoang.clazz = temp3.getClazz();
        aoChoang.type = temp3.getType();
        aoChoang.level = temp3.getLevel();
        aoChoang.icon = temp3.getIcon();
        aoChoang.op = new ArrayList<>();
        for (Option op_temp : temp3.getOp()) {
            aoChoang.op.add(new Option(op_temp.id, op_temp.getParam(0)));
        }
        aoChoang.color = 4;

        aoChoang.expiry_date = System.currentTimeMillis() + (long) sao * 24 * 60 * 60 * 1000;
        aoChoang.part = temp3.getPart();

        conn.p.item.add_item_inventory3(aoChoang);
        conn.p.item.char_inventory(3);

        Service.send_notice_box(conn, "Bạn đã nhận được " + aoChoang.name + " dùng trong " + sao + " ngày.");
    }

    private static void doiQuaDacBiet(Session conn) throws IOException {
        StringBuilder missing = new StringBuilder();
        boolean hasAll = true;

        // Kiểm tra từng viên từ 1 đến 7 sao
        for (short id = 464; id <= 470; id++) {
            int count = conn.p.item.total_item_by_id(7, id);
            if (count < 100) {
                hasAll = false;
                missing.append("- Ngọc rồng ").append(id - 463).append(" sao: thiếu ").append(100 - count).append(" viên\n");
            }
        }

        if (!hasAll) {
            Service.send_notice_box(conn, "Bạn chưa có đủ nguyên liệu:\n" + missing.toString());
            return;
        }

        // Nếu đủ thì mở menu chọn phần thưởng
        String[] listGift = {
                "Áo choàng tỷ phú",
                "Thời trang Công chúa rồng",
                "Dây chuyền mặt trăng"
        };
        send_menu_select(conn, -305, listGift, (byte) 1);
    }


    private static void MenuMissAnna_SubGift(Session conn, byte index) throws IOException {
        // Trừ 10 viên mỗi loại từ 1 đến 7 sao
        for (short id = 464; id <= 470; id++) {
            conn.p.item.remove(7, id, 100);
        }

        int[] itemIds = {4801, 4812, 2940}; // ID của 3 món
        if (index < 0 || index >= itemIds.length) return;

        int itemId = itemIds[index];
        ItemTemplate3 temp3 = ItemTemplate3.item.get(itemId);
        if (temp3 == null) {
            Service.send_notice_box(conn, "Không tìm thấy vật phẩm.");
            return;
        }

        Item3 item = new Item3();
        item.id = temp3.getId();
        item.name = temp3.getName();
        item.clazz = temp3.getClazz();
        item.type = temp3.getType();
        item.level = temp3.getLevel();
        item.icon = temp3.getIcon();
        item.op = temp3.getOp();
        item.part = temp3.getPart();
        item.color = 5; // Vật phẩm hiếm
        // Không set thời hạn → vĩnh viễn

        conn.p.item.add_item_inventory3(item);
        conn.p.item.char_inventory(3);

        Service.send_notice_box(conn, "Bạn đã nhận được: " + item.name);
    }

    private static void Menu_Phap_Su(Session conn, byte index) throws IOException {
        conn.p.ResetCreateItemStar();
        switch (index) {
            case 0: {
                conn.p.id_item_rebuild = -1;
                conn.p.is_use_mayman = false;
                conn.p.id_use_mayman = -1;
                Service.send_box_UI(conn, 18);
                break;
            }
            case 1: {
                conn.p.item_replace = -1;
                conn.p.item_replace2 = -1;
                Service.send_box_UI(conn, 19);
                break;
            }
            case 2: {
                Service.send_box_UI(conn, 17);
                break;
            }
            case 7: {
                Service.send_box_UI(conn, 35);
                break;
            }
            case 9: {
                Service.send_box_UI(conn, 34);
                break;
            }
            case 11: {
                Service.send_box_UI(conn, 36);
                break;
            }
            case 12: {
                Service.send_box_UI(conn, 24);
                break;
            }
            case 13: {
                Service.send_box_UI(conn, 25);
                conn.p.ResetCreateItemStar();
                conn.p.id_medal_is_created = 0;
                break;
            }
            case 14: {
                Service.send_box_UI(conn, 26);
                conn.p.ResetCreateItemStar();
                conn.p.id_medal_is_created = 1;
                break;
            }
            case 15: {
                Service.send_box_UI(conn, 27);
                conn.p.ResetCreateItemStar();
                conn.p.id_medal_is_created = 2;
                break;
            }
            case 16: {
                Service.send_box_UI(conn, 28);
                conn.p.ResetCreateItemStar();
                conn.p.id_medal_is_created = 3;
                break;
            }
            case 17: {
                conn.p.ResetCreateItemStar();
                Service.send_box_UI(conn, 33);
                break;
            }
            case 18:
            case 19: {
                ArrayList<String> myList = new ArrayList<>();
                Item3[] itemw = conn.p.item.wear;

                if (itemw == null) {
                    return;
                }
                if (itemw[12] != null && CheckItem.isMeDay(itemw[12].id)) {
                    myList.add(itemw[12].name + "(1000 ngọc)");
                }
                if (myList.isEmpty()) {
                    return;
                }
                send_menu_select(conn, index == 18 ? 4 : 5, myList.toArray(new String[0]));
                break;
            }
        }
    }

    private static void Menu_Admin(Session conn, byte index) throws IOException {
        if (conn.ac_admin < 1) {
            return;
        }
        switch (index) {
            case 0: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền để thực hiện!");
                    return;
                }
                Service.send_box_input_yesno(conn, 88, "Bạn có chắc chắn muốn bảo trì server?");
                break;
            }
            case 1: {
                if (conn.ac_admin <= 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                conn.p.update_vang(1_000_000_000, "Nhận %s vàng từ lệnh admin");
                Service.send_notice_nobox_white(conn, "+ 1.000.000.000 vàng");
                break;
            }
            case 2: {
                if (conn.ac_admin <= 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                conn.p.update_ngoc(1_000_000);
                Service.send_notice_nobox_white(conn, "+ 1.000.000 ngọc");
                break;
            }
            case 3: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                SaveData.process();
                Service.send_notice_nobox_white(conn, "data đã đc cập nhật");
                break;
            }
            case 4: {
                Service.send_box_input_text(conn, 1, "Get Item",
                        new String[]{"Nhập loại (3,4,7) vật phẩm :", "Nhập id vật phẩm", "Nhập số lượng"});
                break;
            }
            case 5: {
                Service.send_box_input_text(conn, 2, "Plus Level", new String[]{"Nhập level :"});
                break;
            }
            case 6: {
                Service.send_box_input_text(conn, 4, "Set Xp", new String[]{"Nhập mức x :"});
                break;
            }
            case 7: {
                Service.send_box_input_text(conn, 18, "Tên nhân vật", new String[]{"Nhập Tên nhân vật :"});
                break;
            }
            case 8: {
                Service.send_box_input_text(conn, 19, "Tên nhân vật", new String[]{"Nhập Tên nhân vật :"});
                break;
            }
            case 9: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.isLockVX = !Manager.isLockVX;
                Service.send_notice_box(conn, "Vòng xoay vàng ngọc đã " + (Manager.isLockVX ? "khóa" : "mở"));
                //Service.send_box_input_text(conn, 19, "Tên nhân vật", new String[]{"Nhập Tên nhân vật :"});
                break;
            }
            case 10: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.isTrade = !Manager.isTrade;
                Service.send_notice_box(conn, "Giao dịch đã " + (Manager.isTrade ? "mở" : "khóa"));
                break;
            }
            case 11: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.isKmb = !Manager.isKmb;
                Service.send_notice_box(conn, "Giao dịch đã " + (Manager.isKmb ? "mở" : "khóa"));
                break;
            }
            case 12: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                for (Pet pet : conn.p.mypet) {
                    if (pet.time_born > 0) {
                        pet.time_born = 3;
                    }
                }
                Service.send_notice_box(conn, "Đã xong");
                break;
            }
            case 13: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.BuffAdmin = !Manager.BuffAdmin;
                Service.send_notice_box(conn, "Buff Admin đã: " + (Manager.BuffAdmin ? "Bật" : "Tắt"));
                break;
            }
            case 14: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.BuffAdminMaterial = !Manager.BuffAdminMaterial;
                Service.send_notice_box(conn, "Buff nguyên liệu cho Admin Đã: " + (Manager.BuffAdminMaterial ? "Bật" : "Tắt"));
                break;
            }
            case 15: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.gI().chiem_mo.mo_open_atk();
                Manager.gI().chatKTGprocess(" Thời gian chiếm mỏ đã đến!");
                break;
            }
            case 16: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.gI().chiem_mo.mo_close_atk();
                Manager.gI().chatKTGprocess(" Thời gian chiếm mỏ đã đóng!");
                break;
            }
            case 17: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }

                // Bỏ qua kiểm tra số lượt khi admin cưỡng chế mở đăng ký
                KingCupManager.gI().startRegister();
                Service.send_notice_box(conn, "Đã mở đăng ký lôi đài!");
                KingCupManager.register = true;  // Đặt trạng thái mở đăng ký

                break;
            }


            case 20: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                if (Manager.gI().event == 2) {
                    Event_2.ClearMob();
                    Event_2.ResetMob();
                    Service.send_notice_box(conn, "Đã thực hiện reset mob events");
                }
                break;
            }
            case 18: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                if (ChiemThanhManager.isRegister) {
                    ChiemThanhManager.EndRegister();
                } else {
                    ChiemThanhManager.StartRegister();
                }
                Service.send_notice_box(conn, "Đã thực hiện " + (ChiemThanhManager.isRegister ? "mở" : "đóng") + " đăng kí chiếm thành");
                break;
            }
            case 19: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                ChienTruong.gI().open_register();
                Manager.gI().chatKTGprocess("Chiến Trường Đã Bắt Đầu Nhanh Tay Lẹ Chân Lên");
                break;
            }
            case 21: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Service.send_box_input_text(conn, 21, "Dịch chuyển map",
                        new String[]{"Nhập idMap", "Nhập tọa độ x", "Nhập tọa độ y"});
                break;
            }
            case 22: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.gI().load_config();
                break;
            }
            case 23: {
                if (conn.ac_admin < 10) {
                    Service.send_notice_box(conn, "Bạn không đủ quyền!");
                    return;
                }
                Manager.logErrorLogin = !Manager.logErrorLogin;
                Service.send_notice_box(conn, "Bạn đã " + (Manager.logErrorLogin ? "Bật" : "Tắt") + " log error");
                break;
            }
            // case 24: {
            //     Service.send_box_input_text(conn, 24, "Disconnect", new String[]{"Nhập loại :", "Nhập Tên :"});
            //    break;
            //  }
            ////  case 25: {
            //     String ssss = "Start Check \n-----------------------------\n";
            //     try {
            //        Message m = new Message(53);
            //        m.writer().writeUTF("check log");
            //        m.writer().writeByte(1);
            //      int mapnulls = 0;
            //      int mapnull = 0;
            //      int pnull = 0;
            //     ssss += "\nvo ne";
            //     for (Map[] map : Map.entrys) {
            ////      if (map == null) {
            //          mapnulls++;
            //          continue;
            //       }
            //      for (Map map0 : map) {
            //         if (map0 == null) {
            //            mapnull++;
            //             continue;
            //         }
            //        for (int i = 0; i < map0.players.size(); i++) {
            //           if (map0.players.get(i) == null || map0.players.get(i).conn == null) {
            //               pnull++;
            //               continue;
            //           }
            //           map0.players.get(i).conn.addmsg(m);
            //        }
            //          }
            //       }
            //     ssss += "\n" + mapnulls + " Map[]Null";
            //      ssss += "\n" + mapnull + " MapNull";
            //       ssss += "\n" + pnull + " PlayerNull";
            //        m.cleanup();
            //    } catch (Exception ex) {
            //         Service.send_notice_box(conn, "Lỗi: " + ex.getMessage());
            //         ex.printStackTrace();
            //        StackTraceElement[] stackTrace = ex.getStackTrace(); // Lấy thông tin ngăn xếp gọi hàm

            //       for (StackTraceElement element : stackTrace) {
            //          ssss += ("Class: " + element.getClassName());
            //           ssss += ("\nMethod: " + element.getMethodName());
            //          ssss += ("\nFile: " + element.getFileName());
            //          ssss += ("\nLine: " + element.getLineNumber());
            //          ssss += ("------------------------\n");
            //        }

            //      }
            //       Save_Log.process("checkbug.txt", ssss);
            //         break;
            //     }
            //     case 26: {
            //         String ssss = "Start Fix \n-----------------------------\n";
            //      try {
            //          Message m = new Message(53);
            //          m.writer().writeUTF("check log");
            //           m.writer().writeByte(1);
            //          int mapnulls = 0;
            //          int mapnull = 0;
            //          int pnull = 0;
            //          ssss += "\nvo ne";
            //          for (Map[] map : Map.entrys) {
            //              if (map == null) {
            //                 mapnulls++;
            //                 continue;
            //             }
            //             for (Map map0 : map) {
            //                 if (map0 == null) {
            //                     mapnull++;
            //                    continue;
            //              }
            //            for (int i = map0.players.size() - 1; i >= 0; i--) {
            //              if (map0.players.get(i) == null || map0.players.get(i).conn == null) {
            //                map0.players.remove(i);
            //          }
            //    }
            // }
            //                  }
            //                ssss += "\n" + mapnulls + " Map[]Null";
            //              ssss += "\n" + mapnull + " MapNull";
            //            ssss += "\n" + pnull + " PlayerNull";
            //          m.cleanup();
            //    } catch (Exception ex) {
            //      Service.send_notice_box(conn, "Lỗi: " + ex.getMessage());
            //    ex.printStackTrace();
//                    StackTraceElement[] stackTrace = ex.getStackTrace(); // Lấy thông tin ngăn xếp gọi hàm
//
            //                  for (StackTraceElement element : stackTrace) {
            //                    ssss += ("Class: " + element.getClassName());
            //                  ssss += ("\nMethod: " + element.getMethodName());
            //                ssss += ("\nFile: " + element.getFileName());
            //              ssss += ("\nLine: " + element.getLineNumber());
            //            ssss += ("------------------------\n");
            //      }

            //              }
            //            Service.send_notice_box(conn, "xong");
            //          Save_Log.process("checkbug.txt", ssss);
            //        break;
            //  }

            //   }
        }
    }


    private static void Menu_Zulu(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                switch (conn.p.clazz) {
                    case 0: {
                        Service.send_msg_data(conn, 23, "tocchienbinh");
                        break;
                    }
                    case 1: {
                        Service.send_msg_data(conn, 23, "tocsatthu");
                        break;
                    }
                    case 2:
                    case 3: {
                        Service.send_msg_data(conn, 23, "tocphapsu");
                        break;
                    }
                }
                break;
            }
            case 1: {
                if (conn.p.get_ngoc() >= 150) {
                    if (conn.p.maxInventory < 126) {
                        conn.p.maxInventory = 126;
                        conn.p.item.inventory3 = Arrays.copyOf(conn.p.item.inventory3, 126);
                        conn.p.update_ngoc(-150);
                        conn.p.item.char_inventory(3);
                        conn.p.item.char_inventory(4);
                        conn.p.item.char_inventory(7);
                        Service.send_notice_box(conn, "Đã mở rộng hành trang");
                        Service.send_char_main_in4(conn.p);
                    }
                } else {
                    Service.send_notice_box(conn, conn.language.khongdungoc);
                }
                break;
            }
        }
    }

    private static void Menu_ChangeZone(Session conn) throws IOException {
        Map[] map = Map.get_map_by_id(conn.p.map.map_id);
        if (map != null) {
            Message m = new Message(54);
            m.writer().writeByte(conn.p.map.maxzone);
            //
            for (int i = 0; i < conn.p.map.maxzone; i++) {
                if (map[i].players.size() > (map[i].maxplayer - 2)) {
                    m.writer().writeByte(2); // redzone
                } else if (map[i].players.size() >= (map[i].maxplayer / 2)) {
                    m.writer().writeByte(1); // yellow zone
                } else {
                    m.writer().writeByte(0); // green zone
                }
                if (i == 4 && Map.is_map_chiem_mo(conn.p.map, false)) {
                    m.writer().writeByte(i);
                } else if (i == 5 && conn.p.map.is_map_buon()) {
                    m.writer().writeByte(i);
                } else if (i == 1 && !Map.is_map_not_zone2(conn.p.map.map_id)) {
                    m.writer().writeByte(3);
                } else {
                    m.writer().writeByte(0);
                }
            }
            for (int i = 0; i < conn.p.map.maxzone; i++) {
                if (conn.p.map.is_map_buon() && i == 5) {
                    m.writer().writeUTF("Khu đi buôn");
                } else {
                    m.writer().writeUTF(
                            "Khu " + (map[i].zone_id + 1) + " (" + map[i].players.size() + ")");
                }
            }
            conn.addmsg(m);
            m.cleanup();
        }
    }

    private static void Menu_DaDichChuyen10(Session conn, byte index) throws IOException {
        if (conn.p.isKnight() || conn.p.isRobber() || conn.p.isTrader()) {
            Service.send_notice_nobox_white(conn, "Không thể sử dụng");
            return; // dừng, không dùng được
        }
        if (conn.status != 0) {
            Service.send_notice_box(conn, "Cần phải kích hoạt mới có thể vào");
            return;
        }
        Vgo vgo = null;
        switch (index) {
            case 0: {
                vgo = new Vgo();
                vgo.id_map_go = 1;
                vgo.x_new = 432;
                vgo.y_new = 354;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 1: {
                vgo = new Vgo();
                vgo.id_map_go = 33;
                vgo.x_new = 432;
                vgo.y_new = 480;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 2: {
                if (conn.status != 0) {
                    Service.send_notice_box(conn, "Cần phải kích hoạt mới có thể vào");
                    return;
                }
                vgo = new Vgo();
                vgo.id_map_go = 82;
                vgo.x_new = 432;
                vgo.y_new = 354;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 3: {
                if (conn.status != 0) {
                    Service.send_notice_box(conn, "Cần phải kích hoạt mới có thể vào");
                    return;
                }
                vgo = new Vgo();
                vgo.id_map_go = 136;
                vgo.x_new = 432;
                vgo.y_new = 354;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 4: {
                vgo = new Vgo();
                vgo.id_map_go = 4;
                vgo.x_new = 888;
                vgo.y_new = 672;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 5: {
                vgo = new Vgo();
                vgo.id_map_go = 5;
                vgo.x_new = 1056;
                vgo.y_new = 864;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 6: {
                vgo = new Vgo();
                vgo.id_map_go = 8;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 7: {
                vgo = new Vgo();
                vgo.id_map_go = 9;
                vgo.x_new = 1243;
                vgo.y_new = 876;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 8: {
                vgo = new Vgo();
                vgo.id_map_go = 11;
                vgo.x_new = 286;
                vgo.y_new = 708;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 9: {
                vgo = new Vgo();
                vgo.id_map_go = 12;
                vgo.x_new = 240;
                vgo.y_new = 732;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 10: {
                vgo = new Vgo();
                vgo.id_map_go = 13;
                vgo.x_new = 150;
                vgo.y_new = 979;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 11: {
                vgo = new Vgo();
                vgo.id_map_go = 15;
                vgo.x_new = 469;
                vgo.y_new = 1099;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 12: {
                vgo = new Vgo();
                vgo.id_map_go = 16;
                vgo.x_new = 673;
                vgo.y_new = 1093;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 13: {
                vgo = new Vgo();
                vgo.id_map_go = 17;
                vgo.x_new = 660;
                vgo.y_new = 612;
                conn.p.change_map(conn.p, vgo);
                break;
            }
        }
    }

    private static void Menu_DaDichChuyen33(Session conn, byte index) throws IOException {
        if (conn.p.isKnight() || conn.p.isRobber() || conn.p.isTrader()) {
            Service.send_notice_nobox_white(conn, "Không thể sử dụng");
            return; // dừng, không dùng được
        }
        Vgo vgo = null;
        switch (index) {
            case 0: {
                vgo = new Vgo();
                vgo.id_map_go = 67;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 1: {
                vgo = new Vgo();
                vgo.id_map_go = 33;
                vgo.x_new = 432;
                vgo.y_new = 480;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 2: {
                if (conn.status != 0) {
                    Service.send_notice_box(conn, "Cần phải kích hoạt mới có thể vào");
                    return;
                }
                vgo = new Vgo();
                vgo.id_map_go = 82;
                vgo.x_new = 432;
                vgo.y_new = 354;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 3: {
                vgo = new Vgo();
                vgo.id_map_go = 20;
                vgo.x_new = 787;
                vgo.y_new = 966;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 4: {
                vgo = new Vgo();
                vgo.id_map_go = 22;
                vgo.x_new = 120;
                vgo.y_new = 678;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 5: {
                vgo = new Vgo();
                vgo.id_map_go = 24;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 6: {
                vgo = new Vgo();
                vgo.id_map_go = 26;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 7: {
                vgo = new Vgo();
                vgo.id_map_go = 29;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 8: {
                vgo = new Vgo();
                vgo.id_map_go = 31;
                vgo.x_new = 360;
                vgo.y_new = 624;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 9: {
                vgo = new Vgo();
                vgo.id_map_go = 37;
                vgo.x_new = 150;
                vgo.y_new = 674;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 10: {
                vgo = new Vgo();
                vgo.id_map_go = 39;
                vgo.x_new = 199;
                vgo.y_new = 882;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 11: {
                vgo = new Vgo();
                vgo.id_map_go = 41;
                vgo.x_new = 187;
                vgo.y_new = 462;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 12: {
                vgo = new Vgo();
                vgo.id_map_go = 43;
                vgo.x_new = 228;
                vgo.y_new = 43;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 13: {
                vgo = new Vgo();
                vgo.id_map_go = 45;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 14: {
                vgo = new Vgo();
                vgo.id_map_go = 50;
                vgo.x_new = 300;
                vgo.y_new = 300;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            default: {
                Service.send_notice_box(conn, "Chưa có chức năng");
                break;
            }
        }
    }

    private static void Menu_DaDichChuyen55(Session conn, byte index) throws IOException {
        if (conn.p.isKnight() || conn.p.isRobber() || conn.p.isTrader()) {
            Service.send_notice_nobox_white(conn, "Không thể sử dụng");
            return; // dừng, không dùng được
        }
        Vgo vgo = null;
        switch (index) {
            case 0: {
                vgo = new Vgo();
                vgo.id_map_go = 67;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 1: {
                if (conn.status != 0) {
                    Service.send_notice_box(conn, "Cần phải kích hoạt mới có thể vào");
                    return;
                }
                vgo = new Vgo();
                vgo.id_map_go = 82;
                vgo.x_new = 432;
                vgo.y_new = 354;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 2: {
                vgo = new Vgo();
                vgo.id_map_go = 74;
                vgo.x_new = 258;
                vgo.y_new = 354;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 3: {
                vgo = new Vgo();
                vgo.id_map_go = 77;
                vgo.x_new = 576;
                vgo.y_new = 222;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 4: {
                vgo = new Vgo();
                vgo.id_map_go = 93;
                vgo.x_new = 462;
                vgo.y_new = 342;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 5: {
                vgo = new Vgo();
                vgo.id_map_go = 94;
                vgo.x_new = 306;
                vgo.y_new = 240;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 6: {
                vgo = new Vgo();
                vgo.id_map_go = 95;
                vgo.x_new = 390;
                vgo.y_new = 162;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 7: {
                vgo = new Vgo();
                vgo.id_map_go = 96;
                vgo.x_new = 198;
                vgo.y_new = 666;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 8: {
                vgo = new Vgo();
                vgo.id_map_go = 97;
                vgo.x_new = 432;
                vgo.y_new = 168;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 9: {
                vgo = new Vgo();
                vgo.id_map_go = 98;
                vgo.x_new = 270;
                vgo.y_new = 132;
                conn.p.change_map(conn.p, vgo);
                break;
            }
            case 10: {
                vgo = new Vgo();
                vgo.id_map_go = 33;
                vgo.x_new = 432;
                vgo.y_new = 480;
                conn.p.change_map(conn.p, vgo);
                break;
            }
        }
    }

    private static void Menu_Hammer(Session conn, byte index, byte idmenu) throws IOException {
        if (idmenu == 0) {
            switch (index) {
                case 0: {
                    if (1 == 1) {
                        Service.send_notice_box(conn, "Chức năng bảo trì");
                        return;
                    }
                    Service.send_box_UI(conn, 5);
                    break;
                }
                case 1: {
                    if (1 == 1) {
                        Service.send_notice_box(conn, "Chức năng bảo trì");
                        return;
                    }
                    Service.send_box_UI(conn, 6);
                    break;
                }
                case 2: {
                    if (1 == 1) {
                        Service.send_notice_box(conn, "Chức năng bảo trì");
                        return;
                    }
                    Service.send_box_UI(conn, 7);
                    break;
                }
                case 3: {
                    if (1 == 1) {
                        Service.send_notice_box(conn, "Chức năng bảo trì");
                        return;
                    }
                    Service.send_box_UI(conn, 8);
                    break;
                }
                case 4: // chế tạo tinh tú
                {
                    send_menu_select(conn, -5, new String[]{"Chiến binh", "Sát thủ", "Pháp sư", "Xạ thủ"}, (byte) 1);
                    break;
                }
                case 5: { // nâng cấp tinh tú
                    conn.p.isCreateArmor = false;
                    conn.p.isCreateItemStar = true;
                    Service.send_box_UI(conn, 33);

                    break;
                }

                case 6: { // giap sieu nhan
                    if (conn.p.item.wear[20] == null) {
                        Service.send_notice_box(conn, "Không thể thực hiện");
                    } else {
                        Item3 buffer = conn.p.item.wear[20];
                        conn.p.item.wear[20] = null;
                        conn.p.item.add_item_inventory3(buffer);
                        conn.p.item.char_inventory(3);
                        conn.p.fashion = Part_fashion.get_part(conn.p);
                        Service.send_wear(conn.p);
                        Service.send_char_main_in4(conn.p);
                        MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                        Service.send_notice_box(conn, "Tháo thành công");
                    }
                    break;
                }
                case 7: { // thao danh hiẹu
                    if (conn.p.item.wear[19] == null) {
                        Service.send_notice_box(conn, "Không thể thực hiện");
                    } else {
                        Item3 buffer = conn.p.item.wear[19];
                        conn.p.item.wear[19] = null;
                        conn.p.item.add_item_inventory3(buffer);
                        conn.p.item.char_inventory(3);
                        conn.p.fashion = Part_fashion.get_part(conn.p);
                        Service.send_wear(conn.p);
                        Service.send_char_main_in4(conn.p);
                        MapService.update_in4_2_other_inside(conn.p.map, conn.p);
                        Service.send_notice_box(conn, "Tháo thành công");
                    }
                    break;
                }
                case 8: {
                    String[] nemu = new String[]{"Kháng băng", "Kháng lửa", "Kháng điện", "Kháng độc"};
                    send_menu_select(conn, -5, nemu, (byte) 15);
                    break;
                }
                case 9: {
                    conn.p.isCreateArmor = true;
                    Service.send_box_UI(conn, 33);
                    break;
                }
                case 10: {
                    String[] nemu = new String[]{"Sách vật lý", "Sách ma pháp"};
                    send_menu_select(conn, -5, nemu, (byte) 14);
                    break;
                }
            }
        } else if (idmenu == 1) {
            String[] nemu = new String[]{"Nón", "Áo", "Quần", "Giày", "Găng tay", "Nhẫn", "Vũ khí", "Dây chuyền"};
            send_menu_select(conn, -5, nemu, (byte) (10 + index));
        } else if (idmenu >= 10 && idmenu <= 13) {
            conn.p.isCreateItemStar = true;
            conn.p.ClazzItemStar = (byte) (idmenu - 10);
            conn.p.TypeItemStarCreate = index;
            Service.send_box_UI(conn, 40 + index);
        } else if (idmenu == 14) {
            Service.send_box_input_yesno(conn, -123 + index, "Giá ghép sách là 30 ngọc, bạn có muốn tiếp tục không?");
        } else if (idmenu == 15) {
            conn.p.type_armor_create = index;
            String[] nemu = new String[]{"Giáp siêu nhân bạc", "Giáp siêu nhân tím", "Giáp siêu nhân xanh", "Giáp siêu nhân vàng"};
            send_menu_select(conn, -5, nemu, (byte) 16);
        } else if (idmenu == 16) {
            conn.p.id_armor_create = index;
            conn.p.isCreateArmor = true;
            Service.send_box_UI(conn, 50);
        }
    }

    private static void Menu_Doubar(Session conn, byte index, byte idmenu) throws IOException {
        if (idmenu == 0) {
            switch (index) {
                case 0: {

                    Service.send_box_UI(conn, 1);
                    break;
                }
                case 1: {

                    Service.send_box_UI(conn, 2);
                    break;
                }
                case 2: {

                    Service.send_box_UI(conn, 3);
                    break;
                }
                case 3: {

                    Service.send_box_UI(conn, 4);
                    break;
                }
                case 4: {
                    Item3 item = conn.p.item.wear[12];
                    if (item != null) {
                        Service.send_box_input_yesno(conn, 12, "Bạn có muốn tháo " + item.name);
                    }
                    break;
                }
                case 5: {
                    conn.p.down_horse_clan();
                    break;
                }
                case 6: {
                    if (BossServer.listBossActive().length == 0) {
                        Service.send_notice_box(conn, "Hiện tại tất cả boss đều còn sống");
                        return;
                    }
                    send_menu_select(conn, -4, BossServer.listBossActive(), (byte) 1);
                    break;
                }
            }
        } else if (idmenu == 1) {
            // Xem thông tin boss
            send_menu_select(conn, -4, BossServer.sendInfo(index), (byte) 2);
        }
    }

    private static void Menu_keva(Session conn, byte index) throws IOException {
        if (1 == 1) {
            return;
        }
        switch (index) {
            case 0: { // cua hang potion
                Service.send_box_UI(conn, 0);
                break;
            }
        }
    }

    private static void Menu_Mr_Haku(Session conn, byte index) throws IOException {
        if (index == 0) {
            if (conn.status != 0) {
                Service.send_notice_box(conn, "Tài khoản chưa được kích hoạt,");
                return;
            }
            if (conn.p.get_vang() < 500) {
                Service.send_notice_box(conn, "Không đủ 500 vàng");
                return;
            }
            conn.p.update_vang(-500, "Trừ %s vàng NPC Haku");
            Vgo vgo = new Vgo();
            vgo.id_map_go = 67;
            vgo.x_new = 576;
            vgo.y_new = 222;
            conn.p.change_map(conn.p, vgo);
        }
    }

    private static void Menu_Lisa(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: { // cua hang potion
                Service.send_box_UI(conn, 0);
                break;
            }
            case 1: {
                Lottery.sendMessage(conn, (byte) 0);
                break;
            }
            case 2: { // cua hang potion
                Service.send_box_input_text(conn, 22, "% thuế", new String[]{"Nhập % thuế 5 - 15"});
                break;
            }
            case 3: {
                MemberBattlefields temp = ChienTruong.gI().get_bxh(conn.p.name);
                if (temp != null) {
                    switch (ChienTruong.gI().get_index_bxh(temp)) {
                        case 0: {
                            short[] id_ = new short[]{3, 2, 53, 54, 18};
                            short[] id2_ = new short[]{5, 5, 1, 1, 10};
                            short[] id3_ = new short[]{7, 7, 4, 4, 4};
                            for (int i = 0; i < id_.length; i++) {
                                Item47 it = new Item47();
                                it.id = id_[i];
                                it.quantity = id2_[i];
                                conn.p.item.add_item_inventory47(id3_[i], it);
                            }
                            break;
                        }
                        case 1:
                        case 2: {
                            short[] id_ = new short[]{3, 2, 18};
                            short[] id2_ = new short[]{5, 5, 10};
                            short[] id3_ = new short[]{7, 7, 4};
                            for (int i = 0; i < id_.length; i++) {
                                Item47 it = new Item47();
                                it.id = id_[i];
                                it.quantity = id2_[i];
                                conn.p.item.add_item_inventory47(id3_[i], it);
                            }
                            break;
                        }
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9: {
                            short[] id_ = new short[]{3, 18};
                            short[] id2_ = new short[]{5, 10};
                            short[] id3_ = new short[]{7, 4};
                            for (int i = 0; i < id_.length; i++) {
                                Item47 it = new Item47();
                                it.id = id_[i];
                                it.quantity = id2_[i];
                                conn.p.item.add_item_inventory47(id3_[i], it);
                            }
                            break;
                        }
                    }
                } else {
                    Service.send_notice_box(conn, "Không có tên trong danh sách");
                }
                break;
            }
            case 4: {
                ChiemThanhManager.NhanQua(conn.p);
                break;
            }
        }
    }

    private static void doiVangSangNgoc(Player p, long vangDoi, int soNgoc) throws IOException {
        if (p.vang < vangDoi) {
            Service.send_notice_box(p.conn, "Không đủ vàng để đổi!");
            return;
        }

        p.vang -= vangDoi;
        p.kimcuong += soNgoc;

        // Gửi thông báo phần thưởng
        String notice = "Bạn đã đổi thành công " + soNgoc + " ngọc xanh!";
        short[] ids = new short[]{-2};         // -2 = ngọc xanh
        int[] quants = new int[]{soNgoc};
        short[] types = new short[]{4};

        Service.Show_open_box_notice_item(p, notice, ids, quants, types);
        Service.updateVang(p);        // <- lỗi thứ 2 ở đây
        Service.updateKimCuong(p);    // <- lỗi thứ 2 ở đây
    }

    private static void Menu_CayThong(Session conn, byte index) throws IOException {
        if (Manager.gI().event == 1) {
            switch (index) {
                case 0:
                case 1:
                case 2:
                case 3: {
                    int quant = conn.p.item.total_item_by_id(4, (113 + index));
                    if (quant > 0) {
                        //
                        short[] id_4 = new short[]{2, 5, 52, 142, 225, 271};
                        short[] id_7 = new short[]{0, 4, 23, 34, 39, 352, 357, 362, 367, 372, 377, 382, 387, 392, 397, 402,
                                407, 412,};
                        HashMap<Short, Short> list_4 = new HashMap<>();
                        HashMap<Short, Short> list_7 = new HashMap<>();
                        for (int i = 0; i < quant; i++) {
                            if (conn.p.item.get_inventory_able() > 1) {
                                if (80 > Util.random(100)) {
                                    Item47 it = new Item47();
                                    it.category = 4;
                                    it.id = id_4[Util.random(id_4.length)];
                                    it.quantity = (short) Util.random(1, 3);
                                    if (!list_4.containsKey(it.id)) {
                                        list_4.put(it.id, it.quantity);
                                    } else {
                                        short quant_ = it.quantity;
                                        list_4.put(it.id, (short) (list_4.get(it.id) + quant_));
                                    }
                                    conn.p.item.add_item_inventory47(4, it);
                                } else {
                                    Item47 it = new Item47();
                                    it.category = 7;
                                    it.id = id_7[Util.random(id_7.length)];
                                    it.quantity = (short) Util.random(1, 2);
                                    if (!list_7.containsKey(it.id)) {
                                        list_7.put(it.id, it.quantity);
                                    } else {
                                        short quant_ = it.quantity;
                                        list_7.put(it.id, (short) (list_7.get(it.id) + quant_));
                                    }
                                    conn.p.item.add_item_inventory47(7, it);
                                }
                            }
                        }
                        //
                        Event_1.add_caythong(conn.p.name, quant);
                        conn.p.item.remove(4, (113 + index), quant);
                        String item_receiv = "\n";
                        for (Entry<Short, Short> en : list_4.entrySet()) {
                            item_receiv += ItemTemplate4.item.get(en.getKey()).getName() + " " + en.getValue() + "\n";
                        }
                        for (Entry<Short, Short> en : list_7.entrySet()) {
                            item_receiv += ItemTemplate7.item.get(en.getKey()).getName() + " " + en.getValue() + "\n";
                        }
                        Service.send_notice_box(conn, "Trang trí thành công " + quant + " lần và nhận được:" + item_receiv);
                    } else {
                        Service.send_notice_box(conn, "Không đủ trong hành trang!");
                    }
                    break;
                }
                case 4: {
                    send_menu_select(conn, 120, Event_1.get_top_caythong());
                    break;
                }
                default: {
                    Service.send_notice_box(conn, "Đang bảo trì");
                    break;
                }
            }
        }
    }

    private static void Menu_ThaoKhamNgoc(Session conn, byte index) throws IOException {
        if (!conn.p.list_thao_kham_ngoc.isEmpty()) {
            if (conn.p.item.get_inventory_able() < 3) {
                Service.send_notice_box(conn, "Hành trang không đủ chỗ");
                return;
            }
            Item3 it = conn.p.list_thao_kham_ngoc.get(index);
            if (it != null) {
                for (int i = it.op.size() - 1; i >= 0; i--) {
                    byte id = it.op.get(i).id;
                    if (id == 58 || id == 59 || id == 60) {
                        if (it.op.get(i).getParam(0) != -1) {
                            Item47 it_add = new Item47();
                            it_add.id = (short) (it.op.get(i).getParam(0));
                            it_add.quantity = 1;
                            it_add.category = 7;
                            conn.p.item.add_item_inventory47(7, it_add);
                        }
                        it.op.get(i).setParam(-1);
                    } else if (id == 5 || id == 6 || id >= 100 && id <= 107) {
                        it.op.remove(i);
                    }
                }
                Service.send_wear(conn.p);
                Service.send_notice_box(conn, "Tháo thành công");
            }
        }
    }

    private static void Menu_DoiDongMeDaySTG(Session conn, byte index) throws IOException {
        if (conn.p.item.wear != null && conn.p.item.wear.length > 12 && CheckItem.isMeDay(conn.p.item.wear[12].id)) {
            Service.send_box_input_yesno(conn, 94, "Bạn có chắc chắn muốn đổi?");
        } else {
            Service.send_notice_box(conn, "Không có vật phẩm phù hợp!");
        }
    }

    private static void Menu_Nang_Skill(Session conn, byte index) throws IOException {
        // Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        if (conn.p.skill_110[conn.p.id_temp_byte] >= 10) {
            conn.p.id_temp_byte = -1;
            Service.send_notice_box(conn, "Kỹ năng được nâng cấp tối đa");
            return;
        }
        int level = conn.p.skill_110[conn.p.id_temp_byte];
        String name_book = "";
        if (conn.p.id_temp_byte == 1) {
            name_book = switch (conn.p.clazz) {
                case 0 -> "sách học kiếm địa chấn";
                case 1 -> "sách học thần tốc";
                case 2 -> "sách học cơn phẫn nộ";
                case 3 -> "sách học súng điện từ";
                default -> name_book;
            };
        } else if (conn.p.id_temp_byte == 0) {
            name_book = switch (conn.p.clazz) {
                case 0 -> "sách học bão lửa";
                case 1 -> "sách học bão độc";
                case 2 -> "sách học băng trận";
                case 3 -> "sách học súng thần công";
                default -> name_book;
            };
        }
        String format = String.format("Để nâng từ cấp %s lên cấp %s bạn cần %s sách %s và %s ngọc."
                + " Bạn có muốn thực hiện", level, level + 1, level + 1, name_book, level * 5 + 10);
        if (index == 0) {
            Service.send_box_input_yesno(conn, -121, format);
        } else if (index == 1) {
            Service.send_box_input_yesno(conn, -120, format);
        }
    }

    private static void Menu_DoiDongMeDaySTPT(Session conn, byte index) throws IOException {
        if (conn.p.item.wear != null && conn.p.item.wear.length > 12 && CheckItem.isMeDay(conn.p.item.wear[12].id)) {
            Service.send_box_input_yesno(conn, 98, "Bạn có chắc chắn muốn đổi?");
        } else {
            Service.send_notice_box(conn, "Không có vật phẩm phù hợp!");
        }
    }

    private static final int[][] coin_to_gems = {
            {10000, 2500},
            {20000, 5200},
            {50000, 13000},
            {100000, 26500},
            {200000, 53500},
            {500000, 150000}
    };
    private static final int[][] coin_to_gold = {
            {10000, 5000000},
            {20000, 10200000},
            {50000, 26000000},
            {100000, 53000000},
            {200000, 108000000},
            {500000, 275000000}
    };

    private static void Menu_ADMIN_SHARINGAN(Session conn, int idNpc, byte index, byte idMenu) {
        // Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        try {
            if (idMenu == 0) {
                switch (index) {
                    case 0: {
                        if (conn.status != 0) {
                            Service.send_notice_box(conn, "Tài khoản chưa được kích hoạt,");
                            return;
                        }
                        send_menu_select(conn, -127, new String[]{"Hướng đẫn", "Nhận nhiệm vụ", "Huỷ nhiệm vụ", "Trả nhiệm vụ", "Thông tin"}, (byte) 1);
                        break;
                    }
                    case 1: {
                        if (conn.status != 0) {
                            Service.send_notice_box(conn, "Tài khoản chưa được kích hoạt,");
                            return;
                        }
                        send_menu_select(conn, -127, new String[]{
                                "Tổng Nạp: " + conn.p.getTongNap(),
                                "Nhận quà"
                        }, (byte) 7);
                        break;
                    }
                    case 2: {
                        if (conn.status != 0) {
                            Service.send_notice_box(conn, "Tài khoản chưa được kích hoạt,");
                            return;
                        }
                        String[] menu = new String[]{
                                "Đổi coin lấy ngọc",
                                "Đổi coin lấy vàng"
                        };
                        send_menu_select(conn, -127, menu, (byte) 3);
                        break;
                    }
                    //    case 3: {
                    //      send_menu_select(conn, -127, new String[]{"Đổi Áo choàng tỷ phú (" + (5 - Manager.gI().ty_phu.size()) + ")",
                    //          "Đổi Áo choàng triệu phú (" + (10 - Manager.gI().trieu_phu.size()) + ")",
                    //          "Đổi Áo choàng đại gia (" + (20 - Manager.gI().dai_gia.size()) + ")",
                    //            "Hướng dẫn", "Shop"}, (byte) 5);
                    //            break;
                    //         }
                    //          case 4: {
                    //              Admin.quatopLevel(conn);
                    //               break;
                    //           }
                    //           case 5: {
                    //               Admin.quatopEvent(conn);
                    //              break;
                    //            }
                    case 3: {
                        if (conn.status != 0) {
                            Service.send_notice_box(conn, "Tài khoản chưa được kích hoạt,");
                            return;
                        }
                        Service.send_box_UI(conn, 49);
                        break;
                    }
                    case 4: {
                        if (conn.status != 0) {
                            Service.send_notice_box(conn, "Tài khoản chưa được kích hoạt,");
                            return;
                        }
                        Service.send_box_UI(conn, 51);
                        break;
                    }
//                    case 5: { // Shop Đổi Trang bị lấy ngọc
//                        send_menu_select(conn, -127, new String[]{
//                                "Giá token: " + conn.p.getGiaToken(),
//                                "Token hiện có: " + conn.p.getToken(),
//                                "Coin hiên có: " + conn.p.getCoin(),
//                                "Mua token",
//                                "Bán token",
//                                "Nạp coin",
//                                "Bảng giá nạp coin"
//                        }, (byte) 8);
//
////                        int itemId = 327;
////                        int category = 4;
////
////                        // Kiểm tra có item không
////                        if (conn.p.item.total_item_by_id(category, itemId) < 1 && conn.ac_admin < 10) {
////                            Service.send_notice_box(conn, "Bạn không có Thẻ giao dịch đồ khoá! Cút vào mê cung mà tìm đi!!!!!!!");
////                            return;
////                        }
////
////                        // Xóa item
////                        conn.p.item.remove(category, itemId, 1);
////
////                        // Cộng ngọc
////                        conn.p.kimcuong += 1000;
////
////                        // Gửi thông báo mở hộp
////                        String notice = "Bạn nhận được 1000 ngọc khi đổi thẻ GD khoá!";
////                        short[] ids = new short[]{-2};         // -2 là ID đại diện cho ngọc
////                        int[] quants = new int[]{1000};         // số lượng ngọc
////                        short[] types = new short[]{4};        // type 4: vật phẩm ảo (vàng/ngọc)
////
////                        Service.Show_open_box_notice_item(conn.p, notice, ids, quants, types);
////                        Service.send_notice_box(conn, "Đổi thành công 1 Thẻ GD đồ khoá lấy 1000 ngọc.");
//                        break;
//                    }
                    case 5: { // Kích hoạt tài khoản
                        // 1. Kiểm tra nếu đã kích hoạt rồi thì dừng luôn
                        if (conn.status == 0) {
                            Service.send_notice_box(conn, "Tài khoản đã được kích hoạt.");
                            return;
                        }

                        // 2. Xác định giá tiền dựa theo status (Dùng toán tử 3 ngôi cho gọn)
                        // Nếu status = 2 -> 20k, status = 3 -> 50k, còn lại (status 1) -> 5k
                        int cost = (conn.status >= 2) ? 50000 : 20000;
                        // 3. Kiểm tra số dư
                        if (conn.p.getCoin() < cost) {
                            Service.send_notice_box(conn, "Bạn cần " + cost + " coin để kích hoạt.");
                            return;
                        }

                        // 4. Thực hiện giao dịch
                        try (Connection connection = SQL.gI().getConnection(); Statement st = connection.createStatement()) {
                            conn.p.update_coin(-cost); // Trừ tiền
                            conn.p.history_coin(-cost,"(TRỪ COIN) Kích hoạt TK");
//                            His_COIN hisc = new His_COIN(conn.user ,conn.p.name);
//                            hisc.coin_change = cost;
//                            hisc.coin_last = conn.p.getCoin();
//                            hisc.Logger = "(TRỪ COIN) Kích hoạt TK";
//                            hisc.Flus();

                            // Update SQL
                            if (st.executeUpdate("UPDATE `account` SET `status` = 0 WHERE `user` = '" + conn.user + "' LIMIT 1") > 0) {
                                connection.commit();
                                conn.status = 0; // Cập nhật ngay trong Session để không phải login lại

                                Service.updateKimCuong(conn.p); // Cập nhật hiển thị tiền
                                Service.send_notice_box(conn, "Kích hoạt thành công!");
                                Manager.gI().chatKTGprocess("🎉 " + conn.p.name + " đã kích hoạt tài khoản thành công!");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            Service.send_notice_box(conn, "Lỗi kết nối, vui lòng thử lại.");
                        }
                        break;
                    }
//                    case 6: {
//                        String[] phanThuong = new String[]{
//                                "Đổi Cúp Vàng ",
//                                "Đổi Cúp Bạc ",
//                                "Đổi Cúp Đồng "
//                        };
//                        MenuController.send_menu_select(conn, -310, phanThuong, (byte) 3);
//                        break;
//                    }
                }
            } else if (idMenu == 1) {
                switch (index) {
                    case 0: {
                        String notice
                                = "Nhiệm vụ Ngày: đánh quái ngẫu nhiên theo level, tối đa ngày nhận 20 nhiệm vụ, mỗi nhiệm vụ sẽ nhận được phần thưởng kinh nghiệm, ngọc và có cơ hội nhận nguyên liệu mề đay."
                                + "\n Dễ : Vàng Ngọc + Exp" + "\n Bình Thường : Vàng Ngọc, Exp + NL mề Xanh"
                                + "\n Khó :Vàng Ngọc, Exp + NL mề Vàng" + "\n Siêu Khó : Vàng Ngọc, Exp + NL mề Tím";
                        Service.send_notice_box(conn, notice);
                        break;
                    }
                    case 1: {
                        if (conn.p.quest_daily[0] != -1) {
                            Service.send_notice_box(conn, "Đã nhận nhiệm vụ rồi!");
                        } else {
                            if (conn.p.quest_daily[4] > 0) {
                                send_menu_select(conn, idNpc, new String[]{"Cực Dễ", "Bình thường", "Khó", "Siêu Khó"}, (byte) 2);
                            } else {
                                Service.send_notice_box(conn, "Hôm nay đã hết lượt, quay lại vào ngày mai");
                            }
                        }
                        break;
                    }
                    case 2: {
                        DailyQuest.remove_quest(conn.p);
                        break;
                    }
                    case 3: {
                        DailyQuest.finish_quest(conn.p);
                        break;
                    }
                    case 4: {
                        Service.send_notice_box(conn, DailyQuest.info_quest(conn.p));
                        break;
                    }
                }
            } else if (idMenu == 2) {
                DailyQuest.get_quest(conn.p, index);
            } else if (idMenu == 3) {
                switch (index) {
                    case 0: {
                        String[] menu = new String[coin_to_gems.length];
                        for (int i = 0; i < coin_to_gems.length; i++) {
                            menu[i] = "Đổi " + Util.number_format(coin_to_gems[i][0]) + " coin lấy " + Util.number_format(coin_to_gems[i][1]) + " ngọc";
                        }
                        send_menu_select(conn, -127, menu, (byte) 4);
                        break;
                    }
                    case 1: {
                        String[] menu = new String[coin_to_gold.length];
                        for (int i = 0; i < coin_to_gold.length; i++) {
                            menu[i] = "Đổi " + Util.number_format(coin_to_gold[i][0]) + " coin lấy " + Util.number_format(coin_to_gold[i][1]) + " vàng";
                        }
                        send_menu_select(conn, -127, menu, (byte) 9);
                        break;
                    }
                }
            } else if (idMenu == 4) {
                int my_coin = conn.p.getCoin();
                if (my_coin >= coin_to_gems[index][0]) {
                    conn.p.update_coin(-coin_to_gems[index][0]);
                    conn.p.update_ngoc(coin_to_gems[index][1]);
                    conn.p.history_coin(-coin_to_gems[index][0],"(TRỪ COIN) Đổi ngọc");
//                    His_COIN hisc = new His_COIN(conn.user ,conn.p.name);
//                    hisc.coin_change = coin_to_gems[index][0];
//                    hisc.coin_last = conn.p.getCoin();
//                    hisc.Logger = "(TRỪ COIN) Đổi ngọc";
//                    hisc.Flus();
                    Service.send_notice_box(conn, "Đổi thành công");
                } else {
                    Service.send_notice_box(conn, "Không đủ coin, bạn chỉ có " + my_coin + " coin");
                }
            } else if (idMenu == 5) {
                switch (index) {
                    case 0:
                        if (Manager.hour < 10) {
                            Service.send_notice_box(conn, "Đổi thành tích mở vào 10h-23h59' hàng ngày");
                            return;
                        }
                        if (Manager.gI().thanh_tich.containsKey(conn.p.name)) {
                            Service.send_notice_box(conn, "Chỉ được đổi 1 lần");
                            return;
                        }
                        if (5 <= Manager.gI().ty_phu.size()) {
                            Service.send_notice_box(conn, "Đã hết");
                            return;
                        }
                        addInventoryItem(conn, 4746, 100000);
                        break;
                    case 1:
                        if (Manager.hour < 10) {
                            Service.send_notice_box(conn, "Đổi thành tích mở vào 10h-23h59' hàng ngày");
                            return;
                        }
                        if (Manager.gI().thanh_tich.containsKey(conn.p.name)) {
                            Service.send_notice_box(conn, "Chỉ được đổi 1 lần");
                            return;
                        }
                        if (10 <= Manager.gI().trieu_phu.size()) {
                            Service.send_notice_box(conn, "Đã hết");
                            return;
                        }
                        addInventoryItem(conn, 4747, 50000);
                        break;
                    case 2:
                        if (Manager.hour < 10) {
                            Service.send_notice_box(conn, "Đổi thành tích mở vào 10h-23h59' hàng ngày");
                            return;
                        }
                        if (Manager.gI().thanh_tich.containsKey(conn.p.name)) {
                            Service.send_notice_box(conn, "Chỉ được đổi 1 lần");
                            return;
                        }
                        if (20 <= Manager.gI().dai_gia.size()) {
                            Service.send_notice_box(conn, "Đã hết");
                            return;
                        }
                        addInventoryItem(conn, 4748, 20000);
                        break;
                    case 3: {
                        String notice
                                = "- Có 3 mốc thành tích là Đại Gia , Triệu Phú , Tỷ Phú"
                                + "\n- Khi các bạn nạp 1 VNĐ sẽ nhận ngay 1 điểm nạp."
                                + "\n- Mốc tỷ phú yêu cầu tối thiểu 100,000 điểm nạp, số lượng 5 người"
                                + "\n- Mốc triệu phú yêu cầu tối thiểu 50,000 điểm nạp, số lượng 10 người"
                                + "\n- Mốc đại gia yêu cầu tối thiểu 20,000 điểm nạp, số lượng 20 người"
                                + "\nĐiểm nạp sẽ được reset hàng tuần. Khi đổi sẽ bị trừ điểm nạp";
                        Service.send_notice_box(conn, notice);
                        break;
                    }
                    case 4: {
//                        Service.send_box_UI(conn, 37);
                        break;
                    }
                }
            } else if (idMenu == 6) {
                switch (index) {
                    case 0, 1, 2, 3, 4, 5, 7: {
                        if (conn.p.get_ngoc() < 200000) {
                            Service.send_notice_box(conn, "Không đủ ngọc");
                            return;
                        }
                        conn.p.update_ngoc(-200000);
                        Admin.randomTT(conn, (byte) 5, index);
                        break;
                    }
                    case 6: {
                        if (conn.p.get_ngoc() < 500000) {
                            Service.send_notice_box(conn, "Không đủ ngọc");
                            return;
                        }
                        conn.p.update_ngoc(-500000);
                        Admin.randomTT(conn, (byte) 5, index);
                        Service.send_notice_box(conn, "Bạn đã mua thành công.");
                        break;
                    }
                }
            } else if (idMenu == 7) {
                switch (index) {
                    case 1:
                        get_qua(conn);
                        break;
                }
            } else if (idMenu == 9) {
                int my_coin = conn.p.getCoin();
                if (my_coin >= coin_to_gold[index][0]) {
                    conn.p.update_coin(-coin_to_gold[index][0]);
                    conn.p.update_vang(coin_to_gold[index][1], "Nhận %s vàng từ đổi coin");
                    conn.p.history_coin(-coin_to_gold[index][0],"(TRỪ COIN) Đổi vàng");
//                    His_COIN hisc = new His_COIN(conn.user ,conn.p.name);
//                    hisc.coin_change = coin_to_gold[index][0];
//                    hisc.coin_last = conn.p.getCoin();
//                    hisc.Logger = "(TRỪ COIN) Đổi vàng";
//                    hisc.Flus();
                    Service.send_notice_box(conn, "Đổi thành công");
                } else {
                    Service.send_notice_box(conn, "Không đủ coin, bạn chỉ có " + my_coin + " coin");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void Menu_Quyen_Luc(Session conn, byte index, byte idMenu) {
        // Đệ tử
        if (!conn.p.isOwner) {
            return;
        }
        try {
            if (idMenu == 0) {
                switch (index) {
                    case 0: {
                        String notice = "ĐẶC QUYỀN ĐỘC NHẤT TOP 1 DONATE\n"
                                + " - Thu thuế toàn server (1%)\n"
                                + " - Chat kênh thế giới auto hiện: [ĐẠI GIA].\n"
                                + " - Kích hoạt X2 EXP cho toàn bộ máy chủ.\n"
                                + " - Tạo Giftcode (Vàng/Ngọc) ban phát cho ae.\n"
                                + " - Mở Giảm Giá Shop (Max 5%) toàn máy chủ.\n"
                                + "⚠️ LƯU Ý: Nếu bị người khác vượt mốc Nạp, toàn bộ quyền lợi này sẽ chuyển sang người đó ngay lập tức!";
                        Service.send_notice_box(conn, notice);
                        break;
                    }
                    case 1: {
                        if (conn.p.checkIsTop1Nap() == false) {
                            Service.send_notice_box(conn, "Bạn không phải top 1 donate hiện tại");
                            return;
                        }
                        send_menu_select(conn, -126, new String[]{
                                "Bật x2 cho toàn server",
                                "Tạo GIFTCODE cho toàn server",
                                "Bật giảm giá đồ của toàn server"
                        }, (byte) 1);
                        break;
                    }
                    case 2: {
                        openMenuDauTu(conn.p);
                        break;
                    }
                    case 3: { // Shop Đổi Trang bị lấy ngọc
                        String hienThi = String.format("Giá token: %.3f", conn.p.getGiaToken());
                        send_menu_select(conn, -126, new String[]{
                                "Giá token: " + hienThi,
                                "Token hiện có: " + conn.p.getToken(),
                                "Coin hiên có: " + conn.p.getCoin(),
                                "Mua token",
                                "Bán token",
                                "Nạp coin",
                                "Bảng giá nạp coin"
                        }, (byte) 2);
                        break;
                    }
                }
            } else if (idMenu == 1) {
                switch (index) {
                    case 0: {
                        if (Manager.gI().time_x2_server > System.currentTimeMillis()) {
                            Manager.gI().time_x2_server += 24 * 60 * 60 * 1000L;
                        } else {
                            Manager.gI().time_x2_server = System.currentTimeMillis() + 24 * 60 * 60 * 1000L;
                        }
                        long phutConLai = (Manager.gI().time_x2_server - System.currentTimeMillis()) / 60000;
                        String thongBao = "[💲ĐẠI GIA] " + conn.p.name.toUpperCase() + " đã kích hoạt X2 EXP toàn Server!\n"
                                + "⏳ Thời gian X2 còn lại: " + Util.number_format(phutConLai) + " phút.";
                        Manager.gI().chatKTGWhite(thongBao);
                        break;
                    }
                    case 1: {
                        if (conn.p.checkIsTop1Nap()) {
                            // Định nghĩa 4 dòng gợi ý
                            String[] info = new String[]{
                                    "Tên Code (VD: TET2026)",
                                    "Vàng mỗi người",
                                    "Ngọc mỗi người",
                                    "Số lượng giới hạn"
                            };
                            // Gọi bảng nhập với ID là 999 (Bạn có thể đổi số này, miễn là chưa dùng)
                            Service.send_box_input_text(conn, 39, "QUYỀN NĂNG ĐẠI GIA", info);
                        }
                        break;
                    }
                    case 2: {
                        // Kiểm tra quyền Top 1
                        if (conn.p.checkIsTop1Nap()) {
                            // Tạo bảng nhập với 2 dòng: % Giảm và Thời gian
                            String[] info = new String[]{
                                    "Phần trăm giảm (Tối đa 5% )",
                                    "Thời gian hiệu lực (Phút)"
                            };

                            // Gọi bảng nhập với ID 888 (ID mới, đừng trùng với giftcode 999 nhé)
                            Service.send_box_input_text(conn.p.conn, 40, "QUYỀN NĂNG GIẢM GIÁ", info);
                        } else {
                            Service.send_notice_box(conn, "Chỉ Top 1 Nạp mới được sử dụng quyền năng này!");
                        }
                        break;
                    }
                }
            } else if (idMenu == 2) {
                switch (index) {
                    case 3:
                        Service.send_box_input_text(conn, 35, "Nhập số lương", new String[]{"nhập số lượng"});
                        break;
                    case 4:
                        Service.send_box_input_text(conn, 36, "Nhập số lương", new String[]{"nhập số lượng"});
                        break;
                    case 5:
                        String notice = "HƯỚNG DẪN NẠP COIN\n"
                                + "Bạn vui lòng chuyển khoản vào tài khoản sau:\n"
                                + "- Ngân hàng: BIDV\n"
                                + "- STK: 96247HSO\n"
                                + "- Chủ TK: TRAN DUC DUY\n"
                                + "- Số Tiền: 2K > && < 10TR\n"
                                + "Nội dung(Bắt buộc): NAP " + conn.id + "\n" // Tự động điền ID người chơi
                                + "LƯU Ý QUAN TRỌNG:\n"
                                + "1. KHÔNG ghi số tiền hoặc số coin vào nội dung chuyển khoản.\n"
                                + "2. Ghi đúng nội dung chuyển khoản. Sai AD ko chịu trách nhiệm.";
                        Service.send_notice_box(conn, notice);
                        break;
                    case 6:
                        String bang_gia = "=== BẢNG GIÁ NẠP COIN ===\n"
                                + "(Tỷ lệ gốc, chưa bao gồm Khuyến mãi)\n"
                                + "10.000đ      = 12.000 coin\n"
                                + "20.000đ      = 25.000 coin\n"
                                + "50.000đ      = 65.000 coin\n"
                                + "100.000đ     = 140.000 coin\n"
                                + "200.000đ     = 300.000 coin\n"
                                + "500.000đ     = 800.000 coin\n"
                                + "1.000.000đ   = 1.700.000 coin\n"
                                + "2.000.000đ   = 3.600.000 coin\n"
                                + "5.000.000đ   = 10.000.000 coin\n"
                                + "LƯU Ý: Nếu đang có sự kiện X2, số coin thực nhận sẽ được nhân đôi so với bảng giá trên.";
                        Service.send_notice_box(conn, bang_gia);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addInventoryItem(Session conn, int itemId, int diemNeeded) throws IOException {
        int diem = conn.p.getDiemNap();
        if (diem >= diemNeeded) {
            ItemTemplate3 buffer = ItemTemplate3.item.get(itemId);
            Item3 itbag = new Item3();
            itbag.id = buffer.getId();
            itbag.clazz = buffer.getClazz();
            itbag.type = buffer.getType();
            itbag.level = buffer.getLevel();
            itbag.icon = buffer.getIcon();
            itbag.color = buffer.getColor();
            itbag.part = buffer.getPart();
            itbag.islock = true;
            itbag.name = buffer.getName();
            itbag.tier = 0;
            itbag.op = new ArrayList<>(buffer.getOp());
            itbag.expiry_date = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L;
            itbag.UpdateName();
            conn.p.item.add_item_inventory3(itbag);
            conn.p.item.char_inventory(3);
            conn.p.update_diem_nap(-diemNeeded);
            Manager.gI().thanh_tich.put(conn.p.name, (itemId - 4746));
            if (itemId == 4746) {
                Manager.gI().ty_phu.add(conn.p.name);
            } else if (itemId == 4747) {
                Manager.gI().trieu_phu.add(conn.p.name);
            } else {
                Manager.gI().dai_gia.add(conn.p.name);
            }
            Service.send_notice_box(conn, "Đổi thành công");
        } else {
            Service.send_notice_box(conn, "Chưa đủ " + diemNeeded + " điểm nạp, bạn chỉ có " + diem + " điểm.");
        }
    }

    public static void get_qua(Session conn) throws IOException {
        String text = "";
        String text1 = "";
        if (conn.p.getCount() < 1 && conn.p.getTongNap() >= 100_000) {
            text = "moc100";
            text1 = "Mốc 100k ATM";
        } else if (conn.p.getCount() < 2 && conn.p.getTongNap() >= 300_000) {
            text = "moc300";
            text1 = "Mốc 300k ATM ";
        } else if (conn.p.getCount() < 3 && conn.p.getTongNap() >= 500_000) {
            text = "moc500";
            text1 = "Mốc 500k ATM ";
        } else if (conn.p.getCount() < 4 && conn.p.getTongNap() >= 1_000_000) {
            text = "moc1000";
            text1 = "Mốc 1 TRIỆU ATM ";
        } else if (conn.p.getCount() < 5 && conn.p.getTongNap() >= 2_000_000) {
            text = "moc2000";
            text1 = "Mốc 2 TRIỆU ATM";
        } else if (conn.p.getCount() < 6 && conn.p.getTongNap() >= 5_000_000) {
            text = "moc5000";
            text1 = "Mốc 5 TRIỆU ATM ";
        } else if (conn.p.getCount() < 7 && conn.p.getTongNap() >= 8_000_000) {
            text = "moc8000";
            text1 = "Mốc 8 TRIỆU ATM ";
        } else if (conn.p.getCount() < 8 && conn.p.getTongNap() >= 10_000_000) {
            text = "moc10000";
            text1 = "Mốc 10 TRIỆU ATM ";
        } else {
            Service.send_notice_box(conn, "Bạn chưa đạt được mốc để nhận quà");
            return;
        }
        try (Connection connection = SQL.gI().getConnection(); Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM `quatang` WHERE `giftname` = '" + text + "';")) {
            byte empty_box = (byte) 0;
            if (!rs.next()) {
                Service.send_notice_box(conn, "Giftcode đã được nhận hoặc không tồn tại");
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
                } else if (limit < 1 && conn.ac_admin < 4) {
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
//                        short expiry = Short.parseShort(jsar2.get(1).toString());
//                        if (itInventory.type == 14) {
//                            itInventory.time_use = expiry * 24 * 60 * 60 * 1000L;
//                        }
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
                    int coin_up = rs.getInt("coin");
                    conn.p.update_vang(vang_up, "Nhận %s vàng từ nhập giftcode " + text);
                    conn.p.update_ngoc(ngoc_up);
                    conn.p.update_coin(coin_up);
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
                    if (coin_up != 0) {
                        IDs.add((short) -2);
                        Quants.add((int) (coin_up > 2_000_000_000 ? 2_000_000_000 : coin_up));
                        Types.add((short) 4);
                    }
                    conn.p.history_coin(coin_up,"(CỘNG COIN) Nhận giftcode");
//                    His_COIN hisc = new His_COIN(conn.user ,conn.p.name);
//                    hisc.coin_change = coin_up;
//                    hisc.coin_last = conn.p.getCoin();
//                    hisc.Logger = "(CỘNG COIN) Nhận giftcode";
//                    hisc.Flus();

                    short[] ar_id = new short[IDs.size()];
                    int[] ar_quant = new int[Quants.size()];
                    short[] ar_type = new short[Types.size()];
                    for (int i = 0; i < ar_id.length; i++) {
                        ar_id[i] = IDs.get(i);
                        ar_quant[i] = Quants.get(i);
                        ar_type[i] = Types.get(i);
                    }
                    conn.p.update_count(1);
                    conn.p.item.char_inventory(5);
                    Service.Show_open_box_notice_item(conn.p, "Bạn nhận được mốc: " + text1, ar_id, ar_quant, ar_type);
                } else {
                    Service.send_notice_box(conn, "Hành trang phải trống " + empty_box + " ô trở lên!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void Menu_Wedding(Session conn, byte index) throws IOException {
        switch (index) {
            case 0: {
                if (conn.p.item.wear[23] == null) {
                    if (conn.p.getCoin() < 100_000) {
                        Service.send_notice_box(conn, "Không đủ coin");
                        return;
                    }
                    // Sửa lại chỉ gửi 1 input string
                    Service.send_box_input_text(conn, 38, "Nhập thông tin",
                            new String[]{"Tên đối phương : "});
                } else {
                    Service.send_notice_box(conn, "Nhẫn cưới thì đeo đấy mà đòi cưới thêm ai??");
                }
                break;
            }
            case 1: {
                if (conn.p.item.wear[23] != null) {
                    Service.send_notice_box(conn, "Hãy trân trọng đi. Ngoài kia bao nhiêu người dell có ny kia kìa");
                    return;
                } else {
                    Service.send_notice_box(conn, "Đã cưới ai đâu, ảo tưởng à??");
                }
                break;
            }
            case 2: {
                Item3 item = conn.p.item.wear[23];
                if (item == null) {
                    Service.send_notice_box(conn, "Hãy đeo nhẫn cưới vào!");
                    return;
                }

                // --- 1. TÍNH GIỚI HẠN HIỆN TẠI ---

                // Tier 0 -> Max 10% (1000 điểm)
                int limit_percent_val = (10 + item.tier * 10) * 100;

                // Tier 0 -> Max 110 dame
                int limit_dame_val = ((item.tier + 1) * 10) + 100;

                // --- 2. KIỂM TRA ĐIỀU KIỆN ---
                boolean du_dieu_kien = true;
                String ly_do = "";

                for (Option op : item.op) {
                    if (op.id >= 7 && op.id <= 11) {
                        if (op.param < limit_percent_val) {
                            du_dieu_kien = false;
                            ly_do = "Chưa đạt " + (limit_percent_val / 100) + "% sát thương";
                            break;
                        }
                    }
                    if (op.id >= 0 && op.id <= 4) {
                        if (op.param < limit_dame_val) {
                            du_dieu_kien = false;
                            ly_do = "Chưa đạt " + limit_dame_val + " tấn công";
                            break;
                        }
                    }
                }

                if (!du_dieu_kien) {
                    Service.send_notice_box(conn, "Không thể đột phá!\nLý do: " + ly_do + ".\nHãy tiếp tục đi đánh quái.");
                    return;
                }

                // --- SỬA Ở ĐÂY: GIỚI HẠN 50% ---
                // Tier 4 tương ứng với 10 + 40 = 50%. Nếu đang ở Tier 4 thì báo Max luôn.
                if (item.tier >= 4) {
                    Service.send_notice_box(conn, "Nhẫn đã đạt cấp tối đa (50%)!");
                    return;
                }
                // -------------------------------

                // Tính thông số cấp tiếp theo để hiển thị
                int next_percent = 10 + (item.tier + 1) * 10;
                int next_dame = ((item.tier + 2) * 10) + 100;
                int token = (item.tier + 1) * 100;
                String notice = "Đột phá giới hạn Nhẫn (Tier " + item.tier + " -> " + (item.tier + 1) + "):\n"
                        + "- Tấn công: " + limit_dame_val + " -> " + next_dame + "\n"
                        + "- Sát thương: " + (limit_percent_val / 100) + "% -> " + next_percent + "%\n"
                        + "Chi phí: " + token + " token";

                Service.send_box_input_yesno(conn, 112, notice);
                break;
            }
            case 3: {
                String notice = "--- HƯỚNG DẪN KẾT HÔN ---\n"
                        + "1. Đăng ký kết hôn:\n"
                        + "- Phí đăng ký: 100,000 Coin.\n"
                        + "- Yêu cầu: Không đeo nhẫn cưới, nhập chính xác tên đối phương.\n"
                        + "--- HƯỚNG DẪN NUÔI NHẪN ---\n"
                        + "1. Tích lũy chỉ số:\n"
                        + "- Hai vợ chồng đeo nhẫn và cùng đánh quái để nhẫn hấp thụ kinh nghiệm.\n"
                        + "- Nhẫn sẽ tự động tăng chỉ số Tấn công và % Sát thương.\n"
                        + "2. Đột phá giới hạn:\n"
                        + "- Khi chỉ số đạt ngưỡng giới hạn của cấp hiện tại, nhẫn sẽ ngừng tăng.\n"
                        + "- Cần vào menu này chọn 'Đột phá' để lên cấp tiếp theo.\n"
                        + "- Nguyên liệu đột phá: Cần dùng Token.\n"
                        + "- Cấp tối đa hiện tại: 50% chỉ số.";

                Service.send_notice_box(conn, notice);
                break;
            }
            default: {
                Service.send_notice_box(conn, "Chưa có chức năng");
                break;
            }
        }
    }

    public static final int[] MOC_LEVEL_QUY = {30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 139};

    public static final int[] TIEN_THUONG_QUY = {
            30000, 30000, 30000, 30000, 30000, 30000, 30000, 30000, // 30 -> 100 (8 mốc): 30k/mốc = 240k
            50000, 50000, 50000,                                   // 110 -> 130 (3 mốc): 50k/mốc = 150k
            510000                                                 // MỐC 139: Ăn trọn 510k (Hơn 1 nửa tổng quỹ)
    };

    public static void openMenuDauTu(Player p) throws IOException {
        // Nếu chưa mua quỹ -> Hiện bảng Yes/No mời mua
        if (p.da_mua_quy == 0) {
            if (p.level > 20) {
                Service.send_notice_box(p.conn, "Chỉ có thể đầu tư ở level dưới 20");
                return;
            }
            int von = 100000;
            int tong_nhan = 900000;

            String notice = "Kích hoạt Quỹ Đầu Tư (Cấp 30 -> 139):\n"
                    + "- Vốn bỏ ra: " + Util.number_format(von) + " Coin\n"
                    + "- Tổng nhận lại: " + Util.number_format(tong_nhan) + " Coin\n"
                    + "🔥 ĐẶC BIỆT: Đạt cấp 139 nhận ngay 510.000 Coin!\n"
                    + "Bạn có muốn đầu tư không?";

            Service.send_box_input_yesno(p.conn, -11, notice);
        }
        // Nếu đã mua rồi -> Hiện danh sách (Logic giữ nguyên)
        else {
            StringBuilder sb = new StringBuilder();
            sb.append("TIẾN ĐỘ ĐẦU TƯ (Lv hiện tại: ").append(p.level).append(")\n");
            sb.append("----------------\n");

            boolean co_qua_chua_nhan = false;

            for (int i = 0; i < MOC_LEVEL_QUY.length; i++) {
                int lv_moc = MOC_LEVEL_QUY[i];
                int tien = TIEN_THUONG_QUY[i];

                sb.append("Cấp ").append(lv_moc).append(": ").append(Util.number_format(tien));

                if (p.moc_nhan_quy >= lv_moc) {
                    sb.append(" [Đã nhận]\n");
                } else if (p.level >= lv_moc) {
                    sb.append(" [NHẬN NGAY]\n");
                    co_qua_chua_nhan = true;
                } else {
                    sb.append(" [Chưa đạt]\n");
                }
            }

            if (co_qua_chua_nhan) {
                Service.send_box_input_yesno(p.conn, -12, sb.toString() + "\n\nBạn có muốn nhận thưởng ngay không?");
            } else {
                Service.send_notice_box(p.conn, sb.toString());
            }
        }
    }

    public static void xuLyNhanThuongQuy(Player p) throws IOException {
        // 1. Kiểm tra an toàn
        if (p.da_mua_quy == 0) {
            Service.send_notice_box(p.conn, "Bạn chưa mua gói Quỹ Đầu Tư!");
            return;
        }

        long tong_tien_nhan = 0; // Dùng long để tránh tràn số nếu sau này bạn làm quỹ to hơn
        int moc_da_nhan_moi_nhat = p.moc_nhan_quy;
        boolean co_qua = false;
        StringBuilder chi_tiet = new StringBuilder(); // Để liệt kê các mốc nhận được

        // 2. Vòng lặp quét qua tất cả các mốc cấu hình
        for (int i = 0; i < MOC_LEVEL_QUY.length; i++) {
            int lv_moc = MOC_LEVEL_QUY[i];
            int tien_thuong = TIEN_THUONG_QUY[i];

            // LOGIC QUAN TRỌNG:
            // - Cấp hiện tại (p.level) phải lớn hơn hoặc bằng mốc quy định.
            // - Mốc quy định (lv_moc) phải lớn hơn mốc đã từng nhận (p.moc_nhan_quy) để tránh nhận lặp.
            if (p.level >= lv_moc && lv_moc > p.moc_nhan_quy) {
                tong_tien_nhan += tien_thuong;
                moc_da_nhan_moi_nhat = lv_moc; // Cập nhật mốc cao nhất vừa đạt được
                co_qua = true;

                // Ghi chú lại để hiện thông báo cho đẹp
                chi_tiet.append("- Mốc ").append(lv_moc).append(": ").append(Util.number_format(tien_thuong)).append("\n");
            }
        }

        // 3. Xử lý trao thưởng
        if (co_qua) {
            // Cộng tiền
            p.update_coin((int) tong_tien_nhan);
            p.history_coin((int) tong_tien_nhan,"(CỘNG COIN) Nhận Quỹ đầu tư");
//            His_COIN hisc = new His_COIN(p.conn.user ,p.name);
//            hisc.coin_change = (int) tong_tien_nhan;
//            hisc.coin_last = p.getCoin();
//            hisc.Logger = "(CỘNG COIN) Nhận Quỹ đầu tư";
//            hisc.Flus();

            // Cập nhật mốc mới vào data của người chơi
            p.moc_nhan_quy = moc_da_nhan_moi_nhat;

            // LƯU DATA NGAY LẬP TỨC (Bắt buộc phải có để tránh rollback)
            try {
                p.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 4. Thông báo kết quả chi tiết
            String msg = "🎉 NHẬN THƯỞNG THÀNH CÔNG!\n"
                    + "----------------------\n"
                    + chi_tiet.toString()
                    + "----------------------\n"
                    + "💰 Tổng cộng: " + Util.number_format(tong_tien_nhan) + " Coin\n"
                    + "Chúc mừng bạn đã đạt mốc cấp độ " + moc_da_nhan_moi_nhat + "!";

            Service.send_notice_box(p.conn, msg);
        } else {
            // Nếu không có quà nào thỏa mãn điều kiện
            int next_moc = 0;
            for (int m : MOC_LEVEL_QUY) {
                if (m > p.level) {
                    next_moc = m;
                    break;
                }
            }
            String msg_fail = "Bạn chưa đạt cấp độ yêu cầu.\n";
            if (next_moc > 0) {
                msg_fail += "Hãy cày lên Cấp " + next_moc + " để nhận thưởng tiếp nhé!";
            } else {
                msg_fail += "Bạn đã nhận hết toàn bộ phần thưởng của Quỹ rồi!";
            }
            Service.send_notice_box(p.conn, msg_fail);
        }
    }
}
