package hudson.fsp;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String WorkspaceSnapshotSCM_NoBuild(Object arg0, Object arg1) { return holder.format("WorkspaceSnapshotSCM.NoBuild", new Object[] { arg0, arg1 }); }
  
  public static Localizable _WorkspaceSnapshotSCM_NoBuild(Object arg0, Object arg1) { return new Localizable(holder, "WorkspaceSnapshotSCM.NoBuild", new Object[] { arg0, arg1 }); }
  
  public static String WorkspaceSnapshotSCM_NoSuchPermalink(Object arg0, Object arg1) { return holder.format("WorkspaceSnapshotSCM.NoSuchPermalink", new Object[] { arg0, arg1 }); }
  
  public static Localizable _WorkspaceSnapshotSCM_NoSuchPermalink(Object arg0, Object arg1) { return new Localizable(holder, "WorkspaceSnapshotSCM.NoSuchPermalink", new Object[] { arg0, arg1 }); }
  
  public static String WorkspaceSnapshotSCM_IncorrectJobType(Object arg0) { return holder.format("WorkspaceSnapshotSCM.IncorrectJobType", new Object[] { arg0 }); }
  
  public static Localizable _WorkspaceSnapshotSCM_IncorrectJobType(Object arg0) { return new Localizable(holder, "WorkspaceSnapshotSCM.IncorrectJobType", new Object[] { arg0 }); }
  
  public static String WorkspaceSnapshotSCM_NoWorkspace(Object arg0, Object arg1) { return holder.format("WorkspaceSnapshotSCM.NoWorkspace", new Object[] { arg0, arg1 }); }
  
  public static Localizable _WorkspaceSnapshotSCM_NoWorkspace(Object arg0, Object arg1) { return new Localizable(holder, "WorkspaceSnapshotSCM.NoWorkspace", new Object[] { arg0, arg1 }); }
  
  public static String WorkspaceSnapshotSCM_NoSuchJob(Object arg0, Object arg1) { return holder.format("WorkspaceSnapshotSCM.NoSuchJob", new Object[] { arg0, arg1 }); }
  
  public static Localizable _WorkspaceSnapshotSCM_NoSuchJob(Object arg0, Object arg1) { return new Localizable(holder, "WorkspaceSnapshotSCM.NoSuchJob", new Object[] { arg0, arg1 }); }
}
