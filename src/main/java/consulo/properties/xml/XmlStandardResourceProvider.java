package consulo.properties.xml;

import consulo.annotation.component.ExtensionImpl;
import consulo.xml.javaee.ResourceRegistrar;
import consulo.xml.javaee.StandardResourceProvider;

/**
 * @author VISTALL
 * @since 10-Aug-22
 */
@ExtensionImpl
public class XmlStandardResourceProvider implements StandardResourceProvider
{
	@Override
	public void registerResources(ResourceRegistrar resourceRegistrar)
	{
		resourceRegistrar.addStdResource("http://java.sun.com/dtd/properties.dtd", "schemas/properties.dtd", XmlStandardResourceProvider.class);
	}
}
