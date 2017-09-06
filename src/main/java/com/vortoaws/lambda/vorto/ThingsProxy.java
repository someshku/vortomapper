package com.vortoaws.lambda.vorto;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.amazonaws.services.lambda.runtime.Context;

public class ThingsProxy {
	
	
	private String baseUrl;
	
	private String username;
	private String password;
	
	private String apiToken;
	
	private static final String NAMESPACE = "";
	
	public static ThingsProxy create(String baseUrl, String username, String password, String apiToken) {
		return new ThingsProxy(baseUrl, username, password, apiToken);
	}
	
	

	
	private ThingsProxy(String baseUrl, String username, String password, String token) {
		super();
		this.baseUrl = baseUrl;
		this.username = username;
		this.password = password;
		this.apiToken = token;
	}




	public void updateFeatures(String thingId, String featureJson, Context context) throws Exception, IOException {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();

	
		// Payload
//		StringEntity jsonData = new StringEntity(
//				"{\"button\": {\"properties\": {        \"digital_input_state\": false,        \"application_type\": \"\",        \"digital_input_count\": 0      }    },    \"batteryVoltage\": {      \"properties\": {        \"max_range_value\": 0,        \"min_range_value\": 0,        \"application_type\": \"\",        \"sensor_units\": \"\",        \"sensor_value\": 0,        \"min_measured_value\": 0,        \"current_calibration\": \"\",        \"max_measured_value\": 0      }    }}");

		// Things creds
		credsProvider.setCredentials(new AuthScope(baseUrl, 443),
				new UsernamePasswordCredentials(this.username, this.password));

		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).useSystemProperties()
				.build();

		try {

			HttpPut httpput = new HttpPut(
					this.baseUrl+"/api/1/things/"+NAMESPACE+":"+thingId+"/features");
			httpput.setHeader("x-cr-api-token", apiToken);
			httpput.setEntity(new StringEntity(featureJson));

			context.getLogger().log("Executing request " + httpput.getRequestLine());
			CloseableHttpResponse response = httpclient.execute(httpput);
			try {
				context.getLogger().log("----------------------------------------");
				context.getLogger().log(response.getStatusLine().toString()); 
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}
	
	public String getFeaturesAsJson(String thingId, Context context) throws Exception, IOException {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(baseUrl, 443),
				new UsernamePasswordCredentials(this.username, this.password));

		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).useSystemProperties()
				.build();

		try {

			HttpGet getFeatures = new HttpGet(
					
					this.baseUrl+"/api/1/things/"+thingId+"/features");
			getFeatures.setHeader("x-cr-api-token", apiToken);

			context.getLogger().log("Executing request " + getFeatures.getRequestLine());
			CloseableHttpResponse response = httpclient.execute(getFeatures);
			
			try {
				String responseAsString = IOUtils.toString(response.getEntity().getContent());
				context.getLogger().log("----------------------------------------");
				context.getLogger().log(responseAsString); 
				return responseAsString;
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

	public static void main(String[] args) throws Exception {
		
	}

}