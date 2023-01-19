package fr.wonder;

import static fr.wonder.Renderer.*;
import static fr.wonder.Audio.*;

import java.io.File;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import fr.wonder.GameInfo.GameTag;
import fr.wonder.Launcher.MenuController.MenuState;
import fr.wonder.audio.AudioManager;
import fr.wonder.gl.GLWindow;

public class Launcher {
	
	public static final File GAMES_DIR = new File("games");
	
	private static List<GameInfo> games;
	private static GameInfo selectedGame;
	
	private static MenuController menu;
	private static GamesManager gamesManager;
	
	private static GamesList gamesList;
	private static GameDetails gameDetails;
	private static Background background;
	private static PlayingPanel playingPanel;
	
	public static void main(String[] args) {
		GLWindow.createWindow(1600, 900);
		AudioManager.createSoundSystem();
		
		games = GameInfoParser.parseGameInfos();
		GameInfo dummy = new GameInfo();
		dummy.title = "Dummy";
		dummy.description = "A template description spanning\nmultiple lines";
		dummy.creationDate = "today";
		dummy.authors = new String[] { "albin calais", "charles caillon" };
		dummy.tags = new GameTag[] { GameTag.VERSUS, GameTag.COOP, GameTag.PLATFORMER };
		for(int i = 0; i < 10; i++)
			games.add(dummy);
		
		try (GamesManager gamesManager = new GamesManager()) {
			long firstMillis = System.currentTimeMillis();

			menu = new MenuController();
			gamesList = new GamesList();
			gameDetails = new GameDetails();
			background = new Background();
			playingPanel = new PlayingPanel();
			Launcher.gamesManager = gamesManager;
			
			Renderer.updateWinSize(GLWindow.getWinWidth(), GLWindow.getWinHeight());
			GLWindow.setResizeCallback(Renderer::updateWinSize);
			GLWindow.setKeyCallback(key -> {
				float time = (System.currentTimeMillis()-firstMillis)/1000.f;
				menu.processKey(time, key);
			});
			
			menu.start();
			
			GLWindow.show(false);
			Wintool.focusActiveWindow();
			
			while(!GLWindow.shouldDispose()) {
				float time = (System.currentTimeMillis()-firstMillis)/1000.f;
				menu.update();
				
				prepareFrame();
				background.render(time);
				gamesList.render(time);
				gameDetails.render(time);
				playingPanel.render(time);
				endFrame();
				GLWindow.sendFrame();
			}
		}
	}
	
	private static float lerpThroughTime(float current, float target) {
		current = current + (target-current)*.1f;
		return Math.abs(current-target) < .01f ? target : current;
	}
	
	static class Background {
		
		public void render(float time) {
			use(BACKGROUND_SHADER)
				.setUniform1f("u_time", time);
			renderQuad(0, 0, WIN_WIDTH, WIN_HEIGHT);
		}
		
	}
	
	static class MenuController {
		
		private MenuState state = MenuState.IDLE;
		private int quitGameKeyCount = 3;
		
		public void start() {
			MUSIC_SOURCE
				.setLooping(true)
				.setSound(MUSIC)
				.play();
		}
		
		public void processKey(float time, int key) {
			switch(state) {
			case IDLE:
				state = MenuState.MAIN_MENU;
				SFX_SOURCES.play(SFX_TRANSITION);
				break;
			case MAIN_MENU:
				if(key == GLFW.GLFW_KEY_ENTER) {
					gamesManager.runGame(selectedGame);
					playingPanel.setGameStartTime(time);
					MUSIC_SOURCE.pause();
					Wintool.focusGameLater(2.f);
					state = MenuState.PLAYING;
				} else {
					gamesList.processKey(key);
				}
				break;
			case PLAYING:
				if(key == GLFW.GLFW_KEY_ESCAPE) {
					quitGameKeyCount--;
					if(quitGameKeyCount == 0) {
						gamesManager.killGame();
						quitGameKeyCount = 3;
					}
					playingPanel.setForceQuitKeyCount(quitGameKeyCount == 3 ? 0 : quitGameKeyCount);
				}
				break;
			}
		}
		
