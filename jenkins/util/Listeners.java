package jenkins.util;

import hudson.ExtensionList;
import hudson.security.ACL;
import hudson.security.ACLContext;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Listeners {
  public static <L> void notify(Class<L> listenerType, boolean asSystem, Consumer<L> notification) {
    Runnable r = () -> {
        for (L listener : ExtensionList.lookup(listenerType)) {
          try {
            notification.accept(listener);
          } catch (Throwable x) {
            Logger.getLogger(listenerType.getName()).log(Level.WARNING, null, x);
          } 
        } 
      };
    if (asSystem) {
      ACLContext ctx = ACL.as2(ACL.SYSTEM2);
      try {
        r.run();
        if (ctx != null)
          ctx.close(); 
      } catch (Throwable throwable) {
        if (ctx != null)
          try {
            ctx.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } else {
      r.run();
    } 
  }
}
