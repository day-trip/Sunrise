package net.minecraft.client.settings;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.stream.TwitchStream;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class GameSettings
{
    private static final Logger logger = LogManager.getLogger();
    private static final Gson gson = new Gson();
    private static final ParameterizedType typeListString = new ParameterizedType()
    {
        public Type[] getActualTypeArguments()
        {
            return new Type[] {String.class};
        }
        public Type getRawType()
        {
            return List.class;
        }
        public Type getOwnerType()
        {
            return null;
        }
    };

    /** GUI scale values */
    private static final String[] GUISCALES = {"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
    private static final String[] PARTICLES = {"options.particles.all", "options.particles.decreased", "options.particles.minimal"};
    private static final String[] AMBIENT_OCCLUSIONS = {"options.ao.off", "options.ao.min", "options.ao.max"};
    private static final String[] STREAM_COMPRESSIONS = {"options.stream.compression.low", "options.stream.compression.medium", "options.stream.compression.high"};
    private static final String[] STREAM_CHAT_MODES = {"options.stream.chat.enabled.streaming", "options.stream.chat.enabled.always", "options.stream.chat.enabled.never"};
    private static final String[] STREAM_CHAT_FILTER_MODES = {"options.stream.chat.userFilter.all", "options.stream.chat.userFilter.subs", "options.stream.chat.userFilter.mods"};
    private static final String[] STREAM_MIC_MODES = {"options.stream.mic_toggle.mute", "options.stream.mic_toggle.talk"};
    private static final String[] field_181149_aW = {"options.off", "options.graphics.fast", "options.graphics.fancy"};
    public float mouseSensitivity = 0.5F;
    public boolean invertMouse;
    public int renderDistanceChunks = -1;
    public boolean viewBobbing = true;
    public boolean anaglyph;
    public boolean fboEnable = true;
    public int limitFramerate = 120;

    /** Clouds flag */
    public int clouds = 2;
    public boolean fancyGraphics = true;

    /** Smooth Lighting */
    public int ambientOcclusion = 2;
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> field_183018_l = Lists.newArrayList();
    public EntityPlayer.EnumChatVisibility chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
    public boolean chatColours = true;
    public boolean chatLinks = true;
    public boolean chatLinksPrompt = true;
    public float chatOpacity = 1.0F;
    public boolean snooperEnabled = true;
    public boolean fullScreen;
    public boolean enableVsync = true;
    public boolean useVbo;
    public boolean allowBlockAlternatives = true;
    public boolean reducedDebugInfo;
    public boolean hideServerAddress;

    /**
     * Whether to show advanced information on item tooltips, toggled by F3+H
     */
    public boolean advancedItemTooltips;

    /** Whether to pause when the game loses focus, toggled by F3+P */
    public boolean pauseOnLostFocus = true;
    private final Set<EnumPlayerModelParts> setModelParts = Sets.newHashSet(EnumPlayerModelParts.values());
    public boolean touchscreen;
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    public float chatScale = 1.0F;
    public float chatWidth = 1.0F;
    public float chatHeightUnfocused = 0.44366196F;
    public float chatHeightFocused = 1.0F;
    public boolean showInventoryAchievementHint = true;
    public int mipmapLevels = 4;
    private final Map<SoundCategory, Float> mapSoundLevels = Maps.newEnumMap(SoundCategory.class);
    public float streamBytesPerPixel = 0.5F;
    public float streamMicVolume = 1.0F;
    public float streamGameVolume = 1.0F;
    public float streamKbps = 0.5412844F;
    public float streamFps = 0.31690142F;
    public int streamCompression = 1;
    public boolean streamSendMetadata = true;
    public String streamPreferredServer = "";
    public int streamChatEnabled;
    public int streamChatUserFilter;
    public int streamMicToggleBehavior;
    public boolean field_181150_U = true;
    public boolean field_181151_V = true;
    public KeyBinding keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
    public KeyBinding keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
    public KeyBinding keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
    public KeyBinding keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
    public KeyBinding keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
    public KeyBinding keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
    public KeyBinding keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.movement");
    public KeyBinding keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
    public KeyBinding keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
    public KeyBinding keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.gameplay");
    public KeyBinding keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
    public KeyBinding keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
    public KeyBinding keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
    public KeyBinding keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
    public KeyBinding keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
    public KeyBinding keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
    public KeyBinding keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
    public KeyBinding keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
    public KeyBinding keyBindFullscreen = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
    public KeyBinding keyBindSpectatorOutlines = new KeyBinding("key.spectatorOutlines", 0, "key.categories.misc");
    public KeyBinding keyBindStreamStartStop = new KeyBinding("key.streamStartStop", 64, "key.categories.stream");
    public KeyBinding keyBindStreamPauseUnpause = new KeyBinding("key.streamPauseUnpause", 65, "key.categories.stream");
    public KeyBinding keyBindStreamCommercials = new KeyBinding("key.streamCommercial", 0, "key.categories.stream");
    public KeyBinding keyBindStreamToggleMic = new KeyBinding("key.streamToggleMic", 0, "key.categories.stream");
    public KeyBinding[] keyBindsHotbar = {new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
    public KeyBinding[] keyBindings;
    protected Minecraft mc;
    private File optionsFile;
    public EnumDifficulty difficulty;
    public boolean hideGUI;
    public int thirdPersonView;

    /** true if debug info should be displayed instead of version */
    public boolean showDebugInfo;
    public boolean showDebugProfilerChart;
    public boolean field_181657_aC;

    /** The lastServer string. */
    public String lastServer;

    /** Smooth Camera Toggle */
    public boolean smoothCamera;
    public boolean debugCamEnable;
    public float fovSetting;
    public float gammaSetting;
    public float saturation;

    /** GUI scale */
    public int guiScale;

    /** Determines amount of particles. 0 = All, 1 = Decreased, 2 = Minimal */
    public int particleSetting;

    /** Game settings language */
    public String language;
    public boolean forceUnicodeFont;

    public GameSettings(Minecraft mcIn, File p_i46326_2_)
    {
        keyBindings = ArrayUtils.addAll(new KeyBinding[] {keyBindAttack, keyBindUseItem, keyBindForward, keyBindLeft, keyBindBack, keyBindRight, keyBindJump, keyBindSneak, keyBindSprint, keyBindDrop, keyBindInventory, keyBindChat, keyBindPlayerList, keyBindPickBlock, keyBindCommand, keyBindScreenshot, keyBindTogglePerspective, keyBindSmoothCamera, keyBindStreamStartStop, keyBindStreamPauseUnpause, keyBindStreamCommercials, keyBindStreamToggleMic, keyBindFullscreen, keyBindSpectatorOutlines}, keyBindsHotbar);
        difficulty = EnumDifficulty.NORMAL;
        lastServer = "";
        fovSetting = 70.0F;
        language = "en_US";
        forceUnicodeFont = false;
        mc = mcIn;
        optionsFile = new File(p_i46326_2_, "options.txt");

        if (mcIn.isJava64bit() && Runtime.getRuntime().maxMemory() >= 1000000000L)
        {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(32.0F);
        }
        else
        {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(16.0F);
        }

        renderDistanceChunks = mcIn.isJava64bit() ? 12 : 8;
        loadOptions();

        gammaSetting = 10.0f;
    }

    public GameSettings()
    {
        keyBindings = ArrayUtils.addAll(new KeyBinding[] {keyBindAttack, keyBindUseItem, keyBindForward, keyBindLeft, keyBindBack, keyBindRight, keyBindJump, keyBindSneak, keyBindSprint, keyBindDrop, keyBindInventory, keyBindChat, keyBindPlayerList, keyBindPickBlock, keyBindCommand, keyBindScreenshot, keyBindTogglePerspective, keyBindSmoothCamera, keyBindStreamStartStop, keyBindStreamPauseUnpause, keyBindStreamCommercials, keyBindStreamToggleMic, keyBindFullscreen, keyBindSpectatorOutlines}, keyBindsHotbar);
        difficulty = EnumDifficulty.NORMAL;
        lastServer = "";
        fovSetting = 70.0F;
        language = "en_US";
        forceUnicodeFont = false;
    }

    /**
     * Represents a key or mouse button as a string. Args: key
     */
    public static String getKeyDisplayString(int p_74298_0_)
    {
        return p_74298_0_ < 0 ? I18n.format("key.mouseButton", p_74298_0_ + 101): (p_74298_0_ < 256 ? Keyboard.getKeyName(p_74298_0_) : String.format("%c", (char) (p_74298_0_ - 256)).toUpperCase());
    }

    /**
     * Returns whether the specified key binding is currently being pressed.
     */
    public static boolean isKeyDown(KeyBinding p_100015_0_)
    {
        return p_100015_0_.getKeyCode() != 0 && (p_100015_0_.getKeyCode() < 0 ? Mouse.isButtonDown(p_100015_0_.getKeyCode() + 100) : Keyboard.isKeyDown(p_100015_0_.getKeyCode()));
    }

    /**
     * Sets a key binding and then saves all settings.
     */
    public void setOptionKeyBinding(KeyBinding p_151440_1_, int p_151440_2_)
    {
        p_151440_1_.setKeyCode(p_151440_2_);
        saveOptions();
    }

    /**
     * If the specified option is controlled by a slider (float value), this will set the float value.
     */
    public void setOptionFloatValue(GameSettings.Options p_74304_1_, float p_74304_2_)
    {
        if (p_74304_1_ == GameSettings.Options.SENSITIVITY)
        {
            mouseSensitivity = p_74304_2_;
        }

        if (p_74304_1_ == GameSettings.Options.FOV)
        {
            fovSetting = p_74304_2_;
        }

        if (p_74304_1_ == GameSettings.Options.GAMMA)
        {
            gammaSetting = p_74304_2_;
        }

        if (p_74304_1_ == GameSettings.Options.FRAMERATE_LIMIT)
        {
            limitFramerate = (int)p_74304_2_;
        }

        if (p_74304_1_ == GameSettings.Options.CHAT_OPACITY)
        {
            chatOpacity = p_74304_2_;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (p_74304_1_ == GameSettings.Options.CHAT_HEIGHT_FOCUSED)
        {
            chatHeightFocused = p_74304_2_;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (p_74304_1_ == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED)
        {
            chatHeightUnfocused = p_74304_2_;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (p_74304_1_ == GameSettings.Options.CHAT_WIDTH)
        {
            chatWidth = p_74304_2_;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (p_74304_1_ == GameSettings.Options.CHAT_SCALE)
        {
            chatScale = p_74304_2_;
            mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (p_74304_1_ == GameSettings.Options.MIPMAP_LEVELS)
        {
            int i = mipmapLevels;
            mipmapLevels = (int)p_74304_2_;

            if ((float)i != p_74304_2_)
            {
                mc.getTextureMapBlocks().setMipmapLevels(mipmapLevels);
                mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                mc.getTextureMapBlocks().setBlurMipmapDirect(false, mipmapLevels > 0);
                mc.scheduleResourcesRefresh();
            }
        }

        if (p_74304_1_ == GameSettings.Options.BLOCK_ALTERNATIVES)
        {
            allowBlockAlternatives = !allowBlockAlternatives;
            mc.renderGlobal.loadRenderers();
        }

        if (p_74304_1_ == GameSettings.Options.RENDER_DISTANCE)
        {
            renderDistanceChunks = (int)p_74304_2_;
            mc.renderGlobal.setDisplayListEntitiesDirty();
        }

        if (p_74304_1_ == GameSettings.Options.STREAM_BYTES_PER_PIXEL)
        {
            streamBytesPerPixel = p_74304_2_;
        }

        if (p_74304_1_ == GameSettings.Options.STREAM_VOLUME_MIC)
        {
            streamMicVolume = p_74304_2_;
            mc.getTwitchStream().updateStreamVolume();
        }

        if (p_74304_1_ == GameSettings.Options.STREAM_VOLUME_SYSTEM)
        {
            streamGameVolume = p_74304_2_;
            mc.getTwitchStream().updateStreamVolume();
        }

        if (p_74304_1_ == GameSettings.Options.STREAM_KBPS)
        {
            streamKbps = p_74304_2_;
        }

        if (p_74304_1_ == GameSettings.Options.STREAM_FPS)
        {
            streamFps = p_74304_2_;
        }
    }

    /**
     * For non-float options. Toggles the option on/off, or cycles through the list i.e. render distances.
     */
    public void setOptionValue(GameSettings.Options p_74306_1_, int p_74306_2_)
    {
        if (p_74306_1_ == GameSettings.Options.INVERT_MOUSE)
        {
            invertMouse = !invertMouse;
        }

        if (p_74306_1_ == GameSettings.Options.GUI_SCALE)
        {
            guiScale = guiScale + p_74306_2_ & 3;
        }

        if (p_74306_1_ == GameSettings.Options.PARTICLES)
        {
            particleSetting = (particleSetting + p_74306_2_) % 3;
        }

        if (p_74306_1_ == GameSettings.Options.VIEW_BOBBING)
        {
            viewBobbing = !viewBobbing;
        }

        if (p_74306_1_ == GameSettings.Options.RENDER_CLOUDS)
        {
            clouds = (clouds + p_74306_2_) % 3;
        }

        if (p_74306_1_ == GameSettings.Options.FORCE_UNICODE_FONT)
        {
            forceUnicodeFont = !forceUnicodeFont;
            mc.fontRendererObj.setUnicodeFlag(mc.getLanguageManager().isCurrentLocaleUnicode() || forceUnicodeFont);
        }

        if (p_74306_1_ == GameSettings.Options.FBO_ENABLE)
        {
            fboEnable = !fboEnable;
        }

        if (p_74306_1_ == GameSettings.Options.ANAGLYPH)
        {
            anaglyph = !anaglyph;
            mc.refreshResources();
        }

        if (p_74306_1_ == GameSettings.Options.GRAPHICS)
        {
            fancyGraphics = !fancyGraphics;
            mc.renderGlobal.loadRenderers();
        }

        if (p_74306_1_ == GameSettings.Options.AMBIENT_OCCLUSION)
        {
            ambientOcclusion = (ambientOcclusion + p_74306_2_) % 3;
            mc.renderGlobal.loadRenderers();
        }

        if (p_74306_1_ == GameSettings.Options.CHAT_VISIBILITY)
        {
            chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility((chatVisibility.getChatVisibility() + p_74306_2_) % 3);
        }

        if (p_74306_1_ == GameSettings.Options.STREAM_COMPRESSION)
        {
            streamCompression = (streamCompression + p_74306_2_) % 3;
        }

        if (p_74306_1_ == GameSettings.Options.STREAM_SEND_METADATA)
        {
            streamSendMetadata = !streamSendMetadata;
        }

        if (p_74306_1_ == GameSettings.Options.STREAM_CHAT_ENABLED)
        {
            streamChatEnabled = (streamChatEnabled + p_74306_2_) % 3;
        }

        if (p_74306_1_ == GameSettings.Options.STREAM_CHAT_USER_FILTER)
        {
            streamChatUserFilter = (streamChatUserFilter + p_74306_2_) % 3;
        }

        if (p_74306_1_ == GameSettings.Options.STREAM_MIC_TOGGLE_BEHAVIOR)
        {
            streamMicToggleBehavior = (streamMicToggleBehavior + p_74306_2_) % 2;
        }

        if (p_74306_1_ == GameSettings.Options.CHAT_COLOR)
        {
            chatColours = !chatColours;
        }

        if (p_74306_1_ == GameSettings.Options.CHAT_LINKS)
        {
            chatLinks = !chatLinks;
        }

        if (p_74306_1_ == GameSettings.Options.CHAT_LINKS_PROMPT)
        {
            chatLinksPrompt = !chatLinksPrompt;
        }

        if (p_74306_1_ == GameSettings.Options.SNOOPER_ENABLED)
        {
            snooperEnabled = !snooperEnabled;
        }

        if (p_74306_1_ == GameSettings.Options.TOUCHSCREEN)
        {
            touchscreen = !touchscreen;
        }

        if (p_74306_1_ == GameSettings.Options.USE_FULLSCREEN)
        {
            fullScreen = !fullScreen;

            if (mc.isFullScreen() != fullScreen)
            {
                mc.toggleFullscreen();
            }
        }

        if (p_74306_1_ == GameSettings.Options.ENABLE_VSYNC)
        {
            enableVsync = !enableVsync;
            Display.setVSyncEnabled(enableVsync);
        }

        if (p_74306_1_ == GameSettings.Options.USE_VBO)
        {
            useVbo = !useVbo;
            mc.renderGlobal.loadRenderers();
        }

        if (p_74306_1_ == GameSettings.Options.BLOCK_ALTERNATIVES)
        {
            allowBlockAlternatives = !allowBlockAlternatives;
            mc.renderGlobal.loadRenderers();
        }

        if (p_74306_1_ == GameSettings.Options.REDUCED_DEBUG_INFO)
        {
            reducedDebugInfo = !reducedDebugInfo;
        }

        if (p_74306_1_ == GameSettings.Options.ENTITY_SHADOWS)
        {
            field_181151_V = !field_181151_V;
        }

        saveOptions();
    }

    public float getOptionFloatValue(GameSettings.Options p_74296_1_)
    {
        return p_74296_1_ == GameSettings.Options.FOV ? fovSetting : (p_74296_1_ == GameSettings.Options.GAMMA ? gammaSetting : (p_74296_1_ == GameSettings.Options.SATURATION ? saturation : (p_74296_1_ == GameSettings.Options.SENSITIVITY ? mouseSensitivity : (p_74296_1_ == GameSettings.Options.CHAT_OPACITY ? chatOpacity : (p_74296_1_ == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? chatHeightFocused : (p_74296_1_ == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? chatHeightUnfocused : (p_74296_1_ == GameSettings.Options.CHAT_SCALE ? chatScale : (p_74296_1_ == GameSettings.Options.CHAT_WIDTH ? chatWidth : (p_74296_1_ == GameSettings.Options.FRAMERATE_LIMIT ? (float) limitFramerate : (p_74296_1_ == GameSettings.Options.MIPMAP_LEVELS ? (float) mipmapLevels : (p_74296_1_ == GameSettings.Options.RENDER_DISTANCE ? (float) renderDistanceChunks : (p_74296_1_ == GameSettings.Options.STREAM_BYTES_PER_PIXEL ? streamBytesPerPixel : (p_74296_1_ == GameSettings.Options.STREAM_VOLUME_MIC ? streamMicVolume : (p_74296_1_ == GameSettings.Options.STREAM_VOLUME_SYSTEM ? streamGameVolume : (p_74296_1_ == GameSettings.Options.STREAM_KBPS ? streamKbps : (p_74296_1_ == GameSettings.Options.STREAM_FPS ? streamFps : 0.0F))))))))))))))));
    }

    public boolean getOptionOrdinalValue(GameSettings.Options p_74308_1_)
    {
        switch (p_74308_1_)
        {
            case INVERT_MOUSE:
                return invertMouse;

            case VIEW_BOBBING:
                return viewBobbing;

            case ANAGLYPH:
                return anaglyph;

            case FBO_ENABLE:
                return fboEnable;

            case CHAT_COLOR:
                return chatColours;

            case CHAT_LINKS:
                return chatLinks;

            case CHAT_LINKS_PROMPT:
                return chatLinksPrompt;

            case SNOOPER_ENABLED:
                return snooperEnabled;

            case USE_FULLSCREEN:
                return fullScreen;

            case ENABLE_VSYNC:
                return enableVsync;

            case USE_VBO:
                return useVbo;

            case TOUCHSCREEN:
                return touchscreen;

            case STREAM_SEND_METADATA:
                return streamSendMetadata;

            case FORCE_UNICODE_FONT:
                return forceUnicodeFont;

            case BLOCK_ALTERNATIVES:
                return allowBlockAlternatives;

            case REDUCED_DEBUG_INFO:
                return reducedDebugInfo;

            case ENTITY_SHADOWS:
                return field_181151_V;

            default:
                return false;
        }
    }

    /**
     * Returns the translation of the given index in the given String array. If the index is smaller than 0 or greater
     * than/equal to the length of the String array, it is changed to 0.
     */
    private static String getTranslation(String[] p_74299_0_, int p_74299_1_)
    {
        if (p_74299_1_ < 0 || p_74299_1_ >= p_74299_0_.length)
        {
            p_74299_1_ = 0;
        }

        return I18n.format(p_74299_0_[p_74299_1_]);
    }

    /**
     * Gets a key binding.
     */
    public String getKeyBinding(GameSettings.Options p_74297_1_)
    {
        String s = I18n.format(p_74297_1_.getEnumString()) + ": ";

        if (p_74297_1_.getEnumFloat())
        {
            float f1 = getOptionFloatValue(p_74297_1_);
            float f = p_74297_1_.normalizeValue(f1);
            return p_74297_1_ == GameSettings.Options.SENSITIVITY ? (f == 0.0F ? s + I18n.format("options.sensitivity.min") : (f == 1.0F ? s + I18n.format("options.sensitivity.max") : s + (int)(f * 200.0F) + "%")) : (p_74297_1_ == GameSettings.Options.FOV ? (f1 == 70.0F ? s + I18n.format("options.fov.min") : (f1 == 110.0F ? s + I18n.format("options.fov.max") : s + (int)f1)) : (p_74297_1_ == GameSettings.Options.FRAMERATE_LIMIT ? (f1 == p_74297_1_.valueMax ? s + I18n.format("options.framerateLimit.max") : s + (int)f1 + " fps") : (p_74297_1_ == GameSettings.Options.RENDER_CLOUDS ? (f1 == p_74297_1_.valueMin ? s + I18n.format("options.cloudHeight.min") : s + ((int)f1 + 128)) : (p_74297_1_ == GameSettings.Options.GAMMA ? (f == 0.0F ? s + I18n.format("options.gamma.min") : (f == 1.0F ? s + I18n.format("options.gamma.max") : s + "+" + (int)(f * 100.0F) + "%")) : (p_74297_1_ == GameSettings.Options.SATURATION ? s + (int)(f * 400.0F) + "%" : (p_74297_1_ == GameSettings.Options.CHAT_OPACITY ? s + (int)(f * 90.0F + 10.0F) + "%" : (p_74297_1_ == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? s + GuiNewChat.calculateChatboxHeight(f) + "px" : (p_74297_1_ == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? s + GuiNewChat.calculateChatboxHeight(f) + "px" : (p_74297_1_ == GameSettings.Options.CHAT_WIDTH ? s + GuiNewChat.calculateChatboxWidth(f) + "px" : (p_74297_1_ == GameSettings.Options.RENDER_DISTANCE ? s + (int)f1 + " chunks" : (p_74297_1_ == GameSettings.Options.MIPMAP_LEVELS ? (f1 == 0.0F ? s + I18n.format("options.off") : s + (int)f1) : (p_74297_1_ == GameSettings.Options.STREAM_FPS ? s + TwitchStream.formatStreamFps(f) + " fps" : (p_74297_1_ == GameSettings.Options.STREAM_KBPS ? s + TwitchStream.formatStreamKbps(f) + " Kbps" : (p_74297_1_ == GameSettings.Options.STREAM_BYTES_PER_PIXEL ? s + String.format("%.3f bpp", TwitchStream.formatStreamBps(f)): (f == 0.0F ? s + I18n.format("options.off") : s + (int)(f * 100.0F) + "%")))))))))))))));
        }
        else if (p_74297_1_.getEnumBoolean())
        {
            boolean flag = getOptionOrdinalValue(p_74297_1_);
            return flag ? s + I18n.format("options.on") : s + I18n.format("options.off");
        }
        else if (p_74297_1_ == GameSettings.Options.GUI_SCALE)
        {
            return s + getTranslation(GUISCALES, guiScale);
        }
        else if (p_74297_1_ == GameSettings.Options.CHAT_VISIBILITY)
        {
            return s + I18n.format(chatVisibility.getResourceKey());
        }
        else if (p_74297_1_ == GameSettings.Options.PARTICLES)
        {
            return s + getTranslation(PARTICLES, particleSetting);
        }
        else if (p_74297_1_ == GameSettings.Options.AMBIENT_OCCLUSION)
        {
            return s + getTranslation(AMBIENT_OCCLUSIONS, ambientOcclusion);
        }
        else if (p_74297_1_ == GameSettings.Options.STREAM_COMPRESSION)
        {
            return s + getTranslation(STREAM_COMPRESSIONS, streamCompression);
        }
        else if (p_74297_1_ == GameSettings.Options.STREAM_CHAT_ENABLED)
        {
            return s + getTranslation(STREAM_CHAT_MODES, streamChatEnabled);
        }
        else if (p_74297_1_ == GameSettings.Options.STREAM_CHAT_USER_FILTER)
        {
            return s + getTranslation(STREAM_CHAT_FILTER_MODES, streamChatUserFilter);
        }
        else if (p_74297_1_ == GameSettings.Options.STREAM_MIC_TOGGLE_BEHAVIOR)
        {
            return s + getTranslation(STREAM_MIC_MODES, streamMicToggleBehavior);
        }
        else if (p_74297_1_ == GameSettings.Options.RENDER_CLOUDS)
        {
            return s + getTranslation(field_181149_aW, clouds);
        }
        else if (p_74297_1_ == GameSettings.Options.GRAPHICS)
        {
            if (fancyGraphics)
            {
                return s + I18n.format("options.graphics.fancy");
            }
            else
            {
                return s + I18n.format("options.graphics.fast");
            }
        }
        else
        {
            return s;
        }
    }

    /**
     * Loads the options from the options file. It appears that this has replaced the previous 'loadOptions'
     */
    public void loadOptions()
    {
        try
        {
            if (!optionsFile.exists())
            {
                return;
            }

            BufferedReader bufferedreader = new BufferedReader(new FileReader(optionsFile));
            String s;
            mapSoundLevels.clear();

            while ((s = bufferedreader.readLine()) != null)
            {
                try
                {
                    String[] astring = s.split(":");

                    if (astring[0].equals("mouseSensitivity"))
                    {
                        mouseSensitivity = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("fov"))
                    {
                        fovSetting = parseFloat(astring[1]) * 40.0F + 70.0F;
                    }

                    if (astring[0].equals("gamma"))
                    {
                        gammaSetting = 10.0f;
                    }

                    if (astring[0].equals("saturation"))
                    {
                        saturation = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("invertYMouse"))
                    {
                        invertMouse = astring[1].equals("true");
                    }

                    if (astring[0].equals("renderDistance"))
                    {
                        renderDistanceChunks = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("guiScale"))
                    {
                        guiScale = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("particles"))
                    {
                        particleSetting = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("bobView"))
                    {
                        viewBobbing = astring[1].equals("true");
                    }

                    if (astring[0].equals("anaglyph3d"))
                    {
                        anaglyph = astring[1].equals("true");
                    }

                    if (astring[0].equals("maxFps"))
                    {
                        limitFramerate = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("fboEnable"))
                    {
                        fboEnable = astring[1].equals("true");
                    }

                    if (astring[0].equals("difficulty"))
                    {
                        difficulty = EnumDifficulty.getDifficultyEnum(Integer.parseInt(astring[1]));
                    }

                    if (astring[0].equals("fancyGraphics"))
                    {
                        fancyGraphics = astring[1].equals("true");
                    }

                    if (astring[0].equals("ao"))
                    {
                        if (astring[1].equals("true"))
                        {
                            ambientOcclusion = 2;
                        }
                        else if (astring[1].equals("false"))
                        {
                            ambientOcclusion = 0;
                        }
                        else
                        {
                            ambientOcclusion = Integer.parseInt(astring[1]);
                        }
                    }

                    if (astring[0].equals("renderClouds"))
                    {
                        switch (astring[1]) {
                            case "true":
                                clouds = 2;
                                break;
                            case "false":
                                clouds = 0;
                                break;
                            case "fast":
                                clouds = 1;
                                break;
                        }
                    }

                    if (astring[0].equals("resourcePacks"))
                    {
                        resourcePacks = gson.fromJson(s.substring(s.indexOf(58) + 1), typeListString);

                        if (resourcePacks == null)
                        {
                            resourcePacks = Lists.newArrayList();
                        }
                    }

                    if (astring[0].equals("incompatibleResourcePacks"))
                    {
                        field_183018_l = gson.fromJson(s.substring(s.indexOf(58) + 1), typeListString);

                        if (field_183018_l == null)
                        {
                            field_183018_l = Lists.newArrayList();
                        }
                    }

                    if (astring[0].equals("lastServer") && astring.length >= 2)
                    {
                        lastServer = s.substring(s.indexOf(58) + 1);
                    }

                    if (astring[0].equals("lang") && astring.length >= 2)
                    {
                        language = astring[1];
                    }

                    if (astring[0].equals("chatVisibility"))
                    {
                        chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(astring[1]));
                    }

                    if (astring[0].equals("chatColors"))
                    {
                        chatColours = astring[1].equals("true");
                    }

                    if (astring[0].equals("chatLinks"))
                    {
                        chatLinks = astring[1].equals("true");
                    }

                    if (astring[0].equals("chatLinksPrompt"))
                    {
                        chatLinksPrompt = astring[1].equals("true");
                    }

                    if (astring[0].equals("chatOpacity"))
                    {
                        chatOpacity = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("snooperEnabled"))
                    {
                        snooperEnabled = astring[1].equals("true");
                    }

                    if (astring[0].equals("fullscreen"))
                    {
                        fullScreen = astring[1].equals("true");
                    }

                    if (astring[0].equals("enableVsync"))
                    {
                        enableVsync = astring[1].equals("true");
                    }

                    if (astring[0].equals("useVbo"))
                    {
                        useVbo = astring[1].equals("true");
                    }

                    if (astring[0].equals("hideServerAddress"))
                    {
                        hideServerAddress = astring[1].equals("true");
                    }

                    if (astring[0].equals("advancedItemTooltips"))
                    {
                        advancedItemTooltips = astring[1].equals("true");
                    }

                    if (astring[0].equals("pauseOnLostFocus"))
                    {
                        pauseOnLostFocus = astring[1].equals("true");
                    }

                    if (astring[0].equals("touchscreen"))
                    {
                        touchscreen = astring[1].equals("true");
                    }

                    if (astring[0].equals("overrideHeight"))
                    {
                        overrideHeight = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("overrideWidth"))
                    {
                        overrideWidth = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("heldItemTooltips"))
                    {
                        heldItemTooltips = astring[1].equals("true");
                    }

                    if (astring[0].equals("chatHeightFocused"))
                    {
                        chatHeightFocused = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("chatHeightUnfocused"))
                    {
                        chatHeightUnfocused = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("chatScale"))
                    {
                        chatScale = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("chatWidth"))
                    {
                        chatWidth = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("showInventoryAchievementHint"))
                    {
                        showInventoryAchievementHint = astring[1].equals("true");
                    }

                    if (astring[0].equals("mipmapLevels"))
                    {
                        mipmapLevels = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("streamBytesPerPixel"))
                    {
                        streamBytesPerPixel = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("streamMicVolume"))
                    {
                        streamMicVolume = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("streamSystemVolume"))
                    {
                        streamGameVolume = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("streamKbps"))
                    {
                        streamKbps = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("streamFps"))
                    {
                        streamFps = parseFloat(astring[1]);
                    }

                    if (astring[0].equals("streamCompression"))
                    {
                        streamCompression = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("streamSendMetadata"))
                    {
                        streamSendMetadata = astring[1].equals("true");
                    }

                    if (astring[0].equals("streamPreferredServer") && astring.length >= 2)
                    {
                        streamPreferredServer = s.substring(s.indexOf(58) + 1);
                    }

                    if (astring[0].equals("streamChatEnabled"))
                    {
                        streamChatEnabled = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("streamChatUserFilter"))
                    {
                        streamChatUserFilter = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("streamMicToggleBehavior"))
                    {
                        streamMicToggleBehavior = Integer.parseInt(astring[1]);
                    }

                    if (astring[0].equals("forceUnicodeFont"))
                    {
                        forceUnicodeFont = astring[1].equals("true");
                    }

                    if (astring[0].equals("allowBlockAlternatives"))
                    {
                        allowBlockAlternatives = astring[1].equals("true");
                    }

                    if (astring[0].equals("reducedDebugInfo"))
                    {
                        reducedDebugInfo = astring[1].equals("true");
                    }

                    if (astring[0].equals("useNativeTransport"))
                    {
                        field_181150_U = astring[1].equals("true");
                    }

                    if (astring[0].equals("entityShadows"))
                    {
                        field_181151_V = astring[1].equals("true");
                    }

                    for (KeyBinding keybinding : keyBindings)
                    {
                        if (astring[0].equals("key_" + keybinding.getKeyDescription()))
                        {
                            keybinding.setKeyCode(Integer.parseInt(astring[1]));
                        }
                    }

                    for (SoundCategory soundcategory : SoundCategory.values())
                    {
                        if (astring[0].equals("soundCategory_" + soundcategory.getCategoryName()))
                        {
                            mapSoundLevels.put(soundcategory, parseFloat(astring[1]));
                        }
                    }

                    for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values())
                    {
                        if (astring[0].equals("modelPart_" + enumplayermodelparts.getPartName()))
                        {
                            setModelPartEnabled(enumplayermodelparts, astring[1].equals("true"));
                        }
                    }
                }
                catch (Exception var8)
                {
                    logger.warn("Skipping bad option: " + s);
                }
            }

            KeyBinding.resetKeyBindingArrayAndHash();
            bufferedreader.close();
        }
        catch (Exception exception)
        {
            logger.error("Failed to load options", exception);
        }
    }

    /**
     * Parses a string into a float.
     */
    private float parseFloat(String p_74305_1_)
    {
        return p_74305_1_.equals("true") ? 1.0F : (p_74305_1_.equals("false") ? 0.0F : Float.parseFloat(p_74305_1_));
    }

    /**
     * Saves the options to the options file.
     */
    public void saveOptions()
    {
        try
        {
            PrintWriter printwriter = new PrintWriter(new FileWriter(optionsFile));
            printwriter.println("invertYMouse:" + invertMouse);
            printwriter.println("mouseSensitivity:" + mouseSensitivity);
            printwriter.println("fov:" + (fovSetting - 70.0F) / 40.0F);
            printwriter.println("gamma:" + gammaSetting);
            printwriter.println("saturation:" + saturation);
            printwriter.println("renderDistance:" + renderDistanceChunks);
            printwriter.println("guiScale:" + guiScale);
            printwriter.println("particles:" + particleSetting);
            printwriter.println("bobView:" + viewBobbing);
            printwriter.println("anaglyph3d:" + anaglyph);
            printwriter.println("maxFps:" + limitFramerate);
            printwriter.println("fboEnable:" + fboEnable);
            printwriter.println("difficulty:" + difficulty.getDifficultyId());
            printwriter.println("fancyGraphics:" + fancyGraphics);
            printwriter.println("ao:" + ambientOcclusion);

            switch (clouds)
            {
                case 0:
                    printwriter.println("renderClouds:false");
                    break;

                case 1:
                    printwriter.println("renderClouds:fast");
                    break;

                case 2:
                    printwriter.println("renderClouds:true");
            }

            printwriter.println("resourcePacks:" + gson.toJson(resourcePacks));
            printwriter.println("incompatibleResourcePacks:" + gson.toJson(field_183018_l));
            printwriter.println("lastServer:" + lastServer);
            printwriter.println("lang:" + language);
            printwriter.println("chatVisibility:" + chatVisibility.getChatVisibility());
            printwriter.println("chatColors:" + chatColours);
            printwriter.println("chatLinks:" + chatLinks);
            printwriter.println("chatLinksPrompt:" + chatLinksPrompt);
            printwriter.println("chatOpacity:" + chatOpacity);
            printwriter.println("snooperEnabled:" + snooperEnabled);
            printwriter.println("fullscreen:" + fullScreen);
            printwriter.println("enableVsync:" + enableVsync);
            printwriter.println("useVbo:" + useVbo);
            printwriter.println("hideServerAddress:" + hideServerAddress);
            printwriter.println("advancedItemTooltips:" + advancedItemTooltips);
            printwriter.println("pauseOnLostFocus:" + pauseOnLostFocus);
            printwriter.println("touchscreen:" + touchscreen);
            printwriter.println("overrideWidth:" + overrideWidth);
            printwriter.println("overrideHeight:" + overrideHeight);
            printwriter.println("heldItemTooltips:" + heldItemTooltips);
            printwriter.println("chatHeightFocused:" + chatHeightFocused);
            printwriter.println("chatHeightUnfocused:" + chatHeightUnfocused);
            printwriter.println("chatScale:" + chatScale);
            printwriter.println("chatWidth:" + chatWidth);
            printwriter.println("showInventoryAchievementHint:" + showInventoryAchievementHint);
            printwriter.println("mipmapLevels:" + mipmapLevels);
            printwriter.println("streamBytesPerPixel:" + streamBytesPerPixel);
            printwriter.println("streamMicVolume:" + streamMicVolume);
            printwriter.println("streamSystemVolume:" + streamGameVolume);
            printwriter.println("streamKbps:" + streamKbps);
            printwriter.println("streamFps:" + streamFps);
            printwriter.println("streamCompression:" + streamCompression);
            printwriter.println("streamSendMetadata:" + streamSendMetadata);
            printwriter.println("streamPreferredServer:" + streamPreferredServer);
            printwriter.println("streamChatEnabled:" + streamChatEnabled);
            printwriter.println("streamChatUserFilter:" + streamChatUserFilter);
            printwriter.println("streamMicToggleBehavior:" + streamMicToggleBehavior);
            printwriter.println("forceUnicodeFont:" + forceUnicodeFont);
            printwriter.println("allowBlockAlternatives:" + allowBlockAlternatives);
            printwriter.println("reducedDebugInfo:" + reducedDebugInfo);
            printwriter.println("useNativeTransport:" + field_181150_U);
            printwriter.println("entityShadows:" + field_181151_V);

            for (KeyBinding keybinding : keyBindings)
            {
                printwriter.println("key_" + keybinding.getKeyDescription() + ":" + keybinding.getKeyCode());
            }

            for (SoundCategory soundcategory : SoundCategory.values())
            {
                printwriter.println("soundCategory_" + soundcategory.getCategoryName() + ":" + getSoundLevel(soundcategory));
            }

            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values())
            {
                printwriter.println("modelPart_" + enumplayermodelparts.getPartName() + ":" + setModelParts.contains(enumplayermodelparts));
            }

            printwriter.close();
        }
        catch (Exception exception)
        {
            logger.error("Failed to save options", exception);
        }

        sendSettingsToServer();
    }

    public float getSoundLevel(SoundCategory p_151438_1_)
    {
        return mapSoundLevels.getOrDefault(p_151438_1_, 1.0F);
    }

    public void setSoundLevel(SoundCategory p_151439_1_, float p_151439_2_)
    {
        mc.getSoundHandler().setSoundLevel(p_151439_1_, p_151439_2_);
        mapSoundLevels.put(p_151439_1_, p_151439_2_);
    }

    /**
     * Send a client info packet with settings information to the server
     */
    public void sendSettingsToServer()
    {
        if (mc.thePlayer != null)
        {
            int i = 0;

            for (EnumPlayerModelParts enumplayermodelparts : setModelParts)
            {
                i |= enumplayermodelparts.getPartMask();
            }

            mc.thePlayer.sendQueue.addToSendQueue(new C15PacketClientSettings(language, renderDistanceChunks, chatVisibility, chatColours, i));
        }
    }

    public Set<EnumPlayerModelParts> getModelParts()
    {
        return ImmutableSet.copyOf(setModelParts);
    }

    public void setModelPartEnabled(EnumPlayerModelParts p_178878_1_, boolean p_178878_2_)
    {
        if (p_178878_2_)
        {
            setModelParts.add(p_178878_1_);
        }
        else
        {
            setModelParts.remove(p_178878_1_);
        }

        sendSettingsToServer();
    }

    public void switchModelPartEnabled(EnumPlayerModelParts p_178877_1_)
    {
        if (!getModelParts().contains(p_178877_1_))
        {
            setModelParts.add(p_178877_1_);
        }
        else
        {
            setModelParts.remove(p_178877_1_);
        }

        sendSettingsToServer();
    }

    public int func_181147_e()
    {
        return renderDistanceChunks >= 4 ? clouds : 0;
    }

    public boolean func_181148_f()
    {
        return field_181150_U;
    }

    public enum Options
    {
        INVERT_MOUSE("options.invertMouse", false, true),
        SENSITIVITY("options.sensitivity", true, false),
        FOV("options.fov", true, false, 30.0F, 110.0F, 1.0F),
        GAMMA("options.gamma", true, false),
        SATURATION("options.saturation", true, false),
        RENDER_DISTANCE("options.renderDistance", true, false, 2.0F, 16.0F, 1.0F),
        VIEW_BOBBING("options.viewBobbing", false, true),
        ANAGLYPH("options.anaglyph", false, true),
        FRAMERATE_LIMIT("options.framerateLimit", true, false, 10.0F, 260.0F, 10.0F),
        FBO_ENABLE("options.fboEnable", false, true),
        RENDER_CLOUDS("options.renderClouds", false, false),
        GRAPHICS("options.graphics", false, false),
        AMBIENT_OCCLUSION("options.ao", false, false),
        GUI_SCALE("options.guiScale", false, false),
        PARTICLES("options.particles", false, false),
        CHAT_VISIBILITY("options.chat.visibility", false, false),
        CHAT_COLOR("options.chat.color", false, true),
        CHAT_LINKS("options.chat.links", false, true),
        CHAT_OPACITY("options.chat.opacity", true, false),
        CHAT_LINKS_PROMPT("options.chat.links.prompt", false, true),
        SNOOPER_ENABLED("options.snooper", false, true),
        USE_FULLSCREEN("options.fullscreen", false, true),
        ENABLE_VSYNC("options.vsync", false, true),
        USE_VBO("options.vbo", false, true),
        TOUCHSCREEN("options.touchscreen", false, true),
        CHAT_SCALE("options.chat.scale", true, false),
        CHAT_WIDTH("options.chat.width", true, false),
        CHAT_HEIGHT_FOCUSED("options.chat.height.focused", true, false),
        CHAT_HEIGHT_UNFOCUSED("options.chat.height.unfocused", true, false),
        MIPMAP_LEVELS("options.mipmapLevels", true, false, 0.0F, 4.0F, 1.0F),
        FORCE_UNICODE_FONT("options.forceUnicodeFont", false, true),
        STREAM_BYTES_PER_PIXEL("options.stream.bytesPerPixel", true, false),
        STREAM_VOLUME_MIC("options.stream.micVolumne", true, false),
        STREAM_VOLUME_SYSTEM("options.stream.systemVolume", true, false),
        STREAM_KBPS("options.stream.kbps", true, false),
        STREAM_FPS("options.stream.fps", true, false),
        STREAM_COMPRESSION("options.stream.compression", false, false),
        STREAM_SEND_METADATA("options.stream.sendMetadata", false, true),
        STREAM_CHAT_ENABLED("options.stream.chat.enabled", false, false),
        STREAM_CHAT_USER_FILTER("options.stream.chat.userFilter", false, false),
        STREAM_MIC_TOGGLE_BEHAVIOR("options.stream.micToggleBehavior", false, false),
        BLOCK_ALTERNATIVES("options.blockAlternatives", false, true),
        REDUCED_DEBUG_INFO("options.reducedDebugInfo", false, true),
        ENTITY_SHADOWS("options.entityShadows", false, true);

        private final boolean enumFloat;
        private final boolean enumBoolean;
        private final String enumString;
        private final float valueStep;
        private final float valueMin;
        private float valueMax;

        public static GameSettings.Options getEnumOptions(int p_74379_0_)
        {
            for (GameSettings.Options gamesettings$options : values())
            {
                if (gamesettings$options.returnEnumOrdinal() == p_74379_0_)
                {
                    return gamesettings$options;
                }
            }

            return null;
        }

        Options(String p_i1015_3_, boolean p_i1015_4_, boolean p_i1015_5_)
        {
            this(p_i1015_3_, p_i1015_4_, p_i1015_5_, 0.0F, 1.0F, 0.0F);
        }

        Options(String p_i45004_3_, boolean p_i45004_4_, boolean p_i45004_5_, float p_i45004_6_, float p_i45004_7_, float p_i45004_8_)
        {
            enumString = p_i45004_3_;
            enumFloat = p_i45004_4_;
            enumBoolean = p_i45004_5_;
            valueMin = p_i45004_6_;
            valueMax = p_i45004_7_;
            valueStep = p_i45004_8_;
        }

        public boolean getEnumFloat()
        {
            return enumFloat;
        }

        public boolean getEnumBoolean()
        {
            return enumBoolean;
        }

        public int returnEnumOrdinal()
        {
            return ordinal();
        }

        public String getEnumString()
        {
            return enumString;
        }

        public float getValueMax()
        {
            return valueMax;
        }

        public void setValueMax(float p_148263_1_)
        {
            valueMax = p_148263_1_;
        }

        public float normalizeValue(float p_148266_1_)
        {
            return MathHelper.clamp_float((snapToStepClamp(p_148266_1_) - valueMin) / (valueMax - valueMin), 0.0F, 1.0F);
        }

        public float denormalizeValue(float p_148262_1_)
        {
            return snapToStepClamp(valueMin + (valueMax - valueMin) * MathHelper.clamp_float(p_148262_1_, 0.0F, 1.0F));
        }

        public float snapToStepClamp(float p_148268_1_)
        {
            p_148268_1_ = snapToStep(p_148268_1_);
            return MathHelper.clamp_float(p_148268_1_, valueMin, valueMax);
        }

        protected float snapToStep(float p_148264_1_)
        {
            if (valueStep > 0.0F)
            {
                p_148264_1_ = valueStep * (float)Math.round(p_148264_1_ / valueStep);
            }

            return p_148264_1_;
        }
    }
}
