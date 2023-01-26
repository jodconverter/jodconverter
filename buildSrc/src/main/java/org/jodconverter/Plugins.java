package org.jodconverter;

public class Plugins {
  // Latest version -> https://github.com/checkstyle/checkstyle/releases/
  // 9.3 is the latest supporting java8, so stay with it until we set the minimal version to 11
  // Stay aligned with codacy version (log in into codacy and go to the "code patterns" menu)
  public static final String checkstyleVersion = "9.3";
  // Latest version -> https://pmd.github.io
  // Stay aligned with codacy version (log in into codacy and go to the "code patterns" menu)
  public static final String pmdVersion = "6.51.0";

  // Latest version -> https://plugins.gradle.org/plugin/com.github.kt3k.coveralls
  public static final String coveralls =
      "gradle.plugin.org.kt3k.gradle.plugin:coveralls-gradle-plugin:2.12.0";
  // public static final String checkstyleVersion = "10.3.1";
  // Latest version -> https://plugins.gradle.org/plugin/com.netflix.nebula.integtest
  public static final String nebulaIntegTest =
      "com.netflix.nebula:nebula-project-plugin:10.1.2";
  // Latest version -> https://plugins.gradle.org/plugin/io.codearte.nexus-staging
  public static final String nexusStaging =
      "io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0";
  // Latest version -> https://plugins.gradle.org/plugin/com.diffplug.gradle.spotless
  public static final String spotless =
      "com.diffplug.spotless:spotless-plugin-gradle:6.13.0";
}
