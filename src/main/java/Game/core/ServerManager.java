package Game.core;

import Game.activities.ChiemThanhManager;
import Game.Helps._Time;
import Game.client.Clan;
import Game.event.MobDuaBe;
import Game.event.MobMy;
import com.sun.management.OperatingSystemMXBean;
import Game.event.EventManager;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

import Game.activities.ChienTruong;
import Game.activities.KingCup;
import Game.activities.KingCupManager;
import Game.io.Session;

import java.io.File;
import java.io.FileOutputStream;

import Game.map.Map;

public class ServerManager implements Runnable {

    private static ServerManager instance;
    private final Thread mythread;
    private Thread server_live;
    private boolean running;
    private ServerSocket server;
    private final long time;
    public long time_l;
    private long time2;
    private byte checkError;
    private static final int HOUR_START_KING_CUP = 18;
    private static final int MIN_START_KING_CUP = 30;

    public ServerManager() {
        this.time = System.currentTimeMillis();
        this.time_l = System.currentTimeMillis() + 60_000L;
        this.mythread = new Thread(this);
    }

    public static ServerManager gI() {
        if (instance == null) {
            instance = new ServerManager();
        }
        return instance;
    }

    public void init() {
        Manager.gI().init();
        ChiemThanhManager.init();
        server_update_right_time();
        Admin.setTop();
        this.running = true;
        this.mythread.start();
        this.server_live.start();
        new Thread(() -> {
            while (this.running) {
                if (!server_live.isAlive() || !mythread.isAlive()) {
                    System.out.println("+++++++++++++Error alive-----------" + server_live.isAlive() + "   " + mythread.isAlive() + "   " + server.isClosed());
                }
                try {
                    Thread.sleep(5_000L);
                } catch (InterruptedException ex) {
                    System.out.println("core.ServerManager.init()");

                }
                if (System.currentTimeMillis() - time2 > 60_000 && this.running) {
                    try {
                        System.out.println("++++++++++++++++++++reset update+++++++++++++++");
                        time2 = System.currentTimeMillis();
                        if (server_live.isAlive()) {
                            server_live.interrupt();
                            server_update_right_time();
                            this.server_live.start();
                            File f = new File("ERROR/check.txt");
                            f.getParentFile().mkdirs();
                            if (!f.exists()) {
                                if (!f.createNewFile()) {
                                    System.out.println("Tạo file " + "ERROR/check.txt xảy ra lỗi");
                                    continue;
                                }
                            }
                            // Lưu mảng byte vào file
                            FileOutputStream fileOutputStream = new FileOutputStream("ERROR/check.txt");
                            fileOutputStream.write(("Lỗi ở đoạn : " + checkError).getBytes("utf-8"));
                            fileOutputStream.close();
                            System.out.println("Đã lưu mảng byte vào file ERROR/check.txt");
                        }
                    } catch (Exception eee) {
                    }
                }
            }
            System.out.println("-----------GAME EXIT 3----------");
        }).start();
    }

