package be.pdty.vocabulary.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import be.pdty.vocabulary.server.JSonUtils.JSonStream;
import be.pdty.vocabulary.server.JSonUtils.Json;
import be.pdty.vocabulary.server.Store.Response.AlternativeResponse;

@SuppressWarnings("nls")
public class Store
{
  private static enum AchievementName
  {
    SUCCESS_10000("10,000 correct answers!",false,p->getTotalSuccess(p)>=10000),
    SUCCESS_5000("5,000 correct answers!",false,p->getTotalSuccess(p)>=5000),
    SUCCESS_1000("1,000 correct answers!",false,p->getTotalSuccess(p)>=1000),
    SUCCESS_500("500 correct answers!",false,p->getTotalSuccess(p)>=500),
    SUCCESS_100("100 correct answers!",false,p->getTotalSuccess(p)>=100),
    SUCCESS_50("50 correct answers!",false,p->getTotalSuccess(p)>=50),
    HOURLY_SUCCESS_500("500 correct answers in one hour!",true,p->getSuccessInHistory(p)>=500),
    HOURLY_SUCCESS_250("250 correct answers in one hour!",true,p->getSuccessInHistory(p)>=250),
    HOURLY_SUCCESS_100("100 correct answers in one hour!",true,p->getSuccessInHistory(p)>=100),
    ROW_SUCCESS_200("200 correct answers in a row!",true,p->getSuccessInARow(p)>=200),
    ROW_SUCCESS_100("100 correct answers in a row!",true,p->getSuccessInARow(p)>=100),
    ROW_SUCCESS_25("25 correct answers in a row!",true,p->getSuccessInARow(p)>=25),
    ROW_ERROR_10("10 wrong answers in a row...",true,p->getFailuresInARow(p)>=10);
    
    private String _description;
    private boolean _expires;
    private Predicate<PersonalRecord> _predicate;

    private AchievementName(String description,boolean expires,Predicate<PersonalRecord> predicate)
    {
      _description=description;
      _expires=expires;
      _predicate=predicate;
    }
    
    public boolean expires()
    {
      return _expires;
    }
    
    public boolean matches(PersonalRecord record)
    {
      return _predicate.test(record);
    }
    
    public String imageSmall()
    {
      return "img/32/"+name().toLowerCase(Locale.ENGLISH)+".png";
    }
    
    public String imageLarge()
    {
      return "img/128/"+name().toLowerCase(Locale.ENGLISH)+".png";
    }
    
    public String description()
    {
      return _description;
    }
  }

  
  public static class Achievement implements Json
  {
    public Instant time;
    public AchievementName name;
    public Instant expiry;
    
    public Achievement(Instant t,AchievementName n,Instant e)
    {
      time=t;
      name=n;
      expiry=e;
    }
    
    @Override
    public JSonStream toJson(JSonStream stream)
    {
      stream.beginObject().
          field("name",name.name()).
          field("description",name.description()).
          field("imageLarge",name.imageLarge()).
          field("imageSmall",name.imageSmall()).
          field("received",received(time));
      
      if(name.expires())
      {
        stream.field("expire",willExpire(expiry));
      }
      
      return stream.endObject();
    }
  }
  
  public static enum Direction implements Json
  {
    LEFT_TO_RIGHT("L"),
    RIGHT_TO_LEFT("R");
    
    private String _json;
    
    private Direction(String j)
    {
      _json=j;
    }
    
    @Override
    public JSonStream toJson(JSonStream stream)
    {
      return stream.value(_json);
    }
    
    @Override
    public String toString()
    {
      return _json;
    }
  }
  
  public static class Challenge implements Json
  {
    public String session;
    public Direction direction;
    public String leftTitle;
    public String rightTitle;
    public String challenge;
    public int currentRow;
    public int maxRow;
    public List<PersonalStatistic> statistics;
    
    public Challenge(String s,Direction d,String lt,String rt,String c,int cr,int mr,List<PersonalStatistic> ps)
    {
      session=s;
      direction=d;
      leftTitle=lt;
      rightTitle=rt;
      challenge=c;
      currentRow=cr;
      maxRow=mr;
      statistics=ps;
    }
    
