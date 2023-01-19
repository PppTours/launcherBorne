package fr.wonder.gl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

public class Texture {

	public final int width, height;
	public final int id;
	
	private Texture(int id, int width, int height) {
		this.id = id;
		this.width = width;
		this.height = height;
	}
	
	public static Texture loadTexture(String path) {
		try (InputStream is = Texture.class.getResourceAsStream(path)) {
			if (is == null)
				throw new IOException("Resource " + path + " does not exist");
			return loadTexture(is);
		} catch (IOException e) {
			throw new RuntimeException("Could not load texture " + path + " from resources");
		}
	}
	
	public static Texture loadTexture(File file) throws IOException {
		try (InputStream is = new FileInputStream(file)) {
			return loadTexture(is);
		}
	}
	
	public static Texture loadTexture(InputStream stream) throws IOException {
		return loadTexture(ImageIO.read(stream));
	}
		
	public static Texture loadTexture(BufferedImage image) {
		int id, width, height;

		width = image.getWidth();
		height = image.getHeight();

		int size = width * height;
		int[] data = new int[size];

		image.getRGB(0, 0, width, height, data, 0, width);

		int[] px = new int[size];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int pos = i * width + j;
				int a = (data[pos] & 0xff000000) >> 24;
				int r = (data[pos] & 0x00ff0000) >> 16;
				int g = (data[pos] & 0x0000ff00) >> 8;
				int b = (data[pos] & 0x000000ff);
				px[(height - 1 - i) * width + j] =
						a << 24 |
						b << 16 |
						g << 8 |
						r;
			}
		}

		id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, px);
		unbind();
		
		return new Texture(id, width, height);
	}
	
	public static Texture fromBuffer(int width, int height, ByteBuffer buffer) {
		int id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		unbind();
		return new Texture(id, width, height);
	}

	public void bind(int slot) {
		glActiveTexture(GL_TEXTURE0 + slot);
		glBindTexture(GL_TEXTURE_2D, id);
	}
	
	public static void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void dispose() {
		glDeleteTextures(id);
	}
}
