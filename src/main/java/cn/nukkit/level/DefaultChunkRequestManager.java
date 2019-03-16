package cn.nukkit.level;

import cn.nukkit.Player;
import cn.nukkit.level.format.ChunkRequestTask;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.scheduler.AsyncTask;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author CreeperFace
 */
public class DefaultChunkRequestManager implements ChunkRequestManager {

    private final Level level;

    private final Map<Long, Map<Integer, Player>> chunkSendQueue = new HashMap<>();
    private final Map<Long, Boolean> chunkSendTasks = new HashMap<>();

    private final ConcurrentMap<Long, DataPacket> chunkCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .<Long, DataPacket>build().asMap();

    public DefaultChunkRequestManager(Level level) {
        this.level = level;
    }

    @Override
    public void process() {
        if (!this.chunkSendQueue.isEmpty()) {
            this.level.timings.syncChunkSendTimer.startTiming();
            for (Map.Entry<Long, Map<Integer, Player>> entry : new ArrayList<>(this.chunkSendQueue.entrySet())) {
                Long index = entry.getKey();

                if (this.chunkSendTasks.containsKey(index)) {
                    continue;
                }
                int x = Level.getHashX(index);
                int z = Level.getHashZ(index);
                this.chunkSendTasks.put(index, true);
                if (this.chunkCache.containsKey(index)) {
                    this.sendChunkFromCache(x, z);
                    continue;
                }
                this.level.timings.syncChunkSendPrepareTimer.startTiming();
                AsyncTask task = this.level.getProvider().requestChunkTask(x, z);
                if (task != null) {
                    this.level.getServer().getScheduler().scheduleAsyncTask(task);
                }
                this.level.timings.syncChunkSendPrepareTimer.stopTiming();
            }
            this.level.timings.syncChunkSendTimer.stopTiming();
        }
    }

    private void sendChunkFromCache(int x, int z) {
        Long index = Level.chunkHash(x, z);
        if (this.chunkSendTasks.containsKey(index)) {
            for (Player player : this.chunkSendQueue.get(index).values()) {
                if (player.isConnected() && player.usedChunks.containsKey(index)) {
                    DataPacket pk = this.chunkCache.get(index);

                    player.sendChunk(x, z, pk);
                }
            }

            this.chunkSendQueue.remove(index);
            this.chunkSendTasks.remove(index);
        }
    }

    @Override
    public void requestChunk(long index, Player player) {
        if (!this.chunkSendQueue.containsKey(index)) {
            this.chunkSendQueue.put(index, new HashMap<>());
        }

        this.chunkSendQueue.get(index).put(player.getLoaderId(), player);
    }

    @Override
    public void requestCallback(ChunkRequestTask.Result result) {
        this.level.timings.syncChunkSendTimer.startTiming();

        int x = result.x;
        int z = result.z;
        DataPacket payload = result.payload;

        Long index = Level.chunkHash(x, z);

        if (this.level.isCacheChunks() && !this.chunkCache.containsKey(index)) {
            this.chunkCache.put(index, payload);
            this.sendChunkFromCache(x, z);
            this.level.timings.syncChunkSendTimer.stopTiming();
            return;
        }

        if (this.chunkSendTasks.containsKey(index)) {
            for (Player player : this.chunkSendQueue.get(index).values()) {
                if (player.isConnected() && player.usedChunks.containsKey(index)) {
                    player.sendChunk(x, z, payload);
                }
            }

            this.chunkSendQueue.remove(index);
            this.chunkSendTasks.remove(index);
        }
        this.level.timings.syncChunkSendTimer.stopTiming();
    }

    @Override
    public void onChunkChanged(long index) {
        this.chunkCache.remove(index);
    }

    @Override
    public void clearCache(boolean full) {
        if (full) {
            this.chunkCache.clear();
        } else {
            if (this.chunkCache.size() > 2048) {
                this.chunkCache.clear();
            }
        }
    }
}
