package fr.wonder;

import java.io.File;

import fr.wonder.gl.Texture;

public class GameInfo {
	
	public String name;
	public String creationDate;
	public String description;
	public String[] creators;
	public String launchFile;
	public String[] launchArgs;
	public String musicFile;
	public String vignette;
	public GameTag[] tags;
	
	// generated
	
	public Texture vignetteTexture;
	public File gameDirectory;
	public File metaDirectory;
	// TODO music
	
	public static enum GameTag {
		
		VERSUS,
		COOP,
		SOLO,
		PLATFORMER,
		SHOOT_THEM_UP,
		BEAT_THEM_UP,
		RPG,
		GESTION,
		STRATEGY,
		PUZZLE,
		FIGHTING_GAME,
		
	}
	
}
