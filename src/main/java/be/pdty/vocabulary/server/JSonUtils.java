package be.pdty.vocabulary.server;

public class JSonUtils
{
  public static String escape(String text)
  {
    if(text==null) return null;
    
    StringBuilder bld=new StringBuilder();
    for(int i=0;i<text.length();i++)
    {
      char c=text.charAt(i);
      
      if((c>='0'&&c<='9') || (c>='a')&&(c<='z') || (c>='A')&&(c<='Z') || (c==' ') || (c=='_'))
      {
        bld.append(c);
      }
      else
      {
        bld.append("&#x"); //$NON-NLS-1$
        bld.append(Integer.toHexString(c));
        bld.append(";"); //$NON-NLS-1$
      }
      
    }
    return bld.toString();
  }
  
  @SuppressWarnings("nls")
  public static String unescape(String text)
  {
    if(text==null) return null;
    
    StringBuilder build=new StringBuilder();
    int offset=0;
    while(offset<text.length())
    {
      char c=text.charAt(offset);
      if(c=='&')
      {
        int closing=text.indexOf(';',offset+1);
        if(closing==-1)
        {
          break;
        }
        String code=text.substring(offset+1,closing);
        System.out.println(code+"=code");
        if(code.startsWith("#x"))
        {
          String hexa=code.substring(2);
          int val=Integer.parseInt(hexa,16);
          build.append((char)val);
        }
        offset=closing+1;
      }
      else
      {
        build.append(c);
        offset++;
      }
    }
    
    return build.toString();
  }
  
  public static interface Json
  {
    public JSonStream toJson(JSonStream stream);
  }
  
  public static String toJson(Json json)
  {
    JSonStream stream=new JSonStream();
    json.toJson(stream);
    return stream.toString();
  }
  
  public static class JSonStream
  {
    private StringBuilder _target;
    private boolean _previousItem;
    
    public JSonStream()
    {
      _target=new StringBuilder();
    }
    
    public JSonStream beginObject()
    {
      if(_previousItem)
      {
        _target.append(',');
        _previousItem=false;
      }
      _target.append('{');
      return this;
    }
    
    public JSonStream endObject()
    {
      _target.append('}');
      _previousItem=true;
      return this;
    }
    
    public JSonStream beginArray()
    {
      if(_previousItem)
      {
        _target.append(',');
        _previousItem=false;
      }
      _target.append('[');
      return this;
    }
    
    public JSonStream endArray()
    {
      _target.append(']');
      _previousItem=true;
      return this;
    }
    
    public JSonStream beginField(String name)
    {
      if(_previousItem)
      {
        _target.append(',');
        _previousItem=false;
      }
      _target.append('"');
      _target.append(escape(name));
      _target.append("\":"); //$NON-NLS-1$
      return this;
    }
    
    public JSonStream endField()
    {
      _previousItem=true;
      return this;
    }
    
    public JSonStream value(String text)
    {
      if(text==null)
      {
        _target.append("null"); //$NON-NLS-1$
      }
      else
      {
        _target.append('"');
        _target.append(escape(text));
        _target.append('"');
      }
      return this;
    }
    
    public JSonStream value(int val)
    {
      _target.append(Integer.toString(val));
      return this;
    }
    
    public JSonStream value(boolean bool)
    {
      _target.append(bool?"true":"false"); //$NON-NLS-1$ //$NON-NLS-2$
      return this;
    }
    
    public JSonStream field(String name,String text)
    {
      return beginField(name).value(text).endField();
    }
    
    public JSonStream field(String name,int val)
    {
      return beginField(name).value(val).endField();
    }
    
    public JSonStream field(String name,boolean bool)
    {
      return beginField(name).value(bool).endField();
    }
    
    @Override
    public String toString()
    {
      return _target.toString();
    }
  }
  
}
