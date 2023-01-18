package fr.wonder;

import fr.wonder.commons.utils.StringUtils;

public class Logger {
	
	private final String name;
	
	public Logger(String name) {
		this.name = name;
	}
	
	public void log(String format, Object... args) {
		System.out.println("\u001b[33m[" + name + "]\u001b[0m " + StringUtils.formatAnsi(format, args));
	}
	
}
