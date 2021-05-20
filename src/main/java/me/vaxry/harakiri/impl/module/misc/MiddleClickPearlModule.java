package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;


public class MiddleClickPearlModule extends Module {
    private boolean clicked;

    public MiddleClickPearlModule() {
        super("MiddleClickPearl", new String[]{"mcp", "autopearl"}, "Throws a pearl when you middle-click pointing in mid-air", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (mc.player == null || mc.world == null)
                return;

            if (mc.currentScreen == null) {
                if (Mouse.isButtonDown(2)) {
                    if (!this.clicked) {
                        final RayTraceResult result = mc.objectMouseOver;
                        if (result != null && result.typeOfHit == RayTraceResult.Type.MISS) {
                            final int pearlSlot = findPearlInHotbar(mc);
                            if (pearlSlot != -1) {
                                final int oldSlot = mc.player.inventory.currentItem;
                                mc.player.inventory.currentItem = pearlSlot;
                                mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                                mc.player.inventory.currentItem = oldSlot;
                            }
                        }
                    }
                    this.clicked = true;
                } else {
                    this.clicked = false;
                }
            }
        }
    }

    private boolean isItemStackPearl(final ItemStack itemStack) {
        return itemStack.getItem() instanceof ItemEnderPearl;
    }

    private int findPearlInHotbar(final Minecraft mc) {
        for (int index = 0; InventoryPlayer.isHotbar(index); index++) {
            if (isItemStackPearl(mc.player.inventory.getStackInSlot(index))) return index;
        }
        return -1;
    }
}