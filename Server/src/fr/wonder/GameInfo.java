package fr.wonder;

import java.io.File;

import fr.wonder.commons.utils.ArrayOperator;
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
	public GameMod[] mods;
	
	// read from meta/*.png files
	public Texture vignette;
	public Texture cartridge;
	public Texture controls;
	
	// read from game/scores.txt
	public Highscore[] highscores;
	// read from timestamps.txt
	public int totalPlaytime; // in seconds
	
	public boolean hasMod(GameMod mod) {
		return mods != null && ArrayOperator.contains(mods, mod);
	}
	
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
	
	public static enum GameMod {
		
		HIDE_LAUNCHER;
		
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
