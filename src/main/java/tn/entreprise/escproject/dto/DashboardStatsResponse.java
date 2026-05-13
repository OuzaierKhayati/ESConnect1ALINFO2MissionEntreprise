package tn.entreprise.escproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsResponse {

    private long totalUsers;
    private long activeUsers;
    private long pendingUsers;
    private long inactiveUsers;
    private Map<String, Long> usersByRole;
}
