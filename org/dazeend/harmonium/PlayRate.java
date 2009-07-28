/*
 * Copyright 2008 Charles Perry
 *
 * This file is part of Harmonium, the TiVo music player.
 *
 * Harmonium is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Harmonium is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Harmonium.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 
package org.dazeend.harmonium;

/**
 * Represents the rate at which a track should be played.
 * 
 * @author Charles Perry (harmonium@DazeEnd.org)
 *
 */
public enum PlayRate {
		REWx3(-60.0f, "speedup3.snd", "NORMAL", "REWx2"), 
		REWx2(-15.0f, "speedup2.snd", "REWx3", "REWx2"), 
		REWx1(-3.0f, "speedup1.snd", "REWx2", "NORMAL"), 
		PAUSE(0.0f, "select.snd", "REWx1", "FFx1"),
		STOP(0.0f, "select.snd", "STOP", "STOP"),
		NORMAL(1.0f, "slowdown1.snd", "REWx1", "FFx1"), 
		FFx1(3.0f, "speedup1.snd", "NORMAL", "FFx2"), 
		FFx2(15.0f, "speedup2.snd", "FFx1", "FFx3"), 
		FFx3(60.0f, "speedup3.snd", "FFx2", "NORMAL");
		
		
		private float speed;
		private String sound;
		private String nextREW;
		private String nextFF;
		
		PlayRate(float speed, String sound, String nextREW, String nextFF) {
			this.speed = speed;
			this.sound = sound;
			this.nextREW = nextREW;
			this.nextFF = nextFF;
		}

		/**
		 * @return the speed
		 */
		public float getSpeed() {
			return this.speed;
		}

		/**
		 * @return the sound
		 */
		public String getSound() {
			return sound;
		}

		/**
		 * @return the nextREW
		 */
		public PlayRate getNextREW() {
			return PlayRate.valueOf(this.nextREW);
		}

		/**
		 * @return the nextFF
		 */
		public PlayRate getNextFF() {
			return PlayRate.valueOf(this.nextFF);
		}
		
		
		
}
