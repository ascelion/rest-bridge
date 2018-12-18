
package ascelion.rest.micro.cdi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import static java.lang.String.format;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

public class RestBridgeExtension implements Extension
{

	static final Logger L = Logger.getLogger( RestBridgeExtension.class.getName() );

	private final Set<Class<?>> clients = new HashSet<>();
	private final Collection<Throwable> errors = new ArrayList<>();

	public void findClients( @Observes @WithAnnotations( RegisterRestClient.class ) ProcessAnnotatedType<?> pat )
	{
		final Class<?> type = pat.getAnnotatedType().getJavaClass();

		if( type.isInterface() ) {
			this.clients.add( type );

			pat.veto();
		}
		else {
			this.errors.add( new IllegalArgumentException( format( "The class %s is not an interface", type.getName() ) ) );
		}
	}

	void afterBeanDescovery( @Observes AfterBeanDiscovery event, BeanManager bm )
	{
		for( final Class<?> clientType : this.clients ) {
			event.addBean( createBean( bm, clientType ) );
		}
	}

	public void afterDeploymentValidation( @Observes AfterDeploymentValidation adv )
	{
		this.errors.forEach( adv::addDeploymentProblem );
	}

	private <T> Bean<T> createBean( BeanManager bm, Class<T> type )
	{
		return new RestBridgeBean<>( bm, type );
	}
}
