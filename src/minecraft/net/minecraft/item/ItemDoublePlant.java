package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.world.ColorizerGrass;

import java.util.function.Function;

public class ItemDoublePlant extends ItemMultiTexture
{
    public ItemDoublePlant(Block block, Block block2, Function<ItemStack, String> nameFunction)
    {
        super(block, block2, nameFunction);
    }

    public int getColorFromItemStack(ItemStack stack, int renderPass)
    {
        BlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = BlockDoublePlant.EnumPlantType.byMetadata(stack.getMetadata());
        return blockdoubleplant$enumplanttype != BlockDoublePlant.EnumPlantType.GRASS && blockdoubleplant$enumplanttype != BlockDoublePlant.EnumPlantType.FERN ? super.getColorFromItemStack(stack, renderPass) : ColorizerGrass.getGrassColor(0.5D, 1.0D);
    }
}
