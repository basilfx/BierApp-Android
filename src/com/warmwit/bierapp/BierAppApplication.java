package com.warmwit.bierapp;

import java.io.File;

import android.app.Application;
import android.os.Environment;

import com.warmwit.bierapp.data.RemoteClient;
import com.warmwit.bierapp.utils.ImageDownloader;

/**
 *
 * 
 * @author Bas Stottelaar
 */
public class BierAppApplication extends Application {
	public static ImageDownloader imageDownloader;
	public static RemoteClient remoteClient;
	
	public static File generalCache;
	public static File imageCache;
	
	public BierAppApplication() {
		this.initCaches();
		
		//BierAppApplication.remoteClient = new RemoteClient("http://beterlijst.apps.basilfx.net/apps/bierapp/api");
		BierAppApplication.remoteClient = new RemoteClient("http://10.0.0.3:9000/apps/bierapp/api");
		BierAppApplication.imageDownloader = new ImageDownloader(imageCache);
	}
	
	private void initCaches() {
		// Resolve general cache
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        	BierAppApplication.generalCache = new File(Environment.getExternalStorageDirectory(), "BierApp");
        } else {
        	BierAppApplication.generalCache = this.getCacheDir();
        }
        
        // Resolve image cache
        BierAppApplication.imageCache = new File(generalCache, "images");
        
        // Make sure directories exist
        if (!BierAppApplication.generalCache.exists()) { 
        	BierAppApplication.generalCache.mkdirs();
        }
        
        if (!BierAppApplication.imageCache.exists()) {
        	BierAppApplication.imageCache.mkdirs();
        }
	}
	
	public static String getHostUrl() {
		return "http://10.0.0.3:9000";
	}
}
