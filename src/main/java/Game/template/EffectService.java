/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Game.template;

/**
 *
 * @author Tới
 */
import java.util.concurrent.*;
import Game.client.MessageHandler;
import Game.client.Player;
import Game.core.Util;
import Game.io.Message;
import Game.io.Session;  // sửa theo đúng package bạn dùng
import Game.template.MainObject ; // nếu MainObject nằm trong package Game.server
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import Game.client.Player;

public class EffectService {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4); // hoặc 1 nếu bạn muốn đơn luồng

    public static void send_eff_auto(Session conn, List<MainObject> objects, int id_eff, int durationMs, int intervalMs) {
        final long startTime = System.currentTimeMillis();
        final byte zoom = conn.zoomlv;

        // Cache dữ liệu hiệu ứng
        final byte[] data;
        try {
            data = Util.loadfile("data/part_char/imgver/x" + zoom + "/Data/" + (111 + "_" + id_eff));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Runnable task = () -> {
            try {
                long now = System.currentTimeMillis();
                if (now - startTime >= durationMs) return;

                Message m = new Message(-49);
                m.writer().writeByte(4);
                m.writer().writeShort(data.length);
                m.writer().write(data);
                m.writer().writeShort(id_eff);
                m.writer().writeByte(objects.size());

                for (MainObject object : objects) {
                    m.writer().writeShort(object.ID);
                    m.writer().writeByte(object.get_TypeObj());
                    m.writer().writeShort(object.x);
                    m.writer().writeShort(object.y);
                }

                for (Player p : conn.p.map.players) {
                    if (p != null && p.conn != null) {
                        p.conn.addmsg(m);
                    }
                }
                m.cleanup();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // Đặt lịch chạy hiệu ứng theo chu kỳ
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, 0, intervalMs, TimeUnit.MILLISECONDS);

        // Tự hủy sau durationMs
        scheduler.schedule(() -> future.cancel(false), durationMs, TimeUnit.MILLISECONDS);
    }

    public static void send_eff_auto(Session conn, List<MainObject> objects, int id_eff) throws IOException {
        // copy nguyên hàm send_eff_auto của bạn vào đây
    }

    public static void send_eff_auto_repeat(Session conn, List<MainObject> objects, int id_eff, int durationMillis) {
        int repeatInterval = 1000; // 1 giây
        int repeatCount = durationMillis / repeatInterval;

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;

            @Override
            public void run() {
                try {
                    if (count >= repeatCount) {
                        this.cancel();
                        return;
                    }
                    send_eff_auto(conn, objects, id_eff);
                    count++;
                } catch (IOException e) {
                    e.printStackTrace();
                    this.cancel();
                }
            }
        }, 0, repeatInterval);
    }
    public static void send_eff_near_player(Session conn, Player p, int id_eff, int durationMs, int intervalMs, int offsetX, int offsetY) {
    new Thread(() -> {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < durationMs) {
            try {
                byte[] data = Util.loadfile("data/msg_eff/" + id_eff);
                Message m = new Message(-49);
                m.writer().writeByte(1);
                m.writer().writeShort(data.length);
                m.writer().write(data);
                m.writer().writeByte(0); // b3
                m.writer().writeByte(0); // b4
                m.writer().writeByte(id_eff); // id eff
                m.writer().writeShort(p.x + offsetX);
                m.writer().writeShort(p.y + offsetY);
                m.writer().writeByte(0); // b6
                m.writer().writeByte(0); // b7
                m.writer().writeShort(-1); // idnpc
                m.writer().writeShort(0); // loop
                m.writer().writeByte(2); // b8

                for (Player pl : p.map.players) {
                    if (pl != null) {
                        pl.conn.addmsg(m);
                    }
                }

                m.cleanup();
                Thread.sleep(intervalMs);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }).start();
}
  public static ScheduledFuture<?> send_eff_auto_infinite(Session conn, List<MainObject> objects, int id_eff, int intervalMs) {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    return scheduler.scheduleAtFixedRate(() -> {
        try {
            byte[] data = Util.loadfile("data/part_char/imgver/x" + conn.zoomlv + "/Data/" + (111 + "_" + id_eff));
            Message m = new Message(-49);
            m.writer().writeByte(4);
            m.writer().writeShort((short) data.length);
            m.writer().write(data);
            m.writer().writeShort((short) id_eff);
            m.writer().writeByte((byte) objects.size());

            for (MainObject object : objects) {
                int effX = object.x + 10;
                int effY = object.y;
                m.writer().writeShort((short) object.ID);
                m.writer().writeByte((byte) object.get_TypeObj());
                m.writer().writeShort((short) effX);
                m.writer().writeShort((short) effY);
            }

            for (Player p : conn.p.map.players) {
                if (p != null && p.conn != null) {
                    p.conn.addmsg(m);
                }
            }
            m.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }, 0, intervalMs, TimeUnit.MILLISECONDS);
}





}
