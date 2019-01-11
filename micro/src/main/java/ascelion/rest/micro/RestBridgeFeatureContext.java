
package ascelion.rest.micro;

import java.util.Map;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;

import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@RequiredArgsConstructor
final class RestBridgeFeatureContext implements FeatureContext
{

	private final RestClientBuilder bld;

	@Override
	public Configuration getConfiguration()
	{
		return this.bld.getConfiguration();
	}

	@Override
	public FeatureContext property( String name, Object value )
	{
		this.bld.property( name, value );

		return this;
	}

	@Override
	public FeatureContext register( Class<?> componentClass )
	{
		this.bld.register( componentClass );

		return this;
	}

	@Override
	public FeatureContext register( Class<?> componentClass, int priority )
	{
		this.bld.register( componentClass, priority );

		return this;
	}

	@Override
	public FeatureContext register( Class<?> componentClass, Class<?>... contracts )
	{
		this.bld.register( componentClass, contracts );

		return this;
	}

	@Override
	public FeatureContext register( Class<?> componentClass, Map<Class<?>, Integer> contracts )
	{
		this.bld.register( componentClass, contracts );

		return this;
	}

	@Override
	public FeatureContext register( Object component )
	{
		this.bld.register( component );

		return this;
	}

	@Override
	public FeatureContext register( Object component, int priority )
	{
		this.bld.register( component, priority );

		return this;
	}

	@Override
	public FeatureContext register( Object component, Class<?>... contracts )
	{
		this.bld.register( component, contracts );

		return this;
	}

	@Override
	public FeatureContext register( Object component, Map<Class<?>, Integer> contracts )
	{
		this.bld.register( component, contracts );

		return this;
	}

}
