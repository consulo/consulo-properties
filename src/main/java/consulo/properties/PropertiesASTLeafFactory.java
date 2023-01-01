/*
 * Copyright 2013-2017 consulo.io
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

package consulo.properties;

import com.intellij.lang.properties.parsing.PropertiesTokenTypes;
import com.intellij.lang.properties.psi.impl.PropertyValueImpl;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.ast.IElementType;
import consulo.language.impl.ast.ASTLeafFactory;
import consulo.language.impl.ast.LeafElement;
import consulo.language.impl.psi.PsiCommentImpl;
import consulo.language.version.LanguageVersion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * @author cdr
 */
@ExtensionImpl
public class PropertiesASTLeafFactory implements ASTLeafFactory
{
  @Override
  @Nonnull
  public LeafElement createLeaf(@Nonnull final IElementType type, @Nonnull LanguageVersion languageVersion, @Nonnull CharSequence text) {
    if (type == PropertiesTokenTypes.VALUE_CHARACTERS) {
      return new PropertyValueImpl(type, text);
    }

    if (type == PropertiesTokenTypes.END_OF_LINE_COMMENT) {
      return new PsiCommentImpl(type, text);
    }

    return null;
  }

  @Override
  public boolean test(@Nullable IElementType input) {
    return input == PropertiesTokenTypes.VALUE_CHARACTERS || input == PropertiesTokenTypes.END_OF_LINE_COMMENT;
  }
}
