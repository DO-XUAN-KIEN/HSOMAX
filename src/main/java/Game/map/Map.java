package Game.map;

import Game.Boss.BossServer;
import Game.ai.MobAi;
import Game.ai.NhanBan;
import Game.ai.Player_Nhan_Ban;
import Game.client.Player;
import Game.client.Squire;
import Game.core.Manager;
import Game.core.SaveData;
import Game.core.Util;
import Game.event.MobDuaBe;
import Game.event.MobMy;

import Game.io.Session;
import Game.History.His_DelItem;
import Game.NPC.NpcTemplate;

import Game.ai.Bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Game.map.Dungeon;
import Game.client.Party;
import Game.client.Pet;
import Game.core.Service;
import Game.event.MobCay;

import Game.activities.KingCup;
import Game.activities.UseItemArena;
import Game.activities.ChienTruong;
import Game.io.Message;

import java.util.concurrent.CopyOnWriteArrayList;

import Game.template.*;

public class Map implements Runnable {
    public static final List<Map[]> entrys = new CopyOnWriteArrayList<>();
    public static final int NGOC_KHU_2 = 10;
    public final List<Player> players;
    public long[] time_use_item_arena = new long[]{0, 0, 0};
    public final short map_id;
    public final byte zone_id;
    public final ItemMap[] item_map;
    private final Thread mapthread;
    public Mob_in_map[] mobs;
    public NpcMap[] npc_0;
    public NpcMap[] npc_1;
    public NpcMap[] npc_2;
    public List<NpcMap> npcMaps;
    public static short head;
    public static short eye;
    public static short hair;
    public static short weapon;
    public static short body;
    public static short leg;
    public static short hat;
    public static short wing;
    public static String king_battlefield_name = "";
    public final String name;
    public final List<Vgo> vgos;
    public final byte typemap;
    public final boolean ismaplang;
    public final boolean showhs;
    public final short maxplayer;
    public byte maxzone;
    private final byte[] map_data;
    private boolean running;
    public int num_mob_super;
    public int request_level;
    public Dungeon d;
    public KingCup kingCupMap;
    public LuaThieng luathieng;
    public short mapW;
    public short mapH;
    public long time_ct;
    public List<Bot> bots;
    public long time_add_bot;
    public CopyOnWriteArrayList<MobCay> mobEvens = new CopyOnWriteArrayList<MobCay>();
    public CopyOnWriteArrayList<MobMy.Mob_My> mobMyNuong = new CopyOnWriteArrayList<Game.event.MobMy.Mob_My>();
    public CopyOnWriteArrayList<MobDuaBe.Mob_duabe> mobDuaBe = new CopyOnWriteArrayList<>();

    public final CopyOnWriteArrayList<Mob_in_map> bossInMaps = new CopyOnWriteArrayList<>();
    public final CopyOnWriteArrayList<MobAi> Ai_entrys;
    public UseItemArena Arena;

    public Map(short id, int zone, String name, byte typemap, boolean ismaplang, boolean showhs,
               int maxplayer, int maxzone, List<Vgo> vgo, int request_level) throws IOException {
        this.map_id = id;
        this.zone_id = (byte) zone;
        this.name = name;
        this.typemap = typemap;
        this.ismaplang = ismaplang;
        this.showhs = showhs;
        this.maxplayer = (short) maxplayer;
        this.maxzone = (byte) 7;
        this.item_map = new ItemMap[100];
        this.mapthread = new Thread(this);
        this.mobs = new Mob_in_map[0];
        this.players = new ArrayList<>();
        this.vgos = vgo;
        this.running = false;
        this.num_mob_super = 0;
        this.map_data = Util.loadfile("data/map/" + this.map_id);
        this.request_level = request_level;
        this.luathieng = null;
        byte[] data = map_data; // Mảng byte chứa dữ liệu
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
        java.io.DataInputStream dis = new java.io.DataInputStream(bais);

        short number = dis.readShort();
        String str = dis.readUTF();
        short num = dis.readShort();
        this.mapW = dis.readByte();
        this.mapH = dis.readByte();
        Ai_entrys = new CopyOnWriteArrayList<>();
        if (map_id == 54 || map_id == 56 || map_id == 58 || map_id == 60 || map_id == 61) {
            Arena = new UseItemArena();
        }
        if (this.zone_id == maxzone) {
            bots = new ArrayList<>();
        }
    }

    @Override
    public void run() {
        this.running = true;
        long time1 = 0;
        long time2 = 0;
        long time3 = 0;
        while (this.running) {
            try {
                time1 = System.currentTimeMillis();
                if (zone_id <= maxzone || map_id == 102) {
                    update();
                    update_AI();
                    if (Map.is_map_chiem_mo(this, true)) {
                        update_nhanban();
                        if (Manager.gI().chiem_mo.isRunning()) {
                            Mob_MoTaiNguyen moTaiNguyen = Manager.gI().chiem_mo.get_mob_in_map(this);
                            moTaiNguyen.update(this);
                        }
                    }
                    if (ChienTruong.gI().getStatus() == 2 && Map.is_map_chien_truong(this.map_id)) {
                        if (this.time_ct < System.currentTimeMillis()) {
                            this.time_ct = System.currentTimeMillis() + 5000L;
                            for (Player p0 : this.players) {
                                ChienTruong.gI().send_info(p0);
                            }
                        }
                        Player_Nhan_Ban.update(this);
                    }
                    if (this.map_id == 48 && d != null) {
                        d.update();
                    }
                    if (map_id == 102 && kingCupMap != null) {
                        kingCupMap.update();
                        kingCupMap.finish();
                    }
                }
                time2 = System.currentTimeMillis();
                time3 = (1_000L - (time2 - time1));
                if (time3 > 0) {
                    if (time3 < 20) {
                        System.err.println("map_id " + this.map_id + " - zone " + (this.zone_id + 1) + " overload...");
                    }
                    Thread.sleep(time3);
                }
            } catch (Exception e) {
            }
        }
    }

