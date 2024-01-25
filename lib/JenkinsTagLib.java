package lib;

import groovy.lang.Closure;
import java.util.Map;
import org.kohsuke.stapler.jelly.groovy.TagFile;
import org.kohsuke.stapler.jelly.groovy.TagLibraryUri;
import org.kohsuke.stapler.jelly.groovy.TypedTagLibrary;

@TagLibraryUri("/lib/hudson")
public interface JenkinsTagLib extends TypedTagLibrary {
  void node(Map paramMap, Closure paramClosure);
  
  void node(Closure paramClosure);
  
  void node(Map paramMap);
  
  void node();
  
  void logRecords(Map paramMap, Closure paramClosure);
  
  void logRecords(Closure paramClosure);
  
  void logRecords(Map paramMap);
  
  void logRecords();
  
  void buildCaption(Map paramMap, Closure paramClosure);
  
  void buildCaption(Closure paramClosure);
  
  void buildCaption(Map paramMap);
  
  void buildCaption();
  
  void editTypeIcon(Map paramMap, Closure paramClosure);
  
  void editTypeIcon(Closure paramClosure);
  
  void editTypeIcon(Map paramMap);
  
  void editTypeIcon();
  
  @TagFile("failed-test")
  void failed_test(Map paramMap, Closure paramClosure);
  
  @TagFile("failed-test")
  void failed_test(Closure paramClosure);
  
  @TagFile("failed-test")
  void failed_test(Map paramMap);
  
  @TagFile("failed-test")
  void failed_test();
  
  void editableDescription(Map paramMap, Closure paramClosure);
  
  void editableDescription(Closure paramClosure);
  
  void editableDescription(Map paramMap);
  
  void editableDescription();
  
  void buildProgressBar(Map paramMap, Closure paramClosure);
  
  void buildProgressBar(Closure paramClosure);
  
  void buildProgressBar(Map paramMap);
  
  void buildProgressBar();
  
  void listScmBrowsers(Map paramMap, Closure paramClosure);
  
  void listScmBrowsers(Closure paramClosure);
  
  void listScmBrowsers(Map paramMap);
  
  void listScmBrowsers();
  
  void thirdPartyLicenses(Map paramMap, Closure paramClosure);
  
  void thirdPartyLicenses(Closure paramClosure);
  
  void thirdPartyLicenses(Map paramMap);
  
  void thirdPartyLicenses();
  
  void projectView(Map paramMap, Closure paramClosure);
  
  void projectView(Closure paramClosure);
  
  void projectView(Map paramMap);
  
  void projectView();
  
  void buildLink(Map paramMap, Closure paramClosure);
  
  void buildLink(Closure paramClosure);
  
  void buildLink(Map paramMap);
  
  void buildLink();
  
  void abstractItemLink(Map paramMap, Closure paramClosure);
  
  void abstractItemLink(Closure paramClosure);
  
  void abstractItemLink(Map paramMap);
  
  void abstractItemLink();
  
  void help(Map paramMap, Closure paramClosure);
  
  void help(Closure paramClosure);
  
  void help(Map paramMap);
  
  void help();
  
  @TagFile("rssBar-with-iconSize")
  void rssBar_with_iconSize(Map paramMap, Closure paramClosure);
  
  @TagFile("rssBar-with-iconSize")
  void rssBar_with_iconSize(Closure paramClosure);
  
  @TagFile("rssBar-with-iconSize")
  void rssBar_with_iconSize(Map paramMap);
  
  @TagFile("rssBar-with-iconSize")
  void rssBar_with_iconSize();
  
  void progressiveText(Map paramMap, Closure paramClosure);
  
  void progressiveText(Closure paramClosure);
  
  void progressiveText(Map paramMap);
  
  void progressiveText();
  
  void propertyTable(Map paramMap, Closure paramClosure);
  
  void propertyTable(Closure paramClosure);
  
  void propertyTable(Map paramMap);
  
  void propertyTable();
  
  void buildEnvVar(Map paramMap, Closure paramClosure);
  
