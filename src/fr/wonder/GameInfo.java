package fr.wonder;

import java.io.File;

import fr.wonder.gl.Texture;

public class GameInfo {
	
	public String title;
	public String creationDate;
	public String description;
	public String[] authors;
	public String[] runCommand;
	public String vignette;
	public String cartridgeImage;
	public GameTag[] tags;
	
	// generated

	public Texture vignetteTexture;
	public Texture cartridgeTexture;
	public File gameDirectory;
	public File metaDirectory;
	
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
		FIGHTING_GAME;
		
		@Override
		public String toString() {
			return name().toLowerCase().replaceAll("_", " ");
		}
		
	}
	
}
