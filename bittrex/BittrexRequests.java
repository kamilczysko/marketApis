package bittrex;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import preSettings.BotValuesLoader;

public class BittrexRequests {

	private Map<String, String> methods;
	private BittrexConnector bittrex;
	private List<String> currencies;

	private double buyPrice = -1;
	private double sellPrice = -1;
	private double btcToSpend = 0.02;
	private double boughtAmount = 0.0;
	private String boughtPair = "";
	private String tokenSign = "";

	private double buyFactor = 1.1;
	private double sellFactor = 1.75;

	private double tradeFee = 0.9975;

	public BittrexRequests() {
		this.bittrex = new BittrexConnector();
		this.methods = new TreeMap<String, String>();
		methods.put("balance", "/account/getbalance");
		methods.put("getOrder", "/account/getorder");
		methods.put("buy", "/market/buylimit");
		methods.put("sell", "/market/selllimit");
		methods.put("ticker", "/public/getticker");
		methods.put("altcoins", "/public/getmarkets");
		methods.put("openOrders", "/market/getopenorders");

		this.btcToSpend = BotValuesLoader.btcToSpendOnBittrex;
		this.buyFactor = BotValuesLoader.bittrexBuyPriceFactor;
		this.sellFactor = BotValuesLoader.bittrexSellPriceFactor;

	}

	public void getBalance() {
		Map<String, String> params = new LinkedHashMap<String, String>();

		params.put("currency", "btc");
		JSONObject res = bittrex.makeRequest(methods.get("balance"), params).getJSONObject("result");
		System.out.println("Bittrex balance: "+res);

	}

	public void getCurrencies() {
		this.currencies = new ArrayList<String>();
		JSONArray request = bittrex.makeSimpleRequest(methods.get("altcoins"), null).getJSONArray("result");

		for (int i = 0; i < request.length(); i++)
			this.currencies.add(request.getJSONObject(i).getString("MarketCurrency").toLowerCase());

		System.out.println("bittrex: "+this.currencies);
	}

	public boolean hasCoin(String coin) {
		return this.currencies.contains(coin);
	}

	public void getSellPrice(String token) {
		String coin = "btc-" + token;
		Map<String, String> params = new LinkedHashMap<String, String>();

		params.put("market", coin);
		JSONObject request = bittrex.makeSimpleRequest(methods.get("ticker"), params).getJSONObject("result");

		double ask = request.getDouble("Ask");
		System.out.println(ask);

		this.buyPrice = BigDecimal.valueOf(ask * buyFactor).setScale(8, RoundingMode.HALF_DOWN).doubleValue();
		this.sellPrice = BigDecimal.valueOf(buyPrice * sellFactor).setScale(8, RoundingMode.HALF_DOWN).doubleValue();
	}

	public double getAmountToBuy() {
		return this.boughtAmount;
	}

	public double getPriceIllBuy() {
		return this.buyPrice;
	}

	private void buyOrder(double rate, double quantity, String token) {
		this.tokenSign = token;
		String coin = "btc-" + token;
		Map<String, String> params = new LinkedHashMap<String, String>();

		params.put("market", coin);
		params.put("quantity", quantity + "");
		params.put("rate", rate + "");

		if (!BotValuesLoader.debug)
			bittrex.makeRequest(methods.get("buy"), params);

		this.boughtPair = coin;
		this.boughtAmount = BigDecimal.valueOf(quantity * tradeFee).setScale(7, RoundingMode.DOWN).doubleValue();
	}

	public void buyCoins(String token) {
		double amount = amountToBuy();
		buyOrder(this.buyPrice, amount, token);
	}

	private double amountToBuy() {
		double amount = BigDecimal.valueOf(btcToSpend / buyPrice).setScale(5, RoundingMode.HALF_DOWN).doubleValue();
		System.out.println(amount + " - do kupienia");
		return amount;
	}

	private boolean sellOrder(String tokenPair, double rate, double quantity) {

		Map<String, String> params = new LinkedHashMap<String, String>();

		params.put("market", tokenPair);
		params.put("quantity", quantity + "");
		params.put("rate", rate + "");

		if (!BotValuesLoader.debug) {
			JSONObject res = bittrex.makeRequest(methods.get("sell"), params);
			return res.getBoolean("success");
		}
		return false;
	}

	public boolean sellCoins() {
		return sellOrder(this.boughtPair, this.sellPrice, this.boughtAmount);
	}

	public static void main(String args[]) {
		new BittrexRequests().getBalance();
	}

}
