package fr.wonder.audio;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.AL_SEC_OFFSET;

public class SoundSource {
	
	private static final float MASTER_VOLUME = 1.f;
	
	private int sourceId;
	
	public SoundSource() {
		this.sourceId = alGenSources();
		alSourcef(sourceId, AL_GAIN, MASTER_VOLUME);
		alSourcef(sourceId, AL_PITCH, 1);
	}
	
	public SoundSource setVolume(float gain) {
		alSourcef(sourceId, AL_GAIN, MASTER_VOLUME * gain);
		return this;
	}
	
	public SoundSource setLooping(boolean looping) {
		alSourcei(sourceId, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
		return this;
	}
	
	public SoundSource setSound(Sound sound) {
		alSourceStop(sourceId);
		alSourcei(sourceId, AL_BUFFER, sound==null ? 0 : sound.getBufferId());
		return this;
	}
	
	public void play() {
		alSourcePlay(sourceId);
	}
	
	public boolean isPlaying() {
		return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
	}
	
	public float getPlayTime() {
		return alGetSourcef(sourceId, AL_SEC_OFFSET);
	}
	
	public void setPlayTime(float realTime) {
		alSourcef(sourceId, AL_SEC_OFFSET, realTime);
	}
	
	public void pause() {
		alSourcePause(sourceId);
	}
	
	public void resume() {
		alSourcePlay(sourceId);
	}
	
	public void dispose() {
		alDeleteSources(sourceId);
		sourceId = 0;
	}
	
}
