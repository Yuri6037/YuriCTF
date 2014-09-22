package fr.yurictf.language;

import fr.yurictf.server.CTFPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Translator {

    public CTFPlayer thePlayer;
    private String langFileToUse;
    private Map<String, String> playerLanguageMap;

    public Translator(CTFPlayer player){
        thePlayer = player;
        langFileToUse = "english";
        playerLanguageMap = new HashMap<String, String>();
        initTranslatorForPlayer();
    }

    private void initTranslatorForPlayer(){
        if (langFileToUse.equals("french")){
            playerLanguageMap = LanguagesRegistrys.frenchMap;
        } else {
            playerLanguageMap = LanguagesRegistrys.englishMap;
        }
    }

    public void updatePlayerLanguage(String newLang){
        if (newLang.equals("english") || newLang.equals("french")){
            langFileToUse = newLang;
            initTranslatorForPlayer();
        }
    }

    public String translate(String toTranslate){
        if (playerLanguageMap.containsKey(toTranslate)){
            return playerLanguageMap.get(toTranslate);
        }
        return toTranslate;
    }
}
