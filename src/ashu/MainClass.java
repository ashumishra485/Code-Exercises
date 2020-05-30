package ashu;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainClass {

	private static String text;
	private static String[] value;

	public static void main(String[] args) {

		try {
			text = new String(Files.readAllBytes(Paths.get("tiny-tines-sunset.json")), StandardCharsets.UTF_8);
			JSONObject object = new JSONObject(text);
			JSONArray agentArray = object.getJSONArray("agents");

			String urlString = agentArray.getJSONObject(0).getJSONObject("options").getString("url");

			String locationBuffer = callUrl(urlString);

			JSONObject jsonLocationObject = new JSONObject(locationBuffer.toString());

			String sunSetUrl = agentArray.getJSONObject(1).getJSONObject("options").getString("url");

			int index = sunSetUrl.indexOf("?");
			String sunsetSubString = "";

			if (index != -1) {
				sunsetSubString = sunSetUrl.substring(0, index);
			}

			URIBuilder uriBuilder = new URIBuilder(sunsetSubString);
			uriBuilder.addParameter("lat", jsonLocationObject.getString("latitude"));
			uriBuilder.addParameter("lng", jsonLocationObject.getString("longitude"));

			String sunsetBuffer = callUrl(uriBuilder.toString());
			
			JSONObject jsonSunsetObject = new JSONObject(sunsetBuffer);
			JSONObject resultObject = jsonSunsetObject.getJSONObject("results");

			value = new String[] { jsonLocationObject.getString("city"), jsonLocationObject.getString("country"),
					resultObject.getString("sunset") };

			System.out.println(
					MessageStringToken(agentArray.getJSONObject(2).getJSONObject("options").getString("message")));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String callUrl(String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/json; utf-8");
		con.setRequestProperty("Accept", "application/json");

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String input;
		StringBuffer buffer = new StringBuffer();
		while ((input = bufferedReader.readLine()) != null) {
			buffer.append(input);
		}
		bufferedReader.close();

		return buffer.toString();
	}

	public static String MessageStringToken(String messString) {
		int lastIndex = 0, count = 0;
		StringBuilder output = new StringBuilder();
		Pattern pattern = Pattern.compile("\\{(.*?)\\}}");
		Matcher matcher = pattern.matcher(messString);
		while (matcher.find()) {
			output.append(messString, lastIndex, matcher.start()).append(convert(matcher.group(1), count));
			count++;
			lastIndex = matcher.end();
		}
		if (lastIndex < messString.length()) {
			output.append(messString, lastIndex, messString.length());
		}
		return output.toString();
	}

	private static String convert(String token, int count) {
		return value[count];
	}

}
