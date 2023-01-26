package fr.wonder;

import java.io.File;

import fr.wonder.gl.Texture;

public class GameInfo {
	
	// generated
	public File gameDirectory;
	public File metaDirectory;
	
	// read from meta/game.json
	public String title;
	public String creationDate;
	public String description;
	public String[] authors;
	public String[] runCommand;
	public GameTag[] tags;
	
	// read from meta/*.png files
	public Texture vignette;
	public Texture cartridge;
	public Texture controls;
	
	// read from game/scores.txt
	public Highscore[] highscores;
	
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
	
	public static class Highscore {
		
		public final String name;
		public final int score;
		public final String date;
		
		public Highscore(String name, int score, String date) {
			this.name = name;
			this.score = score;
			this.date = date;
		}
		
	}
	
}
