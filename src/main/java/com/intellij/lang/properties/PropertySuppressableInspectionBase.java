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
import com.intellij.lang.properties.psi.PropertiesList;
import com.intellij.lang.properties.psi.Property;
import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.inspection.CustomSuppressableInspectionTool;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.intention.SuppressIntentionAction;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.project.Project;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

/**
 * User: cdr
 */
public abstract class PropertySuppressableInspectionBase extends LocalInspectionTool implements CustomSuppressableInspectionTool
{
	private static final Logger LOG = Logger.getInstance(PropertySuppressableInspectionBase.class);

	@Nonnull
	public String getGroupDisplayName()
	{
		return PropertiesBundle.message("properties.files.inspection.group.display.name");
	}

	@Nonnull
	@Override
	public String[] getGroupPath()
	{
		return new String[]{getGroupDisplayName()};
	}

	public SuppressIntentionAction[] getSuppressActions(final PsiElement element)
	{
		return new SuppressIntentionAction[]{
				new SuppressSinglePropertyFix(getShortName()),
				new SuppressForFile(getShortName())
		};
	}

	public boolean isSuppressedFor(@Nonnull PsiElement element)
	{
		Property property = PsiTreeUtil.getParentOfType(element, Property.class, false);
		PropertiesFile file;
		if(property == null)
		{
			PsiFile containingFile = element.getContainingFile();
			if(containingFile instanceof PropertiesFile)
			{
				file = (PropertiesFile) containingFile;
			}
			else
			{
				return false;
			}
		}
		else
		{
			PsiElement prev = property.getPrevSibling();
			while(prev instanceof PsiWhiteSpace || prev instanceof PsiComment)
			{
				if(prev instanceof PsiComment)
				{
					@NonNls String text = prev.getText();
					if(text.contains("suppress") && text.contains("\"" + getShortName() + "\""))
					{
						return true;
					}
				}
				prev = prev.getPrevSibling();
			}
			file = property.getPropertiesFile();
		}
		PsiElement leaf = file.getContainingFile().findElementAt(0);
		while(leaf instanceof PsiWhiteSpace)
		{
			leaf = leaf.getNextSibling();
		}

		while(leaf instanceof PsiComment)
		{
			@NonNls String text = leaf.getText();
			if(text.contains("suppress") && text.contains("\"" + getShortName() + "\"") && text.contains("file"))
			{
				return true;
			}
			leaf = leaf.getNextSibling();
			if(leaf instanceof PsiWhiteSpace)
			{
				leaf = leaf.getNextSibling();
			}
			// comment before first property get bound to the file, not property
			if(leaf instanceof PropertiesList && leaf.getFirstChild() == property && text.contains("suppress") && text.contains("\"" + getShortName() + "\""))
			{
				return true;
			}
		}

		return false;
	}

	private static class SuppressSinglePropertyFix extends SuppressIntentionAction
	{
		private final String shortName;

		public SuppressSinglePropertyFix(String shortName)
		{
			this.shortName = shortName;
		}

		@Nonnull
		public String getText()
		{
			return PropertiesBundle.message("unused.property.suppress.for.property");
		}

		@Nonnull
		public String getFamilyName()
		{
			return PropertiesBundle.message("unused.property.suppress.for.property");
		}

		public boolean isAvailable(@Nonnull final Project project, final Editor editor, @Nonnull final PsiElement element)
		{
			final Property property = PsiTreeUtil.getParentOfType(element, Property.class);
			return property != null && property.isValid();
		}

		public void invoke(@Nonnull final Project project, final Editor editor, @Nonnull final PsiElement element) throws IncorrectOperationException
		{
			final PsiFile file = element.getContainingFile();
			if(!FileModificationService.getInstance().prepareFileForWrite(file))
			{
				return;
			}

			final Property property = PsiTreeUtil.getParentOfType(element, Property.class);
			LOG.assertTrue(property != null);
			final int start = property.getTextRange().getStartOffset();

			@NonNls final Document doc = PsiDocumentManager.getInstance(project).getDocument(file);
			LOG.assertTrue(doc != null);
			final int line = doc.getLineNumber(start);
			final int lineStart = doc.getLineStartOffset(line);

			doc.insertString(lineStart, "# suppress inspection \"" + shortName +
					"\"\n");
		}
	}

	private static class SuppressForFile extends SuppressIntentionAction
	{
		private final String shortName;

		public SuppressForFile(String shortName)
		{
			this.shortName = shortName;
		}

		@Nonnull
		public String getText()
		{
			return PropertiesBundle.message("unused.property.suppress.for.file");
		}

		@Nonnull
		public String getFamilyName()
		{
			return PropertiesBundle.message("unused.property.suppress.for.file");
		}

		public boolean isAvailable(@Nonnull final Project project, final Editor editor, @Nonnull final PsiElement element)
		{
			return element.isValid() && element.getContainingFile() instanceof PropertiesFile;
		}

		public void invoke(@Nonnull final Project project, final Editor editor, @Nonnull final PsiElement element) throws IncorrectOperationException
		{
			final PsiFile file = element.getContainingFile();
			if(!FileModificationService.getInstance().prepareFileForWrite(file))
			{
				return;
			}

			@NonNls final Document doc = PsiDocumentManager.getInstance(project).getDocument(file);
			LOG.assertTrue(doc != null, file);

			doc.insertString(0, "# suppress inspection \"" +
					shortName +
					"\" for whole file\n");
		}
	}
}
