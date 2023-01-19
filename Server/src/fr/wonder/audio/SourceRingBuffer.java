package fr.wonder.audio;

public class SourceRingBuffer {
	
	private final SoundSource[] sources;
	
	public SourceRingBuffer(int sourceCount) {
		this.sources = new SoundSource[sourceCount];
		for(int i = 0; i < sourceCount; i++)
			sources[i] = new SoundSource();
	}
	
	public void play(Sound sound) {
		for(int i = 0; i < sources.length; i++) {
			if(!sources[i].isPlaying()) {
				sources[i].setSound(sound).play();
				return;
			}
		}
	}

}
