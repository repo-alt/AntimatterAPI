package muramasa.antimatter.cover;

import muramasa.antimatter.Data;
import muramasa.antimatter.client.dynamic.DynamicTexturer;
import muramasa.antimatter.client.dynamic.DynamicTexturers;
import muramasa.antimatter.machine.event.IMachineEvent;
import muramasa.antimatter.tile.TileEntityMachine;
import muramasa.antimatter.tool.AntimatterToolType;
import muramasa.antimatter.util.LazyHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class CoverStack<T extends TileEntity> implements INamedContainerProvider {

    public static final CoverStack<?>[] EMPTY_COVER_ARRAY = new CoverStack[0];

    /** The dyntexturer for covers. **/
    public LazyHolder<DynamicTexturer<Cover, Cover.DynamicKey>> coverTexturer;


    private Cover cover;
    private CompoundNBT nbt;
    private T tile;

    public CoverStack(Cover cover, T tile) {
        this.cover = Objects.requireNonNull(cover);
        this.tile = tile;
        this.nbt = new CompoundNBT();
        //Lazy way to ensure it is only called on client lol
        coverTexturer = LazyHolder.of(() -> new DynamicTexturer<>(DynamicTexturers.COVER_DYNAMIC_TEXTURER));
    }

    //This allows you to instantiate a non-stateful cover, like COVER_EMPTY.
    //Using state with this is a runtime error.
    public CoverStack(Cover cover) {
        this.nbt = new CompoundNBT();
        this.cover = cover;
    }

    //Automatically calls onPlace.
    public CoverStack(Cover cover, T tile, Direction side) {
        this(cover, tile);
    }

    /** Events **/
    public boolean onInteract(PlayerEntity player, Hand hand, Direction side, @Nullable AntimatterToolType type) {
        return cover.onInteract(this, player, hand, side, type);
    }

    public void onPlace(Direction side) {
        cover.onPlace(this, side);
    }

    public void onRemove(Direction side) {
        cover.onRemove(this, side);
    }

    //Called on update of the world.
    public void onUpdate(Direction side) {
        cover.onUpdate(this, side);
    }

    public void onMachineEvent(TileEntityMachine tile, IMachineEvent event, int ...data) {
        cover.onMachineEvent(this, tile, event, data);
    }

    public boolean openGui(PlayerEntity player, Direction side) {
        return cover.openGui(this, player, side);
    }

    public void serialize(CompoundNBT nbt) {
        nbt.putString("id", cover.getId());
    }

    public boolean isEqual(Cover cover) {
        return this.cover.getId().equals(cover.getId());
    }

    public boolean isEqual(CoverStack<T> cover) {
        return this.cover.getId().equals(cover.cover.getId());
    }

    public String getId() {
        return this.cover.getId();
    }

    public boolean isEmpty() {
        return cover == Data.COVERNONE || cover.isEqual(Data.COVERNONE);
    }

    public boolean skipRender() {
        return isEmpty();
    }

    //Gets the backing cover.
    //Because getCover().getCover() looks stupid
    public Cover getCover() {
        return cover;
    }

    public T getTile() {
        return tile;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("TODO");
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return cover.gui != null && cover.gui.getMenuHandler() != null ? cover.gui.getMenuHandler().getMenu(this, inv, windowId) : null;
    }

    public CompoundNBT serialize() {
        //Do final things before saving. Optional as state should usually be set during runtime
        cover.serialize(this,nbt);
        return nbt;
    }

    public void deserialize(CompoundNBT nbt) {
        this.nbt = nbt;
        cover.deserialize(this,nbt);
    }

    public CompoundNBT getNbt() {
        if (this.nbt == null) this.nbt = new CompoundNBT();
        return this.nbt;
    }
}