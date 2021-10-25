package net.minecraft.client.renderer;

import com.google.common.primitives.Floats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;

import java.nio.*;
import java.util.Arrays;
import java.util.BitSet;

public class WorldRenderer
{
    private ByteBuffer byteBuffer;
    private IntBuffer rawIntBuffer;
    private ShortBuffer field_181676_c;
    private FloatBuffer rawFloatBuffer;
    private int vertexCount;
    private VertexFormatElement field_181677_f;
    private int field_181678_g;

    /** Boolean for whether this renderer needs to be updated or not */
    private boolean needsUpdate;
    private int drawMode;
    private double xOffset;
    private double yOffset;
    private double zOffset;
    private VertexFormat vertexFormat;
    private boolean isDrawing;

    public WorldRenderer(int bufferSizeIn)
    {
        byteBuffer = GLAllocation.createDirectByteBuffer(bufferSizeIn * 4);
        rawIntBuffer = byteBuffer.asIntBuffer();
        field_181676_c = byteBuffer.asShortBuffer();
        rawFloatBuffer = byteBuffer.asFloatBuffer();
    }

    private void func_181670_b(int p_181670_1_)
    {
        if (p_181670_1_ > rawIntBuffer.remaining())
        {
            int i = byteBuffer.capacity();
            int j = i % 2097152;
            int k = j + (((rawIntBuffer.position() + p_181670_1_) * 4 - j) / 2097152 + 1) * 2097152;
            LogManager.getLogger().warn("Needed to grow BufferBuilder buffer: Old size " + i + " bytes, new size " + k + " bytes.");
            int l = rawIntBuffer.position();
            ByteBuffer bytebuffer = GLAllocation.createDirectByteBuffer(k);
            byteBuffer.position(0);
            bytebuffer.put(byteBuffer);
            bytebuffer.rewind();
            byteBuffer = bytebuffer;
            rawFloatBuffer = byteBuffer.asFloatBuffer().asReadOnlyBuffer();
            rawIntBuffer = byteBuffer.asIntBuffer();
            rawIntBuffer.position(l);
            field_181676_c = byteBuffer.asShortBuffer();
            field_181676_c.position(l << 1);
        }
    }

    public void func_181674_a(float p_181674_1_, float p_181674_2_, float p_181674_3_)
    {
        int i = vertexCount / 4;
        float[] afloat = new float[i];

        for (int j = 0; j < i; ++j)
        {
            afloat[j] = func_181665_a(rawFloatBuffer, (float)((double)p_181674_1_ + xOffset), (float)((double)p_181674_2_ + yOffset), (float)((double)p_181674_3_ + zOffset), vertexFormat.func_181719_f(), j * vertexFormat.getNextOffset());
        }

        Integer[] ainteger = new Integer[i];

        for (int k = 0; k < ainteger.length; ++k)
        {
            ainteger[k] = k;
        }

        Arrays.sort(ainteger, (p_compare_1_, p_compare_2_) -> Floats.compare(afloat[p_compare_2_], afloat[p_compare_1_]));
        BitSet bitset = new BitSet();
        int l = vertexFormat.getNextOffset();
        int[] aint = new int[l];

        for (int l1 = 0; (l1 = bitset.nextClearBit(l1)) < ainteger.length; ++l1)
        {
            int i1 = ainteger[l1];

            if (i1 != l1)
            {
                rawIntBuffer.limit(i1 * l + l);
                rawIntBuffer.position(i1 * l);
                rawIntBuffer.get(aint);
                int j1 = i1;

                for (int k1 = ainteger[i1]; j1 != l1; k1 = ainteger[k1])
                {
                    rawIntBuffer.limit(k1 * l + l);
                    rawIntBuffer.position(k1 * l);
                    IntBuffer intbuffer = rawIntBuffer.slice();
                    rawIntBuffer.limit(j1 * l + l);
                    rawIntBuffer.position(j1 * l);
                    rawIntBuffer.put(intbuffer);
                    bitset.set(j1);
                    j1 = k1;
                }

                rawIntBuffer.limit(l1 * l + l);
                rawIntBuffer.position(l1 * l);
                rawIntBuffer.put(aint);
            }

            bitset.set(l1);
        }
    }

