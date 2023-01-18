package fr.wonder;

import static fr.wonder.display.Renderer.*;

import java.io.File;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import fr.wonder.display.Renderer;
import fr.wonder.gl.GLWindow;

public class Launcher {
	
	public static final File GAMES_DIR = new File("games");
	
	public static void main(String[] args) {
		GLWindow.createWindow(1600, 900);
		
		List<GameInfo> games = GameInfoParser.parseGameInfos();
		GameInfo dummy = new GameInfo();
		for(int i = 0; i < 10; i++)
			games.add(dummy);
		
		GamesList gamesList = new GamesList(games);
		try (GamesManager gamesManager = new GamesManager()) {
			Renderer.updateWinSize(GLWindow.getWinWidth(), GLWindow.getWinHeight());
			GLWindow.setResizeCallback(Renderer::updateWinSize);
			GLWindow.setKeyCallback(key -> {
				if(key == GLFW.GLFW_KEY_ENTER) {
					gamesManager.runGame(gamesList.getSelectedGame());
				}
				gamesList.processKey(key);
			});
			
			long firstMillis = System.currentTimeMillis();
			
			while(!GLWindow.shouldDispose()) {
				float time = (System.currentTimeMillis()-firstMillis)/1000.f;
				
				prepareFrame();
				gamesList.render(time);
	//			renderer.renderCartridge(0, 0, 400, games.get(0));
	//			TextRenderer.font.renderText("abcdef\nghijk\nlmnop\nqrstu\nvwxyz", 0, 0, .1f, Color.WHITE);
				sendFrame();
				GLWindow.sendFrame();
			}
		}
	}

}

class GamesList {
	
	private final List<GameInfo> games;
	
	private int currentSelection;
	private float currentScroll;
	
	public GamesList(List<GameInfo> games) {
		this.games = games;
		currentSelection = Math.min(2, games.size());
	}

	public GameInfo getSelectedGame() {
		return games.get(currentSelection);
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
	}

	private static final float DISPLAY_SIZE = WIN_WIDTH*.5f;
	private static final float MARGIN = DISPLAY_SIZE*.1f;
	private static final float CARTRIDGE_WIDTH = (DISPLAY_SIZE-MARGIN*3)/2;
	private static final float CARTRIDGE_HEIGHT = CARTRIDGE_WIDTH*1.4f; // based on the texture file
	
	void render(float time) {
		use(GAMES_LIST_BG_SHADER)
			.setUniform1f("u_time", time);
		renderQuad(0, 0, WIN_WIDTH/2, WIN_HEIGHT, null);
		use(TEXTURE_SHADER);
		
		float targetScroll = - currentSelection/2 * CARTRIDGE_HEIGHT;
		currentScroll = currentScroll + (targetScroll-currentScroll)*.1f;
		if(Math.abs(targetScroll - currentScroll) < .01f)
			currentScroll = targetScroll;
		
		for(int i = 0; i < games.size(); i++) {
			renderCartridge(
					MARGIN + (i%2)*(MARGIN+CARTRIDGE_WIDTH),
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
		if(game.vignetteTexture != null) {
			use(TEXTURE_SHADER);
			renderQuad(x+width*.46f, y+height*.3f, width*.46f, height*.64f, game.vignetteTexture);
			use(CARTRIDGE_SHADER);
			renderQuad(x, y, width, height, CARTRIDGE_FG);
		}
	}
	
}
