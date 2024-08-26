package com.prohitman.friendsmod.vc;

import com.prohitman.friendsmod.FriendsMod;
import com.prohitman.friendsmod.common.entity.MimicEntity;
import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.events.*;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;

@ForgeVoicechatPlugin
public class FriendsVoicePlugin implements VoicechatPlugin {
    public static final Map<UUID, EntityAudioChannel> mimicChannels = new HashMap<>();
    public static final Map<UUID, AudioPlayer> mimicPlayers = new HashMap<>();

    public static final Map<UUID, OpusEncoder> playerEncoders = new HashMap<>();
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

        if (event.getPacket().getOpusEncodedData().length <= 0) {
            return;
        }

        if (!(senderConnection.getPlayer().getPlayer() instanceof ServerPlayer)) {
            return;
        }

        if(event.getPacket().getOpusEncodedData().length != 0){
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
                                    System.out.println("Sent Decoded Data");
                                    mimic.setCurrentSound(decodedData);

                                    /*//if(player.getRandom().nextBoolean()){
                                        EntityAudioChannel channel;
                                        if (!FriendsVoicePlugin.mimicChannels.containsKey(mimic.getUUID())){
                                            UUID channelId = UUID.randomUUID();
                                            channel = event.getVoicechat().createEntityAudioChannel(channelId,
                                                    event.getVoicechat().fromEntity(mimic));
                                            FriendsVoicePlugin.mimicChannels.put(mimic.getUUID(), channel);
                                        } else {
                                            channel = FriendsVoicePlugin.mimicChannels.get(mimic.getUUID());
                                        }

                                        if(channel != null){
                                            channel.setCategory(FriendsVoicePlugin.MIMICING);
                                            channel.setDistance(45);

*//*                                            event.getVoicechat().sendStaticSoundPacketTo(event.getSenderConnection(),
                                                    event.getPacket().staticSoundPacketBuilder()
                                                            .channelId(channel.getId())
                                                            .opusEncodedData(event.getPacket().getOpusEncodedData())
                                                            .category(MIMICING)
                                                            .build());*//*

                                            event.getVoicechat().sendEntitySoundPacketTo(event.getSenderConnection(),
                                                    event.getPacket().entitySoundPacketBuilder()
                                                            .distance(45)
                                                            //.whispering(false)
                                                            .opusEncodedData(event.getPacket().getOpusEncodedData())
                                                            .category(MIMICING)
                                                            //.channelId(channel.getId())
                                                            .entityUuid(mimic.getUUID())
                                                            .build());
                                        }

                                    //}

                                    //mimic.vcApi = event.getVoicechat();
                                    //mimic.setCurrentSound(event.getPacket().getOpusEncodedData());
                                    //mimic.fromPlayer = player.getUUID();*/
                                }
                            }
                        }
                );
/*                OpusEncoder encoder = playerEncoders.get(player.getUUID());
                if(encoder == null){
                    return;
                }*/

            }
        }
    }

    @Override
    public void initialize(VoicechatApi api) {
        VoicechatPlugin.super.initialize(api);
        mimicChannels.clear();
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
        //if (!FriendsVoicePlugin.playerDecoders.containsKey(uuid)) {
            //WalkieMod.LOGGER.info("Couldn't find decoder... Adding decoder for UUID (" + uuid  + ")");
            FriendsVoicePlugin.playerDecoders.putIfAbsent(uuid, decoder);
        //}

        OpusEncoder encoder = FriendsVoicePlugin.playerEncoders.computeIfAbsent(uuid, k -> api.createEncoder());
        //if (!FriendsVoicePlugin.PLAYER_ENCODERS.containsKey(uuid)) {
            //WalkieMod.LOGGER.info("Couldn't find encoder... Adding encoder for UUID (" + uuid  + ")");
           FriendsVoicePlugin.playerEncoders.putIfAbsent(uuid, encoder);
        //}

        //RadioFilter radioFilter = new RadioFilter(3000, 50, 2600);
        //if (!WalkiePlugin.PLAYER_FILTERS.containsKey(uuid)) {
            //WalkieMod.LOGGER.info("Couldn't find filter... Adding filter for UUID (" + uuid  + ")");
         //   WalkiePlugin.PLAYER_FILTERS.putIfAbsent(uuid, radioFilter);
        //}
    }

    private void onPlayerDisconnected(PlayerDisconnectedEvent event) {
        UUID uuid = event.getPlayerUuid();

        OpusDecoder decoder = FriendsVoicePlugin.playerDecoders.getOrDefault(uuid, null);
        if (decoder != null) {
           // WalkieMod.LOGGER.info("Closing decoder for UUID (" + uuid + ").");
            decoder.close();
        } else {
           // WalkieMod.LOGGER.warn("Player decoder for UUID (" + uuid + ") did not close.");
        }

        FriendsVoicePlugin.playerDecoders.remove(uuid);

        OpusEncoder encoder = FriendsVoicePlugin.playerEncoders.getOrDefault(uuid, null);
        if (encoder != null) {
          //  WalkieMod.LOGGER.info("Closing encoder for UUID (" + uuid + ").");
            encoder.close();
        } else {
           // WalkieMod.LOGGER.warn("Player encoder for UUID (" + uuid + ") did not close.");
        }

        FriendsVoicePlugin.playerEncoders.remove(uuid);
       // WalkiePlugin.PLAYER_FILTERS.remove(uuid);
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
