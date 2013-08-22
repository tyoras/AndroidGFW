package yoan.game.framework.modules.input;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Gestion de l'acceleromètre
 * @author yoan
 */
public class AccelerometerHandler implements SensorEventListener {
	/** accélération sur l'axe X */
	float accelX;
	/** accélération sur l'axe Y */
	float accelY;
	/** accélération sur l'axe Z */
	float accelZ;

	/**
	 * Constructeur à partir du contexte Android
	 * @param context : contexte Android
	 */
	public AccelerometerHandler(Context context) {
		//récupération du gestionnaire de capteur
		SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        //si l'acceleromètre est disponible
		if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
			Sensor accelerometer= manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
			//on écoute ses évenements
			manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

	/**
	 * Méthode inutilisée
	 */
	public void onAccuracyChanged(Sensor sensor, int accuracy){
		// nothing to do here
	}

	/**
	 * Appelé lors de la lecture de l'état du capteur
	 * @param : l'événement de lecture de l'état
	 */
	public void onSensorChanged(SensorEvent event){
		accelX= event.values[0];
		accelY= event.values[1];
		accelZ= event.values[2];
	}

	/**
	 * Donne l'accélération sur l'axe X
	 * @return l'accélation sur X
	 */
	public float getAccelX(){
		return accelX;
	}

	/**
	 * Donne l'accélération sur l'axe Y
	 * @return l'accélation sur Y
	 */
	public float getAccelY(){
		return accelY;
	}

	/**
	 * Donne l'accélération sur l'axe Z
	 * @return l'accélation sur Z
	 */
	public float getAccelZ(){
		return accelZ;
	}
}