		public void update() {
			switch (state) {
			case IDLE: break;
			case MAIN_MENU: break;
			case PLAYING:
				if(!gamesManager.isGameRunning()) {
					state = MenuState.MAIN_MENU;
					MUSIC_SOURCE.resume();
				}
			}
		}
		
		public MenuState getState() {
			return state;
		}
		
		static enum MenuState {
			IDLE, MAIN_MENU, PLAYING;
		}
		
	}

	static class GamesList {

		private static final float DISPLAY_SIZE = WIN_WIDTH*.5f;
		private static final float MARGIN = DISPLAY_SIZE*.1f;
		private static final float CARTRIDGE_WIDTH = (DISPLAY_SIZE-MARGIN*3)/2;
		private static final float CARTRIDGE_HEIGHT = CARTRIDGE_WIDTH*1.4f; // based on the texture file
		
		private int currentSelection;
		private float currentScroll;

		private float currentX = -WIN_WIDTH*.55f;
		
		public GamesList() {
			currentSelection = Math.min(2, games.size());
			selectedGame = games.get(currentSelection);
		}

		public void processKey(int key) {
			int newSelection;
			
			switch(key) {
			case GLFW.GLFW_KEY_LEFT:
			case GLFW.GLFW_KEY_RIGHT:
				newSelection = currentSelection ^ 1;
				break;
			case GLFW.GLFW_KEY_UP:
				newSelection = currentSelection + 2;
				break;
			case GLFW.GLFW_KEY_DOWN:
				newSelection = currentSelection - 2;
				break;
			default:
				return;
			}
			
			if(newSelection < 0 || newSelection >= games.size())
				return;
			
			currentSelection = newSelection;
			selectedGame = games.get(currentSelection);
			SFX_SOURCES.play(SFX_SELECTION);
		}
		
		void render(float time) {
			currentX = lerpThroughTime(currentX, menu.getState() == MenuState.MAIN_MENU ? 0 : -WIN_WIDTH*.55f);
			
			use(TEXTURE_SHADER);
			
			float targetScroll = - currentSelection/2 * CARTRIDGE_HEIGHT;
			currentScroll = currentScroll + (targetScroll-currentScroll)*.1f;
			if(Math.abs(targetScroll - currentScroll) < .01f)
				currentScroll = targetScroll;
			
			for(int i = 0; i < games.size(); i++) {
				renderCartridge(
						currentX + MARGIN + (i%2)*(MARGIN+CARTRIDGE_WIDTH),
						WIN_HEIGHT/2-CARTRIDGE_HEIGHT/2 + i/2*CARTRIDGE_HEIGHT + currentScroll,
						CARTRIDGE_WIDTH,
						games.get(i), time, currentSelection == i);
			}
		}
		
		private void renderCartridge(float x, float y, float size, GameInfo game, float time, boolean selected) {
			float width = size;
			float height = size*CARTRIDGE_FG.height/CARTRIDGE_BG.width;
			use(CARTRIDGE_SHADER)
				.setUniform1f("u_time", time)
				.setUniform1i("u_selected", selected?1:0);
			renderQuad(x, y, width, height, CARTRIDGE_BG);
			if(game.cartridgeTexture != null) {
				use(TEXTURE_SHADER);
				renderQuad(x+width*.46f, y+height*.3f, width*.46f, height*.64f, game.cartridgeTexture);
				use(CARTRIDGE_SHADER);
				renderQuad(x, y, width, height, CARTRIDGE_FG);
			}
		}
		
	}
	
	static class GameDetails {
		
		private static final float BIG_VIGNETTE_SIZE = WIN_HEIGHT * .45f;
		private static final float TITLE_SIZE = WIN_HEIGHT/15;
		private static final float DESCRIPTION_SIZE = TITLE_SIZE*.5f;
		
