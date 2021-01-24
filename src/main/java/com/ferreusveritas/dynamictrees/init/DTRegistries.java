package com.ferreusveritas.dynamictrees.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.BonsaiPotBlock;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.CocoaFruitBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesPaging;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SpreadableRootyBlock;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.entities.LingeringEffectorEntity;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.tileentity.BonsaiTileEntity;
import com.ferreusveritas.dynamictrees.tileentity.SpeciesTileEntity;

import com.ferreusveritas.dynamictrees.worldgen.DynamicTreeFeature;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTRegistries {
	
	///////////////////////////////////////////
	// BLOCKS
	///////////////////////////////////////////

	public static FruitBlock appleBlock;
	public static CocoaFruitBlock cocoaFruitBlock;
	public static BonsaiPotBlock bonsaiPotBlock;
	public static TrunkShellBlock trunkShellBlock;
	
	public static Map<String, ILeavesProperties> leaves = new HashMap<>();
	
	public static final CommonBlockStates blockStates = new CommonBlockStates();
	
	public static void setupBlocks() {
		bonsaiPotBlock = new BonsaiPotBlock();//Bonsai Pot
		cocoaFruitBlock = new CocoaFruitBlock();//Modified Cocoa pods
		appleBlock = new FruitBlock().setDroppedItem(new ItemStack(Items.APPLE));//Apple
		trunkShellBlock = new TrunkShellBlock();

		DirtHelper.registerSoil(Blocks.GRASS_BLOCK, DirtHelper.DIRTLIKE);
		DirtHelper.registerSoil(Blocks.MYCELIUM, DirtHelper.DIRTLIKE);
		DirtHelper.registerSoil(Blocks.DIRT, DirtHelper.DIRTLIKE, new SpreadableRootyBlock(Blocks.DIRT, 9, Blocks.GRASS_BLOCK, Blocks.MYCELIUM));
		DirtHelper.registerSoil(Blocks.COARSE_DIRT, DirtHelper.DIRTLIKE);
		DirtHelper.registerSoil(Blocks.PODZOL, DirtHelper.DIRTLIKE);
		DirtHelper.registerSoil(Blocks.FARMLAND, DirtHelper.DIRTLIKE, Blocks.DIRT);
		DirtHelper.registerSoil(Blocks.SAND, DirtHelper.SANDLIKE);
		DirtHelper.registerSoil(Blocks.RED_SAND, DirtHelper.SANDLIKE);
//		DirtHelper.registerSoil(Blocks.GRAVEL, DirtHelper.GRAVELLIKE);
		DirtHelper.registerSoil(Blocks.WATER, DirtHelper.WATERLIKE);
//		DirtHelper.registerSoil(Blocks.TERRACOTTA, DirtHelper.HARDCLAYLIKE);
//		DirtHelper.registerSoil(Blocks.WHITE_TERRACOTTA, DirtHelper.HARDCLAYLIKE);
		DirtHelper.registerSoil(Blocks.MYCELIUM, DirtHelper.FUNGUSLIKE);
		DirtHelper.registerSoil(Blocks.CRIMSON_NYLIUM, DirtHelper.FUNGUSLIKE);
		DirtHelper.registerSoil(Blocks.WARPED_NYLIUM, DirtHelper.FUNGUSLIKE);
		DirtHelper.registerSoil(Blocks.NETHERRACK, DirtHelper.NETHERLIKE, new SpreadableRootyBlock(Blocks.NETHERRACK, Items.BONE_MEAL, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM));
		DirtHelper.registerSoil(Blocks.SOUL_SAND, DirtHelper.NETHERLIKE);
		DirtHelper.registerSoil(Blocks.SOUL_SOIL, DirtHelper.NETHERLIKE);
		DirtHelper.registerSoil(Blocks.CRIMSON_NYLIUM, DirtHelper.NETHERLIKE);
		DirtHelper.registerSoil(Blocks.WARPED_NYLIUM, DirtHelper.NETHERLIKE);
		DirtHelper.registerSoil(Blocks.SOUL_SOIL, DirtHelper.NETHERSOILLIKE);
		DirtHelper.registerSoil(Blocks.CRIMSON_NYLIUM, DirtHelper.NETHERSOILLIKE);
		DirtHelper.registerSoil(Blocks.WARPED_NYLIUM, DirtHelper.NETHERSOILLIKE);
		DirtHelper.registerSoil(Blocks.END_STONE, DirtHelper.ENDLIKE);

		setupLeavesProperties();
	}
	
	public static void setupLeavesProperties() {
		leaves = LeavesPaging.build(new ResourceLocation(DynamicTrees.MODID, "leaves/common.json"));
		leaves.put("cactus", new LeavesProperties(null, ItemStack.EMPTY, TreeRegistry.findCellKit("bare")));//Explicitly unbuilt since there's no leaves
	}
	
	@SubscribeEvent
	public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
		IForgeRegistry<Block> registry = blockRegistryEvent.getRegistry();
		
		ArrayList<Block> treeBlocks = new ArrayList<Block>();
		DTTrees.baseFamilies.forEach(tree -> tree.getRegisterableBlocks(treeBlocks));
		DTTrees.dynamicCactus.getRegisterableBlocks(treeBlocks);
		treeBlocks.addAll(LeavesPaging.getLeavesListForModId(DynamicTrees.MODID));

		registry.registerAll(bonsaiPotBlock, cocoaFruitBlock, appleBlock, trunkShellBlock);
		
		registry.registerAll(treeBlocks.toArray(new Block[0]));

		for (RootyBlock rooty : RootyBlockHelper.generateListForRegistry(false)){
			registry.register(rooty);
		}
	}
	
	///////////////////////////////////////////
	// ITEMS
	///////////////////////////////////////////
	
	public static DendroPotion dendroPotion;
	public static DirtBucket dirtBucket;
	public static Staff treeStaff;
	
	public static void setupItems() {
		dendroPotion = new DendroPotion();//Potions
		dirtBucket = new DirtBucket();//Dirt Bucket
		treeStaff = new Staff();//Creative Mode Staff
	}
	
	@SubscribeEvent
	public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
		IForgeRegistry<Item> registry = itemRegistryEvent.getRegistry();
		
		ArrayList<Item> treeItems = new ArrayList<>();
		DTTrees.baseFamilies.forEach(tree -> tree.getRegisterableItems(treeItems));
		DTTrees.dynamicCactus.getRegisterableItems(treeItems);
		
		registry.registerAll(dendroPotion, dirtBucket, treeStaff);
		registry.registerAll(treeItems.toArray(new Item[0]));
	}
	
	///////////////////////////////////////////
	// ENTITIES
	///////////////////////////////////////////
	
	public final static String FALLING_TREE = "falling_tree";
	public final static String LINGERING_EFFECTOR = "lingering_effector";
	
	public static EntityType<EntityFallingTree> fallingTree;
	public static EntityType<LingeringEffectorEntity> lingeringEffector;

	public static void setupEntities() {
		fallingTree = EntityType.Builder.create(EntityFallingTree::new, EntityClassification.MISC)
				.setShouldReceiveVelocityUpdates(true)
				.setTrackingRange(512)
				.setUpdateInterval(Integer.MAX_VALUE)
				.setCustomClientFactory((spawnEntity, world) -> new EntityFallingTree(fallingTree, world))
				.build(FALLING_TREE);

		lingeringEffector = EntityType.Builder.<LingeringEffectorEntity>create(LingeringEffectorEntity::new, EntityClassification.MISC)
				.build(LINGERING_EFFECTOR);
	}
	
	@SubscribeEvent
	public static void onEntitiesRegistry(final RegistryEvent.Register<EntityType<?>> entityRegistryEvent) {
		setupEntities();
		
		IForgeRegistry<EntityType<?>> registry = entityRegistryEvent.getRegistry();
		
		registry.registerAll(fallingTree.setRegistryName(new ResourceLocation(DynamicTrees.MODID, FALLING_TREE)),
				lingeringEffector.setRegistryName(new ResourceLocation(DynamicTrees.MODID, LINGERING_EFFECTOR)));
	}
	
	///////////////////////////////////////////
	// TILE ENTITIES
	///////////////////////////////////////////
	
	public static TileEntityType<SpeciesTileEntity> speciesTE;
	public static TileEntityType<BonsaiTileEntity> bonsaiTE;
	
	public static void setupTileEntities() {
		LinkedList<RootyBlock> rootyDirts = RootyBlockHelper.generateListForRegistry(false);
		speciesTE = TileEntityType.Builder.create(SpeciesTileEntity::new, rootyDirts.toArray(new RootyBlock[0])).build(null);
		bonsaiTE = TileEntityType.Builder.create(BonsaiTileEntity::new, bonsaiPotBlock).build(null);
	}
	
	@SubscribeEvent
	public static void onTileEntitiesRegistry(final RegistryEvent.Register<TileEntityType<?>> tileEntityRegistryEvent) {
		setupTileEntities();
		
		tileEntityRegistryEvent.getRegistry().register(bonsaiTE.setRegistryName(bonsaiPotBlock.getRegistryName()));
		tileEntityRegistryEvent.getRegistry().register(speciesTE.setRegistryName(new ResourceLocation(DynamicTrees.MODID, "tile_entity_species")));
	}
	
	///////////////////////////////////////////
	// MISC
	///////////////////////////////////////////

	/** This is the creative tab that holds all DT items */
	public static final ItemGroup dynamicTreesTab = new ItemGroup("dynamictrees") {
		@Override
		public ItemStack createIcon() {
			return TreeRegistry.findSpeciesSloppy(DynamicTrees.VanillaWoodTypes.oak.toString()).getSeedStack(1);
		}
	};
	
	public static final class CommonBlockStates {
		public final BlockState air = Blocks.AIR.getDefaultState();
		public final BlockState dirt = Blocks.DIRT.getDefaultState();
		public final BlockState coarsedirt = Blocks.COARSE_DIRT.getDefaultState();
		public final BlockState sand = Blocks.SAND.getDefaultState();
		public final BlockState grass = Blocks.GRASS.getDefaultState();
		public final BlockState podzol = Blocks.PODZOL.getDefaultState();
		public final BlockState redMushroom = Blocks.RED_MUSHROOM.getDefaultState();
		public final BlockState brownMushroom = Blocks.BROWN_MUSHROOM.getDefaultState();
	}

	public static final DynamicTreeFeature DYNAMIC_TREE_FEATURE = new DynamicTreeFeature();

	@SubscribeEvent
	public static void onFeatureRegistry (final RegistryEvent.Register<Feature<?>> event) {
		event.getRegistry().register(DYNAMIC_TREE_FEATURE);
	}
	
}