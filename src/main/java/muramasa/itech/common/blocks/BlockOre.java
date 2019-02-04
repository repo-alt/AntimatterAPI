package muramasa.itech.common.blocks;

import muramasa.itech.api.enums.ItemFlag;
import muramasa.itech.api.materials.Material;
import muramasa.itech.api.properties.ITechProperties;
import muramasa.itech.api.util.Utils;
import muramasa.itech.common.items.ItemBlockOres;
import muramasa.itech.common.tileentities.base.TileEntityOre;
import muramasa.itech.common.utils.Ref;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class BlockOre extends Block {

    private static Material[] generatedOres;

    public BlockOre() {
        super(net.minecraft.block.material.Material.ROCK);
        setUnlocalizedName(Ref.MODID + ".block_ore");
        setRegistryName("block_ore");
        setCreativeTab(Ref.TAB_ORES);
        generatedOres = ItemFlag.CRUSHED.getMats(); //TODO cache stacks
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(ITechProperties.MATERIAL, ITechProperties.STONE).build();
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState exState = (IExtendedBlockState) state;
        TileEntity tile = Utils.getTile(world, pos);
        if (tile instanceof TileEntityOre) {
            TileEntityOre ore = (TileEntityOre) tile;
            exState = exState
                .withProperty(ITechProperties.MATERIAL, ore.getMaterialId())
                .withProperty(ITechProperties.STONE, ore.getStoneId());
        }
//        String name = world.getBlockState(pos.down()).getBlock().getRegistryName().toString();
//        System.out.println("EX: " + name);
        return exState;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (Material mat : generatedOres) {
            items.add(new ItemStack(this, 1, mat.getId()));
        }
    }

//    @Override
//    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
//        return null;
//    }

//    @Override
//    public void breakBlock(World world, BlockPos pos, IBlockState state) {
//        TileEntity tile = world.getTileEntity(pos);
//        System.out.println(tile == null);
//        if (tile instanceof TileEntityOre) {
//            int id = ((TileEntityOre) tile).materialId;
//            if (id > -1 && Material.generated[id].hasFlag(MaterialFlag.CRUSHED)) {
//                ItemStack stack = MetaItem.get(Prefix.chunk, Material.generated[id]);
//                world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack));
//            }
//        }
//    }


    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (stack.getItem() instanceof ItemBlockOres) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityOre) {
                ((TileEntityOre) tile).init(/*stack.getMetadata()*/generatedOres[RANDOM.nextInt(generatedOres.length)].getId(), RANDOM.nextInt(6));
            }
        }
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        return 1.0f + (getHarvestLevel(blockState) * 1.0f);
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        return 1;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, fortune);
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityOre();
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        for (Material mat : generatedOres) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), mat.getId(), new ModelResourceLocation(getRegistryName(), "inventory"));
        }
    }

    public static class ColorHandler implements IBlockColor {
        @Override
        public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            if (tintIndex == 1) {
                TileEntity tile = Utils.getTile(worldIn, pos);
                if (tile instanceof TileEntityOre) {
                    Material material = ((TileEntityOre) tile).getMaterial();
                    return material != null ? material.getRGB() : 0xffffff;
                }
            }
            return -1;
        }
    }
}