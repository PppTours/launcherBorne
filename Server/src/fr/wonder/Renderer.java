package fr.wonder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.wonder.commons.types.Tuple;
import fr.wonder.gl.FrameBuffer;
import fr.wonder.gl.GLUtils;
import fr.wonder.gl.IndexBuffer;
import fr.wonder.gl.Shader;
import fr.wonder.gl.ShaderProgram;
import fr.wonder.gl.TFont;
import fr.wonder.gl.TextRenderer;
import fr.wonder.gl.Texture;
import fr.wonder.gl.VertexArray;
import fr.wonder.gl.VertexBuffer;
import fr.wonder.gl.VertexBufferLayout;

public class Renderer {

	/** The display range, even if the window is resized draw commands should be issued with this size in mind */
	public static final int WIN_WIDTH = 1600, WIN_HEIGHT = 900;
	
	public static final VertexArray QUAD_VAO;
	private static final VertexArray PPP_LOGO_VAO;
	private static final int PPP_LOGO_VERTEX_COUNT;

	public static final Texture CARTRIDGE_BG = Texture.loadTexture("/textures/cartridge_background.png");
	public static final Texture CARTRIDGE_FG = Texture.loadTexture("/textures/cartridge_foreground.png");
	public static final Texture WIP_TEXTURE = Texture.loadTexture("/textures/wip.png");
	public static final Texture TEXTURE_GLOBAL_CONTROLS = Texture.loadTexture("/textures/global_controls.png");
	public static final Texture INFO_BACKGROUND_TEXTURE = Texture.fromBuffer(1, 1, ByteBuffer.allocateDirect(4).putInt(0x99999999).position(0)); 

	public static final ShaderProgram TEXTURE_SHADER = makeShader("texture.vs", "texture.fs");
	public static final ShaderProgram BLIT_SHADER = makeShader("blit.vs", "blit.fs");
	public static final ShaderProgram BACKGROUND_SHADER = makeShader("texture.vs", "background.fs");
	public static final ShaderProgram CARTRIDGE_SHADER = makeShader("texture.vs", "cartridge.fs");
	private static final ShaderProgram PPP_LOGO_SHADER = makeShader("logo.vs", "logo.fs");

	public static final TFont FONT_PLAIN = TextRenderer.loadFont("/fonts/arcadepi.ttf", true);
	public static final TFont FONT_TITLE = TextRenderer.loadFont("/fonts/ka1.ttf", true);
	
	private static final FrameBuffer mainFBO = new FrameBuffer(WIN_WIDTH, WIN_HEIGHT, true);
	
	private static ShaderProgram currentShader;
	private static float blitX, blitY, blitW, blitH;
	
	static {
		float[] cameraMatrix = new float[] {
				2.f/WIN_WIDTH, 0,              0,        -1,
				0,             2.f/WIN_HEIGHT, 0,        -1,
				0,             0,              1,         0,
				0,             0,              0,         1,
		};
		for(ShaderProgram shader : Arrays.asList(TEXTURE_SHADER, BACKGROUND_SHADER, CARTRIDGE_SHADER, TFont.SHADER)) {
			shader.bind();
			shader.setUniformMat4f("u_camera", cameraMatrix);
		}
		for(ShaderProgram shader : Arrays.asList(BACKGROUND_SHADER)) {
			shader.bind();
			shader.setUniform2f("u_resolution", WIN_WIDTH, WIN_HEIGHT);
		}
	}
	
