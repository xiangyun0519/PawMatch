package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadopt.dto.PetUpdateRequest;
import com.petadopt.entity.AdopterProfile;
import com.petadopt.entity.AdopterEmbedding;
import com.petadopt.entity.PetEmbedding;
import com.petadopt.entity.PetProfile;
import com.petadopt.mapper.AdopterEmbeddingMapper;
import com.petadopt.mapper.PetEmbeddingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorSearchService {

    private final PetEmbeddingMapper petEmbeddingMapper;
    private final AdopterEmbeddingMapper adopterEmbeddingMapper;
    private final EmbeddingService embeddingService;
    private final JdbcTemplate jdbcTemplate;

    @Value("${embedding.model:Qwen/Qwen3-Embedding-8B}")
    private String embeddingModelName;

    public void savePetEmbedding(PetProfile pet) {
        String content = embeddingService.buildPetContent(pet);
        float[] embedding = embeddingService.generateEmbedding(content);
        int dimension = embeddingService.getDimension();

        PetEmbedding existing = petEmbeddingMapper.selectOne(
                new LambdaQueryWrapper<PetEmbedding>()
                        .eq(PetEmbedding::getPetId, pet.getId())
        );

        PetEmbedding petEmbedding = existing != null ? existing : new PetEmbedding();
        petEmbedding.setPetId(pet.getId());
        petEmbedding.setContent(content);
        petEmbedding.setEmbedding(embedding);
        petEmbedding.setModel(embeddingModelName);
        petEmbedding.setDimension(dimension);
        petEmbedding.setCreatedAt(LocalDateTime.now());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("species", pet.getSpecies());
        metadata.put("size", pet.getSize());
        metadata.put("gender", pet.getGender());
        metadata.put("status", pet.getStatus());
        petEmbedding.setMetadata(metadata);

        if (existing != null) {
            petEmbeddingMapper.updateById(petEmbedding);
        } else {
            petEmbeddingMapper.insert(petEmbedding);
        }
    }

    public void saveAdopterEmbedding(AdopterProfile profile) {
        String content = embeddingService.buildAdopterContent(profile);
        float[] embedding = embeddingService.generateEmbedding(content);
        int dimension = embeddingService.getDimension();

        AdopterEmbedding existing = adopterEmbeddingMapper.selectOne(
                new LambdaQueryWrapper<AdopterEmbedding>()
                        .eq(AdopterEmbedding::getAdopterId, profile.getId())
        );

        AdopterEmbedding adopterEmbedding = existing != null ? existing : new AdopterEmbedding();
        adopterEmbedding.setAdopterId(profile.getId());
        adopterEmbedding.setContent(content);
        adopterEmbedding.setEmbedding(embedding);
        adopterEmbedding.setModel(embeddingModelName);
        adopterEmbedding.setDimension(dimension);
        adopterEmbedding.setCreatedAt(LocalDateTime.now());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("housingType", profile.getHousingType());
        metadata.put("activityLevel", profile.getActivityLevel());
        adopterEmbedding.setMetadata(metadata);

        if (existing != null) {
            adopterEmbeddingMapper.updateById(adopterEmbedding);
        } else {
            adopterEmbeddingMapper.insert(adopterEmbedding);
        }
    }

    public List<Map<String, Object>> searchSimilarPets(Long adopterId, int topK) {
        AdopterEmbedding adopterEmbedding = adopterEmbeddingMapper.selectOne(
                new LambdaQueryWrapper<AdopterEmbedding>()
                        .eq(AdopterEmbedding::getAdopterId, adopterId)
        );
        
        if (adopterEmbedding == null) {
            return Collections.emptyList();
        }

        float[] embedding = adopterEmbedding.getEmbedding();
        String vectorStr = arrayToVectorString(embedding);
        
        String sql = "SELECT pe.pet_id, pe.content, 1 - (pe.embedding <=> ?::vector) as similarity " +
                     "FROM pet_embedding pe " +
                     "JOIN pet_profile pp ON pe.pet_id = pp.id " +
                     "WHERE pp.status = 'AVAILABLE' " +
                     "ORDER BY pe.embedding <=> ?::vector " +
                     "LIMIT ?";
        
        return jdbcTemplate.queryForList(sql, vectorStr, vectorStr, topK);
    }

    public List<Map<String, Object>> searchSimilarAdopters(Long petId, int topK) {
        PetEmbedding petEmbedding = petEmbeddingMapper.selectOne(
                new LambdaQueryWrapper<PetEmbedding>()
                        .eq(PetEmbedding::getPetId, petId)
        );
        
        if (petEmbedding == null) {
            return Collections.emptyList();
        }

        float[] embedding = petEmbedding.getEmbedding();
        String vectorStr = arrayToVectorString(embedding);
        
        String sql = "SELECT ae.adopter_id, ae.content, 1 - (ae.embedding <=> ?::vector) as similarity " +
                     "FROM adopter_embedding ae " +
                     "ORDER BY ae.embedding <=> ?::vector " +
                     "LIMIT ?";
        
        return jdbcTemplate.queryForList(sql, vectorStr, vectorStr, topK);
    }

    private String arrayToVectorString(float[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
