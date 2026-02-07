package Game.template;

import java.util.ArrayList;
import java.util.List;

import Game.map.Map;

public class Mob {
    public static final List<Mob> entry = new ArrayList<>();
    public short mob_id;
    public String name;
    public short level;
    public int hpmax;
    public byte typemove;
    public Map map;
    public boolean isDie;

    public boolean is_boss;
    public boolean isBossEvent() {
        return mob_id == 174;
        
    }
  public boolean isBossServer() {
        return mob_id == 173 || mob_id == 195 || mob_id == 196;
    }

}
