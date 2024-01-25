package hudson.model.labels;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.AutoCompletionCandidates;
import hudson.model.Item;
import hudson.model.Label;
import hudson.model.Messages;
import hudson.util.FormValidation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import jenkins.model.Jenkins;
import jenkins.model.labels.LabelAutoCompleteSeeder;
import jenkins.model.labels.LabelValidator;

public abstract class LabelExpression extends Label {
  protected LabelExpression(String name) { super(name); }
  
  public String getExpression() { return getDisplayName(); }
  
  static String paren(LabelOperatorPrecedence op, Label l) {
    if (op.compareTo(l.precedence()) < 0)
      return "(" + l.getExpression() + ")"; 
    return l.getExpression();
  }
  
  @NonNull
  public static AutoCompletionCandidates autoComplete(@Nullable String label) {
    AutoCompletionCandidates c = new AutoCompletionCandidates();
    Set<Label> labels = Jenkins.get().getLabels();
    List<String> queries = (new LabelAutoCompleteSeeder(Util.fixNull(label))).getSeeds();
    for (String term : queries) {
      for (Label l : labels) {
        if (l.getName().startsWith(term))
          c.add(l.getName()); 
      } 
    } 
    return c;
  }
  
  @NonNull
  public static FormValidation validate(@Nullable String expression) { return validate(expression, null); }
  
  @NonNull
  public static FormValidation validate(@Nullable String expression, @CheckForNull Item item) {
    if (Util.fixEmptyAndTrim(expression) == null)
      return FormValidation.ok(); 
    try {
      Label.parseExpression(expression);
    } catch (IllegalArgumentException e) {
      return FormValidation.error(e, Messages.LabelExpression_InvalidBooleanExpression(e.getMessage()));
    } 
    Jenkins j = Jenkins.get();
    Label l = j.getLabel(expression);
    if (l == null || l.isEmpty()) {
      LabelAtom masterLabel = LabelAtom.get("master");
      Set<LabelAtom> labelAtoms = (l == null) ? Collections.emptySet() : l.listAtoms();
      if (!masterLabel.equals(Jenkins.get().getSelfLabel()) && labelAtoms.contains(masterLabel) && masterLabel.isEmpty())
        return FormValidation.warningWithMarkup(Messages.LabelExpression_ObsoleteMasterLabel()); 
      for (LabelAtom a : labelAtoms) {
        if (a.isEmpty()) {
          LabelAtom nearest = LabelAtom.findNearest(a.getName());
          return FormValidation.warning(Messages.LabelExpression_NoMatch_DidYouMean(a.getName(), nearest.getDisplayName()));
        } 
      } 
      return FormValidation.warning(Messages.LabelExpression_NoMatch());
    } 
    if (item != null) {
      List<FormValidation> problems = new ArrayList<FormValidation>();
      for (AbstractProject.LabelValidator v : j.getExtensionList(AbstractProject.LabelValidator.class)) {
        FormValidation result = v.checkItem(item, l);
        if (FormValidation.Kind.OK.equals(result.kind))
          continue; 
        problems.add(result);
      } 
      for (LabelValidator v : j.getExtensionList(LabelValidator.class)) {
        FormValidation result = v.check(item, l);
        if (FormValidation.Kind.OK.equals(result.kind))
          continue; 
        problems.add(result);
      } 
      if (!problems.isEmpty())
        return FormValidation.aggregate(problems); 
    } 
    return FormValidation.okWithMarkup(Messages.LabelExpression_LabelLink(j
          .getRootUrl(), Util.escape(l.getName()), l.getUrl(), Integer.valueOf(l.getNodes().size()), Integer.valueOf(l.getClouds().size())));
  }
}
