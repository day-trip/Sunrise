package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.profiler.crash.CrashReport;
import net.minecraft.profiler.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class TextureAtlasSprite
{
    private final String iconName;
    protected List<int[][]> framesTextureData = Lists.newArrayList();
    protected int[][] interpolatedFrameData;
    private AnimationMetadataSection animationMetadata;
    protected boolean rotated;
    protected int originX;
    protected int originY;
    protected int width;
    protected int height;
    private float minU;
    private float maxU;
    private float minV;
    private float maxV;
    protected int frameCounter;
    protected int tickCounter;
    private static String locationNameClock = "builtin/clock";
    private static String locationNameCompass = "builtin/compass";

    protected TextureAtlasSprite(String spriteName)
    {
        iconName = spriteName;
    }

    protected static TextureAtlasSprite makeAtlasSprite(ResourceLocation spriteResourceLocation)
    {
        String s = spriteResourceLocation.toString();
        return locationNameClock.equals(s) ? new TextureClock(s) : (locationNameCompass.equals(s) ? new TextureCompass(s) : new TextureAtlasSprite(s));
    }

    public static void setLocationNameClock(String clockName)
    {
        locationNameClock = clockName;
    }

    public static void setLocationNameCompass(String compassName)
    {
        locationNameCompass = compassName;
    }

    public void initSprite(int inX, int inY, int originInX, int originInY, boolean rotatedIn)
    {
        originX = originInX;
        originY = originInY;
        rotated = rotatedIn;
        float f = (float)(0.009999999776482582D / (double)inX);
        float f1 = (float)(0.009999999776482582D / (double)inY);
        minU = (float)originInX / (float)((double)inX) + f;
        maxU = (float)(originInX + width) / (float)((double)inX) - f;
        minV = (float)originInY / (float)inY + f1;
        maxV = (float)(originInY + height) / (float)inY - f1;
    }

    public void copyFrom(TextureAtlasSprite atlasSpirit)
    {
        originX = atlasSpirit.originX;
        originY = atlasSpirit.originY;
        width = atlasSpirit.width;
        height = atlasSpirit.height;
        rotated = atlasSpirit.rotated;
        minU = atlasSpirit.minU;
        maxU = atlasSpirit.maxU;
        minV = atlasSpirit.minV;
        maxV = atlasSpirit.maxV;
    }

    /**
     * Returns the X position of this icon on its texture sheet, in pixels.
     */
    public int getOriginX()
    {
        return originX;
    }

    /**
     * Returns the Y position of this icon on its texture sheet, in pixels.
     */
    public int getOriginY()
    {
        return originY;
    }

    /**
     * Returns the width of the icon, in pixels.
     */
    public int getIconWidth()
    {
        return width;
    }

    /**
     * Returns the height of the icon, in pixels.
     */
    public int getIconHeight()
    {
        return height;
    }

    /**
     * Returns the minimum U coordinate to use when rendering with this icon.
     */
    public float getMinU()
    {
        return minU;
    }

    /**
     * Returns the maximum U coordinate to use when rendering with this icon.
     */
    public float getMaxU()
    {
        return maxU;
    }

    /**
     * Gets a U coordinate on the icon. 0 returns uMin and 16 returns uMax. Other arguments return in-between values.
     */
    public float getInterpolatedU(double u)
    {
        float f = maxU - minU;
        return minU + f * (float)u / 16.0F;
    }

    /**
     * Returns the minimum V coordinate to use when rendering with this icon.
     */
    public float getMinV()
    {
        return minV;
    }

    /**
     * Returns the maximum V coordinate to use when rendering with this icon.
     */
    public float getMaxV()
    {
        return maxV;
    }

    /**
     * Gets a V coordinate on the icon. 0 returns vMin and 16 returns vMax. Other arguments return in-between values.
     */
    public float getInterpolatedV(double v)
    {
        float f = maxV - minV;
        return minV + f * ((float)v / 16.0F);
    }

    public String getIconName()
    {
        return iconName;
    }

    public void updateAnimation()
    {
        ++tickCounter;

        if (tickCounter >= animationMetadata.getFrameTimeSingle(frameCounter))
        {
            int i = animationMetadata.getFrameIndex(frameCounter);
            int j = animationMetadata.getFrameCount() == 0 ? framesTextureData.size() : animationMetadata.getFrameCount();
            frameCounter = (frameCounter + 1) % j;
            tickCounter = 0;
            int k = animationMetadata.getFrameIndex(frameCounter);

            if (i != k && k >= 0 && k < framesTextureData.size())
            {
                TextureUtil.uploadTextureMipmap(framesTextureData.get(k), width, height, originX, originY, false, false);
            }
        }
        else if (animationMetadata.isInterpolate())
        {
            updateAnimationInterpolated();
        }
    }

    private void updateAnimationInterpolated()
    {
        double d0 = 1.0D - (double) tickCounter / (double) animationMetadata.getFrameTimeSingle(frameCounter);
        int i = animationMetadata.getFrameIndex(frameCounter);
        int j = animationMetadata.getFrameCount() == 0 ? framesTextureData.size() : animationMetadata.getFrameCount();
        int k = animationMetadata.getFrameIndex((frameCounter + 1) % j);

        if (i != k && k >= 0 && k < framesTextureData.size())
        {
            int[][] aint = framesTextureData.get(i);
            int[][] aint1 = framesTextureData.get(k);

            if (interpolatedFrameData == null || interpolatedFrameData.length != aint.length)
            {
                interpolatedFrameData = new int[aint.length][];
            }

            for (int l = 0; l < aint.length; ++l)
            {
                if (interpolatedFrameData[l] == null)
                {
                    interpolatedFrameData[l] = new int[aint[l].length];
                }

                if (l < aint1.length && aint1[l].length == aint[l].length)
                {
                    for (int i1 = 0; i1 < aint[l].length; ++i1)
                    {
                        int j1 = aint[l][i1];
                        int k1 = aint1[l][i1];
                        int l1 = (int)((double)((j1 & 16711680) >> 16) * d0 + (double)((k1 & 16711680) >> 16) * (1.0D - d0));
                        int i2 = (int)((double)((j1 & 65280) >> 8) * d0 + (double)((k1 & 65280) >> 8) * (1.0D - d0));
                        int j2 = (int)((double)(j1 & 255) * d0 + (double)(k1 & 255) * (1.0D - d0));
                        interpolatedFrameData[l][i1] = j1 & -16777216 | l1 << 16 | i2 << 8 | j2;
                    }
                }
            }

            TextureUtil.uploadTextureMipmap(interpolatedFrameData, width, height, originX, originY, false, false);
        }
    }

    public int[][] getFrameTextureData(int index)
    {
        return framesTextureData.get(index);
    }

    public int getFrameCount()
    {
        return framesTextureData.size();
    }

    public void setIconWidth(int newWidth)
    {
        width = newWidth;
    }

    public void setIconHeight(int newHeight)
    {
        height = newHeight;
    }

    public void loadSprite(BufferedImage[] images, AnimationMetadataSection meta) throws IOException
    {
        resetSprite();
        int i = images[0].getWidth();
        int j = images[0].getHeight();
        width = i;
        height = j;
        int[][] aint = new int[images.length][];

        for (int k = 0; k < images.length; ++k)
        {
            BufferedImage bufferedimage = images[k];

            if (bufferedimage != null)
            {
                if (k > 0 && (bufferedimage.getWidth() != i >> k || bufferedimage.getHeight() != j >> k))
                {
                    throw new RuntimeException(String.format("Unable to load miplevel: %d, image is size: %dx%d, expected %dx%d", k, bufferedimage.getWidth(), bufferedimage.getHeight(), i >> k, j >> k));
                }

                aint[k] = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
                bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), aint[k], 0, bufferedimage.getWidth());
            }
        }

        if (meta == null)
        {
            if (j != i)
            {
                throw new RuntimeException("broken aspect ratio and not an animation");
            }

            framesTextureData.add(aint);
        }
        else
        {
            int j1 = j / i;
            height = width;

            if (meta.getFrameCount() > 0)
            {

                for (int i1 : meta.getFrameIndexSet()) {
                    if (i1 >= j1) {
                        throw new RuntimeException("invalid frameindex " + i1);
                    }

                    allocateFrameTextureData(i1);
                    framesTextureData.set(i1, getFrameTextureData(aint, i, i, i1));
                }

                animationMetadata = meta;
            }
            else
            {
                List<AnimationFrame> list = Lists.newArrayList();

                for (int l1 = 0; l1 < j1; ++l1)
                {
                    framesTextureData.add(getFrameTextureData(aint, i, i, l1));
                    list.add(new AnimationFrame(l1, -1));
                }

                animationMetadata = new AnimationMetadataSection(list, width, height, meta.getFrameTime(), meta.isInterpolate());
            }
        }
    }

    public void generateMipmaps(int level)
    {
        List<int[][]> list = Lists.newArrayList();

        for (int i = 0; i < framesTextureData.size(); ++i)
        {
            int[][] aint = framesTextureData.get(i);

            if (aint != null)
            {
                try
                {
                    list.add(TextureUtil.generateMipmapData(level, width, aint));
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Generating mipmaps for frame");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Frame being iterated");
                    crashreportcategory.addCrashSection("Frame index", i);
                    crashreportcategory.addCrashSectionCallable("Frame sizes", () -> {
                        StringBuilder stringbuilder = new StringBuilder();

                        for (int[] aint1 : aint)
                        {
                            if (stringbuilder.length() > 0)
                            {
                                stringbuilder.append(", ");
                            }

                            stringbuilder.append(aint1 == null ? "null" : Integer.valueOf(aint1.length));
                        }

                        return stringbuilder.toString();
                    });
                    throw new ReportedException(crashreport);
                }
            }
        }

        setFramesTextureData(list);
    }

    private void allocateFrameTextureData(int index)
    {
        if (framesTextureData.size() <= index)
        {
            for (int i = framesTextureData.size(); i <= index; ++i)
            {
                framesTextureData.add(null);
            }
        }
    }

    private static int[][] getFrameTextureData(int[][] data, int rows, int columns, int p_147962_3_)
    {
        int[][] aint = new int[data.length][];

        for (int i = 0; i < data.length; ++i)
        {
            int[] aint1 = data[i];

            if (aint1 != null)
            {
                aint[i] = new int[(rows >> i) * (columns >> i)];
                System.arraycopy(aint1, p_147962_3_ * aint[i].length, aint[i], 0, aint[i].length);
            }
        }

        return aint;
    }

    public void clearFramesTextureData()
    {
        framesTextureData.clear();
    }

    public boolean hasAnimationMetadata()
    {
        return animationMetadata != null;
    }

    public void setFramesTextureData(List<int[][]> newFramesTextureData)
    {
        framesTextureData = newFramesTextureData;
    }

    private void resetSprite()
    {
        animationMetadata = null;
        setFramesTextureData(Lists.newArrayList());
        frameCounter = 0;
        tickCounter = 0;
    }

    public String toString()
    {
        return "TextureAtlasSprite{name='" + iconName + '\'' + ", frameCount=" + framesTextureData.size() + ", rotated=" + rotated + ", x=" + originX + ", y=" + originY + ", height=" + height + ", width=" + width + ", u0=" + minU + ", u1=" + maxU + ", v0=" + minV + ", v1=" + maxV + '}';
    }
}
