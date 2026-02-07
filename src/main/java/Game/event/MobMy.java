package Game.event;

import Game.client.Player;
import Game.core.Manager;
import Game.core.Util;
import Game.io.Message;
import Game.io.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import Game.map.Map;

public class MobMy {
    private static final CopyOnWriteArrayList<Mob_My> mobTrees = new CopyOnWriteArrayList<>();
    public static long timeCreate;
    public static Mob_My getMob(int idx){
        for(Mob_My m : mobTrees){
            if(m.index == idx )
                return m;
        }
        return null;
    }
    public static void taoMob() {
        long time = System.currentTimeMillis();
        timeCreate = time;

        // Danh sách map cho phép spawn mob
        short[] id_random = new short[]{
                9, 26, 27, 16, 13, 12, 17, 39, 40, 44, 20,
                45, 41, 51, 52, 65, 73, 76, 94, 97, 98
        };

        try {
            // Danh sách map hợp lệ theo điều kiện
            List<Short> validMapIds = new ArrayList<>();
            for (short id : id_random) {
                Map m = Map.get_id(id); // Giả sử Map.get_id là hàm trả về đối tượng Map với ID tương ứng
                if (m != null && !m.ismaplang && !m.showhs && m.typemap == 0
                        && !Map.is_map_cant_save_site(m.map_id)
                        && m.map_id != 49 && m.map_id != 81) {
                    validMapIds.add(id);
                }
            }

            // Random số lượng map được chọn spawn mob
            int numberOfMaps = Util.random(5, Math.min(15, validMapIds.size()));
            Set<Short> selectedMapIds = new HashSet<>();
            while (selectedMapIds.size() < numberOfMaps) {
                int randomIndex = Util.random(0, validMapIds.size() - 1);
                selectedMapIds.add(validMapIds.get(randomIndex));
            }

            // Tạo mob ở các map đã chọn
            for (short mapId : selectedMapIds) {
                Map[] maps = Map.get_map_by_id(mapId);
                if (maps == null || maps.length == 0) continue;
                Map m = maps[Util.random(2,4)];
                if (m == null) continue;

                // Số Mob spawn ngẫu nhiên từ 1–3
                int mobCount = Util.random(1, 3);
                for (int j = 0; j < mobCount; j++) {
                    short index = (short) (30000 + mobTrees.size());
                    Mob_My plot = new Mob_My(m, index);
                    mobTrees.add(plot);
                  //  System.out.println("Spawn Mob tại map: " + m.name);
                }
            }

          //  Manager.gI().chatKTGprocess("Mỵ Nương đã xuất hiện, hãy nhanh chân đi kiếm nào!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void TaoMobDie(Map map, int x, int y) {
        try {
            if (map == null) {
                System.out.println("Map không hợp lệ!");
                return;
            }

            short index = (short) (30000 + mobTrees.size());
            Mob_My mob = new Mob_My(map, index);

            // Đặt vị trí Mob tại tọa độ hiện tại
            mob.x = (short) x;
            mob.y = (short) y;

            mobTrees.add(mob);

          //  System.out.println("Spawn Mob tại map: " + map.name + " tại vị trí x=" + x + ", y=" + y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Class MobFarm
    public static class Mob_My {

        public long timeUpdate;
        public short index;
        public String name = "";
        public String nameOwner = "";
        public Player Owner;
        public Map map;
        public short x, y;

        public Mob_My(Map map, short idx) {
            timeUpdate = System.currentTimeMillis();
            this.map = map;
            this.index = idx;
            x= (short)(Util.random(map.mapW)*24);
            y= (short)(Util.random(map.mapH)*24);
            map.mobMyNuong.add(this);
        }


        public void SendMob(Session conn) throws IOException {
            Message m = new Message(4);
            m.writer().writeByte(1);
            m.writer().writeShort(176);
            m.writer().writeShort(index);
            m.writer().writeShort(x);
            m.writer().writeShort(y);
            m.writer().writeByte(-1);
            conn.addmsg(m);
            m.cleanup();
            m = new Message(7);
            m.writer().writeShort(index);
            m.writer().writeByte(40);
            m.writer().writeShort(x);
            m.writer().writeShort(y);
            m.writer().writeInt(1);
            m.writer().writeInt(1);
            m.writer().writeByte(0);
            m.writer().writeInt(-2);
            m.writer().writeShort(-1);

            m.writer().writeByte(1);
            m.writer().writeByte(1);
            m.writer().writeByte(0);
            m.writer().writeUTF(nameOwner);
            m.writer().writeLong(-11111);
            m.writer().writeByte(0);
            conn.addmsg(m);
            m.cleanup();
        }
        public void setOwner(Player p)throws IOException{
            if(p==null)return;
            nameOwner = p.name;
            Owner = p;
            MobLeave();
        }
        public void MobLeave() throws IOException {
            Message m = new Message(17);
            m.writer().writeShort(Owner == null ? -1 : Owner.ID);
            m.writer().writeShort(index);
            for (Player player : map.players) {
                if (player != null && player.conn != null && player.conn.connected) {
                    player.conn.addmsg(m);
                }
            }
            m.cleanup();
            map.mobMyNuong.remove(this);
        }
    }
    public static void ClearMob(){
        synchronized(mobTrees){
            for(Mob_My mob:mobTrees)
            {
                try{
                    mob.MobLeave();
                }
                catch(Exception e){}
            }
            mobTrees.clear();
        }
    }
    public static void Update(){
        try{
            long time = System.currentTimeMillis();
            if(time - timeCreate > 1000 * 60 * 28 && !mobTrees.isEmpty())
                ClearMob();
            if(time - timeCreate > 1000 * 60  * Util.random(30,40))
                taoMob();
        }catch(Exception e){}
    }
}