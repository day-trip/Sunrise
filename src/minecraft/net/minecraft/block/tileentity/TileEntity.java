package net.minecraft.block.tileentity;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TileEntity
{
    private static final Logger logger = LogManager.getLogger();
    private static final Map < String, Class <? extends TileEntity >> nameToClassMap = Maps.newHashMap();
    private static final Map < Class <? extends TileEntity > , String > classToNameMap = Maps.newHashMap();

    /** the instance of the world the tile entity is in. */
    protected World worldObj;
    protected BlockPos pos = BlockPos.ORIGIN;
    protected boolean tileEntityInvalid;
    private int blockMetadata = -1;

    /** the Block type that this TileEntity is contained within */
    protected Block blockType;

    /**
     * Adds a new two-way mapping between the class and its string name in both hashmaps.
     */
    private static void addMapping(Class <? extends TileEntity > cl, String id)
    {
        if (nameToClassMap.containsKey(id))
        {
            throw new IllegalArgumentException("Duplicate id: " + id);
        }
        else
        {
            nameToClassMap.put(id, cl);
            classToNameMap.put(cl, id);
        }
    }

    /**
     * Returns the worldObj for this tileEntity.
     */
    public World getWorld()
    {
        return worldObj;
    }

    /**
     * Sets the worldObj for this tileEntity.
     */
    public void setWorldObj(World worldIn)
    {
        worldObj = worldIn;
    }

    /**
     * Returns true if the worldObj isn't null.
     */
    public boolean hasWorldObj()
    {
        return worldObj != null;
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        pos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        String s = classToNameMap.get(getClass());

        if (s == null)
        {
            throw new RuntimeException(getClass() + " is missing a mapping! This is a bug!");
        }
        else
        {
            compound.setString("id", s);
            compound.setInteger("x", pos.getX());
            compound.setInteger("y", pos.getY());
            compound.setInteger("z", pos.getZ());
        }
    }

    /**
     * Creates a new entity and loads its data from the specified NBT.
     */
    public static TileEntity createAndLoadEntity(NBTTagCompound nbt)
    {
        TileEntity tileentity = null;

        try
        {
            Class <? extends TileEntity > oclass = nameToClassMap.get(nbt.getString("id"));

            if (oclass != null)
            {
                tileentity = oclass.newInstance();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        if (tileentity != null)
        {
            tileentity.readFromNBT(nbt);
        }
        else
        {
            logger.warn("Skipping BlockEntity with id " + nbt.getString("id"));
        }

        return tileentity;
    }

    public int getBlockMetadata()
    {
        if (blockMetadata == -1)
        {
            IBlockState iblockstate = worldObj.getBlockState(pos);
            blockMetadata = iblockstate.getBlock().getMetaFromState(iblockstate);
        }

        return blockMetadata;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void markDirty()
    {
        if (worldObj != null)
        {
            IBlockState iblockstate = worldObj.getBlockState(pos);
            blockMetadata = iblockstate.getBlock().getMetaFromState(iblockstate);
            worldObj.markChunkDirty(pos, this);

            if (getBlockType() != Blocks.air)
            {
                worldObj.updateComparatorOutputLevel(pos, getBlockType());
            }
        }
    }

    /**
     * Returns the square of the distance between this entity and the passed in coordinates.
     */
    public double getDistanceSq(double x, double y, double z)
    {
        double d0 = (double) pos.getX() + 0.5D - x;
        double d1 = (double) pos.getY() + 0.5D - y;
        double d2 = (double) pos.getZ() + 0.5D - z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double getMaxRenderDistanceSquared()
    {
        return 4096.0D;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    /**
     * Gets the block type at the location of this entity (client-only).
     */
    public Block getBlockType()
    {
        if (blockType == null)
        {
            blockType = worldObj.getBlockState(pos).getBlock();
        }

        return blockType;
    }

    /**
     * Allows for a specialized description packet to be created. This is often used to sync tile entity data from the
     * server to the client easily. For example this is used by signs to synchronise the text to be displayed.
     */
    public Packet getDescriptionPacket()
    {
        return null;
    }

    public boolean isInvalid()
    {
        return tileEntityInvalid;
    }

    /**
     * invalidates a tile entity
     */
    public void invalidate()
    {
        tileEntityInvalid = true;
    }

    /**
     * validates a tile entity
     */
    public void validate()
    {
        tileEntityInvalid = false;
    }

    public boolean receiveClientEvent(int id, int type)
    {
        return false;
    }

    public void updateContainingBlockInfo()
    {
        blockType = null;
        blockMetadata = -1;
    }

    public void addInfoToCrashReport(CrashReportCategory reportCategory)
    {
        reportCategory.addCrashSectionCallable("Name", new Callable<String>()
        {
            public String call() throws Exception
            {
                return classToNameMap.get(TileEntity.this.getClass()) + " // " + TileEntity.this.getClass().getCanonicalName();
            }
        });

        if (worldObj != null)
        {
            CrashReportCategory.addBlockInfo(reportCategory, pos, getBlockType(), getBlockMetadata());
            reportCategory.addCrashSectionCallable("Actual block type", new Callable<String>()
            {
                public String call() throws Exception
                {
                    int i = Block.getIdFromBlock(worldObj.getBlockState(pos).getBlock());

                    try
                    {
                        return String.format("ID #%d (%s // %s)", Integer.valueOf(i), Block.getBlockById(i).getUnlocalizedName(), Block.getBlockById(i).getClass().getCanonicalName());
                    }
                    catch (Throwable var3)
                    {
                        return "ID #" + i;
                    }
                }
            });
            reportCategory.addCrashSectionCallable("Actual block data value", new Callable<String>()
            {
                public String call() throws Exception
                {
                    IBlockState iblockstate = worldObj.getBlockState(pos);
                    int i = iblockstate.getBlock().getMetaFromState(iblockstate);

                    if (i < 0)
                    {
                        return "Unknown? (Got " + i + ")";
                    }
                    else
                    {
                        String s = String.format("%4s", new Object[] {Integer.toBinaryString(i)}).replace(" ", "0");
                        return String.format("%1$d / 0x%1$X / 0b%2$s", Integer.valueOf(i), s);
                    }
                }
            });
        }
    }

    public void setPos(BlockPos posIn)
    {
        pos = posIn;
    }

    public boolean func_183000_F()
    {
        return false;
    }

    static
    {
        addMapping(TileEntityFurnace.class, "Furnace");
        addMapping(TileEntityChest.class, "Chest");
        addMapping(TileEntityEnderChest.class, "EnderChest");
        addMapping(BlockJukebox.TileEntityJukebox.class, "RecordPlayer");
        addMapping(TileEntityDispenser.class, "Trap");
        addMapping(TileEntityDropper.class, "Dropper");
        addMapping(TileEntitySign.class, "Sign");
        addMapping(TileEntityMobSpawner.class, "MobSpawner");
        addMapping(TileEntityNote.class, "Music");
        addMapping(TileEntityPiston.class, "Piston");
        addMapping(TileEntityBrewingStand.class, "Cauldron");
        addMapping(TileEntityEnchantmentTable.class, "EnchantTable");
        addMapping(TileEntityEndPortal.class, "Airportal");
        addMapping(TileEntityCommandBlock.class, "Control");
        addMapping(TileEntityBeacon.class, "Beacon");
        addMapping(TileEntitySkull.class, "Skull");
        addMapping(TileEntityDaylightDetector.class, "DLDetector");
        addMapping(TileEntityHopper.class, "Hopper");
        addMapping(TileEntityComparator.class, "Comparator");
        addMapping(TileEntityFlowerPot.class, "FlowerPot");
        addMapping(TileEntityBanner.class, "Banner");
    }
}
