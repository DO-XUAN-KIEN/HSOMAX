package Game.core;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Game.Helps.ItemStar;
import Game.Helps.Medal;
import Game.client.Player;
import Game.io.Message;
import Game.io.Session;
import Game.template.Item3;
import Game.template.Item47;
import Game.template.ItemTemplate3;
import Game.template.ItemTemplate4;
import Game.template.ItemTemplate7;
import Game.template.MaterialMedal;
import Game.template.Option;


import java.util.Random;
import Game.core.Service;
import Game.core.Util;


import Game.io.Message;

public class Admin {
    public static HashMap<String, Integer> topLevel = new HashMap<>();
    public static HashMap<String, Integer> topEvent = new HashMap<>();

    public static void randomMedal(Player p, byte color_, byte tier_, boolean isLock) {
        ItemTemplate3 temp = ItemTemplate3.item.get(Util.random(4587, 4591));
        Item3 itbag = new Item3();
        itbag.id = temp.getId();
        itbag.clazz = temp.getClazz();
        itbag.type = temp.getType();
        itbag.level = 1; // level required
        itbag.icon = temp.getIcon();
        itbag.color = color_;
        itbag.part = temp.getPart();
        itbag.islock = isLock;
        itbag.name = temp.getName();
        itbag.tier = 0;
        //
        List<Option> opnew = new ArrayList<>();
        byte typest = (byte) Util.random(0, 5);
        int _st;
        byte dongan;
        if (color_ == 0) {
            _st = Util.random(130, 140);
            dongan = (byte) Util.random(5, 7);
        } else if (color_ == 1) {
            _st = Util.random(130, 150);
            dongan = (byte) Util.random(5, 8);
        } else if (color_ == 2) {
            _st = Util.random(140, 160);
            dongan = (byte) Util.random(5, 8);
        } else if (color_ == 3) {
            _st = Util.random(150, 170);
            dongan = (byte) Util.random(5, 9);
        } else {
            _st = Util.random(160, 180);
            dongan = (byte) Util.random(7, 11);
        }
        // thêm dòng st gốc
        opnew.add(new Option(typest, _st, itbag.id));
        opnew.add(new Option(96, dongan, itbag.id));
        //
        itbag.op = new ArrayList<>();
        itbag.opMedal = Medal.CreateMedal(dongan, color_, itbag.id);
        itbag.op.addAll(opnew);
        itbag.time_use = 0;
        itbag.item_medal = new short[5];

        int material_type_1st = Util.random(0, 7);
        int material_type_2nd = Util.random(0, 7);
        while (material_type_1st == material_type_2nd) {
            material_type_2nd = Util.random(0, 7);
        }
        itbag.item_medal[0] = (short) (MaterialMedal.m_white[material_type_1st][Util.random(0, 10)] + 200);
        itbag.item_medal[1] = (short) (MaterialMedal.m_white[material_type_2nd][Util.random(0, 10)] + 200);
        itbag.item_medal[2] = (short) (MaterialMedal.m_blue[Util.random(0, 10)] + 200);
        itbag.item_medal[3] = (short) (MaterialMedal.m_yellow[Util.random(0, 10)] + 200);
        itbag.item_medal[4] = (short) (MaterialMedal.m_violet[Util.random(0, 10)] + 200);

        for (byte i = 0; i < tier_; i++) {
            itbag.tier = (byte) (i + 1);
            Medal.UpgradeMedal(itbag);
        }
        p.item.add_item_inventory3(itbag);
    }

