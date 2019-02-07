package muramasa.gregtech.api.enums;

import muramasa.gregtech.api.data.Materials;
import muramasa.gregtech.api.interfaces.IMaterialFlag;
import muramasa.gregtech.api.materials.Material;

import java.util.ArrayList;

public enum GenerationFlag implements IMaterialFlag {

    //TODO rename to generation flag?

    DUST(), //Add Dusts
    CRUSHED(),
    CRUSHEDC(),
    CRUSHEDP(),
    INGOT(), //Add Ingots and Nuggets (Can't have Nuggets without Ingots)
    BGEM(), //Add Basic Gem
    GEM(), //Add Gem quality variants
    //    CELL(), //Add Fluid Cells //TODO pointless with fluidStack flag?
    FLUID(), //Add Standard Fluid
    GAS(), //Add Gas Fluid
    PLASMA(), //Add Plasma Fluid
    TOOLS(), //Add Tool Parts
    PLATE(), //Add Plates
    ROD(), //Add Rods
    RING(), //Add Rings
    BOLT(), //Add Bolts
    FOIL(), //Add Foils
    SCREW(), //Add Screws
    GEAR(), //Add Gears
    SGEAR(), //Add Small Gear
    WIREF(), //Add Fine Wire
    ROTOR(), //Add Rotors //TODO deprecate? only 1 rotor
    DPLATE(), //Add Dense Plates
    SPRING(), //Add Springs
    HINGOT(), //Hot Ingots
    BLOCK(), //Add Blocks
    FRAME(); //Add Frame Blocks

    private long bit;
    private ArrayList<Integer> materialIds = new ArrayList<>();

    GenerationFlag() {
        bit = 1 << ordinal();
    }

    public void add(Material... mats) {
        for (Material material : mats) {
            materialIds.add(material.getId());
        }
    }

    public long getBit() {
        return this.bit;
    }

    public Material[] getMats() {
        Material[] materials = new Material[materialIds.size()];
        int size = materials.length;
        for (int i = 0; i < size; i++) {
            materials[i] = Materials.get(materialIds.get(i));
        }
        return materials;
    }

    public ArrayList<Integer> getIds() {
        return materialIds;
    }
}