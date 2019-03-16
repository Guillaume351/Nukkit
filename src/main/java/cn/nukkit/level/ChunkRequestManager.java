package cn.nukkit.level;

import cn.nukkit.Player;
import cn.nukkit.level.format.ChunkRequestTask;

/**
 * @author CreeperFace
 */
public interface ChunkRequestManager {

    void process();

    void requestChunk(long index, Player player);

    default void onChunkChanged(int x, int z) {
        onChunkChanged(Level.chunkHash(x, z));
    }

    void onChunkChanged(long index);

    default void clearCache() {
        clearCache(false);
    }

    void clearCache(boolean full);

    default void requestCallback(ChunkRequestTask.Result task) {

    }
}
