package yoan.game.framework.modules.input.touch;

import java.util.List;

import yoan.game.framework.modules.input.Input.TouchEvent;
import android.view.View.OnTouchListener;

/**
 * Interface de la gestion du touchscreen
 * @author yoan
 */
public interface TouchHandler extends OnTouchListener {
	/**
	 * Indique si le pointeur demandé touche l'écran
	 * @param pointer : le pointeur demandé
	 * @return true si écran touché par le pointeur, false sinon
	 */
	public boolean isTouchDown(int pointer);

	/**
	 * Donne l'abscisse du point de contact du pointeur demandé
	 * @param pointer : le pointeur demandé
	 * @return l'abscisse du point de contact du pointeur
	 */
	public int getTouchX(int pointer);

	/**
	 * Donne l'ordonnée du point de contact du pointeur demandé
	 * @param pointer : le pointeur demandé
	 * @return l'ordonnée du point de contact du pointeur
	 */
	public int getTouchY(int pointer);

	/**
	 * Récupère les derniers évenements du touchscreen
	 * @return liste ordonnée d'évenement du touchscreen
	 */
	public List<TouchEvent> getTouchEvents();
}