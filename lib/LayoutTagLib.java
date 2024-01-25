package lib;

import groovy.lang.Closure;
import java.util.Map;
import org.kohsuke.stapler.jelly.groovy.TagFile;
import org.kohsuke.stapler.jelly.groovy.TagLibraryUri;
import org.kohsuke.stapler.jelly.groovy.TypedTagLibrary;

@TagLibraryUri("/lib/layout")
public interface LayoutTagLib extends TypedTagLibrary {
  void overflowButton(Map paramMap, Closure paramClosure);
  
  void overflowButton(Closure paramClosure);
  
  void overflowButton(Map paramMap);
  
  void overflowButton();
  
  void isAdminOrTest(Map paramMap, Closure paramClosure);
  
  void isAdminOrTest(Closure paramClosure);
  
  void isAdminOrTest(Map paramMap);
  
  void isAdminOrTest();
  
  @TagFile("side-panel")
  void side_panel(Map paramMap, Closure paramClosure);
  
  @TagFile("side-panel")
  void side_panel(Closure paramClosure);
  
  @TagFile("side-panel")
  void side_panel(Map paramMap);
  
  @TagFile("side-panel")
  void side_panel();
  
  void renderOnDemand(Map paramMap, Closure paramClosure);
  
  void renderOnDemand(Closure paramClosure);
  
  void renderOnDemand(Map paramMap);
  
  void renderOnDemand();
  
  void userExperimentalFlag(Map paramMap, Closure paramClosure);
  
  void userExperimentalFlag(Closure paramClosure);
  
  void userExperimentalFlag(Map paramMap);
  
  void userExperimentalFlag();
  
  void tasks(Map paramMap, Closure paramClosure);
  
  void tasks(Closure paramClosure);
  
  void tasks(Map paramMap);
  
  void tasks();
  
  void task(Map paramMap, Closure paramClosure);
  
  void task(Closure paramClosure);
  
  void task(Map paramMap);
  
  void task();
  
  void rowSelectionController(Map paramMap, Closure paramClosure);
  
  void rowSelectionController(Closure paramClosure);
  
  void rowSelectionController(Map paramMap);
  
  void rowSelectionController();
  
  void breadcrumb(Map paramMap, Closure paramClosure);
  
  void breadcrumb(Closure paramClosure);
  
  void breadcrumb(Map paramMap);
  
  void breadcrumb();
  
  void notice(Map paramMap, Closure paramClosure);
  
  void notice(Closure paramClosure);
  
  void notice(Map paramMap);
  
  void notice();
  
  void progressAnimation(Map paramMap, Closure paramClosure);
  
  void progressAnimation(Closure paramClosure);
  
  void progressAnimation(Map paramMap);
  
  void progressAnimation();
  
  void stopButton(Map paramMap, Closure paramClosure);
  
  void stopButton(Closure paramClosure);
  
  void stopButton(Map paramMap);
  
  void stopButton();
  
  void layout(Map paramMap, Closure paramClosure);
  
  void layout(Closure paramClosure);
  
  void layout(Map paramMap);
  
  void layout();
  
  void header(Map paramMap, Closure paramClosure);
  
  void header(Closure paramClosure);
  
  void header(Map paramMap);
  
  void header();
  
  @TagFile("search-bar")
  void search_bar(Map paramMap, Closure paramClosure);
  
  @TagFile("search-bar")
  void search_bar(Closure paramClosure);
  
  @TagFile("search-bar")
  void search_bar(Map paramMap);
  
  @TagFile("search-bar")
  void search_bar();
  
  void tabNewItem(Map paramMap, Closure paramClosure);
  
  void tabNewItem(Closure paramClosure);
  
  void tabNewItem(Map paramMap);
  
  void tabNewItem();
  
  void copyButton(Map paramMap, Closure paramClosure);
  
  void copyButton(Closure paramClosure);
  
  void copyButton(Map paramMap);
  
  void copyButton();
  
  void spinner(Map paramMap, Closure paramClosure);
  
  void spinner(Closure paramClosure);
  
  void spinner(Map paramMap);
  
  void spinner();
  
  void yui(Map paramMap, Closure paramClosure);
  
  void yui(Closure paramClosure);
  
