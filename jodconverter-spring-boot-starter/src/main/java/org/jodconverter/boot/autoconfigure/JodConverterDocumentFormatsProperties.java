/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jodconverter.boot.autoconfigure;

import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.jodconverter.core.document.DocumentFormatProperties;

/**
 * Configuration of the document formats shared by every converter of the starter, whether it uses a
 * local, external or remote office manager.
 *
 * @param registry Resource (classpath:, file:...) of the JSON registry of the document formats
 *     supported by the converters. Defaults to the registry shipped with JODConverter. A
 *     classpath:custom-document-formats.json resource, if present, is added to it.
 * @param options Custom load (open) and store (save) properties, by document format extension,
 *     applied on top of the registry. For example,
 *     jodconverter.document-formats.options.txt.load.FilterOptions=utf16.
 */
@ConfigurationProperties("jodconverter.document-formats")
public record JodConverterDocumentFormatsProperties(
    @Nullable String registry,
    @Nullable Map<@NonNull String, @NonNull DocumentFormatProperties> options) {}
