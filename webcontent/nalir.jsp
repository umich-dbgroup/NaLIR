<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="architecture.*" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<style>
.vR {
font-size: 13px;
display: inline-block;
vertical-align: top;
}

.vN {
background-color: #f5f5f5;
border: 1.2px solid #B8B8B8;
cursor: default;
display: block;
height: 20px;
white-space: nowrap;
-webkit-border-radius: 3px;
border-radius: 3px;
}

.vT {
display: inline-block;
margin: 2px;
overflow: hidden;
text-overflow: ellipsis;
direction: ltr;
}

.vM {
display: inline-block;
width: 14px;
height: 20px;
background: no-repeat url("cross.gif") -4px 0;
vertical-align: top;
cursor: pointer;
}

table,th,td
{
border:1px solid black;
border-collapse:collapse;
}
</style>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
  <title>NaliR</title>

<script src="jquery-latest.js"></script>
<script>
$(document).ready(function()
{
	$.post("conductCommand.jsp",
	{
		command: "#useDB mas"
	},
	function(data,status)
	{
		feedback(data); 
	});

	$("#submitQuery").click(function()
	{
		$.post("conductCommand.jsp",
		{
			command: "#query " + $("#inputSentence").val()
		},
		function(data,status)
		{
			feedback(data); 
		});
	});
	$("#database").change(function()
	{
		$.post("conductCommand.jsp",
		{
			command: "#useDB " + $("#database").val() 
		},
		function(data,status)
		{
			feedback(data); 
		});
	});
});

