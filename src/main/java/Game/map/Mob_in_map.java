package Game.map;

import java.util.ArrayList;
import java.util.List;

import Game.Boss.BossServer;
import Game.client.Player;
import Game.core.Manager;
import Game.core.Service;
import Game.core.Util;
import Game.activities.ChiemThanhManager;
import Game.template.MainObject;
import Game.io.Message;
import Game.template.EffTemplate;
import Game.template.StrucEff;

import java.io.IOException;
import java.util.HashMap;

public class Mob_in_map extends MainObject {
    public boolean blockRespawn = false;

    public final static HashMap<Integer, Mob_in_map> ENTRY = new HashMap<>();
    public int time_refresh = 3;
    public boolean is_boss;
    public long time_back;
    public final List<Player> list_fight = new ArrayList<>();
    public long time_fight;
    public boolean is_boss_active;
    public int timeBossRecive = 1000 * 60 * 60 * 2;
    public int count_meterial = 30;
    public final HashMap<String, Long> top_dame = new HashMap<>();
    public Map map;

    public void Reset() {
        hp = get_HpMax();
        isdie = false;
        synchronized (list_fight) {
            list_fight.clear();
        }
        synchronized (top_dame) {
            top_dame.clear();
        }
    }

    public void Set_isBoss(boolean isBoss) {
        is_boss = isBoss;
    }

    @Override
    public boolean isBoss() {
        return is_boss;
    }

    @Override
    public boolean isHouse() {
        return template.mob_id >= 89 && template.mob_id <= 92;
    }

    @Override
    public boolean isMob() {
        return true;
    }

    @Override
    public int get_DameBase() {
        if (dame <= 0) {
            dame = level * 75;
        }

        int dmob = Util.random((int) (this.dame * 0.95), (int) (this.dame * 1.05));
        if (this.level > 30 && this.level <= 50) {
            dmob = (dmob * 13) / 10;
        } else if (this.level > 50 && this.level <= 70) {
            dmob = (dmob * 16) / 10;
        } else if (this.level > 70 && this.level <= 100) {
            dmob = (dmob * 19) / 10;
        } else if (this.level > 100 && this.level <= 600) {
            dmob = (dmob * 21) / 10;
        }
        if (this.is_boss) {
            dmob = (int) (dmob * this.level * 0.004);
        }
        if (this.color_name != 0 && (this.template.mob_id < 89 || this.template.mob_id > 92)) {
            dmob *= 2;
        }
        return (int) (dmob * 4 / 5);
    }

    @Override
    public int get_HpMax() {
        int hpm = hp_max;
        if (!this.isBoss() && this.map != null && this.map.luathieng != null
                && this.map.luathieng.isPointInCircle(this.x, this.y)) {
            hpm *= 2;
        }
        if (get_EffDefault(StrucEff.NOI_TAI_BANG) != null) {
            hpm = hpm / 5 * 4;
            if (hp > hpm) {
                hp = hpm;
            }
        }
        return hpm;
    }

    @Override
    public int get_Miss(boolean giam_ne) {
        if (getEffTinhTu(EffTemplate.BIEN_DANG) != null) {
            return 0;
        }
        return 800;
    }

