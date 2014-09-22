package fr.yurictf.entities;

import net.minecraft.server.v1_7_R4.EntityInsentient;
import net.minecraft.server.v1_7_R4.EntityTypes;
import net.minecraft.server.v1_7_R4.EntityZombie;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Method;

public enum CustomEntityType {

    FRIENDLY_ZOMBIE("FriendlyZombie", 201, EntityType.ZOMBIE, EntityZombie.class, EntityFriendlyZombie.class);

    private String name;
    private int id;
    private EntityType entityType;
    private Class<? extends EntityInsentient> nmsClass;
    private Class<? extends EntityInsentient> customClass;

    private CustomEntityType(String name, int id, EntityType entityType, Class<? extends EntityInsentient> nmsClass, Class<? extends EntityInsentient> customClass){
        this.name = name;
        this.id = id;
        this.entityType = entityType;
        this.nmsClass = nmsClass;
        this.customClass = customClass;
    }

    public String getName(){
        return this.name;
    }

    public int getID(){
        return this.id;
    }

    public EntityType getEntityType(){
        return this.entityType;
    }

    public Class<? extends EntityInsentient> getNMSClass(){
        return this.nmsClass;
    }

    public Class<? extends EntityInsentient> getCustomClass(){
        return this.customClass;
    }

    public static void registerEntities(){
        for (CustomEntityType entity : values()){
            try{
                Method a = EntityTypes.class.getDeclaredMethod("a", new Class<?>[]{Class.class, String.class, int.class});
                a.setAccessible(true);
                a.invoke(null, entity.getCustomClass(), entity.getName(), entity.getID());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}