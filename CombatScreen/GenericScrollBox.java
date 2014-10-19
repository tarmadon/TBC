package TBC.CombatScreen;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import TBC.Pair;
import TBC.TBCMod;
import TBC.Triplet;
import TBC.Combat.Abilities.ICombatAbility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemSaddle;

public class GenericScrollBox extends GuiButton
{
	public static final int ySpacing = 10;
	private int xSpacing = 80;
	private ArrayList<GenericScrollBoxCellData> items;
	private ArrayList<GenericScrollBoxCellData> constantItems;
	private ArrayList<Triplet<Integer, Integer, Integer>> positions;
	private ArrayList<Triplet<Integer, Integer, Integer>> constantPositions;
	private int numColumns;
	private int numRows;
	private int offset;

	public GenericScrollBox(int par1, int par2, int par3, int par4, int par5, String par6Str, ArrayList<GenericScrollBoxCellData> items, ArrayList<GenericScrollBoxCellData> constantItems, Integer numColumns)
	{
		super(par1, par2, par3, par4, par5, par6Str);
		this.offset = 0;
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		int maxStringLength = 0;
		for(GenericScrollBoxCellData i : items)
		{
			int stringLength = fontRenderer.getStringWidth(i.Text);
			if(stringLength > maxStringLength)
			{
				maxStringLength = stringLength;
			}
		}

		if(maxStringLength > 60)
		{
			xSpacing = maxStringLength + 20;
		}

		if(numColumns != 0)
		{
			this.numColumns = numColumns;
		}
		else
		{
			this.numColumns = par4/xSpacing;
		}

		this.numRows = (par5/ySpacing) - 1;
		int curCol = 0;
		int curRow = 0;
		positions = new ArrayList<Triplet<Integer,Integer,Integer>>();
		for(int i = 0; i < items.size(); i++)
		{
			positions.add(new Triplet<Integer, Integer, Integer>(curCol * xSpacing, curRow * ySpacing, i));
			curCol++;
			if(curCol == this.numColumns)
			{
				curCol = 0;
				curRow++;
			}
		}

		curCol = 0;
		constantPositions = new ArrayList<Triplet<Integer,Integer,Integer>>();
		for(int i = 0; i < constantItems.size(); i++)
		{
			constantPositions.add(new Triplet<Integer, Integer, Integer>(curCol * xSpacing, numRows * ySpacing, i));
			curCol++;
		}

		if(positions.size() > this.numColumns * this.numRows)
		{
			constantPositions.add(new Triplet<Integer, Integer, Integer>(this.width - 4, 2, -1));
			constantPositions.add(new Triplet<Integer, Integer, Integer>(this.width - 4, (numRows * ySpacing) - (ySpacing/2 + 2), -2));
		}

		this.enabled = true;
		this.items = items;
		this.constantItems = constantItems;
	}

	public boolean onClick(int x, int y)
	{
		GenericScrollBoxCellData d = GetCellUnderMouse(x, y);
		if(d != null && d.OnClick != null)
		{
			d.OnClick.Invoke();
			return true;
		}
		
		return false;
	}

