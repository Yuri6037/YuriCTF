package fr.yurictf.extentions;

import fr.yurictf.YuriCTF;
import fr.yurictf.extentions.api.Extention;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class ExtentionsManager {

    public static ExtentionsManager instance;

    public Map<String, ExtentionEntry> extentionMap = new HashMap<String, ExtentionEntry>();

    protected YuriCTF thePlugin;

    public ExtentionsManager(YuriCTF plugin){
        thePlugin = plugin;
        instance = this;
    }

    public void initAllExtentions(){
        File cache = new File(thePlugin.getDataFolder() + File.separator + "cache" + File.separator);
        if (!cache.exists()){
            cache.mkdirs();
        }
        File f = new File(thePlugin.getDataFolder() + File.separator + "extentions" + File.separator);
        if (!f.exists()){
            f.mkdirs();
        }
        File[] exts = f.listFiles();
        for (File ext : exts){
            if (!ext.isDirectory()){
                if (getFileExtention(ext).equalsIgnoreCase("JAR")){
                    try {
                        File extInfoFile = new File(thePlugin.getDataFolder() + File.separator + "cache" + File.separator + "EXTENTION.CTF");
                        ZipFileReader.extractTo(ext.toString(), "EXTENTION.CTF", cache + File.separator);
                        Map<String, String> infos = getExtentionInfos(extInfoFile);
                        String id = infos.get("ID");
                        String author = infos.get("AUTHOR");
                        String version = infos.get("VERSION");
                        String mainClass = infos.get("CLASS_MAIN");
                        String description = "ERROR_EXTENTION_NO_DESCRIPTION";
                        if (infos.containsKey("DESCRIPTION")) {
                             description = infos.get("DESCRIPTION");
                        }
                        URLClassLoader child = new URLClassLoader(new URL[]{ext.toURI().toURL()}, this.getClass().getClassLoader());
                        Class<? super Extention> classToLoad = (Class<? super Extention>) Class.forName(mainClass, true, child);
                        Extention extention = (Extention) classToLoad.getDeclaredConstructor().newInstance();
                        ExtentionEntry entry = new ExtentionEntry(id, author, version, extention, description);
                        entry.load(thePlugin.getLogger());
                        extentionMap.put(id, entry);

                        extInfoFile.delete();
                    } catch (Exception e) {
                        thePlugin.getLogger().warning("Could not load YuriCTF Extention File : \'" + ext.getName() + "\' : " + e.getClass().getName() + " - " + e.getMessage());
                        return;
                    }
                }
            }
        }
    }

    public void loadUpAllExtentions(){
        for (Map.Entry entry : extentionMap.entrySet()){
            ExtentionEntry var = (ExtentionEntry) entry.getValue();
            var.theExtentionClass.onExtentionLoad();
        }
    }

    public void unloadAllExtentions(){
        for (Map.Entry entry : extentionMap.entrySet()){
            ExtentionEntry var = (ExtentionEntry) entry.getValue();
            var.theExtentionClass.onExtentionUnLoad();
        }
        extentionMap.clear();
    }

    public boolean unloadSpecificExtention(String id){
        try {
            ExtentionEntry entry = extentionMap.get(id);
            entry.theExtentionClass.onExtentionUnLoad();
            extentionMap.remove(entry.extID);
            return true;
        } catch (Exception e){
            thePlugin.getLogger().warning("Could not unload YuriCTF Extention : \'" + id + "\' !");
            return false;
        }
    }

    private Map<String, String> getExtentionInfos(File f){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            Map<String, String> result = new HashMap<String, String>();
            String line;
            while ((line = reader.readLine()) != null){
                String[] s = line.split(":->:");
                if (s.length > 1) {
                    result.put(s[0], s[1]);
                }
            }
            reader.close();
            return result;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private String getFileExtention(File f){
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