  void buildEnvVar(Closure paramClosure);
  
  void buildEnvVar(Map paramMap);
  
  void buildEnvVar();
  
  void buildHealth(Map paramMap, Closure paramClosure);
  
  void buildHealth(Closure paramClosure);
  
  void buildHealth(Map paramMap);
  
  void buildHealth();
  
  void buildStatusSummary(Map paramMap, Closure paramClosure);
  
  void buildStatusSummary(Closure paramClosure);
  
  void buildStatusSummary(Map paramMap);
  
  void buildStatusSummary();
  
  @TagFile("aggregated-failed-tests")
  void aggregated_failed_tests(Map paramMap, Closure paramClosure);
  
  @TagFile("aggregated-failed-tests")
  void aggregated_failed_tests(Closure paramClosure);
  
  @TagFile("aggregated-failed-tests")
  void aggregated_failed_tests(Map paramMap);
  
  @TagFile("aggregated-failed-tests")
  void aggregated_failed_tests();
  
  @TagFile("test-result")
  void test_result(Map paramMap, Closure paramClosure);
  
  @TagFile("test-result")
  void test_result(Closure paramClosure);
  
  @TagFile("test-result")
  void test_result(Map paramMap);
  
  @TagFile("test-result")
  void test_result();
  
  void jobLink(Map paramMap, Closure paramClosure);
  
  void jobLink(Closure paramClosure);
  
  void jobLink(Map paramMap);
  
  void jobLink();
  
  void buildRangeLink(Map paramMap, Closure paramClosure);
  
  void buildRangeLink(Closure paramClosure);
  
  void buildRangeLink(Map paramMap);
  
  void buildRangeLink();
  
  void scriptConsole(Map paramMap, Closure paramClosure);
  
  void scriptConsole(Closure paramClosure);
  
  void scriptConsole(Map paramMap);
  
  void scriptConsole();
  
  void ballColorTd(Map paramMap, Closure paramClosure);
  
  void ballColorTd(Closure paramClosure);
  
  void ballColorTd(Map paramMap);
  
  void ballColorTd();
  
  void buildListTable(Map paramMap, Closure paramClosure);
  
  void buildListTable(Closure paramClosure);
  
  void buildListTable(Map paramMap);
  
  void buildListTable();
  
  void rssBar(Map paramMap, Closure paramClosure);
  
  void rssBar(Closure paramClosure);
  
  void rssBar(Map paramMap);
  
  void rssBar();
  
  void projectViewRow(Map paramMap, Closure paramClosure);
  
  void projectViewRow(Closure paramClosure);
  
  void projectViewRow(Map paramMap);
  
  void projectViewRow();
  
  void progressBar(Map paramMap, Closure paramClosure);
  
  void progressBar(Closure paramClosure);
  
  void progressBar(Map paramMap);
  
  void progressBar();
  
  void setIconSize(Map paramMap, Closure paramClosure);
  
  void setIconSize(Closure paramClosure);
  
  void setIconSize(Map paramMap);
  
  void setIconSize();
  
  void queue(Map paramMap, Closure paramClosure);
  
  void queue(Closure paramClosure);
  
  void queue(Map paramMap);
  
  void queue();
  
  void actions(Map paramMap, Closure paramClosure);
  
  void actions(Closure paramClosure);
  
  void actions(Map paramMap);
  
  void actions();
  
  void iconSize(Map paramMap, Closure paramClosure);
  
  void iconSize(Closure paramClosure);
  
  void iconSize(Map paramMap);
  
  void iconSize();
  
  void artifactList(Map paramMap, Closure paramClosure);
  
  void artifactList(Closure paramClosure);
  
  void artifactList(Map paramMap);
  
  void artifactList();
  
  void executors(Map paramMap, Closure paramClosure);
  
  void executors(Closure paramClosure);
  
  void executors(Map paramMap);
  
  void executors();
  
  void summary(Map paramMap, Closure paramClosure);
  
  void summary(Closure paramClosure);
  
  void summary(Map paramMap);
  
  void summary();
}
