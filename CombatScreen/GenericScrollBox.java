package TBC.CombatScreen;

import java.awt.Window;
import java.util.ArrayList;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import TBC.Pair;
import TBC.TBCMod;
import TBC.Triplet;
import TBC.Combat.Abilities.ICombatAbility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemSaddle;

public class GenericScrollBox extends GuiButton
{
	public static final int ySpacing = 10;
	private int xSpacing = 80;
	private ArrayList<Triplet<String, String, IGenericAction>> items;
	private ArrayList<Triplet<String, String, IGenericAction>> constantItems;
	private ArrayList<Triplet<Integer, Integer, Integer>> positions;
	private ArrayList<Triplet<Integer, Integer, Integer>> constantPositions;
	private int numColumns;
	private int numRows;
	private int offset;

	public GenericScrollBox(int par1, int par2, int par3, int par4, int par5, String par6Str, ArrayList<Triplet<String, String, IGenericAction>> items, ArrayList<Triplet<String, String, IGenericAction>> constantItems, Integer numColumns)
	{
		super(par1, par2, par3, par4, par5, par6Str);
		this.offset = 0;
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		int maxStringLength = 0;
		for(Triplet<String, String, IGenericAction> i : items)
		{
			int stringLength = fontRenderer.getStringWidth(i.item1);
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
					IGenericAction action = this.items.get(positions.get(i).item3).item3;
					if(action != null)
					{
						action.Invoke();
						return true;
					}
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
					IGenericAction action = this.constantItems.get(itemIndex).item3;
					if(action == null)
					{
						return false;
					}

					action.Invoke();
				}

				return true;
			}
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
            		String itemString = items.get(itemPosition).item1;
                	String countString = items.get(itemPosition).item2;
                	int fontColor = 14737632;
                	if(items.get(itemPosition).item3 == null)
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
            		String itemString = constantItems.get(itemPosition).item1;
                	String countString = constantItems.get(itemPosition).item2;
                	int fontColor = 14737632;
                	if(constantItems.get(itemPosition).item3 == null)
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
}
