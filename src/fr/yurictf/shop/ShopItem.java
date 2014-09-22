package fr.yurictf.shop;

import fr.yurictf.server.CTFPlayer;
import org.bukkit.inventory.ItemStack;

public interface ShopItem {

    public void onItemTaken(CTFPlayer player);
    public ItemStack getShopDisplayedItem();
    public int getAmountToPay();
    public String getItemDisplayNameInChat();

}
