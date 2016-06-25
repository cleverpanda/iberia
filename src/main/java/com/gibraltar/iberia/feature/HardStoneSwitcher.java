package com.gibraltar.iberia.feature;

import com.gibraltar.iberia.Reference;
import com.gibraltar.iberia.blocks.BlockHardStone;
import com.gibraltar.iberia.feature.HardStoneFeature;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class HardStoneSwitcher {
    protected int updateLCG = (new Random()).nextInt();

	@SubscribeEvent
	public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		ItemStack heldItem = event.getEntityPlayer().getHeldItemMainhand();
		if ((heldItem != null && heldItem.getItem() == Items.diamond_pickaxe) && event.getState().getBlock() == HardStoneFeature.hard_stone) {
			event.setNewSpeed(event.getOriginalSpeed() * BlockHardStone.HARDNESS_MULTIPLE);
		}
	}

	private boolean isCompressingBlock(Block block) {
		return block == Blocks.stone || block == HardStoneFeature.hard_stone || block == Blocks.bedrock || block == Blocks.dirt;
	}

	private boolean isSurroundedByCompressingBlocks(World world, BlockPos pos)
	{
		return isCompressingBlock(world.getBlockState(pos.up()).getBlock()) &&
			isCompressingBlock(world.getBlockState(pos.down()).getBlock()) &&
			isCompressingBlock(world.getBlockState(pos.north()).getBlock()) &&
			isCompressingBlock(world.getBlockState(pos.south()).getBlock()) &&
			isCompressingBlock(world.getBlockState(pos.east()).getBlock()) &&
			isCompressingBlock(world.getBlockState(pos.west()).getBlock());
	}


	@SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.START || event.side == Side.CLIENT)
			return;

		int i = event.world.getGameRules().getInt("randomTickSpeed");

		if (i > 0)
		{
			Iterator<Chunk> chunkIterator = ((WorldServer)event.world).getPlayerChunkManager().getChunkIterator();
			Iterator<Chunk> iterator = net.minecraftforge.common.ForgeChunkManager.getPersistentChunksIterableFor(event.world, chunkIterator);

			// The following loop mimics the built in random tick loop to provide random ticks for stone and hard stone blocks
			while (iterator.hasNext())
			{
				Chunk chunk = (Chunk)iterator.next();
				int j = chunk.xPosition * 16;
				int k = chunk.zPosition * 16;
				for (ExtendedBlockStorage extendedblockstorage : chunk.getBlockStorageArray())
				{
					if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && !extendedblockstorage.isEmpty())
					{
						for (int i1 = 0; i1 < i; ++i1)
						{
							this.updateLCG = this.updateLCG * 3 + 1013904223;
							int j1 = this.updateLCG >> 2;
							int k1 = j1 & 15;
							int l1 = j1 >> 8 & 15;
							int i2 = j1 >> 16 & 15;
							IBlockState iblockstate = extendedblockstorage.get(k1, i2, l1);
							Block block = iblockstate.getBlock();

							if (block == Blocks.stone || block == HardStoneFeature.hard_stone)
							{
								BlockPos pos = new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k);
								boolean hard = block == HardStoneFeature.hard_stone;
								boolean shouldBeHard = isSurroundedByCompressingBlocks(event.world, pos);

								if (hard != shouldBeHard)
								{
									Block newBlock = shouldBeHard ? HardStoneFeature.hard_stone : Blocks.stone;
									event.world.setBlockState(pos, newBlock.getStateFromMeta(block.getMetaFromState(iblockstate)), 6 /*no block update, no re-render*/);
								}
							}
						}
					}
				}
			}
		}
    }
}