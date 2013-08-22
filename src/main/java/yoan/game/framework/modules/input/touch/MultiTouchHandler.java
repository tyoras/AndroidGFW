package yoan.game.framework.modules.input.touch;

import java.util.ArrayList;
import java.util.List;

import yoan.game.framework.modules.input.Input.TouchEvent;
import yoan.game.framework.util.Pool;
import yoan.game.framework.util.Pool.PoolObjectFactory;
import android.view.MotionEvent;
import android.view.View;

/**
 * Implémentation du gestionnaire de touchscreen supportant le multitouch
 * @author yoan
 */
public class MultiTouchHandler implements TouchHandler {
	/** nombre MAX de point de contact */
	private static final int MAX_TOUCHPOINTS= 10;
	/** Indique l'état du touchscreen */
	boolean[] isTouched= new boolean[MAX_TOUCHPOINTS];
	/** Abcisses des points de contact */
	int[] touchX= new int[MAX_TOUCHPOINTS];
	/** ordonnées des points de contact */
	int[] touchY= new int[MAX_TOUCHPOINTS];
	/** identifiants des pointeurs */
	int[] id= new int[MAX_TOUCHPOINTS];
	/** Pool de recyclage des instances des TouchEvent */
	Pool<TouchEvent> touchEventPool;
	/** Les derniers événements lus par le jeu */
	List<TouchEvent> touchEvents= new ArrayList<TouchEvent>();
	/** Buffer des derniers événements du touchscreen */
	List<TouchEvent> touchEventsBuffer= new ArrayList<TouchEvent>();
	/** coefficient de rescaling de l'axe X */
	float scaleX;
	/** coefficient de rescaling de l'axe Y */
	float scaleY;

	/**
	 * Constructeur à partir de la View Android
	 * @param view : View Android
	 * @param scaleX : coefficient de rescaling de l'axe X
	 * @param scaleY : coefficient de rescaling de l'axe Y
	 */
	public MultiTouchHandler(View view, float scaleX, float scaleY){
		PoolObjectFactory<TouchEvent> factory= new PoolObjectFactory<TouchEvent>() {
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
			//event.getAction() renvoie un int avec ses 8 premiers bits correspondant à l'action et les 8 derniers au pointerIndex
			int action= event.getAction() & MotionEvent.ACTION_MASK;
			int pointerIndex= (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			//nombre de point de contact actuel sur l'écran
			int pointerCount= event.getPointerCount();
			TouchEvent touchEvent;
			for(int i= 0; i < MAX_TOUCHPOINTS; i++){
				if(i >= pointerCount){
					isTouched[i]= false;
					id[i]= -1;
					continue;
				}
				int pointerId= event.getPointerId(i);
				if(event.getAction() != MotionEvent.ACTION_MOVE && i != pointerIndex){
					// if it's an up/down/cancel/out event, mask the id to see if we should process it for this touch point
					continue;
				}
				switch(action){
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_POINTER_DOWN:
						touchEvent= touchEventPool.newObject();
						touchEvent.type= TouchEvent.TOUCH_DOWN;
						touchEvent.pointer= pointerId;
						//on convertit les coordonnées réelles en coordonnées fixes
						touchEvent.x= touchX[i]= (int) (event.getX(i) * scaleX);
						touchEvent.y= touchY[i]= (int) (event.getY(i) * scaleY);
						isTouched[i]= true;
						id[i]= pointerId;
						touchEventsBuffer.add(touchEvent);
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_POINTER_UP:
					case MotionEvent.ACTION_CANCEL:
						touchEvent= touchEventPool.newObject();
						touchEvent.type= TouchEvent.TOUCH_UP;
						touchEvent.pointer= pointerId;
						//on convertit les coordonnées réelles en coordonnées fixes
						touchEvent.x= touchX[i]= (int) (event.getX(i) * scaleX);
						touchEvent.y= touchY[i]= (int) (event.getY(i) * scaleY);
						isTouched[i]= false;
						id[i]= -1;

						touchEventsBuffer.add(touchEvent);
						break;
					case MotionEvent.ACTION_MOVE:
						touchEvent= touchEventPool.newObject();
						touchEvent.type= TouchEvent.TOUCH_DRAGGED;
						touchEvent.pointer= pointerId;
						//on convertit les coordonnées réelles en coordonnées fixes
						touchEvent.x= touchX[i]= (int) (event.getX(i) * scaleX);
						touchEvent.y= touchY[i]= (int) (event.getY(i) * scaleY);
						isTouched[i]= true;
						id[i]= pointerId;
						touchEventsBuffer.add(touchEvent);
						break;
				}
			}
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
			int index= getIndex(pointer);
			if(index < 0 || index >= MAX_TOUCHPOINTS) return false;
			else return isTouched[index];
		}
	}

	/**
	 * Donne l'abscisse du point de contact du pointeur demandé
	 * @param pointer : le pointeur demandé
	 * @return l'abscisse du point de contact du pointeur
	 */
	public int getTouchX(int pointer){
		synchronized(this){
			int index= getIndex(pointer);
			if(index < 0 || index >= MAX_TOUCHPOINTS) return 0;
			else return touchX[index];
		}
	}

	/**
	 * Donne l'ordonnée du point de contact du pointeur demandé
	 * @param pointer : le pointeur demandé
	 * @return l'ordonnée du point de contact du pointeur
	 */
	public int getTouchY(int pointer){
		synchronized(this){
			int index= getIndex(pointer);
			if(index < 0 || index >= MAX_TOUCHPOINTS) return 0;
			else return touchY[index];
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

	/**
	 * Donne l'index d'un pointerId
	 * @param pointerId : identifiant de pointeur
	 * @return index ou -1 si non trouvé
	 */
	private int getIndex(int pointerId){
		for(int i= 0; i < MAX_TOUCHPOINTS; i++){
			if(id[i] == pointerId){
				return i;
			}
		}
		return -1;
	}
}