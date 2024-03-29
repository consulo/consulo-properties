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

/**
 * @author Alexey
 */
package com.intellij.lang.properties.editor;

import com.intellij.lang.properties.parsing.PropertiesTokenTypes;
import consulo.language.lexer.EmptyLexer;
import consulo.language.lexer.StringLiteralLexer;
import consulo.language.ast.IElementType;
import consulo.language.lexer.LayeredLexer;

public class PropertiesValueHighlightingLexer extends LayeredLexer
{
  public PropertiesValueHighlightingLexer() {
      super(new EmptyLexer(){
        public IElementType getTokenType() {
          return getTokenStart() < getTokenEnd() ? PropertiesTokenTypes.VALUE_CHARACTERS : null;
        }
      });

      registerSelfStoppingLayer(new StringLiteralLexer(StringLiteralLexer.NO_QUOTE_CHAR, PropertiesTokenTypes.VALUE_CHARACTERS, true, "#!=:"),
                                new IElementType[]{PropertiesTokenTypes.VALUE_CHARACTERS}, IElementType.EMPTY_ARRAY);
  }
}
