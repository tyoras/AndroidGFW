package yoan.game.framework.modules.graphics;

import android.util.Log;


/**
 * Classe permettant de compter le nombre de Frame Par Seconde
 * @author yoan
 */
public class AndroidFPSCounter extends FPSCounter {

	/**
	 * Implémentation de la trace du FPSCounter pour Android
	 */
	@Override
	protected void trace(){
		Log.d("FPSCounter", "fps: " + frames);
	}
}