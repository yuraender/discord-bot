package ru.yuraender.discord.music.speechkit;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.util.configuration.file.FileConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YandexSpeechkitAPI {

    private final String OAUTH_TOKEN, FOLDER_ID;

    private String iamToken = null;
    private long tokenExpiresAt = 0;
    @Getter
    @Setter
    private int symbolsLeft = 15000;

    @Getter
    private final Map<Long, Integer> limit = new HashMap<>();

    public YandexSpeechkitAPI() {
        FileConfiguration config = Main.getInstance().getSpeechkitConfig().getConfig();
        OAUTH_TOKEN = config.getString("oauthToken");
        FOLDER_ID = config.getString("folderId");
    }

    @SneakyThrows
    public File getResponse(String text, Voice voice, Voice.Emotion emotion) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost("https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize");
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + createToken());

        List<NameValuePair> params = Lists.newArrayList(
                new BasicNameValuePair("text", text),
                new BasicNameValuePair("lang", "ru-RU"),
                new BasicNameValuePair("voice", voice.name().toLowerCase()),
                new BasicNameValuePair("emotion", emotion.name().toLowerCase()),
                new BasicNameValuePair("speed", "1"),
                new BasicNameValuePair("format", "mp3"),
                new BasicNameValuePair("folderId", FOLDER_ID)
        );
        request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        byte[] bytes = EntityUtils.toByteArray(entity);

        File file = new File(Main.getDataFolder() + "/Audio/" + System.currentTimeMillis() + ".mp3");
        FileUtils.writeByteArrayToFile(file, bytes);

        return file;
    }

    @SneakyThrows
    private String createToken() {
        if (iamToken != null && tokenExpiresAt > System.currentTimeMillis()) {
            return iamToken;
        }

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost("https://iam.api.cloud.yandex.net/iam/v1/tokens");
        request.setEntity(new StringEntity("{\"yandexPassportOauthToken\":\"" + OAUTH_TOKEN + "\"}"));

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        JSONObject responseObject = new JSONObject(EntityUtils.toString(entity));
        iamToken = responseObject.getString("iamToken");
        tokenExpiresAt = ZonedDateTime.parse(responseObject.getString("expiresAt")).toEpochSecond();
        symbolsLeft = 15000;
        limit.clear();

        return iamToken;
    }

    public enum Voice {

        ALENA(Emotion.NEUTRAL, Emotion.GOOD),
        FILIPP(Emotion.NEUTRAL),
        ERMIL(Emotion.NEUTRAL, Emotion.GOOD),
        JANE(Emotion.values()),
        MADIRUS(Emotion.NEUTRAL),
        OMAZH(Emotion.NEUTRAL, Emotion.EVIL),
        ZAHAR(Emotion.NEUTRAL, Emotion.GOOD);

        @Getter
        private final Emotion[] emotions;

        Voice(Emotion... emotions) {
            this.emotions = emotions;
        }

        public enum Emotion {

            NEUTRAL, GOOD, EVIL
        }
    }
}
