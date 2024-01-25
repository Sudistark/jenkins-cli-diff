package hudson.model;

import java.io.IOException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public interface ModifiableItemGroup<T extends Item> extends ItemGroup<T> {
  T doCreateItem(StaplerRequest paramStaplerRequest, StaplerResponse paramStaplerResponse) throws IOException, ServletException;
}
