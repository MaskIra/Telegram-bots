import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class NewsBot {
    private static final String token = "YOUR_TOKEN_FROM_@BotFather";

    public static void main(String[] args){
        TelegramBot bot = new TelegramBot(token);

        bot.setUpdatesListener(updates -> {
            updates.forEach(upd -> {
                try {
                    long chatId = upd.message().chat().id();
                    String incomeMessage = upd.message().text();

                    if (incomeMessage.matches("^[1-9]\\d*$")) {
                            int number = Integer.parseInt(incomeMessage);
                            Document doc = Jsoup.connect("https://lenta.ru/rss").get();

                            int index = number - 1;
                            Element news = doc.select("item").get(index);
                            String category = news.select("category").text();
                            String title = news.select("title").text();
                            String link = news.select("link").text();
                            String description = news.select("description").text();

                            String result = category + "\n" + title + "\n" + description + "\n" + link;
                            SendMessage request = new SendMessage(chatId, result);
                            bot.execute(request);
                    } else {
                        bot.execute(new SendMessage(chatId, "Enter number (more then 0)"));
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}