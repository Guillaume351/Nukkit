package cn.nukkit.blockentity;

import cn.nukkit.block.Block;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * Created by Snake1999 on 2016/2/4.
 * Package cn.nukkit.blockentity in project Nukkit.
 */
public class BlockEntityFlowerPot extends BlockEntitySpawnable {

    private static final Object2IntMap<String> CONVERTER = new Object2IntOpenHashMap<>();

    static {
        CONVERTER.put("sapling", Block.SAPLING);
        CONVERTER.put("yellow_flower", Block.DANDELION);
        CONVERTER.put("red_flower", Block.RED_FLOWER);
        CONVERTER.put("cactus", Block.CACTUS);
        CONVERTER.put("brown_mushroom", Block.BROWN_MUSHROOM);
        CONVERTER.put("red_mushroom", Block.RED_MUSHROOM);
        CONVERTER.put("deadbush", Block.DEAD_BUSH);
        CONVERTER.put("tallgrass", Block.TALL_GRASS);
        CONVERTER.put("fern", Block.TALL_GRASS);

        CONVERTER.defaultReturnValue(0);
    }

    public BlockEntityFlowerPot(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initBlockEntity() {
        if (!namedTag.contains("item")) {
            if (namedTag.contains("Item")) {
                namedTag.putShort("item", CONVERTER.getInt(namedTag.getString("Item")));
                namedTag.remove("Item");
            }

            namedTag.putShort("item", 0);
        }

        if (!namedTag.contains("mData")) {
            int data = 0;

            if (namedTag.contains("data")) {
                data = namedTag.getInt("data");
                namedTag.remove("data");
            } else if (namedTag.contains("Data")) {
                data = namedTag.getInt("Data");
                namedTag.remove("Data");
            }

            namedTag.putInt("mData", data);
        }

//        if (!namedTag.contains("data")) {
//            if (namedTag.contains("mData")) {
//                namedTag.putInt("data", namedTag.getInt("mData"));
//                namedTag.remove("mData");
//            } else {
//                namedTag.putInt("data", 0);
//            }
//        }

        super.initBlockEntity();
    }

    @Override
    public boolean isBlockEntityValid() {
        int blockID = getBlock().getId();
        return blockID == Block.FLOWER_POT_BLOCK;
    }

    @Override
    public CompoundTag getSpawnCompound() {
        return new CompoundTag()
                .putString("id", BlockEntity.FLOWER_POT)
                .putInt("x", (int) this.x)
                .putInt("y", (int) this.y)
                .putInt("z", (int) this.z)
                .putShort("item", this.namedTag.getShort("item"))
                .putInt("mData", this.namedTag.getInt("data"));
    }

}
