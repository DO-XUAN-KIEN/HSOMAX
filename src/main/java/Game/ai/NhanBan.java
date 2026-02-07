package Game.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Game.client.Clan;
import Game.core.Log;
import Game.core.Manager;
import Game.core.Util;
import Game.map.MapService;
import Game.template.*;
import Game.client.Player;
import Game.core.Service;
import Game.io.Message;
import Game.map.Map;
import org.json.simple.JSONArray;

public class NhanBan extends MainObject {
    public Clan ownerClan;
    public List<Part_player> part_p;//
    private byte head;//
    private byte eye;//
    private byte hair;//
    private int pointpk;//
    private short clan_icon = -1;
    private int clan_id = -1;
    private String clan_name_clan_shorted;
    private byte clan_mem_type;
    private short[] fashion;//
    private short mat_na;//
    private short phi_phong;//
    private short danh_hieu;
    private short weapon;//
    private short id_horse;//
    private short id_hair;//
    private short id_wing;//
    private short id_img_mob = -1;//
    private byte type_use_horse;//
    public long act_time;
    public boolean is_move;//
    public Player p_target;
    public int p_skill_id;//

    public long timeATK;
    public long time_hp_buff;
    private int pierce;
    private int crit;
    public int count_update = 1;

    public NhanBan() {
    }

    public NhanBan(JSONArray jar) {
        map_id = ((Long) jar.get(0)).byteValue();
        ID = ((Long) jar.get(1)).intValue();
        x = ((Long) jar.get(2)).shortValue();
        y = ((Long) jar.get(3)).shortValue();
        name = (String) jar.get(4);
        part_p = new ArrayList<>();
        JSONArray jar2 = (JSONArray) jar.get(5);
        if (jar2 != null && !jar2.isEmpty()) {
            for (Object o : jar2) {
                JSONArray jar3 = (JSONArray) o;
                if (jar3 == null || jar3.isEmpty()) {
                    continue;
                }
                Part_player pr = new Part_player();
                pr.type = ((Long) jar.get(0)).byteValue();
                pr.part = ((Long) jar.get(1)).byteValue();
                part_p.add(pr);
                jar3.clear();
            }
            jar2.clear();
        }

        clazz = ((Long) jar.get(6)).byteValue();
        head = ((Long) jar.get(7)).byteValue();
        eye = ((Long) jar.get(8)).byteValue();
        hair = ((Long) jar.get(9)).byteValue();
        level = ((Long) jar.get(10)).shortValue();
        hp = ((Long) jar.get(11)).intValue();
        hp_max = ((Long) jar.get(12)).intValue();
        pointpk = ((Long) jar.get(13)).shortValue();
        clan_icon = ((Long) jar.get(14)).shortValue();
        clan_id = ((Long) jar.get(15)).intValue();
        clan_name_clan_shorted = (String) jar.get(16);
        clan_mem_type = ((Long) jar.get(17)).byteValue();
        jar2 = (JSONArray) jar.get(18);
        if (jar2 != null) {
            fashion = new short[jar2.size()];
            for (int i = 0; i < jar2.size(); i++) {
                fashion[i] = ((Long) jar2.get(i)).byteValue();
            }
        }
        mat_na = ((Long) jar.get(19)).shortValue();
        phi_phong = ((Long) jar.get(20)).shortValue();
        weapon = ((Long) jar.get(21)).shortValue();
        id_horse = ((Long) jar.get(22)).shortValue();
        id_hair = ((Long) jar.get(23)).shortValue();
        id_wing = ((Long) jar.get(24)).shortValue();
        danh_hieu = ((Long) jar.get(25)).shortValue();
        type_use_horse = ((Long) jar.get(26)).byteValue();
        dame = ((Long) jar.get(27)).intValue();
        act_time = (Long) jar.get(28);
        is_move = jar.get(29).equals("true");
        p_skill_id = ((Long) jar.get(30)).intValue();
        crit = ((Long) jar.get(31)).intValue();
        time_hp_buff = (Long) jar.get(32);
        def = ((Long) jar.get(33)).intValue();
        pierce = ((Long) jar.get(34)).intValue();
        typepk = 0;
    }

