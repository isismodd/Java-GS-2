package br.com.fiap.resource;

import br.com.fiap.bo.EquipeBO;
import br.com.fiap.to.EquipeTO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/equipes")
public class EquipeResource {

    private EquipeBO equipeBO = new EquipeBO();

    /**
     * GET /equipes/formar/{idProjeto}
     */
    @GET
    @Path("/formar/{idProjeto}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response formarEquipe(@PathParam("idProjeto") Long idProjeto) {

        EquipeTO equipe = equipeBO.formarEquipe(idProjeto);

        if (equipe != null) {
            return Response.ok(equipe).build();  // 200 OK
        } else {
            return Response.status(Response.Status.NOT_FOUND).build(); // 404
        }
    }
}
