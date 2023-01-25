package fr.wonder;

import java.io.IOException;

import fr.wonder.commons.systems.process.ProcessUtils;

public class Wintool {
	
	private static final Logger logger = new Logger("wintool");
	
	public static void focusActiveWindow() {
		try {
			logger.log("Focusing active window");
			Runtime.getRuntime().exec(new String[] { "wintool.exe", "focus_game" });
		} catch (IOException e) {
			logger.log("Could not focus: %s", e.getMessage());
		}
	}
	
	public static void focusGameLater(float delay) {
		new Thread("wintool") {
			@Override
			public void run() {
				ProcessUtils.sleep((int)(delay*1000));
				focusActiveWindow();
			}
		}.start();
	}

	public static void shutdowComputer() {
		try {
			Process p = Runtime.getRuntime().exec(new String[] { "shutdown", "/p", "/f" });
			int status;
			if((status = p.waitFor()) != 0)
				logger.log("$rCould not shutdown computer, shutdown command exited with code %d", status);
		} catch (IOException | InterruptedException e) {
			logger.log("$rShutdown command error " + e);
		}
	}

}
