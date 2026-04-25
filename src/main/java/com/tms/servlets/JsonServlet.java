package com.tms.servlets;

import com.tms.util.JackSonUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class JsonServlet extends HttpServlet {

    public abstract Class getModalClass();
    public abstract void doPost(HttpServletRequest request , HttpServletResponse response);
    public abstract void doGet(HttpServletRequest request , HttpServletResponse response);
    protected void service(HttpServletRequest request , HttpServletResponse response) throws ServletException, IOException {
        String method = request.getMethod();
        if(!method.equals("GET")){
            try{
                BufferedReader reader = request.getReader();
                StringBuilder builder = new StringBuilder();
                String read;
                while((read = reader.readLine()) != null){
                    builder.append(read);
                }
                read = builder.toString();

                Object input = JackSonUtils.deSerialize(read , getModalClass());
                request.setAttribute("INPUT" , input);


            }
            catch (Exception e){
                System.out.println(e);
            }
        }
        super.service(request , response);
        String output = JackSonUtils.serialize(request.getAttribute("OUTPUT"));
        response.setContentType("text/json");
        PrintWriter resOutput = response.getWriter();
        resOutput.println(output);
        resOutput.close();
    }

}
