package com.warmwit.bierapp;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import android.app.Application;
import android.os.Environment;

import com.warmwit.bierapp.data.RemoteClient;
import com.warmwit.bierapp.utils.ImageLoader;

/**
 *
 * 
 * @author Bas Stottelaar
 */
public class BierAppApplication extends Application {
	public static final String CLIENT_ID = "8df8f62b96ba40d11cd1";
	public static final String CLIENT_SECRET = "eee752653d2a1afc5cdff451ebc5d17ec9b9bc9c";
	public static final String BASE_URL = "http://10.0.0.30:8000/apps/bierapp/api2";
	public static final String REDIRECT_URL = "http://www.beterlijst.nl/oauth/catch_me";
	
	public static ImageLoader imageDownloader;
	public static RemoteClient remoteClient;
	
	public static File generalCache;
	public static File imageCache;
	
	public BierAppApplication() {
		this.initCaches();
		
		BierAppApplication.imageDownloader = new ImageLoader(imageCache);
	}
	
	/**
	 * Initialize the RemoteClient with the given access token.
	 * @param accessToken Access token to use.
	 */
	public void initRemoteClient(String accessToken) {
		BierAppApplication.remoteClient = new RemoteClient(BierAppApplication.BASE_URL, checkNotNull(accessToken));
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
	
	public static String getAuthorizeUrl() {
		return "http://10.0.0.30:8000/oauth2/authorize/?client_id=" + BierAppApplication.CLIENT_ID + "&redirect_uri=" + BierAppApplication.REDIRECT_URL + "&response_type=code";
	}
	
	public static String getAccessTokenFromCode() {
		return "http://10.0.0.30:8000/oauth2/access_token/";
	}
}
