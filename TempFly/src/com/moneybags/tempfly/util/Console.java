package com.moneybags.tempfly.util;

import java.util.logging.Logger;

import com.moneybags.tempfly.TempFly;

public class Console {
	
	protected static TempFly tempfly;
	
	public Console(TempFly plugin) {
		tempfly = plugin;
	}
	
	public static void generateException(String message) {
		try {throw new Exception(message);} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Logger getLogger() {
		return tempfly.getLogger();
	}
	
	public static void info(String... m){
		for (String s: m) {info(s);}
 	}
	
	public static void info(String m){
		getLogger().info(m);
	}
	
	public static void warn(String... m){
		for (String s: m) {warn(s);}
	}
	
	public static void warn(String m){
		getLogger().warning(m);
	}
	
	public static void severe(String... m){
		for (String s: m) {severe(s);}
	}
	
	public static void severe(String m){
		getLogger().severe(m);
	}
	
	/**
	 * lmao
	 */
	public static void extreme(String m) {
		for (int x = 0; x < 23; x++) severe((x > 10 && x < 13) ? m : (x == 11 || x == 13) ? "--------------" : "!!!");
	}

	public static void debug(Object obj) {
		if (V.debug) {getLogger().info("[DEBUG] " + String.valueOf(obj));}
	}
	
	public static void debug(Object... objects) {
		if (V.debug) {
			for (Object obj: objects) {getLogger().info("[DEBUG] " + String.valueOf(obj));}
		}
	}
}
