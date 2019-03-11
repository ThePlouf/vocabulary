var currentChallenge;
var lastModified;

failedRequest=function(xmlhttp)
{
    if(xmlhttp.status===0)
    {
        alert("Failed to connect");
    }
    else
    {
        alert(xmlhttp.statusText);
    }
}

openSession=function()
{
    if(document.getElementById("name").value.length==0) return;
    document.getElementById("name").disabled=true;
    xmlhttp=new XMLHttpRequest();
    xmlhttp.onreadystatechange=function()
    {
        if(xmlhttp.readyState==4)
        {
            document.getElementById("name").disabled=false;
            if(xmlhttp.status!=200)
            {
                failedRequest(xmlhttp);
            }
            else
            {
                window.location="./main.html?session="+xmlhttp.responseText;
            }

        }
    }
    xmlhttp.open("POST","servlet/login?name="+encodeURIComponent(document.getElementById("name").value),true);
    xmlhttp.send();

}

getSessionId=function()
{
    if(location.search)
    {
        var split=document.location.search.substring(1).split('&');
        for(var i=0;i<split.length;i++)
        {
            var parts=split[i].split('=');
            if(parts[0]==='session')
            {
                return parts[1];
            }
        }
    }
    return "";
}

getDirection=function()
{
    if(document.getElementById("leftToRight").checked) return "L";
    return "R";
}

responseTextUpdated=function()
{
    document.getElementById("success").className="hiddenDiv";
    document.getElementById("failure").className="hiddenDiv";
    
}

selectDir=function(dir)
{
    if(dir==='L')
    {
        document.getElementById("leftToRight").checked=true;
    }
    else
    {
        document.getElementById("rightToLeft").checked=true;
    }
    newWord();
}

fillFromChallenge=function(challenge)
{
    currentChallenge=challenge.challenge;
    var stats="<table id='statisticsTable'><thead><tr><th>Full name</th><th>Achievements</th><th>Last seen</th></tr></thead><tbody id='statisticsTableBody'>";
    for(var i=0;i<challenge.statistics.length;i++)
    {
        stats=stats+"<tr>";
        stats=stats+"<td>"+challenge.statistics[i].fullName+"</td>";
        stats=stats+"<td class='achievementCell'>";
        for(var j=0;j<challenge.statistics[i].achievements.length;j++)
        {
            var ach=challenge.statistics[i].achievements[j];
            
            var description=ach.description+"\nReceived "+ach.received;
            if(ach.expire)
            {
                description=description+"\nWill expire "+ach.expire;
            }
            
            stats=stats+"<img class='achievementImage' src='"+ach.imageSmall+"' title='"+description+"' alt='"+ach.name+"'>";
        }
        stats=stats+"</td>";
        stats=stats+"<td>"+challenge.statistics[i].lastSeen+"</td>";
        stats=stats+"</tr>";
    }
    stats=stats+"</tbody></table>";
    document.getElementById("statisticsArea").innerHTML=stats;

    document.getElementById("challengeText").innerHTML=challenge.challenge;
    
    document.getElementById("leftToRightContentDiv").innerHTML=challenge.leftTitle+" to "+challenge.rightTitle;
    document.getElementById("rightToLeftContentDiv").innerHTML=challenge.rightTitle+" to "+challenge.leftTitle;
    
    document.getElementById("challengeResponse").value="";

    document.getElementById("challengeResponse").focus();
}

fillFromResponse=function(response)
{
    var previousChallenge=currentChallenge;
    
    fillFromChallenge(response.newChallenge);
    if(response.success)
    {
        document.getElementById("success").className="";
        document.getElementById("failure").className="hiddenDiv";
    }
    else
    {
        document.getElementById("success").className="hiddenDiv";
        document.getElementById("failure").className="";
    }

    var alts="";
    if(response.success)
    {
        alts="<table id='successAlternativesTable'><thead><tr><th>Correct word</th><th>Remarks</th></tr></thead><tbody id='successAlternativesTableBody'>";
    }
    else
    {
        alts="<table id='failureAlternativesTable'><thead><tr><th>Correct word</th><th>Remarks</th></tr></thead><tbody id='failureAlternativesTableBody'>";
    }
    
    for(var i=0;i<response.alternativeResponses.length;i++)
    {
        alts=alts+"<tr>";
        alts=alts+"<td>"+response.alternativeResponses[i].response+"</td>";
        alts=alts+"<td>"+response.alternativeResponses[i].comments+"</td>";
        alts=alts+"</tr>";
    }
    alts=alts+"</tbody></table>";
    
    if(response.success)
    {
        document.getElementById("successMessage").innerHTML="("+response.newChallenge.currentRow+"/"+response.newChallenge.maxRow+") Correct answer for "+previousChallenge+"!";
        document.getElementById("successAlternativesDiv").innerHTML=alts;
    }
    else
    {
        document.getElementById("failureMessage").innerHTML="Wrong answer for "+previousChallenge+"!";
        document.getElementById("failureAlternativesDiv").innerHTML=alts;
    }
    
    if(response.newAchievements && response.newAchievements.length>0)
    {
        var ach=response.newAchievements[0];
        var text="<div class='achievementDescription'>"+ach.description+"</div>";

        var description=ach.description+"\nReceived "+ach.received;
        if(ach.expire)
        {
            description=description+"\nWill expire "+ach.expire;
        }
        
        var imgLeft="<img class='achievementImageLeft' src='"+ach.imageLarge+"' title='"+description+"' alt='"+ach.name+"'>";
        var imgRight="<img class='achievementImageRight' src='"+ach.imageLarge+"' title='"+description+"' alt='"+ach.name+"'>";

        if(response.success)
        {
            document.getElementById("successAchievement").innerHTML=imgLeft+imgRight+text;
        }
        else
        {
            document.getElementById("failureAchievement").innerHTML=imgLeft+imgRight+text;
        }
    }
    else
    {
        if(response.success)
        {
            document.getElementById("successAchievement").innerHTML="";
        }
        else
        {
            document.getElementById("failureAchievement").innerHTML="";
        }
    }
    
}

