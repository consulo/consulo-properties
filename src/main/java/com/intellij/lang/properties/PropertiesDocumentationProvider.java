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

/*
 * @author max
 */
package com.intellij.lang.properties;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.EditorColorsManager;
import consulo.colorScheme.TextAttributes;
import consulo.language.Language;
import consulo.language.editor.documentation.AbstractDocumentationProvider;
import consulo.language.editor.documentation.LanguageDocumentationProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.ui.color.ColorValue;
import consulo.ui.util.ColorValueUtil;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class PropertiesDocumentationProvider extends AbstractDocumentationProvider implements LanguageDocumentationProvider
{
	@Nullable
	public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement)
	{
		if(element instanceof IProperty)
		{
			return "\"" + renderPropertyValue((IProperty) element) + "\"" + getLocationString(element);
		}
		return null;
	}

	private static String getLocationString(PsiElement element)
	{
		PsiFile file = element.getContainingFile();
		return file != null ? " [" + file.getName() + "]" : "";
	}

	@Nonnull
	private static String renderPropertyValue(IProperty prop)
	{
		String raw = prop.getValue();
		if(raw == null)
		{
			return "<i>empty</i>";
		}
		return StringUtil.escapeXml(raw);
	}

	public String generateDoc(final PsiElement element, @Nullable final PsiElement originalElement)
	{
		if(element instanceof IProperty)
		{
			IProperty property = (IProperty) element;
			String text = property.getDocCommentText();

			@NonNls String info = "";
			if(text != null)
			{
				TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(PropertiesHighlighter.PROPERTY_COMMENT).clone();
				ColorValue background = attributes.getBackgroundColor();
				if(background != null)
				{
					info += "<div bgcolor=#" + ColorValueUtil.toHex(background) + ">";
				}
				String doc = StringUtil.join(StringUtil.split(text, "\n"), "<br>");
				info += "<font color=#" + ColorValueUtil.toHex(attributes.getForegroundColor()) + ">" + doc + "</font>\n<br>";
				if(background != null)
				{
					info += "</div>";
				}
			}
			info += "\n<b>" + property.getName() + "</b>=\"" + renderPropertyValue(((IProperty) element)) + "\"";
			info += getLocationString(element);
			return info;
		}
		return null;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return PropertiesLanguage.INSTANCE;
	}
}