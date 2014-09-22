package fr.yurictf.map;

import fr.yurictf.entities.CustomEntityType;
import fr.yurictf.extentions.EventSystem;
import fr.yurictf.extentions.api.TileEntityAPIHandler;
import fr.yurictf.extentions.event.flag.EventFlagReset;
import fr.yurictf.map.tileentity.TileEntity;
import fr.yurictf.YuriCTF;
import fr.yurictf.server.CTFPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CTFMap {

    /**
     * An instance of the current loaded world and of the waiting world
     */
    public World world;
    public World waitingWorld;

    /**
     * The time value
     */
    public long worldTime;

    /**
     * The red spawn
     */
    public int redSpawnX;
    public int redSpawnY = 80;
    public int redSpawnZ;

    /**
     * The blue spawn
     */
    public int blueSpawnX;
    public int blueSpawnY = 80;
    public int blueSpawnZ;

    /**
     * The red flag spawn
     */
    public int redFlagX;
    public int redFlagY = 80;
    public int redFlagZ;

    /**
     * The blue flag spawn
     */
    public int blueFlagX;
    public int blueFlagY = 80;
    public int blueFlagZ;

    /**
     * The lobby spawn
     */
    public int lobbySpawnX;
    public int lobbySpawnY = 80;
    public int lobbySpawnZ;

    /**
     * The shop spawn
     */
    public int shopSpawnX;
    public int shopSpawnY = 80;
    public int shopSpawnZ;

    /**
     * Used to know when need to reset a dropped flag
     */
    public int redFlagTimeInAir = -1;
    public int blueFlagTimeInAir = -1;

    /**
     * Is red or blue flag stolen
     */
    public boolean redFlagStolen;
    public boolean blueFlagStolen;

    /**
     * Flags as dropped items ; used when a flag carrier die
     */
    public Item droppedRedFlag;
    public Item droppedBlueFlag;

    /**
     * Flags as dropped items ; used when flags are at homes
     */
    public Item homeRedFlag;
    public Item homeBlueFlag;

    /**
     * An instance of the plugin main class
     */
    public YuriCTF thePlugin;

    /**
     * The name of this current CTF map
     */
    public String mapName;

    /**
     * Tile entities map (these tile entities are used to add informations to spesific blocks)
     */
    public Map<Location, TileEntity> tileEntityMap;
    public List<Location> tileEntitiesMarkedForDeletion;

    /**
     * Map extentions
     */
    public CTFMapTimer mapTimer;
    public CTFMapScoreBoard mapScoreBoard;
    public CTFEditingMap mapEditMode;
    public CTFMapChat mapChatSystem;

    public CTFMap(YuriCTF plugin) {
        thePlugin = plugin;
        tileEntityMap = new HashMap<Location, TileEntity>();
        mapEditMode = new CTFEditingMap(plugin);
        mapTimer = new CTFMapTimer(plugin, mapEditMode.isMapInEditMode());
        mapScoreBoard = new CTFMapScoreBoard(plugin);
        String[] strings = new String[4];
        strings[0] = plugin.getConfig().getString("messages.a");
        strings[1] = plugin.getConfig().getString("messages.b");
        strings[2] = plugin.getConfig().getString("messages.c");
        strings[3] = plugin.getConfig().getString("messages.d");
        mapChatSystem = new CTFMapChat(plugin, strings);
        tileEntitiesMarkedForDeletion = new ArrayList<Location>();
    }

    public void createTileEntity(CTFPlayer thePlayer, String tileEntityArgs, World world, String className, Location loc, boolean save) {
        try {
            Class<? super TileEntity> theClass = (Class<? super TileEntity>) Class.forName("fr.yurictf.map.tileentity." + className);
            if (theClass != null) {
                Constructor<? super TileEntity> constructor = theClass.getDeclaredConstructor(YuriCTF.class, String.class);
                TileEntity tileEntity = (TileEntity) constructor.newInstance(thePlugin, tileEntityArgs);
                placeBlockTileEntity(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), world, tileEntity);

                if (save) {
                    FileConfiguration file = new YamlConfiguration();
                    File f = new File(thePlugin.getDataFolder() + File.separator + "mapData" + File.separator);
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    File f1 = new File(f + File.separator + thePlayer.associatedPlayer.getWorld().getName() + ".yml");
                    if (!f1.exists()) {
                        f1.createNewFile();
                    }
                    file.load(f1);
                    int num = file.getInt("tileEntityNumber");
                    num++;
                    String name = "entity_" + num;
                    file.set(name + ".x", loc.getBlockX());
                    file.set(name + ".y", loc.getBlockY());
                    file.set(name + ".z", loc.getBlockZ());
                    file.set(name + ".tileEntityName", className);
                    file.set(name + ".tileEntityArgs", tileEntityArgs);
                    file.set("tileEntityNumber", num);
                    file.save(f + File.separator + thePlayer.associatedPlayer.getWorld().getName() + ".yml");
                }
            } else {
                if (thePlayer != null) {
                    thePlayer.associatedPlayer.sendMessage("Unable to find TileEntity class : \"" + "fr.yurictf.map.tileentity." + className + "\" : CLASS_SUPER_IMPLEMENTATION_ERROR");
                } else {
                    thePlugin.getLogger().warning("Unable to find TileEntity class : \"" + "fr.yurictf.map.tileentity." + className + "\" : CLASS_SUPER_IMPLEMENTATION_ERROR");
                }
            }
        } catch (ClassNotFoundException e) {
            if (thePlayer != null) {
                thePlayer.associatedPlayer.sendMessage("Unable to find TileEntity class : \"" + "fr.yurictf.map.tileentity." + className + "\" : CLASS_NOT_EXISTING");
                thePlayer.associatedPlayer.sendMessage("Trying now searching on Extentions TileEntity Registry");
            } else {
                thePlugin.getLogger().warning("Unable to find TileEntity class : \"" + "fr.yurictf.map.tileentity." + className + "\" : CLASS_NOT_EXISTING");
                thePlugin.getLogger().warning("Trying now searching on Extentions TileEntity Registry");
            }
            try {
                Class<? super TileEntity> theClass = (Class<? super TileEntity>) TileEntityAPIHandler.findTileEntityFromExtentions(className);
                if (theClass != null) {
                    Constructor<? super TileEntity> constructor = theClass.getDeclaredConstructor(YuriCTF.class, String.class);
                    TileEntity tileEntity = (TileEntity) constructor.newInstance(thePlugin, tileEntityArgs);
                    placeBlockTileEntity(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), world, tileEntity);

                    if (save) {
                        FileConfiguration file = new YamlConfiguration();
                        File f = new File(thePlugin.getDataFolder() + File.separator + "mapData" + File.separator);
                        if (!f.exists()) {
                            f.mkdirs();
                        }
                        File f1 = new File(f + File.separator + thePlayer.associatedPlayer.getWorld().getName() + ".yml");
                        if (!f1.exists()) {
                            f1.createNewFile();
                        }
                        file.load(f1);
                        int num = file.getInt("tileEntityNumber");
                        num++;
                        String name = "entity_" + num;
                        file.set(name + ".x", loc.getBlockX());
                        file.set(name + ".y", loc.getBlockY());
                        file.set(name + ".z", loc.getBlockZ());
                        file.set(name + ".tileEntityName", className);
                        file.set(name + ".tileEntityArgs", tileEntityArgs);
                        file.set("tileEntityNumber", num);
                        file.save(f + File.separator + thePlayer.associatedPlayer.getWorld().getName() + ".yml");
                    }
                } else {
                    if (thePlayer != null) {
                        thePlayer.associatedPlayer.sendMessage("Unable to find TileEntity class (in Extentions Registry) : \"" + className + "\".");
                    } else {
                        thePlugin.getLogger().warning("Unable to find TileEntity class (in Extentions Registry) : \"" + className + "\".");
                    }
                }
            } catch(Exception e1){
                if (thePlayer != null) {
                    thePlayer.associatedPlayer.sendMessage("Impossible to find or load TileEntity with name : " + className);
                } else {
                    thePlugin.getLogger().warning("Impossible to find or load TileEntity with name : " + className);
                }
            }
        } catch (NoSuchMethodException e) {
            if (thePlayer != null) {
                thePlayer.associatedPlayer.sendMessage("Unable to find TileEntity class : \"" + "fr.yurictf.map.tileentity." + className + "\" : CLASS_CONSTRUCTOR_NOT_EXISTING");
            } else {
                thePlugin.getLogger().warning("Unable to find TileEntity class : \"" + "fr.yurictf.map.tileentity." + className + "\" : CLASS_CONSTRUCTOR_NOT_EXISTING");
            }
        } catch (InvocationTargetException e) {
            if (thePlayer != null) {
                thePlayer.associatedPlayer.sendMessage("Unable to load TileEntity class : \"" + "fr.yurictf.map.tileentity." + className + "\" : CONSTRUCTOR_INVOCATION_TARGET_ERROR");
            } else {
                thePlugin.getLogger().warning("Unable to load TileEntity class : \"" + "fr.yurictf.map.tileentity." + className + "\" : CONSTRUCTOR_INVOCATION_TARGET_ERROR");
            }
        } catch (InstantiationException e) {
            if (thePlayer != null) {
                thePlayer.associatedPlayer.sendMessage("Unable to load TileEntity class : \"" + "fr.yurictf.map.tileentity." + className + "\" : CONSTRUCTOR_INSTANTIATION_ERROR");
            } else {
                thePlugin.getLogger().warning("Unable to load TileEntity class : \"" + "fr.yurictf.map.tileentity." + className + "\" : CONSTRUCTOR_INSTANTIATION_ERROR");
            }
        } catch (IllegalAccessException e) {
            if (thePlayer != null) {
                thePlayer.associatedPlayer.sendMessage("Unable to load TileEntity class : \"" + "fr.yurictf.map.tileentity." + className + "\" : CONSTRUCTOR_INVALID_ACCESS_ERROR");
            } else {
                thePlugin.getLogger().warning("Unable to load TileEntity class : \"" + "fr.yurictf.map.tileentity." + className + "\" : CONSTRUCTOR_INVALID_ACCESS_ERROR");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeSavedTileEntity(CTFPlayer player, FileConfiguration file, String worldName, Location loc) {
        int num = file.getInt("tileEntityNumber");
        for (int i = 1; i <= num; i++) {
            String name = "entity_" + i;
            int x = file.getInt(name + ".x");
            int y = file.getInt(name + ".y");
            int z = file.getInt(name + ".z");
            Location configLoc = new Location(thePlugin.getServer().getWorld(worldName), x, y, z);
            if (configLoc.equals(loc)) {
                removeBlockTileEntity(configLoc.getBlockX(), configLoc.getBlockY(), configLoc.getBlockZ(), thePlugin.getServer().getWorld(worldName), false);
                file.set(name, null);
                num--;
                file.set("tileEntityNumber", num);
                correctYamlFile(file);
                return;
            }
        }
        player.associatedPlayer.sendMessage(ChatColor.RED + "TileEntity has not been found");
    }

    public net.minecraft.server.v1_7_R4.Entity spawnCustomEntity(CustomEntityType type, boolean inLobby, Location loc){
        if (inLobby) {
            net.minecraft.server.v1_7_R4.World world = ((CraftWorld) waitingWorld).getHandle();
            try {
                net.minecraft.server.v1_7_R4.Entity e = type.getCustomClass().getConstructor(net.minecraft.server.v1_7_R4.World.class).newInstance(world);
                e.setPosition(loc.getX(), loc.getY(), loc.getZ());
                if (world.addEntity(e)){
                    return e;
                }
            } catch (InstantiationException e) {
                thePlugin.getLogger().warning("Unable to spawn custom entity !");
            } catch (IllegalAccessException e) {
                thePlugin.getLogger().warning("Unable to spawn custom entity !");
            } catch (NoSuchMethodException e) {
                thePlugin.getLogger().warning("Unable to spawn custom entity !");
            } catch (InvocationTargetException e) {
                thePlugin.getLogger().warning("Unable to spawn custom entity !");
            }
        } else {
            net.minecraft.server.v1_7_R4.World world = ((CraftWorld) this.world).getHandle();
            try {
                net.minecraft.server.v1_7_R4.Entity e = type.getCustomClass().getConstructor(net.minecraft.server.v1_7_R4.World.class).newInstance(world);
                e.setPosition(loc.getX(), loc.getY(), loc.getZ());
                if (world.addEntity(e)) {
                    return e;
                }
            } catch (InstantiationException e) {
                thePlugin.getLogger().warning("Unable to spawn custom entity !");
            } catch (IllegalAccessException e) {
                thePlugin.getLogger().warning("Unable to spawn custom entity !");
            } catch (NoSuchMethodException e) {
                thePlugin.getLogger().warning("Unable to spawn custom entity !");
            } catch (InvocationTargetException e) {
                thePlugin.getLogger().warning("Unable to spawn custom entity !");
            }
        }
        return null;
    }

    public void unspawnCustomEntity(net.minecraft.server.v1_7_R4.Entity entity, boolean inLobby){
        if (inLobby) {
            net.minecraft.server.v1_7_R4.World world = ((CraftWorld) waitingWorld).getHandle();
            world.removeEntity(entity);
        } else {
            net.minecraft.server.v1_7_R4.World world = ((CraftWorld) this.world).getHandle();
            world.removeEntity(entity);
        }
    }

    private void correctYamlFile(FileConfiguration file) {
        String[] sections = file.getConfigurationSection("").getKeys(false).toArray(new String[0]);
        for(String s : sections) {
            file.set(s, null);
        }

        int i = 0;
        for (Map.Entry entry : tileEntityMap.entrySet()){
            i++;
            Location loc = (Location) entry.getKey();
            TileEntity entity = (TileEntity) entry.getValue();

            String name = "entity_" + i;
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            String className = entity.getClass().getSimpleName();
            String args = entity.getArguments();
            file.set(name + ".x", x);
            file.set(name + ".y", y);
            file.set(name + ".z", z);
            file.set(name + ".tileEntityName", className);
            file.set(name + ".tileEntityArgs", args);
        }
        file.set("tileEntityNumber", i);
    }

    public void loadSavedTileEntitys(FileConfiguration file, String worldName){
        int num = file.getInt("tileEntityNumber");
        for (int i = 1 ; i <= num ; i++){
            String name = "entity_" + i;
            int x = file.getInt(name + ".x");
            int y = file.getInt(name + ".y");
            int z = file.getInt(name + ".z");
            String className = file.getString(name + ".tileEntityName");
            String args = file.getString(name + ".tileEntityArgs");

            thePlugin.getLogger().info("A saved TileEntity has been found : " + className + "(" + x + ", " + y + ", " + z + ")");

            createTileEntity(null, args, thePlugin.getServer().getWorld(worldName), className, new Location(thePlugin.getServer().getWorld(worldName), x, y, z), false);
        }
    }

    public void correctEnvironment(){
        thePlugin = null;
        tileEntityMap = null;
        mapEditMode = null;
        mapTimer = null;
        mapScoreBoard = null;
        mapChatSystem = null;
        mapName = null;
        tileEntitiesMarkedForDeletion = null;
        worldTime = 0;
    }

    /** Resets the blue flag */
    public void resetBlueFlag(){
        droppedBlueFlag = null;
        blueFlagTimeInAir = -1;

        homeBlueFlag = world.dropItem(new Location(world, blueFlagX + 0.5, blueFlagY + 2, blueFlagZ + 0.5), new ItemStack(Material.WOOL, 1, (short)11));
        homeBlueFlag.setPickupDelay(5);

        thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(thePlugin, new Runnable() {
            public void run() {
                homeBlueFlag.teleport(new Location(world, blueFlagX + 0.5, blueFlagY + 2, blueFlagZ + 0.5));
            }
        }, 20L);

        thePlugin.getLogger().info("Blue flag reset : " + homeBlueFlag.getItemStack());

        Map<String, Object[]> map = EventSystem.callEvent(new EventFlagReset(null, thePlugin));
        String var = null;
        for (Map.Entry e : map.entrySet()){
            Object[] obj = (Object[]) e.getValue();
            var = (String) obj[0];
        }
        if (var == null) {
            thePlugin.getServer().broadcastMessage(ChatColor.AQUA + "Blue flag has been reset !");
        } else {
            thePlugin.getServer().broadcastMessage(var);
        }
    }

    /** Resets the red flag */
    public void resetRedFlag(){
        droppedRedFlag = null;
        redFlagTimeInAir = -1;

        homeRedFlag = world.dropItem(new Location(world, redFlagX + 0.5, redFlagY + 2, redFlagZ + 0.5), new ItemStack(Material.WOOL, 1, (short)14));
        homeRedFlag.setPickupDelay(5);

        thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(thePlugin, new Runnable() {
            public void run() {
                homeRedFlag.teleport(new Location(world, redFlagX + 0.5, redFlagY + 2, redFlagZ + 0.5));
            }
        }, 20L);

        thePlugin.getLogger().info("Red flag reset : " + homeRedFlag.getItemStack());

        Map<String, Object[]> map = EventSystem.callEvent(new EventFlagReset(null, thePlugin));
        String var = null;
        for (Map.Entry e : map.entrySet()){
            Object[] obj = (Object[]) e.getValue();
            var = (String) obj[0];
        }
        if (var == null) {
            thePlugin.getServer().broadcastMessage(ChatColor.AQUA + "Red flag has been reset !");
        } else {
            thePlugin.getServer().broadcastMessage(var);
        }
    }

    /**
     * Adds a TileEntity to the map and let this tileentity generate the block
     */
    public void placeBlockTileEntity(int x, int y, int z, World world, TileEntity assinedTileEntity){
        if (world == null){
            world = this.world;
        }
        Location loc = new Location(world, x, y, z);
        assinedTileEntity.onBlockPlaced(thePlugin, this, loc);
        tileEntityMap.put(loc, assinedTileEntity);
    }

    /**
     * Removes a TileEntity to the map and let this tileentity delete the block
     */
    public void removeBlockTileEntity(int x, int y, int z, World world, boolean destroyBlock){
        if (world == null){
            world = this.world;
        }
        Location blockLocation = new Location(world, x, y, z);
        TileEntity tileEntity = tileEntityMap.get(blockLocation);
        if (tileEntity == null){
            return;
        }
        if (destroyBlock) {
            tileEntity.onBlockDestroy(thePlugin, this, blockLocation);
        }
        tileEntitiesMarkedForDeletion.add(blockLocation);
    }

    /** Update the map and update map extentions */
    public void updateMap(){
        if (world != null){
            List<Item> blueFlags = new ArrayList<Item>();
            List<Item> redFlags = new ArrayList<Item>();
            for(Entity e : world.getEntities()){
                if (e instanceof Item){
                    Item item = (Item) e;
                    if (item.getItemStack().getType() == Material.WOOL){
                        if (item.getItemStack().getData().getData() == 11){
                            blueFlags.add(item);
                        } else if (item.getItemStack().getData().getData() == 14){
                            redFlags.add(item);
                        }
                    }
                }
            }
            if (blueFlags.size() > 1 || redFlags.size() > 1){
                thePlugin.getLogger().info("BLUE_FLAG_FAIL_NUMBER=" + blueFlags.size());
                thePlugin.getLogger().info("RED_FLAG_FAIL_NUMBER=" + redFlags.size());
                blueFlags.clear();
                redFlags.clear();
                for (Entity e : world.getEntities()){
                    if (e instanceof Item){
                        e.remove();
                    }
                }
                world.getEntities().clear();
            }
        }
        if (world != null){
            if (homeBlueFlag != null){
                if (homeBlueFlag.isDead()){
                    homeBlueFlag.remove();
                    //Respawn blue flag
                    homeBlueFlag = world.dropItem(new Location(world, blueFlagX + 0.5, blueFlagY + 2, blueFlagZ + 0.5), new ItemStack(Material.WOOL, 1, (short)11));
                    homeBlueFlag.setPickupDelay(5);

                    thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(thePlugin, new Runnable() {
                        public void run() {
                            homeBlueFlag.teleport(new Location(world, blueFlagX + 0.5, blueFlagY + 2, blueFlagZ + 0.5));
                        }
                    }, 20L);
                    //END
                }
            }
            if (homeRedFlag != null){
                if (homeRedFlag.isDead()){
                    homeRedFlag.remove();
                    //Respawn red flag
                    homeRedFlag = world.dropItem(new Location(world, redFlagX + 0.5, redFlagY + 2, redFlagZ + 0.5), new ItemStack(Material.WOOL, 1, (short)14));
                    homeRedFlag.setPickupDelay(5);

                    thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(thePlugin, new Runnable() {
                        public void run() {
                            homeRedFlag.teleport(new Location(world, redFlagX + 0.5, redFlagY + 2, redFlagZ + 0.5));
                        }
                    }, 20L);
                    //END
                }
            }
            if (redFlagTimeInAir != -1){
                redFlagTimeInAir--;
                if (redFlagTimeInAir <= 0){
                    droppedRedFlag.remove();
                    redFlagStolen = false;
                    resetRedFlag();
                    redFlagTimeInAir = -1;
                }
            }

            if (blueFlagTimeInAir != -1){
                blueFlagTimeInAir--;
                if (blueFlagTimeInAir <= 0){
                    droppedBlueFlag.remove();
                    blueFlagStolen = false;
                    resetBlueFlag();
                    blueFlagTimeInAir = -1;
                }
            }
        }

        if (world != null){
            long l = world.getTime();
            if (l != worldTime){
                world.setTime(worldTime);
            }
        }

        if (mapTimer != null && mapTimer.isFinished){
            long l1 = waitingWorld.getTime();
            if (l1 != worldTime){
                waitingWorld.setTime(worldTime);
            }
        }

        if (mapTimer != null){
            mapTimer.updateTimer();
        }
        if (mapScoreBoard != null) {
            mapScoreBoard.updateScoreBoard();
        }
        if (mapChatSystem != null) {
            mapChatSystem.updateChat();
        }

        if (tileEntityMap != null || tileEntitiesMarkedForDeletion != null) {
            for (Object o : tileEntityMap.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                TileEntity entity = (TileEntity) entry.getValue();
                entity.onUpdate(thePlugin, this);
            }
            for (Location location : tileEntitiesMarkedForDeletion) {
                tileEntityMap.remove(location);
            }
            tileEntitiesMarkedForDeletion.clear();
        }
    }
}
