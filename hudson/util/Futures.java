package hudson.util;

import hudson.remoting.Future;

public class Futures {
  public static <T> Future<T> precomputed(T value) { return new Object(value); }
}