    public WorldRenderer.State func_181672_a()
    {
        rawIntBuffer.rewind();
        int i = func_181664_j();
        rawIntBuffer.limit(i);
        int[] aint = new int[i];
        rawIntBuffer.get(aint);
        rawIntBuffer.limit(rawIntBuffer.capacity());
        rawIntBuffer.position(i);
        return new State(aint, new VertexFormat(vertexFormat));
    }

    private int func_181664_j()
    {
        return vertexCount * vertexFormat.func_181719_f();
    }

    private static float func_181665_a(FloatBuffer p_181665_0_, float p_181665_1_, float p_181665_2_, float p_181665_3_, int p_181665_4_, int p_181665_5_)
    {
        float f = p_181665_0_.get(p_181665_5_);
        float f1 = p_181665_0_.get(p_181665_5_ + 1);
        float f2 = p_181665_0_.get(p_181665_5_ + 2);
        float f3 = p_181665_0_.get(p_181665_5_ + p_181665_4_);
        float f4 = p_181665_0_.get(p_181665_5_ + p_181665_4_ + 1);
        float f5 = p_181665_0_.get(p_181665_5_ + p_181665_4_ + 2);
        float f6 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 2);
        float f7 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 2 + 1);
        float f8 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 2 + 2);
        float f9 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 3);
        float f10 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 3 + 1);
        float f11 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 3 + 2);
        float f12 = (f + f3 + f6 + f9) * 0.25F - p_181665_1_;
        float f13 = (f1 + f4 + f7 + f10) * 0.25F - p_181665_2_;
        float f14 = (f2 + f5 + f8 + f11) * 0.25F - p_181665_3_;
        return f12 * f12 + f13 * f13 + f14 * f14;
    }

    public void setVertexState(WorldRenderer.State state)
    {
        rawIntBuffer.clear();
        func_181670_b(state.getRawBuffer().length);
        rawIntBuffer.put(state.getRawBuffer());
        vertexCount = state.getVertexCount();
        vertexFormat = new VertexFormat(state.getVertexFormat());
    }

    public void reset()
    {
        vertexCount = 0;
        field_181677_f = null;
        field_181678_g = 0;
    }

    public void begin(int p_181668_1_, VertexFormat p_181668_2_)
    {
        if (isDrawing)
        {
            throw new IllegalStateException("Already building!");
        }
        else
        {
            isDrawing = true;
            reset();
            drawMode = p_181668_1_;
            vertexFormat = p_181668_2_;
            field_181677_f = p_181668_2_.getElement(field_181678_g);
            needsUpdate = false;
            byteBuffer.limit(byteBuffer.capacity());
        }
    }

    public WorldRenderer tex(double p_181673_1_, double p_181673_3_)
    {
        int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.func_181720_d(field_181678_g);

        switch (field_181677_f.getType())
        {
            case FLOAT:
                byteBuffer.putFloat(i, (float)p_181673_1_);
                byteBuffer.putFloat(i + 4, (float)p_181673_3_);
                break;

            case UINT:
            case INT:
                byteBuffer.putInt(i, (int)p_181673_1_);
                byteBuffer.putInt(i + 4, (int)p_181673_3_);
                break;

            case USHORT:
            case SHORT:
                byteBuffer.putShort(i, (short)((int)p_181673_3_));
                byteBuffer.putShort(i + 2, (short)((int)p_181673_1_));
                break;

            case UBYTE:
            case BYTE:
                byteBuffer.put(i, (byte)((int)p_181673_3_));
                byteBuffer.put(i + 1, (byte)((int)p_181673_1_));
        }

        func_181667_k();
        return this;
    }

    public WorldRenderer lightmap(int p_181671_1_, int p_181671_2_)
    {
        int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.func_181720_d(field_181678_g);

        switch (field_181677_f.getType())
        {
            case FLOAT:
                byteBuffer.putFloat(i, (float)p_181671_1_);
                byteBuffer.putFloat(i + 4, (float)p_181671_2_);
                break;

            case UINT:
            case INT:
                byteBuffer.putInt(i, p_181671_1_);
                byteBuffer.putInt(i + 4, p_181671_2_);
                break;

            case USHORT:
            case SHORT:
                byteBuffer.putShort(i, (short)p_181671_2_);
                byteBuffer.putShort(i + 2, (short)p_181671_1_);
                break;

            case UBYTE:
            case BYTE:
                byteBuffer.put(i, (byte)p_181671_2_);
                byteBuffer.put(i + 1, (byte)p_181671_1_);
        }

        func_181667_k();
        return this;
    }

    public void putBrightness4(int p_178962_1_, int p_178962_2_, int p_178962_3_, int p_178962_4_)
    {
        int i = (vertexCount - 4) * vertexFormat.func_181719_f() + vertexFormat.getUvOffsetById(1) / 4;
        int j = vertexFormat.getNextOffset() >> 2;
        rawIntBuffer.put(i, p_178962_1_);
        rawIntBuffer.put(i + j, p_178962_2_);
        rawIntBuffer.put(i + j * 2, p_178962_3_);
        rawIntBuffer.put(i + j * 3, p_178962_4_);
    }

    public void putPosition(double x, double y, double z)
    {
        int i = vertexFormat.func_181719_f();
        int j = (vertexCount - 4) * i;

        for (int k = 0; k < 4; ++k)
        {
            int l = j + k * i;
            int i1 = l + 1;
            int j1 = i1 + 1;
            rawIntBuffer.put(l, Float.floatToRawIntBits((float)(x + xOffset) + Float.intBitsToFloat(rawIntBuffer.get(l))));
            rawIntBuffer.put(i1, Float.floatToRawIntBits((float)(y + yOffset) + Float.intBitsToFloat(rawIntBuffer.get(i1))));
            rawIntBuffer.put(j1, Float.floatToRawIntBits((float)(z + zOffset) + Float.intBitsToFloat(rawIntBuffer.get(j1))));
        }
    }

    /**
     * Takes in the pass the call list is being requested for. Args: renderPass
     */
    private int getColorIndex(int p_78909_1_)
    {
        return ((vertexCount - p_78909_1_) * vertexFormat.getNextOffset() + vertexFormat.getColorOffset()) / 4;
    }

    public void putColorMultiplier(float red, float green, float blue, int p_178978_4_)
    {
        int i = getColorIndex(p_178978_4_);
        int j = -1;

        if (!needsUpdate)
        {
            j = rawIntBuffer.get(i);

            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
            {
                int k = (int)((float)(j & 255) * red);
                int l = (int)((float)(j >> 8 & 255) * green);
                int i1 = (int)((float)(j >> 16 & 255) * blue);
                j = j & -16777216;
                j = j | i1 << 16 | l << 8 | k;
            }
            else
            {
                int j1 = (int)((float)(j >> 24 & 255) * red);
                int k1 = (int)((float)(j >> 16 & 255) * green);
                int l1 = (int)((float)(j >> 8 & 255) * blue);
                j = j & 255;
                j = j | j1 << 24 | k1 << 16 | l1 << 8;
            }
        }

        rawIntBuffer.put(i, j);
    }

    private void putColor(int argb, int p_178988_2_)
    {
        int i = getColorIndex(p_178988_2_);
        int j = argb >> 16 & 255;
        int k = argb >> 8 & 255;
        int l = argb & 255;
        int i1 = argb >> 24 & 255;
        putColorRGBA(i, j, k, l, i1);
    }

    public void putColorRGB_F(float red, float green, float blue, int p_178994_4_)
    {
        int i = getColorIndex(p_178994_4_);
        int j = MathHelper.clamp_int((int)(red * 255.0F), 0, 255);
        int k = MathHelper.clamp_int((int)(green * 255.0F), 0, 255);
        int l = MathHelper.clamp_int((int)(blue * 255.0F), 0, 255);
        putColorRGBA(i, j, k, l, 255);
    }

    private void putColorRGBA(int index, int red, int p_178972_3_, int p_178972_4_, int p_178972_5_)
    {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
        {
            rawIntBuffer.put(index, p_178972_5_ << 24 | p_178972_4_ << 16 | p_178972_3_ << 8 | red);
        }
        else
        {
            rawIntBuffer.put(index, red << 24 | p_178972_3_ << 16 | p_178972_4_ << 8 | p_178972_5_);
        }
    }

    /**
     * Marks the current renderer data as dirty and needing to be updated.
     */
    public void markDirty()
    {
        needsUpdate = true;
    }

    public WorldRenderer color(float p_181666_1_, float p_181666_2_, float p_181666_3_, float p_181666_4_)
    {
        return color((int)(p_181666_1_ * 255.0F), (int)(p_181666_2_ * 255.0F), (int)(p_181666_3_ * 255.0F), (int)(p_181666_4_ * 255.0F));
    }

    public WorldRenderer color(int p_181669_1_, int p_181669_2_, int p_181669_3_, int p_181669_4_)
    {
        if (needsUpdate)
        {
            return this;
        }
        else
        {
            int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.func_181720_d(field_181678_g);

            switch (field_181677_f.getType())
            {
                case FLOAT:
                    byteBuffer.putFloat(i, (float)p_181669_1_ / 255.0F);
                    byteBuffer.putFloat(i + 4, (float)p_181669_2_ / 255.0F);
                    byteBuffer.putFloat(i + 8, (float)p_181669_3_ / 255.0F);
                    byteBuffer.putFloat(i + 12, (float)p_181669_4_ / 255.0F);
                    break;

                case UINT:
                case INT:
                    byteBuffer.putFloat(i, (float)p_181669_1_);
                    byteBuffer.putFloat(i + 4, (float)p_181669_2_);
                    byteBuffer.putFloat(i + 8, (float)p_181669_3_);
                    byteBuffer.putFloat(i + 12, (float)p_181669_4_);
                    break;

                case USHORT:
                case SHORT:
                    byteBuffer.putShort(i, (short)p_181669_1_);
                    byteBuffer.putShort(i + 2, (short)p_181669_2_);
                    byteBuffer.putShort(i + 4, (short)p_181669_3_);
                    byteBuffer.putShort(i + 6, (short)p_181669_4_);
                    break;

                case UBYTE:
                case BYTE:
                    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
                    {
                        byteBuffer.put(i, (byte)p_181669_1_);
                        byteBuffer.put(i + 1, (byte)p_181669_2_);
                        byteBuffer.put(i + 2, (byte)p_181669_3_);
                        byteBuffer.put(i + 3, (byte)p_181669_4_);
                    }
                    else
                    {
                        byteBuffer.put(i, (byte)p_181669_4_);
                        byteBuffer.put(i + 1, (byte)p_181669_3_);
                        byteBuffer.put(i + 2, (byte)p_181669_2_);
                        byteBuffer.put(i + 3, (byte)p_181669_1_);
                    }
            }

            func_181667_k();
            return this;
        }
    }

    public void addVertexData(int[] vertexData)
    {
        func_181670_b(vertexData.length);
        rawIntBuffer.position(func_181664_j());
        rawIntBuffer.put(vertexData);
        vertexCount += vertexData.length / vertexFormat.func_181719_f();
    }

    public void endVertex()
    {
        ++vertexCount;
        func_181670_b(vertexFormat.func_181719_f());
    }

    public WorldRenderer pos(double p_181662_1_, double p_181662_3_, double p_181662_5_)
    {
        int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.func_181720_d(field_181678_g);

        switch (field_181677_f.getType())
        {
            case FLOAT:
                byteBuffer.putFloat(i, (float)(p_181662_1_ + xOffset));
                byteBuffer.putFloat(i + 4, (float)(p_181662_3_ + yOffset));
                byteBuffer.putFloat(i + 8, (float)(p_181662_5_ + zOffset));
                break;

            case UINT:
            case INT:
                byteBuffer.putInt(i, Float.floatToRawIntBits((float)(p_181662_1_ + xOffset)));
                byteBuffer.putInt(i + 4, Float.floatToRawIntBits((float)(p_181662_3_ + yOffset)));
                byteBuffer.putInt(i + 8, Float.floatToRawIntBits((float)(p_181662_5_ + zOffset)));
                break;

            case USHORT:
            case SHORT:
                byteBuffer.putShort(i, (short)((int)(p_181662_1_ + xOffset)));
                byteBuffer.putShort(i + 2, (short)((int)(p_181662_3_ + yOffset)));
                byteBuffer.putShort(i + 4, (short)((int)(p_181662_5_ + zOffset)));
                break;

            case UBYTE:
            case BYTE:
                byteBuffer.put(i, (byte)((int)(p_181662_1_ + xOffset)));
                byteBuffer.put(i + 1, (byte)((int)(p_181662_3_ + yOffset)));
                byteBuffer.put(i + 2, (byte)((int)(p_181662_5_ + zOffset)));
        }

        func_181667_k();
        return this;
    }

    public void putNormal(float x, float y, float z)
    {
        int i = (byte)((int)(x * 127.0F)) & 255;
        int j = (byte)((int)(y * 127.0F)) & 255;
        int k = (byte)((int)(z * 127.0F)) & 255;
        int l = i | j << 8 | k << 16;
        int i1 = vertexFormat.getNextOffset() >> 2;
        int j1 = (vertexCount - 4) * i1 + vertexFormat.getNormalOffset() / 4;
        rawIntBuffer.put(j1, l);
        rawIntBuffer.put(j1 + i1, l);
        rawIntBuffer.put(j1 + i1 * 2, l);
        rawIntBuffer.put(j1 + i1 * 3, l);
    }

    private void func_181667_k()
    {
        ++field_181678_g;
        field_181678_g %= vertexFormat.getElementCount();
        field_181677_f = vertexFormat.getElement(field_181678_g);

        if (field_181677_f.getUsage() == VertexFormatElement.EnumUsage.PADDING)
        {
            func_181667_k();
        }
    }

    public WorldRenderer normal(float p_181663_1_, float p_181663_2_, float p_181663_3_)
    {
        int i = vertexCount * vertexFormat.getNextOffset() + vertexFormat.func_181720_d(field_181678_g);

        switch (field_181677_f.getType())
        {
            case FLOAT:
                byteBuffer.putFloat(i, p_181663_1_);
                byteBuffer.putFloat(i + 4, p_181663_2_);
                byteBuffer.putFloat(i + 8, p_181663_3_);
                break;

            case UINT:
            case INT:
                byteBuffer.putInt(i, (int)p_181663_1_);
                byteBuffer.putInt(i + 4, (int)p_181663_2_);
                byteBuffer.putInt(i + 8, (int)p_181663_3_);
                break;

            case USHORT:
            case SHORT:
                byteBuffer.putShort(i, (short)((int)p_181663_1_ * 32767 & 65535));
                byteBuffer.putShort(i + 2, (short)((int)p_181663_2_ * 32767 & 65535));
                byteBuffer.putShort(i + 4, (short)((int)p_181663_3_ * 32767 & 65535));
                break;

            case UBYTE:
            case BYTE:
                byteBuffer.put(i, (byte)((int)p_181663_1_ * 127 & 255));
                byteBuffer.put(i + 1, (byte)((int)p_181663_2_ * 127 & 255));
                byteBuffer.put(i + 2, (byte)((int)p_181663_3_ * 127 & 255));
        }

        func_181667_k();
        return this;
    }

    public void setTranslation(double x, double y, double z)
    {
        xOffset = x;
        yOffset = y;
        zOffset = z;
    }

    public void finishDrawing()
    {
        if (!isDrawing)
        {
            throw new IllegalStateException("Not building!");
        }
        else
        {
            isDrawing = false;
            byteBuffer.position(0);
            byteBuffer.limit(func_181664_j() * 4);
        }
    }

    public ByteBuffer getByteBuffer()
    {
        return byteBuffer;
    }

    public VertexFormat getVertexFormat()
    {
        return vertexFormat;
    }

    public int getVertexCount()
    {
        return vertexCount;
    }

    public int getDrawMode()
    {
        return drawMode;
    }

    public void putColor4(int argb)
    {
        for (int i = 0; i < 4; ++i)
        {
            putColor(argb, i + 1);
        }
    }

    public void putColorRGB_F4(float red, float green, float blue)
    {
        for (int i = 0; i < 4; ++i)
        {
            putColorRGB_F(red, green, blue, i + 1);
        }
    }

    public static class State
    {
        private final int[] stateRawBuffer;
        private final VertexFormat stateVertexFormat;

        public State(int[] stateRawBuffer, VertexFormat stateVertexFormat)
        {
            this.stateRawBuffer = stateRawBuffer;
            this.stateVertexFormat = stateVertexFormat;
        }

        public int[] getRawBuffer()
        {
            return stateRawBuffer;
        }

        public int getVertexCount()
        {
            return stateRawBuffer.length / stateVertexFormat.func_181719_f();
        }

        public VertexFormat getVertexFormat()
        {
            return stateVertexFormat;
        }
    }
}