    public static void randomTT(Session conn, byte color, byte type) throws IOException {
        short type_item = ItemStar.ConvertType(type, conn.p.clazz);
        short id_item = ItemStar.GetIDItem(type, conn.p.clazz);
        List<Option> ops = ItemStar.GetOpsItemStar(conn.p.clazz, (byte) type_item, 0);

        Item3 itbag = new Item3();
        itbag.id = id_item;
        itbag.name = ItemTemplate3.item.get(id_item).getName();
        itbag.clazz = ItemTemplate3.item.get(id_item).getClazz();
        itbag.type = ItemTemplate3.item.get(id_item).getType();
        itbag.level = 45;
        itbag.icon = ItemTemplate3.item.get(id_item).getIcon();
        itbag.op = new ArrayList<>();
        for (Option o : ops) {
            int pr = o.getParam(0);
            int pr1 = (int) (pr * color * 0.25);
            if ((o.id >= 58 && o.id <= 60) || (o.id >= 100 && o.id <= 107))
                itbag.op.add(new Option(o.id, pr, itbag.id));
            else if (o.id == 37 || o.id == 38) {
                itbag.op.add(new Option(o.id, 2, itbag.id));
            } else
                itbag.op.add(new Option(o.id, pr1, itbag.id));
        }
        int[] opAo = {-111, -110, -109, -108, -107};
        int[] opNon = {-102, -113, -105};
        int[] opVK = {-101, -113, -86, -84, -82, -80};
        int[] opNhan = {-89, -87, -104, -86, -84, -82, -80};
        int[] opDayChuyen = {-87, -105, -103, -91};
        int[] opGang = {-89, -103, -91};
        int[] opGiay = {-104, -103, -91};

        if (color == 4) {
            if (itbag.type == 0 || itbag.type == 1) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opAo[Util.nextInt(opAo.length)];
                    int opid2 = opAo[Util.nextInt(opAo.length)];
                    while (opid1 == opid2) {
                        opid1 = opAo[Util.nextInt(opAo.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opAo[Util.nextInt(opAo.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 2) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opNon[Util.nextInt(opNon.length)];
                    int opid2 = opNon[Util.nextInt(opNon.length)];
                    while (opid1 == opid2) {
                        opid1 = opNon[Util.nextInt(opNon.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opNon[Util.nextInt(opNon.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 3) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opGang[Util.nextInt(opGang.length)];
                    int opid2 = opGang[Util.nextInt(opGang.length)];
                    while (opid1 == opid2) {
                        opid1 = opGang[Util.nextInt(opGang.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opGang[Util.nextInt(opGang.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 4) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opNhan[Util.nextInt(opNhan.length)];
                    int opid2 = opNhan[Util.nextInt(opNhan.length)];
                    while (opid1 == opid2) {
                        opid1 = opNhan[Util.nextInt(opNhan.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opNhan[Util.nextInt(opNhan.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 5) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    int opid2 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    while (opid1 == opid2) {
                        opid1 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 6) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opGiay[Util.nextInt(opGiay.length)];
                    int opid2 = opGiay[Util.nextInt(opGiay.length)];
                    while (opid1 == opid2) {
                        opid1 = opGiay[Util.nextInt(opGiay.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opGiay[Util.nextInt(opGiay.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type > 6) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opVK[Util.nextInt(opVK.length)];
                    int opid2 = opVK[Util.nextInt(opVK.length)];
                    while (opid1 == opid2) {
                        opid1 = opVK[Util.nextInt(opVK.length)];
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opVK[Util.nextInt(opVK.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            }
        } else if (color == 5) {
            if (itbag.type == 0 || itbag.type == 1) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opAo[Util.nextInt(opAo.length)];
                    int opid2 = opAo[Util.nextInt(opAo.length)];
                    int opid3 = opAo[Util.nextInt(opAo.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opAo[Util.nextInt(opAo.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opAo[Util.nextInt(opAo.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opAo[Util.nextInt(opAo.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opAo[Util.nextInt(opAo.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 2) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opNon[Util.nextInt(opNon.length)];
                    int opid2 = opNon[Util.nextInt(opNon.length)];
                    int opid3 = opNon[Util.nextInt(opNon.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opNon[Util.nextInt(opNon.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opNon[Util.nextInt(opNon.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opNon[Util.nextInt(opNon.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opNon[Util.nextInt(opNon.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 3) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opGang[Util.nextInt(opGang.length)];
                    int opid2 = opGang[Util.nextInt(opGang.length)];
                    int opid3 = opGang[Util.nextInt(opGang.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opGang[Util.nextInt(opGang.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opGang[Util.nextInt(opGang.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opGang[Util.nextInt(opGang.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                } else {
                    int opid = opGang[Util.nextInt(opGang.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 4) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opNhan[Util.nextInt(opNhan.length)];
                    int opid2 = opNhan[Util.nextInt(opNhan.length)];
                    int opid3 = opNhan[Util.nextInt(opNhan.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opNhan[Util.nextInt(opNhan.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opNhan[Util.nextInt(opNhan.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opNhan[Util.nextInt(opNhan.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opNhan[Util.nextInt(opNhan.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 5) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    int opid2 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    int opid3 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opDayChuyen[Util.nextInt(opDayChuyen.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type == 6) {
                int percent = Util.nextInt(0, 100);
                if (percent > 85) {
                    int opid1 = opGiay[Util.nextInt(opGiay.length)];
                    int opid2 = opGiay[Util.nextInt(opGiay.length)];
                    int opid3 = opGiay[Util.nextInt(opGiay.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opGiay[Util.nextInt(opGiay.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opGiay[Util.nextInt(opGiay.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opGiay[Util.nextInt(opGiay.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opGiay[Util.nextInt(opGiay.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            } else if (itbag.type > 7) {
                int percent = Util.nextInt(0, 100);
                if (percent > 90) {
                    int opid1 = opVK[Util.nextInt(opVK.length)];
                    int opid2 = opVK[Util.nextInt(opVK.length)];
                    int opid3 = opVK[Util.nextInt(opVK.length)];
                    while ((opid1 == opid2) || (opid1 == opid3)) {
                        opid1 = opVK[Util.nextInt(opVK.length)];
                    }
                    while ((opid2 == opid1) || (opid2 == opid3)) {
                        opid2 = opVK[Util.nextInt(opVK.length)];
                    }
                    while ((opid3 == opid2) || (opid1 == opid3)) {
                        opid3 = opVK[Util.nextInt(opVK.length)];
                    }
                    if (percent > 95) {
                        itbag.op.add(new Option(opid3, Util.random(100, 200), itbag.id));
                    }
                    itbag.op.add(new Option(opid1, Util.random(100, 200), itbag.id));
                    itbag.op.add(new Option(opid2, Util.random(100, 200), itbag.id));
                } else {
                    int opid = opVK[Util.nextInt(opVK.length)];
                    itbag.op.add(new Option(opid, Util.random(100, 200), itbag.id));
                }
            }
        }
        itbag.color = color;
        itbag.part = ItemTemplate3.item.get(id_item).getPart();
        itbag.tier = 0;
        itbag.time_use = 0;
        itbag.islock = true;
        conn.p.item.add_item_inventory3(itbag);
        conn.p.item.char_inventory(3);
    }
    public static void setTop() {
        topEvent();
    }
    public static void topEvent() {
        topEvent.put("", 1);
    }
   public static void setThanhTich() {
    Manager.gI().ty_phu.clear();
    Manager.gI().trieu_phu.clear();
    Manager.gI().dai_gia.clear();

 for (Session conn : Manager.gI().conns) {
    if (conn != null && conn.p != null && conn.p.item != null) {
        Player p = conn.p;
        if (p.item.wear[15] != null && p.item.wear[15].id == 4746) {
            Manager.gI().ty_phu.add(p.name);
        }
         if (p.item.wear[15] != null && p.item.wear[15].id == 4747) {
            Manager.gI().trieu_phu.add(p.name);
        }
                  if (p.item.wear[15] != null && p.item.wear[15].id == 4748) {
            Manager.gI().dai_gia.add(p.name);
        }
    }
}


   }
    public static void quatopLevel(Session conn) throws IOException {
        if (!Admin.topLevel.containsKey(conn.p.name)) {
            Service.send_notice_box(conn, "Không có tên");
            return;
        }
        if (conn.p.item.get_inventory_able() < 5) {
            Service.send_notice_box(conn, "Hành trang cần tối thiểu 5 ô trống");
            return;
        }
        switch (Admin.topLevel.get(conn.p.name)) {
            case 1: {
                conn.p.update_ngoc(70000);
                conn.p.update_vang(100000000L, "Nhận %s vàng từ quà top Level");

                Item47 item47 = new Item47();
                item47.id = 14;
                item47.quantity = 100;
                conn.p.item.add_item_inventory47(7, item47);

                item47.id = 471;
                item47.quantity = 1;
                conn.p.item.add_item_inventory47(7, item47);

                ItemTemplate3 temp3 = ItemTemplate3.item.get(4580);
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

                temp3 = ItemTemplate3.item.get(4706);// Tóc
                it = new Item3();
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

                Admin.randomMedal(conn.p, (byte) 4, (byte) 10, false);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topLevel.remove(conn.p.name);
                break;
            }
            case 2: {
                conn.p.update_ngoc(40000);
                conn.p.update_vang(100000000L, "Nhận %s vàng từ quà top Level");

                Item47 item47 = new Item47();
                item47.id = 14;
                item47.quantity = 50;
                conn.p.item.add_item_inventory47(7, item47);

                item47.id = 471;
                item47.quantity = 1;
                conn.p.item.add_item_inventory47(7, item47);

                ItemTemplate3 temp3 = ItemTemplate3.item.get(4580);
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

                temp3 = ItemTemplate3.item.get(4706);// Tóc
                it = new Item3();
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

                Admin.randomMedal(conn.p, (byte) 4, (byte) 6, false);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topLevel.remove(conn.p.name);
                break;
            }
            case 3: {
                conn.p.update_ngoc(20000);
                conn.p.update_vang(80000000L, "Nhận %s vàng từ quà top Level");

                Item47 item47 = new Item47();
                item47.id = 14;
                item47.quantity = 40;
                conn.p.item.add_item_inventory47(7, item47);

                item47.id = 471;
                item47.quantity = 1;
                conn.p.item.add_item_inventory47(7, item47);

                ItemTemplate3 temp3 = ItemTemplate3.item.get(4584);
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

                temp3 = ItemTemplate3.item.get(4706);// Tóc
                it = new Item3();
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

                Admin.randomMedal(conn.p, (byte) 4, (byte) 6, true);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topLevel.remove(conn.p.name);
                break;
            }
            case 4: {
                Item47 item47 = new Item47();
                item47.id = 14;
                item47.quantity = 30;
                conn.p.item.add_item_inventory47(7, item47);

                item47.id = 471;
                item47.quantity = 1;
                conn.p.item.add_item_inventory47(7, item47);

                ItemTemplate3 temp3 = ItemTemplate3.item.get(Util.random(4577, 4585));
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

                Admin.randomMedal(conn.p, (byte) 4, (byte) 6, true);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topLevel.remove(conn.p.name);
                break;
            }
        }
      //  Manager.gI().notifierBot.sendNotification(conn.p.name + " đã nhận quà top level");
    }

    public static void quatopEvent(Session conn) throws IOException {
        if (!Admin.topEvent.containsKey(conn.p.name)) {
            Service.send_notice_box(conn, "Không có tên");
            return;
        }
        if (conn.p.item.get_inventory_able() < 10) {
            Service.send_notice_box(conn, "Hành trang cần tối thiểu 10 ô trống");
            return;
        }
        switch (Admin.topEvent.get(conn.p.name)) {
            case 1: {
                ItemTemplate3 temp3 = ItemTemplate3.item.get(4640); // cánh
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

                int id = 4812;
                if (conn.p.clazz <= 1) {
                    id = 4813;
                }
                temp3 = ItemTemplate3.item.get(id);// Thời trang rồng
                it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = new ArrayList<>();
                it.op.add(new Option(7, 7000));
                it.op.add(new Option(8, 7000));
                it.op.add(new Option(9, 7000));
                it.op.add(new Option(10, 7000));
                it.op.add(new Option(11, 7000));
                it.op.add(new Option(27, 1500));
                it.op.add(new Option(-128, 500));
                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                temp3 = ItemTemplate3.item.get(4617);// trứng rồng lửa
                it = new Item3();
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

                Admin.randomMedal(conn.p, (byte) 4, (byte) 10, false);
                randomTT(conn, (byte) 5, (byte) 6);
                randomTT(conn, (byte) 5, (byte) 6);
                randomTT(conn, (byte) 5, (byte) 6);
                randomTT(conn, (byte) 5, (byte) 6);
                randomTT(conn, (byte) 5, (byte) 6);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topEvent.remove(conn.p.name);
                break;
            }
            case 2: {
                ItemTemplate3 temp3 = ItemTemplate3.item.get(4641); // cánh
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

                int id = 4812;
                if (conn.p.clazz <= 1) {
                    id = 4813;
                }
                temp3 = ItemTemplate3.item.get(id);// Thời trang rồng
                it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = new ArrayList<>();
                it.op.add(new Option(7, 5000));
                it.op.add(new Option(8, 5000));
                it.op.add(new Option(9, 5000));
                it.op.add(new Option(10, 5000));
                it.op.add(new Option(11, 5000));
                it.op.add(new Option(27, 1500));
                it.op.add(new Option(-128, 500));

                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                temp3 = ItemTemplate3.item.get(4617);// trứng rồng lửa
                it = new Item3();
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

                Admin.randomMedal(conn.p, (byte) 4, (byte) 8, false);
                randomTT(conn, (byte) 5, (byte) 7);
                randomTT(conn, (byte) 5, (byte) 7);
                randomTT(conn, (byte) 5, (byte) 7);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topEvent.remove(conn.p.name);
                break;
            }
            case 3: {
                ItemTemplate3 temp3 = ItemTemplate3.item.get(4641); // cánh
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
                it.expiry_date = 6L * 30 * 24 * 60 * 60 * 1000 + System.currentTimeMillis();
                conn.p.item.add_item_inventory3(it);

                int id = 4812;
                if (conn.p.clazz <= 1) {
                    id = 4813;
                }
                temp3 = ItemTemplate3.item.get(id);// Thời trang rồng
                it = new Item3();
                it.id = temp3.getId();
                it.name = temp3.getName();
                it.clazz = temp3.getClazz();
                it.type = temp3.getType();
                it.level = temp3.getLevel();
                it.icon = temp3.getIcon();
                it.op = new ArrayList<>();
                it.op.add(new Option(7, 4000));
                it.op.add(new Option(8, 4000));
                it.op.add(new Option(9, 4000));
                it.op.add(new Option(10, 4000));
                it.op.add(new Option(11, 4000));
                it.op.add(new Option(27, 1500));
                it.op.add(new Option(-128, 500));

                it.color = 5;
                it.part = temp3.getPart();
                conn.p.item.add_item_inventory3(it);

                Admin.randomMedal(conn.p, (byte) 4, (byte) 6, false);
                randomTT(conn, (byte) 5, (byte) 5);
                Service.send_notice_box(conn, "Bạn đã nhận được quà");
                Admin.topEvent.remove(conn.p.name);
                break;
            }
        }
       // Manager.gI().notifierBot.sendNotification(conn.p.name + " đã nhận quà top event");
    }
//// Lấy tổng nạp hiện tại
//public static String xemmocnap(Session conn) {
//    try {
//        if (conn == null || conn.p == null) return "0";
//        return Util.number_format(conn.p.tongnap);
//    } catch (Exception e) {
//        return "0";
//    }
//}
//
//// Kiểm tra đã nhận mốc chưa
//public static boolean daNhanMoc(Session conn, int m) {
//    try (Connection con = SQL.gI().getConnection();
//         PreparedStatement ps = con.prepareStatement("SELECT mocnap" + m + " FROM player WHERE id = ?")) {
//        ps.setInt(1, conn.p.ID);
//        ResultSet rs = ps.executeQuery();
//        if (rs.next()) {
//            return rs.getInt(1) == 1;
//        }
//    } catch (SQLException e) {
//        e.printStackTrace();
//    }
//    return false;
//}
//
//// Đánh dấu mốc đã nhận
//public static void setNhanMoc(Session conn, int m) {
//    try (Connection connection = DriverManager.getConnection(
//            "jdbc:mysql://localhost:3306/maxhso?useSSL=false", "root", "");
//         PreparedStatement pstmt = connection.prepareStatement(
//            "UPDATE player SET mocnap" + m + " = 1 WHERE id = ?")) {
//
//        pstmt.setInt(1, conn.p.ID);
//        int rows = pstmt.executeUpdate();
//        if (rows == 0) {
//            System.out.println("Update mốc nạp thất bại cho player id=" + conn.p.ID);
//        }
//    } catch (SQLException e) {
//        e.printStackTrace();
//    }
//}
////public static void resetMoc(Session conn, int moc) {
//    try (Connection con = SQL.gI().getConnection();
//         PreparedStatement ps = con.prepareStatement("UPDATE player SET mocnap" + moc + " = 0 WHERE id = ?")) {
//        ps.setInt(1, conn.p.ID);
//        ps.executeUpdate();
//
//        // reset biến RAM tương ứng
//        switch (moc) {
//            case 1 -> conn.p.mocnap1 = 0;
//            case 2 -> conn.p.mocnap2 = 0;
//            case 3 -> conn.p.mocnap3 = 0;
//            case 4 -> conn.p.mocnap4 = 0;
//            case 5 -> conn.p.mocnap5 = 0;
//            case 6 -> conn.p.mocnap6 = 0;
//            case 7 -> conn.p.mocnap7 = 0;
//            case 8 -> conn.p.mocnap8 = 0;
//            case 9 -> conn.p.mocnap9 = 0;
//            case 10 -> conn.p.mocnap10 = 0;
//            case 11 -> conn.p.mocnap11 = 0;
//            case 12 -> conn.p.mocnap12 = 0;
//        }
//    } catch (SQLException e) {
//        e.printStackTrace();
//    }
//}
//
//
//
//public static void quamocnapExact(Session conn, int moc, long giaTriNap, long soVang, long soNgoc) {
//    try {
//        long soTienNapLanNay = conn.p.lastNapAmount;
//        System.out.println("===== DEBUG NHẬN MỐC NẠP =====");
//        System.out.println("Player ID: " + conn.p.ID);
//        System.out.println("Mốc nạp: " + moc + " - Giá trị mốc: " + giaTriNap);
//        System.out.println("Tổng nạp hiện tại: " + conn.p.tongnap);
//        System.out.println("Số tiền vừa nạp lần này: " + soTienNapLanNay);
//
//        // Kiểm tra ô trống
//        int invent_able = 10;
//        if (invent_able < 5) {
//            Service.send_notice_box(conn, "hành trang đầy, vui lòng dọn trống ít nhất 5 ô.");
//            return;
//        }
//
//        // Chỉ nhận khi số tiền vừa nạp = giá trị mốc
//        if (soTienNapLanNay != giaTriNap) {
//            Service.send_notice_box(conn, "Bạn phải nạp đúng mốc " + Util.number_format(giaTriNap) +
//                    " VNĐ mới nhận được phần thưởng.\nHiện tại tổng nạp: " + Util.number_format(conn.p.tongnap));
//            return;
//        }
//
//        // Kiểm tra đã nhận mốc chưa → reset để nhận lần này
//        if (daNhanMoc(conn, moc)) {
//            resetMoc(conn, moc);
//        }
//
//        // Cộng thưởng
//        conn.p.update_vang(soVang, String.valueOf(conn.p.ID));
//        conn.p.update_ngoc(soNgoc);
//        Service.send_notice_box(conn, "Mốc nạp " + Util.number_format(giaTriNap) +
//                " VNĐ: +" + Util.number_format(soVang) + " vàng + " +
//                Util.number_format(soNgoc) + " ngọc");
//        // Đánh dấu mốc nhận lần này
//        setNhanMoc(conn, moc);
//        // Reset số tiền nạp lần này
//        conn.p.lastNapAmount = 0;
//        System.out.println("[DEBUG] Nhận mốc xong, tổng nạp vẫn giữ nguyên: " + conn.p.tongnap);
//        System.out.println("===== END DEBUG =====");
//
//    } catch (Exception e) {
//        e.printStackTrace();
//        try {
//            Service.send_notice_box(conn, "Có lỗi xảy ra khi nhận mốc nạp " + moc);
//        } catch (IOException ignored) {}
//    }
//}


//
//// Ví dụ 12 hàm debug
//public static void quamocnap1(Session conn) { quamocnapExact(conn, 1, 50000, 5000000, 5000); }
//public static void quamocnap2(Session conn) { quamocnapExact(conn, 2, 100000, 10000000, 10000); }
//public static void quamocnap3(Session conn) { quamocnapExact(conn, 3, 200000, 20000000, 20000); }
//public static void quamocnap4(Session conn) { quamocnapExact(conn, 4, 300000, 30000000, 30000); }
//public static void quamocnap5(Session conn) { quamocnapExact(conn, 5, 500000, 50000000, 50000); }
//public static void quamocnap6(Session conn) { quamocnapExact(conn, 6, 1000000, 100000000, 100000); }
//public static void quamocnap7(Session conn) { quamocnapExact(conn, 7, 2000000, 200000000, 200000); }
//public static void quamocnap8(Session conn) { quamocnapExact(conn, 8, 3000000, 300000000, 300000); }
//public static void quamocnap9(Session conn) { quamocnapExact(conn, 9, 4000000, 400000000, 400000); }
//public static void quamocnap10(Session conn) { quamocnapExact(conn, 10, 5000000, 500000000, 500000); }
//public static void quamocnap11(Session conn) { quamocnapExact(conn, 11, 6000000, 600000000, 600000); }
//public static void quamocnap12(Session conn) { quamocnapExact(conn, 12, 7000000, 700000000, 700000); }

}






