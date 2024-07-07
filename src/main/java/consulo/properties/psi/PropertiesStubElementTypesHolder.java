package consulo.properties.psi;

import com.intellij.lang.properties.parsing.PropertiesStubElementTypes;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.stub.ObjectStubSerializerProvider;
import consulo.language.psi.stub.StubElementTypeHolder;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author VISTALL
 * @since 12-Aug-22
 */
@ExtensionImpl
public class PropertiesStubElementTypesHolder extends StubElementTypeHolder<PropertiesStubElementTypes>
{
	@Nullable
	@Override
	public String getExternalIdPrefix()
	{
		return "properties.";
	}

	@Nonnull
	@Override
	public List<ObjectStubSerializerProvider> loadSerializers()
	{
		return allFromStaticFields(PropertiesStubElementTypes.class, Field::get);
	}
}
