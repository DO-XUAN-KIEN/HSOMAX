package Game.admin;

import Game.core.Manager;
import Game.core.SaveData;
import Game.core.ServerManager;
import Game.core.Service;
import Game.io.Session;
import Game.core.Util;

import java.io.File;
import java.util.concurrent.TimeUnit;
import com.sun.management.OperatingSystemMXBean;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import Game.template.Item3;
import Game.template.Item47;
import Game.template.ItemTemplate3;
import Game.template.ItemTemplate4;
import Game.template.ItemTemplate7;
import Game.client.Player;


import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DriverManager;

public class AdminPanel extends WindowAdapter implements ActionListener {

    private static final Color BG_TOP = new Color(18, 16, 28);
    private static final Color BG_BOTTOM = new Color(32, 22, 48);
    private static final Color NEON_PINK = new Color(255, 115, 230);
    private static final Color NEON_PURPLE = new Color(141, 122, 255);
    private static final Color NEON_CYAN = new Color(120, 220, 255);
    private static final Color NEON_GOLD = new Color(255, 210, 130);

    private static final Color CARD_BG = new Color(32, 24, 44, 220);
    private static final Color CARD_STROKE = new Color(255, 255, 255, 28);
    private static final Color TEXT_PRIMARY = new Color(244, 242, 255);
    private static final Color TEXT_MUTED = new Color(195, 188, 218);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font MONO_FONT = new Font("Consolas", Font.PLAIN, 13);

    // High-contrast cho dialog
    private static final Color DIALOG_BG = new Color(26, 20, 38, 245);
    private static final Color FIELD_BG = new Color(24, 22, 34);
    private static final Color FIELD_FG = new Color(245, 244, 255);
    private static final Color FIELD_BD = new Color(120, 110, 170);
    private static final Color FIELD_BD_F = new Color(180, 160, 255);
    private static final Font DIALOG_FONT = BODY_FONT.deriveFont(14f);

    private JFrame frame;
    private JTextPane logPane;
    private NeonProgress memBar;
    private NeonProgress cpuBar;
    private JLabel onlineLabel;
    private JLabel threadLabel;

    // --- Auto backup state ---
    private javax.swing.Timer autoBackupTimer;
    private final AtomicBoolean isBackingUp = new AtomicBoolean(false);
    private final AtomicInteger backupsDone = new AtomicInteger(0);
    private int autoBackupIntervalMs = 1 * 30 * 60_000; // mặc định 60 giây
    private long nextBackupAtMs = -1;          // thời điểm kế tiếp

    private final ThreadMXBean THREAD_MX = ManagementFactory.getThreadMXBean();

    // Singleton dialog (modeless)
    private SendGoldDialog dlgGold;
    private SendGemDialog dlgGem;
    private BuffLevelDialog dlgLevel;
    private SendItemDialog dlgItem;
    private GiftcodePanel giftcodePanel;
    private ItemsLookupPanel itemsLookupPanel;
    private BuffSkillDialog dlgBuffSkill;
    private BuffPotentialDialog dlgBuffPotential;
   private BuffTongnapDialog dlgBuffTongnap; 

