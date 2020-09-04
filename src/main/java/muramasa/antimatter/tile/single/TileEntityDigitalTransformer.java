package muramasa.antimatter.tile.single;

import muramasa.antimatter.capability.machine.MachineInteractHandler;
import muramasa.antimatter.gui.event.GuiEvent;
import muramasa.antimatter.gui.event.IGuiEvent;
import muramasa.antimatter.machine.MachineState;
import muramasa.antimatter.machine.Tier;
import muramasa.antimatter.machine.types.Machine;
import net.minecraft.client.gui.FontRenderer;

import java.util.Optional;

import static muramasa.antimatter.machine.MachineFlag.CONFIGURABLE;

public class TileEntityDigitalTransformer extends TileEntityTransformer {

    public TileEntityDigitalTransformer(Machine<?> type) {
        super(type, 1, (v) -> (8192L + v * 64L));
    }

    @Override
    public void onFirstTick() {
        if (has(CONFIGURABLE)) interactHandler = Optional.of(new MachineInteractHandler(this));
        super.onFirstTick();
    }

    @Override
    public void onGuiEvent(IGuiEvent event, int... data) {
        if (event == GuiEvent.BUTTON_ACTION) {
            energyHandler.ifPresent(h -> {
                boolean shiftHold = data[1] != 0;
                switch (data[0]) {
                    case 0:
                        voltage -= shiftHold ? 512 : 64;
                        break;
                    case 1:
                        voltage /= shiftHold ? 512 : 64;
                        break;
                    case 2:
                        amperage -= shiftHold ? 512 : 64;
                        break;
                    case 3:
                        amperage /= shiftHold ? 512 : 64;
                        break;
                    case 4:
                        voltage -= shiftHold ? 16 : 1;
                        break;
                    case 5:
                        voltage /= shiftHold ? 16 : 2;
                        break;
                    case 6:
                        amperage -= shiftHold ? 16 : 1;
                        break;
                    case 7:
                        amperage /= shiftHold ? 16 : 2;
                        break;
                    case 8:
                        voltage += shiftHold ? 512 : 64;
                        break;
                    case 9:
                        voltage *= shiftHold ? 512 : 64;
                        break;
                    case 10:
                        amperage += shiftHold ? 512 : 64;
                        break;
                    case 11:
                        amperage *= shiftHold ? 512 : 64;
                        break;
                    case 12:
                        voltage += shiftHold ? 16 : 1;
                        break;
                    case 13:
                        voltage *= shiftHold ? 16 : 2;
                        break;
                    case 14:
                        amperage += shiftHold ? 16 : 1;
                        break;
                    case 15:
                        amperage *= shiftHold ? 16 : 2;
                        break;
                }

                setMachineState((long)(amperage * voltage) >= 0L ? getDefaultMachineState() : MachineState.DISABLED);

                if (isDefaultMachineState()) {
                    h.setInputVoltage(getMachineTier().getVoltage());
                    h.setOutputVoltage(voltage);
                    h.setInputAmperage(amperage);
                    h.setOutputVoltage(1);
                } else {
                    h.setInputVoltage(voltage);
                    h.setOutputVoltage(getMachineTier().getVoltage());
                    h.setInputAmperage(1);
                    h.setOutputVoltage(amperage);
                }

                h.onReset();
            });
        }
    }

    @Override
    public void drawInfo(FontRenderer renderer) {
        renderer.drawString("Digital Trans", 56, 24, 16448255);
        renderer.drawString("VOLT: " + voltage, 56, 40, 16448255);
        renderer.drawString("TIER: " + Tier.getTier(voltage < 0 ? -voltage : voltage).getId().toUpperCase(), 56, 48, 16448255);
        renderer.drawString("AMP: " + amperage, 56, 56, 16448255);
        renderer.drawString("SUM: " + (long)(amperage * voltage), 56, 64, 16448255);
    }
}
