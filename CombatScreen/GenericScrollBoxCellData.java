package TBC.CombatScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericScrollBoxCellData 
{
	public GenericScrollBoxCellData(String text, String additionalData, IGenericAction onClick)
	{
		this(text, additionalData, onClick, "");
	}
	
	public GenericScrollBoxCellData(String text, String additionalData, IGenericAction onClick, String hoverText)
	{
		this(text, additionalData, onClick, hoverText.isEmpty() ? new ArrayList<String>() : new ArrayList(Arrays.asList(hoverText)));
	}
	
	public GenericScrollBoxCellData(String text, String additionalData, IGenericAction onClick, List<String> hoverText)
	{
		this.Text = text;
		this.AdditionalData = additionalData;
		this.OnClick = onClick;
		this.HoverText = hoverText;
	}
	
	public String Text;
	public String AdditionalData;
	public IGenericAction OnClick;
	public List<String> HoverText;
}