function feedback(data)
{
	var feedbacks = data.split("\n"); 
	var i = 0; 
	while(i < feedbacks.length)
	{
		var curFeedback = feedbacks[i].split(" "); 
		if(curFeedback[0] == "#history")
		{
			var history = "<datalist id=\"browsers\">"; 
			while(i < feedbacks.length && curFeedback[0] == "#history")
			{
				history += "<option value='" + feedbacks[i].substring(9) + "'>"; 
				curFeedback = feedbacks[i].split(" "); 
				i++; 
			}
			history += "</datalist>"; 
			i--; 

			document.getElementById("inputBlock").innerHTML = history;	
		}
		if(curFeedback[0] == "#inputWord")
		{
			var sentence = ""; 
			while(i < feedbacks.length && curFeedback[0] == "#inputWord")
			{
				var word = curFeedback[2]; 
				for(var j = 3; j < curFeedback.length; j++)
				{
					word += " " + curFeedback[j]; 
				}
				sentence += "<U>" + word + "</U><sup><small>" + curFeedback[1] + "</small></sup> "; 
				i++; 
				if(i < feedbacks.length)
				{
					curFeedback = feedbacks[i].split(" "); 
				}
			}
			
			document.getElementById("inputWord").innerHTML = "<b>Your input is: </b>" + sentence;
		}
		if(curFeedback[0] == "#deleted")
		{
			var result = ""; 
			while(i < feedbacks.length && curFeedback[0] == "#deleted")
			{
				var word = curFeedback[2]; 
				for(var j = 3; j < curFeedback.length; j++)
				{
					word += " " + curFeedback[j]; 
				}
				result += "<U>" + word + "</U><sup><small>" + curFeedback[1] + "</small></sup> "; 
				i++; 
				if(i < feedbacks.length)
				{
					curFeedback = feedbacks[i].split(" "); 
				}
			}

			if(result.length > 0)
			{
				document.getElementById("useless").innerHTML = "These words are <b>not</b> considered directly useful: " + result;
			}
		}
		if(curFeedback[0] == "#map" || curFeedback[0] == "#mapNum")
		{
			var results = "";
			while(i < feedbacks.length && (curFeedback[0] == "#map" || curFeedback[0] == "#mapNum"))
			{
				var result = ""; 
				choices = feedbacks[i].split("; "); 					
				
				if(choices.length > 5 || choices[parseInt(choices[3])+4].split("#").length > 3 || curFeedback[0] == "#mapNum")
				{
					result += choices[1] + "<sup><small>" + choices[2] + "</small></sup>" + " <b>maps to</b> " 
						+ "<select class = 'map' id = \"" + choices[2] + "\">"; 
				
					var selected = parseInt(choices[3])+4; 
					
					var stringName = ""; 
					if(curFeedback[0] == "#mapNum")
					{
						if(selected == 3)
						{
							result += "<option value = \"" + -1 + "\" selected = \"selected\">" + "none of the above (just a number)" + "</option>";
						}
						else
						{
							result += "<option value = \"" + -1 + "\">" + "none of the above (just a number)" + "</option>";							
						}
					}
					for(var j = 4; j < choices.length; j++)
					{
						if(j == selected)
						{
							stringName = choices[j].split("#")[0]; 
							result += "<option value = \"" + (j-4).toString() + "\" selected = \"selected\">" + stringName + "</option>"; 
						}
						else
						{
							result += "<option value = \"" + (j-4).toString() + "\">" + choices[j] + "</option>"; 
						}
					}
					result += "</select>"; 

					if(choices[selected].split("#").length > 1)
					{
						var specifies = choices[selected].split("#"); 
						if(specifies.length > 2)
						{
							result += " <b>specifically</b> " + "<select class = 'specify' id = \"" + choices[2] + "\">"; 

							var selected = parseInt(specifies[1]); 
							if(specifies.length > 2)
							{
								if(selected == -1)
								{
									result += "<option value = \"" + -1 + "\" selected = \"selected\">" + "any " + stringName + " containing \"" + choices[1] + "\"</option>"; 
								}
								else
								{
									result += "<option value = \"" + -1 + "\">" + "any " + stringName + " containing " + choices[1] + "</option>"; 
								}
							}
							for(var j = 2; j < specifies.length; j++)
							{
								if(j == selected+2)
								{
									result += "<option value = \"" + (j-2).toString() + "\" selected = \"selected\">" + specifies[j] + "</option>"; 
								}
								else
								{
									result += "<option value = \"" + (j-2).toString() + "\">" + specifies[j] + "</option>"; 
								}
							}
							result += "</select>"; 					
						}
					}
					result += "<br>"; 
					results += result; 
				}
				i++; 
				if(i < feedbacks.length)
				{
					curFeedback = feedbacks[i].split(" "); 
				}
			}
			if(results.length > 0)
			{
				document.getElementById("map").innerHTML = results;
			}
		}
		else if(curFeedback[0] == "#general")
		{
			var result = "";
			var selected = curFeedback[1]; 
			i++; 
			var num = 0; 
			curFeedback = feedbacks[i].split(" "); 

			result += "Possible <b>approximate interpretations</b>: <br>"; 
			result += "<select id = \"setGeneralIntepretation\">"; 
			while(i < feedbacks.length && curFeedback[0] == "#general")
			{
				var intepret = ""; 
				for(var j = 1; j < curFeedback.length; j++)
				{
					intepret += curFeedback[j] + " "; 
				}
				if(num == selected)
				{
					result += "<option value = \"" + num + "\" selected = \"selected\">" + intepret + "</option>"; 
				}
				else
				{
					result += "<option value = \"" + num + "\">" + intepret + "</option>"; 
				}
				i++;
				num++; 
				curFeedback = feedbacks[i].split(" "); 
			}
			result += "</select>"; 

			if(result.length > 1)
			{
				document.getElementById("generalIntepretation").innerHTML = result;
			}
		}
 		else if(curFeedback[0] == "#implicit" || curFeedback[0] == "#explicit")
		{
			var result = ""; 			
			var num = 0; 
			result += "<b>Exact interpretation</b>: <br><div class=\"vR\"><div class=\"vT\">&nbsp;</div></div>"; 
			
			while(i < feedbacks.length && (curFeedback[0] == "#implicit" || curFeedback[0] == "#explicit"))
			{
				var word = feedbacks[i].substring(10); 
				
				if(curFeedback[0] == "#explicit")
				{
					result += "<div class=\"vR\"><div class=\"vT\">" + word + "</div></div>"; 
				}
				else
				{
					result += "<div class=\"vR\"><span class=\"vN\" num = \"" + num + "\"><div class=\"vT\">" + word + "</div><div class=\"vM\"></div></span></div>"; 
				}
				i++; 
				num++; 
				curFeedback = feedbacks[i].split(" "); 
			}
			
//			i--; 
			if(result.length > 1)
			{
				document.getElementById("specificIntepretation").innerHTML = result;
			}
		}

		else if(curFeedback[0] == "#result")
		{
			var result = "<b>Results: </b><br>"	+ "<table border = '1' style=\"margin-left:0.5em\">"; 
			var rowNum = -1; 
			
			rowNum++; 
			var line = feedbacks[i].substring(7); 
			var row = line.split("###"); 
			result += "<tr>"; 
			for(var j = 1; j < row.length; j++)
			{
				result += "<td style = \"font-size:14px\"><b>" + row[j] + ": </b></td>"; 
			}
			result += "</tr>"; 
			i++; 
			curFeedback = feedbacks[i].split(" "); 

			while(i < feedbacks.length && curFeedback[0] == "#result")
			{
				rowNum++; 
				var line = feedbacks[i].substring(7); 
				var row = line.split("###"); 
				result += "<tr>"; 
				for(var j = 1; j < row.length; j++)
				{
					result += "<td style = \"font-size:13px\">" + row[j] + "</td>"; 
				}
				result += "</tr>"; 
				i++; 
				curFeedback = feedbacks[i].split(" "); 
			}
			result += "</table>"; 
			if(rowNum > 0)
			{
				document.getElementById("queryResults").innerHTML = result;
			}
			else
			{
				document.getElementById("queryResults").innerHTML = "<b>Result is Empty</b>";
			}
		}
/*
		else if(curFeedback[0] == "feedback")
		{
			var results = ""; 
			while(i < feedbacks.length && curFeedback[0] == "feedback")
			{
				results += feedbacks[i]; 
				i++; 
				if(i < feedbacks.length)
				{
					curFeedback = feedbacks[i].split(" "); 
				}
			}
			if(results.length > 0)
			{
				document.getElementById("feedbacks").innerHTML = results;
			}
		}
*/
		else
		{
			i++; 
		}
	}
	
	$(".map").change(function()
	{
		$.post("conductCommand.jsp",
		{
			command: "#mapSchema " + $(this).attr("id") + " " + $(this).val()
		},
		function(data,status)
		{
			feedback(data); 
		});
	});	
	$(".specify").change(function()
	{
		$.post("conductCommand.jsp",
		{
			command: "#mapValue " + $(this).attr("id") + " " + $(this).val()
		},
		function(data,status)
		{
			feedback(data); 
		});
	});
	$("#setGeneralIntepretation").change(function()
	{
		$.post("conductCommand.jsp",
		{
			command: "#general " + $(this).val()
		},
		function(data,status)
		{
			feedback(data); 
		});
	});
	$(".vN").click(function()
	{
		$.post("conductCommand.jsp",
		{
			command: "#specific " + $(this).attr("num")
		},
		function(data,status)
		{
			feedback(data); 
		});
	});

/*
	$("#setSpecificIntepretation").change(function()
	{
		$.post("conductCommand.jsp",
		{
			command: "specificIntepretation " + $(this).val()
		},
		function(data,status)
		{
			feedback(data); 
		});
	});
*/
}
</script>

<%
CommandInterface system = new CommandInterface();  
session.setAttribute("system", system);  
%>
</head>

<body>
<h2><b><ins>Na</ins>tural <ins>L</ins>anguage <ins>I</ins>nterface over <ins>R</ins>elational Databases</b></h2>

<p>Use 
<select style = "font-size:13px" id = "database">
<option value = "MAS" selected = "selected">Microsoft Academic Search</option> 
<!-- 
<option value = "YahooMovie">Yahoo!Movie</option> 
 -->
<option value = "dblp">DBLP</option> 
</select>
database.</p>

<b>Choose an Existing Query or type in a New Query: </b><br>
<input id = "inputSentence" list = "browsers" size="120" name="browser" style = "font-size:13px">			
<div id = "inputBlock" style="float:left"></div>
<button type ="button" id = "submitQuery">Submit</button>

<p id = "queryResults"></p>
<p id = "inputWord"></p>
<p id = "useless"></p>
<p id = "map"></p>
<p id = "generalIntepretation"></p>
<p id = "specificIntepretation"></p>

</body>
</html>