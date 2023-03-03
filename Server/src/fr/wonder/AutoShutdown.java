package fr.wonder;

public class AutoShutdown {
	
	private static final float SHUTDOWN_INACTIVITY_THRESHOLD = 500;
	private static final float SHUTDOWN_INFO_DELAY = 450;
	
	private static final Logger logger = new Logger("shutdown");
	
	private boolean paused;
	private boolean delayed;
	private float delayTime;
	private boolean printedInfo;
	
	public void delay() {
		delayed = true;
	}
	
	public void pause() {
		paused = true;
	}
	
	public void resume() {
		paused = false;
	}
	
	public boolean shouldShutdown(float time) {
		if(delayed) {
			delayTime = time;
			delayed = false;
			printedInfo = false;
		}
		if(!printedInfo && time-delayTime > SHUTDOWN_INFO_DELAY) {
			logger.log("Shuting down in %ds", (int)(SHUTDOWN_INACTIVITY_THRESHOLD-SHUTDOWN_INFO_DELAY));
			printedInfo = true;
		}
		return !paused && time - delayTime > SHUTDOWN_INACTIVITY_THRESHOLD;
	}
	
}
