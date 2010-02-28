/*
 * $Id: GameModule.java 5488 2009-04-11 14:01:20Z uckelman $
 *
 * Copyright (c) 2009 by Joel Uckelman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.tools.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A cover for the {@link ExecutorService} which submits to the Event
 * Dispatch Thread.
 *
 * @author Joel Uckelman
 * @since 3.1.11
 */
public class EDT {
  private static final EDTExecutorService INSTANCE = new EDTExecutorService();

  public static ExecutorService getInstance() {
    return INSTANCE;
  }

  public static <T> Future<T> submit(final Callable<T> task) {
    return INSTANCE.submit(task);
  }

  public static Future<?> submit(Runnable task) {
    return INSTANCE.submit(task);
  }

  public static <T> Future<T> submit(final Runnable task, T result) {
    return INSTANCE.submit(task, result);
  }

  public static <T> EDTRunnableFuture<T> submit(EDTRunnableFuture<T> task) {
    return INSTANCE.submit(task);
  }

  public static void execute(Runnable r) {
    INSTANCE.execute(r);
  }
}
