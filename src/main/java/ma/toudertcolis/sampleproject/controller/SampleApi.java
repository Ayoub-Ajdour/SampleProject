package ma.toudertcolis.sampleproject.controller;

import ma.toudertcolis.sampleproject.entity.sampleEntity;
import ma.toudertcolis.sampleproject.service.SampleSvc;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/samples") // Base API path
public class SampleApi {
    private final SampleSvc sampleSvc;

    public SampleApi(SampleSvc sampleSvc) {
        this.sampleSvc = sampleSvc;
    }

    @GetMapping
    public ResponseEntity<List<sampleEntity>> getAll() {
        return ResponseEntity.ok(sampleSvc.getAll());
    }
}