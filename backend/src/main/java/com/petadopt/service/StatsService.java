package com.petadopt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadopt.dto.StatsResponse;
import com.petadopt.entity.AdoptionApplication;
import com.petadopt.entity.MqMessageLog;
import com.petadopt.entity.PetProfile;
import com.petadopt.entity.Shelter;
import com.petadopt.entity.User;
import com.petadopt.mapper.AdoptionApplicationMapper;
import com.petadopt.mapper.MqMessageLogMapper;
import com.petadopt.mapper.PetMapper;
import com.petadopt.mapper.ShelterMapper;
import com.petadopt.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final PetMapper petMapper;
    private final AdoptionApplicationMapper applicationMapper;
    private final ShelterMapper shelterMapper;
    private final UserMapper userMapper;
    private final MqMessageLogMapper mqMessageLogMapper;

    public StatsResponse.ShelterStats getShelterStats(Long shelterId) {
        LambdaQueryWrapper<PetProfile> petWrapper = new LambdaQueryWrapper<>();
        petWrapper.eq(PetProfile::getShelterId, shelterId);
        List<PetProfile> pets = petMapper.selectList(petWrapper);
        
        int totalPets = pets.size();
        int availablePets = (int) pets.stream().filter(p -> "AVAILABLE".equals(p.getStatus())).count();
        
        List<Long> petIds = pets.stream().map(PetProfile::getId).toList();
        
        int pendingApplications = 0;
        int completedAdoptions = 0;
        int monthlyAdoptions = 0;
        
        if (!petIds.isEmpty()) {
            LambdaQueryWrapper<AdoptionApplication> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.in(AdoptionApplication::getPetId, petIds)
                         .eq(AdoptionApplication::getStatus, "PENDING");
            pendingApplications = Math.toIntExact(applicationMapper.selectCount(pendingWrapper));
            
            LambdaQueryWrapper<AdoptionApplication> completedWrapper = new LambdaQueryWrapper<>();
            completedWrapper.in(AdoptionApplication::getPetId, petIds)
                           .eq(AdoptionApplication::getStatus, "COMPLETED");
            completedAdoptions = Math.toIntExact(applicationMapper.selectCount(completedWrapper));
            
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LambdaQueryWrapper<AdoptionApplication> monthlyWrapper = new LambdaQueryWrapper<>();
            monthlyWrapper.in(AdoptionApplication::getPetId, petIds)
                         .eq(AdoptionApplication::getStatus, "COMPLETED")
                         .ge(AdoptionApplication::getCompletedAt, monthStart);
            monthlyAdoptions = Math.toIntExact(applicationMapper.selectCount(monthlyWrapper));
        }
        
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<PetProfile> monthlyPetWrapper = new LambdaQueryWrapper<>();
        monthlyPetWrapper.eq(PetProfile::getShelterId, shelterId)
                        .ge(PetProfile::getCreatedAt, monthStart);
        int monthlyNewPets = Math.toIntExact(petMapper.selectCount(monthlyPetWrapper));
        
        return StatsResponse.ShelterStats.builder()
                .totalPets(totalPets)
                .availablePets(availablePets)
                .pendingApplications(pendingApplications)
                .completedAdoptions(completedAdoptions)
                .monthlyViews(0)
                .monthlyNewPets(monthlyNewPets)
                .monthlyAdoptions(monthlyAdoptions)
                .build();
    }

    public StatsResponse.PlatformStats getPlatformStats() {
        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<>());
        
        LambdaQueryWrapper<User> adopterWrapper = new LambdaQueryWrapper<>();
        adopterWrapper.eq(User::getRole, "ADOPTER");
        long totalAdopters = userMapper.selectCount(adopterWrapper);
        
        LambdaQueryWrapper<User> shelterUserWrapper = new LambdaQueryWrapper<>();
        shelterUserWrapper.eq(User::getRole, "SHELTER");
        long totalShelters = userMapper.selectCount(shelterUserWrapper);
        
        long totalPets = petMapper.selectCount(new LambdaQueryWrapper<>());
        
        long totalApplications = applicationMapper.selectCount(new LambdaQueryWrapper<>());
        
        LambdaQueryWrapper<AdoptionApplication> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(AdoptionApplication::getStatus, "COMPLETED");
        long completedAdoptions = applicationMapper.selectCount(completedWrapper);
        
        LambdaQueryWrapper<AdoptionApplication> scoreWrapper = new LambdaQueryWrapper<>();
        scoreWrapper.isNotNull(AdoptionApplication::getMatchingScore);
        List<AdoptionApplication> applicationsWithScore = applicationMapper.selectList(scoreWrapper);
        
        double avgMatchScore = 0.0;
        if (!applicationsWithScore.isEmpty()) {
            BigDecimal totalScore = applicationsWithScore.stream()
                    .map(AdoptionApplication::getMatchingScore)
                    .filter(s -> s != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgMatchScore = totalScore.divide(BigDecimal.valueOf(applicationsWithScore.size()), 2, RoundingMode.HALF_UP).doubleValue();
        }
        
        return StatsResponse.PlatformStats.builder()
                .totalUsers((int) totalUsers)
                .totalAdopters((int) totalAdopters)
                .totalShelters((int) totalShelters)
                .totalPets((int) totalPets)
                .totalApplications((int) totalApplications)
                .completedAdoptions((int) completedAdoptions)
                .avgMatchScore(avgMatchScore)
                .build();
    }

    public List<StatsResponse.MonthlyStats> getMonthlyTrend(int months) {
        List<StatsResponse.MonthlyStats> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            
            LambdaQueryWrapper<AdoptionApplication> adoptionWrapper = new LambdaQueryWrapper<>();
            adoptionWrapper.eq(AdoptionApplication::getStatus, "COMPLETED")
                          .ge(AdoptionApplication::getCompletedAt, monthStart)
                          .lt(AdoptionApplication::getCompletedAt, monthEnd);
            int adoptions = Math.toIntExact(applicationMapper.selectCount(adoptionWrapper));
            
            LambdaQueryWrapper<AdoptionApplication> applicationWrapper = new LambdaQueryWrapper<>();
            applicationWrapper.ge(AdoptionApplication::getCreatedAt, monthStart)
                             .lt(AdoptionApplication::getCreatedAt, monthEnd);
            int applications = Math.toIntExact(applicationMapper.selectCount(applicationWrapper));
            
            LambdaQueryWrapper<PetProfile> petWrapper = new LambdaQueryWrapper<>();
            petWrapper.ge(PetProfile::getCreatedAt, monthStart)
                     .lt(PetProfile::getCreatedAt, monthEnd);
            int newPets = Math.toIntExact(petMapper.selectCount(petWrapper));
            
            result.add(StatsResponse.MonthlyStats.builder()
                    .month(monthStart.format(formatter))
                    .adoptions(adoptions)
                    .applications(applications)
                    .newPets(newPets)
                    .build());
        }
        
        return result;
    }

    public List<StatsResponse.SpeciesDistribution> getSpeciesDistribution() {
        List<PetProfile> allPets = petMapper.selectList(new LambdaQueryWrapper<>());
        
        Map<String, Integer> speciesCount = new HashMap<>();
        for (PetProfile pet : allPets) {
            String species = pet.getSpecies() != null ? pet.getSpecies() : "其他";
            speciesCount.merge(species, 1, Integer::sum);
        }
        
        int total = allPets.size();
        List<StatsResponse.SpeciesDistribution> result = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : speciesCount.entrySet()) {
            double percentage = total > 0 ? (entry.getValue() * 100.0 / total) : 0;
            result.add(StatsResponse.SpeciesDistribution.builder()
                    .species(entry.getKey())
                    .count(entry.getValue())
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build());
        }
        
        result.sort((a, b) -> b.getCount() - a.getCount());
        return result;
    }

    public StatsResponse getFullStats(Long shelterId) {
        return StatsResponse.builder()
                .shelterStats(shelterId != null ? getShelterStats(shelterId) : null)
                .platformStats(getPlatformStats())
                .monthlyTrend(getMonthlyTrend(6))
                .speciesDistribution(getSpeciesDistribution())
                .build();
    }

    /**
     * 匹配转化漏斗：发起匹配 → 提交申请 → 审核通过 → 完成领养。
     */
    public Map<String, Object> getMatchingFunnel() {
        long matchInitiated = mqMessageLogMapper.selectCount(
                new LambdaQueryWrapper<MqMessageLog>()
                        .eq(MqMessageLog::getBusinessType, "MATCH_RECOMMEND")
                        .eq(MqMessageLog::getStatus, "SUCCESS")
        );

        long applications = applicationMapper.selectCount(new LambdaQueryWrapper<>());
        long approved = applicationMapper.selectCount(
                new LambdaQueryWrapper<AdoptionApplication>().eq(AdoptionApplication::getStatus, "APPROVED"));
        long completed = applicationMapper.selectCount(
                new LambdaQueryWrapper<AdoptionApplication>().eq(AdoptionApplication::getStatus, "COMPLETED"));

        double matchToApply = matchInitiated > 0 ? round2(applications * 100.0 / matchInitiated) : 0;
        double applyToApprove = applications > 0 ? round2(approved * 100.0 / applications) : 0;
        double approveToComplete = approved > 0 ? round2(completed * 100.0 / approved) : 0;
        double overallConversion = matchInitiated > 0 ? round2(completed * 100.0 / matchInitiated) : 0;

        Map<String, Object> stage1 = new HashMap<>();
        stage1.put("name", "发起匹配");
        stage1.put("count", matchInitiated);

        Map<String, Object> stage2 = new HashMap<>();
        stage2.put("name", "提交申请");
        stage2.put("count", applications);
        stage2.put("conversionFromPrev", matchToApply);

        Map<String, Object> stage3 = new HashMap<>();
        stage3.put("name", "审核通过");
        stage3.put("count", approved);
        stage3.put("conversionFromPrev", applyToApprove);

        Map<String, Object> stage4 = new HashMap<>();
        stage4.put("name", "完成领养");
        stage4.put("count", completed);
        stage4.put("conversionFromPrev", approveToComplete);

        List<Map<String, Object>> stages = List.of(stage1, stage2, stage3, stage4);

        Map<String, Object> result = new HashMap<>();
        result.put("stages", stages);
        result.put("overallConversion", overallConversion);
        result.put("generatedAt", LocalDateTime.now().toString());
        return result;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
