package ma.toudertcolis.sampleproject.repository;

import ma.toudertcolis.sampleproject.entity.sampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SampleRepo  extends JpaRepository<sampleEntity, Long> {
}
