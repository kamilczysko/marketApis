package liqui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import preSettings.BotValuesLoader;

public class LiquiRequests {

	private LiquiConnector connector;

	private double boughtAmount = -1;// do sprzedazy potem
	private String boughtPair = "";
	private double buyPrice = -1;
	private double sellPrice = -1;
	private double btcToSpend = 0.02;
	// private String tokenSign = "";

	private double buyFactor = 1.1;
	private double sellFactor = 1.75;

	private double tradeFee = 0.9975;

	private List<String> currencies;

	public LiquiRequests() {
		this.connector = new LiquiConnector();
		this.btcToSpend = BotValuesLoader.btcToSpendOnLiqui;
		this.buyFactor = BotValuesLoader.liquiBuyPriceFactor;
		this.sellFactor = BotValuesLoader.liquiSellPriceFactor;
	}

	public void getSellPrice(String coin) {
		String pair = coin + "_btc";
		String url = "https://api.liqui.io/api/3/ticker/" + pair;

		JSONObject simpleRequest = connector.makeSimpleRequest(url).getJSONObject(pair);
		double ask = simpleRequest.getDouble("sell");
		System.out.println(ask + " - cena");

		this.buyPrice = BigDecimal.valueOf(ask * buyFactor).setScale(8, RoundingMode.HALF_DOWN).doubleValue();
		this.sellPrice = BigDecimal.valueOf(buyPrice * sellFactor).setScale(8, RoundingMode.HALF_DOWN).doubleValue();
	}

	public double getPriceIllBuy() {
		return this.buyPrice;
	}

	public void getCurrencies() {
		String url = "https://api.liqui.io/api/3/info";
		String pairSuffix = "_btc";
		this.currencies = new ArrayList<String>();
		JSONObject simpleRequest = connector.makeSimpleRequest(url).getJSONObject("pairs");
		Iterator<?> iterator = simpleRequest.keys();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			if (key.contains(pairSuffix))
				this.currencies.add(getSignFromPair(key));
		}

		System.out.println("liqui: " + this.currencies);
	}

	private void getInfo() {
		Map<String, String> orderStatus = new LinkedHashMap<String, String>();

		orderStatus.put("method", "getinfo");

		JSONObject res = connector.makeRequest(orderStatus).getJSONObject("return");
		System.out.println(res);
	}

	public void getOrderInfo() {
		Map<String, String> orderStatus = new LinkedHashMap<String, String>();

		orderStatus.put("method", "orderinfo");
		orderStatus.put("order_id", orderID + "");

		JSONObject res = connector.makeRequest(orderStatus).getJSONObject("return");
		System.out.println(res.getJSONObject(orderID + ""));
	}

	private String getSignFromPair(String pair) {
		return pair.substring(0, pair.length() - 4);
	}

	public boolean hasCoin(String coin) {
		return this.currencies.contains(coin);
	}

	private int orderID = -1;

	private void makeBuyOrder(String coin, double rate, double amount) {
		Map<String, String> buyOrder = new LinkedHashMap<String, String>();
		String pair = coin + "_btc";

		buyOrder.put("method", "trade");
		buyOrder.put("pair", pair);
		buyOrder.put("type", "buy");
		buyOrder.put("rate", rate + "");
		buyOrder.put("amount", amount + "");

		JSONObject res = null;
		System.out.println("before buy");

		res = connector.makeRequest(buyOrder);
		orderID = res.getJSONObject("return").getInt("order_id");
		System.out.println(res + "\n" + orderID + "-- orderID");

		this.boughtPair = pair;
		this.boughtAmount = BigDecimal.valueOf(amount * tradeFee).setScale(7, RoundingMode.DOWN).doubleValue();
	}

	public void buyCoins(String token) {
		makeBuyOrder(token, this.buyPrice, amountToBuy());
	}

	private double amountToBuy() {
		double amount = BigDecimal.valueOf(btcToSpend / buyPrice).setScale(5, RoundingMode.HALF_DOWN).doubleValue();
		System.out.println(amount + " - do kupienia");
		return amount;
	}

	private boolean makeSellOrder(String coin, double rate, double amount) {
		Map<String, String> buyOrder = new LinkedHashMap<String, String>();

		buyOrder.put("method", "trade");
		buyOrder.put("pair", boughtPair);
		buyOrder.put("type", "sell");
		buyOrder.put("rate", rate + "");
		buyOrder.put("amount", boughtAmount + "");

		JSONObject res = connector.makeRequest(buyOrder);
		System.out.println(res);
		return (res.getInt("success") == 1);
	}

	public boolean sellCoins() {
		return makeSellOrder(this.boughtPair, this.sellPrice, this.boughtAmount);
	}

	public static void main(String args[]) {
		try {
			new LiquiRequests().getInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
