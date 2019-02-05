
package ascelion.rest.bridge.spring;

import ascelion.rest.bridge.client.RestClient;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

public class RestClientFactory<T> implements FactoryBean<T>
{

	private final Class<T> type;
	private final Qualifier qual;

	@Autowired
	private ApplicationContext acx;

	public RestClientFactory( Class<T> type, Qualifier qual )
	{
		this.type = type;
		this.qual = qual;
	}

	@Override
	public T getObject() throws Exception
	{
		return newInterface();
	}

	@Override
	public Class<?> getObjectType()
	{
		return this.type;
	}

	@Override
	public boolean isSingleton()
	{
		return true;
	}

	private T newInterface()
	{
		final RestClient client = this.qual != null
			? this.acx.getBean( this.qual.value(), RestClient.class )
			: this.acx.getBean( RestClient.class );

		return client.getInterface( this.type );
	}
}
