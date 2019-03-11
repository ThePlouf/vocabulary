package be.pdty.vocabulary.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("nls")
public class AdminServlet extends HttpServlet
{
  private static final long serialVersionUID=1L;

  private Store _store;
  
  public AdminServlet(Store store)
  {
    _store=store;
  }
  
  @Override
  protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException
  {
    String password=req.getParameter("password");
    if(password==null)
    {
      Store.WordsFile file=_store.getWordsAsString();
      
      resp.setContentType("text/plain");
      resp.setDateHeader("Last-Modified",file.lastModified.toEpochMilli());
      for(String line:file.lines)
      {
        resp.getWriter().println(line);
      }
      resp.setStatus(200);
      return;
    }
    
    if(!"usr-=ALL".equals(password))
    {
      resp.setStatus(403);
      return;
    }
    
    Instant instant=Instant.ofEpochMilli(req.getDateHeader("Last-Modified"));
    
    List<String> lines=new ArrayList<String>();
    BufferedReader br=new BufferedReader(new InputStreamReader(req.getInputStream(),"UTF-8"));
    String line=br.readLine();
    while(line!=null)
    {
      lines.add(line);
      line=br.readLine();
    }
    
    boolean newer=_store.updateWordsFromString(new Store.WordsFile(instant,lines));
    if(newer)
    {
      resp.setStatus(200);
    }
    else
    {
      resp.setStatus(409);
    }
  }
}
