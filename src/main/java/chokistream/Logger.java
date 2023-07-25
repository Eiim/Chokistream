package chokistream;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeSet;

import chokistream.props.LogLevel;
import chokistream.props.LogMode;

public enum Logger {
	INSTANCE;
	
	private File logFile;
	private PrintWriter fileWriter;
	private LogMode mode;
	private LogLevel level;
	private TreeSet<String> onceLogged;
	
	public void init(LogMode mode, LogLevel level, String file) {
		onceLogged = new TreeSet<>();
		setLevel(level);
		setFile(file);
		setMode(mode); // Important to do this after setFile() to make sure it doesn't try to set up a null file
	}
	
	public void setMode(LogMode m) {
		if((mode == LogMode.CONSOLE || mode == null) && (m == LogMode.FILE || m == LogMode.BOTH)) {
			// If we're just now starting to log to a file, make sure it's initialized
			mode = m;
			initFile();
		} else if((m == LogMode.CONSOLE || m == null) && (mode == LogMode.FILE || mode == LogMode.BOTH)) {
			// If we're no longer writing to a file, close it
			mode = m;
			fileWriter.close();
		} else {
			mode = m;
		}
		
	}
	
	public void setLevel(LogLevel l) {
		level = l;
	}
	
	public void setFile(String f) {
		File newFile = new File(f);
		if(!newFile.equals(logFile)) {
			logFile = newFile;
			if(mode == LogMode.FILE || mode == LogMode.BOTH) {
				initFile();
			}
		}
	}
	
	private void initFile() {
		try {
			if(!logFile.exists()) {
				logFile.createNewFile();
			}
			fileWriter = new PrintWriter(new FileWriter(logFile));
		} catch (IOException e) {
			System.err.println("Failed to write to log file "+logFile.getPath());
		}
	}
	
	public void close() {
		if(fileWriter != null)
			fileWriter.close();
	}
	
	/**
	 * Logs a message based on current logging settings
	 * @param message The message to be logged
	 * @param l The log level of the message
	 */
	public void log(String message, LogLevel l) {
		if(level.getLevelDouble() >= l.getLevelDouble()) {
			if(mode == LogMode.CONSOLE || mode == LogMode.BOTH) {
				System.out.println(message);
			}
			if(mode == LogMode.FILE || mode == LogMode.BOTH) {
				fileWriter.append(message+System.lineSeparator());
				fileWriter.flush();
			}
		}
	}
	
	/**
	 * Logs a message only if it hasn't been logged before by logOnce
	 * @param message The message to be logged
	 * @param l The log level of the message
	 */
	public void logOnce(String message, LogLevel l) {
		if(level.getLevelDouble() >= l.getLevelDouble()) {
			if(!onceLogged.contains(message)) {
				onceLogged.add(message);
				log(message, l);
			}
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
	
	/**
	 * We sometimes want to print out hex strings, but we don't want to have to convert
	 * them if we aren't going to actually use them. We have a helper method for this now.
	 * @param bytes A byte array to log
	 * @param l The level to log at
	 */
	public void log(byte[] bytes, LogLevel l) {
		if(level.getLevelDouble() >= l.getLevelDouble()) {
			log(bytesToHex(bytes), l);
		}
	}
	
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
