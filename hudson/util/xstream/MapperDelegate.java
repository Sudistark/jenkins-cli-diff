package hudson.util.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class MapperDelegate extends MapperWrapper {
  protected Mapper delegate;
  
  public MapperDelegate(Mapper delegate) {
    super(null);
    this.delegate = delegate;
  }
  
  public String serializedClass(Class type) { return this.delegate.serializedClass(type); }
  
  public Class realClass(String elementName) { return this.delegate.realClass(elementName); }
  
  public String serializedMember(Class type, String memberName) { return this.delegate.serializedMember(type, memberName); }
  
  public String realMember(Class type, String serialized) { return this.delegate.realMember(type, serialized); }
  
  public boolean isImmutableValueType(Class type) { return this.delegate.isImmutableValueType(type); }
  
  public Class defaultImplementationOf(Class type) { return this.delegate.defaultImplementationOf(type); }
  
  public String aliasForAttribute(String attribute) { return this.delegate.aliasForAttribute(attribute); }
  
  public String attributeForAlias(String alias) { return this.delegate.attributeForAlias(alias); }
  
  public String aliasForSystemAttribute(String attribute) { return this.delegate.aliasForSystemAttribute(attribute); }
  
  public String getFieldNameForItemTypeAndName(Class definedIn, Class itemType, String itemFieldName) { return this.delegate.getFieldNameForItemTypeAndName(definedIn, itemType, itemFieldName); }
  
  public Class getItemTypeForItemFieldName(Class definedIn, String itemFieldName) { return this.delegate.getItemTypeForItemFieldName(definedIn, itemFieldName); }
  
  public Mapper.ImplicitCollectionMapping getImplicitCollectionDefForFieldName(Class itemType, String fieldName) { return this.delegate.getImplicitCollectionDefForFieldName(itemType, fieldName); }
  
  public boolean shouldSerializeMember(Class definedIn, String fieldName) { return this.delegate.shouldSerializeMember(definedIn, fieldName); }
  
  @Deprecated
  public SingleValueConverter getConverterFromItemType(String fieldName, Class type) { return this.delegate.getConverterFromItemType(fieldName, type); }
  
  @Deprecated
  public SingleValueConverter getConverterFromItemType(Class type) { return this.delegate.getConverterFromItemType(type); }
  
  @Deprecated
  public SingleValueConverter getConverterFromAttribute(String name) { return this.delegate.getConverterFromAttribute(name); }
  
  public Converter getLocalConverter(Class definedIn, String fieldName) { return this.delegate.getLocalConverter(definedIn, fieldName); }
  
  public Mapper lookupMapperOfType(Class type) { return type.isAssignableFrom(getClass()) ? this : this.delegate.lookupMapperOfType(type); }
  
  public SingleValueConverter getConverterFromItemType(String fieldName, Class type, Class definedIn) { return this.delegate.getConverterFromItemType(fieldName, type, definedIn); }
  
  @Deprecated
  public String aliasForAttribute(Class definedIn, String fieldName) { return this.delegate.aliasForAttribute(definedIn, fieldName); }
  
  @Deprecated
  public String attributeForAlias(Class definedIn, String alias) { return this.delegate.attributeForAlias(definedIn, alias); }
  
  @Deprecated
  public SingleValueConverter getConverterFromAttribute(Class type, String attribute) { return this.delegate.getConverterFromAttribute(type, attribute); }
  
  public SingleValueConverter getConverterFromAttribute(Class definedIn, String attribute, Class type) { return this.delegate.getConverterFromAttribute(definedIn, attribute, type); }
  
  public boolean isIgnoredElement(String name) { return this.delegate.isIgnoredElement(name); }
  
  public boolean isReferenceable(Class type) { return this.delegate.isReferenceable(type); }
}