    @Override
    public JSonStream toJson(JSonStream stream)
    {
      stream.beginObject().
        field("session",session).
        beginField("direction");direction.toJson(stream).endField().
        field("challenge",challenge).
        field("leftTitle",leftTitle).
        field("rightTitle",rightTitle).
        field("currentRow",currentRow).
        field("maxRow",maxRow).
        beginField("statistics").beginArray();
        for(PersonalStatistic ps:statistics)
        {
          ps.toJson(stream);
        }
        return stream.endArray().endField().endObject();
    }
    
  }
  
  public static class Response implements Json
  {
    public static class AlternativeResponse implements Json
    {
      public String[] responses;
      public String comments;

      public AlternativeResponse(String[] r,String c)
      {
        responses=r;
        comments=c;
      }
      
      @Override
      public JSonStream toJson(JSonStream stream)
      {
        return stream.beginObject().
            field("response",responses[0]).
            field("comments",comments).
            endObject();
      }
    }
    
    public boolean success;
    public List<AlternativeResponse> alternativeResponses;
    public List<Achievement> newAchievements;
    public Challenge newChallenge;
    
    public Response(boolean s,List<AlternativeResponse> alt,List<Achievement> na,Challenge nc)
    {
      success=s;
      alternativeResponses=alt;
      newAchievements=na;
      newChallenge=nc;
    }
    
    @Override
    public JSonStream toJson(JSonStream stream)
    {
      stream.beginObject().
        field("success",success).
        beginField("alternativeResponses").beginArray();
        for(AlternativeResponse alt:alternativeResponses)
        {
          alt.toJson(stream);
        }
        stream.endArray().endField();
        if(newAchievements.size()>0)
        {
          stream.beginField("newAchievements").beginArray();
          for(Achievement ach:newAchievements)
          {
            ach.toJson(stream);
          }
          stream.endArray().endField();
        }
        stream.beginField("newChallenge");
        return newChallenge.toJson(stream).endField().endObject();
    }
    
  }
  
  public static class PersonalStatistic implements Json
  {
    public String fullName;
    public String lastSeen;
    public int total;
    public int accuracy;
    public List<Achievement> achievements;

    public PersonalStatistic(String f,String l,int t,int a,List<Achievement> ach)
    {
      fullName=f;
      lastSeen=l;
      total=t;
      accuracy=a;
      achievements=ach;
    }
    
    @Override
    public JSonStream toJson(JSonStream stream)
    {
      stream.beginObject().
      field("fullName",fullName).
      field("lastSeen",lastSeen.toString()).
      field("total",total).
      field("accuracy",accuracy).
      
      beginField("achievements").beginArray();
      for(Achievement ach:achievements)
      {
        ach.toJson(stream);
      }
      stream.endArray().endField();
      
      return stream.endObject();
    }
  }
  
  static public class WordsFile
  {
    public Instant lastModified;
    public List<String> lines;
    
    public WordsFile(Instant lm,List<String> l)
    {
      lastModified=lm;
      lines=l;
    }
  }
  
  private static class RecentHistory
  {
    public Instant time;
    public Direction direction;
    public String challenge;
    public boolean success;
    
    public RecentHistory(Instant t,Direction d,String c,boolean s)
    {
      time=t;
      direction=d;
      challenge=c;
      success=s;
    }
  }
  
  private static class PersonalRecord
  {
    public String fullName;
    public List<RecentHistory> history;
    public List<Achievement> achievements;
    public Map<String,WordStatistic> fromLeft;
    public Map<String,WordStatistic> fromRight;
    public String nextLeft;
    public String nextRight;
    
    public PersonalRecord(String f,List<RecentHistory> h,List<Achievement> a,Map<String,WordStatistic> fl,Map<String,WordStatistic> fr,String nl,String nr)
    {
      fullName=f;
      history=h;
      achievements=a;
      fromLeft=fl;
      fromRight=fr;
      nextLeft=nl;
      nextRight=nr;
    }
  }
  
  private static class WordStatistic
  {
    public Instant lastTime;
    public int total;
    public int success;
  }
  
  private static class WordFile
  {
    public Map<String,List<AlternativeResponse>> allWords;
    public String leftTitle;
    public String rightTitle;
  }
  