    public boolean isMapChiemThanh() {
        return map_id >= 83 && map_id <= 87;
    }

    public boolean isMapLoiDai() {
        return map_id == 102;
    }

    public boolean isMapLangPhuSuong() {
        return map_id == 125 || map_id == 127 || map_id == 129 || map_id == 132;
    }

    public void BossDie(Mob_in_map mob) {
        mob.isdie = true;
    }

    public void BossIn4(Session conn, int idx) throws IOException {
        for (Mob_in_map temp : bossInMaps) {
            if (temp.ID == idx && !temp.isdie) {
                Message m = new Message(7);
                m.writer().writeShort(idx);
                m.writer().writeByte((byte) temp.level);
                m.writer().writeShort(temp.x);
                m.writer().writeShort(temp.y);
                m.writer().writeInt(temp.hp);
                m.writer().writeInt(temp.get_HpMax());
                m.writer().writeByte(20); // id skill monster (Spec: 32, ...)
                m.writer().writeInt(temp.timeBossRecive / 1000);
                m.writer().writeShort(-1); // clan monster
                m.writer().writeByte(0);
                m.writer().writeByte(2); // speed
                m.writer().writeByte(0);
                m.writer().writeUTF("");
                m.writer().writeLong(-1);
                m.writer().writeByte(temp.color_name); // color name 1: blue, 2: yellow
                conn.addmsg(m);
                m.cleanup();
                return;
            }
        }
        Message m2 = new Message(17);
        m2.writer().writeShort(-1);
        m2.writer().writeShort(idx);
        conn.addmsg(m2);
        m2.cleanup();
    }

    public Mob_in_map GetBoss(int index) {
        for (Mob_in_map mob : bossInMaps) {
            if (mob.ID == index) {
                return mob;
            }
        }
        return null;
    }

    private void update_AI() {

    }

    private synchronized void update_nhanban() throws IOException {
        // update mo tai nguyen
        Mob_MoTaiNguyen mobtainguyen = Manager.gI().chiem_mo.get_mob_in_map(this);
        if (mobtainguyen != null) {
            if (mobtainguyen.hp <= 0) {
                mobtainguyen.Set_hpMax((mobtainguyen.get_HpMax() / 10) * 12);
                if (mobtainguyen.get_HpMax() > 20_000_000) {
                    mobtainguyen.Set_hpMax(20_000_000);
                }
                mobtainguyen.hp = mobtainguyen.get_HpMax();
                mobtainguyen.isbuff_hp = false;
            }
            if (!mobtainguyen.isbuff_hp && mobtainguyen.hp < mobtainguyen.get_HpMax() / 2) {
                mobtainguyen.Set_hpMax((mobtainguyen.get_HpMax() / 10) * 12);
                if (mobtainguyen.get_HpMax() > 20_000_000) {
                    mobtainguyen.Set_hpMax(20_000_000);
                }
                mobtainguyen.isbuff_hp = true;
            }
            if (mobtainguyen.isbuff_hp && mobtainguyen.time_buff < System.currentTimeMillis()) {
                mobtainguyen.time_buff = System.currentTimeMillis() + 10000L;
                int par = mobtainguyen.get_HpMax() / 20;
                mobtainguyen.hp += par;
                if (mobtainguyen.hp >= mobtainguyen.get_HpMax()) {
                    mobtainguyen.hp = mobtainguyen.get_HpMax();
                    mobtainguyen.isbuff_hp = false;
                }
                Message m_hp = new Message(32);
                m_hp.writer().writeByte(1);
                m_hp.writer().writeShort(mobtainguyen.ID);
                m_hp.writer().writeShort(-1); // id potion in bag
                m_hp.writer().writeByte(0);
                m_hp.writer().writeInt(mobtainguyen.get_HpMax()); // max hp
                m_hp.writer().writeInt(mobtainguyen.hp); // hp
                m_hp.writer().writeInt(par); // param use
                for (Player player : this.players) {
                    player.conn.addmsg(m_hp);
                }
                m_hp.cleanup();
            }
            for (int i = 0; i < mobtainguyen.nhanBans.size(); i++) {
                NhanBan temp = mobtainguyen.nhanBans.get(i);
                if (temp != null) {
                    temp.update(this);
                }
            }
        }
    }

