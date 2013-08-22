package yoan.game.framework.modules.input;

import java.util.ArrayList;
import java.util.List;

import yoan.game.framework.modules.input.Input.KeyEvent;
import yoan.game.framework.util.Pool;
import yoan.game.framework.util.Pool.PoolObjectFactory;
import android.view.View;
import android.view.View.OnKeyListener;

/**
 * Implémentation Android de la gestion du clavier
 * @author yoan
 */
public class KeyboardHandler implements OnKeyListener {
	/** Etat des touches du clavier */
	boolean[] pressedKeys= new boolean[128];
	/** Pool de recyclage des instances des KeyEvent */
	Pool<KeyEvent> keyEventPool;
	/** Buffer des derniers événements du clavier */
	List<KeyEvent> keyEventsBuffer= new ArrayList<KeyEvent>();
	/** Les derniers événements lus par le jeu */
	List<KeyEvent> keyEvents= new ArrayList<KeyEvent>();
	
	/**
	 * Constructeur à partir de la View Android
	 * @param view : View Android
	 */
	public KeyboardHandler(View view){
		//Implémentation de la PoolObjectFactory pour les KeyEvent
		PoolObjectFactory<KeyEvent> factory= new PoolObjectFactory<KeyEvent>() {
			@Override
			public KeyEvent createObject(){
				return new KeyEvent();
			}
		};
		keyEventPool= new Pool<KeyEvent>(factory, 100);
		view.setOnKeyListener(this);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
	}
	
	/**
	 * Appelé lors d'un événement du clavier
	 * @param v : View Android
	 * @param keyCode : code de la touche concernée
	 * @param event : l'événement du clavier
	 * @return true si événement consommé
	 */
	public boolean onKey(View v, int keyCode, android.view.KeyEvent event){
		//on ignore cet événement
		if(event.getAction() == android.view.KeyEvent.ACTION_MULTIPLE) return false;
		synchronized(this){
			KeyEvent keyEvent= keyEventPool.newObject();
			keyEvent.keyCode= keyCode;
			keyEvent.keyChar= (char) event.getUnicodeChar();
			if(event.getAction() == android.view.KeyEvent.ACTION_DOWN){
				keyEvent.type= KeyEvent.KEY_DOWN;
				if(keyCode >= 0 && keyCode < 127) pressedKeys[keyCode]= true;
			}
			if(event.getAction() == android.view.KeyEvent.ACTION_UP){
				keyEvent.type= KeyEvent.KEY_UP;
				if(keyCode >= 0 && keyCode <= 127) pressedKeys[keyCode]= false;
			}
			keyEventsBuffer.add(keyEvent);
		}
		return false;
	}
	
	/**
	 * Indique si une touche est actuellement pressée
	 * @param keyCode : code de la touche
	 * @return boolean
	 */
	public boolean isKeyPressed(int keyCode){
		if(keyCode <= 0 || keyCode >= 127) return false;
		return pressedKeys[keyCode];
	}
	
	/**
	 * Récupère les derniers événements du clavier
	 * @return liste d'événement du clavier
	 */
	public List<KeyEvent> getKeyEvents(){
		synchronized(this){
			//on recycle les instances contenues dans la liste avant de la vider
			int len= keyEvents.size();
			for(int i= 0; i < len; i++){
				keyEventPool.free(keyEvents.get(i));
			}
			keyEvents.clear();
			//puis on récupère le contenu du buffer
			keyEvents.addAll(keyEventsBuffer);
			//que l'on vide
			keyEventsBuffer.clear();
			return keyEvents;
		}
	}
}