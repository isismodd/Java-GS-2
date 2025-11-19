package br.com.fiap.resource;

import br.com.fiap.bo.ColaboradorBO;
import br.com.fiap.to.ColaboradorTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/colaboradores")
public class ColaboradorResource {

    private ColaboradorBO colaboradorBO = new ColaboradorBO();

    /** GET /colaboradores/{id} */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") Long id) {
        ColaboradorTO c = colaboradorBO.findById(id);

        if (c != null) return Response.ok(c).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /** GET /colaboradores */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findAll() {
        List<ColaboradorTO> lista = colaboradorBO.findAll();
        return Response.ok(lista).build();
    }

    /** POST /colaboradores */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response save(@Valid ColaboradorTO colaborador) {
        ColaboradorTO novo = colaboradorBO.save(colaborador);

        if (novo != null && novo.getId() != null)
            return Response.status(Response.Status.CREATED).entity(novo).build();

        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /** PUT /colaboradores/{id} */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@Valid ColaboradorTO colaborador, @PathParam("id") Long id) {
        colaborador.setId(id);
        ColaboradorTO atualizado = colaboradorBO.update(colaborador);

        if (atualizado != null) return Response.ok(atualizado).build();
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /** DELETE /colaboradores/{id} */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        if (colaboradorBO.delete(id))
            return Response.status(Response.Status.NO_CONTENT).build();

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
