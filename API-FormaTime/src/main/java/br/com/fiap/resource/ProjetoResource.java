package br.com.fiap.resource;

import br.com.fiap.bo.ProjetoBO;
import br.com.fiap.to.ProjetoTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/projetos")
public class ProjetoResource {

    private ProjetoBO projetoBO = new ProjetoBO();

    /** GET /projetos/{id} */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") Long id) {
        ProjetoTO projeto = projetoBO.findById(id);

        if (projeto != null) return Response.ok(projeto).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /** GET /projetos */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findAll() {
        List<ProjetoTO> lista = projetoBO.findAll();
        return Response.ok(lista).build();
    }

    /** POST /projetos */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response save(@Valid ProjetoTO projeto) {
        ProjetoTO novo = projetoBO.save(projeto);

        if (novo != null && novo.getId() != null)
            return Response.status(Response.Status.CREATED).entity(novo).build();

        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /** PUT /projetos/{id} */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@Valid ProjetoTO projeto, @PathParam("id") Long id) {
        projeto.setId(id);
        ProjetoTO atualizado = projetoBO.update(projeto);

        if (atualizado != null) return Response.ok(atualizado).build();
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /** DELETE /projetos/{id} */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        if (projetoBO.delete(id))
            return Response.status(Response.Status.NO_CONTENT).build();

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
