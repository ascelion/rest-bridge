
package org.eclipse.microprofile.rest.client.spi;

import ascelion.rest.bridge.tests.api.util.RestClientTrace;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

public class ClientTraceBuilderListener implements RestClientBuilderListener
{

	@Override
	public void onNewBuilder( RestClientBuilder b )
	{
		b.register( new RestClientTrace(), Integer.MIN_VALUE );
	}

}
