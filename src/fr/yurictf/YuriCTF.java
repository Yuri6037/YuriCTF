package fr.yurictf;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.yurictf.classes.CTFClass;
import fr.yurictf.classes.etc.EnumClasses;
import fr.yurictf.classes.extention.Extention;
import fr.yurictf.entities.CustomEntityType;
import fr.yurictf.extentions.EventSystem;
import fr.yurictf.extentions.ExtentionEntry;
import fr.yurictf.extentions.ExtentionsManager;
import fr.yurictf.extentions.event.EventClassChange;
import fr.yurictf.extentions.event.EventGameEnded;
import fr.yurictf.extentions.event.EventGameTick;
import fr.yurictf.extentions.event.shop.EventShopCreate;
import fr.yurictf.map.CTFMap;
import fr.yurictf.server.*;
import fr.yurictf.server.EventListener;
import fr.yurictf.shop.PlayerAbility;
import fr.yurictf.votifier.VotifierHandler;
import fr.yurictf.votifier.VotifierManager;
import net.minecraft.server.v1_7_R4.PacketPlayOutScoreboardObjective;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

public class YuriCTF extends JavaPlugin {

    public CTFMap ctfMap;

    public Map<String, CTFPlayer> playerList;
    public Map<String, List<PlayerAbility>> playerAbilitiesMap;

    private EventListener pluginEvents;

    public int redPlayers;
    public int bluePlayers;

    public int redScore;
    public int blueScore;

    public int maxCaptures;

    public String joinMessage = "";
    public String leaveMessage = "";
    public String kickMessage = "";
    public String secondMotdLine = "";

    private List<String> devMembers = new ArrayList<String>();

    public List<Runnable> listActionToPerform = new ArrayList<Runnable>();

    public PermissionManager permissionManager;

    private int choosedMapID;

    public TeamAPI pluginTeams;


    public YuriCTF() {
    }

