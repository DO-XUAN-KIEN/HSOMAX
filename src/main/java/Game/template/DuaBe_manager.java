package Game.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuaBe_manager {
    private static final HashMap<String, DuaBe> list = new HashMap<>();
    public static List<DuaBe> DuaBe = new ArrayList<>();
    public static synchronized void add(String name, DuaBe temp) {
        DuaBe_manager.list.put(name, temp);
    }

    public static synchronized void remove(String name) {
        DuaBe_manager.list.remove(name);
    }

    public static synchronized DuaBe check(int n) {
        for (Map.Entry<String, DuaBe> en : DuaBe_manager.list.entrySet()) {
            DuaBe temp = en.getValue();
            if (temp.ID == n) {
                return temp;
            }
        }
        return null;
    }
}