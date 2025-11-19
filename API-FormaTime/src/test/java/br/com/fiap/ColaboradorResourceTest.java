package br.com.fiap;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ColaboradorResourceTest {

    // ID retornado no POST
    private static Long colaboradorId;

    // JSON de criação
    private static final String NOVO_COLABORADOR_JSON =
            """
            {
              "nome": "Lucas Almeida",
              "idade": 28,
              "personalidade": "Analítico",
              "experiencia": 4,
              "habilidades": ["Java", "SQL", "Spring"]
            }
            """;

    /**
     * TESTE 1: Criar colaborador
     */
    @Test
    @Order(1)
    void test1_postNovoColaborador() {

        colaboradorId = ((Integer)
                given()
                        .contentType("application/json")
                        .body(NOVO_COLABORADOR_JSON)
                        .when()
                        .post("/colaboradores")
                        .then()
                        .statusCode(201)
                        .body("id", notNullValue())
                        .body("nome", is("Lucas Almeida"))
                        .extract().path("id")
        ).longValue();

        System.out.println("✅ Colaborador criado com ID: " + colaboradorId);
    }

    /**
     * TESTE 2: Buscar colaborador por ID
     */
    @Test
    @Order(2)
    void test2_getColaboradorById() {
        if (colaboradorId == null) return;

        given()
                .when()
                .get("/colaboradores/{id}", colaboradorId)
                .then()
                .statusCode(200)
                .body("id", is(colaboradorId.intValue()))
                .body("nome", is("Lucas Almeida"));

        System.out.println("✅ Colaborador encontrado pelo ID: " + colaboradorId);
    }

    /**
     * TESTE 3: Listar todos os colaboradores
     */
    @Test
    @Order(3)
    void test3_getListaColaboradores() {

        given()
                .when()
                .get("/colaboradores")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));

        System.out.println("✅ Lista de colaboradores retornada com sucesso.");
    }

    /**
     * TESTE 4: Atualizar colaborador
     */
    @Test
    @Order(4)
    void test4_putAtualizarColaborador() {

        String updateJson =
                """
                {
                  "id": %d,
                  "nome": "Lucas Silva",
                  "idade": 29,
                  "personalidade": "Lógico",
                  "experiencia": 5,
                  "habilidades": ["Java", "Docker"]
                }
                """.formatted(colaboradorId);

        given()
                .contentType("application/json")
                .body(updateJson)
                .when()
                .put("/colaboradores/{id}", colaboradorId)
                .then()
                .statusCode(200)
                .body("nome", is("Lucas Silva"))
                .body("experiencia", is(5));

        System.out.println("✅ Colaborador atualizado com sucesso.");
    }

    /**
     * TESTE 5: Deletar colaborador
     */
    @Test
    @Order(5)
    void test5_deleteColaborador() {

        given()
                .when()
                .delete("/colaboradores/{id}", colaboradorId)
                .then()
                .statusCode(204);

        // Certifica que foi deletado
        given()
                .when()
                .get("/colaboradores/{id}", colaboradorId)
                .then()
                .statusCode(404);

        System.out.println("✅ Colaborador deletado com sucesso.");
    }
}
