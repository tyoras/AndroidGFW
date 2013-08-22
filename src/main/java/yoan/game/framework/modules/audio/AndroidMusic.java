package yoan.game.framework.modules.audio;

import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

/**
 * Implémentation Android de la gestion des musics (audio streamé)
 * @author yoan
 */
public class AndroidMusic implements Music, OnCompletionListener {
	/** le gestionnaire de music */
	MediaPlayer mediaPlayer;
	/** indique si le media player est pret à être utilisé */
	boolean isPrepared = false;

	/**
	 * Constructeur
	 * @param assetDescriptor : descriptor de l'asset de la music
	 */
	public AndroidMusic(AssetFileDescriptor assetDescriptor){
		mediaPlayer= new MediaPlayer();
		try{
			mediaPlayer.setDataSource(assetDescriptor.getFileDescriptor(),
									  assetDescriptor.getStartOffset(),
									  assetDescriptor.getLength());
			mediaPlayer.prepare();
			isPrepared= true;
			mediaPlayer.setOnCompletionListener(this);
		}catch(Exception e){
			throw new RuntimeException("Couldn't load music");
		}
	}
	
	/**
	 * Lecture de la music
	 */
	public void play(){
		if(mediaPlayer.isPlaying()) return;
		try{
			//on lock le temps de démarrer la lecture
			synchronized(this){
				if(!isPrepared) mediaPlayer.prepare();
				mediaPlayer.start();
			}
		}catch(IllegalStateException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Pause de la lecture
	 */
	public void pause(){
		if(mediaPlayer.isPlaying()) mediaPlayer.pause();
	}
	
	/**
	 * Arret de la lecture
	 */
	public void stop(){
		mediaPlayer.stop();
		synchronized(this){
			isPrepared= false;
		}
	}
	
	/**
	 * Active/Désactive la mise en boucle
	 * @param looping : indique si la music est lue en boucle
	 */
	public void setLooping(boolean isLooping){
		mediaPlayer.setLooping(isLooping);
	}

	/**
	 * Change le volume de lecture
	 * @param volume [0 (silence); 1 (volume max)]
	 */
	public void setVolume(float volume){
		mediaPlayer.setVolume(volume, volume);
	}
	
	/**
	 * Indique si la music est lue en boucle
	 * @return boolean
	 */
	public boolean isLooping(){
		return mediaPlayer.isLooping();
	}

	/**
	 * Indique si la music est en court de lecture
	 * @return boolean
	 */
	public boolean isPlaying(){
		return mediaPlayer.isPlaying();
	}

	/**
	 * Indique si la music est arrếtée
	 * @return boolean
	 */
	public boolean isStopped(){
		return !isPrepared;
	}
	
	/**
	 * Ferme le stream de lecture
	 */
	public void dispose(){
		if(mediaPlayer.isPlaying()) mediaPlayer.stop();
		mediaPlayer.release();
	}
	
	/**
	 * Appeler à la fin de la music
	 */
	public void onCompletion(MediaPlayer player){
		//on indique qu'on est plus en lecture
		synchronized(this){
			isPrepared= false;
		}
	}
}