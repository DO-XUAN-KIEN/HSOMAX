package Game.core;

public class Start {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                if (SQL.is_connected) {
                    Manager.gI().close();
                    SQL.gI().close();
                    System.out.println("SERVER STOPPED!");
                }
            }
        }));
        try {
            javax.swing.SwingUtilities.invokeLater(() -> new Game.admin.AdminPanel());
        } catch (Throwable ignored) {
        }
        ServerManager.gI().init();
        ServerManager.gI().running();
    }
}
