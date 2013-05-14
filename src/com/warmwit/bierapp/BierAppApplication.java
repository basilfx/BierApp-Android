package com.warmwit.bierapp;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import android.app.Application;
import android.os.Environment;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.warmwit.bierapp.data.RemoteClient;

/**
 *
 * 
 * @author Bas Stottelaar
 */
public class BierAppApplication extends Application {
	public static final String CLIENT_ID = "8df8f62b96ba40d11cd1";
	public static final String CLIENT_SECRET = "eee752653d2a1afc5cdff451ebc5d17ec9b9bc9c";
	public static final String BASE_URL = "http://10.0.0.15:8000/apps/bierapp/api2";
	public static final String REDIRECT_URL = "http://www.beterlijst.nl/oauth/catch_me";
	
	public static RemoteClient remoteClient;
	
	public static File generalCache;
	public static File imageCache;
	
	public BierAppApplication() {
		//super();
		
		this.initCaches();
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
		return "http://10.0.0.15:8000/oauth2/authorize/?client_id=" + BierAppApplication.CLIENT_ID + "&redirect_uri=" + BierAppApplication.REDIRECT_URL + "&response_type=code";
	}
	
	public static String getAccessTokenFromCode() {
		return "http://10.0.0.15:8000/oauth2/access_token/";
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
			.cacheInMemory()
			.cacheOnDisc()
			.build();

		// Create global configuration and initialize ImageLoader with this configuration
		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
            .discCache(new UnlimitedDiscCache(BierAppApplication.imageCache.getAbsoluteFile()))
            .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
            .defaultDisplayImageOptions(displayOptions)
            .build()
        );
	}
}
