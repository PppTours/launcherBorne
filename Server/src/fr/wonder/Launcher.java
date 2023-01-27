package fr.wonder;

import static fr.wonder.Audio.MUSIC;
import static fr.wonder.Audio.MUSIC_SOURCE;
import static fr.wonder.Audio.SFX_SELECTION;
import static fr.wonder.Audio.SFX_SOURCES;
import static fr.wonder.Audio.SFX_TRANSITION;
import static fr.wonder.Keys.KEY_BUTTONS;
import static fr.wonder.Keys.KEY_DIRECTIONS;
import static fr.wonder.Keys.KEY_DOWN;
import static fr.wonder.Keys.KEY_LEFT;
import static fr.wonder.Keys.KEY_QUIT1;
import static fr.wonder.Keys.KEY_QUIT2;
import static fr.wonder.Keys.KEY_RIGHT;
import static fr.wonder.Keys.KEY_START;
import static fr.wonder.Keys.KEY_UP;
import static fr.wonder.Keys.isKeyPressed;
import static fr.wonder.Renderer.BACKGROUND_SHADER;
import static fr.wonder.Renderer.CARTRIDGE_BG;
import static fr.wonder.Renderer.CARTRIDGE_FG;
import static fr.wonder.Renderer.CARTRIDGE_SHADER;
import static fr.wonder.Renderer.FONT_PLAIN;
import static fr.wonder.Renderer.FONT_TITLE;
import static fr.wonder.Renderer.INFO_BACKGROUND_TEXTURE;
import static fr.wonder.Renderer.TEXTURE_GLOBAL_CONTROLS;
import static fr.wonder.Renderer.TEXTURE_SHADER;
import static fr.wonder.Renderer.WIN_HEIGHT;
import static fr.wonder.Renderer.WIN_WIDTH;
import static fr.wonder.Renderer.WIP_TEXTURE;
import static fr.wonder.Renderer.endFrame;
import static fr.wonder.Renderer.prepareFrame;
import static fr.wonder.Renderer.renderQuad;
import static fr.wonder.Renderer.renderText;
import static fr.wonder.Renderer.renderTextCentered;
import static fr.wonder.Renderer.use;

import java.io.File;
import java.util.List;

import fr.wonder.GameInfo.GameMod;
import fr.wonder.GameInfo.Highscore;
import fr.wonder.Launcher.MenuController.MenuState;
import fr.wonder.audio.AudioManager;
import fr.wonder.commons.systems.process.ProcessUtils;
import fr.wonder.commons.utils.ArrayOperator;
import fr.wonder.gl.GLWindow;
import fr.wonder.gl.Texture;

public class Launcher {
	
	/* Note to the maintainer: don't */
	
	public static final File GAMES_DIR = new File("games");
	
	public static final boolean DEBUG_ENV = System.getenv().containsKey("DEBUG_ENV");
	
	private static final boolean WINDOW_FULLSCREEN = !DEBUG_ENV;
	
	private static List<GameInfo> games;
	private static GameInfo selectedGame;
	
	private static MenuController menu;
	private static GamesManager gamesManager;
	
	private static GamesList gamesList;
	private static GameDetails gameDetails;
	private static Background background;
	private static PlayingPanel playingPanel;
	private static PppLogo logo;
	
