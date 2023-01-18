package fr.wonder.display;

import fr.wonder.gl.FrameBuffer;
import fr.wonder.gl.GLUtils;
import fr.wonder.gl.Shader;
import fr.wonder.gl.ShaderProgram;
import fr.wonder.gl.Texture;
import fr.wonder.gl.VertexArray;
import fr.wonder.gl.VertexBuffer;
import fr.wonder.gl.VertexBufferLayout;

public class Renderer {

	/** The display range, even if the window is resized draw commands should be issued with this size in mind */
	public static final int WIN_WIDTH = 1600, WIN_HEIGHT = 900;
	
	public static final VertexArray QUAD_VAO;

	public static final Texture CARTRIDGE_BG = Texture.loadTexture("/textures/cartridge_background.png");
	public static final Texture CARTRIDGE_FG = Texture.loadTexture("/textures/cartridge_foreground.png");

	public static final ShaderProgram TEXTURE_SHADER = makeShader("texture.vs", "texture.fs");
	public static final ShaderProgram BLIT_SHADER = makeShader("blit.vs", "blit.fs");
	public static final ShaderProgram GAMES_LIST_BG_SHADER = makeShader("texture.vs", "bg_gameslist.fs");
	public static final ShaderProgram CARTRIDGE_SHADER = makeShader("texture.vs", "cartridge.fs");
	
	private static final FrameBuffer mainFBO = new FrameBuffer(WIN_WIDTH, WIN_HEIGHT, true);
	
	static {
		float[] cameraMatrix = new float[] {
				2.f/WIN_WIDTH, 0,              0,        -1,
				0,             2.f/WIN_HEIGHT, 0,        -1,
				0,             0,              1,         0,
				0,             0,              0,         1,
		};
		TEXTURE_SHADER.bind();
		TEXTURE_SHADER.setUniformMat4f("u_camera", cameraMatrix);
		GAMES_LIST_BG_SHADER.bind();
		GAMES_LIST_BG_SHADER.setUniformMat4f("u_camera", cameraMatrix);
		CARTRIDGE_SHADER.bind();
		CARTRIDGE_SHADER.setUniformMat4f("u_camera", cameraMatrix);
	}
	
	static {
		QUAD_VAO = new VertexArray();
		QUAD_VAO.setBuffer(new VertexBuffer(GLUtils.createQuadVertices(0, 1)), new VertexBufferLayout().addFloats(2));
		QUAD_VAO.setIndices(GLUtils.createQuadIndexBuffer(1));
	}
	
	private static ShaderProgram makeShader(String vs, String fs) {
		return new ShaderProgram(
				new Shader("/shaders/" + vs, Shader.ShaderType.VERTEX), 
				new Shader("/shaders/" + fs, Shader.ShaderType.FRAGMENT));
	}
	
	private static ShaderProgram currentShader;
	private static float blitX, blitY, blitW, blitH;
	
	public static ShaderProgram use(ShaderProgram shader) {
		currentShader = shader;
		currentShader.bind();
		return shader;
	}
	
	public static void renderQuad(float x, float y, float width, float height, Texture texture) {
		QUAD_VAO.bind();
		currentShader.setUniform4f("u_transform", x, y, width, height);
		if(texture != null)
			texture.bind(0);
		GLUtils.dcQuads(1);
	}
	
	public static void prepareFrame() {
		mainFBO.bind();
		GLUtils.clear();
	}
	
	public static void sendFrame() {
		FrameBuffer.unbind();
		GLUtils.clear();
		mainFBO.bindTexture(0);
		QUAD_VAO.bind();
		BLIT_SHADER.bind();
		BLIT_SHADER.setUniform4f("u_transform", blitX, blitY, blitW, blitH);
		GLUtils.dcQuads(1);
	}
	
	public static void updateWinSize(int displayWidth, int displayHeight) {
		// letterbox stretching
		
		// width and height ratios
		float rw = (float)displayWidth/mainFBO.getWidth();
		float rh = (float)displayHeight/mainFBO.getHeight();
		float mr = Math.min(rw, rh);
		// do not try to find how I found these expressions
		blitW = rw/mr;
		blitH = rh/mr;
		blitX = (1-blitW)/2.f;
		blitY = (1-blitH)/2.f;

		blitX -= 1;
		blitY -= 1;
		blitW *= 2;
		blitH *= 2;
	}

}