    @Override
    public void SetDie(Map map, MainObject mainAtk) throws IOException {
        // Kiểm tra xem mob đã chết chưa
        if (isdie) {
            return;
        }

        hp = 0;  // Set HP = 0 để đánh dấu mob đã chết
        isdie = true;  // Đánh dấu mob đã chết

        Mob_in_map mob = this;

        // Kiểm tra xem đối tượng mainAtk có phải là Player không
        if (mainAtk == null || !mainAtk.isPlayer()) {
            return;
        }

        Player p = (Player) mainAtk; // Chuyển mainAtk về Player

        // Xử lý nếu mob bị giết bởi Player
        if (mainAtk.isPlayer()) {
            // Kiểm tra liệu Player có đang thực hiện hành động "hieuchien" (hiệu chiến)
            if (p.hieuchien > 0 && Math.abs(p.level - mob.level) <= 5) {
                p.hieuchien--; // Giảm giá trị hieuchien
            }

            // Nếu mob là mob đặc biệt, thực hiện hành động đặc biệt
            if (mob.template.mob_id == 152) {
                ChiemThanhManager.SetOwner(p); // Gán quyền sở hữu cho Player
            }

            // Xử lý điểm hoạt động nếu Player là chủ sở hữu (isOwner)
            if (p.isOwner && !p.isSquire && Math.abs(mob.level - p.level) < 10) {
                int point_activity = 10 + mob.level - p.level; // Tính điểm hoạt động
                p.point_activity += point_activity; // Thêm điểm hoạt động cho Player
            }

            // Kiểm tra và cập nhật nhiệm vụ hàng ngày của Player
            if (p.quest_daily != null && p.quest_daily[0] != -1 && p.quest_daily[2] < p.quest_daily[3] && p.quest_daily[0] == mob.template.mob_id) {
                p.quest_daily[2] += 1; // Cập nhật số lượng mob đã tiêu diệt
                Service.send_notice_nobox_white(p.conn, "Nhiệm vụ hàng ngày " + p.quest_daily[2] + "/" + p.quest_daily[3]);
            }

            p.checkQuest(mob.template.mob_id, (byte) 1); // Kiểm tra nhiệm vụ của Player
        }

        boolean check_mob_roi_ngoc_kham = mob.template.mob_id >= 167 && mob.template.mob_id <= 172;

        // Nếu là Boss, xử lý riêng
        if (mob.isBoss()) {
            if (!map.isMapChienTruong()) {
                // Xử lý khi Boss chết ngoài bản đồ chiến trường
                map.BossDie(mob);
                long time_refresh_boss = System.currentTimeMillis() + 6 * 60 * 60 * 1000L; // Thời gian refresh Boss
                BossServer.setTimeRefresh(map.map_id, map.zone_id, time_refresh_boss);

                String p_name = "";
                long top_damage = 0;

                // Tìm người chơi gây sát thương nhiều nhất
                for (java.util.Map.Entry<String, Long> en : mob.top_dame.entrySet()) {
                    if (en.getValue() > top_damage) {
                        top_damage = en.getValue();
                        p_name = en.getKey();
                    }
                }

                mob.isdie = true; // Đánh dấu Boss đã chết

                if (!Map.is_map_cant_save_site(mob.map_id)) {
                    // Nếu không phải map không thể lưu, gửi thông báo
                    Manager.gI().chatKTGprocess(mainAtk.name + " đã tiêu diệt " + mob.template.name);
                    Manager.gI().chatKTGprocess(p_name + " đã nhận quà Top 1 sát thương đánh " + mob.template.name);

                    Player player_top_dame = Map.get_player_by_name(p_name);
                    if (player_top_dame != null) {
                        if (Util.random(0,500)< 5){
                            p.update_token_all(2);
                        }
                        // Thả item cho người chơi gây sát thương top 1
                        LeaveItemMap.leave_item_boss(map, mob, player_top_dame);
                        LeaveItemMap.leave_item_3(map, mob, player_top_dame);
                    }
                }
                // Thả item cho người chơi đã giết Boss
                if (mainAtk.isPlayer()) {
                    if (Util.random(0,500)< 5){
                        p.update_token_all(1);
                    }
                    LeaveItemMap.leave_item_boss(map, mob, p);
                    LeaveItemMap.leave_item_3(map, mob, p);
                }
            } else {
                // Xử lý khi Boss chết trong bản đồ chiến trường
                Manager.gI().chatKTGprocess(mainAtk.name + " đã tiêu diệt " + mob.template.name);
                p.update_point_arena(50); // Thêm điểm Arena cho Player
                mob.isdie = true; // Đánh dấu Boss đã chết

                // Thả item cho Player
                if (mainAtk.isPlayer()) {
                    LeaveItemMap.leave_item_boss(map, mob, p);
                    LeaveItemMap.leave_item_3(map, mob, p);
                }
            }
        } else {
            // Xử lý nếu mob không phải Boss
            mob.time_back = System.currentTimeMillis() + (mob.time_refresh * 1000L) - 1000L; // Thời gian hồi sinh mob

            // Thả item cho Player nếu mob không phải Boss
            if (mainAtk.isPlayer() && Math.abs(mob.level - mainAtk.level) <= 5 && !check_mob_roi_ngoc_kham) {
                int percent = 20;
                int pnt = 0;
                if (p.id_lantern == 173 || p.id_lantern == 210 || p.id_lantern == 264){
                    pnt = 30;
                }
                if (p.item.wear[23] != null) {
                    Service.Cong_Diem_Diet_Quai(p);
                }
                if (map.isMapLangPhuSuong()) {
                    if (percent + pnt > Util.random(0, 300)) {
                        LeaveItemMap.leave_vang(map, mob, p);
                    } else if (percent > Util.random(0, 30)) {
                        LeaveItemMap.leave_item_by_type7(map, (short) Util.random(481, 485), p, mob.ID);
                    } else if (percent > Util.random(0, 30)) {
                        LeaveItemMap.leave_item_by_type7(map, (short) Util.random(472, 480), p, mob.ID);
                    } else if (percent > Util.random(0, 10)) {
                        LeaveItemMap.leave_item_by_type7(map, (short) Util.random(0, 2), p, mob.ID);
                    }

                    if (Manager.gI().event == 4 && Util.random(100) < 100) {
                        LeaveItemMap.leave_item_by_type4(map, (short) Util.random(137, 140), p, mob.ID);
                    }
                    if (Manager.gI().event == 5 && Util.random(100) < 100) {
                        LeaveItemMap.leave_item_by_type4(map, (short) Util.random(329, 333), p, mob.ID);
                    }
                    if (Manager.gI().event == 1 && Util.random(100) < 100) {
                        LeaveItemMap.leave_item_by_type4(map, (short) Util.random(118, 123), p, mob.ID);
                    }
                    if (Manager.gI().event == 3 && Util.random(100) < 100) {
                        short[] nl = new short[]{304, 306};
                        short id = nl[Util.random(0, 2)];
                        LeaveItemMap.leave_item_by_type4(map, id, p, mob.ID);
                    }
                    if (Manager.gI().event != -1 && 30 > Util.random(0, 100) && Math.abs(mob.level - mainAtk.level) <= 5) {
                        LeaveItemMap.leave_item_event(map, mob, p);
                    }
                } else {
                    if (percent > Util.random(0, 300)) {
                        LeaveItemMap.leave_item_3(map, mob, p);
                    } else if (percent > Util.random(0, 300) && zone_id == 1 && !Map.is_map_not_zone2(map_id)
                            && p.get_EffDefault(-127) != null) {
                        if (Util.random(0, 20) < 5) {
                            LeaveItemMap.leave_item_by_type7(map, (short) Util.random(116, 126), p, mob.ID);
                        }
                        if (Util.random(0, 100) < 10) {
                            LeaveItemMap.leave_item_by_type7(map, (short) 13, p, mob.ID);
                        }
                        if (Util.random(0, 2222) < 10){
                            p.update_token_all(1);
                        }
                    } else if (percent > Util.random(0, 20)) {
                        LeaveItemMap.leave_material(map, mob, p);
                    } else if (percent > Util.random(0, 10)) {
                        LeaveItemMap.leave_item_7(map, mob, p);
                    } else if (percent + pnt > Util.random(0, 100)) {
                        LeaveItemMap.leave_vang(map, mob, p);
                    }

                    if (Manager.gI().event == 4 && Util.random(100) < 100) {
                        LeaveItemMap.leave_item_by_type4(map, (short) Util.random(137, 140), p, mob.ID);
                    }
                    if (Manager.gI().event == 5 && Util.random(100) < 100) {
                        LeaveItemMap.leave_item_by_type4(map, (short) Util.random(329, 333), p, mob.ID);
                    }
                    if (Manager.gI().event == 1 && Util.random(100) < 100) {
                        LeaveItemMap.leave_item_by_type4(map, (short) Util.random(118, 123), p, mob.ID);
                    }
                    if (Manager.gI().event == 3 && Util.random(100) < 100) {
                        short[] nl = new short[]{304, 306};
                        short id = nl[Util.random(0, 2)];
                        LeaveItemMap.leave_item_by_type4(map, id, p, mob.ID);
                    }

                    if (percent > Util.random(0, 300)) {
                        LeaveItemMap.leave_item_4(map, mob, p);
                    }

                    if (Manager.gI().event != -1 && 30 > Util.random(0, 100) && Math.abs(mob.level - mainAtk.level) <= 5) {
                        LeaveItemMap.leave_item_event(map, mob, p);
                    }
                }
            }

            // Nếu mob có liên quan đến Ngọc Khảm, thả item đặc biệt
            if (check_mob_roi_ngoc_kham) {
                LeaveItemMap.leave_material_ngockham(map, mob, p);
                if (Util.random(0, 10) < 3) {
                    short nltt = (short) (Util.random(10) * 4 + Util.random(419, 421));
                    LeaveItemMap.leave_item_by_type7(map, nltt, p, mob.ID);
                }
            }

            // Nếu mob có tên màu, giảm số lượng mob siêu cấp
            if (mob.color_name != 0) {
                map.num_mob_super--;
            }
        }
    }


