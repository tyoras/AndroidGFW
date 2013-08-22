package yoan.game.framework.modules.network;

import java.util.Set;

import yoan.game.framework.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activité de dialogue permettant de détecté et selectionné les appareils bluetooth connus et disponibles
 * Renvoie par intent à l'activité appelante l'adresse MAC de l'appareil choisi
 * @author yoan
 */
public class DeviceListActivity extends Activity {
    /** ID de l'extra contenant l'adresse MAC choisie dans l'iintent retour */
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    /** Gestionnaire de BlueTooth d'Android */
    private BluetoothAdapter blueToothAdapter;
    /** Tableau d'affichage des appareil liés */
    private ArrayAdapter<String> pairedDevices;

    /**
     * Executer à la création de l'activité
     * Initialise la liste des appareils connus
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);
        pairedDevices = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevices);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Récupération du gestionnaire de blueTooth
        blueToothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Récupération des appareils liés
        Set<BluetoothDevice> devices = blueToothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : devices) {
            	pairedDevices.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevices.add(noDevices);
        }
        
        // au cas où l'utilisateur fait un back
        setResult(Activity.RESULT_CANCELED);
    }

    /**
     * Listener sur les clicks sur les éléments de la liste
     */
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	 // on annule le mode découverte s'il est lancé
        	blueToothAdapter.cancelDiscovery();

            //récupération de l'adresse MAC dans le texte
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            //création de l'intent de retour
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            //retour OK et fin de l'activité
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
    
    /**
     * Surchage de onDestroy pour vérifier que l'on ne laisse pas le blueTooth en mode découverte
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // on annule le mode découverte s'il est lancé
        if (blueToothAdapter != null) {
        	blueToothAdapter.cancelDiscovery();
        }
    }
}