package api.alimentos.initializer;

import api.alimentos.model.Alimento;
import api.alimentos.repository.AlimentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeder de alimentos.
 * Carga automáticamente 100+ alimentos base en la tabla al iniciar la aplicación.
 * Solo ejecuta si la tabla está vacía (idempotente).
 * 
 * Categorías de alimentos cargados:
 * - Proteínas animales (carnes, pescados, mariscos, huevos)
 * - Carbohidratos (cereales, tubérculos, legumbres)
 * - Verduras (30+ variedades)
 * - Frutas (30+ variedades)
 * - Lácteos (leches, yogures, quesos)
 * - Grasas y frutos secos (15+ tipos)
 * - Complementos y suplementos
 * - Condimentos y salsas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlimentoSeeder {

    private final AlimentoRepository alimentoRepository;

    /**
     * Se ejecuta automáticamente cuando la aplicación Spring está lista.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedAlimentos() {
        // Verificar si ya hay alimentos en la tabla
        if (alimentoRepository.count() > 0) {
            log.info("Tabla de alimentos ya poblada. Seeder omitido.");
            return;
        }

        log.info("Iniciando seeder de alimentos...");
        List<Alimento> alimentos = crearAlimentosBase();
        alimentoRepository.saveAll(alimentos);
        log.info("Seeder completado: {} alimentos cargados", alimentos.size());
    }

    /**
     * Crea la lista de alimentos base para nutrición deportiva.
     * Incluye 100+ alimentos habituales en planes nutricionales.
     */
    private List<Alimento> crearAlimentosBase() {
        List<Alimento> alimentos = new ArrayList<>();

        // ============ PROTEÍNAS ANIMALES ============
        alimentos.add(crearAlimento("Pechuga de pollo", "", bd(165), bd(31.0), bd(0.0), bd(3.6)));
        alimentos.add(crearAlimento("Muslo de pollo", "", bd(209), bd(26.0), bd(0.0), bd(11.0)));
        alimentos.add(crearAlimento("Alas de pollo", "", bd(203), bd(25.5), bd(0.0), bd(10.5)));
        alimentos.add(crearAlimento("Pavo pechuga", "", bd(135), bd(29.0), bd(0.0), bd(1.7)));
        alimentos.add(crearAlimento("Pavo molido", "", bd(165), bd(28.0), bd(0.0), bd(5.6)));
        alimentos.add(crearAlimento("Conejo", "", bd(136), bd(21.0), bd(0.0), bd(5.4)));
        alimentos.add(crearAlimento("Salmón", "", bd(208), bd(20.0), bd(0.0), bd(13.0)));
        alimentos.add(crearAlimento("Trucha", "", bd(148), bd(20.2), bd(0.0), bd(7.0)));
        alimentos.add(crearAlimento("Bacalao", "", bd(82), bd(17.9), bd(0.0), bd(0.7)));
        alimentos.add(crearAlimento("Atún al natural", "", bd(116), bd(25.5), bd(0.0), bd(0.9)));
        alimentos.add(crearAlimento("Atún enlatado", "", bd(132), bd(29.1), bd(0.0), bd(0.6)));
        alimentos.add(crearAlimento("Sardinas", "", bd(208), bd(25.0), bd(0.0), bd(11.0)));
        alimentos.add(crearAlimento("Merluza", "", bd(82), bd(17.5), bd(0.0), bd(1.2)));
        alimentos.add(crearAlimento("Jurel", "", bd(99), bd(21.0), bd(0.0), bd(0.8)));
        alimentos.add(crearAlimento("Dorada", "", bd(100), bd(19.5), bd(0.0), bd(2.2)));
        alimentos.add(crearAlimento("Gambas", "", bd(85), bd(18.0), bd(0.0), bd(1.0)));
        alimentos.add(crearAlimento("Camarones", "", bd(71), bd(14.3), bd(0.0), bd(0.8)));
        alimentos.add(crearAlimento("Mejillones", "", bd(72), bd(11.9), bd(3.7), bd(1.3)));
        alimentos.add(crearAlimento("Almeja", "", bd(86), bd(14.9), bd(3.1), bd(1.0)));
        alimentos.add(crearAlimento("Calamar", "", bd(92), bd(15.6), bd(3.1), bd(1.4)));
        alimentos.add(crearAlimento("Pulpo", "", bd(82), bd(14.9), bd(0.9), bd(1.0)));
        alimentos.add(crearAlimento("Huevo entero", "", bd(143), bd(12.6), bd(0.7), bd(9.5)));
        alimentos.add(crearAlimento("Clara de huevo", "", bd(52), bd(10.9), bd(0.7), bd(0.2)));
        alimentos.add(crearAlimento("Yema de huevo", "", bd(322), bd(16.1), bd(0.4), bd(26.5)));
        alimentos.add(crearAlimento("Ternera magra", "", bd(158), bd(26.0), bd(0.0), bd(5.5)));
        alimentos.add(crearAlimento("Carne molida (10%)", "", bd(166), bd(26.0), bd(0.0), bd(7.0)));
        alimentos.add(crearAlimento("Carne molida (5%)", "", bd(131), bd(26.0), bd(0.0), bd(2.0)));
        alimentos.add(crearAlimento("Solomillo de ternera", "", bd(161), bd(25.0), bd(0.0), bd(6.5)));
        alimentos.add(crearAlimento("Lomo de cerdo", "", bd(159), bd(27.3), bd(0.0), bd(5.5)));
        alimentos.add(crearAlimento("Jamón serrano", "", bd(150), bd(29.0), bd(0.0), bd(3.5)));
        alimentos.add(crearAlimento("Pechuga de pato", "", bd(213), bd(23.5), bd(0.0), bd(12.5)));

        // ============ CARBOHIDRATOS - CEREALES ============
        alimentos.add(crearAlimento("Arroz blanco cocido", "", bd(130), bd(2.7), bd(28.2), bd(0.3)));
        alimentos.add(crearAlimento("Arroz integral cocido", "", bd(112), bd(2.6), bd(23.5), bd(0.9)));
        alimentos.add(crearAlimento("Arroz basmati cocido", "", bd(130), bd(2.7), bd(28.0), bd(0.3)));
        alimentos.add(crearAlimento("Arroz salvaje cocido", "", bd(101), bd(3.9), bd(21.0), bd(0.3)));
        alimentos.add(crearAlimento("Pasta blanca cocida", "", bd(131), bd(5.0), bd(25.0), bd(1.1)));
        alimentos.add(crearAlimento("Pasta integral cocida", "", bd(124), bd(6.0), bd(23.0), bd(0.9)));
        alimentos.add(crearAlimento("Pan blanco", "", bd(265), bd(9.0), bd(49.0), bd(3.3)));
        alimentos.add(crearAlimento("Pan integral", "", bd(247), bd(8.5), bd(41.3), bd(3.4)));
        alimentos.add(crearAlimento("Pan de molde blanco", "", bd(265), bd(8.0), bd(49.0), bd(3.2)));
        alimentos.add(crearAlimento("Pan de molde integral", "", bd(250), bd(8.5), bd(45.0), bd(3.5)));
        alimentos.add(crearAlimento("Pan de centeno", "", bd(259), bd(8.5), bd(48.0), bd(2.0)));
        alimentos.add(crearAlimento("Avena", "", bd(366), bd(13.2), bd(58.7), bd(6.9)));
        alimentos.add(crearAlimento("Copos de avena", "", bd(368), bd(13.7), bd(59.0), bd(6.9)));
        alimentos.add(crearAlimento("Harina de avena", "", bd(389), bd(16.7), bd(66.3), bd(8.7)));
        alimentos.add(crearAlimento("Muesli", "", bd(380), bd(12.0), bd(63.0), bd(8.0)));
        alimentos.add(crearAlimento("Cereales integrales", "", bd(358), bd(11.0), bd(62.0), bd(7.0)));
        alimentos.add(crearAlimento("Cuscús cocido", "", bd(112), bd(3.8), bd(23.0), bd(0.2)));
        alimentos.add(crearAlimento("Polenta cocida", "", bd(68), bd(1.4), bd(14.0), bd(0.6)));
        alimentos.add(crearAlimento("Maíz cocido", "", bd(124), bd(3.6), bd(26.0), bd(1.7)));

        // ============ CARBOHIDRATOS - TUBÉRCULOS Y LEGUMBRES ============
        alimentos.add(crearAlimento("Patata cocida", "", bd(86), bd(2.0), bd(20.1), bd(0.1)));
        alimentos.add(crearAlimento("Patata al horno", "", bd(93), bd(2.1), bd(21.0), bd(0.2)));
        alimentos.add(crearAlimento("Patata frita", "", bd(312), bd(3.4), bd(41.0), bd(15.0)));
        alimentos.add(crearAlimento("Boniato cocido", "", bd(86), bd(1.6), bd(20.1), bd(0.1)));
        alimentos.add(crearAlimento("Ñame", "", bd(70), bd(1.5), bd(17.0), bd(0.1)));
        alimentos.add(crearAlimento("Plátano verde cocido", "", bd(116), bd(1.3), bd(28.0), bd(0.3)));
        alimentos.add(crearAlimento("Garbanzos cocidos", "", bd(164), bd(8.9), bd(27.4), bd(2.6)));
        alimentos.add(crearAlimento("Lentejas cocidas", "", bd(116), bd(9.0), bd(20.1), bd(0.4)));
        alimentos.add(crearAlimento("Judías blancas cocidas", "", bd(127), bd(8.6), bd(23.0), bd(0.4)));
        alimentos.add(crearAlimento("Judías rojas cocidas", "", bd(127), bd(8.7), bd(23.0), bd(0.5)));
        alimentos.add(crearAlimento("Habas cocidas", "", bd(110), bd(8.0), bd(19.0), bd(0.4)));
        alimentos.add(crearAlimento("Guisantes cocidos", "", bd(84), bd(5.4), bd(14.0), bd(0.4)));

        // ============ VERDURAS ============
        alimentos.add(crearAlimento("Brócoli", "", bd(34), bd(2.8), bd(6.6), bd(0.4)));
        alimentos.add(crearAlimento("Coliflor", "", bd(25), bd(1.9), bd(4.9), bd(0.3)));
        alimentos.add(crearAlimento("Col", "", bd(25), bd(1.3), bd(5.8), bd(0.1)));
        alimentos.add(crearAlimento("Col rizada", "", bd(49), bd(4.3), bd(8.8), bd(0.9)));
        alimentos.add(crearAlimento("Espinacas", "", bd(23), bd(2.9), bd(3.6), bd(0.4)));
        alimentos.add(crearAlimento("Acelgas", "", bd(19), bd(1.8), bd(3.7), bd(0.2)));
        alimentos.add(crearAlimento("Lechuga romana", "", bd(15), bd(1.2), bd(2.9), bd(0.3)));
        alimentos.add(crearAlimento("Lechuga iceberg", "", bd(14), bd(0.9), bd(2.9), bd(0.2)));
        alimentos.add(crearAlimento("Tomate", "", bd(18), bd(0.9), bd(3.9), bd(0.2)));
        alimentos.add(crearAlimento("Tomate cherry", "", bd(27), bd(1.2), bd(5.8), bd(0.3)));
        alimentos.add(crearAlimento("Pepino", "", bd(16), bd(0.7), bd(3.6), bd(0.1)));
        alimentos.add(crearAlimento("Calabacín", "", bd(17), bd(1.6), bd(3.1), bd(0.4)));
        alimentos.add(crearAlimento("Zanahoria", "", bd(41), bd(0.9), bd(9.6), bd(0.2)));
        alimentos.add(crearAlimento("Zanahoria cruda", "", bd(41), bd(0.9), bd(10.0), bd(0.2)));
        alimentos.add(crearAlimento("Pimiento rojo", "", bd(31), bd(1.0), bd(6.0), bd(0.3)));
        alimentos.add(crearAlimento("Pimiento verde", "", bd(30), bd(1.0), bd(7.0), bd(0.3)));
        alimentos.add(crearAlimento("Pimiento amarillo", "", bd(27), bd(1.0), bd(6.2), bd(0.3)));
        alimentos.add(crearAlimento("Berenjena", "", bd(25), bd(0.98), bd(5.9), bd(0.2)));
        alimentos.add(crearAlimento("Cebolla", "", bd(40), bd(1.1), bd(9.3), bd(0.1)));
        alimentos.add(crearAlimento("Ajo", "", bd(149), bd(6.4), bd(33.0), bd(0.5)));
        alimentos.add(crearAlimento("Puerro", "", bd(31), bd(2.1), bd(7.0), bd(0.3)));
        alimentos.add(crearAlimento("Champión", "", bd(22), bd(3.1), bd(3.3), bd(0.3)));
        alimentos.add(crearAlimento("Espárragos", "", bd(20), bd(2.2), bd(3.7), bd(0.1)));
        alimentos.add(crearAlimento("Alcachofas", "", bd(47), bd(3.3), bd(10.0), bd(0.2)));
        alimentos.add(crearAlimento("Remolacha", "", bd(43), bd(1.6), bd(9.6), bd(0.2)));
        alimentos.add(crearAlimento("Nabo", "", bd(36), bd(1.1), bd(8.1), bd(0.1)));
        alimentos.add(crearAlimento("Rábanos", "", bd(16), bd(0.7), bd(3.4), bd(0.1)));
        alimentos.add(crearAlimento("Judías verdes", "", bd(31), bd(1.8), bd(7.0), bd(0.2)));

        // ============ FRUTAS ============
        alimentos.add(crearAlimento("Plátano", "", bd(89), bd(1.1), bd(22.8), bd(0.3)));
        alimentos.add(crearAlimento("Manzana roja", "", bd(52), bd(0.3), bd(13.8), bd(0.2)));
        alimentos.add(crearAlimento("Manzana verde", "", bd(45), bd(0.4), bd(11.4), bd(0.2)));
        alimentos.add(crearAlimento("Pera", "", bd(57), bd(0.4), bd(15.0), bd(0.1)));
        alimentos.add(crearAlimento("Naranja", "", bd(47), bd(0.9), bd(11.8), bd(0.1)));
        alimentos.add(crearAlimento("Mandarina", "", bd(47), bd(0.7), bd(12.0), bd(0.3)));
        alimentos.add(crearAlimento("Limón", "", bd(29), bd(1.1), bd(9.3), bd(0.3)));
        alimentos.add(crearAlimento("Pomelo rosa", "", bd(42), bd(0.8), bd(11.0), bd(0.1)));
        alimentos.add(crearAlimento("Fresas", "", bd(32), bd(0.7), bd(7.7), bd(0.3)));
        alimentos.add(crearAlimento("Arándanos", "", bd(57), bd(0.7), bd(14.5), bd(0.3)));
        alimentos.add(crearAlimento("Frambuesas", "", bd(52), bd(1.2), bd(12.0), bd(0.7)));
        alimentos.add(crearAlimento("Moras", "", bd(44), bd(1.4), bd(10.0), bd(0.4)));
        alimentos.add(crearAlimento("Melón", "", bd(34), bd(0.8), bd(8.1), bd(0.2)));
        alimentos.add(crearAlimento("Sandía", "", bd(30), bd(0.6), bd(7.6), bd(0.2)));
        alimentos.add(crearAlimento("Piña", "", bd(50), bd(0.5), bd(13.1), bd(0.1)));
        alimentos.add(crearAlimento("Mango", "", bd(60), bd(0.7), bd(15.0), bd(0.4)));
        alimentos.add(crearAlimento("Papaya", "", bd(43), bd(0.5), bd(11.0), bd(0.3)));
        alimentos.add(crearAlimento("Kiwi", "", bd(61), bd(1.1), bd(14.7), bd(0.5)));
        alimentos.add(crearAlimento("Uva blanca", "", bd(67), bd(0.7), bd(17.0), bd(0.2)));
        alimentos.add(crearAlimento("Uva roja", "", bd(67), bd(0.7), bd(17.0), bd(0.3)));
        alimentos.add(crearAlimento("Higo", "", bd(74), bd(0.8), bd(19.2), bd(0.3)));
        alimentos.add(crearAlimento("Cereza", "", bd(63), bd(1.1), bd(16.0), bd(0.3)));
        alimentos.add(crearAlimento("Melocotón", "", bd(39), bd(0.9), bd(9.5), bd(0.3)));
        alimentos.add(crearAlimento("Albaricoque", "", bd(48), bd(1.4), bd(11.1), bd(0.4)));
        alimentos.add(crearAlimento("Ciruela", "", bd(46), bd(0.7), bd(11.4), bd(0.3)));
        alimentos.add(crearAlimento("Caqui", "", bd(70), bd(0.6), bd(18.0), bd(0.2)));
        alimentos.add(crearAlimento("Granada", "", bd(83), bd(1.7), bd(19.0), bd(0.6)));
        alimentos.add(crearAlimento("Dátil", "", bd(282), bd(2.7), bd(75.0), bd(0.3)));
        alimentos.add(crearAlimento("Coco fresco", "", bd(354), bd(3.3), bd(9.4), bd(35.0)));

        // ============ LÁCTEOS ============
        alimentos.add(crearAlimento("Leche entera", "", bd(61), bd(3.2), bd(4.8), bd(3.3)));
        alimentos.add(crearAlimento("Leche semidesnatada", "", bd(49), bd(3.3), bd(4.9), bd(1.5)));
        alimentos.add(crearAlimento("Leche desnatada", "", bd(35), bd(3.4), bd(5.0), bd(0.1)));
        alimentos.add(crearAlimento("Leche de cabra", "", bd(66), bd(3.6), bd(4.3), bd(4.5)));
        alimentos.add(crearAlimento("Leche de oveja", "", bd(95), bd(5.4), bd(4.8), bd(6.2)));
        alimentos.add(crearAlimento("Leche de almendras", "", bd(30), bd(1.0), bd(1.3), bd(2.5)));
        alimentos.add(crearAlimento("Leche de soja", "", bd(49), bd(3.3), bd(2.0), bd(1.9)));
        alimentos.add(crearAlimento("Leche de coco", "", bd(230), bd(2.3), bd(9.0), bd(21.0)));
        alimentos.add(crearAlimento("Leche de avena", "", bd(46), bd(1.0), bd(8.0), bd(1.5)));
        alimentos.add(crearAlimento("Yogur natural", "", bd(59), bd(3.5), bd(3.2), bd(3.3)));
        alimentos.add(crearAlimento("Yogur griego natural", "", bd(97), bd(9.0), bd(3.6), bd(5.0)));
        alimentos.add(crearAlimento("Yogur desnatado", "", bd(40), bd(3.8), bd(3.3), bd(0.2)));
        alimentos.add(crearAlimento("Kéfir", "", bd(60), bd(3.4), bd(3.8), bd(3.4)));
        alimentos.add(crearAlimento("Queso cottage", "", bd(98), bd(11.1), bd(3.4), bd(4.3)));
        alimentos.add(crearAlimento("Queso fresco", "", bd(74), bd(7.3), bd(2.7), bd(3.2)));
        alimentos.add(crearAlimento("Queso de cabra", "", bd(98), bd(21.0), bd(0.5), bd(8.0)));
        alimentos.add(crearAlimento("Queso Cheddar", "", bd(403), bd(25.4), bd(1.3), bd(33.0)));
        alimentos.add(crearAlimento("Queso Mozzarella", "", bd(280), bd(28.0), bd(3.1), bd(17.0)));
        alimentos.add(crearAlimento("Queso Feta", "", bd(264), bd(21.3), bd(3.6), bd(21.3)));
        alimentos.add(crearAlimento("Queso Parmesano", "", bd(431), bd(38.0), bd(4.1), bd(29.0)));
        alimentos.add(crearAlimento("Requesón", "", bd(74), bd(10.0), bd(4.0), bd(1.7)));
        alimentos.add(crearAlimento("Ricotta", "", bd(174), bd(12.0), bd(3.0), bd(13.0)));
        alimentos.add(crearAlimento("Mantequilla", "", bd(717), bd(0.9), bd(0.1), bd(81.0)));
        alimentos.add(crearAlimento("Crema agria", "", bd(193), bd(2.7), bd(3.2), bd(19.3)));

        // ============ GRASAS Y FRUTOS SECOS ============
        alimentos.add(crearAlimento("Aguacate", "", bd(160), bd(2.0), bd(8.5), bd(14.7)));
        alimentos.add(crearAlimento("Aceite de oliva", "", bd(884), bd(0.0), bd(0.0), bd(100.0)));
        alimentos.add(crearAlimento("Aceite de girasol", "", bd(884), bd(0.0), bd(0.0), bd(100.0)));
        alimentos.add(crearAlimento("Aceite de coco", "", bd(892), bd(0.0), bd(0.0), bd(99.0)));
        alimentos.add(crearAlimento("Aceite de canola", "", bd(884), bd(0.0), bd(0.0), bd(100.0)));
        alimentos.add(crearAlimento("Almendras", "", bd(579), bd(21.2), bd(21.6), bd(49.9)));
        alimentos.add(crearAlimento("Nueces", "", bd(654), bd(15.2), bd(13.7), bd(65.2)));
        alimentos.add(crearAlimento("Avellanas", "", bd(628), bd(14.9), bd(16.7), bd(60.8)));
        alimentos.add(crearAlimento("Pistachos", "", bd(560), bd(20.3), bd(27.7), bd(45.3)));
        alimentos.add(crearAlimento("Cacahuetes", "", bd(567), bd(25.8), bd(16.1), bd(49.2)));
        alimentos.add(crearAlimento("Mantequilla de cacahuete", "", bd(588), bd(25.1), bd(20.0), bd(50.4)));
        alimentos.add(crearAlimento("Mantequilla de almendras", "", bd(614), bd(23.0), bd(20.0), bd(54.0)));
        alimentos.add(crearAlimento("Semillas de girasol", "", bd(584), bd(20.8), bd(20.0), bd(51.5)));
        alimentos.add(crearAlimento("Semillas de lino", "", bd(534), bd(18.3), bd(28.9), bd(42.2)));
        alimentos.add(crearAlimento("Semillas de chía", "", bd(486), bd(16.5), bd(42.1), bd(30.7)));
        alimentos.add(crearAlimento("Semillas de calabaza", "", bd(559), bd(25.2), bd(4.7), bd(49.0)));
        alimentos.add(crearAlimento("Coco desecado", "", bd(660), bd(7.3), bd(24.2), bd(64.5)));

        // ============ COMPLEMENTOS Y SUPLEMENTOS ============
        alimentos.add(crearAlimento("Proteína whey (polvo)", "", bd(370), bd(75.0), bd(8.0), bd(4.0)));
        alimentos.add(crearAlimento("Proteína caseína (polvo)", "", bd(380), bd(80.0), bd(5.0), bd(3.0)));
        alimentos.add(crearAlimento("Proteína vegana (polvo)", "", bd(350), bd(70.0), bd(15.0), bd(5.0)));
        alimentos.add(crearAlimento("Tortita de arroz", "", bd(387), bd(8.0), bd(80.0), bd(3.0)));
        alimentos.add(crearAlimento("Barrita energética", "", bd(420), bd(10.0), bd(55.0), bd(17.0)));
        alimentos.add(crearAlimento("Miel", "", bd(304), bd(0.3), bd(82.4), bd(0.0)));
        alimentos.add(crearAlimento("Melaza", "", bd(290), bd(0.0), bd(75.0), bd(0.0)));
        alimentos.add(crearAlimento("Levadura de cerveza", "", bd(80), bd(15.5), bd(5.5), bd(0.2)));
        alimentos.add(crearAlimento("Germen de trigo", "", bd(360), bd(23.0), bd(49.0), bd(10.0)));
        alimentos.add(crearAlimento("Salvado de avena", "", bd(246), bd(17.0), bd(66.0), bd(4.0)));

        // ============ CONDIMENTOS Y SALSAS ============
        alimentos.add(crearAlimento("Salsa de tomate", "", bd(17), bd(0.8), bd(3.9), bd(0.2)));
        alimentos.add(crearAlimento("Salsa de soja", "", bd(61), bd(8.5), bd(5.6), bd(0.6)));
        alimentos.add(crearAlimento("Mostaza", "", bd(66), bd(3.6), bd(6.4), bd(3.6)));
        alimentos.add(crearAlimento("Mayonesa", "", bd(680), bd(0.4), bd(0.6), bd(75.0)));
        alimentos.add(crearAlimento("Salsa de yogur", "", bd(59), bd(3.5), bd(3.2), bd(3.3)));

        return alimentos;
    }

    /**
     * Crea una instancia de Alimento con los valores especificados.
     */
    private Alimento crearAlimento(String nombre, String marca, BigDecimal kcal, 
                                   BigDecimal proteinas, BigDecimal carbs, BigDecimal grasas) {
        return Alimento.builder()
                .nombre(nombre)
                .marca(marca.isEmpty() ? null : marca)
                .kcalPor100g(kcal)
                .proteinasPor100g(proteinas)
                .carbsPor100g(carbs)
                .grasasPor100g(grasas)
                .creadoPor(null)  // Los alimentos del seeder no tienen creador
                .build();
    }

    /**
     * Convierte un número a BigDecimal.
     */
    private BigDecimal bd(double value) {
        return new BigDecimal(value);
    }
}
