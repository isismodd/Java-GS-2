package br.com.fiap;

import br.com.fiap.to.ColaboradorTO;
import br.com.fiap.to.ProjetoTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EquipeResourceTest {

    private static Long projetoIdValido;

    @Test
    @Order(1)
    public void prepararProjetoParaEquipe() {
        ProjetoTO projeto = new ProjetoTO();
        projeto.setNome("Sistema IA");
        projeto.setDescricao("Projeto teste para equipe");
        projeto.setComplexidade(5);
        projeto.setMinMembros(2);
        projeto.setSkillPrincipalRequerida("Java");
        projeto.setQuantidadeIdeal(3);
        projeto.setPersonalidadeDesejada("Analítico");
        projeto.setRequisitos(java.util.Arrays.asList("Java", "Banco de Dados"));

        // CORREÇÃO ANTERIOR (ClassCastException): Cast para Number antes de chamar longValue()
        projetoIdValido =
                ((Number) given()
                        .contentType(ContentType.JSON)
                        .body(projeto)
                        .when()
                        .post("/projetos")
                        .then()
                        .statusCode(201)
                        .body("id", notNullValue())
                        .extract().path("id")).longValue();

        System.out.println("📌 Projeto criado para os testes de equipe: " + projetoIdValido);
    }

    @Test
    @Order(2)
    public void prepararColaboradores() {
        // Usando construtor padrão + setters (compatível com TOs sem construtor customizado)
        ColaboradorTO c1 = new ColaboradorTO();
        c1.setNome("Ana");
        c1.setIdade(25);
        c1.setPersonalidade("Analítico");
        c1.setExperiencia(5);
        c1.setHabilidades(java.util.Arrays.asList("Java", "Banco de Dados"));

        ColaboradorTO c2 = new ColaboradorTO();
        c2.setNome("João");
        c2.setIdade(30);
        c2.setPersonalidade("Analítico");
        c2.setExperiencia(5);
        c2.setHabilidades(java.util.Arrays.asList("Java", "APIs"));

        ColaboradorTO c3 = new ColaboradorTO();
        c3.setNome("Maria");
        c3.setIdade(28);
        c3.setPersonalidade("Criativo");
        c3.setExperiencia(4);
        c3.setHabilidades(java.util.Arrays.asList("Cloud"));

        given().contentType(ContentType.JSON).body(c1).post("/colaboradores").then().statusCode(201);
        given().contentType(ContentType.JSON).body(c2).post("/colaboradores").then().statusCode(201);
        given().contentType(ContentType.JSON).body(c3).post("/colaboradores").then().statusCode(201);
    }

    // ---------- TESTE PRINCIPAL ----------
    @Test
    @Order(3)
    public void test1_formarEquipeComSucesso() {

        System.out.println("Usando projetoId: " + projetoIdValido);

        given()
                .pathParam("idProjeto", projetoIdValido)
                .when()
                // CORREÇÃO AQUI: Adiciona o '/formar' na rota para corresponder ao EquipeResource
                .get("/equipes/formar/{idProjeto}")
                .then()
                .statusCode(200)
                .body("$", not(empty()));
    }

    // ---------- TESTES DE ERRO ----------
    @Test
    @Order(4)
    public void test2_formarEquipeProjetoInexistente() {
        given()
                .pathParam("idProjeto", 99999)
                .when()
                // ATENÇÃO: Adiciona o '/formar' na rota
                .get("/equipes/formar/{idProjeto}")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(5)
    public void test3_formarEquipeColaboradoresInsuficientes() {

        ProjetoTO projeto = new ProjetoTO();
        projeto.setNome("Projeto Sem Gente");
        projeto.setDescricao("Teste erro");
        projeto.setComplexidade(2);
        projeto.setMinMembros(10); // impossível montar equipe com poucos colaboradores
        projeto.setSkillPrincipalRequerida("Python");
        projeto.setQuantidadeIdeal(10);
        projeto.setPersonalidadeDesejada("Criativo");
        projeto.setRequisitos(java.util.Arrays.asList("Python"));

        // CORREÇÃO ANTERIOR (ClassCastException): Cast para Number antes de chamar longValue()
        Long idProj =
                ((Number) given()
                        .contentType(ContentType.JSON)
                        .body(projeto)
                        .when()
                        .post("/projetos")
                        .then()
                        .statusCode(201)
                        .extract().path("id")).longValue();

        given()
                .pathParam("idProjeto", idProj)
                .when()
                // CORREÇÃO AQUI: Adiciona o '/formar' na rota
                .get("/equipes/formar/{idProjeto}")
                .then()
                .statusCode(anyOf(is(400), is(404))); // dependendo da implementação do BO, pode retornar 400 ou 404
    }
}