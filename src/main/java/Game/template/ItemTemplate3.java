package Game.template;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemTemplate3 {
    public static final List<ItemTemplate3> item = new ArrayList<>();
    private short id;
    private String name;
    private short type;   // ✅
    private short part;   // ✅
    private short clazz;  // ✅
    private short icon;
    private short level;
    private List<Option> op;
    private short color;  // ✅

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public short getPart() {
        return part;
    }

    public void setPart(short part) {
        this.part = part;
    }

    public short getClazz() {
        return clazz;
    }

    public void setClazz(short clazz) {
        this.clazz = clazz;
    }

    public short getIcon() {
        return icon;
    }

    public void setIcon(short icon) {
        this.icon = icon;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public List<Option> getOp() {
        return op;
    }

    public void setOp(List<Option> op) {
        this.op = op;
    }

    public short getColor() {
        return color;
    }

    public void setColor(short color) {
        this.color = color;
    }


    public static ItemTemplate3 getTemplateById(short id) {
        for (ItemTemplate3 temp : item) {
            if (temp.id == id) {
                return temp;
            }
        }
        return null;
    }
}