package fr.wonder.gl;

public class TFont {
	
	public static final int firtChar = 32, lastChar = 127;
	
	private static final float[] bufferData = new float[2*4*2];
	
	private static final VertexArray VAO;
	private static final VertexBuffer VBO;
	private static final ShaderProgram SHADER;
	
	private final Texture texture;
	private final Glyph[] glyphs;
	
	static {
		VAO = new VertexArray();
		VBO = new VertexBuffer(bufferData);
		VAO.setBuffer(VBO, new VertexBufferLayout().addFloats(2).addFloats(2));
		VAO.setIndices(GLUtils.createQuadIndexBuffer(1));
		SHADER = new ShaderProgram(
				new Shader("/shaders/text.vs", Shader.ShaderType.VERTEX),
				new Shader("/shaders/text.fs", Shader.ShaderType.FRAGMENT));
	}
	
	TFont(Texture texture, Glyph[] glyphs, float textureWidth) {
		this.texture = texture;
		this.glyphs = glyphs;
	}
	
	public void renderText(String text, float x, float y, float size, Color color) {
		float posXa = x;
		float posYa = y;
	
		VAO.bind();
		VBO.bind();
		SHADER.bind();
		texture.bind(0);
	    for (int i = 0; i < text.length(); i++) {
	        char ch = text.charAt(i);
	        if (ch == '\n') {
	            /* Line feed, set x and y to draw at the next line */
	        	posYa -= size*2;
	        	posXa = x;
	            continue;
	        }
	        if(ch < firtChar || ch > lastChar)
	        	continue;
	        Glyph g = glyphs[ch-firtChar];
	        if(g == null)
	        	continue;
	        
	        float posXb = posXa + (float)g.texWidth/g.texHeight*(ch==' ' ? size/2.f : size);
	        float posYb = posYa + (float)g.texHeight/texture.height*size;
	        float texXa = (float)g.texX/texture.width;
	        float texYa = (float)g.texY/texture.height;
	        float texXb = (float)(g.texX + g.texWidth )/texture.width;
	        float texYb = (float)(g.texY + g.texHeight)/texture.height;
	        
			bufferData[0]  = posXa;		bufferData[1]  = posYa;		bufferData[2]  = texXa;		bufferData[3]  = texYa;
			bufferData[4]  = posXb;		bufferData[5]  = posYa;		bufferData[6]  = texXb;		bufferData[7]  = texYa;
			bufferData[8]  = posXb;		bufferData[9]  = posYb;		bufferData[10] = texXb;		bufferData[11] = texYb;
			bufferData[12] = posXa;		bufferData[13] = posYb;		bufferData[14] = texXa;		bufferData[15] = texYb;

			VBO.setData(bufferData);
			GLUtils.dcQuads(1);
	        posXa = posXb;
	    }
	}
	
	static class Glyph {
		final int texX;
		final int texY;
		final int texWidth;
		final int texHeight;
		
		Glyph(int texX, int texY, int texWidth, int texHeight){
			this.texX = texX;
			this.texY = texY;
			this.texWidth = texWidth;
			this.texHeight = texHeight;
		}
	}
	
}
