/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
import com.intellij.lang.properties.psi.impl.PropertyImpl;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressManager;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.util.collection.SmartList;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author cdr
 */
@ExtensionImpl
public class TrailingSpacesInPropertyInspection extends PropertySuppressableInspectionBase
{
	@Nonnull
	public String getDisplayName()
	{
		return PropertiesBundle.message("trail.spaces.property.inspection.display.name");
	}

	@Nullable
	@Override
	public Language getLanguage()
	{
		return PropertiesLanguage.INSTANCE;
	}

	@Nonnull
	public String getShortName()
	{
		return "TrailingSpacesInProperty";
	}

	@Nonnull
	@Override
	public HighlightDisplayLevel getDefaultLevel()
	{
		return HighlightDisplayLevel.WARNING;
	}

	public ProblemDescriptor[] checkFile(@Nonnull PsiFile file, @Nonnull final InspectionManager manager, final boolean isOnTheFly)
	{
		if(!(file instanceof PropertiesFile))
		{
			return null;
		}
		final List<IProperty> properties = ((PropertiesFile) file).getProperties();
		final List<ProblemDescriptor> descriptors = new SmartList<ProblemDescriptor>();

		for(IProperty property : properties)
		{
			ProgressManager.checkCanceled();

			ASTNode keyNode = ((PropertyImpl) property).getKeyNode();
			if(keyNode != null)
			{
				PsiElement key = keyNode.getPsi();
				TextRange textRange = getTrailingSpaces(key);
				if(textRange != null)
				{
					descriptors.add(manager.createProblemDescriptor(key, textRange, "Trailing Spaces", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, RemoveTrailingSpacesFix.INSTANCE));
				}
			}
			ASTNode valueNode = ((PropertyImpl) property).getValueNode();
			if(valueNode != null)
			{
				PsiElement value = valueNode.getPsi();
				TextRange textRange = getTrailingSpaces(value);
				if(textRange != null)
				{
					descriptors.add(manager.createProblemDescriptor(value, textRange, "Trailing Spaces", ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, RemoveTrailingSpacesFix.INSTANCE));
				}
			}
		}
		return descriptors.toArray(new ProblemDescriptor[descriptors.size()]);
	}

	private static TextRange getTrailingSpaces(PsiElement element)
	{
		String key = element.getText();

		return PropertyImpl.trailingSpaces(key);
	}

	private static class RemoveTrailingSpacesFix implements LocalQuickFix
	{
		private static final RemoveTrailingSpacesFix INSTANCE = new RemoveTrailingSpacesFix();

		@Nonnull
		public String getName()
		{
			return "Remove Trailing Spaces";
		}

		@Nonnull
		public String getFamilyName()
		{
			return getName();
		}

		public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor)
		{
			PsiElement element = descriptor.getPsiElement();
			PsiElement parent = element == null ? null : element.getParent();
			if(!(parent instanceof PropertyImpl))
			{
				return;
			}
			TextRange textRange = getTrailingSpaces(element);
			if(textRange != null)
			{
				Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
				TextRange docRange = textRange.shiftRight(element.getTextRange().getStartOffset());
				document.deleteString(docRange.getStartOffset(), docRange.getEndOffset());
			}
		}
	}
}
