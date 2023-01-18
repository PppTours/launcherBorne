package fr.wonder.gl;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;

import java.util.ArrayList;
import java.util.List;

public class VertexBufferLayout {

	public class VertexBufferLayoutElement {
		int type;
		int count;
		
		VertexBufferLayoutElement(int type, int count) {
			this.type = type;
			this.count = count;
		}
		
		/**
		 * Return the size in bytes of the layout element
		 */
		int getSize() {
			return (
					type == GL_FLOAT ? 4 :
					type == GL_UNSIGNED_BYTE ? 1 :
					type == GL_UNSIGNED_BYTE ? 4 : 0
							) * count;
		}
	}

	public List<VertexBufferLayoutElement> elements = new ArrayList<VertexBufferLayout.VertexBufferLayoutElement>();
	public int stride;
	
	//there is no need for constructor but it exist and must be called to use these functions
	
	public VertexBufferLayout addFloats(int count) {
		elements.add(new VertexBufferLayoutElement(GL_FLOAT, count));
		stride += elements.get(elements.size()-1).getSize();
		return this;
	}
	
	public VertexBufferLayout addUnsignedBytes(int count) {
		elements.add(new VertexBufferLayoutElement(GL_UNSIGNED_BYTE, count));
		stride += elements.get(elements.size()-1).getSize();
		return this;
	}
	
	public VertexBufferLayout addUnsignedInt(int count) {
		elements.add(new VertexBufferLayoutElement(GL_UNSIGNED_INT, count));
		stride += elements.get(elements.size()-1).getSize();
		return this;
	}
	
}
