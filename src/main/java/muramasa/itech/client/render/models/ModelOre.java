package muramasa.itech.client.render.models;

import com.google.common.collect.ImmutableMap;
import muramasa.itech.api.materials.MaterialSet;
import muramasa.itech.client.render.bakedmodels.BakedModelOre;
import muramasa.itech.common.utils.Ref;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class ModelOre implements IModel {

    public ModelOre() {

    }

//    @Override
//    public IBakedModel bakeModel(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> getter) {
//        IModel model = load(new ModelResourceLocation(ITech.MODID + ":blockores"));
//        IBakedModel bakedModel = model.bake(state, format, getter);
//        return new BakedModelOre(bakedModel);
//    }

    @Override
    public IBakedModel bake(IModelState modelState, VertexFormat vertexFormat, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        try {
            IModel model = ModelLoaderRegistry.getModel(new ModelResourceLocation(Ref.MODID + ":block_ore_base"));
//            IBakedModel bakedModel = /*ModelBase.tex(model, "base", new ResourceLocation(ITech.MODID, "blocks/stone"))*/model.bake(modelState, vertexFormat, bakedTextureGetter);

            IBakedModel[] bakedModels = new IBakedModel[MaterialSet.values().length];
            for (MaterialSet set : MaterialSet.values()) {
                bakedModels[set.ordinal()] = model.retexture(ImmutableMap.of("overlay", set.getOreLoc().toString())).bake(modelState, vertexFormat, bakedTextureGetter);
//                bakedModels[set.ordinal()] = ModelBase.tex(model, "overlay", set.getOreLoc()).bake(modelState, vertexFormat, bakedTextureGetter);
            }

            return new BakedModelOre(bakedModels);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ModelLoaderRegistry.getMissingModel().bake(modelState, vertexFormat, bakedTextureGetter);
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        ArrayList<ResourceLocation> textures = new ArrayList<>();
        for (MaterialSet set : MaterialSet.values()) {
            textures.add(set.getOreLoc());
        }
        textures.add(new ResourceLocation(Ref.MODID + ":blocks/stone"));
        textures.add(new ResourceLocation(Ref.MODID + ":blocks/ore"));
        return textures;
    }
}