package yoan.game.framework.modules.audio;

import java.io.IOException;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Implémentation Android de la gestion de l'audio du jeu
 * @author yoan
 */
public class AndroidAudio implements Audio {
	/** Accès au gestionnaire d'assets */
	AssetManager assets;
	/** le gestionnaire de son */
	SoundPool soundPool;

	/**
	 * Constructeur à partir du contexte Android
	 * @param activity : contexte Android
	 */
	public AndroidAudio(Activity activity){
		// le son sera sur le stream MUSIC
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		this.assets= activity.getAssets();
		// sound pool avec 20 sons consécutifs
		this.soundPool= new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
	}

	/**
	 * Créer une music à partir d'un fichier
	 * @param filename : fichier à lire
	 * @return la music
	 */
	public Music newMusic(String filename){
		try{
			AssetFileDescriptor assetDescriptor= assets.openFd(filename);
			return new AndroidMusic(assetDescriptor);
		}catch(IOException e){
			throw new RuntimeException("Couldn't load music '" + filename + "'");
		}
	}
	
	/**
	 * Créer un son à partir d'un fichier
	 * @param filename : fichier à lire
	 * @return le son
	 */
	public Sound newSound(String filename){
		try{
			AssetFileDescriptor assetDescriptor= assets.openFd(filename);
			int soundId= soundPool.load(assetDescriptor, 0);
			return new AndroidSound(soundPool, soundId);
		}catch(IOException e){
			throw new RuntimeException("Couldn't load sound '" + filename + "'");
		}
	}
}