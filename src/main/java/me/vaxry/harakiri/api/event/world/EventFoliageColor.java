package me.vaxry.harakiri.api.event.world;

import me.vaxry.harakiri.api.event.EventCancellable;

/**
 * Author Seth
 * 8/11/2019 @ 2:44 AM.
 */
public class EventFoliageColor extends EventCancellable {

    private int color;

    public EventFoliageColor() {
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
