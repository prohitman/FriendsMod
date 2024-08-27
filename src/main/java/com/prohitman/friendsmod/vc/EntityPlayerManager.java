package com.prohitman.friendsmod.vc;

import com.prohitman.friendsmod.common.entity.MimicEntity;
import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class EntityPlayerManager {
    public static AudioFormat FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000F, 16, 1, 2, 48000F, false);

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

    @Nullable
    public UUID playEntitySound(VoicechatServerApi api, ServerLevel level, MimicEntity mimic, float distance, @Nullable String category) {
        UUID channelID = UUID.randomUUID();
        LocationalAudioChannel  channel = api.createLocationalAudioChannel(channelID, api.fromServerLevel(level), api.createPosition(mimic.getX(), mimic.getY(), mimic.getZ()));

        if (channel == null) {
            return null;
        }
        if (category != null) {
            channel.setCategory(category);
        }
        channel.setDistance(distance);
        api.getPlayersInRange(api.fromServerLevel(level), channel.getLocation(), distance + 1F, serverPlayer -> {
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

        players.put(channelID, new PlayerReference(() -> {
            synchronized (stopped) {
                stopped.set(true);
                de.maxhenkel.voicechat.api.audiochannel.AudioPlayer audioPlayer = player.get();
                if (audioPlayer != null) {
                    audioPlayer.stopPlaying();
                }
            }
        }, player));

        executor.execute(() -> {
            de.maxhenkel.voicechat.api.audiochannel.AudioPlayer audioPlayer = playChannel(api, channel, mimic);
            if (audioPlayer == null) {
                players.remove(channelID);
                return;
            }
            audioPlayer.setOnStopped(() -> {
                players.remove(channelID);
            });
            synchronized (stopped) {
                if (!stopped.get()) {
                    player.set(audioPlayer);
                } else {
                    audioPlayer.stopPlaying();
                }
            }
        });

        return channelID;
    }

    @Nullable
    private de.maxhenkel.voicechat.api.audiochannel.AudioPlayer playChannel(VoicechatServerApi api, AudioChannel channel, MimicEntity mimic) {
        try {
            short[] audio = AudioUtils.concatenateShortArrays(mimic.getCurrentSound());

            if(!mimic.isSoundClipped){
                audio = clipAudio(audio, mimic.getRandom());
                mimic.isSoundClipped = true;
            }

            mimic.setCurrentSound(List.of(audio));

            de.maxhenkel.voicechat.api.audiochannel.AudioPlayer player = api.createAudioPlayer(channel, api.createEncoder(), audio);
            player.startPlaying();
            return player;
        } catch (Exception e) {
            return null;
        }
    }

    public static short[] clipAudio(short[] audio, RandomSource random) {
        float lengthSeconds = getLengthSeconds(audio);

        if (lengthSeconds < 2.0f) {
            return audio;
        }

        int sampleRate = (int) FORMAT.getSampleRate();

        int minLengthSamples = 2 * sampleRate;
        int maxLengthSamples = 5 * sampleRate;

        int maxStartPos = audio.length - minLengthSamples;

        int startPos = random.nextInt(maxStartPos + 1);

        int clipLengthSamples = minLengthSamples + random.nextInt(Math.min(maxLengthSamples, audio.length - startPos) - minLengthSamples + 1);

        short[] clip = new short[clipLengthSamples];
        System.arraycopy(audio, startPos, clip, 0, clipLengthSamples);

        return clip;
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

    public static float getLengthSeconds(short[] audio) {
        return (float) audio.length / FORMAT.getSampleRate();
    }
}
