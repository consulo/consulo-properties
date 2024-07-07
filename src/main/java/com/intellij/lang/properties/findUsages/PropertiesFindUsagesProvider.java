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
package com.intellij.lang.properties.findUsages;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.lang.properties.parsing.PropertiesWordsScanner;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.LangBundle;
import consulo.language.Language;
import consulo.language.cacheBuilder.WordsScanner;
import consulo.language.findUsage.FindUsagesProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;

import jakarta.annotation.Nonnull;

/**
 * @author cdr
 */
@ExtensionImpl
public class PropertiesFindUsagesProvider implements FindUsagesProvider
{
	public boolean canFindUsagesFor(@Nonnull PsiElement psiElement)
	{
		return psiElement instanceof PsiNamedElement;
	}

	@Nonnull
	public String getType(@Nonnull PsiElement element)
	{
		if(element instanceof IProperty)
		{
			return LangBundle.message("terms.property");
		}
		return "";
	}

	@Nonnull
	public String getDescriptiveName(@Nonnull PsiElement element)
	{
		return ((PsiNamedElement) element).getName();
	}

	@Nonnull
	public String getNodeText(@Nonnull PsiElement element, boolean useFullName)
	{
		return getDescriptiveName(element);
	}

	public WordsScanner getWordsScanner()
	{
		return new PropertiesWordsScanner();
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return PropertiesLanguage.INSTANCE;
	}
}
