package TBC.CombatScreen;

public class GenericScrollBoxCellData 
{
	public GenericScrollBoxCellData(String text, String additionalData, IGenericAction onClick)
	{
		this(text, additionalData, onClick, "");
	}
	
	public GenericScrollBoxCellData(String text, String additionalData, IGenericAction onClick, String hoverText)
	{
		this.Text = text;
		this.AdditionalData = additionalData;
		this.OnClick = onClick;
		this.HoverText = hoverText;
	}
	
	public String Text;
	public String AdditionalData;
	public IGenericAction OnClick;
	public String HoverText;
}