  private File _folder;
  private Object _lock;
  
  public Store(File folder)
  {
    _folder=folder;
    _lock=new Object();
  }
  
  private WordFile readWords(File words,Direction direction) throws IOException
  {
    WordFile ans=new WordFile();
    ans.allWords=new HashMap<>();
    ans.leftTitle="Left";
    ans.rightTitle="Right";
    
    if(!words.exists()) words.createNewFile();
    
    Files.lines(words.toPath()).
      map(s->s.trim()).
      filter(s->!s.startsWith("#")).
      map(s->s.split("\\|",3)).
      filter(array->array.length>=2).
      forEach(array->
      {
        String left;
        String rightGroup;
        if(direction==Direction.LEFT_TO_RIGHT)
        {
          left=array[0].trim();
          rightGroup=array[1].trim();
        }
        else
        {
          left=array[1].trim();
          rightGroup=array[0].trim();
        }
        String comment="";
        if(array.length>2) comment=array[2].trim();
        
        if(left.startsWith("!"))
        {
          ans.leftTitle=left.substring(1);
          ans.rightTitle=rightGroup;
        }
        else
        {
          left=left.split(",")[0];
          List<AlternativeResponse> alternatives=ans.allWords.get(left);
          if(alternatives==null)
          {
            alternatives=new ArrayList<>();
            ans.allWords.put(left,alternatives);
          }

          alternatives.add(new AlternativeResponse(rightGroup.split(","),comment));
        }
      });
    
    return ans;
  }
  
  private PersonalRecord readPersonalRecord(File file) throws IOException
  {
    HashMap<String,WordStatistic> fromLeft=new HashMap<>();
    HashMap<String,WordStatistic> fromRight=new HashMap<>();
    List<Achievement> achievements=new ArrayList<>();
    List<RecentHistory> history=new ArrayList<>();
    String fullName=null;
    String nextLeft=null;
    String nextRight=null;
    
    if(!file.exists()) file.createNewFile();

    for(String line:Files.readAllLines(file.toPath()))
    {
      line=line.trim();
      if(line.startsWith("#")) continue;
      if(line.startsWith("!"))
      {
        line=line.substring(1);
        int pos=line.indexOf('=');
        if(pos>=0)
        {
          String key=line.substring(0,pos).trim().toLowerCase(Locale.ENGLISH);
          String value=line.substring(pos+1).trim();
          switch(key)
          {
            case "fullname":
            {
              fullName=value;
              break;
            }
            case "achievement":
            {
              String[] params=value.split("\\|");
              if(params.length>=3)
              {
                try
                {
                  Achievement a=new Achievement(Instant.parse(params[1]),AchievementName.valueOf(params[0]),Instant.parse(params[2]));
                  achievements.add(a);
                }
                catch(IllegalArgumentException e)
                {
                  //Invalid achievement name
                }
              }
              break;
            }
            case "last":
            {
              String[] params=value.split("\\|");
              if(params.length>=4)
              {
                Direction d=Direction.LEFT_TO_RIGHT;
                if(params[0].equals("R")) d=Direction.RIGHT_TO_LEFT;
                RecentHistory h=new RecentHistory(Instant.parse(params[3]),d,params[1],Boolean.parseBoolean(params[2]));
                history.add(h);
              }
              break;
            }
            case "nextleft":
            {
              nextLeft=value;
              break;
            }
            case "nextright":
            {
              nextRight=value;
              break;
            }
          }
        }
      }
      else
      {
        String[] array=line.split("\\|");
        if(array.length==5)
        {
          WordStatistic stat;
          if(array[0].equals("L"))
          {
            stat=fromLeft.get(array[1]);
            if(stat==null) stat=new WordStatistic();
            fromLeft.put(array[1],stat);
          }
          else
          {
            stat=fromRight.get(array[1]);
            if(stat==null) stat=new WordStatistic();
            fromRight.put(array[1],stat);
          }
          
          stat.success+=Integer.parseInt(array[2]);
          stat.total+=Integer.parseInt(array[3]);
          Instant instant=Instant.parse(array[4]);
          if(stat.lastTime==null || stat.lastTime.isBefore(instant))
          {
            stat.lastTime=instant;
          }
        }
      }
    }
    
    if(fullName==null)
    {
      fullName=file.getName();
      int pos=fullName.indexOf('.');
      if(pos>=0)
      {
        fullName=fullName.substring(0,pos);
      }
      if(fullName.length()>0)
      {
        fullName=Character.toUpperCase(fullName.charAt(0))+fullName.substring(1);
      }
    }
    
    history.removeIf(h->h.time.isBefore(Instant.now().minus(2,ChronoUnit.HOURS)));
    achievements.removeIf(a->a.name.expires()&&a.expiry.isBefore(Instant.now()));
    
    return new PersonalRecord(fullName,history,achievements,fromLeft,fromRight,nextLeft,nextRight);
  }
  
