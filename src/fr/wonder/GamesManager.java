package fr.wonder;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import fr.wonder.commons.loggers.SimpleLogger;
import fr.wonder.commons.systems.process.ProcessUtils;

public class GamesManager implements AutoCloseable {
	
	private Logger logger = new Logger("games");
	
	private Process process;
	private Semaphore processSync = new Semaphore(0);
	private Thread redirectionThread;
	private boolean stopped;
	
	public GamesManager() {
		redirectionThread = new Thread() {
			@Override
			public void run() {
				while(!stopped) {
					try {
						processSync.acquire();
						ProcessUtils.redirectOutputSync(process, new SimpleLogger("#game"));
					} catch (InterruptedException x) {
						break;
					} finally {
						if(process != null && process.isAlive()) {
							logger.log("$rRedirection interrupted, stopping game forcibly");
							process.destroy();
						}
					}
				}
			}
		};
		redirectionThread.start();
	}
	
	public synchronized void runGame(GameInfo game) {
		if(process != null)
			throw new IllegalStateException("A game is already running");
		if(stopped)
			throw new IllegalStateException("The manager was stopped");
		try {
			logger.log("Running $g%s", String.join(" ", game.launchArgs));
			process = Runtime.getRuntime().exec(game.launchArgs, null, game.gameDirectory);
			processSync.release();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized void close() {
		stopped = true;
		redirectionThread.interrupt();
	}
	
	public boolean isGameRunning() {
		return process != null;
	}
	
}
