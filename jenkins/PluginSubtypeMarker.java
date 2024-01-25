package jenkins;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes({"*"})
public class PluginSubtypeMarker extends AbstractProcessor {
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    try {
      Object object = new Object(this);
      for (Element e : roundEnv.getRootElements()) {
        if (e.getKind() == ElementKind.PACKAGE)
          continue; 
        object.scan(e, null);
      } 
      return false;
    } catch (RuntimeException|Error e) {
      e.printStackTrace();
      throw e;
    } 
  }
  
  public SourceVersion getSupportedSourceVersion() { return SourceVersion.latest(); }
  
  private void write(TypeElement c) throws IOException {
    FileObject f = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/hudson.Plugin", new Element[0]);
    Writer w = new OutputStreamWriter(f.openOutputStream(), StandardCharsets.UTF_8);
    try {
      w.write(c.getQualifiedName().toString());
      w.close();
    } catch (Throwable throwable) {
      try {
        w.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  private Element asElement(TypeMirror m) { return this.processingEnv.getTypeUtils().asElement(m); }
}
