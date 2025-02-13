package ma.toudertcolis.sampleproject.service;

import ma.toudertcolis.sampleproject.entity.sampleEntity;
import ma.toudertcolis.sampleproject.repository.SampleRepo;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class SampleSvc {
    private SampleRepo sampleRepo;

    public SampleSvc(SampleRepo sampleRepo) {
        this.sampleRepo = sampleRepo;
    }
    public List<sampleEntity> getAll(){
        return sampleRepo.findAll();
    }
}
