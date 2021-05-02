package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treepacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.treepacks.JsonPropertyApplier;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.resources.JsonRegistryEntryReloadListener;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.ferreusveritas.dynamictrees.util.BiomePredicate;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class SpeciesManager extends JsonRegistryEntryReloadListener<Species> {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * A {@link JsonPropertyApplierList} for applying environment factors to {@link Species} objects.
     * (based on {@link net.minecraftforge.common.BiomeManager.BiomeType}).
     */
    private final JsonPropertyApplierList<Species> environmentFactorAppliers = new JsonPropertyApplierList<>(Species.class);

    public SpeciesManager() {
        super(Species.REGISTRY, JsonApplierRegistryEvent.SPECIES);
    }

    @Override
    public void registerAppliers() {
        BiomeDictionary.Type.getAll().stream().map(type -> new JsonPropertyApplier<>(type.toString().toLowerCase(), Species.class, Float.class, (species, factor) -> species.envFactor(type, factor)))
                .forEach(this.environmentFactorAppliers::register);

        JsonObjectGetters.register(Species.ICommonOverride.class, jsonElement -> {
            final ObjectFetchResult<BiomePredicate> biomePredicateFetchResult = JsonObjectGetters.BIOME_PREDICATE.get(jsonElement);

            if (!biomePredicateFetchResult.wasSuccessful())
                return ObjectFetchResult.failureFromOther(biomePredicateFetchResult);

            return ObjectFetchResult.success((world, pos) -> biomePredicateFetchResult.getValue().test(world.getBiome(pos)));
        });

        this.loadAppliers.register("generate_seed", Boolean.class, Species::setShouldGenerateSeed)
                .register("generate_sapling", Boolean.class, Species::setShouldGenerateSapling);

        this.reloadAppliers.register("tapering", Float.class, Species::setTapering)
                .register("up_probability", Integer.class, Species::setUpProbability)
                .register("lowest_branch_height", Integer.class, Species::setLowestBranchHeight)
                .register("signal_energy", Float.class, Species::setSignalEnergy)
                .register("growth_rate", Float.class, Species::setGrowthRate)
                .register("soil_longevity", Integer.class, Species::setSoilLongevity)
                .register("max_branch_radius", Integer.class, Species::setMaxBranchRadius)
                .register("transformable", Boolean.class, Species::setTransformable)
                .register("growth_logic_kit", GrowthLogicKit.class, Species::setGrowthLogicKit)
                .register("leaves_properties", LeavesProperties.class, Species::setLeavesProperties)
                .register("environment_factors", JsonObject.class, (species, jsonObject) ->
                        this.environmentFactorAppliers.applyAll(jsonObject, species).forEach(failureMessage -> LOGGER.warn("Error applying environment factor for species '{}': {}", species.getRegistryName(), failureMessage)))
                .register("seed_drop_rarity", Float.class, Species::setupStandardSeedDropping)
                .register("stick_drop_rarity", Float.class, Species::setupStandardStickDropping)
                .register("mega_species", ResourceLocation.class, (species, registryName) -> {
                    final ResourceLocation processedRegName = TreeRegistry.processResLoc(registryName);
                    Species.REGISTRY.runOnNextLock(Species.REGISTRY.generateIfValidRunnable(processedRegName, species::setMegaSpecies, () -> LOGGER.warn("Could not set mega species for '" + species + "' as Species '" + processedRegName + "' was not found.")));
                })
                .register("seed", Seed.class, Species::setSeed)
                .register("sapling_grows_naturally", Boolean.class, Species::setCanSaplingGrowNaturally)
                .register("sapling_sound", SoundType.class, Species::setSaplingSound)
                .register("sapling_shape", VoxelShape.class, Species::setSaplingShape)
                .register("primitive_sapling_item", Item.class, Species::addPrimitiveSaplingItem)
                .registerArrayApplier("primitive_sapling_items", Item.class, Species::addPrimitiveSaplingItem)
                .register("primitive_sapling", Block.class, (species, block) -> TreeRegistry.registerSaplingReplacer(block.defaultBlockState(), species))
                .registerArrayApplier("primitive_sapling", Block.class, (species, block) -> TreeRegistry.registerSaplingReplacer(block.defaultBlockState(), species))
                .register("common_override", Species.ICommonOverride.class, Species::setCommonOverride)
                .register("perfect_biomes", BiomeList.class, (species, biomeList) -> species.getPerfectBiomes().addAll(biomeList))
                .registerArrayApplier("acceptable_growth_blocks", Block.class, Species::addAcceptableBlockForGrowth)
                .registerArrayApplier("features", ConfiguredGenFeature.NULL_CONFIGURED_FEATURE.getClass(), Species::addGenFeature)
                .registerArrayApplier("acceptable_soils", String.class, (species, acceptableSoil) -> {
                    if (DirtHelper.getSoilFlags(acceptableSoil) == 0)
                        return PropertyApplierResult.failure("Could not find acceptable soil '" + acceptableSoil + "'.");

                    species.addAcceptableSoils(acceptableSoil);
                    return PropertyApplierResult.success();
                })
                .register("always_show_on_waila", Boolean.class, Species::alwaysShowOnWaila);

        super.registerAppliers();
    }

    @Override
    protected void postLoad(JsonObject jsonObject, Species species, Consumer<String> errorConsumer, Consumer<String> warningConsumer) {
        // Generates seeds and saplings if should.
        species.generateSeed().generateSapling();
    }

}