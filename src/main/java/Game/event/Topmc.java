/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Game.event;

import java.io.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class Topmc {
    

    private static final String FILE_PATH = "data/topmc.json";
    private static final Map<String, Integer> data = new HashMap<>();

    static {
        load();
    }

    public static void log(String name) {
        data.put(name, data.getOrDefault(name, 0) + 1);
        save();
    }

    public static String getTop() {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(data.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        StringBuilder sb = new StringBuilder("🏆 TOP mê cung (đổi tên mất TOP tự chịu) :\n");
        for (int i = 0; i < Math.min(10, list.size()); i++) {
            var e = list.get(i);
            sb.append(i + 1).append(". ").append(e.getKey()).append(": ")
              .append(e.getValue()).append(" lần\n");
        }
        return sb.toString();
    }

    private static void save() {
        try (FileWriter fw = new FileWriter(FILE_PATH)) {
            JSONObject obj = new JSONObject();
            for (var e : data.entrySet()) {
                obj.put(e.getKey(), e.getValue());
            }
            fw.write(obj.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        try (FileReader fr = new FileReader(file)) {
            JSONObject obj = (JSONObject) new JSONParser().parse(fr);
            for (Object key : obj.keySet()) {
                String name = (String) key;
                int count = ((Long) obj.get(key)).intValue();
                data.put(name, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void resetTop() {
    data.clear(); // Xóa toàn bộ dữ liệu trong bộ nhớ
    save();       // Lưu lại file JSON rỗng
}
}
