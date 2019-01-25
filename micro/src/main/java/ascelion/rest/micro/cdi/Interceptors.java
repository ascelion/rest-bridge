
package ascelion.rest.micro.cdi;

import java.util.concurrent.Callable;

import javax.enterprise.inject.spi.BeanManager;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class Interceptors
{

	final BeanManager bm;
	final Class<?> type;

	Callable<?> wrap( Callable<?> a )
	{
		return a;
	}
}
