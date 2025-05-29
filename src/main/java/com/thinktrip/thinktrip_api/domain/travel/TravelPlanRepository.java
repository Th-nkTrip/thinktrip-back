package com.thinktrip.thinktrip_api.domain.travel;

import com.thinktrip.thinktrip_api.domain.travel.TravelPlan;
import com.thinktrip.thinktrip_api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    //List<TravelPlan> findAllByUser(User user);

    List<TravelPlan> findAllByUserAndIsGenerated(User user, boolean isGenerated);

}
