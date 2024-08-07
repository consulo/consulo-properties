/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.*;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
@ExtensionImpl(id = "propertiesCompletion", order = "before javaClassReference")
public class PropertiesCompletionContributor extends CompletionContributor
{
	@RequiredReadAction
	@Override
	public void fillCompletionVariants(CompletionParameters parameters, final CompletionResultSet result)
	{
		if(parameters.isExtendedCompletion())
		{
			CompletionService.getCompletionService().getVariantsFromContributors(parameters.delegateToClassName(), null, result::passResult);
		}
	}

	@Override
	public void beforeCompletion(@Nonnull CompletionInitializationContext context)
	{
		if(context.getFile() instanceof PropertiesFile)
		{
			context.setDummyIdentifier(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED);
		}
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return PropertiesLanguage.INSTANCE;
	}
}
