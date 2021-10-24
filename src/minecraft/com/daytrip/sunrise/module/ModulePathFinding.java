package com.daytrip.sunrise.module;

import com.daytrip.sunrise.util.math.AsyncHackMath;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.Arrays;
import java.util.List;

public class ModulePathFinding {
    public static final Block[] DANGER_BLOCKS = { Blocks.fire, Blocks.lava, Blocks.flowing_lava, Blocks.water, Blocks.flowing_water, Blocks.soul_sand, Blocks.web };

    private final Minecraft minecraft;

    public ModulePathFinding() {
        minecraft = Minecraft.getMinecraft();
    }

    public void run(EntityLivingBase target) {
        BlockPos pos = minecraft.thePlayer.getPosition();
        int x = pos.getX();
        int z = pos.getZ();

        float yaw = MathHelper.angle(x, target.getPosition().getX(), z, target.getPosition().getZ());

        boolean a = safe(x, z + 1);
        boolean b = safe(x + 1, z);
        boolean c = safe(x, z - 1);
        boolean d = safe(x - 1, z);

        minecraft.thePlayer.setRotationYaw(yaw);

        // 1
        if(0 <= yaw && yaw < 45) {
            if(a) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setForward();
            }
            else if(b) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setLeft();
            }
            else if(d) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setRight();
            }
            else if(c) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setBackward();
            }
        }
        // 2
        else if(45 <= yaw && yaw < 90) {
            if(b) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setForward();
            }
            else if(a) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setRight();
            }
            else if(c) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setLeft();
            }
            else if(d) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setBackward();
            }
        }
        // 3
        else if(90 <= yaw && yaw < 135) {
            if(b) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setForward();
            }
            else if(c) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setLeft();
            }
            else if(a) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setRight();
            }
            else if(d) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setBackward();
            }
        }
        // 4
        else if(135 <= yaw && yaw < 180) {
            if(c) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setForward();
            }
            else if(b) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setRight();
            }
            else if(d) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setLeft();
            }
            else if(a) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setBackward();
            }
        }
        // 5
        else if(180 <= yaw && yaw < 225) {
            if(c) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setForward();
            }
            else if(d) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setLeft();
            }
            else if(a) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setRight();
            }
            else if(b) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setBackward();
            }
        }
        // 6
        else if(225 <= yaw && yaw < 270) {
            if(d) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setForward();
            }
            else if(c) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setRight();
            }
            else if(a) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setLeft();
            }
            else if(b) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setBackward();
            }
        }
        // 7
        else if(270 <= yaw && yaw < 315) {
            if(d) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setForward();
            }
            else if(a) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setLeft();
            }
            else if(c) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setRight();
            }
            else if(b) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setBackward();
            }
        }
        // 8
        else if(315 <= yaw && yaw < 360) {
            if(a) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setForward();
            }
            else if(d) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setRight();
            }
            else if(b) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setLeft();
            }
            else if(c) {
                minecraft.thePlayer.movementInput.stopAll();
                minecraft.thePlayer.movementInput.setBackward();
            }
        }
    }

    private boolean safe(int x, int z) {
        List<Block> dangerBlocks = Arrays.asList(DANGER_BLOCKS);

        for(int i = 0; i < 3; i++) {
            if(dangerBlocks.contains(minecraft.theWorld.getBlockState(new BlockPos(x, minecraft.thePlayer.getPosition().getY() - i, z)).getBlock())) {
                return false;
            }
        }

        return true;
    }
}
