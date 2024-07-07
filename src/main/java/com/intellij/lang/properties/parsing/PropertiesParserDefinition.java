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
package com.intellij.lang.properties.parsing;

import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.lang.properties.psi.impl.PropertiesFileImpl;
import com.intellij.lang.properties.psi.impl.PropertiesListImpl;
import com.intellij.lang.properties.psi.impl.PropertyImpl;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.stub.IStubFileElementType;
import consulo.language.version.LanguageVersion;
import consulo.logging.Logger;

import jakarta.annotation.Nonnull;

/**
 * @author max
 */
@ExtensionImpl
public class PropertiesParserDefinition implements ParserDefinition
{
	private static final IFileElementType FILE = new IStubFileElementType(PropertiesLanguage.INSTANCE);

	private static final Logger LOG = Logger.getInstance(PropertiesParserDefinition.class);

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return PropertiesLanguage.INSTANCE;
	}

	@Nonnull
	public Lexer createLexer(@Nonnull LanguageVersion languageVersion)
	{
		return new _PropertiesLexer();
	}

	@Nonnull
	public IFileElementType getFileNodeType()
	{
		return FILE;
	}

	@Nonnull
	public TokenSet getWhitespaceTokens(@Nonnull LanguageVersion languageVersion)
	{
		return PropertiesTokenTypes.WHITESPACES;
	}

	@Nonnull
	public TokenSet getCommentTokens(LanguageVersion languageVersion)
	{
		return PropertiesTokenTypes.COMMENTS;
	}

	@Nonnull
	public TokenSet getStringLiteralElements(LanguageVersion languageVersion)
	{
		return TokenSet.EMPTY;
	}

	@Nonnull
	public PsiParser createParser(@Nonnull LanguageVersion languageVersion)
	{
		return new PropertiesParser();
	}

	public PsiFile createFile(FileViewProvider viewProvider)
	{
		return new PropertiesFileImpl(viewProvider);
	}

	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right)
	{
		return SpaceRequirements.MAY;
	}

	@Nonnull
	public PsiElement createElement(ASTNode node)
	{
		final IElementType type = node.getElementType();
		if(type == PropertiesStubElementTypes.PROPERTY)
		{
			return new PropertyImpl(node);
		}
		else if(type == PropertiesStubElementTypes.PROPERTIES_LIST)
		{
			return new PropertiesListImpl(node);
		}

		LOG.error("Alien element type [" + type + "]. Can't create Property PsiElement for that.");

		return new ASTWrapperPsiElement(node);
	}
}
