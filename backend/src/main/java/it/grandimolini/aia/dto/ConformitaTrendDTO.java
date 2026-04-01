package it.grandimolini.aia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConformitaTrendDTO {

    private Integer anno;
    private Integer mese;
    private Long totaleMisurazioni;
    private Long conformi;
    private Long inAttenzione;
    private Long nonConformi;
    private Double percentualeConformita;
}