		float currentX = WIN_WIDTH*1.05f;
		
		public void render(float time) {
			currentX = lerpThroughTime(currentX, menu.getState() == MenuState.MAIN_MENU || menu.getState() == MenuState.PLAYING ? WIN_WIDTH*.5f : WIN_WIDTH*1.05f);
			
			use(TEXTURE_SHADER);
			renderQuad(currentX, 0, WIN_WIDTH*.5f, WIN_HEIGHT, INFO_BACKGROUND_TEXTURE);
			renderQuad(
					currentX + (WIN_WIDTH*.5f-BIG_VIGNETTE_SIZE)*.5f,
					WIN_HEIGHT*.5f + (WIN_HEIGHT*.5f-BIG_VIGNETTE_SIZE)*.5f,
					BIG_VIGNETTE_SIZE, BIG_VIGNETTE_SIZE,
					selectedGame.vignetteTexture == null ? WIP_TEXTURE : selectedGame.vignetteTexture);
			renderTextCentered(currentX+WIN_WIDTH*.25f, WIN_HEIGHT*.5f, TITLE_SIZE, FONT_TITLE, selectedGame.title);
			renderText(currentX+WIN_WIDTH*.05f, WIN_HEIGHT*.5f-TITLE_SIZE*1.5f, DESCRIPTION_SIZE, FONT_PLAIN, selectedGame.description);
			for(int i = 0; i < selectedGame.authors.length; i++)
				renderText(currentX+WIN_WIDTH*.05f, DESCRIPTION_SIZE*(.5f+i), DESCRIPTION_SIZE, FONT_PLAIN, selectedGame.authors[i]);
			renderText(currentX+WIN_WIDTH*.45f-FONT_PLAIN.getLineLength(selectedGame.creationDate, DESCRIPTION_SIZE),
					DESCRIPTION_SIZE*.5f, DESCRIPTION_SIZE, FONT_PLAIN, selectedGame.creationDate);
			for(int i = 0; i < selectedGame.tags.length; i++) {
				String text = selectedGame.tags[i].toString();
				renderText(currentX+WIN_WIDTH*.45f-FONT_PLAIN.getLineLength(text, DESCRIPTION_SIZE),
						DESCRIPTION_SIZE*(1.75f+i), DESCRIPTION_SIZE, FONT_PLAIN, text);
			}
			
		}
		
	}
	
	static class PlayingPanel {
		
		private float gameStartTime;
		private int forceQuitKeyCount;
		private float currentX = -WIN_WIDTH*.55f;
		
		public void setGameStartTime(float gameStartTime) {
			this.gameStartTime = gameStartTime;
		}
		
		public void render(float time) {
			currentX = lerpThroughTime(currentX, menu.getState() == MenuState.PLAYING ? 0 : -WIN_WIDTH*.55f);
			
			int playtime = (int)(time-gameStartTime);
			String playtimeStr = "";
			if(playtime > 3600) {
				playtimeStr += String.format("%02dh", playtime/3600);
				playtime %= 3600;
			}
			if(playtime > 60) {
				playtimeStr += String.format("%02dmin", playtime/60);
				playtime %= 60;
			}
			playtimeStr += String.format("%02ds", playtime);
			playtimeStr += ".".repeat(playtime%3+1);
			renderText(currentX + WIN_WIDTH*.2f, WIN_HEIGHT*.5f, WIN_HEIGHT*.05f, FONT_PLAIN, "Playing for\n" + playtimeStr);
			if(forceQuitKeyCount != 0)
				renderText(currentX + WIN_WIDTH*.05f, WIN_HEIGHT*.05f, WIN_HEIGHT*.025f, FONT_PLAIN, "Press <esc> " + forceQuitKeyCount + " more times to quit forcibly");
		}
		
		public void setForceQuitKeyCount(int remaining) {
			forceQuitKeyCount = remaining;
		}
		
	}

}


