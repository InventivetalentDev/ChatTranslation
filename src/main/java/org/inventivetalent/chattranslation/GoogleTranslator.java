package org.inventivetalent.chattranslation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GoogleTranslator extends TranslatorAbstract {

	static final String URL_FORMAT = "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t&sl=%s&tl=%s&q=%s";

	Executor requestExecutor = Executors.newSingleThreadExecutor();

	@Override
	public void translate(final String original, String sourceLanguage, String targetLanguage, final TranslationCallback callback) {
		final URL url;
		try {
			url = new URL(String.format(URL_FORMAT, sourceLanguage, targetLanguage, URLEncoder.encode(original, "UTF-8")));
		} catch (MalformedURLException | UnsupportedEncodingException e) {
			callback.error(e);
			return;
		}
		requestExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setRequestProperty("User-Agent", "MC-Chat-Translator");

					if (connection.getResponseCode() != 200) {
						throw new RuntimeException("Google Translate response code was " + connection.getResponseCode());
					}

					JsonElement jsonElement = new JsonParser().parse(new InputStreamReader(connection.getInputStream()));
					if (jsonElement.isJsonArray()) {
						JsonArray jsonArray = jsonElement.getAsJsonArray();
						callback.call(original,
								jsonArray
										.get(0).getAsJsonArray()
										.get(0).getAsJsonArray()
										.get(0).getAsString());
					}
				} catch (IOException e) {
					callback.error(e);
				}
			}
		});
	}

}
