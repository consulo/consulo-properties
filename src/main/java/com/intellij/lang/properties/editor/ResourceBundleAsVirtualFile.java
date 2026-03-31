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
package com.intellij.lang.properties.editor;

import com.intellij.lang.properties.ResourceBundle;
import consulo.virtualFileSystem.BaseVirtualFile;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Alexey
 */
public class ResourceBundleAsVirtualFile extends BaseVirtualFile {
    private final ResourceBundle myResourceBundle;

    public ResourceBundleAsVirtualFile(ResourceBundle resourceBundle) {
        myResourceBundle = resourceBundle;
    }

    public ResourceBundle getResourceBundle() {
        return myResourceBundle;
    }

    @Override
    public VirtualFileSystem getFileSystem() {
        return LocalFileSystem.getInstance();
    }

    @Override
    public String getPath() {
        return getName();
    }

    @Override
    public String getName() {
        return myResourceBundle.getBaseName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResourceBundleAsVirtualFile that = (ResourceBundleAsVirtualFile) o;

        return myResourceBundle.equals(that.myResourceBundle);
    }

    @Override
    public int hashCode() {
        return myResourceBundle.hashCode();
    }

    @Override
    public void rename(Object requestor, String newName) throws IOException {
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public VirtualFile getParent() {
        return myResourceBundle.getBaseDirectory();
    }

    @Override
    public VirtualFile[] getChildren() {
        return EMPTY_ARRAY;
    }

    @Override
    public VirtualFile createChildDirectory(Object requestor, String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public VirtualFile createChildData(Object requestor, String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Object requestor) throws IOException {
        //todo
    }

    @Override
    public void move(Object requestor, VirtualFile newParent) throws IOException {
        //todo
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] contentsToByteArray() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getModificationStamp() {
        return 0;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
    }
}
