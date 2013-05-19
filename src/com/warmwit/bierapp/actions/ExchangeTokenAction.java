package com.warmwit.bierapp.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.warmwit.bierapp.BierAppApplication;
import com.warmwit.bierapp.data.ApiAccessToken;
import com.warmwit.bierapp.exceptions.UnexpectedStatusCode;
import com.warmwit.bierapp.utils.LogUtils;
import com.warmwit.bierapp.utils.TokenInfo;

public class ExchangeTokenAction extends Action {
	public static final String LOG_TAG = "ExchangeTokenAction";
	
	private HttpClient httpClient;
	
	private String code;
	
	private TokenInfo tokenInfo;
	
	public ExchangeTokenAction(String code, HttpClient httpClient) {
		this.code = code;
		this.httpClient = httpClient;
	}
	
	public ExchangeTokenAction(String code) {
		this.code = code;
		this.httpClient = new DefaultHttpClient();
	}
	
	public int exchange() {
		HttpPost request = new HttpPost(BierAppApplication.getAccessTokenFromCode());
		
		// Build request body
		try {
			List<NameValuePair> form = Lists.newArrayList();
			
			form.add(new BasicNameValuePair("client_id", BierAppApplication.CLIENT_ID));
			form.add(new BasicNameValuePair("client_secret", BierAppApplication.CLIENT_SECRET));
			form.add(new BasicNameValuePair("code", this.code));
			form.add(new BasicNameValuePair("grant_type", "authorization_code"));
			
			request.setEntity(new UrlEncodedFormEntity(form));
		} catch (UnsupportedEncodingException e) {
			return LogUtils.logException(LOG_TAG, e, 2);
		}
		
		// Execute request
		try {
			HttpResponse response = this.httpClient.execute(request);

			// Check for valid response
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new UnexpectedStatusCode(response.getStatusLine().getStatusCode(), 200);
			}
			
			// Store result
			String json = EntityUtils.toString(response.getEntity());
			this.tokenInfo = TokenInfo.createFromApi(new Gson().fromJson(json, ApiAccessToken.class));
		} catch (IOException e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_CONNECTION);
		} catch (UnexpectedStatusCode e) {
			return LogUtils.logException(LOG_TAG, e, Action.RESULT_ERROR_SERVER);
		}
		
		// Done
		return Action.RESULT_OK;
	}
	
	public TokenInfo getTokenInfo() {
		return this.tokenInfo;
	}
}
