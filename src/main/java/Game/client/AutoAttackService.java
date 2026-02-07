package Game.client;



import java.io.IOException;
import Game.io.Session;
import Game.io.Message;

public class AutoAttackService {
    public static void toggleAutoAttack(Session conn) throws IOException {
        conn.autoAttack = !conn.autoAttack;
        sendAutoAttack(conn, conn.autoAttack);
    }

    public static void sendAutoAttack(Session conn, boolean on) throws IOException {
        Message m = new Message(-108);
        m.writer().writeByte(5);
        m.writer().writeByte(on ? 0 : 1); // 0 = bật, 1 = tắt
        conn.addmsg(m);
        m.cleanup();
    }
}
