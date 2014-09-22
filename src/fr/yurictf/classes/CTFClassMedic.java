package fr.yurictf.classes;

import fr.yurictf.classes.extention.Extention;
import fr.yurictf.classes.extention.ExtentionMedic;
import fr.yurictf.map.tileentity.TileEntityBreakableBlock;
import fr.yurictf.server.CTFPlayer;
import fr.yurictf.classes.etc.ProjectileType;
import fr.yurictf.YuriCTF;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class CTFClassMedic implements CTFClass {

    public ItemStack[] getSpawningItems() {
        ItemStack sword = new ItemStack(Material.STONE_SWORD, 1);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 2);
        sword.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
        Extention.renameItem(sword, ChatColor.YELLOW + "Healing Sword");
        ItemStack food = new ItemStack(Material.COOKED_BEEF, 4);
        ItemStack web = new ItemStack(Material.WEB,  16);
        return new ItemStack[]{sword, food, web};
    }

    public boolean isDefaultClass(boolean isEditMode) {
        return false;
    }

    public void updatePlayer(CTFPlayer player) {
        if (player.classExtention == null || !(player.classExtention instanceof ExtentionMedic)) {
            return;
        }
        ExtentionMedic medic = (ExtentionMedic) player.classExtention;
        if (player.associatedPlayer.getHealth() < 19.9D && medic.healthTicks != -255) {
            medic.healthTicks++;
            if (medic.healthTicks >= 5) {
                player.associatedPlayer.setHealth(player.associatedPlayer.getHealth() + 0.1D);
                medic.healthTicks = 0;
            }
        }
    }

    public void onPlayerDeath(CTFPlayer player) {
        if (player.classExtention == null || !(player.classExtention instanceof ExtentionMedic)) {
            return;
        }
        ExtentionMedic medic = (ExtentionMedic) player.classExtention;
        medic.healthTicks = -255;
    }

    public boolean canCareFlag(CTFPlayer player) {
        player.associatedPlayer.sendMessage(player.playerTranslator.translate("class.noFlagCare.medic"));
        return false;
    }

    public String getClassName() {
        return "Medic";
    }

    public ItemStack[] getArmorEquipement() {
        ItemStack helmet = new ItemStack(Material.GOLD_HELMET, 1);
        ItemStack body = new ItemStack(Material.GOLD_CHESTPLATE, 1);
        ItemStack legs = new ItemStack(Material.GOLD_LEGGINGS, 1);
        ItemStack boots = new ItemStack(Material.GOLD_BOOTS, 1);
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
            if (player.associatedPlayer.getItemInHand().getType() == Material.WEB && action == Action.RIGHT_CLICK_BLOCK && rightClickedBlock != null){
                player.thePlugin.ctfMap.placeBlockTileEntity(rightClickedBlock.getBlockX(), rightClickedBlock.getBlockY(), rightClickedBlock.getBlockZ(), null, new TileEntityBreakableBlock(player, Material.WEB));
                ItemStack toRemove = new ItemStack(Material.WEB, 1);
                player.associatedPlayer.getInventory().removeItem(toRemove);
                player.associatedPlayer.updateInventory();
            }
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
        return new ExtentionMedic(plugin, player, this);
    }

    public boolean canUseItem(ItemStack stack) {
        return stack != null && stack.getType() == Material.STONE_SWORD || stack != null && stack.getType() == Material.WEB;
    }

    public void onPlayerDamagePlayer(CTFPlayer attackingPlayer, CTFPlayer damagedPlayer) {
        if (attackingPlayer.isBluePlayer && damagedPlayer.isBluePlayer || attackingPlayer.isRedPlayer && damagedPlayer.isRedPlayer) {
            ItemStack hand = attackingPlayer.associatedPlayer.getItemInHand();
            if (hand == null || hand.getType() != Material.STONE_SWORD){
                return;
            }
            damagedPlayer.associatedPlayer.setHealth(20.0D);

            damagedPlayer.associatedPlayer.getInventory().clear();
            for (int i = 0; i < damagedPlayer.currentPlayerClass.getSpawningItems().length; i++) {
                ItemStack stack = damagedPlayer.currentPlayerClass.getSpawningItems()[i];
                damagedPlayer.associatedPlayer.getInventory().setItem(i, stack);
            }
            damagedPlayer.associatedPlayer.getInventory().setHelmet(damagedPlayer.currentPlayerClass.getArmorEquipement()[0]);
            damagedPlayer.associatedPlayer.getInventory().setChestplate(damagedPlayer.currentPlayerClass.getArmorEquipement()[1]);
            damagedPlayer.associatedPlayer.getInventory().setLeggings(damagedPlayer.currentPlayerClass.getArmorEquipement()[2]);
            damagedPlayer.associatedPlayer.getInventory().setBoots(damagedPlayer.currentPlayerClass.getArmorEquipement()[3]);

            if (damagedPlayer.isBlueFlagCarrier) {
                damagedPlayer.associatedPlayer.getInventory().addItem(new ItemStack(Material.WOOL, 1, (short) 11));
            } else if (damagedPlayer.isRedFlagCarrier) {
                damagedPlayer.associatedPlayer.getInventory().addItem(new ItemStack(Material.WOOL, 1, (short) 14));
            }
        }
    }

    public boolean isFlagPoisoning(CTFPlayer player) {
        return false;
    }

    public void onProjectileHit(Location projectileLocation, CTFPlayer shooter, ProjectileType type, YuriCTF plugin) {

    }

    public String getClassDescription(CTFPlayer player) {
        return player.playerTranslator.translate("class.desc.medic");
    }

    public boolean canAccessClass(YuriCTF plugin, CTFPlayer player) {
        return true;
    }
}
