package me.vaxry.harakiri.impl.module.combat;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;


public final class AutoArmorModule extends Module {

    public final Value<Float> delay = new Value<Float>("Delay", new String[]{"Del"}, "Delay, in milliseconds.", 250.0f, 0.0f, 500.f, 1.0f);
    public final Value<Boolean> curse = new Value<Boolean>("Curse", new String[]{"Curses"}, "If on, doesn't select cursed equipment.", false);

    private final Timer equipTimer = new Timer();

    public AutoArmorModule() {
        super("AutoArmor", new String[]{"AutoArm", "AutoArmour"}, "Automatically equips armor.", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (shouldEquip(mc)) {
                equip(mc);
                equipTimer.reset();
            }
        }
    }

    private void equip(final Minecraft mc) {
        int[] armorSlots = new int[]{-1, -1, -1, -1}, armorValues = new int[]{-1, -1, -1, -1};

        for (int type = 0; type < 4; type++) {
            ItemStack current = mc.player.inventory.armorItemInSlot(type);
            if (!current.isEmpty() && current.getItem() instanceof ItemArmor) {
                armorValues[type] = getArmorValue(current);
            }
        }

        for (int slot = 9; slot < 45; slot++) {
            ItemStack itemStack = mc.player.inventoryContainer.getSlot(slot).getStack();
            if (itemStack.isEmpty() || !(itemStack.getItem() instanceof ItemArmor) || itemStack.getCount() > 1 || (this.curse.getValue() && EnchantmentHelper.hasBindingCurse(itemStack)))
                continue;

            ItemArmor armor = (ItemArmor) itemStack.getItem();

            int type = armor.armorType.ordinal() - 2;
            if (type == 2 && mc.player.inventory.armorItemInSlot(type).getItem().equals(Items.ELYTRA))
                continue;

            int value = getArmorValue(itemStack);
            if (value > armorValues[type]) {
                armorSlots[type] = slot;
                armorValues[type] = value;
            }
        }

        for (int type = 0; type < 4; type++) {
            int slot = armorSlots[type];
            if (slot == -1)
                continue;

            ItemStack current = mc.player.inventory.armorItemInSlot(type);
            if (!current.isEmpty() || mc.player.inventory.getFirstEmptyStack() != -1) {
                mc.playerController.windowClick(0, 8 - type, 0, ClickType.QUICK_MOVE, mc.player);
                mc.playerController.windowClick(0, slot, 0, ClickType.QUICK_MOVE, mc.player);
                break;
            }
        }
    }

    private boolean shouldEquip(final Minecraft mc) {
        boolean inInventory = mc.currentScreen instanceof GuiContainer/* && !(mc.currentScreen instanceof InventoryEffectRenderer)*/;
        boolean hasDelayFinished = this.equipTimer.passed(this.delay.getValue());
        boolean isCreative = mc.player.isCreative();
        return !isCreative && hasDelayFinished && !inInventory;
    }

    private int getArmorValue(ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemArmor) {
            int reductionAmount = ((ItemArmor) itemStack.getItem()).damageReduceAmount;
            int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, itemStack);
            return reductionAmount + enchantmentLevel;
        }
        return -1;
    }
}
