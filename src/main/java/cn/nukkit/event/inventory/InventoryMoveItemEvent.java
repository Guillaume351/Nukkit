package cn.nukkit.event.inventory;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.item.Item;
import lombok.Getter;
import lombok.Setter;

/**
 * @author CreeperFace
 * <p>
 * Called when inventory transaction is not caused by a player
 */
@Getter
public class InventoryMoveItemEvent extends InventoryEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private final Inventory targetInventory;
    private final InventoryHolder source;

    @Setter
    private Item item;

    private final Action action;

    public InventoryMoveItemEvent(Inventory from, Inventory targetInventory, InventoryHolder source, Item item, Action action) {
        super(from);
        this.targetInventory = targetInventory;
        this.source = source;
        this.item = item;
        this.action = action;
    }

    public enum Action {
        SLOT_CHANGE, //transaction between 2 inventories
        PICKUP,
        DROP,
        DISPENSE
    }
}
