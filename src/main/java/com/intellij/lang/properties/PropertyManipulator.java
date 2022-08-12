package com.intellij.lang.properties;

import com.intellij.lang.properties.psi.impl.PropertyImpl;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.psi.AbstractElementManipulator;
import consulo.language.util.IncorrectOperationException;

import javax.annotation.Nonnull;

/**
 * @author gregsh
 */
@ExtensionImpl
public class PropertyManipulator extends AbstractElementManipulator<PropertyImpl>
{
	@Override
	public PropertyImpl handleContentChange(PropertyImpl element, TextRange range, String newContent) throws IncorrectOperationException
	{
		TextRange valueRange = getRangeInElement(element);
		final String oldText = element.getText();
		String newText = oldText.substring(0, range.getStartOffset()) + newContent + oldText.substring(range.getEndOffset());
		element.setValue(newText.substring(valueRange.getStartOffset()).replaceAll("([^\\s])\n", "$1 \n"));  // add explicit space before \n
		return element;
	}

	@Override
	public TextRange getRangeInElement(PropertyImpl element)
	{
		ASTNode valueNode = element.getValueNode();
		if(valueNode == null)
		{
			return TextRange.from(element.getTextLength(), 0);
		}
		TextRange range = valueNode.getTextRange();
		return TextRange.from(range.getStartOffset() - element.getTextRange().getStartOffset(), range.getLength());
	}

	@Nonnull
	@Override
	public Class<PropertyImpl> getElementClass()
	{
		return PropertyImpl.class;
	}
}
