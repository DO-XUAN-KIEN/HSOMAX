/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Game.ai;


 
 import Game.client.MessageHandler;

import Game.client.Item;
import Game.client.Player;
import Game.core.Service;
import Game.core.Util;
import Game.io.Message;
import Game.map.Map;
import Game.template.Item3;
import Game.template.MainObject;

import java.io.IOException;

public class Bot extends MainObject {
    public Map map;
    public byte head;
    public byte eye;
    public byte hair;
    public MainObject target;
    private long time_atk;
    private long time_remove;
    private Item item;
    private short[] fashion;
    private short id_weapon;
    private short id_hair;
    private short wing;

    public Bot(int ID, Player p) {
        this.ID = ID;
        this.x = p.x;
        this.y = p.y;
        this.head = p.head;
        this.eye = p.eye;
        this.level = p.level;
        this.name = "cướp - " + p.name;
        this.clazz = p.clazz;
        this.hp = this.hp_max = p.get_HpMax();
        this.map = p.map;
        this.isdie = false;
        this.dame = p.get_DameBase() + 50000;
        this.def = p.get_DefBase();
        this.item = p.item;
        this.fashion = p.fashion;
        this.id_hair = Service.get_id_hair(p);
        this.id_weapon = Service.get_id_weapon(p);
        this.wing = Service.get_id_wing(p);
        this.time_remove = System.currentTimeMillis() + 300000L;
    }

    public void send_info(Player p) {
        try {
            int dem = 0;
            for (int i = 0; i < 11; i++) {
                if (i != 0 && i != 1 && i != 6 && i != 7 && i != 10) {
                    continue;
                }
                if (this.item.wear[i] != null) {
                    dem++;
                }
            }
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
            m.writer().writeByte(0);
            m.writer().writeShort(0);
            m.writer().writeByte(dem);
            //
            for (int i = 0; i < this.item.wear.length; i++) {
                if (i != 0 && i != 1 && i != 6 && i != 7 && i != 10) {
                    continue;
                }
                Item3 temp = this.item.wear[i];
                if (temp != null) {
                    m.writer().writeByte(temp.type);

                    if (i == 10 && this.item.wear[14] != null && (this.item.wear[14].id >= 4638 && this.item.wear[14].id <= 4648)) {
                        m.writer().writeByte(this.item.wear[14].part);
                    } else {
                        m.writer().writeByte(temp.part);
                    }
                    m.writer().writeByte(3);
                    m.writer().writeShort(-1);
                    m.writer().writeShort(-1);
                    m.writer().writeShort(-1);
                    m.writer().writeShort(-1); // eff
                }
            }
            m.writer().writeShort(-1);
            m.writer().writeByte(-1);
            m.writer().writeByte(this.fashion.length);
            for (int i = 0; i < this.fashion.length; i++) {
                if (p.conn.version >= 280) {
                    m.writer().writeShort(this.fashion[i]);
                } else {
                    m.writer().writeByte(this.fashion[i]);
                }
            }
            //
            m.writer().writeShort(-1);
            m.writer().writeByte(-1);
            m.writer().writeBoolean(false);
            m.writer().writeByte(1);
            m.writer().writeByte(0);
            m.writer().writeShort(-1); // mat na
            m.writer().writeByte(1); // paint mat na trc sau
            m.writer().writeShort(-1); // phi phong
            m.writer().writeShort(this.id_weapon); // weapon
            m.writer().writeShort(-1);
            m.writer().writeShort(this.id_hair); // hair
            m.writer().writeShort(this.wing); // wing
            m.writer().writeShort(-1); // phi phong
            m.writer().writeShort(-1); // body
            m.writer().writeShort(-1); // leg
            m.writer().writeShort(-1); // bienhinh
            p.conn.addmsg(m);
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() throws IOException {
        if (!this.isdie) {
            if (this.target != null) {
                if (this.target.map_id == this.map_id && this.zone_id == this.target.zone_id
                        && !this.target.isdie && this.time_atk < System.currentTimeMillis()) {
                    this.time_atk = System.currentTimeMillis() + 2000L;
                    MainObject.MainAttack(this.map, this, this.target, Util.random(new int[]{0, 1, 2, 5, 6, 9, 10, 13, 14, 18}), null, 1);
                } else {
                    this.target = null;
                }
            } else {
                for (int i = 0; i < map.players.size(); i++) {
                    Player p0 = map.players.get(i);
                    if (p0 != null && !p0.isdie) {
                        this.target = p0;
                    }
                }
            }
            if (time_remove < System.currentTimeMillis()) {
                this.isdie = true;
            }
        }
    }
    @Override
    public int get_TypeObj() {
        return 0;
    }
    public boolean isBot() {
        return true;
    }
    @Override
    public void SetDie(Map map, MainObject mainAtk) {
        if (this.hp < 0) {
            this.isdie = true;
            map.bots.remove(this);
            remove();
        }
    }
    public void remove() {
        try {
            Message m3 = new Message(8);
            m3.writer().writeShort(this.ID);
            for (int i = 0; i < map.players.size(); i++) {
                if (map.players.get(i) != null) {
                    map.players.get(i).conn.addmsg(m3);
                }
            }
            m3.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