  void yui(Map paramMap);
  
  void yui();
  
  void ajax(Map paramMap, Closure paramClosure);
  
  void ajax(Closure paramClosure);
  
  void ajax(Map paramMap);
  
  void ajax();
  
  void progressiveRendering(Map paramMap, Closure paramClosure);
  
  void progressiveRendering(Closure paramClosure);
  
  void progressiveRendering(Map paramMap);
  
  void progressiveRendering();
  
  @TagFile("app-bar")
  void app_bar(Map paramMap, Closure paramClosure);
  
  @TagFile("app-bar")
  void app_bar(Closure paramClosure);
  
  @TagFile("app-bar")
  void app_bar(Map paramMap);
  
  @TagFile("app-bar")
  void app_bar();
  
  void hasPermission(Map paramMap, Closure paramClosure);
  
  void hasPermission(Closure paramClosure);
  
  void hasPermission(Map paramMap);
  
  void hasPermission();
  
  void delete(Map paramMap, Closure paramClosure);
  
  void delete(Closure paramClosure);
  
  void delete(Map paramMap);
  
  void delete();
  
  void pane(Map paramMap, Closure paramClosure);
  
  void pane(Closure paramClosure);
  
  void pane(Map paramMap);
  
  void pane();
  
  void rightspace(Map paramMap, Closure paramClosure);
  
  void rightspace(Closure paramClosure);
  
  void rightspace(Map paramMap);
  
  void rightspace();
  
  void view(Map paramMap, Closure paramClosure);
  
  void view(Closure paramClosure);
  
  void view(Map paramMap);
  
  void view();
  
  void helpIcon(Map paramMap, Closure paramClosure);
  
  void helpIcon(Closure paramClosure);
  
  void helpIcon(Map paramMap);
  
  void helpIcon();
  
  @TagFile("main-panel")
  void main_panel(Map paramMap, Closure paramClosure);
  
  @TagFile("main-panel")
  void main_panel(Closure paramClosure);
  
  @TagFile("main-panel")
  void main_panel(Map paramMap);
  
  @TagFile("main-panel")
  void main_panel();
  
  void breakable(Map paramMap, Closure paramClosure);
  
  void breakable(Closure paramClosure);
  
  void breakable(Map paramMap);
  
  void breakable();
  
  void hasAdministerOrManage(Map paramMap, Closure paramClosure);
  
  void hasAdministerOrManage(Closure paramClosure);
  
  void hasAdministerOrManage(Map paramMap);
  
  void hasAdministerOrManage();
  
  void tabBar(Map paramMap, Closure paramClosure);
  
  void tabBar(Closure paramClosure);
  
  void tabBar(Map paramMap);
  
  void tabBar();
  
  void tab(Map paramMap, Closure paramClosure);
  
  void tab(Closure paramClosure);
  
  void tab(Map paramMap);
  
  void tab();
  
  void pageHeader(Map paramMap, Closure paramClosure);
  
  void pageHeader(Closure paramClosure);
  
  void pageHeader(Map paramMap);
  
  void pageHeader();
  
  void icon(Map paramMap, Closure paramClosure);
  
  void icon(Closure paramClosure);
  
  void icon(Map paramMap);
  
  void icon();
  
  void confirmationLink(Map paramMap, Closure paramClosure);
  
  void confirmationLink(Closure paramClosure);
  
  void confirmationLink(Map paramMap);
  
  void confirmationLink();
  
  void svgIcon(Map paramMap, Closure paramClosure);
  
  void svgIcon(Closure paramClosure);
  
  void svgIcon(Map paramMap);
  
  void svgIcon();
  
  void tabPane(Map paramMap, Closure paramClosure);
  
  void tabPane(Closure paramClosure);
  
  void tabPane(Map paramMap);
  
  void tabPane();
  
  void breadcrumbBar(Map paramMap, Closure paramClosure);
  
  void breadcrumbBar(Closure paramClosure);
  
  void breadcrumbBar(Map paramMap);
  
  void breadcrumbBar();
  
  void isAdmin(Map paramMap, Closure paramClosure);
  
  void isAdmin(Closure paramClosure);
  
  void isAdmin(Map paramMap);
  
  void isAdmin();
}
