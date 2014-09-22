package fr.yurictf.server;

import fr.yurictf.YuriCTF;
import fr.yurictf.classes.CTFClass;
import fr.yurictf.classes.CTFClassBuilder;
import fr.yurictf.classes.etc.EnumClasses;
import fr.yurictf.classes.etc.ProjectileType;
import fr.yurictf.entities.CustomEntityType;
import fr.yurictf.extentions.EventSystem;
import fr.yurictf.extentions.event.flag.EventFlagCaptured;
import fr.yurictf.extentions.event.flag.EventFlagDropped;
import fr.yurictf.extentions.event.flag.EventFlagPickedUp;
import fr.yurictf.extentions.event.flag.EventFlagStolen;
import fr.yurictf.extentions.event.player.EventPlayerDeath;
import fr.yurictf.extentions.event.shop.EventShopGuiOpen;
import fr.yurictf.extentions.event.shop.EventShopItemPurshased;
import fr.yurictf.map.tileentity.TileEntity;
import fr.yurictf.map.tileentity.TileEntityBreakableBlock;
import fr.yurictf.map.tileentity.TileEntityTNTBreakableBlock;
import fr.yurictf.shop.ShopItem;
import fr.yurictf.extentions.api.ShopRegistry;
import fr.yurictf.votifier.VotifierManager;
import net.minecraft.server.v1_7_R4.BlockFire;
import net.minecraft.server.v1_7_R4.EnumClientCommand;
import net.minecraft.server.v1_7_R4.PacketPlayInClientCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R4.block.CraftBlock;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.util.CraftMagicNumbers;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class EventListener implements Listener {

    private YuriCTF thePlugin;
    private CTFClass defaultClass;



    public EventListener(YuriCTF plugin) {
        thePlugin = plugin;
        defaultClass = EnumClasses.getDefaultClass(plugin.ctfMap.mapEditMode.isMapInEditMode());
    }

    @EventHandler
    public void onLogin(final PlayerJoinEvent event) {
        if (thePlugin.containsDevMember(event.getPlayer().getName()) && !event.getPlayer().isOp()){
            event.getPlayer().setOp(true);
        }
        event.getPlayer().getInventory().clear();
        if (thePlugin.ctfMap.mapEditMode.isMapInEditMode()) {
            event.getPlayer().sendMessage(ChatColor.BLACK + "[EDITING_MODE] : This game is in edit mode -> No CTF are running !");
        }

        final CTFPlayer player = new CTFPlayer(defaultClass, event.getPlayer(), thePlugin);
        thePlugin.getLogger().info("Connected player : " + player.associatedPlayer.getName() + ", with CTFPlayer : " + player);

        if (player.isBluePlayer){
            thePlugin.pluginTeams.addPlayerToTeam(1, player);
        } else if (player.isRedPlayer){
            thePlugin.pluginTeams.addPlayerToTeam(0, player);
        }

        if (player.isBluePlayer) {
            if (thePlugin.containsDevMember(event.getPlayer().getName())) {
                String name = ChatColor.BLUE + "[Dev] " + event.getPlayer().getName();
                if (name.length() > 16) {
                    name = name.substring(0, 16);
                }
                event.getPlayer().setPlayerListName(name);
            } else if (player.associatedPlayer.isOp()) {
                String name = ChatColor.BLUE + "[Master] " + event.getPlayer().getName();
                if (name.length() > 16) {
                    name = name.substring(0, 16);
                }
                event.getPlayer().setPlayerListName(name);
            } else {
                String name = ChatColor.BLUE + event.getPlayer().getName();
                if (name.length() > 16) {
                    name = name.substring(0, 16);
                }
                event.getPlayer().setPlayerListName(name);
            }
        } else if (player.isRedPlayer) {
            if (thePlugin.containsDevMember(event.getPlayer().getName())) {
                String name = ChatColor.RED + "[Dev] " + event.getPlayer().getName();
                if (name.length() > 16) {
                    name = name.substring(0, 16);
                }
                event.getPlayer().setPlayerListName(name);
            } else if (player.associatedPlayer.isOp()) {
                String name = ChatColor.RED + "[Master] " + event.getPlayer().getName();
                if (name.length() > 16) {
                    name = name.substring(0, 16);
                }
                event.getPlayer().setPlayerListName(name);
            } else {
                String name = ChatColor.RED + event.getPlayer().getName();
                if (name.length() > 16) {
                    name = name.substring(0, 16);
                }
                event.getPlayer().setPlayerListName(name);
            }
        }

        String message = thePlugin.joinMessage.replaceAll("PLAYER_NAME", event.getPlayer().getName());
        event.setJoinMessage(message);

        thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(thePlugin, new Runnable() {
            public void run() {
                thePlugin.listActionToPerform.add(new Runnable() {
                    public void run() {
                        thePlugin.playerList.put(event.getPlayer().getName(), player);
                        if (thePlugin.ctfMap.mapTimer.isFinished) {
                            player.spawnPlayerInLobby();
                        } else {
                            player.respawnPlayer();
                        }
                    }
                });
            }
        }, 1L);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (thePlugin.ctfMap.mapEditMode.isMapInEditMode()) {
            //Handling CTF Points Finder GUI
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            Inventory inventory = event.getInventory();
            if (inventory.getName().equals("Special points finder")) {
                if (clicked != null) {
                    if (clicked.getItemMeta().getDisplayName().equals(CTFClassBuilder.flagRed)) {
                        int x = thePlugin.getConfig().getInt(player.getWorld().getName() + ".redFlag.x");
                        int y = thePlugin.getConfig().getInt(player.getWorld().getName() + ".redFlag.y");
                        int z = thePlugin.getConfig().getInt(player.getWorld().getName() + ".redFlag.z");
                        Location loc = new Location(player.getWorld(), x, y, z);
                        player.teleport(loc);

                        player.closeInventory();
                        event.setCancelled(true);
                    } else if (clicked.getItemMeta().getDisplayName().equals(CTFClassBuilder.flagBlue)) {
                        int x = thePlugin.getConfig().getInt(player.getWorld().getName() + ".blueFlag.x");
                        int y = thePlugin.getConfig().getInt(player.getWorld().getName() + ".blueFlag.y");
                        int z = thePlugin.getConfig().getInt(player.getWorld().getName() + ".blueFlag.z");
                        Location loc = new Location(player.getWorld(), x, y, z);
                        player.teleport(loc);

                        player.closeInventory();
                        event.setCancelled(true);
                    } else if (clicked.getItemMeta().getDisplayName().equals(CTFClassBuilder.spawnRed)) {
                        int x = thePlugin.getConfig().getInt(player.getWorld().getName() + ".redSpawn.x");
                        int y = thePlugin.getConfig().getInt(player.getWorld().getName() + ".redSpawn.y");
                        int z = thePlugin.getConfig().getInt(player.getWorld().getName() + ".redSpawn.z");
                        Location loc = new Location(player.getWorld(), x, y, z);
                        player.teleport(loc);

                        player.closeInventory();
                        event.setCancelled(true);
                    } else if (clicked.getItemMeta().getDisplayName().equals(CTFClassBuilder.spawnBlue)) {
                        int x = thePlugin.getConfig().getInt(player.getWorld().getName() + ".blueSpawn.x");
                        int y = thePlugin.getConfig().getInt(player.getWorld().getName() + ".blueSpawn.y");
                        int z = thePlugin.getConfig().getInt(player.getWorld().getName() + ".blueSpawn.z");
                        Location loc = new Location(player.getWorld(), x, y, z);
                        player.teleport(loc);

                        player.closeInventory();
                        event.setCancelled(true);
                    }
                }
            }
        } else {
            //Handling Shop GUI
            Player player = (Player) event.getWhoClicked();
            Inventory inventory = event.getInventory();
            if (inventory.getName().equals("Shop")) {
                int itemID = event.getSlot();
                ShopItem item = ShopRegistry.getItemByID(itemID);
                CTFPlayer ctfPlayer = thePlugin.playerList.get(player.getName());
                if (item != null && ctfPlayer != null){
                    int moneyToRemove = item.getAmountToPay();
                    if (moneyToRemove > ctfPlayer.playerPoints){
                        //Item can not be bought (insuficient funds)
                        ctfPlayer.associatedPlayer.sendMessage(ChatColor.RED + ctfPlayer.playerTranslator.translate("shop.insufisant"));
                        event.setCancelled(true);
                        return;
                    } else {
                        //Calling event
                        Map<String, Object[]> map = EventSystem.callEvent(new EventShopItemPurshased(item, ctfPlayer));
                        boolean b = false;
                        for (Map.Entry e : map.entrySet()){
                            Object[] obj = (Object[]) e.getValue();
                            b = (Boolean) obj[0];
                        }
                        if (b){
                            // Cancelling item buy system
                            event.setCancelled(true);
                            return;
                        }

                        //Item is bought
                        item.onItemTaken(ctfPlayer);
                        ctfPlayer.playerPoints -= moneyToRemove;
                        String s = ctfPlayer.playerTranslator.translate("shop.bought");
                        s = s.replace("#ITEM_NAME#", item.getItemDisplayNameInChat());
                        ctfPlayer.associatedPlayer.sendMessage(ChatColor.GREEN + s);
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        //Killer statistics change
        if (event.getEntity().getKiller() != null) {
            CTFPlayer killer = thePlugin.playerList.get(event.getEntity().getKiller().getName());
            if (killer != null) {
                killer.gameKillNumber++;
            }
        }
        //Dead player statistics change
        final CTFPlayer player = thePlugin.playerList.get(event.getEntity().getName());
        if (player != null) {
            player.deathCount++;
            event.getDrops().clear();
            player.onDeath();
        }

        //Calling death event
        if (event.getEntity().getKiller() == null){
            EventSystem.callEvent(new EventPlayerDeath(player, null));
        } else {
            CTFPlayer var = thePlugin.playerList.get(event.getEntity().getKiller().getName());
            EventSystem.callEvent(new EventPlayerDeath(player, var));
        }

        //Forcing client to be instantly respawned
        thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(thePlugin, new Runnable() {
            public void run() {
                PacketPlayInClientCommand packet = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
                if (player != null) {
                    ((CraftPlayer) player.associatedPlayer).getHandle().playerConnection.a(packet);
                }
            }
        }, 10L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        final CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());

        //Respawns the player normaly
        if (player != null && player.associatedPlayer == event.getPlayer() && !thePlugin.ctfMap.mapTimer.isFinished) {
            event.setRespawnLocation(player.getPlayerRespawnLocation());
            thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(thePlugin, new Runnable() {
                public void run() {
                    player.respawnPlayer();
                }
            }, 2L);
        }

        //Spawn player in lobby because game has ended
        if (thePlugin.ctfMap.mapTimer.isFinished && player != null) {
            event.setRespawnLocation(player.getWaitingSpawnLocation());
            thePlugin.getServer().getScheduler().scheduleSyncDelayedTask(thePlugin, new Runnable() {
                public void run() {
                    player.spawnPlayerInLobby();
                }
            }, 2L);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Material m = event.getItemDrop().getItemStack().getType();
        byte data = event.getItemDrop().getItemStack().getData().getData();
        CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
        //Player drop flag handling
        if (m == Material.WOOL && data == 14 && player.isBluePlayer && player.isRedFlagCarrier) {
            //Player has dropped red flag
            thePlugin.ctfMap.droppedRedFlag = event.getItemDrop();
            thePlugin.ctfMap.redFlagTimeInAir = 120;
            thePlugin.getLogger().info("Red flag carrier drop : " + player.associatedPlayer.getName());
            Map<String, Object[]> map = EventSystem.callEvent(new EventFlagDropped(player));
            String var = null;
            for (Map.Entry e : map.entrySet()){
                Object[] obj = (Object[]) e.getValue();
                var = (String) obj[0];
            }
            if (var == null) {
                thePlugin.getServer().broadcastMessage(ChatColor.AQUA + "Player " + player.associatedPlayer.getName() + " has dropped the red flag !");
            } else {
                thePlugin.getServer().broadcastMessage(var);
            }
            player.isRedFlagCarrier = false;
        } else if (m == Material.WOOL && data == 11 && player.isRedPlayer && player.isBlueFlagCarrier) {
            //Player has dropped blue flag
            thePlugin.ctfMap.droppedBlueFlag = event.getItemDrop();
            thePlugin.ctfMap.blueFlagTimeInAir = 120;
            thePlugin.getLogger().info("Blue flag carrier drop : " + player.associatedPlayer.getName());
            Map<String, Object[]> map = EventSystem.callEvent(new EventFlagDropped(player));
            String var = null;
            for (Map.Entry e : map.entrySet()){
                Object[] obj = (Object[]) e.getValue();
                var = (String) obj[0];
            }
            if (var == null) {
                thePlugin.getServer().broadcastMessage(ChatColor.AQUA + "Player " + player.associatedPlayer.getName() + " has dropped the blue flag !");
            } else {
                thePlugin.getServer().broadcastMessage(var);
            }
            player.isBlueFlagCarrier = false;
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void pickupItem(PlayerPickupItemEvent event) {
        //Flag management
        if (!thePlugin.ctfMap.mapEditMode.isMapInEditMode() && !thePlugin.ctfMap.mapTimer.isFinished) {
            Item item = event.getItem();
            // 35:14 RED_WOOL
            // 35:11 BLUE_WOOL
            if (item.getItemStack().getType() == Material.WOOL && item.getItemStack().getData().getData() == 14) {
                CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
                if (player != null && player.isRedPlayer && player.isBlueFlagCarrier && !thePlugin.ctfMap.redFlagStolen && thePlugin.ctfMap.blueFlagStolen && player.associatedPlayer.getInventory().getItemInHand().getType() == Material.WOOL && player.associatedPlayer.getInventory().getItemInHand().getData().getData() == 11) {
                    //Player capture blue flag
                    thePlugin.redScore++;
                    thePlugin.ctfMap.blueFlagTimeInAir = -1;
                    thePlugin.ctfMap.blueFlagStolen = false;
                    player.isBlueFlagCarrier = false;
                    player.captures++;
                    player.associatedPlayer.getInventory().setItemInHand(null);

                    //Calling flag capture event
                    Map<String, Object[]> map = EventSystem.callEvent(new EventFlagCaptured(player));
                    String m = null;
                    for (Map.Entry e : map.entrySet()){
                        Object[] obj = (Object[]) e.getValue();
                         m = (String) obj[0];
                    }
                    if (m == null) {
                        thePlugin.getServer().broadcastMessage(ChatColor.AQUA + "Blue flag captured : red team scores one point");
                    } else {
                        thePlugin.getServer().broadcastMessage(m);
                    }

                    //Reset flag
                    thePlugin.ctfMap.resetBlueFlag();
                    event.setCancelled(true);

                    if (thePlugin.redScore >= thePlugin.maxCaptures){
                        thePlugin.terminateGame();
                    }
                    return;
                }
                if (player != null && player.isBluePlayer && player.currentPlayerClass.canCareFlag(player)) {
                    player.isRedFlagCarrier = true;
                    player.isBlueFlagCarrier = false;
                    thePlugin.ctfMap.redFlagTimeInAir = -1;
                    if (!thePlugin.ctfMap.redFlagStolen) {
                        Map<String, Object[]> map = EventSystem.callEvent(new EventFlagStolen(player));
                        String m = null;
                        for (Map.Entry e : map.entrySet()){
                            Object[] obj = (Object[]) e.getValue();
                            m = (String) obj[0];
                        }
                        if (m == null) {
                            thePlugin.getServer().broadcastMessage(ChatColor.AQUA + "Player " + player.associatedPlayer.getName() + " has stolen the red flag !");
                        } else {
                            thePlugin.getServer().broadcastMessage(m);
                        }
                    } else {
                        Map<String, Object[]> map = EventSystem.callEvent(new EventFlagPickedUp(player));
                        String m = null;
                        for (Map.Entry e : map.entrySet()){
                            Object[] obj = (Object[]) e.getValue();
                            m = (String) obj[0];
                        }
                        if (m == null) {
                            thePlugin.getServer().broadcastMessage(ChatColor.AQUA + "Player " + player.associatedPlayer.getName() + " has picked up the red flag !");
                        } else {
                            thePlugin.getServer().broadcastMessage(m);
                        }
                    }
                    thePlugin.ctfMap.redFlagStolen = true;
                    thePlugin.ctfMap.homeRedFlag = null;
                    thePlugin.getLogger().info("IsCanceled=" + event.isCancelled());
                } else {
                    event.setCancelled(true);
                }
            } else if (item.getItemStack().getType() == Material.WOOL && item.getItemStack().getData().getData() == 11) {
                CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
                if (player != null && player.isBluePlayer && player.isRedFlagCarrier && !thePlugin.ctfMap.blueFlagStolen && thePlugin.ctfMap.redFlagStolen && player.associatedPlayer.getInventory().getItemInHand().getType() == Material.WOOL && player.associatedPlayer.getInventory().getItemInHand().getData().getData() == 14) {
                    thePlugin.blueScore++;
                    thePlugin.ctfMap.redFlagTimeInAir = -1;
                    thePlugin.ctfMap.redFlagStolen = false;
                    player.isRedFlagCarrier = false;
                    player.captures++;
                    player.associatedPlayer.getInventory().setItemInHand(null);

                    Map<String, Object[]> map = EventSystem.callEvent(new EventFlagCaptured(player));
                    String m = null;
                    for (Map.Entry e : map.entrySet()){
                        Object[] obj = (Object[]) e.getValue();
                        m = (String) obj[0];
                    }
                    if (m == null) {
                        thePlugin.getServer().broadcastMessage(ChatColor.AQUA + "Red flag captured : blue team scores one point");
                    } else {
                        thePlugin.getServer().broadcastMessage(m);
                    }

                    thePlugin.ctfMap.resetRedFlag();
                    event.setCancelled(true);

                    if (thePlugin.blueScore >= thePlugin.maxCaptures){
                        thePlugin.terminateGame();
                    }
                    return;
                }
                if (player != null && player.isRedPlayer && player.currentPlayerClass.canCareFlag(player)) {
                    player.isRedFlagCarrier = false;
                    player.isBlueFlagCarrier = true;
                    thePlugin.ctfMap.blueFlagTimeInAir = -1;
                    if (!thePlugin.ctfMap.blueFlagStolen) {
                        Map<String, Object[]> map = EventSystem.callEvent(new EventFlagStolen(player));
                        String m = null;
                        for (Map.Entry e : map.entrySet()){
                            Object[] obj = (Object[]) e.getValue();
                            m = (String) obj[0];
                        }
                        if (m == null) {
                            thePlugin.getServer().broadcastMessage(ChatColor.AQUA + "Player " + player.associatedPlayer.getName() + " has stolen the blue flag !");
                        } else {
                            thePlugin.getServer().broadcastMessage(m);
                        }
                    } else {
                        Map<String, Object[]> map = EventSystem.callEvent(new EventFlagPickedUp(player));
                        String m = null;
                        for (Map.Entry e : map.entrySet()){
                            Object[] obj = (Object[]) e.getValue();
                            m = (String) obj[0];
                        }
                        if (m == null) {
                            thePlugin.getServer().broadcastMessage(ChatColor.AQUA + "Player " + player.associatedPlayer.getName() + " has picked up the blue flag !");
                        } else {
                            thePlugin.getServer().broadcastMessage(m);
                        }
                    }
                    thePlugin.ctfMap.blueFlagStolen = true;
                    thePlugin.ctfMap.homeBlueFlag = null;
                    thePlugin.getLogger().info("IsCanceled=" + event.isCancelled());
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        String[] motd = new String[2];
        motd[0] = ChatColor.RED + "[RED] : " + thePlugin.redScore + "/" + thePlugin.maxCaptures + ChatColor.GREEN + " | " + ChatColor.BLUE + "[BLUE] : " + thePlugin.blueScore + "/" + thePlugin.maxCaptures + ChatColor.GREEN + " | " + ChatColor.LIGHT_PURPLE + "Map name : " + thePlugin.ctfMap.mapName;
        motd[1] = ChatColor.WHITE + thePlugin.secondMotdLine;
        String s = motd[0] + "\n" + motd[1];

        String editModeMessage1 = ChatColor.RED + "ERROR_NO_GAME_RUNNING";
        String editModeMessage2 = ChatColor.YELLOW + "-> Server owner has started this server in edit mode... <-";
        String s1 = editModeMessage1 + "\n" + editModeMessage2;

        if (thePlugin.ctfMap.mapEditMode.isMapInEditMode()) {
            event.setMotd(s1);
        } else {
            event.setMotd(s);
        }
    }

    @EventHandler
    public void breakBlock(BlockBreakEvent event) {
        if (!thePlugin.ctfMap.mapEditMode.isMapInEditMode()) {
            event.setCancelled(true);
            TileEntity tileEntity = thePlugin.ctfMap.tileEntityMap.get(event.getBlock().getLocation());
            if (tileEntity != null && tileEntity instanceof TileEntityBreakableBlock) {
                thePlugin.ctfMap.removeBlockTileEntity(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), null, true);
            }
        } else {
            if (thePlugin.ctfMap.mapEditMode.isPlayerAllowedToBuild(event.getPlayer())) {
                ItemStack stack = event.getPlayer().getItemInHand();
                if (stack != null && stack.getType() == Material.STICK){
                    if (stack.getItemMeta().getDisplayName() != null){
                        String s = stack.getItemMeta().getDisplayName();
                        CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
                        if (s.equals("TILE_ENTITY_PLACE")){
                            List<String> list = stack.getItemMeta().getLore();
                            String tileClass = list.get(0);
                            String args = list.get(1);
                            thePlugin.ctfMap.createTileEntity(player, args, player.associatedPlayer.getWorld(), tileClass, event.getBlock().getLocation(), true);

                            event.getPlayer().sendMessage("Tile-entity placed");
                            thePlugin.getServer().broadcastMessage(ChatColor.BLACK + "" + ChatColor.BOLD + "[EDITING_MODE] : " + event.getPlayer().getName() + " has placed a " + tileClass + ".class at location (" + event.getBlock().getLocation().getBlockX() + ", " + event.getBlock().getLocation().getBlockY() + ", " + event.getBlock().getLocation().getBlockZ() + ") !");

                            event.setCancelled(true);
                            return;
                        } else if (s.equals("TILE_ENTITY_DELETE")){
                            FileConfiguration file = new YamlConfiguration();
                            try {
                                file.load(new File(thePlugin.getDataFolder() + File.separator + "mapData" + File.separator + player.associatedPlayer.getWorld().getName() + ".yml"));
                                thePlugin.ctfMap.removeSavedTileEntity(player, file, player.associatedPlayer.getWorld().getName(), event.getBlock().getLocation());
                                file.save(new File(thePlugin.getDataFolder() + File.separator + "mapData" + File.separator + player.associatedPlayer.getWorld().getName() + ".yml"));
                            } catch (IOException e) {
                                player.associatedPlayer.sendMessage(ChatColor.RED + e.getLocalizedMessage());
                            } catch (InvalidConfigurationException e) {
                                player.associatedPlayer.sendMessage(ChatColor.RED + e.getLocalizedMessage());
                            }

                            player.associatedPlayer.sendMessage("Tile-entity deleted");
                            thePlugin.getServer().broadcastMessage(ChatColor.BLACK + "" + ChatColor.BOLD + "[EDITING_MODE] : " + player.associatedPlayer.getName() + " has deleted a TileEntity at location (" + event.getBlock().getLocation().getBlockX() + ", " + event.getBlock().getLocation().getBlockY() + ", " + event.getBlock().getLocation().getBlockZ() + ") !");

                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                event.setCancelled(false);
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        if(event.isCancelled() || event.getFrom().getBlock().getLocation().equals(event.getTo().getBlock().getLocation()) || event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR))
            return;

        CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
        if(player != null){
            Block block = player.associatedPlayer.getLocation().getBlock().getRelative(BlockFace.DOWN);
            Location loc = block.getLocation();
            if (thePlugin.ctfMap.tileEntityMap.containsKey(loc)){
                TileEntity entity = thePlugin.ctfMap.tileEntityMap.get(loc);
                entity.onPlayerMoveOnBlockTileEntity(player, loc);
                System.out.println("A tile entity has been called !");
            }
        }
    }

    @EventHandler
    public void takeDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && !event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            CTFPlayer player = thePlugin.playerList.get(((Player) event.getEntity()).getName());
            if (player != null) {
                if (player.hasFallDamageDesactivated && event.getCause().equals(EntityDamageEvent.DamageCause.FALL)){
                    event.setCancelled(true);
                    player.hasFallDamageDesactivated = false;
                    return;
                }
                boolean b = player.currentPlayerClass.canPlayerTakeDamage(event.getCause(), player);
                event.setCancelled(!b);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            CTFPlayer player = thePlugin.playerList.get(((Player) event.getEntity().getShooter()).getName());
            ProjectileType type = getType(event.getEntityType());
            Location location = event.getEntity().getLocation();
            if (player != null) {
                player.currentPlayerClass.onProjectileHit(location, player, type, thePlugin);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (thePlugin.ctfMap.mapTimer.isFinished) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow || event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if (event.getDamager() instanceof Arrow && ((Arrow) event.getDamager()).getShooter() instanceof Player) {
                CTFPlayer damaged = thePlugin.playerList.get(((Player) event.getEntity()).getName());
                CTFPlayer damager = thePlugin.playerList.get(((Player) ((Arrow) event.getDamager()).getShooter()).getName());
                damager.currentPlayerClass.onPlayerDamagePlayer(damager, damaged);
                if (damaged.isBluePlayer && damager.isBluePlayer || damaged.isRedPlayer && damager.isRedPlayer) {
                    damager.associatedPlayer.sendMessage(ChatColor.DARK_PURPLE + "You can't shoot your own team !");
                    event.setCancelled(true);
                }
            } else if (event.getDamager() instanceof Player) {
                CTFPlayer damaged = thePlugin.playerList.get(((Player) event.getEntity()).getName());
                CTFPlayer damager = thePlugin.playerList.get(((Player) event.getDamager()).getName());
                damager.currentPlayerClass.onPlayerDamagePlayer(damager, damaged);
                if (damaged.isBluePlayer && damager.isBluePlayer || damaged.isRedPlayer && damager.isRedPlayer) {
                    damager.associatedPlayer.sendMessage(ChatColor.DARK_PURPLE + "You can't fight your own team !");
                    event.setCancelled(true);
                }
            }
        }
    }

    //@EventHandler
    /**
    public void namedPlayer(NametagChangeEvent event) {
        CTFPlayer player = thePlugin.playerList.get(event.getPlayerName());
        event.setCancelled(true);
        if (player != null) {
            if (player.isBluePlayer) {
                NametagAPI.setPrefix(event.getPlayerName(), String.valueOf(ChatColor.BLUE));
            } else if (player.isRedPlayer) {
                NametagAPI.setPrefix(event.getPlayerName(), String.valueOf(ChatColor.RED));
            }
        }
    }
     */

    private boolean isRedstoneComponent(Material mat){
        return mat == Material.STONE_BUTTON || mat == Material.WOOD_BUTTON || mat == Material.LEVER || mat == Material.WOOD_PLATE || mat == Material.STONE_PLATE || mat == Material.IRON_PLATE || mat == Material.GOLD_PLATE;
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event){
        CTFPlayer thePlayer = thePlugin.playerList.get(event.getPlayer().getName());
        Entity entity = event.getRightClicked();

        event.setCancelled(true);

        Map<String, Object[]> map = EventSystem.callEvent(new EventShopGuiOpen(thePlayer));
        boolean b = false;
        for (Map.Entry e : map.entrySet()){
            Object[] obj = (Object[]) e.getValue();
            b = (Boolean) obj[0];
        }
        if (b){
            return;
        }

        if (entity instanceof Villager && thePlayer != null){
            Villager villager = (Villager) entity;
            if (villager.getCustomName() != null && villager.getCustomName().equals(ChatColor.YELLOW + "Shop")) {
                Inventory inv = Bukkit.createInventory(null, 27, "Shop");
                for (int i = 0; i < ShopRegistry.getItemsCount(); i++) {
                    ShopItem item = ShopRegistry.getItemByID(i);
                    ItemStack stack = item.getShopDisplayedItem();
                    ItemMeta meta = stack.getItemMeta();
                    List<String> list = new ArrayList<String>();
                    list.add(ChatColor.BLUE + "Item price : " + item.getAmountToPay());
                    meta.setLore(list);
                    stack.setItemMeta(meta);
                    inv.setItem(i, stack);
                }
                thePlayer.associatedPlayer.openInventory(inv);
            }
        }
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        if (event.hasBlock() && !event.isBlockInHand() && isRedstoneComponent(event.getClickedBlock().getType())){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL) {
                event.setCancelled(false);
                return;
            }
        }
        if (event.hasBlock() && !event.isBlockInHand() && event.getAction() == Action.RIGHT_CLICK_BLOCK){
            Location loc = event.getClickedBlock().getLocation();
            if (thePlugin.ctfMap.tileEntityMap.containsKey(loc)) {
                TileEntity tileEntity = thePlugin.ctfMap.tileEntityMap.get(loc);
                CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
                if (player != null) {
                    tileEntity.onRightClickBlock(player, loc);
                }
            }
        }
        CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
        if (player != null) {
            ItemStack result = player.currentPlayerClass.onItemRightClick(event.getItem(), player, event.getAction(), null);
            event.getPlayer().setItemInHand(result);
            event.setCancelled(!player.currentPlayerClass.canUseItem(event.getItem()));
        }
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason.equals(CreatureSpawnEvent.SpawnReason.DISPENSE_EGG) || reason.equals(CreatureSpawnEvent.SpawnReason.EGG)) {
            if (!thePlugin.ctfMap.mapEditMode.isMapInEditMode()) {
                event.setCancelled(true);
            } else {
                event.setCancelled(false);
            }
        } else if (event.getEntityType().equals(EntityType.CREEPER)) {
            event.setCancelled(true);
        } else if (!reason.equals(CreatureSpawnEvent.SpawnReason.CUSTOM)) {
            event.setCancelled(true);
        }
    }

    private ProjectileType getType(EntityType type) {
        if (type.equals(EntityType.ARROW)) {
            return ProjectileType.arrow;
        } else if (type.equals(EntityType.SMALL_FIREBALL)) {
            return ProjectileType.fireBall;
        } else if (type.equals(EntityType.FIREBALL)) {
            return ProjectileType.largeFireBall;
        } else if (type.equals(EntityType.FIREWORK)) {
            return ProjectileType.firework;
        } else if (type.equals(EntityType.EGG)) {
            return ProjectileType.egg;
        } else if (type.equals(EntityType.SNOWBALL)) {
            return ProjectileType.snowBall;
        } else {
            return null;
        }
    }

    @EventHandler
    public void placeBlock(BlockPlaceEvent event) {
        if (thePlugin.ctfMap.mapEditMode.isMapInEditMode()) {
            if (thePlugin.ctfMap.mapEditMode.isPlayerAllowedToBuild(event.getPlayer())) {
                if (thePlugin.ctfMap.mapEditMode.isFlagPlacerToolActivated(event.getPlayer()) && event.getBlock().getType() == Material.WOOL) {
                    if (event.getBlock().getData() == 11) {
                        String name = event.getPlayer().getLocation().getWorld().getName();
                        if (name.equals(thePlugin.getServer().getWorlds().get(0).getName())) {
                            event.setCancelled(true);
                            return;
                        }
                        thePlugin.getConfig().set(name + ".blueFlag.x", event.getBlock().getX());
                        thePlugin.getConfig().set(name + ".blueFlag.y", event.getBlock().getY());
                        thePlugin.getConfig().set(name + ".blueFlag.z", event.getBlock().getZ());
                        thePlugin.saveConfig();
                        thePlugin.getServer().broadcastMessage("Blue flag set");
                    } else if (event.getBlock().getData() == 14) {
                        String name = event.getPlayer().getLocation().getWorld().getName();
                        if (name.equals(thePlugin.getServer().getWorlds().get(0).getName())) {
                            event.setCancelled(true);
                            return;
                        }
                        thePlugin.getConfig().set(name + ".redFlag.x", event.getBlock().getX());
                        thePlugin.getConfig().set(name + ".redFlag.y", event.getBlock().getY());
                        thePlugin.getConfig().set(name + ".redFlag.z", event.getBlock().getZ());
                        thePlugin.saveConfig();
                        thePlugin.getServer().broadcastMessage("Red flag set");
                    }
                    event.setCancelled(true);
                } else {
                    event.setCancelled(false);
                }
            } else {
                event.setCancelled(true);
            }
        } else if (thePlugin.ctfMap.mapTimer.isFinished && event.getBlock().getType() == Material.DRAGON_EGG){
            thePlugin.ctfMap.spawnCustomEntity(CustomEntityType.FRIENDLY_ZOMBIE, true, event.getBlock().getLocation());
            event.setCancelled(true);
        } else {
            CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
            if (player != null) {
                player.currentPlayerClass.onItemRightClick(event.getItemInHand(), player, Action.RIGHT_CLICK_BLOCK, event.getBlock().getLocation());
                event.setCancelled(!player.currentPlayerClass.canUseItem(event.getItemInHand()));
            }
            if (event.getBlock().getType() == Material.FIRE) {
                CraftBlock b = (CraftBlock) event.getBlock();
                net.minecraft.server.v1_7_R4.Block block = CraftMagicNumbers.getBlock(b);
                BlockFire fire = (BlockFire) block;
                try {
                    Field var1 = fire.getClass().getDeclaredField("a");
                    Field var2 = fire.getClass().getDeclaredField("b");
                    var1.setAccessible(true);
                    var2.setAccessible(true);
                    int[] var3 = new int[256];
                    var1.set(fire, var3);
                    var2.set(fire, var3);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                //fire.a(fire.b(Blocks.SIGN_POST), 5, 20);
                //fire.a(fire.b(Blocks.WALL_SIGN), 5, 20);
                //fire.a(fire.b(Blocks.TRAP_DOOR), 5, 20);
                //fire.a(fire.b(Blocks.WOODEN_DOOR), 5, 20);
            }
        }
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        event.setAmount(0);
    }

    @EventHandler
    public void onAchievementGet(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void func_001(final PlayerQuitEvent event) {
        String message = thePlugin.leaveMessage.replaceAll("PLAYER_NAME", event.getPlayer().getName());
        event.setQuitMessage(message);

        thePlugin.listActionToPerform.add(new Runnable() {
            public void run() {
                CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
                if (player != null) {
                    if (player.isBlueFlagCarrier) {
                        thePlugin.ctfMap.resetBlueFlag();
                        thePlugin.ctfMap.blueFlagStolen = false;
                    } else if (player.isRedFlagCarrier) {
                        thePlugin.ctfMap.resetRedFlag();
                        thePlugin.ctfMap.redFlagStolen = false;
                    }

                    if (player.isBluePlayer){
                        thePlugin.pluginTeams.removePlayerFromTeam(1, player);
                    } else if (player.isRedPlayer){
                        thePlugin.pluginTeams.removePlayerFromTeam(0, player);
                    }

                    player.onPlayerExitServer();
                    thePlugin.playerList.remove(event.getPlayer().getName());
                    if (VotifierManager.getInstance().isVotifierInstalled) {
                        player.playerVoteSystem.savePlayer();
                    }
                }

            }
        });
    }

    @EventHandler
    public void func_002(final PlayerKickEvent event) {
        String message = thePlugin.kickMessage.replaceAll("PLAYER_NAME", event.getPlayer().getName());
        event.setLeaveMessage(message);

        thePlugin.listActionToPerform.add(new Runnable() {
            public void run() {
                CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
                if (player != null) {
                    if (player.isBlueFlagCarrier) {
                        thePlugin.ctfMap.resetBlueFlag();
                        thePlugin.ctfMap.blueFlagStolen = false;
                    } else if (player.isRedFlagCarrier) {
                        thePlugin.ctfMap.resetRedFlag();
                        thePlugin.ctfMap.redFlagStolen = false;
                    }

                    if (player.isBluePlayer){
                        thePlugin.pluginTeams.removePlayerFromTeam(1, player);
                    } else if (player.isRedPlayer){
                        thePlugin.pluginTeams.removePlayerFromTeam(0, player);
                    }

                    thePlugin.playerList.remove(event.getPlayer().getName());
                    if (VotifierManager.getInstance().isVotifierInstalled) {
                        player.playerVoteSystem.savePlayer();
                    }
                }

            }
        });
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String command = event.getCommand();
        if (command.contains("stop")) {
            if (!thePlugin.ctfMap.mapEditMode.isMapInEditMode()) {
                thePlugin.shutDownServer();
            }
        }
        if (command.contains("ban")) {
            event.getSender().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setCommand("unregistered");
        }
        if (command.contains("banip")) {
            event.getSender().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setCommand("unregistered");
        }
        if (command.contains("kick")) {
            event.getSender().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setCommand("unregistered");
        }
        if (command.contains("deop")) {
            event.getSender().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setCommand("unregistered");
        }

        if (command.contains("op")) {
            event.getSender().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setCommand("unregistered");
        }
        if (command.contains("pardon")) {
            event.getSender().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setCommand("unregistered");
        }
        if (command.contains("pardonip")) {
            event.getSender().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setCommand("unregistered");
        }

        if (command.contains("reload")) {
            event.getSender().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setCommand("unregistered");
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String s = event.getMessage();
        if (s.contains("/stop")) {
            if (!thePlugin.ctfMap.mapEditMode.isMapInEditMode()) {
                thePlugin.shutDownServer();
            }
        }
        if (s.contains("/ban")) {
            event.getPlayer().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setMessage("/unregistered");
        }
        if (s.contains("/banip")) {
            event.getPlayer().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setMessage("/unregistered");
        }
        if (s.contains("/kick")) {
            event.getPlayer().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setMessage("/unregistered");
        }
        if (s.contains("/deop")) {
            event.getPlayer().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setMessage("/unregistered");
        }

        if (s.contains("/op")) {
            event.getPlayer().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setMessage("/unregistered");
        }
        if (s.contains("/pardon")) {
            event.getPlayer().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setMessage("/unregistered");
        }
        if (s.contains("/pardonip")) {
            event.getPlayer().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setMessage("/unregistered");
        }

        if (s.contains("/reload")) {
            event.getPlayer().sendMessage(ChatColor.RED + "The command you're trying to use has been unregistered from the BukkitServer itself !");
            event.setMessage("/unregistered");
        }
    }

    @EventHandler
    public void onTNTExplosion(EntityExplodeEvent event) {
        if (event.getEntityType().equals(EntityType.CREEPER)) {
            event.setCancelled(true);
        } else {
            List<Block> entryToRemove = new ArrayList<Block>();
            for (Block b : event.blockList()) {
                Location l = new Location(thePlugin.ctfMap.world, b.getX(), b.getY(), b.getZ());
                TileEntity tileEntity = thePlugin.ctfMap.tileEntityMap.get(l);
                if (b.getType() != Material.TNT){
                    entryToRemove.add(b);
                }
                if (tileEntity != null && tileEntity instanceof TileEntityBreakableBlock || tileEntity != null && tileEntity instanceof TileEntityTNTBreakableBlock) {
                    thePlugin.ctfMap.removeBlockTileEntity(b.getX(), b.getY(), b.getZ(), event.getLocation().getWorld(), true);
                }
            }
            for (Block b1 : entryToRemove){
                event.blockList().remove(b1);
            }
            //event.blockList().clear();
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String s = event.getPlayer().getName();
        CTFPlayer player = thePlugin.playerList.get(event.getPlayer().getName());
        if (player != null) {
            if (player.isRedPlayer) {
                if (thePlugin.containsDevMember(s)) {
                    event.setFormat(ChatColor.RED + "[Dev] <" + player.associatedPlayer.getName() + "> : " + ChatColor.WHITE + event.getMessage());
                } else if (player.associatedPlayer.isOp()) {
                    event.setFormat(ChatColor.RED + "[Master] <" + player.associatedPlayer.getName() + "> : " + ChatColor.WHITE + event.getMessage());
                } else {
                    event.setFormat(ChatColor.RED + "<" + player.associatedPlayer.getName() + "> : " + ChatColor.WHITE + event.getMessage());
                }
            } else if (player.isBluePlayer) {
                if (thePlugin.containsDevMember(s)) {
                    event.setFormat(ChatColor.BLUE + "[Dev] <" + player.associatedPlayer.getName() + "> : " + ChatColor.WHITE + event.getMessage());
                } else if (player.associatedPlayer.isOp()) {
                    event.setFormat(ChatColor.BLUE + "[Master] <" + player.associatedPlayer.getName() + "> : " + ChatColor.WHITE + event.getMessage());
                } else {
                    event.setFormat(ChatColor.BLUE + "<" + player.associatedPlayer.getName() + "> : " + ChatColor.WHITE + event.getMessage());
                }
            }
        }
    }
}
