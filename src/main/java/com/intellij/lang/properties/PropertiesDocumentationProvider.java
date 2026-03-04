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

import consulo.annotation.access.RequiredReadAction;
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
import consulo.util.lang.xml.XmlStringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author max
 */
@ExtensionImpl
public class PropertiesDocumentationProvider extends AbstractDocumentationProvider implements LanguageDocumentationProvider {
	@Nullable
	@Override
	@RequiredReadAction
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (element instanceof IProperty property) {
			StringBuilder builder = new StringBuilder().append('"');
			renderPropertyValue(property, builder);
			return builder.append("\"").append(getLocationString(element)).toString();
        }
        return null;
    }

    @RequiredReadAction
	private static String getLocationString(PsiElement element) {
        PsiFile file = element.getContainingFile();
        return file != null ? " [" + file.getName() + "]" : "";
    }

    private static void renderPropertyValue(@Nonnull IProperty prop, @Nonnull StringBuilder builder) {
        String raw = prop.getValue();
        if (raw == null) {
            builder.append("<i>empty</i>");
        }
        else {
			XmlStringUtil.escapeText(raw, builder);
		}
    }

	@Override
	@RequiredReadAction
	public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (element instanceof IProperty property) {
            String text = property.getDocCommentText();

            StringBuilder info = new StringBuilder();
            if (text != null) {
                TextAttributes attributes =
                    EditorColorsManager.getInstance().getGlobalScheme().getAttributes(PropertiesHighlighter.PROPERTY_COMMENT).clone();
                ColorValue background = attributes.getBackgroundColor();
                if (background != null) {
                    info.append("<div bgcolor=#").append(ColorValueUtil.toHex(background)).append(">");
                }
                String doc = StringUtil.join(StringUtil.split(text, "\n"), "<br>");
                info.append("<font color=#").append(ColorValueUtil.toHex(attributes.getForegroundColor())).append(">")
					.append(doc)
					.append("</font>\n<br>");
                if (background != null) {
                    info.append("</div>");
                }
            }
            info.append("\n<b>").append(property.getName()).append("</b>=\"");
            renderPropertyValue(property, info);
			info.append("\"");
            info.append(getLocationString(element));
            return info.toString();
        }
        return null;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PropertiesLanguage.INSTANCE;
    }
}