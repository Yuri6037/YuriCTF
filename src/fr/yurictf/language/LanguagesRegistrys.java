package fr.yurictf.language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class LanguagesRegistrys {
    public static final Map<String, String> englishMap;
    public static final Map<String, String> frenchMap;

    static {
        englishMap = new HashMap<String, String>();
        frenchMap = new HashMap<String, String>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(LanguagesRegistrys.class.getResourceAsStream("content/english.txt")));
        String line;
        try {
            while ((line = reader.readLine()) != null){
                String[] entry = line.split("=");
                if (entry.length == 1) {
                    continue;
                }
                englishMap.put(entry[0], entry[1]);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader1 = new BufferedReader(new InputStreamReader(LanguagesRegistrys.class.getResourceAsStream("content/french.txt")));
        String line1;
        try {
            while ((line1 = reader1.readLine()) != null){
                String[] entry = line1.split("=");
                if (entry.length == 1) {
                    continue;
                }
                frenchMap.put(entry[0], entry[1]);
            }
            reader1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