	public void drawButton(Minecraft par1Minecraft, int par2, int par3)
    {
        if (this.visible)
        {
            FontRenderer fontrenderer = par1Minecraft.fontRenderer;
            par1Minecraft.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;
            int k = this.getHoverState(this.field_146123_n);

            this.drawRect(this.xPosition, this.yPosition, this.width, this.height, 0);
            int yOffset = this.offset * ySpacing;
            for(int i = 0; i< positions.size(); i++)
            {
            	int itemPosition = positions.get(i).item3;
            	int modifiedYPos = positions.get(i).item2 - yOffset;
            	if(modifiedYPos >= 0 && modifiedYPos < numRows * ySpacing)
            	{
            		String itemString = items.get(itemPosition).Text;
                	String countString = items.get(itemPosition).AdditionalData;
                	int fontColor = 14737632;
                	if(items.get(itemPosition).OnClick == null)
                	{
                		fontColor = 2;
                	}

                	fontrenderer.drawString(itemString, this.xPosition + positions.get(i).item1, this.yPosition + modifiedYPos, fontColor);
                	if(!countString.isEmpty())
                	{
                		fontrenderer.drawString(countString, this.xPosition + positions.get(i).item1 + this.xSpacing - 20, this.yPosition + modifiedYPos, fontColor);
                	}
            	}
            }

            for(int i = 0; i< constantPositions.size(); i++)
            {
            	int itemPosition = constantPositions.get(i).item3;
            	if(itemPosition == -1)
            	{
            		fontrenderer.drawString("▲", this.xPosition + constantPositions.get(i).item1, this.yPosition + constantPositions.get(i).item2, 14737632);
            	}
            	else if(itemPosition == -2)
            	{
            		fontrenderer.drawString("▼", this.xPosition + constantPositions.get(i).item1, this.yPosition + constantPositions.get(i).item2, 14737632);
            	}
            	else
            	{
            		String itemString = constantItems.get(itemPosition).Text;
                	String countString = constantItems.get(itemPosition).AdditionalData;
                	int fontColor = 14737632;
                	if(constantItems.get(itemPosition).OnClick == null)
                	{
                		fontColor = 2;
                	}

                	fontrenderer.drawString(itemString, this.xPosition + constantPositions.get(i).item1, this.yPosition + constantPositions.get(i).item2, fontColor);
                	if(!countString.isEmpty())
                	{
                		fontrenderer.drawString(countString, this.xPosition + constantPositions.get(i).item1 + this.xSpacing - 20, this.yPosition + constantPositions.get(i).item2, fontColor);
                	}
            	}
            }
        }
    }
	
	public GenericScrollBoxCellData GetCellUnderMouse(int x, int y)
	{
		int yOffset = this.offset * ySpacing;
		int mouseXPos = x - this.xPosition;
		int mouseYPos = y - this.yPosition;

		if(mouseYPos <= numRows * ySpacing)
		{
			for(int i = 0; i < positions.size(); i++)
			{
				int positionYPos = positions.get(i).item2 - yOffset;
				int positionsXPos = positions.get(i).item1;
				if(mouseXPos < positionsXPos + xSpacing && mouseXPos > positionsXPos && mouseYPos < positionYPos + ySpacing && mouseYPos > positionYPos)
				{
					return this.items.get(positions.get(i).item3);
				}
			}
		}

		for(int i = 0; i < constantPositions.size(); i++)
		{
			int positionYPos = constantPositions.get(i).item2;
			int positionsXPos = constantPositions.get(i).item1;
			if(mouseXPos < positionsXPos + xSpacing && mouseXPos > positionsXPos && mouseYPos < positionYPos + ySpacing && mouseYPos > positionYPos)
			{
				int itemIndex = constantPositions.get(i).item3;
				if(itemIndex == -1)
				{
					if(offset > 0)
					{
						offset--;
					}
				}
				else if(itemIndex == -2)
				{
					if(items.size() > (numRows * numColumns) + (numColumns * offset))
					{
						offset++;
					}
				}
				else
				{
					return this.constantItems.get(itemIndex);
				}

				return null;
			}
		}

		return null;
	}
	
	protected void drawHoveringText(List p_146283_1_, int p_146283_2_, int p_146283_3_, FontRenderer font)
    {
        if (!p_146283_1_.isEmpty())
        {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int k = 0;
            Iterator iterator = p_146283_1_.iterator();

            while (iterator.hasNext())
            {
                String s = (String)iterator.next();
                int l = font.getStringWidth(s);

                if (l > k)
                {
                    k = l;
                }
            }

            int j2 = p_146283_2_ + 12;
            int k2 = p_146283_3_ - 12;
            int i1 = 8;

            if (p_146283_1_.size() > 1)
            {
                i1 += 2 + (p_146283_1_.size() - 1) * 10;
            }

            if (j2 + k > this.width)
            {
                j2 -= 28 + k;
            }

            if (k2 + i1 + 6 > this.height)
            {
                k2 = this.height - i1 - 6;
            }

            this.zLevel = 300.0F;
            int j1 = -267386864;
            this.drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
            this.drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1, j1);
            this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
            this.drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
            this.drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1, j1);
            int k1 = 1347420415;
            int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
            this.drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
            this.drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
            this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
            this.drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1, l1);

            for (int i2 = 0; i2 < p_146283_1_.size(); ++i2)
            {
                String s1 = (String)p_146283_1_.get(i2);
                font.drawStringWithShadow(s1, j2, k2, -1);

                if (i2 == 0)
                {
                    k2 += 2;
                }

                k2 += 10;
            }

            this.zLevel = 0.0F;
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }
}
