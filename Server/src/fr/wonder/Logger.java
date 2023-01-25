package fr.wonder;

import fr.wonder.commons.utils.StringUtils;

public class Logger {
	
	private static final boolean USE_ANSI = Launcher.DEBUG_ENV; // windows cmd shell does not support ansi color codes
	
	private final String name;
	
	public Logger(String name) {
		this.name = name;
	}
	
	public void log(String format, Object... args) {
		if(USE_ANSI) {
			System.out.println("\u001b[33m[" + name + "]\u001b[0m " + StringUtils.formatAnsi(format, args));
		} else {
			System.out.println(String.format("[%s] %s", name, String.format(format.replaceAll("\\$.", ""), args)));
		}
	}
	
}
