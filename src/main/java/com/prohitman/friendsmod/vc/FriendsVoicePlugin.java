package com.prohitman.friendsmod.vc;

import com.prohitman.friendsmod.FriendsMod;
import com.prohitman.friendsmod.common.entity.MimicEntity;
import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;

@ForgeVoicechatPlugin
public class FriendsVoicePlugin implements VoicechatPlugin {
    public static final Map<UUID, EntityAudioChannel> mimicChannels = new HashMap<>();
    public static final Map<UUID, UUID> mimicToPlayerListener = new HashMap<>();
    public static final Map<UUID, OpusDecoder> playerDecoders = new HashMap<>();
    public static final String MIMICING = "mimicing";
    @Nullable
    public static VoicechatServerApi voiceApi;
    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted, 5);
        registration.registerEvent(PlayerConnectedEvent.class, this::onPlayerConnected, 3);
        registration.registerEvent(PlayerDisconnectedEvent.class, this::onPlayerDisconnected, 3);
        registration.registerEvent(MicrophonePacketEvent.class, this::micPacketEvent, 2);
    }

    public void micPacketEvent(MicrophonePacketEvent event){
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) {
            return;
        }

        if (!(senderConnection.getPlayer().getPlayer() instanceof ServerPlayer)) {
            return;
        }

        if(event.getSenderConnection().getPlayer().getPlayer() instanceof ServerPlayer player){
            OpusDecoder decoder = playerDecoders.get(player.getUUID());
            if(decoder == null){
                return;
            }
            byte[] encodedData = event.getPacket().getOpusEncodedData();
            if(encodedData.length == 0){
                decoder.resetState();
            }

            short[] decodedData = decoder.decode(encodedData);

            player.level().getServer().execute(() ->
                {
                    List<MimicEntity> mimics;
                    mimics = player.level().getEntitiesOfClass(MimicEntity.class, player.getBoundingBox().inflate(16));
                    if(!mimics.isEmpty()){
                        for(MimicEntity mimic : mimics){
                            if (event.getPacket().getOpusEncodedData().length <= 0) {
                                if(mimicToPlayerListener.get(mimic.getUUID()) == player.getUUID()){
                                    mimic.canResetSound = true;
                                    mimicToPlayerListener.remove(mimic.getUUID());
                                }

                            } else {
                                if(!mimicToPlayerListener.containsKey(mimic.getUUID())){
                                    mimicToPlayerListener.put(mimic.getUUID(), player.getUUID());
                                } else if(mimicToPlayerListener.get(mimic.getUUID()) != player.getUUID()){
                                    return;
                                }

                                if(mimic.canResetSound){
                                    mimic.setCurrentSound(null);
                                }

                                mimic.canResetSound = false;

                                if(mimic.getCurrentSound() == null){
                                    List<short[]> list = new ArrayList<>();

                                    list.add(decodedData);
                                    mimic.setCurrentSound(list);
                                    mimic.isSoundClipped = false;
                                } else {
                                    mimic.getCurrentSound().add(decodedData);
                                    mimic.isSoundClipped = false;
                                }
                            }
                        }
                    }
                }
            );
        }
    }

    @Override
    public void initialize(VoicechatApi api) {
        VoicechatPlugin.super.initialize(api);
        mimicChannels.clear();
        mimicToPlayerListener.clear();
    }

    @Override
    public String getPluginId() {
        return FriendsMod.MODID;
    }

    private void onPlayerConnected(PlayerConnectedEvent event) {
        VoicechatServerApi api = event.getVoicechat();
        VoicechatConnection connection = event.getConnection();
        de.maxhenkel.voicechat.api.ServerPlayer player = connection.getPlayer();
        UUID uuid = player.getUuid();

        OpusDecoder decoder = FriendsVoicePlugin.playerDecoders.computeIfAbsent(uuid, k -> api.createDecoder());
        FriendsVoicePlugin.playerDecoders.putIfAbsent(uuid, decoder);
    }

    private void onPlayerDisconnected(PlayerDisconnectedEvent event) {
        UUID uuid = event.getPlayerUuid();

        OpusDecoder decoder = FriendsVoicePlugin.playerDecoders.getOrDefault(uuid, null);
        if (decoder != null) {
            decoder.close();
        }

        FriendsVoicePlugin.playerDecoders.remove(uuid);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        VoicechatServerApi api = event.getVoicechat();
        voiceApi = api;
        VolumeCategory mimicing = api.volumeCategoryBuilder()
                .setId(MIMICING)
                .setName("Mimicing")
                .setDescription("The Volume of the Mimic Sounds")
                .setIcon(getIcon("mimicing.png"))
                .build();

        api.registerVolumeCategory(mimicing);
    }

    @Nullable
    private int[][] getIcon(String path) {
        try {
            Enumeration<URL> resources = FriendsVoicePlugin.class.getClassLoader().getResources(path);
            while (resources.hasMoreElements()) {
                BufferedImage bufferedImage = ImageIO.read(resources.nextElement().openStream());
                if (bufferedImage.getWidth() != 16) {
                    continue;
                }
                if (bufferedImage.getHeight() != 16) {
                    continue;
                }
                int[][] image = new int[16][16];
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    for (int y = 0; y < bufferedImage.getHeight(); y++) {
                        image[x][y] = bufferedImage.getRGB(x, y);
                    }
                }
                return image;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
