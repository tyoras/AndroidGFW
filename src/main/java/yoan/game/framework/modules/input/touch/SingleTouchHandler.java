package yoan.game.framework.modules.input.touch;

import java.util.ArrayList;
import java.util.List;

import yoan.game.framework.modules.input.Input.TouchEvent;
import yoan.game.framework.util.Pool;
import yoan.game.framework.util.Pool.PoolObjectFactory;
import android.view.MotionEvent;
import android.view.View;

/**
 * Implémentation du gestionnaire de touchscreen ne supportant pas le multitouch
 * @author yoan
 */
public class SingleTouchHandler implements TouchHandler {
	/** Indique si l'écran est actuellement touché */
	boolean isTouched;
	/** l'abscisse du point de contact */
	int touchX;
	/** l'ordonnée du point de contact */
	int touchY;
	/** Pool de recyclage des instances des TouchEvent */
	Pool<TouchEvent> touchEventPool;
	/** Les derniers événements lus par le jeu */
	List<TouchEvent> touchEvents= new ArrayList<TouchEvent>();
	/** Buffer des derniers événements du touchscreen */
	List<TouchEvent> touchEventsBuffer= new ArrayList<TouchEvent>();
	/** */
	float scaleX;
	/** */
	float scaleY;
	
	/**
	 * Constructeur à partir de la View Android
	 * @param view : View Android
	 * @param scaleX : 
	 * @param scaleY : 
	 */
	public SingleTouchHandler(View view, float scaleX, float scaleY){
		//Implémentation de la PoolObjectFactory pour les TouchEvent
		PoolObjectFactory<TouchEvent> factory= new PoolObjectFactory<TouchEvent>() {
			@Override
			public TouchEvent createObject(){
				return new TouchEvent();
			}
		};
		touchEventPool= new Pool<TouchEvent>(factory, 100);
		view.setOnTouchListener(this);
		this.scaleX= scaleX;
		this.scaleY= scaleY;
	}
	
	/**
	 * Appelé lors d'un événement du touchscreen
	 * @param v : View Android
	 * @param event : l'événement du touchscreen
	 * @return true si événement consommé
	 */
	public boolean onTouch(View v, MotionEvent event){
		synchronized(this){
			TouchEvent touchEvent= touchEventPool.newObject();
			switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					touchEvent.type= TouchEvent.TOUCH_DOWN;
					isTouched= true;
					break;
				case MotionEvent.ACTION_MOVE:
					touchEvent.type= TouchEvent.TOUCH_DRAGGED;
					isTouched= true;
					break;
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					touchEvent.type= TouchEvent.TOUCH_UP;
					isTouched= false;
					break;
			}
			//on convertit les coordonnées réelles en coordonnées fixes
			touchEvent.x= touchX= (int) (event.getX() * scaleX);
			touchEvent.y= touchY= (int) (event.getY() * scaleY);
			touchEventsBuffer.add(touchEvent);
			return true;
		}
	}
	
	/**
	 * Indique si le pointeur demandé touche l'écran
	 * @param pointer : le pointeur demandé
	 * @return true si écran touché par le pointeur, false sinon
	 */
	public boolean isTouchDown(int pointer){
		synchronized(this){
			if(pointer == 0) return isTouched;
			else return false;
		}
	}

	/**
	 * Donne l'abscisse du point de contact 
	 * @param pointer : param inutilisé
	 * @return l'abscisse du point de contact
	 */
	public int getTouchX(int pointer){
		synchronized(this){
			return touchX;
		}
	}

	/**
	 * Donne l'ordonnée du point de contact 
	 * @param pointer : param inutilisé
	 * @return l'ordonnée du point de contact
	 */
	public int getTouchY(int pointer){
		synchronized(this){
			return touchY;
		}
	}
	
	/**
	 * Récupère les derniers évenements du touchscreen
	 * @return liste ordonnée d'évenement du touchscreen
	 */
	public List<TouchEvent> getTouchEvents(){
		synchronized(this){
			//on recycle les instances contenues dans la liste avant de la vider
			int len= touchEvents.size();
			for(int i= 0; i < len; i++)
				touchEventPool.free(touchEvents.get(i));
			touchEvents.clear();
			//puis on récupère le contenu du buffer
			touchEvents.addAll(touchEventsBuffer);
			//que l'on vide
			touchEventsBuffer.clear();
			return touchEvents;
		}
	}
}