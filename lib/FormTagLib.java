package lib;

import groovy.lang.Closure;
import java.util.Map;
import org.kohsuke.stapler.jelly.groovy.TagFile;
import org.kohsuke.stapler.jelly.groovy.TagLibraryUri;
import org.kohsuke.stapler.jelly.groovy.TypedTagLibrary;

@TagLibraryUri("/lib/form")
public interface FormTagLib extends TypedTagLibrary {
  void textbox(Map paramMap, Closure paramClosure);
  
  void textbox(Closure paramClosure);
  
  void textbox(Map paramMap);
  
  void textbox();
  
  void rowSet(Map paramMap, Closure paramClosure);
  
  void rowSet(Closure paramClosure);
  
  void rowSet(Map paramMap);
  
  void rowSet();
  
  void combobox(Map paramMap, Closure paramClosure);
  
  void combobox(Closure paramClosure);
  
  void combobox(Map paramMap);
  
  void combobox();
  
  void descriptorList(Map paramMap, Closure paramClosure);
  
  void descriptorList(Closure paramClosure);
  
  void descriptorList(Map paramMap);
  
  void descriptorList();
  
  void enumSet(Map paramMap, Closure paramClosure);
  
  void enumSet(Closure paramClosure);
  
  void enumSet(Map paramMap);
  
  void enumSet();
  
  void optionalProperty(Map paramMap, Closure paramClosure);
  
  void optionalProperty(Closure paramClosure);
  
  void optionalProperty(Map paramMap);
  
  void optionalProperty();
  
  void dropdownList(Map paramMap, Closure paramClosure);
  
  void dropdownList(Closure paramClosure);
  
  void dropdownList(Map paramMap);
  
  void dropdownList();
  
  void file(Map paramMap, Closure paramClosure);
  
  void file(Closure paramClosure);
  
  void file(Map paramMap);
  
  void file();
  
  @TagFile("hetero-radio")
  void hetero_radio(Map paramMap, Closure paramClosure);
  
  @TagFile("hetero-radio")
  void hetero_radio(Closure paramClosure);
  
  @TagFile("hetero-radio")
  void hetero_radio(Map paramMap);
  
  @TagFile("hetero-radio")
  void hetero_radio();
  
  void number(Map paramMap, Closure paramClosure);
  
  void number(Closure paramClosure);
  
  void number(Map paramMap);
  
  void number();
  
  void repeatableHeteroProperty(Map paramMap, Closure paramClosure);
  
  void repeatableHeteroProperty(Closure paramClosure);
  
  void repeatableHeteroProperty(Map paramMap);
  
  void repeatableHeteroProperty();
  
  void helpArea(Map paramMap, Closure paramClosure);
  
  void helpArea(Closure paramClosure);
  
  void helpArea(Map paramMap);
  
  void helpArea();
  
  void secretTextarea(Map paramMap, Closure paramClosure);
  
  void secretTextarea(Closure paramClosure);
  
  void secretTextarea(Map paramMap);
  
  void secretTextarea();
  
  void textarea(Map paramMap, Closure paramClosure);
  
  void textarea(Closure paramClosure);
  
  void textarea(Map paramMap);
  
  void textarea();
  
  @TagFile("enum")
  void enum_(Map paramMap, Closure paramClosure);
  
  @TagFile("enum")
  void enum_(Closure paramClosure);
  
  @TagFile("enum")
  void enum_(Map paramMap);
  
  @TagFile("enum")
  void enum_();
  
  void prepareDatabinding(Map paramMap, Closure paramClosure);
  
  void prepareDatabinding(Closure paramClosure);
  
  void prepareDatabinding(Map paramMap);
  
  void prepareDatabinding();
  
  void description(Map paramMap, Closure paramClosure);
  
  void description(Closure paramClosure);
  
  void description(Map paramMap);
  
  void description();
  
  void toggleSwitch(Map paramMap, Closure paramClosure);
  
  void toggleSwitch(Closure paramClosure);
  
