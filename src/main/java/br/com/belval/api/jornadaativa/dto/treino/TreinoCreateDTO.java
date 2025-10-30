package br.com.belval.api.jornadaativa.dto.treino;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreinoCreateDTO {



    @NotBlank @Size(max = 255) private String nome;
    @Size(max = 255) private String descricao;
    @Size(max = 50) private String nivel;
    @jakarta.validation.constraints.Digits(integer = 3, fraction = 2) private java.math.BigDecimal distanciaMinKm;
@jakarta.validation.constraints.Digits(integer = 3, fraction = 2) private java.math.BigDecimal distanciaMaxKm;
@jakarta.validation.constraints.Digits(integer = 4, fraction = 2) private java.math.BigDecimal duracaoAlvoMin;   // em minutos
@jakarta.validation.constraints.Digits(integer = 2, fraction = 2) private java.math.BigDecimal paceAlvoMinpkm; // min/km
}
