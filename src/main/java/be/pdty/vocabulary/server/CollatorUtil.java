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

import java.text.CollationElementIterator;
import java.text.Collator;
import java.text.RuleBasedCollator;

/**
 * CollatorUtil.
 */
public class CollatorUtil
{
  private static char[] _primaryToChar=null;
  private static Object _lock=new Object();
  
  private static void computeTable()
  {
    synchronized(_lock)
    {
      if(_primaryToChar!=null)
        return;
      _primaryToChar=new char[65536];
      RuleBasedCollator col=(RuleBasedCollator)Collator.getInstance();
      for(int i=0;i<65536;i++)
      {
        CollationElementIterator it=col.getCollationElementIterator(""+(char)i); //$NON-NLS-1$
        int v=it.next();
        if(v!=CollationElementIterator.NULLORDER && it.next()==CollationElementIterator.NULLORDER)
        {
          int prim=CollationElementIterator.primaryOrder(v);
          int sec=CollationElementIterator.secondaryOrder(v);
          int tier=CollationElementIterator.tertiaryOrder(v);
          if(prim>0 && prim<65536 && sec==0 && tier==0)
          {
            if(_primaryToChar[prim]==0)
            {
              _primaryToChar[prim]=(char)i;
            }
          }
        }
      }
    }
  }
  
  /**
   * Cleanup the given string by removing any accents and upper case.
   * @param s input string.
   * @return cleaned-up string.
   */
  public static String cleanup(String s)
  {
    computeTable();
    if(s==null)
      return null;
    StringBuilder ans=new StringBuilder();
    RuleBasedCollator col=(RuleBasedCollator)Collator.getInstance();
    CollationElementIterator it=col.getCollationElementIterator(s);
    int v=it.next();
    while(v!=CollationElementIterator.NULLORDER)
    {
      int prim=CollationElementIterator.primaryOrder(v);
      if(prim>0 && prim<65536)
      {
        ans.append(_primaryToChar[prim]);
      }
      v=it.next();
    }
    
    return ans.toString();
  }
}
