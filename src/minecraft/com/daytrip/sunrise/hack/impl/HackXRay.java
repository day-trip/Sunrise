package com.daytrip.sunrise.hack.impl;

import com.daytrip.sunrise.hack.Hack;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class HackXRay extends Hack {
    public static final List<Block> ignores = new ArrayList<>();

    public HackXRay() {
        super(Keyboard.KEY_X, "X-Ray", "x_ray");
        ignores.add(Blocks.coal_ore);
        ignores.add(Blocks.iron_ore);
        ignores.add(Blocks.gold_ore);
        ignores.add(Blocks.diamond_ore);
        ignores.add(Blocks.emerald_ore);
        ignores.add(Blocks.redstone_ore);
        ignores.add(Blocks.lit_redstone_ore);
        ignores.add(Blocks.water);
        ignores.add(Blocks.flowing_water);
        ignores.add(Blocks.lava);
        ignores.add(Blocks.flowing_lava);
    }

    @Override
    protected void enable() {
        super.enable();
        minecraft.renderGlobal.loadRenderers();
    }

    @Override
    protected void disable() {
        super.disable();
        minecraft.renderGlobal.loadRenderers();
    }
}