    public NhanBan(NhanBan nhanban) {
        map_id = nhanban.map_id;
        ID = Short.toUnsignedInt((short) Manager.gI().get_index_mob_new());
        x = nhanban.x;
        y = nhanban.y;
        name = nhanban.name;
        part_p = nhanban.part_p;
        clazz = nhanban.clazz;
        head = nhanban.head;
        eye = nhanban.eye;
        hair = nhanban.hair;
        level = nhanban.level;
        hp = nhanban.hp;
        if (nhanban.hp_max >= 2_000_000_000){
            nhanban.hp_max = 2_000_000_000;
        }else {
            hp_max = nhanban.hp_max;
        }
        this.ownerClan = nhanban.ownerClan;
        pointpk = nhanban.pointpk;
        clan_icon = nhanban.clan_icon;
        clan_id = nhanban.clan_id;
        clan_name_clan_shorted = nhanban.clan_name_clan_shorted;
        clan_mem_type = nhanban.clan_mem_type;
        fashion = nhanban.fashion;
        mat_na = nhanban.mat_na;
        phi_phong = nhanban.phi_phong;
        weapon = nhanban.weapon;
        id_horse = nhanban.id_horse;
        id_hair = nhanban.id_hair;
        id_wing = nhanban.id_wing;
        danh_hieu = nhanban.danh_hieu;
        type_use_horse = nhanban.type_use_horse;
        if(nhanban.get_DameBase() >= 1_500_000_000) {
            dame = 1_500_000_000;
        }else {
            dame = nhanban.get_DameBase() + 50000;
        }
        act_time = nhanban.act_time;
        is_move = nhanban.is_move;
        p_skill_id = nhanban.p_skill_id;
        crit = nhanban.crit;
        time_hp_buff = nhanban.time_hp_buff;
        def = nhanban.def;
        pierce = nhanban.pierce;
        typepk = 0;
    }

    public JSONArray GetData() {
        JSONArray jar = new JSONArray();
        try {
            JSONArray jar2 = new JSONArray();
            jar.add(map_id);
            jar.add(ID);
            jar.add(x);
            jar.add(y);
            jar.add(name);
            if (part_p != null && !part_p.isEmpty()) {
                JSONArray jar3 = new JSONArray();
                for (Part_player pr : part_p) {
                    jar3.add(pr.type);
                    jar3.add(pr.part);
                    jar2.add(jar3);
                }
            }
            jar.add(jar2);
            //jar2.clear();
            jar.add(clazz);
            jar.add(head);
            jar.add(eye);
            jar.add(hair);
            jar.add(level);
            jar.add(hp);
            jar.add(hp_max);
            jar.add(pointpk);
            jar.add(clan_icon);
            jar.add(clan_id);
            jar.add(clan_name_clan_shorted);
            jar.add(clan_mem_type);
            JSONArray jar4 = new JSONArray();
            if (fashion != null) {
                for (short b : fashion) {
                    jar4.add(b);
                }
            }
            jar.add(jar4);
            //jar2.clear();
            jar.add(mat_na);
            jar.add(phi_phong);
            jar.add(weapon);
            jar.add(id_horse);
            jar.add(id_hair);
            jar.add(id_wing);
            jar.add(danh_hieu);
            jar.add(type_use_horse);
            jar.add(dame);
            jar.add(act_time);
            jar.add(is_move);

            jar.add(p_skill_id);
            jar.add(crit);
            jar.add(time_hp_buff);
            jar.add(def);
            jar.add(pierce);
        } catch (Exception e) {
            jar.clear();
            e.printStackTrace();
            Log.gI().add_Log_Server("ChiemMo", "Save NhanBan: " + e.getMessage());
        }

        return jar;
        //
    }

