package Game;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.io.File;

public class BotTelegram extends TelegramLongPollingBot {

    // --- CẤU HÌNH ---
    private static final String BOT_TOKEN = "8577281382:AAEPgQoPRdu8iH80CPRKcgsRDQGzYzXeXRI";
    private static final String CHAT_ID = "-5031868420"; // <--- ĐIỀN ID NHÓM CỦA BẠN VÀO ĐÂY

    @Override
    public String getBotUsername() { return "Backhso_bot"; }
    @Override
    public String getBotToken() { return BOT_TOKEN; }
    @Override
    public void onUpdateReceived(Update update) {}

    // --- HÀM MỚI: ĐỂ GỌI TỪ triggerBackup ---
    public static void sendBackupNow(File file, String caption) {
        if (!file.exists()) return;

        // Tạo một instance tạm thời để gửi tin (vì hàm execute không phải static)
        BotTelegram bot = new BotTelegram();
        try {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(CHAT_ID);
            sendDocument.setDocument(new InputFile(file));
            sendDocument.setCaption(caption);

            bot.execute(sendDocument); // Gửi đi
            System.out.println(">> Telegram: Gửi thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(">> Telegram Lỗi: " + e.getMessage());
        }
    }
}