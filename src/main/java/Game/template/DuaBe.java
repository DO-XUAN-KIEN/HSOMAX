package Game.template;

import Game.client.Player;
import Game.map.ItemMap;
import Game.map.Map;

import java.util.ArrayList;
import java.util.List;

public class DuaBe extends MainObject {

    public Player p;
    public short type;
    public List<Short> item;
    public int id_map;
    public long time_move;
    public long time_skill;
    public int speed;
    public String owner;


    public DuaBe(int type, int index_mob, int x, int y, int id_map, String name, Player p) {
        this.type = (short) type;
        this.ID = Short.toUnsignedInt((short) index_mob);
        this.item = new ArrayList<>();
        this.x = (short) x;
        this.y = (short) y;
        this.time_move = System.currentTimeMillis() + 1000L;
        this.id_map = id_map;
        this.name = name;
        this.owner = name;
        this.hp = 1;
        this.hp_max = 1;
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
            }
        } catch (Exception e) {
        }
    }
}