    public void setup(Player p0) {
        this.ID = Short.toUnsignedInt((short) Manager.gI().get_index_mob_new());
        this.x = p0.x;
        this.y = p0.y;
        this.part_p = new ArrayList<>();
        for (int i = 0; i < p0.item.wear.length; i++) {
            Part_player temp_add = new Part_player();
            if (i != 0 && i != 1 && i != 6 && i != 7 && i != 10) {
                continue;
            }
            Item3 temp = p0.item.wear[i];
            if (temp != null) {
                temp_add.type = temp.type;
                if (i == 10 && p0.item.wear[14] != null && (p0.item.wear[14].id >= 4638 && p0.item.wear[14].id <= 4648)) {
                    temp_add.part = p0.item.wear[14].part;
                } else {
                    temp_add.part = temp.part;
                }
            }
            this.part_p.add(temp_add);
        }
        this.name = "Nhân bản - " + p0.name;
        this.clazz = p0.clazz;
        this.head = p0.head;
        this.eye = p0.eye;
        this.hair = p0.hair;
        this.level = p0.level;
        this.hp = p0.hp;
        if (p0.body.get_HpMax() >= 2_000_000_000){
            this.hp_max = 2_000_000_000;
        }else {
            this.hp_max = p0.body.get_HpMax();
        }
        this.hieuchien = p0.hieuchien;
        this.clan_icon = p0.myclan.icon;
        this.clan_id = Clan.get_id_clan(p0.myclan);
        this.clan_name_clan_shorted = p0.myclan.name_clan_shorted;
        this.clan_mem_type = p0.myclan.get_mem_type(p0.name);
        this.fashion = p0.fashion;
        this.mat_na = Service.get_id_mat_na(p0);
        this.phi_phong = Service.get_id_phiphong(p0);
        this.weapon = Service.get_id_weapon(p0);
        this.id_horse = p0.id_horse;
        this.id_hair = Service.get_id_hair(p0);
        this.id_wing = Service.get_id_wing(p0);
        this.danh_hieu = Service.get_id_danhhieu(p0);
        this.type_use_horse = p0.type_use_horse;
        this.dame = (p0.body.get_DameProp(0) + p0.body.get_DameProp(1) + p0.body.get_DameProp(2)
                + p0.body.get_DameProp(3) + p0.body.get_DameProp(4)) / 2;
        this.map_id = p0.map.map_id;
        this.crit = p0.body.getCrit();
        this.def = p0.body.get_DefBase();
        this.pierce = p0.body.getPierce();
        if (this.pierce > 5000) {
            this.pierce = 5000;
        }
        this.is_move = true;
    }

    public void update_info_other_inside(Map map) throws IOException {
        for (int i = 0; i < map.players.size(); i++) {
            Player p0 = map.players.get(i);
            if (p0.ID != this.ID) {
                this.send_in4(p0);
            }
        }
    }

    public void send_in4(Player p) throws IOException {
        Message m = new Message(5);
        m.writer().writeShort(this.ID);
        m.writer().writeUTF(this.name);
        m.writer().writeShort(this.x);
        m.writer().writeShort(this.y);
        m.writer().writeByte(this.clazz);
        m.writer().writeByte(-1);
        m.writer().writeByte(this.head);
        m.writer().writeByte(this.eye);
        m.writer().writeByte(this.hair);
        m.writer().writeShort(this.level);
        m.writer().writeInt(this.hp);
        m.writer().writeInt(this.hp_max);
        m.writer().writeByte(this.typepk); // type pk
        m.writer().writeShort(this.hieuchien);
        m.writer().writeByte(this.part_p.size());
        //
        for (int i = 0; i < this.part_p.size(); i++) {
            m.writer().writeByte(this.part_p.get(i).type);
            m.writer().writeByte(this.part_p.get(i).part);
            m.writer().writeByte(3);
            m.writer().writeShort(-1);
            m.writer().writeShort(-1);
            m.writer().writeShort(-1);
            m.writer().writeShort(-1); // eff
        }
        //
        m.writer().writeShort(this.clan_icon);
        if (clan_icon > -1) {
            m.writer().writeInt(this.clan_id);
            m.writer().writeUTF(this.clan_name_clan_shorted);
            m.writer().writeByte(this.clan_mem_type);
        }
        m.writer().writeByte(-1); // pet
        m.writer().writeByte(this.fashion.length);
        for (int i = 0; i < this.fashion.length; i++) {
            if (p.conn.version >= 280) {
                m.writer().writeShort(this.fashion[i]);
            } else {
                m.writer().writeByte(this.fashion[i]);
            }
        }
        //
        m.writer().writeShort(id_img_mob);//id_img_mob
        m.writer().writeByte(this.type_use_horse);
        m.writer().writeBoolean(false);
        m.writer().writeByte(1);
        m.writer().writeByte(0);
        m.writer().writeShort(this.mat_na); // mat na
        m.writer().writeByte(1); // paint mat na trc sau
        m.writer().writeShort(this.phi_phong); // phi phong
        m.writer().writeShort(this.weapon); // weapon
        m.writer().writeShort(this.id_horse);
        m.writer().writeShort(this.id_hair); // hair
        m.writer().writeShort(this.id_wing); // wing
        m.writer().writeShort(-1); // body
        m.writer().writeShort(-1); // leg
        m.writer().writeShort(-1); // bienhinh
        p.conn.addmsg(m);
        m.cleanup();
    }

    @Override
    public int get_TypeObj() {
        return 0;
    }

    @Override
    public int get_DefBase() {
        return def;
    }

