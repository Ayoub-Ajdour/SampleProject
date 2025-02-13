package ma.toudertcolis.sampleproject.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @ToString
public class sampleEntity {
    @Id
    private int id;
    private String name;
}
