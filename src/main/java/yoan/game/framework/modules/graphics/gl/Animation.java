package yoan.game.framework.modules.graphics.gl;

import yoan.game.framework.modules.graphics.gl.TextureRegion;

/**
 * Gestion Android des animations de sprite
 * @author yoan
 */
public class Animation {
	/** Mode d'animation en boucle */
	public static final int ANIMATION_LOOPING= 0;
	/** Mode d'animation finie à la dernière frame */
	public static final int ANIMATION_NONLOOPING= 1;
	/** Régions de chacune des frames dans la texture */
	final TextureRegion[] keyFrames;
	/** Durée d'une frame d'animation */
	final float frameDuration;

	/**
	 * Constructeur avec paramètres
	 * @param frameDuration : durée d'une frame
	 * @param keyFrames : textures de chacune des frames dans l'ordre d'affichage
	 */
	public Animation(float frameDuration, TextureRegion... keyFrames){
		this.frameDuration= frameDuration;
		this.keyFrames= keyFrames;
	}

	/**
	 * Calcule la frame correspondante au temps écoulé
	 * @param stateTime : temps écoulé depuis le début de l'animation
	 * @param mode : mode d'animation 
	 * @return la texture correspondant à la frame calculée
	 */
	public TextureRegion getKeyFrame(float stateTime, int mode){
		//calcul du nombre de frame depuis le début de l'animation
		int frameNumber= (int) (stateTime / frameDuration);
		if(mode == ANIMATION_NONLOOPING){
			//on prend la dernière frame si l'animation est terminée
			frameNumber= Math.min(keyFrames.length - 1, frameNumber);
		}else{
			//on prend la frame correspondante dans le tableau
			frameNumber= frameNumber % keyFrames.length;
		}
		return keyFrames[frameNumber];
	}
}