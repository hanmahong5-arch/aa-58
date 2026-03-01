package com.aionemu.gameserver.geoEngine.math;


public class Array3f {
	public float a = 0.0f;
	public float b = 0.0f;
	public float c = 0.0f;

	public void reset() {
		this.a = 0.0f;
		this.b = 0.0f;
		this.c = 0.0f;
	}

	public static Array3f newInstance() {
		return new Array3f();
	}

	public static void recycle(Array3f instance) {
		// No-op: object pooling removed
	}
}