  void toggleSwitch(Map paramMap);
  
  void toggleSwitch();
  
  void descriptorRadioList(Map paramMap, Closure paramClosure);
  
  void descriptorRadioList(Closure paramClosure);
  
  void descriptorRadioList(Map paramMap);
  
  void descriptorRadioList();
  
  void form(Map paramMap, Closure paramClosure);
  
  void form(Closure paramClosure);
  
  void form(Map paramMap);
  
  void form();
  
  void optionalBlock(Map paramMap, Closure paramClosure);
  
  void optionalBlock(Closure paramClosure);
  
  void optionalBlock(Map paramMap);
  
  void optionalBlock();
  
  void entry(Map paramMap, Closure paramClosure);
  
  void entry(Closure paramClosure);
  
  void entry(Map paramMap);
  
  void entry();
  
  void editableComboBox(Map paramMap, Closure paramClosure);
  
  void editableComboBox(Closure paramClosure);
  
  void editableComboBox(Map paramMap);
  
  void editableComboBox();
  
  void dropdownDescriptorSelector(Map paramMap, Closure paramClosure);
  
  void dropdownDescriptorSelector(Closure paramClosure);
  
  void dropdownDescriptorSelector(Map paramMap);
  
  void dropdownDescriptorSelector();
  
  void expandableTextbox(Map paramMap, Closure paramClosure);
  
  void expandableTextbox(Closure paramClosure);
  
  void expandableTextbox(Map paramMap);
  
  void expandableTextbox();
  
  void readOnlyTextbox(Map paramMap, Closure paramClosure);
  
  void readOnlyTextbox(Closure paramClosure);
  
  void readOnlyTextbox(Map paramMap);
  
  void readOnlyTextbox();
  
  void invisibleEntry(Map paramMap, Closure paramClosure);
  
  void invisibleEntry(Closure paramClosure);
  
  void invisibleEntry(Map paramMap);
  
  void invisibleEntry();
  
  void apply(Map paramMap, Closure paramClosure);
  
  void apply(Closure paramClosure);
  
  void apply(Map paramMap);
  
  void apply();
  
  void helpLink(Map paramMap, Closure paramClosure);
  
  void helpLink(Closure paramClosure);
  
  void helpLink(Map paramMap);
  
  void helpLink();
  
  void repeatableProperty(Map paramMap, Closure paramClosure);
  
  void repeatableProperty(Closure paramClosure);
  
  void repeatableProperty(Map paramMap);
  
  void repeatableProperty();
  
  @TagFile("class-entry")
  void class_entry(Map paramMap, Closure paramClosure);
  
  @TagFile("class-entry")
  void class_entry(Closure paramClosure);
  
  @TagFile("class-entry")
  void class_entry(Map paramMap);
  
  @TagFile("class-entry")
  void class_entry();
  
  void withCustomDescriptorByName(Map paramMap, Closure paramClosure);
  
  void withCustomDescriptorByName(Closure paramClosure);
  
  void withCustomDescriptorByName(Map paramMap);
  
  void withCustomDescriptorByName();
  
  void nested(Map paramMap, Closure paramClosure);
  
  void nested(Closure paramClosure);
  
  void nested(Map paramMap);
  
  void nested();
  
  void option(Map paramMap, Closure paramClosure);
  
  void option(Closure paramClosure);
  
  void option(Map paramMap);
  
  void option();
  
  void validateButton(Map paramMap, Closure paramClosure);
  
  void validateButton(Closure paramClosure);
  
  void validateButton(Map paramMap);
  
  void validateButton();
  
  @TagFile("breadcrumb-config-outline")
  void breadcrumb_config_outline(Map paramMap, Closure paramClosure);
  
  @TagFile("breadcrumb-config-outline")
  void breadcrumb_config_outline(Closure paramClosure);
  
  @TagFile("breadcrumb-config-outline")
  void breadcrumb_config_outline(Map paramMap);
  
  @TagFile("breadcrumb-config-outline")
  void breadcrumb_config_outline();
  
