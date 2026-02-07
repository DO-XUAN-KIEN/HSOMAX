package Game.template;

import Game.core.SQL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList; // Nhớ import ArrayList
import java.util.HashMap;
import java.util.List;      // Nhớ import List
import java.util.Map;

public class WearEffect {

    public static class EffectInfo {
        public int type;
        public int effId;

        public EffectInfo(int type, int effId) {
            this.type = type;
            this.effId = effId;
        }
    }
    public static final Map<Integer, List<EffectInfo>> effData = new HashMap<>();

    public static void loadWearEffects() {
        effData.clear();
        try (Connection conn = SQL.gI().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM `item_wear_effect`")) {

            while (rs.next()) {
                int itemId = rs.getInt("item_id");
                int type = rs.getInt("type");
                int effId = rs.getInt("eff_id");
                effData.computeIfAbsent(itemId, k -> new ArrayList<>()).add(new EffectInfo(type, effId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Hàm lấy danh sách effect
    public static List<EffectInfo> getListEffect(int itemId) {
        return effData.get(itemId);
    }
}