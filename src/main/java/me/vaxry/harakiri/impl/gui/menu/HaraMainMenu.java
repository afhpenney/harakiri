package me.vaxry.harakiri.impl.gui.menu;

import com.yworks.yguard.test.A;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.framework.texture.Texture;
import me.vaxry.harakiri.impl.fml.harakiriMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;

public class HaraMainMenu extends GuiMainMenu {
    private Texture backgroundTex;
    private ArrayList<HaraMainMenuButton> mainMenuButtons = new ArrayList<>();
    private boolean first = true;

    private float lastButX = 0;
    private final float Y_OFFSET = 0;

    public HaraMainMenu(){
        Harakiri.INSTANCE.getEventManager().addEventListener(this);
    }

    @Listener
    public void displayGui(EventDisplayGui event){
        if (event.getScreen() == null && mc.world == null) {
            event.setCanceled(true);
            Minecraft.getMinecraft().displayGuiScreen(this);
        }

        if (Minecraft.getMinecraft().currentScreen instanceof HaraMainMenu && event.getScreen() == null)
            event.setCanceled(true);

        if (event.getScreen() != null)
            if (event.getScreen() instanceof GuiMainMenu && !(event.getScreen() instanceof HaraMainMenu)) {
                event.setCanceled(true);
                Minecraft.getMinecraft().displayGuiScreen(this);
            }
    }

    @Override
    public void initGui() {
        super.initGui();
        backgroundTex   = new Texture("haramenu.png");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        super.drawScreen(mouseX, mouseY, partialTicks);

        if(first){
            this.mainMenuButtons.clear();
            for(GuiButton button : this.buttonList){
                this.mainMenuButtons.add(new HaraMainMenuButton(button.id, button.x, (int)(button.y + Y_OFFSET), button.width, button.height, button.displayString));
            }
            first = false;
        }

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        this.backgroundTex.bind();
        this.backgroundTex.render(0,0, res.getScaledWidth(), res.getScaledHeight());

        for(HaraMainMenuButton button : this.mainMenuButtons){
            button.drawButton(mc, mouseX, mouseY, partialTicks);
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow("Do not distribute", 0, res.getScaledHeight() -
                2*Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT - 2, 0xFFFFFFFF);
        Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow("Copyright Mojang AB", 0, res.getScaledHeight()
                - Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT - 1, 0xFFFFFFFF);

        Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow("Harakiri v" + harakiriMod.VERSION, res.getScaledWidth() -
                Harakiri.INSTANCE.getTTFFontUtil().getStringWidth("Harakiri v" + harakiriMod.VERSION) - 2,
                res.getScaledHeight() - Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT - 1,
                0xFFFFFFFF);

        Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow("Logged in as " + Harakiri.INSTANCE.getUsername(),
                res.getScaledWidth() - Harakiri.INSTANCE.getTTFFontUtil().getStringWidth("Logged in as " + Harakiri.INSTANCE.getUsername()) - 2,
                0,
                0xFFFFFFFF);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();

        // Reload main menu buttonz
        if(needsAnUpdate()) {
            this.mainMenuButtons.clear();
            for (GuiButton button : this.buttonList) {
                this.mainMenuButtons.add(new HaraMainMenuButton(button.id, button.x, (int)(button.y + Y_OFFSET), button.width, button.height, button.displayString));
            }
        }
    }

    private boolean needsAnUpdate(){
        if(lastButX != this.buttonList.get(0).x){
            lastButX = this.buttonList.get(0).x;
            return true;
        }
        return false;
    }

}
