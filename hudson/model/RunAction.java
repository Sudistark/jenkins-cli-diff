package hudson.model;

@Deprecated
public interface RunAction extends Action {
  void onLoad();
  
  void onAttached(Run paramRun);
  
  void onBuildComplete();
}
