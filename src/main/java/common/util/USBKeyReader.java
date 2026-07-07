package common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class USBKeyReader {

    private static final String KEY_FILE_NAME = "usb_key.txt";

    public static String readUSBToken() {
        File[] roots = File.listRoots();
        if (roots != null) {
            for (File root : roots) {
                File keyFile = new File(root, KEY_FILE_NAME);
                if (keyFile.exists() && keyFile.isFile()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(keyFile))) {
                        String token = reader.readLine();
                        if (token != null) {
                            token = token.trim();
                            if (!token.isEmpty()) {
                                return token;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}