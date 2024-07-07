/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.lang.properties;

import com.intellij.lang.properties.editor.PropertiesValueHighlighter;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.lang.properties.psi.impl.PropertyImpl;
import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.colorScheme.EditorColorsManager;
import consulo.colorScheme.TextAttributes;
import consulo.colorScheme.TextAttributesKey;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.annotation.Annotation;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.lexer.Lexer;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.util.lang.Pair;

import jakarta.annotation.Nonnull;
import java.util.Collection;

/**
 * @author cdr
 */
public class PropertiesAnnotator implements Annotator
{
	@Override
	@RequiredReadAction
	public void annotate(@Nonnull PsiElement element, @Nonnull AnnotationHolder holder)
	{
		if(!(element instanceof IProperty))
		{
			return;
		}
		final Property property = (Property) element;
		PropertiesFile propertiesFile = property.getPropertiesFile();
		Collection<IProperty> others = propertiesFile.findPropertiesByKey(property.getUnescapedKey());
		ASTNode keyNode = ((PropertyImpl) property).getKeyNode();
		if(others.size() != 1)
		{
			holder.newAnnotation(HighlightSeverity.ERROR, PropertiesBundle.message("duplicate.property.key.error.message"))
					.range(keyNode)
					.withFix(new RemovePropertyFix(property))
					.create();
		}

		highlightTokens(property, keyNode, holder, new PropertiesHighlighter());
		ASTNode valueNode = ((PropertyImpl) property).getValueNode();
		if(valueNode != null)
		{
			highlightTokens(property, valueNode, holder, new PropertiesValueHighlighter());
		}
	}

	private static void highlightTokens(final Property property, final ASTNode node, final AnnotationHolder holder, PropertiesHighlighter highlighter)
	{
		Lexer lexer = highlighter.getHighlightingLexer();
		final String s = node.getText();
		lexer.start(s);

		while(lexer.getTokenType() != null)
		{
			IElementType elementType = lexer.getTokenType();
			TextAttributesKey[] keys = highlighter.getTokenHighlights(elementType);
			for(TextAttributesKey key : keys)
			{
				Pair<String, HighlightSeverity> pair = PropertiesHighlighter.DISPLAY_NAMES.get(key);
				String displayName = pair.getFirst();
				HighlightSeverity severity = pair.getSecond();
				if(severity != null)
				{
					int start = lexer.getTokenStart() + node.getTextRange().getStartOffset();
					int end = lexer.getTokenEnd() + node.getTextRange().getStartOffset();
					TextRange textRange = new TextRange(start, end);
					final Annotation annotation;
					if(severity == HighlightSeverity.WARNING)
					{
						annotation = holder.createWarningAnnotation(textRange, displayName);
					}
					else if(severity == HighlightSeverity.ERROR)
					{
						annotation = holder.createErrorAnnotation(textRange, displayName);
					}
					else
					{
						annotation = holder.createInfoAnnotation(textRange, displayName);
					}
					TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(key);
					annotation.setEnforcedTextAttributes(attributes);
					if(key == PropertiesHighlighter.PROPERTIES_INVALID_STRING_ESCAPE)
					{
						annotation.registerFix(new IntentionAction()
						{
							@Nonnull
							public String getText()
							{
								return PropertiesBundle.message("unescape");
							}

							@Nonnull
							public String getFamilyName()
							{
								return getText();
							}

							public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file)
							{
								if(!property.isValid() || !property.getManager().isInProject(property))
								{
									return false;
								}

								String text = property.getPropertiesFile().getContainingFile().getText();
								int startOffset = annotation.getStartOffset();
								return text.length() > startOffset && text.charAt(startOffset) == '\\';
							}

							public void invoke(@Nonnull Project project, Editor editor, PsiFile file)
							{
								if(!FileModificationService.getInstance().prepareFileForWrite(file))
								{
									return;
								}
								int offset = annotation.getStartOffset();
								if(property.getPropertiesFile().getContainingFile().getText().charAt(offset) == '\\')
								{
									editor.getDocument().deleteString(offset, offset + 1);
								}
							}

							public boolean startInWriteAction()
							{
								return true;
							}
						});
					}
				}
			}
			lexer.advance();
		}
	}
}
