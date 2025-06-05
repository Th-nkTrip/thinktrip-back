package com.thinktrip.thinktrip_api.domain.diary;

import com.thinktrip.thinktrip_api.domain.travel.TravelPlan;
import com.thinktrip.thinktrip_api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    // 여행 계획이 있는 경우만
    List<Diary> findAllByTravelPlan(TravelPlan travelPlan);

    // 특정 유저가 작성한 모든 다이어리 (travelPlan 유무 무관)
    List<Diary> findAllByUser(User user);

    // 특정 유저가 작성한 다이어리 중 여행 계획이 없는 것만
    List<Diary> findAllByUserAndTravelPlanIsNull(User user);
}
