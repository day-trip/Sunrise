package net.minecraft.item;

import net.minecraft.block.Block;

import java.util.function.Function;

public class ItemMultiTexture extends ItemBlock
{
    protected final Block theBlock;
    protected final Function<ItemStack, String> nameFunction;

    public ItemMultiTexture(Block block, Block block2, Function<ItemStack, String> nameFunction)
    {
        super(block);
        theBlock = block2;
        this.nameFunction = nameFunction;
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    public ItemMultiTexture(Block block, Block block2, String[] namesByMeta)
    {
        this(block, block2, p_apply_1_ -> {
            int i = p_apply_1_.getMetadata();

            if (i < 0 || i >= namesByMeta.length)
            {
                i = 0;
            }

            return namesByMeta[i];
        });
    }

    /**
     * Converts the given ItemStack damage value into a metadata value to be placed in the world when this Item is
     * placed as a Block (mostly used with ItemBlocks).
     */
    public int getMetadata(int damage)
    {
        return damage;
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack stack)
    {
        return getUnlocalizedName() + "." + nameFunction.apply(stack);
    }
}
