package hudson.model;

import java.util.List;

public interface PermalinkProjectAction extends Action {
  List<Permalink> getPermalinks();
}
