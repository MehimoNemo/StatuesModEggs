package com.svennieke.statues.blocks.Statues;

import com.svennieke.statues.blocks.StatueBase.BlockSombrero;
import com.svennieke.statues.init.StatuesBlocks;
import com.svennieke.statues.util.ParticleUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSombrero_Statue extends BlockSombrero{
		
	public BlockSombrero_Statue(Block.Properties builder) {
		super(builder);
		//setUnlocalizedName(unlocalised);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) 
	{
    	Block block = worldIn.getBlockState(pos.down()).getBlock();
		if (block == Blocks.CACTUS) {
			ParticleUtil.emitExplosionParticles(worldIn, pos);
    		worldIn.setBlockState(pos.down(), StatuesBlocks.bumbo_statue.getDefaultState().with(HORIZONTAL_FACING, placer.getHorizontalFacing().getOpposite()));
    		worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
    	}
    	super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
}
