
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;

import lombok.Getter;

public class RestClientMethodException extends RestClientException
{

	@Getter
	private final Method method;

	RestClientMethodException( String message, Method method )
	{
		super( message );

		this.method = method;
	}
}
