package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.profiler.crash.CrashReport;
import net.minecraft.profiler.crash.CrashReportCategory;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureMap extends AbstractTexture implements ITickableTextureObject
{
    private static final Logger logger = LogManager.getLogger();
    public static final ResourceLocation LOCATION_MISSING_TEXTURE = new ResourceLocation("missingno");
    public static final ResourceLocation locationBlocksTexture = new ResourceLocation("textures/atlas/blocks.png");
    private final List<TextureAtlasSprite> listAnimatedSprites = new ArrayList<>();
    private final Map<String, TextureAtlasSprite> mapRegisteredSprites = new HashMap<>();
    private final Map<String, TextureAtlasSprite> mapUploadedSprites = new HashMap<>();
    private final String basePath;
    private final IIconCreator iconCreator;
    private int mipmapLevels;
    private final TextureAtlasSprite missingImage;

    public TextureMap(String p_i46099_1_)
    {
        this(p_i46099_1_, null);
    }

    public TextureMap(String basePath, IIconCreator iconCreator)
    {
        missingImage = new TextureAtlasSprite("missingno");
        this.basePath = basePath;
        this.iconCreator = iconCreator;
    }

    private void initMissingImage()
    {
        int[] aint = TextureUtil.missingTextureData;
        missingImage.setIconWidth(16);
        missingImage.setIconHeight(16);
        int[][] aint1 = new int[mipmapLevels + 1][];
        aint1[0] = aint;
        missingImage.setFramesTextureData(Lists.newArrayList(new int[][][] {aint1}));
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException
    {
        if (iconCreator != null)
        {
            loadSprites(resourceManager, iconCreator);
        }
    }

    public void loadSprites(IResourceManager resourceManager, IIconCreator p_174943_2_)
    {
        mapRegisteredSprites.clear();
        p_174943_2_.registerSprites(this);
        initMissingImage();
        deleteGlTexture();
        loadTextureAtlas(resourceManager);
    }

    public void loadTextureAtlas(IResourceManager resourceManager)
    {
        int i = Minecraft.getGLMaximumTextureSize();
        Stitcher stitcher = new Stitcher(i, i, true, 0, mipmapLevels);
        mapUploadedSprites.clear();
        listAnimatedSprites.clear();
        int j = Integer.MAX_VALUE;
        int k = 1 << mipmapLevels;

        for (Map.Entry<String, TextureAtlasSprite> entry : mapRegisteredSprites.entrySet())
        {
            TextureAtlasSprite textureatlassprite = entry.getValue();
            ResourceLocation resourcelocation = new ResourceLocation(textureatlassprite.getIconName());
            ResourceLocation resourcelocation1 = completeResourceLocation(resourcelocation, 0);

            try
            {
                IResource iresource = resourceManager.getResource(resourcelocation1);
                BufferedImage[] abufferedimage = new BufferedImage[1 + mipmapLevels];
                abufferedimage[0] = TextureUtil.readBufferedImage(iresource.getInputStream());
                TextureMetadataSection texturemetadatasection = iresource.getMetadata("texture");

                if (texturemetadatasection != null)
                {
                    List<Integer> list = texturemetadatasection.getListMipmaps();

                    if (!list.isEmpty())
                    {
                        int l = abufferedimage[0].getWidth();
                        int i1 = abufferedimage[0].getHeight();

                        if (MathHelper.roundUpToPowerOfTwo(l) != l || MathHelper.roundUpToPowerOfTwo(i1) != i1)
                        {
                            throw new RuntimeException("Unable to load extra miplevels, source-texture is not power of two");
                        }
                    }

                    for (int integer : list) {
                        if (integer > 0 && integer < abufferedimage.length - 1 && abufferedimage[integer] == null) {
                            ResourceLocation resourcelocation2 = completeResourceLocation(resourcelocation, integer);

                            try {
                                abufferedimage[integer] = TextureUtil.readBufferedImage(resourceManager.getResource(resourcelocation2).getInputStream());
                            } catch (IOException ioexception) {
                                logger.error("Unable to load miplevel {} from: {}", integer, resourcelocation2, ioexception);
                            }
                        }
                    }
                }

                AnimationMetadataSection animationmetadatasection = iresource.getMetadata("animation");
                textureatlassprite.loadSprite(abufferedimage, animationmetadatasection);
            }
            catch (RuntimeException runtimeexception)
            {
                logger.error("Unable to parse metadata from " + resourcelocation1, runtimeexception);
                continue;
            }
            catch (IOException ioexception1)
            {
                logger.error("Using missing texture, unable to load " + resourcelocation1, ioexception1);
                continue;
            }

            j = Math.min(j, Math.min(textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight()));
            int l1 = Math.min(Integer.lowestOneBit(textureatlassprite.getIconWidth()), Integer.lowestOneBit(textureatlassprite.getIconHeight()));

            if (l1 < k)
            {
                logger.warn("Texture {} with size {}x{} limits mip level from {} to {}", resourcelocation1, textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight(), MathHelper.calculateLogBaseTwo(k), MathHelper.calculateLogBaseTwo(l1));
                k = l1;
            }

            stitcher.addSprite(textureatlassprite);
        }

        int j1 = Math.min(j, k);
        int k1 = MathHelper.calculateLogBaseTwo(j1);

        if (k1 < mipmapLevels)
        {
            logger.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", basePath, mipmapLevels, k1, j1);
            mipmapLevels = k1;
        }

        for (TextureAtlasSprite textureatlassprite1 : mapRegisteredSprites.values())
        {
            try
            {
                textureatlassprite1.generateMipmaps(mipmapLevels);
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Applying mipmap");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Sprite being mipmapped");
                crashreportcategory.addCrashSectionCallable("Sprite name", textureatlassprite1::getIconName);
                crashreportcategory.addCrashSectionCallable("Sprite size", () -> textureatlassprite1.getIconWidth() + " x " + textureatlassprite1.getIconHeight());
                crashreportcategory.addCrashSectionCallable("Sprite frames", () -> textureatlassprite1.getFrameCount() + " frames");
                crashreportcategory.addCrashSection("Mipmap levels", mipmapLevels);
                throw new ReportedException(crashreport);
            }
        }

        missingImage.generateMipmaps(mipmapLevels);
        stitcher.addSprite(missingImage);

        stitcher.doStitch();

        logger.info("Created: {}x{} {}-atlas", stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), basePath);
        TextureUtil.allocateTextureImpl(getGlTextureId(), mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());
        Map<String, TextureAtlasSprite> map = Maps.newHashMap(mapRegisteredSprites);

        for (TextureAtlasSprite textureatlassprite2 : stitcher.getStichSlots())
        {
            String s = textureatlassprite2.getIconName();
            map.remove(s);
            mapUploadedSprites.put(s, textureatlassprite2);

            try
            {
                TextureUtil.uploadTextureMipmap(textureatlassprite2.getFrameTextureData(0), textureatlassprite2.getIconWidth(), textureatlassprite2.getIconHeight(), textureatlassprite2.getOriginX(), textureatlassprite2.getOriginY(), false, false);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Stitching texture atlas");
                CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Texture being stitched together");
                crashreportcategory1.addCrashSection("Atlas path", basePath);
                crashreportcategory1.addCrashSection("Sprite", textureatlassprite2);
                throw new ReportedException(crashreport1);
            }

            if (textureatlassprite2.hasAnimationMetadata())
            {
                listAnimatedSprites.add(textureatlassprite2);
            }
        }

        for (TextureAtlasSprite textureatlassprite3 : map.values())
        {
            textureatlassprite3.copyFrom(missingImage);
        }
    }

    private ResourceLocation completeResourceLocation(ResourceLocation location, int p_147634_2_)
    {
        return p_147634_2_ == 0 ? new ResourceLocation(location.getResourceDomain(), String.format("%s/%s%s", basePath, location.getResourcePath(), ".png")): new ResourceLocation(location.getResourceDomain(), String.format("%s/mipmaps/%s.%d%s", basePath, location.getResourcePath(), p_147634_2_, ".png"));
    }

    public TextureAtlasSprite getAtlasSprite(String iconName)
    {
        TextureAtlasSprite textureatlassprite = mapUploadedSprites.get(iconName);

        if (textureatlassprite == null)
        {
            textureatlassprite = missingImage;
        }

        return textureatlassprite;
    }

    public void updateAnimations()
    {
        TextureUtil.bindTexture(getGlTextureId());

        for (TextureAtlasSprite textureatlassprite : listAnimatedSprites)
        {
            textureatlassprite.updateAnimation();
        }
    }

    public TextureAtlasSprite registerSprite(ResourceLocation location)
    {
        if (location == null)
        {
            throw new IllegalArgumentException("Location cannot be null!");
        }
        else
        {
            mapRegisteredSprites.putIfAbsent(location.toString(), TextureAtlasSprite.makeAtlasSprite(location));
            return mapRegisteredSprites.get(location.toString());
        }
    }

    public void tick()
    {
        updateAnimations();
    }

    public void setMipmapLevels(int mipmapLevelsIn)
    {
        mipmapLevels = mipmapLevelsIn;
    }

    public TextureAtlasSprite getMissingSprite()
    {
        return missingImage;
    }
}
