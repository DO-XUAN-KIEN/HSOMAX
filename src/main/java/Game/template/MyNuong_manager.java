package Game.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyNuong_manager {
    private static final HashMap<String, MyNuong> list = new HashMap<>();
    public static List<MyNuong> MyNuong = new ArrayList<>();
    public static synchronized void add(String name, MyNuong temp) {
        MyNuong_manager.list.put(name, temp);
    }

    public static synchronized void remove(String name) {
        MyNuong_manager.list.remove(name);
    }

    public static synchronized MyNuong check(int n) {
        for (Map.Entry<String, MyNuong> en : MyNuong_manager.list.entrySet()) {
            MyNuong temp = en.getValue();
            if (temp.ID == n) {
                return temp;
            }
        }
        return null;
    }
}