package hudson;

import java.util.Calendar;

public interface FeedAdapter<E> {
  String getEntryTitle(E paramE);
  
  String getEntryUrl(E paramE);
  
  String getEntryID(E paramE);
  
  String getEntryDescription(E paramE);
  
  Calendar getEntryTimestamp(E paramE);
  
  String getEntryAuthor(E paramE);
}