    public boolean containsDevMember(String name){
        for (String s : devMembers){
            if (s.equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }

    public String getAllDevMembers(){
        String s = "";
        for (String t : devMembers){
            s += t + ",";
        }
        return s;
    }

    /**
     * Returns random integers between par1 and par2 (you need to specify witch random)
     */
    private int generateRandomInteger(int par1, int par2, Random par3Random){
        if (par1 > par2) {
            throw new IllegalArgumentException("par1 > par2");
        }

        long range = (long)par2 - (long)par1 + 1;

        long fraction = (long)(range * par3Random.nextDouble());
        return (int)(fraction + par1);
    }

    public void onEnable() {
        getLogger().info("----------------------------");
        getLogger().info("YuriCTF Plugin by Yuri6037");
        getLogger().info("--> Correcting wrong environment <--");
        playerAbilitiesMap = new HashMap<String, List<PlayerAbility>>();

        permissionManager = null;
        if (ctfMap != null){
            ctfMap.correctEnvironment();
        }
        ctfMap = null;
        pluginEvents = null;
        redScore = 0;
        blueScore = 0;
        redPlayers = 0;
        bluePlayers = 0;
        if (playerList != null){
            playerList.clear();
        }
        if (devMembers != null){
            devMembers.clear();
        }
        if (listActionToPerform != null){
            listActionToPerform.clear();
        }

        assert devMembers != null;
        devMembers.add("Yuri6037");
        devMembers.add("Nathan_4860");

        joinMessage = "";
        leaveMessage = "";
        kickMessage = "";
        secondMotdLine = "";

        getLogger().info("--> Starting plugin initialization <--");
        getLogger().info("-> Initializing INTERNAL_PERMISSION_SYSTEM");
        permissionManager = new PermissionManager(this);
        permissionManager.readPermissions();
        for (String s : permissionManager.allowedBuilders){
            getLogger().info("New builder read : " + s);
        }
        getLogger().info("-> Initializing EXTENTION_SYSTEM");
        new ExtentionsManager(this);
        ExtentionsManager.instance.initAllExtentions();
        EventSystem.tryToSetUpAndStartEventSystem(this);
        getLogger().info("--Performing load of all Extentions--");
        ExtentionsManager.instance.loadUpAllExtentions();
        getLogger().info("-> NMSEntityInjector : Injecting Custom Entities...");
        CustomEntityType.registerEntities();
        getLogger().info("-> NMSEntityInjector : Custom Entities has been injected into Minecraft base !");
        getLogger().info("-> Initializing MAP_SYSTEM");
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();
        //Instantiate CTFMap with config for random map choose !
        ctfMap = new CTFMap(this);
        int max = getConfig().getInt("mapNumber");
        if (max == 0) {
            getLogger().severe("Your map number is 0, must be at least 1 ! Otherwise the game will run with no worlds...");
            getLogger().severe("YuriCTF will now stop to prevent server crash...");
            getPluginLoader().disablePlugin(YuriCTF.getPlugin(YuriCTF.class));
            return;
        }
        if (!ctfMap.mapEditMode.isMapInEditMode()) {
            int i = generateRandomInteger(0, max - 1, new Random());
            choosedMapID = i;
            WorldCreator creator = new WorldCreator("game_" + i);
            creator.generator("YuriCTFWorldGenerator");
            ctfMap.world = getServer().createWorld(creator);

            //Loading map tile entities
            try {
                FileConfiguration file = new YamlConfiguration();
                file.load(new File(getDataFolder() + File.separator + "mapData" + File.separator + "game_" + choosedMapID + ".yml"));
                ctfMap.loadSavedTileEntitys(file, "game_" + choosedMapID);
            } catch (Exception e) {
                getLogger().info("No readable mapData found for the current map");
            }
            //END
        } else {
            for (int i = 0 ; i < max ; i++){
                WorldCreator creator = new WorldCreator("game_" + i);
                creator.generator("YuriCTFWorldGenerator");
                if ((getServer().createWorld(creator)) == null){
                    getLogger().warning("Could not perform load system on map : " + creator.name());
                }

                //Loading map tile entities
                try {
                    FileConfiguration file = new YamlConfiguration();
                    file.load(new File(getDataFolder() + File.separator + "mapData" + File.separator + "game_" + i + ".yml"));
                    ctfMap.loadSavedTileEntitys(file, "game_" + i);
                } catch (Exception e) {
                    getLogger().info("No readable mapData found for the current map");
                }
                //END
            }
        }
        if (!ctfMap.mapEditMode.isMapInEditMode()) {
            Bukkit.getServer().getWorld("game_" + choosedMapID).setAutoSave(false);
        }
        //END
        // Map Varaibles
        ctfMap.blueFlagX = getConfig().getInt("game_" + choosedMapID + ".blueFlag.x");
        ctfMap.blueFlagY = getConfig().getInt("game_" + choosedMapID + ".blueFlag.y");
        ctfMap.blueFlagZ = getConfig().getInt("game_" + choosedMapID + ".blueFlag.z");
        ctfMap.redFlagX = getConfig().getInt("game_" + choosedMapID + ".redFlag.x");
        ctfMap.redFlagY = getConfig().getInt("game_" + choosedMapID + ".redFlag.y");
        ctfMap.redFlagZ = getConfig().getInt("game_" + choosedMapID + ".redFlag.z");
        ctfMap.blueSpawnX = getConfig().getInt("game_" + choosedMapID + ".blueSpawn.x");
        ctfMap.blueSpawnY = getConfig().getInt("game_" + choosedMapID + ".blueSpawn.y");
        ctfMap.blueSpawnZ = getConfig().getInt("game_" + choosedMapID + ".blueSpawn.z");
        ctfMap.redSpawnX = getConfig().getInt("game_" + choosedMapID + ".redSpawn.x");
        ctfMap.redSpawnY = getConfig().getInt("game_" + choosedMapID + ".redSpawn.y");
        ctfMap.redSpawnZ = getConfig().getInt("game_" + choosedMapID + ".redSpawn.z");
        ctfMap.worldTime = getConfig().getLong("game_" + choosedMapID + ".mapTime");

        maxCaptures = getConfig().getInt("capNumber");
        //END
        // Lobby
        ctfMap.lobbySpawnX = getConfig().getInt("lobbySpawn.x");
        ctfMap.lobbySpawnY = getConfig().getInt("lobbySpawn.y");
        ctfMap.lobbySpawnZ = getConfig().getInt("lobbySpawn.z");

        ctfMap.shopSpawnX = getConfig().getInt("shop.x");
        ctfMap.shopSpawnY = getConfig().getInt("shop.y");
        ctfMap.shopSpawnZ = getConfig().getInt("shop.z");
        //END
        String s = getConfig().getString("game_" + choosedMapID + ".mapName");
        if (s == null){
            ctfMap.mapName = "ERROR_NO_MAP_NAME";
        } else {
            ctfMap.mapName = getConfig().getString("game_" + choosedMapID + ".mapName");
        }
        if (!ctfMap.mapEditMode.isMapInEditMode()) {
            ctfMap.resetBlueFlag();
            ctfMap.resetRedFlag();
        }
        ctfMap.waitingWorld = getServer().getWorlds().get(0);
        getLogger().info("-> Initializing CLASSES_SYSTEM");
        new EnumClasses();
        if (EnumClasses.getDefaultClass(ctfMap.mapEditMode.isMapInEditMode()) == null) {
            getLogger().severe("CLASSES_SYSTEM returned error : no default class found");
            getPluginLoader().disablePlugin(YuriCTF.getPlugin(YuriCTF.class));
            return;
        }
        getLogger().info("-> Initializing EVENT_SYSTEM");
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        if (protocolManager == null){
            getLogger().severe("This plugin will be disbaled because dependency ProtocolLibrary.getProtocolManager() is equal to null");
            getPluginLoader().disablePlugin(YuriCTF.getPlugin(YuriCTF.class));
            return;
        }
        protocolManager.addPacketListener(new ProtocolLibListener(this));
        pluginEvents = new fr.yurictf.server.EventListener(this);
        getServer().getPluginManager().registerEvents(pluginEvents, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                ctfMap.updateMap();
                for (Player player0 : getServer().getOnlinePlayers()) {
                    CTFPlayer player = playerList.get(player0.getName());
                    if (player != null) {
                        player.onUpdate();
                    }
                }
                try {
                    for (Runnable r : listActionToPerform) {
                        r.run();
                    }
                    listActionToPerform.clear();
                } catch (ConcurrentModificationException e) {
                    getLogger().warning("Due to bukkit strange plugin code manipulation, using catch block for stopping plugin crash with ConcurentModificationException !");
                }

                EventSystem.callEvent(new EventGameTick(YuriCTF.this));
            }
        }, 1L, 1L);
        Plugin plugin = getServer().getPluginManager().getPlugin("Votifier");
        getLogger().info("Searching for library to hook : CTF_VOTIFIER_SYSTEM");
        new VotifierManager(this);
        if (plugin != null){
            getLogger().info("Trying hooking into VotifierEvent...");
            getServer().getPluginManager().registerEvents(new VotifierHandler(), this);
            VotifierManager.getInstance().isVotifierInstalled = true;
            getLogger().info("YuriCTF VotifierHandler class initialized !");
            getLogger().fine("Successfully hooked library CTF_VOTIFIER_SYSTEM");
        } else {
            getLogger().warning("Votifier could not be found ; some features of YuriCTF will be unavailable...");
            getLogger().severe("Error occured in library CTF_VOTIFIER_SYSTEM");
            VotifierManager.getInstance().isVotifierInstalled = false;
        }
        getLogger().info("-> Initializing PLAYER_NAMETAG_SYSTEM");
        pluginTeams = new TeamAPI(this);
        pluginTeams.resetTeams();
        getLogger().info("-> Initializing JOIN_SYSTEM");
        joinMessage = getConfig().getString("joinMessage");
        leaveMessage = getConfig().getString("leaveMessage");
        kickMessage = getConfig().getString("kickMessage");
        secondMotdLine = getConfig().getString("secondMotdLine");
        getLogger().info("-> Plugin initialization ended");
        getLogger().info("--Reloading players--");
        reloadPlayers();
        reset();
        /**
        for (String name : devMembers){
            BanList serverBanList = getServer().getBanList(BanList.Type.NAME);
            if (serverBanList.isBanned(name)){
                serverBanList.pardon(name);
                getLogger().log(Level.YURI6037_ERROR, "You have not the rights to ban a developer");
            }
            if (!Bukkit.getOfflinePlayer(name).isWhitelisted()){
                Bukkit.getOfflinePlayer(name).setWhitelisted(true);
                getLogger().log(Level.YURI6037_ERROR, "You have not the rights to un-whitelist a developer");
            }
            if (!Bukkit.getOfflinePlayer(name).isOp()){
                Bukkit.getOfflinePlayer(name).setOp(true);
                getLogger().log(Level.YURI6037_ERROR, "You have not the rights to un-op a developer");
            }
        }
         */
        getLogger().info("--Done--");
        getLogger().info("YuriCTF by Yuri6037 has been successfully initialized !");
        getLogger().info("----------------------------");
    }

