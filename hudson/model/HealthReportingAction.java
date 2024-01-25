package hudson.model;

public interface HealthReportingAction extends Action {
  HealthReport getBuildHealth();
}
