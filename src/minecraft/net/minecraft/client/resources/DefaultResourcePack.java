package net.minecraft.client.resources;

import com.google.common.collect.ImmutableSet;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

public class DefaultResourcePack implements IResourcePack
{
    public static final Set<String> defaultResourceDomains = ImmutableSet.of("minecraft");
    private final Map<String, File> mapAssets;

    public DefaultResourcePack(Map<String, File> mapAssetsIn)
    {
        mapAssets = mapAssetsIn;
    }

    public InputStream getInputStream(ResourceLocation location) throws IOException
    {
        InputStream inputstream = getResourceStream(location);

        if (inputstream != null)
        {
            return inputstream;
        }
        else
        {
            InputStream inputstream1 = getInputStreamAssets(location);

            if (inputstream1 != null)
            {
                return inputstream1;
            }
            else
            {
                throw new FileNotFoundException(location.getResourcePath());
            }
        }
    }

    public InputStream getInputStreamAssets(ResourceLocation location) throws IOException {
        File file1 = mapAssets.get(location.toString());
        return file1 != null && file1.isFile() ? new FileInputStream(file1) : null;
    }

    private InputStream getResourceStream(ResourceLocation location)
    {
        return DefaultResourcePack.class.getResourceAsStream("/assets/" + location.getResourceDomain() + "/" + location.getResourcePath());
    }

    public boolean resourceExists(ResourceLocation location)
    {
        return getResourceStream(location) != null || mapAssets.containsKey(location.toString());
    }

    public Set<String> getResourceDomains()
    {
        return defaultResourceDomains;
    }

    public <T extends IMetadataSection> T getPackMetadata(IMetadataSerializer p_135058_1_, String p_135058_2_) {
        try
        {
            InputStream inputstream = new FileInputStream(mapAssets.get("pack.mcmeta"));
            return AbstractResourcePack.readMetadata(p_135058_1_, inputstream, p_135058_2_);
        }
        catch (RuntimeException | FileNotFoundException var4)
        {
            return null;
        }
    }

    public BufferedImage getPackImage() throws IOException
    {
        return TextureUtil.readBufferedImage(DefaultResourcePack.class.getResourceAsStream("/" + (new ResourceLocation("pack.png")).getResourcePath()));
    }

    public String getPackName()
    {
        return "Default";
    }
}