    @Override
    public void update(Map map) {
        try {
            this.updateEff();
            EffTemplate eff = get_EffDefault(StrucEff.NOI_TAI_DOC);
            if (!isdie && eff != null && eff.o_atk != null) {
                this.hp -= eff.param;
                upHP(map, this, -eff.param);
                this.setHp(eff.o_atk, eff.param);
            }
            if (this.isdie && this.ishs && this.time_back < System.currentTimeMillis() && !is_boss && !this.isHouse()) {
                if (this.MainEff != null) {
                    this.MainEff.clear();
                }
                this.isdie = false;
                this.Reset();
                this.hp = this.get_HpMax();
                if (this.isBoss()) {
                    this.color_name = 3;
                } else if (5 > Util.random(200) && map.num_mob_super < 2 && this.level > 50) {
                    this.color_name = (new byte[]{1, 2, 4, 5})[Util.random(4)];
                    map.num_mob_super++;
                } else {
                    this.color_name = 0;
                }
                for (int j = 0; j < map.players.size(); j++) {
                    Player pp = map.players.get(j);
                    if ((Math.abs(pp.x - this.x) < 200) && (Math.abs(pp.y - this.y) < 200)) {
                        if (!pp.other_mob_inside.containsKey(this.ID)) {
                            pp.other_mob_inside.put(this.ID, true);
                        }
                        if (pp.other_mob_inside.get(this.ID)) {
                            Message mm = new Message(4);
                            mm.writer().writeByte(1);
                            mm.writer().writeShort(this.template.mob_id);
                            mm.writer().writeShort(this.ID);
                            mm.writer().writeShort(this.x);
                            mm.writer().writeShort(this.y);
                            mm.writer().writeByte(-1);
                            pp.conn.addmsg(mm);
                            mm.cleanup();
                            pp.other_mob_inside.replace(this.ID, true, false);
                        } else {
                            Service.mob_in4(pp, this.ID);
                        }
                    }
                }
            } else if (!this.isdie && this.isATK && this.time_fight < System.currentTimeMillis()) {
                // Logic quét tìm địch nếu danh sách trống
                if (this.list_fight.isEmpty()) {
                    for (Player p0 : map.players) {
                        // Nếu người chơi còn sống, không tàng hình và trong tầm 200
                        if (!p0.isdie && p0.get_EffMe_Kham(StrucEff.TangHinh) == null
                                && Math.abs(this.x - p0.x) < 200 && Math.abs(this.y - p0.y) < 200) {
                            this.list_fight.add(p0);
                        }
                    }
                }

                // Logic tấn công
                if ((this.template.mob_id == 151 || this.template.mob_id == 152 || this.template.mob_id == 154)) {
                    // Mob trụ chính đánh tất cả
                    for (Player p0 : this.list_fight) {
                        if (p0 != null && !p0.isdie && p0.map.map_id == this.map_id && p0.map.zone_id == this.zone_id
                                && Math.abs(this.x - p0.x) < 250 && Math.abs(this.y - p0.y) < 250) {
                            MainObject.MainAttack(map, this, p0, 0, null, 2);
                        }
                    }
                    this.time_fight = System.currentTimeMillis() + 3500L;
                } else if (!this.list_fight.isEmpty()) {
                    // Mob thường đánh ngẫu nhiên 1 người
                    Player p0 = this.list_fight.get(Util.random(this.list_fight.size()));
                    if (p0 != null && !p0.isdie && p0.map.map_id == this.map_id && p0.map.zone_id == this.zone_id) {
                        if (Math.abs(this.x - p0.x) < 250 && Math.abs(this.y - p0.y) < 250) {
                            if (this.time_fight < System.currentTimeMillis()) {
                                this.time_fight = System.currentTimeMillis() + 1500L; // Tốc độ đánh
                                MainObject.MainAttack(map, this, p0, 0, null, 2);
                            }
                        } else {
                            this.list_fight.remove(p0);
                        }
                    } else {
                        this.list_fight.remove(p0);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public short getIdMaterial() {
        switch (this.template.mob_id) {
            case 103 -> {
                return 136;
            }
            case 104 -> {
                return 137;
            }
            case 101 -> {
                return 138;
            }
            case 84 -> {
                return 139;
            }
            case 105 -> {
                return 140;
            }
            case 83 -> {
                return 141;
            }
            case 106 -> {
                return (short) Util.random(142, 144);
            }
            case 149 -> {
                return (short) Util.random(145, 147);
            }
            case 155 -> {
                return (short) Util.random(136, 146);
            }
            case 174 -> {
                return (short) Util.random(142, 144);
            }
            case 195 -> {
                return (short) Util.random(145, 147);
            }
            case 218 -> {
                return (short) Util.random(136, 146);
            }
            case 219 -> {
                return (short) Util.random(145, 147);
            }
            case 220 -> {
                return (short) Util.random(136, 146);
            }
            default -> {
            }
        }
        return -1;
    }

    public void add_Eff(int id, int param, int time) {
        this.add_EffDefault(id, param, System.currentTimeMillis() + time);
    }

    @Override
    public void setHp(MainObject atk, int dame) {
        try {
            if (atk.isPlayer()) {
                Player p = atk.isPlayer() ? (Player) atk : null;
                if (p == null) {
                    return;
                }
                //<editor-fold defaultstate="collapsed" desc="Tính exp       ...">
                if (this.isMobDungeon()
                        && atk.isPlayer()) {
                    long expup = this.getExp(p.level, dame);
                    p.update_Exp(expup, true);
                } else {
                    long expup = this.getExp(p.level, dame);
                    if (p.hieuchien > 0) {
                        expup /= 2;
                    }
                    if (p.party != null) {
                        for (int i = 0; i < p.party.get_mems().size(); i++) {
                            Player pm = p.party.get_mems().get(i);
                            if (pm.ID != p.ID && (Math.abs(pm.level - p.level) < 10) && pm.map.map_id == p.map.map_id
                                    && pm.map.zone_id == p.map.zone_id) {
                                pm.update_Exp((expup / 10), true);
                            }
                        }
                    }
                    if (Manager.gI().time_x2_server > System.currentTimeMillis()) {
                        expup *= 2;
                    }
                    p.update_Exp(expup, true);
                }
                //</editor-fold>    Tính exp
            }
            this.hp -= (int) dame;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getExp(int p_level, int dame) {
        int lv = this.level - p_level;
        long exp_default = 2000;
        long exp_up;

        if (dame <= 0) {
            return 3;
        }

        // Giữ điều kiện chênh lệch level < 7
        if (Math.abs(lv) < 7) {
            exp_default = exp_default + (lv * 300L);
        } else {
            exp_default = 3;
        }

        // Bonus exp nếu quái đứng trong vùng đặc biệt
        if (!this.isBoss() && this.map != null && this.map.luathieng != null
                && this.map.luathieng.isPointInCircle(this.x, this.y)) {
            exp_default *= 2;
        }

        // Nếu dame >= máu còn lại, tính theo hp_max (one hit vẫn được full exp)
        if (dame >= this.hp) {
            exp_up = this.hp_max * exp_default / this.hp_max; // => exp_default
        } else {
            exp_up = dame * exp_default / this.hp_max;
        }

        // Exp tối thiểu = 3
        if (exp_up <= 0) {
            exp_up = 3;
        }

        return exp_up;
    }
}
