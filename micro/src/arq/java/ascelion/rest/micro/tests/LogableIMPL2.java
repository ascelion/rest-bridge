
package ascelion.rest.micro.tests;

import org.eclipse.microprofile.rest.client.tck.interfaces.Loggable;

public class LogableIMPL2
{

	@Loggable
	public String call()
	{
		return "HELO";
	}
}
