
package ascelion.rest.bridge.client;

public interface RequestInterceptor
{

	void before( RestRequestContext rc );

	default void after( RestRequestContext rc )
	{
	}
}
