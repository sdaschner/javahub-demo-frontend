package com.hopding.jrpicam.enums;

/**
 * Exposure option modes.
 * 
 * @author Andrew Dillon
 */
public enum Exposure {
	
	/**
	 * Automatic exposure mode.
	 */
	AUTO,
	
	/**
	 * Exposure for night shooting.
	 */
	NIGHT,
	
	NIGHTPREVIEW,
	
	/**
	 * Exposure for back lit subject.
	 */
	BACKLIGHT,
	
	SPOTLIGHT,
	
	/**
	 * Exposure for sports (fast shutter etc)
	 */
	SPORTS,
	
	/**
	 * Exposure for snowy scenery.
	 */
	SNOW,
	
	/**
	 * Exposure for beach scenes.
	 */
	BEACH,
	
	/**
	 * Exposure for long takes.
	 */
	VERYLONG,
	
	/**
	 * Exposure for constraining FPS to fixed value.
	 */
	FIXEDFPS,
	
	/**
	 * Exposure for antishake mode.
	 */
	ANTISHAKE,
	
	FIREWORKS;
	
	/**
	 * Returns enum in lowercase.
	 */
	public String toString() {
		String id = name();
		return id.toLowerCase();
	}
}
