package com.intellij.lang.properties.psi.impl;

import jakarta.annotation.Nonnull;

import consulo.document.util.ProperTextRange;
import consulo.document.util.TextRange;
import consulo.language.psi.LiteralTextEscaper;

/**
 * @author gregsh
 */
public class PropertyImplEscaper extends LiteralTextEscaper<PropertyImpl>
{
  private int[] outSourceOffsets;

  public PropertyImplEscaper(PropertyImpl value) {
    super(value);
  }

  @Override
  public boolean decode(@Nonnull TextRange rangeInsideHost, @Nonnull StringBuilder outChars) {
    ProperTextRange.assertProperRange(rangeInsideHost);
    String subText = rangeInsideHost.substring(myHost.getText());
    outSourceOffsets = new int[subText.length() + 1];
    boolean b = PropertyImpl.parseCharacters(subText, outChars, outSourceOffsets);
    if (b) {
      for (int i=0; i<outChars.length(); i++) {
        if (outChars.charAt(i) != subText.charAt(outSourceOffsets[i])) {
          if (subText.charAt(outSourceOffsets[i]) != '\\') {
            throw new IllegalStateException();
          }
        }
      }
    }
    return b;
  }

  @Override
  public int getOffsetInHost(int offsetInDecoded, @Nonnull TextRange rangeInsideHost) {
    int result = offsetInDecoded < outSourceOffsets.length ? outSourceOffsets[offsetInDecoded] : -1;
    if (result == -1) return -1;
    return (result <= rangeInsideHost.getLength() ? result : rangeInsideHost.getLength()) + rangeInsideHost.getStartOffset();
  }

  @Override
  public boolean isOneLine() {
    return !myHost.getText().contains("\\");
  }
}
