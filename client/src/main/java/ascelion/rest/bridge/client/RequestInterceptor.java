
package ascelion.rest.bridge.client;

public interface RequestInterceptor
{

	RestRequestContext before( RestRequestContext rc );

	default void after( RestRequestContext rc )
	{
	}
}