    private void reloadYuriCTFWithoutReboot(){
        getLogger().info("----------------------------");
        getLogger().info("Rebooting YuriCTF Plugin by Yuri6037");
        getLogger().info("--> Correcting wrong environment <--");
        permissionManager = null;
        ctfMap.correctEnvironment();
        ctfMap = null;
        pluginEvents = null;
        redScore = 0;
        blueScore = 0;
        redPlayers = 0;
        bluePlayers = 0;
        playerList.clear();
        devMembers.clear();
        listActionToPerform.clear();
        joinMessage = "";
        leaveMessage = "";
        kickMessage = "";
        secondMotdLine = "";
        getLogger().info("Destroying event system handling...");
        HandlerList.unregisterAll(YuriCTF.getPlugin(YuriCTF.class));
        pluginEvents = null;
        getLogger().info("Done !");
        getLogger().info("--> Starting plugin initialization <--");
        getLogger().info("-> Reinjecting INTERNAL_PERMISSION_SYSTEM");
        permissionManager = new PermissionManager(this);
        permissionManager.readPermissions();
        for (String s : permissionManager.allowedBuilders){
            getLogger().info("New builder read : " + s);
        }
        getLogger().info("-> Reinjecting MAP_SYSTEM");
        //Instantiate CTFMap with config for random map choose !
        ctfMap = new CTFMap(this);
        int max = getConfig().getInt("mapNumber");
        int i = generateRandomInteger(0, max - 1, new Random());
        choosedMapID = i;
        WorldCreator creator = new WorldCreator("game_" + i);
        creator.generator("YuriCTFWorldGenerator");
        ctfMap.world = getServer().createWorld(creator);
        if (!ctfMap.mapEditMode.isMapInEditMode()) {
            Bukkit.getServer().getWorld("game_" + choosedMapID).setAutoSave(false);
        }

        //Loading map tile entities
        try {
            FileConfiguration file = new YamlConfiguration();
            file.load(new File(getDataFolder() + File.separator + "mapData" + File.separator + "game_" + choosedMapID + ".yml"));
            ctfMap.loadSavedTileEntitys(file, "game_" + choosedMapID);
        } catch (Exception e) {
            getLogger().info("No readable mapData found for the current map");
        }
        //END

        // Map Varaibles
        ctfMap.blueFlagX = getConfig().getInt("game_" + choosedMapID + ".blueFlag.x");
        ctfMap.blueFlagY = getConfig().getInt("game_" + choosedMapID + ".blueFlag.y");
        ctfMap.blueFlagZ = getConfig().getInt("game_" + choosedMapID + ".blueFlag.z");
        ctfMap.redFlagX = getConfig().getInt("game_" + choosedMapID + ".redFlag.x");
        ctfMap.redFlagY = getConfig().getInt("game_" + choosedMapID + ".redFlag.y");
        ctfMap.redFlagZ = getConfig().getInt("game_" + choosedMapID + ".redFlag.z");
        ctfMap.blueSpawnX = getConfig().getInt("game_" + choosedMapID + ".blueSpawn.x");
        ctfMap.blueSpawnY = getConfig().getInt("game_" + choosedMapID + ".blueSpawn.y");
        ctfMap.blueSpawnZ = getConfig().getInt("game_" + choosedMapID + ".blueSpawn.z");
        ctfMap.redSpawnX = getConfig().getInt("game_" + choosedMapID + ".redSpawn.x");
        ctfMap.redSpawnY = getConfig().getInt("game_" + choosedMapID + ".redSpawn.y");
        ctfMap.redSpawnZ = getConfig().getInt("game_" + choosedMapID + ".redSpawn.z");
        ctfMap.worldTime = getConfig().getLong("game_" + choosedMapID + ".mapTime");
        //END
        // Lobby
        ctfMap.lobbySpawnX = getConfig().getInt("lobbySpawn.x");
        ctfMap.lobbySpawnY = getConfig().getInt("lobbySpawn.y");
        ctfMap.lobbySpawnZ = getConfig().getInt("lobbySpawn.z");

        ctfMap.shopSpawnX = getConfig().getInt("shop.x");
        ctfMap.shopSpawnY = getConfig().getInt("shop.y");
        ctfMap.shopSpawnZ = getConfig().getInt("shop.z");
        //END
        String s = getConfig().getString("game_" + choosedMapID + ".mapName");
        if (s == null){
            ctfMap.mapName = "ERROR_NO_MAP_NAME";
        } else {
            ctfMap.mapName = getConfig().getString("game_" + choosedMapID + ".mapName");
        }
        if (!ctfMap.mapEditMode.isMapInEditMode()) {
            ctfMap.resetBlueFlag();
            ctfMap.resetRedFlag();
        }
        ctfMap.waitingWorld = getServer().getWorlds().get(0);
        getLogger().info("-> Reinjecting EVENT_SYSTEM");
        pluginEvents = new fr.yurictf.server.EventListener(this);
        getServer().getPluginManager().registerEvents(pluginEvents, this);
        getLogger().info("-> Reinjecting JOIN_SYSTEM");
        joinMessage = getConfig().getString("joinMessage");
        leaveMessage = getConfig().getString("leaveMessage");
        kickMessage = getConfig().getString("kickMessage");
        secondMotdLine = getConfig().getString("secondMotdLine");
        getLogger().info("-> Plugin reboot ended");
        getLogger().info("--Reloading players--");
        reset();
        reloadPlayers();
        /**
        for (String name : devMembers){
            BanList serverBanList = getServer().getBanList(BanList.Type.NAME);
            if (serverBanList.isBanned(name)){
                serverBanList.pardon(name);
                getLogger().log(Level.YURI6037_ERROR, "You have not the rights to ban a developer");
            }
            if (!Bukkit.getOfflinePlayer(name).isWhitelisted()){
                Bukkit.getOfflinePlayer(name).setWhitelisted(true);
                getLogger().log(Level.YURI6037_ERROR, "You have not the rights to un-whitelist a developer");
            }
            if (!Bukkit.getOfflinePlayer(name).isOp()){
                Bukkit.getOfflinePlayer(name).setOp(true);
                getLogger().log(Level.YURI6037_ERROR, "You have not the rights to un-op a developer");
            }
        }
         */
        getLogger().info("--Reboot Done--");
        getLogger().info("YuriCTF by Yuri6037 has been successfully rebooted !");
        getLogger().info("----------------------------");
    }

