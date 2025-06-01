package com.thinktrip.thinktrip_api.domain.travel;

import com.thinktrip.thinktrip_api.domain.travel.TravelPlan;
import com.thinktrip.thinktrip_api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    //List<TravelPlan> findAllByUser(User user);

    List<TravelPlan> findAllByUserIdAndIsGenerated(Long userId, boolean isGenerated);

    Optional<TravelPlan> findFirstByUserIdOrderByStartDateAsc(Long userId);

}
