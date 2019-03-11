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

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

@SuppressWarnings("nls")
public class Main
{
  public static void main(String[] args) throws Exception
  {
    if(args.length!=3)
    {
      System.err.println("Usage: Main port dataFolder resourceFolder");
      System.exit(1);
    }
    int port=Integer.parseInt(args[0]);
    File data=new File(args[1]);
    if(!data.exists() || !data.isDirectory())
    {
      System.err.println(args[1]+" does not exist or is not a directory");
      System.exit(2);
    }
    File rsc=new File(args[2]);
    if(!data.exists() || !data.isDirectory())
    {
      System.err.println(args[2]+" does not exist or is not a directory");
      System.exit(3);
    }
    
    Store store=new Store(data);
    
    Server server=new Server(port);
    HandlerCollection handlers=new HandlerCollection();
    
    ServletContextHandler serverHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS|ServletContextHandler.NO_SECURITY);
    serverHandler.setContextPath("/servlet");
    serverHandler.addServlet(new ServletHolder(new LoginServlet(store)),"/login");
    serverHandler.addServlet(new ServletHolder(new ChallengeServlet(store)),"/challenge");
    serverHandler.addServlet(new ServletHolder(new ResponseServlet(store)),"/response");
    serverHandler.addServlet(new ServletHolder(new AdminServlet(store)),"/admin");
    handlers.addHandler(serverHandler);

    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setResourceBase(rsc.getAbsolutePath());
    handlers.addHandler(resourceHandler);
    
    server.setHandler(handlers);
    server.start();
    server.join();
    
  }
}
