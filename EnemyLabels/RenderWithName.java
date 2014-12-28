package TBC.EnemyLabels;

import java.util.HashSet;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

public class RenderWithName extends Render
{
	public Render internalRender;

	public RenderWithName(Render internalRender)
	{
		this.renderManager = RenderManager.instance;
		this.internalRender = internalRender;
	}

	public void doRender(Entity entity, double d0, double d1, double d2, float f, float f1)
	{
		if(entity instanceof EntityLiving)
		{
			RenderEntityName((EntityLiving)entity, d0, d1, d2, entity.getEntityData().getString("TBCEntityName"));
		}

		this.internalRender.doRender(entity, d0, d1, d2, f, f1);
	}

	public void doRenderShadowAndFire(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
	{
		this.internalRender.doRenderShadowAndFire(par1Entity, par2, par4, par6, par8, par9);
	}

	private void RenderEntityName(EntityLiving par1EntityLiving, double par3, double par5, double par7, String par2Str)
	{
        float f = 1.6F;
        float f1 = 0.016666668F * f;
        FontRenderer fontrenderer = this.renderManager.getFontRenderer();

        GL11.glPushMatrix();
        GL11.glTranslatef((float)par3 + 0.0F, (float)par5 + par1EntityLiving.height + 0.5F, (float)par7);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-f1, -f1, f1);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.instance;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        int j = fontrenderer.getStringWidth(par2Str) / 2;
        tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
        tessellator.addVertex((double)(-j - 1), (double)-1, 0.0D);
        tessellator.addVertex((double)(-j - 1), (double)8, 0.0D);
        tessellator.addVertex((double)(j + 1), (double)8, 0.0D);
        tessellator.addVertex((double)(j + 1), (double)-1, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        fontrenderer.drawString(par2Str, -fontrenderer.getStringWidth(par2Str) / 2, 0, 553648127);
        GL11.glDepthMask(true);
        fontrenderer.drawString(par2Str, -fontrenderer.getStringWidth(par2Str) / 2, 0, -1);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) 
	{
		return null;
	}
}