	static {
		QUAD_VAO = new VertexArray();
		QUAD_VAO.setBuffer(new VertexBuffer(GLUtils.createQuadVertices(0, 1)), new VertexBufferLayout().addFloats(2));
		QUAD_VAO.setIndices(GLUtils.createQuadIndexBuffer(1));
		
		try {
			var model = loadModel("/ppp-logo.obj");
			PPP_LOGO_VAO = model.a;
			PPP_LOGO_VERTEX_COUNT = model.b;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static ShaderProgram makeShader(String vs, String fs) {
		return new ShaderProgram(
				new Shader("/shaders/" + vs, Shader.ShaderType.VERTEX), 
				new Shader("/shaders/" + fs, Shader.ShaderType.FRAGMENT));
	}
	
	public static ShaderProgram use(ShaderProgram shader) {
		currentShader = shader;
		currentShader.bind();
		return shader;
	}

	public static void renderQuad(float x, float y, float width, float height, Texture texture) {
		texture.bind(0);
		renderQuad(x, y, width, height);
	}
	
	public static void renderQuad(float x, float y, float width, float height) {
		QUAD_VAO.bind();
		currentShader.bind();
		currentShader.setUniform4f("u_transform", x, y, width, height);
		GLUtils.dcQuads(1);
	}

	public static void renderText(float x, float y, float size, TFont font, String text) {
		font.renderText(text, x, y, size);
	}
	
	public static void renderTextCentered(float x, float y, float size, TFont font, String text) {
		font.renderText(text, x - font.getLineLength(text, size)*.5f, y - size/2.f, size);
	}
	
	public static void renderLogo(float time, float y) {
		PPP_LOGO_VAO.bind();
		PPP_LOGO_SHADER.bind();
		PPP_LOGO_SHADER.setUniform1f("u_time", time);
		PPP_LOGO_SHADER.setUniform1f("u_y", y);
		GLUtils.enableDepth(true);
		GLUtils.dcTriangles(PPP_LOGO_VERTEX_COUNT);
		GLUtils.enableDepth(false);
	}
	
	public static void prepareFrame() {
		mainFBO.bind();
		GLUtils.clear();
	}
	
	public static void endFrame() {
		FrameBuffer.unbind();
		GLUtils.clear();
		mainFBO.bindTexture(0);
		QUAD_VAO.bind();
		BLIT_SHADER.bind();
		BLIT_SHADER.setUniform4f("u_transform", blitX, blitY, blitW, blitH);
		GLUtils.dcQuads(1);
	}
	
	public static void updateWinSize(int displayWidth, int displayHeight) {
//		// -- letterbox stretching --
//		// width and height ratios
//		float rw = (float)displayWidth/mainFBO.getWidth();
//		float rh = (float)displayHeight/mainFBO.getHeight();
//		float mr = Math.min(rw, rh);
//		// do not try to find how I found these expressions
//		blitW = rw/mr;
//		blitH = rh/mr;
//		blitX = (1-blitW)/2.f;
//		blitY = (1-blitH)/2.f;
		
		// on the real hardware the aspect ratio is close enough to 16:9
		// for the letterbox stretching to not be that fancy, to avoid
		// the black stripes it is easier to stretch the whole display to
		// fit the viewport (=window size)
		blitX = 0;
		blitY = 0;
		blitW = 1;
		blitH = 1;
	}
	
	private static Tuple<VertexArray, Integer> loadModel(String resourcePath) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Renderer.class.getResourceAsStream(resourcePath)))) {
			
			List<float[]/*vec3*/> vertices = new ArrayList<>();
			List<float[]/*vec2*/> uvMap = new ArrayList<>();
			List<float[]/*vec3*/> normals = new ArrayList<>();
			List<int[]/*int[3]*/> faces = new ArrayList<>();
			
			String l;
			int nextObjectId = 0;
			while((l = reader.readLine()) != null) {
				if(l.isEmpty() || l.charAt(0) == '#')
					continue;
				
				String[] values = l.split(" ");
				
				if(l.startsWith("v ")) {
					vertices.add(new float[] { Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3]) });
				} else if(l.startsWith("vt")) {
					uvMap.add(new float[] { Float.parseFloat(values[1]), Float.parseFloat(values[2]) });
				} else if(l.startsWith("vn")) {
					normals.add(new float[] { Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3]) });
				} else if(l.startsWith("f")) {
					String[] v1 = values[1].split("/");
					String[] v2 = values[2].split("/");
					String[] v3 = values[3].split("/");
					faces.add(new int[] { Integer.parseInt(v1[0])-1, Integer.parseInt(v1[1])-1, Integer.parseInt(v1[2])-1, nextObjectId });
					faces.add(new int[] { Integer.parseInt(v2[0])-1, Integer.parseInt(v2[1])-1, Integer.parseInt(v2[2])-1, nextObjectId });
					faces.add(new int[] { Integer.parseInt(v3[0])-1, Integer.parseInt(v3[1])-1, Integer.parseInt(v3[2])-1, nextObjectId });
//					//read non-triangulated file
//					if(values.length >= 5) {
//						for(int i = 0; i < values.length-4; i++) {
//						String[] v4 = values[4+i].split("/");
//							faces.add(faces.get(faces.size()-3));
//							faces.add(faces.get(faces.size()-2));
//							faces.add(new int[] { Integer.parseInt(v4[0]), Integer.parseInt(v4[1]), Integer.parseInt(v4[2]) });
//						}
//					}
				} else if(l.startsWith("o ")) {
					nextObjectId++;
				}
			}
			
			int[] indexData = new int[faces.size()];
			
			List<int[]/*vec4i*/> trueFaces = new ArrayList<>();
			
			for(int i = 0; i < faces.size(); i++) {
				// check if a similar edge already exist.
				boolean existed = false;
				for(int j = 0; j < i; j++) {
					if(Arrays.equals(faces.get(i), faces.get(j))) {
						// found a similar edge
						indexData[i] = indexData[j];
						existed = true;
						break;
					}
				}
				
				// if this vertex did not exist, add a new one
				if(!existed) {
					indexData[i] = trueFaces.size();
					trueFaces.add(faces.get(i));
				}
			}
			
			//3 world pos, 2 UVs, 3 normals, 1 object id
			float[] bufferData = new float[trueFaces.size() * 9];
			
			for(int i = 0; i < trueFaces.size(); i++) {
				int j = i*9;
				float[] position = vertices.get(trueFaces.get(i)[0]);
				float[] uv = uvMap.get(trueFaces.get(i)[1]);
				float[] normal = normals.get(trueFaces.get(i)[2]);
				float objectid = trueFaces.get(i)[3];
				bufferData[j  ] = position[0];
				bufferData[j+1] = position[1];
				bufferData[j+2] = position[2];
				bufferData[j+3] = uv[0];
				bufferData[j+4] = uv[1];
				bufferData[j+5] = normal[0];
				bufferData[j+6] = normal[1];
				bufferData[j+7] = normal[2];
				bufferData[j+8] = objectid;
			}
			
			VertexBuffer vbo = new VertexBuffer(bufferData);
			IndexBuffer ibo = new IndexBuffer(indexData);
			VertexArray vao = new VertexArray();
			vao.setBuffer(vbo, new VertexBufferLayout().addFloats(3).addFloats(2).addFloats(3).addFloats(1));
			vao.setIndices(ibo);
						
			return new Tuple<>(vao, indexData.length);
		}
	}

}
