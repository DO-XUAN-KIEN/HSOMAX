// ItemsLookupPanel.java
package Game.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.sql.SQLException;
import java.util.List;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class ItemsLookupPanel extends JDialog {

    private final JTextField tfSearch3 = new JTextField();
    private final JTextField tfSearch4 = new JTextField();
    private final JTextField tfSearch7 = new JTextField();

    private final Item3TableModel model3 = new Item3TableModel();
    private final SimpleItemTableModel model4 = new SimpleItemTableModel();
    private final SimpleItemTableModel model7 = new SimpleItemTableModel();

    private final JTable table3 = new JTable(model3);
    private final JTable table4 = new JTable(model4);
    private final JTable table7 = new JTable(model7);

    private final ItemsLookupDAO dao = new ItemsLookupDAO();

    public ItemsLookupPanel(Window owner) {
        super(owner, "Tra cứu vật phẩm", Dialog.ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        add(buildTabs(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        setupTable(table3);
        setupTable(table4);
        setupTable(table7);

        reloadItem3();
        reloadItem4();
        reloadItem7();
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Trang bị (item3)", buildTab(tfSearch3, table3, this::reloadItem3, () -> copySelectedItem3()));
        tabs.addTab("Vật phẩm nhiên liệu (item4)", buildTab(tfSearch4, table4, this::reloadItem4, () -> copySelectedSimple(table4, model4)));
        tabs.addTab("Vật phẩm nâng cấp (item7)", buildTab(tfSearch7, table7, this::reloadItem7, () -> copySelectedSimple(table7, model7)));

        return tabs;
    }

    private JPanel buildTab(JTextField tf, JTable table, Runnable reload, Runnable copy) {
        JPanel panel = new JPanel(new BorderLayout(8,8));
        panel.setBorder(new EmptyBorder(8,8,8,8));
        panel.add(buildTopBar(tf, reload, copy), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTopBar(JTextField tf, Runnable reload, Runnable copy) {
        JPanel top = new JPanel(new BorderLayout(8,8));
        JPanel left = new JPanel(new BorderLayout(6,6));
        left.add(new JLabel("Tìm theo tên/ID:"), BorderLayout.WEST);
        left.add(tf, BorderLayout.CENTER);

        JButton btReload = new JButton("Tải lại");
        JButton btCopy = new JButton("Copy dòng chọn");

        btReload.addActionListener(e -> reload.run());
        btCopy.addActionListener(e -> copy.run());
        tf.addActionListener(e -> reload.run());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(btCopy);
        right.add(btReload);

        top.add(left, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel buildBottomBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton close = new JButton("Đóng");
        close.addActionListener(e -> dispose());
        p.add(close);
        return p;
    }

    private void setupTable(JTable table) {
        table.setRowHeight(24);
        table.setRowSorter(new TableRowSorter<>(table.getModel()));
    }

    // ===== Reload =====
    private void reloadItem3() {
        try {
            List<ItemsLookupDAO.ItemRowFull> rows = dao.listItem3(tfSearch3.getText(), 5000);
            model3.setData(rows);
        } catch (SQLException ex) { showError(ex, "Lỗi tải item3"); }
    }

    private void reloadItem4() {
        try {
            List<ItemsLookupDAO.ItemRow> rows = dao.listItem4(tfSearch4.getText(), 1000);
            model4.setData(rows);
        } catch (SQLException ex) { showError(ex, "Lỗi tải item4"); }
    }

    private void reloadItem7() {
        try {
            List<ItemsLookupDAO.ItemRow> rows = dao.listItem7(tfSearch7.getText(), 1000);
            model7.setData(rows);
        } catch (SQLException ex) { showError(ex, "Lỗi tải item7"); }
    }

    // ===== Copy =====
    private void copySelectedItem3() {
        int row = table3.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn một dòng trước!"); return; }
        row = table3.convertRowIndexToModel(row);
        var r = model3.data.get(row);
        String text = r.id + "," + r.name + "," + r.type + "," + r.part + "," + r.clazz + "," +
                      r.iconid + "," + r.level + "," + r.data + "," + r.color;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        JOptionPane.showMessageDialog(this, "Đã copy: " + text);
    }

    private void copySelectedSimple(JTable table, SimpleItemTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn một dòng trước!"); return; }
        row = table.convertRowIndexToModel(row);
        var r = model.data.get(row);
        String text = r.id + "," + r.name;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        JOptionPane.showMessageDialog(this, "Đã copy: " + text);
    }

    private void showError(SQLException ex, String title) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "SQLState=" + ex.getSQLState() + "\nErrorCode=" + ex.getErrorCode() + "\n" + ex.getMessage(),
                title, JOptionPane.ERROR_MESSAGE);
    }

    // ===== TableModel =====
    static class Item3TableModel extends AbstractTableModel {
        private final String[] cols = {"ID","Tên","Type","Part","Clazz","IconID","Level","Data","Color"};
        private List<ItemsLookupDAO.ItemRowFull> data = List.of();
        public void setData(List<ItemsLookupDAO.ItemRowFull> data) { this.data = data == null ? List.of() : data; fireTableDataChanged(); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            var row = data.get(r);
            return switch(c) {
                case 0 -> row.id;
                case 1 -> row.name;
                case 2 -> row.type;
                case 3 -> row.part;
                case 4 -> row.clazz;
                case 5 -> row.iconid;
                case 6 -> row.level;
                case 7 -> row.data;
                case 8 -> row.color;
                default -> null;
            };
        }
        @Override public Class<?> getColumnClass(int c) { return (c==0||c==2||c==3||c==4||c==5||c==6||c==8)? Integer.class : String.class; }
        @Override public boolean isCellEditable(int r, int c) { return false; }
    }

    static class SimpleItemTableModel extends AbstractTableModel {
        private final String[] cols = {"ID","Tên"};
        private List<ItemsLookupDAO.ItemRow> data = List.of();
        public void setData(List<ItemsLookupDAO.ItemRow> data) { this.data = data == null ? List.of() : data; fireTableDataChanged(); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) { var row = data.get(r); return (c==0)?row.id:row.name; }
        @Override public Class<?> getColumnClass(int c) { return (c==0)?Integer.class:String.class; }
        @Override public boolean isCellEditable(int r,int c){ return false; }
    }
}
