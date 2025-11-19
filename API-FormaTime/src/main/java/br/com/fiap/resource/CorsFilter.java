package br.com.fiap.resource;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

// A anotação @Provider registra esta classe como um filtro
@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, 
                       ContainerResponseContext responseContext) throws IOException {

        // 1. Permite acesso de qualquer origem (*)
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");

        // 2. Define os métodos HTTP que o cliente pode usar
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        // 3. Define os cabeçalhos que o cliente pode enviar
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // 4. (Opcional) Define quanto tempo a resposta OPTIONS (pré-voo) pode ser armazenada em cache (em segundos)
        responseContext.getHeaders().add("Access-Control-Max-Age", "86400"); 

        // 5. Trata a requisição OPTIONS (Pré-voo)
        // Se a requisição for OPTIONS, garantimos que a resposta 200/204 seja enviada imediatamente.
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            responseContext.setStatus(Response.Status.OK.getStatusCode());
        }
    }
}
