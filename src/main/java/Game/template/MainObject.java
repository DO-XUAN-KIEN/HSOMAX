package Game.template;

import Game.core.Manager;
import Game.core.Util;
import Game.activities.ChiemThanhManager;
import Game.map.*;
import Game.client.Pet;
import Game.client.Player;
import Game.core.Service;
import Game.activities.ChienTruong;
import Game.io.Message;
import Game.template.Eff_TextFire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainObject {

    public String name;
    public int hp, mp;
    public int hp_max;
    protected int mp_max;
    public boolean isdie, ishs = true, isATK = true;
    public int ID;
    public short x, x_old, y, y_old;
    public short level;
    public short map_id;
    public byte zone_id;
    public int dame, def;
    public boolean isExp = true;
    public byte color_name;
    public byte typepk;
    public Mob template;
    public long exp;
    public byte clazz;
    public Kham_template kham;
    public int hieuchien;
    public int buff_hp; // thêm biến này


    public int Set_hpMax(int hp_max) {
        return this.hp_max = hp_max;
    }

    public int Set_Dame(int dame) {
        return this.dame = dame;
    }

    public List<EffTemplate> MainEff;
    protected List<EffTemplate> Eff_me_kham;
    protected List<EffTemplate> Eff_Tinh_Tu;

    public void updateEff() {
        try {
            if (MainEff != null) {
                synchronized (MainEff) {
                    for (int i = MainEff.size() - 1; i >= 0; i--) {
                        EffTemplate temp = MainEff.get(i);
                        if (temp.time <= System.currentTimeMillis()) {
                            MainEff.remove(i);
                            if (isPlayer()) {
                                if (temp.id == -125) {
                                    ((Player) this).set_x2_xp(0);
                                }
                                if (temp.id == 24 || temp.id == 23 || temp.id == 0
                                        || temp.id == 2 || temp.id == 3
                                        || temp.id == 4 || temp.id == StrucEff.NOI_TAI_BANG) {
                                    if (temp.id == 2 && isPlayer()) {
                                        this.hp += ((Player) this).hp_restore;
                                    }
                                    Service.send_char_main_in4((Player) this);
                                    for (int j = 0; j < ((Player) this).map.players.size(); j++) {
                                        Player p2 = ((Player) this).map.players.get(j);
                                        if (p2 != null && p2.ID != this.ID) {
                                            MapService.send_in4_other_char(((Player) this).map, p2, (Player) this);
                                        }
                                    }
                                }
                            } else if (isMob()) {
                                if (temp.id == StrucEff.NOI_TAI_BANG) {
                                    for (int j = 0; j < ((Mob_in_map) this).map.players.size(); j++) {
                                        Player p2 = ((Mob_in_map) this).map.players.get(j);
                                        if (p2 != null && p2.ID != this.ID) {
                                            Service.mob_in4(p2, this.ID);
                                        }
                                    }
                                }
                            }
                            continue;
                        }
                        //
                        if (temp.id == 1 && !this.isdie && isPlayer()) {
                            if (((Player) this).time_affect_special_sk < System.currentTimeMillis() && ((Player) this).dame_affect_special_sk > 0) {
                                ((Player) this).time_affect_special_sk = System.currentTimeMillis() + 1000L;
                                this.hp -= ((Player) this).dame_affect_special_sk;
                                Service.usepotion(((Player) this), 0, -((Player) this).dame_affect_special_sk);
                                if (this.hp < 0) {
                                    MapService.Player_Die(((Player) this).map, ((Player) this), ((Player) this), true);
                                }
                            }
                        }

                    }
                }
            }
            if (Eff_me_kham != null) {
                synchronized (Eff_me_kham) {
                    for (int i = Eff_me_kham.size() - 1; i >= 0; i--) {
                        EffTemplate temp = Eff_me_kham.get(i);
                        if (temp.time <= System.currentTimeMillis()) {
                            Eff_me_kham.remove(i);
                        }
                    }
                }
            }
            if (Eff_Tinh_Tu != null) {
                synchronized (Eff_Tinh_Tu) {
                    for (int i = Eff_Tinh_Tu.size() - 1; i >= 0; i--) {
                        EffTemplate temp = Eff_Tinh_Tu.get(i);
                        if (temp.time <= System.currentTimeMillis()) {
                            Eff_Tinh_Tu.remove(i);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public void add_EffDefault(int id, int param1, long time_end) {
        if (MainEff == null) {
            return;
        }
        synchronized (MainEff) {
            if (param1 == 0) {
                return;
            }
            for (int i = MainEff.size() - 1; i >= 0; i--) {
                EffTemplate temp_test = MainEff.get(i);
                if (temp_test != null && temp_test.id == id) {
                    MainEff.remove(i);
                }
            }
            MainEff.add(new EffTemplate(id, param1, time_end));
        }
    }

    public void add_EffMe_Kham(int id, int param1, long time_end) {
        if (Eff_me_kham == null) {
            return;
        }
        synchronized (Eff_me_kham) {
            Eff_me_kham.add(new EffTemplate(id, param1, time_end));
        }
    }

    public void addEffTinhTu(int id, int param1, long time_end) {
        synchronized (Eff_Tinh_Tu) {
            Eff_Tinh_Tu.add(new EffTemplate(id, param1, System.currentTimeMillis() + time_end));
        }
    }

    public void removeEffTinhTu(EffTemplate eff) {
        synchronized (Eff_Tinh_Tu) {
            Eff_Tinh_Tu.remove(eff);
        }
    }

    public EffTemplate get_EffDefault(int id) {
        if (MainEff == null) {
            return null;
        }
        long time = System.currentTimeMillis();
        synchronized (MainEff) {
            for (EffTemplate e : MainEff) {
                if (e.id == id && e.time > time) {
                    return e;
                }
            }
        }
        return null;
    }

    public EffTemplate get_EffMe_Kham(int id) {
        if (Eff_me_kham == null) {
            return null;
        }
        long time = System.currentTimeMillis();
        synchronized (Eff_me_kham) {
            for (EffTemplate e : Eff_me_kham) {
                if (e.id == id && e.time > time) {
                    return e;
                }
            }
        }
        return null;
    }

    public EffTemplate getEffTinhTu(int id) {
        long time = System.currentTimeMillis();

        if (Eff_Tinh_Tu == null) {
            return null; // hoặc xử lý khác nếu cần
        }

        synchronized (Eff_Tinh_Tu) {
            for (EffTemplate e : Eff_Tinh_Tu) {
                if (e.id == id && e.time > time) {
                    return e;
                }
            }
        }

        return null;
    }


    public static void sendEffTinhTu(Player p, int id, int time) throws IOException {
        Message m = new Message(-49);
        m.writer().writeByte(2);
        m.writer().writeShort(0);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        m.writer().writeByte(id);
        m.writer().writeShort(p.ID);
        m.writer().writeByte(0);
        m.writer().writeByte(0);
        m.writer().writeInt(time);
        MapService.send_msg_player_inside(p.map, p, m, true);
        m.cleanup();
    }

    public boolean isStunes(boolean isAtk) {
        if (MainEff == null) {
            return false;
        }
        long time = System.currentTimeMillis();
        synchronized (MainEff) {
            for (EffTemplate e : MainEff) {
                if (e.id >= -124 && e.id <= -121 && (!isAtk || e.id != -123) && e.time > time) {
                    return true;
                }
            }
        }
        return false;
    }

    public int get_TypeObj() {
        return 1;
    }

    public int get_HpMax() {
        return hp_max;
    }

    public int get_MpMax() {
        return mp_max;
    }

    public int get_DameBase() {
        return dame;
    }

    public int get_DameProp(int type) {
        return 0;
    }

    public int get_PercentDameProp(int type) {
        return 0;
    }

    public int get_DefBase() {
        return def;
    }

    public int get_PercentDefBase() {
        return 0;
    }

    public int get_PercentDefProp(int type) {
        return 0;
    }

    public boolean isMob() {
        return false;
    }

    public boolean isMoTaiNguyen() {
        return false;
    }

    public boolean isMobDungeon() {
        return false;
    }

    public boolean isMobDiBuon() {
        return false;
    }

    public boolean isPlayer() {
        return false;
    }

    public boolean isNhanBan() {
        return false;
    }

    public boolean isMobCTruong() {
        return false;
    }

    public boolean isHouse() {
        return false;
    }

    public boolean isBoss() {
        return false;
    }

    public int getPierce() {//xuyên giáp
        return 0;
    }

    public int getCrit() {
        return 0;
    }

    public int get_PhanDame() {
        return 0;
    }

    public int get_Miss(boolean giam_ne) {
        return 0;
    }

    public boolean isBot() {
        return false;
    }

    public void Obj_Fire(Map map, MainObject objFocus, int skill, LvSkill temp) throws IOException {
        // không có thì đưa vào null;
    }

    public void SetDie(Map map, MainObject mainAtk) throws IOException {
        if (isdie) {
            return;
        }
        hp = 0;
        isdie = true;
    }

    public static void MainAttack(Map map, MainObject ObjAtk, MainObject focus, int idxSkill, LvSkill temp, int type) {
        try {
            // pvp, pve, mob chiến trường, mob chiếm thành, nhân bản boss, (không đánh mob đi buôn)
            //<editor-fold defaultstate="collapsed" desc="... không thể tấn công    ...">
            if (ObjAtk == null || focus == null || ObjAtk.equals(focus) || ObjAtk.isdie || ObjAtk.isStunes(true)) {
                return;
            }
            if (ObjAtk.isPlayer() && focus.isPlayer()) {
                Player attacker = (Player) ObjAtk;
                Player target = (Player) focus;


            }
            if (focus.isMobDiBuon() && ObjAtk.isPlayer() && ((Player) ObjAtk).isTrader() && ((Pet_di_buon) focus).type == 131) {
                return;
            }
            if (focus.isMobDiBuon() && ObjAtk.isPlayer() && ((Player) ObjAtk).isRobber() && ((Pet_di_buon) focus).type == 132) {
                return;
            }
            //  if (ObjAtk.isPlayer() && focus.isPlayer() && map.zone_id == 1 && !Map.is_map_not_zone2(map.map_id)) {
            //      return;
            //  }
            if (ObjAtk.isPlayer() && focus.isPlayer() && !map.isMapChiemThanh() && (map.ismaplang || ObjAtk.level < 11 || focus.level < 11
                    || (ObjAtk.typepk != 0 && ObjAtk.typepk == focus.typepk) || ObjAtk.hieuchien > 32_000)) {
                return;
            }
            if (focus.isMob() && focus.template.mob_id == 152 && !ChiemThanhManager.isDameTruChinh(map)) {
                return;
            }
            if (Math.abs(ObjAtk.x - focus.x) > 300 || Math.abs(ObjAtk.y - focus.y) > 300) {
                return;
            }
            if (ObjAtk.isStunes(true)) {
                return;
            }
            if (((focus.isMob() && focus.isBoss() && !focus.template.isBossEvent()) || (focus.isPlayer() && ObjAtk.isBoss() && !ObjAtk.template.isBossEvent()))
                    && Math.abs(focus.level - ObjAtk.level) > 5 && map.map_id != 61) {
                return;
            }
            if (focus.isMob() && focus.isBoss() && focus.template.isBossEvent()) {
                if (map.zone_id == 0 && ObjAtk.level > 89) {
                    return;
                }
                if (map.zone_id == 2 && (ObjAtk.level < 89 || ObjAtk.level > 109)) {
                    return;
                }
                if (map.zone_id == 3 && ObjAtk.level < 110) {
                    return;
                }
            }

            if (ObjAtk.isPlayer() && focus.get_TypeObj() == 1) {
                if (map.isMapChienTruong()) {
                    switch (focus.template.mob_id) {
                        case 89: {
                            if (ObjAtk.typepk == 4) {
                                return;
                            }
                            break;
                        }
                        case 90: {
                            if (ObjAtk.typepk == 2) {
                                return;
                            }
                            break;
                        }
                        case 91: {
                            if (ObjAtk.typepk == 5) {
                                return;
                            }
                            break;
                        }
                        case 92: {
                            if (ObjAtk.typepk == 1) {
                                return;
                            }
                            break;
                        }
                    }
                }
            }
            if (focus.isPlayer() && focus.get_EffMe_Kham(StrucEff.TangHinh) != null) {
                return;
            }
            if (ObjAtk.isPlayer() && ObjAtk.get_EffMe_Kham(StrucEff.LuLan) != null) {
                return;
            }
            if (map.map_id == 102 && map.kingCupMap != null && map.kingCupMap.timeWait > System.currentTimeMillis()) {
                return;
            }
            if (focus.isdie || focus.hp <= 0 && ObjAtk.isPlayer()) {
                if (focus.isPlayer()) {
                    MapService.Player_Die(map, focus, ObjAtk, false);
                } else {
                    MapService.MainObj_Die(map, ((Player) ObjAtk).conn, focus, false);
                }
                return;
            }
            if (ObjAtk.isPlayer() && focus.isPlayer() && focus.typepk == -1)// đồ sát
            {
                if (ObjAtk.hieuchien > 1000) {
                    Service.send_notice_box(((Player) ObjAtk).conn, "Không thể đồ sát quá nhiều, cần tẩy điểm trước.");
                    return;
                }
                if (focus.get_EffDefault(-126) != null) {
                    Service.send_notice_box(((Player) ObjAtk).conn, "Đối phương có hiệu ứng chống pk");
                    return;
                }
                //if (map.zone_id == 1 && !Map.is_map_not_zone2(map.map_id)) {
                //    return;
                // }
                if (((Player) focus).pet_follow_id == 4708) {
                    Service.send_notice_box(((Player) ObjAtk).conn, "Đối phương đang được pet bảo vệ");
                    return;
                }
            }
            Player p = ObjAtk.isPlayer() ? (Player) ObjAtk : null;
            if (focus.isPlayer() && ObjAtk.isPlayer() && focus.getEffTinhTu(EffTemplate.BAT_TU) != null && p != null) {
                Service.send_notice_nobox_white(p.conn, "Đối phương đang trong trạng thái bất tử");
                return;
            }
            if (p != null && p.getEffTinhTu(EffTemplate.MU_MAT) != null) {
                Service.send_notice_nobox_white(p.conn, "Bạn đang bị mù mắt");
                MapService.Fire_Player(map, ((Player) ObjAtk).conn, idxSkill, focus.ID, 0, focus.hp, new ArrayList<>(), new Eff_TextFire(11, 0));
                return;
            }

            // ⚠️ CHỈ áp dụng nếu người đánh là Player và có hiệu ứng/đồ kích hoạt Biến Dạng
            if (ObjAtk instanceof Player) {
                Player attacker = (Player) ObjAtk;

                // Giả sử param 69 là dùng để kích hoạt Biến Dạng (ví dụ đồ pet hay vũ khí cam)
                if (attacker.total_item_param((byte) 69) > 0 && map != null) {
                    for (Player pl : map.players) {
                        if (pl != null && pl.hp > 0 && pl.getEffTinhTu(EffTemplate.BIEN_DANG) == null) {
                            // Áp hiệu ứng mất né 100% trong 10s
                            pl.addEffTinhTu(EffTemplate.BIEN_DANG, -100, 10000L);
                            sendEffTinhTu(pl, EffTemplate.BIEN_DANG, 10000);

                            if (pl.conn != null) {
                                //  Service.send_notice_nobox_white(pl.conn, "💀 Bạn bị Biến Dạng – mất toàn bộ né tránh!");
                            }
                        }
                    }


                }
            }

            if (focus.getEffTinhTu(EffTemplate.BIEN_DANG) != null) {
                // Bỏ qua né
            } else {
                boolean giam_ne = p != null && p.isEffTinhTu(99);
                if (focus.get_Miss(giam_ne) > Util.random(10_000)) {
                    if (ObjAtk.isPlayer()) {
                        MapService.Fire_Player(map, ((Player) ObjAtk).conn, idxSkill, focus.ID, 0, focus.hp, new ArrayList<>(), new Eff_TextFire(11, 0));
                    }
                    return;
                }
            }
            //</editor-fold>

            boolean is_send_skill_110 = false;
            if ((idxSkill == 19 && p.skill_110[0] > 1) || (idxSkill == 20 && p.skill_110[1] > 1)) {
                send_eff_to_object(p, focus, p.get_id_eff_skill(idxSkill));
                is_send_skill_110 = true;
            }
            int temp_skill = idxSkill;
            if (is_send_skill_110) {
                temp_skill = 0;
            }

            EffTemplate ef;
            long dame = ObjAtk.get_DameBase();
            int dame_skill = 0;
            int precent_dame_skill = 0;
            int hutHP = 0;
            float ptCrit = 0;
            float DamePlus = 0;
            int ptXG = ObjAtk.getPierce();
            int ptCM = ObjAtk.getCrit();
            int noitai = -1;
            Eff_TextFire stas = new Eff_TextFire(10, 0);

            //<editor-fold defaultstate="collapsed" desc="Skill...">
            if (ObjAtk.isPlayer()) {
                if (idxSkill == 19 && ObjAtk.clazz == 1) {
                    for (Option op : temp.minfo) {
                        if (op.id == 4) {
                            dame_skill += op.getParam(0);
                        } else if (op.id == 11) {
                            precent_dame_skill += op.getParam(0);
                        } else if (op.id == 33) {
                            ptCM += op.getParam(33);
                        } else if (op.id == 36) {
                            ptXG += op.getParam(36);
                        }
                    }
                } else {
                    for (int i = temp.minfo.length - 1; i >= 0; i--) {
                        Option op = temp.minfo[i];
                        if (type == 0) {
                            if (op.id == 0) {
                                dame_skill += op.getParam(0);
                            } else if (op.id == 7) {
                                precent_dame_skill += op.getParam(0);
                            } else if (op.id == 33) {
                                ptCM += op.getParam(0);
                            } else if (op.id == 36) {
                                ptXG += op.getParam(0);
                            }
                        } else {
                            if (op.id == 1 || op.id == 2 || op.id == 3 || op.id == 4) {
                                dame_skill += op.getParam(0);
                            } else if (op.id == 9 || op.id == 10 || op.id == 11 || op.id == 8) {
                                precent_dame_skill += op.getParam(0);
                            } else if (op.id == 33) {
                                ptCM += op.getParam(0);
                            } else if (op.id == 36) {
                                ptXG += op.getParam(0);
                            }
                        }
                    }
                }
                if (p != null && p.get_EffDefault(StrucEff.NOI_TAI_DIEN) != null) {
                    dame -= dame / 5;
                }
            }
            //</editor-fold>

            boolean isXuyenGiap = ptXG > Util.random(10_000);

            //<editor-fold defaultstate="collapsed" desc="Get Dame default...">
            if (ObjAtk.isMob() && focus.isPlayer()) {
                if (ObjAtk.level > focus.level) {
                    dame *= 2;
                }
            }
            byte type_dame = 2;
            if (type == 0) {
                int tempDameProp = ObjAtk.get_DameProp(0);
                tempDameProp = tempDameProp + dame_skill + tempDameProp * (precent_dame_skill / 10000);
                int dameProp = tempDameProp - (int) (isXuyenGiap ? 0 : tempDameProp * 0.0001 * focus.get_PercentDefProp(16));
                dame += Math.max(dameProp, 0);
                noitai = 0;
            } else if (type == 1) {
                switch (ObjAtk.clazz) {
                    case 0: {
                        int tempDameProp = ObjAtk.get_DameProp(2);
                        tempDameProp = tempDameProp + dame_skill + tempDameProp * (precent_dame_skill / 10000);
                        int dameProp = tempDameProp - (int) (isXuyenGiap ? 0 : tempDameProp * 0.0001 * focus.get_PercentDefProp(18));
                        dame += Math.max(dameProp, 0);
                        type_dame = 0;
                        noitai = 2;
                        break;
                    }
                    case 1: {
                        int tempDameProp = ObjAtk.get_DameProp(4);
                        tempDameProp = tempDameProp + dame_skill + tempDameProp * (precent_dame_skill / 10000);
                        int dameProp = tempDameProp - (int) (isXuyenGiap ? 0 : tempDameProp * 0.0001 * focus.get_PercentDefProp(20));
                        dame += Math.max(dameProp, 0);
                        type_dame = 1;
                        noitai = 4;
                        break;
                    }
                    case 2: {
                        int tempDameProp = ObjAtk.get_DameProp(1);
                        tempDameProp = tempDameProp + dame_skill + tempDameProp * (precent_dame_skill / 10000);

                        // Tăng thêm 25% sát thương cho class 2
                        tempDameProp = (int) (tempDameProp * 1.15);

                        int dameProp = tempDameProp - (int) (isXuyenGiap ? 0 : tempDameProp * 0.0001 * focus.get_PercentDefProp(17));
                        dame += Math.max(dameProp, 0);
                        type_dame = 3;
                        noitai = 1;
                        break;
                    }
                    case 3: {
                        int tempDameProp = ObjAtk.get_DameProp(3);
                        tempDameProp = tempDameProp + dame_skill + tempDameProp * (precent_dame_skill / 10000);
                        tempDameProp = (int) (tempDameProp * 1.15);
                        int dameProp = tempDameProp - (int) (isXuyenGiap ? 0 : tempDameProp * 0.0001 * focus.get_PercentDefProp(19));
                        dame += Math.max(dameProp, 0);
                        type_dame = 4;
                        noitai = 3;
                        break;
                    }
                }
            } else {
                dame += ObjAtk.get_DameProp(0);
            }
            //</editor-fold>

            if (Util.random(10000) > 1000) {
                noitai = -1;
            }

            List<Float> giamdame = new ArrayList<>();
            ef = ObjAtk.get_EffDefault(3);
            if (ef != null) {
                giamdame.add((float) 0.2);
            }
            if (ObjAtk.isPlayer() && p.getlevelpercent() < 0) {
                giamdame.add((float) 0.5);
            }

            //<editor-fold defaultstate="collapsed" desc="Nộ cánh...">
            if (ObjAtk.isPlayer()) {
                EffTemplate temp2 = p.get_EffDefault(StrucEff.PowerWing);
                if (temp2 == null) {
                    Item3 it = p.item.wear[10];
                    if (it != null) {
                        int percent = 0;
                        int time = 0;
                        for (Option op : it.op) {
                            if (op.id == 41) {
                                percent = op.getParam(it.tier);
                            } else if (op.id == 42) {
                                time = op.getParam(it.tier);
                            }
                        }
                        if (percent > Util.random(10_000)) {
                            //
                            p.add_EffDefault(StrucEff.PowerWing, 1000, time);
                            //
                            Message mw = new Message(40);
                            mw.writer().writeByte(0);
                            mw.writer().writeByte(3);
                            mw.writer().writeShort(ObjAtk.ID);
                            mw.writer().writeByte(21);
                            mw.writer().writeInt(time);
                            mw.writer().writeShort(ObjAtk.ID);
                            mw.writer().writeByte(0);
                            mw.writer().writeByte(30);
                            byte[] id__ = new byte[]{7, 8, 9, 10, 11, 15, 0, 1, 2, 3, 4, 14};
                            int[] par__ = new int[]{3000, 3000, 3000, 3000, 3000, 3000,
                                    3 * (ObjAtk.get_param_view_in4(0) / 10), 3 * (ObjAtk.get_param_view_in4(1) / 10),
                                    3 * (ObjAtk.get_param_view_in4(2) / 10), 3 * (ObjAtk.get_param_view_in4(3) / 10),
                                    3 * (ObjAtk.get_param_view_in4(4) / 10), 3 * (ObjAtk.get_param_view_in4(14) / 10)};
                            mw.writer().writeByte(id__.length);
                            //
                            for (int i = 0; i < id__.length; i++) {
                                mw.writer().writeByte(id__[i]);
                                mw.writer().writeInt(par__[i]);
                            }
                            //
                            MapService.send_msg_player_inside(p.map, p, mw, true);
                            mw.cleanup();
                        }
                    }
                }
            }

            //</editor-fold>
            ef = ObjAtk.get_EffDefault(53);
            int hpmax = ObjAtk.get_HpMax();
            int HoiHP = 0;
            if (ef != null && ObjAtk.hp < hpmax) {
                HoiHP += hpmax / 100;
            }
            //<editor-fold defaultstate="collapsed" desc="Tác dụng mề...">
            boolean isEffKhaiHoan = focus.isPlayer() && focus.get_EffMe_Kham(StrucEff.NgocKhaiHoan) != null;
            int prMeday = 0;
            if (focus.isPlayer()) {
                giamdame.add((float) (((Player) focus).total_item_param(80) * 0.0001));
            }
//        GiamDame += focus.isPlayer() ? (float) (((Player) focus).total_item_param(80) * 0.0001) : 0;//giáp hắc ám
            if (ObjAtk.isPlayer()) {
                if ((ef = ObjAtk.get_EffMe_Kham(StrucEff.TangHinh)) == null && ObjAtk.total_item_param(82) > Util.random(10_000)) {
                    ObjAtk.add_EffMe_Kham(StrucEff.TangHinh, 0, System.currentTimeMillis() + (prMeday = ObjAtk.total_item_param(81)));
                    Eff_special_skill.send_eff_TangHinh(p, 81, prMeday);
                } else if ((ef = ObjAtk.get_EffMe_Kham(StrucEff.KhienMaThuat)) == null && ObjAtk.total_item_param(85) > Util.random(10_000)) {
                    ObjAtk.add_EffMe_Kham(StrucEff.KhienMaThuat, 0, System.currentTimeMillis() + (prMeday = ObjAtk.total_item_param(86)));
                    Eff_special_skill.send_eff_Medal(p, 86, prMeday);
                }
            }
            if (focus.isPlayer() && !isEffKhaiHoan) {
                if (focus.get_EffMe_Kham(StrucEff.BongLua) == null && ObjAtk.total_item_param(76) > Util.random(10_000)) {
                    focus.add_EffMe_Kham(StrucEff.BongLua, 0, System.currentTimeMillis() + (prMeday = ObjAtk.total_item_param(77)));
                    Eff_special_skill.send_eff_Medal((Player) focus, 77, prMeday);
                } else if (focus.get_EffMe_Kham(StrucEff.BongLanh) == null && ObjAtk.total_item_param(78) > Util.random(10_000)) {
                    focus.add_EffMe_Kham(StrucEff.BongLanh, 0, System.currentTimeMillis() + (prMeday = ObjAtk.total_item_param(79)));
                    Eff_special_skill.send_eff_Medal((Player) focus, 79, prMeday);
                } else if (focus.get_EffMe_Kham(StrucEff.LuLan) == null && ObjAtk.total_item_param(87) > Util.random(10_000)) {
                    focus.add_EffMe_Kham(StrucEff.LuLan, 0, System.currentTimeMillis() + (prMeday = ObjAtk.total_item_param(88)));
                    Eff_special_skill.send_eff_Medal((Player) focus, 88, prMeday);
                }
                if (focus.get_EffMe_Kham(StrucEff.KhienMaThuat) != null) {
                    giamdame.add((float) 0.5);
                }
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Tác dụng khảm...">
            int prKham = 0;
            if (focus.isPlayer() && (ObjAtk.isBoss() || ObjAtk.get_TypeObj() == 0)) {
                if (!isEffKhaiHoan && (prKham = focus.total_item_param(101)) > 0) {
                    if (focus.kham.idAtk_KH == ObjAtk.ID) {
                        focus.kham.CountAtk_KH++;
                    } else {
                        focus.kham.idAtk_KH = ObjAtk.ID;
                        focus.kham.CountAtk_KH = 1;
                    }

                    if (focus.kham.CountAtk_KH >= prKham) {
                        focus.kham.idAtk_KH = 0;
                        focus.kham.CountAtk_KH = 0;
                        focus.add_EffMe_Kham(StrucEff.NgocKhaiHoan, 0, System.currentTimeMillis() + 3000);
                        Eff_special_skill.send_eff_kham((Player) focus, StrucEff.NgocKhaiHoan, 3000);
                    }
                }

                if (focus.get_EffMe_Kham(StrucEff.NgocLucBao) != null) {
                    hutHP += (int) (dame * 0.1);
                } else if ((prKham = focus.total_item_param(102)) > Util.random(10000)) {
                    focus.add_EffMe_Kham(StrucEff.NgocLucBao, prKham, System.currentTimeMillis() + 3000);
                    Eff_special_skill.send_eff_kham((Player) focus, StrucEff.NgocLucBao, 3000);
                }
            }

            if (ObjAtk.isPlayer()) {
                if ((focus.isBoss() || focus.get_TypeObj() == 0)) {
                    if (ObjAtk.get_EffMe_Kham(StrucEff.NgocHonNguyen) != null) {
                        DamePlus += 1;
                    }
                }

                double ptHP = (ObjAtk.hp / ObjAtk.get_HpMax()) * 100;
                if ((ef = ObjAtk.get_EffMe_Kham(StrucEff.NgocPhongMa)) != null) {
                    HoiHP += (int) (hpmax * ef.param * 0.0001);
                } else if (ptHP < ObjAtk.total_item_param(104) / 100 && (prKham = ObjAtk.total_item_param(103)) > Util.random(10_000)) {
                    ObjAtk.add_EffMe_Kham(StrucEff.NgocPhongMa, prKham, System.currentTimeMillis() + 5000);
                    Eff_special_skill.send_eff_kham(p, StrucEff.NgocPhongMa, 5000);
                }

                if (focus.isBoss() && (ef = ObjAtk.get_EffMe_Kham(StrucEff.NgocSinhMenh)) != null) {
                    DamePlus += 0.5;
//                dame += (long)(dame * 0.5);
                } else if (focus.isBoss() && (prKham = ObjAtk.total_item_param(106)) > Util.random(10_000)) {
                    ObjAtk.add_EffMe_Kham(StrucEff.NgocSinhMenh, prKham, System.currentTimeMillis() + 3000);
                    Eff_special_skill.send_eff_kham(p, StrucEff.NgocSinhMenh, 3000);
                }
                ptCrit += ObjAtk.total_item_param(107) * 0.0001;
            }
            //</editor-fold>
            dame += (long) (dame * DamePlus);

            int def = focus.get_DefBase();
//        def += def * focus.get_PercentDefBase() * 0.0001;
//        if (ObjAtk.isPlayer()) {
//            System.out.println("dame:   " + Util.number_format(dame)+"  def:   " +Util.number_format(def) + "   giam: "+GiamDame);
//        }
            if (dame > 2_000_000_000) {
                dame = 2_000_000_000;
            }
            dame -= dame * 0.35;
            dame -= (isXuyenGiap ? 0 : def);
            if (!giamdame.isEmpty()) {
                for (float f : giamdame) {
                    dame -= dame * f;
                }
            }

            if (ObjAtk.isPlayer() && focus.isMob()) {
                boolean check_mob_roi_ngoc_kham = focus.template.mob_id >= 167 && focus.template.mob_id <= 172;
                if (check_mob_roi_ngoc_kham) {
                    if (50 > Util.random(100)) {
                        dame = 0;
                    } else {
                        dame = 1;
                    }
                }
                boolean check = dame < 0
                        || (focus.isBoss() && Math.abs(focus.level - ObjAtk.level) > 5 && focus.level < 120 && focus.template.mob_id != 174 && !Map.is_map_cant_save_site(focus.map_id))
                        || (focus.isBoss() && focus.template.mob_id == 174 && map.zone_id == 0 && ObjAtk.level > 89)
                        || (focus.isBoss() && focus.template.mob_id == 174 && map.zone_id == 2 && !(ObjAtk.level >= 90 && ObjAtk.level < 110))
                        || (focus.isBoss() && focus.template.mob_id == 174 && map.zone_id == 3 && ObjAtk.level < 110);
                if (check) {
                    dame = 0;
                }
            }
            if (focus.isMoTaiNguyen() && ObjAtk.isPlayer()) {
                Mob_MoTaiNguyen mo = (Mob_MoTaiNguyen) focus;
                if (!mo.is_atk) {
                    dame = 0;
                } else if (mo.nhanBans != null) {
                    for (int i = 0; i < mo.nhanBans.size(); i++) {
                        mo.nhanBans.get(i).p_target = (Player) ObjAtk;
                        mo.nhanBans.get(i).is_move = false;
                    }
                }
            }

            if (ObjAtk.isPlayer() && HoiHP > 0) {
                Service.usepotion(p, 0, HoiHP);
            }
            if (idxSkill == 17 && ObjAtk.isPlayer() && focus.isPlayer()) {
                MapService.add_eff_skill(map, p, (Player) focus, (byte) idxSkill);
            }

            //<editor-fold defaultstate="collapsed" desc="Hiệu ứng Crit vv       ...">
            List<Eff_TextFire> ListEf = new ArrayList<>();

            if (hutHP > 0) {
                ListEf.add(new Eff_TextFire(0, (int) dame));
                ListEf.add(new Eff_TextFire(2, hutHP));
                focus.hp += hutHP;
                if (focus.hp > focus.get_HpMax()) {
                    focus.hp = focus.get_HpMax();
                }
            }
            long finalDame = dame;

// ================= XUYÊN GIÁP =================
            if (isXuyenGiap) {
                long xgDame = (long) (finalDame * 0.3);
                finalDame += xgDame;
                ListEf.add(new Eff_TextFire(1, (int) xgDame));
            }

// ================= CHÍ MẠNG =================
            if (ptCM > Util.random(10_000)) {
                long critDame = finalDame; // crit x2
                finalDame += critDame;
                ListEf.add(new Eff_TextFire(4, (int) critDame));
            }

// ================= OPT 107 (+ SAU) =================
            int opt107 = ObjAtk.total_item_param(107); // ví dụ 300 = 3%
            if (opt107 > 0) {
                long plusDame = finalDame * opt107 / 10_000;
                if (plusDame > 0) {
                    finalDame += plusDame;
                    ListEf.add(new Eff_TextFire(7, (int) plusDame));
                }
            }

// ================= CHỐT =================
            dame = finalDame;
            //<editor-fold defaultstate="collapsed" desc="Phản Dame       ...">
            if (focus.get_PhanDame() > Util.random(10_000)) {
                int DAMEpst = (int) (dame * 0.8);
                DAMEpst -= ObjAtk.get_DefBase();
                if (type == 1) {
                    if (ObjAtk.clazz == 0) {
                        DAMEpst -= DAMEpst * 0.0001 * ObjAtk.get_PercentDefProp(18);
                    } else if (ObjAtk.clazz == 1) {
                        DAMEpst -= DAMEpst * 0.0001 * ObjAtk.get_PercentDefProp(20);
                    } else if (ObjAtk.clazz == 2) {
                        DAMEpst -= DAMEpst * 0.0001 * ObjAtk.get_PercentDefProp(17);
                    } else if (ObjAtk.clazz == 3) {
                        DAMEpst -= DAMEpst * 0.0001 * ObjAtk.get_PercentDefProp(19);
                    }
                } else {
                    DAMEpst -= DAMEpst * 0.0001 * ObjAtk.get_PercentDefProp(16);
                }
                if (DAMEpst <= 0) {
                    DAMEpst = 1;
                }

                ListEf.add(new Eff_TextFire(5, DAMEpst));
                ObjAtk.hp -= DAMEpst;
                if (ObjAtk.hp <= 0) {
                    ObjAtk.hp = 5;
                }
            }
            //</editor-fold> Phản Dame

            //</editor-fold>    hiệu ứng crit vv
            //<editor-fold defaultstate="collapsed" desc="Set hp       ...">
            // xả item chiến trường
            long time = System.currentTimeMillis();
            if (ObjAtk.isHouse() && map.Arena != null && map.Arena.timeCnNha > time) {
                dame *= 2;
            } else if (!ObjAtk.isPlayer() && ObjAtk.get_TypeObj() == 0 && map.Arena != null && map.Arena.timeCnLinh > time) {
                dame *= 2;
            }
            if (dame > 2_000_000_000) {
                dame = 2_000_000_000;
            } else if (dame <= 0) {
                dame = 1;
            }
            if (ObjAtk.isPlayer() && focus.isPlayer()) {
                stas.dame = p.getLightDame((Player) focus);
                if (stas.dame == 0) {
                    stas.type = 11;
                    stas.dame = p.getLightDame((Player) focus);
                }
            }
            if (focus.isMobDiBuon()) {
                dame = focus.hp_max / 100;
            }
            if (focus.isHouse() && ObjAtk.isPlayer()) {
                Mob_in_map mob = (Mob_in_map) focus;
                if (focus.hp < focus.hp_max / 2 && Util.random(5) == 2 && mob.count_meterial > 0) {
                    mob.count_meterial--;
                    LeaveItemMap.leave_item_by_type7(map, (short) Util.random(133, 136), p, focus.ID, p.ID);
                }
                if (map.Arena.timeBienHinh > time) {
                    dame = 1;
                }
            }

            //<editor-fold defaultstate="collapsed" desc="Tác dụng đồ tinh tú      ">
            if (focus.isPlayer() && ObjAtk.isPlayer()) {
                Player player = (Player) focus;
                // Miễn st
                if (player.mienST(type_dame)) {
                    dame = 0;
                }
                // Giáp bảo hộ
                EffTemplate eff_bao_ho = focus.getEffTinhTu(EffTemplate.GIAP_BAO_HO);
                if (eff_bao_ho == null && player.isEffTinhTu(-114)) {
                    focus.addEffTinhTu(EffTemplate.GIAP_BAO_HO, 3, 10000L);
                }
                if (eff_bao_ho != null) {
                    if (eff_bao_ho.param > 0) {
                        dame = dame - (4 - eff_bao_ho.param) * dame / 4;
                        eff_bao_ho.param--;
                    } else {
                        focus.removeEffTinhTu(eff_bao_ho);
                    }
                }

                // Bộc phá
                if (player.isEffTinhTu(-125)) {
                    for (int i = 0; i < map.players.size(); i++) {
                        Player p_ = map.players.get(i);
                        if (p_ != null && Math.abs(p.x - p_.x) < 150 && Math.abs(p.y - p_.y) < 150) {
                            long hp_ = (long) p_.hp / 100 * Util.random(10, 40);
                            Service.usepotion(map.players.get(i), 0, -hp_);
                        }
                    }
                    Service.send_notice_nobox_white(player.conn, "Bộc phá");
                }
                // Giáp hung tàn
                EffTemplate eff_hung_tan = p.getEffTinhTu(EffTemplate.HUNG_TAN);
                if (eff_hung_tan == null && p.isEffTinhTu(-117)) {
                    p.addEffTinhTu(EffTemplate.HUNG_TAN, Util.random(3, 6), 20000L);
                    sendEffTinhTu(p, EffTemplate.HUNG_TAN, 20000);
                }
                if (eff_hung_tan != null) {
                    if (eff_hung_tan.param > 0) {
                        if (eff_hung_tan.param > eff_hung_tan.param2) {
                            eff_hung_tan.param2++;
                        } else {
                            HoiHP = p.get_HpMax() * eff_hung_tan.param / 100;
                            Service.usepotion(p, 0, HoiHP);
                            eff_hung_tan.param = (short) Util.random(3, 6);
                        }
                    }
                }
                // Giáp bạch kim
                EffTemplate eff_giap_bach_kim = focus.getEffTinhTu(EffTemplate.GIAP_BACH_KIM);
                if (eff_giap_bach_kim == null && p.isEffTinhTu(-85)) {
                    focus.addEffTinhTu(EffTemplate.GIAP_BACH_KIM, 0, 3000L);
                }
                // Giáp thiên sứ
                EffTemplate eff_giap_thien_su = focus.getEffTinhTu(EffTemplate.GIAP_THIEN_SU);
                if (eff_giap_thien_su == null && p.isEffTinhTu(-83)) {
                    focus.addEffTinhTu(EffTemplate.GIAP_THIEN_SU, 0, 3000L);
                }
                // Giáp vệ binh
                EffTemplate eff_giap_ve_binh = focus.getEffTinhTu(EffTemplate.GIAP_VE_BINH);
                if (eff_giap_ve_binh == null && p.isEffTinhTu(-81)) {
                    focus.addEffTinhTu(EffTemplate.GIAP_VE_BINH, 0, 3000L);
                }
                // Ngu đần
                EffTemplate eff_giap_ngu_dan = focus.getEffTinhTu(EffTemplate.NGU_DAN);
                if (eff_giap_ngu_dan == null && p.isEffTinhTu(-90)) {
                    focus.addEffTinhTu(EffTemplate.NGU_DAN, 0, 30000L);
                }
                // Bất tử
                if (player.hp < (focus.get_HpMax() / 20) && player.total_item_param(-88) > 0
                        && player.cooldown_bat_tu < System.currentTimeMillis()) {
                    player.addEffTinhTu(EffTemplate.BAT_TU, 0, 5000L);
                    player.cooldown_bat_tu = System.currentTimeMillis() + 180_000L;
                }
                // Mù mắt
                EffTemplate eff_giap_mu_mat = focus.getEffTinhTu(EffTemplate.MU_MAT);
                if (eff_giap_mu_mat == null && p.isEffTinhTu(-116)) {
                    focus.addEffTinhTu(EffTemplate.MU_MAT, 0, 5000L);
                    sendEffTinhTu(player, EffTemplate.MU_MAT, 5000);
                }
                // Thiêu cháy
                if (player.hp < (player.get_HpMax() * 3 / 10) && player.total_item_param(-115) > 0 && player.cooldown_thieu_chay < System.currentTimeMillis()) {
                    for (int i = 0; i < map.players.size(); i++) {
                        Player p_ = map.players.get(i);
                        if (p_ != null && Math.abs(p.x - p_.x) < 250 && Math.abs(p.y - p_.y) < 250) {
                            p_.addEffTinhTu(EffTemplate.THIEU_CHAY, 1, 20000);
                            sendEffTinhTu(p_, EffTemplate.THIEU_CHAY, 20000);
                            Service.send_notice_nobox_white(p_.conn, "Bạn bị trúng hiệu ứng thiêu cháy");
                        }
                    }
                    player.cooldown_thieu_chay = System.currentTimeMillis() + 180_000L;
                }
                // Tàn phế
                EffTemplate eff_giap_tan_phe = focus.getEffTinhTu(EffTemplate.TAN_PHE);
                if (eff_giap_tan_phe == null && p.isEffTinhTu(-92)) {
                    long mp = focus.mp - focus.get_MpMax() / 99;
                    focus.addEffTinhTu(EffTemplate.TAN_PHE, 0, 5000L);
                    Service.usepotion(player, 1, -mp);
                    sendEffTinhTu(p, EffTemplate.TAN_PHE, 5000);
                }
                // Bất tử dame = 0
                if (p.getEffTinhTu(EffTemplate.BAT_TU) != null) {
                    dame = 0;
                }


                //    EffTemplate eff_bien_dang = focus.add_EffDefault(68, prKham, 100)
            }

            if (ObjAtk.isPlayer() && p != null && p.countTT() == 9) {
                if (Util.random(100) < 5) { // 20% xác suất
                    p.addEffTinhTu(EffTemplate.SPECIAL, 1, 5000);
                    sendEffTinhTu(p, EffTemplate.SPECIAL, 5000);

                    Message mw = new Message(40);
                    mw.writer().writeByte(0);
                    mw.writer().writeByte(1);
                    mw.writer().writeShort(ObjAtk.ID);
                    mw.writer().writeByte(21);
                    mw.writer().writeInt(5000);
                    mw.writer().writeShort(ObjAtk.ID);
                    mw.writer().writeByte(0);
                    mw.writer().writeByte(30);

                    byte[] id__ = new byte[]{7, 8, 9, 10, 11, 15, 0, 1, 2, 3, 4, 16, 17, 18, 19, 20};
                    int[] par__ = new int[]{
                            4000, 4000, 4000, 4000, 4000, 4000,
                            4 * (ObjAtk.get_param_view_in4(0) / 10),
                            4 * (ObjAtk.get_param_view_in4(1) / 10),
                            4 * (ObjAtk.get_param_view_in4(2) / 10),
                            4 * (ObjAtk.get_param_view_in4(3) / 10),
                            4 * (ObjAtk.get_param_view_in4(4) / 10),
                            4 * (ObjAtk.get_param_view_in4(14) / 10),
                            4000, 4000, 4000, 4000, 4000
                    };

                    mw.writer().writeByte(id__.length);
                    for (int i = 0; i < id__.length; i++) {
                        mw.writer().writeByte(id__[i]);
                        mw.writer().writeInt(par__[i]);
                    }

                    MapService.send_msg_player_inside(p.map, p, mw, true);
                    mw.cleanup();
                }
            }


            byte type_spec = 11;
            int dame_spec = 0;

            if (ObjAtk.isPlayer() && p != null) {
                if (p.total_item_param(5) > 0) {
                    type_spec = 11;
                    dame_spec = p.total_item_param(5);
                } else if (p.total_item_param(6) > 0) {
                    type_spec = 10;
                    dame_spec = p.total_item_param(6);
                }
            }

            if (dame_spec > 0) {
                stas.type = type_spec;
                stas.dame = dame_spec;
                ListEf.add(stas);
            }


            Player p_focus = focus.isPlayer() ? (Player) focus : null;
            if (focus.isPlayer() && p_focus != null && p != null) {
                short horseId = p.id_horse;  // ID ngựa của người đánh (player1)
                short horseIdFocus = p_focus.id_horse;  // ID ngựa của người bị đánh (player2)

                // Các tham số liên quan đến Áo Giáp Lửa và miễn sát thương
                int param101 = p.total_item_param((byte) -101);  // param để kích hoạt Áo Giáp Lửa của player1
                int param128 = p.total_item_param((byte) -128);  // param khác để kích hoạt Áo Giáp Lửa của player1
                int param70 = p_focus.total_item_param((byte) -70);  // param để miễn sát thương của player2

                // Tỷ lệ kích hoạt Áo Giáp Lửa (50% theo điều kiện random)
                boolean isFireArmorActivated = (Util.random(1, 100) <= 50);  // Tỷ lệ kích hoạt 50% cho Áo Giáp Lửa
                boolean isImmuneToFireArmor = (horseIdFocus == 172 && param70 >= 20 && Util.random(1, 100) <= 10);  // Miễn sát thương nếu horseId = 172 và param70 >= 100

                // In thông tin debug


                // Kiểm tra nếu player2 miễn sát thương từ Áo Giáp Lửa
                if (isImmuneToFireArmor) {
                    // Service.send_notice_nobox_white(p.conn, "Đối phương được miễn Áo giáp lửa!");
                    //   Service.send_notice_nobox_white(p_focus.conn, "Bạn không bị ảnh hưởng bởi Áo giáp lửa!");

                }

                // Kiểm tra nếu player1 (người đánh) kích hoạt Áo Giáp Lửa
                if (horseId == 116 && (param101 >= 100 || param128 >= 100) && isFireArmorActivated) {
                    if (isImmuneToFireArmor) {
                        // Nếu player2 miễn sát thương Áo Giáp Lửa, không tính sát thương từ Áo Giáp Lửa

                    } else {
                        // Nếu player2 không miễn sát thương, tính sát thương từ Áo Giáp Lửa
                        int damagePercent = Util.random(5, 20);  // 5% đến 20% HP của player2
                        int damage = (int) -(p_focus.hp * damagePercent * 0.01);

                        if (damage == 0 && p_focus.hp > 0) damage = -1; // Đảm bảo ít nhất trừ 1 máu

                        // Tạo hiệu ứng giảm máu theo thời gian
                        java.util.Timer timer = new java.util.Timer();
                        for (int i = 1; i <= 3; i++) {  // Mỗi giây giảm máu 1 lần trong 3 giây
                            final int tick = i;
                            timer.schedule(new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    if (p_focus.hp > 0) {
                                        // Tính sát thương tại mỗi tick
                                        int dmg = (int) (p_focus.get_HpMax() * 0.2);  // Giảm 10% HP tối đa của player2 mỗi giây
                                        if (dmg <= 0) dmg = 1;  // Đảm bảo ít nhất trừ 1 HP
                                        p_focus.hp -= dmg;
                                        if (p_focus.hp < 1) p_focus.hp = 1;  // Đảm bảo HP không dưới 1
                                        // Nếu hết thời gian, thông báo
                                        if (tick == 3) {

                                        }
                                    }
                                }
                            }, tick * 100L);  // Sau mỗi giây
                        }
                    }
                }
            }
            if (ObjAtk.isPlayer() && focus.isPlayer() && p != null) {
                long now = System.currentTimeMillis();
                if (p.time_one_hit <= now && p.time_one_hit_active <= now) {
                    int param61 = p.total_item_param((byte) -61);
                    if (Util.nextInt(10000) < param61) {
                        p.time_one_hit = now + 20000L;
                        p.time_one_hit_active = now + 5000L;
                        p.one_hit_targets.clear();

                        if (p.conn != null) {
                            Service.send_notice_nobox_white(p.conn, "ONE HIT ACTIVE (5s)!");
                        }
                    }
                }

                Player target = (Player) focus;
                if (p.time_one_hit_active > now) {
                    if (!p.one_hit_targets.contains(target.ID)) {
                        if (target.getEffTinhTu(EffTemplate.VO_DICH) == null) {
                            p.one_hit_targets.add(target.ID);
                            target.hp = 0;
                            MapService.Player_Die(map, target, p, true);
                            return;
                        }
                    }
                }
            }
            if (ObjAtk.isPlayer() && focus.isPlayer() && p_focus != null) {
                long now = System.currentTimeMillis();
                int param63 = p_focus.total_item_param((byte) -62);

                if (p_focus.time_vo_dich <= now) {
                    if (param63 > Util.nextInt(10000)) {
                        if (p_focus.getEffTinhTu(EffTemplate.VO_DICH) == null) {
                            p_focus.addEffTinhTu(EffTemplate.VO_DICH, 0, 15000L);
                            sendEffTinhTu(p_focus, EffTemplate.VO_DICH, 15000);
                            p_focus.time_vo_dich = now + 60000L;

                            if (p_focus.conn != null) {
                                Service.send_notice_nobox_white(p_focus.conn, "VÔ ĐỊCH");
                            }
                        }
                    }
                }

                if (p_focus.getEffTinhTu(EffTemplate.VO_DICH) != null) {
                    return;
                }
            }
            // ================== GIẢM SÁT THƯƠNG CHUNG (0 1 2 3 4) ==================
            if (focus != null && focus.isPlayer()) {

                long debugDame = dame;

                int raw = focus.total_item_param(-63);

                // ===== CHUẨN HÓA % GIẢM SÁT THƯƠNG =====
                int giamPercent = raw;

                // Nếu bị scale (1000, 500, 300...)
                while (giamPercent > 90) {
                    giamPercent /= 10;
                }

                if (giamPercent < 0) giamPercent = 0;

                if (giamPercent > 0) {
                    dame = dame * (100 - giamPercent) / 100;

                    // System.out.println(
                    //    "[GIAM_ST] target=" + focus.name +
                    //     " | dame_goc=" + debugDame +
                    //     " | raw=" + raw +
                    //     " | giam=" + giamPercent + "%" +
                    //    " | dame_sau=" + dame

                }
            }

// Trừ HP
            if (dame < 1) dame = 1;
            focus.hp -= dame;
            if (focus.hp < 1) focus.hp = 1;


            if (focus.isPlayer() && p != null) {


                int param64 = p.total_item_param((byte) -64);


                // Điều kiện kích hoạt Quáng Tuyết
                if (param64 > Util.nextInt(10000)) {
                    // Nếu chưa có hiệu ứng thì mới áp dụng
                    if (p_focus.getEffTinhTu(EffTemplate.QUANG_TUYET) == null) {
                        p_focus.addEffTinhTu(EffTemplate.QUANG_TUYET, 0, 1000L);
                        sendEffTinhTu(p_focus, EffTemplate.QUANG_TUYET, 1000);

                        // Giảm 3% máu
                        int lostHp = (int) (p_focus.get_HpMax() * 0.03);
                        if (lostHp <= 0) lostHp = 1;
                        p_focus.hp -= lostHp;
                        if (p_focus.hp <= 0) p_focus.hp = 1;

                        int lostMp = (int) (p_focus.get_MpMax() * 0.05);
                        if (lostMp <= 0) lostMp = 1;
                        p_focus.mp -= lostMp;
                        if (p_focus.mp < 0) p_focus.mp = 0;


                        // Gửi thông báo
                        if (p.conn != null) {
                            //      Service.send_notice_nobox_white(p.conn, "☃️ Kích hoạt Quáng Tuyết!");
                        }

                        if (p_focus.conn != null) {
                            // Người bị đánh chỉ thấy thông báo nhỏ bên góc
                            //  Service.send_notice_nobox_white(p_focus.conn, "❄️ Bạn bị dính Quáng Tuyết!");
                        }


                    }
                }
            }
            //
            if (focus.isPlayer() && p != null) {
                int param72 = p.total_item_param((byte) -72);


                // Điều kiện kích hoạt Quáng Tuyết
                if (param72 > Util.nextInt(10000)) {
                    // Nếu chưa có hiệu ứng thì mới áp dụng
                    if (p_focus.getEffTinhTu(EffTemplate.GOI_SET) == null) {
                        p_focus.addEffTinhTu(EffTemplate.GOI_SET, 0, 1000L); // Giảm 10% sát thương trong 10 giây
                        sendEffTinhTu(p_focus, EffTemplate.GOI_SET, 1000);

                        // Giảm 5% máu từ gọi sét
                        int lostHpFromLightning = (int) (p_focus.get_HpMax() * 0.1);
                        if (lostHpFromLightning <= 0) lostHpFromLightning = 1;
                        p_focus.hp -= lostHpFromLightning;
                        if (p_focus.hp <= 0) p_focus.hp = 1;

                        // Giảm 10% sát thương từ địch (giảm sát thương tạm thời)
                        // Thêm hiệu ứng NOI_TAI_DIEN để giảm sát thương trong 10 giây
                        if (focus.isPlayer()) {
                            // Thêm hiệu ứng giảm sát thương cho người chơi
                            ((Player) focus).add_EffDefault(StrucEff.NOI_TAI_DIEN, 100, 10000);
                        }

                        // Giảm 10% sát thương từ địch

                        // Giảm 10% MP của focus (nếu có)
                        int lostMp = (int) (p_focus.get_MpMax() * 0.05);
                        if (lostMp <= 0) lostMp = 1;
                        p_focus.mp -= lostMp;
                        if (p_focus.mp < 0) p_focus.mp = 0;

                        if (p.conn != null) {
                            // Service.send_notice_nobox_white(p.conn, "⚡ Kích hoạt Gọi sét!");
                        }

                        if (p_focus.conn != null) {
                            // Người bị đánh chỉ thấy thông báo nhỏ bên góc
                            // Service.send_notice_nobox_white(p_focus.conn, "🌩️ Bạn bị Sét đánh!");
                        }


                    }
                }
            }
//

            if (focus.isPlayer() && p != null) {
                int param96 = p.total_item_param((byte) -96);


                // Điều kiện kích hoạt Lôi Phạt (Lôi Phạt - 10% HP)
                if (param96 > Util.nextInt(10000)) {
                    // Nếu chưa có hiệu ứng thì mới áp dụng
                    if (p_focus.getEffTinhTu(EffTemplate.LOI_PHAT) == null) {
                        p_focus.addEffTinhTu(EffTemplate.LOI_PHAT, 0, 1000L); // Thêm hiệu ứng Lôi Phạt trong 10 giây
                        sendEffTinhTu(p_focus, EffTemplate.LOI_PHAT, 1000);

                        // Gây 10% sát thương từ HP tối đa của mục tiêu (Lôi Phạt)
                        int lostHpFromLightning = (int) (p_focus.get_HpMax() * 0.1);
                        if (lostHpFromLightning <= 0) lostHpFromLightning = 1;  // Đảm bảo ít nhất 1 sát thương
                        p_focus.hp -= lostHpFromLightning;
                        if (p_focus.hp <= 0) p_focus.hp = 1;  // Đảm bảo HP không thấp hơn 1

                        // Gửi thông báo cho mục tiêu bị Lôi Phạt
                        if (p_focus.conn != null) {
                            //    Service.send_notice_nobox_white(p_focus.conn, "🌩️ Bạn bị Lôi Phạt!");
                        }

                        // Gửi thông báo cho người tấn công
                        if (p.conn != null) {
                            //   Service.send_notice_nobox_white(p.conn, "⚡ Kích hoạt Lôi Phạt!");
                        }

                        // Debug thông tin về sát thương

                    }
                }
            }
//
            if (focus.isPlayer() && p != null) {
                int param95 = p.total_item_param((byte) -95);


                // Điều kiện kích hoạt Lửa Hỏa Ngục (Lửa Hỏa Ngục - 10% HP)
                if (param95 > Util.nextInt(10000)) {
                    // Nếu chưa có hiệu ứng thì mới áp dụng
                    if (p_focus.getEffTinhTu(EffTemplate.LUA_HOA_NGUC) == null) {
                        p_focus.addEffTinhTu(EffTemplate.LUA_HOA_NGUC, 0, 1000L); // Thêm hiệu ứng Lửa Hỏa Ngục trong 10 giây
                        sendEffTinhTu(p_focus, EffTemplate.LUA_HOA_NGUC, 1000);

                        // 1. Gây 10% sát thương từ HP tối đa của mục tiêu (Lửa Hỏa Ngục)
                        int lostHpFromFire = (int) (p_focus.get_HpMax() * 0.1);
                        if (lostHpFromFire <= 0) lostHpFromFire = 1;  // Đảm bảo ít nhất 1 sát thương
                        p_focus.hp -= lostHpFromFire;
                        if (p_focus.hp <= 0) p_focus.hp = 1;  // Đảm bảo HP không thấp hơn 1

                        // 2. Không tính sát thương cơ bản (dame), chỉ có sát thương từ HP tối đa của mục tiêu

                        // Gửi thông báo cho mục tiêu bị Lửa Hỏa Ngục
                        if (p_focus.conn != null) {
                            // Service.send_notice_nobox_white(p_focus.conn, "🔥 Bạn bị Lửa Hỏa Ngục!");
                        }

                        // Gửi thông báo cho người tấn công
                        if (p.conn != null) {
                            // Service.send_notice_nobox_white(p.conn, "🔥 Kích hoạt Lửa Hỏa Ngục!");
                        }

                    }
                }
            }


//
// Kiểm tra xem mục tiêu có phải là người chơi không
            if (focus.isPlayer() && p != null) {
                // Player p_focus = (Player) focus; // Người bị tấn công

                // ======= [PHẦN 1] NGƯỜI TẤN CÔNG BỊ CHÓNG MẶT =======
                if (p.getEffTinhTu(EffTemplate.CHONG_MAT) != null) {
                    if (Util.nextInt(100) < 100) { // 60% đánh miss khi đang chóng mặt

                        if (p.conn != null) {
                            // Service.send_notice_nobox_white(p.conn, "🤕 Bạn đang chóng mặt nên đánh trượt!");
                        }
                        return; // Kết thúc đòn đánh
                    }
                }

                // ======= [PHẦN 2] GÂY CHÓNG MẶT CHO ĐỐI THỦ =======
                int paramDizziness = p.total_item_param((byte) -76);  // Xác suất gây chóng mặt


                if (paramDizziness > Util.nextInt(10000)) { // Xác suất theo item
                    if (p_focus.getEffTinhTu(EffTemplate.CHONG_MAT) == null) {
                        // Áp dụng chóng mặt cho người bị tấn công
                        p_focus.addEffTinhTu(EffTemplate.CHONG_MAT, 0, 5000L); // 5 giây
                        sendEffTinhTu(p_focus, EffTemplate.CHONG_MAT, 5000);


                        // Gửi thông báo
                        if (p.conn != null) {
                            // Service.send_notice_nobox_white(p.conn, "⚡ Bạn đã khiến đối thủ chóng mặt!");
                        }
                        if (p_focus.conn != null) {
                            //  Service.send_notice_nobox_white(p_focus.conn, "😵 Bạn bị chóng mặt và không thể tấn công!");
                        }
                    }
                }
            }
            if (focus.isPlayer() && p != null) {
                int paramTeCong = p.total_item_param((byte) 67); // 67 là param của Tê Dái


                if (paramTeCong > Util.nextInt(10000)) {
                    if (p_focus.getEffTinhTu(EffTemplate.TE_DAI) == null) {
                        // Thêm hiệu ứng Tê Dái trong 3 giây
                        p_focus.addEffTinhTu(EffTemplate.TE_DAI, 0, 5000);
                        sendEffTinhTu(p_focus, EffTemplate.TE_DAI, 5000);

                        java.util.Timer timer = new java.util.Timer();
                        for (int i = 1; i <= 3; i++) {
                            final int tick = i;
                            timer.schedule(new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    if (p_focus.hp > 0 && p_focus.getEffTinhTu(EffTemplate.TE_DAI) != null) {
                                        int dmg = (int) (p_focus.get_HpMax() * 0.1);
                                        if (dmg <= 0) dmg = 1;
                                        p_focus.hp -= dmg;
                                        if (p_focus.hp < 1) p_focus.hp = 1;
                                    }
                                }
                            }, tick * 1000L);
                        }
                        // Thông báo cho cả người tấn công và mục tiêu
                        try {
                            if (p.conn != null) {
                                Service.send_notice_nobox_white(p.conn, "❄️ Kích hoạt hiệu ứng Tê Dái!");
                            }
                            if (p_focus.conn != null) {

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
//antimisss
// ======== 1. Biến Dạng trước ========
// ====== 1. Kích hoạt Biến Dạng nếu người đánh có param 69 ======
            if (p != null && p.total_item_param((byte) 69) > 0) { // A có opt 69
                if (focus != null && focus instanceof Player) {   // focus chính là B
                    Player pl = (Player) focus;
                    if (pl.hp > 0 && pl.getEffTinhTu(EffTemplate.BIEN_DANG) == null) {
                        pl.addEffTinhTu(EffTemplate.BIEN_DANG, -100, 10000L); // B mất 100% né trong 10s
                        sendEffTinhTu(pl, EffTemplate.BIEN_DANG, 10000);

                        if (pl.conn != null) {
                            //  Service.send_notice_nobox_white(pl.conn, "💀 Bạn bị Biến Dạng – mất toàn bộ né tránh!");
                        }

                        if (p.conn != null) {
                            // Service.send_notice_nobox_white(p.conn, "💀 Kích hoạt Biến Dạng – mục tiêu mất toàn bộ né tránh!");
                        }
                    }
                }
            }


            if (focus.isPlayer() && p_focus != null && p != null) {
                int param101 = p.total_item_param((byte) -101);
                int param128 = p.total_item_param((byte) -128);


                if (param101 > Util.nextInt(10000) || param128 > Util.nextInt(10000)) {
                    int damagePercent = Util.random(1, 5);
                    int damage = (int) -(p_focus.hp * damagePercent * 0.01);

                    if (damage == 0 && p_focus.hp > 0) damage = -1;

                    Service.usepotion(p_focus, 0, damage);
                    //  Service.send_notice_nobox_white(p.conn, "Sát thương chuẩn!");
                    //  Service.send_notice_nobox_white(p_focus.conn, "Bạn bị sát thương chuẩn!");


                }
            }


            if (focus.isPlayer() && p_focus != null && p != null && p.total_item_param((byte) 185) > Util.nextInt(10000)) {
                focus.hp -= p_focus.hp_max * (Util.nextInt(10, 15) / 100);
                Service.send_notice_nobox_white(p.conn, "Áp đảo");
            }
            if (p != null && focus.isPlayer() && p_focus != null
                    && p_focus.total_item_param((byte) 186) > Util.nextInt(10000)) {
                p.hp -= ((Player) p).hp_max * (Util.nextInt(3, 5) / 100);
                focus.hp += p_focus.hp_max * (Util.nextInt(20, 25) / 100);
                Service.send_notice_nobox_white(p_focus.conn, "Giáp cốt");
            }
            //</editor-fold> Tác dụng đồ tinh tú
            if (ObjAtk.isPlayer() && Util.random(100) < 10
                    && p != null && p.type_use_horse == Horse.SU_TU) {
                List<MainObject> objects = new ArrayList<>();
                objects.add(p);
                // Service.send_eff_auto(p.conn, objects, 127);
                focus.setHp(ObjAtk, (int) (dame / 20));
            }

            Mob_in_map mob = focus.isMob() ? (Mob_in_map) focus : null;

            if (ObjAtk.isPlayer() && noitai != -1) {
                switch (noitai) {
                    case 0 -> {
                        if (focus.isPlayer()) {
                            ((Player) focus).add_EffDefault(StrucEff.NOI_TAI_VAT_LY, 10, 10000);
                        } else if (focus.isMob() && mob != null) {
                            mob.add_Eff(StrucEff.NOI_TAI_VAT_LY, 10, 10000);
                        }
                    }
                    case 2 -> {
                        if (focus.isPlayer()) {
                            ((Player) focus).add_EffDefault(StrucEff.NOI_TAI_LUA, 10, 10000);
                        } else if (focus.isMob() && mob != null) {
                            mob.add_Eff(StrucEff.NOI_TAI_LUA, 10, 10000);
                        }
                    }
                    case 4 -> {
                        if (focus.isPlayer()) {
                            ((Player) focus).add_EffDefault(StrucEff.NOI_TAI_DOC, (int) (dame / 25 * 4), 10000);
                        } else if (focus.isMob() && mob != null) {
                            mob.add_Eff(StrucEff.NOI_TAI_DOC, (int) (dame / 25 * 4), 10000);
                            EffTemplate eff = mob.get_EffDefault(StrucEff.NOI_TAI_DOC);
                            eff.o_atk = ObjAtk;
                        }
                    }
                    case 1 -> {
                        if (focus.isPlayer()) {
                            ((Player) focus).add_EffDefault(StrucEff.NOI_TAI_BANG, 10, 10000);
                            MapService.send_in4_other_char(map, p, ((Player) focus));
                            Service.send_char_main_in4((Player) focus);
                        } else if (focus.isMob() && mob != null) {
                            mob.add_Eff(StrucEff.NOI_TAI_BANG, 20, 10000);
                            Service.mob_in4(p, focus.ID);
                        }
                    }
                    default -> {
                        if (focus.isPlayer()) {
                            ((Player) focus).add_EffDefault(StrucEff.NOI_TAI_DIEN, 10, 10000);
                        } else if (focus.isMob() && mob != null) {
                            mob.add_Eff(StrucEff.NOI_TAI_DIEN, 10, 10000);
                        }
                    }
                }
                Service.send_eff_intrinsic(map, focus, noitai, 10);
            }
            focus.hp -= (dame + dame_spec);
            if (!map.isMapChienTruong() && focus.isBoss() && mob != null
                    && ObjAtk.isPlayer() && p != null && mob.template.mob_id == 101) {
                dame = Util.random(focus.hp_max / 500, focus.hp_max / 450);
            }
            focus.setHp(ObjAtk, (int) dame);

            if (focus.isBoss() && mob != null && ObjAtk.isPlayer() && p != null) {
                if (!map.isMapChienTruong()) {
                    if (focus.hp < focus.hp_max / 2 && mob.count_meterial > 0 && Util.random(2) == 0 && !focus.template.isBossEvent()) {
                        mob.count_meterial--;
                        LeaveItemMap.leave_item_by_type7(map, mob.getIdMaterial(), p, mob.ID, p.ID);
                    } else if (focus.template.isBossEvent() && Util.random(10) < 2 && mob.count_meterial > 0 && Manager.gI().event != -1) {
                        short[] item7 = new short[]{8, 9, 10, 11, 2, 3, 13};
                        short[] item4 = new short[]{184, 185, 186, 187, 188, 189, 190, 191, 52, 259, 205, 206, 207};
                        mob.count_meterial--;
                        if (Util.random(5) == 2) {
                            LeaveItemMap.leave_item_by_type7(map, item7[Util.random(item7.length)], p, mob.ID, p.ID);
                        } else {
                            LeaveItemMap.leave_item_by_type4(map, item4[Util.random(item4.length)], p, mob.ID, p.ID);
                        }
                    }
                    if (!mob.top_dame.containsKey(p.name)) {
                        mob.top_dame.put(p.name, dame);
                    } else {
                        long dame_boss = dame + mob.top_dame.get(p.name);
                        mob.top_dame.put(p.name, dame_boss);
                    }
                } else {
                    if (focus.hp < focus.hp_max / 2 && mob.count_meterial > 0 && Util.random(5) < 3) {
                        mob.count_meterial--;
                        LeaveItemMap.leave_item_by_type7(map, (short) Util.random(130, 133), p, mob.ID, p.ID);
                    }
                }
                focus.hp += Util.random((int) (dame / 8), (int) (dame / 10));
            }

            if (focus.hp <= 0) {
                // Kiểm tra nếu map là Chiem Thanh
                if (map != null && map.isMapChiemThanh()) {
                    ChiemThanhManager.Obj_Die(map, ObjAtk, focus);
                }

                // Kiểm tra nếu focus là mob và p không phải null, myclan không phải null
                if (focus.isMob() && p != null && p.myclan != null) {
                    p.myclan.update_exp(20);
                }

                // Gọi SetDie cho focus
                if (focus != null) {
                    focus.SetDie(map, ObjAtk);
                }

                // Kiểm tra nếu không phải mob đi buôn, không phải player, và map là chiến trường
                if (focus != null && !focus.isMobDiBuon() && !focus.isPlayer() && map != null && map.isMapChienTruong()
                        && focus.template != null && focus.template.mob_id >= 89 && focus.template.mob_id <= 92 && p != null) {
                    // Xử lý sự kiện liên quan đến chiến trường
                    p.update_point_arena(100);
                    String[] name = focus.template.name.split(" ");
                    String name_mob = name.length == 2 ? name[1] : name[1] + " " + name[2];
                    Manager.gI().chatKTGprocess(p.name + " đã đánh sập nhà chính của làng " + name_mob);
                    ChienTruong.gI().update_house_die(focus.template.mob_id);
                    ChienTruong.Obj_Die(map, ObjAtk, focus);
                }

                // Nếu focus là player, xử lý tử vong của player
                if (focus.isPlayer()) {
                    if (map != null) {
                        MapService.Player_Die(map, focus, ObjAtk, true);
                    }
                } else {
                    // Nếu focus không phải player, xử lý tử vong của đối tượng chính
                    if (map != null) {
                        MapService.MainObj_Die(map, null, focus, true);
                    }
                }
            }

            if (ObjAtk.isPlayer() && (focus.isPlayer() || focus.get_TypeObj() == 0)) {
                MapService.Fire_Player(map, p.conn, temp_skill, focus.ID, (int) dame, focus.hp, ListEf, stas);
            } else if (ObjAtk.isPlayer() && focus.get_TypeObj() == 1) {
                if (map.isMapChienTruong()) {
                    MapService.Fire_Mob(map, p.conn, temp_skill, focus.ID, (int) dame, focus.hp, ListEf, focus.template.mob_id, stas);
                } else {
                    MapService.Fire_Mob(map, p.conn, temp_skill, focus.ID, (int) dame, focus.hp, ListEf, 0, stas);
                }
            } else if (ObjAtk.get_TypeObj() == 1 && focus.isPlayer()) {
                MapService.mob_fire(map, (Mob_in_map) ObjAtk, (Player) focus, (int) dame);
            } else if (ObjAtk.get_TypeObj() == 0 && focus.isPlayer()) {
                MapService.MainObj_Fire_Player(map, (Player) focus, ObjAtk, idxSkill, (int) dame, ListEf);
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Pet Attack       ...">
            if (ObjAtk.isPlayer()) {
                if (!focus.isdie && p.pet_follow_id != -1) {
                    Pet my_pet = p.get_pet_follow();
                    if (my_pet != null && my_pet.grown > 0 && p.getEffTinhTu(EffTemplate.NGU_DAN) == null) {
                        int a1 = 0;
                        int a2 = 1;
                        for (OptionPet temp1 : my_pet.op) {
                            if (temp1.maxdame > 0) {
                                a1 = my_pet.getParam(temp1.id);
                                a2 = my_pet.getMaxDame(temp1.id);
                                break;
                            }
                        }
                        int dame_pet = Util.random(a1, Math.max(a2, a1 + 1));
                        if (dame_pet <= 0) {
                            dame_pet = 1;
                        }
                        if (((focus.hp - dame_pet) > 0) && (p.pet_atk_speed < System.currentTimeMillis()) && (a2 > 1)) {
                            if (my_pet.get_id() == 3269) {
                                if (focus.isMob()) {
                                    int vangjoin = Util.random(2000, 3000);
                                    p.update_vang(vangjoin, "Nhận %s vàng từ đại bàng");
                                } else if (focus.isPlayer()) {

                                }
                            }
                            if (focus.isPlayer() && my_pet.get_id() == 4617 && Util.random(10000) < my_pet.getParam((byte) 97)) {
                                //  Player p_focus = (Player) focus;
                                p_focus.add_EffDefault(StrucEff.VET_THUONG_SAU, 15000, my_pet.getParam((byte) 98));
                                Service.send_notice_nobox_white(p_focus.conn, "Bạn bị vết thương sâu");
                            }
                            if (focus.isPlayer() && my_pet.get_id() == 4626 && Util.random(10000) < my_pet.getParam((byte) 67)) {
                                //    Player p_focus = (Player) focus;
                                p_focus.add_EffDefault(StrucEff.TE_CONG, 1, 10000);
                                Service.send_notice_nobox_white(p_focus.conn, "Bạn bị tê cóng");
                            } else if (focus.isPlayer() && Util.random(1000) < my_pet.getParam((byte) 85)) {
                                focus.add_EffDefault(StrucEff.KhienMaThuat, 0, my_pet.getParam((byte) 86));
                                Eff_special_skill.send_eff_Medal(p, 86, my_pet.getParam((byte) 86));
                            } else if (Util.random(1000) < 10 && my_pet.getParam((byte) 45) > 0) {
                                MapService.add_eff_stun(map, ObjAtk, focus, my_pet.getParam((byte) 45) / 1000, 6);
                            } else if (Util.random(1000) < 900 && my_pet.getParam((byte) 46) > 0) {
                                MapService.add_eff_stun(map, ObjAtk, focus, my_pet.getParam((byte) 46) / 1000, 7);
                            }
                            p.pet_atk_speed = System.currentTimeMillis() + 5000L;
                            Message m = new Message(84);
//                            m.writer().writeByte(my_pet.getSkillPet());
                            m.writer().writeByte(0);
                            m.writer().writeShort(p.ID);
                            m.writer().writeByte(focus.get_TypeObj());
                            m.writer().writeByte(1);
                            m.writer().writeShort(focus.ID);
                            m.writer().writeInt(dame_pet);
                            focus.hp -= dame_pet;
                            m.writer().writeInt(focus.hp);
                            m.writer().writeInt(p.hp);
                            m.writer().writeInt(p.body.get_HpMax());
                            p.conn.addmsg(m);
                            m.cleanup();
                        }
                    }
                }
            }//</editor-fold>
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int get_param_view_in4(int type) {
        return switch (type) {
            case 0, 1, 2, 3, 4, 5, 6 -> get_DameProp(type);
            case 7, 8, 9, 10, 11, 12, 13 -> get_PercentDameProp(type);
            case 14 -> get_DefBase();
            case 15 -> get_PercentDefBase();
            case 33 -> getCrit();
            case 34 -> get_Miss(false);
            case 35 -> get_PhanDame();
            case 36 -> getPierce();
            case 16, 17, 18, 19, 20, 21, 27, 28, 22, 155, 185, 186, 192, 191, 128 ->
                    (total_item_param(type) + total_skill_param(type));
            default -> total_item_param(type);
        };
    }

    public static void send_eff_to_object(Player p, MainObject object, int id_eff) {
        try {
            byte[] data = Util.loadfile("data/part_char/imgver/x" + p.conn.zoomlv + "/Data/" + (112 + "_" + id_eff));
            Message m = new Message(-49);
            m.writer().writeByte(2);
            m.writer().writeShort(data.length);
            m.writer().write(data);
            m.writer().writeByte(0); // b3
            m.writer().writeByte(0); // b4
            m.writer().writeByte(id_eff); // num4
            m.writer().writeShort(object.ID); // iD2
            m.writer().writeByte(object.get_TypeObj()); // tem2
            m.writer().writeByte(0); // b5
            m.writer().writeInt(500); // num5

            MapService.send_msg_player_inside(p.map, p, m, true);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void upHP(Map map, MainObject focus, int hp) throws IOException {
        Message m_hp = new Message(32);
        m_hp.writer().writeByte(1);
        m_hp.writer().writeShort(focus.ID);
        m_hp.writer().writeShort(-1);
        m_hp.writer().writeByte(0);
        m_hp.writer().writeInt(focus.get_HpMax());
        m_hp.writer().writeInt(focus.hp);
        m_hp.writer().writeInt(hp);
        for (int i = 0; i < map.players.size(); i++) {
            map.players.get(i).conn.addmsg(m_hp);
        }
        m_hp.cleanup();
    }

    public int total_skill_param(int id) {
        return 0;
    }

    public int total_item_param(int id) {
        return 0;
    }

    public void update(Map map) {

    }

    public void setHp(MainObject atk, int dame) {
        this.hp -= dame;
    }

    public long getExp(int level, int dame) {
        return 0;
    }
}
