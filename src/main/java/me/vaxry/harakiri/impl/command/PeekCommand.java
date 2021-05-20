package me.vaxry.harakiri.impl.command;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.RayTraceResult;


/**
 * Author Seth
 * 4/17/2019 @ 12:18 AM.
 */
public final class PeekCommand extends Command {

    private String entity;

    public PeekCommand() {
        super("Peek", new String[]{"Pk"}, "Allows you to see inside shulker boxes held in hand.", "Peek");
    }

    @Override
    public void run(String input) {
        if (!this.verifyInput(input, 1, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (split.length > 1) {
            if (!this.verifyInput(input, 2, 2)) {
                this.printUsage();
                return;
            }
            this.entity = split[1];
        }

        try {
            Harakiri.get().getEventManager().registerAttender(this);
            Harakiri.get().getEventManager().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Attender<EventRender2D> render2DAttender = new Attender<>(EventRender2D.class, event -> {
        try {
            final Minecraft mc = Minecraft.getMinecraft();

            ItemStack stack = null;

            if (this.entity != null) {
                EntityPlayer target = null;

                for (Entity e : mc.world.loadedEntityList) {
                    if (e != null) {
                        if (e instanceof EntityPlayer && e.getName().equalsIgnoreCase(this.entity)) {
                            target = (EntityPlayer) e;
                        }
                    }
                }

                if (target != null) {
                    stack = getHeldShulker(target);

                    if (stack == null) {
                        Harakiri.get().errorChat("\"" + target.getName() + "\" is not holding a shulker box");
                        this.entity = null;
                        Harakiri.get().getEventManager().unregisterAttender(this);
                        return;
                    }
                } else {
                    Harakiri.get().errorChat("\"" + this.entity + "\" is not within range");
                }
                this.entity = null;
            } else {
                final RayTraceResult ray = mc.objectMouseOver;

                if (ray != null) {
                    if (ray.entityHit != null) {
                        if (ray.entityHit instanceof EntityItemFrame) {
                            final EntityItemFrame itemFrame = (EntityItemFrame) ray.entityHit;
                            if (!itemFrame.getDisplayedItem().isEmpty() && itemFrame.getDisplayedItem().getItem() instanceof ItemShulkerBox) {
                                stack = itemFrame.getDisplayedItem();
                            } else {
                                stack = getHeldShulker(mc.player);
                            }
                        }
                    } else {
                        stack = getHeldShulker(mc.player);
                    }
                } else {
                    stack = getHeldShulker(mc.player);
                }
            }

            if (stack != null) {
                final Item item = stack.getItem();

                if (item instanceof ItemShulkerBox) {
                    if (Block.getBlockFromItem(item) instanceof BlockShulkerBox) {
                        final NBTTagCompound tag = stack.getTagCompound();
                        if (tag != null && tag.hasKey("BlockEntityTag", 10)) {
                            final NBTTagCompound entityTag = tag.getCompoundTag("BlockEntityTag");

                            final TileEntityShulkerBox te = new TileEntityShulkerBox();
                            te.setWorld(mc.world);
                            te.readFromNBT(entityTag);
                            mc.displayGuiScreen(new GuiShulkerBox(mc.player.inventory, te));
                        } else {
                            Harakiri.get().errorChat("This shulker box is empty");
                        }
                    }

                } else {
                    Harakiri.get().errorChat("Please hold a shulker box");
                }
            } else {
                Harakiri.get().errorChat("Please hold a shulker box");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Harakiri.get().getEventManager().unregisterAttender(this);
    });


    private ItemStack getHeldShulker(EntityPlayer entity) {
        if (!entity.getHeldItemMainhand().isEmpty() && entity.getHeldItemMainhand().getItem() instanceof ItemShulkerBox) {
            return entity.getHeldItemMainhand();
        }
        if (!entity.getHeldItemOffhand().isEmpty() && entity.getHeldItemOffhand().getItem() instanceof ItemShulkerBox) {
            return entity.getHeldItemOffhand();
        }
        return null;
    }

}
