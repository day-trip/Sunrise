package com.daytrip.shared;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;

import java.util.List;

public class ExtendedReach
{
    private final Minecraft minecraft;

    public MovingObjectPosition objectMouseOver;
    public boolean airTargeted;

    private final double range;
    private final Entity requestingEntity;

    private float partialTicks;

    public ExtendedReach(Float partialTicks, double range, Entity entity)
    {
        minecraft = Minecraft.getMinecraft();
        if (partialTicks == null)
        {
            this.partialTicks = 1.0F;
        }
        this.range = range;
        requestingEntity = entity;
    }

    public void calculateMouseOver()
    {
        if (requestingEntity != null)
        {
            if (minecraft.theWorld != null)
            {
                Entity pointedEntity = null;
                double d0 = range;

                objectMouseOver = requestingEntity.rayTrace(d0, partialTicks);
                BlockPos blockPos = objectMouseOver.getBlockPos();
                IBlockState state = minecraft.theWorld.getBlockState(blockPos);

                airTargeted = state.getBlock() == Blocks.air;

                Vec3 vec3d = requestingEntity.getPositionEyes(partialTicks);
                boolean flag = false;
                double d1 = d0;
                if (d0 > 3.0D)
                {
                    flag = true;
                }

                if (objectMouseOver != null)
                {
                    d1 = objectMouseOver.hitVec.distanceTo(vec3d);
                }

                Vec3 vec3d1 = requestingEntity.getLook(1.0F);
                Vec3 vec3d2 = vec3d.add(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0);
                Vec3 vec3d3 = null;
                List<Entity> list = minecraft.theWorld.getEntitiesInAABBexcluding(requestingEntity, requestingEntity.getEntityBoundingBox().expand(vec3d1.xCoord * d0, vec3d1.yCoord * d0, vec3d1.zCoord * d0).expand(1.0D, 1.0D, 1.0D), Entity::canBeCollidedWith);
                double d2 = d1; // d1 is either range or distance between ray trace and eye position

                for (Entity entity1 : list)
                {
                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(entity1.getCollisionBorderSize(), entity1.getCollisionBorderSize(), entity1.getCollisionBorderSize());
                    MovingObjectPosition raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2); // vector between eye position and range location
                    if (axisalignedbb.isVecInside(vec3d)) // if entity is intersected, set entity as intersected
                    {
                        if (d2 >= 0.0D)
                        {
                            pointedEntity = entity1;
                            airTargeted = false;

                            vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
                            d2 = 0.0D;
                        }
                    }
                    else if (raytraceresult != null) // run when entity is targeted...
                    {
                        double d3 = vec3d.distanceTo(raytraceresult.hitVec); // distance between eye position and range location
                        if (d3 < d2 || d2 == 0.0D)
                        {
                            pointedEntity = entity1;
                            airTargeted = false;

                            vec3d3 = raytraceresult.hitVec;
                            d2 = d3;
                        }
                    }

                }

                if (pointedEntity != null && flag && vec3d.distanceTo(vec3d3) > 3.0D) // if * AND reach is greater than 3 blocks AND
                {
                    objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec3d3, null, new BlockPos(vec3d3));
                }

                if (pointedEntity != null && (d2 < d1 || objectMouseOver == null))
                {
                    objectMouseOver = new MovingObjectPosition(pointedEntity, vec3d3);
                }

                minecraft.mcProfiler.endSection();
            }
        }
    }
}