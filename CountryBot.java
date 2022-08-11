import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.request.InputMedia;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.request.SendLocation;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendMessage;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CountryBot {
    private static final String token = "5594819972:AAFGiRVrsjkLZ2s3LG1b6xqE_t--Vtx0xn8";
    private static final String welcomeMessage = "Hi! Enter the name of the country (Russia, Belarus, France, Peru, etc) to get information about it!";

    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot(token);

        bot.setUpdatesListener(updates -> {
            updates.forEach(upd -> {
                try {
                    System.out.println(upd);
                    long chatId = upd.message().chat().id();
                    String incomeMessage = upd.message().text();
                    String textResult = "";

                    if (incomeMessage.equals("/start")) {
                        bot.execute(new SendMessage(chatId, welcomeMessage));
                    } else {
                        // regex: one word
                        if (incomeMessage.matches("^[a-zA-Z]*$")) {
                            CountryAPI countryAPI = new CountryAPI(incomeMessage);

                            textResult += "Official name: " + countryAPI.getOfficialName() + "\n";
                            textResult += "Capital: " + countryAPI.getCapital();
                            textResult += "\n\n" + "Languages: \n" + String.join("\n", countryAPI.getLanguages());
                            textResult += "\n\n" + "Names on native languages: \n" + String.join("\n", countryAPI.getNativeNames());
                            textResult += "\n\n" + "Current time:" + "\n" + String.join("\n", countryAPI.getTimezonesWithCurrentTimes());

                            InputMedia flag = new InputMediaPhoto(countryAPI.getFlag());
                            flag.caption(textResult);
                            bot.execute(new SendMediaGroup(chatId, flag));

                            bot.execute(new SendLocation(chatId, countryAPI.getLatitude(), countryAPI.getLongitude()));
                        } else {
                            bot.execute(new SendMessage(chatId, "Enter country name in one word"));
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}

class CountryAPI {
    private JsonNode jsonNode;

    public CountryAPI(String countryName) throws IOException {
        String jsonString = Jsoup.connect("https://restcountries.com/v3.1/name/" + countryName)
                .ignoreContentType(true)
                .execute()
                .body();
        ObjectMapper objectMapper = new ObjectMapper();
        this.jsonNode = objectMapper.readTree(jsonString).get(0);
    }

    public String getOfficialName() {
        return jsonNode.get("name").get("official").asText();
    }

    public String getCapital() {
        return jsonNode.get("capital").get(0).asText();
    }

    public List<String> getLanguages() {
        List<String> list = new ArrayList<>();
        Iterator<JsonNode> elements = jsonNode.get("languages").elements();
        while (elements.hasNext()) {
            list.add(elements.next().textValue());
        }
        return list;
    }

    public List<String> getNativeNames() {
        List<String> list = new ArrayList<>();
        Iterator<JsonNode> elements = jsonNode.get("name").get("nativeName").elements();
        while (elements.hasNext()) {
            list.add(elements.next().elements().next().textValue());
        }
        return list;
    }

    public List<String> getTimezonesWithCurrentTimes() {
        List<String> list = new ArrayList<>();
        var timezones = jsonNode.get("timezones");
        for (int i = 0; i < jsonNode.get("timezones").size(); i++) {
            String timezone = timezones.get(i).asText();
            long hour = Long.parseLong(timezone.substring(4, 6));
            OffsetDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC);
            currentTime = timezone.substring(3, 4).equals("+") ? currentTime.plusHours(hour) : currentTime.minusHours(hour);
            list.add(timezone + ": " + getOnlyHourAndMin(currentTime));
        }
        return list;
    }

    private String getOnlyHourAndMin(OffsetDateTime time) {
        return time.toString().substring(11, 16);
    }

    public String getFlag() {
        return jsonNode.get("flags").get("png").asText();
    }

    public float getLatitude() {
        return Float.parseFloat(jsonNode.get("latlng").get(0).asText());
    }

    public float getLongitude() {
        return Float.parseFloat(jsonNode.get("latlng").get(1).asText());
    }
}