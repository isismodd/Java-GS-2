package br.com.fiap;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjetoResourceTest {

    private static Long projetoId;

    private String jsonValido() {
        return """
        {
          "nome": "Sistema de IA",
          "descricao": "Projeto de análise inteligente.",
          "complexidade": 3,
          "minMembros": 2,
          "skillPrincipalRequerida": "Java",
          "quantidadeIdeal": 4,
          "personalidadeDesejada": "Analítico",
          "requisitos": ["Banco de Dados", "APIs", "Cloud"]
        }
        """;
    }

    private String jsonAtualizado() {
        return """
        {
          "nome": "Sistema de IA 2.0",
          "descricao": "Descrição atualizada",
          "complexidade": 4,
          "minMembros": 2,
          "skillPrincipalRequerida": "Python",
          "quantidadeIdeal": 5,
          "personalidadeDesejada": "Criativo",
          "requisitos": ["Cloud", "Machine Learning"]
        }
        """;
    }

    // ------------------------
    // 1) CREATE
    // ------------------------
    @Test
    @Order(1)
    public void testCreate() {

        projetoId =
                given()
                        .contentType(ContentType.JSON)
                        .body(jsonValido())
                        .when()
                        .post("/projetos")
                        .then()
                        .statusCode(201)
                        .body("id", notNullValue())
                        .extract()
                        .jsonPath().getLong("id");
    }

    // ------------------------
    // 2) GET by ID
    // ------------------------
    @Test
    @Order(2)
    public void testGetById() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/projetos/" + projetoId)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Sistema de IA"));
    }

    // ------------------------
    // 3) UPDATE
    // ------------------------
    @Test
    @Order(3)
    public void testUpdate() {

        given()
                .contentType(ContentType.JSON)
                .body(jsonAtualizado())
                .when()
                .put("/projetos/" + projetoId)
                .then()
                .statusCode(200)
                .body("nome", equalTo("Sistema de IA 2.0"));
    }

    // ------------------------
    // 4) GET ALL
    // ------------------------
    @Test
    @Order(4)
    public void testFindAll() {

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/projetos")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    // ------------------------
    // 5) DELETE
    // ------------------------
    @Test
    @Order(5)
    public void testDelete() {

        given()
                .when()
                .delete("/projetos/" + projetoId)
                .then()
                .statusCode(204);
    }
}
