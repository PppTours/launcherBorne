package fr.wonder.audio;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_close;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_info;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_samples_short_interleaved;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_open_memory;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_stream_length_in_samples;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbisInfo;

import fr.wonder.commons.files.FilesUtils;

public class Sound {
	
	private int bufferId;
	
	private Sound(int bufferId) {
		this.bufferId = bufferId;
		AudioManager.pollALErrors();
	}
	
	int getBufferId() {
		return bufferId;
	}
	
	public void dispose() {
		AudioManager.pollALErrors();
		alDeleteBuffers(bufferId);
		bufferId = 0;
		AudioManager.pollALErrors();
	}
	
	public static Sound loadSound(String resourcePath) {
		try {
			switch(FilesUtils.getFileExtension(resourcePath)) {
			case "ogg": return loadVorbisSound(resourcePath);
			default: throw new IOException("No parser for extension of file " + resourcePath);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Sound loadVorbisSound(String resourcePath) throws IOException {
		int alBuffer = alGenBuffers();
		
		try(STBVorbisInfo info = STBVorbisInfo.malloc()) {
			ByteBuffer vorbis = loadResourceToBuffer(resourcePath);
			IntBuffer error = BufferUtils.createIntBuffer(1);
			long decoder = stb_vorbis_open_memory(vorbis, error, null);
			
			if (decoder == NULL)
				throw new IOException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
			stb_vorbis_get_info(decoder, info);
			int channels = info.channels();
			ShortBuffer pcm = BufferUtils.createShortBuffer(stb_vorbis_stream_length_in_samples(decoder) * channels);
			stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
			stb_vorbis_close(decoder);
			alBufferData(alBuffer, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
			AudioManager.pollALErrors();
			
			return new Sound(alBuffer);
		}
	}
	
	private static ByteBuffer loadResourceToBuffer(String path) throws IOException {
		try (	InputStream source = Sound.class.getResourceAsStream(path);
				ReadableByteChannel rbc = Channels.newChannel(source)) {
			
			ByteBuffer buffer = BufferUtils.createByteBuffer(32 * 1024);
			
			while (true) {
				int bytes = rbc.read(buffer);
				if (bytes == -1) {
					break;
				}
				if (buffer.remaining() == 0) {
					ByteBuffer newBuffer = BufferUtils.createByteBuffer(buffer.capacity() * 3 / 2);
					buffer.flip();
					newBuffer.put(buffer);
					buffer = newBuffer;
				}
			}
			
			buffer.flip();
			return buffer;
		}
	}
	
}
