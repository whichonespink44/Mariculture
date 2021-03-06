package joshie.mariculture.modules.abyssal;

import joshie.mariculture.modules.EventContainer;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkProviderOverworld;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

import static joshie.mariculture.modules.abyssal.Abyssal.OCEAN_FILLER_DEPTH;

@EventContainer(modules = "abyssal")
public class SandyOcean {
    private static final IBlockState AIR = Blocks.AIR.getDefaultState();
    private static final IBlockState STONE = Blocks.STONE.getDefaultState();
    private static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
    private static final IBlockState ICE = Blocks.ICE.getDefaultState();
    private static final IBlockState WATER = Blocks.WATER.getDefaultState();
    private static final IBlockState SANDSTONE = Blocks.SANDSTONE.getDefaultState();
    private static final IBlockState RED_SANDSTONE = Blocks.RED_SANDSTONE.getDefaultState();

    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        int i = worldIn.getSeaLevel();
        IBlockState iblockstate = Abyssal.OCEAN_SURFACE;
        IBlockState iblockstate1 = Abyssal.OCEAN_FILLER;
        int j = -1;
        int k = (int) (noiseVal / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
        int l = x & 15;
        int i1 = z & 15;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        for (int j1 = 255; j1 >= 0; --j1) {
            if (j1 <= rand.nextInt(5)) {
                chunkPrimerIn.setBlockState(i1, j1, l, BEDROCK);
            } else {
                IBlockState iblockstate2 = chunkPrimerIn.getBlockState(i1, j1, l);

                if (iblockstate2.getMaterial() == Material.AIR) {
                    j = -1;
                } else if (iblockstate2.getBlock() == Blocks.STONE) {
                    if (j == -1) {
                        if (k <= 0) {
                            iblockstate = AIR;
                            iblockstate1 = STONE;
                        } else if (j1 >= i - 4 && j1 <= i + 1) {
                            iblockstate = Abyssal.OCEAN_SURFACE;
                            iblockstate1 = Abyssal.OCEAN_FILLER;
                        }

                        if (j1 < i && (iblockstate == null || iblockstate.getMaterial() == Material.AIR)) {
                            if (Biomes.OCEAN.getFloatTemperature(blockpos$mutableblockpos.setPos(x, j1, z)) < 0.15F) {
                                iblockstate = ICE;
                            } else {
                                iblockstate = WATER;
                            }
                        }

                        j = k;

                        if (j1 >= i - OCEAN_FILLER_DEPTH) {
                            chunkPrimerIn.setBlockState(i1, j1, l, iblockstate);
                        } else if (j1 < i - k + 1) {
                            iblockstate = AIR;
                            iblockstate1 = Abyssal.OCEAN_FILLER;
                            chunkPrimerIn.setBlockState(i1, j1, l, Abyssal.OCEAN_SURFACE);
                        } else {
                            chunkPrimerIn.setBlockState(i1, j1, l, iblockstate1);
                        }
                    } else if (j > 0) {
                        --j;
                        chunkPrimerIn.setBlockState(i1, j1, l, iblockstate1);

                        if (j == 0 && iblockstate1.getBlock() == Blocks.SAND) {
                            j = rand.nextInt(4) + Math.max(0, j1 - 63);
                            iblockstate1 = iblockstate1.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND ? RED_SANDSTONE : SANDSTONE;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onChunkReplaceBlocks(ChunkGeneratorEvent.ReplaceBiomeBlocks event) {
        if (Abyssal.OCEAN_REPLACE) {
            if (event.getGen() instanceof ChunkProviderOverworld) {
                ChunkProviderOverworld overworld = ((ChunkProviderOverworld) event.getGen());
                double d0 = 0.03125D;
                overworld.depthBuffer = overworld.surfaceNoise.getRegion(overworld.depthBuffer, (double) (event.getX() * 16), (double) (event.getZ() * 16), 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);

                for (int i = 0; i < 16; ++i) {
                    for (int j = 0; j < 16; ++j) {
                        Biome biome = overworld.biomesForGeneration[j + i * 16];
                        if (biome == Biomes.OCEAN || biome == Biomes.DEEP_OCEAN || biome == Biomes.FROZEN_OCEAN || biome == Biomes.BEACH) {
                            genTerrainBlocks(overworld.worldObj, overworld.rand, event.getPrimer(), event.getX() * 16 + i, event.getZ() * 16 + j, overworld.depthBuffer[j + i * 16]);
                        } else
                            biome.genTerrainBlocks(overworld.worldObj, overworld.rand, event.getPrimer(), event.getX() * 16 + i, event.getZ() * 16 + j, overworld.depthBuffer[j + i * 16]);
                    }
                }

                //We processed it ourself, so cancel the event
                event.setResult(Result.DENY);
            }
        }
    }
}
