package chokistream;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeSet;

public enum Logger {
	INSTANCE;
	
	private File logFile;
	private PrintWriter fileWriter;
	private LogMode mode;
	private LogLevel level;
	private TreeSet<String> onceLogged;
	
	public void init(LogMode mode, LogLevel level, String file) {
		this.mode = mode;
		this.level = level;
		onceLogged = new TreeSet<>();
		logFile = new File(file);
		try {
			if(!logFile.exists()) {
				logFile.createNewFile();
			}
			fileWriter = new PrintWriter(new FileWriter(logFile));
		} catch (IOException e) {
			System.err.println("Failed to initialize logger!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Logs a message based on current logging settings
	 * @param message The message to be logged
	 * @param l The log level of the message
	 */
	public void log(String message, LogLevel l) {
		if(l == LogLevel.REGULAR && level == LogLevel.VERBOSE) {
			return;
		}
		if(mode == LogMode.CONSOLE || mode == LogMode.BOTH) {
			System.out.println(message);
		}
		if(mode == LogMode.FILE || mode == LogMode.BOTH) {
			fileWriter.println(message);
		}
	}
	
	/**
	 * Logs a message only if it hasn't been logged before by logOnce
	 * @param message The message to be logged
	 * @param l The log level of the message
	 */
	public void logOnce(String message, LogLevel l) {
		if(l == LogLevel.REGULAR && level == LogLevel.VERBOSE) {
			return;
		}
		if(!onceLogged.contains(message)) {
			onceLogged.add(message);
			log(message, l);
		}
	}
	
	/**
	 * Logs a message at regular level
	 * @param message The message to be logged
	 */
	public void log(String message) {
		log(message, LogLevel.REGULAR);
	}
	
	/**
	 * Logs a message only if it hasn't been logged before by logOnce at regular level
	 * @param message The message to be logged
	 */
	public void logOnce(String message) {
		logOnce(message, LogLevel.REGULAR);
	}
	
	public enum LogMode {
		CONSOLE, FILE, BOTH
	}
	
	public enum LogLevel {
		REGULAR, VERBOSE
	}
}
