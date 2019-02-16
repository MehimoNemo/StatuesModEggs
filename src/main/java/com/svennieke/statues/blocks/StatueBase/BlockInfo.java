package com.svennieke.statues.blocks.StatueBase;

import com.svennieke.statues.blocks.BaseBlock.BaseNormal;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BlockInfo extends BaseNormal{
	private static final VoxelShape BOUNDING_BOX = Block.makeCuboidShape(4, 0.0D, 4, 12, 8, 12);

	public BlockInfo(Block.Properties builder) {
		super(builder.sound(SoundType.STONE));
//		this.setCreativeTab(Statues.instance.tabStatues);
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        return BOUNDING_BOX;
    }
    
//    @Override
//    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, VoxelShape entityBox, List<VoxelShape> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_)
//    {
//    	addCollisionBoxToList(pos, entityBox, collidingBoxes, BOUNDING_BOX);
//    }
}
