//package muramasa.antimatter.blocks.pipe;
//
//import muramasa.gtu.Ref;
//import muramasa.antimatter.GregTechAPI;
//import muramasa.gtu.data.Textures;
//import muramasa.antimatter.machines.Tier;
//import muramasa.antimatter.materials.Material;
//import muramasa.antimatter.pipe.PipeSize;
//import muramasa.antimatter.registration.IColorHandler;
//import muramasa.antimatter.registration.IItemBlock;
//import muramasa.antimatter.texture.TextureData;
//import muramasa.antimatter.tileentities.pipe.TileEntityCable;
//import muramasa.antimatter.util.Utils;
//import muramasa.antimatter.client.bakedblockold.BakedTextureDataItem;
//import muramasa.gtu.client.render.bakedmodels.BakedPipe;
//import net.minecraft.block.Block;
//import net.minecraft.block.state.BlockStateContainer;
//import net.minecraft.block.BlockState;
//import net.minecraft.client.renderer.model.IBakedModel;
//import net.minecraft.client.renderer.block.model.ModelResourceLocation;
//import net.minecraft.client.util.ITooltipFlag;
//import net.minecraft.creativetab.CreativeTabs;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.Direction;
//import net.minecraft.util.Hand;
//import net.minecraft.util.NonNullList;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.registry.IRegistry;
//import net.minecraft.util.text.TextFormatting;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.World;
//import net.minecraftforge.client.model.ModelLoader;
//import net.minecraftforge.common.property.IExtendedBlockState;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//
//import javax.annotation.Nullable;
//import java.util.List;
//import java.util.Locale;
//
//import static muramasa.antimatter.GregTechProperties.*;
//
//public class BlockCable extends BlockPipe implements IItemBlock, IColorHandler {
//
//    protected int loss, lossInsulated;
//    protected int[] amps;
//    protected Tier tier;
//
//    public BlockCable(Material material, int loss, int lossInsulated, int baseAmps, Tier tier, PipeSize... sizes) {
//        super("cable", material, Textures.PIPE_DATA[1], sizes);
//        this.loss = loss;
//        this.lossInsulated = lossInsulated;
//        this.tier = tier;
//        this.amps = new int[] {
//            baseAmps, baseAmps * 2, baseAmps * 4, baseAmps * 8, baseAmps * 12, baseAmps * 16
//        };
//
//        overrideState(this, new BlockStateContainer.Builder(this).add(PIPE_SIZE, PIPE_INSULATED).add(PIPE_CONNECTIONS, TEXTURE, COVER).build());
//
//        GregTechAPI.register(BlockCable.class, this);
//    }
//
//    @Override
//    public BlockState getExtendedState(BlockState state, IBlockReader world, BlockPos pos) {
//        IExtendedBlockState exState = (IExtendedBlockState) state;
//        TileEntity tile = Utils.getTile(world, pos);
//        if (tile instanceof TileEntityCable) {
//            TileEntityCable cable = (TileEntityCable) tile;
//            exState = exState.withProperty(PIPE_CONNECTIONS, cable.getConnections());
//            exState = exState.withProperty(TEXTURE, state.getValue(PIPE_INSULATED) ? Textures.PIPE_DATA[2] : getData());
//        }
//        return exState;
//    }
//
//    @Override
//    public BlockState getStateFromMeta(int meta) {
//        boolean ins = meta > 7;
//        int size = ins ? meta - 8 : meta;
//        return getDefaultState().withProperty(PIPE_SIZE, PipeSize.VALUES[size]).withProperty(PIPE_INSULATED, ins);
//    }
//
//    @Override
//    public int getMetaFromState(BlockState state) {
//        int meta = state.getValue(PIPE_SIZE).ordinal();
//        return state.getValue(PIPE_INSULATED) ? meta + 8 : meta;
//    }
//
//    @Override
//    public BlockState getStateForPlacement(World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, Hand hand) {
//        int stackMeta = placer.getHeldItem(hand).getMetadata();
//        int size = stackMeta > 7 ? stackMeta - 8 : stackMeta;
//        return getDefaultState().withProperty(PIPE_SIZE, PipeSize.VALUES[size]).withProperty(PIPE_INSULATED, stackMeta > 7);
//    }
//
//    @Override
//    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
//        for (int i = 0; i < sizes.length; i++) {
//            items.add(new ItemStack(this, 1, sizes[i].ordinal()));
//        }
//        for (int i = 0; i < sizes.length; i++) {
//            items.add(new ItemStack(this, 1, sizes[i].ordinal() + 8));
//        }
//    }
//
//    public long getVoltage() {
//        return tier.getVoltage();
//    }
//
//    public int getLoss(boolean insulated) {
//        return insulated ? lossInsulated : loss;
//    }
//
//    public int getAmps(PipeSize size) {
//        return amps[size.ordinal()];
//    }
//
//    public Tier getTier() {
//        return tier;
//    }
//
//    @Nullable
//    @Override
//    public TileEntity createTileEntity(World world, BlockState state) {
//        return new TileEntityCable();
//    }
//
//    @Nullable
//    @Override
//    public String getHarvestTool(BlockState state) {
//        return "wire_cutter";
//    }
//
//    @Override
//    public String getDisplayName(ItemStack stack) {
//        boolean ins = stack.getMetadata() > 7;
//        PipeSize size = PipeSize.VALUES[ins ? stack.getMetadata() - 8 : stack.getMetadata()];
//        return size.getCableThickness() + "x " + material.getDisplayName() + (ins ? " Cable" : " Wire");
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
//        boolean ins = stack.getMetadata() > 7;
//        PipeSize size = PipeSize.VALUES[ins ? stack.getMetadata() - 8 : stack.getMetadata()];
//        tooltip.add("Max Voltage: " + TextFormatting.GREEN + getVoltage() + " (" + getTier().getId().toUpperCase(Locale.ENGLISH) + ")");
//        tooltip.add("Max Amperage: " + TextFormatting.YELLOW + getAmps(size));
//        tooltip.add("Loss/Meter/Ampere: " + TextFormatting.RED + getLoss(ins) + TextFormatting.GRAY + " EU-Volt");
//    }
//
//    @Override
//    public int getBlockColor(BlockState state, @Nullable IBlockReader world, @Nullable BlockPos pos, int i) {
//        if (!(state.getBlock() instanceof BlockCable) && world == null || pos == null) return -1;
//        TileEntity tile = Utils.getTile(world, pos);
//        if (!(tile instanceof TileEntityCable)) return -1;
//        return state.getValue(PIPE_INSULATED) ? i == 2 ? getRGB() : -1 : i == 0 || i == 2 ? getRGB() : -1;
//    }
//
//    @Override
//    public int getItemColor(ItemStack stack, @Nullable Block block, int i) {
//        return stack.getMetadata() > 7 ? i == 1 ? getRGB() : -1 : getRGB();
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void onModelRegistration() {
//        for (int i = 0; i < sizes.length; i++) {
//            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), sizes[i].ordinal(), new ModelResourceLocation(Ref.MODID + ":" + getId(), "size=" + sizes[i].getName() + ",insulated=false"));
//            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), sizes[i].ordinal() + 8, new ModelResourceLocation(Ref.MODID + ":" + getId(), "size=" + sizes[i].getName() + ",insulated=true"));
//        }
//        //Redirect block model to custom baked model handling
//        ModelLoader.setCustomStateMapper(this, new StateMapperRedirect(new ResourceLocation(Ref.MODID, "block_pipe")));
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void onModelBuild(IRegistry<ModelResourceLocation, IBakedModel> registry) {
//        for (int i = 0; i < getSizes().length; i++) {
//            ModelResourceLocation loc = new ModelResourceLocation(Ref.MODID + ":" + getId(), "size=" + getSizes()[i].getName() + ",insulated=false");
//            IBakedModel baked = new BakedTextureDataItem(BakedPipe.BAKED[getSizes()[i].ordinal()][2], new TextureData().base(Textures.PIPE_DATA[1].getBase()).overlay(Textures.PIPE_DATA[1].getOverlay(getSizes()[i].ordinal())));
//            registry.putObject(loc, baked);
//
//            loc = new ModelResourceLocation(Ref.MODID + ":" + getId(), "size=" + getSizes()[i].getName() + ",insulated=true");
//            baked = new BakedTextureDataItem(BakedPipe.BAKED[getSizes()[i].ordinal()][2], new TextureData().base(Textures.PIPE_DATA[2].getBase()).overlay(Textures.PIPE_DATA[2].getOverlay(getSizes()[i].ordinal())));
//            registry.putObject(loc, baked);
//        }
//    }
//}