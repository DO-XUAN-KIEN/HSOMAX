package Game.template;

import Game.template.MainObject;
import Game.client.Player;
import Game.core.Manager;
import Game.core.Service;
import Game.io.Message;
import Game.map.ItemMap;
import Game.map.Map;
import Game.map.MapService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyNuong extends MainObject {

    public Player p;
    public short type;
    public List<Short> item;
    public int id_map;
    public long time_move;
    public long time_skill;
    public int speed;
    public String owner;
    public int power;


    public MyNuong(int type, int index_mob, int x, int y, int id_map, String name, Player p) {
        this.type = (short) type;
        this.ID = Short.toUnsignedInt((short) index_mob);
        this.item = new ArrayList<>();
        this.x = (short) x;
        this.y = (short) y;
        this.time_move = System.currentTimeMillis() + 1000L;
        this.id_map = id_map;
        this.name = name;
        this.owner = name;
        this.power = 1000;
        this.hp = 400000;
        this.hp_max = 400000;
        this.time_skill = System.currentTimeMillis() + 15_000L;
        this.speed = 1;
        this.level = p.level;
        this.p = p;
    }

    @Override
    public boolean isMobDiBuon() {
        return true;
    }

    @Override
    public void SetDie(Map map, MainObject mainAtk) {
        if (isdie) return;
        try {
            if (this.hp <= 0) {
                this.isdie = true;
                this.hp = 0;
                Pet_di_buon_manager.remove(this.name);
                this.p.mynuong = null;
                for (int j = 0; j < this.item.size(); j++) {
                    ItemMap it_leave = new ItemMap();
                    it_leave.id_item = (short) this.item.get(j);
                    it_leave.color = (byte) 0;
                    it_leave.quantity = 1;
                    it_leave.category = 3;
                    it_leave.idmaster = (short) mainAtk.ID;
                    it_leave.op = new ArrayList<>();
                    it_leave.time_exist = System.currentTimeMillis() + 60_000L;
                    it_leave.time_pick = System.currentTimeMillis() + 1_500L;
                    map.add_item_map_leave(map, (Player)this.p, it_leave, this.ID);
                }
            }
        } catch (Exception e) {
        }
    }
}
