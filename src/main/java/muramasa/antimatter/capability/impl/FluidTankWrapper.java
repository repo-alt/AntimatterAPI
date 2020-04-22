package muramasa.antimatter.capability.impl;

import muramasa.antimatter.machine.event.ContentEvent;
import muramasa.antimatter.tile.TileEntityMachine;
import muramasa.antimatter.util.Utils;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import tesseract.api.fluid.FluidData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidTankWrapper implements IFluidHandler {

    private FluidTank[] tanks;
    private boolean dirty = false;

    public FluidTankWrapper(TileEntityMachine machine, int count, int capacity, boolean input) {
        tanks = new FluidTank[count];
        for (int i = 0; i < count; i++) {
            tanks[i] = new FluidTank(capacity) {
                @Override
                protected void onContentsChanged() {
                    dirty = true;
                    machine.onMachineEvent(input ? ContentEvent.FLUID_INPUT_CHANGED : ContentEvent.FLUID_OUTPUT_CHANGED);
                }
            };
        }
    }

//    @Override
//    public IFluidTankProperties[] getTankProperties() {
//        IFluidTankProperties[] properties = new IFluidTankProperties[tanks.length];
//        for (int i = 0; i < tanks.length; i++) {
//            properties[i] = new FluidTankProperties(tanks[i].getFluid(), tanks[i].getCapacity(), tanks[i].canFill(), tanks[i].canDrain());
//        }
//        return properties;
//    }

    public int setFirstEmptyTank(FluidStack fluid) {
        FluidTank tank = getFirstEmptyTank();
        if (tank != null) {
            tank.setFluid(fluid);
            return fluid.getAmount();
        }
        return 0;
    }

    public FluidTank getFirstEmptyTank() {
        for (FluidTank tank : tanks) {
            if (!tank.isEmpty()) return tank;
        }
        return null;
    }

    @Nullable
    public FluidTank getFirstValidTank() {
        for (FluidTank tank : tanks) {
            if (!tank.isEmpty()) return tank;
        }
        return null;
    }

    @Nullable
    public FluidTank findFluidInTanks(FluidStack fluid) {
        for (FluidTank tank : tanks) {
            if (!tank.isEmpty() && Utils.equals(tank.getFluid(), fluid)) return tank;
        }
        return null;
    }

    @Override
    public int getTanks() {
        return tanks.length;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return tanks[tank].getFluid();
    }

    public void setFluidToTank(int tank, FluidStack stack) {
        tanks[tank].setFluid(stack);
    }

    @Nonnull
    public CompoundNBT writeToNBT(int tank, CompoundNBT nbt) {
        tanks[tank].writeToNBT(nbt);
        return nbt;
    }

    @Override
    public int getTankCapacity(int tank) {
        return tanks[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return tanks[tank].isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        FluidTank tank = findFluidInTanks(resource);
        if (tank != null) {
            return tank.fill(resource, action);
        } else {
            tank = getFirstEmptyTank();
            if (tank != null) return tank.fill(resource, action);
        }
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidTank tank = findFluidInTanks(resource);
        if (tank != null) return tank.drain(resource, action);
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidTank tank = getFirstValidTank();
        if (tank != null) return tank.drain(maxDrain, action);
        return FluidStack.EMPTY;
    }

    public boolean isDirty() {
        return dirty;
    }

    public static FluidData pack(FluidStack resource) {
        Fluid fluid = resource.getFluid();
        FluidAttributes attr = fluid.getAttributes();
        return new FluidData(resource, fluid, resource.getAmount(), attr.getTemperature(), attr.isGaseous());
    }
}
