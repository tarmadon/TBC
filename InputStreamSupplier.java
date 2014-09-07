package TBC;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.InputSupplier;

public class InputStreamSupplier implements InputSupplier<InputStream> 
{
	private InputStream s;
	
	public InputStreamSupplier(InputStream s)
	{
		this.s = s;
	}
	
	public InputStream getInput() throws IOException 
	{
		return s;
	}

}