    @Override
    public void SetDie(Map map, MainObject mainAtk) {
        if (isdie) {
            return;
        }
        try {
            if (this.hp <= 0) {
                this.hp = 0;
                Mob_MoTaiNguyen temp_mob = Manager.gI().chiem_mo.get_mob_in_map(map);
                if (temp_mob != null) {
                    temp_mob.nhanBans.remove(this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Map map) {
        try {
            // 1. Hồi phục HP (Giữ nguyên)
            if (this.time_hp_buff < System.currentTimeMillis()) {
                this.time_hp_buff = System.currentTimeMillis() + 2500L;
                if (this.hp < this.get_HpMax()) {
                    int par = this.get_HpMax() / 20;
                    this.hp += par;
                    if (this.hp > this.get_HpMax()) this.hp = this.get_HpMax();
                    Message m_hp = new Message(32);
                    m_hp.writer().writeByte(0);
                    m_hp.writer().writeShort(this.ID);
                    m_hp.writer().writeShort(-1);
                    m_hp.writer().writeByte(0);
                    m_hp.writer().writeInt(this.get_HpMax());
                    m_hp.writer().writeInt(this.hp);
                    m_hp.writer().writeInt(par);
                    for (Player p : map.players) p.conn.addmsg(m_hp);
                    m_hp.cleanup();
                }
            }

            // 2. Logic Tự động đánh
            Mob_MoTaiNguyen mobtainguyen = Manager.gI().chiem_mo.get_mob_in_map(map);
            // Chỉ đánh khi Mỏ ở trạng thái Tấn công (is_atk = true) hoặc Mỏ chưa có chủ
            boolean canAttack = (mobtainguyen != null && (mobtainguyen.is_atk || mobtainguyen.clan == null));

            if (canAttack && this.act_time < System.currentTimeMillis()) {
                this.act_time = System.currentTimeMillis() + 1000L; // Tốc độ đánh 1s

                // Tìm danh sách kẻ thù trong tầm
                List<Player> enemies = new ArrayList<>();
                for (Player p0 : map.players) {
                    if (p0.isdie || p0.get_EffMe_Kham(StrucEff.TangHinh) != null) continue;

                    boolean isEnemy = false;
                    if (mobtainguyen != null && mobtainguyen.clan != null) {
                        // Là địch nếu khác Clan
                        if (p0.myclan == null || p0.myclan.ID != mobtainguyen.clan.ID) {
                            isEnemy = true;
                        }
                    } else {
                        // Mỏ vô chủ -> Đánh tất cả
                        isEnemy = true;
                    }

                    if (isEnemy && Math.abs(p0.x - this.x) < 300 && Math.abs(p0.y - this.y) < 300) {
                        enemies.add(p0);
                    }
                }

                if (!enemies.isEmpty()) {
                    this.is_move = false;
                    // Đánh tất cả kẻ thù trong tầm (Đánh lan)
                    for (Player pTarget : enemies) {
                        int[] skills = {0, 1, 2, 3, 4, 5, 6, 7, 8, 17, 19, 20};
                        int skillId = skills[Util.random(skills.length)];
                        // Gọi hàm đánh từ MainObject
                        MainObject.MainAttack(map, this, pTarget, skillId, null, 2);
                    }
                } else {
                    this.is_move = true;
                }
            }

            // 3. Logic đi tuần (Chỉ chạy khi KHÔNG đánh)
            if (this.is_move && this.act_time < System.currentTimeMillis()) {
                this.act_time = System.currentTimeMillis() + 3000L;

                // Random tọa độ đi tuần quanh vị trí cũ một chút
                this.x = (short) (this.x + Util.random(-50, 50));
                this.y = (short) (this.y + Util.random(-50, 50));

                // Giới hạn không đi quá xa (có thể thêm logic check map bound nếu cần)

                // Gửi gói tin di chuyển
                Message m12 = new Message(4);
                m12.writer().writeByte(0);
                m12.writer().writeShort(0);
                m12.writer().writeShort(this.ID);
                m12.writer().writeShort(this.x);
                m12.writer().writeShort(this.y);
                m12.writer().writeByte(-1);
                MapService.send_msg_player_inside(map, this, m12, true);
                m12.cleanup();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void setHp(MainObject atk, int dame) {
        super.setHp(atk, dame);
        if (this.hp > 0 && atk != null && atk.isPlayer()) {
            Player p_atk = (Player) atk;
            if (this.p_target == null || this.p_target.isdie || this.p_target.ID != p_atk.ID) {
                this.p_target = p_atk;
                this.is_move = false;
                this.act_time = 0;
            }
        }
    }
}
