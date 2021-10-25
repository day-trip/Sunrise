package com.daytrip.sunrise.util.math.interpolation;

public class TickingInterpolationTable extends InterpolationTable {
	private int ticks;

	public float get() {
		return get(ticks);
	}

	public void tick() {
		ticks++;
	}
}
