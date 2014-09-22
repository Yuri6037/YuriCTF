package fr.yurictf.classes;

import fr.yurictf.YuriCTF;
import fr.yurictf.classes.etc.ProjectileType;
import fr.yurictf.classes.extention.Extention;
import fr.yurictf.server.CTFPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CTFClassBuilder implements CTFClass {

    private String pointsFinder = ChatColor.GREEN + "Special CTF points finder";
    private String itemCleaner = ChatColor.GREEN + "Dropped-Items Cleaner";

    public static String flagRed = ChatColor.BLUE + "TP to RedFlag";
    public static String flagBlue = ChatColor.BLUE + "TP to BlueFlag";
    public static String spawnRed = ChatColor.BLUE + "TP to RedSpawn";
    public static String spawnBlue = ChatColor.BLUE + "TP to BlueSpawn";

    public ItemStack[] getSpawningItems() {
        ItemStack worldEditAxe = new ItemStack(Material.WOOD_AXE, 1);
        Extention.renameItem(worldEditAxe, ChatColor.GREEN + "World-Edit (Master only)");

        ItemStack worlEditCompass = new ItemStack(Material.COMPASS, 1);
        Extention.renameItem(worlEditCompass, "World-Edit Teleporter (Master only)");

        ItemStack entityItemCleaner = new ItemStack(Material.DIAMOND, 1);
        Extention.renameItem(entityItemCleaner, itemCleaner);

        ItemStack specialPointsFinder = new ItemStack(Material.WATCH, 1);
        Extention.renameItem(specialPointsFinder, pointsFinder);

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        Extention.renameItem(sword, ChatColor.BLUE + "Entity Killer Sword");
        sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 50);
        sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 50);
        sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 50);
        return new ItemStack[]{sword, worldEditAxe, worlEditCompass, entityItemCleaner, specialPointsFinder};
    }

    public boolean isDefaultClass(boolean isEditMode) {
        return isEditMode;
    }

    public void updatePlayer(CTFPlayer player) {
    }

    public void onPlayerDeath(CTFPlayer player) {
    }

    public boolean canCareFlag(CTFPlayer player) {
        return false;
    }

    public String getClassName() {
        return "Builder";
    }

    public ItemStack[] getArmorEquipement() {
        return new ItemStack[]{null, null, null, null};
    }

    public int getFoodLevel() {
        return 20;
    }

    public boolean canPlayerTakeDamage(EntityDamageEvent.DamageCause cause, CTFPlayer player) {
        return false;
    }

    public ItemStack onItemRightClick(ItemStack stack, CTFPlayer player, Action action, Location rightClickedBlock) {
        if (stack == null || stack.getItemMeta().getDisplayName() == null){
            return stack;
        }
        if (stack.getItemMeta().getDisplayName().equals(itemCleaner) && action == Action.RIGHT_CLICK_AIR || stack.getItemMeta().getDisplayName().equals(itemCleaner) && action == Action.RIGHT_CLICK_BLOCK){
            int num = 0;
            for (Entity e : player.associatedPlayer.getWorld().getEntities()){
                if (e instanceof Item){
                    e.remove();
                    num++;
                }
            }
            player.associatedPlayer.sendMessage("Cleared " + num + " dropped-items...");
            player.associatedPlayer.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + "Dropped-items have been cleared !");
        }

        if (stack.getItemMeta().getDisplayName().equals(pointsFinder) && action == Action.RIGHT_CLICK_AIR || stack.getItemMeta().getDisplayName().equals(pointsFinder) && action == Action.RIGHT_CLICK_BLOCK){
            Inventory inv = Bukkit.createInventory(null, 9, "Special points finder");
            ItemStack redFlag = new ItemStack(Material.WOOL, 1, (short)14);
            ItemStack blueFlag = new ItemStack(Material.WOOL, 1, (short)11);
            ItemStack redSpawn = new ItemStack(Material.NETHERRACK, 1);
            ItemStack blueSpawn = new ItemStack(Material.LAPIS_BLOCK, 1);
            Extention.renameItem(redFlag, flagRed);
            Extention.renameItem(blueFlag, flagBlue);
            Extention.renameItem(redSpawn, spawnRed);
            Extention.renameItem(blueSpawn, spawnBlue);
            inv.setItem(0, redFlag);
            inv.setItem(1, blueFlag);
            inv.setItem(7, redSpawn);
            inv.setItem(8, blueSpawn);
            player.associatedPlayer.openInventory(inv);
        }
        return stack;
    }

    public Extention getClassExtention(CTFPlayer player, YuriCTF plugin) {
        return null;
    }

    public boolean canUseItem(ItemStack stack) {
        if (stack == null){
            return true;
        }
        return !(stack.getType() == Material.WOOD_AXE || stack.getType() == Material.COMPASS);
    }

    public void onPlayerDamagePlayer(CTFPlayer attackingPlayer, CTFPlayer damagedPlayer) {
    }

    public boolean isFlagPoisoning(CTFPlayer player) {
        return false;
    }

    public void onProjectileHit(Location projectileLocation, CTFPlayer shooter, ProjectileType type, YuriCTF plugin) {
    }

    public String getClassDescription(CTFPlayer player) {
        return null;
    }

    public boolean canAccessClass(YuriCTF plugin, CTFPlayer player) {
        return plugin.ctfMap.mapEditMode.isPlayerAllowedToBuild(player.associatedPlayer);
    }
}
