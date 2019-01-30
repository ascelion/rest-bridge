/*
 * Copyright 2019 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ascelion.rest.micro.tests;

import java.net.URI;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.ext.CustomClientHeadersFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

public class ClientHeadersFactoryTest extends Arquillian
{

	@Deployment
	public static Archive<?> createDeployment()
	{
		return ShrinkWrap.create( WebArchive.class, ClientHeadersFactoryTest.class.getSimpleName() + ".war" )
			.addClasses( ClientHeadersFactoryClient.class, ReturnWithAllClientHeadersFilter.class );
	}

	private static ClientHeadersFactoryClient client( Class<?>... providers )
	{
		try {
			final RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri( URI.create( "http://localhost:9080/notused" ) );
			for( final Class<?> provider : providers ) {
				builder.register( provider );
			}
			return builder.build( ClientHeadersFactoryClient.class );
		}
		catch( final Throwable t ) {
			t.printStackTrace();
			return null;
		}
	}

	@Test
	public void testClientHeadersFactoryInvoked()
	{
		CustomClientHeadersFactory.isIncomingHeadersMapNull = true;
		CustomClientHeadersFactory.isOutgoingHeadersMapNull = true;
		CustomClientHeadersFactory.passedInOutgoingHeaders.clear();

		final Map<String, String> headers = client( ReturnWithAllClientHeadersFilter.class ).delete( "argValue" );

		assertFalse( CustomClientHeadersFactory.isIncomingHeadersMapNull );
		assertFalse( CustomClientHeadersFactory.isOutgoingHeadersMapNull );
		assertEquals( CustomClientHeadersFactory.passedInOutgoingHeaders.getFirst( "IntfHeader" ), "intfValue" );
		assertEquals( CustomClientHeadersFactory.passedInOutgoingHeaders.getFirst( "MethodHeader" ), "methodValue" );
		assertEquals( CustomClientHeadersFactory.passedInOutgoingHeaders.getFirst( "ArgHeader" ), "argValue" );

		assertEquals( headers.get( "IntfHeader" ), "intfValueModified" );
		assertEquals( headers.get( "MethodHeader" ), "methodValueModified" );
		assertEquals( headers.get( "ArgHeader" ), "argValueModified" );
		assertEquals( headers.get( "FactoryHeader" ), "factoryValue" );
	}
}
