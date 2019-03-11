/*
 * Copyright 2019 Philippe Detournay
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package be.pdty.vocabulary.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import be.pdty.vocabulary.server.Store.Direction;
import be.pdty.vocabulary.server.Store.Response;

@SuppressWarnings("nls")
public class ResponseServlet extends HttpServlet
{
  private static final long serialVersionUID=1L;

  private Store _store;
  
  public ResponseServlet(Store store)
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
    
    String challenge=JSonUtils.unescape(req.getParameter("challenge"));
    String response=req.getParameter("response");
    
    Response challengeResponse=_store.proposeResponse(session,challenge,direction,response);
    
    resp.setContentType("application/json");
    resp.getWriter().print(JSonUtils.toJson(challengeResponse));
    resp.setStatus(200);
  }
}
