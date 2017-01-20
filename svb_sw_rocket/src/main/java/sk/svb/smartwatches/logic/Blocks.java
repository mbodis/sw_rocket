package sk.svb.smartwatches.logic;

import java.util.Random;

import android.content.res.AssetManager;
import android.graphics.Rect;



public class Blocks {

	public static final int GATE_WIDTH = 40;
	public static final int GATE_HEIGTHT = 10;

	public static final int MIN_SPEED = 3;
	public static final int MAX_SPEED = 8;
	public static final double SPEED_STEP = 0.3;

	int width, height;
	public double speed = MIN_SPEED;
	
	int gate_1 = 40;
	int gate_2 = width - GATE_WIDTH - gate_1;
	
	public int x = 0;
	public int y = 0;
	
	boolean x_up = true;
	Random rand;
	boolean visited = false;
	boolean moving = false;

	int userPoints = 0;

	public Blocks(int width, int height) {
		this.width = width;
		this.height = height;
		rand = new Random();
	}

	public void update(int points, int gameType) {
		userPoints = points;
		y = y + (int)Math.floor(speed);

		if (moving) {
			if (x_up && x < GATE_WIDTH / 2) {
				x += speed / 3;
			} else if (x_up) {
				x_up = !x_up;
			} else if (!x_up && x > 0) {
				x -= speed / 3;
			} else if (!x_up) {
				x_up = !x_up;
			}

		}

		if (y >= height + GATE_HEIGTHT * 3 / 2) {
			y = -80;
			x = 0;
			visited = false;
			moving = false;

			gate_1 = rand.nextInt(width - GATE_WIDTH - 20 * 2) + 20;
			if (moving == false && points > 0 && points % 8 == 0) {
				moving = true;
			}
		}
	}

	public Rect getRect1() {
		return new Rect(x, y, x + gate_1, y + GATE_HEIGTHT);

	}

	public Rect getRect2() {
		return new Rect(-x + gate_1 + GATE_WIDTH, y, -x + width, y
				+ GATE_HEIGTHT);
	}

	public boolean throughGate(int rx, int ry) {
		if (!visited) {
			if (rx > gate_1 && rx < gate_1 + GATE_WIDTH && ry > y
					&& ry < y + GATE_HEIGTHT) {
				visited = true;

				if (speed < MAX_SPEED) {
					speed += SPEED_STEP;
				}

				return true;
			}
		}

		return false;
	}

	public boolean detectCollision(int rx, int ry) {
		if ((rx > 0 && rx < gate_1 && ry > y && ry < y + GATE_HEIGTHT)
				|| (rx > gate_1 + GATE_WIDTH && rx < width && ry > y && ry < y
						+ GATE_HEIGTHT)) {
			return true;
		} else {
			return false;
		}
	}

}