    private void update() {
        try {
            long now_time = System.currentTimeMillis();

            // 1. Xử lý chuyển map đặc biệt (Map 53-60)
            if (this.map_id >= 53 && this.map_id <= 60 && this.map_id % 2 == 1 && !vgos.isEmpty()) {
                Vgo v = vgos.get(0);
                for (int i1 = players.size() - 1; i1 >= 0; i1--) {
                    Player p1 = players.get(i1);
                    if (p1 != null && now_time - p1.timeCantChangeMap > 15_000) {
                        p1.change_map(p1, v);
                    }
                }
            }

            // 2. Xử lý Lửa Thiêng
            if (this.luathieng != null && this.luathieng.isExpired()) {
                for (Player p : players) {
                    try { this.luathieng.sendRemove(p); } catch (Exception e) {}
                }
                this.luathieng = null;
            }

            // --- VÒNG LẶP XỬ LÝ NGƯỜI CHƠI (PLAYER) ---
            for (int i1 = players.size() - 1; i1 >= 0; i1--) {
                try {
                    Player p = players.get(i1);

                    // Check kết nối
                    if (p == null || p.conn == null || p.conn.socket == null || p.conn.socket.isClosed() || !p.conn.connected) {
                        players.remove(p);
                        if (p != null && p.conn != null) {
                            p.conn.close();
                        }
                        continue;
                    }

                    // Xử lý tàng hình
                    if (p.get_EffMe_Kham(StrucEff.TangHinh) != null) {
                        continue;
                    } else if (p.isTangHinh && p.get_EffMe_Kham(StrucEff.TangHinh) == null) {
                        p.isTangHinh = false;
                        Message m6 = new Message(4);
                        m6.writer().writeByte(0);
                        m6.writer().writeShort(0);
                        m6.writer().writeShort(p.ID);
                        m6.writer().writeShort(p.x);
                        m6.writer().writeShort(p.y);
                        m6.writer().writeByte(-1);
                        MapService.send_msg_player_inside(p.map, p, m6, true);
                        m6.cleanup();
                    }

                    // King Cup refresh
                    if (p.isdie && kingCupMap != null && p.time_die + 3000L > now_time) {
                        kingCupMap.refresh();
                    }

                    // --- XỬ LÝ PET (Chỉ chạy ở Map 50) ---
                    if (this.map_id == 50) {
                        Iterator<Pet> iter = p.mypet.iterator();
                        while (iter.hasNext()) {
                            Pet temp = iter.next();
                            // Hết hạn Pet
                            if (temp.expiry_date > 0 && temp.expiry_date < now_time) {
                                if (temp.is_follow) p.pet_follow_id = -1;
                                iter.remove();
                                Service.send_wear(p);
                                Service.send_char_main_in4(p);
                                continue;
                            }
                            // Ấp trứng nở
                            if (temp.is_hatch && temp.time_born < now_time) {
                                temp.is_hatch = false;
                                // Gửi gói tin cập nhật trứng nở (Gộp gọn)
                                Message m = new Message(44);
                                m.writer().writeByte(28);
                                m.writer().writeByte(0);
                                m.writer().writeByte(3);
                                m.writer().writeByte(3);

                                int dem = 0;
                                for (Pet t2 : p.mypet) if (temp.is_hatch && t2.time_born > now_time) dem++;
                                m.writer().writeByte(dem);
                                for (Pet temp2 : p.mypet) {
                                    if (temp.is_hatch && temp2.time_born > now_time) {
                                        int id_ = temp.get_id();
                                        m.writer().writeUTF(ItemTemplate3.item.get(id_).getName());
                                        m.writer().writeByte(4); // clazz
                                        m.writer().writeShort(id_);
                                        m.writer().writeByte(14); // type
                                        m.writer().writeShort(ItemTemplate3.item.get(id_).getIcon());
                                        m.writer().writeByte(0); // tier
                                        m.writer().writeShort(10); // level
                                        m.writer().writeByte(0); // color
                                        m.writer().writeByte(1);
                                        m.writer().writeByte(1);
                                        m.writer().writeByte(0); // op size
                                        long time2 = ((temp2.time_born - now_time) / 60000) + 1;
                                        m.writer().writeInt((int) time2);
                                        m.writer().writeByte(0);
                                    }
                                }
                                p.conn.addmsg(m);
                                m.cleanup();
                                //
                                m = new Message(44);
                                m.writer().writeByte(28);
                                m.writer().writeByte(1);
                                m.writer().writeByte(9);
                                m.writer().writeByte(9);
                                m.writer().writeUTF(temp.name);
                                m.writer().writeByte(temp.type);
                                m.writer().writeShort(p.mypet.indexOf(temp)); // id
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
                                p.conn.addmsg(m);
                                m.cleanup();
                            }
                        }
                    }

                    // Đệ tử
                    if (p.squire != null && !p.isSquire) Squire.update(p);

                    // Cập nhật cánh & Pet ăn
                    p.update_wings_time();
                    for (Pet pet : p.mypet) {
                        if (pet.time_eat < now_time) {
                            pet.time_eat = now_time + 180_000L;
                            pet.update_grown((short) -1);
                        }
                    }

                    p.updateEff(); // Update hiệu ứng nhân vật

                    // --- LOGIC KHI CÒN SỐNG ---
                    if (!p.isdie) {
                        p.update(this);

                        // Hồi phục HP/MP khi cưỡi thú (Tối ưu)
                        if (p.time_horse_regen < now_time) {
                            p.time_horse_regen = now_time + 10000L;
                            int maxMP = p.get_MpMax();
                            // Kiểm tra và bảo vệ MP không âm
                            if (p.mp < 0) {
                                p.mp = 0;  // Đảm bảo MP không âm
                            }
                            if (maxMP > 0 && p.mp >= 0) {
                                if (p.type_use_horse == Horse.RONG_BANG ||
                                        p.type_use_horse == Horse.NGUA_TRANG ||
                                        p.type_use_horse == Horse.XE_TRUOT_TUYET ||
                                        p.type_use_horse == Horse.SKELETON ||
                                        p.type_use_horse == Horse.CHUOT_TUYET) { // Check ID ngựa clan nhanh
                                    int regenMP = maxMP / 5; // 20%
                                    if (p.mp < maxMP) {
                                        p.mp += regenMP;
                                        if (p.mp > maxMP) p.mp = maxMP;
                                        Service.send_char_main_in4(p); // Chỉ gửi khi có thay đổi MP
                                    }
                                }
                            }
                        }

                        if (p.squire != null && p.isLiveSquire) {
                            p.squire.update(this);
                        }

                        // --- TỐI ƯU HIỆU ỨNG ĐỒ MẶC (Gộp chung vào 1 lần check) ---
                        // Thay vì check từng món item[12], item[19], item[20] riêng lẻ
                        // Ta gộp chung vào 1 lần check time_eff_wear
                        if (p.time_eff_wear < now_time) {
                            p.time_eff_wear = now_time + 5000L; // 5 giây chạy 1 lần

                            // 1. Hiệu ứng Medal (Item 12)
                            Item3 it = p.item.wear[12];
                            if (it != null && it.tier >= 3) {
                                sendEffMedal(p, it); // Tách ra hàm riêng cho gọn
                            }
                            // 3. Hiệu ứng đặc biệt (WearEffect)
                            for (Item3 it_ : p.item.wear) {
                                if (it_ != null) {
                                    List<WearEffect.EffectInfo> listInfo = WearEffect.getListEffect(it_.id);
                                    if (listInfo != null) {
                                        for (WearEffect.EffectInfo info : listInfo) {
                                            if (info.type == 112) p.send_eff_112(info.effId, 5000);
                                            else p.send_eff_111(info.effId, 5000);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {}
            }

            // --- VÒNG LẶP MOB & BOSS ---
            for (Mob_in_map mob : this.mobs) {
                if (mob != null) mob.update(this);
            }
            for (Mob_in_map mob : this.bossInMaps) {
                if (mob.isdie) BossServer.refresh_boss((int) map_id, (int) zone_id, mob);
            }
            // --- VÒNG LẶP ITEM MAP (Vật phẩm rơi) ---
            for (int i = 0; i < this.item_map.length; i++) {
                if (this.item_map[i] != null) {
                    // Hết thời gian bảo hộ
                    if (this.item_map[i].idmaster != -1 && (this.item_map[i].time_exist - now_time < 15000L)) {
                        this.item_map[i].idmaster = -1;
                    }
                    // Hết thời gian tồn tại -> Xóa
                    if (this.item_map[i].time_exist < now_time) {
                        this.item_map[i] = null;
                        // Cần gửi gói tin xóa item cho client biết (nếu chưa có)
                        // Service.remove_item_map(this, i);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- CÁC HÀM PHỤ TRỢ (Để code gọn hơn) ---
    private void sendEffMedal(Player p, Item3 it) {
        try {
            Message m = new Message(-49);
            m.writer().writeByte(2);
            m.writer().writeShort(0);
            m.writer().writeByte(0);
            m.writer().writeByte(0);
            byte eff_ = 3;
            switch (it.id) {
                case 4588: eff_ = (byte) ((it.tier >= 15) ? 26 : (it.tier >= 12) ? 25 : (it.tier >= 9) ? 2 : (it.tier >= 6) ? 1 : 0); break;
                case 4589: eff_ = (byte) ((it.tier >= 15) ? 28 : (it.tier >= 12) ? 27 : (it.tier >= 9) ? 11 : (it.tier >= 6) ? 10 : 9); break;
                case 4590: eff_ = (byte) ((it.tier >= 15) ? 32 : (it.tier >= 12) ? 31 : (it.tier >= 9) ? 8 : (it.tier >= 6) ? 7 : 6); break;
                default:   eff_ = (byte) ((it.tier >= 15) ? 30 : (it.tier >= 12) ? 29 : (it.tier >= 9) ? 5 : (it.tier >= 6) ? 4 : 3); break;
            }
            m.writer().writeByte(eff_);
            m.writer().writeShort(p.ID);
            m.writer().writeByte(0); m.writer().writeByte(0); m.writer().writeInt(5000);
            MapService.send_msg_player_inside(this, p, m, true);
            m.cleanup();
        } catch (Exception e) {}
    }

    public void start_map() {
        this.mapthread.start();
    }

    public void stop_map() {
        this.running = false;
        this.mapthread.interrupt();
    }

    public static Player get_player_by_name(String name) {
        for (Map[] maps : entrys) {
            for (Map map : maps) {
                for (Player p0 : map.players) {
                    if (p0.name.equals(name)) {
                        return p0;
                    }

                }
            }
        }
        return null;
    }

    public static Map[] get_map_by_id(int id) {
        for (Map[] temp : entrys) {
            if (temp[0].map_id == id) {
                return temp;
            }
        }
        return null;
    }

    public static Map get_id(int id) {
        for (Map[] temp : entrys) {
            if (temp[0].map_id == id) {
                return temp[0];
            }
        }
        return null;
    }

    public void send_map_data(Player p) {
        try {
            if (p.x / 24 >= mapW || p.y / 24 >= mapH || p.x < 0 || p.y < 0) {
                Vgo vgo = new Vgo();
                vgo.id_map_go = 1;
                vgo.x_new = 432;
                vgo.y_new = 354;
                p.change_map(p, vgo);
                return;
            }
            Message m = new Message(12);
            m.writer().writeShort(this.map_id);
            m.writer().writeShort((short) (p.x / 24));
            m.writer().writeShort((short) (p.y / 24));
            m.writer().write(this.map_data);
            m.writer().writeByte(this.zone_id); // zone
            m.writer().writeByte(this.typemap);
            m.writer().writeBoolean(this.ismaplang);
            m.writer().writeBoolean(this.showhs);
            p.conn.addmsg(m);
            m.cleanup();
            // send npc;
            if (Manager.gI().event == 2) {
                switch (this.map_id) {
                    case 7:
                    case 24:
                    case 72:
                    case 96: {
                        Service.send_msg_data(p.conn, -50, ("vuahung_ev2_" + this.map_id));
                        break;
                    }
                }
            } else if (Manager.gI().event == 4) {
                switch (this.map_id) {
                    case 7:
                    case 24:
                    case 72:
                    case 96: {
                        Service.send_msg_data(p.conn, -50, ("vuahung_ev2_" + this.map_id));
                        break;
                    }
                }
            }
            npcMaps = new ArrayList<>();
            sendNpc(p, npc_0);
            sendNpc(p, npc_1);
            sendNpc(p, npc_2);
            // mob mo tai nguyen
            // mob mo tai nguyen (FIX CASH)
            if (Map.is_map_chiem_mo(p.map, true)) {

                if (Manager.gI().chiem_mo == null) {
                    return;
                }

                Mob_MoTaiNguyen mob_tainguyen =
                        Manager.gI().chiem_mo.get_mob_in_map(p.map);

                if (mob_tainguyen == null) {
                    return;
                }

                Message m2 = new Message(4);
                m2.writer().writeByte(1);
                m2.writer().writeShort(64);
                m2.writer().writeShort(mob_tainguyen.ID);
                m2.writer().writeShort(mob_tainguyen.x);
                m2.writer().writeShort(mob_tainguyen.y);
                m2.writer().writeByte(-1);

                if (mob_tainguyen.nhanban != null
                        && mob_tainguyen.nhanban.ID > 0
                        && mob_tainguyen.nhanban.x >= 0
                        && mob_tainguyen.nhanban.y >= 0) {

                    m2.writer().writeByte(0);
                    m2.writer().writeShort(0);
                    m2.writer().writeShort(mob_tainguyen.nhanban.ID);
                    m2.writer().writeShort(mob_tainguyen.nhanban.x);
                    m2.writer().writeShort(mob_tainguyen.nhanban.y);
                    m2.writer().writeByte(-1);
                }

                p.conn.addmsg(m2);
                m2.cleanup();
            }


            // monument
            if (this.map_id == 1) {
                m = new Message(-96);
                m.writer().writeShort(288);
                m.writer().writeShort(312);
                m.writer().writeShort(264);
                m.writer().writeShort(288);
                m.writer().writeByte(3);
                m.writer().writeByte(1);
                m.writer().writeByte(-1);
                m.writer().writeByte(-25);
                m.writer().writeByte(1);
                m.writer().writeUTF("VUA CHIẾN TRƯỜNG");
                m.writer().writeUTF(Map.king_battlefield_name);
                m.writer().writeByte(-49);
                m.writer().writeByte(15);
                //
                m.writer().writeShort(Map.weapon); // weapon
                m.writer().writeShort(Map.body); // body
                m.writer().writeShort(-1);
                m.writer().writeShort(-1);
                m.writer().writeShort(-1);
                m.writer().writeShort(3); // pet
                m.writer().writeShort(Map.hat); // hat
                m.writer().writeShort(Map.leg); // leg
                m.writer().writeShort(-1);
                m.writer().writeShort(-1);
                m.writer().writeShort(Map.wing); // wing
                m.writer().writeShort(-1);
                m.writer().writeShort(Map.head); // head
                m.writer().writeShort(Map.eye); // eye
                m.writer().writeShort(Map.hair); // hair
                //
                m.writer().write(Util.loadfile("data/msg/msg_-96_x" + p.conn.zoomlv));
                p.conn.addmsg(m);
                m.cleanup();

                NpcMap temp = new NpcMap();
                temp.npcTemplate = NpcTemplate.getNpcById((short) -49);
                temp.x = 384;
                temp.y = 432;
                npcMaps.add(temp);
            } else if (this.map_id == 50) { // map pet
                m = new Message(44);
                m.writer().writeByte(28);
                m.writer().writeByte(0);
                m.writer().writeByte(3);
                m.writer().writeByte(3);
                int dem = 0;
                long now_time = System.currentTimeMillis();
                for (Pet temp2 : p.mypet) {
                    if (temp2.is_hatch && temp2.time_born > now_time) {
                        dem++;
                    }
                }
                m.writer().writeByte(dem);
                for (Pet temp2 : p.mypet) {
                    if (temp2.is_hatch && temp2.time_born > now_time) {
                        int id_ = temp2.get_id();
                        m.writer().writeUTF(ItemTemplate3.item.get(id_).getName());
                        m.writer().writeByte(4); // clazz
                        m.writer().writeShort(id_);
                        m.writer().writeByte(14); // type
                        m.writer().writeShort(ItemTemplate3.item.get(id_).getIcon());
                        m.writer().writeByte(0); // tier
                        m.writer().writeShort(10); // level
                        m.writer().writeByte(0); // color
                        m.writer().writeByte(1);
                        m.writer().writeByte(1);
                        m.writer().writeByte(0); // op size
                        long time2 = ((temp2.time_born - now_time) / 60000) + 1;
                        m.writer().writeInt((int) time2);
                        m.writer().writeByte(0);
                    }
                }
                p.conn.addmsg(m);
                m.cleanup();
                //
                m = new Message(44);
                m.writer().writeByte(28);
                m.writer().writeByte(0);
                m.writer().writeByte(9);
                m.writer().writeByte(9);
                m.writer().writeByte(0);
                p.conn.addmsg(m);
                m.cleanup();
                //
                m = new Message(44);
                m.writer().writeByte(28);
                m.writer().writeByte(0);
                m.writer().writeByte(9);
                m.writer().writeByte(9);
                dem = 0;
                for (Pet temp : p.mypet) {
                    if (!temp.is_follow && !temp.is_hatch) {
                        dem++;
                    }
                }
                m.writer().writeByte(dem); // size pet
                //
                for (int i = 0; i < p.mypet.size(); i++) {
                    if (!p.mypet.get(i).is_follow && !p.mypet.get(i).is_hatch) {
                        m.writer().writeUTF(p.mypet.get(i).name);
                        m.writer().writeByte(p.mypet.get(i).type);
                        m.writer().writeShort(i); // id
                        m.writer().writeShort(p.mypet.get(i).level);
                        m.writer().writeShort(p.mypet.get(i).getlevelpercent()); // exp
                        m.writer().writeByte(p.mypet.get(i).type);
                        m.writer().writeByte(p.mypet.get(i).icon);
                        m.writer().writeByte(p.mypet.get(i).nframe);
                        m.writer().writeByte(p.mypet.get(i).color);
                        m.writer().writeInt(p.mypet.get(i).get_age());
                        m.writer().writeShort(p.mypet.get(i).grown);
                        m.writer().writeShort(p.mypet.get(i).maxgrown);
                        m.writer().writeShort(p.mypet.get(i).sucmanh);
                        m.writer().writeShort(p.mypet.get(i).kheoleo);
                        m.writer().writeShort(p.mypet.get(i).theluc);
                        m.writer().writeShort(p.mypet.get(i).tinhthan);
                        m.writer().writeShort(p.mypet.get(i).maxpoint);
                        m.writer().writeByte(p.mypet.get(i).op.size());
                        for (int i2 = 0; i2 < p.mypet.get(i).op.size(); i2++) {
                            OptionPet temp = p.mypet.get(i).op.get(i2);
                            m.writer().writeByte(temp.id);
                            m.writer().writeInt(p.mypet.get(i).getParam(temp.id));
                            m.writer().writeInt(p.mypet.get(i).getMaxDame(temp.id));
                        }
                    }
                }
                p.conn.addmsg(m);
                m.cleanup();
            }
            if (this.luathieng != null) {
                if (this.luathieng.isExpired()) {
                    this.luathieng.sendRemove(p);
                } else {
                    this.luathieng.send_move(p);
                    this.luathieng.sendInfo(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendNpc(Player p, NpcMap[] npcTemplates) throws IOException {
        if (npcTemplates == null) {
            return;
        }
        for (NpcMap npcMap : npcTemplates) {
            NpcTemplate npcTemplate = npcMap.npcTemplate;
            if (this.map_id == 52 && this.zone_id != 5 && npcTemplate.id == -57) {
                continue;
            }
            Message m = new Message(-50);
            m.writer().writeByte(npcTemplates.length);
            m.writer().writeUTF(npcTemplate.name);
            m.writer().writeUTF(npcTemplate.name_gt);
            m.writer().writeByte(npcTemplate.id);
            m.writer().writeByte(npcTemplate.ID_Image);
            m.writer().writeShort(npcMap.x);
            m.writer().writeShort(npcMap.y);
            m.writer().writeByte(npcTemplate.wBlock);
            m.writer().writeByte(npcTemplate.hBlock);
            m.writer().writeByte(npcTemplate.nFrame);
            m.writer().writeByte(npcTemplate.IdBigAvatar);
            m.writer().writeUTF(npcTemplate.infoObject);
            m.writer().writeByte(npcTemplate.isPerson);
            m.writer().writeByte(npcTemplate.isShowHP);
            if (npcTemplate.id == -65) {
                Service.send_eff_map(this, npcTemplate.id, 60, npcMap.x, npcMap.y, 4, 2, 95);
            } else if (npcTemplate.id == -64) {
                Service.send_eff_map(this, npcTemplate.id, 59, npcMap.x, npcMap.y, 4, 2, 95);
            } else if (npcTemplate.id == -62) {
                Service.send_eff_map(this, npcTemplate.id, 61, npcMap.x, npcMap.y, 3, 2, 75);
            } else if (npcTemplate.id == -66) {
                Service.send_eff_map(this, npcTemplate.id, 64, npcMap.x, npcMap.y, 2, 2, 115);
            } else if (npcTemplate.id == -89) {
                Service.send_eff_map(this, npcTemplate.id, 110, npcMap.x, npcMap.y, 4, 2, 115);
            } else if (npcTemplate.id == -87) {
                Service.send_eff_map(this, npcTemplate.id, 108, npcMap.x, npcMap.y, 4, 2, 115);
            }
            npcMaps.add(npcMap);
            p.conn.addmsg(m);
            m.cleanup();
        }
    }

    public static Map get_map_dungeon(int id) {
        for (Map[] temp : entrys) {
            if (temp[0].map_id == id) {
                return temp[0];
            }
        }
        return null;
    }

    public synchronized void drop_item(Player p, byte type, short id) throws IOException {
        if (type == 4 && id == 245) {
            return;
        }
        His_DelItem hist = new His_DelItem(p.name);
        hist.Logger = "Vứt";
        switch (type) {
            case 3: {
                Item3 temp = p.item.inventory3[id];
                if (temp != null) {
                    if (temp.islock) {
                        Service.send_notice_box(p.conn, "Vật phẩm đã khóa");
                        return;
                    }
                    hist.tem3 = temp;
                    hist.Flus();
                    p.item.remove(3, id, 1);
                }
                break;
            }
            case 4:
            case 7: {
                hist.tem47 = new Item47();
                hist.tem47.id = id;
                hist.tem47.category = type;
                hist.tem47.quantity = (short) p.item.total_item_by_id(type, id);
                hist.Flus();
                p.item.remove(type, id, p.item.total_item_by_id(type, id));
                break;
            }
        }
    }

    public void send_horse(Player p) throws IOException {
        Message m = new Message(-97);
        m.writer().writeByte(0);
        m.writer().writeByte(p.type_use_horse);
        m.writer().writeShort(p.ID);
        MapService.send_msg_player_inside(this, p, m, true);
        m.cleanup();
        Service.send_char_main_in4(p);
    }

    public synchronized void pick_item(Session conn, Message m2) throws IOException {
        short id = m2.reader().readShort();
        byte type = m2.reader().readByte();

        if (item_map[id] == null) {
            // Item đã không còn, thông báo cho client
            Message m = new Message(20);
            m.writer().writeByte(type);
            m.writer().writeShort(id);
            m.writer().writeShort(conn.p.ID);
            MapService.send_msg_player_inside(this, conn.p, m, true);
            m.cleanup();
            item_map[id] = null;
            //  System.out.println("[PICK DEBUG] item null id=" + id + " player=" + conn.p.ID);
            return;
        }

        //System.out.println("[PICK DEBUG] conn.p=" + conn.p.ID + " isSquire=" + conn.p.isSquire
        //       + " owner=" + (conn.p.owner != null ? conn.p.owner.ID : -1)
        //       + " master_id=" + item_map[id].idmaster
        //       + " item.idmaster=" + item_map[id].idmaster
        //       + " id_item=" + item_map[id].id_item
        //       + " category=" + item_map[id].category);

        // Kiểm tra chết
        if (conn.p.isdie) {
            //    System.out.println("[PICK DEBUG] Cannot pick, player is dead");
            return;
        }

        // Nếu không phải dungeon thì kiểm tra chủ sở hữu
        if (this.map_id != 48 && item_map[id].idmaster != -1
                && conn.p.ID != item_map[id].idmaster
                && (conn.p.owner == null || conn.p.owner.ID != item_map[id].idmaster)) {
            Service.send_notice_nobox_white(conn, "Vật phẩm của người khác");
            return;
        }

        type = item_map[id].category;

        // Nhặt vàng
        if (type == 4 && item_map[id].id_item == -1) { // vàng
            if (conn.p.in4_auto[5] == 0) {
                conn.p.update_vang(item_map[id].quantity, "");
                //  System.out.println("[PICK DEBUG] Picked vàng quantity=" + item_map[id].quantity);
                Message m = new Message(20);
                m.writer().writeByte(type);
                m.writer().writeShort(id);
                m.writer().writeShort(conn.p.ID);
                MapService.send_msg_player_inside(this, conn.p, m, true);
                m.cleanup();
                item_map[id] = null;
            }
            return;
        }

        // Nhặt đồ thường
        if (item_map[id].id_item != -1) {
            boolean realPick = false;
            if (type == 3 && conn.p.isDropItemColor4 && item_map[id].color != 4) {
                return; // bỏ qua nếu không phải màu cam
            }
            switch (type) {
                case 3: { // Item3
                    if (item_map[id].id_item < ItemTemplate3.item.size() && conn.p.item.get_inventory_able() > 0) {
                        Short idadd = item_map[id].id_item;
                        Item3 itbag = new Item3();
                        itbag.id = idadd;
                        itbag.name = ItemTemplate3.item.get(idadd).getName();
                        itbag.clazz = ItemTemplate3.item.get(idadd).getClazz();
                        itbag.type = ItemTemplate3.item.get(idadd).getType();
                        itbag.level = ItemTemplate3.item.get(idadd).getLevel();
                        itbag.icon = ItemTemplate3.item.get(idadd).getIcon();
                        itbag.op = new ArrayList<>();
                        itbag.op.addAll(item_map[id].op);
                        itbag.color = item_map[id].color;
                        itbag.part = ItemTemplate3.item.get(idadd).getPart();
                        itbag.tier = 0;
                        itbag.islock = false;
                        itbag.time_use = 0;
                        conn.p.item.add_item_inventory3(itbag);
                        realPick = true;
                    }
                    break;
                }
                case 4: { // Item4
                    if (item_map[id].id_item < ItemTemplate4.item.size()) {
                        Short idadd = item_map[id].id_item;
                        Item47 itbag = new Item47();
                        itbag.id = idadd;
                        itbag.quantity = (short) item_map[id].quantity;
                        itbag.category = 4;
                        conn.p.item.add_item_inventory47(4, itbag);
                        realPick = true;
                    }
                    break;
                }
                case 7: { // Item7
                    if (item_map[id].id_item < ItemTemplate7.item.size()) {
                        Short idadd = item_map[id].id_item;
                        Item47 itbag = new Item47();
                        itbag.id = idadd;
                        itbag.quantity = (short) item_map[id].quantity;
                        itbag.category = 7;
                        conn.p.item.add_item_inventory47(7, itbag);
                        realPick = true;
                    }
                    break;
                }
            }

            if (realPick) {
                Message m = new Message(20);
                m.writer().writeByte(type);
                m.writer().writeShort(id);
                m.writer().writeShort(conn.p.ID);
                MapService.send_msg_player_inside(this, conn.p, m, true);
                m.cleanup();
                //   System.out.println("[PICK DEBUG] Picked " + (type == 3 ? "Item3" : type == 4 ? "Item4" : "Item7")
                //          + " id=" + item_map[id].id_item + " into inventory" + type);
                item_map[id] = null;
            } else {
                //   System.out.println("[PICK DEBUG] Cannot pick regular item id=" + item_map[id].id_item);
            }
        }
    }


    public int get_item_map_index_able() {
        for (int i = 0; i < item_map.length; i++) {
            if (item_map[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public void create_party(Session conn, Message m2) throws IOException {
        byte type = m2.reader().readByte();
        String name = "";
        Player p0 = null;
        if (type != 0 && type != 5 && type != 4) {
            name = m2.reader().readUTF();
            p0 = Map.get_player_by_name(name);
        }
        switch (type) {
            case 1: { // request party other
                if (p0 == null) {
                    Service.send_notice_box(conn, "Có lỗi xảy ra, hãy thử lại");
                    return;
                }
                if (p0.party != null) {
                    if (conn.p.party != null && conn.p.party.get_mems().contains(p0)) {
                        Service.send_notice_box(conn, "Đối phương đã ở trong đội");
                    } else {
                        Service.send_notice_box(conn, "Đối phương đang trong đội khác");
                    }
                    return;
                }
                if (conn.p.party != null) {
                    if (conn.p.party.get_mems().get(0).ID != conn.p.ID) {
                        Service.send_notice_box(conn, "Bạn éo phải đội trưởng, đừng có ra dẻ!!!");
                        return;
                    }
                    if (conn.p.party.get_mems().size() > 4) {
                        Service.send_notice_box(conn, "không thể rủ rê thêm thành viên");
                        return;
                    }
                }
                if (conn.p.party == null) {
                    conn.p.party = new Party();
                    conn.p.party.add_mems(conn.p);
                    conn.p.party.sendin4();
                }
                //
                Message m = new Message(48);
                m.writer().writeByte(type);
                m.writer().writeUTF(conn.p.name);
                p0.conn.addmsg(m);
                m.cleanup();
                break;
            }
            case 2: { // accept
                if (conn.p.party != null) {
                    Service.send_notice_box(conn, "Bạn đã ở trong nhóm");
                    return;
                }
                if (p0 == null || (p0 != null && p0.party == null)) {
                    Service.send_notice_box(conn, "Nhóm không còn tồn tại");
                    return;
                }
                if (p0.party.get_mems().size() > 4) {
                    Service.send_notice_box(conn, "Nhóm đầy");
                    return;
                } else {
                    conn.p.party = p0.party;
                    p0.party.add_mems(conn.p);
                    p0.party.sendin4();
                    p0.party.send_txt_notice(conn.p.name + " vào nhóm");
                }
                break;
            }
            case 3: { // kick
                if (conn.p.party == null) {
                    Service.send_notice_box(conn, "Nhóm không tồn tại");
                    return;
                }
                Player p01 = null;
                for (int i = 0; i < conn.p.party.get_mems().size(); i++) {
                    if (conn.p.party.get_mems().get(i).name.equals(name)) {
                        p01 = conn.p.party.get_mems().get(i);
                        break;
                    }
                }
                if (p01 == null || name.equals("")) {
                    Service.send_notice_box(conn, "Có lỗi xảy ra, hãy thử lại");
                }
                p01.party.remove_mems(p01);
                p01.party.sendin4();
                p01.party = null;
                conn.p.party.send_txt_notice(p01.name + " đã bị đá khỏi đội");
                Service.send_notice_nobox_white(p01.conn, "Bạn đã bị đá khỏi đội ehehe");
                Message m22 = new Message(48);
                m22.writer().writeByte(5);
                p01.conn.addmsg(m22);
                m22.cleanup();
                break;
            }
            case 4: { // giai tan
                Message m = new Message(48);
                m.writer().writeByte(4);
                for (int i = 1; i < conn.p.party.get_mems().size(); i++) {
                    Player p02 = conn.p.party.get_mems().get(i);
                    p02.conn.addmsg(m);
                    p02.party = null;
                }
                conn.addmsg(m);
                conn.p.party.get_mems().clear();
                conn.p.party = null;
                m.cleanup();
                break;
            }
            case 5: { // leave
                if (conn.p.party.get_mems().get(0).ID == conn.p.ID) {
                    Service.send_notice_box(conn, "Là đội trưởng thì phải ra dáng, không đc bỏ nhóm!");
                    return;
                }
                conn.p.party.remove_mems(conn.p);
                conn.p.party.sendin4();
                conn.p.party.send_txt_notice(conn.p.name + " rời nhóm");
                conn.p.party = null;
                //
                Message m = new Message(48);
                m.writer().writeByte(5);
                conn.addmsg(m);
                m.cleanup();
                break;
            }
        }
    }

    public static Player get_player_by_id(int id_player_login) {
        for (Map[] maps : entrys) {
            for (Map map : maps) {
                for (Player p0 : map.players) {
                    if (p0.ID == id_player_login) {
                        return p0;
                    }
                }

            }
        }
        return null;
    }

    public static boolean is_map_cant_save_site(short id) {
        return id == 48 || id == 88 || id == 89 || id == 90 || id == 91 || id == 82 || id == 102 || id == 100 || (id >= 83 && id <= 87) || (id >= 53 && id <= 61)
                || Map.is_map_chien_truong(id) || id == 125 || id == 127 || id == 129 || id == 132 || id == 135;
    }

    public static boolean is_map_not_zone2(short id) {
        return id == 48 || id == 88 || id == 89 || id == 90 || id == 91 || id == 82 || id == 102 || id == 100
                || (id >= 83 && id <= 87) || (id >= 53 && id <= 61) || Map.is_map_chien_truong(id) || id == 1 || id == 10
                || id == 14 || id == 18 || id == 28 || id == 32 || id == 33 || id == 34 || id == 35 || id == 36 || id == 67
                || id == 68 || id == 69 || id == 70 || id == 93;
    }

    public synchronized void add_item_map_leave(Map map, Player p_master, ItemMap temp, int mob_index)
            throws IOException {
        for (int i = 0; i < item_map.length; i++) {
            if (item_map[i] == null) {
                item_map[i] = temp;
                Message mi = new Message(19);
                mi.writer().writeByte(temp.category);
                mi.writer().writeShort(mob_index); // index mob die
                switch (temp.category) {
                    case 3: {
                        mi.writer().writeShort(ItemTemplate3.item.get(temp.id_item).getIcon());
                        mi.writer().writeShort(i); //
                        mi.writer().writeUTF(ItemTemplate3.item.get(temp.id_item).getName());
                        break;
                    }
                    case 4: {
                        mi.writer().writeShort(ItemTemplate4.item.get(temp.id_item).getIcon());
                        mi.writer().writeShort(i); //
                        mi.writer().writeUTF(ItemTemplate4.item.get(temp.id_item).getName());
                        break;
                    }
                    case 7: {
                        mi.writer().writeShort(ItemTemplate7.item.get(temp.id_item).getIcon());
                        mi.writer().writeShort(i); //
                        mi.writer().writeUTF(ItemTemplate7.item.get(temp.id_item).getName());
                        break;
                    }
                }
                mi.writer().writeByte(0); // color
                mi.writer().writeShort(-1); // id player
                MapService.send_msg_player_inside(map, p_master, mi, true);
                mi.cleanup();
                break;
            }
        }
    }

    public static boolean is_map_chiem_mo(Map map, boolean is_zone) {
        boolean is_map = false;
        int[] map_ = new int[]{3, 5, 8, 9, 11, 12, 15, 16, 19, 21, 22, 24, 26, 27, 37, 42};
        for (int i = 0; i < map_.length; i++) {
            if (map_[i] == map.map_id) {
                is_map = true;
                break;
            }
        }
        return (is_zone) ? (map.zone_id == 4 && is_map) : is_map;
    }


    public static boolean is_map__load_board_player(short id) {
        return id == 102;
    }

    public static boolean is_map_chien_truong(short id) {
        return id >= 53 && id <= 61;
    }

    public boolean isMapChienTruong() {
        return map_id >= 53 && map_id <= 61;
    }

    public boolean is_map_buon() {
        return map_id == 8 || map_id == 7 || (map_id >= 15 && map_id <= 18) || (map_id >= 20 && map_id <= 25) || map_id == 33
                || map_id == 34 || (map_id >= 37 && map_id <= 39) || (map_id >= 42 && map_id <= 45) || map_id == 52;
    }

    public boolean isMapMaze() {
        return map_id >= 105 && map_id <= 115;
    }

    public NpcMap find_npc_in_map(short id) {
        for (NpcMap npcMap : npcMaps) {
            if (id == npcMap.npcTemplate.id) {
                return npcMap;
            }
        }
        return null;
    }

}
