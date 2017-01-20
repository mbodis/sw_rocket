package sk.svb.smartwatches.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Point;

public class Stars {

	int width, height;

	private static final int MAX_SIZE = 5;
	private static final int MIN_SPEED = 1;
	private static final int MAX_SPEED = 3;
	private static final int NUM_STARS = 20;
	private List<Star> stars = new ArrayList<Star>();

	Random rand;

	public Stars(int width, int height) {
		this.width = width;
		this.height = height;
		rand = new Random();
		initStars();

	}

	public List<Star> getStarList() {
		return this.stars;
	}

	public void updateStars() {

		for (Star star : stars) {
			if (star.y > height) {				
				star.set(rand.nextInt(width), 0);
				star.size = rand.nextInt(MAX_SIZE);
				star.speed = rand.nextInt(MAX_SPEED) + MIN_SPEED;
			}else{
				star.y += star.speed;
			}
		}
	}

	private void initStars() {
		for (int i = 0; i < NUM_STARS; i++) {
			int gx = rand.nextInt(width);
			int gy = rand.nextInt(height);
			int gsi = rand.nextInt(MAX_SIZE);
			int gspd = rand.nextInt(MAX_SPEED) + MIN_SPEED;
			stars.add(new Star(gx, gy, gspd, gsi));
		}
	}

	public class Star extends Point {
		public int speed = 0;
		public int size = 1;

		public Star(int x, int y, int speed, int size) {
			super(x, y);
			this.speed = speed;
			this.size = size;
		}
	}
}
