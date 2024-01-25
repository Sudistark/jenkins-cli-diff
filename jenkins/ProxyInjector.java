package jenkins;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.Element;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.TypeConverterBinding;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ProxyInjector implements Injector {
  protected abstract Injector resolve();
  
  public void injectMembers(Object instance) { resolve().injectMembers(instance); }
  
  public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) { return resolve().getMembersInjector(typeLiteral); }
  
  public <T> MembersInjector<T> getMembersInjector(Class<T> type) { return resolve().getMembersInjector(type); }
  
  public Map<Key<?>, Binding<?>> getBindings() { return resolve().getBindings(); }
  
  public Map<Key<?>, Binding<?>> getAllBindings() { return resolve().getAllBindings(); }
  
  public <T> Binding<T> getBinding(Key<T> key) { return resolve().getBinding(key); }
  
  public <T> Binding<T> getBinding(Class<T> type) { return resolve().getBinding(type); }
  
  public <T> Binding<T> getExistingBinding(Key<T> key) { return resolve().getExistingBinding(key); }
  
  public <T> List<Binding<T>> findBindingsByType(TypeLiteral<T> type) { return resolve().findBindingsByType(type); }
  
  public <T> Provider<T> getProvider(Key<T> key) { return resolve().getProvider(key); }
  
  public <T> Provider<T> getProvider(Class<T> type) { return resolve().getProvider(type); }
  
  public <T> T getInstance(Key<T> key) { return (T)resolve().getInstance(key); }
  
  public <T> T getInstance(Class<T> type) { return (T)resolve().getInstance(type); }
  
  public Injector getParent() { return resolve().getParent(); }
  
  public Injector createChildInjector(Iterable<? extends Module> modules) { return resolve().createChildInjector(modules); }
  
  public Injector createChildInjector(Module... modules) { return resolve().createChildInjector(modules); }
  
  public Map<Class<? extends Annotation>, Scope> getScopeBindings() { return resolve().getScopeBindings(); }
  
  public Set<TypeConverterBinding> getTypeConverterBindings() { return resolve().getTypeConverterBindings(); }
  
  public List<Element> getElements() { return resolve().getElements(); }
  
  public Map<TypeLiteral<?>, List<InjectionPoint>> getAllMembersInjectorInjectionPoints() { return resolve().getAllMembersInjectorInjectionPoints(); }
}
