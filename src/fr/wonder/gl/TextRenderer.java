package fr.wonder.gl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import fr.wonder.gl.TFont.Glyph;

public class TextRenderer {
	
	public static final TFont font;
	
	static {
		font = loadFont("/fonts/ka1.ttf", true);
	}
	
	private static TFont loadFont(String resourcePath, boolean antiAlias) {
		try {
			Font font = Font.createFont(Font.TRUETYPE_FONT, TextRenderer.class.getResourceAsStream(resourcePath));
//			font = new Font("SERIF", Font.PLAIN, 128);
			// be sure to use a float here, an int refers to another method
			font = font.deriveFont(128f);
			
			int imageWidth = 0;
			int imageHeight = 0;

			for (int i = TFont.firtChar; i < TFont.lastChar; i++) {
				char c = (char) i;
				BufferedImage ch = createCharImage(font, c, antiAlias);
				if (ch == null)
					continue;
				imageWidth += ch.getWidth()+1;
				imageHeight = Math.max(imageHeight, ch.getHeight());
			}

			BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();

			int x = 0;
			
			Glyph[] glyphs = new Glyph[TFont.lastChar-TFont.firtChar];
			for (int i = TFont.firtChar; i < TFont.lastChar; i++) {
				char c = (char) i;
				BufferedImage charImage = createCharImage(font, c, antiAlias);
				if (charImage == null)
					continue;

				int charWidth = charImage.getWidth();
				int charHeight = charImage.getHeight();

				Glyph ch = new Glyph(x, image.getHeight() - charHeight, charWidth, charHeight);
				g.drawImage(charImage, x, 0, null);
				x += charWidth+1;
				glyphs[i-TFont.firtChar] = ch;
			}

			AffineTransform transform = AffineTransform.getScaleInstance(1f, -1f);
			transform.translate(0, -image.getHeight());
			AffineTransformOp operation = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			image = operation.filter(image, null);

			int width = image.getWidth();
			int height = image.getHeight();

			int[] pixels = new int[width * height];
			image.getRGB(0, 0, width, height, pixels, 0, width);
			
			ByteBuffer buffer = GLUtils.createBuffer(width * height * 4);
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					int pixel = pixels[i * width + j];
					buffer.put((byte) ((pixel >> 16) & 0xFF));
					buffer.put((byte) ((pixel >> 8)  & 0xFF));
					buffer.put((byte) ((pixel >> 0)  & 0xFF));
					buffer.put((byte) ((pixel >> 24) & 0xFF));
				}
			}
			buffer.flip();
			buffer.position(0);
			
			Texture texture = Texture.fromBuffer(width, height, buffer);
			return new TFont(texture, glyphs, width);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static BufferedImage createCharImage(Font font, char c, boolean antiAlias) throws IOException {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		if (antiAlias)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		g.dispose();
		
		int charWidth = metrics.charWidth(c);
		int charHeight = metrics.getHeight();
		
		if (charWidth == 0)
			return null;
		
		image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
		g = image.createGraphics();
		if (antiAlias)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(font);
		g.setPaint(Color.WHITE);
		g.drawString(String.valueOf(c), 0, metrics.getAscent());
		g.dispose();
		return image;
	}

}