    public void onGameEndByTimer(){
        for (Player var : Bukkit.getOnlinePlayers()) {
            CTFPlayer player = playerList.get(var.getName());
            if (player != null) {
                player.associatedPlayer.sendMessage(player.playerTranslator.translate("game.reboot"));
                player.associatedPlayer.sendMessage(player.playerTranslator.translate("game.session.new"));
                StatusBarAPI.removeStatusBar(player.associatedPlayer);
                PacketPlayOutScoreboardObjective packet0 = new PacketPlayOutScoreboardObjective(ctfMap.mapScoreBoard.scoreboard.getObjective("Scores"), 1);
                ((CraftPlayer) player.associatedPlayer).getHandle().playerConnection.sendPacket(packet0);
            }
        }

        //Clearing all waiting world entities
        int jdfghrfdthigebvuj = 0;
        for (Entity e : ctfMap.waitingWorld.getEntities()){
            if (e instanceof Villager) {
                ((Villager) e).setCustomName(null);
                e.remove();
                jdfghrfdthigebvuj++;
            }
        }
        getServer().broadcastMessage("Cleared " + jdfghrfdthigebvuj + " EntityVillager's from lobby map !");

        getServer().unloadWorld(getServer().getWorld("game_" + choosedMapID), true);
        reloadYuriCTFWithoutReboot();
        for (Player var : Bukkit.getOnlinePlayers()) {
            CTFPlayer player = playerList.get(var.getName());
            if (player != null) {
                player.associatedPlayer.sendMessage(player.playerTranslator.translate("game.session.done"));
            }
        }
    }

    private void reset(){
        devMembers.clear();
        devMembers.add("Yuri6037");
        devMembers.add("Nathan_4860");
    }

