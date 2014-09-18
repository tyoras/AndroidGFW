package yoan.game.framework.modules.graphics.gl;

import yoan.game.framework.util.math.region.RectangleRegion;
import static yoan.game.framework.util.Preconditions.*;

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
	/** régions corrspondant aux limites physique de chaque frame */
	RectangleRegion[] physicalBounds;
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
		this.physicalBounds = null;
	}
	
	/**
	 * Constructeur avec paramètres
	 * @param keyFrames : limite physique de chacune des frames dans l'ordre d'affichage
	 */
	public void setPhysicalBounds(RectangleRegion... physicalBounds){
		checkNotNull(physicalBounds);
		checkArgument(physicalBounds.length == keyFrames.length, "All animation frame must have physical bounds");
		this.physicalBounds = physicalBounds;
	}
	
	/**
	 * Charge les limites physiques de chacune des frames depuis un fichier JSON
	 */
	public void loadPhysicalBounds(){
		RectangleRegion[] physicalBounds = new RectangleRegion[keyFrames.length];
		
		//TODO : charger les infos depuis un fichier JSON et vérifier à chaque fois que la région ne dépasse pas la texture en taille
		
		this.physicalBounds = physicalBounds;
	}

	/**
	 * Calcule la frame correspondante au temps écoulé
	 * @param stateTime : temps écoulé depuis le début de l'animation
	 * @param mode : mode d'animation 
	 * @return la texture correspondant à la frame calculée
	 */
	public TextureRegion getKeyFrame(float stateTime, int mode){
		return keyFrames[getFrameNumber(stateTime, mode)];
	}
	
	/**
	 * Calcule la frame correspondante au temps écoulé
	 * @param stateTime : temps écoulé depuis le début de l'animation
	 * @param mode : mode d'animation 
	 * @return la texture correspondant à la frame calculée
	 */
	public RectangleRegion getPhysicalBound(float stateTime, int mode){
		checkNotNull(physicalBounds, "Physical bounds must be valorised before using it");
		return physicalBounds[getFrameNumber(stateTime, mode)];
	}
	
	/**
	 * Calcule la frame correspondante au temps écoulé
	 * @param stateTime : temps écoulé depuis le début de l'animation
	 * @param mode : mode d'animation 
	 * @return l'index de la frame
	 */
	private int getFrameNumber(float stateTime, int mode) {
		//calcul du nombre de frame depuis le début de l'animation
		int frameNumber= (int) (stateTime / frameDuration);
		if(mode == ANIMATION_NONLOOPING){
			//on prend la dernière frame si l'animation est terminée
			frameNumber= Math.min(keyFrames.length - 1, frameNumber);
		}else{
			//on prend la frame correspondante dans le tableau
			frameNumber= frameNumber % keyFrames.length;
		}
		return frameNumber;
	}
}