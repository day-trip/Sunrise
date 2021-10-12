package com.daytrip.sunrise.event.impl;

import com.daytrip.sunrise.event.Event;
import net.minecraft.world.chunk.Chunk;

public class EventChunkLoad extends Event {
    private Chunk chunk;

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }
}
