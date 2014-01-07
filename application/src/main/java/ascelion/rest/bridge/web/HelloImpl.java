
package ascelion.rest.bridge.web;

public class HelloImpl
implements Hello
{

	@Override
	public String sayByParam( String username )
	{
		return say( username );
	}

	@Override
	public String sayByPath( String username )
	{
		return say( username );
	}

	private String say( String username )
	{
		return String.format( "Hello, %s!", username );
	}
}
