
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class INTAnnotBase<A extends Annotation> extends INTBase
{

	final A annotation;
}