  private Map<String,WordStatistic> expandPersonalRecord(Map<String,WordStatistic> record,Set<String> allWords)
  {
    Map<String,WordStatistic> ans=new HashMap<>(record);
    for(String s:allWords)
    {
      if(!ans.containsKey(s))
      {
        WordStatistic stat=new WordStatistic();
        stat.success=0;
        stat.total=0;
        stat.lastTime=Instant.EPOCH;
        ans.put(s,stat);
      }
    }
    
    Iterator<Map.Entry<String,WordStatistic>> entryIterator=ans.entrySet().iterator();
    while(entryIterator.hasNext())
    {
      Map.Entry<String,WordStatistic> entry=entryIterator.next();
      if(!allWords.contains(entry.getKey()))
      {
        entryIterator.remove();
      }
    }
    
    return ans;
    
  }
  
  private void savePersonalRecord(String direction,BufferedWriter bw,Map<String,WordStatistic> record) throws IOException
  {
    for(Map.Entry<String,WordStatistic> entry:record.entrySet())
    {
      bw.write(direction+"|");
      bw.write(entry.getKey()+"|");
      bw.write(Integer.toString(entry.getValue().success)+"|");
      bw.write(Integer.toString(entry.getValue().total)+"|");
      bw.write(entry.getValue().lastTime.toString()+"\n");
    }
  }
  
