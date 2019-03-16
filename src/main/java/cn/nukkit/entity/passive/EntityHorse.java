package cn.nukkit.entity.passive;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.SetEntityLinkPacket;

import java.util.Objects;

/**
 * @author PikyCZ
 */
public class EntityHorse extends EntityAnimal implements EntityRideable {

    public static final int NETWORK_ID = 23;

    public EntityHorse(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.6982f;
        }
        return 1.3965f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.8f;
        }
        return 1.6f;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(15);
    }

    @Override
    public Item[] getDrops() {
        return new Item[]{Item.get(Item.LEATHER)};
    }

    @Override
    public void spawnTo(Player player) {
        AddEntityPacket pk = new AddEntityPacket();
        pk.type = this.getNetworkId();
        pk.entityUniqueId = this.getId();
        pk.entityRuntimeId = this.getId();
        pk.x = (float) this.x;
        pk.y = (float) this.y;
        pk.z = (float) this.z;
        pk.speedX = (float) this.motionX;
        pk.speedY = (float) this.motionY;
        pk.speedZ = (float) this.motionZ;
        pk.metadata = this.dataProperties;
        player.dataPacket(pk);

        super.spawnTo(player);
    }

    /**
     * Mount or Dismounts an Entity from a vehicle
     *
     * @param entity The target Entity
     * @return {@code true} if the mounting successful
     */
    @Override
    public boolean mountEntity(Entity entity) {
        Objects.requireNonNull(entity, "The target of the mounting entity can't be null");

        this.PitchDelta = 0.0D;
        this.YawDelta = 0.0D;
        if (entity.riding != null) {
            SetEntityLinkPacket pk;

            pk = new SetEntityLinkPacket();
            pk.rider = getId(); //Weird Weird Weird
            pk.riding = entity.getId();
            pk.type = 3;
            Server.broadcastPacket(this.hasSpawned.values(), pk);

            if (entity instanceof Player) {
                pk = new SetEntityLinkPacket();
                pk.rider = getId();
                pk.riding = entity.getId();
                pk.type = 3;
                ((Player) entity).dataPacket(pk);
            }

            entity.riding = null;
            linkedEntity = null;
            entity.setDataFlag(DATA_FLAGS, DATA_FLAG_RIDING, false);
            return true;
        }

        updateRiderPosition(getMountedOffset());

        SetEntityLinkPacket pk;

        pk = new SetEntityLinkPacket();
        pk.rider = this.getId();
        pk.riding = entity.getId();
        pk.type = 2;
        Server.broadcastPacket(this.hasSpawned.values(), pk);

        if (entity instanceof Player) {
            pk = new SetEntityLinkPacket();
            pk.rider = this.getId();
            pk.riding = 0;
            pk.type = 2;
            ((Player) entity).dataPacket(pk);
        }

        entity.riding = this;
        linkedEntity = entity;

        entity.setDataFlag(DATA_FLAGS, DATA_FLAG_RIDING, true);
        return true;
    }

    @Override
    public boolean onInteract(Player p, Item item) {
        if (linkedEntity != null) {
            return false;
        }

        mountEntity(p);
        return true;
    }
}
