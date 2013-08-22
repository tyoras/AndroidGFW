package yoan.game.framework.modules.input;

import java.util.List;

import yoan.game.framework.modules.input.touch.MultiTouchHandler;
import yoan.game.framework.modules.input.touch.SingleTouchHandler;
import yoan.game.framework.modules.input.touch.TouchHandler;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;

/**
 * Implémentation Android de la gestion des entrées du jeu
 * @author yoan
 */
public class AndroidInput implements Input {
	/** gestionnaire de l'acceleromètre */
	AccelerometerHandler accelHandler;
	/** gestionnaire du clavier */
	KeyboardHandler keyHandler;
	/** gestionnaire du touchscreen */
	TouchHandler touchHandler;
	
	/**
	 * Constructeur à partir du contexte et de la View Android
	 * @param context : contexte Android
	 * @param view : View Android
	 * @param scaleX : coefficient de rescaling de l'axe X
	 * @param scaleY : coefficient de rescaling de l'axe Y
	 */
	public AndroidInput(Context context, View view, float scaleX, float scaleY){
		accelHandler= new AccelerometerHandler(context);
		keyHandler= new KeyboardHandler(view);
		//si la version d'Android ne supporte pas le multitouch on utilise le SingleTouchHandler
		if(VERSION.SDK_INT <  VERSION_CODES.FROYO) touchHandler= new SingleTouchHandler(view, scaleX, scaleY);
		//sinon ce sera le MultiTouchHandler
		else touchHandler= new MultiTouchHandler(view, scaleX, scaleY);
	}
	
	/**
	 * Indique si la touche demandée est pressé
	 * @param keyCode : la touche demandée
	 * @return true si touche pressée, false sinon
	 */
	public boolean isKeyPressed(int keyCode){
		return keyHandler.isKeyPressed(keyCode);
	}

	/**
	 * Indique si le pointeur demandé touche l'écran
	 * @param pointer : le pointeur demandé
	 * @return true si écran touché par le pointeur, false sinon
	 */
	public boolean isTouchDown(int pointer){
		return touchHandler.isTouchDown(pointer);
	}

	/**
	 * Donne l'abscisse du point de contact du pointeur demandé
	 * @param pointer : le pointeur demandé
	 * @return l'abscisse du point de contact du pointeur
	 */
	public int getTouchX(int pointer){
		return touchHandler.getTouchX(pointer);
	}

	/**
	 * Donne l'ordonnée du point de contact du pointeur demandé
	 * @param pointer : le pointeur demandé
	 * @return l'ordonnée du point de contact du pointeur
	 */
	public int getTouchY(int pointer){
		return touchHandler.getTouchY(pointer);
	}

	/**
	 * Donne l'accélération sur l'axe X
	 * @return l'accélation sur X
	 */
	public float getAccelX(){
		return accelHandler.getAccelX();
	}

	/**
	 * Donne l'accélération sur l'axe Y
	 * @return l'accélation sur Y
	 */
	public float getAccelY(){
		return accelHandler.getAccelY();
	}

	/**
	 * Donne l'accélération sur l'axe Z
	 * @return l'accélation sur Z
	 */
	public float getAccelZ(){
		return accelHandler.getAccelZ();
	}

	/**
	 * Récupère les derniers évenements du touchscreen
	 * @return liste ordonnée d'évenement du touchscreen
	 */
	public List<TouchEvent> getTouchEvents(){
		return touchHandler.getTouchEvents();
	}

	/**
	 * Récupère les derniers évenements du clavier
	 * @return liste ordonnée d'évenement du clavier
	 */
	public List<KeyEvent> getKeyEvents(){
		return keyHandler.getKeyEvents();
	}
}