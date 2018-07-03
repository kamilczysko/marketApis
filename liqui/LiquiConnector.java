package liqui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import encryption.ComputeKey;

public class LiquiConnector {

	private String secret = "xxx";
	private String key = "xxx";

	private String url = "https://api.liqui.io/tapi";



	private int nonce = -1;

	public JSONObject makeSimpleRequest(String url){
		JSONObject json = null;

		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(url);
			HttpResponse response = client.execute(request);

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
//			System.out.println(result.toString());

			json = new JSONObject(result.toString());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return json;
	}

	public JSONObject makeRequest(Map<String, String> params) {

		nonce = (int) (new Date().getTime() / 1000);
		JSONObject json = null;
		String header = makeHeader(params);
		String hashedKey = ComputeKey.calculateHMAC(header, this.secret);


		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(url);

			request.addHeader("Key", this.key);
			request.addHeader("Sign", hashedKey);

			List<NameValuePair> par = makeParams(params);

			request.setEntity(new UrlEncodedFormEntity(par));
			System.out.println("request zrobiony");
			HttpResponse response = client.execute(request);
//			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			System.out.println(result);

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

	private String makeHeader(Map<String, String> params) {
		String header = "nonce=" + nonce;
		Set<String> keySet = params.keySet();
		for (String key : keySet) {
			String value = params.get(key);
			header += "&" + key + "=" + value;
		}
		System.out.println("header: "+header);
		return header;
	}

	private List<NameValuePair> makeParams(Map<String, String> params) {
		List<NameValuePair> arguments = new ArrayList<NameValuePair>();
		Set<String> keySet = params.keySet();

		arguments.add(new BasicNameValuePair("nonce", nonce + ""));

		for (String key : keySet){
			arguments.add(new BasicNameValuePair(key, params.get(key)));
			System.out.println(key+" - "+params.get(key));
		}

		return arguments;
	}

}
