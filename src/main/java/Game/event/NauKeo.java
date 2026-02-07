/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Game.event;



import Game.core.Manager;
import java.io.IOException;

import java.io.IOException;

/**
 *
 * @author Administrator
 */
public class NauKeo {
    public int h;
	public int m;
	public int time = 0;

	public synchronized void start() {
		time = 120;
	}

	public synchronized void update(int t) throws IOException {
		if (time != 0) {
			time -= t;
			System.out.println("time NauKeo: "+ time);
			if (time <= 0) {
				time = 0;
				Manager.gI().chatKTGprocess("@Server: Thời gian nấu kẹo đã xong, mời các anh hùng đến nhận kẹo");
				Event_1.finish();
			}
		}
	}
}
