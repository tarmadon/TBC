package TBC.Menu;

import java.util.ArrayList;

import net.minecraft.client.gui.FontRenderer;
import TBC.CombatEntitySaveData;
import TBC.HenchmanItem;
import TBC.Combat.CombatEntity;
import TBC.Combat.LevelingEngine;
import TBC.CombatScreen.GenericScrollBoxCellData;
import TBC.CombatScreen.IGenericAction;

public class ShowStatsForCharMenuFunction implements IGenericAction, ICustomMenuRender
{
	private StatsGui gui;
	private StatMenuCharData player;
	private CombatEntitySaveData xpData;
	
	public ShowStatsForCharMenuFunction(StatsGui gui, StatMenuCharData player)
	{
		this.gui = gui;
		this.player = player;
	}

	@Override
	public void Invoke() 
	{
		if(this.player.Player != null)
		{
			xpData = LevelingEngine.Instance.GetPlayerSaveData(this.player.Player);
		}
		else
		{
			xpData = HenchmanItem.GetCombatEntitySaveData(this.player.Item);
		}
		
		ArrayList<GenericScrollBoxCellData> buttons = new ArrayList<GenericScrollBoxCellData>();
		buttons.add(new GenericScrollBoxCellData("Name:   " + this.player.CombatEntity.name, "", null));
		
		ArrayList<GenericScrollBoxCellData> constantButtons = new ArrayList<GenericScrollBoxCellData>();
		constantButtons.add(new GenericScrollBoxCellData("Back", "", new SelectStatsMenuFunction(this.gui)));
		
		this.gui.ChangeButtonForCustomRenderer(this, "ShowStats", buttons, constantButtons, 0);
	}

	@Override
	public void Render(StatsGui gui, FontRenderer fontRendererObj) 
	{
		int leftLabelXPos = 10;
    	int leftValueXPos = leftLabelXPos + 37;
    	int rightLabelXPos = leftValueXPos + 57;
    	int rightValueXPos = rightLabelXPos + 37;

    	int zeroLineYPos = 25;
    	int firstLineYPos = 44;
    	int secondLineYPos = 56;
    	int thirdLineYPos = 68;
    	int fourthLineYPos = 80;
    	int fifthLineYPos = 92;
    	int sixthLineYPos = 104;
    	int seventhLineYPos = 116;

    	fontRendererObj.drawString("Level:", leftLabelXPos, zeroLineYPos, 2);
    	fontRendererObj.drawString(xpData.Level + "", leftValueXPos, zeroLineYPos, 2);
    	
    	fontRendererObj.drawString("HP:", leftLabelXPos, firstLineYPos, 2);
    	fontRendererObj.drawString(player.CombatEntity.currentHp + " / " + player.CombatEntity.GetMaxHp(), leftValueXPos, firstLineYPos, 2);

    	fontRendererObj.drawString("MP:", leftLabelXPos, secondLineYPos, 2);
    	fontRendererObj.drawString(player.CombatEntity.currentMp + " / " + player.CombatEntity.GetMaxMp(), leftValueXPos, secondLineYPos, 2);

    	fontRendererObj.drawString("XP:", rightLabelXPos, firstLineYPos, 2);
    	fontRendererObj.drawString(xpData.CurrentXp + " / " + LevelingEngine.GetXpRequiredForLevel(xpData.Level), rightValueXPos, firstLineYPos, 2);

    	fontRendererObj.drawString("AP:", rightLabelXPos, secondLineYPos, 2);
    	fontRendererObj.drawString(xpData.CurrentAp + "", rightValueXPos, secondLineYPos, 2);

    	fontRendererObj.drawString("Att:", leftLabelXPos, thirdLineYPos, 2);
    	fontRendererObj.drawString(player.CombatEntity.GetAttack() + "", leftValueXPos, thirdLineYPos, 2);

    	fontRendererObj.drawString("Def:", leftLabelXPos, fourthLineYPos, 2);
    	fontRendererObj.drawString(player.CombatEntity.GetDefense() + "", leftValueXPos, fourthLineYPos, 2);

    	fontRendererObj.drawString("MAtt:", leftLabelXPos, fifthLineYPos, 2);
    	fontRendererObj.drawString(player.CombatEntity.GetMagic() + "", leftValueXPos, fifthLineYPos, 2);

    	fontRendererObj.drawString("MDef:", leftLabelXPos, sixthLineYPos, 2);
    	fontRendererObj.drawString(player.CombatEntity.GetMagicDefense() + "", leftValueXPos, sixthLineYPos, 2);

    	fontRendererObj.drawString("Spd:", leftLabelXPos, seventhLineYPos, 2);
    	fontRendererObj.drawString(player.CombatEntity.GetSpeed() + "", leftValueXPos, seventhLineYPos, 2);

    	fontRendererObj.drawString("Str:", rightLabelXPos, thirdLineYPos, 2);
    	fontRendererObj.drawString(0 + "", rightValueXPos, thirdLineYPos, 2);

    	fontRendererObj.drawString("Dex:", rightLabelXPos, fourthLineYPos, 2);
    	fontRendererObj.drawString(0 + "", rightValueXPos, fourthLineYPos, 2);

    	fontRendererObj.drawString("Con:", rightLabelXPos, fifthLineYPos, 2);
    	fontRendererObj.drawString(0 + "", rightValueXPos, fifthLineYPos, 2);

    	fontRendererObj.drawString("Int:", rightLabelXPos, sixthLineYPos, 2);
    	fontRendererObj.drawString(0 + "", rightValueXPos, sixthLineYPos, 2);

    	fontRendererObj.drawString("Will:", rightLabelXPos, seventhLineYPos, 2);
    	fontRendererObj.drawString(0 + "", rightValueXPos, seventhLineYPos, 2);
	}
}
