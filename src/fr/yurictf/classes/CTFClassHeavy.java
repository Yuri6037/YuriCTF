package fr.yurictf.classes;

import fr.yurictf.classes.extention.Extention;
import fr.yurictf.server.CTFPlayer;
import fr.yurictf.classes.etc.ProjectileType;
import fr.yurictf.YuriCTF;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class CTFClassHeavy implements CTFClass {

    public ItemStack[] getSpawningItems() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        ItemStack food = new ItemStack(Material.COOKED_BEEF, 3);
        return new ItemStack[]{sword, food};
    }

    public boolean isDefaultClass(boolean isEditMode) {
        return !isEditMode;
    }

    public void updatePlayer(CTFPlayer player) {

    }

    public void onPlayerDeath(CTFPlayer player) {

    }

    public boolean canCareFlag(CTFPlayer player) {
        player.associatedPlayer.sendMessage(player.playerTranslator.translate("class.noFlagCare.heavy"));
        return false;
    }

    public String getClassName() {
        return "Heavy";
    }

    public ItemStack[] getArmorEquipement() {
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET, 1);
        ItemStack body = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        ItemStack legs = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS, 1);
        return new ItemStack[]{helmet, body, legs, boots};
    }

    public int getFoodLevel() {
        return 16;
    }

    public boolean canPlayerTakeDamage(EntityDamageEvent.DamageCause cause, CTFPlayer player) {
        return true;
    }

    public ItemStack onItemRightClick(ItemStack stack, CTFPlayer player, Action action, Location rightClickedBlock) {
        if (stack != null) {
            double health = player.associatedPlayer.getHealth();
            if (player.associatedPlayer.getItemInHand().getType() == Material.COOKED_BEEF && health < 20.0D) {
                stack.setAmount(stack.getAmount() - 1);
                if (health < 16.0D) {
                    player.associatedPlayer.setHealth(player.associatedPlayer.getHealth() + 4.0D);
                } else {
                    player.associatedPlayer.setHealth(20.0D);
                }
                return stack;
            }
        }
        return stack;
    }

    public Extention getClassExtention(CTFPlayer player, YuriCTF plugin) {
        return null;
    }

    public boolean canUseItem(ItemStack stack) {
        return stack != null && stack.getType() == Material.DIAMOND_SWORD;
    }

    public void onPlayerDamagePlayer(CTFPlayer attackingPlayer, CTFPlayer damagedPlayer) {

    }

    public boolean isFlagPoisoning(CTFPlayer player) {
        return true;
    }

    public void onProjectileHit(Location projectileLocation, CTFPlayer shooter, ProjectileType type, YuriCTF plugin) {

    }

    public String getClassDescription(CTFPlayer player) {
        return player.playerTranslator.translate("class.desc.heavy");
    }

    public boolean canAccessClass(YuriCTF plugin, CTFPlayer player) {
        return true;
    }
}
