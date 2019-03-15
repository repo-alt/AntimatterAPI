package muramasa.gregtech.client.render.bakedmodels;

import muramasa.gregtech.api.cover.Cover;
import muramasa.gregtech.api.properties.GTProperties;
import muramasa.gregtech.api.texture.TextureData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.List;

public class BakedTextureDataCovered extends BakedTextureData {

    public BakedTextureDataCovered(IBakedModel baked, ItemOverrideList item) {
        super(baked, item);
    }

    public BakedTextureDataCovered(IBakedModel baked, TextureData data) {
        super(baked, data);
    }

    @Override
    public List<BakedQuad> getBakedQuads(@Nullable IExtendedBlockState exState, @Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (exState != null) {
            data = exState.getValue(GTProperties.TEXTURE);
            Cover[] covers = exState.getValue(GTProperties.COVER);

            return data.apply(baked);
        }
        return data.apply(baked);
    }
}