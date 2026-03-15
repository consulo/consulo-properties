package com.intellij.lang.properties;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.highlight.SingleLazyInstanceSyntaxHighlighterFactory;
import consulo.language.editor.highlight.SyntaxHighlighter;

/**
 * @author VISTALL
 * @since 2019-02-25
 */
@ExtensionImpl
public class PropertiesSyntaxHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory
{
	@Override
	protected SyntaxHighlighter createHighlighter()
	{
		return new PropertiesHighlighter();
	}

	@Override
	public Language getLanguage()
	{
		return PropertiesLanguage.INSTANCE;
	}
}
