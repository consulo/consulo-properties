package com.intellij.lang.properties;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09-Aug-22
 */
@ExtensionImpl
public class PropertiesAnnotatorFactory implements AnnotatorFactory
{
	@Nullable
	@Override
	public Annotator createAnnotator()
	{
		return new PropertiesAnnotator();
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return PropertiesLanguage.INSTANCE;
	}
}
