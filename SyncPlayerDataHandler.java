package TBC;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet40EntityMetadata;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ChunkLoader;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class SyncPlayerDataHandler implements IPacketHandler 
{
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) 
	{
		if(player instanceof EntityPlayerMP)
		{
			EntityPlayerMP playerEntity = (EntityPlayerMP)player;
			if(packet.data.length != 0)
			{
				NBTTagCompound c = null;
				try {
					c = (NBTTagCompound) NBTTagCompound.readNamedTag(new DataInputStream(new ByteArrayInputStream(packet.data)));
				} catch (IOException e) {}
				
				NBTTagCompound tag = playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
				MergeTagCompound(c, tag);
				playerEntity.getEntityData().setCompoundTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try {
				NBTTagCompound.writeNamedTag(playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG), new DataOutputStream(outputStream));
			} catch (IOException e) {}
				
			playerEntity.playerNetServerHandler.sendPacketToPlayer(new Packet250CustomPayload("TBCPlayerData", outputStream.toByteArray()));
		}
		else
		{
			EntityPlayer playerEntity = (EntityPlayer)player;
			NBTTagCompound c = null;
			try {
				c = (NBTTagCompound) NBTTagCompound.readNamedTag(new DataInputStream(new ByteArrayInputStream(packet.data)));
			} catch (IOException e) {}
			
			
			NBTTagCompound tag = playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			MergeTagCompound(c, tag);
			playerEntity.getEntityData().setCompoundTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
			MainMod.playerDataInit = true;
		}
	}
	
	private void MergeTagCompound(NBTTagCompound newTag, NBTTagCompound existingTag)
	{
		if(newTag.hasKey("TBCPlayerMP"))
		{
			existingTag.setInteger("TBCPlayerMP", newTag.getInteger("TBCPlayerMP"));
		}
		
		if(newTag.hasKey("playerXP"))
		{
			existingTag.setInteger("playerXP", newTag.getInteger("playerXP"));
		}
		
		if(newTag.hasKey("playerAP"))
		{
			existingTag.setInteger("playerAP", newTag.getInteger("playerAP"));
		}
		
		if(newTag.hasKey("playerLevel"))
		{
			existingTag.setInteger("playerLevel", newTag.getInteger("playerLevel"));
		}
		
		if(newTag.hasKey("bowSkillLevel"))
		{
			existingTag.setInteger("bowSkillLevel", newTag.getInteger("bowSkillLevel"));
		}
		
		if(newTag.hasKey("swordSkillLevel"))
		{
			existingTag.setInteger("swordSkillLevel", newTag.getInteger("swordSkillLevel"));
		}
		
		if(newTag.hasKey("axeSkillLevel"))
		{
			existingTag.setInteger("axeSkillLevel", newTag.getInteger("axeSkillLevel"));
		}

		if(newTag.hasKey("playerMaxHP"))
		{
			existingTag.setInteger("playerMaxHP", newTag.getInteger("playerMaxHP"));
		}
		
		if(newTag.hasKey("playerMaxMP"))
		{
			existingTag.setInteger("playerMaxMP", newTag.getInteger("playerMaxMP"));
		}
		
		if(newTag.hasKey("playerAttack"))
		{
			existingTag.setInteger("playerAttack", newTag.getInteger("playerAttack"));
		}
		
		if(newTag.hasKey("playerDefense"))
		{
			existingTag.setInteger("playerDefense", newTag.getInteger("playerDefense"));
		}
		
		if(newTag.hasKey("playerMAttack"))
		{
			existingTag.setInteger("playerMAttack", newTag.getInteger("playerMAttack"));
		}
		
		if(newTag.hasKey("playerMDefense"))
		{
			existingTag.setInteger("playerMDefense", newTag.getInteger("playerMDefense"));
		}
		
		if(newTag.hasKey("playerSpeed"))
		{
			existingTag.setInteger("playerSpeed", newTag.getInteger("playerSpeed"));
		}
		
		if(newTag.hasKey("playerAbilities"))
		{
			existingTag.setString("playerAbilities", newTag.getString("playerAbilities"));
		}
		
		if(newTag.hasKey("questProgress"))
		{
			existingTag.setInteger("questProgress", newTag.getInteger("questProgress"));
		}
	}
}
