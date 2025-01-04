package net.legacy.library.player.task;

import com.github.benmanes.caffeine.cache.Cache;
import de.leonhard.storage.internal.serialize.SimplixSerializer;
import dev.morphia.Datastore;
import io.fairyproject.scheduler.ScheduledTask;
import lombok.RequiredArgsConstructor;
import net.legacy.library.cache.model.LockSettings;
import net.legacy.library.cache.service.CacheServiceInterface;
import net.legacy.library.cache.service.redis.RedisCacheServiceInterface;
import net.legacy.library.commons.task.TaskInterface;
import net.legacy.library.player.model.LegacyPlayerData;
import net.legacy.library.player.service.LegacyPlayerDataService;
import net.legacy.library.player.util.KeyUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;

import java.time.Duration;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author qwq-dev
 * @since 2025-01-04 12:53
 */
@RequiredArgsConstructor
public class PlayerDataPersistenceTask implements TaskInterface {
    private final Duration delay;
    private final Duration interval;
    private final LockSettings lockSettings;
    private final LegacyPlayerDataService legacyPlayerDataService;

    public static PlayerDataPersistenceTask of(Duration delay, Duration interval, LockSettings lockSettings, LegacyPlayerDataService legacyPlayerDataService) {
        return new PlayerDataPersistenceTask(delay, interval, lockSettings, legacyPlayerDataService);
    }

    @Override
    public ScheduledTask<?> start() {
        Runnable runnable = () -> {
            CacheServiceInterface<Cache<String, LegacyPlayerData>, LegacyPlayerData> l1Cache =
                    legacyPlayerDataService.getL1Cache();
            RedisCacheServiceInterface l2Cache = legacyPlayerDataService.getL2Cache();
            RedissonClient redissonClient = l2Cache.getCache();

            // Sync L1 cache to L2 cache
            l1Cache.getCache().asMap().forEach((key, legacyPlayerData) -> {
                UUID uuid = legacyPlayerData.getUuid();
                String serialized = SimplixSerializer.serialize(legacyPlayerData).toString();
                String bucketKey = KeyUtil.getLegacyPlayerDataServiceKey(uuid, legacyPlayerDataService, "bucket-key");
                String syncLockKey = KeyUtil.getLegacyPlayerDataServiceKey(uuid, legacyPlayerDataService, "persistence-l1-sync");

                l2Cache.execute(
                        client -> client.getLock(syncLockKey),
                        client -> {
                            client.getBucket(bucketKey).set(serialized);
                            return null;
                        },
                        LockSettings.of(0, 0, TimeUnit.MILLISECONDS)
                );
            });

            String lockKey = KeyUtil.getLegacyPlayerDataServiceKey(legacyPlayerDataService) + "-persistence-lock";
            RLock lock = redissonClient.getLock(lockKey);

            try {
                if (!lock.tryLock(lockSettings.getWaitTime(), lockSettings.getLeaseTime(), lockSettings.getTimeUnit())) {
                    throw new RuntimeException("Could not acquire lock: " + lock.getName());
                }

                try {
                    Datastore datastore = legacyPlayerDataService.getMongoDBConnectionConfig().getDatastore();

                    // Get all LPDS key (name + "-lpds-*")
                    Iterator<String> keys =
                            redissonClient.getKeys().getKeys(
                                    KeysScanOptions.defaults().pattern(KeyUtil.getLegacyPlayerDataServiceKey(legacyPlayerDataService))
                            ).iterator();

                    // Deserialize and save all LPD
                    while (keys.hasNext()) {
                        LegacyPlayerData legacyPlayerData = l2Cache.getWithType(
                                client -> SimplixSerializer.deserialize(client.getBucket(keys.next()).get().toString(), LegacyPlayerData.class), () -> null, null, false
                        );
                        datastore.save(legacyPlayerData);
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while trying to acquire lock.", exception);
            } catch (Exception exception) {
                throw new RuntimeException("Unexpected error during legacy player data migration.", exception);
            }
        };

        return scheduleAtFixedRate(runnable, delay, interval);
    }
}
