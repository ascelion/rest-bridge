package ascelion.rest.micro.tests;

import org.eclipse.microprofile.rest.client.tck.interfaces.Loggable;

public interface LogableITF
{

	@Loggable
	String call();
}

