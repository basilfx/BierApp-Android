package com.basilfx.bierapp;

import java.io.File;
import java.util.Set;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.basilfx.bierapp.data.RemoteClient;
import com.basilfx.bierapp.service.BatchIntentReceiver;
import com.google.common.collect.Sets;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 *
 * 
 * @author Bas Stottelaar
 */
public class BierAppApplication extends Application {
	public static final String LOG_TAG = "BierAppApplication";
	
	//public static final String SITE_URL = "http://www.beterlijst.nl";
	//public static final String API_URL = "http://www.beterlijst.nl";
	//public static final String SITE_URL = "http://10.0.0.3:8000";
	//public static final String API_URL = "http://10.0.0.3:8000/api";
	public static final String SITE_URL = "http://192.168.1.114:8000";
	public static final String API_URL = "http://192.168.1.114:8000/api";
	
	public static final String OAUTH2_CLIENT_ID = "8df8f62b96ba40d11cd1";
	public static final String OAUTH2_CLIENT_SECRET = "eee752653d2a1afc5cdff451ebc5d17ec9b9bc9c";
	
	public static final String OAUTH2_REDIRECT_URL = "http://www.beterlijst.nl/oauth2/catch_me";
	public static final String OAUTH2_AUTHORIZE_URL = SITE_URL + "/oauth2/authorize";
	public static final String OAUTH2_TOKEN_URL = SITE_URL + "/oauth2/access_token";
	
	private static RemoteClient remoteClient;
	
	public static File generalCache;
	public static File imageCache;
	
	public static Set<String> badImageUrls = Sets.newHashSet();

	private void initCaches() {
		// Resolve general cache
		BierAppApplication.generalCache =  this.getExternalCacheDir();
        
        // Resolve image cache
        BierAppApplication.imageCache = new File(generalCache, "images");
        
        // Make sure directories exist
        if (!BierAppApplication.generalCache.exists()) { 
        	if (!BierAppApplication.generalCache.mkdirs()) {
        		Log.e(LOG_TAG, "Unable to create directory for general cache: " + BierAppApplication.generalCache);
        	}
        }
        
        if (!BierAppApplication.imageCache.exists()) {
        	if (!BierAppApplication.imageCache.mkdirs()) {
        		Log.e(LOG_TAG, "Unable to create directory for image cache: " + BierAppApplication.imageCache);
        	}
        }
	}
	
	public static RemoteClient getRemoteClient() {
		return BierAppApplication.remoteClient;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Init caches
		this.initCaches();
		
		// Set default ImageLoader display options
		DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.resetViewBeforeLoading(false)
			.build();

		// Create global configuration and initialize ImageLoader with this configuration
		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
            .discCache(new UnlimitedDiscCache(BierAppApplication.imageCache))
            .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
            .defaultDisplayImageOptions(displayOptions)
            .build()
        );
		
		// Setup remote client
		BierAppApplication.remoteClient = new RemoteClient(this, BierAppApplication.API_URL);
		
		// Send initial intent to setup alarm
		Intent intent = new Intent(this, BatchIntentReceiver.class);
		intent.setAction(BatchIntentReceiver.INITIAL_ACTION_NAME);
		
		this.sendBroadcast(intent);
	}
}