  private void savePersonalRecord(File file,PersonalRecord record) throws IOException
  {
    try(BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),Charset.forName("UTF-8"))))
    {
      bw.write("!fullname="+record.fullName+"\n");
      if(record.nextLeft!=null)
      {
        bw.write("!nextleft="+record.nextLeft+"\n");
      }
      if(record.nextRight!=null)
      {
        bw.write("!nextright="+record.nextRight+"\n");
      }
      for(Achievement a:record.achievements)
      {
        bw.write("!achievement="+a.name.name()+"|"+a.time+"|"+a.expiry+"\n");
      }
      for(RecentHistory h:record.history)
      {
        bw.write("!last="+h.direction+"|"+h.challenge+"|"+h.success+"|"+h.time+"\n");
      }
      savePersonalRecord("L",bw,record.fromLeft);
      savePersonalRecord("R",bw,record.fromRight);
      bw.flush();
    }
  }

  private static String received(Instant time)
  {
    long seconds=Instant.now().truncatedTo(ChronoUnit.DAYS).getEpochSecond()-time.truncatedTo(ChronoUnit.DAYS).getEpochSecond();
    
    long minutes=seconds/60;
    long hours=minutes/60;
    long days=hours/24;
    if(days<=0) return "today";
    if(days==1) return "yesterday";
    return days+" days ago";
  }
  
  private static String willExpire(Instant time)
  {
    long seconds=time.getEpochSecond()-Instant.now().getEpochSecond();
    
    long minutes=seconds/60;
    long hours=minutes/60;
    long days=hours/24;
    if(days<=0) return "today";
    if(days==1) return "tomorrow";
    return "in "+days+" days";
  }
  
  private static String lastSeen(Instant time)
  {
    if(time.getEpochSecond()==0) return "Never";
    long seconds=Instant.now().getEpochSecond()-time.getEpochSecond();
    
    if(seconds<10) return "Just now";
    if(seconds<60) return seconds+" seconds ago";
    
    long minutes=seconds/60;
    
    if(minutes==1) return "1 minute ago";
    if(minutes<60) return minutes+" minutes ago";

    long hours=minutes/60;
    
    if(hours==1) return "1 hour ago";
    if(hours<24) return hours+" hours ago";
    
    long days=hours/24;

    if(days==1) return "Yesterday";
    return days+" days ago";
  }
  
  private static PersonalStatistic extractStatistics(PersonalRecord record)
  {
    String fullName=record.fullName;
    Instant lastTime=Instant.EPOCH;
    int accuracy=0;
    int total=0;
    for(WordStatistic s:record.fromLeft.values())
    {
      if(s.lastTime.isAfter(lastTime)) lastTime=s.lastTime;
      accuracy+=s.success;
      total+=s.total;
    }
    for(WordStatistic s:record.fromRight.values())
    {
      if(s.lastTime.isAfter(lastTime)) lastTime=s.lastTime;
      accuracy+=s.success;
      total+=s.total;
    }
    if(total>0)
    {
      accuracy=100*accuracy/total;
    }
    else
    {
      accuracy=0;
    }
    String lastSeen=lastSeen(lastTime);
    
    List<Achievement> ach=new ArrayList<Achievement>(record.achievements);
    Collections.sort(ach,(a,b)->
    {
      int name=Integer.compare(a.name.ordinal(),b.name.ordinal());
      if(name!=0) return name;
      return a.time.compareTo(b.time);
    });
    
    return new PersonalStatistic(fullName,lastSeen,total,accuracy,ach);
  }
  
  private static int getSuccessInARow(PersonalRecord record)
  {
    Collections.sort(record.history,(a,b)->-a.time.compareTo(b.time));
    int total=0;
    for(RecentHistory h:record.history)
    {
      if(!h.success) return total;
      total++;
    }
    return total;
  }
  
  private static int getMaxSuccessInARow(PersonalRecord record)
  {
    Collections.sort(record.history,(a,b)->-a.time.compareTo(b.time));
    int total=0;
    int maxTotal=0;
    for(RecentHistory h:record.history)
    {
      if(!h.success)
      {
        total=0;
      }
      else
      {
        total++;
      }
      if(total>maxTotal)
      {
        maxTotal=total;
      }
    }
    return maxTotal;
  }
  
  private static int getFailuresInARow(PersonalRecord record)
  {
    Collections.sort(record.history,(a,b)->-a.time.compareTo(b.time));
    int total=0;
    for(RecentHistory h:record.history)
    {
      if(h.success) return total;
      total++;
    }
    return total;
  }
  
  private static int getSuccessInHistory(PersonalRecord record)
  {
    int total=0;
    for(RecentHistory h:record.history)
    {
      if(h.success) total++;
    }
    return total;
  }
  
  private static int getTotalSuccess(PersonalRecord record)
  {
    int total=0;
    for(WordStatistic s:record.fromLeft.values())
    {
      total+=s.success;
    }
    for(WordStatistic s:record.fromRight.values())
    {
      total+=s.success;
    }
    return total;
  }
  
  private File getWordsFile()
  {
    return new File(_folder,"words.txt");
  }
  
  private List<PersonalStatistic> getAllStatistics() throws IOException
  {
    List<PersonalStatistic> ans=new ArrayList<>();
    File[] files=_folder.listFiles();
    if(files==null) return ans;
    File wordsFile=getWordsFile().getCanonicalFile();
    for(File file:files)
    {
        if(file.isFile()&&!file.getCanonicalFile().equals(wordsFile))
        {
          PersonalRecord record=readPersonalRecord(file);
          ans.add(extractStatistics(record));
        }
    }
    return ans;
  }
  
  private static class ScoredWord implements Comparable<ScoredWord>
  {
    public ScoredWord(String w,double s)
    {
      word=w;
      score=s;
    }
    
    public String word;
    public double score;
    
    @Override
    public int compareTo(ScoredWord o)
    {
      return -Double.compare(score,o.score);
    }
  }
  
  private double getScore(WordStatistic stat)
  {
    double usage=10-stat.total;
    if(usage<0) usage=0;
    
    double accuracy=0;
    if(stat.total>0) accuracy=10-((10.0*stat.success)/stat.total);
    
    double lastUsed=(Instant.now().toEpochMilli()-stat.lastTime.toEpochMilli())/(1000.0*60*60*24);
    if(lastUsed>10) lastUsed=10;
    lastUsed-=2;
    
    double random=Math.random()*3;
    
    
    return usage+accuracy+lastUsed+random;
  }
  
  private String cleanupSession(String session)
  {
    StringBuilder bld=new StringBuilder();
    for(int i=0;i<session.length();i++)
    {
      char c=session.charAt(i);
      if(c>='a' && c<='z')
      {
        bld.append(c);
      }
    }
    return bld.toString();
  }
  
  private File getFileFromSession(String session)
  {
    return new File(_folder,cleanupSession(session)+".txt");
  }
  
  private boolean stillToday(Instant instant)
  {
    return instant.truncatedTo(ChronoUnit.DAYS).equals(Instant.now().truncatedTo(ChronoUnit.DAYS));
  }
  
  private boolean canReceiveAchievement(AchievementName name,PersonalRecord record)
  {
    for(Achievement a:record.achievements)
    {
      if(a.name.equals(name))
      {
        if(!name.expires()) return false;
        if(stillToday(a.time)) return false;
      }
    }
    return true;
  }
  
  private List<Achievement> updateAchievements(PersonalRecord record)
  {
    List<Achievement> ans=new ArrayList<>();
    
    for(AchievementName name:AchievementName.values())
    {
      if(canReceiveAchievement(name,record))
      {
        if(name.matches(record))
        {
          Achievement a=new Achievement(Instant.now(),name,Instant.now().plus(7,ChronoUnit.DAYS));
          record.achievements.add(a);
          ans.add(a);
        }
      }
    }
    
    return ans;
  }
  
  public String openSession(String name) throws IOException
  {
    synchronized(_lock)
    {
      String session=cleanupSession(CollatorUtil.cleanup(name).toLowerCase(Locale.ENGLISH));
      File file=getFileFromSession(session);
      if(!file.exists()) file.createNewFile();
      return session;
    }
  }
  
  public boolean isValidSession(String session) throws IOException
  {
    synchronized(_lock)
    {
      if(session==null) return false;
      if(cleanupSession(session).isEmpty()) return false;
      File file=getFileFromSession(session);
      if(!file.exists() || !file.isFile()) return false;
      if(file.getCanonicalFile().equals(getWordsFile().getCanonicalFile())) return false;
      return true;
    }
  }
  
  private String getNextWord(Map<String,WordStatistic> expanded)
  {
    ScoredWord scored=new ScoredWord(null,0);
    
    expanded.entrySet().stream().
      map(entry->new ScoredWord(entry.getKey(),getScore(entry.getValue()))).
      sorted().
      findFirst().ifPresent(s->
      {
        scored.word=s.word;
        scored.score=s.score;
      });
    
    return scored.word;
  }
  
  public Challenge getChallenge(String session,Direction direction) throws IOException
  {
    synchronized(_lock)
    {
      WordFile file=readWords(getWordsFile(),direction);
      PersonalRecord record=readPersonalRecord(getFileFromSession(session));
      String nextWord;
      if(direction==Direction.LEFT_TO_RIGHT)
      {
        nextWord=record.nextLeft;
      }
      else
      {
        nextWord=record.nextRight;
      }
      
      if(nextWord!=null && !file.allWords.containsKey(nextWord))
      {
        nextWord=null;
      }
      
      if(nextWord==null)
      {
        if(direction==Direction.LEFT_TO_RIGHT)
        {
          nextWord=getNextWord(expandPersonalRecord(record.fromLeft,file.allWords.keySet()));
          record.nextLeft=nextWord;
        }
        else
        {
          nextWord=getNextWord(expandPersonalRecord(record.fromRight,file.allWords.keySet()));
          record.nextRight=nextWord;
        }
        savePersonalRecord(getFileFromSession(session),record);
      }
      
      if(nextWord==null)
      {
        return null;
      }
      
      return new Challenge(session,direction,file.leftTitle,file.rightTitle,nextWord,getSuccessInARow(record),getMaxSuccessInARow(record),getAllStatistics());
    }
  }
  
  public Response proposeResponse(String session,String challenge,Direction direction,String response) throws IOException
  {
    synchronized(_lock)
    {
      WordFile file=readWords(getWordsFile(),direction);
      File userFile=getFileFromSession(session);
      PersonalRecord record=readPersonalRecord(userFile);
      
      boolean success=false;
      List<AlternativeResponse> alternativeResponses=new ArrayList<>();
      
      List<AlternativeResponse> allowedResponses=file.allWords.get(challenge);
      for(AlternativeResponse allowed:allowedResponses)
      {
        for(String allowedString:allowed.responses)
        {
          if(CollatorUtil.cleanup(response.trim()).toLowerCase(Locale.ENGLISH).equals(CollatorUtil.cleanup(allowedString.trim()).toLowerCase(Locale.ENGLISH)))
          {
            success=true;
          }
        }
        alternativeResponses.add(allowed);
      }
      
      Map<String,WordStatistic> toUpdate;
      if(direction==Direction.LEFT_TO_RIGHT)
      {
        toUpdate=record.fromLeft;
      }
      else
      {
        toUpdate=record.fromRight;
      }
      
      WordStatistic wordStats=toUpdate.get(challenge);
      if(wordStats==null)
      {
        wordStats=new WordStatistic();
        wordStats.success=0;
        wordStats.total=0;
        toUpdate.put(challenge,wordStats);
      }
      wordStats.lastTime=Instant.now();
      wordStats.total++;
      if(success)
      {
        wordStats.success++;
      }
      
      RecentHistory history=new RecentHistory(wordStats.lastTime,direction,challenge,success);
      record.history.add(history);
      record.history.removeIf(h->h.time.isBefore(Instant.now().minus(2,ChronoUnit.HOURS)));
      record.achievements.removeIf(a->a.name.expires()&&a.expiry.isBefore(Instant.now()));
      List<Achievement> newAchievements=updateAchievements(record);

      String previousNextWord;
      String nextNextWord;
      if(direction==Direction.LEFT_TO_RIGHT)
      {
        previousNextWord=record.nextLeft;
        nextNextWord=getNextWord(expandPersonalRecord(record.fromLeft,file.allWords.keySet()));
      }
      else
      {
        previousNextWord=record.nextRight;
        nextNextWord=getNextWord(expandPersonalRecord(record.fromRight,file.allWords.keySet()));
      }
      
      if(previousNextWord==null || previousNextWord.equals(challenge))
      {
        if(direction==Direction.LEFT_TO_RIGHT)
        {
          record.nextLeft=nextNextWord;
        }
        else
        {
          record.nextRight=nextNextWord;
        }
      }
      
      savePersonalRecord(userFile,record);
      
      return new Response(success,alternativeResponses,newAchievements,getChallenge(session,direction));
    }
  }
  
  public WordsFile getWordsAsString() throws IOException
  {
    synchronized(_lock)
    {
      File words=getWordsFile();
      if(!words.exists()) words.createNewFile();
      return new WordsFile(Instant.ofEpochMilli(words.lastModified()).truncatedTo(ChronoUnit.SECONDS),Files.readAllLines(words.toPath()));
    }
  }
  
  public boolean updateWordsFromString(WordsFile file) throws IOException
  {
    synchronized(_lock)
    {
      File words=getWordsFile();
      if(words.exists())
      {
        Instant instant=Instant.ofEpochMilli(words.lastModified()).truncatedTo(ChronoUnit.SECONDS);
        if(instant.isAfter(file.lastModified))
        {
          return false;
        }

        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String suffix=formatter.withZone(ZoneId.of("Z")).format(LocalDateTime.now().atZone(ZoneId.systemDefault()));
        
        File backupFolder=new File(words.getParentFile(),"sav");
        backupFolder.mkdir();
        
        File backup=new File(backupFolder,words.getName()+"_"+suffix+"Z");
        if(backup.exists())
        {
          backup.delete();
        }
        words.renameTo(backup);
      }
      Files.write(words.toPath(),file.lines);
      return true;
    }
  }
  
  public static void main(String[] args) throws Exception
  {
    Store store=new Store(new File("d:\\temp\\vocabulary"));
    
    String session=store.openSession("plouf");
    
    Response response=store.proposeResponse(session,"Dan",Direction.LEFT_TO_RIGHT,"and");
    System.out.println(JSonUtils.toJson(response));
    
    
  }
}
