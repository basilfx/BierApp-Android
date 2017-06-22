package com.basilfx.bierapp;

import java.io.File;
import java.util.Set;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.basilfx.bierapp.data.RemoteClient;
import com.basilfx.bierapp.service.BatchIntentReceiver;
import com.google.common.collect.Sets;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Application main class.
 * 
 * @author Bas Stottelaar
 */
public class BierAppApplication extends Application {
	public static final String LOG_TAG = "BierAppApplication";
	
    // Change the values below if you run your own BierApp server instance
	public static final String SITE_URL = "https://bierapp.huizewarmwit.nl";
	public static final String API_URL = "https://bierapp.huizewarmwit.nl/api/v1";
	
	public static final String OAUTH2_CLIENT_ID = "8df8f62b96ba40d11cd1";
	public static final String OAUTH2_CLIENT_SECRET = "eee752653d2a1afc5cdff451ebc5d17ec9b9bc9c";
	
	public static final String OAUTH2_REDIRECT_URL = "https://bierapp.huizewarmwit.nl/oauth2/catch_me";
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
		
		// Set default ImageLoader display options.
		DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.build();

		// Create global configuration and initialize ImageLoader with this configuration.
		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
            .diskCache(new UnlimitedDiskCache(BierAppApplication.imageCache))
            .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
            .defaultDisplayImageOptions(displayOptions)
            .build()
        );
		
		// Setup remote client.
		BierAppApplication.remoteClient = new RemoteClient(this, BierAppApplication.API_URL);
		
		// Send initial intent to setup alarm.
		Intent intent = new Intent(this, BatchIntentReceiver.class);
		intent.setAction(BatchIntentReceiver.INITIAL_ACTION_NAME);
		
		this.sendBroadcast(intent);
	}
}
