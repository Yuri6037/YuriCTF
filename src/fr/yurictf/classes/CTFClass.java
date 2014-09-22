package fr.yurictf.classes;

import fr.yurictf.classes.extention.Extention;
import fr.yurictf.server.CTFPlayer;
import fr.yurictf.classes.etc.ProjectileType;
import fr.yurictf.YuriCTF;
import org.bukkit.Location;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public interface CTFClass {

    public ItemStack[] getSpawningItems();

    public boolean isDefaultClass(boolean isEditMode);

    public void updatePlayer(CTFPlayer player);

    public void onPlayerDeath(CTFPlayer player);

    public boolean canCareFlag(CTFPlayer player);

    public String getClassName();

    public ItemStack[] getArmorEquipement();

    public int getFoodLevel();

    public boolean canPlayerTakeDamage(EntityDamageEvent.DamageCause cause, CTFPlayer player);

    public ItemStack onItemRightClick(ItemStack stack, CTFPlayer player, Action action, Location rightClickedBlock);

    public Extention getClassExtention(CTFPlayer player, YuriCTF plugin);

    public boolean canUseItem(ItemStack stack);

    public void onPlayerDamagePlayer(CTFPlayer attackingPlayer, CTFPlayer damagedPlayer);

    public boolean isFlagPoisoning(CTFPlayer player);

    public void onProjectileHit(Location projectileLocation, CTFPlayer shooter, ProjectileType type, YuriCTF plugin);

    public String getClassDescription(CTFPlayer player);

    public boolean canAccessClass(YuriCTF plugin, CTFPlayer player);

}