    public AdminPanel() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        SwingUtilities.invokeLater(this::buildUI);
    }

    private void buildUI() {
        frame = new JFrame("Menu service");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(this);
        frame.setSize(1000, 680);
        frame.setLocationRelativeTo(null);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        frame.setContentPane(root);

        JPanel header = buildHeader();
        root.add(header, BorderLayout.NORTH);

        JScrollPane leftScroll = new JScrollPane(buildActions());
        styleScroll(leftScroll);
        leftScroll.setPreferredSize(new Dimension(520, 0));
        root.add(leftScroll, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout(14, 14));
        center.setOpaque(false);
        center.add(buildStatsCard(), BorderLayout.NORTH);
        center.add(buildLogCard(), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        startAutoBackup();
        new javax.swing.Timer(1000, e -> updateInfo()).start();
        frame.setVisible(true);
    }

    private JPanel buildHeader() {
        GlassCard header = new GlassCard(20);
        header.setLayout(new BorderLayout(10, 0));
        header.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("⚡ Menu service ");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        onlineLabel = createPillLabel("Online: 0");
        threadLabel = createPillLabel("Threads: 0");

        right.add(new GlassPill(onlineLabel));
        right.add(new GlassPill(threadLabel));

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

   private JPanel buildActions() {
    String[] labels = {
        "🛠 Bảo trì",
        "💾 Lưu Dữ Liệu",
        "🚪 Kích All Player",
        "📢 Thông Báo",
        "👥 Player Online",
        "🧠 Tối ưu Memory",
        "🧵 Thread Dump",
        "🎒 Gửi Item",
        "💎 Gửi Ngọc",
        "🪙 Gửi Vàng",
        "⤴ Buff Level",
        "Tạo giftcode",
        "🔎 Tra cứu vật phẩm",
        "🛡 Chống DDoS",
        "🗄 Sao lưu / Auto Backup",
        "🗄 Buff tiềm năng",
         "🗄 Buff kĩ năng",
        "🗄 Buff tổng nạp",
        
    };
    String[] commands = {
        "Maintenance",
        "SaveData",
        "KickAll",
        "Broadcast",
        "OnlineList",
        "OptimizeMemory",
        "ThreadDump",
        "SendItem",
        "SendGem",
        "SendGold",
        "BuffLevel",
        "GIFT_CODE",
        "LookupItems",
        "DDoSGuard",
        "BackupCenter",
        "BuffPotential",
        "BuffSkill",
        "BuffTongNap",
    };

    JPanel actionsWrap = new JPanel(new BorderLayout());
    actionsWrap.setOpaque(false);

    JLabel groupTitle = new JLabel("Bảng điều khiển");
    groupTitle.setForeground(TEXT_MUTED);
    groupTitle.setBorder(new EmptyBorder(8, 8, 8, 8));
    actionsWrap.add(groupTitle, BorderLayout.NORTH);

    JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
    grid.setOpaque(false);

    for (int i = 0; i < labels.length; i++) {
        NeonButton b = new NeonButton(labels[i]);
        b.setActionCommand(commands[i]);
        b.addActionListener(this);
        grid.add(b);
    }

    GlassCard card = new GlassCard(20);
    card.setBorder(new EmptyBorder(16, 16, 16, 16));
    card.setLayout(new BorderLayout());
    card.add(grid, BorderLayout.CENTER);

    actionsWrap.add(card, BorderLayout.CENTER);
    return actionsWrap;
}


    private static class NeonButton extends JButton {

        private boolean hover;
        private int rippleR = 0;
        private Point rippleCenter = new Point(-1, -1);
        private javax.swing.Timer rippleTimer;

        NeonButton(String text) {
            super(text);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(12, 14, 12, 14));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    rippleCenter = e.getPoint();
                    rippleR = 0;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    startRipple();
                }
            });
            rippleTimer = new javax.swing.Timer(16, null);
            rippleTimer.setRepeats(true);
            rippleTimer.addActionListener(e -> {
                rippleR += 18;
                if (rippleR > Math.max(getWidth(), getHeight()) * 1.2) {
                    ((javax.swing.Timer) e.getSource()).stop();
                }
                repaint();
            });
        }

        private void startRipple() {
            rippleR = 0;
            if (!rippleTimer.isRunning()) {
                rippleTimer.start();
            }
        }

        @Override
        public void updateUI() {
            super.updateUI();
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(12, 14, 12, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (hover) {
                g2.setColor(new Color(NEON_PINK.getRed(), NEON_PINK.getGreen(), NEON_PINK.getBlue(), 70));
                for (int i = 0; i < 6; i++) {
                    g2.drawRoundRect(3 - i, 3 - i, w - 6 + 2 * i, h - 6 + 2 * i, 20, 20);
                }
            }
            GradientPaint gp = new GradientPaint(0, 0, NEON_PURPLE, w, h, NEON_PINK);
            g2.setPaint(gp);
            g2.fillRoundRect(3, 3, w - 6, h - 6, 20, 20);
            if (rippleCenter.x >= 0) {
                g2.setClip(new RoundRectangle2D.Float(3, 3, w - 6, h - 6, 20, 20));
                g2.setColor(new Color(255, 255, 255, 70));
                g2.fillOval(rippleCenter.x - rippleR, rippleCenter.y - rippleR, rippleR * 2, rippleR * 2);
            }
            g2.setColor(new Color(255, 255, 255, 50));
            g2.drawRoundRect(3, 3, w - 6, h - 6, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JPanel buildStatsCard() {
        GlassCard statsCard = new GlassCard(20);
        statsCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        statsCard.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;

        int row = 0;
        JLabel memTitle = label("RAM");
        memBar = new NeonProgress();
        memBar.setStringPainted(true);
        put(statsCard, gc, memTitle, row++);
        put(statsCard, gc, memBar, row++);

        JLabel cpuTitle = label("CPU");
        cpuBar = new NeonProgress();
        cpuBar.setStringPainted(true);
        put(statsCard, gc, cpuTitle, row++);
        put(statsCard, gc, cpuBar, row++);

        return statsCard;
    }

    private JPanel buildLogCard() {
        GlassCard card = new GlassCard(20);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = label("Log & Trạng thái trực tiếp");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(title, BorderLayout.NORTH);

        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setFont(MONO_FONT);
        logPane.setForeground(TEXT_PRIMARY);
        logPane.setBackground(new Color(26, 20, 38, 180));
        logPane.setCaretColor(TEXT_PRIMARY);

        JScrollPane sp = new JScrollPane(logPane);
        styleScroll(sp);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void styleScroll(JScrollPane sp) {
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUI(new NeonScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new NeonScrollBarUI());
    }

    private JLabel createPillLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Color.WHITE);
        l.setBorder(new EmptyBorder(8, 12, 8, 12));
        l.setOpaque(false);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return l;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_MUTED);
        l.setFont(BODY_FONT);
        return l;
    }

    private void put(JPanel p, GridBagConstraints gc, JComponent c, int row) {
        gc.gridy = row;
        p.add(c, gc);
    }

    private Game.client.Player findOnlinePlayer(String name) {
        if (name == null) {
            return null;
        }
        try {
            return Game.map.Map.get_player_by_name(name);
        } catch (Throwable t) {
            return null;
        }
    }

    private void shutdownServerGracefully() {
        try {
            stopAutoBackup();
        } catch (Throwable ignored) {
        }

        appendColored("Bắt đầu lưu dữ liệu trước khi tắt server...\n", NEON_CYAN);
        try {
            SaveData.process();
            appendColored("Lưu dữ liệu xong.\n", new Color(160, 255, 160));
        } catch (Throwable ex) {
            appendColored("Lỗi khi lưu dữ liệu: " + ex.getMessage() + "\n", new Color(255, 180, 180));
        }
        int closed = 0;
        try {
            synchronized (Session.client_entry) {
                for (Session s : new ArrayList<>(Session.client_entry)) {
                    try {
                        s.close();
                        closed++;
                    } catch (Throwable ignored) {
                    }
                }
            }
        } catch (Throwable ex) {
            appendColored("Lỗi khi đóng kết nối: " + ex.getMessage() + "\n", new Color(255, 180, 180));
        }
        appendColored("Đã đóng " + closed + " kết nối.\n", NEON_GOLD);
        try {
            ServerManager.gI().close();
            appendColored("Server đã đóng socket & dừng vòng lặp.\n", NEON_GOLD);
        } catch (Throwable ex) {
            appendColored("Lỗi khi đóng server: " + ex.getMessage() + "\n", new Color(255, 180, 180));
        }
    }

    private void triggerBackup(String reason) {
        if (!isBackingUp.compareAndSet(false, true)) {
            appendColored("Backup đang chạy, bỏ qua lượt (" + reason + ")\n", new Color(255, 220, 160));
            return;
        }
        final long start = System.currentTimeMillis();
        appendColored("Bắt đầu backup [" + reason + "]...\n", NEON_CYAN);

        new Thread(() -> {
            try {
                // 1. Lưu dữ liệu nhân vật/game (Code cũ của bạn)
                SaveData.process();
                appendColored("Đang trích xuất SQL...\n", Color.YELLOW);
                boolean isSqlOk = Game.BackupSQL.performBackup(); // Gọi class BackupSQL

                long ms = System.currentTimeMillis() - start;
                int n = backupsDone.incrementAndGet();
                appendColored(String.format("Backup #%d hoàn tất trong %.2fs\n", n, ms / 1000.0), new Color(160, 255, 160));

                // 3. Gửi lên Telegram (Chỉ gửi nếu backup SQL thành công)
                if (isSqlOk) {
                    appendColored("Đang tải lên Telegram...\n", Color.YELLOW);
                    File fileSql = new File("backup.sql"); // Tên file phải khớp với bên BackupSQL

                    String caption = "📦 **AUTO BACKUP**\n"
                            + "🔖 Lý do: " + reason + "\n"
                            + "⏱ Thời gian chạy: " + (ms/1000.0) + "s";
                    Game.BotTelegram.sendBackupNow(fileSql, caption);
                    appendColored("Đã gửi backup lên nhóm Telegram!\n", Color.GREEN);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                appendColored("Backup lỗi: " + ex.getMessage() + "\n", new Color(255, 180, 180));
            } finally {
                isBackingUp.set(false);
            }
        }, "CPanel-Backup").start();
    }

    private void startAutoBackup() {
        startAutoBackup(1 * 30 * 60_000);
    }

    private void startAutoBackup(int intervalMs) {
        if (intervalMs < 5_000) {
            intervalMs = 5_000;
        }
        autoBackupIntervalMs = intervalMs;

        if (autoBackupTimer == null) {
            autoBackupTimer = new javax.swing.Timer(intervalMs, e -> {
                nextBackupAtMs = System.currentTimeMillis() + autoBackupIntervalMs;
                triggerBackup("Auto");
            });
            autoBackupTimer.setRepeats(true);
        } else {
            autoBackupTimer.stop();
            autoBackupTimer.setDelay(intervalMs);
            autoBackupTimer.setInitialDelay(intervalMs);
        }
        nextBackupAtMs = System.currentTimeMillis() + intervalMs;
        autoBackupTimer.start();
        appendColored("Auto Backup: ON (cứ " + (intervalMs / (60 * 1000)) + " phút /1 lần)\n", NEON_GOLD);
    }

    private void stopAutoBackup() {
        if (autoBackupTimer != null) {
            autoBackupTimer.stop();
        }
        nextBackupAtMs = -1;
        appendColored("Auto Backup: OFF\n", NEON_GOLD);
    }

    // ======= Backup Center (giữ nguyên logic, chỉ gọi open dialog) =======
    private void openBackupCenter() {
        JDialog dlg = new JDialog(frame, "Trung tâm Sao lưu", false);
        dlg.setModalityType(Dialog.ModalityType.MODELESS);
        dlg.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        dlg.setAlwaysOnTop(false);

        boolean on = (autoBackupTimer != null && autoBackupTimer.isRunning());

        JCheckBox enable = new JCheckBox("Bật Auto Backup");
        enable.setOpaque(false);
        enable.setForeground(TEXT_PRIMARY);
        enable.setSelected(on);

        JLabel lbCycle = new JLabel("Chu kỳ (giây, ≥5):");
        lbCycle.setForeground(TEXT_MUTED);

        int currentSec = Math.max(5, autoBackupIntervalMs / 1000);
        JSpinner secSpin = new JSpinner(new SpinnerNumberModel(currentSec, 5, 86400, 5));
        ((JSpinner.DefaultEditor) secSpin.getEditor()).getTextField().setColumns(6);

        JLabel status = new JLabel();
        status.setForeground(TEXT_MUTED);

        JButton btnBackupNow = new JButton("Sao lưu ngay");
        JButton btnSave = new JButton("Lưu & Áp dụng");
        JButton btnClose = new JButton("Đóng");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(4, 0, 4, 8);
        form.add(enable, gc);
        gc.gridy++;
        form.add(lbCycle, gc);
        gc.gridx = 1;
        form.add(secSpin, gc);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(btnBackupNow);
        btns.add(btnSave);
        btns.add(btnClose);

        GlassCard card = new GlassCard(16);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setLayout(new BorderLayout(10, 10));
        JLabel title = new JLabel("Quản lý sao lưu");
        title.setForeground(TEXT_PRIMARY);
        title.setFont(TITLE_FONT.deriveFont(Font.PLAIN, 16f));
        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(status, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.CENTER);
        wrap.add(btns, BorderLayout.SOUTH);

        dlg.setContentPane(wrap);
        dlg.setSize(460, 230);
        dlg.setLocationRelativeTo(frame);

        javax.swing.Timer countdown = new javax.swing.Timer(1000, ev -> {
            String state = (autoBackupTimer != null && autoBackupTimer.isRunning()) ? "ON" : "OFF";
            String nextTxt = "—";
            if (nextBackupAtMs > 0 && "ON".equals(state)) {
                long remain = Math.max(0, nextBackupAtMs - System.currentTimeMillis());
                long s = remain / 1000;
                nextTxt = s + "s";
            }
            status.setText(String.format("<html><i>Trạng thái:</i> %s · <i>Kế tiếp:</i> %s · <i>Đã chạy:</i> %d</html>",
                    state, nextTxt, backupsDone.get()));
        });
        countdown.start();
        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                countdown.stop();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                countdown.stop();
            }
        });

        btnBackupNow.addActionListener(ev -> triggerBackup("Manual"));
        btnSave.addActionListener(ev -> {
            int sec = (Integer) secSpin.getValue();
            if (enable.isSelected()) {
                startAutoBackup(sec * 1000);
            } else {
                stopAutoBackup();
            }
        });
        btnClose.addActionListener(ev -> dlg.dispose());

        dlg.setVisible(true);
    }
    // =====================================================================

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        switch (cmd) {
            case "SaveData": {
                appendColored("Lưu dữ liệu bắt đầu...\n", NEON_CYAN);
                try {
                    SaveData.process();
                    appendColored("Lưu dữ liệu xong.\n", new Color(160, 255, 160));
                } catch (Throwable ex) {
                    appendColored("Lỗi khi lưu dữ liệu: " + ex.getMessage() + "\n", new Color(255, 180, 180));
                }
                break;
            }
            case "KickAll": {
                int confirm = JOptionPane.showConfirmDialog(frame, "Bạn có chắc chắn muốn kích tất cả người chơi không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    int closed = 0;
                    try {
                        synchronized (Session.client_entry) {
                            for (Session s : new ArrayList<>(Session.client_entry)) {
                                try {
                                    s.close();
                                    closed++;
                                } catch (Throwable ignored) {
                                }
                            }
                        }
                        appendColored("Đã kích " + closed + " kết nối.\n", NEON_GOLD);
                    } catch (Throwable ex) {
                        appendColored("Lỗi khi kích: " + ex.getMessage() + "\n", new Color(255, 180, 180));
                    }
                }
                break;
            }
            case "Broadcast": {
                String msg = JOptionPane.showInputDialog(frame, "Nhập nội dung thông báo gửi đến toàn bộ người chơi:", "Thông báo server", JOptionPane.PLAIN_MESSAGE);
                if (msg != null && !msg.trim().isEmpty()) {
                    int sent = 0;
                    for (Session s : snapshotSessions()) {
                        if (s != null && s.isLogin) {
                            try {
                                Service.send_notice_box(s, msg);
                                sent++;
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                    appendColored("Đã gửi thông báo đến " + sent + " người chơi.\n", NEON_PINK);
                }
                break;
            }
            case "OnlineList": {
                JDialog dlg = new JDialog(frame, "Người chơi online", false);
                dlg.setModalityType(Dialog.ModalityType.MODELESS);
                dlg.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                dlg.setSize(420, 520);
                dlg.setLocationRelativeTo(frame);
                DefaultListModel<String> model = new DefaultListModel<>();
                int total = 0, logged = 0;
                for (Session s : snapshotSessions()) {
                    total++;
                    if (s != null && s.isLogin && s.p != null) {
                        logged++;
                        String name = s.p.name;
                        model.addElement((name != null ? name : "(no-name)") + " @ " + (s.ip != null ? s.ip : "?"));
                    }
                }
                JList<String> list = new JList<>(model);
                list.setFont(new Font("Consolas", Font.PLAIN, 13));
                dlg.setLayout(new BorderLayout());
                dlg.add(new JScrollPane(list), BorderLayout.CENTER);
                dlg.add(new JLabel("Đăng nhập: " + logged + " / Kết nối: " + total, SwingConstants.CENTER), BorderLayout.SOUTH);
                dlg.setVisible(true);
                break;
            }
            case "OptimizeMemory": {
                long before = usedMem();
                System.gc();
                long after = usedMem();
                appendColored(String.format("Memory Clear: %.2f MB -> %.2f MB\n", before / 1048576.0, after / 1048576.0), NEON_CYAN);
                break;
            }
            case "ThreadDump": {
                ThreadMXBean tm = ManagementFactory.getThreadMXBean();
                long[] ids = tm.getAllThreadIds();
                appendColored("Thread dump (" + ids.length + " threads):\n", NEON_GOLD);
                for (long id : ids) {
                    ThreadInfo ti = tm.getThreadInfo(id, 5);
                    if (ti != null) {
                        appendColored(" - " + ti.getThreadName() + " [" + ti.getThreadState() + "]\n", TEXT_PRIMARY);
                    }
                }
                break;
            }

            case "SendGold": {
                if (dlgGold == null || !dlgGold.isDisplayable()) {
                    dlgGold = new SendGoldDialog(frame);
                }
                dlgGold.setLocationRelativeTo(frame);
                dlgGold.setVisible(true);
                dlgGold.toFront();
                break;
            }
            case "SendGem": {
                if (dlgGem == null || !dlgGem.isDisplayable()) {
                    dlgGem = new SendGemDialog(frame);
                }
                dlgGem.setLocationRelativeTo(frame);
                dlgGem.setVisible(true);
                dlgGem.toFront();
                break;
            }
            case "BuffLevel": {
                if (dlgLevel == null || !dlgLevel.isDisplayable()) {
                    dlgLevel = new BuffLevelDialog(frame);
                }
                dlgLevel.setLocationRelativeTo(frame);
                dlgLevel.setVisible(true);
                dlgLevel.toFront();
                break;
            }
            case "SendItem": {
                if (dlgItem == null || !dlgItem.isDisplayable()) {
                    dlgItem = new SendItemDialog(frame);
                }
                dlgItem.setLocationRelativeTo(frame);
                dlgItem.setVisible(true);
                dlgItem.toFront();
                break;
            }

            case "BackupCenter": {
                openBackupCenter();
                break;
            }
            case "Maintenance": {
                openMaintenanceDialog();
                break;
            }
            case "GIFT_CODE": {
                if (giftcodePanel == null || !giftcodePanel.isDisplayable()) {
                    giftcodePanel = new GiftcodePanel(frame);
                }
                giftcodePanel.setLocationRelativeTo(frame);
                giftcodePanel.setVisible(true);
                giftcodePanel.toFront();
                break;
            }
            case "LookupItems": {
                if (itemsLookupPanel == null || !itemsLookupPanel.isDisplayable()) {
                    itemsLookupPanel = new ItemsLookupPanel(frame);
                }
                itemsLookupPanel.setLocationRelativeTo(frame);
                itemsLookupPanel.setVisible(true);
                itemsLookupPanel.toFront();
                break;
            }
            case "DDoSGuard": {
                JDialog dlg = new DdosGuardDialog(frame);
                dlg.setLocationRelativeTo(frame);
                dlg.setVisible(true);
                break;
            }
            case "BuffPotential": {  // giả sử bạn tạo dialog BuffTienNangDialog
    if (dlgBuffPotential == null || !dlgBuffPotential.isDisplayable()) {
        dlgBuffPotential = new BuffPotentialDialog(frame);
    }
    dlgBuffPotential.setLocationRelativeTo(frame);
    dlgBuffPotential.setVisible(true);
    dlgBuffPotential.toFront();
    break;
}
            case "BuffSkill": {
    if (dlgBuffSkill == null || !dlgBuffSkill.isDisplayable()) {
        dlgBuffSkill = new BuffSkillDialog(frame);
    }
    dlgBuffSkill.setLocationRelativeTo(frame);
    dlgBuffSkill.setVisible(true);
    dlgBuffSkill.toFront();
    break;
}
 case "BuffTongNap": {
    if (dlgBuffTongnap == null || !dlgBuffTongnap.isDisplayable()) {
        dlgBuffTongnap = new BuffTongnapDialog(frame);
    }
    dlgBuffTongnap.setLocationRelativeTo(frame);
    dlgBuffTongnap.setVisible(true);
    dlgBuffTongnap.toFront();
    break;
}

    }
    }
    // === DDoS GUARD DIALOG ===
    private static class DdosGuardDialog extends JDialog {

        private final JTextField ipField = new JTextField(20);
        private final JSpinner minutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 7 * 24 * 60, 1)); // 0 = forever
        private final JCheckBox firewallBox = new JCheckBox("Block ở Firewall (VPS)");
        private final JCheckBox udpBox = new JCheckBox("Chặn thêm UDP 19129");
        private final JRadioButton scopeWebGame = new JRadioButton("Chỉ port 80 & 19129", true);
        private final JRadioButton scopeAllPorts = new JRadioButton("Tất cả cổng");
        private final DefaultListModel<String> listModel = new DefaultListModel<>();
        private final JList<String> list = new JList<>(listModel);
        private final JButton refreshBtn = new JButton("Làm mới");
        private final JButton blockBtn = new JButton("Block");
        private final JButton unblockBtn = new JButton("Unblock");
        private final DefaultListModel<String> hotModel = new DefaultListModel<>();
        private final JList<String> hotList = new JList<>(hotModel);
        private final JButton scanBtn = new JButton("Quét kết nối (80 & 19129)");
// --- AUTO BAN ---
        private final JSpinner autoThreshold = new JSpinner(new SpinnerNumberModel(10, 1, 100000, 1));
        private final JSpinner autoBanMinutes = new JSpinner(new SpinnerNumberModel(60, 1, 7 * 24 * 60, 1));
        private final JCheckBox autoFirewall = new JCheckBox("Firewall", true);
        private final JCheckBox autoOnlyWebGame = new JCheckBox("Chỉ 80 & 19129", true);
        private final JCheckBox autoUdp = new JCheckBox("UDP 19129");
        private final JTextField whitelistField = new JTextField(24); // CSV IP: 1.2.3.4,5.6.7.8
        private final JSpinner autoIntervalSec = new JSpinner(new SpinnerNumberModel(15, 5, 3600, 1));
        private final JButton autoScanBanBtn = new JButton("Quét & Auto-ban ngay");
        private final JToggleButton autoToggle = new JToggleButton("Bật quét tự động");

        private void doScan() {
            hotModel.clear();
            try {
                java.util.List<Game.core.PortHostspot.Stat> list = Game.core.PortHostspot.scanBoth(50);
                for (Game.core.PortHostspot.Stat st : list) {
                    hotModel.addElement(st.ip + " | port " + st.port + " | conn=" + st.count);
                }
                if (list.isEmpty()) {
                    hotModel.addElement("Không thấy kết nối đáng kể lúc này.");
                }
            } catch (Throwable ex) {
                hotModel.addElement("Lỗi khi quét: " + ex.getMessage());
            }
        }

        private void doAutoScanBan() {
            int thr = ((Number) autoThreshold.getValue()).intValue();
            int mins = ((Number) autoBanMinutes.getValue()).intValue();
            boolean fw = autoFirewall.isSelected();
            boolean onlyWG = autoOnlyWebGame.isSelected();
            boolean udp = autoUdp.isSelected();
            java.util.Set<String> wl = new java.util.HashSet<>();
            for (String s : whitelistField.getText().split(",")) {
                if (s != null && !s.trim().isEmpty()) {
                    wl.add(s.trim());
                }
            }
            try {
                int banned = Game.core.AutoDdosBan.scanOnceAndBan(thr, mins, fw, udp, onlyWG, wl);
                JOptionPane.showMessageDialog(this, "Đã auto-ban " + banned + " IP (ngưỡng > " + thr + " socket).");
                refreshList();
            } catch (Throwable ex) {
                JOptionPane.showMessageDialog(this, "Lỗi auto-ban: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void toggleAuto() {
            int thr = ((Number) autoThreshold.getValue()).intValue();
            int mins = ((Number) autoBanMinutes.getValue()).intValue();
            boolean fw = autoFirewall.isSelected();
            boolean onlyWG = autoOnlyWebGame.isSelected();
            boolean udp = autoUdp.isSelected();
            int interval = ((Number) autoIntervalSec.getValue()).intValue();
            java.util.List<String> wl = new java.util.ArrayList<>();
            for (String s : whitelistField.getText().split(",")) {
                if (s != null && !s.trim().isEmpty()) {
                    wl.add(s.trim());
                }
            }
            try {
                Game.core.AutoDdosBan.configure(thr, mins, fw, udp, onlyWG, interval, wl);
                if (!autoToggle.isSelected()) {
                    Game.core.AutoDdosBan.disableAuto();
                    autoToggle.setText("Bật quét tự động");
                    JOptionPane.showMessageDialog(this, "Đã tắt auto-quét.");
                } else {
                    Game.core.AutoDdosBan.enableAuto();
                    autoToggle.setText("Tắt quét tự động");
                    JOptionPane.showMessageDialog(this, "Đã bật auto-quét mỗi " + interval + " giây.");
                }
            } catch (Throwable ex) {
                JOptionPane.showMessageDialog(this, "Lỗi thiết lập auto: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }

        DdosGuardDialog(JFrame owner) {
            super(owner, "🛡 Chống DDoS", true);
            setSize(720, 480);
            setLayout(new BorderLayout(10, 10));

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6, 6, 6, 6);
            gc.anchor = GridBagConstraints.WEST;
            // --- PANEL AUTO BAN ---
            JPanel auto = new JPanel(new GridBagLayout());
            auto.setBorder(BorderFactory.createTitledBorder("Auto-ban theo số socket/IP"));

            GridBagConstraints a = new GridBagConstraints();
            a.insets = new Insets(6, 6, 6, 6);
            a.anchor = GridBagConstraints.WEST;

            int rr = 0;
            a.gridx = 0;
            a.gridy = rr;
            auto.add(new JLabel("Ngưỡng (>socket):"), a);
            a.gridx = 1;
            a.gridy = rr;
            auto.add(autoThreshold, a);
            rr++;

            a.gridx = 0;
            a.gridy = rr;
            auto.add(new JLabel("Ban (phút):"), a);
            a.gridx = 1;
            a.gridy = rr;
            auto.add(autoBanMinutes, a);
            rr++;

            a.gridx = 0;
            a.gridy = rr;
            auto.add(new JLabel("Tùy chọn:"), a);
            JPanel opts = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            opts.add(autoFirewall);
            opts.add(autoOnlyWebGame);
            opts.add(autoUdp);
            a.gridx = 1;
            a.gridy = rr;
            auto.add(opts, a);
            rr++;

            a.gridx = 0;
            a.gridy = rr;
            auto.add(new JLabel("Whitelist (CSV):"), a);
            a.gridx = 1;
            a.gridy = rr;
            auto.add(whitelistField, a);
            rr++;

            a.gridx = 0;
            a.gridy = rr;
            auto.add(new JLabel("Chu kỳ quét (giây):"), a);
            a.gridx = 1;
            a.gridy = rr;
            auto.add(autoIntervalSec, a);
            rr++;

            JPanel abtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            abtns.add(autoScanBanBtn);
            abtns.add(autoToggle);

            JPanel autoWrap = new JPanel(new BorderLayout());
            autoWrap.add(auto, BorderLayout.CENTER);
            autoWrap.add(abtns, BorderLayout.SOUTH);

// Bạn có thể đặt Auto-ban ở dưới cùng:
            add(autoWrap, BorderLayout.SOUTH);

// Sự kiện:
            autoScanBanBtn.addActionListener(e2 -> doAutoScanBan());
            autoToggle.addActionListener(e2 -> toggleAuto());

            // Panel danh sách IP nóng (đang kết nối nhiều)
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setBorder(BorderFactory.createTitledBorder("Top IP đang kết nối (80 & 19129)"));
            bottom.add(new JScrollPane(hotList), BorderLayout.CENTER);
            JPanel hb = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            hb.add(scanBtn);
            bottom.add(hb, BorderLayout.SOUTH);
            add(bottom, BorderLayout.SOUTH);

// sự kiện
            scanBtn.addActionListener(e -> doScan());
            hotList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    String s = hotList.getSelectedValue();
                    if (s != null) {
                        String ip = s.split("\\s+\\|")[0].trim(); // định dạng từ PortHotspot.toString()
                        ipField.setText(ip);
                    }
                }
            });

            int r = 0;
            gc.gridx = 0;
            gc.gridy = r;
            form.add(new JLabel("IP cần block:"), gc);
            gc.gridx = 1;
            gc.gridy = r;
            form.add(ipField, gc);
            r++;

            gc.gridx = 0;
            gc.gridy = r;
            form.add(new JLabel("Số phút (0 = vĩnh viễn):"), gc);
            gc.gridx = 1;
            gc.gridy = r;
            form.add(minutesSpinner, gc);
            r++;

            gc.gridx = 0;
            gc.gridy = r;
            form.add(new JLabel("Phạm vi:"), gc);
            ButtonGroup grp = new ButtonGroup();
            grp.add(scopeWebGame);
            grp.add(scopeAllPorts);
            JPanel scopes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            scopes.add(scopeWebGame);
            scopes.add(scopeAllPorts);
            gc.gridx = 1;
            gc.gridy = r;
            form.add(scopes, gc);
            r++;

            gc.gridx = 1;
            gc.gridy = r;
            form.add(udpBox, gc);
            r++;
            gc.gridx = 1;
            gc.gridy = r;
            form.add(firewallBox, gc);
            r++;

            JPanel top = new JPanel(new BorderLayout());
            top.add(form, BorderLayout.CENTER);

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btns.add(refreshBtn);
            btns.add(blockBtn);
            btns.add(unblockBtn);
            top.add(btns, BorderLayout.SOUTH);
            add(top, BorderLayout.NORTH);

            JScrollPane sp = new JScrollPane(list);
            add(sp, BorderLayout.CENTER);

            refreshBtn.addActionListener(e -> refreshList());
            blockBtn.addActionListener(e -> doBlock());
            unblockBtn.addActionListener(e -> doUnblock());
            refreshList();
        }

        private void refreshList() {
            
            listModel.clear();
            try {
                java.util.List<Game.core.DdosGuard.Entry> entries = Game.core.DdosGuard.snapshot();
                for (Game.core.DdosGuard.Entry en : entries) {
                    String line = String.format("%s | hết hạn: %s | firewall: %s | scope: %s",
                            en.ip, en.expiryHuman(), en.firewall ? "Có" : "Không",
                            en.onlyWebAndGame ? "80 & 19129" : "All");
                    listModel.addElement(line);
                }
                // show thêm app-level (nếu trước đó đã bị block sẵn)
                java.util.Set<String> app = Game.core.DdosGuard.appLevelBlockedSnapshot();
                for (String ip : app) {
                    boolean exists = false;
                    for (int i = 0; i < listModel.size(); i++) {
                        if (listModel.get(i).startsWith(ip + " ")) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        listModel.addElement(ip + " | (app-level) | hết hạn: vĩnh viễn | firewall: Không | scope: -");
                    }
                }
            } catch (Throwable ex) {
                listModel.addElement("Lỗi khi đọc danh sách: " + ex.getMessage());
            }
        }

        private void doBlock() {
            String ip = ipField.getText().trim();
            if (ip.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nhập IP trước!", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int minutes = ((Number) minutesSpinner.getValue()).intValue();
            boolean fw = firewallBox.isSelected();
            boolean udp = udpBox.isSelected();
            boolean onlyWebGame = scopeWebGame.isSelected();
            try {
                Game.core.DdosGuard.block(ip, minutes, fw, udp, onlyWebGame, "manual");
                JOptionPane.showMessageDialog(this, "Đã block " + ip + (minutes > 0 ? (" trong " + minutes + " phút") : " vĩnh viễn"));
                refreshList();
            } catch (Throwable ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Block thất bại", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void doUnblock() {
            String selected = list.getSelectedValue();
            if (selected == null) {
                String ip = ipField.getText().trim();
                if (ip.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Chọn IP trong danh sách hoặc nhập IP", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                unblockIp(ip);
                return;
            }
            String ip = selected.split("\\s+\\|")[0].trim();
            unblockIp(ip);
        }

        private void unblockIp(String ip) {
            try {
                boolean ok = Game.core.DdosGuard.unblock(ip);
                JOptionPane.showMessageDialog(this, (ok ? "Đã " : "Không ") + "unblock " + ip);
                refreshList();
            } catch (Throwable ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Unblock thất bại", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

   private void openMaintenanceDialog() {
    JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
    panel.setOpaque(true);
    panel.setBackground(DIALOG_BG);

    String[] modes = {"Bật bảo trì", "Tắt bảo trì"};
    JComboBox<String> modeBox = new JComboBox<>(modes);
    styleCombo(modeBox);
    JTextField countdownM = new JTextField("1");
    styleField(countdownM);
    JCheckBox kickAll = new JCheckBox("Tự động kick tất cả khi Bật", true);
    kickAll.setOpaque(false);
    kickAll.setForeground(TEXT_PRIMARY);
    kickAll.setFont(DIALOG_FONT);
    JTextArea msg = new JTextArea("Máy chủ sẽ bảo trì, vui lòng thoát ra. Cảm ơn!");
    msg.setLineWrap(true);
    msg.setWrapStyleWord(true);
    styleField(msg);

    panel.add(new JLabel("Chế độ:"));
    panel.add(modeBox);
    panel.add(new JLabel("Đếm ngược (phút, khi BẬT):"));
    panel.add(countdownM);
    panel.add(kickAll);
    panel.add(new JLabel("Thông báo:"));
    JScrollPane sp = new JScrollPane(msg);
    styleScroll(sp);
    panel.add(sp);
    tintLabels(panel);

    JDialog dlg = new JDialog(frame, "Bảo trì", false);
    dlg.setModalityType(Dialog.ModalityType.MODELESS);
    dlg.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    dlg.setContentPane(wrapDialog(panel));
    dlg.setSize(520, 420);
    dlg.setLocationRelativeTo(frame);

    JButton ok = new JButton("Áp dụng");
    JButton close = new JButton("Đóng");
    ((JPanel) dlg.getContentPane().getComponent(1)).add(ok);
    ((JPanel) dlg.getContentPane().getComponent(1)).add(close);

    ok.addActionListener(ev -> {
        boolean turnOn = modeBox.getSelectedIndex() == 0;
        String content = msg.getText().trim();
        int minutes = 1;
        try {
            minutes = Math.max(0, Integer.parseInt(countdownM.getText().trim()));
        } catch (Throwable ignore) {}

        if (turnOn) {
            int sent = 0;
            for (Session s : snapshotSessions()) {
                if (s != null && s.isLogin) {
                    try {
                        String text = "[BẢO TRÌ] " + content + (minutes > 0 ? (" (" + minutes + " phút)") : "");
                        Service.send_notice_box(s, text);
                        sent++;
                    } catch (Throwable ignore) {}
                }
            }
            appendColored("Đã gửi thông báo bảo trì đến " + sent + " người chơi.\n", NEON_PINK);

            Runnable doMaintain = () -> {
                Manager.setMaintenance(true, content);
                appendColored("ĐÃ BẬT chế độ BẢO TRÌ.\n", NEON_GOLD);
                if (kickAll.isSelected()) {
                    int closed = 0;
                    List<Session> snap;
                    synchronized (Session.client_entry) {
                        snap = new ArrayList<>(Session.client_entry);
                    }
                    for (Session s : snap) {
                        try {
                            s.close();
                            closed++;
                        } catch (Throwable ignore) {}
                    }
                    appendColored("Đã kick " + closed + " kết nối để vào bảo trì.\n", NEON_GOLD);
                    try {
                        ServerManager.gI().close();
                        new Thread(() -> {
                            SaveData.process();
                            for (int k = Session.client_entry.size() - 1; k >= 0; k--) {
                                try {
                                    Session.client_entry.get(k).p = null;
                                    Session.client_entry.get(k).close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Manager.gI().close();
                        }).start();
                    } catch (IOException ex) {
                        Logger.getLogger(AdminPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            if (minutes <= 0) {
                doMaintain.run();
            } else {
                final int minutesFinal = minutes;
                final Runnable perMin = new Runnable() {
                    int left = minutesFinal;
                    @Override
                    public void run() {
                        left--;
                        if (left <= 0) return;
                        for (Session s : snapshotSessions()) {
                            if (s != null && s.isLogin) {
                                try {
                                    Service.send_notice_box(s, "[BẢO TRÌ] Còn " + left + " phút.");
                                } catch (Throwable ignore) {}
                            }
                        }
                    }
                };
                try {
                    final ScheduledFuture<?> ticker
                            = Util.everyFixed(60, 60, TimeUnit.SECONDS, perMin);
                    Util.after(minutesFinal, TimeUnit.MINUTES, () -> {
                        ticker.cancel(false);
                        doMaintain.run();
                    });
                } catch (Throwable t) {
                    new Thread(() -> {
                        for (int i = minutesFinal; i > 0; i--) {
                            try {
                                Thread.sleep(60_000L);
                            } catch (InterruptedException ignored) {}
                            if (i > 1) {
                                for (Session s : snapshotSessions()) {
                                    if (s != null && s.isLogin) {
                                        try {
                                            Service.send_notice_box(s, "[BẢO TRÌ] Còn " + (i - 1) + " phút.");
                                        } catch (Throwable ignore) {}
                                    }
                                }
                            }
                        }
                        doMaintain.run();
                    }, "Maintain-Countdown").start();
                }
            }
        } else {
            Manager.setMaintenance(false, ""); // truyền chuỗi rỗng
            appendColored("ĐÃ TẮT chế độ BẢO TRÌ.\n", NEON_CYAN);
            int sent = 0;
            for (Session s : snapshotSessions()) {
                if (s != null && s.isLogin) {
                    try {
                        Service.send_notice_box(s, "[BẢO TRÌ] Máy chủ đã mở lại. Chúc bạn chơi vui!");
                        sent++;
                    } catch (Throwable ignore) {}
                }
            }
            appendColored("Đã thông báo mở lại cho " + sent + " người chơi.\n", NEON_PINK);
        }
    });
    close.addActionListener(ev -> dlg.dispose());

    dlg.setVisible(true);
}


    // ===== Modeless Dialog Base & Styling =====
    private JPanel wrapDialog(JComponent center) {
        JPanel wrap = new JPanel(new BorderLayout(10, 10));
        wrap.setBorder(new EmptyBorder(12, 12, 12, 12));
        wrap.setBackground(DIALOG_BG);
        GlassCard card = new GlassCard(16);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.add(center, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(DIALOG_BG);
        outer.add(card, BorderLayout.CENTER);
        outer.add(btns, BorderLayout.SOUTH);
        return new JPanel(new BorderLayout()) {
            {
                setOpaque(true);
                setBackground(DIALOG_BG);
                add(outer, BorderLayout.CENTER);
                add(btns, BorderLayout.SOUTH);
            }
        };
    }

    private void tintLabels(Container c) {
        for (Component x : c.getComponents()) {
            if (x instanceof JLabel) {
                ((JLabel) x).setForeground(TEXT_PRIMARY);
                x.setFont(DIALOG_FONT);
            }
            if (x instanceof Container) {
                tintLabels((Container) x);
            }
        }
    }

    private void styleField(javax.swing.text.JTextComponent tf) {
        tf.setForeground(FIELD_FG);
        tf.setBackground(FIELD_BG);
        tf.setCaretColor(FIELD_FG);
        tf.setFont(DIALOG_FONT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(FIELD_BD, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(FIELD_BD_F, 1, true),
                        new EmptyBorder(8, 10, 8, 10)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(FIELD_BD, 1, true),
                        new EmptyBorder(8, 10, 8, 10)
                ));
            }
        });
    }

    private <T> void styleCombo(JComboBox<T> cb) {
        cb.setForeground(FIELD_FG);
        cb.setBackground(FIELD_BG);
        cb.setFont(DIALOG_FONT);
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(DIALOG_FONT);
                setForeground(FIELD_FG);
                setBackground(isSelected ? new Color(60, 50, 90) : FIELD_BG);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
    }

    // ===== Custom modeless dialogs =====
    private abstract class BaseModelessDialog extends JDialog {

        protected final JLabel status = new JLabel(" ");

        BaseModelessDialog(Frame owner, String title) {
            super(owner, title, false);
            setModalityType(Dialog.ModalityType.MODELESS);
            setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
            addWindowFocusListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    requestFocusInWindow();
                }
            });
        }

        protected JPanel createShell(JComponent form, JButton... buttons) {
            JPanel body = new JPanel(new BorderLayout(8, 8));
            body.setOpaque(true);
            body.setBackground(DIALOG_BG);
            tintLabels(form);
            status.setForeground(TEXT_MUTED);
            status.setFont(DIALOG_FONT);
            body.add(form, BorderLayout.CENTER);
            body.add(status, BorderLayout.SOUTH);

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            btns.setOpaque(false);
            for (JButton b : buttons) {
                btns.add(b);
            }

            JPanel shell = new JPanel(new BorderLayout(10, 10));
            shell.setOpaque(true);
            shell.setBackground(DIALOG_BG);
            GlassCard card = new GlassCard(16);
            card.setBorder(new EmptyBorder(14, 14, 14, 14));
            card.setLayout(new BorderLayout());
            card.add(body, BorderLayout.CENTER);
            shell.add(card, BorderLayout.CENTER);
            shell.add(btns, BorderLayout.SOUTH);
            return shell;
        }

        protected void okMsg(String msg) {
            status.setForeground(new Color(130, 255, 160));
            status.setText("✅ " + msg);
        }

        protected void errMsg(String msg) {
            status.setForeground(new Color(255, 140, 140));
            status.setText("❌ " + msg);
        }
    }

   private final class SendGoldDialog extends BaseModelessDialog {

    private final JTextField name = new JTextField();
    private final JTextField amount = new JTextField();

    SendGoldDialog(Frame owner) {
        super(owner, "Gửi Vàng");
        styleField(name);
        styleField(amount);

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        form.add(new JLabel("Tên nhân vật:"));
        form.add(name);
        form.add(new JLabel("Số vàng (+/-):"));
        form.add(amount);

        JButton send = new JButton("Gửi");
        JButton close = new JButton("Đóng");

        send.addActionListener(ev -> {
            try {
                String playerName = name.getText().trim();
                long goldAmount = Long.parseLong(amount.getText().trim());

                Game.client.Player player = findOnlinePlayer(playerName);
                if (player == null) {
                    errMsg("Không thấy người chơi: " + playerName);
                    return;
                }

                // Cộng vàng cho người chơi
                player.update_vang(goldAmount, "CPanel: %+d vàng");

                // Gửi thông báo trực tiếp cho người chơi
                String msg = "Bạn đã nhận: " + goldAmount + " vàng từ ADMIN";
                Service.send_notice_box(player.conn, msg);

                // Log ở admin
                appendColored(String.format("Gửi %d vàng cho %s\n", goldAmount, playerName), NEON_GOLD);
                okMsg("Đã gửi vàng cho " + playerName + ": " + goldAmount);

                // Cập nhật inventory (nếu cần)
                try {
                    player.item.update_add_inventory(-1, null, null);
                } catch (Throwable ignore) {}

            } catch (Exception ex) {
                errMsg("Lỗi: " + ex.getMessage());
            }
        });

        close.addActionListener(e -> setVisible(false));

        setContentPane(createShell(form, send, close));
        setSize(420, 420);
    }
}


 private final class SendGemDialog extends BaseModelessDialog {

    private final JTextField name = new JTextField();
    private final JTextField amount = new JTextField();

    SendGemDialog(Frame owner) {
        super(owner, "Gửi Ngọc");
        styleField(name);
        styleField(amount);

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        form.add(new JLabel("Tên nhân vật:"));
        form.add(name);
        form.add(new JLabel("Số ngọc (+/-):"));
        form.add(amount);

        JButton send = new JButton("Gửi");
        JButton close = new JButton("Đóng");

        send.addActionListener(ev -> {
            try {
                String playerName = name.getText().trim();
                long gemAmount = Long.parseLong(amount.getText().trim());

                Game.client.Player player = findOnlinePlayer(playerName);
                if (player == null) {
                    errMsg("Không thấy người chơi: " + playerName);
                    return;
                }

                // Cập nhật ngọc
                player.update_ngoc(gemAmount);

                // Gửi thông báo trực tiếp cho người chơi
                String msg = "Bạn đã nhận: " + gemAmount + " ngọc từ ADMIN";
                Service.send_notice_box(player.conn, msg);

                // Log ở admin
                appendColored(String.format("Gửi %d ngọc cho %s\n", gemAmount, playerName), NEON_PINK);

                okMsg("Đã gửi ngọc cho " + playerName + ": " + gemAmount);

                // Cập nhật inventory (nếu cần)
                try {
                    player.item.update_add_inventory(-1, null, null);
                } catch (Throwable ignore) {}

            } catch (NumberFormatException ex) {
                errMsg("Số ngọc không hợp lệ!");
            } catch (Exception ex) {
                errMsg("Lỗi: " + ex.getMessage());
            }
        });

        close.addActionListener(e -> setVisible(false));

        setContentPane(createShell(form, send, close));
        setSize(420, 420);
    }
}


    

    private final class BuffLevelDialog extends BaseModelessDialog {

        private final JTextField name = new JTextField();
        private final JTextField level = new JTextField();

        BuffLevelDialog(Frame owner) {
            super(owner, "Buff Level");
            styleField(name);
            styleField(level);

            JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
            form.setOpaque(false);
            form.add(new JLabel("Tên nhân vật:"));
            form.add(name);
            form.add(new JLabel("Level muốn đặt (>=2):"));
            form.add(level);

            JButton send = new JButton("Áp dụng");
            JButton close = new JButton("Đóng");
            send.addActionListener(ev -> {
                try {
                    String n = name.getText().trim();
                    int lv = Integer.parseInt(level.getText().trim());
                    Game.client.Player p = findOnlinePlayer(n);
                    if (p == null) {
                        errMsg("Không thấy người chơi: " + n);
                        return;
                    }
                    if (lv < 2) {
                        lv = 2;
                    }
                    if (lv > Game.core.Manager.gI().lvmax) {
                        lv = Game.core.Manager.gI().lvmax;
                    }
                    p.level = (short) (lv - 1);
                    p.exp = Game.template.Level.entry.get(lv - 2).exp - 1;
                    p.update_Exp(1, false);
                    Game.core.Service.send_char_main_in4(p);
                    try {
                        for (Game.client.Player p0 : p.map.players) {
                            if (p0 != null && p0.ID != p.ID
                                    && (Math.abs(p0.x - p.x) < 200) && (Math.abs(p0.y - p.y) < 200)) {
                                Game.map.MapService.send_in4_other_char(p0.map, p0, p);
                            }
                        }
                    } catch (Throwable ignore) {
                    }
                    okMsg("Đã buff level cho " + n + " tới " + lv);
                    appendColored("Đã buff level cho " + n + " tới " + lv + "\n", NEON_CYAN);
                } catch (Exception ex) {
                    errMsg("Lỗi: " + ex.getMessage());
                }
            });
            close.addActionListener(e -> setVisible(false));

            setContentPane(createShell(form, send, close));
            setSize(420, 420);
        }
    }

public final class SendItemDialog extends BaseModelessDialog {

    private final JTextField name = new JTextField();
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{
            "3 - Trang bị (Item3)",
            "4 - Vật phẩm (Item4)",
            "7 - Nguyên liệu (Item7)"
    });
    private final JTextField idField = new JTextField();
    private final JTextField qtyField = new JTextField("1");
    private final JTextField daysField = new JTextField("0");

    public SendItemDialog(Frame owner) {
        super(owner, "Gửi Item");
        styleField(name);
        styleField(idField);
        styleField(qtyField);
        styleField(daysField);
        styleCombo(typeBox);

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        form.add(new JLabel("Tên nhân vật:"));
        form.add(name);
        form.add(new JLabel("Loại item:"));
        form.add(typeBox);
        form.add(new JLabel("ID item:"));
        form.add(idField);
        form.add(new JLabel("Số lượng (type 4/7):"));
        form.add(qtyField);
        form.add(new JLabel("Hạn ngày (type 3, 0 = vĩnh viễn):"));
        form.add(daysField);

        JButton send = new JButton("Gửi");
        JButton close = new JButton("Đóng");

        send.addActionListener(ev -> {
            try {
                String n = name.getText().trim();
                Player p = findOnlinePlayer(n);
                if (p == null) {
                    errMsg("Không thấy người chơi: " + n);
                    return;
                }

                int typeSel = typeBox.getSelectedIndex();
                int type = (typeSel == 0 ? 3 : (typeSel == 1 ? 4 : 7));
                short id = Short.parseShort(idField.getText().trim());

                String itemName = "";
                String msg = "";

                if (type == 3) { // Item3
                    int days = Integer.parseInt(daysField.getText().trim());
                    p.item.add_item_inventory3_default(id, days, true);
                    p.item.char_inventory(3);

                    itemName = ItemTemplate3.item.get(id).getName();
                    if (days <= 0) {
                        msg = "ADMIN gửi đến bạn: " + itemName + " vĩnh viễn";
                    } else {
                        msg = "ADMIN gửi đến bạn: " + itemName  +  days +  " ngày";
                    }
                } else { // Item4/7
                    short qt = Short.parseShort(qtyField.getText().trim());
                    if (qt <= 0) {
                        errMsg("Số lượng phải > 0");
                        return;
                    }
                    p.item.add_item_inventory47(id, qt, (byte) type);
                    p.item.char_inventory(type);

                    if (type == 4) itemName = ItemTemplate4.item.get(id).getName();
                    else itemName = ItemTemplate7.item.get(id).getName();

                    msg = "ADMIN gửi đến bạn: " + itemName + " x" + qt;
                }

                Service.send_notice_box(p.conn, msg);
                okMsg("Đã gửi item id=" + id + " cho " + n);
                appendColored("Đã gửi item id=" + id + " cho " + n + "\n", NEON_PINK);

            } catch (Exception ex) {
                errMsg("Lỗi: " + ex.getMessage());
            }
        });

        close.addActionListener(e -> setVisible(false));
        setContentPane(createShell(form, send, close));
        setSize(460, 560);
    }
}

    private final class BuffPotentialDialog extends BaseModelessDialog {

    private final JTextField name = new JTextField();
    private final JTextField points = new JTextField("1");

    BuffPotentialDialog(Frame owner) {
        super(owner, "Buff Tiềm Năng");
        styleField(name);
        styleField(points);

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        form.add(new JLabel("Tên nhân vật:"));
        form.add(name);
        form.add(new JLabel("Số điểm tiềm năng (+/-):"));
        form.add(points);

        JButton send = new JButton("Gửi");
        JButton close = new JButton("Đóng");

        send.addActionListener(ev -> {
            try {
                String playerName = name.getText().trim();
                int pts = Integer.parseInt(points.getText().trim());

                Game.client.Player player = findOnlinePlayer(playerName);
                if (player == null) {
                    errMsg("Không thấy người chơi: " + playerName);
                    return;
                }

                // Cộng điểm trực tiếp
                player.tiemnang += pts;

                // Thông báo nhận ngay
                if (player.conn != null) {
                    Service.send_notice_box(player.conn, "Bạn nhận được " + pts + " điểm tiềm năng từ ADMIN!");
                }

                appendColored(String.format("Buff tiềm năng cho %s: %+d\n", playerName, pts), NEON_PINK);
                okMsg("Đã buff tiềm năng cho " + playerName + ": " + pts);

            } catch (NumberFormatException ex) {
                errMsg("Số điểm không hợp lệ!");
            } catch (Exception ex) {
                errMsg("Lỗi: " + ex.getMessage());
            }
        });

        close.addActionListener(e -> setVisible(false));
        setContentPane(createShell(form, send, close));
        setSize(420, 420);
    }
}

private final class BuffSkillDialog extends BaseModelessDialog {

    private final JTextField name = new JTextField();
    private final JTextField points = new JTextField("1"); // số điểm skill

    BuffSkillDialog(Frame owner) {
        super(owner, "Buff Kĩ Năng");
        styleField(name);
        styleField(points);

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        form.add(new JLabel("Tên nhân vật:"));
        form.add(name);
        form.add(new JLabel("Số điểm kĩ năng (+/-):"));
        form.add(points);

        JButton send = new JButton("Gửi");
        JButton close = new JButton("Đóng");

        send.addActionListener(ev -> {
            try {
                String playerName = name.getText().trim();
                int pt = Integer.parseInt(points.getText().trim());

                Game.client.Player player = findOnlinePlayer(playerName);
                if (player == null) {
                    errMsg("Không thấy người chơi: " + playerName);
                    return;
                }

                // Cộng trực tiếp skill
                player.kynang += pt;

                // Gửi thông báo nhận ngay cho người chơi
                if (player.conn != null) {
                    Service.send_notice_box(player.conn, "Bạn nhận được " + pt + " điểm kĩ năng từ ADMIN!");
                }

                // Log cPanel
                appendColored(String.format("Buff kĩ năng cho %s: %+d\n", playerName, pt), NEON_PINK);
                okMsg("Đã buff kĩ năng cho " + playerName + ": " + pt);

            } catch (NumberFormatException ex) {
                errMsg("Số điểm không hợp lệ!");
            } catch (Exception ex) {
                errMsg("Lỗi: " + ex.getMessage());
            }
        });

        close.addActionListener(e -> setVisible(false));
        setContentPane(createShell(form, send, close));
        setSize(420, 420);
    }
}

private final class BuffTongnapDialog extends BaseModelessDialog {

    private final JTextField name = new JTextField();
    private final JTextField tongnap = new JTextField("1000"); // mặc định 1000 VNĐ

    BuffTongnapDialog(Frame owner) {
        super(owner, "Buff Tổng Nạp");

        // style giống các field khác
        styleField(name);
        styleField(tongnap);

        // form
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);
        form.add(new JLabel("Tên nhân vật:"));
        form.add(name);
        form.add(new JLabel("Số tiền nạp (+/- VNĐ):"));
        form.add(tongnap);

        // nút gửi / đóng
        JButton send = new JButton("Gửi");
        JButton close = new JButton("Đóng");

        send.addActionListener(ev -> doBuff());
        close.addActionListener(ev -> setVisible(false));

        // dùng createShell giống BuffSkill
        setContentPane(createShell(form, send, close));
        pack(); // pack để tự động hiển thị đúng kích thước
    }

    private void doBuff() {
//        try {
//            // Lấy tên và số tiền buff, trim khoảng trắng
//            String playerNameField = name.getText().trim();
//            long amt = Long.parseLong(tongnap.getText().trim());
//
//            if (playerNameField.isEmpty()) {
//                errMsg("Vui lòng nhập tên nhân vật!");
//                return;
//            }
//
//            // Tìm player online
//            Player player = findOnlinePlayer(playerNameField);
//            if (player == null) {
//                errMsg("Không thấy người chơi online: " + playerNameField);
//                return;
//            }
//
//            // Debug log
//            System.out.println("===== DEBUG BUFF TỔNG NẠP (ACCOUNT) =====");
//            // Lưu ý: userId là id của tài khoản. Kiểm tra lại biến này trong source của bạn (có thể là account_id)
//            System.out.println("Player: " + player.name + ", Account ID: " + player.userId);
//            System.out.println("Buff: " + amt);
//
//            // ====== Cập nhật DB (BẢNG ACCOUNT) ======
//            try (Connection conn = DriverManager.getConnection(
//                    "jdbc:mysql://localhost:3306/hihi?useSSL=false", "root", "")) {
//
//                // SỬA: Update vào bảng account, tìm theo id
//                PreparedStatement pst = conn.prepareStatement(
//                        "UPDATE account SET tongnap = tongnap + ? WHERE id = ?"
//                );
//
//                pst.setLong(1, amt);
//                // SỬA: Dùng ID tài khoản thay vì tên nhân vật
//                pst.setInt(2, player.userId);
//
//                int updated = pst.executeUpdate();
//
//                if (updated == 0) {
//                    errMsg("Cập nhật SQL thất bại: Không tìm thấy ID tài khoản trong bảng account!");
//                    return;
//                }
//
//                System.out.println("Rows affected (Account table): " + updated);
//            } catch (SQLException ex) {
//                errMsg("Lỗi cập nhật SQL: " + ex.getMessage());
//                return;
//            }
//
//            // ====== Cập nhật object ======
//            // Vẫn cộng vào player để hiển thị ngay trong game mà không cần relogin
//            player.tongnap += amt;
//
//            // **Cập nhật số tiền nạp lần này để mốc nạp check đúng**
//            player.lastNapAmount = amt;
//
//            // ====== Gửi thông báo ======
//            if (player.conn != null) {
//                Service.send_notice_box(player.conn, "Bạn đã được cộng nạp vào tài khoản: " + amt + " VNĐ");
//            }
//
//            // ====== Log Admin ======
//            appendColored(String.format("Buff tổng nạp ACCOUNT cho %s (ID: %d): +%d VNĐ\n", player.name, player.userId, amt), NEON_PINK);
//            okMsg("Đã buff tổng nạp cho tài khoản của " + player.name + ": " + amt);
//
//        } catch (NumberFormatException ex) {
//            errMsg("Số tiền không hợp lệ!");
//        } catch (Exception ex) {
//            errMsg("Lỗi: " + ex.getMessage());
//            ex.printStackTrace();
//        }
    }



   }


    private List<Session> snapshotSessions() {
        synchronized (Session.client_entry) {
            return new ArrayList<>(Session.client_entry);
        }
    }

    private long usedMem() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    @Override
    public void windowClosing(WindowEvent e) {
        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Đóng bảng điều khiển và TẮT SERVER sau khi lưu dữ liệu?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        frame.setEnabled(false);
        JDialog wait = new JDialog(frame, "Đang lưu dữ liệu & tắt server...", true);
        wait.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(14, 14, 14, 14));
        p.add(new JLabel("Vui lòng chờ..."), BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        wait.setContentPane(p);
        wait.setSize(360, 120);
        wait.setLocationRelativeTo(frame);

        Runnable task = () -> {
            try {
                shutdownServerGracefully();
            } finally {
                SwingUtilities.invokeLater(() -> {
                    wait.dispose();
                    System.exit(0);
                });
            }
        };
        try {
       new Thread(task, "CPanel-Shutdown").start();


        } catch (Throwable ignored) {
            new Thread(task, "CPanel-Shutdown").start();
        }
        wait.setVisible(true);
    }

    private void updateInfo() {
        try {
            Runtime rt = Runtime.getRuntime();
            long total = rt.totalMemory();
            long used = usedMem();
            int memPct = (int) Math.min(100, Math.round((used * 100.0) / total));
            memBar.setValue(memPct);
            memBar.setString(String.format("%.2f / %.2f MB (%d%%)", used / 1048576.0, total / 1048576.0, memPct));

            double cpu = getProcessCpu();
            int cpuPct = (int) Math.max(0, Math.min(100, Math.round(cpu)));
            cpuBar.setValue(cpuPct);
            cpuBar.setString(String.format("%.2f%%", cpu));

            int totalConn = 0, logged = 0;
            for (Session s : snapshotSessions()) {
                totalConn++;
                if (s != null && s.isLogin) {
                    logged++;
                }
            }
            onlineLabel.setText("Online: " + logged + " / " + totalConn);

            updateThreadMetrics();

            StyledDocument doc = logPane.getStyledDocument();
            if (doc.getLength() == 0) {
                appendColored("Khởi tạo CPanel thành công.\n", NEON_PINK);
            }
        } catch (Exception ignored) {
        }
    }

    private void updateThreadMetrics() {
        int live = THREAD_MX.getThreadCount();
        int peak = THREAD_MX.getPeakThreadCount();
        int daemon = 0;
        EnumMap<Thread.State, Integer> states = new EnumMap<>(Thread.State.class);
        for (Thread.State s : Thread.State.values()) {
            states.put(s, 0);
        }

        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t == null || !t.isAlive()) {
                continue;
            }
            if (t.isDaemon()) {
                daemon++;
            }
            Thread.State st = t.getState();
            states.put(st, states.get(st) + 1);
        }
        int nonDaemon = Math.max(0, live - daemon);
        threadLabel.setText("Threads: " + live + " (daemon " + daemon + ", non " + nonDaemon + ")");
        threadLabel.setToolTipText(
                "<html>live=" + live + ", peak=" + peak
                + "<br>RUNNABLE=" + states.get(Thread.State.RUNNABLE)
                + ", BLOCKED=" + states.get(Thread.State.BLOCKED)
                + "<br>WAITING=" + states.get(Thread.State.WAITING)
                + ", TIMED_WAITING=" + states.get(Thread.State.TIMED_WAITING)
                + "</html>"
        );
    }

    private double getProcessCpu() {
        try {
            OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double v = os.getProcessCpuLoad();
            if (v >= 0) {
                return v * 100.0;
            }
        } catch (Throwable ignored) {
        }
        return 0.0;
    }

    private void appendColored(String text, Color color) {
        StyledDocument doc = logPane.getStyledDocument();
        SimpleAttributeSet as = new SimpleAttributeSet();
        StyleConstants.setForeground(as, color);
        try {
            doc.insertString(doc.getLength(), text, as);
        } catch (BadLocationException ignored) {
        }
    }

    // ======= Visual components used elsewhere (giữ nguyên) =======
    private static class GradientPanel extends JPanel {

        private float hue = 0.58f;
        private final List<Point2D> stars = new ArrayList<>();
        private int tick = 0;

        GradientPanel() {
            setOpaque(true);
            Random r = new Random();
            int count = 40;
            for (int i = 0; i < count; i++) {
                stars.add(new Point2D(r.nextFloat(), r.nextFloat(), 0.6f + r.nextFloat() * 1.4f));
            }
            new javax.swing.Timer(80, e -> onTick()).start();
        }

        private void onTick() {
            if (!isShowing()) {
                return;
            }
            tick++;
            if ((tick % 2) == 0) {
                hue += 0.0007f;
                if (hue > 1) {
                    hue -= 1;
                }
            }
            for (Point2D s : stars) {
                s.y += 0.0005f * s.spd;
                if (s.y > 1.05f) {
                    s.y = -0.05f;
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth(), h = getHeight();
            Color top = blend(BG_TOP, Color.getHSBColor(hue, 0.22f, 0.24f), 0.22f);
            Color bottom = blend(BG_BOTTOM, Color.getHSBColor((hue + 0.08f) % 1f, 0.32f, 0.34f), 0.22f);
            GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bottom);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(255, 255, 255, 120));
            for (Point2D s : stars) {
                int x = (int) (s.x * w), y = (int) (s.y * h);
                g2.fillRect(x, y, 2, 2);
            }
            g2.dispose();
        }

        private Color blend(Color a, Color b, float t) {
            int r = (int) (a.getRed() * (1 - t) + b.getRed() * t);
            int g = (int) (a.getGreen() * (1 - t) + b.getGreen() * t);
            int bl = (int) (a.getBlue() * (1 - t) + b.getBlue() * t);
            return new Color(r, g, bl);
        }

        private static class Point2D {

            float x, y, spd;

            Point2D(float x, float y, float spd) {
                this.x = x;
                this.y = y;
                this.spd = spd;
            }
        }
    }

    private static class GlassCard extends JPanel {

        private final int arc;

        GlassCard(int arc) {
            this.arc = arc;
            setOpaque(false);
            setBackground(CARD_BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 60));
            for (int i = 0; i < 6; i++) {
                g2.fillRoundRect(3 - i, 6 - i, w - 6 + i * 2, h - 6 + i * 2, arc + i, arc + i);
            }
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            g2.setColor(CARD_STROKE);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class NeonScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(120, 90, 180, 180);
            trackColor = new Color(45, 34, 66, 160);
        }

        @Override
        protected Dimension getMinimumThumbSize() {
            return new Dimension(10, 44);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(r.x, r.y, NEON_PURPLE, r.x + r.width, r.y + r.height, NEON_PINK);
            g2.setPaint(gp);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 12, 12);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(trackColor);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g2.dispose();
        }
    }

    private static class NeonProgress extends JProgressBar {

        private int phase = 0;

        NeonProgress() {
            super(0, 100);
            setOpaque(false);
            setForeground(NEON_PINK);
            setBackground(new Color(54, 41, 79));
            new javax.swing.Timer(80, e -> {
                if (!isShowing()) {
                    return;
                }
                phase = (phase + 3) % 1000;
                repaint();
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(40, 30, 60, 180));
            g2.fillRoundRect(0, 0, w, h, h, h);
            int fill = (int) Math.round((w) * (getValue() / 100.0));
            GradientPaint gp = new GradientPaint(0, 0, NEON_PURPLE, w, 0, NEON_PINK);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, fill, h, h, h);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
            g2.setColor(Color.WHITE);
            int stripeW = 24, offset = phase / 3;
            for (int x = -w; x < fill + w; x += stripeW) {
                g2.fillRect(x + offset, 0, 10, h);
            }
            g2.setComposite(AlphaComposite.SrcOver);
            if (isStringPainted()) {
                String s = getString();
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Color.WHITE);
                g2.drawString(s, (w - fm.stringWidth(s)) / 2, (h + fm.getAscent()) / 2 - 2);
            }
            g2.dispose();
        }
    }

    private static class GlassPill extends JComponent {

        private final JLabel inner;

        GlassPill(JLabel inner) {
            this.inner = inner;
            setLayout(new BorderLayout());
            setOpaque(false);
            add(inner, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillRoundRect(0, 0, w, h, h, h);
            GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 255, 70), w, h, new Color(255, 255, 255, 25));
            g2.setPaint(gp);
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        new AdminPanel();
    }
}
