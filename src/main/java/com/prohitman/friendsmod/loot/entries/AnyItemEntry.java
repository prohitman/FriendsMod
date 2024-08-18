package com.prohitman.friendsmod.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.prohitman.friendsmod.loot.ModLootPoolEntryTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class AnyItemEntry extends LootPoolSingletonContainer {
    public static final MapCodec<AnyItemEntry> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(ItemPredicate.CODEC.listOf().optionalFieldOf("filters", List.of()).forGetter(p_298012_ -> p_298012_.filters))
                    .and(singletonFields(instance))
                    .apply(instance, AnyItemEntry::new)
    );

    private final List<ItemPredicate> filters;
    private List<Holder.Reference<Item>> cache;

    protected AnyItemEntry(List<ItemPredicate> filters, int pWeight, int pQuality, List<LootItemCondition> pConditions, List<LootItemFunction> pFunctions) {
        super(pWeight, pQuality, pConditions, pFunctions);
        this.filters = filters;
    }

    @Override
    protected void createItemStack(@NotNull Consumer<ItemStack> pStackConsumer, @NotNull LootContext pLootContext) {
        if (cache == null) {
            cache = BuiltInRegistries.ITEM.holders()
                    .filter(item -> item.value() != Items.AIR && filters.stream().allMatch(filter -> filter.test(new ItemStack(item))))
                    .collect(ObjectArrayList.toList());
        } else if (cache.isEmpty()) {
            return;
        }

        pStackConsumer.accept(new ItemStack(Util.getRandom(cache, pLootContext.getRandom())));
    }

    @Override
    public LootPoolEntryType getType() {
        return ModLootPoolEntryTypes.ANY_ITEM.get();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends LootPoolSingletonContainer.Builder<Builder> {
        private final List<ItemPredicate> filters = new ObjectArrayList<>();

        @Override
        protected @NotNull Builder getThis() {
            return this;
        }

        public Builder filter(ItemPredicate predicate) {
            filters.add(predicate);
            return this;
        }

        public List<ItemPredicate> getFilters() {
            return filters;
        }

        @Override
        public @NotNull LootPoolEntryContainer build() {
            return new AnyItemEntry( getFilters(), weight, quality, getConditions(), getFunctions());
        }
    }
}
