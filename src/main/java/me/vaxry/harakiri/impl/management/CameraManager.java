package me.vaxry.harakiri.impl.management;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.camera.Camera;
import me.vaxry.harakiri.api.camera.Camera2;
import me.vaxry.harakiri.api.event.minecraft.EventUpdateFramebufferSize;
import me.vaxry.harakiri.api.event.player.EventFovModifier;
import me.vaxry.harakiri.api.event.render.*;
import me.vaxry.harakiri.api.event.render.*;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 12/9/2019 @ 6:09 AM.
 */
public final class CameraManager {

    private List<Camera> cameraList = new ArrayList();
    private List<Camera2> camera2List = new ArrayList();

    public CameraManager() {
        Harakiri.INSTANCE.getEventManager().addEventListener(this);
    }

    public void update() {
        if (Minecraft.getMinecraft().inGameHasFocus && Minecraft.getMinecraft().currentScreen == null) {
            for (Camera cam : this.cameraList) {
                if (cam != null && !cam.isRecording() && cam.isRendering()) {
                    cam.updateFbo();
                }
            }
            for (Camera2 cam : this.camera2List) {
                if (cam != null && !cam.isRecording() && cam.isRendering()) {
                    cam.updateFbo();
                }
            }
        }
    }

    @Listener
    public void renderOverlay(EventRenderOverlay event) {
        if (this.isCameraRecording()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void fboResize(EventUpdateFramebufferSize event) {
        for (Camera cam : this.cameraList) {
            if (cam != null) {
                cam.resize();
            }
        }

        for (Camera2 cam : this.camera2List) {
            if (cam != null) {
                cam.resize();
            }
        }
    }

    @Listener
    public void fovModifier(EventFovModifier event) {
        if (this.isCameraRecording()) {
            event.setFov(90.0f);
            event.setCanceled(true);
        }
    }

    @Listener
    public void renderOutlines(EventRenderEntityOutlines event) {
        if (this.isCameraRecording()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void hurtCamEffect(EventHurtCamEffect event) {
        if (this.isCameraRecording()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void renderSky(EventRenderSky event) {
        if (this.isCameraRecording()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void renderBlockDamage(EventRenderBlockDamage event) {
        if (this.isCameraRecording()) {
            event.setCanceled(true);
        }
    }

    public void addCamera(Camera cam) {
        this.cameraList.add(cam);
    }

    public void addCamera2(Camera2 cam) {
        this.camera2List.add(cam);
    }

    public void unload() {
        this.cameraList.clear();
        Harakiri.INSTANCE.getEventManager().removeEventListener(this);
    }

    public boolean isCameraRecording() {
        if (Minecraft.getMinecraft().inGameHasFocus && Minecraft.getMinecraft().currentScreen == null) {
            for (Camera cam : this.cameraList) {
                if (cam != null && cam.isRecording()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Camera> getCameraList() {
        return cameraList;
    }

    public void setCameraList(List<Camera> cameraList) {
        this.cameraList = cameraList;
    }
}
