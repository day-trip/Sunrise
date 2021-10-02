package net.minecraft.client.renderer.block.model;

import net.minecraft.util.EnumFacing;

public class BakedQuad
{
    /**
     * Joined 4 vertex records, each has 7 fields (x, y, z, shadeColor, u, v, <unused>), see
     * FaceBakery.storeVertexData()
     */
    protected final int[] vertexData;
    protected final int tintIndex;
    protected final EnumFacing face;

    public BakedQuad(int[] vertexDataIn, int tintIndexIn, EnumFacing faceIn)
    {
        vertexData = vertexDataIn;
        tintIndex = tintIndexIn;
        face = faceIn;
    }

    public int[] getVertexData()
    {
        return vertexData;
    }

    public boolean hasTintIndex()
    {
        return tintIndex != -1;
    }

    public int getTintIndex()
    {
        return tintIndex;
    }

    public EnumFacing getFace()
    {
        return face;
    }
}