   public void running() {
    Calendar now;
    int millis;
    long lastSaveTime = 0;

    while (ServerManager.gI().running) {
        try {
            now = Calendar.getInstance();
            int second = now.get(Calendar.SECOND);
            int minute = now.get(Calendar.MINUTE);
            int hour = now.get(Calendar.HOUR_OF_DAY);

            // === Reset BXH tuần ===
            if (now.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY && hour < 10
                    && (!Manager.gI().thanh_tich.isEmpty() || !Manager.gI().ty_phu.isEmpty()
                    || !Manager.gI().trieu_phu.isEmpty() || !Manager.gI().dai_gia.isEmpty())) {
                Manager.gI().thanh_tich.clear();
                Manager.gI().ty_phu.clear();
                Manager.gI().dai_gia.clear();
                Manager.gI().trieu_phu.clear();
            }
            if (hour == 20 && minute == 45 && !ChienTruong.running) {
                ChienTruong.running = true;
                ChienTruong.gI().open_register();
                Manager.gI().chatKTGprocess("Đăng ký chiến trường mở lúc 20:45! Chiến trường sẽ bắt đầu sau 45 phút nữa.");
            }
            if (ChienTruong.running) {
                ChienTruong.gI().update();
            }
            SaveData.process();
            millis = now.get(Calendar.MILLISECOND);
            long time_sleep = 1000 - millis;
            if (time_sleep > 0) {
                Thread.sleep(time_sleep);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



    public void run() {
        try {
            try {
                this.server = new ServerSocket(Manager.gI().server_port);
                System.out.println("Started in " + (System.currentTimeMillis() - this.time) + "s");
                System.out.println();
                System.out.println("LISTEN PORT " + Manager.gI().server_port + "...");
            } catch (Exception ee) {
                ee.printStackTrace();
                System.exit(0);
            }
            while (this.running) {
                try {
                    Socket client = this.server.accept();
                    if (this.running) {
                        Session ss = new Session(client);
                        ss.init();
                        if (Manager.gI().event == 4) {
                            MobMy.taoMob();
                            System.out.println("Tao mi thanh cong");
                        }
                    }
                } catch (Exception eee) {
                    eee.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("-----------GAME EXIT 2----------");
    }

    private void server_update_right_time() {
        this.server_live = new Thread(() -> {
            Calendar now;
            int DayOfWeek;
            SaveData.process();
            while (this.running) {
                try {
                    time2 = System.currentTimeMillis();
                    checkError = 1;
                    now = Calendar.getInstance();
                    Manager.hour = now.get(Calendar.HOUR_OF_DAY);
                    Manager.minute = now.get(Calendar.MINUTE);
                    Manager.second = now.get(Calendar.SECOND);
                    Manager.millisecond = now.get(Calendar.MILLISECOND);
                    DayOfWeek = now.get(Calendar.DAY_OF_WEEK);
                    if (Manager.hour == HOUR_START_KING_CUP && Manager.minute == MIN_START_KING_CUP
                            && Manager.second == 0 && KingCup.kingCup == null) {
                        if (KingCupManager.TURN_KING_CUP < KingCupManager.MAX_TURN) {
                            KingCup.start();
                            KingCupManager.TURN_KING_CUP++;
                            KingCupManager.updateTurn();
                            Manager.gI().chatKTGprocess("Bắt đầu lôi đài");
                        }
                        if (KingCupManager.TURN_KING_CUP >= KingCupManager.MAX_TURN && KingCup.kingCup == null) {
                            KingCupManager.TURN_KING_CUP++;
                            KingCupManager.updateTurn();
                        }

                    }
                    if (KingCupManager.TURN_KING_CUP >= KingCupManager.DAY_OFF + KingCupManager.MAX_TURN) {
                        KingCupManager.TURN_KING_CUP = 0;
                        KingCupManager.updateTurn();
                    }
                    if (KingCup.kingCup == null && Manager.second % 10 == 0 && KingCupManager.TURN_KING_CUP < KingCupManager.MAX_TURN) {
                        Map[] tapKet = Map.get_map_by_id(100);
                        int h = (HOUR_START_KING_CUP - Manager.hour + 24) % 24;
                        int m = (MIN_START_KING_CUP - Manager.minute + 59) % 60;
                        if (HOUR_START_KING_CUP == Manager.hour && MIN_START_KING_CUP <= Manager.minute) {
                            h = 23;
                            if (m == 0) {
                                m = 59;
                            }
                        }
                        int s = 60 - Manager.second;
                        for (Map map : tapKet) {
                            Service.npcChat(map, -82, String.format("Lôi đài bắt đầu đợt %s sau: %s giờ %s phút %s giây.", KingCupManager.TURN_KING_CUP + 1, h, m, s));
                        }
                        if (h == 0 && m == 1 && s == 0) {
                            Manager.gI().chatKTGprocess("Lôi đài sẽ bắt đầu sau 1 phút");
                        }
                    }
                    if (KingCup.kingCup == null && Manager.second % 10 == 0 && KingCupManager.TURN_KING_CUP >= KingCupManager.MAX_TURN) {
                        Map[] tapKet = Map.get_map_by_id(100);
                        for (Map map : tapKet) {
                            Service.npcChat(map, -82, "Lôi đài đang trong thời gian nghỉ giữa 2 mùa");
                        }
                    }
                    if (KingCup.kingCup != null && KingCup.count < 10 && Manager.second % 9 == 0) {
                        Map[] tapKet = Map.get_map_by_id(100);
                        int time = (int) ((KingCup.NEXT_MATCHES - System.currentTimeMillis()) / 1000);
                        for (Map map : tapKet) {
                            Service.npcChat(map, -82, String.format("Trận đấu bắt đầu sau %s giây.", time));
                        }
                    }
                    checkError = 3;
                    if (Manager.second == 0 && Manager.minute == 0 && (Manager.hour == 8 || Manager.hour == 20)) {
                        Clan.camroibang = true;
                        Manager.gI().chatKTGprocess("Bắt đầu thời gian cấm rời bang(sáng 8h-9h). Tối(8h-11h)");
                    } else if (Manager.second == 0 && Manager.minute == 0 && (Manager.hour == 9 || Manager.hour == 23)) {
                        Clan.camroibang = false;
                        Manager.gI().chatKTGprocess("Thời gian cấm rời bang kết thúc");
                    }
                    checkError = 4;
                    if (Manager.second == 10) {
                        SessionManager.CheckBandWidth();
                    }
                    checkError = 5;
                    SessionManager.RemoveClient();
//                    if (Manager.second % 30 == 0) {
//                        SessionManager.RemoveClient();
//                    }
                    checkError = 7;
                    if (Manager.hour == 0 && Manager.minute == 0 && Manager.second == 1) {
                        Manager.gI().ip_create_char.clear();
                        for (Map[] map : Map.entrys) {
                            for (Map map0 : map) {
                                for (int i = 0; i < map0.players.size(); i++) {
                                    try {
                                        map0.players.get(i).change_new_date();
                                    } catch (Exception eee) {
                                        eee.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    checkError = 8;
                    if (Manager.gI().event == 4){
                        MobMy.Update();
                    }
                    MobDuaBe.Update();
                    if (Manager.gI().event == 0 || Manager.gI().event == 1) {
                        if (EventManager.eventManager == null) {
                            EventManager.eventManager = new EventManager();
                        }
                        if (Manager.hour == 15 && Manager.minute == 40 && Manager.second == 0) {
                            EventManager.eventManager.start();
                        }
                        if (Manager.second == 0) {
                            EventManager.update(1);
                        }
                    }
                    if ((DayOfWeek == 2 || DayOfWeek == 4 || DayOfWeek == 6) && Manager.hour >= 20) {
                        checkError = 16;
                        if (Manager.hour == 20 && Manager.minute == 45)
                            ChiemThanhManager.StartRegister();
                        else if (Manager.hour == 21 && Manager.minute == 30)
                            ChiemThanhManager.EndRegister();
                        checkError = 17;
                        ChiemThanhManager.update();
                    }
                    checkError = 9;
                    if (Manager.second == 0 && Manager.minute == 0 && (Manager.hour == 8 || Manager.hour == 20)) {
                        Manager.gI().chiem_mo.mo_open_atk();
                        Manager.gI().chatKTGprocess("Các mỏ tài nguyên đã hoạt động trở lại, các bang hãy đánh chiếm để tăng sức mạnh");
                    } else if (Manager.second == 0 && Manager.minute == 0 && (Manager.hour == 9 || Manager.hour == 21)) {
                        Manager.gI().chiem_mo.mo_close_atk();
                        Manager.gI().chatKTGprocess("Thời gian chiếm mỏ đã kết thúc");
                    }
                    checkError = 11;
                    if (Manager.minute % 5 == 0 && Manager.second == 4) {
                        Manager.gI().chiem_mo.harvest_all();
                    }
                    checkError = 14;
                    //
                    _Time.timeDay = _Time.GetTime();
                    checkError = 21;
                    long time_sleep = 1000 - Manager.millisecond;
                    if (time_sleep < 100) {
                        System.err.println("server time update process is overloading...");
                    }
                    Thread.sleep(time_sleep);
                    checkError = 22;
                    if ((Manager.hour >= 7 && Manager.hour <= 8) || (Manager.hour >= 19 && Manager.hour <= 20)) {
                        // --- KHỐI THÔNG BÁO 5 PHÚT ---
                        // Sửa lại phút: 4:54 (cách 4:59 đúng 5p) và 19:49 (cách 19:54 đúng 5p)
                        if ((Manager.hour == 7 && Manager.minute == 49 && Manager.second == 0)
                                || (Manager.hour == 19 && Manager.minute == 49 && Manager.second == 0)) {
                            System.out.println("Bảo trì sau 5p");
                            Manager.gI().chatKTGprocess("Server sẽ tự động bảo trì sau 5 phút nữa, vui lòng thoát game để tránh mất dữ liệu.");
                        }

                        // --- KHỐI THÔNG BÁO 2 PHÚT ---
                        // Sửa lại phút: 4:57 (cách 4:59 đúng 2p) và 19:52 (cách 19:54 đúng 2p)
                        if ((Manager.hour == 7 && Manager.minute == 52 && Manager.second == 0)
                                || (Manager.hour == 19 && Manager.minute == 52 && Manager.second == 0)) {
                            System.out.println("Bảo trì sau 2p");
                            Manager.gI().chatKTGprocess("Server sẽ tự động bảo trì sau 2 phút nữa, vui lòng thoát game để tránh mất dữ liệu.");
                        }

                        // --- KHỐI THỰC THI BẢO TRÌ ---
                        // Thời điểm tắt: 4:59 hoặc 19:54
                        if (!Manager.isMaintenance && // Kiểm tra chưa bảo trì thì mới chạy
                                ((Manager.hour == 7 && Manager.minute == 54 && Manager.second == 0)
                                        || (Manager.hour == 19 && Manager.minute == 54 && Manager.second == 0))) {

                            Manager.isMaintenance = true; // Đánh dấu đang bảo trì ngay lập tức để không lặp lại
                            Manager.gI().chatKTGprocess("Hệ thống bắt đầu bảo trì. Server sẽ khởi động lại sau 1 phút.");

                            // Chạy luồng tắt server
                            new Thread(() -> {
                                try {
                                    System.out.println("Stopping server...");

                                    // 1. Kick tất cả người chơi trước
                                    // Duyệt ngược là đúng rồi, nhưng thêm try-catch cho chắc
                                    if (Session.client_entry != null) {
                                        for (int k = Session.client_entry.size() - 1; k >= 0; k--) {
                                            try {
                                                if (Session.client_entry.get(k) != null) {
                                                    Session.client_entry.get(k).close();
                                                }
                                            } catch (Exception e) {
                                                System.out.println("Lỗi kick session: " + e.getMessage());
                                            }
                                        }
                                    }

                                    // 2. Lưu dữ liệu quan trọng
                                    System.out.println("Saving data...");
                                    SaveData.process();
                                    Manager.gI().close(); // Đóng kết nối database/socket server

                                    System.out.println("Server closed. Waiting to restart...");
                                    System.out.println("Đã kick và lưu data xong. Đang chờ 60s để ổn định hệ thống...");
                                    Thread.sleep(120000); // Chờ 2 phút
                                    // 4. Gọi lệnh mở lại server
                                    try {
                                        Service.openCmd("run.bat");
                                    } catch (Exception e) {
                                        System.out.println("Lỗi mở run.bat: " + e.getMessage());
                                    }

                                    // 5. Tắt hẳn tiến trình Java hiện tại
                                    System.exit(0);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        }
                    }
//                    if((Manager.hour >= 4 && Manager.hour <= 6) || (Manager.hour >= 19 && Manager.hour <= 20)) {
//                        if ((Manager.hour == 4 && Manager.minute == 55 && Manager.second == 0) || (Manager.hour == 19 && Manager.minute == 50 && Manager.second == 0)) {
//                            System.out.println("bảo trì sau 5p");
//                            Manager.gI().chatKTGprocess("Sever sẽ tự động bảo trì sau 5 phút nữa, hãy thoát ra trước khi bảo trì để tránh lỗi không mong muốn");
//                        }
//                        if ((Manager.hour == 4 && Manager.minute == 58 && Manager.second == 0) || (Manager.hour == 19 && Manager.minute == 53 && Manager.second == 0)) {
//                            System.out.println("bảo trì sau 2p");
//                            Manager.gI().chatKTGprocess("Sever sẽ tự động bảo trì sau 2 phút nữa, hãy thoát ra trước khi bảo trì để tránh lỗi không mong muốn");
//                        }
//                        if ((Manager.hour == 4 && Manager.minute == 59 && Manager.second == 0) || (Manager.hour == 19 && Manager.minute == 54 && Manager.second == 0)) {
//                            Manager.gI().chatKTGprocess("Sever sẽ tự động bảo trì sau 1 phút nữa, hãy thoát ra trước khi bảo trì để tránh lỗi không mong muốn");
//                            new Thread(() -> {
//                                while (this.running) {
//                                    System.out.println("Close server is processing....");
//                                    new Thread(() -> {
//                                        SaveData.process();
//                                        for (int k = Session.client_entry.size() - 1; k >= 0; k--) {
//                                            try {
//                                                Session.client_entry.get(k).close();
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                        Manager.gI().close();
//                                        try {
//                                            Thread.sleep(60000);
//                                            Service.openCmd("run.bat");
//                                        } catch (Exception e) {
//                                        }
//                                        System.exit(0);
//                                    }).start();
//                                    try {
//                                        Thread.sleep(30000L);
//                                    } catch (InterruptedException e) {
//                                    }
//                                }
//                            }).start();
//                        }
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("-----------GAME EXIT 1----------");
        });
    }

    public static int getMemory() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return (int) ((1.0 - osBean.getFreeMemorySize() * 1.0 / osBean.getTotalMemorySize()) * 100);
    }

    public void close() throws IOException {
        System.out.println("----------SERVER CLOSE----------");
        running = false;
        server_live.interrupt();
        server.close();
    }
}
