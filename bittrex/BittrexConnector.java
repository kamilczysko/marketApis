package bittrex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import encryption.ComputeKey;
import interfaces.Connector;
import preSettings.Keys;

public class BittrexConnector extends Connector{

	private String urlBase = "https://bittrex.com/api/v1.1";

	public BittrexConnector(){
		initKeys(Keys.BITTREX_KEY, Keys.BITTREX_SECRET);
	}
	
	public JSONObject makeSimpleRequest(String method, Map<String, String> params) {

		String url = makeURL(method, params);
		JSONObject json = null;

		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
//			System.out.println(result.toString());

			json = new JSONObject(result.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;

	}

	public JSONObject makeRequest(String method, Map<String, String> params) {

		String url = makeURL(method, params);
		String apisign = ComputeKey.calculateHMAC(url, secret);
		System.out.println("hmac: "+apisign);
		JSONObject json = null;

		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			request.addHeader("apisign", apisign);
			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			System.out.println(result.toString());

			json = new JSONObject(result.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;

	}

	private String makeURL(String method, Map<String, String> params) {

		String nonce = new Date().getTime() / 1000 + "";
		String auth = "?apikey=" + this.key + "&nonce=" + nonce;
		String url = urlBase + "" + method;
		if (params != null) {
			url +=  "/" + auth;
			Set<String> keySet = params.keySet();
			for (String key : keySet) {
				String value = params.get(key);
				url += "&" + key + "=" + value;
			}
		}
		System.out.println("url: " + url);

		return url;
	}

}
