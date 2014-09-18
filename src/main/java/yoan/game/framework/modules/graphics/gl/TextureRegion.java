package yoan.game.framework.modules.graphics.gl;

import yoan.game.framework.util.math.region.RectangleRegion;

/**
 * Représente une région d'un texture atlas
 * @author yoan
 */
public class TextureRegion extends RectangleRegion {
	/** Texture atlas */
	public final Texture texture;

	/**
	 * Constructeur avec paramètres
	 * @param texture : texture atlas
	 * @param x : abcisse en pixel du coin supérieur gauche dans le texture atlas
	 * @param y : ordonnée en pixel du coin supérieur gauche dans le texture atlas
	 * @param width : largeur de la région
	 * @param height : hauteur de la région
	 */
	public TextureRegion(Texture texture, float x, float y, float width, float height){
		super(texture, x, y, width, height);
		this.texture= texture;
	}
}