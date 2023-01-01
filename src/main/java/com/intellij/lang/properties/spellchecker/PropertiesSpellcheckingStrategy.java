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
package com.intellij.lang.properties.spellchecker;

import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.lang.properties.psi.impl.PropertyImpl;
import com.intellij.lang.properties.psi.impl.PropertyValueImpl;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.inspections.PropertiesSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.intellij.spellchecker.tokenizer.TokenizerBase;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;


@ExtensionImpl
public class PropertiesSpellcheckingStrategy extends SpellcheckingStrategy
{
	private Tokenizer<PropertyValueImpl> myPropertyValueTokenizer = TokenizerBase.create(PlainTextSplitter.getInstance());
	private Tokenizer<PropertyImpl> myPropertyTokenizer = new MyPropertyTokenizer();

	@RequiredReadAction
	@Nonnull
	@Override
	public Tokenizer getTokenizer(PsiElement element)
	{
		if(element instanceof PropertyValueImpl)
		{
			return myPropertyValueTokenizer;
		}
		if(element instanceof PropertyImpl)
		{
			return myPropertyTokenizer;
		}
		return super.getTokenizer(element);
	}

	private static class MyPropertyTokenizer extends Tokenizer<PropertyImpl>
	{
		public void tokenize(@Nonnull PropertyImpl element, TokenConsumer consumer)
		{
			String key = element.getKey();
			consumer.consumeToken(element, key, true, 0, TextRange.allOf(key), PropertiesSplitter.getInstance());
		}
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return PropertiesLanguage.INSTANCE;
	}
}
