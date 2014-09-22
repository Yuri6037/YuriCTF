package fr.yurictf.server;

import fr.yurictf.YuriCTF;
import fr.yurictf.classes.CTFClass;
import fr.yurictf.classes.extention.Extention;
import fr.yurictf.extentions.EventSystem;
import fr.yurictf.extentions.event.flag.EventFlagDropped;
import fr.yurictf.language.Translator;
import fr.yurictf.shop.PlayerAbility;
import fr.yurictf.votifier.VotifierManager;
import fr.yurictf.votifier.VotifierPlayer;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CTFPlayer {

    // The bukkit player for this CTFPlayer
    public Player associatedPlayer;

    // Is the player a flag-carrier and witch flag is he carrying
    public boolean isRedFlagCarrier;
    public boolean isBlueFlagCarrier;

    // The player choosed class
    public CTFClass currentPlayerClass;
    public Extention classExtention;

    // The plugin itself
    public YuriCTF thePlugin;

    // Witch team this player is in
    public boolean isRedPlayer;
    public boolean isBluePlayer;

    public boolean hasScoreboard = false;

    public int ticksBeforePoison;

    public VotifierPlayer playerVoteSystem;

    public Translator playerTranslator;

    public boolean hasFallDamageDesactivated = false;

    //Player stats
    public int gameKillNumber;
    public int deathCount;
    public int captures;
    public int playerPoints;



    public void onPlayerExitServer(){
        FileConfiguration file = new YamlConfiguration();
        file.set("points", playerPoints);
        try {
            File f = new File(thePlugin.getDataFolder() + File.separator + "save" + File.separator);
            if (!f.exists()){
                f.mkdirs();
            }
            file.save(thePlugin.getDataFolder() + File.separator + "save" + File.separator + associatedPlayer.getUniqueId() + ".yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onPlayerLoad(){
        FileConfiguration file = new YamlConfiguration();
        try {
            file.load(thePlugin.getDataFolder() + File.separator + "save" + File.separator + associatedPlayer.getUniqueId() + ".yml");
            playerPoints = file.getInt("points");
        } catch (IOException e) {
            //e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public CTFPlayer(CTFClass ctfClass, Player player, YuriCTF plugin) {
        playerTranslator = new Translator(this);
        associatedPlayer = player;
        currentPlayerClass = ctfClass;
        thePlugin = plugin;
        onPlayerLoad();
        if (VotifierManager.getInstance().isVotifierInstalled){
            playerVoteSystem = new VotifierPlayer(plugin, this);
        }
        int red = thePlugin.redPlayers;
        int blue = thePlugin.bluePlayers;
        if (red == 0 && blue == 0) {
            Random r = new Random();
            boolean b = r.nextBoolean();
            if (b) {
                isRedPlayer = true;
                isBluePlayer = false;
                thePlugin.redPlayers++;
            } else {
                isBluePlayer = true;
                isRedPlayer = false;
                thePlugin.bluePlayers++;
            }
        } else if (red > blue) {
            isBluePlayer = true;
            isRedPlayer = false;
            thePlugin.bluePlayers++;
        } else if (blue > red) {
            isRedPlayer = true;
            isBluePlayer = false;
            thePlugin.redPlayers++;
        } else if (blue == red) {
            Random r = new Random();
            boolean b = r.nextBoolean();
            if (b) {
                isRedPlayer = true;
                isBluePlayer = false;
                thePlugin.redPlayers++;
            } else {
                isBluePlayer = true;
                isRedPlayer = false;
                thePlugin.bluePlayers++;
            }
        }
    }

    public void onUpdate() {
        currentPlayerClass.updatePlayer(this);
        if (classExtention != null) {
            classExtention.onUpdate();
        }
        if (isBlueFlagCarrier && currentPlayerClass.isFlagPoisoning(this) || isRedFlagCarrier && currentPlayerClass.isFlagPoisoning(this)){
            ticksBeforePoison++;
            if (ticksBeforePoison >= 120){
                associatedPlayer.damage(0.5D);
                associatedPlayer.sendMessage(ChatColor.RED + "The flag you're carrying has poisoned you !");
                ticksBeforePoison = 0;
            }
        }
    }

    public void onDeath() {
        currentPlayerClass.onPlayerDeath(this);
        if (isBlueFlagCarrier) {
            thePlugin.ctfMap.droppedBlueFlag = thePlugin.ctfMap.world.dropItemNaturally(associatedPlayer.getLocation(), new ItemStack(Material.WOOL, 1, (short) 11));
            thePlugin.ctfMap.blueFlagTimeInAir = 120;
            thePlugin.getLogger().info("Blue flag carrier death : " + associatedPlayer.getName());
            Map<String, Object[]> map = EventSystem.callEvent(new EventFlagDropped(this));
            String var = null;
            for (Map.Entry e : map.entrySet()){
                Object[] obj = (Object[]) e.getValue();
                var = (String) obj[0];
            }
            if (var == null) {
                thePlugin.getServer().broadcastMessage(ChatColor.AQUA + "Player " + associatedPlayer.getName() + " has dropped the blue flag !");
            } else {
                thePlugin.getServer().broadcastMessage(var);
            }
        }

        if (isRedFlagCarrier) {
            thePlugin.ctfMap.droppedRedFlag = thePlugin.ctfMap.world.dropItemNaturally(associatedPlayer.getLocation(), new ItemStack(Material.WOOL, 1, (short) 14));
            thePlugin.ctfMap.redFlagTimeInAir = 120;
            thePlugin.getLogger().info("Red flag carrier death : " + associatedPlayer.getName());
            Map<String, Object[]> map = EventSystem.callEvent(new EventFlagDropped(this));
            String var = null;
            for (Map.Entry e : map.entrySet()){
                Object[] obj = (Object[]) e.getValue();
                var = (String) obj[0];
            }
            if (var == null) {
                thePlugin.getServer().broadcastMessage(ChatColor.AQUA + "Player " + associatedPlayer.getName() + " has dropped the red flag !");
            } else {
                thePlugin.getServer().broadcastMessage(var);
            }
        }
    }

    public Location getPlayerRespawnLocation() {
        if (thePlugin.ctfMap.mapEditMode.isMapInEditMode()){
            return thePlugin.getServer().getWorlds().get(0).getSpawnLocation();
        }
        if (isBluePlayer) {
            return (new Location(thePlugin.ctfMap.world, thePlugin.ctfMap.blueSpawnX + 0.47627, thePlugin.ctfMap.blueSpawnY, thePlugin.ctfMap.blueSpawnZ + 0.52554, 0, 0));
        } else if (isRedPlayer) {
            return (new Location(thePlugin.ctfMap.world, thePlugin.ctfMap.redSpawnX + 0.47627, thePlugin.ctfMap.redSpawnY, thePlugin.ctfMap.redSpawnZ + 0.52554, 0, 0));
        }
        return getWaitingSpawnLocation();
    }

    public Location getWaitingSpawnLocation(){
        return new Location(thePlugin.ctfMap.waitingWorld, thePlugin.ctfMap.lobbySpawnX, thePlugin.ctfMap.lobbySpawnY, thePlugin.ctfMap.lobbySpawnZ);
    }

    private String generateBookPage(){
        if (isBluePlayer){
            int i = captures * 2;
            return ChatColor.BLUE + "[BLUE] " + associatedPlayer.getName() + " :\n\n\n" + ChatColor.DARK_BLUE + "Kills > " + gameKillNumber + "\n" + ChatColor.DARK_RED + "Deaths > " + deathCount + "\n" + ChatColor.LIGHT_PURPLE + "Captures > " + captures + "\n" + ChatColor.DARK_GREEN + "Earned points > " + (gameKillNumber + i) + "\n" + ChatColor.DARK_BLUE + "Total points > " + playerPoints;
        } else {
            int i = captures * 2;
            return ChatColor.RED + "[RED] " + associatedPlayer.getName() + " :\n\n\n" + ChatColor.DARK_BLUE + "Kills > " + gameKillNumber + "\n" + ChatColor.DARK_RED + "Deaths > " + deathCount + "\n" + ChatColor.LIGHT_PURPLE + "Captures > " + captures + "\n" + ChatColor.DARK_GREEN + "Earned points > " + (gameKillNumber + i) + "\n" + ChatColor.DARK_BLUE + "Total points > " + playerPoints;
        }
    }

    public void spawnPlayerInLobby(){
        //Reset all player infos
        isBlueFlagCarrier = false;
        isRedFlagCarrier = false;
        associatedPlayer.resetPlayerTime();
        associatedPlayer.getInventory().clear();
        associatedPlayer.getInventory().setHelmet(null);
        associatedPlayer.getInventory().setChestplate(null);
        associatedPlayer.getInventory().setLeggings(null);
        associatedPlayer.getInventory().setBoots(null);
        associatedPlayer.setLevel(0);
        associatedPlayer.setExp(0.0F);
        for(PotionEffect effect : associatedPlayer.getActivePotionEffects()) {
            associatedPlayer.removePotionEffect(effect.getType());
        }
        associatedPlayer.setFireTicks(0);
        associatedPlayer.setHealth(20.0D);

        //Game statistics book
        playerPoints += gameKillNumber;
        int i = captures * 2;
        playerPoints += i;
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setAuthor("YuriCTF");
        meta.setTitle("Players statistics");
        List<String> lore = new ArrayList<String>();
        List<String> pages = new ArrayList<String>();
        pages.add(generateBookPage());
        lore.add(ChatColor.WHITE + "" + ChatColor.BOLD + "Game Players (" + thePlugin.getServer().getOnlinePlayers().size() + "/" + thePlugin.getServer().getMaxPlayers() + ")");
        for (Map.Entry entry : thePlugin.playerList.entrySet()){
            String name = (String) entry.getKey();
            CTFPlayer p = (CTFPlayer) entry.getValue();
            if (p.isBluePlayer){
                lore.add(ChatColor.BLUE + name);
            } else {
                lore.add(ChatColor.RED + name);
            }

            if (!name.equals(associatedPlayer.getName())){
                pages.add(p.generateBookPage());
            }
        }
        meta.setPages(pages);
        meta.setLore(lore);
        book.setItemMeta(meta);
        associatedPlayer.getInventory().addItem(book);

        associatedPlayer.getInventory().addItem(new ItemStack(Material.DRAGON_EGG));

        //Teleport player to correct team
        associatedPlayer.teleport(getWaitingSpawnLocation());
    }

    public void respawnPlayer() {
        //Reset CTFPlayer values
        if (classExtention != null){
            classExtention.resetExtention();
        }
        classExtention = currentPlayerClass.getClassExtention(this, thePlugin);
        associatedPlayer.resetPlayerTime();
        isBlueFlagCarrier = false;
        isRedFlagCarrier = false;

        //Teleport player to correct team
        if (isBluePlayer) {
            associatedPlayer.teleport(getPlayerRespawnLocation());
            thePlugin.getLogger().info("Teleported player to blue spawn");
        } else if (isRedPlayer) {
            associatedPlayer.teleport(getPlayerRespawnLocation());
            thePlugin.getLogger().info("Teleported player to red spawn");
        }

        //Reset player inventory
        associatedPlayer.getInventory().clear();

        //Set player inventory
        for (int i = 0; i < currentPlayerClass.getSpawningItems().length; i++) {
            ItemStack stack = currentPlayerClass.getSpawningItems()[i];
            associatedPlayer.getInventory().setItem(i, stack);
        }

        //Set player armor
        associatedPlayer.getInventory().setHelmet(currentPlayerClass.getArmorEquipement()[0]);
        associatedPlayer.getInventory().setChestplate(currentPlayerClass.getArmorEquipement()[1]);
        associatedPlayer.getInventory().setLeggings(currentPlayerClass.getArmorEquipement()[2]);
        associatedPlayer.getInventory().setBoots(currentPlayerClass.getArmorEquipement()[3]);

        //Set player game mode
        if (thePlugin.ctfMap.mapEditMode.isPlayerAllowedToBuild(associatedPlayer)) {
            associatedPlayer.setGameMode(GameMode.CREATIVE);
        } else {
            associatedPlayer.setGameMode(GameMode.SURVIVAL);
        }

        //Reset player values
        for(PotionEffect effect : associatedPlayer.getActivePotionEffects()) {
            associatedPlayer.removePotionEffect(effect.getType());
        }
        associatedPlayer.setFireTicks(0);
        associatedPlayer.setFoodLevel(currentPlayerClass.getFoodLevel());
        associatedPlayer.setLevel(0);
        associatedPlayer.setExp(0.0F);
        associatedPlayer.setHealth(20.0D);

        //Applying player abilities if have one
        applyPlayerAbilities();

        //Add player to a team if it doesn't have one
        if (isBluePlayer && !thePlugin.pluginTeams.isPlayerInTeam(1, this)){
            thePlugin.pluginTeams.addPlayerToTeam(1, this);
        } else if (isRedPlayer && !thePlugin.pluginTeams.isPlayerInTeam(0, this)){
            thePlugin.pluginTeams.addPlayerToTeam(0, this);
        }

        thePlugin.getLogger().info("Respawned player : " + associatedPlayer.getName());
    }

    private void applyPlayerAbilities(){
        List<PlayerAbility> list = thePlugin.playerAbilitiesMap.get(associatedPlayer.getName());
        List<PlayerAbility> itemToDelete = new ArrayList<PlayerAbility>();
        if (list != null) {
            for (PlayerAbility ability : list) {
                if (ability.isPotion()){
                    associatedPlayer.addPotionEffect(ability.getPotionEffect());
                    itemToDelete.add(ability);
                } else if (ability.isItemStack()){
                    associatedPlayer.getInventory().addItem(ability.getItemStack());
                }
            }
            for (PlayerAbility ab : itemToDelete){
                list.remove(ab);
            }
            thePlugin.playerAbilitiesMap.remove(associatedPlayer.getName());
            thePlugin.playerAbilitiesMap.put(associatedPlayer.getName(), list);
        }
    }
}
