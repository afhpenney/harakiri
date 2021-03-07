package me.vaxry.harakiri.impl.gui.hud;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.hud.component.DraggableHudComponent;
import me.vaxry.harakiri.framework.gui.hud.component.HudComponent;
import me.vaxry.harakiri.framework.texture.Texture;
import me.vaxry.harakiri.framework.util.ColorUtil;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.impl.gui.hud.anchor.AnchorPoint;
import me.vaxry.harakiri.impl.gui.hud.component.PlexusComponent;
import me.vaxry.harakiri.impl.gui.hud.component.SwitchViewComponent;
import me.vaxry.harakiri.impl.gui.hud.component.module.ModuleListComponent;
import me.vaxry.harakiri.impl.gui.hud.component.module.ModuleSearchComponent;
import me.vaxry.harakiri.impl.module.ui.HudEditorModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Author Seth
 * 7/25/2019 @ 4:15 AM.
 */
public final class GuiHudEditor extends GuiScreen {

    float rainSpeed = 0.1f; // +0.1 default

    protected Texture bg = null;

    public int rainbowColor = 0x00000000;
    private float hue = 0;

    private Timer timer = new Timer();

    private ArrayList<HudComponent> hudComponentsSorted = new ArrayList<>();

    // Color picker handle
    public boolean isColorPickerOpen = false;
    public float colorPickerX = 0;
    public float colorPickerY = 0;
    public final float colorPickerXY = 70;
    public HudComponent colorPickerParent = null;
    public String colorPickerName = "";
    public boolean specialColorClick = false;
    public boolean forceCloseColorPicker = false;