	public static void main(String[] args) {
		GLWindow.createWindow();
		AudioManager.createSoundSystem();
		
		games = GameInfoParser.parseGameInfos();
		
		try (GamesManager gamesManager = new GamesManager()) {
			long firstMillis = System.currentTimeMillis();

			menu = new MenuController();
			gamesList = new GamesList();
			gameDetails = new GameDetails();
			background = new Background();
			playingPanel = new PlayingPanel();
			logo = new PppLogo();
			Launcher.gamesManager = gamesManager;
			
			Renderer.updateWinSize(GLWindow.getWinWidth(), GLWindow.getWinHeight());
			GLWindow.setResizeCallback(Renderer::updateWinSize);
			GLWindow.setKeyCallback(key -> {
				float time = (System.currentTimeMillis()-firstMillis)/1000.f;
				menu.processKey(time, key);
			});
			
			menu.start();
			
			GLWindow.show(WINDOW_FULLSCREEN);
			Wintool.focusActiveWindow();
			
			while(!GLWindow.shouldDispose()) {
				float time = (System.currentTimeMillis()-firstMillis)/1000.f;
				menu.update(time);
				
				prepareFrame();
				background.render(time);
				gamesList.render(time);
				gameDetails.render(time);
				playingPanel.render(time);
				endFrame();
				logo.render(time); // must be after #endFrame(), 3D rendering uses the screen fbo
				GLWindow.sendFrame();
			}
		} finally {
			GLWindow.dispose();
			ProcessUtils.runLater(() -> System.exit(0), 1000); // forcibly exit other threads
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
	
	static class PppLogo {
	
		private float currentY = 0;
		
		public void render(float time) {
			currentY = lerpThroughTime(currentY, menu.state == MenuState.IDLE ? 0 : -10.5f);
			if(currentY > -10.f)
				Renderer.renderLogo(time, currentY);
		}
		
	}
	
	static class MenuController {
		
		private static final float ALLOWED_IDLE_TIME = 10.f;
		
		private MenuState state = MenuState.IDLE;
		private int quitGameKeyCount = 3;
		private float idleTime = 0;
		
		public void start() {
			MUSIC_SOURCE
				.setLooping(true)
				.setSound(MUSIC)
				.play();
		}
		
		public void processKey(float time, int key) {
			idleTime = time;
			switch(state) {
			case IDLE:
				state = MenuState.MAIN_MENU;
				SFX_SOURCES.play(SFX_TRANSITION);
				break;
			case MAIN_MENU:
				if(isKeyPressed(KEY_QUIT1) && isKeyPressed(KEY_QUIT2)) {
					Wintool.shutdowComputer();
					System.exit(0);
				} else if(key == KEY_START) {
					if(selectedGame.hasMod(GameMod.HIDE_LAUNCHER))
						GLWindow.hide();
					gamesManager.runGame(selectedGame);
					playingPanel.setGameStartTime(time);
					MUSIC_SOURCE.pause();
//					Wintool.focusGameLater(3.f);
//					Wintool.focusGameLater(10.f);
					state = MenuState.PLAYING;
				} else if(ArrayOperator.contains(KEY_BUTTONS, key)) {
					gameDetails.nextDetailsPage();
				} else if(ArrayOperator.contains(KEY_DIRECTIONS, key)) {
					gamesList.processKey(key);
				}
				break;
			case PLAYING:
				if(key == KEY_START) {
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
		
		public void update(float time) {
			switch (state) {
			case IDLE: break;
			case MAIN_MENU:
				if(time - idleTime > ALLOWED_IDLE_TIME)
					state = MenuState.IDLE;
				break;
			case PLAYING:
				if(!gamesManager.isGameRunning()) {
					state = MenuState.MAIN_MENU;
					idleTime = time;
					MUSIC_SOURCE.resume();
					GLWindow.hide();
					GLWindow.show(WINDOW_FULLSCREEN);
					GameInfoParser.reloadHighscores(selectedGame);
				}
				break;
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
			currentSelection = Math.min(2, games.size()-1);
			selectedGame = games.get(currentSelection);
		}

		public void processKey(int key) {
			int newSelection;
			
			switch(key) {
			case KEY_LEFT:
			case KEY_RIGHT:
				newSelection = currentSelection ^ 1;
				break;
			case KEY_UP:
				newSelection = currentSelection + 2;
				break;
			case KEY_DOWN:
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
			
			Texture tex = TEXTURE_GLOBAL_CONTROLS;
			use(TEXTURE_SHADER);
			renderQuad(currentX, 0, WIN_WIDTH*.5f, (float)tex.height/tex.width*WIN_WIDTH*.5f, tex);
		}
		
		private void renderCartridge(float x, float y, float size, GameInfo game, float time, boolean selected) {
			float width = size;
			float height = size*CARTRIDGE_FG.height/CARTRIDGE_BG.width;
			use(CARTRIDGE_SHADER)
				.setUniform1f("u_time", time)
				.setUniform1i("u_selected", selected?1:0);
			renderQuad(x, y, width, height, CARTRIDGE_BG);
			if(game.cartridge != null) {
				use(TEXTURE_SHADER);
				renderQuad(x+width*.46f, y+height*.3f, width*.46f, height*.64f, game.cartridge);
				use(CARTRIDGE_SHADER);
				renderQuad(x, y, width, height, CARTRIDGE_FG);
			}
		}
		
	}
	
	static class GameDetails {
		
		private static final float BIG_VIGNETTE_SIZE = WIN_HEIGHT * .45f;
		private static final float TITLE_SIZE = WIN_HEIGHT/15;
		private static final float DESCRIPTION_SIZE = TITLE_SIZE*.5f;
		
		private float currentX = WIN_WIDTH*1.05f;
		
		private int currentPage = 0;
		
		public void render(float time) {
			currentX = lerpThroughTime(currentX, menu.getState() == MenuState.MAIN_MENU || menu.getState() == MenuState.PLAYING ? WIN_WIDTH*.5f : WIN_WIDTH*1.05f);
			
			use(TEXTURE_SHADER);
			renderQuad(currentX, 0, WIN_WIDTH*.5f, WIN_HEIGHT, INFO_BACKGROUND_TEXTURE);
			
			if(currentPage == 0) {
				// cartridge, description and general info
				renderQuad(
						currentX + (WIN_WIDTH*.5f-BIG_VIGNETTE_SIZE)*.5f,
						WIN_HEIGHT*.5f + (WIN_HEIGHT*.5f-BIG_VIGNETTE_SIZE)*.5f,
						BIG_VIGNETTE_SIZE, BIG_VIGNETTE_SIZE,
						selectedGame.vignette == null ? WIP_TEXTURE : selectedGame.vignette);
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
			} else {
				// controls and high scores
				renderTextCentered(currentX+WIN_WIDTH*.25f, WIN_HEIGHT*.5f, TITLE_SIZE, FONT_TITLE, selectedGame.title);
				if(selectedGame.highscores != null) {
					for(int i = 0; i < selectedGame.highscores.length; i++) {
						Highscore hs = selectedGame.highscores[i];
						float x = currentX + WIN_WIDTH*.05f;
						float x1 = x, x2 = x+WIN_WIDTH*.5f*.2f, x3 = x+WIN_WIDTH*.5f*.5f;
						float y = WIN_HEIGHT*.5f-DESCRIPTION_SIZE*1.05f*i-TITLE_SIZE*1.5f;
						renderText(x1, y, DESCRIPTION_SIZE, FONT_PLAIN, hs.name);
						renderText(x2, y, DESCRIPTION_SIZE, FONT_PLAIN, hs.date);
						renderText(x3, y, DESCRIPTION_SIZE, FONT_PLAIN, String.valueOf(hs.score));
					}
				} else {
					renderTextCentered(currentX + WIN_WIDTH*.25f, WIN_HEIGHT*.25f, DESCRIPTION_SIZE, FONT_PLAIN, "No highscores");
				}
				
				if(selectedGame.controls != null) {
					float r = Math.min(
							BIG_VIGNETTE_SIZE/selectedGame.controls.width,
							BIG_VIGNETTE_SIZE/selectedGame.controls.height);
					float w = selectedGame.controls.width*r;
					float h = selectedGame.controls.height*r;
					float cx = currentX + WIN_WIDTH*.25f;
					float cy = WIN_HEIGHT*.75f;
					renderQuad(cx-w*.5f, cy-h*.5f, w, h, INFO_BACKGROUND_TEXTURE);
					renderQuad(cx-w*.48f, cy-h*.48f, w*.96f, h*.96f, selectedGame.controls);
				} else {
					renderTextCentered(currentX + WIN_WIDTH*.25f, WIN_HEIGHT*.75f, DESCRIPTION_SIZE, FONT_PLAIN, "No controls (WIP)");
				}
			}
		}
		
		public void nextDetailsPage() {
			currentPage++;
			currentPage %= 2;
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
			renderText(currentX + WIN_WIDTH*.2f, WIN_HEIGHT*.5f, WIN_HEIGHT*.05f, FONT_PLAIN, "Playing since\n" + playtimeStr);
			if(forceQuitKeyCount != 0)
				renderText(currentX + WIN_WIDTH*.05f, WIN_HEIGHT*.05f, WIN_HEIGHT*.025f, FONT_PLAIN, "Press <esc> " + forceQuitKeyCount + " more times to quit forcibly");
		}
		
		public void setForceQuitKeyCount(int remaining) {
			forceQuitKeyCount = remaining;
		}
		
	}

}
