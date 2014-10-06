package TBC;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import TBC.Messages.NBTTagCompoundMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ChunkLoader;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class SyncPlayerDataHandler implements IMessageHandler<NBTTagCompoundMessage, NBTTagCompoundMessage>
{
	@Override
	public NBTTagCompoundMessage onMessage(NBTTagCompoundMessage message, MessageContext ctx) 
	{
		if(ctx.side == Side.SERVER)
		{
			EntityPlayerMP playerEntity = (EntityPlayerMP)ctx.getServerHandler().playerEntity;
			if(message.tag != null)
			{
				NBTTagCompound tag = playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
				MergeTagCompound(message.tag, tag);
				playerEntity.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
			}

			NBTTagCompoundMessage replyMessage = new NBTTagCompoundMessage();
			replyMessage.tag = playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			return replyMessage;
		}
		else if(message.tag != null)
		{
			EntityPlayer playerEntity = Minecraft.getMinecraft().thePlayer;
			NBTTagCompound c = message.tag;
			NBTTagCompound tag = playerEntity.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
			MergeTagCompound(c, tag);
			playerEntity.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, tag);
			MainMod.playerDataInit = true;
		}
		
		return null;
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
