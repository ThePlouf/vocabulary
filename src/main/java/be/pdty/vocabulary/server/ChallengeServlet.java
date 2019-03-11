package be.pdty.vocabulary.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import be.pdty.vocabulary.server.Store.Challenge;
import be.pdty.vocabulary.server.Store.Direction;

@SuppressWarnings("nls")
public class ChallengeServlet extends HttpServlet
{
  private static final long serialVersionUID=1L;

  private Store _store;
  
  public ChallengeServlet(Store store)
  {
    _store=store;
  }
  
  @Override
  protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException
  {
    String session=req.getParameter("session");
    if(!_store.isValidSession(session))
    {
      resp.setStatus(403);
      return;
    }
    Direction direction=Direction.LEFT_TO_RIGHT;
    if("R".equals(req.getParameter("direction"))) direction=Direction.RIGHT_TO_LEFT;
    Challenge challenge=_store.getChallenge(session,direction);
    if(challenge==null)
    {
      resp.setStatus(404);
      return;
    }
    resp.setContentType("application/json");
    resp.getWriter().print(JSonUtils.toJson(challenge));
    resp.setStatus(200);
  }
}
