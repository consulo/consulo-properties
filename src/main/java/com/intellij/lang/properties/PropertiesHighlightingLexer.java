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

import com.intellij.lang.properties.parsing.PropertiesTokenTypes;
import com.intellij.lang.properties.parsing._PropertiesLexer;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.StringLiteralLexer;
import com.intellij.psi.tree.IElementType;

/**
 * @author cdr
 */
public class PropertiesHighlightingLexer extends LayeredLexer{
  public PropertiesHighlightingLexer() {
    super(new _PropertiesLexer());
    registerSelfStoppingLayer(new StringLiteralLexer(StringLiteralLexer.NO_QUOTE_CHAR, PropertiesTokenTypes.VALUE_CHARACTERS, true, "#!=:"),
                              new IElementType[]{PropertiesTokenTypes.VALUE_CHARACTERS},
                              IElementType.EMPTY_ARRAY);
    registerSelfStoppingLayer(new StringLiteralLexer(StringLiteralLexer.NO_QUOTE_CHAR, PropertiesTokenTypes.KEY_CHARACTERS, true, "#!=: "),
                              new IElementType[]{PropertiesTokenTypes.KEY_CHARACTERS},
                              IElementType.EMPTY_ARRAY);
  }
}
