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

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ProcessingContext;

import javax.annotation.Nonnull;

/**
 * @author yole
 */
public class ResourceBundleReferenceProvider extends PsiReferenceProvider
{
	protected boolean mySoft;

	public ResourceBundleReferenceProvider()
	{
		this(false);
	}

	public ResourceBundleReferenceProvider(boolean soft)
	{
		mySoft = soft;
	}

	@Nonnull
	public PsiReference[] getReferencesByElement(@Nonnull PsiElement element, @Nonnull final ProcessingContext context)
	{
		ResourceBundleReference reference = new ResourceBundleReference(element, mySoft);
		return new PsiReference[]{reference};
	}

}
