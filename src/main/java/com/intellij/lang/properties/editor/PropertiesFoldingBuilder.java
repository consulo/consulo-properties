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
package com.intellij.lang.properties.editor;

import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.editor.folding.CustomFoldingBuilder;
import consulo.language.editor.folding.FoldingDescriptor;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Rustam Vishnyakov
 */
@ExtensionImpl
public class PropertiesFoldingBuilder extends CustomFoldingBuilder
{
	@RequiredReadAction
	@Override
	protected void buildLanguageFoldRegions(@Nonnull List<FoldingDescriptor> descriptors,
											@Nonnull PsiElement root,
											@Nonnull Document document,
											boolean quick)
	{
	}

	@Override
	protected String getLanguagePlaceholderText(@Nonnull ASTNode node, @Nonnull TextRange range)
	{
		return "";
	}

	@Override
	protected boolean isRegionCollapsedByDefault(@Nonnull ASTNode node)
	{
		return false;
	}

	@Override
	protected boolean isCustomFoldingRoot(ASTNode node)
	{
		return node.getPsi() instanceof PropertiesFile;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return PropertiesLanguage.INSTANCE;
	}
}
