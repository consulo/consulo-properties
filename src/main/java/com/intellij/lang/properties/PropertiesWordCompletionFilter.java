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

import com.intellij.lang.properties.parsing.PropertiesStubElementTypes;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.editor.completion.WordCompletionElementFilter;
import consulo.language.version.LanguageVersion;

import javax.annotation.Nonnull;

@ExtensionImpl
public class PropertiesWordCompletionFilter implements WordCompletionElementFilter
{
	public boolean isWordCompletionEnabledIn(final IElementType element, LanguageVersion languageVersion)
	{
		return WordCompletionElementFilter.super.isWordCompletionEnabledIn(element, languageVersion) || element == PropertiesStubElementTypes.PROPERTY;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return PropertiesLanguage.INSTANCE;
	}
}