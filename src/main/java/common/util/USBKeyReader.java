package common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class USBKeyReader {

    private static final String KEY_FILE_NAME = "usb_key.txt";

    public static String readUSBToken() {
        File[] roots = File.listRoots();
        if (roots != null) {
            for (File root : roots) {
                try {
                    File keyFile = new File(root, KEY_FILE_NAME);
                    if (keyFile.exists() && keyFile.isFile()) {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(new FileInputStream(keyFile), StandardCharsets.UTF_8))) {
                            String token = reader.readLine();
                            if (token != null) {
                                token = token.trim();
                                if (!token.isEmpty()) {
                                    System.out.println("USB验证：找到密钥文件 " + keyFile.getAbsolutePath());
                                    return token;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("USB验证：读取 " + root.getAbsolutePath() + " 失败: " + e.getMessage());
                }
            }
        }
        System.out.println("USB验证：未找到 usb_key.txt 文件");
        return null;
    }
}