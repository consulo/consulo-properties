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
package com.intellij.lang.properties.refactoring;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.lang.properties.ResourceBundle;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.refactoring.rename.RenamePsiElementProcessor;
import consulo.language.editor.refactoring.rename.UnresolvableCollisionUsageInfo;
import consulo.language.psi.PsiElement;
import consulo.usage.UsageInfo;
import consulo.util.lang.Comparing;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@ExtensionImpl
public class RenamePropertyProcessor extends RenamePsiElementProcessor
{
	public boolean canProcessElement(@Nonnull final PsiElement element)
	{
		return element instanceof IProperty;
	}

	public void prepareRenaming(final PsiElement element, final String newName,
								final Map<PsiElement, String> allRenames)
	{
		IProperty property = (IProperty) element;
		ResourceBundle resourceBundle = property.getPropertiesFile().getResourceBundle();
		List<IProperty> properties = PropertiesUtil.findAllProperties(element.getProject(), resourceBundle, property.getUnescapedKey());
		allRenames.clear();
		for(IProperty otherProperty : properties)
		{
			allRenames.put(otherProperty.getPsiElement(), newName);
		}
	}

	@Override
	public void findCollisions(PsiElement element,
							   final String newName,
							   Map<? extends PsiElement, String> allRenames,
							   List<UsageInfo> result)
	{
		for(IProperty property : ((PropertiesFile) element.getContainingFile()).getProperties())
		{
			if(Comparing.strEqual(newName, property.getKey()))
			{
				result.add(new UnresolvableCollisionUsageInfo(property.getPsiElement(), element)
				{
					@Override
					public String getDescription()
					{
						return "New property name \'" + newName + "\' hides existing property";
					}
				});
			}
		}
	}

	@Override
	public boolean isToSearchInComments(PsiElement element)
	{
		return PropertiesRefactoringSettings.getInstance().RENAME_SEARCH_IN_COMMENTS;
	}

	@Override
	public void setToSearchInComments(PsiElement element, boolean enabled)
	{
		PropertiesRefactoringSettings.getInstance().RENAME_SEARCH_IN_COMMENTS = enabled;
	}
}
