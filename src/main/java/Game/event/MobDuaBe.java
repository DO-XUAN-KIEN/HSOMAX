package Game.event;

import Game.client.Player;
import Game.core.Manager;
import Game.core.Service;
import Game.core.Util;
import Game.io.Message;
import Game.io.Session;
import Game.map.Map;
import Game.template.DuaBe_manager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MobDuaBe {

    // Danh sách mob hiện có (Dùng CopyOnWriteArrayList để an toàn luồng)
    private static final CopyOnWriteArrayList<Mob_duabe> mobTrees = new CopyOnWriteArrayList<>();

    // Thời điểm spawn lần cuối (ms)
    public static long timeCreate = 0L;

    // Counter để đảm bảo index không trùng
    private static short nextIndex = 30000;

    // --- CẤU HÌNH THỜI GIAN ---
    private static final long TIME_LIMIT_MS = 5 * 60 * 1000; // 5 phút (300,000 ms)

    public static Mob_duabe getMob(int idx) {
        for (Mob_duabe m : mobTrees) {
            if (m.index == idx) return m;
        }
        return null;
    }

    /**
     * Tìm mob mà người chơi đang dắt (Dùng cho hàm updatePlayer)
     */
    public static Mob_duabe getMobByPlayerID(int playerId) {
        for (Mob_duabe m : mobTrees) {
            if (m.Owner != null && m.Owner.ID == playerId) {
                return m;
            }
        }
        return null;
    }

    /**
     * Spawn 3 mob trên 3 bản đồ khác nhau
     */
    public static void taoMob() {
        long time = System.currentTimeMillis();
        timeCreate = time;

        short[] id_map_random = new short[]{
                9, 26, 27, 16, 13, 12, 17, 39, 40, 44, 20,
                45, 41, 51, 52, 65, 73, 76, 94, 97, 98
        };

        try {
            // Lọc map hợp lệ
            List<Short> validMapIds = new ArrayList<>();
            for (short id : id_map_random) {
                Map m = Map.get_id(id);
                if (m != null && !m.ismaplang && !m.showhs && m.typemap == 0
                        && !Map.is_map_cant_save_site(m.map_id)
                        && m.map_id != 49 && m.map_id != 81) {
                    validMapIds.add(id);
                }
            }

            if (validMapIds.size() < 3) {
                System.out.println("MobDuaBe: Khong du map hop le de spawn.");
                return;
            }

            // Chọn ngẫu nhiên 3 map
            Set<Short> selectedMapIds = new HashSet<>();
            while (selectedMapIds.size() < 3) {
                int ri = Util.random(0, validMapIds.size() - 1);
                selectedMapIds.add(validMapIds.get(ri));
            }

            // Spawn
            for (short mapId : selectedMapIds) {
                Map[] maps = Map.get_map_by_id(mapId);
                if (maps == null || maps.length == 0) continue;

                int randIndex = Util.random(0, maps.length - 1);
                Map m = maps[randIndex];
                if (m == null) continue;

                short index;
                synchronized (MobDuaBe.class) {
                    index = nextIndex++;
                    if (nextIndex >= Short.MAX_VALUE - 1) nextIndex = 30000;
                }

                Mob_duabe mob = new Mob_duabe(m, index);
                // Tọa độ random theo kích thước map
                mob.x = (short) (Util.random(m.mapW) * 24);
                mob.y = (short) (Util.random(m.mapH) * 24);

                mobTrees.add(mob);

                System.out.println("Spawn Mob Dua Be: Map " + m.name + " (" + (m.zone_id + 1) + ") idx:" + index);
            }

            Manager.gI().chatKTGprocess("Đứa bé đã xuất hiện ở 3 nơi khác nhau, hãy nhanh chân đi kiếm nào!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void taoMobAD() {
        long time = System.currentTimeMillis();
        timeCreate = time;

        // Danh sách các map có thể spawn
        short[] id_map_random = new short[]{
                9, 26, 27, 16, 13, 12, 17, 39, 40, 44, 20,
                45, 41, 51, 52, 65, 73, 76, 94, 97, 98
        };

        try {
            // 1. Lọc ra danh sách các ID Map hợp lệ
            List<Short> validMapIds = new ArrayList<>();
            for (short id : id_map_random) {
                Map m = Map.get_id(id);
                if (m != null && !m.ismaplang && !m.showhs && m.typemap == 0
                        && !Map.is_map_cant_save_site(m.map_id)
                        && m.map_id != 49 && m.map_id != 81) {
                    validMapIds.add(id);
                }
            }

            if (validMapIds.isEmpty()) {
                System.out.println("MobDuaBe: Khong co map hop le de spawn.");
                return;
            }

            // 2. Chạy vòng lặp 40 lần để tạo 40 con
            int soLuongMob = 40;

            for (int i = 0; i < soLuongMob; i++) {
                // Lấy ngẫu nhiên 1 map ID trong danh sách hợp lệ (Cho phép trùng map ID)
                int indexRandom = Util.random(0, validMapIds.size() - 1);
                short mapId = validMapIds.get(indexRandom);

                // Lấy danh sách các khu (zone) của map đó
                Map[] maps = Map.get_map_by_id(mapId);
                if (maps == null || maps.length == 0) continue;

                // Chọn ngẫu nhiên 1 khu (zone) để spawn
                int randIndex = Util.random(0, maps.length - 1);
                Map m = maps[randIndex];
                if (m == null) continue;

                // Tạo index mob (đồng bộ để không trùng ID mob)
                short index;
                synchronized (MobDuaBe.class) {
                    index = nextIndex++;
                    if (nextIndex >= Short.MAX_VALUE - 1) nextIndex = 30000;
                }

                Mob_duabe mob = new Mob_duabe(m, index);

                // Random tọa độ
                mob.x = (short) (Util.random(m.mapW) * 24);
                mob.y = (short) (Util.random(m.mapH) * 24);

                mobTrees.add(mob);

                System.out.println("Spawn Mob (" + (i + 1) + "/" + soLuongMob + "): " + m.name + " khu " + (m.zone_id + 1));
            }

            Manager.gI().chatKTGprocess("40 Đứa bé đã xuất hiện rải rác khắp nơi, hãy nhanh chân đi kiếm nào!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Spawn Mob tại vị trí cụ thể (Dùng khi mob chết/rớt lại)
     */
    public static void TaoMobDie(Map map, int x, int y) {
        try {
            if (map == null) return;

            short index;
            synchronized (MobDuaBe.class) {
                index = nextIndex++;
                if (nextIndex >= Short.MAX_VALUE - 1) nextIndex = 30000;
            }

            Mob_duabe mob = new Mob_duabe(map, index);
            mob.x = (short) x;
            mob.y = (short) y;
            mobTrees.add(mob);

            // Gửi thông báo có mob mới cho map đó (nếu cần)
            // Code cũ của bạn chưa có đoạn gửi packet mob mới ở đây,
            // nếu client không thấy mob thì cần thêm logic gửi packet AddMob.

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    //      CLASS MOB (LOGIC CHÍNH Ở ĐÂY)
    // ==========================================
    public static class Mob_duabe {
        public short index;
        public String name = "";
        public String nameOwner = "";
        public Player Owner;
        public Map map;
        public short x, y;

        // --- BIẾN THỜI GIAN MỚI ---
        public long pickUpTime = 0;     // Thời điểm bắt đầu dắt
        public long lastNotifyTime = 0; // Thời điểm thông báo gần nhất

        public Mob_duabe(Map map, short idx) {
            this.map = map;
            this.index = idx;
            this.x = 0;
            this.y = 0;
            if (map != null) {
                map.mobDuaBe.add(this);
            }
        }

        public void SendMob(Session conn) throws IOException {
            Message m = new Message(4);
            m.writer().writeByte(1);
            m.writer().writeShort(175);
            m.writer().writeShort(index);
            m.writer().writeShort(x);
            m.writer().writeShort(y);
            m.writer().writeByte(-1);
            conn.addmsg(m);
            m.cleanup();

            m = new Message(7); // Info mob
            m.writer().writeShort(index);
            m.writer().writeByte(40);
            m.writer().writeShort(x);
            m.writer().writeShort(y);
            m.writer().writeInt(1); // HP
            m.writer().writeInt(1); // Max HP
            m.writer().writeByte(0);
            m.writer().writeInt(-2);
            m.writer().writeShort(-1);

            // Info Owner
            m.writer().writeByte(1);
            m.writer().writeByte(1);
            m.writer().writeByte(0);
            m.writer().writeUTF(nameOwner);
            m.writer().writeLong(-11111);
            m.writer().writeByte(0);
            conn.addmsg(m);
            m.cleanup();
        }

        public void setOwner(Player p) throws IOException {
            if (p == null) return;
            this.nameOwner = p.name;
            this.Owner = p;
            // [QUAN TRỌNG] Khởi tạo thời gian bắt đầu
            this.pickUpTime = System.currentTimeMillis();
            this.lastNotifyTime = 0; // Reset để thông báo ngay lập tức hoặc đợi 30s tùy logic
            Service.send_notice_box(p.conn, "Bạn bắt đầu dắt đứa bé.\nThời gian giới hạn: 5 phút!");
            MobLeave(); // Xóa mob khỏi map (để người khác không thấy/không nhặt được nữa)
        }

        public void MobLeave() throws IOException {
            try {
                Message m = new Message(17);
                m.writer().writeShort(Owner == null ? -1 : Owner.ID);
                m.writer().writeShort(index);

                if (map != null) {
                    for (Player player : map.players) {
                        if (player != null && player.conn != null && player.conn.connected) {
                            player.conn.addmsg(m);
                        }
                    }
                    map.mobDuaBe.remove(this);
                }
                m.cleanup();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Xử lý khi hết thời gian 5 phút
         */
        public void failEvent() {
            try {
                if (Owner != null && Owner.conn != null) {
                    Service.send_notice_box(Owner.conn, "Đã hết thời gian dắt đứa bé!");

                    // Logic bổ sung: Xóa hiệu ứng dắt trên người (nếu có)
                    // Reset trạng thái
                    Owner = null;
                    nameOwner = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void ClearMob() {
        synchronized (mobTrees) {
            for (Mob_duabe mob : mobTrees) {
                try {
                    if (mob == null) continue;
                    Message m = new Message(17);
                    m.writer().writeShort(-1);
                    m.writer().writeShort(mob.index);
                    if (mob.map != null) {
                        for (Player pl : mob.map.players) {
                            if (pl != null && pl.conn != null && pl.conn.connected) {
                                pl.conn.addmsg(m);
                            }
                        }
                        mob.map.mobDuaBe.remove(mob);
                    }
                    m.cleanup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mobTrees.clear();
        }
    }

    /**
     * Hàm Update chạy định kỳ (Spawn lại mob sau 8h)
     */
    public static void Update() {
        try {
            long now = System.currentTimeMillis();
            long timeDelay = 8L * 60 * 60 * 1000; // 8 Tiếng

            if (mobTrees.isEmpty()) {
                taoMob();
                return;
            }

            // Logic reset toàn bộ mob sau thời gian định sẵn
            if (now - timeCreate >= timeDelay) {
                ClearMob();
                taoMob();
            }

            // LƯU Ý: Logic kiểm tra timeout của từng người chơi đã được chuyển sang
            // hàm updatePlayerDuaBe(Player p) để tối ưu hóa và hiện chat chính xác.

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * [MỚI] Hàm cập nhật cho từng người chơi
     * Đặt hàm này trong Player.update()
     */
    public static void updatePlayerDuaBe(Player p) {
        try {
            // Tìm xem người này có đang dắt con mob nào không
            Mob_duabe mob = getMobByPlayerID(p.ID);
            if (p.duabe == null){
                return;
            }
            if (mob != null) {
                long now = System.currentTimeMillis();
                long endTime = mob.pickUpTime + TIME_LIMIT_MS;

                // 1. Kiểm tra hết giờ
                if (now > endTime) {
                    mob.failEvent();
                    mobTrees.remove(mob); // Xóa khỏi danh sách quản lý
                    p.duabe = null;
                    DuaBe_manager.remove(p.duabe.name);
                    return;
                }

                // 2. Tính toán thời gian còn lại
                int timeRemaining = (int) ((endTime - now) / 1000);
                int min = timeRemaining / 60;
                int sec = timeRemaining % 60;

                // 3. Thông báo ở giây thứ 0 hoặc 30 (Message 27 - Chat đầu)
                if (sec == 0 || sec == 30) {
                    // Chặn spam (chỉ gửi 1 lần mỗi giây)
                    if (now - mob.lastNotifyTime > 1000) {
                        String chatMsg;
                        if (min > 0) {
                            chatMsg = "Thời gian dắt bé còn: " + min + " phút " + sec + " giây.";
                        } else {
                            chatMsg = "Thời gian dắt bé còn: " + sec + " giây.";
                        }

                        // Gửi Message 27
                        Message m = new Message(27);
                        m.writer().writeShort(p.ID);
                        m.writer().writeByte(0);
                        m.writer().writeUTF(chatMsg);
                        p.conn.addmsg(m);
                        m.cleanup();

                        mob.lastNotifyTime = now;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}