newWord=function()
{
    var sessionId=getSessionId();
    if(sessionId=="")
    {
        window.location="index.html";
        return;
    }
    
    if(document.getElementById("leftToRight").checked==true)
    {
        document.getElementById("leftToRightContentDiv").className="radioContent-selected";
        document.getElementById("rightToLeftContentDiv").className="radioContent-unselected";
    }
    else
    {
        document.getElementById("leftToRightContentDiv").className="radioContent-unselected";
        document.getElementById("rightToLeftContentDiv").className="radioContent-selected";
    }
    

    xmlhttp=new XMLHttpRequest();
    xmlhttp.onreadystatechange=function()
    {
        if(xmlhttp.readyState==4)
        {
            document.getElementById("challengeResponse").disabled=false;

            if(xmlhttp.status!=200)
            {
                failedRequest(xmlhttp);
            }
            else
            {
                var challenge=JSON.parse(xmlhttp.responseText);
                fillFromChallenge(challenge);
            }

        }
    }
    document.getElementById("challengeResponse").disabled=true;

    xmlhttp.open("POST","servlet/challenge?session="+encodeURIComponent(sessionId)+"&direction="+encodeURIComponent(getDirection()),true);
    xmlhttp.send();
}

submitResponse=function()
{
    if(document.getElementById("challengeResponse").value.length==0) return;

    var sessionId=getSessionId();
    if(sessionId=="")
    {
        window.location="index.html";
        return;
    }

    var challenge=currentChallenge;
    var response=document.getElementById("challengeResponse").value;

    xmlhttp=new XMLHttpRequest();
    xmlhttp.onreadystatechange=function()
    {
        if(xmlhttp.readyState==4)
        {
            document.getElementById("challengeResponse").disabled=false;
            
            if(xmlhttp.status!=200)
            {
                failedRequest(xmlhttp);
            }
            else
            {
                var response=JSON.parse(xmlhttp.responseText);
                fillFromResponse(response);
            }

        }
    }

    document.getElementById("challengeResponse").disabled=true;
    
    xmlhttp.open("POST","servlet/response?session="+encodeURIComponent(sessionId)+"&direction="+encodeURIComponent(getDirection())+"&challenge="+encodeURIComponent(challenge)+"&response="+encodeURIComponent(response),true);
    xmlhttp.send();
}

fillAdmin=function()
{
    xmlhttp=new XMLHttpRequest();
    xmlhttp.onreadystatechange=function()
    {
        if(xmlhttp.readyState==4)
        {
            if(xmlhttp.status!=200)
            {
                failedRequest(xmlhttp);
            }
            else
            {
                document.getElementById("adminArea").value=xmlhttp.responseText;
                lastModified=xmlhttp.getResponseHeader("Last-Modified");
            }

        }
    }
    
    xmlhttp.open("POST","servlet/admin",true);
    xmlhttp.send();
}

submitAdmin=function()
{
    xmlhttp=new XMLHttpRequest();
    xmlhttp.onreadystatechange=function()
    {
        if(xmlhttp.readyState==4)
        {
            if(xmlhttp.status!=200)
            {
                failedRequest(xmlhttp);
            }
            else
            {
                alert("Updated");
                document.location.reload();
            }
        }
    }
    
    xmlhttp.open("POST","servlet/admin?password="+encodeURIComponent(document.getElementById("adminPassword").value),true);
    xmlhttp.setRequestHeader("Last-Modified",lastModified);
    xmlhttp.send(document.getElementById("adminArea").value);
}