    public GuiHudEditor(){
        hudComponentsSorted.addAll(Harakiri.INSTANCE.getHudManager().getComponentList());
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        final HudEditorModule mod = (HudEditorModule) Harakiri.INSTANCE.getModuleManager().find(HudEditorModule.class);

        if (mod != null) {
            if (keyCode == Keyboard.getKeyIndex(mod.getKey())) {
                if (mod.isOpen()) {
                    mod.setOpen(false);
                } else {
                    Minecraft.getMinecraft().displayGuiScreen(null);
                }
            }
        }

        for (HudComponent component : Harakiri.INSTANCE.getHudManager().getComponentList()) {
            if (component.isVisible()) {
                component.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {
        super.onResize(mcIn, w, h);

        final ScaledResolution sr = new ScaledResolution(mcIn);
        for (AnchorPoint anchorPoint : Harakiri.INSTANCE.getHudManager().getAnchorPoints()) {
            anchorPoint.updatePosition(sr);
        }
    }

    private float getJitter() {
        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;

        final float desiredTimePerSecond = rainSpeed;

        this.timer.reset();
        return Math.min(desiredTimePerSecond * seconds, 1.0f);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        // Init plexus
        if(Harakiri.INSTANCE.getPlexusEffect() == null)
            Harakiri.INSTANCE.initPlexusEffect((PlexusComponent) Harakiri.INSTANCE.getHudManager().findComponent(PlexusComponent.class));

        rainbowColor = Harakiri.INSTANCE.getHudManager().rainbowColor;

        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawDefaultBackground();

        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        // Draw the text

        if(this.bg == null)
            this.bg = new Texture("harabackground.png");

        GlStateManager.enableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();

        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        this.bg.bind();
        this.bg.render(0,0, res.getScaledWidth(), res.getScaledHeight());

        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();

        // Plexus
        Harakiri.INSTANCE.getPlexusEffect().render(mouseX, mouseY);

        final float halfWidth = res.getScaledWidth() / 2.0f;
        final float halfHeight = res.getScaledHeight() / 2.0f;
        // ugly
        //RenderUtil.drawLine(halfWidth, 0, halfWidth, res.getScaledHeight(), 1, 0x75909090);
        //RenderUtil.drawLine(0, halfHeight, res.getScaledWidth(), halfHeight, 1, 0x75909090);

        // Rainbow Border
        RenderUtil.drawLine(0, 0,0, res.getScaledHeight(), 2, rainbowColor); // Left
        RenderUtil.drawLine(0, 0, res.getScaledWidth(), 0, 2, rainbowColor); // Top
        RenderUtil.drawLine(0, res.getScaledHeight() - 1, res.getScaledWidth(), res.getScaledHeight() - 1, 1, rainbowColor); // Bottom
        RenderUtil.drawLine(res.getScaledWidth(), res.getScaledHeight(),res.getScaledWidth(), 0, 2, rainbowColor); // Right

        for (AnchorPoint point : Harakiri.INSTANCE.getHudManager().getAnchorPoints()) {
            //RenderUtil.drawRect(point.getX() - 1, point.getY() - 1, point.getX() + 1, point.getY() + 1, 0x75909090);
            //dont :)
        }

        SwitchViewComponent swc = (SwitchViewComponent)Harakiri.INSTANCE.getHudManager().findComponent(SwitchViewComponent.class);

        for (int i = 0; i < this.hudComponentsSorted.size(); ++i) {

            this.isColorPickerOpen = false;

            // Render from bottom.
            HudComponent component = this.hudComponentsSorted.get(i);

            if((component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && !swc.isModules)
                continue;

            if(!(component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && swc.isModules && component != swc)
                continue;

            if (component.isVisible()) {
                component.render(mouseX, mouseY, partialTicks);

                if (component instanceof DraggableHudComponent) {
                    DraggableHudComponent draggable = (DraggableHudComponent) component;
                    if (draggable.isDragging()) {
                        int SIZE = 2;
                        if (draggable.getW() < 12 || draggable.getH() < 12)
                            SIZE = 1;
                        else if (draggable.getW() <= 0 || draggable.getH() <= 0)
                            SIZE = 0;

                        boolean colliding = false;

                        for (HudComponent other : Harakiri.INSTANCE.getHudManager().getComponentList()) {
                            if (other instanceof DraggableHudComponent) {
                                DraggableHudComponent otherDraggable = (DraggableHudComponent) other;
                                if (other != draggable && draggable.collidesWith(otherDraggable) && otherDraggable.isVisible() && draggable.isSnappable() && otherDraggable.isSnappable()) {
                                    colliding = true;
                                    RenderUtil.drawBorderedRect(draggable.getX() - 1, draggable.getY() - 1, draggable.getX() + draggable.getW() + 1, draggable.getY() + draggable.getH() + 1, 1, 0x00000000, ColorUtil.changeAlpha(rainbowColor, 0x33));
                                    RenderUtil.drawRect(draggable.getX(), draggable.getY(), draggable.getX() + draggable.getW(), draggable.getY() + draggable.getH(), ColorUtil.changeAlpha(rainbowColor, 0x33));
                                    RenderUtil.drawBorderedRect(other.getX() - 1, other.getY() - 1, other.getX() + other.getW() + 1, other.getY() + other.getH() + 1, 1, 0x00000000, ColorUtil.changeAlpha(rainbowColor, 0x33));
                                    RenderUtil.drawRect(other.getX(), other.getY(), other.getX() + other.getW(), other.getY() + other.getH(), ColorUtil.changeAlpha(rainbowColor, 0x33));
                                }
                            }
                        }

                        // render white borders if snapable and is not colliding with any components
                        if (draggable.isSnappable() && !colliding) {
                            int snappableBackgroundColor = 0x00000000;
                            if (draggable.findClosest(mouseX, mouseY) != null) { // has an anchor point nearby
                                snappableBackgroundColor = 0x35FFFFFF;
                            }
                            RenderUtil.drawRect(draggable.getX(), draggable.getY(), draggable.getW() + draggable.getX(), draggable.getH() + draggable.getY(), 0x22DDDDDD);
                            //RenderUtil.drawBorderedRect(draggable.getX() - 1, draggable.getY() - 1, draggable.getX() + draggable.getW() + 1, draggable.getY() + draggable.getH() + 1, 1, snappableBackgroundColor, 0x90FFFFFF);
                            //RenderUtil.drawRect(draggable.getX(), draggable.getY(), draggable.getX() + SIZE, draggable.getY() + SIZE, 0x90FFFFFF);
                            //RenderUtil.drawRect(draggable.getX() + draggable.getW() - SIZE, draggable.getY(), draggable.getX() + draggable.getW(), draggable.getY() + SIZE, 0x90FFFFFF);
                            //RenderUtil.drawRect(draggable.getX(), (draggable.getY() + draggable.getH()) - SIZE, draggable.getX() + SIZE, draggable.getY() + draggable.getH(), 0x90FFFFFF);
                            //RenderUtil.drawRect(draggable.getX() + draggable.getW() - SIZE, (draggable.getY() + draggable.getH()) - SIZE, draggable.getX() + draggable.getW(), draggable.getY() + draggable.getH(), 0x90FFFFFF);
                        }

                        // dragging highlight
                        //RenderUtil.drawRect(draggable.getX(), draggable.getY(), draggable.getX() + draggable.getW(), draggable.getY() + draggable.getH(), 0x35FFFFFF);
                    }
                }
            }
        }

        swc.render(mouseX, mouseY, partialTicks);
    }

    HudComponent componentMoving = null;

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        SwitchViewComponent swc = (SwitchViewComponent)Harakiri.INSTANCE.getHudManager().findComponent(SwitchViewComponent.class);

        if(this.isColorPickerOpen){
            if(mouseX >= colorPickerX && mouseX <= colorPickerX + colorPickerXY && mouseY >= colorPickerY && mouseY <= colorPickerY + colorPickerXY){
                if(colorPickerParent != null){
                    specialColorClick = true;
                    colorPickerParent.mouseClickMove(mouseX, mouseY, clickedMouseButton);
                    specialColorClick = false;
                    return;
                }
            }
        }

        for (int i = this.hudComponentsSorted.size() - 1; i >= 0; --i) {

            // From top!!
            HudComponent component = this.hudComponentsSorted.get(i);

            if((component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && !swc.isModules)
                continue;

            if(!(component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && swc.isModules && component != swc)
                continue;

            if (component.isVisible() && component.isMouseInside(mouseX, mouseY)) {
                component.mouseClickMove(mouseX, mouseY, clickedMouseButton);
                // bring component to top
                componentMoving = component;
                this.bringComponentToTopOfScreen(component);
                break;
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            SwitchViewComponent swc = (SwitchViewComponent)Harakiri.INSTANCE.getHudManager().findComponent(SwitchViewComponent.class);
            swc.mouseClicked(mouseX, mouseY, mouseButton);
            Harakiri.INSTANCE.getPlexusEffect().onMouseClicked();

            if(this.isColorPickerOpen){
                if(mouseX >= colorPickerX && mouseX <= colorPickerX + colorPickerXY && mouseY >= colorPickerY && mouseY <= colorPickerY + colorPickerXY){
                    if(colorPickerParent != null){
                        specialColorClick = true;
                        colorPickerParent.mouseClick(mouseX,mouseY,mouseButton);
                        specialColorClick = false;
                        return;
                    }
                }
            }

            for (int i = this.hudComponentsSorted.size() - 1; i >= 0; --i) {

                // From top!!
                HudComponent component = this.hudComponentsSorted.get(i);

                if((component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && !swc.isModules)
                    continue;

                if(!(component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && swc.isModules && component != swc)
                    continue;

                if (component.isVisible() && component.isMouseInside(mouseX, mouseY)) {
                    component.mouseClick(mouseX, mouseY, mouseButton);
                    componentMoving = component;
                    this.bringComponentToTopOfScreen(component);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        SwitchViewComponent swc = (SwitchViewComponent)Harakiri.INSTANCE.getHudManager().findComponent(SwitchViewComponent.class);

        if(this.isColorPickerOpen){
            if(mouseX >= colorPickerX && mouseX <= colorPickerX + colorPickerXY && mouseY >= colorPickerY && mouseY <= colorPickerY + colorPickerXY){
                if(colorPickerParent != null){
                    specialColorClick = true;
                    colorPickerParent.mouseRelease(mouseX, mouseY, state);
                    specialColorClick = false;
                    return;
                }
            }
        }


        for (int i = this.hudComponentsSorted.size() - 1; i >= 0; --i) {

            // From top!!
            HudComponent component = this.hudComponentsSorted.get(i);

            if((component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && !swc.isModules)
                continue;

            if(!(component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && swc.isModules && component != swc)
                continue;

            if (component.isVisible() && component.isMouseInside(mouseX, mouseY)) {
                if(componentMoving != null && component != componentMoving)
                    componentMoving.mouseRelease(mouseX, mouseY, state);
                else
                    component.mouseRelease(mouseX, mouseY, state);
                break;
            }
        }
        try {
            swc.mouseReleased(mouseX, mouseY, state);
        }catch(Throwable t){
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        //harakiri.INSTANCE.getConfigManager().saveAll();

        final HudEditorModule hudEditorModule = (HudEditorModule) Harakiri.INSTANCE.getModuleManager().find(HudEditorModule.class);
        if (hudEditorModule != null) {
            if (hudEditorModule.blur.getValue()) {
                if (OpenGlHelper.shadersSupported) {
                    mc.entityRenderer.stopUseShader();
                }
            }
        }

        for (HudComponent component : Harakiri.INSTANCE.getHudManager().getComponentList()) {
            if (component instanceof DraggableHudComponent) {
                if (component.isVisible()) {
                    final DraggableHudComponent draggable = (DraggableHudComponent) component;
                    if (draggable.isDragging()) {
                        draggable.setDragging(false);
                    }

                    component.onClosed();
                }
            }
        }

        // go back to previous screen
        super.onGuiClosed();
    }

    public void unload() {
        // empty
    }

    private void bringComponentToTopOfScreen(HudComponent component){
        this.hudComponentsSorted.remove(component);
        this.hudComponentsSorted.add(component);
        this.forceCloseColorPicker = true;
    }
}
