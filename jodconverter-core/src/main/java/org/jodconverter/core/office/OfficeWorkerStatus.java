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

package org.jodconverter.core.office;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A snapshot of a worker of an {@link AbstractOfficeWorkerPool}.
 *
 * @param state The state of the worker.
 * @param tasksSinceStart The number of tasks the worker executed, successfully or not, since it was
 *     last made ready.
 * @param restarts The number of times the worker was restarted since the pool started.
 * @param startFailures The number of attempts to make the worker ready that failed in a row; 0 when
 *     the worker is ready.
 */
public record OfficeWorkerStatus(
    @NonNull OfficeWorkerState state, int tasksSinceStart, int restarts, int startFailures) {}
