package muramasa.gtu.api.worldgen;

import com.google.gson.annotations.Expose;
import com.google.gson.internal.LinkedTreeMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import muramasa.gtu.api.GregTechAPI;
import muramasa.gtu.api.blocks.BlockStone;
import muramasa.gtu.api.ore.BlockOre;
import muramasa.gtu.api.ore.StoneType;
import muramasa.gtu.api.tileentities.TileEntityOre;
import muramasa.gtu.api.util.Utils;
import muramasa.gtu.api.util.XSTR;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldGenStone extends WorldGenBase {

    private static final double SIZE_CONVERSION[] = {1, 1, 1.333333, 1.333333, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4}; // Bias the sizes towards skinnier boulders, ie more "shafts" than dikes or sills.

    @Expose public String type;
    @Expose public int minY, maxY, amount, size, probability;

    public BlockStone block;
    public IBlockState stone;
    public LongOpenHashSet CHECKED_SEEDS;

    public WorldGenStone(String id, StoneType type, int amount, int size, int probability, int minY, int maxY, int... dimensions) {
        super(id, dimensions);
        this.type = type.getId();
        this.amount = amount;
        this.size = size;
        this.probability = probability;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public WorldGenBase onDataOverride(LinkedTreeMap dataMap) {
        super.onDataOverride(dataMap);
        if (dataMap.containsKey("type")) type = Utils.parseString(dataMap.get("type"), type);
        if (dataMap.containsKey("minY")) minY = Utils.parseInt(dataMap.get("minY"), minY);
        if (dataMap.containsKey("maxY")) maxY = Utils.parseInt(dataMap.get("maxY"), maxY);
        if (dataMap.containsKey("amount")) amount = Utils.parseInt(dataMap.get("amount"), amount);
        if (dataMap.containsKey("size")) size = Utils.parseInt(dataMap.get("size"), size);
        if (dataMap.containsKey("probability")) probability = Utils.parseInt(dataMap.get("probability"), probability);
        return this;
    }

    @Override
    public WorldGenBase build() {
        super.build();
        this.block = GregTechAPI.get(BlockStone.class, type);
        if (block == null) throw new IllegalArgumentException("WorldGenOreLayer - " + getId() + ": was given a invalid stone type");
        this.stone = block.getDefaultState();
        this.CHECKED_SEEDS = new LongOpenHashSet();
        return this;
    }

    @Override
    public boolean generate(World world, XSTR rand, int passedX, int passedZ, BlockPos.MutableBlockPos pos, IBlockState state, IChunkGenerator generator, IChunkProvider provider) {
        // I think the real size of the balls is mSize/8, but the original code was difficult to understand.
        // Overall there will be less GT stones since they aren't spheres any more. /16 since this code uses it as a radius.
        int realSize = size / 16;
        int windowWidth = realSize / 16 + 1; // Width of chunks to check for a potential stoneseed
        // Check stone seeds to see if they have been added
        for (int chunkX = passedX / 16 - windowWidth; chunkX < passedX / 16 + windowWidth + 1; chunkX++) {
            for (int chunkZ = passedZ / 16 - windowWidth; chunkZ < passedZ / 16 + windowWidth + 1; chunkZ++) {
                //compute hash for dimension and position
                long hash = (world.provider.getDimension() & 0xffL) << 56 | ((long) chunkX & 0x000000000fffffffL) << 28 | (long) chunkZ & 0x000000000fffffffL;
                if (!CHECKED_SEEDS.contains(hash) && (probability <= 1 || rand.nextInt(probability) == 0)) CHECKED_SEEDS.add(hash);
                if (CHECKED_SEEDS.contains(hash)) {
                    int x = chunkX * 16;
                    int z = chunkZ * 16;
                    rand.setSeed(world.getSeed() ^ ((world.provider.getDimension() & 0xffL) << 56 | ((long) x & 0x000000000fffffffL) << 28 | (long) z & 0x000000000fffffffL) + Math.abs(0/*mBlockMeta*/) + Math.abs(size) + (block.getType() == StoneType.GRANITE_RED || block.getType() == StoneType.GRANITE_BLACK ? 32768 : 0));  //Don't judge me
                    for (int i = 0; i < amount; i++) { // Not sure why you would want more than one in a chunk! Left alone though.
                        // Locate the stoneseed XYZ. Original code would request an isAir at the seed location, causing a chunk generation request.
                        // To reduce potential worldgen cascade, we just always try to place a ball and use the check inside the for loop to prevent
                        // placement instead.
                        int tX = x + rand.nextInt(16);
                        int tY = minY + rand.nextInt(maxY - minY);
                        int tZ = z + rand.nextInt(16);

                        //Determine the XYZ sizes of the stoneseed
                        double xSize = SIZE_CONVERSION[rand.nextInt(SIZE_CONVERSION.length)];
                        double ySize = SIZE_CONVERSION[rand.nextInt(SIZE_CONVERSION.length) / 2];  // Skew the ySize towards the larger sizes, more long skinny pipes
                        double zSize = SIZE_CONVERSION[rand.nextInt(SIZE_CONVERSION.length)];

                        //Equation for an ellipsoid centered around 0,0,0
                        // Sx, Sy, and Sz are size controls (size = 1/S_)
                        // 1 = full size, 1.333 = 75%, 2 = 50%, 4 = 25%
                        // (chunkX * Sx)^2 + (y * Sy)^2 + (z * sZ)^2 <= (mSize)^2

                        //So, we setup the intial boundaries to be the size of the boulder plus a block in each direction
                        int tMinX = tX - (int) (realSize / xSize - 1.0);
                        int tMaxX = tX + (int) (realSize / xSize + 2.0);
                        int tMinY = tY - (int) (realSize / ySize - 1.0);
                        int tMaxY = tY + (int) (realSize / ySize + 2.0);
                        int tMinZ = tZ - (int) (realSize / zSize - 1.0);
                        int tMaxZ = tZ + (int) (realSize / zSize + 2.0);

                        // If the (tY-ySize) of the stoneseed is air in the current chunk, mark the seed empty and move on.

                        //pos = new BlockPos.MutableBlockPos(passedX + 8, tMinY, passedZ + 8);
                        //state = world.getBlockState(pos);
                        //if (state.getBlock().isAir(state, world, pos)) {
                        //if (Ref.debugStones) GregTech.LOGGER.info(id + " tX=" + tX + " tY=" + tY + " tZ=" + tZ + " realSize=" + realSize + " xSize=" + realSize/xSize + " ySize=" + realSize/ySize + " zSize=" + realSize/zSize + " tMinY=" + tMinY + " tMaxY=" + tMaxY + " - Skipped because first requesting chunk would not contain this stone");
                        //long hash = (world.provider.getDimension() & 0xffL) << 56 | ((long) x & 0x000000000fffffffL) << 28 | (long) z & 0x000000000fffffffL;
                        //CHECKED_SEEDS.put(hash, false);
                        //return;
                        //}

                        //Chop the boundaries by the parts that intersect with the current chunk
                        int wX = Math.max(tMinX, passedX + 8);
                        int eX = Math.min(tMaxX, passedX + 8 + 16);

                        int sZ = Math.max(tMinZ, passedZ + 8);
                        int nZ = Math.min(tMaxZ, passedZ + 8 + 16);

                        //if (Ref.debugStones) GregTech.LOGGER.info(id + " tX=" + tX + " tY=" + tY + " tZ=" + tZ + " realSize=" + realSize + " xSize=" + realSize/xSize + " ySize=" + realSize/ySize + " zSize=" + realSize/zSize + " wX=" + wX + " eX=" + eX + " tMinY=" + tMinY + " tMaxY=" + tMaxY + " sZ=" + sZ + " nZ=" + nZ);

                        double rightHandSide = realSize * realSize + 1;  //Precalc the right hand side
                        for (int iY = tMinY; iY < tMaxY; iY++) {  // Do placement from the bottom up layer up.  Maybe better on cache usage?
                            double yCalc = (double) (iY - tY) * ySize;
                            yCalc = yCalc * yCalc; // (y*Sy)^2
                            double leftHandSize = yCalc;
                            if (leftHandSize > rightHandSide) {
                                continue; // If Y alone is larger than the RHS, skip the rest of the loops
                            }
                            for (int iX = wX; iX < eX; iX++) {
                                double xCalc = (double) (iX - tX) * xSize;
                                xCalc = xCalc * xCalc;
                                leftHandSize = yCalc + xCalc;
                                if (leftHandSize > rightHandSide) { // Again, if X and Y is larger than the RHS, skip to the next value
                                    continue;
                                }
                                for (int iZ = sZ; iZ < nZ; iZ++) {
                                    double zCalc = (double) (iZ - tZ) * zSize;
                                    zCalc = zCalc * zCalc;
                                    leftHandSize = zCalc + xCalc + yCalc;
                                    if (leftHandSize <= rightHandSide) {
                                        // Yay! We can actually place a block now. (this part copied from original code)
                                        pos.setPos(iX, iY, iZ);
                                        state = world.getBlockState(pos);
                                        if (state.getBlock().isReplaceableOreGen(state, world, pos, WorldGenHelper.STONE_PREDICATE)) {
                                            world.setBlockState(pos, stone);
                                        } else if (state.getBlock() instanceof BlockOre) {
                                            world.setBlockState(pos, WorldGenHelper.ORE_STATE, 2 | 16);
                                            TileEntity tile = Utils.getTile(world, pos);
                                            if (tile instanceof TileEntityOre) {
                                                world.setTileEntity(pos, new TileEntityOre(((TileEntityOre) tile).getMaterial(), block.getType(), ((TileEntityOre) tile).getType()));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}