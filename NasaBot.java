import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import org.jsoup.Jsoup;

public class NasaBot {
    private static final String token = "YOUR_TOKEN_FROM_@BotFather";

    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot(token);

        bot.setUpdatesListener(updates -> {
            updates.forEach(upd -> {
                try {
                    System.out.println(upd);
                    long chatId = upd.message().chat().id();
                    String incomeMessage = upd.message().text();

                    String date = incomeMessage; // date=2022-08-08
                    String jsonString = Jsoup.connect("https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY&date=" + date)
                            .ignoreContentType(true)
                            .execute()
                            .body();
                    ObjectMapper objectMapper = new ObjectMapper();
                    var jsonNode = objectMapper.readTree(jsonString);
                    String imageUrl = jsonNode.get("url").asText();
                    String explanation = jsonNode.get("explanation").asText();
                    String result = imageUrl + "\n" + explanation;

                    SendMessage request = new SendMessage(chatId, result);
                    bot.execute(request);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}