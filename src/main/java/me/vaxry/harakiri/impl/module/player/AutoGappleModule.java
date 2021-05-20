package me.vaxry.harakiri.impl.module.player;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;


public final class AutoGappleModule extends Module {

    public final Value<Float> health = new Value<Float>("Health", new String[]{"Hp", "h"}, "The amount of health needed to acquire a notch apple.", 8.0f, 0.0f, 20.0f, 0.5f);
    public final Value<Integer> forcedSlot = new Value<Integer>("Slot", new String[]{"s"}, "The hot-bar slot to put the notch apple into. (45 for offhand)", 44, 0, 44, 1);

    private int previousHeldItem = -1;
    private int notchAppleSlot = -1;

    public AutoGappleModule() {
        super("AutoGapple", new String[]{"Gapple", "AutoApple"}, "Automatically eats a (god) apple when health is below the set threshold.", "NONE", -1, ModuleType.PLAYER);
    }

    @Override
    public String getMetaData() {
        return "" + this.getNotchAppleCount();
    }

    Attender<EventPlayerUpdate> onPlayerUpdate = new Attender<>(EventPlayerUpdate.class, event -> {
        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null)
            return;

        if (mc.player.getHealth() < this.health.getValue() && mc.player.getAbsorptionAmount() == 0) {
            this.notchAppleSlot = this.findNotchApple();
        }

        if (this.notchAppleSlot != -1) {
            if (this.forcedSlot.getValue() != 45) { // we aren't trying to put it in the offhand
                if (this.previousHeldItem == -1) {
                    this.previousHeldItem = mc.player.inventory.currentItem;
                }

                if (this.notchAppleSlot < 36) {
                    mc.playerController.windowClick(0, this.forcedSlot.getValue(), 0, ClickType.QUICK_MOVE, mc.player); // last hotbar slot
                    mc.playerController.windowClick(0, this.notchAppleSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, this.forcedSlot.getValue(), 0, ClickType.PICKUP, mc.player);
                    mc.player.inventory.currentItem = this.forcedSlot.getValue() - 36;
                } else {
                    mc.player.inventory.currentItem = this.notchAppleSlot - 36; // in the hotbar, so remove the inventory offset
                }
            } else { // we need this notch apple in the offhand
                if (mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE) {
                    mc.playerController.windowClick(0, 45, 0, ClickType.QUICK_MOVE, mc.player); // offhand slot
                    mc.playerController.windowClick(0, this.notchAppleSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                }
            }

            if (mc.player.getHealth() >= this.health.getValue() && mc.player.getAbsorptionAmount() > 0) {
                mc.gameSettings.keyBindUseItem.pressed = false;
                if (this.previousHeldItem != -1) {
                    mc.player.inventory.currentItem = this.previousHeldItem;
                }
                this.notchAppleSlot = -1;
                this.previousHeldItem = -1;
            } else {
                mc.gameSettings.keyBindUseItem.pressed = true;
            }
        }
    });

    private int findNotchApple() {
        for (int slot = 44; slot > 8; slot--) {
            ItemStack itemStack = Minecraft.getMinecraft().player.inventoryContainer.getSlot(slot).getStack();
            if (itemStack.isEmpty() || itemStack.getItemDamage() == 0)
                continue;

            if (itemStack.getItem() == Items.GOLDEN_APPLE) {
                return slot;
            }
        }
        return -1;
    }

    private int getNotchAppleCount() {
        int gapples = 0;

        if (Minecraft.getMinecraft().player == null)
            return gapples;

        for (int i = 0; i < 45; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.GOLDEN_APPLE && stack.getItemDamage() != 0) {
                gapples += stack.getCount();
            }
        }

        return gapples;
    }
}
