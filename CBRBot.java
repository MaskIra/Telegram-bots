import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class CBRBot {
    private static final String token = "YOUR_TOKEN_FROM_@BotFather";

    public static void main(String[] args) throws IOException {
        TelegramBot bot = new TelegramBot(token);

        bot.setUpdatesListener(updates -> {
            updates.forEach(upd -> {
                try {
                    long chatId = upd.message().chat().id();
                    String incomeMessage = upd.message().text();

                    if (incomeMessage.equals("/start")) {
                        SendMessage request = new SendMessage(chatId, "Hi, " + upd.message().from().firstName() + "! Good to see you. Enter the date in the format dd/mm/yyyy for which you want to receive the dollar and euro exchange rate");
                        bot.execute(request);
                    } else {
                        // regex: date in format dd/mm/yyyy
                        if (incomeMessage.matches("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)")) {
                            Document doc = Jsoup.connect("https://www.cbr.ru/scripts/XML_daily.asp?date_req=" + incomeMessage).get();
                            String usd = null, eur = null;
                            for (Element valute : doc.select("Valute")) {
                                if (valute.attr("ID").equals("R01235"))
                                    usd = valute.select("Value").text();
                                if (valute.attr("ID").equals("R01239"))
                                    eur = valute.select("Value").text();
                            }
                            bot.execute(new SendMessage(chatId, "Exchange rates: USD/RUB " + usd + ", EUR/RUB " + eur));
                        } else {
                            bot.execute(new SendMessage(chatId, "For exchange rates need format: dd/mm/yyyy"));
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