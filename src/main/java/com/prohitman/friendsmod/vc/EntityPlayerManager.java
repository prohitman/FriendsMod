package com.prohitman.friendsmod.vc;

import com.prohitman.friendsmod.common.entity.MimicEntity;
import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class EntityPlayerManager {

    private final Map<UUID, PlayerReference> players;
    private final ExecutorService executor;

    public EntityPlayerManager() {
        this.players = new ConcurrentHashMap<>();
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "MimicPlayerThread");
            thread.setDaemon(true);
            return thread;
        });
    }

/*    @Nullable
    public UUID playLocational(VoicechatServerApi api, ServerLevel level, Vec3 pos, UUID sound, @Nullable ServerPlayer p, float distance, @Nullable String category, int maxLengthSeconds) {
        return playLocational(api, level, pos, sound, p, distance, category, maxLengthSeconds, false);
    }*/

    @Nullable
    public UUID playEntitySound(VoicechatServerApi api, ServerLevel level, MimicEntity mimic, float distance, @Nullable String category) {
        EntityAudioChannel channel;
        if(!FriendsVoicePlugin.mimicChannels.containsKey(mimic.getUUID())){
            UUID channelID = UUID.randomUUID();
            channel = api.createEntityAudioChannel(channelID, api.fromEntity(mimic));
            System.out.println("Created Channel for: " + mimic.getUUID());
            FriendsVoicePlugin.mimicChannels.put(mimic.getUUID(), channel);
        } else {
            channel = FriendsVoicePlugin.mimicChannels.get(mimic.getUUID());
            System.out.println("Channel exists for: " + mimic.getUUID());

        }

        if (channel == null) {
            return null;
        }
        if (category != null) {
            channel.setCategory(category);
        }
        channel.setDistance(distance);
        api.getPlayersInRange(api.fromServerLevel(level), channel.getEntity().getPosition(), distance + 1F, serverPlayer -> {
            VoicechatConnection connection = api.getConnectionOf(serverPlayer);
            if (connection != null) {
                return connection.isDisabled();
            }

            return true;
        }).stream().map(Player::getPlayer).map(ServerPlayer.class::cast).forEach(player -> {
            player.displayClientMessage(Component.literal("You need to enable voice chat to hear custom audio"), true);
        });

        AtomicBoolean stopped = new AtomicBoolean();
        AtomicReference<de.maxhenkel.voicechat.api.audiochannel.AudioPlayer> player = new AtomicReference<>();

        players.put(channel.getId(), new PlayerReference(() -> {
            synchronized (stopped) {
                stopped.set(true);
                de.maxhenkel.voicechat.api.audiochannel.AudioPlayer audioPlayer = player.get();
                if (audioPlayer != null) {
                    System.out.println("Stopped playing for some other reason");

                    audioPlayer.stopPlaying();
                }
            }
        }, player));

        executor.execute(() -> {
            de.maxhenkel.voicechat.api.audiochannel.AudioPlayer audioPlayer = playChannel(api, channel, mimic);
            if (audioPlayer == null) {
                players.remove(channel.getId());
                return;
            }
            audioPlayer.setOnStopped(() -> {
                players.remove(channel.getId());
            });
            synchronized (stopped) {
                if (!stopped.get()) {
                    player.set(audioPlayer);
                } else {
                    System.out.println("Stopped playing for some reason");
                    audioPlayer.stopPlaying();
                }
            }
        });

        return channel.getId();
    }

    @Nullable
    private de.maxhenkel.voicechat.api.audiochannel.AudioPlayer playChannel(VoicechatServerApi api, AudioChannel channel, MimicEntity mimic) {
        try {
            short[] audio = mimic.getCurrentSound();

            if (audio.length == 0) {
                System.out.println("Audio is empty");
            }

            de.maxhenkel.voicechat.api.audiochannel.AudioPlayer player = api.createAudioPlayer(channel, api.createEncoder(), audio);
            player.startPlaying();
            System.out.println("Started to Play");
            return player;
        } catch (Exception e) {
            return null;
        }
    }

    public void stop(UUID channelID) {
        PlayerReference player = players.get(channelID);
        if (player != null) {
            player.onStop.stop();
        }
        players.remove(channelID);
    }

    public boolean isPlaying(UUID channelID) {
        PlayerReference player = players.get(channelID);
        if (player == null) {
            return false;
        }
        de.maxhenkel.voicechat.api.audiochannel.AudioPlayer p = player.player.get();
        if (p == null) {
            return true;
        }
        return p.isPlaying();
    }

    private static EntityPlayerManager instance;

    public static EntityPlayerManager instance() {
        if (instance == null) {
            instance = new EntityPlayerManager();
        }
        return instance;
    }

    private interface Stoppable {
        void stop();
    }

    private record PlayerReference(Stoppable onStop,
                                   AtomicReference<de.maxhenkel.voicechat.api.audiochannel.AudioPlayer> player) {
    }

/*    @Nullable
    public UUID findChannelID(UUID sound, boolean onlyByCommand) {
        for (Map.Entry<UUID, PlayerReference> entry : players.entrySet()) {
            if (entry.getValue().sound.equals(sound) && (entry.getValue().byCommand || !onlyByCommand)) {
                return entry.getKey();
            }
        }
        return null;
    }*/

}
