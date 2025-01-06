package net.legacy.library.player.task.redis.impl;

import com.google.common.reflect.TypeToken;
import io.fairyproject.log.Log;
import net.legacy.library.commons.util.GsonUtil;
import net.legacy.library.player.annotation.RStreamAccepterRegister;
import net.legacy.library.player.service.LegacyPlayerDataService;
import net.legacy.library.player.task.redis.L1ToL2DataSyncTask;
import net.legacy.library.player.task.redis.RStreamAccepterInterface;
import org.apache.commons.lang3.tuple.Pair;
import org.redisson.api.RStream;
import org.redisson.api.StreamMessageId;

import java.util.Optional;
import java.util.UUID;

/**
 * @author qwq-dev
 * @since 2025-01-04 20:59
 */
@RStreamAccepterRegister
public class PlayerDataSyncUUIDRedisStreamAccept implements RStreamAccepterInterface {
    @Override
    public String getActionName() {
        return "player-data-sync-uuid";
    }

    public String getTargetLegacyPlayerDataServiceName() {
        return "player-data-service";
    }

    @Override
    public boolean isRecodeLimit() {
        return false;
    }

    @Override
    public void accept(RStream<Object, Object> rStream, StreamMessageId streamMessageId, Pair<String, String> data) {
        Object value = data.getValue();

        Pair<String, String> pair = GsonUtil.getGson().fromJson(
                value.toString(), new TypeToken<Pair<String, String>>() {
                }.getType()
        );

        String first = pair.getKey();
        String second = pair.getValue();

        Optional<LegacyPlayerDataService> legacyPlayerDataService =
                LegacyPlayerDataService.getLegacyPlayerDataService(first);

        legacyPlayerDataService.ifPresent(service -> L1ToL2DataSyncTask.of(UUID.fromString(second), service).start().getFuture().whenComplete((aVoid, throwable) -> {
            if (throwable != null) {
                Log.error("Error while syncing player data", throwable);
                return;
            }

            rStream.remove(streamMessageId);
        }));
    }
}