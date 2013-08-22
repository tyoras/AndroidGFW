package yoan.game.framework.modules.audio;

import android.media.SoundPool;

/**
 * Implémentation Android de la gestion des sons (audio entierement en mémoire)
 * @author yoan
 */
public class AndroidSound implements Sound {
	/** identifiant du son dans le soundpool */
	int soundId;
	/** le gestionnaire de son */
	SoundPool soundPool;

	/**
	 * Constructeur
	 * @param soundPool : le soundpool de l'application
	 * @param soundId : l'identifiant du son dans le soundpool
	 */
	public AndroidSound(SoundPool soundPool, int soundId){
		this.soundId= soundId;
		this.soundPool= soundPool;
	}

	/**
	 * Lit le son à un volume donné
	 * @param volume [0 (silence); 1 (volume max)]
	 */
	public void play(float volume){
		soundPool.play(soundId, volume, volume, 0, 0, 1);
	}

	/**
	 * Libère la mémoire prise par le son 
	 */
	public void dispose(){
		soundPool.unload(soundId);
	}
}