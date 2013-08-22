package yoan.game.framework.modules.fileio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.preference.PreferenceManager;

/**
 * Implémentation Android de la gestion des fichiers du jeu
 * @author yoan
 */
public class AndroidFileIO implements FileIO {
	/** Contexte Android */
	Context context;
	/** Accès au gestionnaire d'assets */
	AssetManager assets;
	/** Chemin vers la carte SD */
	String externalStoragePath;

	/**
	 * Constructeur à partir du contexte Android
	 * @param context : contexte Android
	 */
	public AndroidFileIO(Context context){
		this.context= context;
		this.assets= context.getAssets();
		this.externalStoragePath= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
	}

	/**
	 * Lecture d'un asset
	 * @param fileName : nom du fichier à lire
	 * @return InputStream
	 * @throws IOException : en cas de problème
	 */
	public InputStream readAsset(String fileName) throws IOException{
		return assets.open(fileName);
	}

	/**
	 * Lecture d'un fichier
	 * @param fileName : nom du fichier à lire
	 * @return InputStream
	 * @throws IOException : en cas de problème
	 */
	public InputStream readFile(String fileName) throws IOException{
		return new FileInputStream(externalStoragePath + fileName);
	}

	/**
	 * Ecriture dans un fichier
	 * @param fileName : nom du fichier à écrire
	 * @return OutputStream
	 * @throws IOException : en cas de problème
	 */
	public OutputStream writeFile(String fileName) throws IOException{
		return new FileOutputStream(externalStoragePath + fileName);
	}

	/**
	 * Accès aux préférences du contexte
	 * @return SharedPreferences
	 */
	public SharedPreferences getPreferences(){
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
}