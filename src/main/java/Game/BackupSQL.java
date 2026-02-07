package Game;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BackupSQL {

    // --- CẤU HÌNH THEO ẢNH BẠN GỬI ---
    private static final String DB_NAME = "maxhso"; // <--- Đã sửa theo ảnh của bạn
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    // Kiểm tra lại đường dẫn này trên máy bạn
    private static final String MYSQL_DUMP_PATH = "C:/xampp/mysql/bin/mysqldump.exe";

    public static boolean performBackup() {
        try {
            // 1. Tạo nơi lưu trữ
            File folder = new File("backup_data");
            if (!folder.exists()) folder.mkdirs();

            // Đường dẫn tuyệt đối file đầu ra
            String filePath = folder.getAbsolutePath() + File.separator + "backup_full.sql";

            System.out.println("Dang backup database: " + DB_NAME + "...");

            // 2. Cấu hình lệnh
            List<String> command = new ArrayList<>();
            command.add(MYSQL_DUMP_PATH);

            // Login
            command.add("-u" + DB_USER);
            if (!DB_PASS.isEmpty()) command.add("-p" + DB_PASS);
            command.add("-hlocalhost");
            command.add("-P3306");

            // --- CÁC CỜ QUAN TRỌNG NHẤT ---

            // 1. Dòng này giúp in mỗi dữ liệu ra 1 dòng riêng biệt (File sẽ nặng hơn nhưng dễ check)
            command.add("--skip-extended-insert");

            // 2. Thêm tên cột vào lệnh insert để an toàn
            command.add("--complete-insert");

            // 3. Lấy full sự kiện và thủ tục game (Rất quan trọng)
            command.add("--routines");
            command.add("--events");
            command.add("--triggers");

            // 4. Xử lý mã hóa và tiếng Việt
            command.add("--hex-blob");
            command.add("--default-character-set=utf8mb4");

            // 5. File đích
            command.add("--result-file=" + filePath);

            // 6. Tên Database (Luôn cuối cùng)
            command.add(DB_NAME);

            // 3. Chạy lệnh
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            // Fix môi trường Windows
            Map<String, String> env = pb.environment();
            env.put("OS", "Windows_NT");

            Process process = pb.start();

            // Đọc log để debug
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("error") || line.contains("Usage")) {
                    System.err.println("MYSQL LOG: " + line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // Copy ra file backup.sql để Bot gửi đi
                File finalFile = new File("backup.sql");
                java.nio.file.Files.copy(
                        new File(filePath).toPath(),
                        finalFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                return true;
            } else {
                System.err.println("❌ LỖI BACKUP. Exit Code: " + exitCode);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}