<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="architecture.*" %>
<%String command = request.getParameter("command");
CommandInterface system = (CommandInterface)session.getAttribute("system");  
system.executeCommand(command); %>
<%=system.feedback%>
