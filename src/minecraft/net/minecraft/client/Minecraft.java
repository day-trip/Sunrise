package net.minecraft.client;

import com.daytrip.sunrise.util.math.CommonMath;
import com.daytrip.sunrise.gui.LoadingManager;
import com.daytrip.sunrise.SunriseClient;
import com.daytrip.sunrise.event.Event;
import com.daytrip.sunrise.event.EventBus;
import com.daytrip.sunrise.event.EventListener;
import com.daytrip.sunrise.event.impl.*;
import com.google.common.collect.*;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.stream.GuiStreamUnavailable;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.*;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.stream.IStream;
import net.minecraft.client.stream.NullStream;
import net.minecraft.client.stream.TwitchStream;
import net.minecraft.profiler.crash.CrashReport;
import net.minecraft.profiler.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.profiler.IPlayerUsage;
import net.minecraft.profiler.PlayerUsageSnooper;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.block.tileentity.TileEntity;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraft.util.*;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class Minecraft implements IThreadListener, IPlayerUsage, EventListener
{
    public static final Logger logger = LogManager.getLogger();
    public static final boolean isRunningOnMac = Util.getOSType() == Util.EnumOS.OSX;

    /** A 10MiB preallocation to ensure the heap is reasonably sized. */
    public static byte[] memoryReserve = new byte[10485760];
    private static final List<DisplayMode> macDisplayModes = Lists.newArrayList(new DisplayMode(2560, 1600), new DisplayMode(2880, 1800));
    private final File fileResourcepacks;
    private final PropertyMap twitchDetails;
    private final PropertyMap field_181038_N;
    private ServerData currentServerData;

    /** The RenderEngine instance used by Minecraft */
    private TextureManager renderEngine;

    /**
     * Set to 'this' in Minecraft constructor; used by some settings get methods
     */
    private static Minecraft theMinecraft;
    public PlayerControllerMP playerController;
    private boolean fullscreen;
    private boolean hasCrashed;

    /** Instance of CrashReport. */
    private CrashReport crashReporter;
    public int displayWidth;
    public int displayHeight;
    private boolean field_181541_X;
    public final Timer timer = new Timer(20.0F);

    /** Instance of PlayerUsageSnooper. */
    private final PlayerUsageSnooper usageSnooper = new PlayerUsageSnooper("client", this, MinecraftServer.getCurrentTimeMillis());
    public WorldClient theWorld;
    public RenderGlobal renderGlobal;
    private RenderManager renderManager;
    private RenderItem renderItem;
    private ItemRenderer itemRenderer;
    public EntityPlayerSP thePlayer;
    private Entity renderViewEntity;
    public Entity pointedEntity;
    public EffectRenderer effectRenderer;
    public Session session;
    private boolean isGamePaused;

    /** The font renderer used for displaying and measuring text */
    public FontRenderer fontRendererObj;
    public FontRenderer smoothFontRendererObj;
    public FontRenderer standardGalacticFontRenderer;

    /** The GuiScreen that's being displayed at the moment. */
    public GuiScreen currentScreen;
    public LoadingScreenRenderer loadingScreen;
    public EntityRenderer entityRenderer;

    /** Mouse left click counter */
    private int leftClickCounter;

    /** Display width */
    private final int tempDisplayWidth;

    /** Display height */
    private final int tempDisplayHeight;

    /** Instance of IntegratedServer. */
    private IntegratedServer theIntegratedServer;

    /** Gui achievement */
    public GuiAchievement guiAchievement;
    public GuiIngame ingameGUI;

    /** Skip render world */
    public boolean skipRenderWorld;

    /** The ray trace hit that the mouse is over. */
    public MovingObjectPosition objectMouseOver;

    /** The game settings that currently hold effect. */
    public GameSettings gameSettings;

    /** Mouse helper instance. */
    public MouseHelper mouseHelper;
    public final File mcDataDir;
    private final File fileAssets;
    private final String launchedVersion;
    private final Proxy proxy;
    private ISaveFormat saveLoader;

    /**
     * This is set to fpsCounter every debug screen update, and is shown on the debug screen. It's also sent as part of
     * the usage snooping.
     */
    private static int debugFPS;

    /**
     * When you place a block, it's set to 6, decremented once per tick, when it's 0, you can place another block.
     */
    private int rightClickDelayTimer;
    private String serverName;
    private int serverPort;

    /**
     * Does the actual gameplay have focus. If so then mouse and keys will effect the player instead of menus.
     */
    public boolean inGameHasFocus;
    long systemTime = getSystemTime();

    /** Join player counter */
    private int joinPlayerCounter;
    public final FrameTimer field_181542_y = new FrameTimer();
    long field_181543_z = System.nanoTime();
    private final boolean jvm64bit;
    private NetworkManager myNetworkManager;
    private boolean integratedServerIsRunning;

    /** The profiler instance */
    public final Profiler mcProfiler = new Profiler();

    /**
     * Keeps track of how long the debug crash keycombo (F3+C) has been pressed for, in order to crash after 10 seconds.
     */
    private long debugCrashKeyPressTime = -1L;
    private IReloadableResourceManager mcResourceManager;
    private final IMetadataSerializer metadataSerializer_ = new IMetadataSerializer();
    private final List<IResourcePack> defaultResourcePacks = Lists.newArrayList();
    private final DefaultResourcePack mcDefaultResourcePack;
    private ResourcePackRepository mcResourcePackRepository;
    private LanguageManager mcLanguageManager;
    private IStream stream;
    private Framebuffer framebufferMc;
    private TextureMap textureMapBlocks;
    private SoundHandler mcSoundHandler;
    private MusicTicker mcMusicTicker;
    private ResourceLocation mojangLogo;
    private final MinecraftSessionService sessionService;
    private SkinManager skinManager;
    private final Queue < FutureTask<? >> scheduledTasks = Queues.newArrayDeque();
    private final Thread mcThread = Thread.currentThread();

    /**
     * The BlockRenderDispatcher instance that will be used based off gamesettings
     */
    private BlockRendererDispatcher blockRenderDispatcher;

    /**
     * Set to true to keep the game loop running. Set to false by shutdown() to allow the game loop to exit cleanly.
     */
    volatile boolean running = true;

    /** String that shows the debug information */
    public String debug = "";
    public boolean renderChunksMany = true;

    /** Approximate time (in ms) of last update to debug string */
    long debugUpdateTime = getSystemTime();

    /** holds the current fps */
    int fpsCounter;
    long prevFrameTime = -1L;

    /** Profiler currently displayed in the debug screen pie chart */
    private String debugProfilerName = "root";

    public Minecraft(GameConfiguration gameConfig)
    {
        logger.info("Minecraft initialization starting!");
        theMinecraft = this;
        mcDataDir = gameConfig.folderInfo.mcDataDir;
        fileAssets = gameConfig.folderInfo.assetsDir;
        fileResourcepacks = gameConfig.folderInfo.resourcePacksDir;
        launchedVersion = gameConfig.gameInfo.version;
        twitchDetails = gameConfig.userInfo.userProperties;
        field_181038_N = gameConfig.userInfo.field_181172_c;
        mcDefaultResourcePack = new DefaultResourcePack((new ResourceIndex(gameConfig.folderInfo.assetsDir, gameConfig.folderInfo.assetIndex)).getResourceMap());
        proxy = gameConfig.userInfo.proxy == null ? Proxy.NO_PROXY : gameConfig.userInfo.proxy;
        sessionService = (new YggdrasilAuthenticationService(gameConfig.userInfo.proxy, UUID.randomUUID().toString())).createMinecraftSessionService();
        session = gameConfig.userInfo.session;
        displayWidth = gameConfig.displayInfo.width > 0 ? gameConfig.displayInfo.width : 1;
        displayHeight = gameConfig.displayInfo.height > 0 ? gameConfig.displayInfo.height : 1;
        tempDisplayWidth = gameConfig.displayInfo.width;
        tempDisplayHeight = gameConfig.displayInfo.height;
        fullscreen = gameConfig.displayInfo.fullscreen;
        jvm64bit = isJvm64bit();
        theIntegratedServer = new IntegratedServer(this);

        if (gameConfig.serverInfo.serverName != null)
        {
            serverName = gameConfig.serverInfo.serverName;
            serverPort = gameConfig.serverInfo.serverPort;
        }

        ImageIO.setUseCache(false);
        Bootstrap.register();
    }

    public void run()
    {
        running = true;

        try
        {
            //EventBus.initBus();

            EventBus.registerListener(new SunriseClient());

            new EventRegisterListeners().post();

            EventBus.registerListener(this);

            new EventGamePreInit().post();
            new EventGameInit().post();
            new EventGamePostInit().post();
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Initializing game");
            crashreport.makeCategory("Initialization");
            displayCrashReport(addGraphicsAndWorldToCrashReport(crashreport));
            return;
        }

        while (true)
        {
            try
            {
                while (running)
                {
                    if (!hasCrashed || crashReporter == null)
                    {
                        try
                        {
                            runGameLoop();
                        }
                        catch (OutOfMemoryError var10)
                        {
                            freeMemory();
                            displayGuiScreen(new GuiMemoryErrorScreen());
                            System.gc();
                        }
                    }
                    else
                    {
                        displayCrashReport(crashReporter);
                    }

                }
            }
            catch (MinecraftError var12)
            {
                break;
            }
            catch (ReportedException reportedexception)
            {
                addGraphicsAndWorldToCrashReport(reportedexception.getCrashReport());
                freeMemory();
                logger.fatal("Reported exception thrown!", reportedexception);
                displayCrashReport(reportedexception.getCrashReport());
                break;
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport1 = addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", throwable1));
                freeMemory();
                logger.fatal("Unreported exception thrown!", throwable1);
                displayCrashReport(crashreport1);
                break;
            }
            finally
            {
                shutdownMinecraftApplet();
            }

            return;
        }
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if(event instanceof EventGamePreInit) {
            preInit();
        }
        if(event instanceof EventGameInit) {
            init();
        }
        if(event instanceof EventGamePostInit) {
            postInit();
        }
        if(event instanceof EventClickMouse) {
            if(((EventClickMouse) event).getButton() == 0) {
                clickMouse();
            }
            if(((EventClickMouse) event).getButton() == 1) {
                rightClickMouse();
            }
            if(((EventClickMouse) event).getButton() == 2) {
                middleClickMouse();
            }
        }
        if(event instanceof EventKeypress) {
            logger.info(((EventKeypress) event).getKey());
            keyPress(((EventKeypress) event).getKey());
        }
    }

    private void postInit() {
        checkGLError("Post startup");
        ingameGUI = new GuiIngame(this);
        LoadingManager.next();

        if (serverName != null)
        {
            displayGuiScreen(new GuiConnecting(new GuiMainMenu(), this, serverName, serverPort));
        }
        else
        {
            displayGuiScreen(new GuiMainMenu());
        }

        renderEngine.deleteTexture(mojangLogo);
        mojangLogo = null;
        loadingScreen = new LoadingScreenRenderer(this);

        if (gameSettings.fullScreen && !fullscreen)
        {
            toggleFullscreen();
        }

        try
        {
            Display.setVSyncEnabled(gameSettings.enableVsync);
        }
        catch (OpenGLException var2)
        {
            gameSettings.enableVsync = false;
            gameSettings.saveOptions();
        }

        renderGlobal.makeEntityOutlineShader();
    }

    private void init() {
        checkGLError("Startup");
        textureMapBlocks = new TextureMap("textures");
        textureMapBlocks.setMipmapLevels(gameSettings.mipmapLevels);
        LoadingManager.next();
        drawSplashScreen();
        renderEngine.loadTickableTexture(TextureMap.locationBlocksTexture, textureMapBlocks);
        renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        LoadingManager.next();
        drawSplashScreen();
        textureMapBlocks.setBlurMipmapDirect(false, gameSettings.mipmapLevels > 0);
        LoadingManager.next();
        drawSplashScreen();
        ModelManager modelManager = new ModelManager(textureMapBlocks);
        mcResourceManager.registerReloadListener(modelManager);
        renderItem = new RenderItem(renderEngine, modelManager);
        renderManager = new RenderManager(renderEngine, renderItem);
        LoadingManager.next();
        drawSplashScreen();
        itemRenderer = new ItemRenderer(this);
        mcResourceManager.registerReloadListener(renderItem);
        entityRenderer = new EntityRenderer(this, mcResourceManager);
        mcResourceManager.registerReloadListener(entityRenderer);
        LoadingManager.next();
        drawSplashScreen();
        blockRenderDispatcher = new BlockRendererDispatcher(modelManager.getBlockModelShapes(), gameSettings);
        mcResourceManager.registerReloadListener(blockRenderDispatcher);
        renderGlobal = new RenderGlobal(this);
        mcResourceManager.registerReloadListener(renderGlobal);
        guiAchievement = new GuiAchievement(this);
        LoadingManager.next();
        drawSplashScreen();
        GlStateManager.viewport(0, 0, displayWidth, displayHeight);
        LoadingManager.next();
        drawSplashScreen();
        effectRenderer = new EffectRenderer(theWorld, renderEngine);
        LoadingManager.next();
        drawSplashScreen();
    }

    private void preInit() throws LWJGLException {
        LoadingManager.setTotal(28);
        gameSettings = new GameSettings(this, mcDataDir);
        LoadingManager.next();
        defaultResourcePacks.add(mcDefaultResourcePack);
        LoadingManager.next();
        startTimerHackThread();

        if (gameSettings.overrideHeight > 0 && gameSettings.overrideWidth > 0)
        {
            displayWidth = gameSettings.overrideWidth;
            displayHeight = gameSettings.overrideHeight;
        }

        LoadingManager.next();

        logger.info("LWJGL Version: " + Sys.getVersion());
        setWindowIcon();
        setInitialDisplayMode();
        createDisplay();
        LoadingManager.next();
        OpenGlHelper.initializeTextures();
        LoadingManager.next();
        framebufferMc = new Framebuffer(displayWidth, displayHeight, true);
        framebufferMc.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        LoadingManager.next();
        registerMetadataSerializers();
        LoadingManager.next();
        mcResourcePackRepository = new ResourcePackRepository(fileResourcepacks, new File(mcDataDir, "server-resource-packs"), mcDefaultResourcePack, metadataSerializer_, gameSettings);
        mcResourceManager = new SimpleReloadableResourceManager(metadataSerializer_);
        mcLanguageManager = new LanguageManager(metadataSerializer_, gameSettings.language);
        mcResourceManager.registerReloadListener(mcLanguageManager);
        refreshResources();
        LoadingManager.next();
        drawSplashScreen();
        renderEngine = new TextureManager(mcResourceManager);
        mcResourceManager.registerReloadListener(renderEngine);
        LoadingManager.next();
        fontRendererObj = new FontRenderer(gameSettings, new ResourceLocation("textures/font/ascii.png"), renderEngine, false);
        smoothFontRendererObj = new FontRenderer(gameSettings, new ResourceLocation("textures/font/ascii_smooth.png"), renderEngine, false);
        LoadingManager.next();
        drawSplashScreen();
        initStream();
        LoadingManager.next();
        drawSplashScreen();
        skinManager = new SkinManager(renderEngine, new File(fileAssets, "skins"), sessionService);
        LoadingManager.next();
        drawSplashScreen();
        saveLoader = new AnvilSaveConverter(new File(mcDataDir, "saves"));
        LoadingManager.next();
        drawSplashScreen();
        mcSoundHandler = new SoundHandler(mcResourceManager, gameSettings);
        LoadingManager.next();
        drawSplashScreen();
        mcResourceManager.registerReloadListener(mcSoundHandler);
        LoadingManager.next();
        drawSplashScreen();
        mcMusicTicker = new MusicTicker(this);
        LoadingManager.next();
        drawSplashScreen();

        if (gameSettings.language != null)
        {
            fontRendererObj.setUnicodeFlag(isUnicode());
            fontRendererObj.setBidiFlag(mcLanguageManager.isCurrentLanguageBidirectional());
        }

        standardGalacticFontRenderer = new FontRenderer(gameSettings, new ResourceLocation("textures/font/ascii_sga.png"), renderEngine, false);
        mcResourceManager.registerReloadListener(fontRendererObj);
        mcResourceManager.registerReloadListener(smoothFontRendererObj);
        mcResourceManager.registerReloadListener(standardGalacticFontRenderer);
        LoadingManager.next();
        drawSplashScreen();
        mcResourceManager.registerReloadListener(new GrassColorReloadListener());
        mcResourceManager.registerReloadListener(new FoliageColorReloadListener());
        AchievementList.openInventory.setStatStringFormatter(p_74535_1_ -> {
            try
            {
                return String.format(p_74535_1_, GameSettings.getKeyDisplayString(gameSettings.keyBindInventory.getKeyCode()));
            }
            catch (Exception exception)
            {
                return "Error: " + exception.getLocalizedMessage();
            }
        });
        LoadingManager.next();
        drawSplashScreen();
        mouseHelper = new MouseHelper();
        checkGLError("Pre startup");
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(7425);
        GlStateManager.clearDepth(1.0D);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(515);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.cullFace(1029);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        LoadingManager.next();
        drawSplashScreen();
    }

    private void registerMetadataSerializers()
    {
        metadataSerializer_.registerMetadataSectionType(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
        metadataSerializer_.registerMetadataSectionType(new FontMetadataSectionSerializer(), FontMetadataSection.class);
        metadataSerializer_.registerMetadataSectionType(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
        metadataSerializer_.registerMetadataSectionType(new PackMetadataSectionSerializer(), PackMetadataSection.class);
        metadataSerializer_.registerMetadataSectionType(new LanguageMetadataSectionSerializer(), LanguageMetadataSection.class);
    }

    private void initStream()
    {
        try
        {
            stream = new TwitchStream(this, Iterables.getFirst(twitchDetails.get("twitch_access_token"), null));
        }
        catch (Throwable throwable)
        {
            stream = new NullStream(throwable);
            logger.error("Couldn't initialize twitch stream");
        }
    }

    private void createDisplay() throws LWJGLException
    {
        Display.setResizable(true);
        Display.setTitle("Sunrise Client 1.8.8");

        try
        {
            Display.create((new PixelFormat()).withDepthBits(24));
        }
        catch (LWJGLException lwjglexception)
        {
            logger.error("Couldn't set pixel format", lwjglexception);

            try
            {
                Thread.sleep(1000L);
            }
            catch (InterruptedException ignored)
            {
            }

            if (fullscreen)
            {
                updateDisplayMode();
            }

            Display.create();
        }
    }

    private void setInitialDisplayMode() throws LWJGLException
    {
        if (fullscreen)
        {
            Display.setFullscreen(true);
            DisplayMode displaymode = Display.getDisplayMode();
            displayWidth = Math.max(1, displaymode.getWidth());
            displayHeight = Math.max(1, displaymode.getHeight());
        }
        else
        {
            Display.setDisplayMode(new DisplayMode(displayWidth, displayHeight));
        }
    }

    private void setWindowIcon()
    {
        Util.EnumOS util$enumos = Util.getOSType();

        if (util$enumos != Util.EnumOS.OSX)
        {
            InputStream inputstream = null;
            InputStream inputstream1 = null;

            try
            {
                inputstream = mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_16x16.png"));
                inputstream1 = mcDefaultResourcePack.getInputStreamAssets(new ResourceLocation("icons/icon_32x32.png"));

                if (inputstream != null && inputstream1 != null)
                {
                    Display.setIcon(new ByteBuffer[] {readImageToBuffer(inputstream), readImageToBuffer(inputstream1)});
                }
            }
            catch (IOException ioexception)
            {
                logger.error("Couldn't set icon", ioexception);
            }
            finally
            {
                IOUtils.closeQuietly(inputstream);
                IOUtils.closeQuietly(inputstream1);
            }
        }
    }

    private static boolean isJvm64bit()
    {
        String[] astring = {"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

        for (String s : astring)
        {
            String s1 = System.getProperty(s);

            if (s1 != null && s1.contains("64"))
            {
                return true;
            }
        }

        return false;
    }

    public Framebuffer getFramebuffer()
    {
        return framebufferMc;
    }

    public String getVersion()
    {
        return launchedVersion;
    }

    private void startTimerHackThread()
    {
        Thread thread = new Thread("Timer hack thread")
        {
            public void run()
            {
                while (running)
                {
                    try
                    {
                        Thread.sleep(2147483647L);
                    }
                    catch (InterruptedException ignored)
                    {
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void crashed(CrashReport crash)
    {
        hasCrashed = true;
        crashReporter = crash;
    }

    /**
     * Wrapper around displayCrashReportInternal
     */
    public void displayCrashReport(CrashReport crashReportIn)
    {
        File file1 = new File(getMinecraft().mcDataDir, "crash-reports");
        File file2 = new File(file1, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
        System.out.println(crashReportIn.getCompleteReport());

        if (crashReportIn.getFile() != null)
        {
            System.out.println("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReportIn.getFile());
            System.exit(-1);
        }
        else if (crashReportIn.saveToFile(file2))
        {
            System.out.println("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
            System.exit(-1);
        }
        else
        {
            System.out.println("#@?@# Game crashed! Crash report could not be saved. #@?@#");
            System.exit(-2);
        }
    }

    public boolean isUnicode()
    {
        return mcLanguageManager.isCurrentLocaleUnicode() || gameSettings.forceUnicodeFont;
    }

    public void refreshResources()
    {
        List<IResourcePack> list = Lists.newArrayList(defaultResourcePacks);

        for (ResourcePackRepository.Entry resourcepackrepository$entry : mcResourcePackRepository.getRepositoryEntries())
        {
            list.add(resourcepackrepository$entry.getResourcePack());
        }

        if (mcResourcePackRepository.getResourcePackInstance() != null)
        {
            list.add(mcResourcePackRepository.getResourcePackInstance());
        }

        try
        {
            mcResourceManager.reloadResources(list);
        }
        catch (RuntimeException runtimeexception)
        {
            logger.info("Caught error stitching, removing all assigned resourcepacks", runtimeexception);
            list.clear();
            list.addAll(defaultResourcePacks);
            mcResourcePackRepository.setRepositories(Collections.emptyList());
            mcResourceManager.reloadResources(list);
            gameSettings.resourcePacks.clear();
            gameSettings.field_183018_l.clear();
            gameSettings.saveOptions();
        }

        mcLanguageManager.parseLanguageMetadata(list);

        if (renderGlobal != null)
        {
            renderGlobal.loadRenderers();
        }
    }

    private ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException
    {
        BufferedImage bufferedimage = ImageIO.read(imageStream);
        int[] aint = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), null, 0, bufferedimage.getWidth());
        ByteBuffer bytebuffer = ByteBuffer.allocate(4 * aint.length);

        for (int i : aint)
        {
            bytebuffer.putInt(i << 8 | i >> 24 & 255);
        }

        bytebuffer.flip();
        return bytebuffer;
    }

    private void updateDisplayMode() throws LWJGLException
    {
        Set<DisplayMode> set = Sets.newHashSet();
        Collections.addAll(set, Display.getAvailableDisplayModes());
        DisplayMode displaymode = Display.getDesktopDisplayMode();

        if (!set.contains(displaymode) && Util.getOSType() == Util.EnumOS.OSX)
        {
            label53:

            for (DisplayMode displaymode1 : macDisplayModes)
            {
                boolean flag = true;

                for (DisplayMode displaymode2 : set)
                {
                    if (displaymode2.getBitsPerPixel() == 32 && displaymode2.getWidth() == displaymode1.getWidth() && displaymode2.getHeight() == displaymode1.getHeight())
                    {
                        flag = false;
                        break;
                    }
                }

                if (!flag)
                {
                    Iterator<DisplayMode> iterator = set.iterator();
                    DisplayMode displaymode3;

                    do {
                        if (!iterator.hasNext()) {
                            continue label53;
                        }

                        displaymode3 = iterator.next();

                    } while (displaymode3.getBitsPerPixel() != 32 || displaymode3.getWidth() != displaymode1.getWidth() / 2 || displaymode3.getHeight() != displaymode1.getHeight() / 2);

                    displaymode = displaymode3;
                }
            }
        }

        Display.setDisplayMode(displaymode);
        displayWidth = displaymode.getWidth();
        displayHeight = displaymode.getHeight();
    }

    private void drawPlainBackground(int color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        float f = (float)(color >> 24 & 255) / 255.0F;
        float f1 = (float)(color >> 16 & 255) / 255.0F;
        float f2 = (float)(color >> 8 & 255) / 255.0F;
        float f3 = (float)(color & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(displayWidth, 0, 0.0D).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(0, 0, 0.0D).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(0, displayHeight, 0.0D).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(displayWidth, displayHeight, 0.0D).color(f1, f2, f3, f).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private void drawLoadingImage(int width, int height, float textureWidth, float textureHeight) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        renderEngine.bindTexture(new ResourceLocation("textures/gui/title/galaxy.png"));

        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(0, height, 0.0D).tex((float) 0 * f, ((float) 0 + (float)height) * f1).endVertex();
        worldrenderer.pos(width, height, 0.0D).tex(((float) 0 + (float)width) * f, ((float) 0 + (float)height) * f1).endVertex();
        worldrenderer.pos(width, 0, 0.0D).tex(((float) 0 + (float)width) * f, (float) 0 * f1).endVertex();
        worldrenderer.pos(0, 0, 0.0D).tex((float) 0 * f, (float) 0 * f1).endVertex();
        tessellator.draw();
    }

    private void drawLoadingGradient() {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        float f = (float)(-1072689136 >> 24 & 255) / 255.0F;
        float f1 = (float)(-1072689136 >> 16 & 255) / 255.0F;
        float f2 = (float)(-1072689136 >> 8 & 255) / 255.0F;
        float f3 = (float)(-1072689136 & 255) / 255.0F;
        float f4 = (float)(-804253680 >> 24 & 255) / 255.0F;
        float f5 = (float)(-804253680 >> 16 & 255) / 255.0F;
        float f6 = (float)(-804253680 >> 8 & 255) / 255.0F;
        float f7 = (float)(-804253680 & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(displayWidth, 0, 0.0D).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(0, 0, 0.0D).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(0, displayHeight, 0.0D).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(displayWidth, displayHeight, 0.0D).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private void drawLoadingBar(float progress) {
        ScaledResolution scaledresolution = new ScaledResolution(this);
        int k = scaledresolution.getScaledWidth();
        int l = scaledresolution.getScaledHeight();

        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -200.0F);

        if (!OpenGlHelper.isFramebufferEnabled())
        {
            GlStateManager.clear(16640);
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (progress >= 0)
        {
            int width = k / 2;
            int height = 13;
            int xPos = k / 2 - width / 2;
            int yPos = l - l / 6;
            float scaledProgress = CommonMath.lerp(0, width, progress);
            GlStateManager.disableTexture2D();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(xPos, yPos, 0.0D).color(128, 128, 128, 255).endVertex();
            worldrenderer.pos(xPos, yPos + height, 0.0D).color(128, 128, 128, 255).endVertex();
            worldrenderer.pos(xPos + width, yPos + height, 0.0D).color(128, 128, 128, 255).endVertex();
            worldrenderer.pos(xPos + width, yPos, 0.0D).color(128, 128, 128, 255).endVertex();
            worldrenderer.pos(xPos, yPos, 0.0D).color(128, 255, 128, 255).endVertex();
            worldrenderer.pos(xPos, yPos + height, 0.0D).color(128, 255, 128, 255).endVertex();
            worldrenderer.pos(xPos + scaledProgress, yPos + height, 0.0D).color(128, 255, 128, 255).endVertex();
            worldrenderer.pos(xPos + scaledProgress, yPos, 0.0D).color(128, 255, 128, 255).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
    }

    private void drawSplashScreen() {
        ScaledResolution scaledresolution = new ScaledResolution(this);
        int i = scaledresolution.getScaleFactor();
        Framebuffer framebuffer = new Framebuffer(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i, true);
        framebuffer.bindFramebuffer(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();

        if(renderEngine == null) {
            drawPlainBackground(Color.darkGray.getRGB());
        } else {
            drawLoadingImage(scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight());
            drawLoadingGradient();
        }
        drawLoadingBar(LoadingManager.getPercentage());

        framebuffer.unbindFramebuffer();
        framebuffer.framebufferRender(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i);

        updateDisplay();
    }

    /**
     * Returns the save loader that is currently being used
     */
    public ISaveFormat getSaveLoader()
    {
        return saveLoader;
    }

    /**
     * Sets the argument GuiScreen as the main (topmost visible) screen.
     */
    public void displayGuiScreen(GuiScreen guiScreenIn)
    {
        if (currentScreen != null)
        {
            currentScreen.onGuiClosed();
        }

        if (guiScreenIn == null && theWorld == null)
        {
            guiScreenIn = new GuiMainMenu();
        }
        else if (guiScreenIn == null && thePlayer.getHealth() <= 0.0F)
        {
            guiScreenIn = new GuiGameOver();
        }

        if (guiScreenIn instanceof GuiMainMenu)
        {
            gameSettings.showDebugInfo = false;
            ingameGUI.getChatGUI().clearChatMessages();
        }

        currentScreen = guiScreenIn;

        if (guiScreenIn != null)
        {
            setIngameNotInFocus();
            ScaledResolution scaledresolution = new ScaledResolution(this);
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            guiScreenIn.setWorldAndResolution(this, i, j);
            skipRenderWorld = false;
        }
        else
        {
            mcSoundHandler.resumeSounds();
            setIngameFocus();
        }
    }

    /**
     * Checks for an OpenGL error. If there is one, prints the error ID and error string.
     */
    private void checkGLError(String message)
    {
        int i = GL11.glGetError();

        if (i != 0)
        {
            String s = GLU.gluErrorString(i);
            logger.error("########## GL ERROR ##########");
            logger.error("@ " + message);
            logger.error(i + ": " + s);
        }
    }

    /**
     * Shuts down the minecraft applet by stopping the resource downloads, and clearing up GL stuff; called when the
     * application (or web page) is exited.
     */
    public void shutdownMinecraftApplet()
    {
        try
        {
            stream.shutdownStream();
            logger.info("Stopping!");

            try
            {
                loadWorld(null);
            }
            catch (Throwable ignored)
            {
            }

            mcSoundHandler.unloadSounds();
        }
        finally
        {
            Display.destroy();

            if (!hasCrashed)
            {
                System.exit(0);
            }
        }

        System.gc();
    }

    /**
     * Called repeatedly from run()
     */
    private void runGameLoop() throws Exception
    {
        long i = System.nanoTime();
        mcProfiler.startSection("root");

        if (Display.isCreated() && Display.isCloseRequested())
        {
            shutdown();
        }

        if (isGamePaused && theWorld != null)
        {
            float f = timer.renderPartialTicks;
            timer.updateTimer();
            timer.renderPartialTicks = f;
        }
        else
        {
            timer.updateTimer();
        }

        mcProfiler.startSection("scheduledExecutables");

        synchronized (scheduledTasks)
        {
            while (!scheduledTasks.isEmpty())
            {
                Util.func_181617_a((FutureTask) scheduledTasks.poll(), logger);
            }
        }

        mcProfiler.endSection();
        long l = System.nanoTime();
        mcProfiler.startSection("tick");

        for (int j = 0; j < timer.elapsedTicks; ++j)
        {
            runTick();
        }

        mcProfiler.endStartSection("preRenderErrors");
        checkGLError("Pre render");
        mcProfiler.endStartSection("sound");
        mcSoundHandler.setListener(thePlayer, timer.renderPartialTicks);
        mcProfiler.endSection();
        mcProfiler.startSection("render");
        GlStateManager.pushMatrix();
        GlStateManager.clear(16640);
        framebufferMc.bindFramebuffer(true);
        mcProfiler.startSection("display");
        GlStateManager.enableTexture2D();

        if (thePlayer != null && thePlayer.isEntityInsideOpaqueBlock())
        {
            gameSettings.thirdPersonView = 0;
        }

        mcProfiler.endSection();

        if (!skipRenderWorld)
        {
            mcProfiler.endStartSection("gameRenderer");
            entityRenderer.func_181560_a(timer.renderPartialTicks, i);
            mcProfiler.endSection();
        }

        mcProfiler.endSection();

        if (gameSettings.showDebugInfo && gameSettings.showDebugProfilerChart && !gameSettings.hideGUI)
        {
            if (!mcProfiler.profilingEnabled)
            {
                mcProfiler.clearProfiling();
            }

            mcProfiler.profilingEnabled = true;
            displayDebugInfo();
        }
        else
        {
            mcProfiler.profilingEnabled = false;
            prevFrameTime = System.nanoTime();
        }

        guiAchievement.updateAchievementWindow();
        framebufferMc.unbindFramebuffer();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        framebufferMc.framebufferRender(displayWidth, displayHeight);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        entityRenderer.renderStreamIndicator();
        GlStateManager.popMatrix();
        mcProfiler.startSection("root");
        updateDisplay();
        Thread.yield();
        mcProfiler.startSection("stream");
        mcProfiler.startSection("update");
        stream.func_152935_j();
        mcProfiler.endStartSection("submit");
        stream.func_152922_k();
        mcProfiler.endSection();
        mcProfiler.endSection();
        checkGLError("Post render");
        ++fpsCounter;
        isGamePaused = isSingleplayer() && currentScreen != null && currentScreen.doesGuiPauseGame() && !theIntegratedServer.getPublic();
        long k = System.nanoTime();
        field_181542_y.func_181747_a(k - field_181543_z);
        field_181543_z = k;

        while (getSystemTime() >= debugUpdateTime + 1000L)
        {
            debugFPS = fpsCounter;
            debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s", debugFPS, RenderChunk.renderChunksUpdated, RenderChunk.renderChunksUpdated != 1 ? "s" : "", (float) gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ? "inf" : Integer.valueOf(gameSettings.limitFramerate), gameSettings.enableVsync ? " vsync" : "", gameSettings.fancyGraphics ? "" : " fast", gameSettings.clouds == 0 ? "" : (gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"), OpenGlHelper.useVbo() ? " vbo" : "");
            RenderChunk.renderChunksUpdated = 0;
            debugUpdateTime += 1000L;
            fpsCounter = 0;
            usageSnooper.addMemoryStatsToSnooper();

            if (!usageSnooper.isSnooperRunning())
            {
                usageSnooper.startSnooper();
            }
        }

        if (isFramerateLimitBelowMax())
        {
            mcProfiler.startSection("fpslimit_wait");
            Display.sync(getLimitFramerate());
            mcProfiler.endSection();
        }

        mcProfiler.endSection();
    }

    public void updateDisplay()
    {
        mcProfiler.startSection("display_update");
        Display.update();
        mcProfiler.endSection();
        checkWindowResize();
    }

    protected void checkWindowResize()
    {
        if (!fullscreen && Display.wasResized())
        {
            int i = displayWidth;
            int j = displayHeight;
            displayWidth = Display.getWidth();
            displayHeight = Display.getHeight();

            if (displayWidth != i || displayHeight != j)
            {
                if (displayWidth <= 0)
                {
                    displayWidth = 1;
                }

                if (displayHeight <= 0)
                {
                    displayHeight = 1;
                }

                resize(displayWidth, displayHeight);
            }
        }
    }

    public int getLimitFramerate()
    {
        return theWorld == null && currentScreen != null ? 30 : gameSettings.limitFramerate;
    }

    public boolean isFramerateLimitBelowMax()
    {
        return (float) getLimitFramerate() < GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
    }

    public void freeMemory()
    {
        try
        {
            memoryReserve = new byte[0];
            renderGlobal.deleteAllDisplayLists();
        }
        catch (Throwable ignored)
        {
        }

        try
        {
            System.gc();
            loadWorld(null);
        }
        catch (Throwable ignored)
        {
        }

        System.gc();
    }

    /**
     * Update debugProfilerName in response to number keys in debug screen
     */
    private void updateDebugProfilerName(int keyCount)
    {
        List<Profiler.Result> list = mcProfiler.getProfilingData(debugProfilerName);

        if (list != null && !list.isEmpty())
        {
            Profiler.Result profiler$result = list.remove(0);

            if (keyCount == 0)
            {
                if (profiler$result.field_76331_c.length() > 0)
                {
                    int i = debugProfilerName.lastIndexOf(".");

                    if (i >= 0)
                    {
                        debugProfilerName = debugProfilerName.substring(0, i);
                    }
                }
            }
            else
            {
                --keyCount;

                if (keyCount < list.size() && !list.get(keyCount).field_76331_c.equals("unspecified"))
                {
                    if (debugProfilerName.length() > 0)
                    {
                        debugProfilerName = debugProfilerName + ".";
                    }

                    debugProfilerName = debugProfilerName + list.get(keyCount).field_76331_c;
                }
            }
        }
    }

    /**
     * Parameter appears to be unused
     */
    private void displayDebugInfo()
    {
        if (mcProfiler.profilingEnabled)
        {
            List<Profiler.Result> list = mcProfiler.getProfilingData(debugProfilerName);
            Profiler.Result profiler$result = list.remove(0);
            GlStateManager.clear(256);
            GlStateManager.matrixMode(5889);
            GlStateManager.enableColorMaterial();
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, displayWidth, displayHeight, 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);
            GL11.glLineWidth(1.0F);
            GlStateManager.disableTexture2D();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            int i = 160;
            int j = displayWidth - i - 10;
            int k = displayHeight - i * 2;
            GlStateManager.enableBlend();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos((float)j - (float)i * 1.1F, (float)k - (float)i * 0.6F - 16.0F, 0.0D).color(200, 0, 0, 0).endVertex();
            worldrenderer.pos((float)j - (float)i * 1.1F, k + i * 2, 0.0D).color(200, 0, 0, 0).endVertex();
            worldrenderer.pos((float)j + (float)i * 1.1F, k + i * 2, 0.0D).color(200, 0, 0, 0).endVertex();
            worldrenderer.pos((float)j + (float)i * 1.1F, (float)k - (float)i * 0.6F - 16.0F, 0.0D).color(200, 0, 0, 0).endVertex();
            tessellator.draw();
            GlStateManager.disableBlend();
            double d0 = 0.0D;

            for (Profiler.Result profiler$result1 : list) {
                int i1 = MathHelper.floor_double(profiler$result1.field_76332_a / 4.0D) + 1;
                worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                int j1 = profiler$result1.func_76329_a();
                int k1 = j1 >> 16 & 255;
                int l1 = j1 >> 8 & 255;
                int i2 = j1 & 255;
                worldrenderer.pos(j, k, 0.0D).color(k1, l1, i2, 255).endVertex();

                for (int j2 = i1; j2 >= 0; --j2) {
                    float f = (float) ((d0 + profiler$result1.field_76332_a * (double) j2 / (double) i1) * Math.PI * 2.0D / 100.0D);
                    float f1 = MathHelper.sin(f) * (float) i;
                    float f2 = MathHelper.cos(f) * (float) i * 0.5F;
                    worldrenderer.pos((float) j + f1, (float) k - f2, 0.0D).color(k1, l1, i2, 255).endVertex();
                }

                tessellator.draw();
                worldrenderer.begin(5, DefaultVertexFormats.POSITION_COLOR);

                for (int i3 = i1; i3 >= 0; --i3) {
                    float f3 = (float) ((d0 + profiler$result1.field_76332_a * (double) i3 / (double) i1) * Math.PI * 2.0D / 100.0D);
                    float f4 = MathHelper.sin(f3) * (float) i;
                    float f5 = MathHelper.cos(f3) * (float) i * 0.5F;
                    worldrenderer.pos((float) j + f4, (float) k - f5, 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
                    worldrenderer.pos((float) j + f4, (float) k - f5 + 10.0F, 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
                }

                tessellator.draw();
                d0 += profiler$result1.field_76332_a;
            }

            DecimalFormat decimalformat = new DecimalFormat("##0.00");
            GlStateManager.enableTexture2D();
            String s = "";

            if (!profiler$result.field_76331_c.equals("unspecified"))
            {
                s = s + "[0] ";
            }

            if (profiler$result.field_76331_c.length() == 0)
            {
                s = s + "ROOT ";
            }
            else
            {
                s = s + profiler$result.field_76331_c + " ";
            }

            int l2 = 16777215;
            fontRendererObj.drawStringWithShadow(s, (float)(j - i), (float)(k - i / 2 - 16), l2);
            fontRendererObj.drawStringWithShadow(s = decimalformat.format(profiler$result.field_76330_b) + "%", (float)(j + i - fontRendererObj.getStringWidth(s)), (float)(k - i / 2 - 16), l2);

            for (int k2 = 0; k2 < list.size(); ++k2)
            {
                Profiler.Result profiler$result2 = list.get(k2);
                String s1 = "";

                if (profiler$result2.field_76331_c.equals("unspecified"))
                {
                    s1 = s1 + "[?] ";
                }
                else
                {
                    s1 = s1 + "[" + (k2 + 1) + "] ";
                }

                s1 = s1 + profiler$result2.field_76331_c;
                fontRendererObj.drawStringWithShadow(s1, (float)(j - i), (float)(k + i / 2 + k2 * 8 + 20), profiler$result2.func_76329_a());
                fontRendererObj.drawStringWithShadow(s1 = decimalformat.format(profiler$result2.field_76332_a) + "%", (float)(j + i - 50 - fontRendererObj.getStringWidth(s1)), (float)(k + i / 2 + k2 * 8 + 20), profiler$result2.func_76329_a());
                fontRendererObj.drawStringWithShadow(s1 = decimalformat.format(profiler$result2.field_76330_b) + "%", (float)(j + i - fontRendererObj.getStringWidth(s1)), (float)(k + i / 2 + k2 * 8 + 20), profiler$result2.func_76329_a());
            }
        }
    }

    /**
     * Called when the window is closing. Sets 'running' to false which allows the game loop to exit cleanly.
     */
    public void shutdown()
    {

        running = false;
    }

    /**
     * Will set the focus to ingame if the Minecraft window is the active with focus. Also clears any GUI screen
     * currently displayed
     */
    public void setIngameFocus()
    {
        if (Display.isActive())
        {
            if (!inGameHasFocus)
            {
                inGameHasFocus = true;
                mouseHelper.grabMouseCursor();
                displayGuiScreen(null);
                leftClickCounter = 10000;
            }
        }
    }

    /**
     * Resets the player keystate, disables the ingame focus, and ungrabs the mouse cursor.
     */
    public void setIngameNotInFocus()
    {
        if (inGameHasFocus)
        {
            KeyBinding.unPressAllKeys();
            inGameHasFocus = false;
            mouseHelper.ungrabMouseCursor();
        }
    }

    /**
     * Displays the ingame menu
     */
    public void displayInGameMenu()
    {
        if (currentScreen == null)
        {
            displayGuiScreen(new GuiIngameMenu());

            if (isSingleplayer() && !theIntegratedServer.getPublic())
            {
                mcSoundHandler.pauseSounds();
            }
        }
    }

    private void sendClickBlockToController(boolean leftClick)
    {
        if (!leftClick)
        {
            leftClickCounter = 0;
        }

        if (leftClickCounter <= 0 && !thePlayer.isUsingItem())
        {
            if (leftClick && objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                BlockPos blockpos = objectMouseOver.getBlockPos();

                if (theWorld.getBlockState(blockpos).getBlock().getMaterial() != Material.air && playerController.onPlayerDamageBlock(blockpos, objectMouseOver.sideHit))
                {
                    effectRenderer.addBlockHitEffects(blockpos, objectMouseOver.sideHit);
                    thePlayer.swingItem();
                }
            }
            else
            {
                playerController.resetBlockRemoving();
            }
        }
    }

    public void clickMouse() throws Exception {
        if (leftClickCounter <= 0)
        {
            thePlayer.swingItem();

            if (objectMouseOver == null)
            {
                logger.error("Null returned as 'hitResult', this shouldn't happen!");

                if (playerController.isNotCreative())
                {
                    leftClickCounter = 10;
                }
            }
            else
            {
                switch (objectMouseOver.typeOfHit)
                {
                    case ENTITY:
                        playerController.attackEntity(thePlayer, objectMouseOver.entityHit);
                        break;

                    case BLOCK:
                        BlockPos blockpos = objectMouseOver.getBlockPos();

                        if (theWorld.getBlockState(blockpos).getBlock().getMaterial() != Material.air)
                        {
                            playerController.clickBlock(blockpos, objectMouseOver.sideHit);
                            break;
                        }

                    case MISS:
                    default:
                        if (playerController.isNotCreative())
                        {
                            leftClickCounter = 10;
                        }
                }
            }
        }
    }


    /**
     * Called when user clicked his mouse right button (place)
     */
    private void rightClickMouse()
    {
        if (!playerController.func_181040_m())
        {
            rightClickDelayTimer = 4;
            boolean flag = true;
            ItemStack itemstack = thePlayer.inventory.getCurrentItem();

            if (objectMouseOver == null)
            {
                logger.warn("Null returned as 'hitResult', this shouldn't happen!");
            }
            else
            {
                switch (objectMouseOver.typeOfHit)
                {
                    case ENTITY:
                        if (playerController.func_178894_a(thePlayer, objectMouseOver.entityHit, objectMouseOver))
                        {
                            flag = false;
                        }
                        else if (playerController.interactWithEntitySendPacket(thePlayer, objectMouseOver.entityHit))
                        {
                            flag = false;
                        }

                        break;

                    case BLOCK:
                        BlockPos blockpos = objectMouseOver.getBlockPos();

                        if (theWorld.getBlockState(blockpos).getBlock().getMaterial() != Material.air)
                        {
                            int i = itemstack != null ? itemstack.stackSize : 0;

                            if (playerController.onPlayerRightClick(thePlayer, theWorld, itemstack, blockpos, objectMouseOver.sideHit, objectMouseOver.hitVec))
                            {
                                flag = false;
                                thePlayer.swingItem();
                            }

                            if (itemstack == null)
                            {
                                return;
                            }

                            if (itemstack.stackSize == 0)
                            {
                                thePlayer.inventory.mainInventory[thePlayer.inventory.currentItem] = null;
                            }
                            else if (itemstack.stackSize != i || playerController.isInCreativeMode())
                            {
                                entityRenderer.itemRenderer.resetEquippedProgress();
                            }
                        }
                }
            }

            if (flag)
            {
                ItemStack itemstack1 = thePlayer.inventory.getCurrentItem();

                if (itemstack1 != null && playerController.sendUseItem(thePlayer, theWorld, itemstack1))
                {
                    entityRenderer.itemRenderer.resetEquippedProgress2();
                }
            }
        }
    }

    /**
     * Toggles fullscreen mode.
     */
    public void toggleFullscreen()
    {
        try
        {
            fullscreen = !fullscreen;
            gameSettings.fullScreen = fullscreen;

            if (fullscreen)
            {
                updateDisplayMode();
                displayWidth = Display.getDisplayMode().getWidth();
                displayHeight = Display.getDisplayMode().getHeight();

                if (displayWidth <= 0)
                {
                    displayWidth = 1;
                }

                if (displayHeight <= 0)
                {
                    displayHeight = 1;
                }
            }
            else
            {
                Display.setDisplayMode(new DisplayMode(tempDisplayWidth, tempDisplayHeight));
                displayWidth = tempDisplayWidth;
                displayHeight = tempDisplayHeight;

                if (displayWidth <= 0)
                {
                    displayWidth = 1;
                }

                if (displayHeight <= 0)
                {
                    displayHeight = 1;
                }
            }

            if (currentScreen != null)
            {
                resize(displayWidth, displayHeight);
            }
            else
            {
                updateFramebufferSize();
            }

            Display.setFullscreen(fullscreen);
            Display.setVSyncEnabled(gameSettings.enableVsync);
            updateDisplay();
        }
        catch (Exception exception)
        {
            logger.error("Couldn't toggle fullscreen", exception);
        }
    }

    /**
     * Called to resize the current screen.
     */
    private void resize(int width, int height)
    {
        displayWidth = Math.max(1, width);
        displayHeight = Math.max(1, height);

        if (currentScreen != null)
        {
            ScaledResolution scaledresolution = new ScaledResolution(this);
            currentScreen.onResize(this, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight());
        }

        loadingScreen = new LoadingScreenRenderer(this);
        updateFramebufferSize();
    }

    private void updateFramebufferSize()
    {
        framebufferMc.createBindFramebuffer(displayWidth, displayHeight);

        if (entityRenderer != null)
        {
            entityRenderer.updateShaderGroupSize(displayWidth, displayHeight);
        }
    }

    public MusicTicker func_181535_r()
    {
        return mcMusicTicker;
    }

    private void keyPress(int k) throws Exception {
        dispatchKeypresses();

        if (Keyboard.getEventKeyState())
        {
            if (k == 62 && entityRenderer != null)
            {
                entityRenderer.switchUseShader();
            }

            if (currentScreen != null)
            {
                currentScreen.handleKeyboardInput();
            }
            else
            {
                if (k == 1)
                {
                    displayInGameMenu();
                }

                if (k == 32 && Keyboard.isKeyDown(61) && ingameGUI != null)
                {
                    ingameGUI.getChatGUI().clearChatMessages();
                }

                if (k == 31 && Keyboard.isKeyDown(61))
                {
                    refreshResources();
                }

                if (k == 17) {
                    Keyboard.isKeyDown(61);
                }

                if (k == 18) {
                    Keyboard.isKeyDown(61);
                }

                if (k == 47) {
                    Keyboard.isKeyDown(61);
                }

                if (k == 38) {
                    Keyboard.isKeyDown(61);
                }

                if (k == 22) {
                    Keyboard.isKeyDown(61);
                }

                if (k == 20 && Keyboard.isKeyDown(61))
                {
                    refreshResources();
                }

                if (k == 33 && Keyboard.isKeyDown(61))
                {
                    gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, GuiScreen.isShiftKeyDown() ? -1 : 1);
                }

                if (k == 30 && Keyboard.isKeyDown(61))
                {
                    renderGlobal.loadRenderers();
                }

                if (k == 35 && Keyboard.isKeyDown(61))
                {
                    gameSettings.advancedItemTooltips = !gameSettings.advancedItemTooltips;
                    gameSettings.saveOptions();
                }

                if (k == 48 && Keyboard.isKeyDown(61))
                {
                    renderManager.setDebugBoundingBox(!renderManager.isDebugBoundingBox());
                }

                if (k == 25 && Keyboard.isKeyDown(61))
                {
                    gameSettings.pauseOnLostFocus = !gameSettings.pauseOnLostFocus;
                    gameSettings.saveOptions();
                }

                if (k == 59)
                {
                    gameSettings.hideGUI = !gameSettings.hideGUI;
                }

                if (k == 61)
                {
                    gameSettings.showDebugInfo = !gameSettings.showDebugInfo;
                    gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
                    gameSettings.field_181657_aC = GuiScreen.isAltKeyDown();
                }

                if (gameSettings.keyBindTogglePerspective.isPressed())
                {
                    ++gameSettings.thirdPersonView;

                    if (gameSettings.thirdPersonView > 2)
                    {
                        gameSettings.thirdPersonView = 0;
                    }

                    if (gameSettings.thirdPersonView == 0)
                    {
                        entityRenderer.loadEntityShader(getRenderViewEntity());
                    }
                    else if (gameSettings.thirdPersonView == 1)
                    {
                        entityRenderer.loadEntityShader(null);
                    }

                    renderGlobal.setDisplayListEntitiesDirty();
                }

                if (gameSettings.keyBindSmoothCamera.isPressed())
                {
                    gameSettings.smoothCamera = !gameSettings.smoothCamera;
                }
            }

            if (gameSettings.showDebugInfo && gameSettings.showDebugProfilerChart)
            {
                if (k == 11)
                {
                    updateDebugProfilerName(0);
                }

                for (int j1 = 0; j1 < 9; ++j1)
                {
                    if (k == 2 + j1)
                    {
                        updateDebugProfilerName(j1 + 1);
                    }
                }
            }
        }
    }

    /**
     * Runs the current tick.
     */
    public void runTick() throws Exception
    {
        Event event = new EventTick();
        event.post();
        if(event.isCancelled()) {
            return;
        }

        if (rightClickDelayTimer > 0)
        {
            --rightClickDelayTimer;
        }

        mcProfiler.startSection("gui");

        if (!isGamePaused)
        {
            ingameGUI.updateTick();
        }

        mcProfiler.endSection();
        entityRenderer.getMouseOver(1.0F);
        mcProfiler.startSection("gameMode");

        if (!isGamePaused && theWorld != null)
        {
            playerController.updateController();
        }

        mcProfiler.endStartSection("textures");

        if (!isGamePaused)
        {
            renderEngine.tick();
        }

        if (currentScreen == null && thePlayer != null)
        {
            if (thePlayer.getHealth() <= 0.0F)
            {
                displayGuiScreen(null);
            }
            else if (thePlayer.isPlayerSleeping() && theWorld != null)
            {
                displayGuiScreen(new GuiSleepMP());
            }
        }
        else if (currentScreen != null && currentScreen instanceof GuiSleepMP && !thePlayer.isPlayerSleeping())
        {
            displayGuiScreen(null);
        }

        if (currentScreen != null)
        {
            leftClickCounter = 10000;
        }

        if (currentScreen != null)
        {
            try
            {
                currentScreen.handleInput();
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Updating screen events");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Affected screen");
                crashreportcategory.addCrashSectionCallable("Screen name", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return currentScreen.getClass().getCanonicalName();
                    }
                });
                throw new ReportedException(crashreport);
            }

            if (currentScreen != null)
            {
                try
                {
                    currentScreen.updateScreen();
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Ticking screen");
                    CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Affected screen");
                    crashreportcategory1.addCrashSectionCallable("Screen name", new Callable<String>()
                    {
                        public String call() throws Exception
                        {
                            return currentScreen.getClass().getCanonicalName();
                        }
                    });
                    throw new ReportedException(crashreport1);
                }
            }
        }

        if (currentScreen == null || currentScreen.allowUserInput)
        {
            mcProfiler.endStartSection("mouse");

            while (Mouse.next())
            {
                int i = Mouse.getEventButton();
                KeyBinding.setKeyBindState(i - 100, Mouse.getEventButtonState());

                if (Mouse.getEventButtonState())
                {
                    if (thePlayer.isSpectator() && i == 2)
                    {
                        ingameGUI.getSpectatorGui().func_175261_b();
                    }
                    else
                    {
                        KeyBinding.onTick(i - 100);
                    }
                }

                long i1 = getSystemTime() - systemTime;

                if (i1 <= 200L)
                {
                    int j = Mouse.getEventDWheel();

                    if (j != 0)
                    {
                        if (thePlayer.isSpectator())
                        {
                            j = j < 0 ? -1 : 1;

                            if (ingameGUI.getSpectatorGui().func_175262_a())
                            {
                                ingameGUI.getSpectatorGui().func_175259_b(-j);
                            }
                            else
                            {
                                float f = MathHelper.clamp_float(thePlayer.capabilities.getFlySpeed() + (float)j * 0.005F, 0.0F, 0.2F);
                                thePlayer.capabilities.setFlySpeed(f);
                            }
                        }
                        else
                        {
                            thePlayer.inventory.changeCurrentItem(j);
                        }
                    }

                    if (currentScreen == null)
                    {
                        if (!inGameHasFocus && Mouse.getEventButtonState())
                        {
                            setIngameFocus();
                        }
                    }
                    else {
                        currentScreen.handleMouseInput();
                    }
                }
            }

            if (leftClickCounter > 0)
            {
                --leftClickCounter;
            }

            mcProfiler.endStartSection("keyboard");

            while (Keyboard.next())
            {
                int k = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
                KeyBinding.setKeyBindState(k, Keyboard.getEventKeyState());

                if (Keyboard.getEventKeyState())
                {
                    KeyBinding.onTick(k);
                }

                if (debugCrashKeyPressTime > 0L)
                {
                    if (getSystemTime() - debugCrashKeyPressTime >= 6000L)
                    {
                        throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
                    }

                    if (!Keyboard.isKeyDown(46) || !Keyboard.isKeyDown(61))
                    {
                        debugCrashKeyPressTime = -1L;
                    }
                }
                else if (Keyboard.isKeyDown(46) && Keyboard.isKeyDown(61))
                {
                    debugCrashKeyPressTime = getSystemTime();
                }

                EventKeypress eventKeypress = new EventKeypress();
                eventKeypress.setKey(k);
                eventKeypress.post();
            }

            for (int l = 0; l < 9; ++l)
            {
                if (gameSettings.keyBindsHotbar[l].isPressed())
                {
                    if (thePlayer.isSpectator())
                    {
                        ingameGUI.getSpectatorGui().func_175260_a(l);
                    }
                    else
                    {
                        thePlayer.inventory.currentItem = l;
                    }
                }
            }

            boolean flag = gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;

            while (gameSettings.keyBindInventory.isPressed())
            {
                if (playerController.isRidingHorse())
                {
                    thePlayer.sendHorseInventory();
                }
                else
                {
                    getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                    displayGuiScreen(new GuiInventory(thePlayer));
                }
            }

            while (gameSettings.keyBindDrop.isPressed())
            {
                if (!thePlayer.isSpectator())
                {
                    thePlayer.dropOneItem(GuiScreen.isCtrlKeyDown());
                }
            }

            while (gameSettings.keyBindChat.isPressed() && flag)
            {
                displayGuiScreen(new GuiChat());
            }

            if (currentScreen == null && gameSettings.keyBindCommand.isPressed() && flag)
            {
                displayGuiScreen(new GuiChat("/"));
            }

            if (thePlayer.isUsingItem())
            {
                if (!gameSettings.keyBindUseItem.isKeyDown())
                {
                    playerController.onStoppedUsingItem(thePlayer);
                }

                while (gameSettings.keyBindAttack.isPressed())
                {
                }

                while (gameSettings.keyBindUseItem.isPressed())
                {
                }

                while (gameSettings.keyBindPickBlock.isPressed())
                {
                }
            }
            else
            {
                while (gameSettings.keyBindAttack.isPressed())
                {
                    EventClickMouse eventClickMouse = new EventClickMouse();
                    eventClickMouse.setButton(0);
                    eventClickMouse.post();
                }

                while (gameSettings.keyBindUseItem.isPressed())
                {
                    EventClickMouse eventClickMouse = new EventClickMouse();
                    eventClickMouse.setButton(1);
                    eventClickMouse.post();
                }

                while (gameSettings.keyBindPickBlock.isPressed())
                {
                    EventClickMouse eventClickMouse = new EventClickMouse();
                    eventClickMouse.setButton(2);
                    eventClickMouse.post();
                }
            }

            if (gameSettings.keyBindUseItem.isKeyDown() && rightClickDelayTimer == 0 && !thePlayer.isUsingItem())
            {
                rightClickMouse();
            }

            sendClickBlockToController(currentScreen == null && gameSettings.keyBindAttack.isKeyDown() && inGameHasFocus);
        }

        if (theWorld != null)
        {
            if (thePlayer != null)
            {
                ++joinPlayerCounter;

                if (joinPlayerCounter == 30)
                {
                    joinPlayerCounter = 0;
                    theWorld.joinEntityInSurroundings(thePlayer);
                }
            }

            mcProfiler.endStartSection("gameRenderer");

            if (!isGamePaused)
            {
                entityRenderer.updateRenderer();
            }

            mcProfiler.endStartSection("levelRenderer");

            if (!isGamePaused)
            {
                renderGlobal.updateClouds();
            }

            mcProfiler.endStartSection("level");

            if (!isGamePaused)
            {
                if (theWorld.getLastLightningBolt() > 0)
                {
                    theWorld.setLastLightningBolt(theWorld.getLastLightningBolt() - 1);
                }

                theWorld.updateEntities();
            }
        }
        else if (entityRenderer.isShaderActive())
        {
            entityRenderer.func_181022_b();
        }

        if (!isGamePaused)
        {
            mcMusicTicker.update();
            mcSoundHandler.update();
        }

        if (theWorld != null)
        {
            if (!isGamePaused)
            {
                theWorld.setAllowedSpawnTypes(theWorld.getDifficulty() != EnumDifficulty.PEACEFUL, true);

                try
                {
                    theWorld.tick();
                }
                catch (Throwable throwable2)
                {
                    CrashReport crashreport2 = CrashReport.makeCrashReport(throwable2, "Exception in world tick");

                    if (theWorld == null)
                    {
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Affected level");
                        crashreportcategory2.addCrashSection("Problem", "Level is null!");
                    }
                    else
                    {
                        theWorld.addWorldInfoToCrashReport(crashreport2);
                    }

                    throw new ReportedException(crashreport2);
                }
            }

            mcProfiler.endStartSection("animateTick");

            if (!isGamePaused && theWorld != null)
            {
                theWorld.doVoidFogParticles(MathHelper.floor_double(thePlayer.posX), MathHelper.floor_double(thePlayer.posY), MathHelper.floor_double(thePlayer.posZ));
            }

            mcProfiler.endStartSection("particles");

            if (!isGamePaused)
            {
                effectRenderer.updateEffects();
            }
        }
        else if (myNetworkManager != null)
        {
            mcProfiler.endStartSection("pendingConnection");
            myNetworkManager.processReceivedPackets();
        }

        mcProfiler.endSection();
        systemTime = getSystemTime();
    }

    /**
     * Arguments: World foldername,  World ingame name, WorldSettings
     */
    public void launchIntegratedServer(String folderName, String worldName, WorldSettings worldSettingsIn)
    {
        loadWorld(null);
        System.gc();
        ISaveHandler isavehandler = saveLoader.getSaveLoader(folderName, false);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();

        if (worldinfo == null && worldSettingsIn != null)
        {
            worldinfo = new WorldInfo(worldSettingsIn, folderName);
            isavehandler.saveWorldInfo(worldinfo);
        }

        if (worldSettingsIn == null)
        {
            worldSettingsIn = new WorldSettings(worldinfo);
        }

        try
        {
            theIntegratedServer = new IntegratedServer(this, folderName, worldName, worldSettingsIn);
            theIntegratedServer.startServerThread();
            integratedServerIsRunning = true;
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Starting integrated server");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Starting integrated server");
            crashreportcategory.addCrashSection("Level ID", folderName);
            crashreportcategory.addCrashSection("Level Name", worldName);
            throw new ReportedException(crashreport);
        }

        loadingScreen.displaySavingString(I18n.format("menu.loadingLevel"));

        while (!theIntegratedServer.serverIsInRunLoop())
        {
            String s = theIntegratedServer.getUserMessage();

            if (s != null)
            {
                loadingScreen.displayLoadingString(I18n.format(s));
            }
            else
            {
                loadingScreen.displayLoadingString("");
            }

            try
            {
                Thread.sleep(200L);
            }
            catch (InterruptedException ignored)
            {
            }
        }

        displayGuiScreen(null);
        SocketAddress socketaddress = theIntegratedServer.getNetworkSystem().addLocalEndpoint();
        NetworkManager networkmanager = NetworkManager.provideLocalClient(socketaddress);
        networkmanager.setNetHandler(new NetHandlerLoginClient(networkmanager, this, null));
        networkmanager.sendPacket(new C00Handshake(47, socketaddress.toString(), 0, EnumConnectionState.LOGIN));
        networkmanager.sendPacket(new C00PacketLoginStart(getSession().getProfile()));
        myNetworkManager = networkmanager;
    }

    /**
     * unloads the current world first
     */
    public void loadWorld(WorldClient worldClientIn)
    {
        loadWorld(worldClientIn, "");
    }

    /**
     * par2Str is displayed on the loading screen to the user unloads the current world first
     */
    public void loadWorld(WorldClient worldClientIn, String loadingMessage)
    {
        if (worldClientIn == null)
        {
            NetHandlerPlayClient nethandlerplayclient = getNetHandler();

            if (nethandlerplayclient != null)
            {
                nethandlerplayclient.cleanup();
            }

            if (theIntegratedServer != null && theIntegratedServer.isAnvilFileSet())
            {
                theIntegratedServer.initiateShutdown();
                theIntegratedServer.setStaticInstance();
            }

            theIntegratedServer = null;
            guiAchievement.clearAchievements();
            entityRenderer.getMapItemRenderer().clearLoadedMaps();
        }

        renderViewEntity = null;
        myNetworkManager = null;

        if (loadingScreen != null)
        {
            loadingScreen.resetProgressAndMessage(loadingMessage);
            loadingScreen.displayLoadingString("");
        }

        if (worldClientIn == null && theWorld != null)
        {
            mcResourcePackRepository.func_148529_f();
            ingameGUI.func_181029_i();
            setServerData(null);
            integratedServerIsRunning = false;
        }

        mcSoundHandler.stopSounds();
        theWorld = worldClientIn;

        if (worldClientIn != null)
        {
            if (renderGlobal != null)
            {
                renderGlobal.setWorldAndLoadRenderers(worldClientIn);
            }

            if (effectRenderer != null)
            {
                effectRenderer.clearEffects(worldClientIn);
            }

            if (thePlayer == null)
            {
                thePlayer = playerController.func_178892_a(worldClientIn, new StatFileWriter());
                playerController.flipPlayer(thePlayer);
            }

            thePlayer.preparePlayerToSpawn();
            worldClientIn.spawnEntityInWorld(thePlayer);
            thePlayer.movementInput = new MovementInputFromOptions(gameSettings);
            playerController.setPlayerCapabilities(thePlayer);
            renderViewEntity = thePlayer;
        }
        else
        {
            saveLoader.flushCache();
            thePlayer = null;
        }

        System.gc();
        systemTime = 0L;
    }

    public void setDimensionAndSpawnPlayer(int dimension)
    {
        theWorld.setInitialSpawnLocation();
        theWorld.removeAllEntities();
        int i = 0;
        String s = null;

        if (thePlayer != null)
        {
            i = thePlayer.getEntityId();
            theWorld.removeEntity(thePlayer);
            s = thePlayer.getClientBrand();
        }

        renderViewEntity = null;
        EntityPlayerSP entityplayersp = thePlayer;
        thePlayer = playerController.func_178892_a(theWorld, thePlayer == null ? new StatFileWriter() : thePlayer.getStatFileWriter());
        thePlayer.getDataWatcher().updateWatchedObjectsFromList(entityplayersp.getDataWatcher().getAllWatched());
        thePlayer.dimension = dimension;
        renderViewEntity = thePlayer;
        thePlayer.preparePlayerToSpawn();
        thePlayer.setClientBrand(s);
        theWorld.spawnEntityInWorld(thePlayer);
        playerController.flipPlayer(thePlayer);
        thePlayer.movementInput = new MovementInputFromOptions(gameSettings);
        thePlayer.setEntityId(i);
        playerController.setPlayerCapabilities(thePlayer);
        thePlayer.setReducedDebug(entityplayersp.hasReducedDebug());

        if (currentScreen instanceof GuiGameOver)
        {
            displayGuiScreen(null);
        }
    }

    public NetHandlerPlayClient getNetHandler()
    {
        return thePlayer != null ? thePlayer.sendQueue : null;
    }

    public static boolean isGuiEnabled()
    {
        return theMinecraft == null || !theMinecraft.gameSettings.hideGUI;
    }

    public static boolean isFancyGraphicsEnabled()
    {
        return theMinecraft != null && theMinecraft.gameSettings.fancyGraphics;
    }

    /**
     * Returns if ambient occlusion is enabled
     */
    public static boolean isAmbientOcclusionEnabled()
    {
        return theMinecraft != null && theMinecraft.gameSettings.ambientOcclusion != 0;
    }

    /**
     * Called when user clicked he's mouse middle button (pick block)
     */
    private void middleClickMouse()
    {
        if (objectMouseOver != null)
        {
            boolean flag = thePlayer.capabilities.isCreativeMode;
            int i = 0;
            boolean flag1 = false;
            TileEntity tileentity = null;
            Item item;

            if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                BlockPos blockpos = objectMouseOver.getBlockPos();
                Block block = theWorld.getBlockState(blockpos).getBlock();

                if (block.getMaterial() == Material.air)
                {
                    return;
                }

                item = block.getItem(theWorld, blockpos);

                if (item == null)
                {
                    return;
                }

                if (flag && GuiScreen.isCtrlKeyDown())
                {
                    tileentity = theWorld.getTileEntity(blockpos);
                }

                Block block1 = item instanceof ItemBlock && !block.isFlowerPot() ? Block.getBlockFromItem(item) : block;
                i = block1.getDamageValue(theWorld, blockpos);
                flag1 = item.getHasSubtypes();
            }
            else
            {
                if (objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY || objectMouseOver.entityHit == null || !flag)
                {
                    return;
                }

                if (objectMouseOver.entityHit instanceof EntityPainting)
                {
                    item = Items.painting;
                }
                else if (objectMouseOver.entityHit instanceof EntityLeashKnot)
                {
                    item = Items.lead;
                }
                else if (objectMouseOver.entityHit instanceof EntityItemFrame)
                {
                    EntityItemFrame entityitemframe = (EntityItemFrame) objectMouseOver.entityHit;
                    ItemStack itemstack = entityitemframe.getDisplayedItem();

                    if (itemstack == null)
                    {
                        item = Items.item_frame;
                    }
                    else
                    {
                        item = itemstack.getItem();
                        i = itemstack.getMetadata();
                        flag1 = true;
                    }
                }
                else if (objectMouseOver.entityHit instanceof EntityMinecart)
                {
                    EntityMinecart entityminecart = (EntityMinecart) objectMouseOver.entityHit;

                    switch (entityminecart.getMinecartType())
                    {
                        case FURNACE:
                            item = Items.furnace_minecart;
                            break;

                        case CHEST:
                            item = Items.chest_minecart;
                            break;

                        case TNT:
                            item = Items.tnt_minecart;
                            break;

                        case HOPPER:
                            item = Items.hopper_minecart;
                            break;

                        case COMMAND_BLOCK:
                            item = Items.command_block_minecart;
                            break;

                        default:
                            item = Items.minecart;
                    }
                }
                else if (objectMouseOver.entityHit instanceof EntityBoat)
                {
                    item = Items.boat;
                }
                else if (objectMouseOver.entityHit instanceof EntityArmorStand)
                {
                    item = Items.armor_stand;
                }
                else
                {
                    item = Items.spawn_egg;
                    i = EntityList.getEntityID(objectMouseOver.entityHit);
                    flag1 = true;

                    if (!EntityList.entityEggs.containsKey(i))
                    {
                        return;
                    }
                }
            }

            InventoryPlayer inventoryplayer = thePlayer.inventory;

            if (tileentity == null)
            {
                inventoryplayer.setCurrentItem(item, i, flag1, flag);
            }
            else
            {
                ItemStack itemstack1 = func_181036_a(item, i, tileentity);
                inventoryplayer.setInventorySlotContents(inventoryplayer.currentItem, itemstack1);
            }

            if (flag)
            {
                int j = thePlayer.inventoryContainer.inventorySlots.size() - 9 + inventoryplayer.currentItem;
                playerController.sendSlotPacket(inventoryplayer.getStackInSlot(inventoryplayer.currentItem), j);
            }
        }
    }

    private ItemStack func_181036_a(Item p_181036_1_, int p_181036_2_, TileEntity p_181036_3_)
    {
        ItemStack itemstack = new ItemStack(p_181036_1_, 1, p_181036_2_);
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        p_181036_3_.writeToNBT(nbttagcompound);

        if (p_181036_1_ == Items.skull && nbttagcompound.hasKey("Owner"))
        {
            NBTTagCompound nbttagcompound2 = nbttagcompound.getCompoundTag("Owner");
            NBTTagCompound nbttagcompound3 = new NBTTagCompound();
            nbttagcompound3.setTag("SkullOwner", nbttagcompound2);
            itemstack.setTagCompound(nbttagcompound3);
            return itemstack;
        }
        else
        {
            itemstack.setTagInfo("BlockEntityTag", nbttagcompound);
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            NBTTagList nbttaglist = new NBTTagList();
            nbttaglist.appendTag(new NBTTagString("(+NBT)"));
            nbttagcompound1.setTag("Lore", nbttaglist);
            itemstack.setTagInfo("display", nbttagcompound1);
            return itemstack;
        }
    }

    /**
     * adds core server Info (GL version , Texture pack, isModded, type), and the worldInfo to the crash report
     */
    public CrashReport addGraphicsAndWorldToCrashReport(CrashReport theCrash)
    {
        theCrash.getCategory().addCrashSectionCallable("Launched Version", () -> launchedVersion);
        theCrash.getCategory().addCrashSectionCallable("LWJGL", Sys::getVersion);
        theCrash.getCategory().addCrashSectionCallable("OpenGL", () -> GL11.glGetString(GL11.GL_RENDERER) + " GL version " + GL11.glGetString(GL11.GL_VERSION) + ", " + GL11.glGetString(GL11.GL_VENDOR));
        theCrash.getCategory().addCrashSectionCallable("GL Caps", OpenGlHelper::getLogText);
        theCrash.getCategory().addCrashSectionCallable("Using VBOs", () -> gameSettings.useVbo ? "Yes" : "No");
        theCrash.getCategory().addCrashSectionCallable("Is Modded", () -> {
            String s = ClientBrandRetriever.getClientModName();
            return !s.equals("vanilla") ? "Definitely; Client brand changed to '" + s + "'" : (Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and client brand is untouched.");
        });
        theCrash.getCategory().addCrashSectionCallable("Type", () -> "Client (map_client.txt)");
        theCrash.getCategory().addCrashSectionCallable("Resource Packs", () -> {
            StringBuilder stringbuilder = new StringBuilder();

            for (String s : gameSettings.resourcePacks)
            {
                if (stringbuilder.length() > 0)
                {
                    stringbuilder.append(", ");
                }

                stringbuilder.append(s);

                if (gameSettings.field_183018_l.contains(s))
                {
                    stringbuilder.append(" (incompatible)");
                }
            }

            return stringbuilder.toString();
        });
        theCrash.getCategory().addCrashSectionCallable("Current Language", () -> mcLanguageManager.getCurrentLanguage().toString());
        theCrash.getCategory().addCrashSectionCallable("Profiler Position", () -> mcProfiler.profilingEnabled ? mcProfiler.getNameOfLastSection() : "N/A (disabled)");
        theCrash.getCategory().addCrashSectionCallable("CPU", OpenGlHelper::func_183029_j);

        if (theWorld != null)
        {
            theWorld.addWorldInfoToCrashReport(theCrash);
        }

        return theCrash;
    }

    /**
     * Return the singleton Minecraft instance for the game
     */
    public static Minecraft getMinecraft()
    {
        return theMinecraft;
    }

    public ListenableFuture<Object> scheduleResourcesRefresh()
    {
        return addScheduledTask(this::refreshResources);
    }

    public void addServerStatsToSnooper(PlayerUsageSnooper playerSnooper)
    {
        playerSnooper.addClientStat("fps", debugFPS);
        playerSnooper.addClientStat("vsync_enabled", gameSettings.enableVsync);
        playerSnooper.addClientStat("display_frequency", Display.getDisplayMode().getFrequency());
        playerSnooper.addClientStat("display_type", fullscreen ? "fullscreen" : "windowed");
        playerSnooper.addClientStat("run_time", (MinecraftServer.getCurrentTimeMillis() - playerSnooper.getMinecraftStartTimeMillis()) / 60L * 1000L);
        playerSnooper.addClientStat("current_action", func_181538_aA());
        String s = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "little" : "big";
        playerSnooper.addClientStat("endianness", s);
        playerSnooper.addClientStat("resource_packs", mcResourcePackRepository.getRepositoryEntries().size());
        int i = 0;

        for (ResourcePackRepository.Entry resourcepackrepository$entry : mcResourcePackRepository.getRepositoryEntries())
        {
            playerSnooper.addClientStat("resource_pack[" + i++ + "]", resourcepackrepository$entry.getResourcePackName());
        }

        if (theIntegratedServer != null && theIntegratedServer.getPlayerUsageSnooper() != null)
        {
            playerSnooper.addClientStat("snooper_partner", theIntegratedServer.getPlayerUsageSnooper().getUniqueID());
        }
    }

    private String func_181538_aA()
    {
        return theIntegratedServer != null ? (theIntegratedServer.getPublic() ? "hosting_lan" : "singleplayer") : (currentServerData != null ? (currentServerData.func_181041_d() ? "playing_lan" : "multiplayer") : "out_of_game");
    }

    public void addServerTypeToSnooper(PlayerUsageSnooper playerSnooper)
    {
        playerSnooper.addStatToSnooper("opengl_version", GL11.glGetString(GL11.GL_VERSION));
        playerSnooper.addStatToSnooper("opengl_vendor", GL11.glGetString(GL11.GL_VENDOR));
        playerSnooper.addStatToSnooper("client_brand", ClientBrandRetriever.getClientModName());
        playerSnooper.addStatToSnooper("launched_version", launchedVersion);
        ContextCapabilities contextcapabilities = GLContext.getCapabilities();
        playerSnooper.addStatToSnooper("gl_caps[ARB_arrays_of_arrays]", contextcapabilities.GL_ARB_arrays_of_arrays);
        playerSnooper.addStatToSnooper("gl_caps[ARB_base_instance]", contextcapabilities.GL_ARB_base_instance);
        playerSnooper.addStatToSnooper("gl_caps[ARB_blend_func_extended]", contextcapabilities.GL_ARB_blend_func_extended);
        playerSnooper.addStatToSnooper("gl_caps[ARB_clear_buffer_object]", contextcapabilities.GL_ARB_clear_buffer_object);
        playerSnooper.addStatToSnooper("gl_caps[ARB_color_buffer_float]", contextcapabilities.GL_ARB_color_buffer_float);
        playerSnooper.addStatToSnooper("gl_caps[ARB_compatibility]", contextcapabilities.GL_ARB_compatibility);
        playerSnooper.addStatToSnooper("gl_caps[ARB_compressed_texture_pixel_storage]", contextcapabilities.GL_ARB_compressed_texture_pixel_storage);
        playerSnooper.addStatToSnooper("gl_caps[ARB_compute_shader]", contextcapabilities.GL_ARB_compute_shader);
        playerSnooper.addStatToSnooper("gl_caps[ARB_copy_buffer]", contextcapabilities.GL_ARB_copy_buffer);
        playerSnooper.addStatToSnooper("gl_caps[ARB_copy_image]", contextcapabilities.GL_ARB_copy_image);
        playerSnooper.addStatToSnooper("gl_caps[ARB_depth_buffer_float]", contextcapabilities.GL_ARB_depth_buffer_float);
        playerSnooper.addStatToSnooper("gl_caps[ARB_compute_shader]", contextcapabilities.GL_ARB_compute_shader);
        playerSnooper.addStatToSnooper("gl_caps[ARB_copy_buffer]", contextcapabilities.GL_ARB_copy_buffer);
        playerSnooper.addStatToSnooper("gl_caps[ARB_copy_image]", contextcapabilities.GL_ARB_copy_image);
        playerSnooper.addStatToSnooper("gl_caps[ARB_depth_buffer_float]", contextcapabilities.GL_ARB_depth_buffer_float);
        playerSnooper.addStatToSnooper("gl_caps[ARB_depth_clamp]", contextcapabilities.GL_ARB_depth_clamp);
        playerSnooper.addStatToSnooper("gl_caps[ARB_depth_texture]", contextcapabilities.GL_ARB_depth_texture);
        playerSnooper.addStatToSnooper("gl_caps[ARB_draw_buffers]", contextcapabilities.GL_ARB_draw_buffers);
        playerSnooper.addStatToSnooper("gl_caps[ARB_draw_buffers_blend]", contextcapabilities.GL_ARB_draw_buffers_blend);
        playerSnooper.addStatToSnooper("gl_caps[ARB_draw_elements_base_vertex]", contextcapabilities.GL_ARB_draw_elements_base_vertex);
        playerSnooper.addStatToSnooper("gl_caps[ARB_draw_indirect]", contextcapabilities.GL_ARB_draw_indirect);
        playerSnooper.addStatToSnooper("gl_caps[ARB_draw_instanced]", contextcapabilities.GL_ARB_draw_instanced);
        playerSnooper.addStatToSnooper("gl_caps[ARB_explicit_attrib_location]", contextcapabilities.GL_ARB_explicit_attrib_location);
        playerSnooper.addStatToSnooper("gl_caps[ARB_explicit_uniform_location]", contextcapabilities.GL_ARB_explicit_uniform_location);
        playerSnooper.addStatToSnooper("gl_caps[ARB_fragment_layer_viewport]", contextcapabilities.GL_ARB_fragment_layer_viewport);
        playerSnooper.addStatToSnooper("gl_caps[ARB_fragment_program]", contextcapabilities.GL_ARB_fragment_program);
        playerSnooper.addStatToSnooper("gl_caps[ARB_fragment_shader]", contextcapabilities.GL_ARB_fragment_shader);
        playerSnooper.addStatToSnooper("gl_caps[ARB_fragment_program_shadow]", contextcapabilities.GL_ARB_fragment_program_shadow);
        playerSnooper.addStatToSnooper("gl_caps[ARB_framebuffer_object]", contextcapabilities.GL_ARB_framebuffer_object);
        playerSnooper.addStatToSnooper("gl_caps[ARB_framebuffer_sRGB]", contextcapabilities.GL_ARB_framebuffer_sRGB);
        playerSnooper.addStatToSnooper("gl_caps[ARB_geometry_shader4]", contextcapabilities.GL_ARB_geometry_shader4);
        playerSnooper.addStatToSnooper("gl_caps[ARB_gpu_shader5]", contextcapabilities.GL_ARB_gpu_shader5);
        playerSnooper.addStatToSnooper("gl_caps[ARB_half_float_pixel]", contextcapabilities.GL_ARB_half_float_pixel);
        playerSnooper.addStatToSnooper("gl_caps[ARB_half_float_vertex]", contextcapabilities.GL_ARB_half_float_vertex);
        playerSnooper.addStatToSnooper("gl_caps[ARB_instanced_arrays]", contextcapabilities.GL_ARB_instanced_arrays);
        playerSnooper.addStatToSnooper("gl_caps[ARB_map_buffer_alignment]", contextcapabilities.GL_ARB_map_buffer_alignment);
        playerSnooper.addStatToSnooper("gl_caps[ARB_map_buffer_range]", contextcapabilities.GL_ARB_map_buffer_range);
        playerSnooper.addStatToSnooper("gl_caps[ARB_multisample]", contextcapabilities.GL_ARB_multisample);
        playerSnooper.addStatToSnooper("gl_caps[ARB_multitexture]", contextcapabilities.GL_ARB_multitexture);
        playerSnooper.addStatToSnooper("gl_caps[ARB_occlusion_query2]", contextcapabilities.GL_ARB_occlusion_query2);
        playerSnooper.addStatToSnooper("gl_caps[ARB_pixel_buffer_object]", contextcapabilities.GL_ARB_pixel_buffer_object);
        playerSnooper.addStatToSnooper("gl_caps[ARB_seamless_cube_map]", contextcapabilities.GL_ARB_seamless_cube_map);
        playerSnooper.addStatToSnooper("gl_caps[ARB_shader_objects]", contextcapabilities.GL_ARB_shader_objects);
        playerSnooper.addStatToSnooper("gl_caps[ARB_shader_stencil_export]", contextcapabilities.GL_ARB_shader_stencil_export);
        playerSnooper.addStatToSnooper("gl_caps[ARB_shader_texture_lod]", contextcapabilities.GL_ARB_shader_texture_lod);
        playerSnooper.addStatToSnooper("gl_caps[ARB_shadow]", contextcapabilities.GL_ARB_shadow);
        playerSnooper.addStatToSnooper("gl_caps[ARB_shadow_ambient]", contextcapabilities.GL_ARB_shadow_ambient);
        playerSnooper.addStatToSnooper("gl_caps[ARB_stencil_texturing]", contextcapabilities.GL_ARB_stencil_texturing);
        playerSnooper.addStatToSnooper("gl_caps[ARB_sync]", contextcapabilities.GL_ARB_sync);
        playerSnooper.addStatToSnooper("gl_caps[ARB_tessellation_shader]", contextcapabilities.GL_ARB_tessellation_shader);
        playerSnooper.addStatToSnooper("gl_caps[ARB_texture_border_clamp]", contextcapabilities.GL_ARB_texture_border_clamp);
        playerSnooper.addStatToSnooper("gl_caps[ARB_texture_buffer_object]", contextcapabilities.GL_ARB_texture_buffer_object);
        playerSnooper.addStatToSnooper("gl_caps[ARB_texture_cube_map]", contextcapabilities.GL_ARB_texture_cube_map);
        playerSnooper.addStatToSnooper("gl_caps[ARB_texture_cube_map_array]", contextcapabilities.GL_ARB_texture_cube_map_array);
        playerSnooper.addStatToSnooper("gl_caps[ARB_texture_non_power_of_two]", contextcapabilities.GL_ARB_texture_non_power_of_two);
        playerSnooper.addStatToSnooper("gl_caps[ARB_uniform_buffer_object]", contextcapabilities.GL_ARB_uniform_buffer_object);
        playerSnooper.addStatToSnooper("gl_caps[ARB_vertex_blend]", contextcapabilities.GL_ARB_vertex_blend);
        playerSnooper.addStatToSnooper("gl_caps[ARB_vertex_buffer_object]", contextcapabilities.GL_ARB_vertex_buffer_object);
        playerSnooper.addStatToSnooper("gl_caps[ARB_vertex_program]", contextcapabilities.GL_ARB_vertex_program);
        playerSnooper.addStatToSnooper("gl_caps[ARB_vertex_shader]", contextcapabilities.GL_ARB_vertex_shader);
        playerSnooper.addStatToSnooper("gl_caps[EXT_bindable_uniform]", contextcapabilities.GL_EXT_bindable_uniform);
        playerSnooper.addStatToSnooper("gl_caps[EXT_blend_equation_separate]", contextcapabilities.GL_EXT_blend_equation_separate);
        playerSnooper.addStatToSnooper("gl_caps[EXT_blend_func_separate]", contextcapabilities.GL_EXT_blend_func_separate);
        playerSnooper.addStatToSnooper("gl_caps[EXT_blend_minmax]", contextcapabilities.GL_EXT_blend_minmax);
        playerSnooper.addStatToSnooper("gl_caps[EXT_blend_subtract]", contextcapabilities.GL_EXT_blend_subtract);
        playerSnooper.addStatToSnooper("gl_caps[EXT_draw_instanced]", contextcapabilities.GL_EXT_draw_instanced);
        playerSnooper.addStatToSnooper("gl_caps[EXT_framebuffer_multisample]", contextcapabilities.GL_EXT_framebuffer_multisample);
        playerSnooper.addStatToSnooper("gl_caps[EXT_framebuffer_object]", contextcapabilities.GL_EXT_framebuffer_object);
        playerSnooper.addStatToSnooper("gl_caps[EXT_framebuffer_sRGB]", contextcapabilities.GL_EXT_framebuffer_sRGB);
        playerSnooper.addStatToSnooper("gl_caps[EXT_geometry_shader4]", contextcapabilities.GL_EXT_geometry_shader4);
        playerSnooper.addStatToSnooper("gl_caps[EXT_gpu_program_parameters]", contextcapabilities.GL_EXT_gpu_program_parameters);
        playerSnooper.addStatToSnooper("gl_caps[EXT_gpu_shader4]", contextcapabilities.GL_EXT_gpu_shader4);
        playerSnooper.addStatToSnooper("gl_caps[EXT_multi_draw_arrays]", contextcapabilities.GL_EXT_multi_draw_arrays);
        playerSnooper.addStatToSnooper("gl_caps[EXT_packed_depth_stencil]", contextcapabilities.GL_EXT_packed_depth_stencil);
        playerSnooper.addStatToSnooper("gl_caps[EXT_paletted_texture]", contextcapabilities.GL_EXT_paletted_texture);
        playerSnooper.addStatToSnooper("gl_caps[EXT_rescale_normal]", contextcapabilities.GL_EXT_rescale_normal);
        playerSnooper.addStatToSnooper("gl_caps[EXT_separate_shader_objects]", contextcapabilities.GL_EXT_separate_shader_objects);
        playerSnooper.addStatToSnooper("gl_caps[EXT_shader_image_load_store]", contextcapabilities.GL_EXT_shader_image_load_store);
        playerSnooper.addStatToSnooper("gl_caps[EXT_shadow_funcs]", contextcapabilities.GL_EXT_shadow_funcs);
        playerSnooper.addStatToSnooper("gl_caps[EXT_shared_texture_palette]", contextcapabilities.GL_EXT_shared_texture_palette);
        playerSnooper.addStatToSnooper("gl_caps[EXT_stencil_clear_tag]", contextcapabilities.GL_EXT_stencil_clear_tag);
        playerSnooper.addStatToSnooper("gl_caps[EXT_stencil_two_side]", contextcapabilities.GL_EXT_stencil_two_side);
        playerSnooper.addStatToSnooper("gl_caps[EXT_stencil_wrap]", contextcapabilities.GL_EXT_stencil_wrap);
        playerSnooper.addStatToSnooper("gl_caps[EXT_texture_3d]", contextcapabilities.GL_EXT_texture_3d);
        playerSnooper.addStatToSnooper("gl_caps[EXT_texture_array]", contextcapabilities.GL_EXT_texture_array);
        playerSnooper.addStatToSnooper("gl_caps[EXT_texture_buffer_object]", contextcapabilities.GL_EXT_texture_buffer_object);
        playerSnooper.addStatToSnooper("gl_caps[EXT_texture_integer]", contextcapabilities.GL_EXT_texture_integer);
        playerSnooper.addStatToSnooper("gl_caps[EXT_texture_lod_bias]", contextcapabilities.GL_EXT_texture_lod_bias);
        playerSnooper.addStatToSnooper("gl_caps[EXT_texture_sRGB]", contextcapabilities.GL_EXT_texture_sRGB);
        playerSnooper.addStatToSnooper("gl_caps[EXT_vertex_shader]", contextcapabilities.GL_EXT_vertex_shader);
        playerSnooper.addStatToSnooper("gl_caps[EXT_vertex_weighting]", contextcapabilities.GL_EXT_vertex_weighting);
        playerSnooper.addStatToSnooper("gl_caps[gl_max_vertex_uniforms]", GL11.glGetInteger(GL20.GL_MAX_VERTEX_UNIFORM_COMPONENTS));
        GL11.glGetError();
        playerSnooper.addStatToSnooper("gl_caps[gl_max_fragment_uniforms]", GL11.glGetInteger(GL20.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS));
        GL11.glGetError();
        playerSnooper.addStatToSnooper("gl_caps[gl_max_vertex_attribs]", GL11.glGetInteger(GL20.GL_MAX_VERTEX_ATTRIBS));
        GL11.glGetError();
        playerSnooper.addStatToSnooper("gl_caps[gl_max_vertex_texture_image_units]", GL11.glGetInteger(GL20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS));
        GL11.glGetError();
        playerSnooper.addStatToSnooper("gl_caps[gl_max_texture_image_units]", GL11.glGetInteger(GL20.GL_MAX_TEXTURE_IMAGE_UNITS));
        GL11.glGetError();
        playerSnooper.addStatToSnooper("gl_caps[gl_max_texture_image_units]", GL11.glGetInteger(35071));
        GL11.glGetError();
        playerSnooper.addStatToSnooper("gl_max_texture_size", getGLMaximumTextureSize());
    }

    /**
     * Used in the usage snooper.
     */
    public static int getGLMaximumTextureSize()
    {
        for (int i = 16384; i > 0; i >>= 1)
        {
            GL11.glTexImage2D(GL11.GL_PROXY_TEXTURE_2D, 0, GL11.GL_RGBA, i, i, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            int j = GL11.glGetTexLevelParameteri(GL11.GL_PROXY_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);

            if (j != 0)
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns whether snooping is enabled or not.
     */
    public boolean isSnooperEnabled()
    {
        return gameSettings.snooperEnabled;
    }

    /**
     * Set the current ServerData instance.
     */
    public void setServerData(ServerData serverDataIn)
    {
        currentServerData = serverDataIn;
    }

    public ServerData getCurrentServerData()
    {
        return currentServerData;
    }

    public boolean isIntegratedServerRunning()
    {
        return integratedServerIsRunning;
    }

    /**
     * Returns true if there is only one player playing, and the current server is the integrated one.
     */
    public boolean isSingleplayer()
    {
        return integratedServerIsRunning && theIntegratedServer != null;
    }

    /**
     * Returns the currently running integrated server
     */
    public IntegratedServer getIntegratedServer()
    {
        return theIntegratedServer;
    }

    public static void stopIntegratedServer()
    {
        if (theMinecraft != null)
        {
            IntegratedServer integratedserver = theMinecraft.getIntegratedServer();

            if (integratedserver != null)
            {
                integratedserver.stopServer();
            }
        }
    }

    /**
     * Returns the PlayerUsageSnooper instance.
     */
    public PlayerUsageSnooper getPlayerUsageSnooper()
    {
        return usageSnooper;
    }

    /**
     * Gets the system time in milliseconds.
     */
    public static long getSystemTime()
    {
        return Sys.getTime() * 1000L / Sys.getTimerResolution();
    }

    /**
     * Returns whether we're in full screen or not.
     */
    public boolean isFullScreen()
    {
        return fullscreen;
    }

    public Session getSession()
    {
        return session;
    }

    public PropertyMap getTwitchDetails()
    {
        return twitchDetails;
    }

    public PropertyMap func_181037_M()
    {
        if (field_181038_N.isEmpty())
        {
            GameProfile gameprofile = getSessionService().fillProfileProperties(session.getProfile(), false);
            field_181038_N.putAll(gameprofile.getProperties());
        }

        return field_181038_N;
    }

    public Proxy getProxy()
    {
        return proxy;
    }

    public TextureManager getTextureManager()
    {
        return renderEngine;
    }

    public IResourceManager getResourceManager()
    {
        return mcResourceManager;
    }

    public ResourcePackRepository getResourcePackRepository()
    {
        return mcResourcePackRepository;
    }

    public LanguageManager getLanguageManager()
    {
        return mcLanguageManager;
    }

    public TextureMap getTextureMapBlocks()
    {
        return textureMapBlocks;
    }

    public boolean isJava64bit()
    {
        return jvm64bit;
    }

    public boolean isGamePaused()
    {
        return isGamePaused;
    }

    public SoundHandler getSoundHandler()
    {
        return mcSoundHandler;
    }

    public MusicTicker.MusicType getAmbientMusicType()
    {
        return thePlayer != null ? (thePlayer.worldObj.provider instanceof WorldProviderHell ? MusicTicker.MusicType.NETHER : (thePlayer.worldObj.provider instanceof WorldProviderEnd ? (BossStatus.bossName != null && BossStatus.statusBarTime > 0 ? MusicTicker.MusicType.END_BOSS : MusicTicker.MusicType.END) : (thePlayer.capabilities.isCreativeMode && thePlayer.capabilities.allowFlying ? MusicTicker.MusicType.CREATIVE : MusicTicker.MusicType.GAME))) : MusicTicker.MusicType.MENU;
    }

    public IStream getTwitchStream()
    {
        return stream;
    }

    public void dispatchKeypresses()
    {
        int i = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() : Keyboard.getEventKey();

        if (i != 0 && !Keyboard.isRepeatEvent())
        {
            if (!(currentScreen instanceof GuiControls) || ((GuiControls) currentScreen).time <= getSystemTime() - 20L)
            {
                if (Keyboard.getEventKeyState())
                {
                    if (i == gameSettings.keyBindStreamStartStop.getKeyCode())
                    {
                        if (getTwitchStream().isBroadcasting())
                        {
                            getTwitchStream().stopBroadcasting();
                        }
                        else if (getTwitchStream().isReadyToBroadcast())
                        {
                            displayGuiScreen(new GuiYesNo((result, id) -> {
                                if (result)
                                {
                                    getTwitchStream().func_152930_t();
                                }

                                displayGuiScreen(null);
                            }, I18n.format("stream.confirm_start"), "", 0));
                        }
                        else if (getTwitchStream().func_152928_D() && getTwitchStream().func_152936_l())
                        {
                            if (theWorld != null)
                            {
                                ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Not ready to start streaming yet!"));
                            }
                        }
                        else
                        {
                            GuiStreamUnavailable.func_152321_a(currentScreen);
                        }
                    }
                    else if (i == gameSettings.keyBindStreamPauseUnpause.getKeyCode())
                    {
                        if (getTwitchStream().isBroadcasting())
                        {
                            if (getTwitchStream().isPaused())
                            {
                                getTwitchStream().unpause();
                            }
                            else
                            {
                                getTwitchStream().pause();
                            }
                        }
                    }
                    else if (i == gameSettings.keyBindStreamCommercials.getKeyCode())
                    {
                        if (getTwitchStream().isBroadcasting())
                        {
                            getTwitchStream().requestCommercial();
                        }
                    }
                    else if (i == gameSettings.keyBindStreamToggleMic.getKeyCode())
                    {
                        stream.muteMicrophone(true);
                    }
                    else if (i == gameSettings.keyBindFullscreen.getKeyCode())
                    {
                        toggleFullscreen();
                    }
                    else if (i == gameSettings.keyBindScreenshot.getKeyCode())
                    {
                        ingameGUI.getChatGUI().printChatMessage(ScreenShotHelper.saveScreenshot(mcDataDir, displayWidth, displayHeight, framebufferMc));
                    }
                }
                else if (i == gameSettings.keyBindStreamToggleMic.getKeyCode())
                {
                    stream.muteMicrophone(false);
                }
            }
        }
    }

    public MinecraftSessionService getSessionService()
    {
        return sessionService;
    }

    public SkinManager getSkinManager()
    {
        return skinManager;
    }

    public Entity getRenderViewEntity()
    {
        return renderViewEntity;
    }

    public void setRenderViewEntity(Entity viewingEntity)
    {
        renderViewEntity = viewingEntity;
        entityRenderer.loadEntityShader(viewingEntity);
    }

    public <V> ListenableFuture<V> addScheduledTask(Callable<V> callableToSchedule)
    {
        Validate.notNull(callableToSchedule);

        if (!isCallingFromMinecraftThread())
        {
            ListenableFutureTask<V> listenablefuturetask = ListenableFutureTask.create(callableToSchedule);

            synchronized (scheduledTasks)
            {
                scheduledTasks.add(listenablefuturetask);
                return listenablefuturetask;
            }
        }
        else
        {
            try
            {
                return Futures.immediateFuture(callableToSchedule.call());
            }
            catch (Exception exception)
            {
                return Futures.immediateFailedCheckedFuture(exception);
            }
        }
    }

    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule)
    {
        Validate.notNull(runnableToSchedule);
        return addScheduledTask(Executors.callable(runnableToSchedule));
    }

    public boolean isCallingFromMinecraftThread()
    {
        return Thread.currentThread() == mcThread;
    }

    public BlockRendererDispatcher getBlockRendererDispatcher()
    {
        return blockRenderDispatcher;
    }

    public RenderManager getRenderManager()
    {
        return renderManager;
    }

    public RenderItem getRenderItem()
    {
        return renderItem;
    }

    public ItemRenderer getItemRenderer()
    {
        return itemRenderer;
    }

    public static int getDebugFPS()
    {
        return debugFPS;
    }

    public FrameTimer func_181539_aj()
    {
        return field_181542_y;
    }

    public static Map<String, String> getSessionInfo()
    {
        Map<String, String> map = Maps.newHashMap();
        map.put("X-Minecraft-Username", getMinecraft().getSession().getUsername());
        map.put("X-Minecraft-UUID", getMinecraft().getSession().getPlayerID());
        map.put("X-Minecraft-Version", "1.8.8");
        return map;
    }

    public boolean func_181540_al()
    {
        return field_181541_X;
    }

    public void func_181537_a(boolean p_181537_1_)
    {
        field_181541_X = p_181537_1_;
    }


    /*
    Custom code from PVPer client
     */

    public boolean inWorld() {
        return theWorld != null && thePlayer != null;
    }
}
