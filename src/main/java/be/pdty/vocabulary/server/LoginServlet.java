package be.pdty.vocabulary.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("nls")
public class LoginServlet extends HttpServlet
{
  private static final long serialVersionUID=1L;

  private Store _store;
  
  public LoginServlet(Store store)
  {
    _store=store;
  }
  
  @Override
  protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException
  {
    String session=_store.openSession(req.getParameter("name"));
    resp.getWriter().print(session);
    resp.setStatus(200);
  }
}
