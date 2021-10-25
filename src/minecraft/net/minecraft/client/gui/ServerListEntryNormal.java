package net.minecraft.client.gui;

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerListEntryNormal implements GuiListExtended.IGuiListEntry
{
    private static final Logger logger = LogManager.getLogger();

    /**
     * The thread pool for pinging the server
     */
    private static final ThreadPoolExecutor serverPingerThreadPool = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());

    /**
     * The texture location of the server selection buttons
     */
    private static final ResourceLocation SERVER_SELECTION_BUTTONS = new ResourceLocation("textures/gui/server_selection.png");

    /**
     * The screen that opened this screen
     */
    private final GuiMultiplayer parentScreen;
    /**
     * A reference to Minecraft
     */
    private final Minecraft mc;

    private final ServerData serverData;
    private final ResourceLocation serverIconLocation;
    /**
     * The server icon as a Base64 string. This is what is stored in NBT data
     */
    private String serverIconBase64;
    private DynamicTexture serverIcon;

    /**
     * Uses to keep track when you double click a server entry
     */
    private long lastClickTime;

    protected ServerListEntryNormal(GuiMultiplayer parentScreen, ServerData data)
    {
        this.parentScreen = parentScreen;
        serverData = data;
        mc = Minecraft.getMinecraft();
        serverIconLocation = new ResourceLocation("servers/" + data.serverIP + "/icon");
        serverIcon = (DynamicTexture) mc.getTextureManager().getTexture(serverIconLocation);
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        if (!serverData.field_78841_f)
        {
            serverData.field_78841_f = true;
            serverData.pingToServer = -2L;
            serverData.serverMOTD = "";
            serverData.populationInfo = "";
            serverPingerThreadPool.submit(() -> {
                try
                {
                    parentScreen.getOldServerPinger().ping(serverData);
                }
                catch (UnknownHostException var2)
                {
                    serverData.pingToServer = -1L;
                    serverData.serverMOTD = EnumChatFormatting.DARK_RED + "Can't resolve hostname";
                }
                catch (Exception var3)
                {
                    serverData.pingToServer = -1L;
                    serverData.serverMOTD = EnumChatFormatting.DARK_RED + "Can't connect to server.";
                }
            });
        }

        boolean flag = serverData.version > 47;
        boolean flag1 = serverData.version < 47;
        boolean flag2 = flag || flag1;
        mc.fontRendererObj.drawString(serverData.serverName, x + 32 + 3, y + 1, 16777215);
        List<String> list = mc.fontRendererObj.listFormattedStringToWidth(serverData.serverMOTD, listWidth - 32 - 2);

        for (int i = 0; i < Math.min(list.size(), 2); ++i)
        {
            mc.fontRendererObj.drawString(list.get(i), x + 32 + 3, y + 12 + mc.fontRendererObj.FONT_HEIGHT * i, 8421504);
        }

        String s2 = flag2 ? EnumChatFormatting.DARK_RED + serverData.gameVersion : serverData.populationInfo;
        int j = mc.fontRendererObj.getStringWidth(s2);
        mc.fontRendererObj.drawString(s2, x + listWidth - j - 15 - 2, y + 1, 8421504);
        int k = 0;
        String s = null;
        int l;
        String s1;

        if (flag2)
        {
            l = 5;
            s1 = flag ? "Client out of date!" : "Server out of date!";
            s = serverData.playerList;
        }
        else if (serverData.field_78841_f && serverData.pingToServer != -2L)
        {
            if (serverData.pingToServer < 0L)
            {
                l = 5;
            }
            else if (serverData.pingToServer < 150L)
            {
                l = 0;
            }
            else if (serverData.pingToServer < 300L)
            {
                l = 1;
            }
            else if (serverData.pingToServer < 600L)
            {
                l = 2;
            }
            else if (serverData.pingToServer < 1000L)
            {
                l = 3;
            }
            else
            {
                l = 4;
            }

            if (serverData.pingToServer < 0L)
            {
                s1 = "(no connection)";
            }
            else
            {
                s1 = serverData.pingToServer + "ms";
                s = serverData.playerList;
            }
        }
        else
        {
            k = 1;
            l = (int)(Minecraft.getSystemTime() / 100L + (slotIndex * 2L) & 7L);

            if (l > 4)
            {
                l = 8 - l;
            }

            s1 = "Pinging...";
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(Gui.icons);
        Gui.drawModalRectWithCustomSizedTexture(x + listWidth - 15, y, (float)(k * 10), (float)(176 + l * 8), 10, 8, 256.0F, 256.0F);

        if (serverData.getBase64EncodedIconData() != null && !serverData.getBase64EncodedIconData().equals(serverIconBase64))
        {
            serverIconBase64 = serverData.getBase64EncodedIconData();
            prepareServerIcon();
            parentScreen.getServerList().saveServerList();
        }

        if (serverIcon != null)
        {
            drawServerIcon(x, y, serverIconLocation);
        }
        else
        {
            drawServerIcon(x, y, new ResourceLocation("textures/misc/unknown.png"));
        }

        int i1 = mouseX - x;
        int j1 = mouseY - y;

        if (i1 >= listWidth - 15 && i1 <= listWidth - 5 && j1 >= 0 && j1 <= 8)
        {
            parentScreen.setHoveringText(s1);
        }
        else if (i1 >= listWidth - j - 15 - 2 && i1 <= listWidth - 15 - 2 && j1 >= 0 && j1 <= 8)
        {
            parentScreen.setHoveringText(s);
        }

        if (mc.gameSettings.touchscreen || isSelected)
        {
            mc.getTextureManager().bindTexture(SERVER_SELECTION_BUTTONS);
            Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int k1 = mouseX - x;
            int l1 = mouseY - y;

            if (k1 < 32 && k1 > 16)
            {
                Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
            }
            else
            {
                Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);
            }

            if(serverData.isEditable()) {
                if (parentScreen.func_175392_a(this, slotIndex))
                {
                    if (k1 < 16 && l1 < 16)
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                    }
                    else
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                    }
                }

                if (parentScreen.func_175394_b(this, slotIndex))
                {
                    if (k1 < 16 && l1 > 16)
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                    }
                    else
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                    }
                }
            }
        }
    }

    protected void drawServerIcon(int x, int y, ResourceLocation location)
    {
        mc.getTextureManager().bindTexture(location);
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        GlStateManager.disableBlend();
    }

    private void prepareServerIcon()
    {
        if (serverData.getBase64EncodedIconData() == null)
        {
            mc.getTextureManager().deleteTexture(serverIconLocation);
            serverIcon = null;
        }
        else
        {
            ByteBuf bytebuf = Unpooled.copiedBuffer(serverData.getBase64EncodedIconData(), Charsets.UTF_8);
            ByteBuf bytebuf1 = Base64.decode(bytebuf);
            BufferedImage bufferedimage;
            label101:
            {
                try
                {
                    bufferedimage = TextureUtil.readBufferedImage(new ByteBufInputStream(bytebuf1));
                    Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                    Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                    break label101;
                }
                catch (Throwable throwable)
                {
                    logger.error("Invalid icon for server " + serverData.serverName + " (" + serverData.serverIP + ")", throwable);
                    serverData.setBase64EncodedIconData(null);
                }
                finally
                {
                    bytebuf.release();
                    bytebuf1.release();
                }

                return;
            }

            if (serverIcon == null)
            {
                serverIcon = new DynamicTexture(bufferedimage.getWidth(), bufferedimage.getHeight());
                mc.getTextureManager().loadTexture(serverIconLocation, serverIcon);
            }

            bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), serverIcon.getTextureData(), 0, bufferedimage.getWidth());
            serverIcon.updateDynamicTexture();
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control.
     */
    public boolean mousePressed(int slotIndex, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_)
    {
        if (p_148278_5_ <= 32)
        {
            if (p_148278_5_ < 32 && p_148278_5_ > 16)
            {
                parentScreen.selectServer(slotIndex);
                parentScreen.connectToSelected();
                return true;
            }

            if(serverData.isEditable()) {
                if (p_148278_5_ < 16 && p_148278_6_ < 16 && parentScreen.func_175392_a(this, slotIndex))
                {
                    parentScreen.func_175391_a(this, slotIndex, GuiScreen.isShiftKeyDown());
                    return true;
                }

                if (p_148278_5_ < 16 && p_148278_6_ > 16 && parentScreen.func_175394_b(this, slotIndex))
                {
                    parentScreen.func_175393_b(this, slotIndex, GuiScreen.isShiftKeyDown());
                    return true;
                }
            }
        }

        parentScreen.selectServer(slotIndex);

        if (Minecraft.getSystemTime() - lastClickTime < 250L)
        {
            parentScreen.connectToSelected();
        }

        lastClickTime = Minecraft.getSystemTime();
        return false;
    }

    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_)
    {
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }

    public ServerData getServerData()
    {
        return serverData;
    }
}
