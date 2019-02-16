package com.svennieke.statues.blocks.Statues;

import com.svennieke.statues.blocks.IStatue;
import com.svennieke.statues.blocks.StatueBase.BlockEtho;
import com.svennieke.statues.entity.fakeentity.FakeCreeper;
import com.svennieke.statues.tileentity.StatueTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class BlockEtho_Statue extends BlockEtho implements IStatue{
	
	private int TIER;

	public BlockEtho_Statue(Block.Properties builder) {
		super(builder);
		//setRegistryName(registry);
		//setUnlocalizedName(unlocalised);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Block setTier(int tier)
	{
		this.TIER = tier;
//		setUnlocalizedName(super.getUnlocalizedName().replace("tile.", "") + (tier > 1 ? "t" + tier : ""));
		//setRegistryName("block" + super.getTranslationKey().replace("tile.", ""));
		return this;
	}
	
	@Override
	public int getTier()
	{
		return this.TIER;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(IBlockState state, IBlockReader world) {
		return new StatueTileEntity();
	}
	
	private StatueTileEntity getTE(World world, BlockPos pos) {
        return (StatueTileEntity) world.getTileEntity(pos);
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.animateTick(stateIn, worldIn, pos, rand);
		if (rand.nextInt(24) == 0)
        {
            worldIn.playSound((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 0.3F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
        }
	}
    
	@Override
	public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(this.TIER >= 2)
		{
	        if (!worldIn.isRemote) {
	        	int statuetier = getTE(worldIn, pos).getTier();
	        	if(statuetier != this.TIER)
	        	{
	        		getTE(worldIn, pos).setTier(this.TIER);
	        	}
	        	/*
	        	ArrayList<ItemStack> stackList = new ArrayList<>(StatueLootList.getStacksForStatue("etho"));
	        	ItemStack stack1 = stackList.get(0);
        		ItemStack stack2 = stackList.get(1);
        		ItemStack stack3 = stackList.get(2);
        		
        		if(stack1.getItem() != StatuesItems.marshmallow)
        		{
        			getTE(worldIn, pos).PlaySound(SoundEvents.BLOCK_FIRE_AMBIENT, pos, worldIn);
    	        	getTE(worldIn, pos).GiveItem(stack1, stack2, stack3, playerIn);
        		}
        		else
        		{
    	        	getTE(worldIn, pos).CampfireBehavior(worldIn, pos, playerIn, stack1, stack2, stack3);
        		}
        		
	        	getTE(worldIn, pos).FakeMobs(getGeneral(worldIn), worldIn, pos, false);
	        	*/
	        }
	        return true;
		}
		else
		return false;
	}
	
	public FakeCreeper getGeneral(World worldIn)
	{
		FakeCreeper general = new FakeCreeper(worldIn);
		general.setCustomName(new TextComponentString("General Spazz"));
        
        return general;
	}
}
