
package ascelion.rest.bridge.client;

public class RestClientException extends RuntimeException
{

	RestClientException( String message )
	{
		super( message );
	}

	RestClientException( String message, Throwable cause )
	{
		super( message, cause );
	}
}
