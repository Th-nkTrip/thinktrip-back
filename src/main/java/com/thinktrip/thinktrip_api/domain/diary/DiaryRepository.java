package com.thinktrip.thinktrip_api.domain.diary;

import com.thinktrip.thinktrip_api.domain.diary.Diary;
import com.thinktrip.thinktrip_api.domain.travel.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findAllByTravelPlan(TravelPlan travelPlan);
    Optional<Diary> findByIdAndTravelPlan(Long id, TravelPlan travelPlan);
}
