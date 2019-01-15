
package ascelion.rest.micro.tests;

import java.util.Collection;
import java.util.HashSet;

import static java.util.Arrays.asList;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public abstract class PackageArchiveProcessor implements ApplicationArchiveProcessor
{

	private final String name;
	private final Collection<String> packageNames = new HashSet<>();
	private final Collection<Package> packages = new HashSet<>();

	public PackageArchiveProcessor()
	{
		this.name = getClass().getSimpleName().replace( "ArchiveProcessor", "" ).toLowerCase();
	}

	public PackageArchiveProcessor( String name )
	{
		this.name = name;
	}

	public void addPackage( String... pkgNames )
	{
		this.packageNames.addAll( asList( pkgNames ) );
	}

	public void addPackage( Package... pkgs )
	{
		this.packages.addAll( asList( pkgs ) );
	}

	@Override
	public final void process( Archive<?> archive, TestClass testClass )
	{
		final JavaArchive packed = ShrinkWrap
			.create( JavaArchive.class, this.name + ".jar" )
			.addPackages( true, this.packageNames.toArray( new String[0] ) )
			.addPackages( true, this.packages.toArray( new Package[0] ) );

		archive.as( WebArchive.class ).addAsLibrary( packed );
	}

}
