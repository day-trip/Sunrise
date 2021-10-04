package net.minecraft.client.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadDownloadImageData extends SimpleTexture
{
    private static final Logger logger = LogManager.getLogger();
    private static final AtomicInteger threadDownloadCounter = new AtomicInteger(0);
    private final File cacheFile;
    private final String imageUrl;
    private final IImageBuffer imageBuffer;
    private BufferedImage bufferedImage;
    private Thread imageThread;
    private boolean textureUploaded;

    public ThreadDownloadImageData(File cacheFileIn, String imageUrlIn, ResourceLocation textureResourceLocation, IImageBuffer imageBufferIn)
    {
        super(textureResourceLocation);
        cacheFile = cacheFileIn;
        imageUrl = imageUrlIn;
        imageBuffer = imageBufferIn;
    }

    private void checkTextureUploaded()
    {
        if (!textureUploaded)
        {
            if (bufferedImage != null)
            {
                if (textureLocation != null)
                {
                    deleteGlTexture();
                }

                TextureUtil.uploadTextureImage(super.getGlTextureId(), bufferedImage);
                textureUploaded = true;
            }
        }
    }

    public int getGlTextureId()
    {
        checkTextureUploaded();
        return super.getGlTextureId();
    }

    public void setBufferedImage(BufferedImage bufferedImageIn)
    {
        bufferedImage = bufferedImageIn;

        if (imageBuffer != null)
        {
            imageBuffer.skinAvailable();
        }
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException
    {
        if (bufferedImage == null && textureLocation != null)
        {
            super.loadTexture(resourceManager);
        }

        if (imageThread == null)
        {
            if (cacheFile != null && cacheFile.isFile())
            {
                logger.debug("Loading http texture from local cache ({})", cacheFile);

                try
                {
                    bufferedImage = ImageIO.read(cacheFile);

                    if (imageBuffer != null)
                    {
                        setBufferedImage(imageBuffer.parseUserSkin(bufferedImage));
                    }
                }
                catch (IOException ioexception)
                {
                    logger.error("Couldn't load skin " + cacheFile, ioexception);
                    loadTextureFromServer();
                }
            }
            else
            {
                loadTextureFromServer();
            }
        }
    }

    protected void loadTextureFromServer()
    {
        imageThread = new Thread("Texture Downloader #" + threadDownloadCounter.incrementAndGet())
        {
            public void run()
            {
                HttpURLConnection httpurlconnection = null;
                logger.debug("Downloading http texture from {} to {}", imageUrl, cacheFile);

                try
                {
                    httpurlconnection = (HttpURLConnection)(new URL(imageUrl)).openConnection(Minecraft.getMinecraft().getProxy());
                    httpurlconnection.setDoInput(true);
                    httpurlconnection.setDoOutput(false);
                    httpurlconnection.connect();

                    if (httpurlconnection.getResponseCode() / 100 == 2)
                    {
                        BufferedImage bufferedimage;

                        if (cacheFile != null)
                        {
                            FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), cacheFile);
                            bufferedimage = ImageIO.read(cacheFile);
                        }
                        else
                        {
                            bufferedimage = TextureUtil.readBufferedImage(httpurlconnection.getInputStream());
                        }

                        if (imageBuffer != null)
                        {
                            bufferedimage = imageBuffer.parseUserSkin(bufferedimage);
                        }

                        setBufferedImage(bufferedimage);
                    }
                }
                catch (Exception exception)
                {
                    logger.error("Couldn't download http texture", exception);
                }
                finally
                {
                    if (httpurlconnection != null)
                    {
                        httpurlconnection.disconnect();
                    }
                }
            }
        };
        imageThread.setDaemon(true);
        imageThread.start();
    }
}
