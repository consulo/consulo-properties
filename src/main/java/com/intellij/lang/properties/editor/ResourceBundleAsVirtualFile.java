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

import com.intellij.lang.properties.ResourceBundle;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileSystem;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceBundleAsVirtualFile extends VirtualFile {
  private final ResourceBundle myResourceBundle;

  public ResourceBundleAsVirtualFile(ResourceBundle resourceBundle) {
    myResourceBundle = resourceBundle;
  }

  public ResourceBundle getResourceBundle() {
    return myResourceBundle;
  }

  @Nonnull
  public VirtualFileSystem getFileSystem() {
    return LocalFileSystem.getInstance();
  }

  public String getPath() {
    return getName();
  }

  @Nonnull
  public String getName() {
    return myResourceBundle.getBaseName();
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final ResourceBundleAsVirtualFile resourceBundleAsVirtualFile = (ResourceBundleAsVirtualFile)o;

    if (!myResourceBundle.equals(resourceBundleAsVirtualFile.myResourceBundle)) return false;

    return true;
  }

  public int hashCode() {
    return myResourceBundle.hashCode();
  }

  public void rename(Object requestor, @Nonnull String newName) throws IOException {
  }

  public boolean isWritable() {
    return true;
  }

  public boolean isDirectory() {
    return false;
  }

  public boolean isValid() {
    return true;
  }

  public VirtualFile getParent() {
    return myResourceBundle.getBaseDirectory();
  }

  public VirtualFile[] getChildren() {
    return EMPTY_ARRAY;
  }

  public VirtualFile createChildDirectory(Object requestor, String name) throws IOException {
    throw new UnsupportedOperationException();
  }

  public VirtualFile createChildData(Object requestor, @Nonnull String name) throws IOException {
    throw new UnsupportedOperationException();
  }

  public void delete(Object requestor) throws IOException {
    //todo
  }

  public void move(Object requestor, @Nonnull VirtualFile newParent) throws IOException {
    //todo
  }

  public InputStream getInputStream() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  public byte[] contentsToByteArray() throws IOException {
    throw new UnsupportedOperationException();
  }

  public long getModificationStamp() {
    return 0;
  }

  public long getTimeStamp() {
    return 0;
  }

  public long getLength() {
    return 0;
  }

  public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {

  }
}
