package muramasa.gregtech.client.render.overrides;

import com.google.common.collect.ImmutableList;
import muramasa.gregtech.api.items.ItemFluidCell;
import muramasa.gregtech.client.render.ModelUtils;
import muramasa.gregtech.client.render.bakedmodels.BakedFluidCell;
import muramasa.gregtech.client.render.models.ModelFluidCell;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;

public class ItemOverrideFluidCell extends ItemOverrideList {

    private static HashMap<String, IBakedModel> CACHE = new HashMap<>();

    public ItemOverrideFluidCell() {
        super(ImmutableList.of());
    }

    @Override
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
        FluidStack fluidStack = ItemFluidCell.getContents(stack);
        if (fluidStack == null) return originalModel;
        IBakedModel baked = CACHE.get(fluidStack.getFluid().getName());
        if (baked == null) {
            BakedFluidCell bakedCell = (BakedFluidCell) originalModel;
            ModelFluidCell model = new ModelFluidCell(fluidStack.getFluid());
            CACHE.put(fluidStack.getFluid().getName(), (baked = model.bake(TRSRTransformation.identity(), bakedCell.format, ModelUtils.getTextureGetter())));
        }
        return baked;
    }
}
