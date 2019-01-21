
package ascelion.rest.micro.cdi;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@RequiredArgsConstructor
final class RBBCreation
{

	private final BeanManager bm;
	private CreationalContext<?> cc;

	RestClientBuilder create()
	{
		final Set<Bean<?>> beans = this.bm.getBeans( RestClientBuilder.class );
		final RestClientBuilder bld;

		if( beans.isEmpty() ) {
			bld = RestClientBuilder.newBuilder();
		}
		else if( beans.size() > 1 ) {
			// TODO better message
			throw new AmbiguousResolutionException( "RestClientBuilder is ambigous" );
		}
		else {
			final Bean<?> bean = beans.iterator().next();

			this.cc = this.bm.createCreationalContext( bean );
			bld = (RestClientBuilder) this.bm.getReference( bean, RestClientBuilder.class, this.cc );
		}

		return bld;
	}

	void release()
	{
		if( this.cc != null ) {
			this.cc.release();
		}
	}
}
