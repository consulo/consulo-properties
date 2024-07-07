/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
package com.intellij.lang.properties.xml;

import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.pom.PomService;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import consulo.xml.lang.xml.XMLLanguage;
import consulo.xml.patterns.XmlPatterns;
import consulo.xml.psi.xml.XmlTag;
import jakarta.annotation.Nonnull;

/**
 * @author Dmitry Avdeev
 * Date: 9/15/11
 */
@ExtensionImpl
public class XmlPropertiesReferenceContributor extends PsiReferenceContributor
{
	@Override
	public void registerReferenceProviders(PsiReferenceRegistrar registrar)
	{
		registrar.registerReferenceProvider(XmlPatterns.xmlAttributeValue().withLocalName("key"),
				new PsiReferenceProvider()
				{
					@Nonnull
					@Override
					public PsiReference[] getReferencesByElement(@Nonnull PsiElement element, @Nonnull ProcessingContext context)
					{
						PropertiesFile propertiesFile = PropertiesUtil.getPropertiesFile(element.getContainingFile());
						if(propertiesFile == null)
						{
							return PsiReference.EMPTY_ARRAY;
						}
						XmlProperty property = new XmlProperty(PsiTreeUtil.getParentOfType(element, XmlTag.class), (XmlPropertiesFile) propertiesFile);
						return new PsiReference[]{new PsiReferenceBase.Immediate<PsiElement>(element, PomService.convertToPsi(property))};
					}
				});
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return XMLLanguage.INSTANCE;
	}
}