  void dropdownListBlock(Map paramMap, Closure paramClosure);
  
  void dropdownListBlock(Closure paramClosure);
  
  void dropdownListBlock(Map paramMap);
  
  void dropdownListBlock();
  
  void repeatable(Map paramMap, Closure paramClosure);
  
  void repeatable(Closure paramClosure);
  
  void repeatable(Map paramMap);
  
  void repeatable();
  
  void password(Map paramMap, Closure paramClosure);
  
  void password(Closure paramClosure);
  
  void password(Map paramMap);
  
  void password();
  
  void checkbox(Map paramMap, Closure paramClosure);
  
  void checkbox(Closure paramClosure);
  
  void checkbox(Map paramMap);
  
  void checkbox();
  
  void editableComboBoxValue(Map paramMap, Closure paramClosure);
  
  void editableComboBoxValue(Closure paramClosure);
  
  void editableComboBoxValue(Map paramMap);
  
  void editableComboBoxValue();
  
  void property(Map paramMap, Closure paramClosure);
  
  void property(Closure paramClosure);
  
  void property(Map paramMap);
  
  void property();
  
  void advanced(Map paramMap, Closure paramClosure);
  
  void advanced(Closure paramClosure);
  
  void advanced(Map paramMap);
  
  void advanced();
  
  void submit(Map paramMap, Closure paramClosure);
  
  void submit(Closure paramClosure);
  
  void submit(Map paramMap);
  
  void submit();
  
  void section(Map paramMap, Closure paramClosure);
  
  void section(Closure paramClosure);
  
  void section(Map paramMap);
  
  void section();
  
  void link(Map paramMap, Closure paramClosure);
  
  void link(Closure paramClosure);
  
  void link(Map paramMap);
  
  void link();
  
  @TagFile("hetero-list")
  void hetero_list(Map paramMap, Closure paramClosure);
  
  @TagFile("hetero-list")
  void hetero_list(Closure paramClosure);
  
  @TagFile("hetero-list")
  void hetero_list(Map paramMap);
  
  @TagFile("hetero-list")
  void hetero_list();
  
  void possibleReadOnlyField(Map paramMap, Closure paramClosure);
  
  void possibleReadOnlyField(Closure paramClosure);
  
  void possibleReadOnlyField(Map paramMap);
  
  void possibleReadOnlyField();
  
  void repeatableDeleteButton(Map paramMap, Closure paramClosure);
  
  void repeatableDeleteButton(Closure paramClosure);
  
  void repeatableDeleteButton(Map paramMap);
  
  void repeatableDeleteButton();
  
  void block(Map paramMap, Closure paramClosure);
  
  void block(Closure paramClosure);
  
  void block(Map paramMap);
  
  void block();
  
  @TagFile("slave-mode")
  void slave_mode(Map paramMap, Closure paramClosure);
  
  @TagFile("slave-mode")
  void slave_mode(Closure paramClosure);
  
  @TagFile("slave-mode")
  void slave_mode(Map paramMap);
  
  @TagFile("slave-mode")
  void slave_mode();
  
  void radioBlock(Map paramMap, Closure paramClosure);
  
  void radioBlock(Closure paramClosure);
  
  void radioBlock(Map paramMap);
  
  void radioBlock();
  
  void radio(Map paramMap, Closure paramClosure);
  
  void radio(Closure paramClosure);
  
  void radio(Map paramMap);
  
  void radio();
  
  void bottomButtonBar(Map paramMap, Closure paramClosure);
  
  void bottomButtonBar(Closure paramClosure);
  
  void bottomButtonBar(Map paramMap);
  
  void bottomButtonBar();
  
  void select(Map paramMap, Closure paramClosure);
  
  void select(Closure paramClosure);
  
  void select(Map paramMap);
  
  void select();
  
  void booleanRadio(Map paramMap, Closure paramClosure);
  
  void booleanRadio(Closure paramClosure);
  
  void booleanRadio(Map paramMap);
  
  void booleanRadio();
}
