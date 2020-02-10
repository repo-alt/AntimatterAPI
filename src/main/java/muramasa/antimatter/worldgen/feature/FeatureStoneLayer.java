package muramasa.antimatter.worldgen.feature;

import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.worldgen.*;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;

public class FeatureStoneLayer extends Feature<NoFeatureConfig> implements IAntimatterFeature {

    public FeatureStoneLayer() {
        super(NoFeatureConfig::deserialize);
        AntimatterAPI.register(IAntimatterFeature.class, this);
    }

    @Override
    public String getId() {
        return "feature_stone_layer";
    }

    @Override
    public void init() {
        for (Biome biome : ForgeRegistries.BIOMES) {
            biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, new ConfiguredFeature<>(this, IFeatureConfig.NO_FEATURE_CONFIG));
        }
    }

    @Override
    public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {

        List<StoneLayer> stones = AntimatterWorldGenerator.STONE_LAYERS;
        StoneLayer[] layers = new StoneLayer[7];
        NoiseGenerator noise = new NoiseGenerator(world);
        int stonesSize = stones.size(), stonesMax = stonesSize - 1;

        BlockState existing;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                int tX = pos.getX() + i, tZ = pos.getZ() + j;

                layers[0] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, -2, tZ) + 1) / 2) * stonesSize)));
                layers[1] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, -1, tZ) + 1) / 2) * stonesSize)));
                layers[2] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, 0, tZ) + 1) / 2) * stonesSize)));
                layers[3] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, 1, tZ) + 1) / 2) * stonesSize)));
                layers[4] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, 2, tZ) + 1) / 2) * stonesSize)));
                layers[5] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, 3, tZ) + 1) / 2) * stonesSize)));
                layers[6] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, 4, tZ) + 1) / 2) * stonesSize)));

                //if (layers[3].getType().getState() == Blocks.STONE.getDefaultState()) continue;

                int maxHeight = world.getHeight(Heightmap.Type.WORLD_SURFACE_WG, pos.add(i, 0, j)).getY();
                for (int tY = 1; tY < maxHeight; tY++) {
                    existing = world.getBlockState(pos.add(i, tY, j));

                    boolean shouldPlaceStone = true;
                    if (layers[1] == layers[5]) {
                        for (StoneLayerOre ore : layers[3].getOres()) {
                            if (ore.canPlace(pos.add(i, tY, j), rand) && WorldGenHelper.setOre(world, pos.add(i, tY, j), existing, layers[0] == layers[6] ? ore.getState() : ore.getStateSmall())) {
                                shouldPlaceStone = false;
                                break;
                            }
                        }
                    } else {
//                        for (StoneLayerOre ore : x) {
//                            if (WorldGenHelper.setOre(world, pos.add(i, tY, j), existing, ore.material, MaterialType.ORE)) {
//                                shouldPlaceStone = false;
//                                break;
//                            }
//                        }
                    }

                    //If we haven't placed an ore, and not trying to set the same state as existing
                    if (shouldPlaceStone && existing != layers[3].getStoneState()) {
                        WorldGenHelper.setStone(world, pos.add(i, tY, j), existing, layers[3].getStoneState());
                    }

                    // And scan for next Block on the Stone Layer Type.
                    for (int t = 1; t < layers.length; t++) {
                        layers[t - 1] = layers[t];
                    }
                    layers[6] = stones.get(Math.min(stonesMax, (int)(((noise.get(tX, tY + 4, tZ) + 1) / 2) * stonesSize)));
                }
            }
        }
        return true;
    }
}
