package fr.yurictf.shop;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerAbility {

    private Object obj;


    public PlayerAbility(Object obj){
        this.obj = obj;
    }

    public boolean isPotion(){
        return obj instanceof PotionEffect;
    }

    public boolean isItemStack(){
        return obj instanceof ItemStack;
    }

    public ItemStack getItemStack(){
        if (isItemStack()) {
            return (ItemStack) obj;
        }
        return null;
    }

    public PotionEffect getPotionEffect(){
        if (isPotion()) {
            return (PotionEffect) obj;
        }
        return null;
    }
}