    public String getLanguage(Player p){
        String language = null;
        try {
            Object ep = getMethod("getHandle", p.getClass()).invoke(p, (Object[]) null);
            Field f = ep.getClass().getDeclaredField("locale");
            f.setAccessible(true);
            language = (String) f.get(ep);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return language;
    }
    private Method getMethod(String name, Class<?> clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(name))
                return m;
        }
        return null;
    }

    public void reloadPlayers() {
        playerList = new HashMap<String, CTFPlayer>();
        CTFClass defaultClass = EnumClasses.getDefaultClass(ctfMap.mapEditMode.isMapInEditMode());
        for (Player player : this.getServer().getOnlinePlayers()) {
            CTFPlayer ctfPlayer = new CTFPlayer(defaultClass, player, this);
            String lang = getLanguage(player);
            if (!(lang.startsWith("fr_"))){
                ctfPlayer.playerTranslator.updatePlayerLanguage("english");
            } else {
                ctfPlayer.playerTranslator.updatePlayerLanguage("french");
            }
            if (ctfPlayer.isBluePlayer){
                if (containsDevMember(ctfPlayer.associatedPlayer.getName())){
                    String name = ChatColor.BLUE + "[Dev] " + ctfPlayer.associatedPlayer.getName();
                    if (name.length() > 16){
                        name = name.substring(0, 16);
                    }
                    ctfPlayer.associatedPlayer.setPlayerListName(name);
                } else if (ctfPlayer.associatedPlayer.isOp()) {
                    String name = ChatColor.BLUE + "[Master] " + ctfPlayer.associatedPlayer.getName();
                    if (name.length() > 16){
                        name = name.substring(0, 16);
                    }
                    ctfPlayer.associatedPlayer.setPlayerListName(name);
                } else {
                    String name = ChatColor.BLUE + ctfPlayer.associatedPlayer.getName();
                    if (name.length() > 16){
                        name = name.substring(0, 16);
                    }
                    ctfPlayer.associatedPlayer.setPlayerListName(name);
                }
            } else if (ctfPlayer.isRedPlayer){
                if (containsDevMember(ctfPlayer.associatedPlayer.getName())){
                    String name = ChatColor.RED + "[Dev] " + ctfPlayer.associatedPlayer.getName();
                    if (name.length() > 16){
                        name = name.substring(0, 16);
                    }
                    ctfPlayer.associatedPlayer.setPlayerListName(name);
                } else if (ctfPlayer.associatedPlayer.isOp()) {
                    String name = ChatColor.RED + "[Master] " + ctfPlayer.associatedPlayer.getName();
                    if (name.length() > 16){
                        name = name.substring(0, 16);
                    }
                    ctfPlayer.associatedPlayer.setPlayerListName(name);
                } else {
                    String name = ChatColor.RED + ctfPlayer.associatedPlayer.getName();
                    if (name.length() > 16){
                        name = name.substring(0, 16);
                    }
                    ctfPlayer.associatedPlayer.setPlayerListName(name);
                }
            }
            ctfPlayer.respawnPlayer();
            playerList.put(player.getName(), ctfPlayer);
        }
    }

    public void terminateGame(){
        getServer().broadcastMessage("Clearing " + ctfMap.world.getEntities().size() + " EntityItem's from ctf game map !");
        for (Entity e : ctfMap.world.getEntities()){
            if (e instanceof Item){
                e.remove();
            }
        }
        ctfMap.world.getEntities().clear();
        ctfMap.world = null;
        getServer().broadcastMessage("Done !");

        for (Player var : Bukkit.getOnlinePlayers()){
            CTFPlayer player = playerList.get(var.getName());
            if (player != null){
                player.associatedPlayer.sendMessage(ChatColor.RED + player.playerTranslator.translate("game.terminated.1"));
                player.associatedPlayer.sendMessage(ChatColor.BLACK + player.playerTranslator.translate("game.terminated.2"));

                player.spawnPlayerInLobby();
                player.onPlayerExitServer();
            }
        }

        //Spawns the shop
        playerAbilitiesMap.clear();
        Map<String, Object[]> map = EventSystem.callEvent(new EventShopCreate(ctfMap));
        boolean b = false;
        for (Map.Entry e : map.entrySet()){
            Object[] obj = (Object[]) e.getValue();
            b = (Boolean) obj[0];
        }
        if (!b) {
            final Villager villager = (Villager) ctfMap.waitingWorld.spawnEntity(new Location(ctfMap.waitingWorld, ctfMap.shopSpawnX, ctfMap.shopSpawnY, ctfMap.shopSpawnZ), EntityType.VILLAGER);
            villager.setProfession(Villager.Profession.LIBRARIAN);
            villager.setCustomName(ChatColor.YELLOW + "Shop");
        }

        ctfMap.mapTimer.elapsedTicks = 0;
        ctfMap.mapTimer.isFinished = true;

        EventSystem.callEvent(new EventGameEnded(this));

        pluginTeams.resetTeams();
    }

    public void onDisable() {
        getLogger().info("----------------------------");
        getLogger().info("YuriCTF Plugin by Yuri6037");
        getLogger().info("-> Unloading INTERNAL_PERMISSION_SYSTEM");
        permissionManager.writePermissions();
        getLogger().info("-> Unloading MAP_SYSTEM");
        if (!ctfMap.mapEditMode.isMapInEditMode()) {
            Bukkit.getServer().unloadWorld(getServer().getWorld("game_0"), true);
            Bukkit.getServer().reload();
        }
        getLogger().info("-> Unloading CLASSES_SYSTEM");
        getLogger().info("-> Unloading EVENT_SYSTEM");
        HandlerList.unregisterAll(YuriCTF.getPlugin(YuriCTF.class));
        pluginEvents = null;
        getLogger().info("-> Unloading EXTENTION_SYSTEM");
        ExtentionsManager.instance.unloadAllExtentions();
        getLogger().info("YuriCTF by Yuri6037 has been successfully unloaded !");
        getLogger().info("----------------------------");
    }

    public void shutDownServer(){
        for (Player player : getServer().getOnlinePlayers()){
            player.kickPlayer("You have been disconnected due to game end !");
        }
        Bukkit.getServer().unloadWorld(getServer().getWorld("game_" + choosedMapID), true);
        Bukkit.getServer().shutdown();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         /** Commands replacement */
        if (label.equalsIgnoreCase("ctfKick")){
            if (args.length == 1){
                if (containsDevMember(args[0])){
                    sender.sendMessage("You cannot kick a developer");
                    return true;
                }
                getServer().dispatchCommand(sender, "kick " + args[0]);
            } else if (args.length == 2){
                if (containsDevMember(args[0])){
                    sender.sendMessage("You cannot kick a developer");
                    return true;
                }
                getServer().dispatchCommand(sender, "kick " + args[0] + " " + args[1]);
            } else{
                sender.sendMessage("Unable to perform command");
                return true;
            }
            return true;
        }
        if (label.equalsIgnoreCase("ctfBan")){
            if (args.length == 1){
                if (containsDevMember(args[0])){
                    sender.sendMessage("You cannot ban a developer");
                    return true;
                }
                getServer().dispatchCommand(sender, "ban " + args[0]);
            } else if (args.length == 2){
                if (containsDevMember(args[0])){
                    sender.sendMessage("You cannot ban a developer");
                    return true;
                }
                getServer().dispatchCommand(sender, "ban " + args[0] + " " + args[1]);
            } else{
                sender.sendMessage("Unable to perform command");
            }
            sender.sendMessage("User banned successfully !");
            return true;
        }
        if (label.equalsIgnoreCase("ctfDeop")){
            if (args.length == 1) {
                if (containsDevMember(args[0])) {
                    sender.sendMessage(ChatColor.RED + "You cannot deop a developer");
                    return false;
                }
                if (getServer().getPlayer(args[0]).isOnline()){
                    Player p = getServer().getPlayer(args[0]);
                    if (p.isOp()){
                        p.setOp(false);
                        sender.sendMessage(ChatColor.GREEN + "You deopped player " + args[0]);
                        p.sendMessage(ChatColor.YELLOW + "You have been deopped by " + sender.getName());
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player " + args[0] + " is already deopped !");
                    }
                    return true;
                } else {
                    getServer().dispatchCommand(sender, "deop " + args[0]);
                    return true;
                }
            } else if (args.length == 2){
                if (getServer().getPlayer(args[0]).isOnline()){
                    Player p = getServer().getPlayer(args[0]);
                    if (p.isOp()){
                        p.setOp(false);
                        sender.sendMessage(ChatColor.GREEN + "You deopped player " + args[0]);
                        p.sendMessage(ChatColor.YELLOW + "You have been deopped by " + sender.getName() + " for " + args[1]);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player " + args[0] + " is already deopped !");
                    }
                    return true;
                } else {
                    getServer().dispatchCommand(sender, "deop " + args[0]);
                    return true;
                }
            } else {
                sender.sendMessage("Unable to perform command");
                return false;
            }
        }

        if (label.equalsIgnoreCase("ctfOp")){
            if (args.length == 1){
                if (getServer().getPlayer(args[0]).isOnline()){
                    Player p = getServer().getPlayer(args[0]);
                    if (p.isOp()){
                        sender.sendMessage(ChatColor.RED + "Player " + args[0] + " is already opped !");
                    } else {
                        p.setOp(true);
                        sender.sendMessage(ChatColor.GREEN + "You opped player " + args[0]);
                        p.sendMessage(ChatColor.YELLOW + "You have been opped by " + sender.getName());
                    }
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Unable to find player " + args[0]);
                    return false;
                }
            } else{
                sender.sendMessage("Unable to perform command");
                return false;
            }
        }
        if (label.equalsIgnoreCase("ctfUnban")){
            if (args.length == 1){
                 getServer().dispatchCommand(sender, "pardon " + args[0]);
            } else{
                sender.sendMessage("Unable to perform command");
                return true;
            }
            return true;
        }
        /** End */

        CTFPlayer ctfPlayer = playerList.get(sender.getName());
        if (ctfPlayer == null){
            sender.sendMessage("ERROR_DECLARED_INSTANCE_NULL");
            return false;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ctfPlayer.playerTranslator.translate("command.noPlayer"));
            return false;
        }

        //CTF map special points (flags, red/blue spawn, lobby spawn)
        if (label.equalsIgnoreCase("toggleFlagSet") && sender.isOp() && ctfMap.mapEditMode.isPlayerAllowedToBuild((Player) sender)) {
            ctfMap.mapEditMode.flagPlacerTool = !ctfMap.mapEditMode.flagPlacerTool;
            getServer().broadcastMessage("FlagSetTool=" + ctfMap.mapEditMode.flagPlacerTool);
            getServer().broadcastMessage(ChatColor.BLACK + "[EDITING_MODE] : Flag Set Mode toggled !");
            return true;
        }
        if (label.equalsIgnoreCase("setRedSpawn") && sender.isOp() && ctfMap.mapEditMode.isPlayerAllowedToBuild((Player) sender)) {
            String name = ctfPlayer.associatedPlayer.getLocation().getWorld().getName();
            if (name.equals(getServer().getWorlds().get(0).getName())){
                return false;
            }
            getConfig().set(name + ".redSpawn.x", ((Player) sender).getLocation().getBlockX());
            getConfig().set(name + ".redSpawn.y", ((Player) sender).getLocation().getBlockY());
            getConfig().set(name + ".redSpawn.z", ((Player) sender).getLocation().getBlockZ());
            saveConfig();
            getServer().broadcastMessage(ChatColor.BLACK + "[EDITING_MODE] : Red Spawn set !");
            return true;
        }
        if (label.equalsIgnoreCase("setBlueSpawn") && sender.isOp() && ctfMap.mapEditMode.isPlayerAllowedToBuild((Player) sender)) {
            String name = ctfPlayer.associatedPlayer.getLocation().getWorld().getName();
            if (name.equals(getServer().getWorlds().get(0).getName())){
                return false;
            }
            getConfig().set(name + ".blueSpawn.x", ((Player) sender).getLocation().getBlockX());
            getConfig().set(name + ".blueSpawn.y", ((Player) sender).getLocation().getBlockY());
            getConfig().set(name + ".blueSpawn.z", ((Player) sender).getLocation().getBlockZ());
            saveConfig();
            getServer().broadcastMessage(ChatColor.BLACK + "[EDITING_MODE] : Blue Spawn set !");
            return true;
        }
        if (label.equalsIgnoreCase("setLobbySpawn") && sender.isOp() && ctfMap.mapEditMode.isPlayerAllowedToBuild((Player) sender)) {
            getConfig().set("lobbySpawn.x", ((Player) sender).getLocation().getBlockX());
            getConfig().set("lobbySpawn.y", ((Player) sender).getLocation().getBlockY());
            getConfig().set("lobbySpawn.z", ((Player) sender).getLocation().getBlockZ());
            saveConfig();
            getServer().broadcastMessage(ChatColor.BLACK + "[EDITING_MODE] : Lobby Spawn set !");
            return true;
        }
        if (label.equalsIgnoreCase("setShopSpawn") && sender.isOp() && ctfMap.mapEditMode.isPlayerAllowedToBuild((Player) sender)) {
            getConfig().set("shop.x", ((Player) sender).getLocation().getBlockX());
            getConfig().set("shop.y", ((Player) sender).getLocation().getBlockY());
            getConfig().set("shop.z", ((Player) sender).getLocation().getBlockZ());
            saveConfig();
            getServer().broadcastMessage(ChatColor.BLACK + "[EDITING_MODE] : Shop Spawn set !");
            return true;
        }

        if (label.equalsIgnoreCase("setMapTime") && sender.isOp() && ctfMap.mapEditMode.isPlayerAllowedToBuild((Player) sender)){
            if (args.length == 1) {
                String name = ctfPlayer.associatedPlayer.getLocation().getWorld().getName();
                getConfig().set(name + ".mapTime", ((Player) sender).getLocation().getBlockX());
                saveConfig();
                getServer().broadcastMessage(ChatColor.BLACK + "[EDITING_MODE] : Map time set !");
                return true;
            } else {
                sender.sendMessage("Invalid args number");
                return false;
            }
        }
        //End

        //Used to change map between server ctf maps (Edit Mode only)
        if (label.equalsIgnoreCase("ctfChangeMap") && ctfMap.mapEditMode.isPlayerAllowedToBuild((Player) sender)){
            if (args.length == 0){
                sender.sendMessage("Invalid number of arguments");
                return false;
            }
            String mapName = args[0];
            if ((getServer().getWorld(mapName)) != null){
                Location loc = getServer().getWorld(mapName).getSpawnLocation();
                Player p = (Player) sender;
                p.teleport(loc);
                return true;
            } else {
                sender.sendMessage("World does not exist");
                return true;
            }
        }
        //End

        //Generates new ctf map (only working in edit mode)
        if (label.equalsIgnoreCase("ctfGenerateWorld") && ctfMap.mapEditMode.isMapInEditMode() && sender.isOp() && ctfMap.mapEditMode.isPlayerAllowedToBuild((Player) sender)){
            getServer().broadcastMessage("Generating new CTF map...");
            int numberMap = getConfig().getInt("mapNumber");
            WorldCreator creator = new WorldCreator("game_" + numberMap);
            creator.generator("YuriCTFWorldGenerator");
            getServer().createWorld(creator);
            //
            numberMap++;
            getConfig().set("mapNumber", numberMap);
            //
            saveConfig();
            getServer().broadcastMessage("Map generation done ! You can now use /ctfChangeMap game_" + (numberMap - 1));
            return true;
        }
        //End

        //Ends the current game
        if (label.equalsIgnoreCase("ctfEndGame") && sender.isOp()){
            if (ctfMap.mapEditMode.isMapInEditMode()){
                sender.sendMessage("You can't end the CTF game because no CTF are running : you're in edit mode...");
                return false;
            }
            if (ctfMap.mapTimer.isFinished){
                sender.sendMessage("You can't end the CTF game because no CTF are running : CTF already finished...");
                return false;
            }

            terminateGame();
            return true;
        }
        //End

        //CTF TileEntities management
        if (label.equalsIgnoreCase("ctfCreateTileEntity") && sender.isOp() && ctfMap.mapEditMode.isPlayerAllowedToBuild((Player) sender)){
            if (args.length == 2){
                String className = args[0];
                String tileEntityArgs = args[1];

                ItemStack stack = new ItemStack(Material.STICK, 1);
                Extention.renameItem(stack, "TILE_ENTITY_PLACE");
                List<String> list = new ArrayList<String>();
                list.add(className);
                list.add(tileEntityArgs);
                ItemMeta meta = stack.getItemMeta();
                meta.setLore(list);
                stack.setItemMeta(meta);

                ((Player) sender).getInventory().addItem(stack);
                sender.sendMessage("You have been equipped with a TileEntity creation stick, just left click blocks you want to be TileEntitys !");
                return true;
            } else {
                sender.sendMessage("Invalid number of arguments");
                return false;
            }
        }
        if (label.equalsIgnoreCase("ctfDeleteTileEntity") && sender.isOp() && ctfMap.mapEditMode.isPlayerAllowedToBuild((Player) sender)){
            ItemStack stack = new ItemStack(Material.STICK, 1);
            Extention.renameItem(stack, "TILE_ENTITY_DELETE");

            ((Player) sender).getInventory().addItem(stack);
            sender.sendMessage("You have been equipped with a TileEntity deletion stick, just left click blocks you want to remove their TileEntitys !");
            return true;
        }
        //End

        //Editing mode user management
        if (label.equalsIgnoreCase("ctfAddBuilder") && sender.isOp() && ctfMap.mapEditMode.isMapInEditMode()){
            if (args.length < 1){
                sender.sendMessage("Invalid number of argument");
                return false;
            }
            if (permissionManager.allowedBuilders.contains(args[0])){
                sender.sendMessage("Builder already exists");
                return false;
            }
            permissionManager.allowedBuilders.add(args[0]);
            ctfMap.mapEditMode.allowedEditingPlayers.add(args[0]);
            sender.sendMessage("Builder added !");
            return true;
        }
        if (label.equalsIgnoreCase("ctfRemoveBuilder") && sender.isOp() && ctfMap.mapEditMode.isMapInEditMode()){
            if (args.length < 1){
                sender.sendMessage("Invalid number of argument");
                return false;
            }
            if (!permissionManager.allowedBuilders.contains(args[0])){
                sender.sendMessage("Builder does not exists");
                return false;
            }
            permissionManager.allowedBuilders.remove(args[0]);
            ctfMap.mapEditMode.allowedEditingPlayers.remove(args[0]);
            sender.sendMessage("Builder removed !");
            return true;
        }
        //End

        //The extention command
        if (label.equalsIgnoreCase("extention") && args.length > 0 && sender.isOp()) {
            String com = args[0];
            if (com.equalsIgnoreCase("list")){
                Map<String, ExtentionEntry> map = ExtentionsManager.instance.extentionMap;
                sender.sendMessage(ChatColor.DARK_BLUE + "[ExtentionsManager] : Listing of all loaded extentions > ");
                int i = 0;
                for (Map.Entry entry : map.entrySet()){
                    ExtentionEntry extEntry = (ExtentionEntry) entry.getValue();
                    String name = extEntry.extID;
                    String author = extEntry.extAuthor;
                    String version = extEntry.extVersion;
                    String description = extEntry.extDescription;
                    sender.sendMessage(ChatColor.YELLOW + "Entry #" + i + " :");
                    sender.sendMessage(ChatColor.ITALIC + "     Name : " + name);
                    sender.sendMessage(ChatColor.ITALIC + "     Version : " + version);
                    sender.sendMessage(ChatColor.ITALIC + "     Author : " + author);
                    sender.sendMessage(ChatColor.ITALIC + "     Description : " + description);
                    i++;
                }
                sender.sendMessage(ChatColor.DARK_BLUE + "[ExtentionsManager] : Done !");
            } else if (com.equalsIgnoreCase("disable") && args.length == 2){
                String id = args[1];
                boolean b = ExtentionsManager.instance.unloadSpecificExtention(id);
                if (b){
                    sender.sendMessage(ChatColor.GREEN + "[ExtentionsManager] : Extention " + id + " successfully disabled !");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "[ExtentionsManager] : An error has occured while trying to disable extention " + id + "...");
                    return true;
                }
            }
            return false;
        }
        //End

        //The class command
        if (label.equalsIgnoreCase("class") && args.length == 1) {
            if (ctfMap.mapTimer.isFinished){
                sender.sendMessage(ChatColor.BOLD + ctfPlayer.playerTranslator.translate("command.class.noGame"));
                return false;
            }
            if (args[0].equalsIgnoreCase("list")){
                sender.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "" + ChatColor.BOLD + ctfPlayer.playerTranslator.translate("command.class.list"));
                for (String s : EnumClasses.getAllClassesHelp(ctfPlayer)){
                    sender.sendMessage(ChatColor.GRAY + s);
                }
                return true;
            }
            CTFClass ctfClass = EnumClasses.getCorrespondingClass(args[0]);
            if (ctfClass == null || !ctfClass.canAccessClass(this, ctfPlayer)) {
                sender.sendMessage(ChatColor.BOLD + ctfPlayer.playerTranslator.translate("command.class.notFound"));
                return false;
            }
            CTFPlayer player = playerList.get(sender.getName());
            if (player != null) {
                Map<String, Object[]> map = EventSystem.callEvent(new EventClassChange(player, ctfClass));
                boolean b = false;
                for (Map.Entry e : map.entrySet()){
                    Object[] obj = (Object[]) e.getValue();
                    b = (Boolean) obj[0];
                }
                if (b){
                    player.associatedPlayer.sendMessage("Your class change request has been cancelled");
                    return false;
                } else {
                    player.currentPlayerClass = ctfClass;
                    player.onDeath();
                    player.respawnPlayer();
                    String s = ctfPlayer.playerTranslator.translate("command.class.choosed");
                    String s1 = s.replace("#CLASS_NAME#", ctfClass.getClassName());
                    sender.sendMessage(ChatColor.GREEN + s1);
                    return true;
                }
            }
        }
        //End

        sender.sendMessage(ctfPlayer.playerTranslator.translate("command.invalid"));
        return false;
    }

    public static String getCardinalDirection(Player player) {
        double rotation = (player.getLocation().getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return "N";
        } else if (22.5 <= rotation && rotation < 67.5) {
            return "NE";
        } else if (67.5 <= rotation && rotation < 112.5) {
            return "E";
        } else if (112.5 <= rotation && rotation < 157.5) {
            return "SE";
        } else if (157.5 <= rotation && rotation < 202.5) {
            return "S";
        } else if (202.5 <= rotation && rotation < 247.5) {
            return "SW";
        } else if (247.5 <= rotation && rotation < 292.5) {
            return "W";
        } else if (292.5 <= rotation && rotation < 337.5) {
            return "NW";
        } else if (337.5 <= rotation && rotation < 360.0) {
            return "N";
        } else {
            return null;
        }
    }
}
