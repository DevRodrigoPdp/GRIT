package grit.sistema.backend.dto.login;

public record LoginData(
        String rol,
        String estado,
        String nombre,
        Boolean tituloEntrenamiento,
        Boolean tituloNutricion,
        String servicio
) {

}
