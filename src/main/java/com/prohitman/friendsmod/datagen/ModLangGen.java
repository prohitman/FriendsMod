package com.prohitman.friendsmod.datagen;

import com.prohitman.friendsmod.FriendsMod;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.function.Consumer;

public class ModLangGen extends LanguageProvider {
    private final Consumer<ModLangGen> languageProviderConsumer;

    public ModLangGen(PackOutput output, String locale, Consumer<ModLangGen> languageProviderConsumer) {
        super(output, FriendsMod.MODID, locale);
        this.languageProviderConsumer = languageProviderConsumer;
    }

    @Override
    protected void addTranslations() {
        languageProviderConsumer.accept(this);
    }
}
