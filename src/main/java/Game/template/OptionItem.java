package Game.template;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OptionItem {
	public static final List<OptionItem> entry = new ArrayList<>();
	private String name;
	private byte color;
	private byte ispercent;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte getColor() {
		return color;
	}

	public void setColor(byte color) {
		this.color = color;
	}

	public byte getIspercent() {
		return ispercent;
	}

	public void setIspercent(byte ispercent) {
		this.ispercent = ispercent;
	}
}
